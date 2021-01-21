package com.oliambot.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyLog {
    final static Logger logger = LoggerFactory.getLogger("BOT_LOG");

    public static void debug(String text, Object... objects) {
        logger.debug(text, objects);
    }

    public static void info(String text, Object... objects) {
        logger.info(text, objects);
    }

    public static void failed(String text, Object... objects) {
        logger.error(text, objects);
    }

    public static void error(Throwable e) {
        logger.error(e.toString(), e);
    }
}
