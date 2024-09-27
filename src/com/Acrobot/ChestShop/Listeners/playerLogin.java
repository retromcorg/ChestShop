package com.Acrobot.ChestShop.Listeners;

import com.Acrobot.ChestShop.ChestShop;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;

public class playerLogin extends PlayerListener {

    public void onPlayerLogin(PlayerLoginEvent event) {
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            return;
        }
        ChestShop.getUUIDCache().updatePlayerProfile(event.getPlayer().getName(), event.getPlayer().getUniqueId());
    }
}
