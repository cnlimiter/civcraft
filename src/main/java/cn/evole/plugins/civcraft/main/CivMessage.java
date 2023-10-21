/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.main;

import cn.evole.plugins.civcraft.camp.Camp;
import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.object.Civilization;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.util.CivColor;
import cn.evole.plugins.civcraft.util.Reflection;
import com.connorlinfoot.titleapi.TitleAPI;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CivMessage {

    /* Stores the player name and the hash code of the last message sent to prevent error spamming the player. */
    private static HashMap<String, Integer> lastMessageHashCode = new HashMap<String, Integer>();

    /* Indexed off of town names, contains a list of extra people who listen to town chats.(mostly for admins to listen to towns) */
    private static Map<String, ArrayList<String>> extraTownChatListeners = new ConcurrentHashMap<String, ArrayList<String>>();

    /* Indexed off of civ names, contains a list of extra people who listen to civ chats. (mostly for admins to list to civs) */
    private static Map<String, ArrayList<String>> extraCivChatListeners = new ConcurrentHashMap<String, ArrayList<String>>();

    public static void sendErrorNoRepeat(Object sender, String line) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            Integer hashcode = lastMessageHashCode.get(player.getName());
            if (hashcode != null && hashcode == line.hashCode()) {
                return;
            }

            lastMessageHashCode.put(player.getName(), line.hashCode());
        }

        send(sender, CivColor.Rose + line);
    }

    public static void sendError(Object sender, String line) {
        send(sender, CivColor.Rose + line);
    }

    /*
     * Sends message to playerName(if online) AND console.
     */
    public static void console(String playerName, String line) {
        try {
            Player player = CivGlobal.getPlayer(playerName);
            send(player, line);
        } catch (CivException e) {
        }
        CivLog.info(line);
    }

    public static void sendTitle(Object sender, int fadeIn, int show, int fadeOut, String title, String subTitle) {
        if (CivSettings.hasTitleAPI) {
            Player player = null;
            Resident resident = null;
            if ((sender instanceof Player)) {
                player = (Player) sender;
                resident = CivGlobal.getResident(player);
            } else if (sender instanceof Resident) {
                try {
                    resident = (Resident) sender;
                    player = CivGlobal.getPlayer(resident);
                } catch (CivException e) {
                    // No player online
                }
            }
            if (player != null && resident != null && resident.isTitleAPI()) {
                TitleAPI.sendTitle(player, fadeIn, show, fadeOut, title, subTitle);
            }
        }
        send(sender, title);
        if (subTitle != "") {
            send(sender, subTitle);
        }
    }


    public static void sendTitle(Object sender, String title, String subTitle) {
        sendTitle(sender, 10, 40, 5, title, subTitle);
    }

    public static void send(Object sender, String line) {
        List<String> list = new ArrayList<>();
        if (line.length() > 128) {
            for (int i = 0; i < line.length() / 128 + 1; i++) {
                int begin = i * 128;
                int end = (i + 1) * 128;
                if (begin < line.length()) {
                    if (end > line.length()) {
                        end = line.length();
                    }
                    list.add(line.substring(begin, end));
                }
            }
        } else {
            list.add(line);
        }
        send(sender, list);
    }

    public static void send(Object sender, List<String> list) {
        for (String line : list) {
            if ((sender instanceof Player)) {
                ((Player) sender).sendMessage(line);
            } else if (sender instanceof CommandSender) {
                ((CommandSender) sender).sendMessage(line);
            } else if (sender instanceof Resident) {
                try {
                    CivGlobal.getPlayer(((Resident) sender)).sendMessage(line);
                } catch (CivException e) {
                    // No player online
                }
            }
        }
    }

    public static String itemTooltip(ItemStack itemStack) {
        try {
            Object nmsItem = Reflection.getMethod(Reflection.getOBCClass("inventory.CraftItemStack"), "asNMSCopy", new Class[]{ItemStack.class}).invoke(null, itemStack);
            return (Reflection.getMethod(Reflection.getNMSClass("ItemStack"), "save", new Class[]{Reflection.getNMSClass("NBTTagCompound")}).invoke(nmsItem, Reflection.getNMSClass("NBTTagCompound").newInstance()).toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void send(Object sender, String line, ItemStack item) {
        if ((sender instanceof Player)) {
            Player p = (Player) sender;
            TextComponent msg = new TextComponent(line);
            msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new ComponentBuilder(itemTooltip(item)).create()));

            p.spigot().sendMessage(msg);

        } else if (sender instanceof CommandSender) {

            ((CommandSender) sender).sendMessage(line);
        } else if (sender instanceof Resident) {
            try {
                Player p = CivGlobal.getPlayer(((Resident) sender));
                TextComponent msg = new TextComponent(line);
                msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new ComponentBuilder(itemTooltip(item)).create()));

                p.spigot().sendMessage(msg);
            } catch (CivException e) {
                // No player online
            }
        }
    }

    public static void send(Object sender, String[] lines) {
        boolean isPlayer = false;
        if (sender instanceof Player)
            isPlayer = true;

        for (String line : lines) {
            if (isPlayer) {
                ((Player) sender).sendMessage(line);
            } else {
                ((CommandSender) sender).sendMessage(line);
            }
        }
    }

    public static String buildTitle(String title) {
        String line = "-------------------------------------------------";
        String titleBracket = "[ " + CivColor.Yellow + title + CivColor.LightBlue + " ]";

        if (titleBracket.length() > line.length()) {
            return CivColor.LightBlue + "-" + titleBracket + "-";
        }

        int min = (line.length() / 2) - titleBracket.length() / 2;
        int max = (line.length() / 2) + titleBracket.length() / 2;

        String out = CivColor.LightBlue + line.substring(0, Math.max(0, min));
        out += titleBracket + line.substring(max);

        return out;
    }

    public static String buildSmallTitle(String title) {
        String line = CivColor.LightBlue + "------------------------------";

        String titleBracket = "[ " + title + " ]";

        int min = (line.length() / 2) - titleBracket.length() / 2;
        int max = (line.length() / 2) + titleBracket.length() / 2;

        String out = CivColor.LightBlue + line.substring(0, Math.max(0, min));
        out += titleBracket + line.substring(max);

        return out;
    }

    public static void sendSubHeading(CommandSender sender, String title) {
        send(sender, buildSmallTitle(title));
    }

    public static void sendHeading(Resident resident, String title) {
        Player player;
        try {
            player = CivGlobal.getPlayer(resident);
            sendHeading(player, title);
        } catch (CivException e) {
        }
    }

    public static void sendHeading(CommandSender sender, String title) {
        send(sender, buildTitle(title));
    }

    public static void sendSuccess(CommandSender sender, String message) {
        send(sender, CivColor.LightGreen + message);
    }

    public static void global(String string) {
        CivLog.info("[Global] " + string);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(CivColor.LightBlue + CivSettings.localize.localizedString("civMsg_Globalprefix") + " " + CivColor.White + string);
        }
    }

    public static void globalTitle(String title, String subTitle) {
        CivLog.info("[GlobalTitle] " + title + " - " + subTitle);
        for (Player player : Bukkit.getOnlinePlayers()) {
            Resident resident = CivGlobal.getResident(player);
            if (CivSettings.hasTitleAPI && resident.isTitleAPI()) {
                CivMessage.sendTitle(player, 10, 60, 10, title, subTitle);
            } else {
                send(player, buildTitle(title));
                if (!subTitle.equals("")) {
                    send(player, subTitle);
                }
            }
        }
    }

    public static void globalHeading(String string) {
        CivLog.info("[GlobalHeading] " + string);
        for (Player player : Bukkit.getOnlinePlayers()) {
            send(player, buildTitle(string));
        }
    }

    public static void sendScout(Civilization civ, String string) {
        CivLog.info("[Scout:" + civ.getName() + "] " + string);
        for (Town t : civ.getTowns()) {
            for (Resident resident : t.getResidents()) {
                if (!resident.isShowScout()) {
                    continue;
                }

                Player player;
                try {
                    player = CivGlobal.getPlayer(resident);
                    if (player != null) {
                        CivMessage.send(player, CivColor.Purple + CivSettings.localize.localizedString("civMsg_ScoutPrefix") + " " + CivColor.White + string);
                    }
                } catch (CivException e) {
                }
            }

        }
    }

    public static void sendTown(Town town, String string) {
        CivLog.info("[Town:" + town.getName() + "] " + string);

        for (Resident resident : town.getResidents()) {
            if (!resident.isShowTown()) {
                continue;
            }

            Player player;
            try {
                player = CivGlobal.getPlayer(resident);
                if (player != null) {
                    CivMessage.send(player, CivColor.Gold + CivSettings.localize.localizedString("civMsg_Townprefix") + " " + CivColor.White + string);
                }
            } catch (CivException e) {
            }
        }
    }

    public static void sendCiv(Civilization civ, String string) {
        CivLog.info("[Civ:" + civ.getName() + "] " + string);
        for (Town t : civ.getTowns()) {
            for (Resident resident : t.getResidents()) {
                if (!resident.isShowCiv()) {
                    continue;
                }

                Player player;
                try {
                    player = CivGlobal.getPlayer(resident);
                    if (player != null) {
                        CivMessage.send(player, CivColor.LightPurple + CivSettings.localize.localizedString("civMsg_Civprefix") + " " + CivColor.White + string);
                    }
                } catch (CivException e) {
                }
            }

        }
    }


    public static void send(CommandSender sender, List<String> outs) {
        for (String str : outs) {
            send(sender, str);
        }
    }


    public static void sendTownChat(Town town, Resident resident, String format, String message) {
        if (town == null) {
            try {
                Player player = CivGlobal.getPlayer(resident);
                player.sendMessage(CivColor.Rose + CivSettings.localize.localizedString("civMsg_tcNotInTown"));

            } catch (CivException e) {
            }
            return;
        }

        CivLog.info("[TC:" + town.getName() + "] " + resident.getName() + ": " + message);

        for (Resident r : town.getResidents()) {
            try {
                Player player = CivGlobal.getPlayer(r);
                String msg = CivColor.LightBlue + CivSettings.localize.localizedString("civMsg_tcPrefix") + CivColor.White + String.format(format, resident.getName(), message);
                player.sendMessage(msg);
            } catch (CivException e) {
                continue; /* player not online. */
            }
        }

        for (String name : getExtraTownChatListeners(town)) {
            try {
                Player player = CivGlobal.getPlayer(name);
                String msg = CivColor.LightBlue + CivSettings.localize.localizedString("civMsg_tcPrefix2") + town.getName() + "]" + CivColor.White + String.format(format, resident.getName(), message);
                player.sendMessage(msg);
            } catch (CivException e) {
                /* player not online. */
            }
        }
    }


    public static void sendCivChat(Civilization civ, Resident resident, String format, String message) {
        if (civ == null) {
            try {
                Player player = CivGlobal.getPlayer(resident);
                player.sendMessage(CivColor.Rose + CivSettings.localize.localizedString("civMsg_ccNotInCiv"));

            } catch (CivException ignored) {
            }
            return;
        }

        String townName = "";
        if (resident.getTown() != null) {
            townName = resident.getTown().getName();
        }

        for (Town t : civ.getTowns()) {
            for (Resident r : t.getResidents()) {
                try {
                    Player player = CivGlobal.getPlayer(r);


                    String msg = CivColor.Gold + CivSettings.localize.localizedString("civMsg_ccPrefix1") + " " + townName + "]" + CivColor.White + String.format(format, resident.getName(), message);
                    player.sendMessage(msg);
                } catch (CivException e) {
                    continue; /* player not online. */
                }
            }
        }

        for (String name : getExtraCivChatListeners(civ)) {
            try {
                Player player = CivGlobal.getPlayer(name);
                String msg = CivColor.Gold + CivSettings.localize.localizedString("civMsg_ccPrefix2") + civ.getName() + " " + townName + "]" + CivColor.White + String.format(format, resident.getName(), message);
                player.sendMessage(msg);
            } catch (CivException e) {
                /* player not online. */
            }
        }

        return;
    }

    public static void sendChat(Resident resident, String format, String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            String msg = String.format(format, resident.getName(), message);
            player.sendMessage(msg);
        }
    }

    public static void addExtraTownChatListener(Town town, String name) {

        ArrayList<String> names = extraTownChatListeners.get(town.getName().toLowerCase());
        if (names == null) {
            names = new ArrayList<String>();
        }

        for (String str : names) {
            if (str.equals(name)) {
                return;
            }
        }

        names.add(name);
        extraTownChatListeners.put(town.getName().toLowerCase(), names);
    }

    public static void removeExtraTownChatListener(Town town, String name) {
        ArrayList<String> names = extraTownChatListeners.get(town.getName().toLowerCase());
        if (names == null) {
            return;
        }

        for (String str : names) {
            if (str.equals(name)) {
                names.remove(str);
                break;
            }
        }

        extraTownChatListeners.put(town.getName().toLowerCase(), names);
    }

    public static ArrayList<String> getExtraTownChatListeners(Town town) {
        ArrayList<String> names = extraTownChatListeners.get(town.getName().toLowerCase());
        if (names == null) {
            return new ArrayList<String>();
        }
        return names;
    }

    public static void addExtraCivChatListener(Civilization civ, String name) {

        ArrayList<String> names = extraCivChatListeners.get(civ.getName().toLowerCase());
        if (names == null) {
            names = new ArrayList<String>();
        }

        for (String str : names) {
            if (str.equals(name)) {
                return;
            }
        }

        names.add(name);

        extraCivChatListeners.put(civ.getName().toLowerCase(), names);
    }

    public static void removeExtraCivChatListener(Civilization civ, String name) {
        ArrayList<String> names = extraCivChatListeners.get(civ.getName().toLowerCase());
        if (names == null) {
            return;
        }

        for (String str : names) {
            if (str.equals(name)) {
                names.remove(str);
                break;
            }
        }

        extraCivChatListeners.put(civ.getName().toLowerCase(), names);
    }

    public static ArrayList<String> getExtraCivChatListeners(Civilization civ) {
        ArrayList<String> names = extraCivChatListeners.get(civ.getName().toLowerCase());
        if (names == null) {
            return new ArrayList<String>();
        }
        return names;
    }

    public static void sendTownSound(Town town, Sound sound, float f, float g) {
        for (Resident resident : town.getResidents()) {
            Player player;
            try {
                player = CivGlobal.getPlayer(resident);

                player.playSound(player.getLocation(), sound, f, g);
            } catch (CivException e) {
                //player not online.
            }
        }

    }

    public static void sendAll(String str) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(str);
        }
    }

    public static void sendCamp(Camp camp, String message) {
        for (Resident resident : camp.getMembers()) {
            try {
                Player player = CivGlobal.getPlayer(resident);
                player.sendMessage(CivColor.Yellow + "[Camp] " + CivColor.Yellow + message);
                CivLog.info("[Camp:" + camp.getName() + "] " + message);

            } catch (CivException e) {
                //player not online.
            }
        }
    }

    public static void sendTownHeading(Town town, String string) {
        CivLog.info("[Town:" + town.getName() + "] " + string);
        for (Resident resident : town.getResidents()) {
            if (!resident.isShowTown()) {
                continue;
            }

            Player player;
            try {
                player = CivGlobal.getPlayer(resident);
                if (player != null) {
                    CivMessage.sendHeading(player, string);
                }
            } catch (CivException e) {
            }
        }
    }

    public static void sendSuccess(Resident resident, String message) {
        try {
            Player player = CivGlobal.getPlayer(resident);
            sendSuccess(player, message);
        } catch (CivException e) {
            return;
        }
    }
}
