package gpl.sqlib.database;


public class SQLiteDatabase extends Database {

    private final String directory;
    private Mode mode = Mode.WAL2;

    public enum Mode {
        DELETE,
        TRUNCATE,
        PERSIST,
        MEMORY,
        WAL,
        WAL2,
        OFF
    }

    public SQLiteDatabase(String modId, String name, String directory) {
        super(modId, name);
        this.directory = directory;
        open();
        executeCommand(String.format("PRAGMA journal_mode = %s;", mode), true);
    }

    @Override
    public String getConnectionUrl() {
        return "jdbc:sqlite:" + directory + "/" + name + ".db";
    }

    @Override
    public String getTableCreationQuery(String tableName, String columns) {
        return String.format("CREATE TABLE IF NOT EXISTS %s (%s, ID mediumtext PRIMARY KEY);", tableName, columns);
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        executeCommand(String.format("PRAGMA journal_mode = %s;", mode), true);
    }

    @Override
    public void beginTransaction() {
        sqlConnection.beginTransaction(true);
    }
}