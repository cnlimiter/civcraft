
package com.avrgaming.civcraft.structure;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigUnit;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.StructureChest;
import com.avrgaming.civcraft.object.StructureSign;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.sessiondb.SessionEntry;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.threading.tasks.ArtifactSaveAsyncTask;
import com.avrgaming.civcraft.util.BlockCoord;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;
import com.avrgaming.civcraft.util.SimpleBlock;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;

public class Ordinary extends Structure {
    private int index = 0;
    private StructureSign artifactNameSign;
    private ConfigUnit trainingArtifact = null;
    private double currentHammers = 0.0;
    private TreeMap<Integer, StructureSign> progresBar = new TreeMap<Integer, StructureSign>();
    private Date lastSave = null;

    protected Ordinary(Location center, String id, Town town) throws CivException {
        super(center, id, town);
    }

    public Ordinary(ResultSet rs) throws SQLException, CivException {
        super(rs);
    }

    @Override
    public String getMarkerIconName() {
        return "list_on";
    }

    private String getArtifactSignText(int index) throws IndexOutOfBoundsException {
        ArrayList<ConfigUnit> artifactList = this.getTown().getAvailableArtifacts();
        if (artifactList.size() == 0) {
            return "\n§7" + CivSettings.localize.localizedString("Nothing") + "\n" + CivColor.LightGray + CivSettings.localize.localizedString("Available");
        }
        ConfigUnit unit = artifactList.get(index);
        String out = "\n";
        double coinCost = unit.cost;
        out = out + CivColor.LightPurple + unit.name + "\n";
        out = out + CivColor.Yellow + coinCost + "\n";
        out = out + CivColor.Yellow + CivSettings.CURRENCY_NAME;
        return out;
    }

    private void changeIndex(int newIndex) {
        if (this.artifactNameSign != null) {
            try {
                this.artifactNameSign.setText(this.getArtifactSignText(newIndex));
                this.index = newIndex;
            } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                // empty catch block
            }
            this.artifactNameSign.update();
        } else {
            CivLog.warning("Could not find unit name sign for ordinary:" + this.getId() + " at " + this.getCorner());
        }
    }

    private void train() throws CivException {
        ArrayList<ConfigUnit> artifactList = this.getTown().getAvailableArtifacts();
        ConfigUnit artifact = artifactList.get(this.index);
        if (artifact == null) {
            throw new CivException(CivSettings.localize.localizedString("ordinary_unknownArtifact"));
        }
        if (artifact.id.contains("u_")) {
            throw new CivException(CivSettings.localize.localizedString("ordinary_settlerOnlyInBarracks"));
        }
        if (artifact.limit != 0 && artifact.limit < this.getTown().getArtifactTypeCount(artifact.id)) {
            throw new CivException(CivSettings.localize.localizedString("var_ordinary_atLimit", artifact.name));
        }
        if (!artifact.isAvailable(this.getTown())) {
            throw new CivException(CivSettings.localize.localizedString("ordinary_unavailable"));
        }
        if (this.trainingArtifact != null) {
            throw new CivException(CivSettings.localize.localizedString("var_ordinary_inProgress", this.trainingArtifact.name));
        }
        double coinCost = artifact.cost;
        if (!this.getTown().getTreasury().hasEnough(coinCost)) {
            throw new CivException(CivSettings.localize.localizedString("var_barracks_tooPoor", artifact.name, coinCost, CivSettings.CURRENCY_NAME));
        }
        this.getTown().getTreasury().withdraw(coinCost);
        if (!this.getTown().getTreasury().hasEnough(coinCost)) {
            throw new CivException(CivSettings.localize.localizedString("var_ordinary_tooPoor", artifact.name, coinCost, CivSettings.CURRENCY_NAME));
        }
        this.getTown().getTreasury().withdraw(coinCost);
        this.setCurrentHammers(0.0);
        this.setTrainingArtifact(artifact);
        CivMessage.sendTown(this.getTown(), CivSettings.localize.localizedString("var_ordinary_begin", artifact.name));
        this.updateTraining();
        this.onTechUpdate();
    }

    @Override
    public void processSignAction(Player player, StructureSign sign, PlayerInteractEvent event) {
        Resident resident = CivGlobal.getResident(player);
        if (resident == null) {
            return;
        }
        switch (sign.getAction()) {
            case "prev": {
                this.changeIndex(this.index - 1);
                break;
            }
            case "next": {
                this.changeIndex(this.index + 1);
                break;
            }
            case "train": {
                if (!resident.hasTown()) break;
                try {
                    if (!this.getTown().getAssistantGroup().hasMember(resident) && !this.getTown().getMayorGroup().hasMember(resident)) {
                        throw new CivException(CivSettings.localize.localizedString("ordinary_actionNoPerms"));
                    }
                    this.train();
                    break;
                } catch (CivException e) {
                    CivMessage.send((Object) player, CivColor.Red + e.getMessage());
                }
            }
        }
    }

    @Override
    public void onTechUpdate() {
        class OrdinarySyncUpdate implements Runnable {
            StructureSign artifactNameSign;

            public OrdinarySyncUpdate(StructureSign unitNameSign) {
                this.artifactNameSign = unitNameSign;
            }

            @Override
            public void run() {
                this.artifactNameSign.setText(Ordinary.this.getArtifactSignText(Ordinary.this.index));
                this.artifactNameSign.update();
            }
        }
        TaskMaster.syncTask(new OrdinarySyncUpdate(this.artifactNameSign));
    }

    @Override
    public void onPostBuild(BlockCoord absCoord, SimpleBlock sb) {
        switch (sb.command) {
            case "/prev": {
                ItemManager.setTypeId(absCoord.getBlock(), sb.getType());
                ItemManager.setData(absCoord.getBlock(), sb.getData());
                StructureSign structSign = new StructureSign(absCoord, this);
                structSign.setText("\n" + (Object) ChatColor.BOLD + (Object) ChatColor.UNDERLINE + CivSettings.localize.localizedString("ordinary_sign_previousArtifact"));
                structSign.setDirection(sb.getData());
                structSign.setAction("prev");
                structSign.update();
                this.addStructureSign(structSign);
                CivGlobal.addStructureSign(structSign);
                break;
            }
            case "/unitname": {
                ItemManager.setTypeId(absCoord.getBlock(), sb.getType());
                ItemManager.setData(absCoord.getBlock(), sb.getData());
                StructureSign structSign = new StructureSign(absCoord, this);
                structSign.setText(this.getArtifactSignText(0));
                structSign.setDirection(sb.getData());
                structSign.setAction("info");
                structSign.update();
                this.artifactNameSign = structSign;
                this.addStructureSign(structSign);
                CivGlobal.addStructureSign(structSign);
                break;
            }
            case "/next": {
                ItemManager.setTypeId(absCoord.getBlock(), sb.getType());
                ItemManager.setData(absCoord.getBlock(), sb.getData());
                StructureSign structSign = new StructureSign(absCoord, this);
                structSign.setText("\n" + (Object) ChatColor.BOLD + (Object) ChatColor.UNDERLINE + CivSettings.localize.localizedString("ordinary_sign_nextArtifact"));
                structSign.setDirection(sb.getData());
                structSign.setAction("next");
                structSign.update();
                this.addStructureSign(structSign);
                CivGlobal.addStructureSign(structSign);
                break;
            }
            case "/train": {
                ItemManager.setTypeId(absCoord.getBlock(), sb.getType());
                ItemManager.setData(absCoord.getBlock(), sb.getData());
                StructureSign structSign = new StructureSign(absCoord, this);
                structSign.setText("\n" + (Object) ChatColor.BOLD + (Object) ChatColor.UNDERLINE + CivSettings.localize.localizedString("ordinary_sign_do"));
                structSign.setDirection(sb.getData());
                structSign.setAction("train");
                structSign.update();
                this.addStructureSign(structSign);
                CivGlobal.addStructureSign(structSign);
                break;
            }
            case "/progress": {
                ItemManager.setTypeId(absCoord.getBlock(), sb.getType());
                ItemManager.setData(absCoord.getBlock(), sb.getData());
                StructureSign structSign = new StructureSign(absCoord, this);
                structSign.setText("");
                structSign.setDirection(sb.getData());
                structSign.setAction("");
                structSign.update();
                this.addStructureSign(structSign);
                CivGlobal.addStructureSign(structSign);
                this.progresBar.put(Integer.valueOf(sb.keyvalues.get("id")), structSign);
            }
        }
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public ConfigUnit getTrainingArtifact() {
        return this.trainingArtifact;
    }

    public void setTrainingArtifact(ConfigUnit trainingArtifact) {
        this.trainingArtifact = trainingArtifact;
    }

    public void setCurrentHammers(double currentHammers) {
        this.currentHammers = currentHammers;
    }

    public void createArtifact(ConfigUnit artifact) {
        block4:
        {
            ArrayList<StructureChest> chests = this.getAllChestsById(0);
            if (chests.size() == 0) {
                return;
            }
            Chest chest = (Chest) chests.get(0).getCoord().getBlock().getState();
            try {
                Class<?> c = Class.forName("com.avrgaming.civcraft.items.units." + artifact.class_name);
                Method m = c.getMethod("spawn", Inventory.class, Town.class);
                m.invoke(null, chest.getInventory(), this.getTown());
                CivMessage.sendTown(this.getTown(), CivSettings.localize.localizedString("var_ordinary_completedTraining", artifact.name));
                this.trainingArtifact = null;
                this.currentHammers = 0.0;
                CivGlobal.getSessionDB().delete_all(this.getSessionKey());
            } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException e) {
                this.trainingArtifact = null;
                this.currentHammers = 0.0;
                CivMessage.sendTown(this.getTown(), "§4" + CivSettings.localize.localizedString("ordinary_errorUnknown") + e.getMessage());
            } catch (InvocationTargetException e) {
                CivMessage.sendTown(this.getTown(), CivColor.Red + e.getCause().getMessage());
                this.currentHammers -= 20.0;
                if (this.currentHammers >= 0.0) break block4;
                this.currentHammers = 0.0;
            }
        }
    }

    public void updateProgressBar() {
        double percentageDone = this.currentHammers / this.trainingArtifact.hammer_cost;
        int size = this.progresBar.size();
        int textCount = (int) ((double) (size * 16) * percentageDone);
        int textIndex = 0;
        for (int i = 0; i < size; ++i) {
            StructureSign structSign = this.progresBar.get(i);
            String[] text = new String[]{"", "", "", ""};
            for (int j = 0; j < 16; ++j) {
                if (textIndex == 0) {
                    String[] arrstring = text;
                    arrstring[2] = arrstring[2] + "[";
                } else if (textIndex == size * 15 + 3) {
                    String[] arrstring = text;
                    arrstring[2] = arrstring[2] + "]";
                } else if (textIndex < textCount) {
                    String[] arrstring = text;
                    arrstring[2] = arrstring[2] + "=";
                } else {
                    String[] arrstring = text;
                    arrstring[2] = arrstring[2] + "_";
                }
                ++textIndex;
            }
            if (i == size / 2) {
                text[1] = CivColor.Green + this.trainingArtifact.name;
            }
            structSign.setText(text);
            structSign.update();
        }
    }

    public String getSessionKey() {
        return this.getTown().getName() + ":ordinary:" + this.getId();
    }

    public void saveProgress() {
        if (this.getTrainingArtifact() != null) {
            String key = this.getSessionKey();
            String value = this.getTrainingArtifact().id + ":" + this.currentHammers;
            ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(key);
            if (entries.size() > 0) {
                SessionEntry entry = entries.get(0);
                CivGlobal.getSessionDB().update(entry.request_id, key, value);
                for (int i = 1; i < entries.size(); ++i) {
                    SessionEntry bad_entry = entries.get(i);
                    CivGlobal.getSessionDB().delete(bad_entry.request_id, key);
                }
            } else {
                this.sessionAdd(key, value);
            }
            this.lastSave = new Date();
        }
    }

    @Override
    public void onUnload() {
        this.saveProgress();
    }

    @Override
    public void onLoad() {
        String key = this.getSessionKey();
        ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(key);
        if (entries.size() > 0) {
            SessionEntry entry = entries.get(0);
            String[] values = entry.value.split(":");
            this.trainingArtifact = CivSettings.units.get(values[0]);
            if (this.trainingArtifact == null) {
                CivLog.error("Couldn't find in-progress artifact id:" + values[0] + " for town " + this.getTown().getName());
                return;
            }
            this.currentHammers = Double.parseDouble(values[1]);
            for (int i = 1; i < entries.size(); ++i) {
                SessionEntry bad_entry = entries.get(i);
                CivGlobal.getSessionDB().delete(bad_entry.request_id, key);
            }
        }
    }

    public void updateTraining() {
        if (this.trainingArtifact != null) {
            double addedHammers = this.getTown().getHammers().total / 60.0 / 60.0;
            this.currentHammers += addedHammers;
            this.updateProgressBar();
            Date now = new Date();
            if (this.lastSave == null || this.lastSave.getTime() + 60000L < now.getTime()) {
                TaskMaster.asyncTask(new ArtifactSaveAsyncTask(this), 0L);
            }
            if (this.currentHammers >= this.trainingArtifact.hammer_cost) {
                this.currentHammers = this.trainingArtifact.hammer_cost;
                this.createArtifact(this.trainingArtifact);
            }
        }
    }

}

