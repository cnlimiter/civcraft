package gpl.sqlib;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import gpl.sqlib.sql.SQLConnection;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.NBTBase;
import net.minecraft.server.v1_12_R1.NBTTagString;

import java.util.UUID;

public class DataContainer {

    private final Table table;
    private final SQLConnection sqlConnection;
    private final String id;

    public DataContainer(String id, Table table, SQLConnection sqlConnection) {
        this.id = id;
        this.table = table;
        this.sqlConnection = sqlConnection;
    }

    public String getIdAsString() {
        return id;
    }

    public UUID getIdAsUUID() {
        return UUID.fromString(id);
    }

    public int getIdAsInt() {
        return Integer.parseInt(id);
    }

    public void put(String field, String value) {
        sqlConnection.writeField(table, id, field, value);
    }

    public void put(String field, int value) {
        sqlConnection.writeField(table, id, field, value);
    }

    public void put(String field, double value) {
        sqlConnection.writeField(table, id, field, value);
    }

    public void put(String field, long value) {
        sqlConnection.writeField(table, id, field, value);
    }

    public void put(String field, boolean value) {
        sqlConnection.writeField(table, id, field, value ? 1 : 0); // Convert bool to int, SQLite compat
    }

    public void put(String field, BlockPosition value) {
        sqlConnection.writeField(table, id, field, value.asLong());
    }

    public void put(String field, JsonElement value) {
        sqlConnection.writeField(table, id, field, value.toString());
    }

    public void put(String field, NBTBase value) {
        sqlConnection.writeField(table, id, field, value.toString());
    }

    public void put(String field, UUID value) {
        sqlConnection.writeField(table, id, field, value.toString());
    }

    public String getString(String field) {
        return sqlConnection.readField(table, id, field, String.class);
    }

    public int getInt(String field) {
        return sqlConnection.readField(table, id, field, Integer.class);
    }

    public double getDouble(String field) {
        return sqlConnection.readField(table, id, field, Double.class);
    }

    public double getLong(String field) {
        return sqlConnection.readField(table, id, field, Long.class);
    }

    public boolean getBool(String field) {
        return sqlConnection.readField(table, id, field, Integer.class) > 0; //Int to bool, SQLite compat
    }

    public BlockPosition getBlockPos(String field) {
        Long pos = sqlConnection.readField(table, id, field, Long.class);
        if (pos == null) return null;
        return BlockPosition.fromLong(pos);
    }

    public JsonElement getJson(String field) {
        String json = sqlConnection.readField(table, id, field, String.class);
        if (json == null) return null;
        return new JsonParser().parse(json);
    }

    public NBTBase getNbt(String field) {
        try {
            String nbt = sqlConnection.readField(table, id, field, String.class);
            if (nbt == null) return null;
            return new NBTTagString(nbt);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public UUID getUUID(String field) {
        String uuid = sqlConnection.readField(table, id, field, String.class);
        if (uuid == null) return null;
        return UUID.fromString(uuid);
    }

}