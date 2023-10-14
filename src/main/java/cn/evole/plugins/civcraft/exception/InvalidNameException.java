/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.exception;

public class InvalidNameException extends Exception {

    private static final long serialVersionUID = -697962518690144537L;

    public InvalidNameException() {
        super("Invalid name, name was null");
    }

    public InvalidNameException(String message) {
        super("Invalid name:" + message);
    }
}
