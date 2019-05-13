package com.blinkfox.jpack;

import com.blinkfox.jpack.handler.PlatformPackContext;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * 打包的 Maven 插件主入口 Mojo 类.
 *
 * @author blinkfox on 2019-04-28.
 */
@Mojo(name = "build", defaultPhase = LifecyclePhase.PACKAGE)
public class PackBuildMojo extends AbstractBaseMojo {

    /**
     * 正式执行构建各平台包的方法.
     */
    @Override
    protected void exec() {
        // 在各平台下执行打包.
        new PlatformPackContext().pack(super.platforms, super.buildPackInfo());
    }

}
