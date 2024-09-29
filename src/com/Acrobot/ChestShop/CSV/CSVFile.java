package com.Acrobot.ChestShop.CSV;

import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.DB.Transaction;
import com.Acrobot.ChestShop.Logging.Logging;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CSVFile {

    private File csvFile;
    private UUID uuid;

    public CSVFile(UUID uuid) {
        this.uuid = uuid;
        csvFile = new File(ChestShop.folder + File.separator + "csv" + File.separator + uuid.toString() + ".csv");
        if (!csvFile.exists()) {
            csvFile.getParentFile().mkdirs();
            try { csvFile.createNewFile(); }
            catch (IOException e) {
                Logging.log("Failed to create csv file for user " + uuid);
            }
        }
    }

    public void addTransaction(Transaction tr) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile.getPath(), true))) {
            writer.append("", 0, 0);
            String csvString = String.format("%s,%s,%s,%s,%s,%s,%s",
                    tr.getShopUser().toString(),
                    tr.isBuy(),
                    tr.getItemID(),
                    tr.getItemDurability(),
                    tr.getAmount(),
                    tr.getPrice(),
                    tr.getSec());
            writer.write(csvString);
            writer.newLine();
        } catch (Exception e) {
            Logging.log("Failed to add transaction to user " + uuid);
            e.printStackTrace();
        }
    }
    //shop_user,buy,item_id,item_durability,amount,price,sec
    public ArrayList<Transaction> getTransactions(int entries) {
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile.getPath()))) {
            ArrayList<Transaction> transactions = new ArrayList<>();
            List<String> lines = reader.lines().collect(Collectors.toList());
            Collections.reverse(lines);

            for (int i = 0; i < entries && i < lines.size(); i++) {
                String[] strings = lines.get(i).split(",");
                Transaction tr = new Transaction();
                tr.setShopUser(UUID.fromString(strings[0]));
                tr.setBuy(Boolean.parseBoolean(strings[1]));
                tr.setItemID(Integer.parseInt(strings[2]));
                tr.setItemDurability(Integer.parseInt(strings[3]));
                tr.setAmount(Integer.parseInt(strings[4]));
                tr.setPrice(Float.parseFloat(strings[5]));
                tr.setSec(Long.parseLong(strings[6]));
                transactions.add(tr);
            }
            return transactions;
        } catch (Exception e) {
            Logging.log("Could not get transactions of shops owned by " + uuid);
            throw new RuntimeException(e);
        }
    }

    public static boolean exists(UUID uuid) {
        return new File(ChestShop.folder + File.separator + "csv" + File.separator + uuid.toString() + ".csv").exists();
    }
}
