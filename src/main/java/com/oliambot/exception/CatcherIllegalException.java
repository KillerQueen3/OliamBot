package com.oliambot.exception;

public class CatcherIllegalException extends LoadException {
    public CatcherIllegalException() {
        super("消息入口格式不正确！");
    }

    public CatcherIllegalException(String message) {
        super(message);
    }
}
