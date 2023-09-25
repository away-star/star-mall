package star.xingxing.mall.service;

import star.xingxing.mall.controller.vo.ExposerVO;
import star.xingxing.mall.controller.vo.SeckillSuccessVO;
import star.xingxing.mall.entity.StarMallSeckill;
import star.xingxing.mall.util.PageQueryUtil;
import star.xingxing.mall.util.PageResult;

import java.util.List;

public interface StarMallSeckillService {

    PageResult getSeckillPage(PageQueryUtil pageUtil);

    boolean saveSeckill(StarMallSeckill starMallSeckill);

    boolean updateSeckill(StarMallSeckill starMallSeckill);

    StarMallSeckill getSeckillById(Long id);

    boolean deleteSeckillById(Long id);

    List<StarMallSeckill> getHomeSeckillPage();

    ExposerVO exposerUrl(Long seckillId);

    SeckillSuccessVO executeSeckill(Long seckillId, Long userId);
}
