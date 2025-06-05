package com.gilbertomorales.howlyvelocity.comandos;

import com.gilbertomorales.howlyvelocity.utils.CoresUtils;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;

import java.util.List;

import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection;

public class AnuncioCommand implements SimpleCommand {

    private final ProxyServer server;

    public AnuncioCommand(ProxyServer server) {
        this.server = server;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player sender)) {
            invocation.source().sendMessage(legacySection().deserialize("§cApenas jogadores podem usar este comando."));
            return;
        }

        if (!sender.hasPermission("howly.coordenador")) {
            sender.sendMessage(legacySection().deserialize("§cVocê precisa ser do grupo Coordenador §cou superior para usar este comando."));
            return;
        }

        String[] args = invocation.arguments();
        if (args.length == 0) {
            sender.sendMessage(legacySection().deserialize("§cUtilize: /anuncio <mensagem>"));
            return;
        }

        String message = String.join(" ", args);
        String formattedMessage = CoresUtils.colorir("&d&l[ANÚNCIO] &f" + message);

        server.getAllPlayers().forEach(player -> {
            player.sendMessage(Component.text(" "));
            player.sendMessage(Component.text(formattedMessage));
            player.sendMessage(Component.text(" "));
        });

        sender.sendMessage(legacySection().deserialize("§aAnúncio enviado para todos os jogadores da rede."));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return List.of();
    }
}
