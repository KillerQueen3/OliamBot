package com.oliambot.utils;

import com.oliambot.exception.LoadException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class Settings {
    public static long botID;
    public static String botPW;
    public static boolean pixivLarge;
    public static boolean pixivR18;
    public static String pixivID;
    public static String pixivPWD;
    public static int pixivPort;
    public static String pixivHost;
    public static int pixivInterval;
    public static int pixivSearchNum;
    public static boolean pixivRemovePl;
    public static String loliconApiKey;
    public static int lowestNum;

    public static void initSettings() throws LoadException {
        try {
            PropertiesConfiguration config = new PropertiesConfiguration("settings.properties");
            botID = config.getLong("botQQ");
            botPW = config.getString("botPassword");
            pixivID = config.getString("pixivID");
            pixivPWD = config.getString("pixivPWD");
            pixivLarge = config.getBoolean("pixivImgLarge", false);
            pixivR18 = config.getBoolean("pixivR18", false);
            pixivHost = config.getString("pixivHost");
            pixivPort = config.getInt("pixivPort");
            pixivInterval = config.getInt("pixivInterval");
            pixivSearchNum = config.getInt("pixivSearchNum");
            pixivRemovePl = config.getBoolean("pixivRemovePl", true);
            loliconApiKey = config.getString("loliconApiKey", "");
            lowestNum = config.getInt("lowestNum", 1000);
            System.out.println("设置读取成功！");
        } catch (Exception e) {
            e.printStackTrace();
            throw new LoadException("读取设置失败！");
        }
    }

}
