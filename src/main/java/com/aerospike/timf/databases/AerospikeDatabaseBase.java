package com.aerospike.timf.databases;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.aerospike.timf.model.Person;
import com.aerospike.timf.service.DatabaseConfigItem;

public abstract class AerospikeDatabaseBase  {
    protected static final String NAMESPACE_CONFIG_NAME = "Namespace";
    protected static final String HOST_CONFIG_NAME = "Host";
    protected static final String PORT_CONFIG_NAME = "Port";
    protected static final String USER_CONFIG_NAME = "User";
    protected static final String PASSWORD_CONFIG_NAME = "Password";
    protected static final String USE_TLS_CONFIG_NAME = "UseTLS";
    protected static final String TLS_HOST_CONFIG_NAME = "TLSHost";
    
    public List<DatabaseConfigItem> getCommonConfigItems() {
        return new ArrayList<>(Arrays.asList(
                new DatabaseConfigItem(NAMESPACE_CONFIG_NAME, "Namespace", "", "test"),
                new DatabaseConfigItem(HOST_CONFIG_NAME, "Host", "Host identity, either IP address or DNS name", getDefaultHost()),
                new DatabaseConfigItem(PORT_CONFIG_NAME, "Port", "Port to connect", "Port", getDefaultPort()),
                new DatabaseConfigItem(USER_CONFIG_NAME, "User", "User name to connect to database (Optional)", DatabaseConfigItem.Type.STRING, isUserRequired()),
                new DatabaseConfigItem(PASSWORD_CONFIG_NAME, "Password", "User Password", DatabaseConfigItem.Type.PASSWORD, isUserRequired())
        ));
    }

    protected abstract int getDefaultPort();
    protected abstract String getDefaultHost();
    protected abstract boolean isUserRequired();
    
    public void disconnectInstance(AerospikeInstanceDetails params) {
        params.getClient().close();
    }

    public void insertPerson(Object instance, Person person) {
        AerospikeInstanceDetails details = (AerospikeInstanceDetails)instance;
        details.getMapper().save(person);
    }

    public void updatePerson(Object instance, Person person) {
        AerospikeInstanceDetails details = (AerospikeInstanceDetails)instance;
        details.getMapper().save(person);
    }

    public Person readPerson(Object instance, long id) {
        AerospikeInstanceDetails details = (AerospikeInstanceDetails)instance;
        return details.getMapper().read(Person.class, id);
    }

}
