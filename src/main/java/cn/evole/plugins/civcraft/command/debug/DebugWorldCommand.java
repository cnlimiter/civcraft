package cn.evole.plugins.civcraft.command.debug;

import cn.evole.plugins.civcraft.command.CommandBase;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.util.ChunkCoord;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;

public class DebugWorldCommand extends CommandBase {

    @Override
    public void init() {
        command = "/dbg world";
        displayName = "Debug World";

        commands.put("create", "[name] - 创建新的世界.");
        commands.put("tp", "[name] teleports you to spawn at the specified world.");
        commands.put("list", "Lists worlds according to bukkit.");
    }

    public void list_cmd() {
        CivMessage.sendHeading(sender, "Worlds");
        for (World world : Bukkit.getWorlds()) {
            CivMessage.send(sender, world.getName());
        }
    }

    public void create_cmd() throws CivException {
        String name = getNamedString(1, "输入世界的名字");

        WorldCreator wc = new WorldCreator(name);
        wc.environment(Environment.NORMAL);
        wc.type(WorldType.FLAT);
        wc.generateStructures(false);

        World world = Bukkit.getServer().createWorld(wc);
        world.setSpawnFlags(false, false);
        ChunkCoord.addWorld(world);

        CivMessage.sendSuccess(sender, "世界 " + name + " 医生称.");

    }

    public void tp_cmd() throws CivException {
        String name = getNamedString(1, "输入一个世界的名字");
        Player player = getPlayer();

        World world = Bukkit.getWorld(name);
        player.teleport(world.getSpawnLocation());

        CivMessage.sendSuccess(sender, "传送到世界:" + name + "的出生点");
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
