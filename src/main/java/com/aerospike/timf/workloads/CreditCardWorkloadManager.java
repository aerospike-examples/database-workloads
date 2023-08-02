package com.aerospike.timf.workloads;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import com.aerospike.timf.databases.DatabaseFunctions;
import com.aerospike.timf.generators.CreditCardGeneratorSevice;
import com.aerospike.timf.generators.TransactionGeneratorService;
import com.aerospike.timf.model.CreditCard;
import com.aerospike.timf.model.Transaction;

public class CreditCardWorkloadManager extends SubordinateObjectsWorkloadManager<CreditCard, Transaction> {
    private final CreditCardGeneratorSevice creditCardGenerator;
    private final TransactionGeneratorService txnGenerator;
    
    public CreditCardWorkloadManager(CreditCardGeneratorSevice generator, TransactionGeneratorService txnGenerator) {
        this.creditCardGenerator = generator;
        this.txnGenerator = txnGenerator;
    }
    
    @Override
    public CreditCard generatePrimaryEntity(long id) {
        return this.creditCardGenerator.generate(id);
    }

    @Override
    public <C> void insertPrimaryEntity(CreditCard entity, DatabaseFunctions<C> databaseFunctions, C databaseConnection) throws Exception {
        databaseFunctions.insertCreditCard(databaseConnection, entity);
    }
    
    @Override
    public int getNumberOfSubordinateObjects(CreditCard entity) {
        // Number of transactions in the last 180 days. Average people might have 10, big spenders 1000 a day
        // We want a weighted mean.
        double weight = 1;
        for (int i = 0; i < 5; i++) {
            weight *= ThreadLocalRandom.current().nextDouble();
        }
        return (int)(TransactionGeneratorService.DAYS_TO_GENERATE * 1000 * weight);
    }

    @Override
    public Transaction generateSubordinateEntity(CreditCard mainEntity, long subordinateId) {
        return this.txnGenerator.generateTransaction(subordinateId, mainEntity.getPan(), true);
    }
    
    @Override
    public <C> void saveSubordinateObject(Transaction subordinate, CreditCard mainEntity, DatabaseFunctions<C> databaseFunctions,
            C databaseConnection) throws Exception {
        databaseFunctions.addTransactionToCreditCard(databaseConnection, mainEntity, (Transaction)subordinate);
    }

    @Override
    public <C> void executeContinualRunOperation(long id, Object object, Map<String, Object> options,
            DatabaseFunctions<C> databaseFunctions, C databaseConnection) throws Exception {

        databaseFunctions.readCreditCardTransactions(databaseConnection, id);
    }
}
