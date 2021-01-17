package com.oliambot.inf;

public interface Plugin {
    public abstract String getName() throws Exception;

    public abstract void drop() throws Exception;
}
