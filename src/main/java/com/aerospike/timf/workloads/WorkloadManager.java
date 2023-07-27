package com.aerospike.timf.workloads;

import java.util.Map;

import com.aerospike.timf.databases.DatabaseFunctions;

public abstract class WorkloadManager<T> {
    public int getNumberOfSubordinateObjects(T entity) {
        return 0;
    }

    public abstract T generatePrimaryEntity(long id);
    public abstract void insertPrimaryEntity(T entity, DatabaseFunctions<?> databaseFunctions, Object databaseConnection);

    public void saveSubordinateObject(Object subordinate, T mainEntity, DatabaseFunctions<?> databaseFunctions, Object databaseConnection) {}
    
    public Object generateSubordinateEntity(T mainEntity, long subordinateId) {
        return null;
    }
    
    /**
     * Optional method called prior to {@link WorkplaceManager.executeContinualRunOperation}. This call is not timed and so can be used
     * to run operations needed for the database operation which should not be included in the timing. For example, if a Person object
     * is to be saved to the database, this method can be used to generate the person, and the actual database call deferred until the later method.
     * @param id - a random number from 0 to the numberOfRecordsInDatabase. Can be used as the id of the record to change.
     * @param numRecordsInDatabase
     * @param options
     * @return
     */
    public Object prepareForContinualRunOperation(final long id, final long numRecordsInDatabase, Map<String, Object> options) {
        return null;
    }
    
    /**
     * Execute one operation for the continual run. This method must be thread-safe. This method will be timed and recorded. If the method throws an
     * exception, the run will be logged as unsuccessful, otherwise it will be logged as successful.
     * @param id - a random number from 0 to the numberOfRecordsInDatabase. Can be used as the id of the record to change.
     * @param object - The object returned from the {@link WorkplaceManager.prepareForContinualRunOperation} and can be null
     * @param options - The options passed as arguments into this workflow
     * @param databaseFunctions - The functions used to perform database operations
     * @param databaseConnection - The object which stored database connection information.
     */
    public abstract void executeContinualRunOperation(final long id, Object object, Map<String, Object> options, DatabaseFunctions<?> databaseFunctions, Object databaseConnection);
}
