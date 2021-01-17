package com.oliambot.messages;

import com.oliambot.exception.CatcherIllegalException;
import com.oliambot.inf.Catch;
import com.oliambot.inf.MessageCatcher;
import com.oliambot.utils.MyLog;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MessageCenter {
    protected static class MMethod {
        public MMethod(Method method, int p) {
            this.method = method;
            permission = p;
        }

        public Method method;
        public int permission;
    }

    protected Map<String, MMethod> onGroup;
    protected Map<String, MMethod> onFriend;

    public MessageCenter() {
        onFriend = new HashMap<>();
        onGroup = new HashMap<>();
    }

    public void addMethod(Method m, @NotNull Catch c) {
        if (c.listen() == Catch.ON_GROUP || c.listen() == Catch.BOTH) {
            onGroup.putIfAbsent(c.entry(), new MMethod(m, c.permission()));
        }
        if (c.listen() == Catch.ON_FRIEND || c.listen() == Catch.BOTH) {
            onFriend.putIfAbsent(c.entry(), new MMethod(m, c.permission()));
        }
    }

    public void decodeClass(@NotNull Class<? extends MessageCatcher> target) throws CatcherIllegalException {
        Method[] methods = target.getDeclaredMethods();
        for (Method method: methods) {
            Catch c = method.getAnnotation(Catch.class);
            if (c != null) {
                if (!Modifier.isStatic(method.getModifiers())) {
                    throw new CatcherIllegalException(method.getName() + "非静态方法！");
                }
                Class[] classes = method.getParameterTypes();
                if (classes.length != 2) {
                    throw new CatcherIllegalException(method.getName() + "参数数量错误！");
                }
                if (!(User.class.isAssignableFrom(classes[0]) && Message.class.isAssignableFrom(classes[1]))) {
                    throw new CatcherIllegalException(method.getName() + "参数类型错误！");
                }
                if (c.entry().length() == 0) {
                    throw new CatcherIllegalException(method.getName() + "入口文本长度为0！");
                }
                addMethod(method, c);
            }
        }
    }

    public void catchGroupMessage(Member sender, @NotNull MessageChain chain) {
        String r = chain.contentToString();
        for (String regex: onGroup.keySet()) {
            if (r.matches(regex)) {
                MMethod mMethod = onGroup.get(regex);
                if (mMethod.permission == Catch.ADMIN && sender.getPermission() == MemberPermission.MEMBER) {
                    sender.getGroup().sendMessage("需要管理员权限。");
                    return;
                }
                if (mMethod.permission == Catch.OWNER && sender.getPermission() != MemberPermission.OWNER) {
                    sender.getGroup().sendMessage("需要群主权限。");
                    return;
                }
                try {
                    mMethod.method.invoke(null, sender, chain);
                } catch (Exception e) {
                    MyLog.error(e);
                }
            }
        }
    }

    public void catchFriendMessage(Friend sender, @NotNull MessageChain chain) {
        String r = chain.contentToString();
        for (String regex: onFriend.keySet()) {
            if (r.matches(regex)) {
                try {
                    onFriend.get(regex).method.invoke(null, sender, chain);
                } catch (Exception e) {
                    MyLog.error(e);
                }
            }
        }
    }

    public void decodeClasses(Set<Class<? extends MessageCatcher>> classes) throws CatcherIllegalException {
        onFriend.clear();
        onGroup.clear();
        for (Class c: classes) {
            decodeClass(c);
        }
    }

    public void decodeDefaultClasses() throws CatcherIllegalException {
        Reflections reflections = new Reflections("com.oliambot.messages");
        Set<Class<? extends MessageCatcher>> subTypes = reflections.getSubTypesOf(MessageCatcher.class);
        if (subTypes.size() > 0) {
            decodeClasses(subTypes);
        }
        System.out.println("默认指令：共读取 " + onGroup.size() + " 条群聊指令和 " + onFriend.size() + " 条私聊指令。");
    }
}
