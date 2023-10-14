/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.object;

import cn.evole.plugins.civcraft.main.CivGlobal;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

public class DiplomacyManager {

    /*
     * Manages diplomatic relationships for the object it is attached to.
     * Diplomatic relationships are stored in the SessionDB with this civ as the key.
     * There will be duplicate data that needs to be cleaned up for mutual relationships.
     * For example, if Civ A is at war with Civ B, both Civ A and Civ B will have a war relationship
     * entry.
     */

    private Civilization ourCiv;

    /*
     * List of our relationships, hashed by civ id.
     */
    private HashMap<Integer, Relation> relations = new HashMap<Integer, Relation>();

    /* Number of civ's at war with us, will maintain this for fast isWar() lookups */
    private int warCount = 0;

    public DiplomacyManager(Civilization civ) {
        ourCiv = civ;
    }


    public boolean atWarWith(Civilization other) {
        if (ourCiv.getId() == other.getId()) {
            return false;
        }

        Relation relation = relations.get(other.getId());
        if (relation != null && relation.getStatus() == Relation.Status.WAR) {
            return true;
        }
        return false;
    }

    public boolean isAtWar() {
        return (warCount != 0);
    }

    public void deleteRelation(Relation relation) {

        if (relation.getStatus() == Relation.Status.WAR &&
                relations.containsKey(relation.getOtherCiv().getId())) {
            warCount--;
            if (warCount < 0) {
                warCount = 0;
            }
        }
        relations.remove(relation.getOtherCiv().getId());

        Relation theirRelation = relation.getOtherCiv().getDiplomacyManager().getRelation(ourCiv);
        if (theirRelation != null) {
            try {
                relation.getOtherCiv().getDiplomacyManager().relations.remove(theirRelation.getOtherCiv().getId());
                theirRelation.delete();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        try {
            relation.delete();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteAllRelations() {
        LinkedList<Relation> removeUs = new LinkedList<Relation>();

        for (Relation relation : relations.values()) {
            removeUs.add(relation);
        }

        for (Relation relation : removeUs) {
            this.deleteRelation(relation);
        }

        this.relations.clear();
    }

    public void setAggressor(Civilization aggressor, Civilization otherCiv) {
        Relation relation = relations.get(otherCiv.getId());
        if (relation != null) {
            relation.setAggressor(aggressor);
            relation.save();
        }
    }

    public void setRelation(Civilization otherCiv, Relation.Status status, Date expires) {
        Relation relation = relations.get(otherCiv.getId());

        if (relation == null) {
            relations.put(otherCiv.getId(), new Relation(ourCiv, otherCiv, status, expires));
        } else {
            if (relation.getStatus() == status) {
                return;
            }

            if (relation.getStatus() == Relation.Status.WAR) {
                //Status was war, new status is not the same, so reduce our warcount.
                warCount--;
            }

            if (expires != null) {
                relation.setExpires(expires);
            }
            relation.setStatus(status);
//			if (status == Status.VASSAL) {
//				//End all wars with this civilization.
//				for (Relation rel : this.getRelations()) {
//					if (rel.getOtherCiv() != otherCiv) {
//						if (rel.getStatus() == Status.WAR) {
//							CivGlobal.setRelation(ourCiv, rel.getOtherCiv(), Status.NEUTRAL);
//							CivMessage.sendCiv(this.ourCiv, 
//									"Our war with "+rel.getOtherCiv().getName()+" has ended because we are now a vassal to "+otherCiv.getName());
//							CivMessage.sendCiv(rel.getOtherCiv(), 
//									"Our war with "+ourCiv.getName()+" has ended because they are now a vassal to "+otherCiv.getName());
//						}
//					}
//				}
//			}
        }

        if (status == Relation.Status.WAR) {
            warCount++;
        }
    }

    public Relation.Status getRelationStatus(Civilization otherCiv) {
        if (otherCiv.getId() == ourCiv.getId()) {
            return Relation.Status.ALLY;
        }

        Relation relation = relations.get(otherCiv.getId());
        if (relation == null) {
            return Relation.Status.NEUTRAL;
        }
        return relation.getStatus();
    }

    public Relation getRelation(Civilization otherCiv) {
        return relations.get(otherCiv.getId());
    }

    public void addRelation(Relation relation) {
        Relation currentRelation = relations.get(relation.getOtherCiv().getId());

        if (relation.getStatus() == Relation.Status.WAR) {
            if (currentRelation == null || currentRelation.getStatus() != Relation.Status.WAR) {
                warCount++;
            }
        }
        relations.put(relation.getOtherCiv().getId(), relation);
    }

    public Collection<Relation> getRelations() {
        return relations.values();
    }

    public int getWarCount() {
        return warCount;
    }

    public boolean atWarWith(Player attacker) {
        Resident resident = CivGlobal.getResident(attacker);
        if (resident == null) {
            return false;
        }
        if (!resident.hasTown()) {
            return false;
        }

        return atWarWith(resident.getTown().getCiv());
    }

    public Relation.Status getRelationStatus(Player player) {
        Resident resident = CivGlobal.getResident(player);
        if (resident == null) {
            return Relation.Status.NEUTRAL;
        }
        if (!resident.hasTown()) {
            return Relation.Status.NEUTRAL;
        }

        return getRelationStatus(resident.getTown().getCiv());
    }

//	public Civilization getMasterCiv() {
//		for (Relation rel : this.relations.values()) {
//			if (rel.getStatus() == Status.VASSAL) {
//				return rel.getOtherCiv();
//			}
//		}
//		return null;
//	}


    public boolean isHostileWith(Resident resident) {
        return isHostileWith(resident.getCiv());
    }

    public boolean isHostileWith(Civilization civ) {
        Relation relation = this.relations.get(civ.getId());
        if (relation == null) {
            return false;
        }
        switch (relation.getStatus()) {
            case WAR:
            case HOSTILE:
                return true;
            default:
                return false;
        }
    }
}
