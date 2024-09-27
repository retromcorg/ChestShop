package com.Acrobot.ChestShop.Cache;

import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Utils.uLocation;
import com.Acrobot.ChestShop.Utils.uSign;
import net.minecraft.server.Packet130UpdateSign;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class ShopCache {
    private ChestShop plugin;
    private JSONObject jsonData;
    private File cacheFile;
    private boolean memoryOnly = false;
    private HashMap<uLocation, UUID> shopToUUIDMap = new HashMap<>();
    private HashMap<UUID, HashSet<uLocation>> uuidToShopsMap = new HashMap<>();

    public ShopCache(ChestShop plugin) {
        this.plugin = plugin;
        cacheFile = new File(plugin.getDataFolder() + File.separator + "cache" + File.separator + "ShopCache.json");
        if (!cacheFile.exists()) {
            cacheFile.getParentFile().mkdirs();
            try {
                FileWriter file = new FileWriter(cacheFile);
                plugin.getServer().getLogger().log(Level.INFO, ChestShop.chatPrefix + "Generating ShopCache.json", 1);
                jsonData = new JSONObject();
                file.write(jsonData.toJSONString());
                file.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            plugin.getServer().getLogger().log(Level.INFO, ChestShop.chatPrefix + "Reading ShopCache.json file", 1);
            JSONParser parser = new JSONParser();
            jsonData = (JSONObject) parser.parse(new FileReader(cacheFile));
        } catch (ParseException e) {
            plugin.getServer().getLogger().log(Level.WARNING, ChestShop.chatPrefix + "ShopCache.json file is corrupt, resetting file: " + e + " : " + e.getMessage());
            jsonData = new JSONObject();
        } catch (Exception e) {
            plugin.getServer().getLogger().log(Level.WARNING, ChestShop.chatPrefix + "ShopCache.json file is corrupt, changing to memory only mode.");
            memoryOnly = true;
            jsonData = new JSONObject();
        }
        saveData();

        //Generate in memory maps
        plugin.getServer().getLogger().log(Level.INFO, ChestShop.chatPrefix + "Generating in memory HashMaps for ShopCache.", 2);

        for (Object uuidObj : jsonData.keySet()) {
            UUID uuid = UUID.fromString(uuidObj.toString());
            uuidToShopsMap.put(uuid, new HashSet<>());
            JSONArray shopsArray = (JSONArray) jsonData.get(uuidObj.toString());
            for (Object loc : shopsArray) {
                uLocation uLoc = new uLocation(((String) loc).split(","));
                shopToUUIDMap.put(uLoc, uuid);
                uuidToShopsMap.get(uuid).add(uLoc);
            }
        }
    }

    public String getUsernameFromShop(uLocation uLoc) {
        return ChestShop.getUUIDCache().getUsernameFromUUID(shopToUUIDMap.get(uLoc));
    }

    public HashSet<uLocation> getPlayerShops(UUID uuid) {
        return uuidToShopsMap.getOrDefault(uuid, null);
    }

    public void addPlayerShop(UUID uuid, Location location) {
        if (uuid == null || location == null) return;
        uLocation loc = new uLocation(location);
        if (!shopToUUIDMap.containsKey(loc)) {
            shopToUUIDMap.put(loc, uuid);
            if (!uuidToShopsMap.containsKey(uuid)) {
                uuidToShopsMap.put(uuid, new HashSet<>());
            }
            uuidToShopsMap.get(uuid).add(loc);
            JSONArray shopsArray = jsonData.get(uuid.toString()) != null ? (JSONArray) jsonData.get(uuid.toString()) : new JSONArray();
            shopsArray.add(loc.toString());
            jsonData.put(uuid.toString(), shopsArray);

        }
    }

    public void removePlayerShop(Location location) {
        if (location == null) return;
        uLocation loc = new uLocation(location);
        if (shopToUUIDMap.containsKey(loc)) {
            UUID uuid = shopToUUIDMap.get(loc);
            uuidToShopsMap.get(uuid).remove(loc);
            shopToUUIDMap.remove(loc);
            JSONArray shopsArray = (JSONArray) jsonData.get(uuid.toString());
            shopsArray.remove(loc.toString());
            if (shopsArray.isEmpty()) {
                jsonData.remove(uuid.toString());
            } else {
                jsonData.put(uuid.toString(), shopsArray);
            }
        }
    }

    public void updatePlayerShops(UUID uuid, String username) {
        if (username == null || username.isEmpty()) return;
        for (uLocation uLoc : getPlayerShops(uuid)) {
            Block block = Bukkit.getWorld(uLoc.getWorld()).getBlockAt(uLoc.getX(), uLoc.getY(), uLoc.getZ());
            if (uSign.isSign(block) && uSign.isValid((Sign) block.getState())) {
                Sign sign = (Sign) block.getState();
                sign.setLine(0, username);
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendPacket(player, new Packet130UpdateSign(uLoc.getX(), uLoc.getY(), uLoc.getZ(), sign.getLines()));
                }
            }
        }
    }

    public void saveData() {
        if (memoryOnly) return;
        try (FileWriter file = new FileWriter(cacheFile)) {
            plugin.getServer().getLogger().log(Level.INFO, ChestShop.chatPrefix + "Saving ShopCache.json", 1);
            file.write(jsonData.toJSONString());
            file.flush();
        } catch (IOException e) {
            plugin.getServer().getLogger().log(Level.WARNING, ChestShop.chatPrefix + "Error saving ShopCache.json: " + e + " : " + e.getMessage());
        }
    }
}
