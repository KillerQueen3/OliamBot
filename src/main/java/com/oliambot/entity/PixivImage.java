package com.oliambot.entity;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

public class PixivImage {
    public long pid;
    public int p;
    public long uid;
    public String title;
    public String author;
    public String url;
    public String originalUrl;
    public boolean r18;
    public int width;
    public int height;
    public String[] tags;

    public PixivImage(@NotNull PixivImage image) {
        this.pid = image.pid;
        this.p = image.p;
        this.uid = image.uid;
        this.title = image.title;
        this.author = image.author;
        this.url = image.url;
        this.r18 = image.r18;
        this.width = image.width;
        this.height = image.height;
        this.tags = image.tags;
        this.originalUrl = image.originalUrl;
    }

    public PixivImage(int pid) {
        this.pid = pid;
    }

    public PixivImage(long pid, int p, long uid, String title, String author, String url, String originalUrl, boolean r18) {
        this.pid = pid;
        this.p = p;
        this.uid = uid;
        this.title = title;
        this.author = author;
        this.url = url;
        this.originalUrl = originalUrl;
        this.r18 = r18;
    }

    public String getInfo() {
        return "pid: " + pid +
                "\n标题: " + title + (r18 ? " (R18)" : "") +
                "\n作者: " + author.replaceAll("@.*", "") +
                "\nuid: " + uid +
                (p > 1? "\n有" + p + "张图片": "") +
                "\n链接: " + originalUrl;
    }

    @Override
    public String toString() {
        return "PixivImage{" +
                "pid=" + pid +
                ", p=" + p +
                ", uid=" + uid +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", url='" + url + '\'' +
                ", originalUrl='" + originalUrl + '\'' +
                ", r18=" + r18 +
                ", width=" + width +
                ", height=" + height +
                ", tags=" + Arrays.toString(tags) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PixivImage image = (PixivImage) o;
        return pid == image.pid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pid);
    }

    public static final PixivImage NO_MORE_PICTURES = new PixivImage(-1);
    public static final PixivImage API_ERROR = new PixivImage(-2);
    public static final PixivImage TIME_OUT = new PixivImage(-3);
    public static final PixivImage NO_SEARCH_RESULT = new PixivImage(-4);
    public static final PixivImage NO_ARTIST_ILLUST = new PixivImage(-5);
    public static final PixivImage NO_SUCH_ILLUST = new PixivImage(-6);
    public static final PixivImage NO_SINGLE_IMAGE = new PixivImage(-7);

    public static final PixivImage UNKNOWN_ERROR = new PixivImage(-100);
}
