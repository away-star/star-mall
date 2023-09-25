
package star.xingxing.mall.dao;

import star.xingxing.mall.entity.StarMallShoppingCartItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StarMallShoppingCartItemMapper {
    int deleteByPrimaryKey(Long cartItemId);

    int insert(StarMallShoppingCartItem record);

    int insertSelective(StarMallShoppingCartItem record);

    StarMallShoppingCartItem selectByPrimaryKey(Long cartItemId);

    StarMallShoppingCartItem selectByUserIdAndGoodsId(@Param("newBeeMallUserId") Long newBeeMallUserId, @Param("goodsId") Long goodsId);

    List<StarMallShoppingCartItem> selectByUserId(@Param("newBeeMallUserId") Long newBeeMallUserId, @Param("number") int number);

    int selectCountByUserId(Long newBeeMallUserId);

    int updateByPrimaryKeySelective(StarMallShoppingCartItem record);

    int updateByPrimaryKey(StarMallShoppingCartItem record);

    int deleteBatch(List<Long> ids);
}