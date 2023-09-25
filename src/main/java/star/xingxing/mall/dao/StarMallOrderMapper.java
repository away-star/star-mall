
package star.xingxing.mall.dao;

import star.xingxing.mall.entity.StarMallOrder;
import star.xingxing.mall.util.PageQueryUtil;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StarMallOrderMapper {
    int deleteByPrimaryKey(Long orderId);

    int insert(StarMallOrder record);

    int insertSelective(StarMallOrder record);

    StarMallOrder selectByPrimaryKey(Long orderId);

    StarMallOrder selectByOrderNo(String orderNo);

    int updateByPrimaryKeySelective(StarMallOrder record);

    int updateByPrimaryKey(StarMallOrder record);

    List<StarMallOrder> findNewBeeMallOrderList(PageQueryUtil pageUtil);

    int getTotalNewBeeMallOrders(PageQueryUtil pageUtil);

    List<StarMallOrder> selectByPrimaryKeys(@Param("orderIds") List<Long> orderIds);

    int checkOut(@Param("orderIds") List<Long> orderIds);

    int closeOrder(@Param("orderIds") List<Long> orderIds, @Param("orderStatus") int orderStatus);

    int checkDone(@Param("orderIds") List<Long> asList);

    List<StarMallOrder> selectPrePayOrders();
}
