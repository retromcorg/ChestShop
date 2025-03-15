package com.Acrobot.ChestShop.Data;

import org.bukkit.Location;

import java.util.Objects;

/**
 * This class represents the location of a shop's sign.
 *
 * @author zavdav
 */
public class ShopLocation {

    public final String world;
    public final int x;
    public final int y;
    public final int z;

    public ShopLocation(String world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public ShopLocation(Location location) {
        this(location.getWorld().getName(),
             location.getBlockX(),
             location.getBlockY(),
             location.getBlockZ()
        );
    }

    public ShopLocation(String[] strings) {
        this(strings[0],
             Integer.parseInt(strings[1]),
             Integer.parseInt(strings[2]),
             Integer.parseInt(strings[3])
        );
    }

    @Override
    public String toString() {
        return String.format("%s,%d,%d,%d", world, x, y, z);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ShopLocation)) return false;

        ShopLocation other = (ShopLocation) obj;
        return world.equals(other.world) && x == other.x && y == other.y && z == other.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, y, z);
    }
}
