/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.object;

import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.exception.InvalidNameException;
import cn.evole.plugins.civcraft.exception.InvalidObjectException;

import java.sql.ResultSet;
import java.sql.SQLException;

/*
 * Any object that needs to be saved will extend this object so it can be
 * saved in the database.
 */
public abstract class SQLObject extends NamedObject {

    private boolean isDeleted = false;

    public abstract void load(ResultSet rs) throws SQLException, InvalidNameException, InvalidObjectException, CivException;

    public abstract void save();

    public abstract void saveNow() throws SQLException;

    public abstract void delete() throws SQLException;

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

}
