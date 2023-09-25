package star.xingxing.mall.dao;

import star.xingxing.mall.entity.StarMallSeckillSuccess;

public interface StarMallSeckillSuccessMapper {
    int deleteByPrimaryKey(Integer secId);

    int insert(StarMallSeckillSuccess record);

    int insertSelective(StarMallSeckillSuccess record);

    StarMallSeckillSuccess selectByPrimaryKey(Long secId);

    int updateByPrimaryKeySelective(StarMallSeckillSuccess record);

    int updateByPrimaryKey(StarMallSeckillSuccess record);

    StarMallSeckillSuccess getSeckillSuccessByUserIdAndSeckillId(Long userId, Long seckillId);
}
