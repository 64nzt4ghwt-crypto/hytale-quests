package com.howlstudio.quests;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.UUID;

public class QuestListener {
    private final QuestManager manager;

    public QuestListener(QuestManager manager) {
        this.manager = manager;
    }

    public void register() {
        var bus = HytaleServer.get().getEventBus();
        bus.registerGlobal(PlayerReadyEvent.class, this::onPlayerReady);
        bus.registerGlobal(PlayerDisconnectEvent.class, this::onPlayerDisconnect);
        bus.registerGlobal(PlayerChatEvent.class, this::onPlayerChat);
    }

    private void onPlayerReady(PlayerReadyEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;
        PlayerRef ref = player.getPlayerRef();
        if (ref == null) return;
        UUID uuid = ref.getUuid();
        String name = ref.getUsername() != null ? ref.getUsername() : (uuid != null ? uuid.toString().substring(0, 8) : "?");
        if (uuid != null) {
            manager.trackName(uuid, name);
            // Notify player of active quests
            PlayerQuestData data = manager.getData(uuid);
            if (!data.getActiveQuests().isEmpty()) {
                ref.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                    "§6[Quest] §fYou have §e" + data.getActiveQuests().size() + " §factive quest(s). Use §e/quest list§f."));
            }
        }
    }

    private void onPlayerDisconnect(PlayerDisconnectEvent event) {
        PlayerRef ref = event.getPlayerRef();
        if (ref == null) return;
        UUID uuid = ref.getUuid();
        if (uuid != null) manager.onLeave(uuid);
    }

    private void onPlayerChat(PlayerChatEvent event) {
        if (event.isCancelled()) return;
        PlayerRef sender = event.getSender();
        if (sender == null) return;
        UUID uuid = sender.getUuid();
        if (uuid == null) return;

        // "chat_once" objective: any chat message triggers it
        manager.recordProgress(uuid, QuestObjective.Type.CUSTOM, 1, sender);
    }
}
