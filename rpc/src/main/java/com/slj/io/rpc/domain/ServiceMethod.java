package com.slj.io.rpc.domain;

import lombok.Data;

import java.lang.reflect.Method;

/**
 * @author songlijiang
 * @version 2019/7/5 15:51
 */
@Data
public class ServiceMethod {

    private String id;

    private Object Service;

    private Class serviceClass;

    private Method method;



}
