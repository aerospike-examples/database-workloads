package com.aerospike.timf.databases;

import com.zaxxer.hikari.HikariDataSource;

import lombok.Value;

@Value
public class PostgresConnectionOptions {
    private String connectString;
    private String user;
    private String password;
    
    private HikariDataSource dataSource;
}