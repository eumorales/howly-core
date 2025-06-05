package com.gilbertomorales.howlyvelocity.comandos;

import com.gilbertomorales.howlyvelocity.managers.GroupManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.List;
import java.util.Optional;

import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection;

public class GoCommand implements SimpleCommand {

    private final ProxyServer server;
    private final GroupManager groupManager;

    public GoCommand(ProxyServer server, GroupManager groupManager) {
        this.server = server;
        this.groupManager = groupManager;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();

        if (!(sender instanceof Player player)) {
            sender.sendMessage(legacySection().deserialize("§cApenas jogadores podem usar este comando."));
            return;
        }

        if (!player.hasPermission("howly.ajudante")) {
            player.sendMessage(legacySection().deserialize("§cVocê precisa ser do grupo §eAjudante §cou superior para usar este comando."));
            return;
        }

        String[] args = invocation.arguments();
        if (args.length == 0) {
            player.sendMessage(legacySection().deserialize("§cUtilize: /go <jogador>"));
            return;
        }

        String targetName = args[0];
        Optional<Player> targetOptional = server.getPlayer(targetName);

        if (targetOptional.isEmpty()) {
            player.sendMessage(legacySection().deserialize("§cJogador não encontrado ou offline."));
            return;
        }

        Player target = targetOptional.get();
        Optional<ServerConnection> targetServerConnection = target.getCurrentServer();

        if (targetServerConnection.isEmpty()) {
            player.sendMessage(legacySection().deserialize("§cNão foi possível determinar o servidor do jogador."));
            return;
        }

        RegisteredServer targetServer = targetServerConnection.get().getServer();
        Optional<ServerConnection> playerServerConnection = player.getCurrentServer();

        // Verificar se está no mesmo servidor
        if (playerServerConnection.isPresent() && 
            playerServerConnection.get().getServer().equals(targetServer)) {
            
            String formattedTargetName = groupManager.getFormattedPlayerName(target);
            player.sendMessage(legacySection().deserialize("§cVocê está conectado no mesmo servidor de " + formattedTargetName));
            return;
        }

        // Teleportar para o servidor do jogador
        player.createConnectionRequest(targetServer).fireAndForget();
        
        String formattedTargetName = groupManager.getFormattedPlayerName(target);
        player.sendMessage(legacySection().deserialize("§aTeleportando para o servidor de " + formattedTargetName + "§a..."));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        if (invocation.arguments().length == 1) {
            String arg = invocation.arguments()[0].toLowerCase();
            
            return server.getAllPlayers().stream()
                    .map(Player::getUsername)
                    .filter(name -> name.toLowerCase().startsWith(arg))
                    .toList();
        }
        return List.of();
    }
}
