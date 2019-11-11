
package com.avrgaming.civcraft.items.components;

import gpl.AttributeUtil;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.items.components.ItemComponent;
import com.avrgaming.civcraft.lorestorage.LoreCraftableMaterial;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.util.CallbackInterface;
import com.avrgaming.civcraft.util.CivColor;

public class ResearchSpaceShuttle extends ItemComponent implements CallbackInterface {
    @Override
    public void onPrepareCreate(AttributeUtil attrUtil) {
        attrUtil.addLore((Object)ChatColor.RESET + CivColor.Gold + CivSettings.localize.localizedString("researchSpaceShuttle_lore1"));
        attrUtil.addLore((Object)ChatColor.RESET + CivColor.Red + CivSettings.localize.localizedString("itemLore_RightClickToUse"));
        attrUtil.addEnhancement("LoreEnhancementSoulBound", null, null);
        attrUtil.addLore(CivColor.Gold + CivSettings.localize.localizedString("Soulbound"));
    }

    @Override
    public void onInteract(PlayerInteractEvent event) {
        this.execute(event.getPlayer().getName());
    }

    @Override
    public void execute(String playerName) {
        Player player;
        try {
            player = CivGlobal.getPlayer(playerName);
        }
        catch (CivException e) {
            return;
        }
        Resident interactor = CivGlobal.getResident(player);
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(itemStack);
        if (craftMat == null || !craftMat.hasComponent("ResearchSpaceShuttle")) {
            CivMessage.sendError((Object)player, CivSettings.localize.localizedString("researchSpaceShuttle_lore2"));
            return;
        }
        if (interactor.getCiv() == null) {
            CivMessage.sendError((Object)player, CivSettings.localize.localizedString("researchSpaceShuttle_lore3"));
            return;
        }
        Civilization civ = interactor.getCiv();
        if (!civ.getLeaderGroup().hasMember(interactor)) {
            CivMessage.sendError((Object)player, CivSettings.localize.localizedString("researchSpaceShuttle_lore4", civ.getName()));
            return;
        }
        if (civ.hasTechnology("tech_space_shuttle")) {
            CivMessage.sendError((Object)player, CivSettings.localize.localizedString("researchSpaceShuttle_lore5"));
            return;
        }
        ItemStack newStack = new ItemStack(Material.AIR);
        player.getInventory().setItemInMainHand(newStack);
        civ.addTech(CivSettings.techs.get("tech_space_shuttle"));
        CivMessage.sendSuccess((CommandSender)player, CivSettings.localize.localizedString("researchSpaceShuttle_lore6"));
        String fullName = player.getDisplayName();
        String message = CivSettings.localize.localizedString("researchSpaceShuttle_lore7", fullName, civ.getName());
        CivMessage.global(message);
        CivMessage.sendCiv(civ, CivSettings.localize.localizedString("researchSpaceShuttle_lore8", fullName));
    }
}

