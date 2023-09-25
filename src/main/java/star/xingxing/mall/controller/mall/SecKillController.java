package star.xingxing.mall.controller.mall;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import star.xingxing.mall.common.Constants;
import star.xingxing.mall.controller.vo.ExposerVO;
import star.xingxing.mall.controller.vo.StarMallSeckillGoodsVO;
import star.xingxing.mall.controller.vo.StarMallUserVO;
import star.xingxing.mall.controller.vo.SeckillSuccessVO;
import star.xingxing.mall.dao.StarMallGoodsMapper;
import star.xingxing.mall.entity.StarMallGoods;
import star.xingxing.mall.entity.StarMallSeckill;
import star.xingxing.mall.exception.StarMallException;
import star.xingxing.mall.redis.RedisCache;
import star.xingxing.mall.service.StarMallSeckillService;
import star.xingxing.mall.util.BeanUtil;
import star.xingxing.mall.util.MD5Util;
import star.xingxing.mall.util.Result;
import star.xingxing.mall.util.ResultGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class SecKillController {

    @Resource
    private StarMallSeckillService starMallSeckillService;

    @Resource
    private StarMallGoodsMapper starMallGoodsMapper;

    @Resource
    private RedisCache redisCache;

    @GetMapping("/seckill")
    public String seckillIndex() {
        return "mall/seckill-list";
    }

    /**
     * 获取服务器时间
     *
     * @return result
     */
    @ResponseBody
    @GetMapping("/seckill/time/now")
    public Result getTimeNow() {
        return ResultGenerator.genSuccessResult(System.currentTimeMillis());
    }

    /**
     * 判断秒杀商品虚拟库存是否足够
     *
     * @param seckillId 秒杀ID
     * @return result
     */
    @ResponseBody
    @PostMapping("/seckill/{seckillId}/checkStock")
    public Result seckillCheckStock(@PathVariable Long seckillId) {
        Integer stock = redisCache.getCacheObject(Constants.SECKILL_GOODS_STOCK_KEY + seckillId);
        if (stock == null || stock < 0) {
            return ResultGenerator.genFailResult("秒杀商品库存不足");
        }
        // redis虚拟库存大于等于0时，可以执行秒杀
        return ResultGenerator.genSuccessResult();
    }

    /**
     * 获取秒杀链接
     *
     * @param seckillId 秒杀商品ID
     * @return result
     */
    @ResponseBody
    @PostMapping("/seckill/{seckillId}/exposer")
    public Result exposerUrl(@PathVariable Long seckillId) {
        ExposerVO exposerVO = starMallSeckillService.exposerUrl(seckillId);
        return ResultGenerator.genSuccessResult(exposerVO);
    }

    /**
     * 使用限流注解进行接口限流操作
     *
     * @param seckillId 秒杀ID
     * @param userId    用户ID
     * @param md5       秒杀链接的MD5加密信息
     * @return result
     */
    @ResponseBody
    @PostMapping(value = "/seckillExecution/{seckillId}/{userId}/{md5}")
    public Result execute(@PathVariable Long seckillId,
                          @PathVariable Long userId,
                          @PathVariable String md5) {
        // 判断md5信息是否合法
        if (md5 == null || userId == null || !md5.equals(MD5Util.MD5Encode(seckillId.toString(), Constants.UTF_ENCODING))) {
            throw new StarMallException("秒杀商品不存在");
        }
        SeckillSuccessVO seckillSuccessVO = starMallSeckillService.executeSeckill(seckillId, userId);
        return ResultGenerator.genSuccessResult(seckillSuccessVO);
    }

    @GetMapping("/seckill/info/{seckillId}")
    public String seckillInfo(@PathVariable Long seckillId,
                              HttpServletRequest request,
                              HttpSession httpSession) {
        StarMallUserVO user = (StarMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        if (user != null) {
            request.setAttribute("userId", user.getUserId());
        }
        request.setAttribute("seckillId", seckillId);
        return "mall/seckill-detail";
    }

    @GetMapping("/seckill/list")
    @ResponseBody
    public Result secondKillGoodsList() {
        // 直接返回配置的秒杀商品列表
        // 不返回商品id，每配置一条秒杀数据，就生成一个唯一的秒杀id和发起秒杀的事件id，根据秒杀id去访问详情页
        List<StarMallSeckillGoodsVO> starMallSeckillGoodsVOS = redisCache.getCacheObject(Constants.SECKILL_GOODS_LIST);
        if (starMallSeckillGoodsVOS == null) {
            List<StarMallSeckill> list = starMallSeckillService.getHomeSeckillPage();
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
            starMallSeckillGoodsVOS = list.stream().map(newBeeMallSeckill -> {
                StarMallSeckillGoodsVO starMallSeckillGoodsVO = new StarMallSeckillGoodsVO();
                BeanUtil.copyProperties(newBeeMallSeckill, starMallSeckillGoodsVO);
                StarMallGoods starMallGoods = starMallGoodsMapper.selectByPrimaryKey(newBeeMallSeckill.getGoodsId());
                if (starMallGoods == null) {
                    return null;
                }
                starMallSeckillGoodsVO.setGoodsName(starMallGoods.getGoodsName());
                starMallSeckillGoodsVO.setGoodsCoverImg(starMallGoods.getGoodsCoverImg());
                starMallSeckillGoodsVO.setSellingPrice(starMallGoods.getSellingPrice());
                Date seckillBegin = starMallSeckillGoodsVO.getSeckillBegin();
                Date seckillEnd = starMallSeckillGoodsVO.getSeckillEnd();
                String formatBegin = sdf.format(seckillBegin);
                String formatEnd = sdf.format(seckillEnd);
                starMallSeckillGoodsVO.setSeckillBeginTime(formatBegin);
                starMallSeckillGoodsVO.setSeckillEndTime(formatEnd);
                return starMallSeckillGoodsVO;
            }).filter(Objects::nonNull).collect(Collectors.toList());
            redisCache.setCacheObject(Constants.SECKILL_GOODS_LIST, starMallSeckillGoodsVOS, 60 * 60 * 100, TimeUnit.SECONDS);
        }
        return ResultGenerator.genSuccessResult(starMallSeckillGoodsVOS);
    }

    @GetMapping("/seckill/{seckillId}")
    @ResponseBody
    public Result seckillGoodsDetail(@PathVariable Long seckillId) {
        // 返回秒杀商品详情VO，如果秒杀时间未到，不允许访问详情页，也不允许返回数据，参数为秒杀id
        // 根据返回的数据解析出秒杀的事件id，发起秒杀
        // 不访问详情页不会获取到秒杀的事件id，不然容易被猜到url路径从而直接发起秒杀请求
        StarMallSeckillGoodsVO starMallSeckillGoodsVO = redisCache.getCacheObject(Constants.SECKILL_GOODS_DETAIL + seckillId);
        if (starMallSeckillGoodsVO == null) {
            StarMallSeckill starMallSeckill = starMallSeckillService.getSeckillById(seckillId);
            if (!starMallSeckill.getSeckillStatus()) {
                return ResultGenerator.genFailResult("秒杀商品已下架");
            }
            starMallSeckillGoodsVO = new StarMallSeckillGoodsVO();
            BeanUtil.copyProperties(starMallSeckill, starMallSeckillGoodsVO);
            StarMallGoods starMallGoods = starMallGoodsMapper.selectByPrimaryKey(starMallSeckill.getGoodsId());
            starMallSeckillGoodsVO.setGoodsName(starMallGoods.getGoodsName());
            starMallSeckillGoodsVO.setGoodsIntro(starMallGoods.getGoodsIntro());
            starMallSeckillGoodsVO.setGoodsDetailContent(starMallGoods.getGoodsDetailContent());
            starMallSeckillGoodsVO.setGoodsCoverImg(starMallGoods.getGoodsCoverImg());
            starMallSeckillGoodsVO.setSellingPrice(starMallGoods.getSellingPrice());
            Date seckillBegin = starMallSeckillGoodsVO.getSeckillBegin();
            Date seckillEnd = starMallSeckillGoodsVO.getSeckillEnd();
            starMallSeckillGoodsVO.setStartDate(seckillBegin.getTime());
            starMallSeckillGoodsVO.setEndDate(seckillEnd.getTime());
            redisCache.setCacheObject(Constants.SECKILL_GOODS_DETAIL + seckillId, starMallSeckillGoodsVO);
        }
        return ResultGenerator.genSuccessResult(starMallSeckillGoodsVO);
    }

}
