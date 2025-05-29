package com.flancraft.jailtracker.listeners;

import com.flancraft.jailtracker.JailTracker;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class JailCommandListener implements CommandExecutor {

    private final JailTracker plugin;

    // Lista de motivos válidos reconocidos por la web
    private static final Set<String> motivosValidos = new HashSet<>(Arrays.asList(
            "hacks", "fly", "minar survival", "insultos", "tpakill", "granja de lag", "grif", "spam", "flood", "usar bugs", "estafas", "otros"
    ));

    public JailCommandListener(JailTracker plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 3) {
            String targetName = args[0];
            String jailName = args[1];
            String duration = args[2];
            Player target = Bukkit.getPlayerExact(targetName);

            if (target != null) {
                // Obtener motivo a partir de los args
                String reason = "sin_clasificar";
                if (args.length > 3) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 3; i < args.length; i++) {
                        sb.append(args[i]).append(" ");
                    }
                    reason = sb.toString().trim().toLowerCase();

                    // Si no está en la lista, ponerlo como "otros"
                    if (!motivosValidos.contains(reason)) {
                        reason = "otros";
                    }
                }

                String servidor = plugin.getConfig().getString("server-name", "desconocido");

                String json = "{"
                        + "\"uuid\": \"" + target.getUniqueId() + "\","
                        + "\"name\": \"" + target.getName() + "\","
                        + "\"moderator\": \"" + sender.getName() + "\","
                        + "\"duration\": \"" + duration + "\","
                        + "\"timestamp\": \"" + System.currentTimeMillis() + "\","
                        + "\"server\": \"" + servidor + "\","
                        + "\"type\": \"" + reason + "\""
                        + "}";

                sendToWebhook(json);

                // Ejecutar el comando de Essentials
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        "essentials:jail " + target.getName() + " " + jailName + " " + duration);

            } else {
                sender.sendMessage("§cEse jugador no está conectado.");
            }
        } else {
            sender.sendMessage("§cUso correcto: /jail <jugador> <carcel> <tiempo> [motivo]");
        }

        return true;
    }

    private void sendToWebhook(String json) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                HttpClient client = HttpClient.newBuilder()
                        .followRedirects(HttpClient.Redirect.ALWAYS)
                        .build();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://flancraft.com/api/jail-report/"))
                        .header("Content-Type", "application/json")
                        .header("X-API-Key", "flancraft_super_token_439")
                        .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                plugin.getLogger().info("✅ Webhook respondió con: " + response.statusCode());
                plugin.getLogger().info("📦 Respuesta: " + response.body());

            } catch (Exception e) {
                plugin.getLogger().warning("❌ Error al enviar datos al webhook: " + e.getMessage());
            }
        });
    }
}
