package com.slj.io.rpc.domain;

import lombok.Data;

/**
 * @author songlijiang
 * @version 2019/6/26 15:07
 */
@Data
public class InvokeResponse<T> {

    private int status;

    private T data;

}
