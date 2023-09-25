package star.xingxing.mall.dao;

import star.xingxing.mall.entity.StarMallSeckill;
import star.xingxing.mall.util.PageQueryUtil;

import java.util.List;
import java.util.Map;

public interface StarMallSeckillMapper {
    int deleteByPrimaryKey(Long seckillId);

    int insert(StarMallSeckill record);

    int insertSelective(StarMallSeckill record);

    StarMallSeckill selectByPrimaryKey(Long seckillId);

    int updateByPrimaryKeySelective(StarMallSeckill record);

    int updateByPrimaryKey(StarMallSeckill record);

    List<StarMallSeckill> findSeckillList(PageQueryUtil pageUtil);

    int getTotalSeckills(PageQueryUtil pageUtil);

    List<StarMallSeckill> findHomeSeckillList();

    int getHomeTotalSeckills(PageQueryUtil pageUtil);

    void killByProcedure(Map<String, Object> map);

    boolean addStock(Long seckillId);
}
