package com.aerospike.timf.generators;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aerospike.timf.model.CreditCard;

@Service
public class CreditCardGeneratorSevice {
    @Autowired 
    private NameGeneratorService nameGeneratrService;
    
    public CreditCard generate(long id) {
        Random random = ThreadLocalRandom.current();
        CreditCard result = new CreditCard();
        result.setCardNumber(Utils.stringOfNumbers(16));
        result.setCvcHash(random.nextLong(100_000_000));
        result.setExpMonth(random.nextInt(12)+1);
        result.setExpYear(random.nextInt(10)+23);
        result.setNameOnCard(nameGeneratrService.getName());
        result.setPan("Pan-" + id );
        result.setSuppliementalCards(random.nextInt(4));
        result.setVersion(random.nextInt(10));
        return result;
    }
}
