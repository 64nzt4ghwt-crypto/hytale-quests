package com.howlstudio.quests;

import java.util.*;

public class PlayerQuestData {
    // objectiveId → current progress
    private final Map<String, Integer> progress = new HashMap<>();
    // questId → completed timestamp
    private final Map<String, Long> completed = new HashMap<>();
    // Currently active quest IDs
    private final Set<String> active = new LinkedHashSet<>();

    public boolean hasCompleted(String questId) { return completed.containsKey(questId); }
    public boolean isActive(String questId) { return active.contains(questId); }
    public Set<String> getActiveQuests() { return Collections.unmodifiableSet(active); }

    public void startQuest(String questId) { active.add(questId); }

    public void completeQuest(String questId) {
        active.remove(questId);
        completed.put(questId, System.currentTimeMillis());
    }

    public void abandonQuest(String questId, Quest quest) {
        active.remove(questId);
        // Clear objective progress for this quest
        for (QuestObjective obj : quest.getObjectives()) {
            progress.remove(questId + ":" + obj.getId());
        }
    }

    public int getProgress(String questId, String objectiveId) {
        return progress.getOrDefault(questId + ":" + objectiveId, 0);
    }

    public void addProgress(String questId, String objectiveId, int amount) {
        String key = questId + ":" + objectiveId;
        progress.merge(key, amount, Integer::sum);
    }

    public boolean isObjectiveComplete(String questId, QuestObjective obj) {
        return getProgress(questId, obj.getId()) >= obj.getTargetCount();
    }

    public boolean isQuestComplete(Quest quest) {
        for (QuestObjective obj : quest.getObjectives()) {
            if (!isObjectiveComplete(quest.getId(), obj)) return false;
        }
        return true;
    }

    public Map<String, Long> getCompletedQuests() { return Collections.unmodifiableMap(completed); }
}
