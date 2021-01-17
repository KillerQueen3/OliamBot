package com.oliambot.plugins;

import com.oliambot.inf.Plugin;
import com.oliambot.messages.MessageCenter;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.MessageChain;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class PluginLoader extends MessageCenter {
    private final Map<Long, Boolean> enabled;
    private final Plugin plugin;

    public PluginLoader(Plugin plugin) {
        super();
        this.enabled = new HashMap<>();
        this.plugin = plugin;
    }

    public void drop() throws Exception {
        plugin.drop();
    }

    public void enable(long groupID) {
        enabled.put(groupID, true);
    }

    public void disable(long groupID) {
        enabled.put(groupID, false);
    }

    public boolean isEnabled(long groupID) {
        return enabled.getOrDefault(groupID, true);
    }

    @Override
    public void catchGroupMessage(Member sender, @NotNull MessageChain chain) {
        if (isEnabled(sender.getGroup().getId())) {
            super.catchGroupMessage(sender, chain);
        }
    }
}
