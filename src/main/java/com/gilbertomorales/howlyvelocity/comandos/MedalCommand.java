package com.gilbertomorales.howlyvelocity.comandos;

import com.gilbertomorales.howlyvelocity.managers.MedalManager;
import com.gilbertomorales.howlyvelocity.utils.CoresUtils;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.List;
import java.util.StringJoiner;

import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection;

public class MedalCommand implements SimpleCommand {

    private final ProxyServer server;
    private final MedalManager medalManager;

    public MedalCommand(ProxyServer server, MedalManager medalManager) {
        this.server = server;
        this.medalManager = medalManager;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();

        if (!(source instanceof Player player)) {
            source.sendMessage(legacySection().deserialize("§cApenas jogadores podem usar este comando."));
            return;
        }

        String[] args = invocation.arguments();

        // Mostrar todas as medalhas disponíveis
        if (args.length == 0) {
            showAvailableMedals(player);
            return;
        }

        String subCommand = args[0].toLowerCase();

        // Comando para remover medalha
        if (subCommand.equals("remover") || subCommand.equals("remove")) {
            medalManager.removePlayerMedal(player.getUniqueId());
            player.sendMessage(legacySection().deserialize("§aMedalha removida com sucesso! Agora você não possui nenhuma medalha."));
            return;
        }

        // Selecionar uma medalha
        String medalId = subCommand;

        if (!medalManager.hasMedal(medalId)) {
            player.sendMessage(legacySection().deserialize("§cMedalha não encontrada. Use /medalha para ver as medalhas disponíveis."));
            return;
        }

        MedalManager.MedalInfo medalInfo = medalManager.getMedalInfo(medalId);

        // Verificar permissão
        if (!medalInfo.getPermission().isEmpty() && !player.hasPermission(medalInfo.getPermission())) {
            player.sendMessage(legacySection().deserialize("§cVocê não tem permissão para usar esta medalha."));
            return;
        }

        // Definir a medalha
        medalManager.setPlayerMedal(player.getUniqueId(), medalId);

        if (medalId.equals("nenhuma")) {
            player.sendMessage(legacySection().deserialize("§aMedalha removida com sucesso!"));
        } else {
            Component medalComponent = Component.text(medalInfo.getSymbol()).color(CoresUtils.getTextColorFromCode(medalInfo.getColor()));
            Component message = legacySection().deserialize("§aMedalha alterada para ")
                    .append(medalComponent)
                    .append(legacySection().deserialize(" §acom sucesso!"));

            player.sendMessage(message);
        }
    }

    private void showAvailableMedals(Player player) {
        List<String> availableMedals = medalManager.getPlayerAvailableMedals(player);

        if (availableMedals.isEmpty()) {
            player.sendMessage(legacySection().deserialize("§cVocê não tem nenhuma medalha disponível."));
            return;
        }

        player.sendMessage(legacySection().deserialize(""));
        player.sendMessage(legacySection().deserialize("§eUse /medalha <nome> para selecionar uma medalha"));
        player.sendMessage(legacySection().deserialize("§eUse /medalha remover para remover sua medalha atual"));
        player.sendMessage(legacySection().deserialize(""));

        // Mostrar medalha atual
        String currentMedal = medalManager.getCurrentPlayerMedal(player.getUniqueId());
        if (currentMedal != null) {
            MedalManager.MedalInfo currentMedalInfo = medalManager.getMedalInfo(currentMedal);
            if (currentMedalInfo != null) {
                Component medalComponent = Component.text(currentMedalInfo.getSymbol()).color(CoresUtils.getTextColorFromCode(currentMedalInfo.getColor()));
                player.sendMessage(legacySection().deserialize("§fMedalha atual: ").append(medalComponent));
            }
        } else {
            player.sendMessage(legacySection().deserialize("§fMedalha atual: §7Nenhuma"));
        }

        // Listar medalhas disponíveis
        StringJoiner medalsJoiner = new StringJoiner("§7, ");
        for (String medalId : availableMedals) {
            if (!medalId.equals("nenhuma")) {
                MedalManager.MedalInfo medalInfo = medalManager.getMedalInfo(medalId);
                String medalDisplay = medalInfo.getColor() + medalInfo.getSymbol() + " " + capitalizeFirst(medalId);
                medalsJoiner.add(medalDisplay);
            }
        }

        player.sendMessage(legacySection().deserialize("§fMedalhas disponíveis: " + medalsJoiner.toString()));
    }

    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) {
            return List.of();
        }

        if (invocation.arguments().length == 1) {
            String arg = invocation.arguments()[0].toLowerCase();

            List<String> suggestions = new java.util.ArrayList<>(medalManager.getPlayerAvailableMedals(player));
            suggestions.add("remover");

            return suggestions.stream()
                    .filter(medal -> medal.toLowerCase().startsWith(arg))
                    .toList();
        }

        return List.of();
    }
}
