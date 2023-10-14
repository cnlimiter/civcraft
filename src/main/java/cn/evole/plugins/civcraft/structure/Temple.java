/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.structure;

import cn.evole.plugins.civcraft.components.ConsumeLevelComponent;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.config.ConfigTempleLevel;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.exception.CivTaskAbortException;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.StructureChest;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.threading.CivAsyncTask;
import cn.evole.plugins.civcraft.util.CivColor;
import cn.evole.plugins.civcraft.util.MultiInventory;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Temple extends Structure {

    private ConsumeLevelComponent consumeComp = null;

    protected Temple(Location center, String id, Town town) throws CivException {
        super(center, id, town);
    }

    public Temple(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }

    public ConsumeLevelComponent getConsumeComponent() {
        if (consumeComp == null) {
            consumeComp = (ConsumeLevelComponent) this.getComponent(ConsumeLevelComponent.class.getSimpleName());
        }
        return consumeComp;
    }

    @Override
    public void loadSettings() {
        super.loadSettings();

//		attrComp = new AttributeComponent();
//		attrComp.setType(AttributeType.DIRECT);
//		attrComp.setOwnerKey(this.getTown().getName());
//		attrComp.setAttrKey(Attribute.TypeKeys.COINS.name());
//		attrComp.setSource("Cottage("+this.getCorner().toString()+")");
//		attrComp.registerComponent();
    }

    @Override
    public String getDynmapDescription() {
        if (getConsumeComponent() == null) {
            return "";
        }

        String out = "";
        out += CivSettings.localize.localizedString("Level") + " " + getConsumeComponent().getLevel() + " " + getConsumeComponent().getCountString();
        return out;
    }

    @Override
    public String getMarkerIconName() {
        return "church";
    }

    public String getkey() {
        return this.getTown().getName() + "_" + this.getConfigId() + "_" + this.getCorner().toString();
    }

    public ConsumeLevelComponent.Result consume(CivAsyncTask task) throws InterruptedException {

        //Look for the temple's chest.
        if (this.getChests().size() == 0)
            return ConsumeLevelComponent.Result.STAGNATE;

        MultiInventory multiInv = new MultiInventory();

        ArrayList<StructureChest> chests = this.getAllChestsById(1);

        // Make sure the chest is loaded and add it to the multi inv.
        for (StructureChest c : chests) {
            task.syncLoadChunk(c.getCoord().getWorldname(), c.getCoord().getX(), c.getCoord().getZ());
            Inventory tmp;
            try {
                tmp = task.getChestInventory(c.getCoord().getWorldname(), c.getCoord().getX(), c.getCoord().getY(), c.getCoord().getZ(), true);
            } catch (CivTaskAbortException e) {
                return ConsumeLevelComponent.Result.STAGNATE;
            }
            multiInv.addInventory(tmp);
        }
        getConsumeComponent().setSource(multiInv);
        getConsumeComponent().setConsumeRate(1.0);
        try {
            ConsumeLevelComponent.Result result = getConsumeComponent().processConsumption();
            getConsumeComponent().onSave();
            return result;
        } catch (IllegalStateException e) {
            CivLog.exception(this.getDisplayName() + " Process Error in town: " + this.getTown().getName() + " and Location: " + this.getCorner(), e);
            return ConsumeLevelComponent.Result.STAGNATE;
        }
    }

    public void templeCulture(CivAsyncTask task) throws InterruptedException {
        ConsumeLevelComponent.Result result = this.consume(task);
        switch (result) {
            case STARVE:
                CivMessage.sendTown(getTown(), CivColor.Rose + CivSettings.localize.localizedString("var_temple_productionFell", getConsumeComponent().getLevel(), getConsumeComponent().getCountString()));
                return;
            case LEVELDOWN:
                CivMessage.sendTown(getTown(), CivColor.Rose + CivSettings.localize.localizedString("var_temple_lostalvl", getConsumeComponent().getLevel()));
                return;
            case STAGNATE:
                CivMessage.sendTown(getTown(), CivColor.Rose + CivSettings.localize.localizedString("var_temple_stagnated", getConsumeComponent().getLevel(), getConsumeComponent().getCountString()));
                return;
            case GROW:
                CivMessage.sendTown(getTown(), CivColor.LightGreen + CivSettings.localize.localizedString("var_temple_productionGrew", getConsumeComponent().getLevel(), getConsumeComponent().getCountString()));
                break;
            case LEVELUP:
                CivMessage.sendTown(getTown(), CivColor.LightGreen + CivSettings.localize.localizedString("var_temple_lvlUp", getConsumeComponent().getLevel()));
                break;
            case MAXED:
                CivMessage.sendTown(getTown(), CivColor.LightGreen + CivSettings.localize.localizedString("var_temple_maxed", getConsumeComponent().getLevel(), getConsumeComponent().getCountString()));
                break;
            case UNKNOWN:
                CivMessage.sendTown(getTown(), CivColor.DarkPurple + CivSettings.localize.localizedString("temple_unknown"));
                return;
            default:
                break;
        }

        ConfigTempleLevel lvl = null;
        if (result == ConsumeLevelComponent.Result.LEVELUP) {
            lvl = CivSettings.templeLevels.get(getConsumeComponent().getLevel() - 1);
        } else {
            lvl = CivSettings.templeLevels.get(getConsumeComponent().getLevel());
        }

        int total_culture = (int) Math.round(lvl.culture * this.getTown().getCottageRate());
//		if (this.getTown().getBuffManager().hasBuff("buff_pyramid_cottage_bonus")) {
//			total_coins *= this.getTown().getBuffManager().getEffectiveDouble("buff_pyramid_cottage_bonus");
//		}
        this.getTown().addAccumulatedCulture(total_culture);
        this.getTown().save();

        CivMessage.sendTown(getTown(), CivColor.LightGreen + CivSettings.localize.localizedString("var_temple_cultureGenerated", (CivColor.LightPurple + total_culture + CivColor.LightGreen)));

    }

    public int getLevel() {
        return this.getConsumeComponent().getLevel();
    }

    public int getCount() {
        return this.getConsumeComponent().getCount();
    }

    public int getMaxCount() {
        int level = getLevel();

        ConfigTempleLevel lvl = CivSettings.templeLevels.get(level);
        return lvl.count;
    }

    public ConsumeLevelComponent.Result getLastResult() {
        return this.getConsumeComponent().getLastResult();
    }

    public double getCultureGenerated() {
        int level = getLevel();

        ConfigTempleLevel lvl = CivSettings.templeLevels.get(level);
        if (lvl == null) {
            return 0;
        }
        return lvl.culture;
    }

    public void delevel() {
        int currentLevel = getLevel();

        if (currentLevel > 1) {
            getConsumeComponent().setLevel(getLevel() - 1);
            getConsumeComponent().setCount(0);
            getConsumeComponent().onSave();
        }
    }

    @Override
    public void delete() throws SQLException {
        super.delete();
        if (getConsumeComponent() != null) {
            getConsumeComponent().onDelete();
        }
    }

    public void onDestroy() {
        super.onDestroy();

        getConsumeComponent().setLevel(1);
        getConsumeComponent().setCount(0);
        getConsumeComponent().onSave();
    }


}
