
package star.xingxing.mall.controller.mall;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import star.xingxing.mall.common.Constants;
import star.xingxing.mall.common.IndexConfigTypeEnum;
import star.xingxing.mall.controller.vo.StarMallIndexCategoryVO;
import star.xingxing.mall.exception.StarMallException;
import star.xingxing.mall.controller.vo.StarMallIndexCarouselVO;
import star.xingxing.mall.controller.vo.StarMallIndexConfigGoodsVO;
import star.xingxing.mall.service.StarMallCarouselService;
import star.xingxing.mall.service.StarMallCategoryService;
import star.xingxing.mall.service.StarMallIndexConfigService;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class IndexController {

    @Resource
    private StarMallCarouselService starMallCarouselService;

    @Resource
    private StarMallIndexConfigService starMallIndexConfigService;

    @Resource
    private StarMallCategoryService starMallCategoryService;

    @GetMapping({"/index", "/", "/index.html"})
    public String indexPage(HttpServletRequest request) {
        List<StarMallIndexCategoryVO> categories = starMallCategoryService.getCategoriesForIndex();
        if (CollectionUtils.isEmpty(categories)) {
            StarMallException.fail("分类数据不完善");
        }
        List<StarMallIndexCarouselVO> carousels = starMallCarouselService.getCarouselsForIndex(Constants.INDEX_CAROUSEL_NUMBER);
        List<StarMallIndexConfigGoodsVO> hotGoodses = starMallIndexConfigService.getConfigGoodsesForIndex(IndexConfigTypeEnum.INDEX_GOODS_HOT.getType(), Constants.INDEX_GOODS_HOT_NUMBER);
        List<StarMallIndexConfigGoodsVO> newGoodses = starMallIndexConfigService.getConfigGoodsesForIndex(IndexConfigTypeEnum.INDEX_GOODS_NEW.getType(), Constants.INDEX_GOODS_NEW_NUMBER);
        List<StarMallIndexConfigGoodsVO> recommendGoodses = starMallIndexConfigService.getConfigGoodsesForIndex(IndexConfigTypeEnum.INDEX_GOODS_RECOMMEND.getType(), Constants.INDEX_GOODS_RECOMMOND_NUMBER);
        // 分类数据
        request.setAttribute("categories", categories);
        // 轮播图
        request.setAttribute("carousels", carousels);
        // 热销商品
        request.setAttribute("hotGoodses", hotGoodses);
        // 新品
        request.setAttribute("newGoodses", newGoodses);
        // 推荐商品
        request.setAttribute("recommendGoodses", recommendGoodses);
        return "mall/index";
    }
}
