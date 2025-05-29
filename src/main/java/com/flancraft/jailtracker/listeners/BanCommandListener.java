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

public class BanCommandListener implements CommandExecutor {

    private final JailTracker plugin;

    private static final Set<String> motivosValidos = new HashSet<>(Arrays.asList(
        "hacks", "fly", "minar survival", "insultos", "tpakill", "granja de lag",
        "grif", "spam", "flood", "usar bugs", "estafas", "otros"
    ));

    public BanCommandListener(JailTracker plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 2) {
            String playerName = args[0];
            String rawMotivo = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            String motivo = rawMotivo.toLowerCase().trim();

            if (!motivosValidos.contains(motivo)) {
                motivo = "otros";
            }

            String uuid = "desconocido"; // Puedes usar UUIDFetcher si lo integras
            String servidor = plugin.getConfig().getString("server-name", "desconocido");

            String json = "{"
                    + "\"uuid\": \"" + uuid + "\","
                    + "\"name\": \"" + playerName + "\","
                    + "\"moderator\": \"" + sender.getName() + "\","
                    + "\"duration\": \"PERMABAN\","
                    + "\"timestamp\": \"" + System.currentTimeMillis() + "\","
                    + "\"server\": \"" + servidor + "\","
                    + "\"type\": \"" + motivo + "\","
                    + "\"banType\": \"perma\""
                    + "}";

            sendToWebhook(json);

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    "essentials:ban " + playerName + " " + rawMotivo);

            return true;
        } else {
            sender.sendMessage("§cUso correcto: /ban <jugador> <motivo>");
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
