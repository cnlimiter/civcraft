package cn.evole.plugins.civcraft.loregui.book;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.loregui.GuiAction;
import cn.evole.plugins.civcraft.main.CivMessage;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;


public class BookTutorialWikiLink
        implements GuiAction {
    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        CivMessage.send((Object) event.getWhoClicked(), "§a" + CivSettings.localize.localizedString("cmd_wiki_wikiLink", "http://wiki.minetexas.com/index.php/Civcraft_Wiki"));
    }
}

