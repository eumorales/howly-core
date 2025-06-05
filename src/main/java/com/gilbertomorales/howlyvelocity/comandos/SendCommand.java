package com.gilbertomorales.howlyvelocity.comandos;

import com.gilbertomorales.howlyvelocity.managers.TagManager;
import com.gilbertomorales.howlyvelocity.utils.PlayerUtils;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Optional;

import com.gilbertomorales.howlyvelocity.api.HowlyAPI;
import com.gilbertomorales.howlyvelocity.managers.GroupManager;

import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection;

public class SendCommand implements SimpleCommand {

    private final ProxyServer server;
    private final TagManager tagManager;

    public SendCommand(ProxyServer server, TagManager tagManager) {
        this.server = server;
        this.tagManager = tagManager;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();

        if (sender instanceof Player player) {
            if (!player.hasPermission("howly.coordenador")) {
                sender.sendMessage(legacySection().deserialize("§cVocê precisa ser do grupo §cCoordenador §cou superior para usar este comando."));
                return;
            }
        }

        String[] args = invocation.arguments();
        if (args.length < 2) {
            sender.sendMessage(legacySection().deserialize("§cUtilize: /send <jogador/#id> <servidor>"));
            return;
        }

        String targetIdentifier = args[0];
        String serverName = args[1];

        Optional<RegisteredServer> serverOptional = server.getServer(serverName);
        if (serverOptional.isEmpty()) {
            sender.sendMessage(legacySection().deserialize("§cServidor não encontrado."));
            return;
        }

        RegisteredServer targetServer = serverOptional.get();
        sender.sendMessage(legacySection().deserialize("§eBuscando usuário..."));

        PlayerUtils.findPlayer(server, targetIdentifier).thenAccept(result -> {
            if (result != null) {
                if (result.isOnline()) {
                    Player target = result.getOnlinePlayer();
                    
                    target.createConnectionRequest(targetServer).fireAndForget();
                    
                    GroupManager groupManager = HowlyAPI.getInstance().getPlugin().getGroupManager();
                    String senderName = sender instanceof Player ? groupManager.getFormattedPlayerName((Player) sender) : "§4[CONSOLE]";
                    String targetName = groupManager.getFormattedPlayerName(target);
                    
                    sender.sendMessage(legacySection().deserialize("§aJogador " + targetName + " §aenviado para o servidor §e" + targetServer.getServerInfo().getName()));
                    target.sendMessage(legacySection().deserialize("§aVocê foi enviado para o servidor §e" + targetServer.getServerInfo().getName() + " §apor " + senderName));
                } else {
                    sender.sendMessage(legacySection().deserialize("§cO usuário precisa estar online para ser enviado."));
                }
            } else {
                sender.sendMessage(legacySection().deserialize("§cUsuário não encontrado."));
            }
        }).exceptionally(ex -> {
            sender.sendMessage(legacySection().deserialize("§cErro ao buscar usuário: " + ex.getMessage()));
            ex.printStackTrace();
            return null;
        });
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        if (invocation.arguments().length == 1) {
            String arg = invocation.arguments()[0].toLowerCase();
            List<String> suggestions = server.getAllPlayers().stream()
                    .map(Player::getUsername)
                    .filter(name -> name.toLowerCase().startsWith(arg))
                    .toList();
            
            // Adicionar sugestões de ID se começar com #
            if (arg.startsWith("#")) {
                suggestions.addAll(List.of("#1", "#2", "#3", "#4", "#5"));
            }
            
            return suggestions;
        } else if (invocation.arguments().length == 2) {
            return server.getAllServers().stream()
                    .map(s -> s.getServerInfo().getName())
                    .filter(name -> name.toLowerCase().startsWith(invocation.arguments()[1].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
