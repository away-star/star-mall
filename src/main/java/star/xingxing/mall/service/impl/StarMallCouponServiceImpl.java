package star.xingxing.mall.service.impl;

import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import star.xingxing.mall.controller.vo.StarMallCouponVO;
import star.xingxing.mall.controller.vo.StarMallMyCouponVO;
import star.xingxing.mall.controller.vo.StarMallShoppingCartItemVO;
import star.xingxing.mall.dao.StarMallCouponMapper;
import star.xingxing.mall.dao.StarMallGoodsMapper;
import star.xingxing.mall.dao.StarMallUserCouponRecordMapper;
import star.xingxing.mall.entity.StarMallCoupon;
import star.xingxing.mall.entity.StarMallGoods;
import star.xingxing.mall.entity.StarMallUserCouponRecord;
import star.xingxing.mall.exception.StarMallException;
import star.xingxing.mall.service.StarMallCouponService;
import star.xingxing.mall.util.BeanUtil;
import star.xingxing.mall.util.PageQueryUtil;
import star.xingxing.mall.util.PageResult;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Service
public class StarMallCouponServiceImpl implements StarMallCouponService {

    @Resource
    private StarMallCouponMapper starMallCouponMapper;

    @Resource
    private StarMallUserCouponRecordMapper newBeeMallUserCouponRecordMapper;

    @Resource
    private StarMallGoodsMapper starMallGoodsMapper;

    @Override
    public PageResult getCouponPage(PageQueryUtil pageUtil) {
        List<StarMallCoupon> carousels = starMallCouponMapper.findCouponlList(pageUtil);
        int total = starMallCouponMapper.getTotalCoupons(pageUtil);
        return new PageResult(carousels, total, pageUtil.getLimit(), pageUtil.getPage());
    }

    @Override
    public boolean saveCoupon(StarMallCoupon starMallCoupon) {
        return starMallCouponMapper.insertSelective(starMallCoupon) > 0;
    }

    @Override
    public boolean updateCoupon(StarMallCoupon starMallCoupon) {
        return starMallCouponMapper.updateByPrimaryKeySelective(starMallCoupon) > 0;
    }

    @Override
    public StarMallCoupon getCouponById(Long id) {
        return starMallCouponMapper.selectByPrimaryKey(id);
    }

    @Override
    public boolean deleteCouponById(Long id) {
        return starMallCouponMapper.deleteByPrimaryKey(id) > 0;
    }

    @Override
    public List<StarMallCouponVO> selectAvailableCoupon(Long userId) {
        List<StarMallCoupon> coupons = starMallCouponMapper.selectAvailableCoupon();
        List<StarMallCouponVO> couponVOS = BeanUtil.copyList(coupons, StarMallCouponVO.class);
        for (StarMallCouponVO couponVO : couponVOS) {
            if (userId != null) {
                int num = newBeeMallUserCouponRecordMapper.getUserCouponCount(userId, couponVO.getCouponId());
                if (num > 0) {
                    couponVO.setHasReceived(true);
                }
            }
            if (couponVO.getCouponTotal() != 0) {
                int count = newBeeMallUserCouponRecordMapper.getCouponCount(couponVO.getCouponId());
                if (count >= couponVO.getCouponTotal()) {
                    couponVO.setSaleOut(true);
                }
            }
        }
        return couponVOS;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean saveCouponUser(Long couponId, Long userId) {
        StarMallCoupon starMallCoupon = starMallCouponMapper.selectByPrimaryKey(couponId);
        if (starMallCoupon.getCouponLimit() != 0) {
            int num = newBeeMallUserCouponRecordMapper.getUserCouponCount(userId, couponId);
            if (num != 0) {
                throw new StarMallException("优惠券已经领过了,无法再次领取！");
            }
        }
        if (starMallCoupon.getCouponTotal() != 0) {
            int count = newBeeMallUserCouponRecordMapper.getCouponCount(couponId);
            if (count >= starMallCoupon.getCouponTotal()) {
                throw new StarMallException("优惠券已经领完了！");
            }
            if (starMallCouponMapper.reduceCouponTotal(couponId) <= 0) {
                throw new StarMallException("优惠券领取失败！");
            }
        }
        StarMallUserCouponRecord couponUser = new StarMallUserCouponRecord();
        couponUser.setUserId(userId);
        couponUser.setCouponId(couponId);
        return newBeeMallUserCouponRecordMapper.insertSelective(couponUser) > 0;
    }

    @Override
    public PageResult<StarMallCouponVO> selectMyCoupons(PageQueryUtil pageUtil) {
        Integer total = newBeeMallUserCouponRecordMapper.countMyCoupons(pageUtil);
        List<StarMallCouponVO> couponVOS = new ArrayList<>();
        if (total > 0) {
            List<StarMallUserCouponRecord> userCouponRecords = newBeeMallUserCouponRecordMapper.selectMyCoupons(pageUtil);
            List<Long> couponIds = userCouponRecords.stream().map(StarMallUserCouponRecord::getCouponId).collect(toList());
            if (CollectionUtils.isNotEmpty(couponIds)) {
                List<StarMallCoupon> starMallCoupons = starMallCouponMapper.selectByIds(couponIds);
                Map<Long, StarMallCoupon> listMap = starMallCoupons.stream().collect(toMap(StarMallCoupon::getCouponId, newBeeMallCoupon -> newBeeMallCoupon));
                for (StarMallUserCouponRecord couponUser : userCouponRecords) {
                    StarMallCouponVO starMallCouponVO = new StarMallCouponVO();
                    StarMallCoupon starMallCoupon = listMap.getOrDefault(couponUser.getCouponId(), new StarMallCoupon());
                    BeanUtil.copyProperties(starMallCoupon, starMallCouponVO);
                    starMallCouponVO.setCouponUserId(couponUser.getCouponUserId());
                    starMallCouponVO.setUseStatus(couponUser.getUseStatus() == 1);
                    couponVOS.add(starMallCouponVO);
                }
            }
        }
        return new PageResult<>(couponVOS, total, pageUtil.getLimit(), pageUtil.getPage());
    }

    @Override
    public List<StarMallMyCouponVO> selectOrderCanUseCoupons(List<StarMallShoppingCartItemVO> myShoppingCartItems, int priceTotal, Long userId) {
        List<StarMallUserCouponRecord> couponUsers = newBeeMallUserCouponRecordMapper.selectMyAvailableCoupons(userId);
        List<StarMallMyCouponVO> myCouponVOS = BeanUtil.copyList(couponUsers, StarMallMyCouponVO.class);
        List<Long> couponIds = couponUsers.stream().map(StarMallUserCouponRecord::getCouponId).collect(toList());
        if (!couponIds.isEmpty()) {
            ZoneId zone = ZoneId.systemDefault();
            List<StarMallCoupon> coupons = starMallCouponMapper.selectByIds(couponIds);
            for (StarMallCoupon coupon : coupons) {
                for (StarMallMyCouponVO myCouponVO : myCouponVOS) {
                    if (coupon.getCouponId().equals(myCouponVO.getCouponId())) {
                        myCouponVO.setName(coupon.getCouponName());
                        myCouponVO.setCouponDesc(coupon.getCouponDesc());
                        myCouponVO.setDiscount(coupon.getDiscount());
                        myCouponVO.setMin(coupon.getMin());
                        myCouponVO.setGoodsType(coupon.getGoodsType());
                        myCouponVO.setGoodsValue(coupon.getGoodsValue());
                        ZonedDateTime startZonedDateTime = coupon.getCouponStartTime().atStartOfDay(zone);
                        ZonedDateTime endZonedDateTime = coupon.getCouponEndTime().atStartOfDay(zone);
                        myCouponVO.setStartTime(Date.from(startZonedDateTime.toInstant()));
                        myCouponVO.setEndTime(Date.from(endZonedDateTime.toInstant()));
                    }
                }
            }
        }
        long nowTime = System.currentTimeMillis();
        return myCouponVOS.stream().filter(item -> {
            // 判断有效期
            Date startTime = item.getStartTime();
            Date endTime = item.getEndTime();
            if (startTime == null || endTime == null || nowTime < startTime.getTime() || nowTime > endTime.getTime()) {
                return false;
            }
            // 判断使用条件
            boolean b = false;
            if (item.getMin() <= priceTotal) {
                if (item.getGoodsType() == 1) { // 指定分类可用
                    String[] split = item.getGoodsValue().split(",");
                    List<Long> goodsValue = Arrays.stream(split).map(Long::valueOf).toList();
                    List<Long> goodsIds = myShoppingCartItems.stream().map(StarMallShoppingCartItemVO::getGoodsId).collect(toList());
                    List<StarMallGoods> goods = starMallGoodsMapper.selectByPrimaryKeys(goodsIds);
                    List<Long> categoryIds = goods.stream().map(StarMallGoods::getGoodsCategoryId).toList();
                    for (Long categoryId : categoryIds) {
                        if (goodsValue.contains(categoryId)) {
                            b = true;
                            break;
                        }
                    }
                } else if (item.getGoodsType() == 2) { // 指定商品可用
                    String[] split = item.getGoodsValue().split(",");
                    List<Long> goodsValue = Arrays.stream(split).map(Long::valueOf).toList();
                    List<Long> goodsIds = myShoppingCartItems.stream().map(StarMallShoppingCartItemVO::getGoodsId).toList();
                    for (Long goodsId : goodsIds) {
                        if (goodsValue.contains(goodsId)) {
                            b = true;
                            break;
                        }
                    }
                } else { // 全场通用
                    b = true;
                }
            }
            return b;
        }).sorted(Comparator.comparingInt(StarMallMyCouponVO::getDiscount)).collect(toList());
    }

    @Override
    public boolean deleteCouponUser(Long couponUserId) {
        return newBeeMallUserCouponRecordMapper.deleteByPrimaryKey(couponUserId) > 0;
    }

    @Override
    public void releaseCoupon(Long orderId) {
        StarMallUserCouponRecord starMallUserCouponRecord = newBeeMallUserCouponRecordMapper.getUserCouponByOrderId(orderId);
        if (starMallUserCouponRecord == null) {
            return;
        }
        starMallUserCouponRecord.setUseStatus((byte) 0);
        starMallUserCouponRecord.setUpdateTime(new Date());
        newBeeMallUserCouponRecordMapper.updateByPrimaryKey(starMallUserCouponRecord);
    }
}
