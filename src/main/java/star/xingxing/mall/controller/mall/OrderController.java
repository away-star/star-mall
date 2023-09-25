
package star.xingxing.mall.controller.mall;

import com.alibaba.fastjson2.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import star.xingxing.mall.annotion.RepeatSubmit;
import star.xingxing.mall.common.Constants;
import star.xingxing.mall.common.StarMallOrderStatusEnum;
import star.xingxing.mall.common.PayStatusEnum;
import star.xingxing.mall.common.ServiceResultEnum;
import star.xingxing.mall.config.AlipayConfig;
import star.xingxing.mall.config.ProjectConfig;
import star.xingxing.mall.controller.vo.StarMallOrderDetailVO;
import star.xingxing.mall.controller.vo.StarMallShoppingCartItemVO;
import star.xingxing.mall.controller.vo.StarMallUserVO;
import star.xingxing.mall.entity.StarMallOrder;
import star.xingxing.mall.exception.StarMallException;
import star.xingxing.mall.service.StarMallOrderService;
import star.xingxing.mall.service.StarMallShoppingCartService;
import star.xingxing.mall.util.MD5Util;
import star.xingxing.mall.util.PageQueryUtil;
import star.xingxing.mall.util.Result;
import star.xingxing.mall.util.ResultGenerator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.*;

@Controller
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    @Resource
    private StarMallShoppingCartService starMallShoppingCartService;
    @Resource
    private StarMallOrderService starMallOrderService;
    @Resource
    private AlipayConfig alipayConfig;

    @GetMapping("/orders/{orderNo}")
    public String orderDetailPage(HttpServletRequest request, @PathVariable("orderNo") String orderNo, HttpSession httpSession) {
        StarMallUserVO user = (StarMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        StarMallOrderDetailVO orderDetailVO = starMallOrderService.getOrderDetailByOrderNo(orderNo, user.getUserId());
        request.setAttribute("orderDetailVO", orderDetailVO);
        return "mall/order-detail";
    }

    @GetMapping("/orders")
    public String orderListPage(@RequestParam Map<String, Object> params, HttpServletRequest request, HttpSession httpSession) {
        StarMallUserVO user = (StarMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        params.put("userId", user.getUserId());
        if (StringUtils.isEmpty((CharSequence) params.get("page"))) {
            params.put("page", 1);
        }
        params.put("limit", Constants.MY_ORDERS_PAGE_LIMIT);
        //封装我的订单数据
        PageQueryUtil pageUtil = new PageQueryUtil(params);
        request.setAttribute("pageResult", starMallOrderService.getMyOrders(pageUtil));
        request.setAttribute("path", "orders");
        return "mall/my-orders";
    }

    @RepeatSubmit
    @GetMapping("/saveOrder")
    public String saveOrder(Long couponUserId, HttpSession httpSession) {
        StarMallUserVO user = (StarMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        List<StarMallShoppingCartItemVO> myShoppingCartItems = starMallShoppingCartService.getMyShoppingCartItems(user.getUserId());
        if (StringUtils.isEmpty(user.getAddress().trim())) {
            //无收货地址
            StarMallException.fail(ServiceResultEnum.NULL_ADDRESS_ERROR.getResult());
        }
        if (CollectionUtils.isEmpty(myShoppingCartItems)) {
            //购物车中无数据则跳转至错误页
            StarMallException.fail(ServiceResultEnum.SHOPPING_ITEM_ERROR.getResult());
        }
        //保存订单并返回订单号
        String saveOrderResult = starMallOrderService.saveOrder(user, couponUserId, myShoppingCartItems);
        //跳转到订单详情页
        return "redirect:/orders/" + saveOrderResult;
    }

    @RepeatSubmit
    @GetMapping("/saveSeckillOrder/{seckillSuccessId}/{userId}/{seckillSecretKey}")
    public String saveOrder(@PathVariable Long seckillSuccessId,
                            @PathVariable Long userId,
                            @PathVariable String seckillSecretKey) {
        if (seckillSecretKey == null || !seckillSecretKey.equals(MD5Util.MD5Encode(seckillSuccessId + Constants.SECKILL_ORDER_SALT, Constants.UTF_ENCODING))) {
            throw new StarMallException("秒杀商品下单不合法");
        }
        // 保存订单并返回订单号
        String saveOrderResult = starMallOrderService.seckillSaveOrder(seckillSuccessId, userId);
        // 跳转到订单详情页
        return "redirect:/orders/" + saveOrderResult;
    }

    @RepeatSubmit
    @GetMapping("/selectPayType")
    public String selectPayType(HttpServletRequest request, @RequestParam("orderNo") String orderNo, HttpSession httpSession) {
        StarMallUserVO user = (StarMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        StarMallOrder starMallOrder = judgeOrderUserId(orderNo, user.getUserId());
        //判断订单状态
        if (starMallOrder.getOrderStatus().intValue() != StarMallOrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()) {
            StarMallException.fail(ServiceResultEnum.ORDER_STATUS_ERROR.getResult());
        }
        request.setAttribute("orderNo", orderNo);
        request.setAttribute("totalPrice", starMallOrder.getTotalPrice());
        return "mall/pay-select";
    }

    @RepeatSubmit
    @GetMapping("/payPage")
    public String payOrder(HttpServletRequest request, @RequestParam("orderNo") String orderNo, HttpSession httpSession, @RequestParam("payType") int payType) throws UnsupportedEncodingException {
        StarMallUserVO mallUserVO = (StarMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        Long userId = mallUserVO.getUserId();
        StarMallOrder starMallOrder = judgeOrderUserId(orderNo, userId);
        // 判断订单userId
        if (!userId.equals(starMallOrder.getUserId())) {
            StarMallException.fail(ServiceResultEnum.NO_PERMISSION_ERROR.getResult());
        }
        // 判断订单状态
        if (starMallOrder.getOrderStatus() != StarMallOrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()
                || starMallOrder.getPayStatus() != PayStatusEnum.PAY_ING.getPayStatus()) {
            throw new StarMallException("订单结算异常");
        }
        request.setAttribute("orderNo", orderNo);
        request.setAttribute("totalPrice", starMallOrder.getTotalPrice());
        if (payType == 1) {
            request.setCharacterEncoding(Constants.UTF_ENCODING);
            // 初始化
            AlipayClient alipayClient = new DefaultAlipayClient(alipayConfig.getGateway(), alipayConfig.getAppId(),
                    alipayConfig.getRsaPrivateKey(), alipayConfig.getFormat(), alipayConfig.getCharset(), alipayConfig.getAlipayPublicKey(),
                    alipayConfig.getSigntype());
            // 创建API对应的request
            AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
            // 在公共参数中设置回跳和通知地址,通知地址需要公网可访问
            String url = ProjectConfig.getServerUrl() + request.getContextPath();
            alipayRequest.setReturnUrl(url + "/returnOrders/" + starMallOrder.getOrderNo() + "/" + userId);
            alipayRequest.setNotifyUrl(url + "/paySuccess?payType=1&orderNo=" + starMallOrder.getOrderNo());

            // 填充业务参数

            // 必填
            // 商户订单号，需保证在商户端不重复
            String out_trade_no = starMallOrder.getOrderNo() + new Random().nextInt(9999);
            // 销售产品码，与支付宝签约的产品码名称。目前仅支持FAST_INSTANT_TRADE_PAY
            String product_code = "FAST_INSTANT_TRADE_PAY";
            // 订单总金额，单位为元，精确到小数点后两位，取值范围[0.01,100000000]。
            String total_amount = String.valueOf(starMallOrder.getTotalPrice());
            // 订单标题
            String subject = "支付宝测试";

            // 选填
            // 商品描述，可空
            String body = "商品描述";

            alipayRequest.setBizContent("{" + "\"out_trade_no\":\"" + out_trade_no + "\"," + "\"product_code\":\""
                    + product_code + "\"," + "\"total_amount\":\"" + total_amount + "\"," + "\"subject\":\"" + subject
                    + "\"," + "\"body\":\"" + body + "\"}");
            // 请求
            String form;
            try {
                // 需要自行申请支付宝的沙箱账号、申请appID，并在配置文件中依次配置AppID、密钥、公钥，否则这里会报错。
                form = alipayClient.pageExecute(alipayRequest).getBody();//调用SDK生成表单
                request.setAttribute("form", form);
            } catch (AlipayApiException e) {
                e.printStackTrace();
            }
            return "mall/alipay";
        } else {
            return "mall/wxpay";
        }
    }

    @GetMapping("/returnOrders/{orderNo}/{userId}")
    public String returnOrderDetailPage(HttpServletRequest request,
                                        HttpSession httpSession,
                                        @PathVariable String orderNo, @PathVariable Long userId) {
        StarMallUserVO user = (StarMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        if (!Objects.equals(user.getUserId(), userId)) {
            return "error/error_5xx";
        }
        log.info("支付宝return通知数据记录：orderNo: {}, 当前登陆用户：{}", orderNo, userId);
        // NewBeeMallOrder newBeeMallOrder = judgeOrderUserId(orderNo, userId);
        // 将notifyUrl中逻辑放到此处：未支付订单更新订单状态
        StarMallOrderDetailVO orderDetailVO = starMallOrderService.getOrderDetailByOrderNo(orderNo, userId);
        if (orderDetailVO == null) {
            return "error/error_5xx";
        }
        request.setAttribute("orderDetailVO", orderDetailVO);
        return "mall/order-detail";
    }

    @PostMapping("/paySuccess")
    @ResponseBody
    public Result paySuccess(Integer payType, String orderNo, HttpServletRequest request) throws AlipayApiException {
        log.info("支付宝paySuccess通知数据记录：request.getParameterMap() is {}", JSON.toJSONString(request.getParameterMap()));
        if (payType == 1 && alipayConfig.getSigntype().equals(request.getParameter("sign_type"))
                && "trade_status_sync".equals(request.getParameter("notify_type"))
                && alipayConfig.getAppId().equals(request.getParameter("app_id"))
                && this.verifySign(request)) {
            String payResult = starMallOrderService.paySuccess(orderNo, payType);
            if (ServiceResultEnum.SUCCESS.getResult().equals(payResult)) {
                return ResultGenerator.genSuccessResult();
            } else {
                return ResultGenerator.genFailResult(payResult);
            }
        } else if (payType == 2) {
            String payResult = starMallOrderService.paySuccess(orderNo, payType);
            if (ServiceResultEnum.SUCCESS.getResult().equals(payResult)) {
                return ResultGenerator.genSuccessResult();
            } else {
                return ResultGenerator.genFailResult(payResult);
            }
        } else {
            return ResultGenerator.genFailResult("支付类型错误");
        }
    }

    /**
     * 验签
     */
    private boolean verifySign(HttpServletRequest request) throws AlipayApiException {
        //正式环境验签
        //编码
        String charset = request.getParameter("charset");
        //签名算法类型
        String signType = request.getParameter("sign_type");
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            // 乱码解决，这段代码在出现乱码时使用
            params.put(name, valueStr);
        }
        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayConfig.getAlipayPublicKey(), charset, signType);
        log.info("支付宝回调:verifySignparams={},signVerified={}", JSON.toJSONString(params), signVerified);
        return signVerified;
    }

    @RepeatSubmit
    @PutMapping("/orders/{orderNo}/cancel")
    @ResponseBody
    public Result cancelOrder(@PathVariable("orderNo") String orderNo, HttpSession httpSession) {
        StarMallUserVO user = (StarMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        String cancelOrderResult = starMallOrderService.cancelOrder(orderNo, user.getUserId());
        if (ServiceResultEnum.SUCCESS.getResult().equals(cancelOrderResult)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(cancelOrderResult);
        }
    }

    @RepeatSubmit
    @PutMapping("/orders/{orderNo}/finish")
    @ResponseBody
    public Result finishOrder(@PathVariable("orderNo") String orderNo, HttpSession httpSession) {
        StarMallUserVO user = (StarMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        String finishOrderResult = starMallOrderService.finishOrder(orderNo, user.getUserId());
        if (ServiceResultEnum.SUCCESS.getResult().equals(finishOrderResult)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(finishOrderResult);
        }
    }

    /**
     * 判断订单关联用户id和当前登陆用户是否一致
     *
     * @param orderNo 订单编号
     * @param userId  用户ID
     * @return 验证成功后返回订单对象
     */
    private StarMallOrder judgeOrderUserId(String orderNo, Long userId) {
        StarMallOrder starMallOrder = starMallOrderService.getNewBeeMallOrderByOrderNo(orderNo);
        // 判断订单userId
        if (starMallOrder == null || !starMallOrder.getUserId().equals(userId)) {
            throw new StarMallException("当前订单用户异常");
        }
        return starMallOrder;
    }
}
