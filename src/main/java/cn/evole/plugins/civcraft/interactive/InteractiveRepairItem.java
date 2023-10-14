package cn.evole.plugins.civcraft.interactive;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.lorestorage.LoreCraftableMaterial;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.structure.Barracks;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.entity.Player;

public class InteractiveRepairItem implements InteractiveResponse {

    double cost;
    String playerName;
    LoreCraftableMaterial craftMat;

    public InteractiveRepairItem(double cost, String playerName, LoreCraftableMaterial craftMat) {
        this.cost = cost;
        this.playerName = playerName;
        this.craftMat = craftMat;
    }

    public void displayMessage() {
        Player player;
        try {
            player = CivGlobal.getPlayer(playerName);
        } catch (CivException e) {
            return;
        }

        CivMessage.sendHeading(player, CivSettings.localize.localizedString("interactive_repair_heading"));
        CivMessage.send(player, CivColor.LightGreen + CivColor.BOLD + CivSettings.localize.localizedString("var_interactive_repair_prompt1", craftMat.getName()));
        CivMessage.send(player, CivColor.LightGreen + CivColor.BOLD + CivSettings.localize.localizedString("var_interactive_repair_prompt2", CivColor.Yellow + CivColor.BOLD + cost + CivColor.LightGreen, CivColor.Yellow + CivColor.BOLD + CivSettings.CURRENCY_NAME + CivColor.LightGreen));
        CivMessage.send(player, CivColor.LightGreen + CivColor.BOLD + CivSettings.localize.localizedString("interactive_repair_prompt3"));

    }


    @Override
    public void respond(String message, Resident resident) {
        resident.clearInteractiveMode();

        if (!message.equalsIgnoreCase("yes")) {
            CivMessage.send(resident, CivColor.LightGray + CivSettings.localize.localizedString("interactive_repair_canceled"));
            return;
        }

        Barracks.repairItemInHand(cost, resident.getName(), craftMat);
    }

}
