package com.howlstudio.quests;

public class QuestObjective {
    public enum Type { KILL, COLLECT, REACH, TALK, CUSTOM }

    private final String id;
    private final Type type;
    private final String description;
    private final int targetCount;

    public QuestObjective(String id, Type type, String description, int targetCount) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.targetCount = targetCount;
    }

    public String getId() { return id; }
    public Type getType() { return type; }
    public String getDescription() { return description; }
    public int getTargetCount() { return targetCount; }
}
