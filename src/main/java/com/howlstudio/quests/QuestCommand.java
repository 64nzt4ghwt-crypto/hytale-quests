package com.howlstudio.quests;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class QuestCommand extends AbstractPlayerCommand {
    private final QuestManager manager;

    public QuestCommand(QuestManager manager) {
        super("quest", "Quest system. /quest <list|start|active|abandon|info>");
        this.manager = manager;
    }

    @Override
    protected void execute(CommandContext ctx, Store<EntityStore> store, Ref<EntityStore> ref,
                           PlayerRef playerRef, World world) {
        UUID uuid = playerRef.getUuid();
        if (uuid == null) return;

        String input = ctx.getInputString().trim();
        String[] parts = input.split("\\s+");
        String[] args = parts.length > 1 ? Arrays.copyOfRange(parts, 1, parts.length) : new String[0];

        if (args.length == 0) { sendHelp(playerRef); return; }

        switch (args[0].toLowerCase()) {
            case "list" -> doList(playerRef, uuid);
            case "start" -> {
                if (args.length < 2) { playerRef.sendMessage(Message.raw("§cUsage: /quest start <quest_id>")); return; }
                manager.startQuest(uuid, args[1], playerRef);
            }
            case "active" -> doActive(playerRef, uuid);
            case "abandon" -> {
                if (args.length < 2) { playerRef.sendMessage(Message.raw("§cUsage: /quest abandon <quest_id>")); return; }
                doAbandon(playerRef, uuid, args[1]);
            }
            case "info" -> {
                if (args.length < 2) { playerRef.sendMessage(Message.raw("§cUsage: /quest info <quest_id>")); return; }
                doInfo(playerRef, uuid, args[1]);
            }
            // Admin: record progress manually
            case "progress" -> {
                if (args.length < 3) { playerRef.sendMessage(Message.raw("§cUsage: /quest progress <type> <amount>")); return; }
                QuestObjective.Type type = parseType(args[1]);
                int amount = parseInt(args[2], 1);
                manager.recordProgress(uuid, type, amount, playerRef);
            }
            default -> sendHelp(playerRef);
        }
    }

    private void sendHelp(PlayerRef ref) {
        ref.sendMessage(Message.raw("§6[Quest] §eCommands:"));
        ref.sendMessage(Message.raw("§f/quest list §7— Browse available quests"));
        ref.sendMessage(Message.raw("§f/quest start <id> §7— Start a quest"));
        ref.sendMessage(Message.raw("§f/quest active §7— View active quests + progress"));
        ref.sendMessage(Message.raw("§f/quest abandon <id> §7— Abandon a quest"));
        ref.sendMessage(Message.raw("§f/quest info <id> §7— View quest details"));
    }

    private void doList(PlayerRef ref, UUID uuid) {
        PlayerQuestData data = manager.getData(uuid);
        List<Quest> available = QuestRegistry.getAvailable(data);
        if (available.isEmpty()) {
            ref.sendMessage(Message.raw("§6[Quest] §fNo new quests available. Complete prerequisites first."));
            return;
        }
        ref.sendMessage(Message.raw("§6[Quest] §eAvailable Quests:"));
        for (Quest q : available) {
            String repeat = q.isRepeatable() ? " §7[repeatable]" : "";
            ref.sendMessage(Message.raw("  §f" + q.getId() + " §e— " + q.getName() + repeat));
            ref.sendMessage(Message.raw("    §7" + q.getDescription()));
            ref.sendMessage(Message.raw("    §7Reward: " + q.getReward().getSummary()));
        }
    }

    private void doActive(PlayerRef ref, UUID uuid) {
        PlayerQuestData data = manager.getData(uuid);
        if (data.getActiveQuests().isEmpty()) {
            ref.sendMessage(Message.raw("§6[Quest] §fNo active quests. Use §e/quest list§f to find one."));
            return;
        }
        ref.sendMessage(Message.raw("§6[Quest] §eActive Quests:"));
        for (String questId : data.getActiveQuests()) {
            Quest q = QuestRegistry.getById(questId);
            if (q == null) continue;
            ref.sendMessage(Message.raw("  §e" + q.getName() + " §7[" + questId + "]"));
            for (QuestObjective obj : q.getObjectives()) {
                int current = data.getProgress(questId, obj.getId());
                int target = obj.getTargetCount();
                String done = current >= target ? "§a✓" : "§c□";
                ref.sendMessage(Message.raw("    " + done + " §f" + current + "§7/§f" + target + " §7" + obj.getDescription().replaceAll("\\(.*\\)", "").trim()));
            }
        }
    }

    private void doAbandon(PlayerRef ref, UUID uuid, String questId) {
        Quest quest = QuestRegistry.getById(questId);
        PlayerQuestData data = manager.getData(uuid);
        if (quest == null || !data.isActive(questId)) {
            ref.sendMessage(Message.raw("§c[Quest] Quest not active: §f" + questId));
            return;
        }
        data.abandonQuest(questId, quest);
        ref.sendMessage(Message.raw("§6[Quest] §fAbandoned §e" + quest.getName() + "§f."));
    }

    private void doInfo(PlayerRef ref, UUID uuid, String questId) {
        Quest q = QuestRegistry.getById(questId);
        if (q == null) { ref.sendMessage(Message.raw("§c[Quest] Unknown: §f" + questId)); return; }
        PlayerQuestData data = manager.getData(uuid);

        String status = data.isActive(questId) ? "§aActive" : data.hasCompleted(questId) ? "§7Completed" : "§eAvailable";
        ref.sendMessage(Message.raw("§6[Quest] §e" + q.getName() + " §7[" + status + "§7]"));
        ref.sendMessage(Message.raw("§7" + q.getDescription()));
        ref.sendMessage(Message.raw("§fObjectives:"));
        for (QuestObjective obj : q.getObjectives()) {
            ref.sendMessage(Message.raw("  §7• " + obj.getDescription()));
        }
        ref.sendMessage(Message.raw("§fReward: " + q.getReward().getSummary()));
        if (q.getPrerequisiteId() != null) {
            Quest prereq = QuestRegistry.getById(q.getPrerequisiteId());
            ref.sendMessage(Message.raw("§fRequires: §e" + (prereq != null ? prereq.getName() : q.getPrerequisiteId())));
        }
        if (q.isRepeatable()) ref.sendMessage(Message.raw("§7[Repeatable daily quest]"));
    }

    private QuestObjective.Type parseType(String s) {
        try { return QuestObjective.Type.valueOf(s.toUpperCase()); }
        catch (Exception e) { return QuestObjective.Type.CUSTOM; }
    }

    private int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }
}
