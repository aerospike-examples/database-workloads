package com.aerospike.timf.service;

import java.util.Map;

import lombok.Data;

@Data
public class DatabaseParameters {
    private String name;
    private String database;
    private String version;
    private Map<String, Object> configParams;
}
