
package com.avrgaming.civcraft.command.civ;

import java.util.Iterator;
import java.util.TreeSet;

import org.bukkit.entity.Player;

import com.avrgaming.civcraft.command.CommandBase;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigLevelTalent;
import com.avrgaming.civcraft.interactive.InteractiveTalentConfirmation;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.object.Buff;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.Talent;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.util.CivColor;

public class CivTalentCommand extends CommandBase {
    protected void addBuffToTown(Town town, String id) {
        try {
            town.getBuffManager().addBuff(id, id, "Talent " + town.getName());
        }
        catch (CivException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init() {
        this.command = "/civ talent";
        this.displayName = CivSettings.localize.localizedString("cmd_civ_talent_name");
        this.commands.put("list", CivSettings.localize.localizedString("cmd_civ_talent_listDesc"));
        this.commands.put("choose", CivSettings.localize.localizedString("cmd_civ_talent_chooseDesc"));
        this.commands.put("next", CivSettings.localize.localizedString("cmd_civ_talent_nextDesc"));
        this.commands.put("info", CivSettings.localize.localizedString("cmd_civ_talent_infoDesc"));
    }

    public void info_cmd() {
        CivMessage.send((Object)this.sender, CivColor.Green + CivSettings.localize.localizedString("cmd_civ_talent_info_link"));
    }

    public void next_cmd() throws CivException {
        Resident sender = this.getResident();
        Civilization civ = this.getSenderCiv();
        Town capitol = civ.getCapitol();
        if (capitol == null) {
            return;
        }
        
        int talentLevel = capitol.highestTalentLevel();
        CivLog.debug("TalentLevel: " + talentLevel);
        int cultureLevel = capitol.getCultureLevel();
        CivLog.debug("cultureLevel: " + cultureLevel);
        if (talentLevel == cultureLevel && talentLevel < 10) {
            CivMessage.sendError(sender, CivSettings.localize.localizedString("cmd_civ_talent_choose_notNow", civ.getCapitol().getName(), civ.getCapitol().getCultureLevel() + 1));
            return;
        }

        if (talentLevel >= 10) {
            CivMessage.send(sender, CivColor.Red + CivSettings.localize.localizedString("cmd_civ_talent_choose_ended"));
            return;
        }
        ConfigLevelTalent configLevelTalent = CivSettings.talentLevels.get(talentLevel+1);
        if (configLevelTalent == null) {
            throw new CivException(CivColor.Red + CivSettings.localize.localizedString("cmd_civ_talent_next_invalid"));
        }
        CivMessage.sendHeading(sender, configLevelTalent.levelName + " (" + configLevelTalent.level + ")");
        CivMessage.send((Object)sender, CivColor.Gold + configLevelTalent.levelBuffDesc1);
        CivMessage.send((Object)sender, CivColor.Green + configLevelTalent.levelBuffDesc2);
        CivMessage.send((Object)sender, CivColor.LightBlue + configLevelTalent.levelBuffDesc3);
    }

    public void list_cmd() throws CivException {
        Town capitol = this.getSenderCiv().getCapitol();
        boolean has = false;
        if (capitol == null) {
            return;
        }
        TreeSet<String> talents = new TreeSet<String>();
        for (Talent talent : capitol.getTalents()) {
            ConfigLevelTalent configLevelTalent = CivSettings.talentLevels.get(talent.level);
            String description = CivSettings.localize.localizedString("cmd_talentcount_broken");
            if (configLevelTalent.levelBuff1.equals(talent.buff)) {
                description = configLevelTalent.levelBuffDesc1;
            } else if (configLevelTalent.levelBuff2.equals(talent.buff)) {
                description = configLevelTalent.levelBuffDesc2;
            } else if (configLevelTalent.levelBuff3.equals(talent.buff)) {
                description = configLevelTalent.levelBuffDesc3;
            }
            talents.add(CivColor.Green + configLevelTalent.level + CivColor.RESET + " (" + CivColor.Gold + configLevelTalent.levelName + CivColor.RESET + ")" + CivColor.LightBlueBold + ": " + CivColor.RESET + description);
            has = true;
        }
        if (!has) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_talent_list_noOne"));
        }
        Civilization civ = this.getSenderCiv();
        CivMessage.sendHeading(this.sender, CivSettings.localize.localizedString("cmd_civ_talent_list_heading", civ.getName()));
        Iterator<String> iter = talents.iterator();
        while (iter.hasNext()) {
            CivMessage.send((Object)this.sender, (String)iter.next());
        }
    }

    public void choose_cmd() throws CivException {
        Resident sender = this.getResident();
        Player player = this.getPlayer();
        Civilization civ = this.getSenderCiv();
        Town capitol = civ.getCapitol();
        if (capitol == null) {
            return;
        }
        int talentLevel = capitol.highestTalentLevel();
        int cultureLevel = capitol.getCultureLevel();
        if (talentLevel == cultureLevel && talentLevel < 10) {
            CivMessage.sendError((Object)player, CivSettings.localize.localizedString("cmd_civ_talent_choose_notNow", civ.getCapitol().getName(), civ.getCapitol().getCultureLevel() + 1));
            return;
        }

        if (talentLevel >= 10) {
            CivMessage.send(sender, CivColor.Red + CivSettings.localize.localizedString("cmd_civ_talent_choose_ended"));
            return;
        }
        ConfigLevelTalent configLevelTalent = CivSettings.talentLevels.get(talentLevel+1);
        if (configLevelTalent == null) {
            CivMessage.sendError(sender, CivColor.Red + CivSettings.localize.localizedString("cmd_civ_talent_next_invalid"));
            return;
        }
        Integer talentChoosen = 0;
        Talent talent = null;
        if (this.args.length < 2) {
            CivMessage.send((Object)sender, CivColor.LightPurple + CivSettings.localize.localizedString("cmd_civ_talent_choose_chooseOne"));
            CivMessage.sendHeading(sender, configLevelTalent.levelName + " (" + configLevelTalent.level + ")");
            CivMessage.send((Object)sender, CivColor.Gold + "1 - " + configLevelTalent.levelBuffDesc1);
            CivMessage.send((Object)sender, CivColor.Green + "2 - " + configLevelTalent.levelBuffDesc2);
            CivMessage.send((Object)sender, CivColor.LightBlue + "3 - " + configLevelTalent.levelBuffDesc3);
        } else {
            try {
                talentChoosen = Integer.parseInt(this.args[1]);
            }
            catch (NumberFormatException ignored) {
                throw new CivException(CivSettings.localize.localizedString("cmd_civ_talent_choose_numberformat", this.args[1]));
            }
        }
        InteractiveTalentConfirmation confirmation = null;
        String message = null;
        if (talentChoosen != 0) {
        	String buff = "";
        	String buffDescription = "";
            switch (talentChoosen) {
                case 1: {
                	buff = configLevelTalent.levelBuff1;
                	buffDescription = configLevelTalent.levelBuffDesc1;
                	break;
                }
                case 2: {
                	buff = configLevelTalent.levelBuff2;
                	buffDescription = configLevelTalent.levelBuffDesc2;
                	break;
                }
                case 3: {
                	buff = configLevelTalent.levelBuff3;
                	buffDescription = configLevelTalent.levelBuffDesc3;
                     break;
                }
                default: {
                    CivMessage.send((Object)sender, CivColor.Red + CivSettings.localize.localizedString("cmd_civ_talent_choose_badInteger", talentChoosen));
                    return;
                }
            }

        	talent = new Talent(configLevelTalent.level, buff);
        	confirmation = new InteractiveTalentConfirmation(civ, this.getPlayer(), talent, CivSettings.localize.localizedString("cmd_civ_talent_choose_sucusses", player.getDisplayName(), talentChoosen, configLevelTalent.levelBuffDesc3, configLevelTalent.level));
            message = CivColor.Green + CivSettings.localize.localizedString("cmd_civ_talent_choose_interactiveConfirmationText",
            		CivColor.GreenBold + configLevelTalent.level + CivColor.Green,
            		CivColor.GoldBold + buffDescription + CivColor.Green,
            		CivColor.LightBlueBold + talentChoosen + CivColor.Green);
            
        }
        if (confirmation != null && message != null) {
            this.getResident().setInteractiveMode(confirmation);
            CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_civ_talent_choose_interactiveConfirmationHeading"));
            CivMessage.send((Object)sender, message);
            CivMessage.send((Object)sender, CivColor.Purple + CivSettings.localize.localizedString("cmd_civ_talent_choose_interactiveConfirmationTypeSth"));
        }
    }

    @Override
    public void doDefaultAction() throws CivException {
        this.showHelp();
    }

    @Override
    public void showHelp() {
        this.showBasicHelp();
    }

    @Override
    public void permissionCheck() throws CivException {
        this.validLeader();
    }
}

