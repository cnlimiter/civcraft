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

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.util.CivColor;
import gpl.AttributeUtil;

public class Soulbound extends ItemComponent {

    @Override
    public void onPrepareCreate(AttributeUtil attrs) {
        attrs.addLore(CivColor.Gold + CivSettings.localize.localizedString("Soulbound"));
    }

}
