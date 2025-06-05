package com.gilbertomorales.howlyvelocity.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class CoresUtils {

    /**
     * Converte códigos de cor & para § (método principal para compatibilidade)
     */
    public static String colorir(String text) {
        return convertAmpersandToSection(text);
    }

    /**
     * Converte texto com códigos legacy para Component (alias para colorize)
     */
    public static Component fromLegacy(String text) {
        return colorize(text);
    }

    /**
     * Converte códigos de cor & para §
     */
    public static String convertAmpersandToSection(String text) {
        if (text == null) return "";

        return text.replace("&0", "§0")
                .replace("&1", "§1")
                .replace("&2", "§2")
                .replace("&3", "§3")
                .replace("&4", "§4")
                .replace("&5", "§5")
                .replace("&6", "§6")
                .replace("&7", "§7")
                .replace("&8", "§8")
                .replace("&9", "§9")
                .replace("&a", "§a")
                .replace("&b", "§b")
                .replace("&c", "§c")
                .replace("&d", "§d")
                .replace("&e", "§e")
                .replace("&f", "§f")
                .replace("&k", "§k")
                .replace("&l", "§l")
                .replace("&m", "§m")
                .replace("&n", "§n")
                .replace("&o", "§o")
                .replace("&r", "§r");
    }

    /**
     * Converte string com códigos de cor para Component
     */
    public static Component colorize(String text) {
        if (text == null) {
            return Component.empty();
        }

        // Primeiro converte & para §
        String converted = convertAmpersandToSection(text);

        // Usa o LegacyComponentSerializer para converter § para Component
        return LegacyComponentSerializer.legacySection().deserialize(converted);
    }

    /**
     * Aplica cores no texto se tiver permissão
     */
    public static String applyColorsWithPermission(String message, boolean hasPermission) {
        if (hasPermission) {
            return convertAmpersandToSection(message);
        }
        return message;
    }

    /**
     * Remove códigos de cor de uma mensagem
     */
    public static String stripColors(String message) {
        if (message == null) return "";

        return message.replaceAll("§[0-9a-fk-or]", "")
                .replaceAll("&[0-9a-fk-or]", "");
    }

    /**
     * Verifica se uma mensagem contém códigos de cor
     */
    public static boolean hasColorCodes(String message) {
        if (message == null) return false;

        return message.matches(".*[§&][0-9a-fk-or].*");
    }

    /**
     * Converte código de cor para TextColor
     */
    public static TextColor getTextColorFromCode(String colorCode) {
        if (colorCode == null || colorCode.isEmpty()) {
            return TextColor.color(255, 255, 255);
        }

        char code = colorCode.charAt(colorCode.length() - 1);
        return switch (code) {
            case '0' -> TextColor.color(0, 0, 0);          // Preto
            case '1' -> TextColor.color(0, 0, 170);        // Azul escuro
            case '2' -> TextColor.color(0, 170, 0);        // Verde escuro
            case '3' -> TextColor.color(0, 170, 170);      // Ciano
            case '4' -> TextColor.color(170, 0, 0);        // Vermelho escuro
            case '5' -> TextColor.color(170, 0, 170);      // Roxo
            case '6' -> TextColor.color(255, 170, 0);      // Dourado
            case '7' -> TextColor.color(170, 170, 170);    // Cinza
            case '8' -> TextColor.color(85, 85, 85);       // Cinza escuro
            case '9' -> TextColor.color(85, 85, 255);      // Azul
            case 'a' -> TextColor.color(85, 255, 85);      // Verde
            case 'b' -> TextColor.color(85, 255, 255);     // Azul claro
            case 'c' -> TextColor.color(255, 85, 85);      // Vermelho
            case 'd' -> TextColor.color(255, 85, 255);     // Rosa
            case 'e' -> TextColor.color(255, 255, 85);     // Amarelo
            case 'f' -> TextColor.color(255, 255, 255);    // Branco
            default -> TextColor.color(255, 255, 255);
        };
    }
}
