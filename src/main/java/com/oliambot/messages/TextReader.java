package com.oliambot.messages;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.oliambot.exception.LoadException;
import com.oliambot.utils.MyLog;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class TextReader {
    private static final String FILE_PATH = "./resource/message.json";
    private static final Map<String, String> TEXTS = new HashMap<>();

    public static void loadTexts() throws LoadException {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            throw new LoadException(FILE_PATH + "不存在！");
        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            Map<String, String> res = new Gson().fromJson(br, new TypeToken<Map<String, String>>() {
            }.getType());
            if (res == null) {
                throw new LoadException(FILE_PATH + "空文件！");
            }
            TEXTS.clear();
            TEXTS.putAll(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getString(String key) {
        if (TEXTS.containsKey(key)) {
            return TEXTS.get(key);
        } else {
            MyLog.failed("无此项目: {}", key);
            return "";
        }
    }

    public static MessageChain getText(String key, Member sender) {
        String res = TEXTS.getOrDefault(key, key);
        if (res.contains("[at]") && sender != null) {
            return new At(sender).plus(res.replaceAll("\\[at]", ""));
        }
        return MessageUtils.newChain(res);
    }

    public static MessageChain getText(String key) {
        return MessageUtils.newChain(TEXTS.getOrDefault(key, key));
    }

    public static MessageChain getRandomText(String key, String split, Member sender) {
        String res = TEXTS.getOrDefault(key, null);
        if (res == null)
            return MessageUtils.newChain(key);
        String[] r = res.split(split);
        String ran = r[(int) (Math.random() * r.length)];
        if (ran.contains("[at]") && sender != null) {
            return new At(sender).plus(ran.replaceAll("\\[at]", ""));
        }
        return MessageUtils.newChain(ran);
    }

    public static List<String> getStrings(String key, String split) {
        String r = TEXTS.getOrDefault(key, null);
        if (r == null) {
            return new ArrayList<>();
        }
        String[] sp = r.split(split);
        return Arrays.asList(sp);
    }
}
