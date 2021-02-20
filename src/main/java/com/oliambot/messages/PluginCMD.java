package com.oliambot.messages;

import com.oliambot.inf.Catch;
import com.oliambot.inf.MessageCatcher;
import com.oliambot.plugins.PluginManager;
import com.oliambot.utils.MyLog;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.MessageChain;

public class PluginCMD implements MessageCatcher {
    @Catch(entry = "^卸载\\S+", permission = Catch.SUPER_USER, listen = Catch.ON_FRIEND)
    public static void drop(Friend sender, MessageChain chain) {
        String name = chain.contentToString().replaceFirst("卸载", "");
        String msg = "";
        try {
            msg = PluginManager.dropPlugin(name) ? "成功！" : ("未找到" + name);
        } catch (Exception e) {
            msg = "卸载出错！";
            MyLog.error(e);
        }
        sender.sendMessage(msg);
    }

    @Catch(entry = "^插件列表$")
    public static void list(Member sender, MessageChain chain) {
        sender.getGroup().sendMessage(PluginManager.getPluginList(sender.getGroup().getId()));
    }

    @Catch(entry = "^加载插件$", permission = Catch.SUPER_USER, listen = Catch.ON_FRIEND)
    public static void load(Friend sender, MessageChain chain) throws Exception {
        try {
            PluginManager.loadPlugins();
            sender.sendMessage("完成。");
        } catch (Exception e) {
            sender.sendMessage("加载插件时发生错误！信息：" + e.getMessage());
            throw e;
        }
    }

    @Catch(entry = "^重载插件$", permission = Catch.SUPER_USER, listen = Catch.ON_FRIEND)
    public static void reload(Friend sender, MessageChain chain) throws Exception {
        try {
            PluginManager.dropAll();
            PluginManager.loadPlugins();
            sender.sendMessage("完成。");
        } catch (Exception e) {
            sender.sendMessage("加载插件时发生错误！信息：" + e.getMessage());
            throw e;
        }
    }

    @Catch(entry = "^重新加载.+", permission = Catch.SUPER_USER, listen = Catch.ON_FRIEND)
    public static void reloadOne(Friend sender, MessageChain chain) throws Exception {
        try {
            String name = chain.contentToString().replaceFirst("重新加载", "");
            if (PluginManager.dropPlugin(name)) {
                PluginManager.loadPlugins();
                sender.sendMessage("完成。");
            } else {
                sender.sendMessage("未找到" + name);
            }
        } catch (Exception e) {
            sender.sendMessage("加载插件时发生错误！信息：" + e.getMessage());
            throw e;
        }
    }

    @Catch(entry = "^=禁用\\S+", permission = Catch.ADMIN)
    public static void disable(Member sender, MessageChain chain) {
        String name = chain.contentToString().replaceFirst("禁用", "");
        sender.getGroup().sendMessage(PluginManager.disablePlugin(name, sender.getGroup().getId()) ? "成功！" : ("未找到" + name));
    }

    @Catch(entry = "^=启用\\S+", permission = Catch.ADMIN)
    public static void enable(Member sender, MessageChain chain) {
        String name = chain.contentToString().replaceFirst("启用", "");
        sender.getGroup().sendMessage(PluginManager.enablePlugin(name, sender.getGroup().getId()) ? "成功！" : ("未找到" + name));
    }
}
