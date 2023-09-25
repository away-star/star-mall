
package star.xingxing.mall.dao;

import star.xingxing.mall.entity.StarMallOrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StarMallOrderItemMapper {
    int deleteByPrimaryKey(Long orderItemId);

    int insert(StarMallOrderItem record);

    int insertSelective(StarMallOrderItem record);

    StarMallOrderItem selectByPrimaryKey(Long orderItemId);

    /**
     * 根据订单id获取订单项列表
     *
     * @param orderId
     * @return
     */
    List<StarMallOrderItem> selectByOrderId(Long orderId);

    /**
     * 根据订单ids获取订单项列表
     *
     * @param orderIds
     * @return
     */
    List<StarMallOrderItem> selectByOrderIds(@Param("orderIds") List<Long> orderIds);

    /**
     * 批量insert订单项数据
     *
     * @param orderItems
     * @return
     */
    int insertBatch(@Param("orderItems") List<StarMallOrderItem> orderItems);

    int updateByPrimaryKeySelective(StarMallOrderItem record);

    int updateByPrimaryKey(StarMallOrderItem record);
}