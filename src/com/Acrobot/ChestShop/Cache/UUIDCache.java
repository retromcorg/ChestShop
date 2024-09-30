package com.Acrobot.ChestShop.Cache;

import com.Acrobot.ChestShop.ChestShop;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

public class UUIDCache {
    private ChestShop plugin;
    private JSONObject jsonData;
    private File cacheFile;
    private boolean memoryOnly = false;
    private HashMap<UUID, String> uuidToUsernameMap = new HashMap<>();
    private HashMap<String, UUID> usernameToUUIDMap = new HashMap<>();

    public UUIDCache(ChestShop plugin) {
        this.plugin = plugin;
        cacheFile = new File(plugin.getDataFolder() + File.separator + "cache" + File.separator + "UUIDCache.json");
        if (!cacheFile.exists()) {
            cacheFile.getParentFile().mkdirs();
            try {
                FileWriter file = new FileWriter(cacheFile);
                plugin.getServer().getLogger().log(Level.INFO, ChestShop.chatPrefix + "Generating UUIDCache.json", 1);
                jsonData = new JSONObject();
                file.write(jsonData.toJSONString());
                file.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            plugin.getServer().getLogger().log(Level.INFO, ChestShop.chatPrefix + "Reading UUIDCache.json file", 1);
            JSONParser parser = new JSONParser();
            jsonData = (JSONObject) parser.parse(new FileReader(cacheFile));
        } catch (ParseException e) {
            plugin.getServer().getLogger().log(Level.WARNING, ChestShop.chatPrefix + "UUIDCache.json file is corrupt, resetting file: " + e + " : " + e.getMessage());
            jsonData = new JSONObject();
        } catch (Exception e) {
            plugin.getServer().getLogger().log(Level.WARNING, ChestShop.chatPrefix + "UUIDCache.json file is corrupt, changing to memory only mode.");
            memoryOnly = true;
            jsonData = new JSONObject();
        }
        saveData();

        //Generate in memory maps
        plugin.getServer().getLogger().log(Level.INFO, "Generating in memory HashMaps for UUIDCache.", 2);

        for (Object uuid : jsonData.keySet()) {
            String playerUsername = (String) ((JSONObject) jsonData.get(uuid)).get("username");
            UUID playerUUID = UUID.fromString((String) uuid);
            uuidToUsernameMap.put(playerUUID, playerUsername);
            usernameToUUIDMap.put(playerUsername.toLowerCase(), playerUUID);
        }
    }

    public void updatePlayerProfile(String username, UUID uuid) {
        boolean updateShops = false;
        JSONObject userEntry = (JSONObject) jsonData.getOrDefault(uuid.toString(), new JSONObject());
        if (userEntry.containsKey("username") && !userEntry.get("username").toString().equals(username)) {
            updateShops = true;
        }
        userEntry.put("username", username);
        jsonData.put(uuid.toString(), userEntry);

        uuidToUsernameMap.put(uuid, username);
        usernameToUUIDMap.put(username.toLowerCase(), uuid);
        if (updateShops) ChestShop.getShopCache().updatePlayerShops(uuid, username);
    }

    public UUID getUUIDFromPartialUsername(String partialUsername) {
        for (String key : usernameToUUIDMap.keySet()) {
            if (key.toLowerCase().startsWith(partialUsername.toLowerCase())) {
                return usernameToUUIDMap.get(key);
            }
        }
        return null;
    }

    public String get16CharacterName(String shortName) {
        for (String value: uuidToUsernameMap.values()) {
            if (value.startsWith(shortName) && value.length() == 16) {
                return value;
            }
        }
        return shortName;
    }

    public UUID getUUIDFromUsername(String username) {
        return usernameToUUIDMap.getOrDefault(username.toLowerCase(), null);
    }

    public String getUsernameFromUUID(UUID uuid) {
        return uuidToUsernameMap.getOrDefault(uuid, null);
    }

    public boolean isPlayerKnown(UUID uuid) {
        return uuidToUsernameMap.containsKey(uuid);
    }

    public void saveData() {
        saveJsonArray();
    }

    private void saveJsonArray() {
        if (memoryOnly) {
            return;
        }
        try (FileWriter file = new FileWriter(cacheFile)) {
            plugin.getServer().getLogger().log(Level.INFO, ChestShop.chatPrefix + "Saving UUIDCache.json", 1);
            file.write(jsonData.toJSONString());
            file.flush();
        } catch (IOException e) {
            plugin.getServer().getLogger().log(Level.WARNING, ChestShop.chatPrefix + "Error saving UUIDCache.json: " + e + " : " + e.getMessage());
        }
    }

}
