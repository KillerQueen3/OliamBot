package com.oliambot.entity;

import java.util.Date;

public class History {
    public String nick;
    public long qqID;
    public String groupName;
    public long groupID;
    public String type;
    public String cmd;
    public Date date;

    public History(String nick, long qqID, String groupName, long groupID, String cmd, String type) {
        this.nick = nick;
        this.qqID = qqID;
        this.groupName = groupName;
        this.groupID = groupID;
        this.cmd = cmd;
        this.type = type;
        date = new Date();
    }
}
