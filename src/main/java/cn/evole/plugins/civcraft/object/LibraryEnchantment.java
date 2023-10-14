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

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.loreenhancements.LoreEnhancement;
import cn.evole.plugins.civcraft.structure.Library;
import org.bukkit.enchantments.Enchantment;

public class LibraryEnchantment {
    public Enchantment enchant;
    public LoreEnhancement enhancement;
    public int level;
    public double price;
    public String name;
    public String displayName;

    public LibraryEnchantment(String name, int lvl, double p) throws CivException {
        enchant = Library.getEnchantFromString(name);
        if (enchant == null) {
            enhancement = LoreEnhancement.enhancements.get(name);
            if (enhancement == null) {
                throw new CivException(CivSettings.localize.localizedString("libraryEnchantError1", name));
            }
        }
        level = lvl;
        price = p;

        this.name = name;
        if (enchant != null) {
            displayName = name.replace("_", " ");
        } else {
            displayName = enhancement.getDisplayName();
        }

    }
}
