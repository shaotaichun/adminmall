package com.transo.store_admin_backend.Interceptor;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Joiner;
import com.transo.store_admin_backend.Annotations.LoginRequired;
import com.transo.store_bean.Entity.Role;
import com.transo.store_bean.Entity.RoleAuthority;
import com.transo.store_tools.Tools.HttpClientUtils;
import com.transo.store_tools.Tools.IpUtils;
import com.transo.store_tools.Tools.JsonResult;
import com.transo.store_tools.Tools.JwtUtil;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.lang.System.out;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Autowired
    RedisTemplate redisTemplate;
    private String token_key = "ATGUIGU_GMALL_KEY";
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1.判断是否存在注解
        if(!(handler instanceof HandlerMethod)){
            return true;
        }
        HandlerMethod method = (HandlerMethod)handler;
        boolean hasLoginAnnotation=method.getMethod().isAnnotationPresent(LoginRequired.class);
        if(!hasLoginAnnotation){
            //不存在LoginRequired注解，则直接通过
            return true;
        }
        LoginRequired loginRequired=method.getMethod().getAnnotation(LoginRequired.class);
        //2.required=false,则无需检查是否登录
        if(!loginRequired.required()){
            return true;
        }
        //3.登录状态检查,使用response返回指定信息
        String salt = IpUtils.getIpAddr(request);
        String token = request.getHeader("token");
        redisTemplate.expire("token"+ JwtUtil.decode(token,token_key,salt).get("phone"),15, TimeUnit.MINUTES);
        if (token == null ||"".equals(token)||JwtUtil.decode(token,token_key,salt) == null||JwtUtil.decode(token,token_key,salt).get("phone") == null ||redisTemplate.opsForValue().get("token"+ JwtUtil.decode(token,token_key,salt).get("phone")) ==null||!IpUtils.getIpAddr(request).equals(JwtUtil.decode(token,token_key,salt).get("ip").toString())){
            int myCode = 600;
            response.setStatus(myCode);
            request.setAttribute("javax.servlet.error.status_code", myCode);
            request.setCharacterEncoding("UTF-8");
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().write(JSON.toJSON(new JsonResult("token无效！",600,"FAIL")).toString());
            response.getWriter().close();
            return false;
        }
        int userId = Integer.parseInt(JwtUtil.decode(token,token_key,salt).get("userId").toString());
        if (userId != 232){
            int myCode = 700;
            response.setStatus(myCode);
            request.setAttribute("javax.servlet.error.status_code", myCode);
            request.setCharacterEncoding("UTF-8");
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().write(JSON.toJSON(new JsonResult("没有操作权限!",700,"FAIL")).toString());
            response.getWriter().close();
            return false;
        }
       /* String url = request.getServletPath();
        String urlJudge = "http://127.0.0.1:8066/authority/getAuthorityForJudge/" + userId;
        String resultJudge = HttpClientUtils.httpGet(urlJudge);
        JSONObject jsonObjectJudge1 = JSONObject.fromObject(resultJudge);
        Map classMap = new HashMap();
        classMap.put("authority", RoleAuthority.class);
        Role role=(Role)JSONObject.toBean(jsonObjectJudge1, Role.class,classMap);
       if (!judgeAuthority(role,getRealUrl(handler,url),"4")){
           request.setCharacterEncoding("UTF-8");
           response.setContentType("text/html;charset=utf-8");
           response.getWriter().write(JSON.toJSON(new JsonResult("没有操作权限!")).toString());
           response.getWriter().close();
           return false;
       }*/
        return true;
    }

    public Boolean judgeAuthority(Role role,String url,String roleType){
        List<RoleAuthority> authority = role.getAuthority();
        for (RoleAuthority all:authority) {
            if (roleType.equals(all.getAuthorityRole())&&url.equals(all.getApiAddress())){
                return  true;

            }
        }
        out.println(url);
    return false;
    }
    private String getRealUrl(Object handler,String url){
        Annotation[][] parameterAnnotations = ((HandlerMethod) handler).getMethod().getParameterAnnotations();
        int i = 0;
        for (Annotation[] annotations : parameterAnnotations) {
            for (Annotation annotation : annotations) {
                if(annotation instanceof PathVariable){
                    i++;
                    break;
                }
            }
        }
        if (i == 0){
            return url;
        }
        List<String> split = Arrays.asList(url.split("\\/"));
        List<String> subList = split.subList(0, split.size() - i);
        String join = Joiner.on("/").join(subList);
        return join;
    }
}