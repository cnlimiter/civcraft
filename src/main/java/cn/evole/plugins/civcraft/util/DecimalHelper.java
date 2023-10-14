/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.util;

import java.text.DecimalFormat;

public class DecimalHelper {
    public static String formatPercentage(double d) {
        DecimalFormat df = new DecimalFormat();
        return df.format(d * 100) + "%";
    }

}
