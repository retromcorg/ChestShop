package com.Acrobot.ChestShop.Data;

import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Utils.uLongName;
import com.Acrobot.ChestShop.Utils.uSign;
import com.google.gson.*;
import net.minecraft.server.Packet130UpdateSign;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

/**
 * This class stores mappings of a {@link ShopLocation} to the owning player's {@link UUID},
 * and can be used for lookup operations and for updating the username on a player's shop signs.
 *
 * @author zavdav
 */
public final class Shops {

    private static final File shopsFile;
    private static final JsonObject jsonData;
    private static final Gson gson;

    private static final HashMap<ShopLocation, UUID> lookupUuidByShop;
    private static final HashMap<UUID, HashSet<ShopLocation>> lookupShopsByUuid;

    private Shops() {}

    public static void initialize() {}

    static {
        shopsFile = new File(ChestShop.folder, "shops.json");
        gson = new GsonBuilder().setPrettyPrinting().create();
        lookupShopsByUuid = new HashMap<>();
        lookupUuidByShop = new HashMap<>();

        if (!shopsFile.exists()) {
            jsonData = new JsonObject();
        } else {
            try {
                jsonData = (JsonObject) JsonParser.parseReader(new FileReader(shopsFile));
            } catch (JsonParseException e) {
                Bukkit.getLogger().severe(ChestShop.chatPrefix + "Failed to parse data in " + shopsFile.getName());
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        populate();

        Bukkit.getScheduler().scheduleSyncRepeatingTask(
                ChestShop.pm.getPlugin("ChestShop"),
                () -> saveData(true), 600 * 20, 600 * 20
        );
    }

    private static void populate() {
        jsonData.entrySet().forEach(it -> put(
                new ShopLocation(it.getKey().split(",", 4)),
                UUID.fromString(it.getValue().getAsString())
        ));
    }

    public static void put(ShopLocation shopLocation, UUID uuid) {
        if (uuid == null || shopLocation == null) return;

        lookupUuidByShop.put(shopLocation, uuid);
        HashSet<ShopLocation> shops = lookupShopsByUuid.computeIfAbsent(uuid, k -> new HashSet<>());
        shops.add(shopLocation);
        jsonData.addProperty(shopLocation.toString(), uuid.toString());
    }

    public static void remove(ShopLocation shopLocation) {
        lookupUuidByShop.remove(shopLocation);
        lookupShopsByUuid.values().forEach(it -> it.removeIf(shopLocation::equals));
        jsonData.remove(shopLocation.toString());
    }

    public static void updateUsername(UUID uuid, String newUsername) {
        if (lookupShopsByUuid.get(uuid) == null) return;
        uLongName.saveName(newUsername);

        lookupShopsByUuid.get(uuid).forEach(it -> {
            Block block = Bukkit.getWorld(it.world).getBlockAt(it.x, it.y, it.z);
            if (!uSign.isSign(block) || !uSign.isValid((Sign) block.getState()))
                return;

            Sign sign = (Sign) block.getState();
            sign.setLine(0, newUsername);

            Arrays.stream(Bukkit.getOnlinePlayers()).forEach(player ->
                    player.sendPacket(player, new Packet130UpdateSign(it.x, it.y, it.z, sign.getLines()))
            );
        });
    }

    public UUID lookupUUID(ShopLocation shopLocation) {
        return shopLocation != null ? lookupUuidByShop.get(shopLocation) : null;
    }

    public HashSet<ShopLocation> lookupShops(UUID uuid) {
        return uuid != null ? lookupShopsByUuid.get(uuid) : null;
    }

    public static void saveData(boolean async) {
        Bukkit.getLogger().info(ChestShop.chatPrefix + "Saving " + shopsFile.getName());
        String jsonString = gson.toJson(jsonData);

        if (async) {
            Bukkit.getScheduler().scheduleAsyncDelayedTask(
                    ChestShop.pm.getPlugin("ChestShop"), () -> writeData(jsonString)
            );
        } else {
            writeData(jsonString);
        }
    }

    private static void writeData(String jsonString) {
        try (FileWriter writer = new FileWriter(shopsFile)) {
            writer.write(jsonString);
        } catch (IOException e) {
            Bukkit.getLogger().warning(ChestShop.chatPrefix + "Failed to save data in " + shopsFile.getName());
        }
    }

}
