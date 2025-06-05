package com.gilbertomorales.howlyvelocity.comandos;

import com.gilbertomorales.howlyvelocity.managers.IgnoreManager;
import com.gilbertomorales.howlyvelocity.managers.TagManager;
import com.gilbertomorales.howlyvelocity.utils.ChatUtils;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.format.TextColor;
import com.gilbertomorales.howlyvelocity.api.HowlyAPI;
import com.gilbertomorales.howlyvelocity.managers.GroupManager;
import com.gilbertomorales.howlyvelocity.utils.CoresUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection;

public class ReplyCommand implements SimpleCommand {

    private final ProxyServer server;
    private final TagManager tagManager;
    private final IgnoreManager ignoreManager;
    private final TellCommand tellCommand;

    public ReplyCommand(ProxyServer server, TagManager tagManager, IgnoreManager ignoreManager, TellCommand tellCommand) {
        this.server = server;
        this.tagManager = tagManager;
        this.ignoreManager = ignoreManager;
        this.tellCommand = tellCommand;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!(source instanceof Player sender)) {
            source.sendMessage(legacySection().deserialize("§cApenas jogadores podem usar este comando."));
            return;
        }

        if (args.length < 1) {
            sender.sendMessage(legacySection().deserialize("§cUtilize: /r <mensagem>"));
            return;
        }

        UUID lastSenderUUID = tellCommand.getLastMessageSender(sender.getUniqueId());
        if (lastSenderUUID == null) {
            sender.sendMessage(legacySection().deserialize("§cVocê não tem ninguém para responder."));
            return;
        }

        Optional<Player> targetOptional = server.getPlayer(lastSenderUUID);
        if (targetOptional.isEmpty()) {
            sender.sendMessage(legacySection().deserialize("§cO jogador não está mais online."));
            return;
        }

        Player target = targetOptional.get();

        // Verificar se o destinatário está ignorando o remetente
        if (ignoreManager.isIgnoring(target.getUniqueId(), sender.getUniqueId())) {
            sender.sendMessage(legacySection().deserialize("§cEste jogador está te ignorando."));
            return;
        }

        // Construir a mensagem
        String rawMessage = String.join(" ", args);
        String message = ChatUtils.applyColors(rawMessage, sender);

        // Criar componentes formatados para remetente e destinatário
        Component senderMessage = createReplyMessage("para", target, sender, message);
        Component targetMessage = createReplyMessage("de", sender, target, message);

        // Enviar mensagens
        sender.sendMessage(senderMessage);
        target.sendMessage(targetMessage);

        // Atualizar para /r do destinatário
        tellCommand.updateLastMessageSender(target.getUniqueId(), sender.getUniqueId());
    }

    /**
     * Cria uma mensagem formatada para o sistema de reply
     */
    private Component createReplyMessage(String direction, Player displayPlayer, Player contextPlayer, String message) {
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
        return List.of();
    }
}
