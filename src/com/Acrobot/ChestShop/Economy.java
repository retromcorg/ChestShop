package com.Acrobot.ChestShop;

import com.Acrobot.ChestShop.Utils.uLongName;
import com.LRFLEW.register.payment.forChestShop.Method;
import org.bukkit.World;

/**
 * @author Acrobot
 *         Economy management
 */
public class Economy {
    public static Method economy;

    public static boolean hasAccount(String p, World world) {
        return economy.hasAccount(uLongName.getName(p), world);
    }

    public static void add(String name, float amount, World world) {
        economy.getAccount(uLongName.getName(name), world).add(amount, world);
    }

    public static void substract(String name, float amount, World world) {
        economy.getAccount(uLongName.getName(name), world).subtract(amount, world);
    }

    public static boolean hasEnough(String name, float amount, World world) {
        return economy.getAccount(uLongName.getName(name), world).hasEnough(amount, world);
    }

    public static double balance(String name, World world) {
        return economy.getAccount(uLongName.getName(name), world).balance(world);
    }

    public static String formatBalance(double amount) {
        return economy.format(amount);
    }
}
