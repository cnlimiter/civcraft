/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.database;

import java.sql.SQLException;
import java.util.HashMap;

public class SQLInsertTask implements Runnable {

    HashMap<String, Object> hashmap;
    String tablename;

    public SQLInsertTask(HashMap<String, Object> hashmap, String tablename) {

    }

    @Override
    public void run() {
        try {
            SQL.insertNow(hashmap, tablename);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
