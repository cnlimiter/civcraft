package cn.evole.plugins.civcraft.loregui;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.lorestorage.LoreGuiItem;
import cn.evole.plugins.civcraft.lorestorage.LoreGuiItemListener;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.civcraft.util.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class Confirmation implements GuiAction {
    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        Inventory inv = Bukkit.createInventory((InventoryHolder) event.getWhoClicked(), (int) 27, (String) "§a" + CivSettings.localize.localizedString("resident_tradeNotconfirmed"));
        String fields = LoreGuiItem.getActionData(stack, "passFields");
        String action = LoreGuiItem.getActionData(stack, "passAction");
        String confirmText = LoreGuiItem.getActionData(stack, "confirmText");
        ItemStack confirm = LoreGuiItem.build("§a" + confirmText, ItemManager.getId(Material.EMERALD_BLOCK), 0);
        confirm = LoreGuiItem.setAction(confirm, action);
        for (String field : fields.split(",")) {
            confirm = LoreGuiItem.setActionData(confirm, field, LoreGuiItem.getActionData(stack, field));
        }
        inv.setItem(11, confirm);
        ItemStack cancel = LoreGuiItem.build("§c" + CivSettings.localize.localizedString("loregui_cancel"), ItemManager.getId(Material.REDSTONE_BLOCK), 0, new String[0]);
        cancel = LoreGuiItem.setAction(cancel, "CloseInventory");
        inv.setItem(15, cancel);
        LoreGuiItemListener.guiInventories.put(inv.getName(), inv);
        TaskMaster.syncTask(new OpenInventoryTask((Player) event.getWhoClicked(), inv));
    }
}

