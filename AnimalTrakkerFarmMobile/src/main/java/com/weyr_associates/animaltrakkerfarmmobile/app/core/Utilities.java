package com.weyr_associates.animaltrakkerfarmmobile.app.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

/**
 * Write a description of class Utilities here.
 * 
 * @author ww 
 * em added the take note utility
 * @version 2014-05-11
 */
public class Utilities {

public static String TimeIs() {
	Calendar calendar = Calendar.getInstance();
    // 12 hour format
    //	int hour = cal.get(Calendar.HOUR);
    //  24 hour format
	int hourofday = calendar.get(Calendar.HOUR_OF_DAY);
	int minute = calendar.get(Calendar.MINUTE);
	int second = calendar.get(Calendar.SECOND);
	  
	return Make2Digits(hourofday) + ":" + Make2Digits(minute) + ":" + Make2Digits(second) ;
}
public static String TodayIs() {
		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH);
		int year = calendar.get(Calendar.YEAR);
		return year + "-" + Make2Digits(month + 1) + "-" +  Make2Digits(day) ;
	}
 public static String Make2Digits(int i) {
		if (i < 10) {
			return "0" + i;
		} else {
			return Integer.toString(i);
		}
	}

	public static void copy(InputStream in, OutputStream out) throws IOException {
		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		out.flush();
		out.close();
		in.close();
	}
}
 
 