/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.components;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.threading.TaskMaster;
import cn.evole.plugins.civcraft.util.BlockCoord;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class SignSelectionComponent extends Component {

    private int selectedIndex = 0;

    private BlockCoord actionSignCoord;
    private HashMap<Integer, SelectionItem> items = new HashMap<Integer, SelectionItem>();

    @Override
    public void onLoad() {
    }

    @Override
    public void onSave() {
    }

    public void addItem(String[] message, SignSelectionActionInterface action) {

        SelectionItem item = new SelectionItem();
        item.message = message;
        item.action = action;
        items.put(items.size(), item);
    }

    public void updateActionSign() {
        class SyncTask implements Runnable {
            @Override
            public void run() {
                if (actionSignCoord == null) {
                    CivLog.warning("No action sign block coord found!");
                    return;
                }

                Block block = actionSignCoord.getBlock();

                if (block.getState() instanceof Sign) {
                    Sign sign = (Sign) block.getState();
                    SelectionItem item = items.get(selectedIndex);
                    if (item != null) {
                        sign.setLine(0, item.message[0]);
                        sign.setLine(1, item.message[1]);
                        sign.setLine(2, item.message[2]);
                        sign.setLine(3, item.message[3]);
                        sign.update();
                    } else {
                        sign.setLine(0, "");
                        sign.setLine(1, CivSettings.localize.localizedString("Nothing"));
                        sign.setLine(2, CivSettings.localize.localizedString("Available"));
                        sign.setLine(3, "");
                        sign.update();
                    }
                }
            }
        }
        TaskMaster.syncTask(new SyncTask());
    }

    public void processNext() {
        selectedIndex++;

        if (selectedIndex >= items.size()) {
            selectedIndex = 0;
        }

        updateActionSign();
    }

    public void processPrev() {
        selectedIndex--;

        if (selectedIndex < 0) {
            selectedIndex = items.size() - 1;
        }

        updateActionSign();
    }

    public void processAction(Player player) {
        SelectionItem item = items.get(selectedIndex);
        if (item == null) {
            CivLog.warning("Selected index:" + selectedIndex + " has no selection item!");
            return;
        }

        item.action.process(player);
    }

    public void setActionSignCoord(BlockCoord absCoord) {
        this.actionSignCoord = absCoord;
    }

    public void setMessageAllItems(int i, String string) {
        for (SelectionItem item : items.values()) {
            item.message[i] = string;
        }
        updateActionSign();
    }

    private class SelectionItem {
        public String[] message;
        public SignSelectionActionInterface action;
    }


}
