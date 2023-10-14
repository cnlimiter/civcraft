/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.items.components;

import gpl.AttributeUtil;
import gpl.AttributeUtil.Attribute;
import gpl.AttributeUtil.AttributeType;

public class MaxHealth extends ItemComponent {

    @Override
    public void onPrepareCreate(AttributeUtil attrs) {
        attrs.add(Attribute.newBuilder().name("Health").
                type(AttributeType.GENERIC_MAX_HEALTH).
                amount(this.getDouble("value")).
                build());
    }

}
