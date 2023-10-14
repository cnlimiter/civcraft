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

public class StoreMaterial {
    public int type;
    public byte data;
    public String name;
    public double price;

    public StoreMaterial(String strtype, String strdata, String strname, String strprice) {
        type = Integer.valueOf(strtype);
        data = Integer.valueOf(strdata).byteValue();
        name = strname;
        price = Double.valueOf(strprice);
    }
}
