package com.zuke.seckill.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

//秒杀消息对象
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SecKillMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private User user;
    private long goodsId;
}
