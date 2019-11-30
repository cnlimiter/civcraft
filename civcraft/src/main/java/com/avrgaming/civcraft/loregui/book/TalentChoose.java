
package com.avrgaming.civcraft.loregui.book;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigLevelTalent;
import com.avrgaming.civcraft.loregui.GuiAction;
import com.avrgaming.civcraft.loregui.OpenInventoryTask;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.lorestorage.LoreGuiItemListener;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
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

public class TalentChoose
        implements GuiAction {
    Inventory inventory = null;

    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        Player player = (Player) event.getWhoClicked();
        Resident whoClicked = CivGlobal.getResident(player);
        Civilization civ = whoClicked.getCiv();
        Town capitol = civ.getCapitol();
        if (capitol == null) {
            return;
        }
        int talentLevel = capitol.highestTalentLevel();
        int cultureLevel = capitol.getCultureLevel();
        if (talentLevel == cultureLevel && talentLevel < 10) {
            CivMessage.sendError((Object) player, CivSettings.localize.localizedString("cmd_civ_talent_choose_notNow", civ.getCapitol().getName(), civ.getCapitol().getCultureLevel() + 1));
            return;
        }

        if (talentLevel >= 10) {
            CivMessage.send(whoClicked, CivColor.Red + CivSettings.localize.localizedString("cmd_civ_talent_choose_ended"));
            return;
        }
        ConfigLevelTalent configLevelTalent = CivSettings.talentLevels.get(cultureLevel);
        if (configLevelTalent == null) {
            CivMessage.sendError(whoClicked, CivColor.Red + CivSettings.localize.localizedString("cmd_civ_talent_next_invalid"));
            return;
        }
        this.inventory = Bukkit.getServer().createInventory((InventoryHolder) player, 9, configLevelTalent.levelName + " (" + configLevelTalent.level + ")");
        ItemStack firstTalent = LoreGuiItem.build("", ItemManager.getId(Material.REDSTONE_BLOCK), 0, configLevelTalent.levelBuffDesc1);
        firstTalent = LoreGuiItem.setAction(firstTalent, "Confirmation");
        firstTalent = LoreGuiItem.setActionData(firstTalent, "level", "" + configLevelTalent.level);
        firstTalent = LoreGuiItem.setActionData(firstTalent, "buff", configLevelTalent.levelBuff1);
        firstTalent = LoreGuiItem.setActionData(firstTalent, "number", "1");
        firstTalent = LoreGuiItem.setActionData(firstTalent, "description", configLevelTalent.levelBuffDesc1);
        firstTalent = LoreGuiItem.setActionData(firstTalent, "passFields", "buff,number,description");
        firstTalent = LoreGuiItem.setActionData(firstTalent, "passAction", "ChooseTalent");
        firstTalent = LoreGuiItem.setActionData(firstTalent, "confirmText", CivSettings.localize.localizedString("cmd_civ_talent_choose_confirmText", CivColor.GreenBold + cultureLevel + "§a", CivColor.GoldBold + "1" + "§a"));
        firstTalent = LoreGuiItem.setActionData(firstTalent, "confirmText2", CivColor.RoseBold + configLevelTalent.levelBuffDesc1);
        ItemStack secondTalent = LoreGuiItem.build("", ItemManager.getId(Material.EMERALD_BLOCK), 0, configLevelTalent.levelBuffDesc2);
        secondTalent = LoreGuiItem.setAction(secondTalent, "Confirmation");
        firstTalent = LoreGuiItem.setActionData(firstTalent, "level", "" + configLevelTalent.level);
        secondTalent = LoreGuiItem.setActionData(secondTalent, "buff", configLevelTalent.levelBuff2);
        secondTalent = LoreGuiItem.setActionData(secondTalent, "number", "2");
        secondTalent = LoreGuiItem.setActionData(secondTalent, "description", configLevelTalent.levelBuffDesc2);
        secondTalent = LoreGuiItem.setActionData(secondTalent, "passFields", "buff,number,description");
        secondTalent = LoreGuiItem.setActionData(secondTalent, "passAction", "ChooseTalent");
        secondTalent = LoreGuiItem.setActionData(secondTalent, "confirmText", CivSettings.localize.localizedString("cmd_civ_talent_choose_confirmText", CivColor.GreenBold + cultureLevel + "§a", CivColor.GoldBold + "2" + "§a"));
        secondTalent = LoreGuiItem.setActionData(secondTalent, "confirmText2", CivColor.GoldBold + configLevelTalent.levelBuffDesc2);
        ItemStack thirdTalent = LoreGuiItem.build("", ItemManager.getId(Material.LAPIS_BLOCK), 0, configLevelTalent.levelBuffDesc3);
        thirdTalent = LoreGuiItem.setActionData(thirdTalent, "confirmText2", CivColor.BlueBold + configLevelTalent.levelBuffDesc3);
        thirdTalent = LoreGuiItem.setAction(thirdTalent, "Confirmation");
        firstTalent = LoreGuiItem.setActionData(firstTalent, "level", "" + configLevelTalent.level);
        thirdTalent = LoreGuiItem.setActionData(thirdTalent, "buff", configLevelTalent.levelBuff3);
        thirdTalent = LoreGuiItem.setActionData(thirdTalent, "number", "3");
        thirdTalent = LoreGuiItem.setActionData(thirdTalent, "description", configLevelTalent.levelBuffDesc3);
        thirdTalent = LoreGuiItem.setActionData(thirdTalent, "passFields", "buff,number,description");
        thirdTalent = LoreGuiItem.setActionData(thirdTalent, "passAction", "ChooseTalent");
        thirdTalent = LoreGuiItem.setActionData(thirdTalent, "confirmText", CivSettings.localize.localizedString("cmd_civ_talent_choose_confirmText", CivColor.GreenBold + cultureLevel + "§a", CivColor.GoldBold + "3" + "§a"));
        this.inventory.addItem(new ItemStack[]{firstTalent});
        this.inventory.addItem(new ItemStack[]{secondTalent});
        this.inventory.addItem(new ItemStack[]{thirdTalent});
        ItemStack backButton = LoreGuiItem.build(CivSettings.localize.localizedString("bookReborn_back"), ItemManager.getId(Material.MAP), 0, CivSettings.localize.localizedString("bookReborn_backTo", BookTalentGui.inventory.getName()));
        backButton = LoreGuiItem.setAction(backButton, "OpenInventory");
        backButton = LoreGuiItem.setActionData(backButton, "invType", "showGuiInv");
        backButton = LoreGuiItem.setActionData(backButton, "invName", BookTalentGui.inventory.getName());
        this.inventory.setItem(8, backButton);
        LoreGuiItemListener.guiInventories.put(this.inventory.getName(), this.inventory);
        TaskMaster.syncTask(new OpenInventoryTask(player, this.inventory));
    }
}

