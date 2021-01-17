package com.oliambot.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.oliambot.entity.Chara;
import com.oliambot.entity.History;
import com.oliambot.entity.PixivImage;
import com.oliambot.entity.Trans;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Utils {
    private static final Map<String, String> trans = new HashMap<>();
    private static final Map<String, String> chara = new HashMap<>();

    @NotNull
    @Contract(pure = true)
    private static String keywordReplace(@NotNull String raw) { // 替换掉中日文不同的汉字。
        return raw.replaceAll("姬", "姫");
    }

    private static String getSingleTrans(String keyword) {
        if (chara.containsKey(keyword))
            return chara.get(keyword);
        if (trans.containsKey(keyword))
            return trans.get(keyword);
        return keyword;
    }

    public static String getTrans(String keyword) {
        String[] keywords = keywordReplace(keyword).toLowerCase().split(" ");
        for (int i = 0; i < keywords.length; i++) {
            keywords[i] = Utils.getSingleTrans(keywords[i]);
        }
        return StringUtils.join(keywords, " ");
    }

    @NotNull
    public static List<String> autoComplete(String src) {
        List<String> res = new ArrayList<>();
        for (String x : trans.keySet()) {
            if (x.contains(src) || src.contains(x)) { // 可能引起未知问题。
                res.add(trans.get(x));
            }
        }
        return res;
    }

    public static void reload() {
        trans.clear();
        trans.putAll(getTrans());
        chara.clear();
        chara.putAll(getChara());
    }

    @NotNull
    private static Map<String, String> getTrans() {
        Map<String, String> res = new HashMap<>();
        try {
            FileReader reader = new FileReader("./resource/trans.json");
            Gson gson = new Gson();
            List<Trans> json = gson.fromJson(reader, new TypeToken<List<Trans>>() {
            }.getType());
            for (Trans t : json) {
                if (t.translation != null && t.translation.length() > 0) {
                    res.putIfAbsent(t.translation.toLowerCase(), t.text);
                }
            }
            System.out.println("翻译读取成功！");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    @NotNull
    private static Map<String, String> getChara() {
        Map<String, String> res = new HashMap<>();
        try {
            File file = new File("./resource/pcrChara.json");
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            List<Chara> charas = new Gson().fromJson(br, new TypeToken<List<Chara>>() {
            }.getType());
            for (Chara c : charas) {
                String jpName = c.jp.replaceAll("\\(.*\\)|（.*）", "") + " プリコネ";
                res.put(c.ch, jpName);
                res.put(jpName, jpName);
                for (String n : c.nick) {
                    res.putIfAbsent(n.toLowerCase(), jpName);
                }
            }
            System.out.println("角色读取成功！");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    @NotNull
    private static Map<String, List<PixivImage>> readSearchCache(long groupID, String tag) {
        try {
            File file = new File(getFileName(groupID, tag));
            if (!file.exists()) {
                file.createNewFile();
                return new HashMap<>();
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            Map<String, List<PixivImage>> res = new Gson().fromJson(br, new TypeToken<Map<String, List<PixivImage>>>() {
            }.getType());
            br.close();
            if (res == null)
                return new HashMap<>();
            return res;
        } catch (IOException e) {
            MyLog.error(e);
            return new HashMap<>();
        }
    }

    @NotNull
    private static String getFileName(long groupID, @NotNull String tag) {
        String hash = Integer.toHexString(tag.hashCode());
        if (hash.length() >= 3)
            hash = hash.substring(0, 2);
        return "./resource/search/" + groupID + "-" + hash + ".json";
    }

    public static List<PixivImage> getSearchCache(long groupID, String tag) {
        Map<String, List<PixivImage>> read = readSearchCache(groupID, tag);
        return read.getOrDefault(tag, null);
    }

    public static boolean cleanCache(long groupID, String trans) {
        Map<String, List<PixivImage>> read = readSearchCache(groupID, trans);
        List<PixivImage> res = read.remove(trans);
        if (writeCache(groupID, trans, read) && res != null) {
            MyLog.info("Clean cache: tag: {}", trans);
            return true;
        }
        return false;
    }

    private static boolean writeCache(long groupID, String tag, Map<String, List<PixivImage>> map) {
        return writeToFile(new Gson().toJson(map), new File(getFileName(groupID, tag)));
    }

    private static boolean writeToFile(String thing, File file) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(thing.getBytes(StandardCharsets.UTF_8));
            fos.flush();
            fos.close();
            return true;
        } catch (Exception e) {
            MyLog.error(e);
            return false;
        }
    }

    public static void writeSearchCache(long groupID, String tag, List<PixivImage> search) {
        Map<String, List<PixivImage>> read = readSearchCache(groupID, tag);
        read.remove(tag);
        read.put(tag, search);
        if (writeCache(groupID, tag, read))
            MyLog.info("Write cache to file: {} tag: {} size: {}", getFileName(groupID, tag), tag, search.size());
    }

    public static void writeHistory(History history) {
        Gson gson = new Gson();
        try {
            File file = new File("./resource/history.json");
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            List<History> historyList = gson.fromJson(br, new TypeToken<List<History>>() {
            }.getType());
            if (historyList == null)
                historyList = new LinkedList<>();
            br.close();
            historyList.add(history);
            writeToFile(gson.toJson(historyList), file);
        } catch (Exception e) {
            MyLog.error(e);
        }
    }

    public static void writeFailed(String fail) {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();
        try {
            File file = new File("./resource/fail.json");
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            List<String> failedList = gson.fromJson(br, new TypeToken<List<String>>() {
            }.getType());
            if (failedList == null)
                failedList = new LinkedList<>();
            br.close();
            failedList.add(fail);
            writeToFile(gson.toJson(fail), file);
        } catch (Exception e) {
            MyLog.error(e);
        }
    }
}
