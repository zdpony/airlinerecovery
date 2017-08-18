package sghku.tianchi.IntelligentAviation.common;

import java.util.HashSet;
import java.util.Set;

import sghku.tianchi.IntelligentAviation.entity.Airport;

public class Parameter {
	
	//Minimum transfer time
	public final static int MIN_TRANS_TIME_D2D = 60;
	public final static int MIN_TRANS_TIME_D2I = 75;
	public final static int MIN_TRANS_TIME_I2D = 80;
	public final static int MIN_TRANS_TIME_I2I = 90;
	
	public final static int TOTAL_AIRPORT_NUM = 79;
	public final static int TOTAL_AIRCRAFT_NUM = 142;
	public final static int TOTAL_AIRCRAFTTYPE_NUM = 5;
	
	public final static String EXCEL_FILENAME = "厦航大赛数据20170814.xls";
	//public final static String FLYTIME_FILENAME = "flytimetable.csv";
	
	//Maximum lead time
	public final static int MAX_LEAD_TIME = 360;
	//Maximum delay time
	public final static int MAX_DELAY_DOMESTIC_TIME = 1440;
	public final static int MAX_DELAY_INTERNATIONAL_TIME = 2160;
	
	//不受台风影响的航班的delay时间
	public static int NORMAL_DELAY_TIME = 360;
	//场景的邻域范围
	public final static int SCENE_NEIGHBOR = 420;
	public final static int DEADHEAD_SCALE = 3;
	
	/*//不受台风影响的航班的delay时间
	public static int NORMAL_DELAY_TIME = 240;
	//场景的邻域范围
	public final static int SCENE_NEIGHBOR = 180;
	public final static int DEADHEAD_SCALE = 2;*/
	
	//Miminum turn-out time
	public final static int MIN_BUFFER_TIME = 50;
	
	
	//Save of a straightned flight
	//public final static int SAVE_STRAIGHTEN_TIME = 60;
	
	public final static int COST_DEADHEAD = 5000;
	public final static int COST_CANCEL = 1000;
	public final static int COST_AIRCRAFTTYPE_VARIATION = 1000;
	public final static int COST_STRAIGHTEN = 750;
	public final static int COST_DELAY = 100;  //per hour
	public final static int COST_EARLINESS = 150;  //per hour
	
	public final static int SWAP_LIMIT = 180;
	
	
	public static int emptyFlightNum = 0;              //调机航班数
	public static  double cancelFlightNum = 0;          //取消航班数量
	public static  double flightTypeChangeNum = 0;      //机型发生变化的航班数量
	public static  double connectFlightStraightenNum = 0;  //联程拉直航班对的个数
	public static  double totalFlightDelayHours = 0.0;  //航班总延误时间（小时）
	public static  double totalFlightAheadHours = 0.0;  //航班总提前时间（小时）
	
	public static double objective;
	
	public static double fractionalObjective;
	
	public static int deadheadIndex = 9001;
	
	public static int initialTabuValue = 3;
	public static Set<Airport> restrictedAirportSet = new HashSet<>();
	
	/*public static int neighStartIndex = -6;
	public static int neighEndIndex = 12;*/
	
	/*public static int neighStartIndex = -2;
	public static int neighEndIndex = 8;*/
	
	/*public static int neighStartIndex = -4;
	public static int neighEndIndex = 12;*/
	
	/*public static int neighStartIndex = -8;
	public static int neighEndIndex = 24;*/
	
	public static int neighStartIndex = -8;
	public static int neighEndIndex = 24;
	
	/*public static int neighGap = 15;*/
	public static int neighGap = 5;
	
	public static int fileIndex = 1;
	
	public static long timeWindowStartTime = 6*1440+6*60;
	public static long timeWindowEndTime = 8*1440+24*60;
	
	public static long aircraftChangeThreshold = 6*1440+16*60;
	public static double aircraftChangeCostLarge = 15;
	public static double aircraftChangeCostSmall = 5;
	public static double passengerCancelCost = 4;

}