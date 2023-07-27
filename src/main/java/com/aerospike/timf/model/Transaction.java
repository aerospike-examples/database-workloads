package com.aerospike.timf.model;

import java.util.Date;

import lombok.Data;

@Data
public class Transaction {
    private String txnId;
    private String pan;
    private Date txnDate;
    private long amount; // In cents
    private String merchantName;
    private String description;
    private String custId;
    private String terminalId;
    
}
