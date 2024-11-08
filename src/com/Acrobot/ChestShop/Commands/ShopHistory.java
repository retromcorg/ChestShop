package com.Acrobot.ChestShop.Commands;

import com.Acrobot.ChestShop.CSV.CSVFile;
import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Config.Config;
import com.Acrobot.ChestShop.Config.Language;
import com.Acrobot.ChestShop.Config.Property;
import com.Acrobot.ChestShop.DB.Transaction;
import com.Acrobot.ChestShop.Economy;
import com.Acrobot.ChestShop.Permission;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ShopHistory implements CommandExecutor {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!Config.getBoolean(Property.LOG_TO_CSV)) {
            if (sender.isOp() || sender instanceof Player && Permission.has((Player) sender, Permission.MOD)) {
                sender.sendMessage(ChatColor.RED + Language.HISTORY_DISABLED.toString());
            }
            return true;
        }
        if (!(sender instanceof Player) && args.length == 0) {
            sender.sendMessage("Invalid usage, use /shophistory <username>");
            return true;
        }

        String playerName = sender.getName();
        int page = 1;

        if (args.length > 0) {
            try { page = Integer.parseInt(args[0]); }
            catch (NumberFormatException e) {
                if (sender.isOp() || sender instanceof Player && Permission.has((Player) sender, Permission.MOD)) {
                    if (!args[0].matches("[a-zA-Z0-9_]{3,16}")) {
                        sender.sendMessage(ChatColor.RED + Language.INVALID_USAGE_ADMIN.toString());
                        return true;
                    }
                    playerName = args[0];
                    if (args.length > 1) {
                        try { page = Integer.parseInt(args[1]); }
                        catch (NumberFormatException ignored) {}
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + Language.INVALID_USAGE.toString());
                    return true;
                }
            }
        }

        if (page < 1) {
            sender.sendMessage(ChatColor.RED + Language.INVALID_PAGE_NUMBER.toString());
            sender.sendMessage(ChatColor.RED + Language.INVALID_USAGE.toString());
            return true;
        }

        UUID uuid = ChestShop.getUUIDCache().getUUIDFromUsername(playerName);
        if (uuid == null) {
            sender.sendMessage(ChatColor.RED + Language.PLAYER_NOT_FOUND.toString().replace("%player%", playerName));
            sender.sendMessage(ChatColor.RED + Language.INVALID_USAGE_ADMIN.toString());
            return true;
        }
        playerName = ChestShop.getUUIDCache().getUsernameFromUUID(uuid);

        List<Transaction> list;
        if (!CSVFile.exists(uuid) || (list = new CSVFile(uuid).getTransactions()).isEmpty()) {
            sender.sendMessage(Config.getLocal(Language.NO_ENTRIES_IN_HISTORY));
            return true;
        }

        int pageCount = (int) Math.ceil((double) list.size() / 10);
        if (page <= pageCount) {
            sender.sendMessage(Config.getLocal(Language.HISTORY_PAGE_X)
                    .replace("%page%", String.valueOf(page))
                    .replace("%pagecount%", String.valueOf(pageCount)));

            for (int i = page * 10 - 10; (i < page * 10) && i < list.size() && list.get(i) != null; i++) {
                Transaction entry = list.get(i);
                if (i == page * 10 - 10 || entry.getSec() < list.get(i - 1).getSec()) {
                    String date = dateFormat.format(new Date(entry.getSec() * 1000L));
                    sender.sendMessage(ChatColor.GOLD + date + ":");
                }
                String message;

                if (entry.getItemID() == 0 && entry.getItemDurability() == 0 && entry.getAmount() == 0) {
                    message = Config.getLocal(Language.SOMEBODY_ACTIVATED_YOUR_SIGN)
                            .replace("%buyer", ChestShop.getUUIDCache().getUsernameFromUUID(entry.getShopUser()))
                            .replace("%price", Economy.formatBalance(entry.getPrice()));
                    message = !playerName.equalsIgnoreCase(sender.getName()) ?
                            message.replace("%your", playerName + "'s") : message.replace("%your", "your");
                } else {
                    message = (entry.isBuy() ? Config.getLocal(Language.SOMEBODY_BOUGHT_FROM_YOUR_SHOP) : Config.getLocal(Language.SOMEBODY_SOLD_TO_YOUR_SHOP))
                            .replace("%seller", ChestShop.getUUIDCache().getUsernameFromUUID(entry.getShopUser()))
                            .replace("%buyer", ChestShop.getUUIDCache().getUsernameFromUUID(entry.getShopUser()))
                            .replace("%amount", String.valueOf(entry.getAmount()))
                            .replace("%item", new ItemStack(entry.getItemID()).getType().name())
                            .replace("%price", Economy.formatBalance(entry.getPrice()));
                    message = !playerName.equalsIgnoreCase(sender.getName()) ?
                            message.replace("%you", playerName) : message.replace("%you", "you");
                }

                sender.sendMessage(message);
            }
        }
        else {
            sender.sendMessage(ChatColor.RED + Language.INVALID_PAGE_NUMBER.toString());
            sender.sendMessage(ChatColor.RED + Language.INVALID_USAGE.toString());
        }
        return true;
    }
}
