package com.oliambot.messages;

import com.oliambot.entity.History;
import com.oliambot.entity.PixivImage;
import com.oliambot.inf.Catch;
import com.oliambot.inf.MessageCatcher;
import com.oliambot.setu.ImageSearch;
import com.oliambot.setu.NetImageTool;
import com.oliambot.setu.SetuCenter;
import com.oliambot.utils.MyLog;
import com.oliambot.utils.Utils;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.*;

import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Setu implements MessageCatcher {
    @Catch(entry = "^来[点份张].*[色涩]图.*")
    public static void search(Member sender, MessageChain chain) {
        List<String> banned = TextReader.getStrings("bannedTags", "-");
        String content = chain.contentToString();
        for (String b : banned) {
            if (content.contains(b)) {
                sender.getGroup().sendMessage(new At(sender).plus("不准" + b + "！"));
                return;
            }
        }
        sender.getGroup().sendMessage(TextReader.getText("requireImageReply", sender));
        String cmd = SetuCenter.SEARCH + content.replaceAll("^来[点份张]", "").replaceAll("[涩色]图.*", "");
        long res = sendImage(cmd, sender.getGroup(), true);
        Utils.writeHistory(new History(sender.getNameCard(), sender.getId(), sender.getGroup().getName(), sender.getGroup().getId(), cmd, "SEARCH", res));
    }

    @Catch(entry = "^重置记录\\S+.*")
    public static void clean(Member sender, MessageChain chain) {
        String c = chain.contentToString().replaceAll("重置记录", "");
        boolean r18 = c.matches(".*[rR]-?18.*");
        if (r18) {
            c = c.replaceAll("[rR]-?18", "");
        }
        sender.getGroup().sendMessage(Utils.cleanCache(
                sender.getGroup().getId(), Utils.getTrans(c) + (r18 ? " R-18" : ""))
                ? "成功!" : "未找到相关记录!");
    }

    @Catch(entry = "^[多再][来色涩]点.*|^不够[涩色].*")
    public static void more(Member sender, MessageChain chain) {
        String cmd = SetuCenter.getMore(sender.getGroup().getId());
        if (cmd == null)
            return;
        sender.getGroup().sendMessage(TextReader.getText("moreImageReply", sender));
        long res = sendImage(cmd, sender.getGroup(), false);
        Utils.writeHistory(new History(sender.getNameCard(), sender.getId(), sender.getGroup().getName(), sender.getGroup().getId(), cmd, "MORE", res));
    }

    @Catch(entry = "^来点推荐.*|^推荐[色涩]图.*")
    public static void recommend(Member sender, MessageChain chain) {
        sender.getGroup().sendMessage(TextReader.getText("recommendReply", sender));
        long res = sendImage(SetuCenter.RECOMMEND, sender.getGroup(), true);
        Utils.writeHistory(new History(sender.getNameCard(), sender.getId(), sender.getGroup().getName(), sender.getGroup().getId(), null, "RECOMMEND", res));
    }

    @Catch(entry = "^查图\\d+")
    public static void illustIdSearch(Member sender, MessageChain chain) {
        sender.getGroup().sendMessage(TextReader.getText("requireImageReply", sender));
        sendImage(SetuCenter.ILLUST + chain.contentToString().replaceAll("查图", ""),
                sender.getGroup(), false);
    }

    @Catch(entry = "^作者\\d+")
    public static void artistIdSearch(Member sender, MessageChain chain) {
        sender.getGroup().sendMessage(TextReader.getText("requireImageReply", sender));
        sendImage(SetuCenter.ARTIST + chain.contentToString().replaceAll("作者", ""),
                sender.getGroup(), true);
    }

    @Catch(entry = "^搜图[\\s\\S]*")
    public static void searchByImage(Member sender, MessageChain chain) {
        Image image = null;
        for (Message msg : chain) {
            if (msg instanceof Image) {
                image = (Image) msg;
                break;
            }
        }
        if (image == null) {
            return;
        }
        sender.getGroup().sendMessage(TextReader.getText("requireImageReply", sender));
        String[][] res = ImageSearch.searchAscii2d(NetImageTool.getImageURL(image));
        if (res == null) {
            sender.getGroup().sendMessage("搜索失败！");
            return;
        }
        ExecutorService service = Executors.newCachedThreadPool();
        service.submit(() -> {
            try {
                sender.getGroup().sendMessage(MessageUtils.newChain("ascii2d色合检索：\n")
                        .plus(sender.getGroup().uploadImage(new URL(res[0][0])))
                        .plus("\n链接: " + res[0][1]));
            } catch (Exception e) {
                sender.getGroup().sendMessage("ascii2d色合检索：\n图片获取失败！\n链接: " + res[0][1]);
            }
        });
        if (res[1] == null) {
            sender.getGroup().sendMessage("特征搜索失败！");
        } else {
            service.submit(() -> {
                try {
                    sender.getGroup().sendMessage(MessageUtils.newChain("ascii2d特征检索：\n")
                            .plus(sender.getGroup().uploadImage(new URL(res[1][0])))
                            .plus("\n链接: " + res[1][1]));
                } catch (Exception e) {
                    sender.getGroup().sendMessage("ascii2d特征检索：\n图片获取失败！\n链接: " + res[1][1]);
                }
            });
        }
        service.shutdown();
        try {
            if (!service.awaitTermination(30, TimeUnit.SECONDS)) {
                service.shutdownNow();
            }
        } catch (Exception e) {
            MyLog.error(e);
            e.printStackTrace();
        }
    }

    private static long sendImage(String cmd, Group group, boolean needMore) {
        PixivImage image = SetuCenter.cmd(cmd, group.getId());
        if (image.pid < 0) {
            group.sendMessage(TextReader.getText("sendImageFailed").plus(SetuCenter.getErrorString(image.pid)));
        } else {
            if (needMore)
                SetuCenter.putMore(group.getId(), cmd);
            SetuCenter.sendImage(image, group);
        }
        return image.pid;
    }
}
