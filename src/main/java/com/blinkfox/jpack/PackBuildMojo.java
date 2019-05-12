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
public class PackBuildMojo extends BaseMojo {

    /**
     * 执行该 Mojo 的方法.
     */
    @Override
    public void execute() {
        // 初始化系统日志和打印 logo.
        super.initLogoAndPrintLogo();

        // 在各平台下执行打包.
        new PlatformPackContext().pack(super.platforms, super.buildPackInfo());
    }

}
