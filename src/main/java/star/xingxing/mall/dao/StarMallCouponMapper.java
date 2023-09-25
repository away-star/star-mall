package star.xingxing.mall.dao;

import star.xingxing.mall.entity.StarMallCoupon;
import star.xingxing.mall.util.PageQueryUtil;

import java.util.List;

public interface StarMallCouponMapper {
    int deleteByPrimaryKey(Long couponId);

    int deleteBatch(Integer[] couponIds);

    int insert(StarMallCoupon record);

    int insertSelective(StarMallCoupon record);

    StarMallCoupon selectByPrimaryKey(Long couponId);

    int updateByPrimaryKeySelective(StarMallCoupon record);

    int updateByPrimaryKey(StarMallCoupon record);

    List<StarMallCoupon> findCouponlList(PageQueryUtil pageUtil);

    int getTotalCoupons(PageQueryUtil pageUtil);

    List<StarMallCoupon> selectAvailableCoupon();

    int reduceCouponTotal(Long couponId);

    List<StarMallCoupon> selectByIds(List<Long> couponIds);

    List<StarMallCoupon> selectAvailableGiveCoupon();

}
