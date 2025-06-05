package com.gilbertomorales.howlyvelocity.comandos;

import com.gilbertomorales.howlyvelocity.managers.GroupManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

import java.util.ArrayList;
import java.util.List;

import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection;

public class StaffChatCommand implements SimpleCommand {

    public static ArrayList<String> restricaoChatEquipe = new ArrayList<>();
    private final ProxyServer server;
    private final GroupManager groupManager;

    public StaffChatCommand(ProxyServer server, GroupManager groupManager) {
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

        if (!player.hasPermission("howly.ajudante")) {
            player.sendMessage(legacySection().deserialize("§cVocê precisa ser do grupo §eAjudante §cou superior para executar este comando."));
            return;
        }

        String[] args = invocation.arguments();

        if (args.length == 1 && args[0].equalsIgnoreCase("toggle")) {
            if (restricaoChatEquipe.contains(player.getUsername())) {
                restricaoChatEquipe.remove(player.getUsername());
                player.sendMessage(legacySection().deserialize("§aAgora você pode enviar e receber mensagens no chat da equipe."));
            } else {
                restricaoChatEquipe.add(player.getUsername());
                player.sendMessage(legacySection().deserialize("§eO chat da equipe foi desativado para você."));
            }
            return;
        }

        if (restricaoChatEquipe.contains(player.getUsername())) {
            player.sendMessage(legacySection().deserialize("§cVocê está com o chat da equipe desabilitado!"));
            player.sendMessage(legacySection().deserialize("§cUtilize /s toggle para ativá-lo."));
            return;
        }

        if (args.length == 0) {
            player.sendMessage(legacySection().deserialize("§cUso correto: /s <\"mensagem\"/toggle>"));
            return;
        }

        StringBuilder messageBuilder = new StringBuilder();
        for (String part : args) {
            if (messageBuilder.length() > 0) messageBuilder.append(" ");
            messageBuilder.append(part);
        }
        String message = messageBuilder.toString();

        String formattedName = groupManager.getFormattedPlayerName(player);

        for (Player staff : server.getAllPlayers()) {
            if (staff.hasPermission("howly.ajudante")) {
                if (!restricaoChatEquipe.contains(staff.getUsername())) {
                    staff.sendMessage(legacySection().deserialize("§d§l[S] §7" + formattedName + ": §f" + message));
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
