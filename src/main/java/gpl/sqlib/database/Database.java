package gpl.sqlib.database;

import lombok.Getter;
import gpl.sqlib.Table;
import gpl.sqlib.sql.SQLConnection;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

public abstract class Database {

    @Getter
    protected final String name;
    @Getter
    protected final String prefix;
    private final HashMap<String, Table> tables = new HashMap<>();
    protected SQLConnection sqlConnection;

    public Database(String prefix, String name) {
        this.prefix = prefix;
        this.name = name;
    }

    public abstract String getConnectionUrl();

    public Properties getConnectionProperties() {
        return new Properties();
    }

    public abstract String getTableCreationQuery(String tableName, String columns);

    public void open() {
        if (sqlConnection == null) sqlConnection = new SQLConnection(getConnectionUrl(), getConnectionProperties());
    }

    public void close() {
        if (sqlConnection != null) sqlConnection.close();
        sqlConnection = null;
    }

    public abstract void beginTransaction();

    public void endTransaction() {
        sqlConnection.endTransaction();
    }

    public Table createTable(String name) {
        return new Table(prefix, name, this, sqlConnection);
    }

    public void addTable(Table table) {
        tables.put(table.getNoConflictName(), table);
    }

    public Table getTable(String name) {
        return tables.get(prefix == null ? name : prefix + "_" + name);
    }
    public boolean hasTable(String name) {
        return tables.containsKey(name);
    }
    public ArrayList<Table> getTables() {
        return new ArrayList<>(tables.values());
    }

    public PreparedStatement executeCommand(String sql, boolean autoClose, Object... params) {
        return sqlConnection.executeCommand(sql, autoClose, params);
    }
}