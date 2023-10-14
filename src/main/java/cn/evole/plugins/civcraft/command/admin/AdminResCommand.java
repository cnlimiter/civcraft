/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.command.admin;

import cn.evole.plugins.civcraft.camp.Camp;
import cn.evole.plugins.civcraft.command.CommandBase;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.AlreadyRegisteredException;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.exception.InvalidNameException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.object.Town;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;

public class AdminResCommand extends CommandBase {

    @Override
    public void init() {
        command = "/ad res";
        displayName = CivSettings.localize.localizedString("adcmd_res_Name");

        commands.put("settown", CivSettings.localize.localizedString("adcmd_res_setTownDesc"));
        commands.put("setcamp", CivSettings.localize.localizedString("adcmd_res_setCampDesc"));
        commands.put("cleartown", CivSettings.localize.localizedString("adcmd_res_clearTownDesc"));
        commands.put("enchant", CivSettings.localize.localizedString("adcmd_res_enchantDesc"));
        commands.put("rename", CivSettings.localize.localizedString("adcmd_res_renameDesc"));
    }

    public void rename_cmd() throws CivException {
        Resident resident = getNamedResident(1);
        String newName = getNamedString(2, CivSettings.localize.localizedString("adcmd_res_renamePrompt"));


        Resident newResident = CivGlobal.getResident(newName);
        if (newResident != null) {
            throw new CivException(CivSettings.localize.localizedString("var_adcmd_res_renameExists", newResident.getName(), resident.getName()));
        }

        /* Create a dummy resident to make sure name is valid. */
        try {
            new Resident(null, newName);
        } catch (InvalidNameException e1) {
            throw new CivException(CivSettings.localize.localizedString("adcmd_res_renameInvalid"));
        }

        /* Delete the old resident object. */
        try {
            resident.delete();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CivException(e.getMessage());
        }

        /* Remove resident from CivGlobal tables. */
        CivGlobal.removeResident(resident);

        /* Change the resident's name. */
        try {
            resident.setName(newName);
        } catch (InvalidNameException e) {
            e.printStackTrace();
            throw new CivException(CivSettings.localize.localizedString("internalCommandException") + " " + e.getMessage());
        }

        /* Resave resident to DB and global tables. */
        CivGlobal.addResident(resident);
        resident.save();

        CivMessage.send(sender, CivSettings.localize.localizedString("adcmd_res_renameSuccess"));
    }

    public void enchant_cmd() throws CivException {
        Player player = getPlayer();
        String enchant = getNamedString(1, CivSettings.localize.localizedString("adcmd_res_enchantHeading"));
        int level = getNamedInteger(2);


        ItemStack stack = player.getInventory().getItemInMainHand();
        Enchantment ench = Enchantment.getByName(enchant);
        if (ench == null) {
            String out = "";
            for (Enchantment ench2 : Enchantment.values()) {
                out += ench2.getName() + ",";
            }
            throw new CivException(CivSettings.localize.localizedString("var_adcmd_res_enchantInvalid1", enchant, out));
        }

        stack.addUnsafeEnchantment(ench, level);
        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("adcmd_res_enchantSuccess"));
    }

    public void cleartown_cmd() throws CivException {
        if (args.length < 2) {
            throw new CivException(CivSettings.localize.localizedString("EnterPlayerName"));
        }

        Resident resident = getNamedResident(1);

        if (resident.hasTown()) {
            resident.getTown().removeResident(resident);
        }

        resident.save();
        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_adcmd_res_cleartownSuccess", resident.getName()));

    }

    public void setcamp_cmd() throws CivException {
        Resident resident = getNamedResident(1);
        Camp camp = getNamedCamp(2);

        if (resident.hasCamp()) {
            resident.getCamp().removeMember(resident);
        }

        camp.addMember(resident);

        camp.save();
        resident.save();
        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_adcmd_res_setcampSuccess", resident.getName(), camp.getName()));
    }


    public void settown_cmd() throws CivException {

        if (args.length < 3) {
            throw new CivException(CivSettings.localize.localizedString("adcmd_res_settownPrompt"));
        }

        Resident resident = getNamedResident(1);

        Town town = getNamedTown(2);

        if (resident.hasTown()) {
            resident.getTown().removeResident(resident);
        }

        try {
            town.addResident(resident);
        } catch (AlreadyRegisteredException e) {
            e.printStackTrace();
            throw new CivException(CivSettings.localize.localizedString("adcmd_res_settownErrorInTown"));
        }

        town.save();
        resident.save();
        CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_adcmd_res_setTownSuccess", resident.getName(), town.getName()));
    }

    @Override
    public void doDefaultAction() throws CivException {
        showHelp();
    }

    @Override
    public void showHelp() {
        showBasicHelp();
    }

    @Override
    public void permissionCheck() throws CivException {

    }

}
