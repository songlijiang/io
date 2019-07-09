package com.slj.io.rpc.domain;

import lombok.Data;

import java.util.List;

/**
 * @author songlijiang
 * @version 2019/6/26 15:05
 */
@Data
public class InvokeRequest {

    private String serviceName;

    private String methodName;

    private List<Object> args;

}
