package cn.evole.plugins.civcraft.util;


import cn.evole.plugins.civcraft.main.CivCraft;
import gpl.jnbt.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

public class Schematic {
    private short[] blocks;
    private byte[] data;
    private short width;
    private short lenght;
    private short height;
    private String name;

    public Schematic(final String name, final short[] blocks, final byte[] data, final short width, final short lenght, final short height) {
        this.blocks = blocks;
        this.data = data;
        this.width = width;
        this.lenght = lenght;
        this.height = height;
        this.name = name;
    }

    public static Schematic loadSchematic(final File file) throws IOException {
        final FileInputStream stream = new FileInputStream(file);
        final NBTInputStream nbtStream = new NBTInputStream(stream);
        final CompoundTag schematicTag = (CompoundTag) nbtStream.readTag();
        if (!schematicTag.getName().equals("Schematic")) {
            nbtStream.close();
            throw new IllegalArgumentException("Tag \"Schematic\" does not exist or is not first");
        }
        final Map<String, Tag> schematic = schematicTag.getValue();
        if (!schematic.containsKey("Blocks")) {
            nbtStream.close();
            throw new IllegalArgumentException("Schematic file is missing a \"Blocks\" tag");
        }
        final short width = getChildTag(schematic, "Width", ShortTag.class).getValue();
        final short length = getChildTag(schematic, "Length", ShortTag.class).getValue();
        final short height = getChildTag(schematic, "Height", ShortTag.class).getValue();
        final byte[] blockId = getChildTag(schematic, "Blocks", ByteArrayTag.class).getValue();
        final byte[] blockData = getChildTag(schematic, "Data", ByteArrayTag.class).getValue();
        byte[] addId = new byte[0];
        final short[] blocks = new short[blockId.length];
        if (schematic.containsKey("AddBlocks")) {
            addId = getChildTag(schematic, "AddBlocks", ByteArrayTag.class).getValue();
        }
        for (int index = 0; index < blockId.length; ++index) {
            if (index >> 1 >= addId.length) {
                blocks[index] = (short) (blockId[index] & 0xFF);
            } else if ((index & 0x1) == 0x0) {
                blocks[index] = (short) (((addId[index >> 1] & 0xF) << 8) + (blockId[index] & 0xFF));
            } else {
                blocks[index] = (short) (((addId[index >> 1] & 0xF0) << 4) + (blockId[index] & 0xFF));
            }
        }
        nbtStream.close();
        return new Schematic(file.getName().replace(".schematic", ""), blocks, blockData, width, length, height);
    }

    private static <T extends Tag> T getChildTag(final Map<String, Tag> items, final String key, final Class<T> expected) throws IllegalArgumentException {
        if (!items.containsKey(key)) {
            throw new IllegalArgumentException("Schematic file is missing a \"" + key + "\" tag");
        }
        final Tag tag = items.get(key);
        if (!expected.isInstance(tag)) {
            throw new IllegalArgumentException(key + " tag is not of tag type " + expected.getName());
        }
        return expected.cast(tag);
    }

    @SuppressWarnings("deprecation")
    public void paste(final Location loc) {
        final BlockFace[] bf = {BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST};
        final short[] blocks = this.getBlocks();
        final byte[] blockData = this.getData();
        final short length = this.getLenght();
        final short width = this.getWidth();
        final short height = this.getHeight();
        //TODO: Add Lucky Blocks?
//        boolean luckkyBlockPasted = false;
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < length; ++z) {
                    final int index = y * width * length + z * width + x;
                    final Block block = new Location(loc.getWorld(), x + loc.getX(), y + loc.getY(), z + loc.getZ()).getBlock();
                    if (Material.getMaterial((int) blocks[index]) != null && (blocks[index] != 0 || (block.getType() != Material.STATIONARY_WATER && block.getType() != Material.STATIONARY_LAVA))) {
                        block.setTypeIdAndData((int) blocks[index], blockData[index], false);
                    }
                    Material.getMaterial("test");
                    if (Material.getMaterial((int) blocks[index]) == Material.CHEST) {
//                        if (!luckkyBlockPasted) {
//                            block.setType(Material.LUCKY_BLOCK);
//                            luckkyBlockPasted = true;
//                        }
//                        else {
                        block.setType(Material.AIR);
//                        }
                    } else if (Material.getMaterial((int) blocks[index]) == Material.SKULL) {
                        final Skull skull = (Skull) block.getState();
                        skull.setRotation(bf[CivCraft.civRandom.nextInt(bf.length)]);
                        skull.update();
                    }
                }
            }
        }
    }

    public short[] getBlocks() {
        return this.blocks;
    }

    public String getName() {
        return this.name;
    }

    public byte[] getData() {
        return this.data;
    }

    public short getWidth() {
        return this.width;
    }

    public short getLenght() {
        return this.lenght;
    }

    public short getHeight() {
        return this.height;
    }
}
