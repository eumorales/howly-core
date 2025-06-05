package com.gilbertomorales.howlyvelocity.listeners;

import com.gilbertomorales.howlyvelocity.api.HowlyAPI;
import com.gilbertomorales.howlyvelocity.api.punishment.Punishment;
import com.gilbertomorales.howlyvelocity.managers.PlayerDataManager;
import com.gilbertomorales.howlyvelocity.managers.TagManager;
import com.gilbertomorales.howlyvelocity.managers.PlaytimeManager;
import com.gilbertomorales.howlyvelocity.utils.TimeUtils;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

public class PlayerListener {

    private final ProxyServer server;
    private final Logger logger;
    private final PlayerDataManager playerDataManager;
    private final TagManager tagManager;

    public PlayerListener(ProxyServer server, Logger logger, PlayerDataManager playerDataManager, TagManager tagManager) {
        this.server = server;
        this.logger = logger;
        this.playerDataManager = playerDataManager;
        this.tagManager = tagManager;
    }

    private HowlyAPI getAPI() {
        return HowlyAPI.getInstance();
    }

    private PlaytimeManager getPlaytimeManager() {
        return HowlyAPI.getInstance().getPlugin().getPlaytimeManager();
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onPreLogin(PreLoginEvent event) {
        // Verificar se o jogador está banido antes de permitir o login
        String username = event.getUsername();

        // Não podemos verificar banimento aqui porque ainda não temos o UUID
        // A verificação será feita no LoginEvent
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onLogin(LoginEvent event) {
        Player player = event.getPlayer();

        // Verificar se o jogador está banido
        getAPI().getPunishmentAPI().getActiveBan(player.getUniqueId()).thenAccept(punishment -> {
            if (punishment != null) {
                String timeRemaining = punishment.isPermanent() ? "Permanente" : TimeUtils.formatDuration(punishment.getRemainingTime());
                String kickMessage = "§c§lHOWLY" + "\n" + "§cVocê está suspenso do servidor." + "\n\n" +
                        "§fMotivo: §7" + punishment.getReason() + "\n" +
                        "§fAutor: §7" + punishment.getPunisher() + "\n" +
                        "§fTempo restante: §7" + timeRemaining + "\n\n" +
                        "§eUse o ID #" + punishment.getId() + " para criar uma revisão em §ndiscord.gg/howly§e.";

                player.disconnect(legacySection().deserialize(kickMessage));
            }
        });

        // Salvar dados do jogador no banco de dados
        playerDataManager.updatePlayerData(player.getUniqueId(), player.getUsername());

        // Iniciar sessão de tempo online
        getPlaytimeManager().startSession(player.getUniqueId());
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        Player player = event.getPlayer();

        // Verificar se o jogador está mutado e notificar
        server.getScheduler().buildTask(HowlyAPI.getInstance().getPlugin(), () -> {
            getAPI().getPunishmentAPI().getActiveMute(player.getUniqueId()).thenAccept(punishment -> {
                if (punishment != null) {
                    String timeRemaining = punishment.isPermanent() ? "Permanente" : TimeUtils.formatDuration(punishment.getRemainingTime());
                    String message = "\n§cVocê está silenciado.\n\n" +
                            "§fMotivo: §7" + punishment.getReason() + "\n" +
                            "§fAutor: §7" + punishment.getPunisher() + "\n" +
                            "§fTempo restante: §7" + timeRemaining + "\n\n§eVocê pode apelar no nosso discord §ndiscord.gg/howly§e.\n";

                    player.sendMessage(legacySection().deserialize(message));
                }
            });
        }).delay(1, TimeUnit.SECONDS).schedule();
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();

        // Finalizar sessão de tempo online
        getPlaytimeManager().endSession(player.getUniqueId());
    }
}
