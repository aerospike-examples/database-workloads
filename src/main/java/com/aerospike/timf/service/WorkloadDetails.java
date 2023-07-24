package com.aerospike.timf.service;

import java.util.Map;

import lombok.Data;

@Data
public class WorkloadDetails {
    private String name;
    private String jobName;
    private Map<String, Object> parameters;
}
