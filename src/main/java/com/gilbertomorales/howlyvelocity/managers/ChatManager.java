package com.gilbertomorales.howlyvelocity.managers;

import com.gilbertomorales.howlyvelocity.api.HowlyAPI;
import com.gilbertomorales.howlyvelocity.api.punishment.PunishmentAPI;
import com.velocitypowered.api.proxy.ProxyServer;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ChatManager {
    private final ProxyServer server;
    private final TagManager tagManager;
    private final MedalManager medalManager;
    private PunishmentAPI punishmentAPI;

    public ChatManager(ProxyServer server, TagManager tagManager, MedalManager medalManager) {
        this.server = server;
        this.tagManager = tagManager;
        this.medalManager = medalManager;
    }

    /**
     * Inicializa a API de punições após a HowlyAPI estar disponível
     */
    public void initializePunishmentAPI() {
        try {
            HowlyAPI api = HowlyAPI.getInstance();
            if (api != null) {
                this.punishmentAPI = api.getPunishmentAPI();
            }
        } catch (Exception e) {
            // Log do erro se necessário
            this.punishmentAPI = null;
        }
    }
    
    /**
     * Verifica se um jogador está mutado de forma síncrona
     * @param uuid UUID do jogador
     * @return true se o jogador estiver mutado, false caso contrário
     */
    public boolean isPlayerMuted(UUID uuid) {
        if (punishmentAPI == null) {
            return false;
        }
        
        try {
            // Obter o resultado de forma síncrona com timeout
            return punishmentAPI.getActiveMute(uuid)
                .thenApply(mute -> mute != null)
                .get(500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            // Em caso de erro, assumir que não está mutado
            return false;
        }
    }
}
