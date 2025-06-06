package com.gilbertomorales.howlyvelocity.comandos;

import com.gilbertomorales.howlyvelocity.managers.GroupManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

import java.util.ArrayList;
import java.util.List;

import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection;

public class BunkerChatCommand implements SimpleCommand {

    public static ArrayList<String> restricaoChatBunker = new ArrayList<>();
    private final ProxyServer server;
    private final GroupManager groupManager;

    public BunkerChatCommand(ProxyServer server, GroupManager groupManager) {
        this.server = server;
        this.groupManager = groupManager;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();

        if (!(sender instanceof Player player)) {
            sender.sendMessage(legacySection().deserialize("§cApenas jogadores podem executar este comando."));
            return;
        }

        if (!player.hasPermission("howly.gerente")) {
            player.sendMessage(legacySection().deserialize("§cVocê precisa ser do grupo §4Gerente §cou superior para executar este comando."));
            return;
        }

        String[] args = invocation.arguments();

        if (args.length == 1 && args[0].equalsIgnoreCase("toggle")) {
            if (restricaoChatBunker.contains(player.getUsername())) {
                restricaoChatBunker.remove(player.getUsername());
                player.sendMessage(legacySection().deserialize("§aAgora você pode enviar e receber mensagens no chat bunker."));
            } else {
                restricaoChatBunker.add(player.getUsername());
                player.sendMessage(legacySection().deserialize("§eO chat bunker foi desativado para você."));
            }
            return;
        }

        if (restricaoChatBunker.contains(player.getUsername())) {
            player.sendMessage(legacySection().deserialize("§cVocê está com o chat bunker desabilitado!"));
            player.sendMessage(legacySection().deserialize("§cUtilize /b toggle para ativá-lo."));
            return;
        }

        if (args.length == 0) {
            player.sendMessage(legacySection().deserialize("§cUso correto: /b <\"mensagem\"/toggle>"));
            return;
        }

        StringBuilder messageBuilder = new StringBuilder();
        for (String part : args) {
            if (messageBuilder.length() > 0) messageBuilder.append(" ");
            messageBuilder.append(part);
        }
        String message = messageBuilder.toString();

        String formattedName = groupManager.getFormattedPlayerName(player);

        for (Player gerente : server.getAllPlayers()) {
            if (gerente.hasPermission("howly.gerente")) {
                if (!restricaoChatBunker.contains(gerente.getUsername())) {
                    gerente.sendMessage(legacySection().deserialize("§c§l[B] §7" + formattedName + ": §f" + message));
                }
            }
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        if (invocation.arguments().length == 1) {
            return List.of("toggle");
        }
        return List.of();
    }
}
