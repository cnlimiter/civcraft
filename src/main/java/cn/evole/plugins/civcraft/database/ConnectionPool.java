package cn.evole.plugins.civcraft.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionPool {

    HikariDataSource pool;


    public ConnectionPool(String dbcUrl, String user, String pass) throws ClassNotFoundException, SQLException {

        /* setup the connection pool */
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbcUrl);
        config.setUsername(user);
        config.setPassword(pass);


        pool = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return pool.getConnection();
    }

    public void shutdown() {
        pool.close();
    }

}
