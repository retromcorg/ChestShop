package com.Acrobot.ChestShop.Listeners;

import com.Acrobot.ChestShop.Config.Config;
import com.Acrobot.ChestShop.Config.Language;
import com.Acrobot.ChestShop.Config.Property;
import com.Acrobot.ChestShop.Data.ShopLocation;
import com.Acrobot.ChestShop.Data.Shops;
import com.Acrobot.ChestShop.Permission;
import com.Acrobot.ChestShop.Protection.Plugins.Default;
import com.Acrobot.ChestShop.Shop.ShopManagement;
import com.Acrobot.ChestShop.Signs.restrictedSign;
import com.Acrobot.ChestShop.Utils.uBlock;
import com.Acrobot.ChestShop.Utils.uLongName;
import com.Acrobot.ChestShop.Utils.uSign;
import net.minecraft.server.IInventory;
import net.minecraft.server.InventoryLargeChest;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

import java.util.HashMap;

/**
 * @author Acrobot
 */
public class playerInteract extends PlayerListener {

    private static final HashMap<Player, Long> lastTransactionTimeChestShop = new HashMap<Player, Long>(); //Last player's transaction
    private static final int intervalChestShop = 100; //Minimal interval between transactions
    private static final HashMap<Player, Long> lastTransactionTimeRedstoneSign = new HashMap<Player, Long>();
    private static final int intervalRedstoneSign = 800;


    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        Player player = event.getPlayer();

        if (Config.getBoolean(Property.USE_BUILT_IN_PROTECTION) && block.getType() == Material.CHEST) {
            Default protection = new Default();
            if (!hasAdminPermissions(player) && (protection.isProtected(block) && !protection.canAccess(player, block))) {
                player.sendMessage(Config.getLocal(Language.ACCESS_DENIED));
                event.setCancelled(true);
                return;
            }
        }

        if (!uSign.isSign(block)) return; //It's not a sign!
        Sign sign = (Sign) block.getState();

        if (!uSign.isValid(sign) || player.isSneaking()) return;

        if (uSign.isRedstoneSign(sign)) {
            if (!enoughTimeHasPassedRedstoneSign(player)) {
                return;
            }
            else{
                lastTransactionTimeRedstoneSign.put(player, System.currentTimeMillis());
            }
        }
        else{
            if (!enoughTimeHasPassedChestShop(player)) {
                return;
            }
            else{
                lastTransactionTimeChestShop.put(player, System.currentTimeMillis());
            }
        }

        if (action == Action.RIGHT_CLICK_BLOCK) event.setCancelled(true);

        if (!uSign.isRedstoneSign(sign) && uLongName.stripName(player.getName()).equals(sign.getLine(0)) && (action != Action.LEFT_CLICK_BLOCK || !Config.getBoolean(Property.ALLOW_LEFT_CLICK_DESTROYING))) {
            showChestGUI(player, block);
            return;
        }

        if (restrictedSign.isRestrictedShop(sign) && !restrictedSign.canAccess(sign, player)) {
            player.sendMessage(Config.getLocal(Language.ACCESS_DENIED));
            return;
        }

        Action buy = (Config.getBoolean(Property.REVERSE_BUTTONS) ? Action.LEFT_CLICK_BLOCK : Action.RIGHT_CLICK_BLOCK);

        if (uSign.isRedstoneSign(sign)){
            if (action == buy) {
                ShopManagement.activate(sign, player);
            }
        }
        else if (action == buy) {
            ShopManagement.buy(sign, player);
        } else {
            ShopManagement.sell(sign, player);
        }
    }

    private static boolean enoughTimeHasPassedChestShop(Player player) {
        return !lastTransactionTimeChestShop.containsKey(player) || (System.currentTimeMillis() - lastTransactionTimeChestShop.get(player)) >= intervalChestShop;
    }

    private static boolean enoughTimeHasPassedRedstoneSign(Player player) {
        return !lastTransactionTimeRedstoneSign.containsKey(player) || (System.currentTimeMillis() - lastTransactionTimeRedstoneSign.get(player)) >= intervalRedstoneSign;
    }

    private static boolean hasAdminPermissions(Player player) {
        return Permission.has(player, Permission.ADMIN) || Permission.has(player, Permission.MOD);
    }

    private static void showChestGUI(Player player, Block block) {
        Chest chest = uBlock.findChest(block);
        if (chest == null) { //Sorry, no chest found
            player.sendMessage(Config.getLocal(Language.NO_CHEST_DETECTED));
            return;
        }

        IInventory inventory = ((CraftInventory) chest.getInventory()).getInventory();
        chest = uBlock.findNeighbor(chest);

        if (chest != null) { //There is also a neighbor chest
            inventory = new InventoryLargeChest(player.getName() + "'s Shop", inventory, ((CraftInventory) chest.getInventory()).getInventory());
        }

        Shops.put(new ShopLocation(block.getLocation()), player.getUniqueId());
        ((CraftPlayer) player).getHandle().a(inventory); //Show inventory on the screen
    }
}
