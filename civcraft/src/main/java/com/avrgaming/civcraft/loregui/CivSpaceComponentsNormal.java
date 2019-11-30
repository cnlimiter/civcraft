
package com.avrgaming.civcraft.loregui;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigSpaceRocket;
import com.avrgaming.civcraft.lorestorage.LoreCraftableMaterial;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.lorestorage.LoreGuiItemListener;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.util.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class CivSpaceComponentsNormal
        implements GuiAction {
    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        Player player = (Player) event.getWhoClicked();
        Inventory guiInventory = Bukkit.getServer().createInventory((InventoryHolder) player, 9, CivSettings.localize.localizedString("bookReborn_civSpaceComponentsHeading"));
        int i = Integer.valueOf(LoreGuiItem.getActionData(stack, "i"));
        boolean fromEnded = Boolean.valueOf(LoreGuiItem.getActionData(stack, "b"));
        ConfigSpaceRocket configSpaceRocket = CivSettings.spaceRocket_name.get(i);
        for (String craftMatID : configSpaceRocket.components.split(":")) {
            int count = Integer.parseInt(craftMatID.replaceAll("[^\\d]", ""));
            String craftMat = craftMatID.replace(String.valueOf(count), "");
            LoreCraftableMaterial itemToGetName = LoreCraftableMaterial.getCraftMaterialFromId(craftMat);
            ItemStack itemStack = LoreGuiItem.build(itemToGetName.getName(), itemToGetName.getConfigMaterial().item_id, itemToGetName.getConfigMaterial().item_data, "§6" + CivSettings.localize.localizedString("bookReborn_civSpaceMenu"));
            itemStack.setAmount(count);
            guiInventory.addItem(itemStack);
        }
        String backTo = fromEnded ? CivSettings.localize.localizedString("bookReborn_civSpaceEndedHeading") : CivSettings.localize.localizedString("bookReborn_civSpaceFutureHeading");
        ItemStack backButton = LoreGuiItem.build(CivSettings.localize.localizedString("loreGui_recipes_back"), ItemManager.getId(Material.MAP), 0, CivSettings.localize.localizedString("loregui_backto") + " " + backTo);
        backButton = LoreGuiItem.setAction(backButton, "OpenInventory");
        backButton = LoreGuiItem.setActionData(backButton, "invType", "showGuiInv");
        backButton = LoreGuiItem.setActionData(backButton, "invName", fromEnded ? CivSpaceEnded.guiInventory.getName() : CivSpaceFuture.guiInventory.getName());
        guiInventory.setItem(8, backButton);
        LoreGuiItemListener.guiInventories.put(guiInventory.getName(), guiInventory);
        TaskMaster.syncTask(new OpenInventoryTask(player, guiInventory));
    }
}

