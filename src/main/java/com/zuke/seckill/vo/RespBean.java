package com.zuke.seckill.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RespBean {

    private long code;
    private String msg;
    private Object data;

    //成功
    public static RespBean success() {
        return new RespBean(RespBeanEnum.success.getCode(), RespBeanEnum.success.getMsg(), null);
    }

    //成功，同时携带数据返回
    public static RespBean success(Object data) {
        return new RespBean(RespBeanEnum.success.getCode(), RespBeanEnum.success.getMsg(), data);
    }

    //失败
    public static RespBean error(RespBeanEnum respBeanEnum) {
        return new RespBean(respBeanEnum.getCode(), respBeanEnum.getMsg(), null);
    }

    //失败，同时携带数据返回
    public static RespBean error(RespBeanEnum respBeanEnum, Object data) {
        return new RespBean(respBeanEnum.getCode(), respBeanEnum.getMsg(), data);
    }
}
