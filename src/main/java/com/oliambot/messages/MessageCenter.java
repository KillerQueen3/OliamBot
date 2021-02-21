package com.oliambot.messages;

import com.oliambot.exception.CatcherIllegalException;
import com.oliambot.inf.Catch;
import com.oliambot.inf.MessageCatcher;
import com.oliambot.utils.MyLog;
import com.oliambot.utils.Settings;
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
    public enum CatcherType {
        NONE, SENDER, MESSAGE, SENDER_AND_MESSAGE, MESSAGE_AND_SENDER;
    }

    protected static class MMethod {
        public MMethod(Method method, CatcherType type) {
            this.method = method;
            this.type = type;
        }

        public void invoke(User sender, MessageChain chain) throws Exception {
            switch (type) {
                case NONE:
                    method.invoke(null);
                    break;
                case MESSAGE:
                    method.invoke(null, chain);
                    break;
                case SENDER:
                    method.invoke(null, sender);
                    break;
                case SENDER_AND_MESSAGE:
                    method.invoke(null, sender, chain);
                    break;
                case MESSAGE_AND_SENDER:
                    method.invoke(null, chain, sender);
                    break;
            }
        }
        public int getPermission() {
            return permission;
        }

        public void setPermission(int permission) {
            this.permission = permission;
        }

        private Method method;
        private int permission;
        private CatcherType type;
    }

    protected Map<String, MMethod> onGroup;
    protected Map<String, MMethod> onFriend;

    public MessageCenter() {
        onFriend = new HashMap<>();
        onGroup = new HashMap<>();
    }

    public void addMethod(MMethod mMethod, Catch c) {
        mMethod.setPermission(c.permission());
        if (c.listen() == Catch.ON_GROUP) {
            onGroup.putIfAbsent(c.entry(), mMethod);
        }
        if (c.listen() == Catch.ON_FRIEND) {
            onFriend.putIfAbsent(c.entry(), mMethod);
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
                if (c.entry().length() == 0) {
                    throw new CatcherIllegalException(method.getName() + "入口文本长度为0！");
                }

                Class[] classes = method.getParameterTypes();
                if (classes.length > 2) {
                    throw new CatcherIllegalException(method.getName() + "参数数量错误！");
                }

                if (classes.length == 0) {
                    addMethod(new MMethod(method, CatcherType.NONE), c);
                } else if (classes.length == 1) {
                    if (User.class.isAssignableFrom(classes[0])) {
                        addMethod(new MMethod(method, CatcherType.SENDER), c);
                    } else if (Message.class.isAssignableFrom(classes[0])) {
                        addMethod(new MMethod(method, CatcherType.MESSAGE), c);
                    } else {
                        throw new CatcherIllegalException(method.getName() + "参数类型错误！");
                    }
                } else {
                    if (User.class.isAssignableFrom(classes[0]) && Message.class.isAssignableFrom(classes[1])) {
                        addMethod(new MMethod(method, CatcherType.SENDER_AND_MESSAGE), c);
                    } else if (Message.class.isAssignableFrom(classes[0]) && User.class.isAssignableFrom(classes[1])) {
                        addMethod(new MMethod(method, CatcherType.MESSAGE_AND_SENDER), c);
                    } else {
                        throw new CatcherIllegalException(method.getName() + "参数类型错误！");
                    }
                }
            }
        }
    }

    public void catchGroupMessage(Member sender, @NotNull MessageChain chain) {
        String r = chain.contentToString();
        for (String regex: onGroup.keySet()) {
            if (r.matches(regex)) {
                MMethod mMethod = onGroup.get(regex);
                if (mMethod.getPermission() == Catch.SUPER_USER && sender.getId() != Settings.superUser) {
                    //sender.getGroup().sendMessage("需要超级用户权限。");
                    return;
                }
                if (mMethod.getPermission() == Catch.ADMIN && sender.getPermission() == MemberPermission.MEMBER) {
                    //sender.getGroup().sendMessage("需要管理员权限。");
                    return;
                }
                if (mMethod.getPermission() == Catch.OWNER && sender.getPermission() != MemberPermission.OWNER) {
                    //sender.getGroup().sendMessage("需要群主权限。");
                    return;
                }
                try {
                    mMethod.invoke(sender, chain);
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
                MMethod mMethod = onFriend.get(regex);
                if (mMethod.getPermission() == Catch.SUPER_USER && sender.getId() != Settings.superUser) {
                    sender.sendMessage("需要超级用户权限。");
                    return;
                }
                try {
                    mMethod.invoke(sender, chain);
                } catch (Exception e) {
                    MyLog.error(e);
                }
            }
        }
    }

    public void decodeClasses(@NotNull Set<Class<? extends MessageCatcher>> classes) throws CatcherIllegalException {
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
