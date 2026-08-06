package com.inscopelabs.abx.server.contractdispatcher;

import com.inscopelabs.abx.server.contractdispatcher.DispatcherRequest;
import com.inscopelabs.abx.server.contractdispatcher.DispatcherResponse;

interface IDispatcherExecutor {
    DispatcherResponse execute(in DispatcherRequest request);
}
