package com.flancraft.jailtracker.listeners;

import com.flancraft.jailtracker.JailTracker;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class JailTrackerCommand implements CommandExecutor {

    private final JailTracker plugin;

    public JailTrackerCommand(JailTracker plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            sender.sendMessage("§a✅ Configuración de JailTracker recargada correctamente.");

            // Opcional: imprime en consola para verificar
            plugin.getLogger().info("🔁 Config recargado: server-name=" + plugin.getConfig().getString("server-name"));
            plugin.getLogger().info("🔁 Token: " + plugin.getConfig().getString("jailtracker-token"));
            return true;
        }

        sender.sendMessage("§cUso correcto: /jailtracker reload");
        return false;
    }
}
