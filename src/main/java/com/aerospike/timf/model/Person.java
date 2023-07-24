package com.aerospike.timf.model;

import java.util.Date;
import java.util.List;

import com.aerospike.mapper.annotations.AerospikeBin;
import com.aerospike.mapper.annotations.AerospikeEmbed;
import com.aerospike.mapper.annotations.AerospikeEmbed.EmbedType;
import com.aerospike.mapper.annotations.AerospikeKey;
import com.aerospike.mapper.annotations.AerospikeRecord;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@AerospikeRecord(namespace = "test", set = "people")
public class Person {
	public enum Gender {
		MALE, FEMALE;
	}
	@AerospikeKey
	private long id;
	private String firstName;
	private String lastName;
	@AerospikeBin(name = "dob")
	private Date dateOfBirth;
	private Gender gender;
	@AerospikeEmbed(type = EmbedType.LIST)
	private Address homeAddress;
	@AerospikeEmbed(type = EmbedType.MAP)
	private Address workAddress;
	@AerospikeEmbed(elementType = EmbedType.LIST, type = EmbedType.MAP)
	private List<Account> accounts;
}
