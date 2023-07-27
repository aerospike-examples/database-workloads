package com.aerospike.timf.model;

import lombok.Data;

@Data
public class CreditCard {
    private String pan;
    private String cardNumber;
    private int version;
    private String nameOnCard;
    private int suppliementalCards;
    private int expMonth;
    private int expYear;
    private long cvcHash;
}
