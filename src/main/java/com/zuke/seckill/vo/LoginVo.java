package com.zuke.seckill.vo;

import com.zuke.seckill.annotation.IsMobile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

//接收用户登录时，发送的信息
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginVo {

    @IsMobile(required = true)
    @NotNull
    private String mobile;

    @NotBlank
    @Length(min = 32)
    private String password;
}
