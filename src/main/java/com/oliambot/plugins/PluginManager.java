package com.oliambot.plugins;

import com.oliambot.exception.LoadException;
import com.oliambot.inf.MessageCatcher;
import com.oliambot.inf.Plugin;
import com.oliambot.utils.MyLog;
import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.message.GroupMessageEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class PluginManager {
    private static final Map<String, PluginLoader> PLUGINS = new HashMap<>();

    @NotNull
    @Contract(" -> new")
    public static SimpleListenerHost getHost() {
        return new SimpleListenerHost() {
            @EventHandler
            public ListeningStatus onGroup(GroupMessageEvent event) throws Exception {
                for (PluginLoader loader: PLUGINS.values()) {
                    loader.catchGroupMessage(event.getSender(), event.getMessage());
                }
                return ListeningStatus.LISTENING;
            }

            @Override
            public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception) {
                super.handleException(context, exception);
                MyLog.error(exception);
            }
        };
    }

    @NotNull
    public static String getPluginList(long gid) {
        if (PLUGINS.size() == 0) {
            return "未找到插件。";
        }
        StringBuilder builder = new StringBuilder("现有插件");
        for (String name: PLUGINS.keySet()) {
            builder.append("\n").append(name).append("  -  ").append(PLUGINS.get(name).isEnabled(gid)? "启用": "禁用");
        }
        return builder.toString();
    }

    public static boolean dropPlugin(String name) throws Exception {
        PluginLoader plugin = PLUGINS.remove(name);
        if (plugin == null)
            return false;
        plugin.drop();
        MyLog.info("drop {}", name);
        return true;
    }

    public static void dropAll() throws Exception {
        for (PluginLoader loader: PLUGINS.values()) {
            loader.drop();
        }
        PLUGINS.clear();
    }

    public static boolean disablePlugin(String name, long gid) {
        PluginLoader p = PLUGINS.get(name);
        if (p == null)
            return false;
        p.disable(gid);
        return true;
    }

    public static boolean enablePlugin(String name, long gid) {
        PluginLoader p = PLUGINS.get(name);
        if (p == null)
            return false;
        p.enable(gid);
        return true;
    }

    public static void loadPlugins() throws Exception {
        List<URL> urls = getPlugins();
        for (URL url: urls) {
            loadPlugin(url);
        }
        MyLog.info("插件加载完成，共{}个插件。", PLUGINS.size());
    }

    @NotNull
    private static List<URL> getPlugins() throws Exception {
        File f = new File("./plugins");
        File[] files = f.listFiles(file -> file.getName().endsWith(".jar"));
        List<URL> res = new ArrayList<>();
        for (File file: files) {
            res.add(new URL("file:" + file.getPath()));
        }
        return res;
    }

    private static void loadPlugin(URL url) throws Exception {
        URLClassLoader urlCL = new URLClassLoader(new URL[]{url});
        Reflections reflections = new Reflections(new ConfigurationBuilder().setUrls(
                ClasspathHelper.forClassLoader(urlCL)
        ).addClassLoader(urlCL));
        Set<Class<? extends Plugin>> classes = reflections.getSubTypesOf(Plugin.class);
        Class<? extends Plugin> plugin = classes.iterator().next();
        Plugin plugin1 = plugin.newInstance();
        String name = plugin1.getName();
        if (name == null || name.length() == 0) {
            plugin1.drop();
            throw new LoadException(url + "无插件名！");
        }
        if (PLUGINS.containsKey(name)) {
            plugin1.drop();
            return;
        }
        Set<Class<? extends MessageCatcher>> c = reflections.getSubTypesOf(MessageCatcher.class);
        if (c == null || c.size() == 0) {
            plugin1.drop();
            throw new LoadException(name + " 无MessageCatcher。");
        }
        PluginLoader loader = new PluginLoader(plugin1);
        loader.decodeClasses(c);
        PLUGINS.put(name, loader);
    }
}
