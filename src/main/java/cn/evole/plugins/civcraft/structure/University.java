package cn.evole.plugins.civcraft.structure;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.StructureSign;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.util.CivColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.sql.ResultSet;
import java.sql.SQLException;

public class University extends Structure {


    protected University(Location center, String id, Town town) throws CivException {
        super(center, id, town);
    }

    public University(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();

    }

    @Override
    public String getMarkerIconName() {
        return "bronzestar";
    }

    private StructureSign getSignFromSpecialId(int special_id) {
        for (StructureSign sign : getSigns()) {
            int id = Integer.valueOf(sign.getAction());
            if (id == special_id) {
                return sign;
            }
        }
        return null;
    }

    @Override
    public void updateSignText() {
        int count = 0;
        for (; count < getSigns().size(); count++) {
            StructureSign sign = getSignFromSpecialId(count);
            if (sign == null) {
                CivLog.error("University sign was null");
                return;
            }

            sign.setText("\n" + CivSettings.localize.localizedString("university_sign", this.getTown().getName())
            );

            sign.update();
        }
    }

    @Override
    public void processSignAction(Player player, StructureSign sign, PlayerInteractEvent event) {
        CivMessage.send(player, CivColor.Green + CivSettings.localize.localizedString("university_sign", this.getTown().getName()));
    }


}
