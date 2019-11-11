
package com.avrgaming.civcraft.structure;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.items.units.Unit;
import com.avrgaming.civcraft.main.CivCraft;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.StructureSign;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.structure.Structure;
import com.avrgaming.civcraft.util.BlockCoord;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;
import com.avrgaming.civcraft.util.SimpleBlock;
import com.avrgaming.civcraft.war.War;

public class Portal
extends Structure {
    public Location spawnLocation;

    protected Portal(Location center, String id, Town town) throws CivException {
        super(center, id, town);
    }

    public Portal(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
    }

    @Override
    public String getDynmapDescription() {
        return null;
    }

    @Override
    public String getMarkerIconName() {
        return "pin";
    }

    @Override
    public void processSignAction(Player player, StructureSign sign, PlayerInteractEvent event) throws CivException {
        Resident resident = CivGlobal.getResident(player);
        if (resident == null) {
            return;
        }
        switch (sign.getAction()) {
            case "teleport": {
                if (War.isWarTime()) {
                    throw new CivException(CivSettings.localize.localizedString("var_portal_wartime", this.getCiv().getName()));
                }
                if (resident.isProtected()) {
                    throw new CivException(CivSettings.localize.localizedString("var_portal_pvptimer"));
                }
                if (!Unit.isWearingFullHell(player)) {
                    throw new CivException(CivSettings.localize.localizedString("var_portal_notFullSet"));
                }
                boolean right = CivCraft.civRandom.nextBoolean();
                Location bossLocation = right ? new Location(Bukkit.getWorld((String)"world_nether"), 143.0, 147.0, -613.0) : new Location(Bukkit.getWorld((String)"world_nether"), 1.0, 148.0, -610.0);
                CivMessage.sendSuccess((CommandSender)player, CivSettings.localize.localizedString("var_portal_teleporting", CivColor.Red));
                player.teleport(bossLocation);
            }
        }
    }

    @Override
    public void onPostBuild(BlockCoord absCoord, SimpleBlock commandBlock) {
        if (commandBlock.command.equals("/teleport")) {
            ItemManager.setTypeId(absCoord.getBlock(), commandBlock.getType());
            ItemManager.setData(absCoord.getBlock(), commandBlock.getData());
            StructureSign structSign = new StructureSign(absCoord, this);
            structSign.setText(CivSettings.localize.localizedString("structure_portal_sign"));
            structSign.setDirection(commandBlock.getData());
            structSign.setAction("teleport");
            structSign.update();
            this.addStructureSign(structSign);
            CivGlobal.addStructureSign(structSign);
            if (this.spawnLocation == null) {
                this.spawnLocation = structSign.getCoord().getLocation();
            }
        }
    }
}

