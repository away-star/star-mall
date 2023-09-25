
package star.xingxing.mall.service;

import star.xingxing.mall.controller.vo.StarMallOrderDetailVO;
import star.xingxing.mall.controller.vo.StarMallOrderItemVO;
import star.xingxing.mall.controller.vo.StarMallShoppingCartItemVO;
import star.xingxing.mall.controller.vo.StarMallUserVO;
import star.xingxing.mall.entity.StarMallOrder;
import star.xingxing.mall.util.PageQueryUtil;
import star.xingxing.mall.util.PageResult;

import java.util.List;

public interface StarMallOrderService {
    /**
     * 后台分页
     *
     * @param pageUtil
     * @return
     */
    PageResult<StarMallOrder> getNewBeeMallOrdersPage(PageQueryUtil pageUtil);

    /**
     * 订单信息修改
     *
     * @param starMallOrder
     * @return
     */
    String updateOrderInfo(StarMallOrder starMallOrder);

    /**
     * 根据主键修改订单信息
     *
     * @param starMallOrder
     * @return
     */
    boolean updateByPrimaryKeySelective(StarMallOrder starMallOrder);

    /**
     * 配货
     *
     * @param ids
     * @return
     */
    String checkDone(Long[] ids);

    /**
     * 出库
     *
     * @param ids
     * @return
     */
    String checkOut(Long[] ids);

    /**
     * 关闭订单
     *
     * @param ids
     * @return
     */
    String closeOrder(Long[] ids);

    /**
     * 保存订单
     *
     * @param user
     * @param couponUserId
     * @param myShoppingCartItems
     * @return
     */
    String saveOrder(StarMallUserVO user, Long couponUserId, List<StarMallShoppingCartItemVO> myShoppingCartItems);

    /**
     * 生成秒杀订单
     *
     * @param seckillSuccessId
     * @param userId
     * @return
     */
    String seckillSaveOrder(Long seckillSuccessId, Long userId);

    /**
     * 获取订单详情
     *
     * @param orderNo
     * @param userId
     * @return
     */
    StarMallOrderDetailVO getOrderDetailByOrderNo(String orderNo, Long userId);

    /**
     * 获取订单详情
     *
     * @param orderNo
     * @return
     */
    StarMallOrder getNewBeeMallOrderByOrderNo(String orderNo);

    /**
     * 我的订单列表
     *
     * @param pageUtil
     * @return
     */
    PageResult getMyOrders(PageQueryUtil pageUtil);

    /**
     * 手动取消订单
     *
     * @param orderNo
     * @param userId
     * @return
     */
    String cancelOrder(String orderNo, Long userId);

    /**
     * 确认收货
     *
     * @param orderNo
     * @param userId
     * @return
     */
    String finishOrder(String orderNo, Long userId);

    String paySuccess(String orderNo, int payType);

    List<StarMallOrderItemVO> getOrderItems(Long id);
}
