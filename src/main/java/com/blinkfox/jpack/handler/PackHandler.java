package com.blinkfox.jpack.handler;

import com.blinkfox.jpack.entity.PackInfo;

/**
 * 打包成各个平台所需要的部署包的处理器接口.
 *
 * @author blinkfox on 2019-04-28.
 */
public interface PackHandler {

    /**
     * 根据打包的相关参数进行打包的方法.
     *
     * @param packInfo 打包的相关参数实体
     */
    void pack(PackInfo packInfo);

}
