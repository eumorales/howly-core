package com.gilbertomorales.howlyvelocity.utils;

import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

public class TitleAPI {

    public static void sendTitle(Player player, Component title, Component subtitle) {
        player.showTitle(Title.title(title, subtitle));
    }

    // Manter compatibilidade com strings (convertendo para Component)
    public static void sendTitle(Player player, String title, String subtitle) {
        Component titleComponent = CoresUtils.fromLegacy(title);
        Component subtitleComponent = CoresUtils.fromLegacy(subtitle);
        player.showTitle(Title.title(titleComponent, subtitleComponent));
    }
}
