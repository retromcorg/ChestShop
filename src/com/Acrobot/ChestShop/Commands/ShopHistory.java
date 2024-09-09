package com.Acrobot.ChestShop.Commands;

import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Config.Config;
import com.Acrobot.ChestShop.Config.Language;
import com.Acrobot.ChestShop.Config.Property;
import com.Acrobot.ChestShop.DB.Transaction;
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

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (Config.getBoolean(Property.LOG_TO_DATABASE) && Config.getBoolean(Property.LOG_TO_FILE)) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChestShop.chatPrefix + "This command is unavailable to console!");
                return true;
            }
            Player shopOwner = (Player) sender;

            EbeanServer db = ChestShop.getDB();
            List<Transaction> list;

            list = db.find(Transaction.class).where().eq("shop_owner", shopOwner.getName()).findList();
            Collections.reverse(list);

            if (list.size() == 0) {
                shopOwner.sendMessage(Config.getLocal(Language.NO_ENTRIES_IN_HISTORY));
                return true;
            }

            int page;

            if (args.length > 0) {
                try {
                    page = Integer.parseInt(args[0]);
                } catch (NumberFormatException e){
                    shopOwner.sendMessage(ChatColor.RED + Language.INVALID_USAGE.toString());
                    return true;
                }
            } else {
                page = 1;
            }

            double pageCount = Math.ceil((double) list.size() / 10);
            if (page > pageCount) {
                shopOwner.sendMessage((ChatColor.GOLD + Config.getLocal(Language.HISTORY_PAGE_X))
                        .replace("%page", String.valueOf(page))
                        .replace("%pagecount", String.valueOf(pageCount)));

                for (int i = page * 10 - 10;
                     (i < page * 10) && i < list.size() && list.get(i) != null; i++) {
                    Transaction entry = list.get(i);
                    String date = dateFormat.format(new Date(entry.getSec() * 1000L));
                    String message = (ChatColor.GOLD + date + ": " + ChatColor.WHITE +
                            (entry.isBuy() ? Config.getLocal(Language.SOMEBODY_SOLD_TO_YOUR_SHOP)
                                    : Config.getLocal(Language.SOMEBODY_BOUGHT_FROM_YOUR_SHOP))

                                    .replace("%seller", entry.getShopUser())
                                    .replace("%buyer", entry.getShopUser())
                                    .replace("%amount", String.valueOf(entry.getAmount()))
                                    .replace("%item", new ItemStack(entry.getItemID()).getType().name())
                                    .replace("%price", String.valueOf(entry.getPrice())));

                    shopOwner.sendMessage(message);
                }
            }
            else {

            }

            return true;
        } else {
            sender.sendMessage(ChatColor.RED + Language.PLEASE_SET_CONFIG_PARAMS_FOR_HISTORY.toString());
            return true;
        }
    }
}
