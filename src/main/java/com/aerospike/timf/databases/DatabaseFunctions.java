package com.aerospike.timf.databases;

import java.util.List;
import java.util.Map;

import com.aerospike.timf.model.CreditCard;
import com.aerospike.timf.model.Person;
import com.aerospike.timf.model.Transaction;
import com.aerospike.timf.service.DatabaseConfigItem;

public interface DatabaseFunctions<C> {
    List<DatabaseConfigItem> getConfigItems();
    /**
     * Attach to an instance of the database. The parameters passed to this function will be the user selected values for the config items.
     * <p>
     * If the connection fails, throw an exception, the message of which will be displayed to the user.
     * </p>
     * @param configParameters
     * @return - An object used to store the state of this instance. This will be passed back into the methods to execute work.
     */
    C connectInstance(Map<String, Object> configParameters);
    
    void disconnectInstance(C params);
    
    // For now, until we clean up the workload management
    void insertPerson(C databaseConnection, Person person) throws Exception;
    void updatePerson(C databaseConnection, Person person) throws Exception;
    Person readPerson(C databaseConnection, long id) throws Exception;
    
    // Credit card processing
    void insertCreditCard(C databaseConnection, CreditCard card) throws Exception;
    void addTransactionToCreditCard(C databaseConnection, CreditCard card, Transaction transaction) throws Exception;
    void readCreditCardTransactions(C databaseConnection, long cardId) throws Exception;
}
