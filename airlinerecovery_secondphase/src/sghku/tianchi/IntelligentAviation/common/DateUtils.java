package sghku.tianchi.IntelligentAviation.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
	public static Date getNowDate(String dateString) {
		//SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		SimpleDateFormat formatter = null;
		
		if(dateString.contains(":") && !dateString.contains("/")) {
			String[] strArray = dateString.split(":");
			int hSize = strArray[0].length();
			int mSize = strArray[1].length();
			
			String formatStr = "";
			for(int i=0;i<hSize;i++) {
				formatStr += "H";
			}
			formatStr += ":";
			for(int i=0;i<mSize;i++) {
				formatStr += "m";
			}
			
			formatter = new SimpleDateFormat(formatStr);
		}else if(!dateString.contains(":") && dateString.contains("/")) {
			String[] strArray = dateString.split("/");
			int mSize = strArray[0].length();
			int dSize = strArray[1].length();
			int ySize = strArray[2].length();
			
			String formatStr = "";
			for(int i=0;i<mSize;i++) {
				formatStr += "M";
			}
			formatStr += "/";
			for(int i=0;i<dSize;i++) {
				formatStr += "d";
			}
			formatStr += "/";
			for(int i=0;i<ySize;i++) {
				formatStr += "y";
			}
			
			formatter = new SimpleDateFormat(formatStr);
		}else {
			String[] dateAndTime = dateString.split(" ");
			
			String[] strArray = dateAndTime[0].split("/");
			int mSize = strArray[0].length();
			int dSize = strArray[1].length();
			int ySize = strArray[2].length();
			
			String formatStr = "";
			for(int i=0;i<mSize;i++) {
				formatStr += "M";
			}
			formatStr += "/";
			for(int i=0;i<dSize;i++) {
				formatStr += "d";
			}
			formatStr += "/";
			for(int i=0;i<ySize;i++) {
				formatStr += "y";
			}
			
			formatStr += " ";
			
			strArray = dateAndTime[1].split(":");
			int hSize = strArray[0].length();
			mSize = strArray[1].length();
			
			for(int i=0;i<hSize;i++) {
				formatStr += "H";
			}
			formatStr += ":";
			for(int i=0;i<mSize;i++) {
				formatStr += "m";
			}
			
			formatter = new SimpleDateFormat(formatStr);
		}
		
		/*if(dateString.trim().length() == 10) {
			formatter = new SimpleDateFormat("MM/dd/yyyy");
		}else if(dateString.trim().length() == 5) {
			formatter = new SimpleDateFormat("HH:mm");
		}else {
			formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		}*/
		
		Date currentTime_2 = null;
		try {
			currentTime_2 = formatter.parse(dateString);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return currentTime_2;
	}
	
	public static double getTimeGap(Date date1, Date date2){
		return (date1.getTime()-date2.getTime())*1.0/1000.0/60.0;
	}
	
	public static void main(String[] args) {
		//String dateStr = "05/04/2012 12:34:23";  
		//String dateStr = "05/04/2013";  
		String dateStr = "2:34";  
		
		System.out.println(getNowDate(dateStr));
	}
}
