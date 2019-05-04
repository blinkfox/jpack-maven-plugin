package com.blinkfox.jpack.consts;

import org.junit.Assert;
import org.junit.Test;

/**
 * PlatformEnumTest.
 *
 * @author blinkfox on 2019-05-04.
 */
public class PlatformEnumTest {

    @Test
    public void of() {
        Assert.assertEquals(PlatformEnum.LINUX, PlatformEnum.of("linux"));
        Assert.assertNull(PlatformEnum.of("a"));
    }

}
