package com.oliambot.run;

import com.oliambot.messages.MessageCenter;
import com.oliambot.utils.MyLog;
import com.oliambot.utils.Settings;
import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactoryJvm;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.message.FriendMessageEvent;
import net.mamoe.mirai.message.GroupMessageEvent;
import net.mamoe.mirai.network.LoginFailedException;
import net.mamoe.mirai.utils.BotConfiguration;
import org.jetbrains.annotations.NotNull;

public class MyBot {
    public static long startTime;
    public static String nick;

    public final static Bot bot = BotFactoryJvm.newBot(Settings.botID, Settings.botPW, new BotConfiguration() {
        {
            fileBasedDeviceInfo("deviceInfo.json");
            setProtocol(MiraiProtocol.ANDROID_PHONE);
        }
    });

    public static boolean login() {
        try {
            bot.login();
            startTime = System.currentTimeMillis();
            nick = bot.getNick();
            MyLog.info("LOGIN");
            return true;
        } catch (LoginFailedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static SimpleListenerHost getListener(MessageCenter center) {
        return new SimpleListenerHost() {
            @EventHandler
            public ListeningStatus onGroupMessageEvent(GroupMessageEvent event) throws Exception {
                center.catchGroupMessage(event.getSender(), event.getMessage());
                return ListeningStatus.LISTENING;
            }

            @EventHandler
            public ListeningStatus onFriendMessageEvent(FriendMessageEvent event) throws Exception {
                center.catchFriendMessage(event.getSender(), event.getMessage());
                return ListeningStatus.LISTENING;
            }

            @Override
            public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception) {
                super.handleException(context, exception);
                MyLog.error(exception);
            }
        };
    }
}
