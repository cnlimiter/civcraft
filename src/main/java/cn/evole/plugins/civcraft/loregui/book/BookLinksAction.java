package cn.evole.plugins.civcraft.loregui.book;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.InvalidConfiguration;
import cn.evole.plugins.civcraft.loregui.GuiAction;
import cn.evole.plugins.civcraft.main.CivMessage;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;


public class BookLinksAction implements GuiAction {
    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        CivMessage.send(event.getWhoClicked(), "§a" + CivSettings.localize.localizedString("cmd_wiki_wikiLink", "http://wiki.minetexas.com/index.php/Civcraft_Wiki"));
        try {
            String url = CivSettings.getStringBase("dynmap_url");
            if (!url.isEmpty()) {
                CivMessage.send(event.getWhoClicked(), "§2" + CivSettings.localize.localizedString("cmd_map_dynmapLink", url));
            }
        } catch (InvalidConfiguration e) {
            CivMessage.send(event.getWhoClicked(), "§2" + CivSettings.localize.localizedString("cmd_map_dynmapLink", "None"));
        }
    }
}

