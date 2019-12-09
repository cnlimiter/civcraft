// 
// Decompiled by Procyon v0.5.30
// 

package com.avrgaming.civcraft.loregui.book;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigLevelTalent;
import com.avrgaming.civcraft.loregui.GuiAction;
import com.avrgaming.civcraft.loregui.OpenInventoryTask;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.lorestorage.LoreGuiItemListener;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.object.Buff;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.TreeMap;

public class TalentList implements GuiAction {
    Inventory inventory;

    public TalentList() {
        this.inventory = null;
    }

    @Override
    public void performAction(final InventoryClickEvent event, final ItemStack stack) {
        final Player player = (Player) event.getWhoClicked();
        final Resident whoClicked = CivGlobal.getResident(player);
        final Civilization civ = whoClicked.getCiv();
        final Town capitol = civ.getCapitol();
        boolean hasAnyTalent = false;
        if (capitol == null) {
            return;
        }
        this.inventory = Bukkit.getServer().createInventory((InventoryHolder) player, 18, CivSettings.localize.localizedString("talentGui_talentList"));
        final TreeMap<Integer, ItemStack> talents = new TreeMap<>();
        for (final Buff buff : capitol.getBuffManager().getAllBuffs()) {
            if (buff.getId().contains("level")) {
                final int talentLevel = Integer.parseInt(buff.getId().replaceAll("[^\\d]", ""));
                final ConfigLevelTalent configLevelTalent = CivSettings.talentLevels.get(talentLevel);
                int id = 5;
                String description = CivSettings.localize.localizedString("Broken");
                if (configLevelTalent.levelBuff1.equals(buff.getId())) {
                    id = ItemManager.getId(Material.REDSTONE_BLOCK);
                    description = "§c" + configLevelTalent.levelBuffDesc1;
                } else if (configLevelTalent.levelBuff2.equals(buff.getId())) {
                    id = ItemManager.getId(Material.EMERALD_BLOCK);
                    description = "§a" + configLevelTalent.levelBuffDesc2;
                } else if (configLevelTalent.levelBuff3.equals(buff.getId())) {
                    id = ItemManager.getId(Material.LAPIS_BLOCK);
                    description = "§b" + configLevelTalent.levelBuffDesc3;
                }
                final ItemStack talent = LoreGuiItem.build(CivColor.GoldBold + configLevelTalent.levelName + CivColor.RESET + " (" + CivColor.BlueBold + configLevelTalent.level + CivColor.RESET + ")", id, 0, description);
                talents.put(configLevelTalent.level, talent);
                hasAnyTalent = true;
            }
        }
        if (!hasAnyTalent) {
            final ItemStack talent2 = LoreGuiItem.build("§c" + CivSettings.localize.localizedString("cmd_civ_talent_list_noOne"), ItemManager.getId(Material.REDSTONE_BLOCK), 0, new String[0]);
            this.inventory.addItem(talent2);
        }
        this.inventory.addItem(talents.values().toArray(new ItemStack[talents.values().size()]));
        ItemStack backButton = LoreGuiItem.build(CivSettings.localize.localizedString("bookReborn_back"), ItemManager.getId(Material.MAP), 0, CivSettings.localize.localizedString("bookReborn_backTo", BookTalentGui.inventory.getName()));
        backButton = LoreGuiItem.setAction(backButton, "OpenInventory");
        backButton = LoreGuiItem.setActionData(backButton, "invType", "showGuiInv");
        backButton = LoreGuiItem.setActionData(backButton, "invName", BookTalentGui.inventory.getName());
        this.inventory.setItem(17, backButton);
        LoreGuiItemListener.guiInventories.put(this.inventory.getName(), this.inventory);
        TaskMaster.syncTask(new OpenInventoryTask(player, this.inventory));
    }
}
