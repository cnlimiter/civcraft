
package com.avrgaming.civcraft.loregui.book;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.exception.InvalidConfiguration;
import com.avrgaming.civcraft.loregui.GuiAction;
import com.avrgaming.civcraft.main.CivMessage;


public class BookLinksAction
implements GuiAction {
    @Override
    public void performAction(InventoryClickEvent event, ItemStack stack) {
        CivMessage.send((Object)event.getWhoClicked(), "§a" + CivSettings.localize.localizedString("cmd_wiki_wikiLink", "http://wiki.minetexas.com/index.php/Civcraft_Wiki"));
        try {
			String url = CivSettings.getStringBase("dynmap_url");
			if (!url.isEmpty()) {
		        CivMessage.send((Object)event.getWhoClicked(), "§2" + CivSettings.localize.localizedString("cmd_map_dynmapLink", url));
			}
		} catch (InvalidConfiguration e) {
	        CivMessage.send((Object)event.getWhoClicked(), "§2" + CivSettings.localize.localizedString("cmd_map_dynmapLink", "None"));
		}
    }
}

