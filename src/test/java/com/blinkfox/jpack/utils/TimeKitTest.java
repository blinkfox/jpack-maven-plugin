package com.blinkfox.jpack.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * TimeKitTest.
 *
 * @author blinkfox on 2019-05-11.
 * @since v1.1.0
 */
public class TimeKitTest {

    /**
     * 测试 join 方法的边界情况.
     */
    @Test
    public void join() {
        Assert.assertEquals(TimeKit.EMPTY, TimeKit.join(""));
        Assert.assertEquals(TimeKit.EMPTY, TimeKit.join(null, ""));
        Assert.assertEquals("ab", TimeKit.join("a", null, "b"));
    }

    /**
     * 测试 convertTime 方法.
     */
    @Test
    public void convertTime() {
        Assert.assertEquals("5623 ns", TimeKit.convertTime(5_623));
        Assert.assertEquals("0.22 ms", TimeKit.convertTime(215_623));
        Assert.assertEquals("32.53 ms", TimeKit.convertTime(32_525_623));
        Assert.assertEquals("1.53 s", TimeKit.convertTime(1_526_165_603));
        Assert.assertEquals("1.67 min", TimeKit.convertTime(100_000_000_000L));
        Assert.assertEquals("5.0 h", TimeKit.convertTime(18_000_000_000_000L));
        Assert.assertEquals("5.0 h", TimeKit.convertTime(18_000_000_000_000L));
        Assert.assertEquals("3.0 d", TimeKit.convertTime(259_200_000_000_000L));
    }

}
