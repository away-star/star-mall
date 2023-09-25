package star.xingxing.mall.task;

import star.xingxing.mall.common.Constants;
import star.xingxing.mall.common.StarMallOrderStatusEnum;
import star.xingxing.mall.dao.StarMallGoodsMapper;
import star.xingxing.mall.dao.StarMallOrderItemMapper;
import star.xingxing.mall.dao.StarMallOrderMapper;
import star.xingxing.mall.dao.StarMallSeckillMapper;
import star.xingxing.mall.entity.StarMallOrder;
import star.xingxing.mall.entity.StarMallOrderItem;
import star.xingxing.mall.redis.RedisCache;
import star.xingxing.mall.service.StarMallCouponService;
import star.xingxing.mall.util.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

/**
 * 未支付订单超时自动取消任务
 */
public class OrderUnPaidTask extends Task {
    /**
     * 默认延迟时间30分钟，单位毫秒
     */
    private static final long DELAY_TIME = 30 * 60 * 1000;

    private final Logger log = LoggerFactory.getLogger(OrderUnPaidTask.class);
    /**
     * 订单id
     */
    private final Long orderId;

    public OrderUnPaidTask(Long orderId, long delayInMilliseconds) {
        super("OrderUnPaidTask-" + orderId, delayInMilliseconds);
        this.orderId = orderId;
    }

    public OrderUnPaidTask(Long orderId) {
        super("OrderUnPaidTask-" + orderId, DELAY_TIME);
        this.orderId = orderId;
    }

    @Override
    public void run() {
        log.info("系统开始处理延时任务---订单超时未付款--- {}", this.orderId);

        StarMallOrderMapper starMallOrderMapper = SpringContextUtil.getBean(StarMallOrderMapper.class);
        StarMallOrderItemMapper starMallOrderItemMapper = SpringContextUtil.getBean(StarMallOrderItemMapper.class);
        StarMallGoodsMapper starMallGoodsMapper = SpringContextUtil.getBean(StarMallGoodsMapper.class);
        StarMallCouponService starMallCouponService = SpringContextUtil.getBean(StarMallCouponService.class);

        StarMallOrder order = starMallOrderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            log.info("系统结束处理延时任务---订单超时未付款--- {}", this.orderId);
            return;
        }
        if (order.getOrderStatus() != StarMallOrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()) {
            log.info("系统结束处理延时任务---订单超时未付款--- {}", this.orderId);
            return;
        }

        // 设置订单为已取消状态
        order.setOrderStatus((byte) StarMallOrderStatusEnum.ORDER_CLOSED_BY_EXPIRED.getOrderStatus());
        order.setUpdateTime(new Date());
        if (starMallOrderMapper.updateByPrimaryKey(order) <= 0) {
            throw new RuntimeException("更新数据已失效");
        }

        // 商品货品数量增加
        List<StarMallOrderItem> starMallOrderItems = starMallOrderItemMapper.selectByOrderId(orderId);
        for (StarMallOrderItem orderItem : starMallOrderItems) {
            if (orderItem.getSeckillId() != null) {
                Long seckillId = orderItem.getSeckillId();
                StarMallSeckillMapper starMallSeckillMapper = SpringContextUtil.getBean(StarMallSeckillMapper.class);
                RedisCache redisCache = SpringContextUtil.getBean(RedisCache.class);
                if (!starMallSeckillMapper.addStock(seckillId)) {
                    throw new RuntimeException("秒杀商品货品库存增加失败");
                }
                redisCache.increment(Constants.SECKILL_GOODS_STOCK_KEY + seckillId);
            } else {
                Long goodsId = orderItem.getGoodsId();
                Integer goodsCount = orderItem.getGoodsCount();
                if (!starMallGoodsMapper.addStock(goodsId, goodsCount)) {
                    throw new RuntimeException("商品货品库存增加失败");
                }
            }
        }

        // 返还优惠券
        starMallCouponService.releaseCoupon(orderId);
        log.info("系统结束处理延时任务---订单超时未付款--- {}", this.orderId);
    }
}
