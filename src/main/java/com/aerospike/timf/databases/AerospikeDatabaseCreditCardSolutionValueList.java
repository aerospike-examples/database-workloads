package com.aerospike.timf.databases;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Host;
import com.aerospike.client.IAerospikeClient;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.aerospike.client.Value;
import com.aerospike.client.cdt.MapOperation;
import com.aerospike.client.cdt.MapOrder;
import com.aerospike.client.cdt.MapPolicy;
import com.aerospike.client.cdt.MapWriteFlags;
import com.aerospike.client.policy.BatchPolicy;
import com.aerospike.client.policy.ClientPolicy;
import com.aerospike.client.policy.TlsPolicy;
import com.aerospike.mapper.tools.AeroMapper;
import com.aerospike.timf.databases.AerospikeDatabaseBase.AerospikeInstanceDetails;
import com.aerospike.timf.model.CreditCard;
import com.aerospike.timf.model.Transaction;
import com.aerospike.timf.service.DatabaseConfigItem;

@Database(name = "Aerospike", version = "Credit Card Soln 2")
@Service
public class AerospikeDatabaseCreditCardSolutionValueList extends AerospikeDatabaseBase implements DatabaseFunctions<AerospikeInstanceDetails> {
    
    private final static long EPOCH_TIME = new GregorianCalendar(2021, 0, 1).getTime().getTime();
    private final static String MAP_BIN = "txns";
    private final static String SET_NAME = "txns2";
    private final static int DAYS_TO_FETCH = 30;
    private String creditCardSet;
    private String creditCardNamespace;
    private MapPolicy mapPolicy = new MapPolicy(MapOrder.KEY_ORDERED, MapWriteFlags.DEFAULT);
    
    
    
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
        AeroMapper mapper = getAeroMapper(client, namespaceName);
        this.creditCardSet = mapper.getSet(CreditCard.class);
        this.creditCardNamespace = mapper.getNamespace(CreditCard.class);
        return new AerospikeInstanceDetails(client, getAeroMapper(client, namespaceName));
    }

    private long calculateDaysSinceEpoch(Date date) {
        return (date.getTime()- EPOCH_TIME) / 86400000;
    }
    
    private Key getCardBucketKey(Transaction transaction) {
        return new Key(creditCardNamespace, SET_NAME, transaction.getPan() + ":" + calculateDaysSinceEpoch(transaction.getTxnDate()));
    }
    
    @Override
    public void insertCreditCard(Object instance, CreditCard card) {
        AerospikeInstanceDetails details = (AerospikeInstanceDetails)instance;
        details.getMapper().save(card);
    }
    
    @Override
    public void addTransactionToCreditCard(Object instance, CreditCard card, Transaction transaction) {
        try {
        AerospikeInstanceDetails details = (AerospikeInstanceDetails)instance;
        Key key = getCardBucketKey(transaction);
        List<Object> transactionAsList = Arrays.asList(
                transaction.getTxnDate().getTime(),
                transaction.getAmount(),
                transaction.getCustId(),
                transaction.getDescription(),
                transaction.getPan(),
                transaction.getTerminalId(),
                transaction.getMerchantName());
        
        details.getClient().operate(null, key,
                MapOperation.setMapPolicy(mapPolicy, MAP_BIN),
                MapOperation.put(mapPolicy, MAP_BIN, Value.get(transaction.getTxnId()), Value.get(transactionAsList))
            );
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void readCreditCardTransactions(Object databaseConnection, long cardId) {
        int LIMIT = 50_000;
        AerospikeInstanceDetails details = (AerospikeInstanceDetails)databaseConnection;
        long dayOffset = calculateDaysSinceEpoch(new Date());
        Key[] keys = new Key[DAYS_TO_FETCH];
        for (int i = 0; i < DAYS_TO_FETCH; i++) {
            keys[i] = new Key(creditCardNamespace, SET_NAME, "Pan-" + cardId + ":" + (dayOffset - i));
        }
        Record records[] = details.getClient().get(null, keys);
        List<Transaction> txnList = new ArrayList<>();
        int count = 0;
        for (int i = 0; i < DAYS_TO_FETCH && count < LIMIT; i++) {
            if (records[i] == null) {
                continue;
            }
            TreeMap<String, List<Object>> map = (TreeMap<String,List<Object>>)records[i].getMap(MAP_BIN);
            for (String txnId : map.descendingKeySet()) {
                List<Object> data = map.get(txnId);
                Transaction txn = new Transaction();
                txn.setTxnId(txnId);
                txn.setTxnDate(new Date((long)data.get(0)));
                txn.setAmount((long)data.get(1));
                txn.setCustId((String)data.get(2));
                txn.setDescription((String)data.get(3));
                txn.setPan((String)data.get(4));
                txn.setTerminalId((String)data.get(5));
                txn.setMerchantName((String)data.get(6));
                txnList.add(txn);
                count++;
                if (count >= LIMIT) {
                    break;
                }
            }
        }
    }

}
