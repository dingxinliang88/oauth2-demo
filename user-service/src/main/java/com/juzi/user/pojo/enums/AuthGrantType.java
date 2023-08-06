package com.juzi.user.pojo.enums;

/**
 * 权限认证类别
 *
 * @author codejuzi
 */
public enum AuthGrantType {
    implicit,
    client_credentials,
    authorization_code,
    refresh_token,
    password
}