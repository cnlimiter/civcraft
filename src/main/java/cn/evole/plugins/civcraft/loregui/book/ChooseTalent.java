package cn.evole.plugins.civcraft.loregui.book;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.loregui.GuiAction;
import cn.evole.plugins.civcraft.lorestorage.LoreGuiItem;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.object.Talent;
import cn.evole.plugins.civcraft.object.Town;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;

public class ChooseTalent implements GuiAction {
    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        Player player = (Player) event.getWhoClicked();
        Resident whoClicked = CivGlobal.getResident(player);
        Civilization civ = whoClicked.getCiv();
        Town capitol = civ.getCapitol();
        if (capitol == null) {
            return;
        }
        String buff = LoreGuiItem.getActionData(stack, "buff");
        int level = Integer.parseInt(LoreGuiItem.getActionData(stack, "level"));
        String description = LoreGuiItem.getActionData(stack, "description");
        Integer number = Integer.valueOf(LoreGuiItem.getActionData(stack, "number"));
        if (capitol.highestTalentLevel() >= level) {
            CivMessage.sendError((Object) player, CivSettings.localize.localizedString("cmd_civ_talent_choose_notNow", civ.getCapitol().getName(), civ.getCapitol().getCultureLevel() + 1));
            return;
        }
        Talent talent = new Talent(level, buff);
        try {
            capitol.addTalent(talent);
            capitol.saveNow();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (CivException e) {
            e.printStackTrace();
        }
        CivMessage.sendCiv(civ, CivSettings.localize.localizedString("cmd_civ_talent_choose_sucusses", player.getDisplayName(), number, description, capitol.getCultureLevel()));
        CivMessage.send((Object) player, "§a" + CivSettings.localize.localizedString("cmd_civ_talent_choose_sucussesSender"));
        player.closeInventory();
    }

    protected void addBuffToTown(Town town, String id) {
        try {
            town.getBuffManager().addBuff(id, id, "Capitol in " + town.getName());
        } catch (CivException e) {
            e.printStackTrace();
        }
    }
}

