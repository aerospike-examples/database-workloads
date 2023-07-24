package com.aerospike.timf.model;

import java.util.Date;

import com.aerospike.mapper.annotations.AerospikeBin;
import com.aerospike.mapper.annotations.AerospikeKey;
import com.aerospike.mapper.annotations.AerospikeRecord;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@AerospikeRecord(namespace = "test", set = "account")
public class Account {
    @AerospikeKey
    private long acctNum;
    @AerospikeBin(name = "name")
    private String accountName;
    private Date openDate;
    private long balance;
    private long routingNo;
}
