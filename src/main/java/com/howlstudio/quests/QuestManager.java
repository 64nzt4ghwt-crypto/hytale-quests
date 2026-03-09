package com.howlstudio.quests;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class QuestManager {
    private final Map<UUID, PlayerQuestData> playerData = new ConcurrentHashMap<>();
    private final Map<UUID, String> names = new ConcurrentHashMap<>();

    public void trackName(UUID uuid, String name) { names.put(uuid, name); }
    public String getName(UUID uuid) { return names.getOrDefault(uuid, "?"); }

    public PlayerQuestData getData(UUID uuid) {
        return playerData.computeIfAbsent(uuid, k -> new PlayerQuestData());
    }

    public void onLeave(UUID uuid) {
        // Keep data in memory; real implementation would save to disk here
    }

    /** Start a quest for a player. Returns false if not startable. */
    public boolean startQuest(UUID uuid, String questId, PlayerRef ref) {
        Quest quest = QuestRegistry.getById(questId);
        if (quest == null) { ref.sendMessage(Message.raw("§c[Quest] Unknown quest: §f" + questId)); return false; }

        PlayerQuestData data = getData(uuid);
        if (data.isActive(questId)) { ref.sendMessage(Message.raw("§c[Quest] Already active.")); return false; }
        if (data.hasCompleted(questId) && !quest.isRepeatable()) {
            ref.sendMessage(Message.raw("§c[Quest] §e" + quest.getName() + " §calready completed."));
            return false;
        }
        if (quest.getPrerequisiteId() != null && !data.hasCompleted(quest.getPrerequisiteId())) {
            Quest prereq = QuestRegistry.getById(quest.getPrerequisiteId());
            String prereqName = prereq != null ? prereq.getName() : quest.getPrerequisiteId();
            ref.sendMessage(Message.raw("§c[Quest] Requires §f" + prereqName + " §cfirst."));
            return false;
        }

        data.startQuest(questId);
        ref.sendMessage(Message.raw("§6[Quest] §aStarted: §e" + quest.getName()));
        ref.sendMessage(Message.raw("§7" + quest.getDescription()));
        for (QuestObjective obj : quest.getObjectives()) {
            ref.sendMessage(Message.raw("  §f□ " + obj.getDescription()));
        }
        return true;
    }

    /** Record progress on an objective type for all active quests. */
    public void recordProgress(UUID uuid, QuestObjective.Type type, int amount, PlayerRef ref) {
        PlayerQuestData data = getData(uuid);
        for (String questId : data.getActiveQuests()) {
            Quest quest = QuestRegistry.getById(questId);
            if (quest == null) continue;
            for (QuestObjective obj : quest.getObjectives()) {
                if (obj.getType() != type) continue;
                if (data.isObjectiveComplete(questId, obj)) continue;
                data.addProgress(questId, obj.getId(), amount);
                int current = data.getProgress(questId, obj.getId());
                int target = obj.getTargetCount();
                if (data.isObjectiveComplete(questId, obj)) {
                    ref.sendMessage(Message.raw("§6[Quest] §a✓ §e" + quest.getName() + " §7— " + obj.getDescription().replaceAll("\\(.*\\)", "").trim() + " §adone!"));
                    checkQuestCompletion(uuid, questId, data, ref);
                } else {
                    ref.sendMessage(Message.raw("§6[Quest] §e" + quest.getName() + " §7— §f" + current + "§7/§f" + target));
                }
            }
        }
    }

    private void checkQuestCompletion(UUID uuid, String questId, PlayerQuestData data, PlayerRef ref) {
        Quest quest = QuestRegistry.getById(questId);
        if (quest == null) return;
        if (!data.isQuestComplete(quest)) return;

        data.completeQuest(questId);
        QuestReward reward = quest.getReward();

        ref.sendMessage(Message.raw(""));
        ref.sendMessage(Message.raw("§6★ §eQuest Complete: §f" + quest.getName() + " §6★"));
        ref.sendMessage(Message.raw("§fReward: " + reward.getSummary()));
        ref.sendMessage(Message.raw(""));

        // Check for newly unlocked quests
        PlayerQuestData fresh = getData(uuid);
        for (Quest q : QuestRegistry.getAll()) {
            if (questId.equals(q.getPrerequisiteId()) && !fresh.hasCompleted(q.getId())) {
                ref.sendMessage(Message.raw("§6[Quest] §fNew quest unlocked: §e" + q.getName()));
            }
        }
    }
}
