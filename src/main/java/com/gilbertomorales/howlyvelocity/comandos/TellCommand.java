package com.gilbertomorales.howlyvelocity.comandos;

import com.gilbertomorales.howlyvelocity.api.HowlyAPI;
import com.gilbertomorales.howlyvelocity.managers.IgnoreManager;
import com.gilbertomorales.howlyvelocity.managers.TagManager;
import com.gilbertomorales.howlyvelocity.utils.ChatUtils;
import com.gilbertomorales.howlyvelocity.utils.PlayerUtils;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import com.gilbertomorales.howlyvelocity.managers.GroupManager;
import com.gilbertomorales.howlyvelocity.utils.CoresUtils;

import java.util.*;

import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection;

public class TellCommand implements SimpleCommand {

    private final ProxyServer server;
    private final TagManager tagManager;
    private final IgnoreManager ignoreManager;
    private final Map<UUID, UUID> lastReplies = new HashMap<>();

    public TellCommand(ProxyServer server, TagManager tagManager, IgnoreManager ignoreManager) {
        this.server = server;
        this.tagManager = tagManager;
        this.ignoreManager = ignoreManager;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!(source instanceof Player sender)) {
            source.sendMessage(legacySection().deserialize("§cApenas jogadores podem usar este comando."));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(legacySection().deserialize("§cUtilize: /tell <jogador/#id> <mensagem>"));
            return;
        }

        String targetIdentifier = args[0];
        String rawMessage = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        sender.sendMessage(Component.text("§eBuscando usuário..."));

        PlayerUtils.findPlayer(server, targetIdentifier).thenAccept(result -> {
            if (result != null) {
                if (result.isOnline()) {
                    Player target = result.getOnlinePlayer();

                    // Não permitir enviar mensagem para si mesmo
                    if (target.equals(sender)) {
                        sender.sendMessage(legacySection().deserialize("§cVocê não pode enviar mensagens para si mesmo."));
                        return;
                    }

                    // Verificar se o destinatário está ignorando o remetente
                    if (ignoreManager.isIgnoring(target.getUniqueId(), sender.getUniqueId())) {
                        sender.sendMessage(legacySection().deserialize("§cEste jogador está te ignorando."));
                        return;
                    }

                    String message = ChatUtils.applyColors(rawMessage, sender);

                    // Criar componentes formatados para remetente e destinatário
                    Component senderMessage = createTellMessage("para", target, sender, message);
                    Component targetMessage = createTellMessage("de", sender, target, message);

                    // Enviar mensagens
                    sender.sendMessage(senderMessage);
                    target.sendMessage(targetMessage);

                    // Salvar para /r
                    updateLastMessageSender(target.getUniqueId(), sender.getUniqueId());
                } else {
                    sender.sendMessage(legacySection().deserialize("§cO usuário precisa estar online para receber mensagens."));
                }
            } else {
                sender.sendMessage(legacySection().deserialize("§cJogador não encontrado."));
            }
        }).exceptionally(ex -> {
            sender.sendMessage(Component.text("Erro ao buscar usuário: " + ex.getMessage()).color(TextColor.color(255, 85, 85)));
            ex.printStackTrace();
            return null;
        });
    }

    /**
     * Cria uma mensagem formatada para o sistema de tell
     */
    private Component createTellMessage(String direction, Player displayPlayer, Player contextPlayer, String message) {
        Component finalMessage = Component.text("Mensagem " + direction + " ").color(TextColor.color(85, 85, 85));

        // Obter grupo do jogador que será exibido
        GroupManager groupManager = HowlyAPI.getInstance().getPlugin().getGroupManager();
        String groupPrefix = "";
        String nameColor = "§7";
        
        if (groupManager.isLuckPermsAvailable()) {
            groupPrefix = groupManager.getPlayerGroupPrefix(displayPlayer);
            nameColor = groupManager.getPlayerGroupNameColor(displayPlayer);
        }

        // Adicionar grupo se existir (SEM espaço extra se não tiver grupo)
        if (!groupPrefix.isEmpty()) {
            finalMessage = finalMessage.append(Component.text(groupPrefix + " "));
        }

        // Adicionar nome do jogador
        TextColor playerNameColor = CoresUtils.getTextColorFromCode(nameColor);
        finalMessage = finalMessage.append(Component.text(displayPlayer.getUsername()).color(playerNameColor));

        // Adicionar dois pontos e mensagem
        finalMessage = finalMessage.append(Component.text(": ").color(TextColor.color(85, 85, 85)))
                .append(Component.text(message).color(TextColor.color(255, 255, 255)));

        return finalMessage;
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
        }
        return List.of();
    }

    public UUID getLastMessageSender(UUID playerUUID) {
        return lastReplies.get(playerUUID);
    }

    public void updateLastMessageSender(UUID playerUUID, UUID senderUUID) {
        lastReplies.put(playerUUID, senderUUID);
    }
}
