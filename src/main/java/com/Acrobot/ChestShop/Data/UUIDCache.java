package com.Acrobot.ChestShop.Data;

import com.Acrobot.ChestShop.ChestShop;
import com.google.gson.*;
import org.bukkit.Bukkit;

import java.io.*;
import java.util.HashMap;
import java.util.UUID;

/**
 * This class stores mappings of a player's {@link UUID} to their username
 * and can be used for lookup operations.
 *
 * @author zavdav
 */
public final class UUIDCache {

    private static final File cacheFile;
    private static final JsonObject jsonData;
    private static final Gson gson;

    private static final HashMap<UUID, String> lookupNameByUuid;
    private static final HashMap<String, UUID> lookupUuidByName;

    private UUIDCache() {}

    public static void initialize() {}

    static {
        cacheFile = new File(ChestShop.folder, "uuidcache.json");
        gson = new GsonBuilder().setPrettyPrinting().create();
        lookupNameByUuid = new HashMap<>();
        lookupUuidByName = new HashMap<>();

        if (!cacheFile.exists()) {
            jsonData = new JsonObject();
        } else {
            try {
                jsonData = (JsonObject) JsonParser.parseReader(new FileReader(cacheFile));
            } catch (JsonParseException e) {
                Bukkit.getLogger().severe(ChestShop.chatPrefix + "Failed to parse data in " + cacheFile.getName());
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
        Bukkit.getLogger().info(ChestShop.chatPrefix + "Populating UUID cache");

        jsonData.entrySet().forEach(it ->
                put(UUID.fromString(it.getKey()), it.getValue().getAsString())
        );
    }

    public static void put(UUID uuid, String username) {
        if (uuid == null || username == null) return;

        lookupNameByUuid.put(uuid, username);
        lookupUuidByName.put(username, uuid);
        jsonData.addProperty(uuid.toString(), username);
    }

    public static UUID lookupUUID(String username) {
        return username != null ? lookupUuidByName.get(username) : null;
    }

    public static String lookupUsername(UUID uuid) {
        return uuid != null ? lookupNameByUuid.get(uuid) : null;
    }

    public static void saveData(boolean async) {
        Bukkit.getLogger().info(ChestShop.chatPrefix + "Saving " + cacheFile.getName());
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
        try (FileWriter writer = new FileWriter(cacheFile)) {
            writer.write(jsonString);
        } catch (IOException e) {
            Bukkit.getLogger().warning(ChestShop.chatPrefix + "Failed to save data in " + cacheFile.getName());
        }
    }

}
