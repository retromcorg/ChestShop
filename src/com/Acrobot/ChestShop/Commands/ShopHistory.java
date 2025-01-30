package com.Acrobot.ChestShop.Commands;

import com.Acrobot.ChestShop.CSV.CSVFile;
import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Config.Config;
import com.Acrobot.ChestShop.Config.Language;
import com.Acrobot.ChestShop.Config.Property;
import com.Acrobot.ChestShop.DB.Transaction;
import com.Acrobot.ChestShop.Economy;
import com.Acrobot.ChestShop.Permission;
import org.apache.commons.lang3.tuple.Pair;
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
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c" + Language.PLAYER_ONLY);
            return true;
        }

        Player player = (Player) sender;
        if (!Config.getBoolean(Property.LOG_TO_CSV)) {
            if (Permission.has(player, Permission.MOD)) {
                sender.sendMessage("§c" + Language.HISTORY_DISABLED);
            }
            return true;
        }

        Pair<String, Integer> pair = parseArgs(player, args);
        String name = pair.getLeft();
        int page = pair.getRight();

        UUID uuid = ChestShop.getUUIDCache().getUUIDFromUsername(name);
        if (uuid == null) {
            sender.sendMessage(("§c" + Language.PLAYER_NOT_FOUND).replace("%player%", name));
            return true;
        }
        name = ChestShop.getUUIDCache().getUsernameFromUUID(uuid);

        List<Transaction> list;
        if (!CSVFile.exists(uuid) || (list = new CSVFile(uuid).getTransactions()).isEmpty()) {
            sender.sendMessage(Config.getLocal(Language.HISTORY_EMPTY));
            return true;
        }

        int pages = (int) Math.ceil((double) list.size() / 10);
        if (page > pages) {
            sender.sendMessage("§c" + Language.INVALID_PAGE);
            return true;
        }

        printPage(player, list, name, page, pages);
        return true;
    }

    private Pair<String, Integer> parseArgs(Player player, String[] args) {
        String name = player.getName();
        int page = 1;

        switch (args.length) {
            case 0: break;
            case 1:
                try {
                    int parsed = Integer.parseInt(args[0]);
                    if (parsed > 0) page = parsed;
                } catch (NumberFormatException ignored) {}
                break;
            default:
                if (!Permission.has(player, Permission.MOD)) break;
                name = args[0];
                try {
                    int parsed = Integer.parseInt(args[1]);
                    if (parsed > 0) page = parsed;
                } catch (NumberFormatException ignored) {}
        }
        return Pair.of(name, page);
    }

    private void printPage(Player player, List<Transaction> list, String name, int page, int pages) {
        player.sendMessage(Config.getLocal(Language.HISTORY_PAGE)
                .replace("%page%", String.valueOf(page))
                .replace("%pages%", String.valueOf(pages)));

        int start = page * 10 - 10;
        for (int i = start; i < page * 10; i++) {
            if (i >= list.size()) break;
            Transaction entry = list.get(i);

            if (i == start || entry.getSec() < list.get(i - 1).getSec()) {
                String date = dateFormat.format(new Date(entry.getSec() * 1000L));
                player.sendMessage("§6" + date + ":");
            }

            String line;
            String shopUser = ChestShop.getUUIDCache().getUsernameFromUUID(entry.getShopUser());
            boolean isRedstoneSign =
                    entry.getItemID() == 0 &&
                    entry.getItemDurability() == 0 &&
                    entry.getAmount() == 0;

            if (isRedstoneSign) {
                line = Config.getLocal(Language.SOMEBODY_ACTIVATED_YOUR_SIGN)
                        .replace("%player", name.equalsIgnoreCase(player.getName()) ? "your" : name + "'s");
            }  else {
                line = Config.getLocal(entry.isBuy() ?
                        Language.SOMEBODY_BOUGHT_FROM_YOUR_SHOP :
                        Language.SOMEBODY_SOLD_TO_YOUR_SHOP);
            }

            line = line
                    .replace("%seller", shopUser)
                    .replace("%buyer", shopUser)
                    .replace("%amount", String.valueOf(entry.getAmount()))
                    .replace("%item", new ItemStack(entry.getItemID()).getType().name())
                    .replace("%price", Economy.formatBalance(entry.getPrice()))
                    .replace("%player", name.equalsIgnoreCase(player.getName()) ? "you" : name);

            player.sendMessage(line);
        }
    }
}