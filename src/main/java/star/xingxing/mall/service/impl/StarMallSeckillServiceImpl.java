package star.xingxing.mall.service.impl;

import com.google.common.util.concurrent.RateLimiter;
import jakarta.annotation.Resource;
import star.xingxing.mall.common.Constants;
import star.xingxing.mall.common.SeckillStatusEnum;
import star.xingxing.mall.common.ServiceResultEnum;
import star.xingxing.mall.controller.vo.ExposerVO;
import star.xingxing.mall.controller.vo.StarMallSeckillGoodsVO;
import star.xingxing.mall.controller.vo.SeckillSuccessVO;
import star.xingxing.mall.dao.StarMallGoodsMapper;
import star.xingxing.mall.dao.StarMallSeckillMapper;
import star.xingxing.mall.dao.StarMallSeckillSuccessMapper;
import star.xingxing.mall.entity.StarMallSeckill;
import star.xingxing.mall.entity.StarMallSeckillSuccess;
import star.xingxing.mall.exception.StarMallException;
import star.xingxing.mall.redis.RedisCache;
import star.xingxing.mall.service.StarMallSeckillService;
import star.xingxing.mall.util.MD5Util;
import star.xingxing.mall.util.PageQueryUtil;
import star.xingxing.mall.util.PageResult;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class StarMallSeckillServiceImpl implements StarMallSeckillService {

    // 使用令牌桶RateLimiter 限流
    private static final RateLimiter RATE_LIMITER = RateLimiter.create(100);

    @Resource
    private StarMallSeckillMapper starMallSeckillMapper;

    @Resource
    private StarMallSeckillSuccessMapper starMallSeckillSuccessMapper;

    @Resource
    private StarMallGoodsMapper starMallGoodsMapper;

    @Resource
    private RedisCache redisCache;

    @Override
    public PageResult getSeckillPage(PageQueryUtil pageUtil) {
        List<StarMallSeckill> carousels = starMallSeckillMapper.findSeckillList(pageUtil);
        int total = starMallSeckillMapper.getTotalSeckills(pageUtil);
        return new PageResult(carousels, total, pageUtil.getLimit(), pageUtil.getPage());
    }

    @Override
    public boolean saveSeckill(StarMallSeckill starMallSeckill) {
        if (starMallGoodsMapper.selectByPrimaryKey(starMallSeckill.getGoodsId()) == null) {
            StarMallException.fail(ServiceResultEnum.GOODS_NOT_EXIST.getResult());
        }
        return starMallSeckillMapper.insertSelective(starMallSeckill) > 0;
    }

    @Override
    public boolean updateSeckill(StarMallSeckill starMallSeckill) {
        if (starMallGoodsMapper.selectByPrimaryKey(starMallSeckill.getGoodsId()) == null) {
            StarMallException.fail(ServiceResultEnum.GOODS_NOT_EXIST.getResult());
        }
        StarMallSeckill temp = starMallSeckillMapper.selectByPrimaryKey(starMallSeckill.getSeckillId());
        if (temp == null) {
            StarMallException.fail(ServiceResultEnum.DATA_NOT_EXIST.getResult());
        }
        starMallSeckill.setUpdateTime(new Date());
        return starMallSeckillMapper.updateByPrimaryKeySelective(starMallSeckill) > 0;
    }

    @Override
    public StarMallSeckill getSeckillById(Long id) {
        return starMallSeckillMapper.selectByPrimaryKey(id);
    }

    @Override
    public boolean deleteSeckillById(Long id) {
        return starMallSeckillMapper.deleteByPrimaryKey(id) > 0;
    }

    @Override
    public List<StarMallSeckill> getHomeSeckillPage() {
        return starMallSeckillMapper.findHomeSeckillList();
    }

    @Override
    public ExposerVO exposerUrl(Long seckillId) {
        StarMallSeckillGoodsVO starMallSeckillGoodsVO = redisCache.getCacheObject(Constants.SECKILL_GOODS_DETAIL + seckillId);
        Date startTime = starMallSeckillGoodsVO.getSeckillBegin();
        Date endTime = starMallSeckillGoodsVO.getSeckillEnd();
        // 系统当前时间
        Date nowTime = new Date();
        if (nowTime.getTime() < startTime.getTime() || nowTime.getTime() > endTime.getTime()) {
            return new ExposerVO(SeckillStatusEnum.NOT_START, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
        }
        // 检查虚拟库存
        Integer stock = redisCache.getCacheObject(Constants.SECKILL_GOODS_STOCK_KEY + seckillId);
        if (stock == null || stock < 0) {
            return new ExposerVO(SeckillStatusEnum.STARTED_SHORTAGE_STOCK, seckillId);
        }
        // 加密
        String md5 = MD5Util.MD5Encode(seckillId.toString(), Constants.UTF_ENCODING);
        return new ExposerVO(SeckillStatusEnum.START, md5, seckillId);
    }

    @Override
    public SeckillSuccessVO executeSeckill(Long seckillId, Long userId) {
        // 判断能否在500毫秒内得到令牌，如果不能则立即返回false，不会阻塞程序
        if (!RATE_LIMITER.tryAcquire(500, TimeUnit.MILLISECONDS)) {
            throw new StarMallException("秒杀失败");
        }
        // 判断用户是否购买过秒杀商品
        if (redisCache.containsCacheSet(Constants.SECKILL_SUCCESS_USER_ID + seckillId, userId)) {
            throw new StarMallException("您已经购买过秒杀商品，请勿重复购买");
        }
        // 更新秒杀商品虚拟库存
        Long stock = redisCache.luaDecrement(Constants.SECKILL_GOODS_STOCK_KEY + seckillId);
        if (stock < 0) {
            throw new StarMallException("秒杀商品已售空");
        }
        StarMallSeckill starMallSeckill = redisCache.getCacheObject(Constants.SECKILL_KEY + seckillId);
        if (starMallSeckill == null) {
            starMallSeckill = starMallSeckillMapper.selectByPrimaryKey(seckillId);
            redisCache.setCacheObject(Constants.SECKILL_KEY + seckillId, starMallSeckill, 24, TimeUnit.HOURS);
        }
        // 判断秒杀商品是否再有效期内
        long beginTime = starMallSeckill.getSeckillBegin().getTime();
        long endTime = starMallSeckill.getSeckillEnd().getTime();
        Date now = new Date();
        long nowTime = now.getTime();
        if (nowTime < beginTime) {
            throw new StarMallException("秒杀未开启");
        } else if (nowTime > endTime) {
            throw new StarMallException("秒杀已结束");
        }

        Date killTime = new Date();
        Map<String, Object> map = new HashMap<>(8);
        map.put("seckillId", seckillId);
        map.put("userId", userId);
        map.put("killTime", killTime);
        map.put("result", null);
        // 执行存储过程，result被赋值
        try {
            starMallSeckillMapper.killByProcedure(map);
        } catch (Exception e) {
            throw new StarMallException(e.getMessage());
        }
        // 获取result -2sql执行失败 -1未插入数据 0未更新数据 1sql执行成功
        map.get("result");
        int result = MapUtils.getInteger(map, "result", -2);
        if (result != 1) {
            throw new StarMallException("很遗憾！未抢购到秒杀商品");
        }
        // 记录购买过的用户
        redisCache.setCacheSet(Constants.SECKILL_SUCCESS_USER_ID + seckillId, userId);
        long endExpireTime = endTime / 1000;
        long nowExpireTime = nowTime / 1000;
        redisCache.expire(Constants.SECKILL_SUCCESS_USER_ID + seckillId, endExpireTime - nowExpireTime, TimeUnit.SECONDS);
        StarMallSeckillSuccess seckillSuccess = starMallSeckillSuccessMapper.getSeckillSuccessByUserIdAndSeckillId(userId, seckillId);
        SeckillSuccessVO seckillSuccessVO = new SeckillSuccessVO();
        Long seckillSuccessId = seckillSuccess.getSecId();
        seckillSuccessVO.setSeckillSuccessId(seckillSuccessId);
        seckillSuccessVO.setMd5(MD5Util.MD5Encode(seckillSuccessId + Constants.SECKILL_ORDER_SALT, Constants.UTF_ENCODING));
        return seckillSuccessVO;
    }

}
