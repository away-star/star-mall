
package star.xingxing.mall.service.impl;

import jakarta.annotation.Resource;
import star.xingxing.mall.common.Constants;
import star.xingxing.mall.common.ServiceResultEnum;
import star.xingxing.mall.controller.vo.StarMallShoppingCartItemVO;
import star.xingxing.mall.dao.StarMallGoodsMapper;
import star.xingxing.mall.dao.StarMallShoppingCartItemMapper;
import star.xingxing.mall.entity.StarMallGoods;
import star.xingxing.mall.entity.StarMallShoppingCartItem;
import star.xingxing.mall.service.StarMallShoppingCartService;
import star.xingxing.mall.util.BeanUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class StarMallShoppingCartServiceImpl implements StarMallShoppingCartService {

    @Resource
    private StarMallShoppingCartItemMapper starMallShoppingCartItemMapper;

    @Resource
    private StarMallGoodsMapper starMallGoodsMapper;

    @Override
    public String saveNewBeeMallCartItem(StarMallShoppingCartItem starMallShoppingCartItem) {
        StarMallShoppingCartItem temp = starMallShoppingCartItemMapper.selectByUserIdAndGoodsId(starMallShoppingCartItem.getUserId(), starMallShoppingCartItem.getGoodsId());
        if (temp != null) {
            //已存在则修改该记录
            temp.setGoodsCount(starMallShoppingCartItem.getGoodsCount());
            return updateNewBeeMallCartItem(temp);
        }
        StarMallGoods starMallGoods = starMallGoodsMapper.selectByPrimaryKey(starMallShoppingCartItem.getGoodsId());
        //商品为空
        if (starMallGoods == null) {
            return ServiceResultEnum.GOODS_NOT_EXIST.getResult();
        }
        int totalItem = starMallShoppingCartItemMapper.selectCountByUserId(starMallShoppingCartItem.getUserId()) + 1;
        //超出单个商品的最大数量
        if (starMallShoppingCartItem.getGoodsCount() > Constants.SHOPPING_CART_ITEM_LIMIT_NUMBER) {
            return ServiceResultEnum.SHOPPING_CART_ITEM_LIMIT_NUMBER_ERROR.getResult();
        }
        //超出最大数量
        if (totalItem > Constants.SHOPPING_CART_ITEM_TOTAL_NUMBER) {
            return ServiceResultEnum.SHOPPING_CART_ITEM_TOTAL_NUMBER_ERROR.getResult();
        }
        //保存记录
        if (starMallShoppingCartItemMapper.insertSelective(starMallShoppingCartItem) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public String updateNewBeeMallCartItem(StarMallShoppingCartItem starMallShoppingCartItem) {
        StarMallShoppingCartItem starMallShoppingCartItemUpdate = starMallShoppingCartItemMapper.selectByPrimaryKey(starMallShoppingCartItem.getCartItemId());
        if (starMallShoppingCartItemUpdate == null) {
            return ServiceResultEnum.DATA_NOT_EXIST.getResult();
        }
        //超出单个商品的最大数量
        if (starMallShoppingCartItem.getGoodsCount() > Constants.SHOPPING_CART_ITEM_LIMIT_NUMBER) {
            return ServiceResultEnum.SHOPPING_CART_ITEM_LIMIT_NUMBER_ERROR.getResult();
        }
        // 数量相同不会进行修改
        if (starMallShoppingCartItemUpdate.getGoodsCount().equals(starMallShoppingCartItem.getGoodsCount())) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        // userId不同不能修改
        if (!starMallShoppingCartItem.getUserId().equals(starMallShoppingCartItemUpdate.getUserId())) {
            return ServiceResultEnum.NO_PERMISSION_ERROR.getResult();
        }
        starMallShoppingCartItemUpdate.setGoodsCount(starMallShoppingCartItem.getGoodsCount());
        starMallShoppingCartItemUpdate.setUpdateTime(new Date());
        //修改记录
        if (starMallShoppingCartItemMapper.updateByPrimaryKeySelective(starMallShoppingCartItemUpdate) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public StarMallShoppingCartItem getNewBeeMallCartItemById(Long newBeeMallShoppingCartItemId) {
        return starMallShoppingCartItemMapper.selectByPrimaryKey(newBeeMallShoppingCartItemId);
    }

    @Override
    public Boolean deleteById(Long shoppingCartItemId, Long userId) {
        StarMallShoppingCartItem starMallShoppingCartItem = starMallShoppingCartItemMapper.selectByPrimaryKey(shoppingCartItemId);
        if (starMallShoppingCartItem == null) {
            return false;
        }
        //userId不同不能删除
        if (!userId.equals(starMallShoppingCartItem.getUserId())) {
            return false;
        }
        return starMallShoppingCartItemMapper.deleteByPrimaryKey(shoppingCartItemId) > 0;
    }

    @Override
    public List<StarMallShoppingCartItemVO> getMyShoppingCartItems(Long newBeeMallUserId) {
        List<StarMallShoppingCartItemVO> starMallShoppingCartItemVOS = new ArrayList<>();
        List<StarMallShoppingCartItem> starMallShoppingCartItems = starMallShoppingCartItemMapper.selectByUserId(newBeeMallUserId, Constants.SHOPPING_CART_ITEM_TOTAL_NUMBER);
        if (!CollectionUtils.isEmpty(starMallShoppingCartItems)) {
            //查询商品信息并做数据转换
            List<Long> newBeeMallGoodsIds = starMallShoppingCartItems.stream().map(StarMallShoppingCartItem::getGoodsId).collect(Collectors.toList());
            List<StarMallGoods> starMallGoods = starMallGoodsMapper.selectByPrimaryKeys(newBeeMallGoodsIds);
            Map<Long, StarMallGoods> newBeeMallGoodsMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(starMallGoods)) {
                newBeeMallGoodsMap = starMallGoods.stream().collect(Collectors.toMap(StarMallGoods::getGoodsId, Function.identity(), (entity1, entity2) -> entity1));
            }
            for (StarMallShoppingCartItem starMallShoppingCartItem : starMallShoppingCartItems) {
                StarMallShoppingCartItemVO starMallShoppingCartItemVO = new StarMallShoppingCartItemVO();
                BeanUtil.copyProperties(starMallShoppingCartItem, starMallShoppingCartItemVO);
                if (newBeeMallGoodsMap.containsKey(starMallShoppingCartItem.getGoodsId())) {
                    StarMallGoods starMallGoodsTemp = newBeeMallGoodsMap.get(starMallShoppingCartItem.getGoodsId());
                    starMallShoppingCartItemVO.setGoodsCoverImg(starMallGoodsTemp.getGoodsCoverImg());
                    String goodsName = starMallGoodsTemp.getGoodsName();
                    // 字符串过长导致文字超出的问题
                    if (goodsName.length() > 28) {
                        goodsName = goodsName.substring(0, 28) + "...";
                    }
                    starMallShoppingCartItemVO.setGoodsName(goodsName);
                    starMallShoppingCartItemVO.setSellingPrice(starMallGoodsTemp.getSellingPrice());
                    starMallShoppingCartItemVOS.add(starMallShoppingCartItemVO);
                }
            }
        }
        return starMallShoppingCartItemVOS;
    }
}
