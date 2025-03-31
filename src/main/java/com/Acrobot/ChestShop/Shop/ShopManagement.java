package com.Acrobot.ChestShop.Shop;

import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Chests.MinecraftChest;
import com.Acrobot.ChestShop.Config.Config;
import com.Acrobot.ChestShop.Config.Language;
import com.Acrobot.ChestShop.Config.Property;
import com.Acrobot.ChestShop.Data.ShopLocation;
import com.Acrobot.ChestShop.Data.Shops;
import com.Acrobot.ChestShop.Data.UUIDCache;
import com.Acrobot.ChestShop.Economy;
import com.Acrobot.ChestShop.Items.Items;
import com.Acrobot.ChestShop.Logging.Logging;
import com.Acrobot.ChestShop.Permission;
import com.Acrobot.ChestShop.Utils.uBlock;
import com.Acrobot.ChestShop.Utils.uLongName;
import com.Acrobot.ChestShop.Utils.uSign;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * @author Acrobot
 */
public class ShopManagement {
    public static void buy(Sign sign, Player player) {
        Chest chestMc = uBlock.findChest(sign);
        ItemStack item = Items.getItemStack(sign.getLine(3));
        if (item == null) {
            player.sendMessage(ChatColor.RED + "[Shop] The item is not recognised!");
            return;
        }
        Shop shop = new Shop(chestMc != null ? new MinecraftChest(chestMc) : null, true, sign, item);
        shop.buy(player);
    }

    public static void sell(Sign sign, Player player) {
        Chest chestMc = uBlock.findChest(sign);
        ItemStack item = Items.getItemStack(sign.getLine(3));
        if (item == null) {
            player.sendMessage(ChatColor.RED + "[Shop] The item is not recognised!");
            return;
        }
        Shop shop = new Shop(chestMc != null ? new MinecraftChest(chestMc) : null, false, sign, item);
        shop.sell(player);
    }

    public static void activate(Sign sign, Player player) {
        World world = sign.getWorld();
        float buyPrice = uSign.buyPrice(sign.getLine(2));
        String owner = sign.getLine(0);
        String playerName = player.getName();

        if (buyPrice == -1) {
            player.sendMessage(Config.getLocal(Language.NO_BUYING_HERE));
            return;
        }
        if (!Permission.has(player, Permission.BUY)) {
            player.sendMessage(Config.getLocal(Language.NO_PERMISSION));
            return;
        }

        if (!Economy.hasEnough(playerName, buyPrice, world)) {
            player.sendMessage(Config.getLocal(Language.NOT_ENOUGH_MONEY));
            return;
        }

        Block attachedBlock = sign.getBlock().getRelative(((org.bukkit.material.Sign) sign.getData()).getAttachedFace());

        if (!HasRedstoneTorch(attachedBlock)) {
            player.sendMessage(Config.getLocal(Language.SIGN_NOT_CONNECTED));
            return;
        }

        UUID uuid = UUIDCache.lookupUUID(uLongName.getName(owner));
        if (uuid != null) {
            Shops.put(new ShopLocation(sign.getBlock().getLocation()), uuid);
        }

        String account = getOwnerAccount(owner);
        if (!account.isEmpty() && Economy.hasAccount(account, world)) Economy.add(account, buyPrice, world);

        Economy.substract(playerName, buyPrice, world);

        String formattedPrice = Economy.formatBalance(buyPrice);
        if (Config.getBoolean(Property.SHOW_TRANSACTION_INFORMATION_CLIENT)) {
            player.sendMessage(Config.getLocal(Language.YOU_ACTIVATED_SIGN)
                    .replace("%owner", owner)
                    .replace("%price", formattedPrice));
        }

        Logging.logActivation(player, owner, buyPrice);

        if (Config.getBoolean(Property.SHOW_TRANSACTION_INFORMATION_OWNER)) {
            sendMessageToOwner(Config.getLocal(Language.SOMEBODY_ACTIVATED_YOUR_SIGN)
                    .replace("%buyer", playerName)
                    .replace("%price", formattedPrice), owner);
        }

        powerSingleAdjacent(attachedBlock);
    }

    private static void powerSingleAdjacent(Block block) {
        final BlockFace[] adjacentFaces = {
                BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN
        };

        for (BlockFace face : adjacentFaces) {
            Block adjacentBlock = block.getRelative(face);
            if (adjacentBlock.getType() == Material.REDSTONE_TORCH_ON){
                adjacentBlock.setType(Material.AIR);

                Bukkit.getScheduler().scheduleSyncDelayedTask(ChestShop.pm.getPlugin("ChestShop"), () -> {
                    ReplaceRedstoneTorch(adjacentBlock);
                }, 8);
                break;
            }
        }
    }

    private static boolean HasRedstoneTorch(Block block) {
        final BlockFace[] adjacentFaces = {
                BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN
        };

        for (BlockFace face : adjacentFaces) {
            Block adjacentBlock = block.getRelative(face);
            if (adjacentBlock.getType() == Material.REDSTONE_TORCH_ON) {
                return true;
            }
        }

        return false;
    }

    private static String getOwnerAccount(String owner) {
        return uSign.isAdminShop(owner) ? Config.getString(Property.SERVER_ECONOMY_ACCOUNT) : owner;
    }

    private static boolean isAdminShop(String owner) {
        return uSign.isAdminShop(owner);
    }

    private static void sendMessageToOwner(String msg, String owner) {
        if (!isAdminShop(owner)) {
            Player player = ChestShop.getBukkitServer().getPlayer(owner);
            if (player != null) {
                player.sendMessage(msg);
            }
        }
    }

    private static void ReplaceRedstoneTorch(Block block){
        if (block.getType() == Material.AIR){
            block.setType(Material.REDSTONE_TORCH_ON);
        }
    }
}
