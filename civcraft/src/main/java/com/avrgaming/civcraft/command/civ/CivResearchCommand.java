/*************************************************************************
 * 
 * AVRGAMING LLC
 * __________________
 * 
 *  [2013] AVRGAMING LLC
 *  All Rights Reserved.
 * 
 * NOTICE:  All information contained herein is, and remains
 * the property of AVRGAMING LLC and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to AVRGAMING LLC
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from AVRGAMING LLC.
 */
package com.avrgaming.civcraft.command.civ;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import com.avrgaming.civcraft.command.CommandBase;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigTech;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.structure.TownHall;
import com.avrgaming.civcraft.util.CivColor;

public class CivResearchCommand extends CommandBase {

	@Override
	public void init() {
		command = "/civ research";
		displayName = CivSettings.localize.localizedString("cmd_civ_research_name");
		
		commands.put("list", CivSettings.localize.localizedString("cmd_civ_research_listDesc"));
		commands.put("progress", CivSettings.localize.localizedString("cmd_civ_research_progressDesc"));
		commands.put("on", CivSettings.localize.localizedString("cmd_civ_research_onDesc"));
		commands.put("change", CivSettings.localize.localizedString("cmd_civ_research_changeDesc"));
		commands.put("finished", CivSettings.localize.localizedString("cmd_civ_research_finishedDesc"));
		commands.put("era", CivSettings.localize.localizedString("cmd_civ_research_eraDesc"));
        commands.put("calc", CivSettings.localize.localizedString("cmd_civ_researchcalc_Desc"));
        commands.put("queuelist", CivSettings.localize.localizedString("cmd_civ_research_queueList"));
        commands.put("queueadd", CivSettings.localize.localizedString("cmd_civ_research_queueAdd"));
        commands.put("queueremove", CivSettings.localize.localizedString("cmd_civ_research_queueRemove"));
	}
	
	public void queueadd_cmd() throws CivException {
        Civilization civ = this.getSenderCiv();
        Town capitol = CivGlobal.getTown(civ.getCapitolName());
        TownHall townhall = capitol.getTownHall();
        if (this.args.length < 2) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_research_queueEnterName"));
        }
        if (townhall == null) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_research_queueTownHallNULL"));
        }
        if (!townhall.isActive()) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_research_queueNotCompletedTownHall"));
        }
        String techname = this.combineArgs(this.stripArgs(this.args, 1));
        ConfigTech tech = CivSettings.getTechByName(techname);
        if (civ.getResearchTech() == tech) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_research_queueArleadyThis"));
        }
        if (civ.getResearchTech() == null) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_research_queueNoResearchingNow", tech.name));
        }
        if (tech == null) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_research_queueUnknownTech", techname));
        }
        if (civ.getTechQueued() != null) {
            if (civ.getTechQueued() == tech) {
                throw new CivException(CivSettings.localize.localizedString("cmd_civ_research_queueArleayIn"));
            }
            if (civ.getResearchTech() == null) {
                throw new CivException(CivSettings.localize.localizedString("cmd_civ_research_queueNoResearchingNow", tech.name));
            }
            ConfigTech oldQueue = civ.getTechQueued();
            civ.setTechQueued(tech);
            CivMessage.sendCiv(civ, CivSettings.localize.localizedString("cmd_civ_research_queueSucussesAdded", tech.name));
            CivMessage.send((Object)this.sender, CivColor.YellowBold + CivSettings.localize.localizedString("cmd_civ_research_queueSucussesWithWarning", oldQueue.name, tech.name));
            civ.save();
        } else {
            civ.setTechQueued(tech);
            CivMessage.sendCiv(civ, CivSettings.localize.localizedString("cmd_civ_research_queueSucussesAdded", tech.name));
            civ.save();
        }
    }

    public void queueremove_cmd() throws CivException {
        Civilization civ = this.getSenderCiv();
        if (civ.getTechQueued() == null) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_research_queueErrorListRemove"));
        }
        ConfigTech oldQueue = civ.getTechQueued();
        civ.setTechQueued(null);
        CivMessage.sendCiv(civ, CivSettings.localize.localizedString("cmd_civ_research_queueRemoveSucusses", oldQueue.name));
        civ.save();
    }

    public void queuelist_cmd() throws CivException {
        Civilization civ = this.getSenderCiv();
        if (civ.getTechQueued() == null) {
            throw new CivException(CivSettings.localize.localizedString("cmd_civ_research_queueErrorListRemove"));
        }
        CivMessage.sendCiv(civ, CivSettings.localize.localizedString("cmd_civ_research_queueListSucusses") + "§d" + civ.getTechQueued().name);
    }

    public void calc_cmd() throws CivException {
		SimpleDateFormat sdf = new SimpleDateFormat("M/dd h:mm:ss a z");
        
        Resident resident = this.getResident();
        Civilization civ = this.getSenderCiv();
        if (resident == null) {
            throw new CivException(CivSettings.localize.localizedString("resident_null"));
        }
        if (civ.getResearchTech() == null) {
            throw new CivException(CivSettings.localize.localizedString("no_research"));
        }
        double mins = (civ.getResearchTech().getAdjustedBeakerCost(civ) - civ.getResearchProgress()) / civ.getBeakers() * 60.0;
        long timeNow = Calendar.getInstance().getTimeInMillis();
        double seconds = mins * 60.0;
        long endResearch = (long)((double)timeNow + 1000.0 * seconds);
        CivMessage.sendCiv(civ, CivSettings.localize.localizedString("cmd_civ_research_calc_result", civ.getResearchTech().name, sdf.format(endResearch)));
    }
	
	public void change_cmd() throws CivException {
		Civilization civ = getSenderCiv();
		
		if (args.length < 2) {
			list_cmd();
			throw new CivException(CivSettings.localize.localizedString("cmd_civ_research_changePrompt"));
		}
		
		String techname = combineArgs(stripArgs(args, 1));
		ConfigTech tech = CivSettings.getTechByName(techname);
		if (tech == null) {
			throw new CivException(CivSettings.localize.localizedString("var_cmd_civ_research_NotFound",techname));
		}
		
		if (!civ.getTreasury().hasEnough(tech.getAdjustedTechCost(civ))) {
			throw new CivException(CivSettings.localize.localizedString("var_cmd_civ_research_NotEnough1",CivSettings.CURRENCY_NAME,tech.name));
		}
		
		if(!tech.isAvailable(civ)) {
			throw new CivException(CivSettings.localize.localizedString("cmd_civ_research_NotAllowedNow"));
		}
		
		if (civ.getResearchTech() != null) {
			civ.setResearchProgress(0);
			CivMessage.send(sender, CivColor.Rose+CivSettings.localize.localizedString("var_cmd_civ_research_lostProgress1",civ.getResearchTech().name));
			civ.setResearchTech(null);
		}
	
		civ.startTechnologyResearch(tech);
		CivMessage.sendCiv(civ, CivSettings.localize.localizedString("var_cmd_civ_research_start",tech.name));
	}
	
	public void finished_cmd() throws CivException {
		Civilization civ = getSenderCiv();
		
		CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_civ_research_finishedHeading"));
		String out = "";
		for (ConfigTech tech : civ.getTechs()) {
			out += tech.name+", ";
		}
		CivMessage.send(sender, out);
	}

	public void on_cmd() throws CivException {
		Civilization civ = getSenderCiv();
		
		if (args.length < 2) {
			throw new CivException(CivSettings.localize.localizedString("cmd_civ_research_onPrompt"));
		}
		
		Town capitol = CivGlobal.getTown(civ.getCapitolName());
		if (capitol == null) {
			throw new CivException(CivSettings.localize.localizedString("var_cmd_civ_research_missingCapitol",civ.getCapitolName())+" "+CivSettings.localize.localizedString("internalCommandException"));
		}
	
		TownHall townhall = capitol.getTownHall();
		if (townhall == null) {
			throw new CivException(CivSettings.localize.localizedString("cmd_civ_research_missingTownHall"));
		}
		
		if (!townhall.isActive()) {
			throw new CivException(CivSettings.localize.localizedString("cmd_civ_research_incompleteTownHall"));
		}
		
		String techname = combineArgs(stripArgs(args, 1));
		ConfigTech tech = CivSettings.getTechByName(techname);
		if (tech == null) {
			throw new CivException(CivSettings.localize.localizedString("var_cmd_civ_research_NotFound",techname));
		}
		
		civ.startTechnologyResearch(tech);
		CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_cmd_civ_research_start",tech.name));
	}
	
	public void progress_cmd() throws CivException {
		Civilization civ = getSenderCiv();
		
		CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_civ_research_current"));
		
		if (civ.getResearchTech() != null) {
			int percentageComplete = (int)((civ.getResearchProgress() / civ.getResearchTech().getAdjustedBeakerCost(civ))*100);
			CivMessage.send(sender, CivSettings.localize.localizedString("var_cmd_civ_research_current",civ.getResearchTech().name,percentageComplete,(civ.getResearchProgress()+" / "+civ.getResearchTech().getAdjustedBeakerCost(civ))));
		} else {
			CivMessage.send(sender, CivSettings.localize.localizedString("cmd_civ_research_NotAnything"));
		}
		
	}
	
	public void list_cmd() throws CivException {
		Civilization civ = getSenderCiv();
		ArrayList<ConfigTech> techs = ConfigTech.getAvailableTechs(civ);
		
		CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_civ_research_Available"));
		for (ConfigTech tech : techs) {
			CivMessage.send(sender, tech.name+CivColor.LightGray+" "+CivSettings.localize.localizedString("Cost")+" "+
					CivColor.Yellow+tech.getAdjustedTechCost(civ)+CivColor.LightGray+" "+CivSettings.localize.localizedString("Beakers")+" "+
					CivColor.Yellow+tech.getAdjustedBeakerCost(civ));
		}
				
	}
	
	public void era_cmd() throws CivException {
		Civilization civ = getSenderCiv();
		
		CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_civ_research_era"));
		CivMessage.send(sender, CivColor.White+CivSettings.localize.localizedString("var_cmd_civ_research_currentEra", CivColor.LightBlue+CivGlobal.localizedEraString(civ.getCurrentEra())));
		CivMessage.send(sender, CivColor.White+CivSettings.localize.localizedString("var_cmd_civ_research_highestEra", CivColor.LightBlue+CivGlobal.localizedEraString(CivGlobal.highestCivEra)));
		
		double eraRate = ConfigTech.eraRate(civ);
		if (eraRate == 0.0) {
			CivMessage.send(sender, CivColor.Yellow+CivSettings.localize.localizedString("cmd_civ_research_eraNoDiscount"));
		} else {
			CivMessage.send(sender, CivColor.Green+CivSettings.localize.localizedString("var_cmd_civ_research_eraDiscount",(eraRate*100),CivSettings.CURRENCY_NAME));
			
		}
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
		Resident resident = getResident();
		Civilization civ = getSenderCiv();
		
		if (!civ.getLeaderGroup().hasMember(resident) && !civ.getAdviserGroup().hasMember(resident)) {
			throw new CivException(CivSettings.localize.localizedString("cmd_civ_research_notLeader"));
		}		
	}

}
