package com.howlstudio.quests;

import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

public final class QuestSystemPlugin extends JavaPlugin {

    private QuestManager questManager;

    public QuestSystemPlugin(JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        System.out.println("[QuestSystem] Loading...");
        questManager = new QuestManager();
        new QuestListener(questManager).register();
        CommandManager.get().register(new QuestCommand(questManager));
        System.out.println("[QuestSystem] Loaded " + QuestRegistry.getAll().size() + " quests. /quest available.");
    }
}
