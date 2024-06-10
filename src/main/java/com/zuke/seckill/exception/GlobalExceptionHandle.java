package com.zuke.seckill.exception;

import com.zuke.seckill.vo.RespBean;
import com.zuke.seckill.vo.RespBeanEnum;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandle {

    //处理所有异常
    @ExceptionHandler(Exception.class)
    public RespBean ExceptionHandle(Exception exception) {

        //如果是全局异常，就正常处理
        if (exception instanceof GlobalException){
            GlobalException seckillException = (GlobalException) exception;
            return RespBean.error(seckillException.getRespBeanEnum());
        }else if (exception instanceof BindException){
            BindException bindException = (BindException) exception;
            RespBean respBean = RespBean.error(RespBeanEnum.BING_ERROR);
            respBean.setMsg("参数校验异常：" + bindException.getBindingResult().getAllErrors().get(0).getDefaultMessage());
            return respBean;
        }
        return RespBean.error(RespBeanEnum.ERROR);
    }
}
