package com.oliambot.messages;

import com.oliambot.inf.Catch;
import com.oliambot.inf.MessageCatcher;
import com.oliambot.run.Run;
import com.oliambot.utils.Utils;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.MessageChain;

public class Control implements MessageCatcher {
    @Catch(entry = "^=.+", permission = Catch.SUPER_USER, listen = Catch.ON_FRIEND)
    public static void control(Friend sender, MessageChain chain) {
        String cmd = chain.contentToString().replaceAll("=", "");
        switch (cmd) {
            case "读取翻译":
                Utils.reload();
                sender.sendMessage("读取完成！");
                return;
            case "reload":
                sender.sendMessage(Run.reload()? "成功！": "失败！");
                return;
            default:
                sender.sendMessage("未知指令。");
        }
    }
}
