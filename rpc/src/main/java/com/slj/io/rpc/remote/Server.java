package com.slj.io.rpc.remote;

import com.slj.io.rpc.domain.InvokeRequest;
import com.slj.io.rpc.domain.InvokeResponse;

/**
 * @author songlijiang
 * @version 2019/6/26 15:01
 */
public interface Server {

    void start();

    void stop();

    InvokeResponse invoke(InvokeRequest invokeRequest);

}
