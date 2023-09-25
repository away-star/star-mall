package star.xingxing.mall.controller.mall;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import star.xingxing.mall.common.Constants;
import star.xingxing.mall.controller.vo.StarMallCouponVO;
import star.xingxing.mall.controller.vo.StarMallUserVO;
import star.xingxing.mall.service.StarMallCouponService;
import star.xingxing.mall.util.PageQueryUtil;
import star.xingxing.mall.util.PageResult;
import star.xingxing.mall.util.Result;
import star.xingxing.mall.util.ResultGenerator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class CouponController {

    @Resource
    private StarMallCouponService starMallCouponService;

    @GetMapping("/couponList")
    public String couponList(HttpServletRequest request, HttpSession session) {
        Long userId = null;
        if (session.getAttribute(Constants.MALL_USER_SESSION_KEY) != null) {
            userId = ((StarMallUserVO) request.getSession().getAttribute(Constants.MALL_USER_SESSION_KEY)).getUserId();
        }
        List<StarMallCouponVO> coupons = starMallCouponService.selectAvailableCoupon(userId);
        request.setAttribute("coupons", coupons);
        return "mall/coupon-list";
    }

    @GetMapping("/myCoupons")
    public String myCoupons(@RequestParam Map<String, Object> params, HttpServletRequest request, HttpSession session) {
        StarMallUserVO user = (StarMallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        params.put("userId", user.getUserId());
        int status = Integer.parseInt((String) params.getOrDefault("status", "0"));
        params.put("status", status);
        if (StringUtils.isEmpty((CharSequence) params.get("page"))) {
            params.put("page", 1);
        }
        params.put("limit", Constants.MY_COUPONS_LIMIT);
        //封装我的订单数据
        PageQueryUtil pageUtil = new PageQueryUtil(params);

        PageResult<StarMallCouponVO> pageResult = starMallCouponService.selectMyCoupons(pageUtil);
        request.setAttribute("pageResult", pageResult);
        request.setAttribute("path", "myCoupons");
        request.setAttribute("status", status);
        return "mall/my-coupons";
    }

    @ResponseBody
    @PostMapping("coupon/{couponId}")
    public Result save(@PathVariable Long couponId, HttpSession session) {
        StarMallUserVO userVO = (StarMallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        return ResultGenerator.genDmlResult(starMallCouponService.saveCouponUser(couponId, userVO.getUserId()));
    }

    @ResponseBody
    @DeleteMapping("coupon/{couponUserId}")
    public Result delete(@PathVariable Long couponUserId) {
        return ResultGenerator.genDmlResult(starMallCouponService.deleteCouponUser(couponUserId));
    }
}
