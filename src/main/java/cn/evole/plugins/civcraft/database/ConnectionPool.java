package cn.evole.plugins.civcraft.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionPool {

    HikariDataSource pool;


    public ConnectionPool(String dbcUrl) throws ClassNotFoundException, SQLException {

        /* setup the connection pool */

        HikariConfig config = new HikariConfig();
        config.setPoolName("CivCraftSQLitePool");
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl(dbcUrl);
        pool = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return pool.getConnection();
    }

    public void shutdown() {
        pool.close();
    }

}
