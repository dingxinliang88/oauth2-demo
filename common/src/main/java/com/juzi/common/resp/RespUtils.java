package com.juzi.common.resp;

/**
 * @author codejuzi
 */
public class RespUtils {

    public static CommonResponse success(Object content) {
        return CommonResponse.builder()
                .code(RespCodeEnum.SUCCESS.getCode())
                .msg(RespCodeEnum.SUCCESS.getMsg())
                .content(content)
                .build();
    }

    public static CommonResponse fail(Integer code, Object content, String msg) {
        return CommonResponse.builder()
                .code(code)
                .msg(msg)
                .content(content)
                .build();

    }

}
