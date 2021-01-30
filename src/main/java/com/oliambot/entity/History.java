package com.oliambot.entity;

import java.util.Date;

public class History {
    public Date date;
    public String nick;
    public long qqID;
    public String groupName;
    public long groupID;
    public String type;
    public String cmd;
    public long resultPid;

    public History(String nick, long qqID, String groupName, long groupID, String cmd, String type, long pid) {
        this.nick = nick;
        this.qqID = qqID;
        this.groupName = groupName;
        this.groupID = groupID;
        this.cmd = cmd;
        this.type = type;
        date = new Date();
        this.resultPid = pid;
    }
}
