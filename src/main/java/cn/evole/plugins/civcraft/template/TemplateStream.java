/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.template;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.util.BlockCoord;
import cn.evole.plugins.civcraft.util.ItemManager;
import cn.evole.plugins.civcraft.util.SimpleBlock;
import org.bukkit.Location;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TemplateStream {
    /*
     * TemplateStream
     *
     * Rather than load an entire template into memory, only request particular parts of a template at
     * any one time. This is useful for reducing total memory usage. Applications are as follows:
     * 		-) Structure floating validation
     * 		       We only want the bottom layer (aka the shape) of a template when attempting to
     *             perform structure floating validation.
     *      -) Post-Build/Reload tasks
     *             When the server starts, we run a the post build task which reloads the template
     *             for each structure in the server. All of the command blocks are found and re-applied
     *             at this time. There is no need to load the entire template. Just the command blocks.
     */

    /*
     * Container that holds any/all simple block objects we've retrieved from the file.
     * Rather than clear and create new simple blocks, we'd rather overwrite old ones
     * that way we're not pressing the allocator/garbage collector all the time.
     */
    ArrayList<SimpleBlock> blocks = new ArrayList<SimpleBlock>();
    /* source file we're going to be grabbing data from. */
    private String source;
    /* Sizes loaded from the top of the template file. */
    private int sizeX;
    private int sizeY;
    private int sizeZ;
    private int currentBlockCount = 0;

    private File sourceFile = null;

    public TemplateStream(String filepath) throws IOException {
        this.setSource(filepath);
    }

    private SimpleBlock getSimpleBlockFromLine(String line) {
        String locTypeSplit[] = line.split(",");
        String location = locTypeSplit[0];
        String type = locTypeSplit[1];

        /* Parse out location */
        String locationSplit[] = location.split(":");
        int blockX = Integer.valueOf(locationSplit[0]);
        int blockY = Integer.valueOf(locationSplit[1]);
        int blockZ = Integer.valueOf(locationSplit[2]);

        /* Parse out type */
        String typeSplit[] = type.split(":");
        int blockId = Integer.valueOf(typeSplit[0]);
        int blockData = Integer.valueOf(typeSplit[1]);


        SimpleBlock block;
        if (currentBlockCount < blocks.size()) {
            /* Get an already allocated simple block. */
            block = blocks.get(currentBlockCount);
        } else {
            /* allocate a new one and add to cache. */
            block = new SimpleBlock(blockId, blockData);
            blocks.add(block);
        }
        currentBlockCount++;

        block.x = blockX;
        block.y = blockY;
        block.z = blockZ;
        return block;
    }

    /*
     * Loads all blocks on y-layer y and returns them in a list.
     */
    public List<SimpleBlock> getBlocksForLayer(int y) throws IOException {
        if (y > sizeY) {
            throw new IllegalArgumentException();
        }

        BufferedReader reader = new BufferedReader(new FileReader(sourceFile));
        LinkedList<SimpleBlock> returnBlocks = new LinkedList<SimpleBlock>();

        try {
            /* Read past the starting size line. */
            reader.readLine();

            String line = reader.readLine();
            while (line != null) {
                String locTypeSplit[] = line.split(",");
                String location = locTypeSplit[0];
                String locationSplit[] = location.split(":");
                int blockY = Integer.valueOf(locationSplit[1]);

                if (blockY == y) {
                    returnBlocks.add(getSimpleBlockFromLine(line));
                }

                line = reader.readLine();
            }
            return returnBlocks;
        } finally {
            reader.close();
        }
    }

    /*
     * Loads entire template into simple blocks
     */
    public List<SimpleBlock> getTemplateBlocks() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(sourceFile));
        LinkedList<SimpleBlock> returnBlocks = new LinkedList<SimpleBlock>();

        try {
            /* Read past the starting size line. */
            reader.readLine();

            String line = reader.readLine();
            while (line != null) {
                returnBlocks.add(getSimpleBlockFromLine(line));
                line = reader.readLine();
            }
            return returnBlocks;
        } finally {
            reader.close();
        }
    }

    /*
     * builds the contents of the blocks array centered on our location.
     */
    public void debugBuildBlocksHere(Location location) {

        for (SimpleBlock block : blocks) {
            BlockCoord bcoord = new BlockCoord(location);
            bcoord.setX(bcoord.getX() + block.x);
            bcoord.setY(bcoord.getY() + block.y);
            bcoord.setZ(bcoord.getZ() + block.z);
            ItemManager.setTypeIdAndData(bcoord.getBlock(), block.getType(), block.getData(), false);
        }

    }


    public String getSource() {
        return source;
    }

    public void setSource(String filepath) throws IOException {
        this.source = filepath;
        sourceFile = new File(filepath);
        BufferedReader reader = new BufferedReader(new FileReader(sourceFile));
        try {
            // Read first line and get size.
            String line = null;
            line = reader.readLine();
            if (line == null) {
                throw new IOException(CivSettings.localize.localizedString("template_invalidFile") + " " + filepath);
            }

            String split[] = line.split(";");
            sizeX = Integer.valueOf(split[0]);
            sizeY = Integer.valueOf(split[1]);
            sizeZ = Integer.valueOf(split[2]);
        } finally {
            reader.close();
        }
    }

    public int getSizeX() {
        return sizeX;
    }

    public void setSizeX(int sizeX) {
        this.sizeX = sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public void setSizeY(int sizeY) {
        this.sizeY = sizeY;
    }

    public int getSizeZ() {
        return sizeZ;
    }

    public void setSizeZ(int sizeZ) {
        this.sizeZ = sizeZ;
    }
}
