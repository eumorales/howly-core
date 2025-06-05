package com.gilbertomorales.howlyvelocity.comandos;

import com.gilbertomorales.howlyvelocity.api.HowlyAPI;
import com.gilbertomorales.howlyvelocity.managers.GroupManager;
import com.gilbertomorales.howlyvelocity.managers.PlaytimeManager;
import com.gilbertomorales.howlyvelocity.utils.PlayerUtils;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection;

public class TempoCommand implements SimpleCommand {

    private final ProxyServer server;
    private final PlaytimeManager playtimeManager;
    private final GroupManager groupManager;

    public TempoCommand(ProxyServer server, PlaytimeManager playtimeManager) {
        this.server = server;
        this.playtimeManager = playtimeManager;
        this.groupManager = HowlyAPI.getInstance().getPlugin().getGroupManager();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!(source instanceof Player sender)) {
            source.sendMessage(legacySection().deserialize("§cApenas jogadores podem usar este comando."));
            return;
        }

        if (args.length == 0) {
            // Mostrar tempo próprio
            showPlayerTime(sender, sender);
            return;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "top" -> showTopPlaytime(sender);
            case "resetar", "reset" -> {
                if (args.length < 2) {
                    sender.sendMessage(legacySection().deserialize("§cUtilize: /tempo resetar <jogador/#id>"));
                    return;
                }

                if (!sender.hasPermission("howly.gerente")) {
                    sender.sendMessage(legacySection().deserialize("§cVocê precisa ser do grupo §4Gerente §cou superior para usar este comando."));
                    return;
                }

                resetPlayerTime(sender, args[1]);
            }
            default -> {
                // Mostrar tempo de outro jogador
                showOtherPlayerTime(sender, args[0]);
            }
        }
    }

    private void showPlayerTime(Player sender, Player target) {
        sender.sendMessage(legacySection().deserialize("§eBuscando tempo online..."));

        playtimeManager.getPlayerPlaytime(target.getUniqueId()).thenAccept(playtime -> {
            String formattedTime = playtimeManager.formatPlaytime(playtime);
            String formattedName = groupManager.getFormattedPlayerName(target);

            if (sender.equals(target)) {
                sender.sendMessage(legacySection().deserialize("§aSeu tempo online: §f" + formattedTime));
            } else {
                sender.sendMessage(legacySection().deserialize("§eTempo online de " + formattedName + "§e: §f" + formattedTime));
            }
        }).exceptionally(ex -> {
            sender.sendMessage(legacySection().deserialize("§cErro ao buscar tempo online: " + ex.getMessage()));
            ex.printStackTrace();
            return null;
        });
    }

    private void showOtherPlayerTime(Player sender, String targetIdentifier) {
        sender.sendMessage(legacySection().deserialize("§eBuscando usuário..."));

        PlayerUtils.findPlayer(server, targetIdentifier).thenAccept(result -> {
            if (result != null) {
                playtimeManager.getPlayerPlaytime(result.getUUID()).thenAccept(playtime -> {
                    String formattedTime = playtimeManager.formatPlaytime(playtime);
                    String playerName = result.getName();

                    if (result.isOnline()) {
                        String formattedName = groupManager.getFormattedPlayerName(result.getOnlinePlayer());
                        sender.sendMessage(legacySection().deserialize("§eTempo online de " + formattedName + "§e: §f" + formattedTime));
                    } else {
                        // Usar o método para jogadores offline
                        String formattedName = groupManager.getFormattedPlayerNameByUUID(result.getUUID(), playerName);
                        sender.sendMessage(legacySection().deserialize("§eTempo online de " + formattedName + "§e: §f" + formattedTime));
                    }
                }).exceptionally(ex -> {
                    sender.sendMessage(legacySection().deserialize("§cErro ao buscar tempo online: " + ex.getMessage()));
                    ex.printStackTrace();
                    return null;
                });
            } else {
                sender.sendMessage(legacySection().deserialize("§cUsuário não encontrado."));
            }
        }).exceptionally(ex -> {
            sender.sendMessage(legacySection().deserialize("§cErro ao buscar usuário: " + ex.getMessage()));
            ex.printStackTrace();
            return null;
        });
    }

    private void showTopPlaytime(Player sender) {
        sender.sendMessage(legacySection().deserialize("§eBuscando ranking de tempo online..."));

        playtimeManager.getTopPlaytime().thenAccept(topList -> {
            if (topList.isEmpty()) {
                sender.sendMessage(legacySection().deserialize("§cNenhum dado de tempo online encontrado."));
                return;
            }

            sender.sendMessage(legacySection().deserialize(" "));
            sender.sendMessage(legacySection().deserialize("§eUsuários com mais tempo online na rede:"));
            sender.sendMessage(legacySection().deserialize(" "));

            for (int i = 0; i < topList.size(); i++) {
                PlaytimeManager.PlaytimeEntry entry = topList.get(i);
                String formattedTime = playtimeManager.formatPlaytime(entry.getPlaytime());

                String position = "§f" + (i + 1) + "º";

                String formattedName;

                Optional<Player> onlinePlayer = server.getPlayer(entry.getPlayerUuid());
                if (onlinePlayer.isPresent()) {
                    formattedName = groupManager.getFormattedPlayerName(onlinePlayer.get());
                } else {
                    formattedName = groupManager.getFormattedPlayerNameByUUID(entry.getPlayerUuid(), entry.getPlayerName());
                }

                String time = "§7" + formattedTime;

                sender.sendMessage(legacySection().deserialize(position + " " + formattedName + "§8 - " + time));
            }

            sender.sendMessage(legacySection().deserialize(" "));
        }).exceptionally(ex -> {
            sender.sendMessage(legacySection().deserialize("§cErro ao buscar ranking: " + ex.getMessage()));
            ex.printStackTrace();
            return null;
        });
    }

    private void resetPlayerTime(Player sender, String targetIdentifier) {
        sender.sendMessage(legacySection().deserialize("§eBuscando usuário..."));

        PlayerUtils.findPlayer(server, targetIdentifier).thenAccept(result -> {
            if (result != null) {
                playtimeManager.resetPlayerPlaytime(result.getUUID()).thenAccept(success -> {
                    if (success) {
                        String playerName = result.getName();
                        sender.sendMessage(legacySection().deserialize("§aTempo online de " + playerName + " resetado com sucesso!"));

                        // Notificar o jogador se estiver online
                        if (result.isOnline()) {
                            Player target = result.getOnlinePlayer();
                            String senderName = groupManager.getFormattedPlayerName(sender);
                            target.sendMessage(legacySection().deserialize("§eSeu tempo online foi resetado por " + senderName + "§e."));
                        }
                    } else {
                        sender.sendMessage(legacySection().deserialize("§cNão foi possível resetar o tempo online do jogador."));
                    }
                }).exceptionally(ex -> {
                    sender.sendMessage(legacySection().deserialize("§cErro ao resetar tempo online: " + ex.getMessage()));
                    ex.printStackTrace();
                    return null;
                });
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
        String[] args = invocation.arguments();

        if (args.length == 1) {
            String arg = args[0].toLowerCase();
            List<String> suggestions = List.of("top", "resetar").stream()
                    .filter(cmd -> cmd.startsWith(arg))
                    .collect(Collectors.toList());

            // Adicionar jogadores online
            suggestions.addAll(server.getAllPlayers().stream()
                    .map(Player::getUsername)
                    .filter(name -> name.toLowerCase().startsWith(arg))
                    .collect(Collectors.toList()));

            return suggestions;
        } else if (args.length == 2 && args[0].equalsIgnoreCase("resetar")) {
            String arg = args[1].toLowerCase();
            return server.getAllPlayers().stream()
                    .map(Player::getUsername)
                    .filter(name -> name.toLowerCase().startsWith(arg))
                    .collect(Collectors.toList());
        }

        return List.of();
    }
}
