/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.items.units;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.config.ConfigUnit;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.lorestorage.LoreMaterial;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.object.MissionLogger;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.util.BookUtil;
import gpl.AttributeUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.HashMap;

public class Spy extends UnitMaterial {

    public static final int BOOK_ID = 403;

    public ArrayList<UnitItemMaterial> missionBooks = new ArrayList<UnitItemMaterial>();

    public Spy(String id, ConfigUnit configUnit) {
        super(id, configUnit);
    }

    public static void spawn(Inventory inv, Town town) throws CivException {
        ItemStack is = LoreMaterial.spawn(Unit.SPY_UNIT);

        setOwningTown(town, is);
        if (!Unit.addItemNoStack(inv, is)) {
            throw new CivException(CivSettings.localize.localizedString("var_settler_errorBarracksFull", Unit.SPY_UNIT.getUnit().name));
        }
    }

    public static void spawn(Inventory inv) throws CivException {
        ItemStack is = spawn(Unit.SPY_UNIT);
        if (!Unit.addItemNoStack(inv, is)) {
            throw new CivException(CivSettings.localize.localizedString("var_settler_errorBarracksFull", Unit.SPY_UNIT.getUnit().name));
        }
    }

    public void addMissionBook(UnitItemMaterial umat) {
        this.missionBooks.add(umat);
        this.allowedSubslots.add(umat.getSocketSlot());
    }

    public void giveMissionBooks(Player player) {

        Inventory inv = player.getInventory();

        for (UnitItemMaterial book : missionBooks) {


            ItemStack stack = inv.getItem(book.getSocketSlot());

            ItemStack is = spawn(book);
            AttributeUtil attrs = new AttributeUtil(is);
            attrs.setLore(book.getLore());
            is = attrs.getStack();

            if (!player.getInventory().contains(is)) {
                inv.setItem(book.getSocketSlot(), is);

                // Try to re-add any removed items.
                if (stack != null) {
                    HashMap<Integer, ItemStack> leftovers = inv.addItem(stack);

                    for (ItemStack s : leftovers.values()) {
                        player.getWorld().dropItem(player.getLocation(), s);
                    }
                }
            }
        }

    }

    @Override
    public void onItemToPlayer(Player player, ItemStack stack) {
        //Add Books to inventory.
        giveMissionBooks(player);
    }


    @Override
    public void onItemFromPlayer(Player player, ItemStack stack) {
        removeChildren(player.getInventory());
    }

    @Override
    public void onPlayerDeath(EntityDeathEvent event, ItemStack stack) {

        Player player = (Player) event.getEntity();
        Resident resident = CivGlobal.getResident(player);
        if (resident == null || !resident.hasTown()) {
            return;
        }


        ArrayList<String> bookout = MissionLogger.getMissionLogs(resident.getTown());

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
        BookMeta meta = (BookMeta) book.getItemMeta();
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("Mission Report");
        meta.setAuthor("Mission Reports");
        meta.setTitle("Missions From" + " " + resident.getTown().getName());

        String out = "";
        for (String str : bookout) {
            out += str + "\n";
        }
        BookUtil.paginate(meta, out);


        meta.setLore(lore);
        book.setItemMeta(meta);
        player.getWorld().dropItem(player.getLocation(), book);

    }


}
