package com.howlstudio.quests;

import java.util.List;

public class Quest {
    private final String id;
    private final String name;
    private final String description;
    private final List<QuestObjective> objectives;
    private final QuestReward reward;
    private final boolean repeatable;
    private final String prerequisiteId; // null if no prerequisite

    public Quest(String id, String name, String description,
                 List<QuestObjective> objectives, QuestReward reward,
                 boolean repeatable, String prerequisiteId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.objectives = objectives;
        this.reward = reward;
        this.repeatable = repeatable;
        this.prerequisiteId = prerequisiteId;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<QuestObjective> getObjectives() { return objectives; }
    public QuestReward getReward() { return reward; }
    public boolean isRepeatable() { return repeatable; }
    public String getPrerequisiteId() { return prerequisiteId; }
}
