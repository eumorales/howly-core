package com.gilbertomorales.howlyvelocity.comandos;

import com.gilbertomorales.howlyvelocity.managers.GroupManager;
import com.gilbertomorales.howlyvelocity.utils.PlayerUtils;
import com.gilbertomorales.howlyvelocity.utils.TitleAPI;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.gilbertomorales.howlyvelocity.utils.CoresUtils;

import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection;

public class GrupoCommand implements SimpleCommand {

   private final ProxyServer server;
   private final GroupManager groupManager;

   public GrupoCommand(ProxyServer server, GroupManager groupManager) {
       this.server = server;
       this.groupManager = groupManager;
   }

   @Override
   public void execute(Invocation invocation) {
       CommandSource source = invocation.source();

       if (!(source instanceof Player sender)) {
           source.sendMessage(legacySection().deserialize("§cEste comando só pode ser executado por jogadores."));
           return;
       }

       if (!sender.hasPermission("howly.gerente")) {
           sender.sendMessage(legacySection().deserialize("§cVocê precisa ser do grupo §4Gerente §cou superior para executar este comando."));
           return;
       }

       if (!groupManager.isLuckPermsAvailable()) {
           sender.sendMessage(legacySection().deserialize("§cLuckPerms não está disponível. Sistema de grupos desabilitado."));
           return;
       }

       String[] args = invocation.arguments();
       if (args.length < 1) {
           sendUsage(sender);
           return;
       }

       String action = args[0].toLowerCase();

       if (action.equals("listar")) {
           listGroups(sender);
           return;
       }

       if (args.length < 3) {
           sendUsage(sender);
           return;
       }

       String targetIdentifier = args[1];
       String groupName = args[2].toLowerCase();

       if (!groupManager.groupExists(groupName)) {
           sender.sendMessage(legacySection().deserialize("§cO grupo especificado não existe."));
           return;
       }

       sender.sendMessage(legacySection().deserialize("§eBuscando usuário..."));

       PlayerUtils.findPlayer(server, targetIdentifier).thenAccept(result -> {
           if (result != null) {
               if (result.isOnline()) {
                   Player target = result.getOnlinePlayer();
                   switch (action) {
                       case "adicionar", "add" -> addPlayerToGroup(sender, target, groupName);
                       case "remover", "remove" -> removePlayerFromGroup(sender, target, groupName);
                       case "definir", "set" -> setPlayerGroup(sender, target, groupName);
                       default -> sendUsage(sender);
                   }
               } else {
                   sender.sendMessage(legacySection().deserialize("§cO usuário precisa estar online para alterar grupos."));
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

   private void addPlayerToGroup(Player sender, Player target, String groupName) {
       groupManager.addPlayerToGroup(target, groupName).thenAccept(success -> {
           if (success) {
               sender.sendMessage(legacySection().deserialize("§aUsuário \"" + target.getUsername() + "\" adicionado ao grupo \"" + groupName + "\"."));
               
               if (groupManager.isVipGroup(groupName)) {
                   sendVipTitle(target, groupName);
               }
           } else {
               sender.sendMessage(legacySection().deserialize("§cEste usuário já está no grupo \"" + groupName + "\" ou ocorreu um erro."));
           }
       });
   }

   private void removePlayerFromGroup(Player sender, Player target, String groupName) {
       groupManager.removePlayerFromGroup(target, groupName).thenAccept(success -> {
           if (success) {
               sender.sendMessage(legacySection().deserialize("§eUsuário \"" + target.getUsername() + "\" removido do grupo \"" + groupName + "\"."));
           } else {
               sender.sendMessage(legacySection().deserialize("§cEste usuário não pertence ao grupo \"" + groupName + "\" ou ocorreu um erro."));
           }
       });
   }

   private void setPlayerGroup(Player sender, Player target, String groupName) {
       groupManager.setPlayerGroup(target, groupName).thenAccept(success -> {
           if (success) {
               sender.sendMessage(legacySection().deserialize("§eGrupo do usuário \"" + target.getUsername() + "\" definido como \"" + groupName + "\"."));
               sender.sendMessage(legacySection().deserialize("§eTodos os outros grupos foram removidos."));
               
               if (groupManager.isVipGroup(groupName)) {
                   sendVipTitle(target, groupName);
               }
           } else {
               sender.sendMessage(legacySection().deserialize("§cOcorreu um erro ao definir o grupo do jogador."));
           }
       });
   }

   private void listGroups(Player sender) {
       sender.sendMessage(legacySection().deserialize(" "));
       sender.sendMessage(legacySection().deserialize("§eGrupos disponíveis:"));
       sender.sendMessage(legacySection().deserialize(" "));
       
       Map<String, GroupManager.GroupInfo> groups = groupManager.getAvailableGroups();
       
       // Ordenar por prioridade (maior para menor)
       groups.entrySet().stream()
               .sorted((a, b) -> Integer.compare(b.getValue().getPriority(), a.getValue().getPriority()))
               .forEach(entry -> {
                   String groupId = entry.getKey();
                   GroupManager.GroupInfo info = entry.getValue();
                   
                   if (!groupId.equals("default")) {
                       String vipIndicator = info.isVip() ? " §7(VIP)" : "";
                       sender.sendMessage(legacySection().deserialize("§7- " + info.getFormattedPrefix() + vipIndicator));
                   }
               });
       
       sender.sendMessage(legacySection().deserialize(" "));
   }

   private void sendVipTitle(Player target, String groupName) {
       GroupManager.GroupInfo groupInfo = groupManager.getAvailableGroups().get(groupName);
       if (groupInfo == null || !groupInfo.isVip()) {
           return;
       }

       // Usar CoresUtils para garantir que as cores funcionem corretamente
       Component titleComponent = CoresUtils.colorize(groupInfo.getColor() + target.getUsername());
       Component subtitleComponent = CoresUtils.colorize("§f tornou-se " + groupInfo.getColor() + "[" + groupInfo.getDisplayName() + "]");

       // Enviar title para todos os jogadores
       for (Player player : server.getAllPlayers()) {
           TitleAPI.sendTitle(player, titleComponent, subtitleComponent);
       }
   }

   private void sendUsage(Player sender) {
       sender.sendMessage(legacySection().deserialize(" "));
       sender.sendMessage(legacySection().deserialize("§eUso do comando /grupo:"));
       sender.sendMessage(legacySection().deserialize(" "));
       sender.sendMessage(legacySection().deserialize("§e/grupo listar §8- §7Lista todos os grupos disponíveis"));
       sender.sendMessage(legacySection().deserialize("§e/grupo adicionar <usuário/#id> <grupo> §8- §7Adiciona um usuário ao grupo"));
       sender.sendMessage(legacySection().deserialize("§e/grupo remover <usuário/#id> <grupo> §8- §7Remove um usuário do grupo"));
       sender.sendMessage(legacySection().deserialize("§e/grupo definir <usuário/#id> <grupo> §8- §7Define o grupo principal do usuário e remove todos os outros"));
       sender.sendMessage(legacySection().deserialize(" "));
   }

   @Override
   public List<String> suggest(Invocation invocation) {
       String[] args = invocation.arguments();

       if (args.length == 1) {
           return List.of("adicionar", "remover", "definir", "listar").stream()
                   .filter(action -> action.startsWith(args[0].toLowerCase()))
                   .collect(Collectors.toList());
       } else if (args.length == 2 && !args[0].equalsIgnoreCase("listar")) {
           String arg = args[1].toLowerCase();
           return server.getAllPlayers().stream()
                   .map(Player::getUsername)
                   .filter(name -> name.toLowerCase().startsWith(arg))
                   .collect(Collectors.toList());
       } else if (args.length == 3) {
           return groupManager.getAvailableGroups().keySet().stream()
                   .filter(group -> !group.equals("default"))
                   .filter(group -> group.startsWith(args[2].toLowerCase()))
                   .collect(Collectors.toList());
       }

       return List.of();
   }
}
