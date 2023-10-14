/**
 * [2023] Cnlimiter LLC
 * All Rights Reserved.
 * Name: civcraft
 * Author: cnlimiter
 * UpdateTime: 2023/10/14 16:15
 * Description:
 * License: ARR
 */
package cn.evole.plugins.civcraft.structure;

import cn.evole.plugins.civcraft.config.CivSettings;
import cn.evole.plugins.civcraft.config.ConfigMarketItem;
import cn.evole.plugins.civcraft.exception.CivException;
import cn.evole.plugins.civcraft.main.CivGlobal;
import cn.evole.plugins.civcraft.main.CivLog;
import cn.evole.plugins.civcraft.main.CivMessage;
import cn.evole.plugins.civcraft.object.Resident;
import cn.evole.plugins.civcraft.object.StructureSign;
import cn.evole.plugins.civcraft.object.Town;
import cn.evole.plugins.civcraft.util.BlockCoord;
import cn.evole.plugins.civcraft.util.CivColor;
import cn.evole.plugins.civcraft.util.ItemManager;
import cn.evole.plugins.civcraft.util.SimpleBlock;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;

public class Market extends Structure {

    public static int BULK_AMOUNT = 64;
    public HashMap<Integer, LinkedList<StructureSign>> signIndex = new HashMap<Integer, LinkedList<StructureSign>>();

    protected Market(Location center, String id, Town town) throws CivException {
        super(center, id, town);
        CivGlobal.addMarket(this);
    }

    public Market(ResultSet rs) throws SQLException, CivException {
        super(rs);
        CivGlobal.addMarket(this);
    }

    public static void globalSignUpdate(int id) {
        for (Market market : CivGlobal.getMarkets()) {

            LinkedList<StructureSign> signs = market.signIndex.get(id);
            if (signs == null) {
                continue;
            }

            for (StructureSign sign : signs) {
                ConfigMarketItem item = CivSettings.marketItems.get(id);
                if (item != null) {
                    try {
                        market.setSignText(sign, item);
                    } catch (ClassCastException e) {
                        CivLog.error("Can't cast structure sign to sign for market update. " + sign.getCoord().getX() + " " + sign.getCoord().getY() + " " + sign.getCoord().getZ());
                        continue;
                    }
                }
            }
        }
    }

    @Override
    public void delete() throws SQLException {
        super.delete();
        CivGlobal.removeMarket(this);
    }

    public void processBuy(Player player, Resident resident, int bulkCount, ConfigMarketItem item) throws CivException {
        item.buy(resident, player, bulkCount);
    }

    public void processSell(Player player, Resident resident, int bulkCount, ConfigMarketItem item) throws CivException {
        item.sell(resident, player, bulkCount);
    }

    @Override
    public void processSignAction(Player player, StructureSign sign, PlayerInteractEvent event) throws CivException {

        Integer id = Integer.valueOf(sign.getType());
        ConfigMarketItem item = CivSettings.marketItems.get(id);
        Resident resident = CivGlobal.getResident(player);

        if (resident == null) {
            CivMessage.sendError(player, CivSettings.localize.localizedString("market_invalidPlayer"));
            return;
        }

        if (item == null) {
            CivMessage.sendError(player, CivSettings.localize.localizedString("market_invalidID") + id);
            return;
        }

        switch (sign.getAction().toLowerCase()) {
            case "sellbig":
                processSell(player, resident, BULK_AMOUNT, item);
                break;
            case "sell":
                processSell(player, resident, 1, item);
                break;
            case "buy":
                processBuy(player, resident, 1, item);
                break;
            case "buybig":
                processBuy(player, resident, BULK_AMOUNT, item);
                break;
        }

        player.updateInventory();
        Market.globalSignUpdate(id);
    }

    public void setSignText(StructureSign sign, ConfigMarketItem item) throws ClassCastException {

        String itemColor;
        switch (item.lastaction) {
            case BUY:
                itemColor = CivColor.LightGreen;
                break;
            case SELL:
                itemColor = CivColor.Rose;
                break;
            default:
                itemColor = CivColor.Black;
                break;
        }

        try {
            Sign s;
            switch (sign.getAction().toLowerCase()) {
                case "sellbig":
                    if (sign.getCoord().getBlock().getState() instanceof Sign) {
                        s = (Sign) sign.getCoord().getBlock().getState();
                        s.setLine(0, ChatColor.BOLD + CivSettings.localize.localizedString("market_sign_sellBulk"));
                        s.setLine(1, item.name);
                        s.setLine(2, itemColor + item.getSellCostForAmount(BULK_AMOUNT) + " " + CivSettings.CURRENCY_NAME);
                        s.setLine(3, CivSettings.localize.localizedString("var_market_sign_amount", BULK_AMOUNT));
                        s.update();
                    }
                    break;
                case "sell":
                    if (sign.getCoord().getBlock().getState() instanceof Sign) {
                        s = (Sign) sign.getCoord().getBlock().getState();
                        s.setLine(0, ChatColor.BOLD + CivSettings.localize.localizedString("market_sign_sell"));
                        s.setLine(1, item.name);
                        s.setLine(2, itemColor + item.getSellCostForAmount(1) + " " + CivSettings.CURRENCY_NAME);
                        s.setLine(3, CivSettings.localize.localizedString("var_market_sign_amount", 1));
                        s.update();
                    }
                    break;
                case "buy":
                    if (sign.getCoord().getBlock().getState() instanceof Sign) {
                        s = (Sign) sign.getCoord().getBlock().getState();
                        s.setLine(0, ChatColor.BOLD + CivSettings.localize.localizedString("market_sign_buy"));
                        s.setLine(1, item.name);
                        s.setLine(2, itemColor + item.getBuyCostForAmount(1) + " " + CivSettings.CURRENCY_NAME);
                        s.setLine(3, CivSettings.localize.localizedString("var_market_sign_amount", 1));
                        s.update();
                    }
                    break;
                case "buybig":
                    if (sign.getCoord().getBlock().getState() instanceof Sign) {
                        s = (Sign) sign.getCoord().getBlock().getState();
                        s.setLine(0, ChatColor.BOLD + CivSettings.localize.localizedString("market_sign_buyBulk"));
                        s.setLine(1, item.name);
                        s.setLine(2, itemColor + item.getBuyCostForAmount(BULK_AMOUNT) + " " + CivSettings.CURRENCY_NAME);
                        s.setLine(3, CivSettings.localize.localizedString("var_market_sign_amount", BULK_AMOUNT));
                        s.update();
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void buildSign(String action, Integer id, BlockCoord absCoord, ConfigMarketItem item, SimpleBlock commandBlock) {
        Block b = absCoord.getBlock();

        ItemManager.setTypeIdAndData(b, ItemManager.getId(Material.WALL_SIGN), (byte) commandBlock.getData(), false);

        StructureSign structSign = CivGlobal.getStructureSign(absCoord);
        if (structSign == null) {
            structSign = new StructureSign(absCoord, this);
        }

        structSign.setDirection(ItemManager.getData(b.getState()));
        structSign.setType("" + id);
        structSign.setAction(action);

        structSign.setOwner(this);
        this.addStructureSign(structSign);
        CivGlobal.addStructureSign(structSign);

        LinkedList<StructureSign> signs = this.signIndex.get(id);
        if (signs == null) {
            signs = new LinkedList<StructureSign>();
        }

        signs.add(structSign);
        this.signIndex.put(id, signs);
        this.setSignText(structSign, item);
    }

    @Override
    public void onPostBuild(BlockCoord absCoord, SimpleBlock commandBlock) {
        Integer id;
        ConfigMarketItem item;
        switch (commandBlock.command.toLowerCase().trim()) {
            case "/sellbig":
                id = Integer.valueOf(commandBlock.keyvalues.get("id"));
                item = CivSettings.marketItems.get(id);
                if (item != null) {
                    if (item.isStackable()) {
                        buildSign("sellbig", id, absCoord, item, commandBlock);
                    }
                }
                break;
            case "/sell":
                id = Integer.valueOf(commandBlock.keyvalues.get("id"));

                item = CivSettings.marketItems.get(id);
                if (item != null) {
                    buildSign("sell", id, absCoord, item, commandBlock);
                }
                break;
            case "/buy":
                id = Integer.valueOf(commandBlock.keyvalues.get("id"));
                item = CivSettings.marketItems.get(id);
                if (item != null) {
                    buildSign("buy", id, absCoord, item, commandBlock);
                }
                break;
            case "/buybig":
                id = Integer.valueOf(commandBlock.keyvalues.get("id"));
                item = CivSettings.marketItems.get(id);
                if (item != null) {
                    if (item.isStackable()) {
                        buildSign("buybig", id, absCoord, item, commandBlock);
                    }
                }
                break;
        }
    }


}
