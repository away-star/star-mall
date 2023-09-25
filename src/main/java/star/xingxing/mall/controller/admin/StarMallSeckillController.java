package star.xingxing.mall.controller.admin;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import star.xingxing.mall.common.Constants;
import star.xingxing.mall.entity.StarMallSeckill;
import star.xingxing.mall.redis.RedisCache;
import star.xingxing.mall.service.StarMallSeckillService;
import star.xingxing.mall.util.PageQueryUtil;
import star.xingxing.mall.util.Result;
import star.xingxing.mall.util.ResultGenerator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("admin")
public class StarMallSeckillController {

    @Resource
    private StarMallSeckillService starMallSeckillService;
    @Resource
    private RedisCache redisCache;

    @GetMapping("/seckill")
    public String index(HttpServletRequest request) {
        request.setAttribute("path", "newbee_mall_seckill");
        return "admin/newbee_mall_seckill";
    }

    @ResponseBody
    @GetMapping("/seckill/list")
    public Result list(@RequestParam Map<String, Object> params) {
        if (StringUtils.isEmpty((CharSequence) params.get("page")) || StringUtils.isEmpty((CharSequence) params.get("limit"))) {
            return ResultGenerator.genFailResult("参数异常！");
        }
        PageQueryUtil pageUtil = new PageQueryUtil(params);
        return ResultGenerator.genSuccessResult(starMallSeckillService.getSeckillPage(pageUtil));
    }

    /**
     * 保存
     */
    @ResponseBody
    @PostMapping("/seckill/save")
    public Result save(@RequestBody StarMallSeckill starMallSeckill) {
        if (starMallSeckill == null || starMallSeckill.getGoodsId() < 1 || starMallSeckill.getSeckillNum() < 1 || starMallSeckill.getSeckillPrice() < 1) {
            return ResultGenerator.genFailResult("参数异常");
        }
        boolean result = starMallSeckillService.saveSeckill(starMallSeckill);
        if (result) {
            // 虚拟库存预热
            redisCache.setCacheObject(Constants.SECKILL_GOODS_STOCK_KEY + starMallSeckill.getSeckillId(), starMallSeckill.getSeckillNum());
        }
        return ResultGenerator.genDmlResult(result);
    }

    /**
     * 更新
     */
    @PostMapping("/seckill/update")
    @ResponseBody
    public Result update(@RequestBody StarMallSeckill starMallSeckill) {
        if (starMallSeckill == null || starMallSeckill.getSeckillId() == null || starMallSeckill.getGoodsId() < 1 || starMallSeckill.getSeckillNum() < 1 || starMallSeckill.getSeckillPrice() < 1) {
            return ResultGenerator.genFailResult("参数异常");
        }
        boolean result = starMallSeckillService.updateSeckill(starMallSeckill);
        if (result) {
            // 虚拟库存预热
            redisCache.setCacheObject(Constants.SECKILL_GOODS_STOCK_KEY + starMallSeckill.getSeckillId(), starMallSeckill.getSeckillNum());
            redisCache.deleteObject(Constants.SECKILL_GOODS_DETAIL + starMallSeckill.getSeckillId());
            redisCache.deleteObject(Constants.SECKILL_GOODS_LIST);
        }
        return ResultGenerator.genDmlResult(result);
    }

    /**
     * 详情
     */
    @GetMapping("/seckill/{id}")
    @ResponseBody
    public Result Info(@PathVariable("id") Long id) {
        StarMallSeckill starMallSeckill = starMallSeckillService.getSeckillById(id);
        return ResultGenerator.genSuccessResult(starMallSeckill);
    }

    /**
     * 删除
     */
    @DeleteMapping("/seckill/{id}")
    @ResponseBody
    public Result delete(@PathVariable Long id) {
        redisCache.deleteObject(Constants.SECKILL_GOODS_DETAIL + id);
        redisCache.deleteObject(Constants.SECKILL_GOODS_LIST);
        return ResultGenerator.genDmlResult(starMallSeckillService.deleteSeckillById(id));
    }
}
