package com.aerospike.timf.databases;


// This file is commented out as it used to rely on open source Lettuce, but this has an incorrect
// version of guava which conflicts with the Aerospike proxy client. 
/*
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.aerospike.timf.model.CreditCard;
import com.aerospike.timf.model.Person;
import com.aerospike.timf.model.Transaction;
import com.aerospike.timf.service.DatabaseConfigItem;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;

@Service
@Database(name = "Redis", version = "Initial")
public class RedisDatabase implements DatabaseFunctions<RedisConnection> {
    private static final String HOST_CONFIG_NAME = "Host";
    private static final String PORT_CONFIG_NAME = "Port";
    private static final String PASSWORD_CONFIG_NAME = "Password";
    @Override
    public List<DatabaseConfigItem> getConfigItems() {
    return Arrays.asList(
        new DatabaseConfigItem(HOST_CONFIG_NAME, "Host", "Host identity, either IP address or DNS name", "localhost"),
        new DatabaseConfigItem(PORT_CONFIG_NAME, "Port", "Port to connect", "Port", 6379),
        new DatabaseConfigItem(PASSWORD_CONFIG_NAME, "Password", "User Password", DatabaseConfigItem.Type.PASSWORD, false)
    );
    }
    @Override
    public RedisConnection<String, String> connectInstance(Map<String, Object> configParameters) {
        String host = (String) configParameters.get(HOST_CONFIG_NAME);
        int port = (int) configParameters.get(PORT_CONFIG_NAME);
        String password = (String) configParameters.get(PASSWORD_CONFIG_NAME);
        RedisClient redisClient = new RedisClient(RedisURI.create(
            String.format("redis://%s%s:%d",
            password != null && !password.isEmpty() ? password + "@" : "", host, port)));
        RedisConnection<String, String> connection = redisClient.connect();
        return connection;
    }
    @Override
    public void disconnectInstance(RedisConnection params) {
        // TODO Auto-generated method stub
        
    }
    @Override
    public void insertPerson(RedisConnection databaseConnection, Person person) throws Exception {
        // TODO Auto-generated method stub
        
    }
    @Override
    public void updatePerson(RedisConnection databaseConnection, Person person) throws Exception {
        // TODO Auto-generated method stub
        
    }
    @Override
    public Person readPerson(RedisConnection databaseConnection, long id) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public void insertCreditCard(RedisConnection connection, CreditCard card) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        connection.set(card.getPan(), mapper.writeValueAsString(card));
    }
    @Override
    public void addTransactionToCreditCard(RedisConnection databaseConnection, CreditCard card, Transaction transaction)
            throws Exception {
        // TODO Auto-generated method stub
        
    }
    @Override
    public void readCreditCardTransactions(RedisConnection databaseConnection, long cardId) throws Exception {
        // TODO Auto-generated method stub
        
    }
}
*/