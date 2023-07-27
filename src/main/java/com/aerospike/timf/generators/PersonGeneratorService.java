package com.aerospike.timf.generators;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aerospike.timf.model.Account;
import com.aerospike.timf.model.Address;
import com.aerospike.timf.model.Person;
import com.aerospike.timf.model.Person.Gender;

@Service
public class PersonGeneratorService {
    @Autowired
    private NameGeneratorService nameGeneratorService;
    
    @Autowired
    private AddressGeneratorService addressGeneratorService;
    
    private static final long MS_IN_100_YEARS = TimeUnit.MILLISECONDS.convert(365*100, TimeUnit.DAYS); //100L*365*24*60*60*1000;

    public Person generatePerson(long id) {
        Random random = ThreadLocalRandom.current();
        String firstName = nameGeneratorService.getFirstName();
        String lastName = nameGeneratorService.getLastName();
        long now = new Date().getTime();
        Date date = new Date(now - random.nextLong(MS_IN_100_YEARS));
        
        Address homeAddr = new Address(
                addressGeneratorService.getAddressLine1(), 
                addressGeneratorService.getSuburb(), 
                addressGeneratorService.getState(),
                addressGeneratorService.getZipCode());
        Address workAddr = new Address(
                addressGeneratorService.getAddressLine1(), 
                addressGeneratorService.getSuburb(), 
                addressGeneratorService.getState(),
                addressGeneratorService.getZipCode());
        
        List<Account> accounts = new ArrayList<>();
        
        int numAccounts = random.nextInt(10)+3;
        for (int i = 0; i < numAccounts; i++) {
            Account thisAccount = new Account();
            thisAccount.setAccountName(firstName + " " + lastName + " Account");
            thisAccount.setAcctNum(100*id + i+1);
            thisAccount.setBalance(random.nextLong(10_000_000));
            thisAccount.setOpenDate(new Date());
            thisAccount.setRoutingNo(710000003468L);
            accounts.add(thisAccount);
        }
        Person person = new Person(id, firstName, lastName, date, Gender.MALE, homeAddr, workAddr, accounts);
        return person;
    }
}
