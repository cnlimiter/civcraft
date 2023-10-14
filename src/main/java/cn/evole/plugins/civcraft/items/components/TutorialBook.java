package cn.evole.plugins.civcraft.items.components;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.tutorial.Book;
import cn.evole.plugins.civcraft.util.CivColor;
import gpl.AttributeUtil;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class TutorialBook extends ItemComponent {

    @Override
    public void onPrepareCreate(AttributeUtil attrs) {
        attrs.addLore(CivColor.Gold + CivSettings.localize.localizedString("tutorialBook_lore1"));
        attrs.addLore(CivColor.Rose + CivSettings.localize.localizedString("tutorialBook_lore2"));
    }


    public void onInteract(PlayerInteractEvent event) {

        event.setCancelled(true);
        if (!event.getAction().equals(Action.RIGHT_CLICK_AIR) &&
                !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        //CivTutorial.showCraftingHelp(event.getPlayer());
        Book.spawnGuiBook(event.getPlayer());

    }

    public void onItemSpawn(ItemSpawnEvent event) {
        event.setCancelled(true);
    }


}
