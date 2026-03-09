package com.howlstudio.quests;

import java.util.*;

public class QuestRegistry {
    private static final Map<String, Quest> QUESTS = new LinkedHashMap<>();

    static {
        // --- Tier 1: Starter Quests ---
        register(new Quest(
            "first_steps", "First Steps",
            "Welcome to the world! Explore your surroundings.",
            List.of(
                new QuestObjective("chat_once", QuestObjective.Type.CUSTOM, "Say something in chat (1/1)", 1)
            ),
            QuestReward.of(10, 25),
            false, null
        ));

        register(new Quest(
            "warrior_initiate", "Warrior Initiate",
            "Prove yourself in combat by defeating enemies.",
            List.of(
                new QuestObjective("kill_5", QuestObjective.Type.KILL, "Defeat 5 enemies (0/5)", 5)
            ),
            QuestReward.of(50, 100),
            false, "first_steps"
        ));

        register(new Quest(
            "resource_run", "Resource Run",
            "Gather basic resources to prepare for adventure.",
            List.of(
                new QuestObjective("collect_wood", QuestObjective.Type.COLLECT, "Collect 10 wood (0/10)", 10),
                new QuestObjective("collect_stone", QuestObjective.Type.COLLECT, "Collect 5 stone (0/5)", 5)
            ),
            QuestReward.of(30, 75),
            true, null
        ));

        // --- Tier 2: Advanced Quests ---
        register(new Quest(
            "elite_hunter", "Elite Hunter",
            "Become a true hunter — take down powerful foes.",
            List.of(
                new QuestObjective("kill_25", QuestObjective.Type.KILL, "Defeat 25 enemies (0/25)", 25)
            ),
            QuestReward.of(200, 500),
            false, "warrior_initiate"
        ));

        register(new Quest(
            "master_crafter", "Master Crafter",
            "Gather rare materials for advanced crafting.",
            List.of(
                new QuestObjective("collect_iron", QuestObjective.Type.COLLECT, "Collect 20 iron (0/20)", 20),
                new QuestObjective("collect_gems", QuestObjective.Type.COLLECT, "Collect 5 gems (0/5)", 5)
            ),
            QuestReward.of(150, 400),
            false, "resource_run"
        ));

        register(new Quest(
            "explorer", "Explorer",
            "Journey far and discover new lands.",
            List.of(
                new QuestObjective("reach_zone", QuestObjective.Type.REACH, "Reach the ancient ruins (0/1)", 1)
            ),
            QuestReward.of(100, 300),
            false, "first_steps"
        ));

        // --- Daily / Repeatable ---
        register(new Quest(
            "daily_hunt", "Daily Hunt",
            "The hunt never ends. Complete it daily for bonus rewards.",
            List.of(
                new QuestObjective("kill_10_daily", QuestObjective.Type.KILL, "Defeat 10 enemies (0/10)", 10)
            ),
            QuestReward.of(75, 150),
            true, null
        ));
    }

    private static void register(Quest q) { QUESTS.put(q.getId(), q); }

    public static Quest getById(String id) { return QUESTS.get(id); }
    public static Collection<Quest> getAll() { return QUESTS.values(); }

    public static List<Quest> getAvailable(PlayerQuestData data) {
        List<Quest> result = new ArrayList<>();
        for (Quest q : QUESTS.values()) {
            if (data.isActive(q.getId())) continue;
            if (data.hasCompleted(q.getId()) && !q.isRepeatable()) continue;
            if (q.getPrerequisiteId() != null && !data.hasCompleted(q.getPrerequisiteId())) continue;
            result.add(q);
        }
        return result;
    }
}
