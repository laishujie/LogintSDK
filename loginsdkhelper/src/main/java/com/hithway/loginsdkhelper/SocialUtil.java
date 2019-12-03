package com.hithway.loginsdkhelper;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by arvinljw on 17/11/27 15:37
 * Function：
 * Desc：
 */
public final class SocialUtil {
    public static final String QQ_PKG = "com.tencent.mobileqq";
    public static final String WECHAT_PKG = "com.tencent.mm";
    public static final String SINA_PKG = "com.sina.weibo";
    public static final String QQ_FRIENDS_PAGE = "com.tencent.mobileqq.activity.JumpActivity";//qq选择好友、群、我的电脑

    static String get(URL url) throws Exception {
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        if (conn.getResponseCode() == 200) {
            InputStream is = conn.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while (-1 != (len = is.read(buffer))) {
                out.write(buffer, 0, len);
                out.flush();
            }
            return out.toString("utf-8");
        }
        return null;
    }

   public static String getAppStateName(Context context) {
        String packageName = context.getPackageName();
        int beginIndex = 0;
        if (packageName.contains(".")) {
            beginIndex = packageName.lastIndexOf(".");
        }
        return packageName.substring(beginIndex);
    }

     static String getQQSex(String gender) {
        return "男".equals(gender) ? "M" : "F";
    }

    static String getWXSex(String gender) {
        return "1".equals(gender) ? "M" : "F";
    }

    static String getWBSex(String gender) {
        return "m".equals(gender) ? "M" : "F";
    }

   public static String buildTransaction(String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

   public static byte[] bmpToByteArray(final Bitmap bmp, boolean needThumb) {
        Bitmap newBmp;
        if (needThumb) {
            int width = bmp.getWidth();
            int height = bmp.getHeight();
            if (width > height) {
                height = height * 150 / width;
                width = 150;
            } else {
                width = width * 150 / height;
                height = 150;
            }
            newBmp = Bitmap.createScaledBitmap(bmp, width, height, true);
        } else {
            newBmp = bmp;
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        newBmp.compress(Bitmap.CompressFormat.JPEG, 100, output);

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (!bmp.isRecycled()) {
                bmp.recycle();
            }
            if (!newBmp.isRecycled()) {
                newBmp.recycle();
            }
        }

        return result;
    }

    /**
     * 通过图片头获取图片文件的扩展名
     *
     * @param saveFile
     * @return
     */
    public static String getImageFileEndName(File saveFile) {


        if (saveFile != null && saveFile.exists()) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(saveFile.getAbsolutePath(), options);
            String miniType = options.outMimeType;
            if (miniType != null && miniType.startsWith("image/")) {
                return miniType.replaceFirst("image/", "");
            }
        }
        return null;
    }

    /**
     * @param path 路径
     * @return 是否是 gif 文件
     */
    public static boolean isGifFile(String path) {
        return isGIFImage(new File(path));
    }
    /**
     * 通过文件头判断图片是否为GIF
     *
     * @param imagePath File类型传参
     * @return
     */
    public static boolean isGIFImage(File imagePath) {
        String endName = getImageFileEndName(imagePath);
        return !TextUtils.isEmpty(endName) && endName.toUpperCase().contains("GIF");
    }

    /**
     * 是否安装qq
     */
    static boolean isQQInstalled(Context context) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packageInfo = packageManager.getInstalledPackages(0);
        if (packageInfo != null) {
            for (int i = 0; i < packageInfo.size(); i++) {
                String pn = packageInfo.get(i).packageName;
                if (pn.equals("com.tencent.mobileqq")) {
                    return true;
                }
            }
        }
        return false;
    }


}
