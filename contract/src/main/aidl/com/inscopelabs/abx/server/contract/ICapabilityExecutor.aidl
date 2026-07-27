package com.inscopelabs.abx.server.contract;

import com.inscopelabs.abx.server.contract.CapabilityRequest;
import com.inscopelabs.abx.server.contract.CapabilityResponse;

interface ICapabilityExecutor {
    CapabilityResponse execute(in CapabilityRequest request);
}
