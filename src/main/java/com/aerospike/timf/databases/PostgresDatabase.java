package com.aerospike.timf.databases;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.aerospike.timf.model.CreditCard;
import com.aerospike.timf.model.Person;
import com.aerospike.timf.model.Transaction;
import com.aerospike.timf.service.DatabaseConfigItem;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Database(name = "Postgres", version = "Credit Card Solution 1")
@Service
public class PostgresDatabase implements DatabaseFunctions<PostgresConnectionOptions> {
    protected static final String DATABASE_CONFIG_NAME = "Database";
    protected static final String HOST_CONFIG_NAME = "Host";
    protected static final String PORT_CONFIG_NAME = "Port";
    protected static final String USER_CONFIG_NAME = "User";
    protected static final String PASSWORD_CONFIG_NAME = "Password";
    
    @Override
    public List<DatabaseConfigItem> getConfigItems() {
        return Arrays.asList(
                new DatabaseConfigItem(HOST_CONFIG_NAME, "Host", "Host identity, either IP address or DNS name", "localhost"),
                new DatabaseConfigItem(PORT_CONFIG_NAME, "Port", "Port to connect", "Port", 5432),
                new DatabaseConfigItem(DATABASE_CONFIG_NAME, "Database", "", "test"),
                new DatabaseConfigItem(USER_CONFIG_NAME, "User", "User name to connect to database", DatabaseConfigItem.Type.STRING, true),
                new DatabaseConfigItem(PASSWORD_CONFIG_NAME, "Password", "User Password", DatabaseConfigItem.Type.PASSWORD, true)
        );
    }

    @Override
    public PostgresConnectionOptions connectInstance(Map<String, Object> configParameters) {
        String db = (String) configParameters.get(DATABASE_CONFIG_NAME);
        String host = (String) configParameters.get(HOST_CONFIG_NAME);
        int port = (int) configParameters.get(PORT_CONFIG_NAME);
        String user = (String) configParameters.get(USER_CONFIG_NAME);
        String password = (String) configParameters.get(PASSWORD_CONFIG_NAME);
        
        String connectString = String.format("jdbc:postgresql://%s:%d/%s", host, port, db);
        Connection conn = null;
        try  {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(connectString);
            config.setUsername(user);
            config.setPassword(password);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.setMaximumPoolSize(120);
            config.setMinimumIdle(10);
            
            HikariDataSource ds = new HikariDataSource(config);
            
            conn = ds.getConnection();
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(
                    "create table if not exists CREDIT_CARD("
                    + "PAN varchar(40) NOT NULL, "
                    + "CARD_NO varchar(20), "
                    + "VERSION integer NOT NULL, "
                    + "CARD_NAME varchar(100) NOT NULL, "
                    + "SUPPL_CARDS integer NOT NULL, "
                    + "EXP_MONTH integer NOT NULL, "
                    + "EXP_YEAR integer NOT NULL, "
                    + "CVC_HASH bigint NOT NULL, "
                    + "PRIMARY KEY(PAN));"
                    );
            stmt.executeUpdate(
                    "create table if not exists TRANSACTION("
                    + "TXN_ID varchar(40) NOT NULL, "
                    + "PAN varchar(40) NOT NULL REFERENCES CREDIT_CARD(PAN), "
                    + "DATE timestamp NOT NULL, "
                    + "AMOUNT integer NOT NULL, "
                    + "MERCHANT_NAME varchar(100) NOT NULL, "
                    + "DESCRIPTION varchar(250) NOT NULL, "
                    + "CUST_ID varchar(15) NOT NULL, "
                    + "TERMINAL_ID varchar(20) NOT NULL, "
                    + "PRIMARY KEY(TXN_ID));"
                    );
            stmt.executeUpdate(
                    "create index if not exists TXN_DATE_IDX on TRANSACTION(DATE DESC)");
            
            return new PostgresConnectionOptions(connectString, user, password, ds);
        }
        catch (Exception e) {
            System.err.printf("Error connecting to database:%s, user: %s. Error: %s(%s)", connectString, user, e.getMessage(), e.getClass());
            throw new RuntimeException(e);
        }
        finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
        
    }

    @Override
    public void disconnectInstance(PostgresConnectionOptions params) {
        params.getDataSource().close();
    }

    @Override
    public void insertPerson(Object databaseConnection, Person person) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void updatePerson(Object instance, Person person) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Person readPerson(Object instance, long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void insertCreditCard(Object databaseConnection, CreditCard card) {
        PostgresConnectionOptions connOptions = (PostgresConnectionOptions)databaseConnection;
        final String preparedStmtText = "insert into CREDIT_CARD(PAN, CARD_NO, VERSION, CARD_NAME, SUPPL_CARDS, EXP_MONTH, EXP_YEAR, CVC_HASH) values "
                + "(?, ?, ?, ?, ?, ?, ?, ?)"; 
        Connection conn = null;
        try {
            conn = connOptions.getDataSource().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(preparedStmtText);
            pstmt.setString(1, card.getPan());
            pstmt.setString(2, card.getCardNumber());
            pstmt.setInt(3, card.getVersion());
            pstmt.setString(4, card.getNameOnCard());
            pstmt.setInt(5, card.getSuppliementalCards());
            pstmt.setInt(6, card.getExpMonth());
            pstmt.setInt(7, card.getExpYear());
            pstmt.setLong(8, card.getCvcHash());
            pstmt.executeUpdate();
            pstmt.close();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e1) {}
            }
        }
    }

    @Override
    public void addTransactionToCreditCard(Object databaseConnection, CreditCard card, Transaction transaction) {
        PostgresConnectionOptions connOptions = (PostgresConnectionOptions)databaseConnection;
        final String preparedStmtText = "insert into TRANSACTION(TXN_ID, PAN, DATE, AMOUNT, MERCHANT_NAME, DESCRIPTION, CUST_ID, TERMINAL_ID) values "
                + "(?, ?, ?, ?, ?, ?, ?, ?)"; 
        Connection conn = null;
        try {
            java.sql.Timestamp sqlDateTime = new Timestamp(transaction.getTxnDate().getTime());
            conn = connOptions.getDataSource().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(preparedStmtText);
            pstmt.setString(1, transaction.getTxnId());
            pstmt.setString(2, transaction.getPan());
            pstmt.setTimestamp(3, sqlDateTime);
            pstmt.setInt(4, (int)transaction.getAmount());
            pstmt.setString(5, transaction.getMerchantName());
            pstmt.setString(6, transaction.getDescription());
            pstmt.setString(7, transaction.getCustId());
            pstmt.setString(8, transaction.getTerminalId());
            pstmt.executeUpdate();
            pstmt.close();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e1) {}
            }
        }
    }

    @Override
    public void readCreditCardTransactions(Object databaseConnection, long cardId) {
        PostgresConnectionOptions connOptions = (PostgresConnectionOptions)databaseConnection;
        final String preparedStmtText = "select TXN_ID, PAN, DATE, AMOUNT, MERCHANT_NAME, DESCRIPTION, CUST_ID, TERMINAL_ID "
                + "from TRANSACTION where PAN = ? and date >= ? order by DATE desc LIMIT 50000";
        Connection conn = null;
        try {
            long startTime = new java.util.Date().getTime() - TimeUnit.DAYS.toMillis(30);
            java.sql.Timestamp sqlDateTime = new Timestamp(startTime);
            String pk = "Pan-" + cardId;
            conn = connOptions.getDataSource().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(preparedStmtText);
            
            pstmt.setString(1, pk);
            pstmt.setTimestamp(2, sqlDateTime);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                // Dummy read all the fields
                Transaction result = new Transaction();
                result.setTxnId(rs.getString("txn_id"));
                result.setPan(rs.getString("pan"));
                result.setTxnDate(rs.getTimestamp("date"));
                result.setAmount(rs.getInt("amount"));
                result.setMerchantName(rs.getString("merchant_name"));
                result.setDescription(rs.getString("description"));
                result.setCustId(rs.getString("cust_id"));
                result.setTerminalId(rs.getString("terminal_id"));
            }
            rs.close();
            pstmt.close();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e1) {}
            }
        }
    }

}
