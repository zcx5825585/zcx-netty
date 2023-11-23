package org.zcx.netty.common.exception;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private final Log log = LogFactory.getLog(this.getClass());

    @ExceptionHandler(BeanException.class)
    public String handleBeanException(BeanException e, HttpServletRequest request) {
        log.error("Bean异常：" + e.getMessage());
        return "Bean异常：" + e.getMessage();
    }
    @ExceptionHandler(HandlerException.class)
    public String handleHandlerException(HandlerException e, HttpServletRequest request) {
        log.error("Handler异常：" + e.getMessage());
        return "Handler异常：" + e.getMessage();
    }

}
