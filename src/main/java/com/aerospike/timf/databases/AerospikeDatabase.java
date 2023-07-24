package com.aerospike.timf.databases;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Host;
import com.aerospike.client.IAerospikeClient;
import com.aerospike.client.policy.ClientPolicy;
import com.aerospike.client.policy.TlsPolicy;
import com.aerospike.mapper.tools.AeroMapper;
import com.aerospike.mapper.tools.configuration.ClassConfig;
import com.aerospike.timf.model.Person;
import com.aerospike.timf.service.DatabaseConfigItem;

@Database(name = "Aerospike", version = "Standard")
@Service
public class AerospikeDatabase extends AerospikeDatabaseBase implements DatabaseFunctions<AerospikeInstanceDetails> {
    @Override
    public List<DatabaseConfigItem> getConfigItems() {
        List<DatabaseConfigItem> allItems = super.getCommonConfigItems();
        allItems.add(new DatabaseConfigItem(USE_TLS_CONFIG_NAME, "Use TLS", "Use TLS for secure communications between client and server", false));
        allItems.add(new DatabaseConfigItem(TLS_HOST_CONFIG_NAME, "TLS host name",
                        "The TLS host name. This should match the certificate name, and is only needed if Use TLS is checked", 
                        DatabaseConfigItem.Type.STRING, false));
        return allItems;
    }

    @Override
    protected String getDefaultHost() {
        return "127.0.0.1";
    }
    @Override
    protected int getDefaultPort() {
        return 3000;
    }
    @Override
    protected boolean isUserRequired() {
        return false;
    }
    
    @Override
    public AerospikeInstanceDetails connectInstance(Map<String, Object> configParameters) {
        String host = (String) configParameters.get(HOST_CONFIG_NAME);
        int port = (int) configParameters.get(PORT_CONFIG_NAME);
        String user = (String) configParameters.get(USER_CONFIG_NAME);
        String password = (String) configParameters.get(PASSWORD_CONFIG_NAME);
        boolean useTls = (Boolean) configParameters.get(USE_TLS_CONFIG_NAME);
        String tlsName = (String) configParameters.get(TLS_HOST_CONFIG_NAME);
        String namespaceName = (String) configParameters.get(NAMESPACE_CONFIG_NAME);
        
        ClientPolicy cp = new ClientPolicy();
        cp.user = user;
        cp.password = password;
        if (useTls) {
            cp.tlsPolicy = new TlsPolicy();
        }
        
        IAerospikeClient client = new AerospikeClient(cp, new Host[] { new Host(host, tlsName, port) } );
        ClassConfig personConfig = new ClassConfig.Builder(Person.class).withNamespace(namespaceName).build();
        AeroMapper mapper = new AeroMapper.Builder(client).withClassConfigurations(personConfig).build();
        return new AerospikeInstanceDetails(client, mapper);
    }
}
