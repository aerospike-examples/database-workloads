package com.aerospike.timf.databases;

import com.aerospike.client.IAerospikeClient;
import com.aerospike.mapper.tools.AeroMapper;

public class AerospikeInstanceDetails {
    private final IAerospikeClient client;
    private final AeroMapper mapper;

    public AerospikeInstanceDetails(IAerospikeClient client, AeroMapper mapper) {
        super();
        this.client = client;
        this.mapper = mapper;
    }
    
    public IAerospikeClient getClient() {
        return client;
    }
    
    public AeroMapper getMapper() {
        return mapper;
    }
}
