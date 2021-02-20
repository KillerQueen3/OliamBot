package com.oliambot.setu;

import com.oliambot.entity.PixivImage;
import com.oliambot.utils.TextReader;
import com.oliambot.utils.MyLog;
import com.oliambot.utils.Settings;
import com.oliambot.utils.Utils;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SetuCenter {
    public static final String SEARCH = "search ";
    public static final String RECOMMEND = "recommend";
    public static final String ILLUST = "illust ";
    public static final String ARTIST = "artist ";
    public static final String H_IMG = "./resource/h.png";
    private static final Map<Long, String> BEFORE = new ConcurrentHashMap<>();

    public static String getErrorString(long code) {
        switch ((int) code) {
            case -1:
                return TextReader.getString("noMoreImageError");
            case -2:
                return TextReader.getString("apiError");
            case -3:
                return TextReader.getString("timeOutError");
            case -4:
                return TextReader.getString("noSearchResult");
            case -5:
                return TextReader.getString("noArtistIllust");
            case -6:
                return TextReader.getString("noSuchIllust");
            case -7:
                return TextReader.getString("noSingleImage");
            default:
                return TextReader.getString("unknownError");
        }
    }

    public static PixivImage cmd(String cmd, long groupID) {
        try {
            if (cmd.contains(SEARCH)) {
                cmd = cmd.replaceFirst(SEARCH, "");
                boolean r18 = cmd.matches(".*[rR]-?18.*");
                if (r18) {
                    cmd = cmd.replaceAll("[rR]-?18", "");
                }
                if (!r18 && cmd.replaceAll(" ", "").length() == 0) {
                    return NetImageTool.getSeTuInfo();
                } else {
                    return NetImageTool.getSeTuInfo(groupID, cmd, Utils.getTrans(cmd), Settings.lowestNum, r18);
                }
            } else if (cmd.equals(RECOMMEND)) {
                return NetImageTool.recommend();
            } else if (cmd.contains(ILLUST)) {
                long id = Long.parseLong(cmd.replaceAll(ILLUST, ""));
                return NetImageTool.getIllust(id);
            } else if (cmd.contains(ARTIST)) {
                long uid = Long.parseLong(cmd.replaceAll(ARTIST, ""));
                return NetImageTool.getArtistImg(groupID, uid);
            } else {
                return PixivImage.UNKNOWN_ERROR;
            }
        } catch (Exception e) {
            MyLog.error(e);
            return PixivImage.UNKNOWN_ERROR;
        }
    }

    public static Message getLocalImage(Group group, String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            return group.uploadImage(file);
        }
        MyLog.failed("FILE NOT FOUND: {}", fileName);
        return MessageUtils.newChain("[不存在的图片]");
    }

    public static void sendImage(PixivImage imageInfo, Group group) {
        String url = imageInfo.url;
        if (imageInfo.originalUrl == null) {
            imageInfo.originalUrl = imageInfo.url;
        }
        MyLog.info("Send image: {}", url);
        try {
            if (!imageInfo.r18) {
                group.sendMessage(group.uploadImage(new URL(imageInfo.url)).plus(imageInfo.getInfo()));
            } else {
                if (Settings.pixivR18) {
                    BufferedImage image = NetImageTool.getUrlImg(url);
                    if (image != null) {
                        NetImageTool.r18Image(image);
                        group.sendMessage(group.uploadImage(image).plus(imageInfo.getInfo()));
                    } else {
                        group.sendMessage(TextReader.getText("sendImageFailed") + "链接: " + imageInfo.originalUrl);
                    }
                } else {
                    group.sendMessage(getLocalImage(group, H_IMG).plus(imageInfo.getInfo()));
                }
            }
        } catch (Exception e) {
            group.sendMessage(TextReader.getText("sendImageFailed") + "链接: " + imageInfo.originalUrl);
            MyLog.error(e);
        }
    }

    public static String getMore(long groupID) {
        return BEFORE.getOrDefault(groupID, null);
    }

    public static void putMore(long groupID, String cmd) {
        BEFORE.remove(groupID);
        BEFORE.put(groupID, cmd);
    }
}
