package star.xingxing.mall.dao;

import star.xingxing.mall.entity.StarMallUserCouponRecord;
import star.xingxing.mall.util.PageQueryUtil;

import java.util.List;

public interface StarMallUserCouponRecordMapper {
    int deleteByPrimaryKey(Long couponUserId);

    int insert(StarMallUserCouponRecord record);

    int insertSelective(StarMallUserCouponRecord record);

    StarMallUserCouponRecord selectByPrimaryKey(Long couponUserId);

    int updateByPrimaryKeySelective(StarMallUserCouponRecord record);

    int updateByPrimaryKey(StarMallUserCouponRecord record);

    int getUserCouponCount(Long userId, Long couponId);

    int getCouponCount(Long couponId);

    List<StarMallUserCouponRecord> selectMyCoupons(PageQueryUtil pageQueryUtil);

    Integer countMyCoupons(PageQueryUtil pageQueryUtil);

    List<StarMallUserCouponRecord> selectMyAvailableCoupons(Long userId);

    StarMallUserCouponRecord getUserCouponByOrderId(Long orderId);
}
