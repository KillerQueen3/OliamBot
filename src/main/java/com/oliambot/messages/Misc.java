package com.oliambot.messages;

import com.oliambot.inf.Catch;
import com.oliambot.inf.MessageCatcher;
import com.oliambot.run.MyBot;
import com.oliambot.setu.NetImageTool;
import com.oliambot.utils.MyLog;
import com.oliambot.utils.Settings;
import com.oliambot.utils.TextReader;
import net.dreamlu.mica.http.HttpRequest;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.*;

import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Misc implements MessageCatcher {
    @Catch(entry = "[\\s\\S]*@[\\s\\S]*")
    public static void atEvent(Member sender, MessageChain chain) {
        List<Long> ats = new LinkedList<>();
        for (Message m: chain) {
            if (m instanceof At) {
                ats.add(((At) m).getTarget());
            }
        }
        if (ats.contains(Settings.botID)) {
            String thing = chain.contentToString();
            Pattern p = Pattern.compile(TextReader.getString("offense"));
            Matcher matcher = p.matcher(thing);
            if (matcher.find()) {
                String reply = HttpRequest.get("https://nmsl.shadiao.app/api.php?level=min")
                        .execute()
                        .asString();
                sender.getGroup().sendMessage(reply == null?
                    TextReader.getText("offenseReply", sender):
                    new At(sender).plus(reply));
            }
        }
    }

    @Catch(entry = "(?i)[\\s\\S]*to[:：]\\d+[\\s\\S]*", listen = Catch.ON_FRIEND)
    public static void forward(Friend sender, MessageChain chain) {
        String target = chain.contentToString().replaceAll("\\[.*]", "")
                .replaceAll("(?i)to[:：]", "").replaceAll("[ \\n\\t\\r]", "");
        if (!target.matches("^\\d+$")) {
            return;
        }
        long groupId = Long.parseLong(target);
        Group targetGroup;
        try {
            targetGroup = MyBot.bot.getGroup(groupId);
        } catch (NoSuchElementException e) {
            sender.sendMessage("bot不在群中！");
            return;
        }
        long friendId = sender.getId();
        Member m = targetGroup.getOrNull(friendId);
        if (m == null) {
            sender.sendMessage("您不在此群中！");
            return;
        }
        sender.sendMessage("处理中...");
        Image image = null;
        for (Message msg : chain) {
            if (msg instanceof Image) {
                image = (Image) msg;
                break;
            }
        }
        if (image == null) {
            sender.sendMessage("未找到图片！");
            return;
        }
        String imageUrl = "http://gchat.qpic.cn/gchatpic_new/0/0-0-" +
                image.getImageId().split("-")[2] +
                "/0?term=2";
        MyLog.info("Forward from: {}, to: {}", friendId, groupId);
        BufferedImage bufferedImage = NetImageTool.getUrlImg(imageUrl);
        if (bufferedImage == null) {
            sender.sendMessage("失败！");
            return;
        }
        NetImageTool.r18Image(bufferedImage);

        targetGroup.sendMessage(MessageUtils.newChain("转发自").plus(new At(m))
                .plus(targetGroup.uploadImage(bufferedImage)));
        sender.sendMessage("成功！");
    }
}
