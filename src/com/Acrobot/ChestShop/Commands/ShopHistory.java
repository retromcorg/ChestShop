package com.Acrobot.ChestShop.Commands;

import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Config.Config;
import com.Acrobot.ChestShop.Config.Language;
import com.Acrobot.ChestShop.Config.Property;
import com.Acrobot.ChestShop.DB.Transaction;
import com.Acrobot.ChestShop.Permission;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import com.avaje.ebean.EbeanServer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ShopHistory implements CommandExecutor {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (Config.getBoolean(Property.LOG_TO_DATABASE) && Config.getBoolean(Property.LOG_TO_FILE)) {
            if (!(sender instanceof Player) && args.length == 0) {
                sender.sendMessage(ChestShop.chatPrefix + "This command is unavailable to console!");
                return true;
            }
            int page = 1;
            String playerName = sender.getName();

            if (sender.isOp() || (sender instanceof Player && Permission.has((Player) sender, Permission.USER_VIEW_SHOP_HISTORY))) {
                if (args.length > 0) {
                    try {
                        page = Integer.parseInt(args[0]);
                    } catch (NumberFormatException e) {
                        if (!args[0].matches("[a-zA-Z0-9_]{3,16}")) {
                            sender.sendMessage(ChatColor.RED + Language.INVALID_USAGE_ADMIN.toString());
                            return true;
                        }
                        playerName = args[0];
                        if (args.length > 1) {
                            try {
                                page = Integer.parseInt(args[1]);
                            } catch (NumberFormatException f){
                                page = 1;
                            }
                        }
                    }
                }
            } else {
                if (args.length > 0) {
                    try {
                        page = Integer.parseInt(args[0]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + Language.INVALID_USAGE.toString());
                        return true;
                    }
                }
            }

            EbeanServer db = ChestShop.getDB();
            List<Transaction> list;
            list = db.find(Transaction.class).where().eq("lower(shop_owner)", playerName.toLowerCase()).findList();
            Collections.reverse(list);

            if (list.size() == 0) {
                sender.sendMessage(Config.getLocal(Language.NO_ENTRIES_IN_HISTORY));
                return true;
            }

            int pageCount = (int) Math.ceil((double) list.size() / 10);
            if (page <= pageCount) {
                sender.sendMessage(Config.getLocal(Language.HISTORY_PAGE_X)
                        .replace("%page%", String.valueOf(page))
                        .replace("%pagecount%", String.valueOf(pageCount)));

                for (int i = page * 10 - 10;
                     (i < page * 10) && i < list.size() && list.get(i) != null; i++) {
                    Transaction entry = list.get(i);
                    if (i == page * 10 - 10 || entry.getSec() < list.get(i - 1).getSec()) {
                        String date = dateFormat.format(new Date(entry.getSec() * 1000L));
                        sender.sendMessage(ChatColor.GOLD + date + ":");
                    }
                    String message =
                            (entry.isBuy() ? Config.getLocal(Language.SOMEBODY_BOUGHT_FROM_YOUR_SHOP)
                                    : Config.getLocal(Language.SOMEBODY_SOLD_TO_YOUR_SHOP))

                                    .replace("%seller", entry.getShopUser())
                                    .replace("%buyer", entry.getShopUser())
                                    .replace("%amount", String.valueOf(entry.getAmount()))
                                    .replace("%item", new ItemStack(entry.getItemID()).getType().name())
                                    .replace("%price", "$" + entry.getPrice());

                    sender.sendMessage(message);
                }
            }
            return true;
        } else {
            sender.sendMessage(ChatColor.RED + Language.PLEASE_SET_CONFIG_PARAMS_FOR_HISTORY.toString());
            return true;
        }
    }
}
