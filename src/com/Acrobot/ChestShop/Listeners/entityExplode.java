package com.Acrobot.ChestShop.Listeners;

import com.Acrobot.ChestShop.ChestShop;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;
import com.Acrobot.ChestShop.Utils.uSign;

/**
 * @author Acrobot
 */
public class entityExplode extends EntityListener {
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled() || event.blockList() == null) return;
        for (Block block : event.blockList()) {
            if (blockBreak.cancellingBlockBreak(block, null)) {
                event.setCancelled(true);
                return;
            }
            if (uSign.isSign(block) && uSign.isValid((Sign) block.getState())) {
                ChestShop.getShopCache().removePlayerShop(block.getLocation());
            }
        }
    }
}
