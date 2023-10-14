package cn.evole.plugins.civcraft.util;

import org.bukkit.ChatColor;

public class CivColor {
    public static final String Black = "§0";
    public static final String Navy = "§1";
    public static final String Green = "§2";
    public static final String Blue = "§3";
    public static final String Red = "§4";
    public static final String Purple = "§5";
    public static final String Gold = "§6";
    public static final String LightGray = "§7";
    public static final String Gray = "§8";
    public static final String DarkPurple = "§9";
    public static final String LightGreen = "§a";
    public static final String LightBlue = "§b";
    public static final String Rose = "§c";
    public static final String LightPurple = "§d";
    public static final String Yellow = "§e";
    public static final String White = "§f";
    public static final String BOLD = "" + ChatColor.BOLD;
    public static final String ITALIC = "" + ChatColor.ITALIC;
    public static final String RESET = "" + ChatColor.RESET;
    public static final String UNDERLINE = "" + ChatColor.UNDERLINE;

    public static final String BlackBold = "§0" + ChatColor.BOLD;
    public static final String NavyBold = "§1" + ChatColor.BOLD;
    public static final String GreenBold = "§2" + ChatColor.BOLD;
    public static final String BlueBold = "§3" + ChatColor.BOLD;
    public static final String RedBold = "§4" + ChatColor.BOLD;
    public static final String PurpleBold = "§5" + ChatColor.BOLD;
    public static final String GoldBold = "§6" + ChatColor.BOLD;
    public static final String LightGrayBold = "§7" + ChatColor.BOLD;
    public static final String GrayBold = "§8" + ChatColor.BOLD;
    public static final String DarkPurpleBold = "§9" + ChatColor.BOLD;
    public static final String LightGreenBold = "§a" + ChatColor.BOLD;
    public static final String LightBlueBold = "§b" + ChatColor.BOLD;
    public static final String RoseBold = "§c" + ChatColor.BOLD;
    public static final String LightPurpleBold = "§d" + ChatColor.BOLD;
    public static final String YellowBold = "§e" + ChatColor.BOLD;
    public static final String WhiteBold = "§f" + ChatColor.BOLD;

    public static final String BlackItalic = "§0" + ChatColor.ITALIC;
    public static final String NavyItalic = "§1" + ChatColor.ITALIC;
    public static final String GreenItalic = "§2" + ChatColor.ITALIC;
    public static final String BlueItalic = "§3" + ChatColor.ITALIC;
    public static final String RedItalic = "§4" + ChatColor.ITALIC;
    public static final String PurpleItalic = "§5" + ChatColor.ITALIC;
    public static final String GoldItalic = "§6" + ChatColor.ITALIC;
    public static final String LightGrayItalic = "§7" + ChatColor.ITALIC;
    public static final String GrayItalic = "§8" + ChatColor.ITALIC;
    public static final String DarkPurpleItalic = "§9" + ChatColor.ITALIC;
    public static final String LightGreenItalic = "§a" + ChatColor.ITALIC;
    public static final String LightBlueItalic = "§b" + ChatColor.ITALIC;
    public static final String RoseItalic = "§c" + ChatColor.ITALIC;
    public static final String LightPurpleItalic = "§d" + ChatColor.ITALIC;
    public static final String YellowItalic = "§e" + ChatColor.ITALIC;
    public static final String WhiteItalic = "§f" + ChatColor.ITALIC;

    public static final String YellowBoldItalic = "§e" + ChatColor.BOLD + ChatColor.ITALIC;

    public static String colorize(String input) {
        String output = input;

        output = output.replaceAll("<red>", "§4");
        output = output.replaceAll("<rose>", "§c");
        output = output.replaceAll("<gold>", "§6");
        output = output.replaceAll("<yellow>", "§e");
        output = output.replaceAll("<green>", "§2");
        output = output.replaceAll("<lightgreen>", "§a");
        output = output.replaceAll("<lightblue>", "§b");
        output = output.replaceAll("<blue>", "§3");
        output = output.replaceAll("<navy>", "§1");
        output = output.replaceAll("<darkpurple>", "§9");
        output = output.replaceAll("<lightpurple>", "§d");
        output = output.replaceAll("<purple>", "§5");
        output = output.replaceAll("<white>", "§f");
        output = output.replaceAll("<lightgray>", "§7");
        output = output.replaceAll("<gray>", "§8");
        output = output.replaceAll("<black>", "§0");

        output = output.replaceAll("<redb>", RedBold);
        output = output.replaceAll("<roseb>", RoseBold);
        output = output.replaceAll("<goldb>", GoldBold);
        output = output.replaceAll("<yellowb>", YellowBold);
        output = output.replaceAll("<greenb>", GreenBold);
        output = output.replaceAll("<lightgreenb>", LightGreenBold);
        output = output.replaceAll("<lightblueb>", LightBlueBold);
        output = output.replaceAll("<blueb>", BlueBold);
        output = output.replaceAll("<navyb>", NavyBold);
        output = output.replaceAll("<darkpurpleb>", DarkPurpleBold);
        output = output.replaceAll("<lightpurpleb>", LightPurpleBold);
        output = output.replaceAll("<purpleb>", PurpleBold);
        output = output.replaceAll("<whiteb>", WhiteBold);
        output = output.replaceAll("<lightgrayb>", LightGrayBold);
        output = output.replaceAll("<grayb>", GrayBold);
        output = output.replaceAll("<blackb>", BlackBold);

        output = output.replaceAll("<redi>", RedItalic);
        output = output.replaceAll("<rosei>", RoseItalic);
        output = output.replaceAll("<goldi>", GoldItalic);
        output = output.replaceAll("<yellowi>", YellowItalic);
        output = output.replaceAll("<greeni>", GreenItalic);
        output = output.replaceAll("<lightgreeni>", LightGreenItalic);
        output = output.replaceAll("<lightbluei>", LightBlueItalic);
        output = output.replaceAll("<bluei>", BlueItalic);
        output = output.replaceAll("<navyi>", NavyItalic);
        output = output.replaceAll("<darkpurplei>", DarkPurpleItalic);
        output = output.replaceAll("<lightpurplei>", LightPurpleItalic);
        output = output.replaceAll("<purplei>", PurpleItalic);
        output = output.replaceAll("<whitei>", WhiteItalic);
        output = output.replaceAll("<lightgrayi>", LightGrayItalic);
        output = output.replaceAll("<grayi>", GrayItalic);
        output = output.replaceAll("<blacki>", BlackItalic);

        output = output.replaceAll("<yellowib>", YellowBoldItalic);

        output = output.replaceAll("<b>", "" + ChatColor.BOLD);
        output = output.replaceAll("<u>", "" + ChatColor.UNDERLINE);
        output = output.replaceAll("<i>", "" + ChatColor.ITALIC);
        output = output.replaceAll("<r>", "" + ChatColor.RESET);

        return output;
    }

    public static String strip(String line) {
        for (ChatColor cc : ChatColor.values())
            line.replaceAll(cc.toString(), "");
        return line;
    }

    public static String valueOf(String color) {
        switch (color.toLowerCase()) {
            case "black":
                return "§0";
            case "navy":
                return "§1";
            case "green":
                return "§2";
            case "blue":
                return "§3";
            case "red":
                return "§4";
            case "purple":
                return "§5";
            case "gold":
                return "§6";
            case "lightgray":
                return "§7";
            case "gray":
                return "§8";
            case "darkpurple":
                return "§9";
            case "lightgreen":
                return "§a";
            case "lightblue":
                return "§b";
            case "rose":
                return "§c";
            case "lightpurple":
                return "§d";
            case "yellow":
                return "§e";
            case "white":
                return "§f";
        }
        return "§f";
    }

    public static String stripTags(String input) {
        String output = input;

        output = output.replaceAll("<red>", "");
        output = output.replaceAll("<rose>", "");
        output = output.replaceAll("<gold>", "");
        output = output.replaceAll("<yellow>", "");
        output = output.replaceAll("<green>", "");
        output = output.replaceAll("<lightgreen>", "");
        output = output.replaceAll("<lightblue>", "");
        output = output.replaceAll("<blue>", "");
        output = output.replaceAll("<navy>", "");
        output = output.replaceAll("<darkpurple>", "");
        output = output.replaceAll("<lightpurple>", "");
        output = output.replaceAll("<purple>", "");
        output = output.replaceAll("<white>", "");
        output = output.replaceAll("<lightgray>", "");
        output = output.replaceAll("<gray>", "");
        output = output.replaceAll("<black>", "");

        output = output.replaceAll("<redb>", "");
        output = output.replaceAll("<roseb>", "");
        output = output.replaceAll("<goldb>", "");
        output = output.replaceAll("<yellowb>", "");
        output = output.replaceAll("<greenb>", "");
        output = output.replaceAll("<lightgreenb>", "");
        output = output.replaceAll("<lightblueb>", "");
        output = output.replaceAll("<blueb>", "");
        output = output.replaceAll("<navyb>", "");
        output = output.replaceAll("<darkpurpleb>", "");
        output = output.replaceAll("<lightpurpleb>", "");
        output = output.replaceAll("<purpleb>", "");
        output = output.replaceAll("<whiteb>", "");
        output = output.replaceAll("<lightgrayb>", "");
        output = output.replaceAll("<grayb>", "");
        output = output.replaceAll("<blackb>", "");

        output = output.replaceAll("<redi>", "");
        output = output.replaceAll("<rosei>", "");
        output = output.replaceAll("<goldi>", "");
        output = output.replaceAll("<yellowi>", "");
        output = output.replaceAll("<greeni>", "");
        output = output.replaceAll("<lightgreeni>", "");
        output = output.replaceAll("<lightbluei>", "");
        output = output.replaceAll("<bluei>", "");
        output = output.replaceAll("<navyi>", "");
        output = output.replaceAll("<darkpurplei>", "");
        output = output.replaceAll("<lightpurplei>", "");
        output = output.replaceAll("<purplei>", "");
        output = output.replaceAll("<whitei>", "");
        output = output.replaceAll("<lightgrayi>", "");
        output = output.replaceAll("<grayi>", "");
        output = output.replaceAll("<blacki>", "");

        output = output.replaceAll("<yellowib>", "");

        output = output.replaceAll("<i>", "");
        output = output.replaceAll("<u>", "");
        output = output.replaceAll("<i>", "");
        output = output.replaceAll("<s>", "");
        output = output.replaceAll("<r>", "");

        return output;
    }
}

