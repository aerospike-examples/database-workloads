package com.aerospike.timf.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.aerospike.timf.databases.Database;
import com.aerospike.timf.databases.DatabaseFunctions;

import jakarta.annotation.PostConstruct;

@Service
public class DatabaseVersionService {

    @Autowired
    private ApplicationContext applicationContext;
    
    private final Map<String, Map<String, DatabaseFunctions<?>>> databaseImplementations = new HashMap<>();
    
    @PostConstruct
    public void init() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(Database.class);
        for (String beanName :beans.keySet()) {
            System.out.printf("bean: %s, instance: %s\n", beanName, beans.get(beanName));
            Object bean = beans.get(beanName);
            
            if (DatabaseFunctions.class.isAssignableFrom(bean.getClass())) {
                saveDatabase((DatabaseFunctions<?>) bean);
            }
            else {
                throw new RuntimeException("Beans which implement the @Database annotation must also implement the DatabaseFunctions interface");
            }
        }
        
        System.out.println("Databases: " + getDatabaseImplementations());
        System.out.println("Versions: " + getDatabaseVersions(getDatabaseImplementations().get(0)));
        DatabaseFunctions<?> db = getDatabaseImplmentation("Aerospike", "Managed");
        System.out.println(db.getConfigItems());
    }
    
    private void saveDatabase(DatabaseFunctions<?> database) {
        Database annotation = database.getClass().getAnnotation(Database.class);
        String databaseName = annotation.name();

        Map<String, DatabaseFunctions<?>> thisDatabaseDetails = databaseImplementations.get(databaseName);
        if (thisDatabaseDetails == null) {
            thisDatabaseDetails = new HashMap<>();
            databaseImplementations.put(databaseName, thisDatabaseDetails);
        }
        
        String databaseVersion = annotation.version();
        if (thisDatabaseDetails.containsKey(databaseVersion)) {
            DatabaseFunctions<?> existingObject = thisDatabaseDetails.get(databaseVersion);
            throw new RuntimeException(String.format("Both %s and %s are marked as implementing version %s of database %s", 
                    database.getClass(), existingObject.getClass(), databaseVersion, database));
        }
        thisDatabaseDetails.put(databaseVersion, database);
    }
    
    public List<String> getDatabaseImplementations() {
        List<String> results = new ArrayList<String>(this.databaseImplementations.keySet());
        results.sort(null);
        return results;
    }

    public List<String> getDatabaseVersions(String databaseName) {
        Map<String, DatabaseFunctions<?>> databaseDetails = databaseImplementations.get(databaseName);
        if (databaseDetails == null) {
            return new ArrayList<>();
        }
        else {
            List<String> results = new ArrayList<>(databaseDetails.keySet());
            results.sort(null);
            return results;
        }
    }
    
    public DatabaseFunctions<?> getDatabaseImplmentation(String databaseName, String version) {
        Map<String, DatabaseFunctions<?>> databaseDetails = databaseImplementations.get(databaseName);
        if (databaseDetails == null || !databaseDetails.containsKey(version)) {
            throw new RuntimeException("No database implementation found for " + databaseName + ", version " + version);
        }
        return databaseDetails.get(version);
    }
}
