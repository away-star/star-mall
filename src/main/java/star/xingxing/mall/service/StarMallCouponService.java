package star.xingxing.mall.service;

import star.xingxing.mall.controller.vo.StarMallCouponVO;
import star.xingxing.mall.controller.vo.StarMallMyCouponVO;
import star.xingxing.mall.controller.vo.StarMallShoppingCartItemVO;
import star.xingxing.mall.entity.StarMallCoupon;
import star.xingxing.mall.util.PageQueryUtil;
import star.xingxing.mall.util.PageResult;

import java.util.List;

public interface StarMallCouponService {

    PageResult getCouponPage(PageQueryUtil pageUtil);

    boolean saveCoupon(StarMallCoupon starMallCoupon);

    boolean updateCoupon(StarMallCoupon starMallCoupon);

    StarMallCoupon getCouponById(Long id);

    boolean deleteCouponById(Long id);

    /**
     * 查询可用优惠券
     *
     * @param userId
     * @return
     */
    List<StarMallCouponVO> selectAvailableCoupon(Long userId);

    /**
     * 用户领取优惠劵
     *
     * @param couponId 优惠劵ID
     * @param userId   用户ID
     * @return boolean
     */
    boolean saveCouponUser(Long couponId, Long userId);

    /**
     * 查询我的优惠券
     *
     * @param userId 用户ID
     * @return
     */
    PageResult<StarMallCouponVO> selectMyCoupons(PageQueryUtil pageQueryUtil);

    /**
     * 查询当前订单可用的优惠券
     *
     * @param myShoppingCartItems
     * @param priceTotal
     * @param userId
     * @return
     */
    List<StarMallMyCouponVO> selectOrderCanUseCoupons(List<StarMallShoppingCartItemVO> myShoppingCartItems, int priceTotal, Long userId);

    /**
     * 删除用户优惠券
     *
     * @param couponUserId
     * @return
     */
    boolean deleteCouponUser(Long couponUserId);

    /**
     * 回复未支付的优惠券
     * @param orderId
     */
    void releaseCoupon(Long orderId);

}
