package com.Acrobot.ChestShop.Utils;

import org.bukkit.Location;

import java.util.Objects;

public class uLocation {
    private String world;
    private int x;
    private int y;
    private int z;

    public uLocation(Location location) {
        this(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public uLocation(String world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public uLocation(String[] strings) {
        this(strings[0],
            Integer.parseInt(strings[1]),
            Integer.parseInt(strings[2]),
            Integer.parseInt(strings[3]));
    }

    public String getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public String toString() {
        return String.format("%s,%d,%d,%d", world, x, y, z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        uLocation uLocation = (uLocation) o;
        return world.equals(uLocation.world) && x == uLocation.x && y == uLocation.y && z == uLocation.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, y, z);
    }
}
