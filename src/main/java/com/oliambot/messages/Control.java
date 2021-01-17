package com.oliambot.messages;

import com.oliambot.inf.Catch;
import com.oliambot.inf.MessageCatcher;
import com.oliambot.run.Run;
import com.oliambot.utils.Utils;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.MessageChain;

public class Control implements MessageCatcher {
    @Catch(entry = "^=.+", permission = Catch.ADMIN)
    public static void control(Member sender, MessageChain chain) {
        String cmd = chain.contentToString().replaceAll("=", "");
        switch (cmd) {
            case "读取翻译":
                Utils.reload();
                sender.getGroup().sendMessage("读取完成！");
                return;
            case "reload":
                sender.getGroup().sendMessage(Run.reload()? "成功！": "失败！");
                return;
            default:
                //sender.getGroup().sendMessage("未知指令。");
        }
    }
}
