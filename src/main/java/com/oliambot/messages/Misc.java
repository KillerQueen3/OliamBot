package com.oliambot.messages;

import com.oliambot.inf.Catch;
import com.oliambot.inf.MessageCatcher;
import com.oliambot.run.MyBot;
import com.oliambot.setu.NetImageTool;
import com.oliambot.utils.MyLog;
import com.oliambot.utils.Settings;
import com.oliambot.utils.TextReader;
import com.oliambot.utils.Utils;
import net.dreamlu.mica.http.HttpRequest;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.ExternalResource;

import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
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
                    new At(sender.getId()).plus(reply));
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


        Group targetGroup = MyBot.bot.getGroup(groupId);
        if (targetGroup == null) {
            sender.sendMessage("bot不在群中！");
            return;
        }
        long friendId = sender.getId();
        Member m = targetGroup.get(friendId);
        if (m == null) {
            sender.sendMessage("您不在此群中！");
            return;
        }
        sender.sendMessage("处理中...");
        List<Image> images = Utils.getImages(chain);
        if (images.size() == 0) {
            sender.sendMessage("未找到图片！");
            return;
        }

        for (Image image : images) {
            String imageUrl = NetImageTool.getImageURL(image);
            MyLog.info("Forward {} from: {}, to: {}", imageUrl, friendId, groupId);
            BufferedImage bufferedImage = NetImageTool.getUrlImg(imageUrl);
            if (bufferedImage == null) {
                sender.sendMessage("失败！");
                return;
            }
            NetImageTool.r18Image(bufferedImage);
            try {
                ExternalResource resource = ExternalResource.create(Utils.bufferedImageToBytes(bufferedImage));
                targetGroup.sendMessage(MessageUtils.newChain(new PlainText("From: ")).plus(new At(m.getId()))
                        .plus(targetGroup.uploadImage(resource)));
                resource.close();
                sender.sendMessage("成功！");
            } catch (Exception e) {
                sender.sendMessage("失败。");
            }
        }
    }
}
