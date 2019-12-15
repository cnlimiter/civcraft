package com.avrgaming.civcraft.structure;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigSpaceCraftMat;
import com.avrgaming.civcraft.config.ConfigSpaceCraftMat2;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.exception.CivTaskAbortException;
import com.avrgaming.civcraft.lorestorage.LoreCraftableMaterial;
import com.avrgaming.civcraft.lorestorage.LoreMaterial;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.StructureChest;
import com.avrgaming.civcraft.object.StructureSign;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.threading.CivAsyncTask;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.threading.sync.ValidateFactoryCraft;
import com.avrgaming.civcraft.threading.sync.request.UpdateInventoryRequest;
import com.avrgaming.civcraft.util.*;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class Factory extends Structure {
    private StructureSign trainSign;
    private int index;

    protected Factory(final Location center, final String id, final Town town) throws CivException {
        super(center, id, town);
        this.index = 0;
    }

    public Factory(final ResultSet rs) throws SQLException, CivException {
        super(rs);
        this.index = 0;
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
    }

    @Override
    public String getDynmapDescription() {
        return "";
    }

    @Override
    public String getMarkerIconName() {
        return "factory";
    }

    public void trainCraftMat(final CivAsyncTask task, final Player player) throws InterruptedException {
        if (this.getChests().size() == 0) {
            return;
        }
        CivMessage.send(player, "§5" + CivSettings.localize.localizedString("var_science_w8"));
        final MultiInventory source = new MultiInventory();
        final MultiInventory target = new MultiInventory();
        final Collection<StructureChest> chests = this.getAllChestsById(0, 6);
        final Collection<StructureChest> targetChest = this.getAllChestsById(7);
        for (final StructureChest c : chests) {
            task.syncLoadChunk(c.getCoord().getWorldname(), c.getCoord().getX(), c.getCoord().getZ());
            Inventory tmp;
            try {
                tmp = task.getChestInventory(c.getCoord().getWorldname(), c.getCoord().getX(), c.getCoord().getY(), c.getCoord().getZ(), false);
            } catch (CivTaskAbortException e) {
                return;
            }
            source.addInventory(tmp);
        }
        for (final StructureChest c : targetChest) {
            task.syncLoadChunk(c.getCoord().getWorldname(), c.getCoord().getX(), c.getCoord().getZ());
            Inventory tmp;
            try {
                tmp = task.getChestInventory(c.getCoord().getWorldname(), c.getCoord().getX(), c.getCoord().getY(), c.getCoord().getZ(), false);
            } catch (CivTaskAbortException e) {
                e.printStackTrace();
                return;
            }
            target.addInventory(tmp);
        }
        final ArrayList<ConfigSpaceCraftMat> crafts = new ArrayList<>(CivSettings.space_crafts.values());
        final ConfigSpaceCraftMat configSpaceCraftMat = crafts.get(this.index);
        final HashMap<String, Integer> craftMatComponents = new HashMap<String, Integer>();
        final String[] split = configSpaceCraftMat.civcraftComponents.split(":");
        if (!configSpaceCraftMat.civcraftComponents.equals("")) {
            for (final String component : split) {
                final int count = Integer.parseInt(component.replaceAll("[^\\d]", ""));
                final String craftMatID = component.replace(String.valueOf(count), "");
                craftMatComponents.put(craftMatID, count);
            }
        }
        boolean allMatchCraftMat = true;
        boolean allMatchMinecraft = false;
        boolean allAllMatch = false;
        try {
            if (configSpaceCraftMat.minecraftComponents == null) {
                allMatchMinecraft = true;
            }
        } catch (Exception ignored) {
            allMatchMinecraft = true;
        }
        final HashMap<String, Integer> multiInvContentsCraftMat = new HashMap<String, Integer>();
        final HashMap<Integer, Integer> multiInvContentsMinecraft = new HashMap<Integer, Integer>();
        if (!configSpaceCraftMat.civcraftComponents.equals("")) {
            for (final ItemStack itemStack : source.getContents()) {
                if (itemStack != null) {
                    final LoreMaterial loreMaterial = LoreMaterial.getMaterial(itemStack);
                    if (loreMaterial != null) {
                        if (multiInvContentsCraftMat.get(loreMaterial.getId()) == null) {
                            multiInvContentsCraftMat.put(loreMaterial.getId(), itemStack.getAmount());
                        } else {
                            final int oldAmount = multiInvContentsCraftMat.get(loreMaterial.getId());
                            multiInvContentsCraftMat.put(loreMaterial.getId(), itemStack.getAmount() + oldAmount);
                        }
                    }
                }
            }
        }
        if (!allMatchMinecraft) {
            for (final ItemStack itemStack : source.getContents()) {
                if (itemStack != null) {
                    final LoreMaterial loreMaterial = LoreMaterial.getMaterial(itemStack);
                    if (loreMaterial == null) {
                        final Integer id = ItemManager.getId(itemStack);
                        if (multiInvContentsMinecraft.get(id) == null) {
                            multiInvContentsMinecraft.put(id, itemStack.getAmount());
                        } else {
                            final int oldAmount2 = multiInvContentsMinecraft.get(id);
                            multiInvContentsMinecraft.put(id, itemStack.getAmount() + oldAmount2);
                        }
                    }
                }
            }
        }
        final LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterialFromId(configSpaceCraftMat.originalCraftMat);
            final StringBuilder notMatchComponents = new StringBuilder(CivColor.Red
                + CivSettings.localize.localizedString("var_structure_factory_create_failed", craftMat.getName())
                + CivColor.Gold);
        final String crutches = CivColor.Red + CivSettings.localize.localizedString("var_structure_factory_create_failed_crutches", craftMat.getName()) + CivColor.Gold;
        if (!configSpaceCraftMat.civcraftComponents.equals("")) {
            for (final String component2 : split) {
                final int count2 = Integer.parseInt(component2.replaceAll("[^\\d]", ""));
                final String craftMatID2 = component2.replace(String.valueOf(count2), "");
                final LoreCraftableMaterial itemToGetName = LoreCraftableMaterial.getCraftMaterialFromId(craftMatID2);
                if (multiInvContentsCraftMat.get(craftMatID2) == null) {
                    allMatchCraftMat = false;
                    notMatchComponents.append(itemToGetName.getName()).append(" ").append(count2).append(" ").append(CivSettings.localize.localizedString("structure_factory_pieces")).append("\n");
                } else if (multiInvContentsCraftMat.get(craftMatID2) < count2) {
                    allMatchCraftMat = false;
                    final int reaming = count2 - multiInvContentsCraftMat.get(craftMatID2);
                    notMatchComponents.append(itemToGetName.getName()).append(" ").append(reaming).append(" ").append(CivSettings.localize.localizedString("structure_factory_pieces")).append("\n");
                }
            }
        }
        if (!allMatchMinecraft && configSpaceCraftMat.minecraftComponents.values() != null) {
            for (final ConfigSpaceCraftMat2 configSpaceCraftMat2 : configSpaceCraftMat.minecraftComponents.values()) {
                final int count3 = configSpaceCraftMat2.count;
                final Integer id2 = configSpaceCraftMat2.typeID;
                if (multiInvContentsMinecraft.get(id2) == null) {
                    allMatchCraftMat = false;
                    final boolean succusess = true;
                    if (succusess) {
                        notMatchComponents.append("§a");
                    }
                    notMatchComponents.append(configSpaceCraftMat2.name).append(" ").append(count3).append(" ").append(CivSettings.localize.localizedString("structure_factory_pieces")).append("\n");
                } else {
                    if (multiInvContentsMinecraft.get(id2) >= count3) {
                        continue;
                    }
                    allMatchCraftMat = false;
                    final boolean succusess = true;
                    if (succusess) {
                        notMatchComponents.append("§a");
                    }
                    final int reaming2 = count3 - multiInvContentsMinecraft.get(id2);
                    notMatchComponents.append(configSpaceCraftMat2.name).append(" ").append(reaming2).append(" ").append(CivSettings.localize.localizedString("structure_factory_pieces")).append("\n");
                }
            }
        }
        if (allMatchCraftMat && allMatchMinecraft) {
            allAllMatch = true;
        }
        if (crutches.equalsIgnoreCase(notMatchComponents.toString())) {
            allAllMatch = true;
        }
        if (allAllMatch) {
            for (final ItemStack itemStack2 : source.getContents()) {
                if (itemStack2 != null) {
                    task.updateInventory(UpdateInventoryRequest.Action.REMOVE, source, itemStack2);
                }
            }
            final String fullName = player.getDisplayName();
            CivMessage.sendCiv(super.getCiv(), "§a" + CivSettings.localize.localizedString("var_factory_succusess", craftMat.getName(), fullName));

            final ItemStack stack = LoreMaterial.spawn(craftMat);
            task.updateInventory(UpdateInventoryRequest.Action.ADD, target, stack);
            return;
        }
        CivMessage.send(player, notMatchComponents.toString());
    }

    public boolean lastKeySet(final int current, final HashMap<String, Integer> array) {
        int lenght = 0;
        for (final String string : array.keySet()) {
            if (string != null && string.equalsIgnoreCase("")) {
                ++lenght;
            }
        }
        return current == lenght;
    }

    private void changeIndex(final int newIndex) {
        final ArrayList<ConfigSpaceCraftMat> crafts = new ArrayList<>(CivSettings.space_crafts.values());
        if (this.trainSign != null) {
            try {
                final LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterialFromId(crafts.get(newIndex).originalCraftMat);
                this.trainSign.setText(CivSettings.localize.localizedString("structure_factory_sign_construct") +
                        "\n" + CivColor.GreenBold + craftMat.getName());
                this.index = newIndex;
            } catch (IndexOutOfBoundsException e) {
                if (crafts.size() > 0) {
                    final LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterialFromId(crafts.get(0).originalCraftMat);
                    this.trainSign.setText(CivSettings.localize.localizedString("structure_factory_sign_construct") + "\n" + CivColor.GreenBold + craftMat.getName());
                    this.index = 0;
                }
            }
            this.trainSign.update();
        } else {
            CivLog.warning("Cannot find Construct sign for " + this.getId() + " in " + this.getCorner());
        }
    }

    @Override
    public void processSignAction(final Player player, final StructureSign sign, final PlayerInteractEvent event) {
        final Resident resident = CivGlobal.getResident(player);
        boolean access = true;
        if (resident == null) {
            access = false;
        }
        if (!sign.getOwner().getCiv().getLeaderGroup().hasMember(resident)) {
            access = false;
        }
        if (access) {
            final String action = sign.getAction();
            switch (action) {
                case "prev": {
                    this.changeIndex(this.index - 1);
                    break;
                }
                case "next": {
                    this.changeIndex(this.index + 1);
                    break;
                }
                case "craft": {
                    TaskMaster.asyncTask("ValidateFactoryCraft", new ValidateFactoryCraft(sign.getOwner().getTown(), player), 0L);
                    break;
                }
            }
        } else {
            CivMessage.sendError(player, CivSettings.localize.localizedString("var_factory_noPerm", sign.getOwner().getCiv().getName()));
        }
    }

    @Override
    public void onPostBuild(final BlockCoord absCoord, final SimpleBlock commandBlock) {
        final String command = commandBlock.command;
        switch (command) {
            case "/prev": {
                ItemManager.setTypeId(absCoord.getBlock(), commandBlock.getType());
                ItemManager.setData(absCoord.getBlock(), commandBlock.getData());
                final StructureSign structSign = new StructureSign(absCoord, this);
                structSign.setText(CivColor.UNDERLINE + CivSettings.localize.localizedString("factory_sign_previousUnit"));
                structSign.setDirection(commandBlock.getData());
                structSign.setAction("prev");
                structSign.update();
                this.addStructureSign(structSign);
                CivGlobal.addStructureSign(structSign);
                break;
            }
            case "/next": {
                ItemManager.setTypeId(absCoord.getBlock(), commandBlock.getType());
                ItemManager.setData(absCoord.getBlock(), commandBlock.getData());
                final StructureSign structSign = new StructureSign(absCoord, this);
                structSign.setText(CivColor.UNDERLINE + CivSettings.localize.localizedString("factory_sign_nextUnit"));
                structSign.setDirection(commandBlock.getData());
                structSign.setAction("next");
                structSign.update();
                this.addStructureSign(structSign);
                CivGlobal.addStructureSign(structSign);
                break;
            }
            case "/craft": {
                final ArrayList<ConfigSpaceCraftMat> crafts = new ArrayList<>();
                for (final ConfigSpaceCraftMat configSpaceCraftMat : CivSettings.space_crafts.values()) {
                    crafts.add(configSpaceCraftMat);
                }
                ItemManager.setTypeId(absCoord.getBlock(), commandBlock.getType());
                ItemManager.setData(absCoord.getBlock(), commandBlock.getData());
                final StructureSign structSign = new StructureSign(absCoord, this);
                structSign.setText(CivColor.UNDERLINE + CivSettings.localize.localizedString("structure_factory_sign_construct"));
                structSign.setDirection(commandBlock.getData());
                structSign.setAction("craft");
                structSign.update();
                this.addStructureSign(structSign);
                CivGlobal.addStructureSign(structSign);
                this.trainSign = structSign;
                break;
            }
        }
    }
}
