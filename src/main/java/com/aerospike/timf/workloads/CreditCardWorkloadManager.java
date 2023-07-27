package com.aerospike.timf.workloads;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import com.aerospike.timf.databases.DatabaseFunctions;
import com.aerospike.timf.generators.CreditCardGeneratorSevice;
import com.aerospike.timf.generators.TransactionGeneratorService;
import com.aerospike.timf.model.CreditCard;
import com.aerospike.timf.model.Transaction;

public class CreditCardWorkloadManager extends WorkloadManager<CreditCard> {
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
    public void insertPrimaryEntity(CreditCard entity, DatabaseFunctions<?> databaseFunctions, Object databaseConnection) {
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
    public Object generateSubordinateEntity(CreditCard mainEntity, long subordinateId) {
        return this.txnGenerator.generateTransaction(subordinateId, mainEntity.getPan(), true);
    }
    
    @Override
    public void saveSubordinateObject(Object subordinate, CreditCard mainEntity, DatabaseFunctions<?> databaseFunctions,
            Object databaseConnection) {
        databaseFunctions.addTransactionToCreditCard(databaseConnection, mainEntity, (Transaction)subordinate);
    }

    @Override
    public void executeContinualRunOperation(long id, Object object, Map<String, Object> options,
            DatabaseFunctions<?> databaseFunctions, Object databaseConnection) {

        databaseFunctions.readCreditCardTransactions(databaseConnection, id);
    }
}
