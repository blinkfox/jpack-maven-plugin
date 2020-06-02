package com.blinkfox.jpack.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * Registry 远程仓库的权限认证信息实体类.
 *
 * @author blinkfox on 2020-06-02.
 * @since v1.4.0
 */
@Getter
@Setter
public class RegistryUser {

    /**
     * 登录认证的用户名称.
     */
    private String username;

    /**
     * 登录认证密码.
     */
    private String password;

    /**
     * 用户的邮箱.
     */
    private String email;

    /**
     * 服务地址.
     */
    private String serverAddress;

    /**
     * ID 标识.
     */
    private String identityToken;

}
