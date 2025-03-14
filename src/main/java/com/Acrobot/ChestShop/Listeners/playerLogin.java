package com.Acrobot.ChestShop.Listeners;

import com.Acrobot.ChestShop.Data.UUIDCache;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;

public class playerLogin extends PlayerListener {

    @Override
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (!event.getResult().equals(PlayerLoginEvent.Result.ALLOWED))
            return;

        UUIDCache.put(event.getPlayer().getUniqueId(), event.getPlayer().getName());
    }

}
