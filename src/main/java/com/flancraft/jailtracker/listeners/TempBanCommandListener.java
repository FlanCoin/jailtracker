package com.flancraft.jailtracker.listeners;

import com.flancraft.jailtracker.JailTracker;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TempBanCommandListener implements CommandExecutor {

    private final JailTracker plugin;

    private static final Set<String> motivosValidos = new HashSet<>(Arrays.asList(
        "hacks", "fly", "minar survival", "insultos", "tpakill", "granja de lag",
        "grif", "spam", "flood", "usar bugs", "estafas", "otros"
    ));

    public TempBanCommandListener(JailTracker plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 3) {
            String targetName = args[0];
            String duration = args[1];
            String rawReason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
            String reason = rawReason.toLowerCase().trim();

            if (!motivosValidos.contains(reason)) {
                reason = "otros";
            }

            String uuid = "desconocido"; // Puedes mejorarlo luego
            String servidor = plugin.getConfig().getString("server-name", "desconocido");

            String json = "{"
                    + "\"uuid\": \"" + uuid + "\","
                    + "\"name\": \"" + targetName + "\","
                    + "\"moderator\": \"" + sender.getName() + "\","
                    + "\"duration\": \"" + duration + "\","
                    + "\"timestamp\": \"" + System.currentTimeMillis() + "\","
                    + "\"server\": \"" + servidor + "\","
                    + "\"type\": \"" + reason + "\","
                    + "\"banType\": \"temp\""
                    + "}";

            sendToWebhook(json);

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    "essentials:tempban " + targetName + " " + duration + " " + rawReason);

            return true;
        } else {
            sender.sendMessage("§cUso correcto: /tempban <jugador> <tiempo> <motivo>");
            return false;
        }
    }

    private void sendToWebhook(String json) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                HttpClient client = HttpClient.newBuilder()
                        .followRedirects(HttpClient.Redirect.ALWAYS)
                        .build();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://flancraftweb-backend.onrender.com/api/jails"))
                        .header("Content-Type", "application/json")
                        .header("X-API-Key", plugin.getConfig().getString("jailtracker-token"))
                        .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                plugin.getLogger().info("✅ Backend respondió con: " + response.statusCode());
                plugin.getLogger().info("📦 Respuesta: " + response.body());
            } catch (Exception e) {
                plugin.getLogger().warning("❌ Error al enviar sanción al backend: " + e.getMessage());
            }
        });
    }
}
