package star.xingxing.mall.controller.admin;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import star.xingxing.mall.entity.StarMallCoupon;
import star.xingxing.mall.service.StarMallCouponService;
import star.xingxing.mall.util.PageQueryUtil;
import star.xingxing.mall.util.Result;
import star.xingxing.mall.util.ResultGenerator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

@Controller
@RequestMapping("admin")
public class StarMallCouponController {

    @Resource
    private StarMallCouponService starMallCouponService;

    @GetMapping("/coupon")
    public String index(HttpServletRequest request) {
        request.setAttribute("path", "newbee_mall_coupon");
        return "admin/newbee_mall_coupon";
    }

    @ResponseBody
    @GetMapping("/coupon/list")
    public Result list(@RequestParam Map<String, Object> params) {
        if (StringUtils.isEmpty((CharSequence) params.get("page")) || StringUtils.isEmpty((CharSequence) params.get("limit"))) {
            return ResultGenerator.genFailResult("参数异常！");
        }
        PageQueryUtil pageUtil = new PageQueryUtil(params);
        return ResultGenerator.genSuccessResult(starMallCouponService.getCouponPage(pageUtil));
    }

    /**
     * 保存
     */
    @ResponseBody
    @PostMapping("/coupon/save")
    public Result save(@RequestBody StarMallCoupon starMallCoupon) {
        return ResultGenerator.genDmlResult(starMallCouponService.saveCoupon(starMallCoupon));
    }

    /**
     * 更新
     */
    @PostMapping("/coupon/update")
    @ResponseBody
    public Result update(@RequestBody StarMallCoupon starMallCoupon) {
        starMallCoupon.setUpdateTime(new Date());
        return ResultGenerator.genDmlResult(starMallCouponService.updateCoupon(starMallCoupon));
    }

    /**
     * 详情
     */
    @GetMapping("/coupon/{id}")
    @ResponseBody
    public Result Info(@PathVariable("id") Long id) {
        StarMallCoupon starMallCoupon = starMallCouponService.getCouponById(id);
        return ResultGenerator.genSuccessResult(starMallCoupon);
    }

    /**
     * 删除
     */
    @DeleteMapping("/coupon/{id}")
    @ResponseBody
    public Result delete(@PathVariable Long id) {
        return ResultGenerator.genDmlResult(starMallCouponService.deleteCouponById(id));
    }
}
