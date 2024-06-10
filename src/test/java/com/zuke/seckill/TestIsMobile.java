package com.zuke.seckill;

import com.zuke.seckill.util.ValidatorUtil;
import org.junit.jupiter.api.Test;

public class TestIsMobile {

    @Test
    public void t1(){
        boolean mobile = ValidatorUtil.isMobile("13345678912");
        System.out.println(mobile);
    }
}
