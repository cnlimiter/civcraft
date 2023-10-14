package cn.evole.plugins.civcraft.loregui.book;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.config.ConfigLevelTalent;
import cn.evole.plugins.civcraft.loregui.GuiAction;
import cn.evole.plugins.civcraft.loregui.OpenInventoryTask;
import cn.evole.plugins.civcraft.lorestorage.LoreGuiItem;
import cn.evole.plugins.civcraft.lorestorage.LoreGuiItemListener;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.civcraft.util.CivColor;
import cn.evole.plugins.civcraft.util.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class TalentChoose implements GuiAction {
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
            CivMessage.sendError(player, CivSettings.localize.localizedString("cmd_civ_talent_choose_notNow", civ.getCapitol().getName(), civ.getCapitol().getCultureLevel() + 1));
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
        firstTalent = LoreGuiItem.setActionData(firstTalent, "level", String.valueOf(configLevelTalent.level));
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
        this.inventory.addItem(firstTalent);
        this.inventory.addItem(secondTalent);
        this.inventory.addItem(thirdTalent);
        ItemStack backButton = LoreGuiItem.build(CivSettings.localize.localizedString("bookReborn_back"), ItemManager.getId(Material.MAP), 0, CivSettings.localize.localizedString("bookReborn_backTo", BookTalentGui.inventory.getName()));
        backButton = LoreGuiItem.setAction(backButton, "OpenInventory");
        backButton = LoreGuiItem.setActionData(backButton, "invType", "showGuiInv");
        backButton = LoreGuiItem.setActionData(backButton, "invName", BookTalentGui.inventory.getName());
        this.inventory.setItem(8, backButton);
        LoreGuiItemListener.guiInventories.put(this.inventory.getName(), this.inventory);
        TaskMaster.syncTask(new OpenInventoryTask(player, this.inventory));
    }
}

