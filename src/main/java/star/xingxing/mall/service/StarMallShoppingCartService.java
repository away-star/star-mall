
package star.xingxing.mall.service;

import star.xingxing.mall.controller.vo.StarMallShoppingCartItemVO;
import star.xingxing.mall.entity.StarMallShoppingCartItem;

import java.util.List;

public interface StarMallShoppingCartService {

    /**
     * 保存商品至购物车中
     *
     * @param starMallShoppingCartItem
     * @return
     */
    String saveNewBeeMallCartItem(StarMallShoppingCartItem starMallShoppingCartItem);

    /**
     * 修改购物车中的属性
     *
     * @param starMallShoppingCartItem
     * @return
     */
    String updateNewBeeMallCartItem(StarMallShoppingCartItem starMallShoppingCartItem);

    /**
     * 获取购物项详情
     *
     * @param newBeeMallShoppingCartItemId
     * @return
     */
    StarMallShoppingCartItem getNewBeeMallCartItemById(Long newBeeMallShoppingCartItemId);

    /**
     * 删除购物车中的商品
     *
     *
     * @param shoppingCartItemId
     * @param userId
     * @return
     */
    Boolean deleteById(Long shoppingCartItemId, Long userId);

    /**
     * 获取我的购物车中的列表数据
     *
     * @param newBeeMallUserId
     * @return
     */
    List<StarMallShoppingCartItemVO> getMyShoppingCartItems(Long newBeeMallUserId);
}
