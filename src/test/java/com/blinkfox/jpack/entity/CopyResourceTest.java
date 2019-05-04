package com.blinkfox.jpack.entity;

import org.junit.Assert;
import org.junit.Test;

/**
 * CopyResourceTest.
 *
 * @author blinkfox on 2019-05-04.
 */
public class CopyResourceTest {

    /**
     * 测试 CopyResource 的 getter 和 setter 方法.
     */
    @Test
    public void test() {
        String from = "/abc/test.md";
        String to = ".";
        CopyResource copyResource = new CopyResource();
        copyResource.setFrom(from);
        copyResource.setTo(to);

        Assert.assertEquals(from, copyResource.getFrom());
        Assert.assertEquals(to, copyResource.getTo());
    }

}
