package com.aerospike.timf.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aerospike.timf.databases.DatabaseFunctions;
import com.aerospike.timf.generators.CreditCardGeneratorSevice;
import com.aerospike.timf.generators.PersonGeneratorService;
import com.aerospike.timf.generators.TransactionGeneratorService;
import com.aerospike.timf.model.CreditCard;
import com.aerospike.timf.service.WorkloadExecutor.State;
import com.aerospike.timf.workloads.CreditCardWorkloadManager;
import com.aerospike.timf.workloads.WorkloadManager;

@Service
public class WorkloadManagerService {
    @Autowired
    private PersonGeneratorService personGeneratorService;
    
    @Autowired
    private CreditCardGeneratorSevice creditCardGenerator;
    
    @Autowired
    private TransactionGeneratorService transactionGenerator;
    @Autowired
    private TimingService timingService;
    
    private final Map<String, WorkloadExecutor> databaseInstances = new ConcurrentHashMap<>();
    
    public synchronized void createDatabaseInstance(DatabaseParameters parameters, DatabaseFunctions<?> databaseFunctions) {
        String name = parameters.getName();
        // TODO: Refreshing the browser without stopping the server will break this!
        if (databaseInstances.containsKey(name)) {
            throw new IllegalArgumentException("database " + name + " already exists");
        }
        Object databaseConnection = databaseFunctions.connectInstance(parameters.getConfigParams());
        WorkloadExecutor executionDetails = new WorkloadExecutor(name, databaseConnection, databaseFunctions, personGeneratorService, timingService);
        databaseInstances.put(name, executionDetails);
    }
    
    private WorkloadExecutor validateAndGetExecutor(String databaseName) {
        WorkloadExecutor workloadExecutor = databaseInstances.get(databaseName);
        if (workloadExecutor == null) {
            throw new IllegalArgumentException("database " + databaseName + " does not exist");            
        }
        return workloadExecutor;
    }
    
    public synchronized void startWorkload(String databaseName, String jobName, Map<String, Object> parameters) {
        // TODO: Fix this workload stuff
//        WorkloadManager<Person> manager = new PersonWorkloadManager(personGeneratorService);
        WorkloadManager<CreditCard> manager = new CreditCardWorkloadManager(creditCardGenerator, transactionGenerator);
        
        WorkloadExecutor workloadExecutor = validateAndGetExecutor(databaseName);
        switch (jobName) {
        //TODO: This is very fragile code which comes about due to the desire to make workload management more flexible later.
        case "ContinuousRun":
            workloadExecutor.startContinuousRun(
                    ((Number)parameters.get("numThreads")).intValue(), 
                    ((Number)parameters.get("numRecordsInDatabase")).longValue(), 
                    ((Number)parameters.get("writePercent")).intValue(),
                    manager);
            break;
            
        case "SeedData":
            workloadExecutor.startSeedData(
                    ((Number)parameters.get("numThreads")).intValue(),
                    ((Number)parameters.get("numRecords")).longValue(),
                    manager);
            break;
        }
    }
    
    public synchronized void pauseWorkload(String databaseName) {
        WorkloadExecutor workloadExecutor = validateAndGetExecutor(databaseName);
        workloadExecutor.pauseJob();
    }
    public synchronized void resumeWorkload(String databaseName) {
        WorkloadExecutor workloadExecutor = validateAndGetExecutor(databaseName);
        workloadExecutor.resumeJob();
    }
    public synchronized void stopWorkload(String databaseName) {
        WorkloadExecutor workloadExecutor = validateAndGetExecutor(databaseName);
        workloadExecutor.terminateJob();
    }
    
    public synchronized Map<String, State> getStateOfAllWorkloads() {
        Map<String, State> results = new HashMap<>();
        for (String key : this.databaseInstances.keySet()) {
            results.put(key, databaseInstances.get(key).getState());
        }
        return results;
    }
    
    public synchronized long getTenthsPercentageComplete(String databaseName) {
        WorkloadExecutor workloadExecutor = databaseInstances.get(databaseName);
        if (workloadExecutor == null) {
            return -1;
        }
        else {
            return workloadExecutor.getTenthsPercentageComplete();
        }
    }
}
