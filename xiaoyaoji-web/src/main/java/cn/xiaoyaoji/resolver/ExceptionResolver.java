package cn.xiaoyaoji.resolver;

import cn.xiaoyaoji.core.common.Result;
import cn.xiaoyaoji.core.common._HashMap;
import cn.xiaoyaoji.core.exception.NotLoginException;
import cn.xiaoyaoji.core.exception.ServiceException;
import com.alibaba.fastjson.support.spring.FastJsonJsonView;
import org.apache.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author: zhoujingjie
 * @Date: 17/3/30
 */
public class ExceptionResolver extends SimpleMappingExceptionResolver {
    private Logger logger = Logger.getLogger(getClass());
    @Override
    protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String from = null;
        if(request.getMethod().equals("GET")){
            from = request.getRequestURI();
            if(request.getQueryString()!=null){
                from +="?"+ request.getQueryString();
            }
            try {
                from = URLEncoder.encode(from,"UTF-8");
            } catch (UnsupportedEncodingException e) {
            }
        }
        ex = getActualException(ex);
        boolean isXhr = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
        if(isXhr){
            response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
            ModelAndView mv = new ModelAndView(new FastJsonJsonView());
            String errorMsg = ex.getMessage();
            if(!(ex instanceof IllegalArgumentException) && !(ex instanceof ServiceException)){
                logger.error("",ex);
                errorMsg = "系统错误";
            }
            if (ex instanceof NotLoginException) {
                mv.addAllObjects(new _HashMap<String, Object>().add("code", Result.NOT_LOGIN).add("errorMsg", "会话已过期"));
            }else {
                mv.addAllObjects(new _HashMap<String, Object>().add("code", -1).add("errorMsg", errorMsg));
            }
            return mv;
        }

        if(ex instanceof NotLoginException){
            String redirectURL = "redirect:/login";
            if(from!=null){
                redirectURL +="?refer="+from;
            }
            return new ModelAndView(redirectURL);
        }
        return super.doResolveException(request, response, handler, ex);
    }

    private Exception getActualException(Exception e){
        if(e.getCause() != null && e.getCause() instanceof Exception){
            e = (Exception) e.getCause();
            return getActualException(e);
        }
        return e;
    }
}
