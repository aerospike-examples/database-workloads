package com.aerospike.timf.workloads;

import com.aerospike.timf.databases.DatabaseFunctions;

public abstract class SubordinateObjectsWorkloadManager<T, S> extends WorkloadManager<T>{
    public abstract int getNumberOfSubordinateObjects(T entity);
    public <C> void saveSubordinateObject(S subordinate, T mainEntity, DatabaseFunctions<C> databaseFunctions, C databaseConnection) throws Exception {}
    
    public S generateSubordinateEntity(T mainEntity, long subordinateId) throws Exception {
        return null;
    }

}
