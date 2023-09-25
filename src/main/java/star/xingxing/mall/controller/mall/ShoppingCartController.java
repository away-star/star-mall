
package star.xingxing.mall.controller.mall;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import star.xingxing.mall.common.Constants;
import star.xingxing.mall.exception.StarMallException;
import star.xingxing.mall.common.ServiceResultEnum;
import star.xingxing.mall.controller.vo.StarMallMyCouponVO;
import star.xingxing.mall.controller.vo.StarMallShoppingCartItemVO;
import star.xingxing.mall.controller.vo.StarMallUserVO;
import star.xingxing.mall.entity.StarMallShoppingCartItem;
import star.xingxing.mall.service.StarMallCouponService;
import star.xingxing.mall.service.StarMallShoppingCartService;
import star.xingxing.mall.util.Result;
import star.xingxing.mall.util.ResultGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ShoppingCartController {

    @Resource
    private StarMallShoppingCartService starMallShoppingCartService;

    @Resource
    private StarMallCouponService starMallCouponService;

    @GetMapping("/shop-cart")
    public String cartListPage(HttpServletRequest request,
                               HttpSession httpSession) {
        StarMallUserVO user = (StarMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        int itemsTotal = 0;
        int priceTotal = 0;
        List<StarMallShoppingCartItemVO> myShoppingCartItems = starMallShoppingCartService.getMyShoppingCartItems(user.getUserId());
        if (!CollectionUtils.isEmpty(myShoppingCartItems)) {
            //购物项总数
            itemsTotal = myShoppingCartItems.stream().mapToInt(StarMallShoppingCartItemVO::getGoodsCount).sum();
            if (itemsTotal < 1) {
                StarMallException.fail("购物项不能为空");
            }
            //总价
            for (StarMallShoppingCartItemVO starMallShoppingCartItemVO : myShoppingCartItems) {
                priceTotal += starMallShoppingCartItemVO.getGoodsCount() * starMallShoppingCartItemVO.getSellingPrice();
            }
            if (priceTotal < 1) {
                StarMallException.fail("购物项价格异常");
            }
        }
        request.setAttribute("itemsTotal", itemsTotal);
        request.setAttribute("priceTotal", priceTotal);
        request.setAttribute("myShoppingCartItems", myShoppingCartItems);
        return "mall/cart";
    }

    @PostMapping("/shop-cart")
    @ResponseBody
    public Result saveNewBeeMallShoppingCartItem(@RequestBody StarMallShoppingCartItem starMallShoppingCartItem,
                                                 HttpSession httpSession) {
        StarMallUserVO user = (StarMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        starMallShoppingCartItem.setUserId(user.getUserId());
        String saveResult = starMallShoppingCartService.saveNewBeeMallCartItem(starMallShoppingCartItem);
        //添加成功
        if (ServiceResultEnum.SUCCESS.getResult().equals(saveResult)) {
            return ResultGenerator.genSuccessResult();
        }
        //添加失败
        return ResultGenerator.genFailResult(saveResult);
    }

    @PutMapping("/shop-cart")
    @ResponseBody
    public Result updateNewBeeMallShoppingCartItem(@RequestBody StarMallShoppingCartItem starMallShoppingCartItem,
                                                   HttpSession httpSession) {
        StarMallUserVO user = (StarMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        starMallShoppingCartItem.setUserId(user.getUserId());
        String updateResult = starMallShoppingCartService.updateNewBeeMallCartItem(starMallShoppingCartItem);
        //修改成功
        if (ServiceResultEnum.SUCCESS.getResult().equals(updateResult)) {
            return ResultGenerator.genSuccessResult();
        }
        //修改失败
        return ResultGenerator.genFailResult(updateResult);
    }

    @DeleteMapping("/shop-cart/{newBeeMallShoppingCartItemId}")
    @ResponseBody
    public Result updateNewBeeMallShoppingCartItem(@PathVariable("newBeeMallShoppingCartItemId") Long newBeeMallShoppingCartItemId,
                                                   HttpSession httpSession) {
        StarMallUserVO user = (StarMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        Boolean deleteResult = starMallShoppingCartService.deleteById(newBeeMallShoppingCartItemId, user.getUserId());
        //删除成功
        if (deleteResult) {
            return ResultGenerator.genSuccessResult();
        }
        //删除失败
        return ResultGenerator.genFailResult(ServiceResultEnum.OPERATE_ERROR.getResult());
    }

    @GetMapping("/shop-cart/settle")
    public String settlePage(HttpServletRequest request,
                             HttpSession httpSession) {
        int priceTotal = 0;
        StarMallUserVO user = (StarMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        List<StarMallShoppingCartItemVO> myShoppingCartItems = starMallShoppingCartService.getMyShoppingCartItems(user.getUserId());
        if (CollectionUtils.isEmpty(myShoppingCartItems)) {
            //无数据则不跳转至结算页
            return "/shop-cart";
        } else {
            //总价
            for (StarMallShoppingCartItemVO starMallShoppingCartItemVO : myShoppingCartItems) {
                priceTotal += starMallShoppingCartItemVO.getGoodsCount() * starMallShoppingCartItemVO.getSellingPrice();
            }
            if (priceTotal < 1) {
                StarMallException.fail("购物项价格异常");
            }
        }
        List<StarMallMyCouponVO> myCouponVOS = starMallCouponService.selectOrderCanUseCoupons(myShoppingCartItems, priceTotal, user.getUserId());
        request.setAttribute("coupons", myCouponVOS);
        request.setAttribute("priceTotal", priceTotal);
        request.setAttribute("myShoppingCartItems", myShoppingCartItems);
        return "mall/order-settle";
    }
}
