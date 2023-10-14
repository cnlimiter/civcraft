package cn.evole.plugins.civcraft.loregui;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.config.ConfigBuildableInfo;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.lorestorage.LoreGuiItem;
import cn.evole.plugins.civcraft.main.CivData;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.structure.Structure;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.civcraft.tutorial.Book;
import cn.evole.plugins.civcraft.util.CivColor;
import cn.evole.plugins.civcraft.util.ItemManager;
import cn.evole.plugins.global.perks.Perk;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class BuildChooseTemplate implements GuiAction {

    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        Player player = (Player) event.getWhoClicked();
        Resident resident = CivGlobal.getResident(player);
        ConfigBuildableInfo sinfo = CivSettings.structures.get(LoreGuiItem.getActionData(stack, "info"));
        Structure struct;
        try {
            struct = Structure.newStructure(player.getLocation(), sinfo.id, resident.getTown());
        } catch (CivException e) {
            e.printStackTrace();
            return;
        }

        /* Look for any custom template perks and ask the player if they want to use them. */
        ArrayList<Perk> perkList = struct.getTown().getTemplatePerks(struct, resident, struct.info);
        ArrayList<Perk> personalUnboundPerks = resident.getUnboundTemplatePerks(perkList, struct.info);
        //if (perkList.size() != 0 || personalUnboundPerks.size() != 0) {
        /* Store the pending buildable. */
        resident.pendingBuildable = struct;

        /* Build an inventory full of templates to select. */
        Inventory inv = Bukkit.getServer().createInventory(player, Book.MAX_CHEST_SIZE * 9);
        ItemStack infoRec = LoreGuiItem.build("Default " + struct.getDisplayName(),
                ItemManager.getId(Material.WRITTEN_BOOK),
                0, CivColor.Gold + CivSettings.localize.localizedString("loreGui_template_clickToBuild"));
        infoRec = LoreGuiItem.setAction(infoRec, "BuildWithTemplate");
        inv.addItem(infoRec);

        for (Perk perk : perkList) {
            if (!perk.getIdent().contains("template")) {
                infoRec = LoreGuiItem.build(perk.getDisplayName(),
                        perk.configPerk.type_id,
                        perk.configPerk.data, CivColor.Gold + CivSettings.localize.localizedString("loreGui_template_clickToBuild"),
                        CivColor.Gray + CivSettings.localize.localizedString("loreGui_template_providedBy") + " " + CivColor.LightBlue + perk.provider);
                infoRec = LoreGuiItem.setAction(infoRec, "BuildWithTemplate");
                infoRec = LoreGuiItem.setActionData(infoRec, "perk", perk.getIdent());
                inv.addItem(infoRec);
            }
        }

        for (Perk perk : personalUnboundPerks) {
            if (!perk.getIdent().contains("template")) {
                infoRec = LoreGuiItem.build(perk.getDisplayName(),
                        CivData.BEDROCK,
                        perk.configPerk.data, CivColor.Gold + CivSettings.localize.localizedString("loreGui_template_clickToBuild"),
                        CivColor.Gray + CivSettings.localize.localizedString("loreGui_template_unbound"),
                        CivColor.Gray + CivSettings.localize.localizedString("loreGui_template_unbound2"),
                        CivColor.Gray + CivSettings.localize.localizedString("loreGui_template_unbound3"),
                        CivColor.Gray + CivSettings.localize.localizedString("loreGui_template_unbound4"),
                        CivColor.Gray + CivSettings.localize.localizedString("loreGui_template_unbound5"));
                infoRec = LoreGuiItem.setAction(infoRec, "ActivatePerk");
                infoRec = LoreGuiItem.setActionData(infoRec, "perk", perk.getIdent());
                inv.addItem(infoRec);
            }
        }

        TaskMaster.syncTask(new OpenInventoryTask(player, inv));
        return;
    }
}
