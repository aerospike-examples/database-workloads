package com.aerospike.timf.databases;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.aerospike.client.Host;
import com.aerospike.client.IAerospikeClient;
import com.aerospike.client.policy.ClientPolicy;
import com.aerospike.client.policy.TlsPolicy;
import com.aerospike.client.proxy.AerospikeClientProxy;
import com.aerospike.mapper.tools.AeroMapper;
import com.aerospike.mapper.tools.configuration.ClassConfig;
import com.aerospike.timf.model.Person;
import com.aerospike.timf.service.DatabaseConfigItem;

@Database(name = "Aerospike", version = "Managed")
@Service
public class AerospikeManagedDatabase extends AerospikeDatabaseBase implements DatabaseFunctions<AerospikeInstanceDetails> {
    @Override
    public List<DatabaseConfigItem> getConfigItems() {
        return super.getCommonConfigItems();
    }

    @Override
    protected String getDefaultHost() {
        return null;
    }
    
    @Override
    protected int getDefaultPort() {
        return 4000;
    }
    
    @Override
    protected boolean isUserRequired() {
        return true;
    }
    
    @Override
    public AerospikeInstanceDetails connectInstance(Map<String, Object> configParameters) {
        String host = (String) configParameters.get(HOST_CONFIG_NAME);
        int port = (int) configParameters.get(PORT_CONFIG_NAME);
        String user = (String) configParameters.get(USER_CONFIG_NAME);
        String password = (String) configParameters.get(PASSWORD_CONFIG_NAME);
        String namespaceName = (String) configParameters.get(NAMESPACE_CONFIG_NAME);
        
        ClientPolicy cp = new ClientPolicy();
        cp.user = user;
        cp.password = password;
        cp.tlsPolicy = new TlsPolicy();
        
        IAerospikeClient client = new AerospikeClientProxy(cp, new Host[] { new Host(host, port) } );
        ClassConfig personConfig = new ClassConfig.Builder(Person.class).withNamespace(namespaceName).build();
        AeroMapper mapper = new AeroMapper.Builder(client).withClassConfigurations(personConfig).build();
        return new AerospikeInstanceDetails(client, mapper);
    }

}
