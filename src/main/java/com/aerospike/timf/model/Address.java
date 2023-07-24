package com.aerospike.timf.model;

import com.aerospike.mapper.annotations.AerospikeRecord;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@AerospikeRecord
public class Address {
	private String line1;
	private String suburb;
	private String state;
	private String zipCode;
}
