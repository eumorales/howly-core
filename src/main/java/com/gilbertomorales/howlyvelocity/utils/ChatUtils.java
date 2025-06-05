package com.gilbertomorales.howlyvelocity.utils;

import com.velocitypowered.api.proxy.Player;

public class ChatUtils {

    /**
     * Aplica cores no texto se o jogador tiver permissão
     */
    public static String applyColors(String message, Player player) {
        return CoresUtils.applyColorsWithPermission(message, player.hasPermission("chat.cores"));
    }

    /**
     * Remove códigos de cor de uma mensagem
     */
    public static String stripColors(String message) {
        return CoresUtils.stripColors(message);
    }

    /**
     * Verifica se uma mensagem contém códigos de cor
     */
    public static boolean hasColorCodes(String message) {
        return CoresUtils.hasColorCodes(message);
    }

    /**
     * Converte códigos de cor & para §
     */
    public static String colorize(String message) {
        return CoresUtils.convertAmpersandToSection(message);
    }
}
