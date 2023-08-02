package com.aerospike.timf.databases;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Host;
import com.aerospike.client.IAerospikeClient;
import com.aerospike.client.Record;
import com.aerospike.client.exp.Exp;
import com.aerospike.client.policy.ClientPolicy;
import com.aerospike.client.policy.QueryPolicy;
import com.aerospike.client.policy.TlsPolicy;
import com.aerospike.client.query.Filter;
import com.aerospike.client.query.IndexType;
import com.aerospike.client.query.RecordSet;
import com.aerospike.client.query.Statement;
import com.aerospike.client.task.IndexTask;
import com.aerospike.timf.databases.AerospikeDatabaseBase.AerospikeInstanceDetails;
import com.aerospike.timf.model.CreditCard;
import com.aerospike.timf.model.Transaction;
import com.aerospike.timf.service.DatabaseConfigItem;

@Database(name = "Aerospike", version = "Credit Card Soln 1")
@Service
public class AerospikeDatabaseSimpleCreditCardSolution extends AerospikeDatabaseBase implements DatabaseFunctions<AerospikeInstanceDetails> {
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
        
        // Create an index on the PAN filed in the transaction table
        IndexTask indexTask = client.createIndex(null, "test", "txns", "txn_idx", "pan", IndexType.STRING);
        indexTask.waitTillComplete(1000, 200000);
        return new AerospikeInstanceDetails(client, getAeroMapper(client, namespaceName));
    }

    @Override
    public void insertCreditCard(AerospikeInstanceDetails instance, CreditCard card) {
        instance.getMapper().save(card);
    }
    
    @Override
    public void addTransactionToCreditCard(AerospikeInstanceDetails instance, CreditCard card, Transaction transaction) {
        instance.getMapper().save(transaction);
    }

    @Override
    public void readCreditCardTransactions(AerospikeInstanceDetails databaseConnection, long cardId) {
        // Perform a secondary index query with an expression to filter out any dates older than 30 days
        Statement stmt = new Statement();
        stmt.setNamespace("test");
        stmt.setSetName("txns");
        stmt.setFilter(Filter.equal("pan", "Pan-" + cardId));
        long startTime = new Date().getTime() - TimeUnit.DAYS.toMillis(30);
        
        QueryPolicy queryPolicy = new QueryPolicy();
        queryPolicy.filterExp = Exp.build(Exp.ge(Exp.intBin("txnDate"), Exp.val(startTime)));
        
        List<Transaction> txnList = new ArrayList<>();
        RecordSet results = databaseConnection.getClient().query(queryPolicy, stmt);
        while (results.next()) {
            Record record  = results.getRecord();
            Transaction trans = databaseConnection.getMapper().getMappingConverter().convertToObject(Transaction.class, record);
            txnList.add(trans);
        }
        
        txnList.sort((a,b) -> a.getTxnDate().compareTo(b.getTxnDate()));
        int count = txnList.size();
        // Taking the top 50,000 is easy here if needed.
    }

}
