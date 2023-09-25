
package star.xingxing.mall.service.impl;

import jakarta.annotation.Resource;
import star.xingxing.mall.common.StarMallCategoryLevelEnum;
import star.xingxing.mall.common.ServiceResultEnum;
import star.xingxing.mall.controller.vo.StarMallSearchGoodsVO;
import star.xingxing.mall.dao.GoodsCategoryMapper;
import star.xingxing.mall.dao.StarMallGoodsMapper;
import star.xingxing.mall.entity.GoodsCategory;
import star.xingxing.mall.entity.StarMallGoods;
import star.xingxing.mall.exception.StarMallException;
import star.xingxing.mall.service.StarMallGoodsService;
import star.xingxing.mall.util.BeanUtil;
import star.xingxing.mall.util.PageQueryUtil;
import star.xingxing.mall.util.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class StarMallGoodsServiceImpl implements StarMallGoodsService {

    @Resource
    private StarMallGoodsMapper goodsMapper;
    @Resource
    private GoodsCategoryMapper goodsCategoryMapper;

    @Override
    public PageResult getNewBeeMallGoodsPage(PageQueryUtil pageUtil) {
        List<StarMallGoods> goodsList = goodsMapper.findNewBeeMallGoodsList(pageUtil);
        int total = goodsMapper.getTotalNewBeeMallGoods(pageUtil);
        return new PageResult(goodsList, total, pageUtil.getLimit(), pageUtil.getPage());
    }

    @Override
    public String saveNewBeeMallGoods(StarMallGoods goods) {
        GoodsCategory goodsCategory = goodsCategoryMapper.selectByPrimaryKey(goods.getGoodsCategoryId());
        // 分类不存在或者不是三级分类，则该参数字段异常
        if (goodsCategory == null || goodsCategory.getCategoryLevel().intValue() != StarMallCategoryLevelEnum.LEVEL_THREE.getLevel()) {
            return ServiceResultEnum.GOODS_CATEGORY_ERROR.getResult();
        }
        if (goodsMapper.selectByCategoryIdAndName(goods.getGoodsName(), goods.getGoodsCategoryId()) != null) {
            return ServiceResultEnum.SAME_GOODS_EXIST.getResult();
        }
        if (goodsMapper.insertSelective(goods) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public void batchSaveNewBeeMallGoods(List<StarMallGoods> starMallGoodsList) {
        if (!CollectionUtils.isEmpty(starMallGoodsList)) {
            goodsMapper.batchInsert(starMallGoodsList);
        }
    }

    @Override
    public String updateNewBeeMallGoods(StarMallGoods goods) {
        GoodsCategory goodsCategory = goodsCategoryMapper.selectByPrimaryKey(goods.getGoodsCategoryId());
        // 分类不存在或者不是三级分类，则该参数字段异常
        if (goodsCategory == null || goodsCategory.getCategoryLevel().intValue() != StarMallCategoryLevelEnum.LEVEL_THREE.getLevel()) {
            return ServiceResultEnum.GOODS_CATEGORY_ERROR.getResult();
        }
        StarMallGoods temp = goodsMapper.selectByPrimaryKey(goods.getGoodsId());
        if (temp == null) {
            return ServiceResultEnum.DATA_NOT_EXIST.getResult();
        }
        StarMallGoods temp2 = goodsMapper.selectByCategoryIdAndName(goods.getGoodsName(), goods.getGoodsCategoryId());
        if (temp2 != null && !temp2.getGoodsId().equals(goods.getGoodsId())) {
            //name和分类id相同且不同id 不能继续修改
            return ServiceResultEnum.SAME_GOODS_EXIST.getResult();
        }
        goods.setUpdateTime(new Date());
        if (goodsMapper.updateByPrimaryKeySelective(goods) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public StarMallGoods getNewBeeMallGoodsById(Long id) {
        StarMallGoods starMallGoods = goodsMapper.selectByPrimaryKey(id);
        if (starMallGoods == null) {
            StarMallException.fail(ServiceResultEnum.GOODS_NOT_EXIST.getResult());
        }
        return starMallGoods;
    }

    @Override
    public Boolean batchUpdateSellStatus(Long[] ids, int sellStatus) {
        return goodsMapper.batchUpdateSellStatus(ids, sellStatus) > 0;
    }

    @Override
    public PageResult searchNewBeeMallGoods(PageQueryUtil pageUtil) {
        List<StarMallGoods> goodsList = goodsMapper.findNewBeeMallGoodsListBySearch(pageUtil);
        int total = goodsMapper.getTotalNewBeeMallGoodsBySearch(pageUtil);
        List<StarMallSearchGoodsVO> starMallSearchGoodsVOS = new ArrayList<>();
        if (!CollectionUtils.isEmpty(goodsList)) {
            starMallSearchGoodsVOS = BeanUtil.copyList(goodsList, StarMallSearchGoodsVO.class);
            for (StarMallSearchGoodsVO starMallSearchGoodsVO : starMallSearchGoodsVOS) {
                String goodsName = starMallSearchGoodsVO.getGoodsName();
                String goodsIntro = starMallSearchGoodsVO.getGoodsIntro();
                // 字符串过长导致文字超出的问题
                if (goodsName.length() > 28) {
                    goodsName = goodsName.substring(0, 28) + "...";
                    starMallSearchGoodsVO.setGoodsName(goodsName);
                }
                if (goodsIntro.length() > 30) {
                    goodsIntro = goodsIntro.substring(0, 30) + "...";
                    starMallSearchGoodsVO.setGoodsIntro(goodsIntro);
                }
            }
        }
        return new PageResult(starMallSearchGoodsVOS, total, pageUtil.getLimit(), pageUtil.getPage());
    }
}
