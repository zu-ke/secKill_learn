package com.zuke.seckill;

import com.zuke.seckill.util.MD5Util;
import org.junit.jupiter.api.Test;

public class TestMD5Utils {

    @Test
    public void t1(){
        //明文密码12345
        //获取到明文密码的中间密码【即客户端加密加盐后在网络上传输的密码】
        String midPass = MD5Util.inputPassToMidPass("12345");
        System.out.println("midPass:"+midPass);

        String dbPass1 = MD5Util.midPassToDbPass(midPass, "33GoVAJW4tr5W5");
        System.out.println("dbPass1:"+dbPass1);

        //我将其他中间方法改成private后，可以直接调用下面的最终方法

        //最终存储数据库的密码
        String dbPass2 = MD5Util.inputPassToDbPass("12345", "33GoVAJW4tr5W5");
        System.out.println("dbPass:"+dbPass2);
    }
}
