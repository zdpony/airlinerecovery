package sghku.tianchi.IntelligentAviation.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import sghku.tianchi.IntelligentAviation.entity.Aircraft;
import sghku.tianchi.IntelligentAviation.entity.Airport;
import sghku.tianchi.IntelligentAviation.entity.ClosureInfo;
import sghku.tianchi.IntelligentAviation.entity.Failure;
import sghku.tianchi.IntelligentAviation.entity.FailureType;
import sghku.tianchi.IntelligentAviation.entity.Flight;
import sghku.tianchi.IntelligentAviation.entity.Leg;
import sghku.tianchi.IntelligentAviation.entity.ConnectingFlightpair;
import sghku.tianchi.IntelligentAviation.entity.TransferPassenger;

public class ExcelOperator {


	public static List<Failure> getFaultList(String filename, List<Airport> airportList, List<Flight> flightList, List<Aircraft> aircraftList, long earliestTime, Date earliestDate, long latestTime, Date latestDate){
			

		List<Failure> faultList = new ArrayList<Failure>();

		try{

			//读�?�Excel
			InputStream inp = new FileInputStream(filename);
			Workbook wb = WorkbookFactory.create(inp);

			Sheet sheet = wb.getSheetAt(3);	//获�?�故障表

			for(int i=1; i<sheet.getPhysicalNumberOfRows(); i++){

				Row row = sheet.getRow(i);

				Failure fault = new Failure();
				fault.id = i;	//故障ID
				fault.startTime = row.getCell(0).getDateCellValue();	//开始时间
				fault.endTime = row.getCell(1).getDateCellValue();	//结�?�时间

				//获�?�故障类型
				String str = row.getCell(2).getStringCellValue();
				if(str.equals("降落")){
					fault.type = FailureType.landing;
				}else if(str.equals("起飞")){
					fault.type = FailureType.takeoff;
				}else if(str.equals("停机")){
					fault.type = FailureType.parking;
				}

				//获�?��?�影�?机场信�?�
				if(row.getCell(3) == null||(int)row.getCell(3).getNumericCellValue()==0){
					fault.airport = null;
				}else{
					fault.airport = airportList.get((int)row.getCell(3).getNumericCellValue()-1);
				}

				//获�?��?�影�?航�?�ID
				if(row.getCell(4) == null||(int)row.getCell(4).getNumericCellValue()==0){
					fault.flight = null;
				}else{
					fault.flight = flightList.get((int)row.getCell(4).getNumericCellValue()-1);
				}

				//获�?��?�影�?飞机信�?�
				if(row.getCell(5) == null||(int)row.getCell(5).getNumericCellValue()==0){
					fault.aircraft = null;
				}else{
					fault.aircraft = aircraftList.get((int)row.getCell(5).getNumericCellValue()-1);
				}
				
				if(fault.airport != null){
					fault.airport.failureList.add(fault);
				}else if(fault.aircraft != null){
					fault.aircraft.faultList.add(fault);
				}else{
					fault.flight.faultList.add(fault);
				}

				//停机数   -- 
				if(row.getCell(6) == null){
					fault.parkingLimit = 10000;
				}else{
					fault.parkingLimit = (int)row.getCell(6).getNumericCellValue();
				}


				//转�?�fault的开始和结�?�时间，截�?�所需分钟段
				if(fault.startTime.getTime() < earliestTime){
					fault.sTime = earliestDate.getDate()*1440+earliestDate.getHours()*60+earliestDate.getMinutes();
				}else{
					fault.sTime = fault.startTime.getDate()*1440+fault.startTime.getHours()*60+fault.startTime.getMinutes();					
				}

				if(fault.endTime.getTime() > latestTime){
					fault.eTime = latestDate.getDate()*1440+latestDate.getHours()*60+latestDate.getMinutes();
				}else{
					fault.eTime = fault.endTime.getDate()*1440+fault.endTime.getHours()*60+fault.endTime.getMinutes();					
				}

				//System.out.println(fault.type);
				faultList.add(fault);
			}

			inp.close();	//关闭Excel文件

			return faultList;
		}catch(Exception e){
			System.out.println("An exception has occured in 'getFaultList'");
			System.out.println(e);		
			return null;
		}

	}
	
	public static void getClosureInfo(String filename, List<Airport> airportList, Date earliestDate, Date latestDate){

		//有效性验�?
		if(filename == null || airportList == null)	return;

		try{

			//读�?�Excel
			InputStream inp = new FileInputStream(filename);
			Workbook wb = WorkbookFactory.create(inp);

			Sheet sheet = wb.getSheetAt(2);	//获�?�机场关闭信�?�

			for(int i=1; i<sheet.getPhysicalNumberOfRows(); i++){

				Row row = sheet.getRow(i);

				//记录机场关闭信�?�
				Date closeTime = row.getCell(1).getDateCellValue();
				Date startDate = row.getCell(3).getDateCellValue();
				startDate.setHours(closeTime.getHours());
				startDate.setMinutes(closeTime.getMinutes());

				Date openTime = row.getCell(2).getDateCellValue();
				Date endDate = row.getCell(4).getDateCellValue();
				endDate.setHours(openTime.getHours());
				endDate.setMinutes(openTime.getMinutes());

				Date timewindowStart = null;
				Date timewindowEnd =  null;
				int timewindowStartTime = 0;
				int timewindowEndTime = 0;
				
				if(startDate.getTime() > earliestDate.getTime()){
					timewindowStart = startDate;
				}else{
					timewindowStart = earliestDate;
				}
				timewindowStartTime = 1440*timewindowStart.getDate()+60*timewindowStart.getHours()+timewindowStart.getMinutes();
				
				if(endDate.getTime() < latestDate.getTime()){
					timewindowEnd = endDate;
				}else{
					timewindowEnd = latestDate;
				}
				timewindowEndTime = 1440*timewindowEnd.getDate()+60*timewindowEnd.getHours()+timewindowEnd.getMinutes();

				int firstTime = 0;
				if(timewindowStartTime%1440 == 0){
					firstTime = timewindowStartTime;
				}else{
					firstTime = timewindowStartTime - timewindowStartTime%1440;
				}
				
				int dailyTimewindowStart = 60*closeTime.getHours()+closeTime.getMinutes();
				int dailyTimewidowEnd = 60*openTime.getHours()+openTime.getMinutes();

				//fetch airport
				int airportID = (int)row.getCell(0).getNumericCellValue();
				Airport airport = airportList.get(airportID-1);
				
				for(int d=0;d<=10;d++){
					int currentTimewindowStart = firstTime+1440*d+dailyTimewindowStart;
					int currentTimewindowEnd = firstTime+1440*d+dailyTimewidowEnd;
									
					if(currentTimewindowStart >= timewindowStartTime && currentTimewindowEnd <= timewindowEndTime){
						ClosureInfo cInfo = new ClosureInfo();
						cInfo.startTime = currentTimewindowStart;
						cInfo.endTime = currentTimewindowEnd;
						
						//将关闭信�?�添加至相应机场
						airport.closedSlots.add(cInfo);
					}else if(currentTimewindowStart < timewindowStartTime && currentTimewindowEnd > timewindowStartTime){
						ClosureInfo cInfo = new ClosureInfo();
						cInfo.startTime = timewindowStartTime;
						cInfo.endTime = Math.min(currentTimewindowEnd, timewindowEndTime);
						
						//将关闭信�?�添加至相应机场
						airport.closedSlots.add(cInfo);
					}else if(currentTimewindowStart < timewindowEndTime && currentTimewindowEnd > timewindowEndTime){
						ClosureInfo cInfo = new ClosureInfo();
						cInfo.startTime = Math.max(timewindowStartTime, currentTimewindowStart);
						cInfo.endTime = timewindowEndTime;
						
						//将关闭信�?�添加至相应机场
						airport.closedSlots.add(cInfo);
					}
					
					if(currentTimewindowEnd > timewindowEndTime){
						break;
					}
				}

			}

			inp.close();	//关闭Excel文件

		}catch(Exception e){
			System.out.println("An exception has occured in 'getClosureInfo'");
			System.out.println(e);	
			e.printStackTrace();
		}

	}

	public static void getTabuLegs(String filename, List<Aircraft> aircraftList, List<Leg> legList){

		//有效性验�?
		if(filename == null || aircraftList == null)	return;

		try{
			//读�?�Excel
			InputStream inp = new FileInputStream(filename);
			System.out.println("file name:"+filename);
			Workbook wb = WorkbookFactory.create(inp);

			Sheet sheet = wb.getSheetAt(1);	//获�?��?飞航段列表

			for(int i=1; i<sheet.getPhysicalNumberOfRows(); i++){

				Row row = sheet.getRow(i);
				int aircraftID = (int)row.getCell(2).getNumericCellValue();	//飞机ID
				int dAirport = (int)row.getCell(0).getNumericCellValue();	//起飞机场
				int aAirport = (int)row.getCell(1).getNumericCellValue();	//降落机场

				int legID = (aAirport-1) + (dAirport-1)*Parameter.TOTAL_AIRPORT_NUM;	//计算航段ID
		
				aircraftList.get(aircraftID-1).tabuLegs.add(legList.get(legID));	//添加该航段ID至飞机的�?飞航段表中

			}

			inp.close();	//关闭Excel文件

		}catch(Exception e){
			System.out.println("An exception has occured in 'getTabuLegs'");
			System.out.println(e);		
		}

	}

	public static List<Flight> getFlightList(String filename, List<Aircraft> aircraftList, List<Leg> legList){

		//有效性验�?
		if(filename == null || aircraftList == null)	return null;

		//�?始化航�?�列表
		List<Flight> fList = new ArrayList<Flight>();

		try{

			//读�?�Excel
			InputStream inp = new FileInputStream(filename);
			Workbook wb = WorkbookFactory.create(inp);

			Sheet sheet = wb.getSheetAt(0);	//获�?�航�?�信�?�表

			for(int i=1; i<sheet.getPhysicalNumberOfRows(); i++){

				Row row = sheet.getRow(i);

				Flight flight = new Flight();

				flight.id = (int)row.getCell(0).getNumericCellValue();	//航�?�ID
				flight.date = row.getCell(1).getDateCellValue();	//日期
				flight.flightNo = (int)row.getCell(3).getNumericCellValue();	//航�?��?�	
				flight.takeoffTime = row.getCell(6).getDateCellValue();	//起飞时间
				flight.landingTime = row.getCell(7).getDateCellValue();		//�?�?�时间
				
				flight.importance = row.getCell(13).getNumericCellValue();	//�?�?系数

				//航�?�类型：国内/国际
				String str = row.getCell(2).getStringCellValue();
				if(str.equals("国内")){
					flight.isDomestic = true;
				}else{
					flight.isDomestic = false;
				}

				//获取航段信息
				int dAirport = (int)row.getCell(4).getNumericCellValue();	//起飞机场
				int aAirport = (int)row.getCell(5).getNumericCellValue();	//�?�?�机场
				flight.leg = legList.get((aAirport-1) + ((dAirport-1)*Parameter.TOTAL_AIRPORT_NUM));	//计算航段ID

				//获取飞机信息
				int aircraftID = (int)row.getCell(8).getNumericCellValue();	//飞机ID
				int aircraftType = (int)row.getCell(9).getNumericCellValue();	//飞机类型
				aircraftList.get(aircraftID-1).type = aircraftType;	//记录该飞机类型
				flight.aircraft = aircraftList.get(aircraftID-1);		//添加该飞机至该航�?�
				flight.initialAircraftType = flight.aircraft.type;
				flight.aircraft.flightList.add(flight);

				//读取乘客信息
				flight.passengerNumber = (int)row.getCell(10).getNumericCellValue();	//旅客数
				flight.connectedPassengerNumber = (int)row.getCell(11).getNumericCellValue();	//�?�程旅客数
				flight.passengerCapacity = (int)row.getCell(12).getNumericCellValue();	//�?�程旅客数

				aircraftList.get(aircraftID-1).passengerCapacity = flight.passengerCapacity;
				
				//转换起飞和降落时间
				flight.initialTakeoffT = flight.takeoffTime.getDate()*1440+flight.takeoffTime.getHours()*60+flight.takeoffTime.getMinutes();
				flight.initialLandingT = flight.landingTime.getDate()*1440+flight.landingTime.getHours()*60+flight.landingTime.getMinutes();

				fList.add(flight);

			}


			inp.close();	//关闭Excel文件

			return fList;
		}catch(Exception e){
			System.out.println("An exception has occured in 'getFlightList'");
			System.out.println(e);		
			return null;
		}

	}
	//读取所有的联程航班
	public static List<ConnectingFlightpair> getConnectingFlightList(List<Flight> flightList, List<Leg> legList){
		List<ConnectingFlightpair> connectingFlightList = new ArrayList<>();
		for(int i=0;i<flightList.size();i++){
			Flight fi = flightList.get(i);
			for(int j=i+1;j<flightList.size();j++){
				Flight fj = flightList.get(j);
				int legId = (fi.leg.originAirport.id-1)*Parameter.TOTAL_AIRPORT_NUM + fj.leg.destinationAirport.id-1;

				Leg leg = legList.get(legId);
				
				if(fi.date.getDate()==fj.date.getDate()&&fi.flightNo==fj.flightNo){
					if(fi.takeoffTime.getTime()<fj.takeoffTime.getTime()){
						ConnectingFlightpair mf = new ConnectingFlightpair();
						mf.firstFlight = fi;
						mf.secondFlight = fj;
						mf.straightenLeg = leg;  //corresponds to straighten flight
						mf.initialAircraft = fi.aircraft;
						
						connectingFlightList.add(mf);
					}else{
						ConnectingFlightpair mf = new ConnectingFlightpair();
						mf.firstFlight = fj;
						mf.secondFlight = fi;
						mf.straightenLeg = leg;  //corresponds to straighten flight
						mf.initialAircraft = fi.aircraft;
						
						connectingFlightList.add(mf);
					}
					break;
				}
				
			}
		}
		return connectingFlightList;
	}

	public static void readflytimeMap(String filename, List<Leg> legList){
				
		for(Leg leg:legList){
			for(int i=0;i<Parameter.TOTAL_AIRCRAFTTYPE_NUM;i++){
				leg.flytimeArray[i] = -1;   //infeasible to fly between them
			}
		}
		try {
			
			InputStream inp = new FileInputStream(filename);
			Workbook wb = WorkbookFactory.create(inp);

			Sheet sheet = wb.getSheetAt(4);	// fly time sheet
			
			for(int i=1; i<sheet.getPhysicalNumberOfRows(); i++){
				Row row = sheet.getRow(i);
				
				int aircraftType = (int)row.getCell(0).getNumericCellValue();
				int origin = (int)row.getCell(1).getNumericCellValue();
				int destination = (int)row.getCell(2).getNumericCellValue();
				int flyTime = (int)row.getCell(3).getNumericCellValue();
				
				
				int legId = (origin-1)*Parameter.TOTAL_AIRPORT_NUM+destination-1;
				Leg leg = legList.get(legId);
				
				leg.flytimeArray[aircraftType-1] = flyTime;
				
			}		
		} catch (EncryptedDocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	 //获取机型变换参数
    public static double getFlightTypeChangeParam(int typeOne, int typeTwo){
        double coefficient = 0.0;
        if(typeOne == typeTwo
                || typeOne == 2 && typeTwo == 1){
            coefficient = 0.0;
        }
        else if(typeOne == 1 && typeTwo == 2){
            coefficient = 0.5;
        }
        else if((typeOne == 1 && typeTwo == 3)
                || (typeOne == 1 && typeTwo == 4)
                || (typeOne == 2 && typeTwo == 3)
                || (typeOne == 2 && typeTwo == 4)){
            coefficient = 1.5;
        }
        else if((typeOne == 3 && typeTwo == 1)
                || (typeOne == 3 && typeTwo == 2)
                || (typeOne == 3 && typeTwo == 4)
                || (typeOne == 4 && typeTwo == 3)){
            coefficient = 2.0;
        }
        else if((typeOne == 4 && typeTwo == 1)
                || (typeOne == 4 && typeTwo == 2)){
            coefficient = 4.0;
        }
        return 500 * coefficient;
    }
    
    public static double getPassengerDelayParameter(int delay) {
    	if(delay <= 2 && delay > 0) {
    		return 1.;
    	}else if(delay <= 4) {
    		return 1.5;
    	}else if(delay <= 8) {
    		return 2.;
    	}else if(delay <= 36) {
    		return 3.;
    	}else {
    		System.out.println("error : delay:"+delay);
    		System.exit(1);
    		return -1000000;
    	}
    	
    }
}