
package star.xingxing.mall.controller.mall;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import star.xingxing.mall.common.Constants;
import star.xingxing.mall.common.ServiceResultEnum;
import star.xingxing.mall.controller.vo.StarMallUserVO;
import star.xingxing.mall.entity.MallUser;
import star.xingxing.mall.exception.StarMallException;
import star.xingxing.mall.service.StarMallCouponService;
import star.xingxing.mall.service.StarMallUserService;
import star.xingxing.mall.util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Controller
@Slf4j
public class PersonalController {

    @Resource
    private StarMallUserService starMallUserService;

    @Resource
    private StarMallCouponService starMallCouponService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private MailClientUtil mailClientUtil;


    @GetMapping("/personal")
    public String personalPage(HttpServletRequest request,
                               HttpSession httpSession) {
        request.setAttribute("path", "personal");
        return "mall/personal";
    }

    @GetMapping("/logout")
    public String logout(HttpSession httpSession) {
        httpSession.removeAttribute(Constants.MALL_USER_SESSION_KEY);
        return "mall/login";
    }

    @GetMapping({"/login", "login.html"})
    public String loginPage(HttpServletRequest request) {
        if (HttpUtil.isAjaxRequest(request)) {
            throw new StarMallException("请先登陆！");
        }
        return "mall/login";
    }

    @GetMapping({"/register", "register.html"})
    public String registerPage() {
        return "mall/register";
    }

    @GetMapping("/personal/addresses")
    public String addressesPage() {
        return "mall/addresses";
    }

    @GetMapping("/mail/captcha")
    @ResponseBody
    public Result getCapture(@RequestParam(value = "email", required = true) String email) {
        log.info("邮箱：{}", email);
        if (StringUtils.isEmpty(email)) {
            return ResultGenerator.genFailResult("邮箱不能为空");
        }
        if (Objects.equals(stringRedisTemplate.opsForValue().get(email + ":count"), "5")) {
            return ResultGenerator.genFailResult("今日发送次数已达上限");
        }
        String code4String = ValidateCodeUtils.generateValidateCode4String(4);
        stringRedisTemplate.opsForValue().set(email, code4String, 5, TimeUnit.MINUTES);
        if (stringRedisTemplate.opsForValue().get(email + ":count") != null) {
            stringRedisTemplate.opsForValue().increment(email + ":count", 1);
        } else {
            stringRedisTemplate.opsForValue().set(email + ":count", "1", 24, TimeUnit.HOURS);
        }
        mailClientUtil.sendMail(email, "star-mall注册验证码", code4String);
        return ResultGenerator.genSuccessResult();
    }

    @PostMapping("/login")
    @ResponseBody
    public Result login(@RequestParam("loginName") String loginName,
                        @RequestParam("verifyCode") String verifyCode,
                        @RequestParam("password") String password,
                        HttpSession httpSession) {
        if (StringUtils.isEmpty(loginName)) {
            return ResultGenerator.genFailResult(ServiceResultEnum.LOGIN_NAME_NULL.getResult());
        }
        if (StringUtils.isEmpty(password)) {
            return ResultGenerator.genFailResult(ServiceResultEnum.LOGIN_PASSWORD_NULL.getResult());
        }
        if (StringUtils.isEmpty(verifyCode)) {
            return ResultGenerator.genFailResult(ServiceResultEnum.LOGIN_VERIFY_CODE_NULL.getResult());
        }
        String kaptchaCode = httpSession.getAttribute(Constants.MALL_VERIFY_CODE_KEY) + "";
        if (StringUtils.isEmpty(kaptchaCode) || !verifyCode.toLowerCase().equals(kaptchaCode)) {
            return ResultGenerator.genFailResult(ServiceResultEnum.LOGIN_VERIFY_CODE_ERROR.getResult());
        }
        httpSession.setAttribute(Constants.MALL_VERIFY_CODE_KEY, null);
        String loginResult = starMallUserService.login(loginName, MD5Util.MD5Encode(password, Constants.UTF_ENCODING), httpSession);
        //登录成功
        if (ServiceResultEnum.SUCCESS.getResult().equals(loginResult)) {
            //删除session中的verifyCode
            httpSession.removeAttribute(Constants.MALL_VERIFY_CODE_KEY);
            return ResultGenerator.genSuccessResult();
        }
        //登录失败
        return ResultGenerator.genFailResult(loginResult);
    }

    @PostMapping("/register")
    @ResponseBody
    public Result register(@RequestParam("loginName") String loginName,
                           @RequestParam("verifyCode") String verifyCode,
                           @RequestParam("password") String password,
                           @RequestParam("mailCode") String mailCode,
                           HttpSession httpSession) {
        log.error("loginName:{},verifyCode:{},password:{},mailCode:{}", loginName, verifyCode, password, mailCode);
        if (StringUtils.isEmpty(loginName)) {
            return ResultGenerator.genFailResult(ServiceResultEnum.LOGIN_NAME_NULL.getResult());
        }
        if (StringUtils.isEmpty(mailCode)) {
            return ResultGenerator.genFailResult(ServiceResultEnum.MAIL_CODE_ERROR.getResult());
        }
        if (StringUtils.isEmpty(password)) {
            return ResultGenerator.genFailResult(ServiceResultEnum.LOGIN_PASSWORD_NULL.getResult());
        }
        if (StringUtils.isEmpty(verifyCode)) {
            return ResultGenerator.genFailResult(ServiceResultEnum.LOGIN_VERIFY_CODE_NULL.getResult());
        }
        String s = stringRedisTemplate.opsForValue().get(loginName);


        if (!mailCode.equals(s)) {
            return ResultGenerator.genFailResult("邮箱验证码错误");
        }

        String kaptchaCode = httpSession.getAttribute(Constants.MALL_VERIFY_CODE_KEY) + "";
        if (StringUtils.isEmpty(kaptchaCode) || !verifyCode.toLowerCase().equals(kaptchaCode)) {
            return ResultGenerator.genFailResult(ServiceResultEnum.LOGIN_VERIFY_CODE_ERROR.getResult());
        }
        httpSession.setAttribute(Constants.MALL_VERIFY_CODE_KEY, null);
        String registerResult = starMallUserService.register(loginName, password);
        //注册成功
        if (ServiceResultEnum.SUCCESS.getResult().equals(registerResult)) {
            //删除session中的verifyCode
            httpSession.removeAttribute(Constants.MALL_VERIFY_CODE_KEY);
            return ResultGenerator.genSuccessResult();
        }
        //注册失败
        return ResultGenerator.genFailResult(registerResult);
    }

    @PostMapping("/personal/updateInfo")
    @ResponseBody
    public Result updateInfo(@RequestBody MallUser mallUser, HttpSession httpSession) {
        StarMallUserVO mallUserTemp = starMallUserService.updateUserInfo(mallUser, httpSession);
        if (mallUserTemp == null) {
            return ResultGenerator.genFailResult("修改失败");
        } else {
            //返回成功
            return ResultGenerator.genSuccessResult();
        }
    }
}
