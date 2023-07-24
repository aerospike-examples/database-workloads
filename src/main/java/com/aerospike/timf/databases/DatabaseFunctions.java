package com.aerospike.timf.databases;

import java.util.List;
import java.util.Map;

import com.aerospike.timf.model.Person;
import com.aerospike.timf.service.DatabaseConfigItem;

public interface DatabaseFunctions<T> {
    List<DatabaseConfigItem> getConfigItems();
    /**
     * Attach to an instance of the database. The parameters passed to this function will be the user selected values for the config items.
     * <p>
     * If the connection fails, throw an exception, the message of which will be displayed to the user.
     * </p>
     * @param configParameters
     * @return - An object used to store the state of this instance. This will be passed back into the methods to execute work.
     */
    T connectInstance(Map<String, Object> configParameters);
    
    void disconnectInstance(T params);
    
    // For now, until we clean up the workload management
    void insertPerson(Object databaseConnection, Person person);
    void updatePerson(Object instance, Person person);
    Person readPerson(Object instance, long id);
}
