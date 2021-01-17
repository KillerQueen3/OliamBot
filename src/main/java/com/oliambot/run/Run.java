package com.oliambot.run;

import com.oliambot.exception.CatcherIllegalException;
import com.oliambot.exception.LoadException;
import com.oliambot.messages.MessageCenter;
import com.oliambot.messages.TextReader;
import com.oliambot.plugins.PluginManager;
import com.oliambot.setu.NetImageTool;
import com.oliambot.utils.MyLog;
import com.oliambot.utils.Settings;
import com.oliambot.utils.Utils;
import net.mamoe.mirai.event.Events;

public class Run {
    static MessageCenter center;

    public static boolean reload() {
        try {
            Settings.initSettings();
            TextReader.loadTexts();
        } catch (LoadException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        center = new MessageCenter();
        if (!reload()) {
            System.exit(-1);
        }
        try {
            center.decodeDefaultClasses();
        } catch (CatcherIllegalException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        if (!MyBot.login()) {
            System.exit(-1);
        }
        Utils.reload();
        NetImageTool.autoLoginThreadStart(Settings.pixivInterval);
        Events.registerEvents(MyBot.bot, MyBot.getListener(center));

        try {
            PluginManager.loadPlugins();
            Events.registerEvents(MyBot.bot, PluginManager.getHost());
        } catch (Exception e) {
            MyLog.error(e);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            MyBot.bot.close(null);
            try {
                PluginManager.dropAll();
            } catch (Exception e) {
                MyLog.error(e);
            }
            MyLog.info("CLOSED");
        }));

        MyBot.bot.join();
    }
}
