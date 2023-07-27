package com.aerospike.timf.workloads;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import com.aerospike.timf.databases.DatabaseFunctions;
import com.aerospike.timf.generators.PersonGeneratorService;
import com.aerospike.timf.model.Person;

public class PersonWorkloadManager extends WorkloadManager<Person> {
    private final PersonGeneratorService personGeneratorService;
    
    public PersonWorkloadManager(PersonGeneratorService generator) {
        this.personGeneratorService = generator;
    }
    
    @Override
    public Person generatePrimaryEntity(long id) {
        return this.personGeneratorService.generatePerson(id);
    }

    @Override
    public void insertPrimaryEntity(Person person, DatabaseFunctions<?> databaseFunctions, Object databaseConnection) {
        databaseFunctions.insertPerson(databaseConnection, person);
    }

    @Override
    public Object prepareForContinualRunOperation(long id, long numRecordsInDatabase, Map<String, Object> options) {
        Integer writePercent = (Integer) options.get("writePercent");
        int writePct;
        if (writePercent == null) {
            writePct = 50;
        }
        else {
            writePct = writePercent;
        }
        int percent = ThreadLocalRandom.current().nextInt(100);
        if (percent < writePercent) {
            // Do an update, so generate a new person
            return personGeneratorService.generatePerson(id);
        }
        else {
            // THis is a read operation, do nothing.
            return null;
        }
    }
    
    @Override
    public void executeContinualRunOperation(long id, Object object, Map<String, Object> options,
            DatabaseFunctions<?> databaseFunctions, Object databaseConnection) {
        
        if (object == null) {
            // read operation
            databaseFunctions.readPerson(databaseConnection, id);
        }
        else {
            databaseFunctions.updatePerson(databaseConnection, (Person)object);
        }
    }
}
