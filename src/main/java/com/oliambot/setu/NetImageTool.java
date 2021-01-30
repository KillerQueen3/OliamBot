package com.oliambot.setu;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.oliambot.entity.PixivImage;
import com.oliambot.exception.SendImageException;
import com.oliambot.utils.MyLog;
import com.oliambot.utils.Settings;
import com.oliambot.utils.Utils;
import net.dreamlu.mica.http.HttpRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.net.URLConnection;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class NetImageTool {
    private final static String LOLICON_API = "https://api.lolicon.app/setu/";
    private static final int[] USERS = new int[]{250, 500, 1000, 5000, 10000, 20000};
    private static final List<PixivImage> SETU_FROM_LOLICON = new CopyOnWriteArrayList<>();
    private static final List<PixivImage> RECOMMEND = new CopyOnWriteArrayList<>();

    private static String getPixivInfoApi() {
        return "http://" + Settings.pixivHost + ":" + Settings.pixivPort + "/pixiv/";
    }

    @NotNull
    private static List<Integer> getUpUsers(int bottom) {
        List<Integer> res = new LinkedList<>();
        for (int c : USERS) {
            if (c > bottom)
                res.add(c);
        }
        return res;
    }

    private static void addSetu() {
        String url = LOLICON_API + "?apikey=" + Settings.loliconApiKey + "&size1200=&num=10&r18=" + (Settings.pixivR18 ? "2" : "0");
        MyLog.info("Getting {}", url);
        String t = HttpRequest.get(url).connectTimeout(Duration.ofSeconds(10)).execute().asString();
        JsonObject object = (JsonObject) JsonParser.parseString(t);
        if (object.get("code").getAsInt() == 0) {
            JsonArray info = object.getAsJsonArray("data").getAsJsonArray();
            List<PixivImage> res = new Gson().fromJson(info, new TypeToken<List<PixivImage>>() {
            }.getType());
            if (res == null)
                return;
            SETU_FROM_LOLICON.addAll(res);
        } else {
            MyLog.failed("LOLICON API ERROR! RESPONSE: {}", t);
        }
    }

    //lolicon随机色图
    public static PixivImage getSeTuInfo() {
        if (SETU_FROM_LOLICON.size() == 0) {
            addSetu();
        }
        if (SETU_FROM_LOLICON.size() == 0) {
            return PixivImage.API_ERROR;
        }
        return SETU_FROM_LOLICON.remove(0);
    }

    public static void autoLoginThreadStart(int intervalMin) {
        Thread auto = new Thread(() -> {
            while (true) {
                pixivLogin();
                try {
                    Thread.sleep((long) intervalMin * 60 * 1000);
                } catch (InterruptedException e) {
                    MyLog.error(e);
                }
            }
        });
        auto.setName("pixiv-auto-login");
        auto.start();
    }

    public static void pixivLogin() {
        String url = getPixivInfoApi() + "login?name=" + Settings.pixivID + "&pwd=" + Settings.pixivPWD;
        try {
            String t = HttpRequest.get(url).connectTimeout(Duration.ofSeconds(5)).execute().asString();
            JsonObject object = (JsonObject) JsonParser.parseString(t);
            if (object.get("code").getAsInt() != 200) {
                MyLog.failed(object.get("message").getAsString());
            }
            // MyLog.info("PIXIV LOGIN");
        } catch (Exception e) {
            MyLog.error(e);
        }
    }

    public static void addRecommend() {
        String url = getPixivInfoApi() + "recommend";
        try {
            String t = HttpRequest.get(url).connectTimeout(Duration.ofSeconds(20)).readTimeout(Duration.ofSeconds(30))
                    .execute().asString();
            JsonObject object = (JsonObject) JsonParser.parseString(t);
            if (object.get("code").getAsInt() == 200) {
                JsonArray resp = object.get("message").getAsJsonArray();
                if (resp != null) {
                    RECOMMEND.addAll(decodeJsonArray(resp));
                    MyLog.info("Add recommend size: {}", RECOMMEND.size());
                } else {
                    pixivLogin();
                    MyLog.failed("RECOMMEND FAILED!");
                }
            } else {
                MyLog.failed("RECOMMEND FAILED!: {}", t);
            }
        } catch (Exception e) {
            pixivLogin();
            MyLog.error(e);
        }
    }

    public static PixivImage recommend() {
        if (RECOMMEND.size() == 0)
            addRecommend();
        if (RECOMMEND.size() == 0)
            return PixivImage.API_ERROR;
        return RECOMMEND.remove(0);
    }

    @NotNull
    private static List<PixivImage> searchSeTu(String tag, int num, boolean r18) throws Exception {
        String word = tag + (num > 0 ? " " + num + "users" : "") + (r18 ? " R-18" : "");
        String url = getPixivInfoApi() + "search?limit=" + Settings.pixivSearchNum + "&word=" + word;
        MyLog.info("Getting {}", url);
        String t = HttpRequest.get(url).connectTimeout(Duration.ofSeconds(20)).readTimeout(Duration.ofSeconds(30))
                .execute().asString();
        JsonObject res = (JsonObject) JsonParser.parseString(t);
        if (res.get("code").getAsInt() == 200) {
            JsonArray jsonArray = res.get("message").getAsJsonArray();
            return decodeJsonArray(jsonArray);
        } else {
            MyLog.failed("GET {} FAILED! RESPONSE: {}", url, t);
            pixivLogin();
            throw new SendImageException();
        }
    }

    public static PixivImage getSeTuInfo(long groupID, String tag, String trans, int num, boolean r18) {
        String w = trans + (r18 ? " R-18" : "");
        List<PixivImage> works = Utils.getSearchCache(groupID, w);
        if (works == null) {
            try {
                Set<PixivImage> set = new CopyOnWriteArraySet<>(searchSeTu(trans, num, r18));
                ExecutorService service = Executors.newCachedThreadPool();
                List<Integer> ups = getUpUsers(num);
                for (int u : ups) {
                    service.submit(() -> {
                        try {
                            set.addAll(searchSeTu(trans, u, r18));
                        } catch (Exception e) {
                            MyLog.error(e);
                        }
                    });
                }
                service.shutdown();
                try {
                    if (!service.awaitTermination(15, TimeUnit.SECONDS)) {
                        service.shutdownNow();
                    }
                } catch (Exception e) {
                    MyLog.error(e);
                }
                if (set.size() < 15) {
                    List<String> moreTrans = Utils.autoComplete(tag);
                    for (String more : moreTrans) {
                        set.addAll(searchSeTu(more, num, r18));
                    }
                }
                if (set.size() < 15 && num > 500) {
                    set.addAll(searchSeTu(trans, 500, r18));
                }
                works = new LinkedList<>(set);
                MyLog.info("Search: {}, trans: {}, result size: {}", tag, trans, works.size());
            } catch (JsonParseException | SendImageException e) {
                MyLog.error(e);
                return PixivImage.API_ERROR;
            } catch (TimeoutException e) {
                MyLog.error(e);
                return PixivImage.TIME_OUT;
            } catch (Exception e) {
                MyLog.error(e);
                return PixivImage.UNKNOWN_ERROR;
            }
        } else {
            MyLog.info("Read cache: tag: {} size: {}", w, works.size());
            if (works.size() == 0) {
                return PixivImage.NO_MORE_PICTURES;
            }
        }
        PixivImage image = getRandomImg(works, Settings.pixivRemovePl);
        if (image.pid > 0) {
            works.remove(image);
            Utils.writeSearchCache(groupID, w, works);
        } else if (image.equals(PixivImage.NO_SEARCH_RESULT)) {
            Utils.writeFailed(trans);
        }
        return image;
    }

    public static PixivImage getIllust(long pid) {
        String url = getPixivInfoApi() + "illust?pid=" + pid;
        MyLog.info("Getting {}", url);
        try {
            String t = HttpRequest.get(url).connectTimeout(Duration.ofSeconds(20)).readTimeout(Duration.ofSeconds(30))
                    .execute().asString();
            JsonObject res = (JsonObject) JsonParser.parseString(t);
            int code = res.get("code").getAsInt();
            if (code == 200) {
                JsonObject illust = res.get("message").getAsJsonObject();
                return decodeImgJSON(illust);
            } else if (code == -2) {
                return PixivImage.NO_SUCH_ILLUST;
            } else {
                pixivLogin();
                return PixivImage.API_ERROR;
            }
        } catch (JsonParseException e) {
            MyLog.error(e);
            return PixivImage.API_ERROR;
        } catch (Exception e) {
            MyLog.error(e);
            return PixivImage.UNKNOWN_ERROR;
        }
    }

    public static PixivImage getArtistImg(long groupID, long uid) {
        List<PixivImage> works = Utils.getSearchCache(groupID, String.valueOf(uid));
        if (works == null) {
            try {
                String url = getPixivInfoApi() + "artist?limit=" + Settings.pixivSearchNum + "&uid=" + uid;
                MyLog.info("Getting {}", url);
                String t = HttpRequest.get(url).connectTimeout(Duration.ofSeconds(20)).readTimeout(Duration.ofSeconds(30))
                        .execute().asString();
                JsonObject res = (JsonObject) JsonParser.parseString(t);
                if (res.get("code").getAsInt() == 200) {
                    JsonArray jsonArray = res.get("message").getAsJsonArray();
                    works = decodeJsonArray(jsonArray);
                    if (works.size() == 0) {
                        return PixivImage.NO_ARTIST_ILLUST;
                    }
                } else {
                    MyLog.failed("GET {} FAILED! RESPONSE: {}", url, t);
                    pixivLogin();
                    return PixivImage.API_ERROR;
                }
            } catch (JsonParseException e) {
                MyLog.error(e);
                return PixivImage.API_ERROR;
            } catch (Exception e) {
                MyLog.error(e);
                return PixivImage.UNKNOWN_ERROR;
            }
        } else {
            MyLog.info("Read cache: tag: {}, size: {}", uid, works.size());
            if (works.size() == 0) {
                return PixivImage.NO_MORE_PICTURES;
            }
        }
        PixivImage image = getRandomImg(works, false);
        if (image.pid > 0) {
            works.remove(image);
            Utils.writeSearchCache(groupID, String.valueOf(uid), works);
        }
        return image;
    }

    @Nullable
    public static BufferedImage getUrlImg(String url) {
        try {
            URL url1 = new URL(url);
            URLConnection connection = url1.openConnection();
            connection.setDoOutput(true);
            connection.setConnectTimeout(30 * 1000);
            connection.setReadTimeout(30 * 1000);
            return ImageIO.read(connection.getInputStream());
        } catch (Exception e) {
            MyLog.failed("GETTING IMAGE FAILED: {}", url);
            MyLog.error(e);
            return null;
        }
    }

    @NotNull
    public static PixivImage[] getUrls(@NotNull PixivImage image) {
        if (image.p == 1)
            return new PixivImage[]{image};
        else {
            PixivImage im = new PixivImage(image);
            PixivImage[] res = new PixivImage[image.p];
            res[0] = image;
            for (int i = 1; i < image.p; i++) {
                PixivImage imm = new PixivImage(im);
                imm.url = im.url.replaceAll("_p0", "_p" + i);
                imm.originalUrl = im.originalUrl.replaceAll("_p0", "_p" + i);
                res[i] = imm;
            }
            return res;
        }
    }

    public static void r18Image(@NotNull BufferedImage source) {
        Graphics g = source.getGraphics();
        g.drawRect(0, 0, 1, 1);
        g.dispose();
    }

    @NotNull
    private static List<PixivImage> decodeJsonArray(@NotNull JsonArray array) {
        List<PixivImage> res = new LinkedList<>();
        for (JsonElement j : array) {
            res.add(decodeImgJSON(j.getAsJsonObject()));
        }
        return res;
    }

    @NotNull
    private static PixivImage decodeImgJSON(@NotNull JsonObject imageInfo) {
        long pid = imageInfo.get("id").getAsLong();
        String title = imageInfo.get("title").getAsString();
        String user = imageInfo.get("user_name").getAsString();
        long uid = imageInfo.get("user_id").getAsLong();
        boolean r18 = imageInfo.get("r18").getAsBoolean();
        int p = imageInfo.get("page_count").getAsInt();
        String urls = Settings.pixivLarge ? imageInfo.get("large_url").getAsString() :
                imageInfo.get("medium_url").getAsString();
        String urlLarge = imageInfo.get("original_url").getAsString();
        urlLarge = urlLarge.replaceAll("i\\.pximg\\.net", "i.pixiv.cat");
        urls = urls.replaceAll("i\\.pximg\\.net", "i.pixiv.cat");
        return new PixivImage(pid, p, uid, title, user, urls, urlLarge, r18);
    }

    private static PixivImage getRandomImg(@NotNull List<PixivImage> pixivImages, boolean removePlural) {
        if (pixivImages.size() == 0) {
            return PixivImage.NO_SEARCH_RESULT;
        }
        if (removePlural) {
            pixivImages.removeIf(p -> p.p > 1);
        }
        if (pixivImages.size() == 0)
            return PixivImage.NO_SINGLE_IMAGE;
        int index = (int) (Math.random() * pixivImages.size());
        return pixivImages.get(index);
    }
}
