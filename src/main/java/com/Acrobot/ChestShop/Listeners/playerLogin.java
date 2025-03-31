package com.Acrobot.ChestShop.Listeners;

import com.Acrobot.ChestShop.Data.Shops;
import com.Acrobot.ChestShop.Data.UUIDCache;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.UUID;

public class playerLogin extends PlayerListener {

    @Override
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (!event.getResult().equals(PlayerLoginEvent.Result.ALLOWED))
            return;

        UUID uuid = event.getPlayer().getUniqueId();
        String newName = event.getPlayer().getName();
        String prevName = UUIDCache.lookupUsername(uuid);

        UUIDCache.put(uuid, newName);
        if (prevName != null && !newName.equals(prevName)) {
            Shops.updateUsername(uuid, newName);
        }
    }

}
