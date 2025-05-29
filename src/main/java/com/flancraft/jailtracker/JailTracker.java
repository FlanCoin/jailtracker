package com.flancraft.jailtracker;

import com.flancraft.jailtracker.listeners.BanCommandListener;
import com.flancraft.jailtracker.listeners.JailCommandListener;
import com.flancraft.jailtracker.listeners.TempBanCommandListener;
import com.flancraft.jailtracker.listeners.JailTrackerCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class JailTracker extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("✅ JailTracker activado en FlanCraft.");

        getCommand("jail").setExecutor(new JailCommandListener(this));
        getCommand("ban").setExecutor(new BanCommandListener(this));
        getCommand("tempban").setExecutor(new TempBanCommandListener(this));
        getCommand("jailtracker").setExecutor(new JailTrackerCommand(this));

        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        getLogger().info("❌ JailTracker desactivado.");
    }
}
