package com.aerospike.timf.controller;

import java.util.List;
import java.util.Map;

import com.aerospike.timf.service.WorkloadExecutor.State;
import com.aerospike.timf.timing.Sample;

public class ServerStatus {
    private final Map<String, State> workloadStates;
    private final List<Sample> activeWorkloadTimings;
    private final long activeWorkloadTenthsPercentComplete;
    
    public ServerStatus(Map<String, State> workloadStates, List<Sample> activeWorkloadTimings,
            long activeWorkloadTenthsPercentComplete) {
        super();
        this.workloadStates = workloadStates;
        this.activeWorkloadTimings = activeWorkloadTimings;
        this.activeWorkloadTenthsPercentComplete = activeWorkloadTenthsPercentComplete;
    }
    public Map<String, State> getWorkloadStates() {
        return workloadStates;
    }
    public List<Sample> getActiveWorkloadTimings() {
        return activeWorkloadTimings;
    }
    public long getActiveWorkloadTenthsPercentComplete() {
        return activeWorkloadTenthsPercentComplete;
    }
}
