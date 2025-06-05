package com.gilbertomorales.howlyvelocity.comandos;

import com.gilbertomorales.howlyvelocity.api.HowlyAPI;
import com.gilbertomorales.howlyvelocity.managers.PlayerDataManager;
import com.gilbertomorales.howlyvelocity.managers.TagManager;
import com.gilbertomorales.howlyvelocity.utils.PlayerUtils;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection;

public class UnbanCommand implements SimpleCommand {

    private final ProxyServer server;
    private final TagManager tagManager;
    private final PlayerDataManager playerDataManager;
    private final HowlyAPI api;

    public UnbanCommand(ProxyServer server, TagManager tagManager) {
        this.server = server;
        this.tagManager = tagManager;
        this.playerDataManager = HowlyAPI.getInstance().getPlugin().getPlayerDataManager();
        this.api = HowlyAPI.getInstance();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!source.hasPermission("howly.gerente")) {
            source.sendMessage(legacySection().deserialize("§cVocê precisa ser do grupo §4Gerente §cou superior para usar este comando."));
            return;
        }

        if (args.length < 1) {
            source.sendMessage(legacySection().deserialize("§cUtilize: /unban <jogador/#id>"));
            return;
        }

        String targetIdentifier = args[0];

        // Obter nome do unbanner
        String unbannerName;
        if (source instanceof Player) {
            unbannerName = ((Player) source).getUsername();
        } else {
            unbannerName = "Console";
        }

        source.sendMessage(legacySection().deserialize("§eBuscando jogador..."));

        PlayerUtils.findPlayer(server, targetIdentifier).thenAccept(result -> {
            if (result != null) {
                unbanPlayer(source, result.getUUID(), result.getName(), unbannerName);
            } else {
                source.sendMessage(legacySection().deserialize("§cJogador não encontrado."));
            }
        }).exceptionally(ex -> {
            source.sendMessage(legacySection().deserialize("§cErro ao buscar jogador: " + ex.getMessage()));
            ex.printStackTrace();
            return null;
        });
    }

    private void unbanPlayer(CommandSource source, java.util.UUID targetUUID, String targetName, String unbannerName) {
        // Verificar se o jogador está banido
        api.getPunishmentAPI().isPlayerBanned(targetUUID).thenAccept(isBanned -> {
            if (!isBanned) {
                source.sendMessage(legacySection().deserialize("§cEste jogador não está banido."));
                return;
            }

            // Desbanir jogador
            CompletableFuture<Boolean> unbanFuture = api.getPunishmentAPI().unbanPlayer(targetUUID, unbannerName);

            unbanFuture.thenAccept(success -> {
                if (success) {
                    // Notificar staff
                    String unbanMessage = "\n§e" + targetName + " foi desbanido por §a" + unbannerName + "§e." + "\n";

                    server.getAllPlayers().stream()
                            .filter(p -> p.hasPermission("howly.ajudante"))
                            .forEach(p -> p.sendMessage(legacySection().deserialize(unbanMessage)));

                    // Notificar quem executou o comando
                    source.sendMessage(legacySection().deserialize("§aJogador desbanido com sucesso!"));
                } else {
                    source.sendMessage(legacySection().deserialize("§cNão foi possível desbanir o jogador."));
                }
            }).exceptionally(ex -> {
                source.sendMessage(legacySection().deserialize("§cErro ao desbanir jogador: " + ex.getMessage()));
                ex.printStackTrace();
                return null;
            });
        });
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();

        if (args.length == 1) {
            String partialName = args[0].toLowerCase();
            List<String> suggestions = server.getAllPlayers().stream()
                    .map(Player::getUsername)
                    .filter(name -> name.toLowerCase().startsWith(partialName))
                    .collect(Collectors.toList());
            
            // Adicionar sugestões de ID se começar com #
            if (partialName.startsWith("#")) {
                suggestions.addAll(List.of("#1", "#2", "#3", "#4", "#5"));
            }
            
            return suggestions;
        }

        return List.of();
    }
}
