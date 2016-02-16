package com.example.temp;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		String time = null;
		long h = mills / (60 * 60 * 1000);
		if (h < 10) {
			time = "0" + h;
		} else {
			time = "" + h;
		}
		mills -= h * 60 * 60 * 1000;
		long m = mills / (60 * 1000);
		if (m < 10) {
			time += ":0" + m;
		} else {
			time += ":" + m;
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
	 * @author 宋疆疆
	 * @date 2014-3-28 上午10:41:44
	 * @param url
	 * @return
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
	 * @author 宋疆疆
	 * @date 2014-4-2 下午3:10:56
	 * @param number
	 *            要补零的数字
	 * @param digit
	 *            要补多少位的0
	 * @return
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
	 * @author 宋疆疆
	 * @date 2014-5-9 下午2:36:03 
	 * @param b
	 * @return
	 */
	public static String byte2HexString(byte[] b) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			sb.append(hex);
		}
		return sb.toString();
	}
}
