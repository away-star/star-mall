
package star.xingxing.mall.service;

import star.xingxing.mall.entity.StarMallGoods;
import star.xingxing.mall.util.PageQueryUtil;
import star.xingxing.mall.util.PageResult;

import java.util.List;

public interface StarMallGoodsService {
    /**
     * 后台分页
     *
     * @param pageUtil
     * @return
     */
    PageResult getNewBeeMallGoodsPage(PageQueryUtil pageUtil);

    /**
     * 添加商品
     *
     * @param goods
     * @return
     */
    String saveNewBeeMallGoods(StarMallGoods goods);

    /**
     * 批量新增商品数据
     *
     * @param starMallGoodsList
     * @return
     */
    void batchSaveNewBeeMallGoods(List<StarMallGoods> starMallGoodsList);

    /**
     * 修改商品信息
     *
     * @param goods
     * @return
     */
    String updateNewBeeMallGoods(StarMallGoods goods);

    /**
     * 获取商品详情
     *
     * @param id
     * @return
     */
    StarMallGoods getNewBeeMallGoodsById(Long id);

    /**
     * 批量修改销售状态(上架下架)
     *
     * @param ids
     * @return
     */
    Boolean batchUpdateSellStatus(Long[] ids,int sellStatus);

    /**
     * 商品搜索
     *
     * @param pageUtil
     * @return
     */
    PageResult searchNewBeeMallGoods(PageQueryUtil pageUtil);
}
