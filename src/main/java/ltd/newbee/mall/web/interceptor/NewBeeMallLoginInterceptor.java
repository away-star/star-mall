
package ltd.newbee.mall.web.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ltd.newbee.mall.common.Constants;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * star-mall系统身份验证拦截器
 *
 * @author xingxing
 * @email 2064989403@qq.com

 */
@Component
public class NewBeeMallLoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        // 秒杀请求放过（压力测试使用）
        if (request.getRequestURI().startsWith("/seckillExecution")) {
            return true;
        }
        if (null == request.getSession().getAttribute(Constants.MALL_USER_SESSION_KEY)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
