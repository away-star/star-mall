
package star.xingxing.mall.dao;

import star.xingxing.mall.entity.StarMallGoods;
import star.xingxing.mall.entity.StockNumDTO;
import star.xingxing.mall.util.PageQueryUtil;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StarMallGoodsMapper {
    int deleteByPrimaryKey(Long goodsId);

    int insert(StarMallGoods record);

    int insertSelective(StarMallGoods record);

    StarMallGoods selectByPrimaryKey(Long goodsId);

    StarMallGoods selectByCategoryIdAndName(@Param("goodsName") String goodsName, @Param("goodsCategoryId") Long goodsCategoryId);

    int updateByPrimaryKeySelective(StarMallGoods record);

    int updateByPrimaryKeyWithBLOBs(StarMallGoods record);

    int updateByPrimaryKey(StarMallGoods record);

    List<StarMallGoods> findNewBeeMallGoodsList(PageQueryUtil pageUtil);

    int getTotalNewBeeMallGoods(PageQueryUtil pageUtil);

    List<StarMallGoods> selectByPrimaryKeys(List<Long> goodsIds);

    List<StarMallGoods> findNewBeeMallGoodsListBySearch(PageQueryUtil pageUtil);

    int getTotalNewBeeMallGoodsBySearch(PageQueryUtil pageUtil);

    int batchInsert(@Param("newBeeMallGoodsList") List<StarMallGoods> starMallGoodsList);

    int updateStockNum(@Param("stockNumDTOS") List<StockNumDTO> stockNumDTOS);

    int batchUpdateSellStatus(@Param("orderIds")Long[] orderIds,@Param("sellStatus") int sellStatus);

    boolean addStock(Long goodsId, Integer goodsCount);
}
