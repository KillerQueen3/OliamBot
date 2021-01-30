package com.oliambot.messages;

import com.oliambot.inf.Catch;
import com.oliambot.inf.MessageCatcher;
import com.oliambot.plugins.PluginManager;
import com.oliambot.utils.MyLog;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.MessageChain;

public class PluginCMD implements MessageCatcher {
    @Catch(entry = "^卸载\\S+", permission = Catch.OWNER)
    public static void drop(Member sender, MessageChain chain) {
        String name = chain.contentToString().replaceFirst("卸载", "");
        String msg = "";
        try {
            msg = PluginManager.dropPlugin(name)? "成功！": ("未找到" + name);
        } catch (Exception e) {
            msg = "卸载出错！";
            MyLog.error(e);
        }
        sender.getGroup().sendMessage(msg);
    }

    @Catch(entry = "^插件列表$")
    public static void list(Member sender, MessageChain chain) {
        sender.getGroup().sendMessage(PluginManager.getPluginList(sender.getGroup().getId()));
    }

    @Catch(entry = "^加载插件$", permission = Catch.OWNER)
    public static void load(Member sender, MessageChain chain) throws Exception {
        try {
            PluginManager.loadPlugins();
            sender.getGroup().sendMessage("完成。  " + PluginManager.getPluginList(sender.getGroup().getId()));
        } catch (Exception e) {
            sender.getGroup().sendMessage("加载插件时发生错误！信息：" + e.getMessage());
            throw e;
        }
    }

    @Catch(entry = "^=禁用\\S+", permission = Catch.ADMIN)
    public static void disable(Member sender, MessageChain chain) {
        String name = chain.contentToString().replaceFirst("禁用", "");
        sender.getGroup().sendMessage(PluginManager.disablePlugin(name, sender.getGroup().getId())? "成功！": ("未找到" + name));
    }

    @Catch(entry = "^=启用\\S+", permission = Catch.ADMIN)
    public static void enable(Member sender, MessageChain chain) {
        String name = chain.contentToString().replaceFirst("启用", "");
        sender.getGroup().sendMessage(PluginManager.enablePlugin(name, sender.getGroup().getId())? "成功！": ("未找到" + name));
    }
}
