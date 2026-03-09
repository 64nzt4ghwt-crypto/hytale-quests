package com.howlstudio.quests;

public class QuestReward {
    private final int gold;
    private final int xp;
    private final String itemId;   // Optional item reward ID
    private final int itemCount;

    public QuestReward(int gold, int xp, String itemId, int itemCount) {
        this.gold = gold;
        this.xp = xp;
        this.itemId = itemId;
        this.itemCount = itemCount;
    }

    public static QuestReward of(int gold, int xp) {
        return new QuestReward(gold, xp, null, 0);
    }

    public int getGold() { return gold; }
    public int getXp() { return xp; }
    public String getItemId() { return itemId; }
    public int getItemCount() { return itemCount; }

    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        if (gold > 0) sb.append("§6").append(gold).append(" gold§f");
        if (xp > 0) { if (!sb.isEmpty()) sb.append(", "); sb.append("§b").append(xp).append(" XP§f"); }
        if (itemId != null) { if (!sb.isEmpty()) sb.append(", "); sb.append("§e").append(itemCount).append("x ").append(itemId).append("§f"); }
        return sb.isEmpty() ? "nothing" : sb.toString();
    }
}
