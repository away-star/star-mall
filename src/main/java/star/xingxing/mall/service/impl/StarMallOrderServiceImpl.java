
package star.xingxing.mall.service.impl;

import jakarta.annotation.Resource;
import star.xingxing.mall.common.*;
import star.xingxing.mall.config.ProjectConfig;
import star.xingxing.mall.controller.vo.*;
import star.xingxing.mall.dao.*;
import star.xingxing.mall.entity.*;
import star.xingxing.mall.exception.StarMallException;
import star.xingxing.mall.service.StarMallOrderService;
import star.xingxing.mall.task.OrderUnPaidTask;
import star.xingxing.mall.task.TaskService;
import star.xingxing.mall.util.BeanUtil;
import star.xingxing.mall.util.NumberUtil;
import star.xingxing.mall.util.PageQueryUtil;
import star.xingxing.mall.util.PageResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
public class StarMallOrderServiceImpl implements StarMallOrderService {

    @Resource
    private StarMallOrderMapper starMallOrderMapper;
    @Resource
    private StarMallOrderItemMapper starMallOrderItemMapper;
    @Resource
    private StarMallShoppingCartItemMapper starMallShoppingCartItemMapper;
    @Resource
    private StarMallGoodsMapper starMallGoodsMapper;
    @Resource
    private StarMallUserCouponRecordMapper newBeeMallUserCouponRecordMapper;
    @Resource
    private StarMallCouponMapper starMallCouponMapper;
    @Resource
    private StarMallSeckillMapper starMallSeckillMapper;
    @Resource
    private StarMallSeckillSuccessMapper starMallSeckillSuccessMapper;
    @Resource
    private TaskService taskService;

    @Override
    public PageResult<StarMallOrder> getNewBeeMallOrdersPage(PageQueryUtil pageUtil) {
        int total = starMallOrderMapper.getTotalNewBeeMallOrders(pageUtil);
        List<StarMallOrder> starMallOrders = starMallOrderMapper.findNewBeeMallOrderList(pageUtil);
        return new PageResult<>(starMallOrders, total, pageUtil.getLimit(), pageUtil.getPage());
    }

    @Override
    @Transactional
    public String updateOrderInfo(StarMallOrder starMallOrder) {
        StarMallOrder temp = starMallOrderMapper.selectByPrimaryKey(starMallOrder.getOrderId());
        // 不为空且orderStatus>=0且状态为出库之前可以修改部分信息
        if (temp != null && temp.getOrderStatus() >= 0 && temp.getOrderStatus() < 3) {
            temp.setTotalPrice(starMallOrder.getTotalPrice());
            temp.setUserAddress(starMallOrder.getUserAddress());
            temp.setUpdateTime(new Date());
            if (starMallOrderMapper.updateByPrimaryKeySelective(temp) > 0) {
                return ServiceResultEnum.SUCCESS.getResult();
            }
            return ServiceResultEnum.DB_ERROR.getResult();
        }
        return ServiceResultEnum.DATA_NOT_EXIST.getResult();
    }

    @Override
    public boolean updateByPrimaryKeySelective(StarMallOrder starMallOrder) {
        return starMallOrderMapper.updateByPrimaryKeySelective(starMallOrder) > 0;
    }

    @Override
    @Transactional
    public String checkDone(Long[] ids) {
        // 查询所有的订单 判断状态 修改状态和更新时间
        List<StarMallOrder> orders = starMallOrderMapper.selectByPrimaryKeys(Arrays.asList(ids));
        StringBuilder errorOrderNos = new StringBuilder();
        if (!CollectionUtils.isEmpty(orders)) {
            for (StarMallOrder starMallOrder : orders) {
                if (starMallOrder.getIsDeleted() == 1) {
                    errorOrderNos.append(starMallOrder.getOrderNo()).append(" ");
                    continue;
                }
                if (starMallOrder.getOrderStatus() != 1) {
                    errorOrderNos.append(starMallOrder.getOrderNo()).append(" ");
                }
            }
            if (StringUtils.isEmpty(errorOrderNos.toString())) {
                // 订单状态正常 可以执行配货完成操作 修改订单状态和更新时间
                if (starMallOrderMapper.checkDone(Arrays.asList(ids)) > 0) {
                    return ServiceResultEnum.SUCCESS.getResult();
                } else {
                    return ServiceResultEnum.DB_ERROR.getResult();
                }
            } else {
                // 订单此时不可执行出库操作
                if (errorOrderNos.length() > 0 && errorOrderNos.length() < 100) {
                    return errorOrderNos + "订单的状态不是支付成功无法执行出库操作";
                } else {
                    return "你选择了太多状态不是支付成功的订单，无法执行配货完成操作";
                }
            }
        }
        // 未查询到数据 返回错误提示
        return ServiceResultEnum.DATA_NOT_EXIST.getResult();
    }

    @Override
    @Transactional
    public String checkOut(Long[] ids) {
        // 查询所有的订单 判断状态 修改状态和更新时间
        List<StarMallOrder> orders = starMallOrderMapper.selectByPrimaryKeys(Arrays.asList(ids));
        StringBuilder errorOrderNos = new StringBuilder();
        if (!CollectionUtils.isEmpty(orders)) {
            for (StarMallOrder starMallOrder : orders) {
                if (starMallOrder.getIsDeleted() == 1) {
                    errorOrderNos.append(starMallOrder.getOrderNo()).append(" ");
                    continue;
                }
                if (starMallOrder.getOrderStatus() != 1 && starMallOrder.getOrderStatus() != 2) {
                    errorOrderNos.append(starMallOrder.getOrderNo()).append(" ");
                }
            }
            if (StringUtils.isEmpty(errorOrderNos.toString())) {
                // 订单状态正常 可以执行出库操作 修改订单状态和更新时间
                if (starMallOrderMapper.checkOut(Arrays.asList(ids)) > 0) {
                    return ServiceResultEnum.SUCCESS.getResult();
                } else {
                    return ServiceResultEnum.DB_ERROR.getResult();
                }
            } else {
                // 订单此时不可执行出库操作
                if (errorOrderNos.length() > 0 && errorOrderNos.length() < 100) {
                    return errorOrderNos + "订单的状态不是支付成功或配货完成无法执行出库操作";
                } else {
                    return "你选择了太多状态不是支付成功或配货完成的订单，无法执行出库操作";
                }
            }
        }
        // 未查询到数据 返回错误提示
        return ServiceResultEnum.DATA_NOT_EXIST.getResult();
    }

    @Override
    @Transactional
    public String closeOrder(Long[] ids) {
        // 查询所有的订单 判断状态 修改状态和更新时间
        List<StarMallOrder> orders = starMallOrderMapper.selectByPrimaryKeys(Arrays.asList(ids));
        StringBuilder errorOrderNos = new StringBuilder();
        if (!CollectionUtils.isEmpty(orders)) {
            for (StarMallOrder starMallOrder : orders) {
                // isDeleted=1 一定为已关闭订单
                if (starMallOrder.getIsDeleted() == 1) {
                    errorOrderNos.append(starMallOrder.getOrderNo()).append(" ");
                    continue;
                }
                // 已关闭或者已完成无法关闭订单
                if (starMallOrder.getOrderStatus() == 4 || starMallOrder.getOrderStatus() < 0) {
                    errorOrderNos.append(starMallOrder.getOrderNo()).append(" ");
                }
            }
            if (StringUtils.isEmpty(errorOrderNos.toString())) {
                // 订单状态正常 可以执行关闭操作 修改订单状态和更新时间
                if (starMallOrderMapper.closeOrder(Arrays.asList(ids), StarMallOrderStatusEnum.ORDER_CLOSED_BY_JUDGE.getOrderStatus()) > 0) {
                    return ServiceResultEnum.SUCCESS.getResult();
                } else {
                    return ServiceResultEnum.DB_ERROR.getResult();
                }
            } else {
                // 订单此时不可执行关闭操作
                if (errorOrderNos.length() > 0 && errorOrderNos.length() < 100) {
                    return errorOrderNos + "订单不能执行关闭操作";
                } else {
                    return "你选择的订单不能执行关闭操作";
                }
            }
        }
        // 未查询到数据 返回错误提示
        return ServiceResultEnum.DATA_NOT_EXIST.getResult();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveOrder(StarMallUserVO user, Long couponUserId, List<StarMallShoppingCartItemVO> myShoppingCartItems) {
        List<Long> itemIdList = myShoppingCartItems.stream().map(StarMallShoppingCartItemVO::getCartItemId).collect(Collectors.toList());
        List<Long> goodsIds = myShoppingCartItems.stream().map(StarMallShoppingCartItemVO::getGoodsId).collect(Collectors.toList());
        List<StarMallGoods> starMallGoods = starMallGoodsMapper.selectByPrimaryKeys(goodsIds);
        // 检查是否包含已下架商品
        List<StarMallGoods> goodsListNotSelling = starMallGoods.stream()
                .filter(newBeeMallGoodsTemp -> newBeeMallGoodsTemp.getGoodsSellStatus() != Constants.SELL_STATUS_UP)
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(goodsListNotSelling)) {
            // goodsListNotSelling 对象非空则表示有下架商品
            StarMallException.fail(goodsListNotSelling.get(0).getGoodsName() + "已下架，无法生成订单");
        }
        Map<Long, StarMallGoods> newBeeMallGoodsMap = starMallGoods.stream().collect(Collectors.toMap(StarMallGoods::getGoodsId, Function.identity(), (entity1, entity2) -> entity1));
        // 判断商品库存
        for (StarMallShoppingCartItemVO shoppingCartItemVO : myShoppingCartItems) {
            // 查出的商品中不存在购物车中的这条关联商品数据，直接返回错误提醒
            if (!newBeeMallGoodsMap.containsKey(shoppingCartItemVO.getGoodsId())) {
                StarMallException.fail(ServiceResultEnum.SHOPPING_ITEM_ERROR.getResult());
            }
            // 存在数量大于库存的情况，直接返回错误提醒
            if (shoppingCartItemVO.getGoodsCount() > newBeeMallGoodsMap.get(shoppingCartItemVO.getGoodsId()).getStockNum()) {
                StarMallException.fail(ServiceResultEnum.SHOPPING_ITEM_COUNT_ERROR.getResult());
            }
        }
        if (CollectionUtils.isEmpty(itemIdList) || CollectionUtils.isEmpty(goodsIds) || CollectionUtils.isEmpty(starMallGoods)) {
            StarMallException.fail(ServiceResultEnum.ORDER_GENERATE_ERROR.getResult());
        }
        if (starMallShoppingCartItemMapper.deleteBatch(itemIdList) <= 0) {
            StarMallException.fail(ServiceResultEnum.DB_ERROR.getResult());
        }
        List<StockNumDTO> stockNumDTOS = BeanUtil.copyList(myShoppingCartItems, StockNumDTO.class);
        int updateStockNumResult = starMallGoodsMapper.updateStockNum(stockNumDTOS);
        if (updateStockNumResult < 1) {
            StarMallException.fail(ServiceResultEnum.SHOPPING_ITEM_COUNT_ERROR.getResult());
        }
        // 生成订单号
        String orderNo = NumberUtil.genOrderNo();
        int priceTotal = 0;
        // 保存订单
        StarMallOrder starMallOrder = new StarMallOrder();
        starMallOrder.setOrderNo(orderNo);
        starMallOrder.setUserId(user.getUserId());
        starMallOrder.setUserAddress(user.getAddress());
        // 总价
        for (StarMallShoppingCartItemVO starMallShoppingCartItemVO : myShoppingCartItems) {
            priceTotal += starMallShoppingCartItemVO.getGoodsCount() * starMallShoppingCartItemVO.getSellingPrice();
        }
        // 如果使用了优惠券
        if (couponUserId != null) {
            StarMallUserCouponRecord starMallUserCouponRecord = newBeeMallUserCouponRecordMapper.selectByPrimaryKey(couponUserId);
            Long userId = starMallUserCouponRecord.getUserId();
            if (!Objects.equals(userId, user.getUserId())) {
                StarMallException.fail("优惠卷所属用户与当前用户不一致！");
            }
            Long couponId = starMallUserCouponRecord.getCouponId();
            StarMallCoupon starMallCoupon = starMallCouponMapper.selectByPrimaryKey(couponId);
            priceTotal -= starMallCoupon.getDiscount();
        }
        if (priceTotal < 1) {
            StarMallException.fail(ServiceResultEnum.ORDER_PRICE_ERROR.getResult());
        }
        starMallOrder.setTotalPrice(priceTotal);
        String extraInfo = "starMall-plus支付宝沙箱支付";
        starMallOrder.setExtraInfo(extraInfo);
        // 生成订单项并保存订单项纪录
        if (starMallOrderMapper.insertSelective(starMallOrder) <= 0) {
            StarMallException.fail(ServiceResultEnum.DB_ERROR.getResult());
        }
        // 如果使用了优惠券，则更新优惠券状态
        if (couponUserId != null) {
            StarMallUserCouponRecord couponUser = new StarMallUserCouponRecord();
            couponUser.setCouponUserId(couponUserId);
            couponUser.setOrderId(starMallOrder.getOrderId());
            couponUser.setUseStatus((byte) 1);
            couponUser.setUsedTime(new Date());
            couponUser.setUpdateTime(new Date());
            newBeeMallUserCouponRecordMapper.updateByPrimaryKeySelective(couponUser);
        }
        // 生成所有的订单项快照，并保存至数据库
        List<StarMallOrderItem> starMallOrderItems = new ArrayList<>();
        for (StarMallShoppingCartItemVO starMallShoppingCartItemVO : myShoppingCartItems) {
            StarMallOrderItem starMallOrderItem = new StarMallOrderItem();
            // 使用BeanUtil工具类将newBeeMallShoppingCartItemVO中的属性复制到newBeeMallOrderItem对象中
            BeanUtil.copyProperties(starMallShoppingCartItemVO, starMallOrderItem);
            // NewBeeMallOrderMapper文件insert()方法中使用了useGeneratedKeys因此orderId可以获取到
            starMallOrderItem.setOrderId(starMallOrder.getOrderId());
            starMallOrderItems.add(starMallOrderItem);
        }
        // 保存至数据库
        if (starMallOrderItemMapper.insertBatch(starMallOrderItems) <= 0) {
            StarMallException.fail(ServiceResultEnum.DB_ERROR.getResult());
        }
        // 订单支付超期任务，超过300秒自动取消订单
        taskService.addTask(new OrderUnPaidTask(starMallOrder.getOrderId(), ProjectConfig.getOrderUnpaidOverTime() * 1000));
        // 所有操作成功后，将订单号返回，以供Controller方法跳转到订单详情
        return orderNo;
    }

    @Override
    public String seckillSaveOrder(Long seckillSuccessId, Long userId) {
        StarMallSeckillSuccess starMallSeckillSuccess = starMallSeckillSuccessMapper.selectByPrimaryKey(seckillSuccessId);
        if (!starMallSeckillSuccess.getUserId().equals(userId)) {
            throw new StarMallException("当前登陆用户与抢购秒杀商品的用户不匹配");
        }
        Long seckillId = starMallSeckillSuccess.getSeckillId();
        StarMallSeckill starMallSeckill = starMallSeckillMapper.selectByPrimaryKey(seckillId);
        Long goodsId = starMallSeckill.getGoodsId();
        StarMallGoods starMallGoods = starMallGoodsMapper.selectByPrimaryKey(goodsId);
        // 生成订单号
        String orderNo = NumberUtil.genOrderNo();
        // 保存订单
        StarMallOrder starMallOrder = new StarMallOrder();
        starMallOrder.setOrderNo(orderNo);
        starMallOrder.setTotalPrice(starMallSeckill.getSeckillPrice());
        starMallOrder.setUserId(userId);
        starMallOrder.setUserAddress("秒杀测试地址");
        starMallOrder.setOrderStatus((byte) StarMallOrderStatusEnum.ORDER_PAID.getOrderStatus());
        starMallOrder.setPayStatus((byte) PayStatusEnum.PAY_SUCCESS.getPayStatus());
        starMallOrder.setPayType((byte) PayTypeEnum.WEIXIN_PAY.getPayType());
        starMallOrder.setPayTime(new Date());
        String extraInfo = "";
        starMallOrder.setExtraInfo(extraInfo);
        if (starMallOrderMapper.insertSelective(starMallOrder) <= 0) {
            throw new StarMallException("生成订单内部异常");
        }
        // 保存订单商品项
        StarMallOrderItem starMallOrderItem = new StarMallOrderItem();
        Long orderId = starMallOrder.getOrderId();
        starMallOrderItem.setOrderId(orderId);
        starMallOrderItem.setSeckillId(seckillId);
        starMallOrderItem.setGoodsId(starMallGoods.getGoodsId());
        starMallOrderItem.setGoodsCoverImg(starMallGoods.getGoodsCoverImg());
        starMallOrderItem.setGoodsName(starMallGoods.getGoodsName());
        starMallOrderItem.setGoodsCount(1);
        starMallOrderItem.setSellingPrice(starMallSeckill.getSeckillPrice());
        if (starMallOrderItemMapper.insert(starMallOrderItem) <= 0) {
            throw new StarMallException("生成订单内部异常");
        }
        // 订单支付超期任务
        taskService.addTask(new OrderUnPaidTask(starMallOrder.getOrderId(), 30 * 1000));
        return orderNo;
    }

    @Override
    public StarMallOrderDetailVO getOrderDetailByOrderNo(String orderNo, Long userId) {
        StarMallOrder starMallOrder = starMallOrderMapper.selectByOrderNo(orderNo);
        if (starMallOrder == null) {
            StarMallException.fail(ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult());
        }
        //验证是否是当前userId下的订单，否则报错
        if (!userId.equals(starMallOrder.getUserId())) {
            StarMallException.fail(ServiceResultEnum.NO_PERMISSION_ERROR.getResult());
        }
        List<StarMallOrderItem> orderItems = starMallOrderItemMapper.selectByOrderId(starMallOrder.getOrderId());
        //获取订单项数据
        if (CollectionUtils.isEmpty(orderItems)) {
            StarMallException.fail(ServiceResultEnum.ORDER_ITEM_NOT_EXIST_ERROR.getResult());
        }
        List<StarMallOrderItemVO> starMallOrderItemVOS = BeanUtil.copyList(orderItems, StarMallOrderItemVO.class);
        StarMallOrderDetailVO starMallOrderDetailVO = new StarMallOrderDetailVO();
        BeanUtil.copyProperties(starMallOrder, starMallOrderDetailVO);
        starMallOrderDetailVO.setOrderStatusString(StarMallOrderStatusEnum.getNewBeeMallOrderStatusEnumByStatus(starMallOrderDetailVO.getOrderStatus()).getName());
        starMallOrderDetailVO.setPayTypeString(PayTypeEnum.getPayTypeEnumByType(starMallOrderDetailVO.getPayType()).getName());
        starMallOrderDetailVO.setStarMallOrderItemVOS(starMallOrderItemVOS);
        return starMallOrderDetailVO;
    }

    @Override
    public StarMallOrder getNewBeeMallOrderByOrderNo(String orderNo) {
        return starMallOrderMapper.selectByOrderNo(orderNo);
    }

    @Override
    public PageResult getMyOrders(PageQueryUtil pageUtil) {
        int total = starMallOrderMapper.getTotalNewBeeMallOrders(pageUtil);
        List<StarMallOrderListVO> orderListVOS = new ArrayList<>();
        if (total > 0) {
            List<StarMallOrder> starMallOrders = starMallOrderMapper.findNewBeeMallOrderList(pageUtil);
            // 数据转换 将实体类转成vo
            orderListVOS = BeanUtil.copyList(starMallOrders, StarMallOrderListVO.class);
            // 设置订单状态中文显示值
            for (StarMallOrderListVO starMallOrderListVO : orderListVOS) {
                starMallOrderListVO.setOrderStatusString(StarMallOrderStatusEnum.getNewBeeMallOrderStatusEnumByStatus(starMallOrderListVO.getOrderStatus()).getName());
            }
            List<Long> orderIds = starMallOrders.stream().map(StarMallOrder::getOrderId).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(orderIds)) {
                List<StarMallOrderItem> orderItems = starMallOrderItemMapper.selectByOrderIds(orderIds);
                Map<Long, List<StarMallOrderItem>> itemByOrderIdMap = orderItems.stream().collect(groupingBy(StarMallOrderItem::getOrderId));
                for (StarMallOrderListVO starMallOrderListVO : orderListVOS) {
                    // 封装每个订单列表对象的订单项数据
                    if (itemByOrderIdMap.containsKey(starMallOrderListVO.getOrderId())) {
                        List<StarMallOrderItem> orderItemListTemp = itemByOrderIdMap.get(starMallOrderListVO.getOrderId());
                        // 将NewBeeMallOrderItem对象列表转换成NewBeeMallOrderItemVO对象列表
                        List<StarMallOrderItemVO> starMallOrderItemVOS = BeanUtil.copyList(orderItemListTemp, StarMallOrderItemVO.class);
                        starMallOrderListVO.setStarMallOrderItemVOS(starMallOrderItemVOS);
                    }
                }
            }
        }
        return new PageResult(orderListVOS, total, pageUtil.getLimit(), pageUtil.getPage());
    }

    @Override
    public String cancelOrder(String orderNo, Long userId) {
        StarMallOrder starMallOrder = starMallOrderMapper.selectByOrderNo(orderNo);
        if (starMallOrder != null) {
            // 验证是否是当前userId下的订单，否则报错
            if (!userId.equals(starMallOrder.getUserId())) {
                StarMallException.fail(ServiceResultEnum.NO_PERMISSION_ERROR.getResult());
            }
            // 订单状态判断
            if (starMallOrder.getOrderStatus().intValue() == StarMallOrderStatusEnum.ORDER_SUCCESS.getOrderStatus()
                    || starMallOrder.getOrderStatus().intValue() == StarMallOrderStatusEnum.ORDER_CLOSED_BY_MALLUSER.getOrderStatus()
                    || starMallOrder.getOrderStatus().intValue() == StarMallOrderStatusEnum.ORDER_CLOSED_BY_EXPIRED.getOrderStatus()
                    || starMallOrder.getOrderStatus().intValue() == StarMallOrderStatusEnum.ORDER_CLOSED_BY_JUDGE.getOrderStatus()) {
                return ServiceResultEnum.ORDER_STATUS_ERROR.getResult();
            }
            if (starMallOrderMapper.closeOrder(Collections.singletonList(starMallOrder.getOrderId()), StarMallOrderStatusEnum.ORDER_CLOSED_BY_MALLUSER.getOrderStatus()) > 0) {
                return ServiceResultEnum.SUCCESS.getResult();
            } else {
                return ServiceResultEnum.DB_ERROR.getResult();
            }
        }
        return ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult();
    }

    @Override
    public String finishOrder(String orderNo, Long userId) {
        StarMallOrder starMallOrder = starMallOrderMapper.selectByOrderNo(orderNo);
        if (starMallOrder != null) {
            // 验证是否是当前userId下的订单，否则报错
            if (!userId.equals(starMallOrder.getUserId())) {
                return ServiceResultEnum.NO_PERMISSION_ERROR.getResult();
            }
            // 订单状态判断 非出库状态下不进行修改操作
            if (starMallOrder.getOrderStatus().intValue() != StarMallOrderStatusEnum.ORDER_EXPRESS.getOrderStatus()) {
                return ServiceResultEnum.ORDER_STATUS_ERROR.getResult();
            }
            starMallOrder.setOrderStatus((byte) StarMallOrderStatusEnum.ORDER_SUCCESS.getOrderStatus());
            starMallOrder.setUpdateTime(new Date());
            if (starMallOrderMapper.updateByPrimaryKeySelective(starMallOrder) > 0) {
                return ServiceResultEnum.SUCCESS.getResult();
            } else {
                return ServiceResultEnum.DB_ERROR.getResult();
            }
        }
        return ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult();
    }

    @Override
    public String paySuccess(String orderNo, int payType) {
        StarMallOrder starMallOrder = starMallOrderMapper.selectByOrderNo(orderNo);
        if (starMallOrder == null) {
            return ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult();
        }
        // 订单状态判断 非待支付状态下不进行修改操作
        if (starMallOrder.getOrderStatus().intValue() != StarMallOrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()) {
            return ServiceResultEnum.ORDER_STATUS_ERROR.getResult();
        }
        starMallOrder.setOrderStatus((byte) StarMallOrderStatusEnum.ORDER_PAID.getOrderStatus());
        starMallOrder.setPayType((byte) payType);
        starMallOrder.setPayStatus((byte) PayStatusEnum.PAY_SUCCESS.getPayStatus());
        starMallOrder.setPayTime(new Date());
        starMallOrder.setUpdateTime(new Date());
        if (starMallOrderMapper.updateByPrimaryKeySelective(starMallOrder) <= 0) {
            return ServiceResultEnum.DB_ERROR.getResult();
        }
        taskService.removeTask(new OrderUnPaidTask(starMallOrder.getOrderId()));
        return ServiceResultEnum.SUCCESS.getResult();
    }

    @Override
    public List<StarMallOrderItemVO> getOrderItems(Long id) {
        StarMallOrder starMallOrder = starMallOrderMapper.selectByPrimaryKey(id);
        if (starMallOrder != null) {
            List<StarMallOrderItem> orderItems = starMallOrderItemMapper.selectByOrderId(starMallOrder.getOrderId());
            // 获取订单项数据
            if (!CollectionUtils.isEmpty(orderItems)) {
                return BeanUtil.copyList(orderItems, StarMallOrderItemVO.class);
            }
        }
        return null;
    }
}
