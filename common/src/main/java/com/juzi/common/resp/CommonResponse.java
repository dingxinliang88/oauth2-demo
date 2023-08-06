package com.juzi.common.resp;

import lombok.Builder;
import lombok.Data;

/**
 * @author codejuzi
 */
@Data
@Builder
public class CommonResponse {

    private Integer code;

    private String msg;

    private Object content;
}
