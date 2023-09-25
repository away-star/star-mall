
package star.xingxing.mall.service;

import star.xingxing.mall.controller.vo.StarMallIndexConfigGoodsVO;
import star.xingxing.mall.entity.IndexConfig;
import star.xingxing.mall.util.PageQueryUtil;
import star.xingxing.mall.util.PageResult;

import java.util.List;

public interface StarMallIndexConfigService {
    /**
     * 后台分页
     *
     * @param pageUtil
     * @return
     */
    PageResult getConfigsPage(PageQueryUtil pageUtil);

    String saveIndexConfig(IndexConfig indexConfig);

    String updateIndexConfig(IndexConfig indexConfig);

    IndexConfig getIndexConfigById(Long id);

    /**
     * 返回固定数量的首页配置商品对象(首页调用)
     *
     * @param number
     * @return
     */
    List<StarMallIndexConfigGoodsVO> getConfigGoodsesForIndex(int configType, int number);

    Boolean deleteBatch(Long[] ids);
}
