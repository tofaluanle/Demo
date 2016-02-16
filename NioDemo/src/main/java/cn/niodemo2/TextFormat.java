package cn.niodemo2;

import android.text.TextUtils;

import java.text.DecimalFormat;

public class TextFormat {

    public static String formatByte(long data, int digit) {
        String pattern = "##.";
        for (int i = 0; i < digit; i++) {
            pattern += "#";
        }
        if (0 == digit) {
            pattern = "##";
        }
        DecimalFormat format = new DecimalFormat(pattern);
        if (data < 1024) {
            return data + "byte";
        } else if (data < 1024l * 1024l) {
            return format.format(data / 1024f) + "KB";
        } else if (data < 1024l * 1024l * 1024l) {
            return format.format(data / 1024f / 1024f) + "MB";
        } else if (data < 1024l * 1024l * 1024l * 1024l) {
            return format.format(data / 1024f / 1024f / 1024f) + "GB";
        } else if (data < 1024l * 1024l * 1024l * 1024l * 1024l) {
            return format.format(data / 1024f / 1024f / 1024f / 1024f) + "TB";
        } else if (data < 1024l * 1024l * 1024l * 1024l * 1024l * 1024l) {
            return format.format(data / 1024f / 1024f / 1024f / 1024f / 1024f)
                    + "PB";
        } else {
            return "超出统计返回";
        }
    }

    /**
     * 把传入的毫秒转化为HH:mm:ss的时间格式
     *
     * @param mills
     * @return
     */
    public static String intToTime(long mills) {
        String time = "";
        long h = mills / (60 * 60 * 1000);
        if (h <= 0) {
        } else if (h < 10) {
            time = "0" + h + ":";
        } else {
            time = "" + h + ":";
        }
        mills -= h * 60 * 60 * 1000;
        long m = mills / (60 * 1000);
        if (m < 10) {
            time += "0" + m;
        } else {
            time += "" + m;
        }
        mills -= m * 60 * 1000;
        long s = mills / 1000;
        if (s < 10) {
            time += ":0" + s;
        } else {
            time += ":" + s;
        }
        return time;
    }

    /**
     * 用来转义html代码
     *
     * @param url
     * @return
     * @author 宋疆疆
     * @date 2014-3-28 上午10:41:44
     */
    public static String decodeHtml(String url) {
        url = url.replaceAll("&amp;", "&").replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">").replaceAll("&apos;", "\'")
                .replaceAll("&quot;", "\"").replaceAll("&nbsp;", " ")
                .replaceAll("&copy;", "@").replaceAll("&reg;", "?");
        return url;
    }

    /**
     * 对数字进行补零
     *
     * @param number 要补零的数字
     * @param digit  要补多少位的0
     * @return
     * @author 宋疆疆
     * @date 2014-4-2 下午3:10:56
     */
    public static String supplementZero(long number, int digit) {
        String pattern = "";
        for (int i = 0; i < digit; i++) {
            pattern += "0";
        }
        DecimalFormat format = new DecimalFormat(pattern);
        return format.format(number);
    }

    /**
     * 把byte数组转换成16进制字符串
     *
     * @param b
     * @return
     * @author 宋疆疆
     * @date 2014-5-9 下午2:36:03
     */
    public static String byte2HexString(byte[] b) {
        return byte2HexString(b, 0, b.length);
    }

    public static String byte2HexString(byte[] b, int offset, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = offset; i < count; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex + " ");
        }
        return sb.toString();
    }

    /**
     * hex字符串转byte数组<br/>
     * 2个hex转为一个byte
     *
     * @param src
     * @return
     */
    public static byte[] hex2Bytes(String src) {
        byte[] res = new byte[src.length() / 2];
        char[] chs = src.toCharArray();
        int[] b = new int[2];

        for (int i = 0, c = 0; i < chs.length; i += 2, c++) {
            for (int j = 0; j < 2; j++) {
                if (chs[i + j] >= '0' && chs[i + j] <= '9') {
                    b[j] = (chs[i + j] - '0');
                } else if (chs[i + j] >= 'A' && chs[i + j] <= 'F') {
                    b[j] = (chs[i + j] - 'A' + 10);
                } else if (chs[i + j] >= 'a' && chs[i + j] <= 'f') {
                    b[j] = (chs[i + j] - 'a' + 10);
                }
            }
            b[0] = (b[0] & 0x0f) << 4;
            b[1] = (b[1] & 0x0f);
            res[c] = (byte) (b[0] | b[1]);
        }

        return res;
    }

    /**
     * 将字符串进行空格补齐到指定长度，只试用于中文占两个宽度，字符占一个宽度的情况，默认居中补齐
     *
     * @param src
     * @param len
     * @return
     */
    public static String supplementSpace(String src, int len) {
        String ret = null;
        int width = getWidth(src);
        StringBuilder space = new StringBuilder();
        int spaceCount = len - width;
        for (int i = 0; i < spaceCount / 2; i++) {
            space.append(" ");
        }
        ret = space.toString() + src + space.toString();
        if (spaceCount % 2 != 0) {
            ret += " ";
        }
        return ret;
    }

    /**
     * 获取字符串的物理长度，只试用于中文占两个宽度，字符占一个宽度的情况
     *
     * @param content
     * @return
     */
    public static int getWidth(String content) {
        if (TextUtils.isEmpty(content)) {
            return 0;
        }
        int width = 0;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c >= 0x4E00 && c <= 0x9FBF) {
                width += 1;
            }
            width += 1;
        }
        return width;
    }

    public static boolean hasChineseChar(String content) {
        if (TextUtils.isEmpty(content)) {
            return false;
        }
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c >= 0x4E00 && c <= 0x9FBF) {
                return true;
            }
        }
        return false;
    }
}
