package sghku.tianchi.IntelligentAviation.entity;

import java.util.Date;

import sghku.tianchi.IntelligentAviation.common.Parameter;

public class Failure {
	
	public int id;
	public FailureType type;

	public Date startTime;
	public Date endTime;
	//public int type;
	public Airport airport;
	public Flight flight;
	public Aircraft aircraft;
	public int parkingLimit;
	
	/**************zd**************/
	public int sTime;
	public int eTime;
	

	
	//print itself
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		sb.append(startTime+","+endTime+","+type+",");
		if(airport != null){
			sb.append(airport.id+",");
		}else{
			sb.append("null,");
		}
		if(flight != null){
			sb.append(flight.id+",");
		}else{
			sb.append("null,");
		}
		sb.append(parkingLimit+","+sTime+","+eTime);
		return sb.toString();
	}
	
	
	/**
     * 根据传进来的数据，判断是否落在受影响的场景内
     * @param flightId       //暂时没有航班故障做处理
     * @param airplaneId     //暂时没有飞机故障做处理
     * @param startAirport
     * @param endAirport
     * @param startTime
     * @param endTime
     * @return
     */
    public boolean isInScene(int flightId, int airplaneId, Airport startAirport, Airport endAirport, int startTime, int endTime){
        if(type.equals(FailureType.parking)) {         //停机判断
            if(airport.equals(endAirport) && endTime>sTime && endTime<eTime)
        	//if(airport.equals(endAirport) && endTime>sTime && endTime<eTime)
                return true;
        }
        else if(type.equals(FailureType.landing)){    //降落判断
        	if(airport.equals(endAirport) && endTime>sTime && endTime<eTime)
        	//if(airport.equals(endAirport) && endTime>sTime && endTime<eTime)
                return true;
        }
        else if(type.equals(FailureType.takeoff)){    //起飞判断
        	if(airport.equals(startAirport) && startTime>sTime && startTime<eTime)
        	//if(airport.equals(startAirport) && startTime>sTime && startTime<eTime)
                return true;
        }
        return false;
    }

    //判断是否在某一个场景的领域内
    public boolean isRelatedToScene(int flightId, int airplaneId, Airport startAirport, Airport endAirport, int startTime, int endTime){
        if(type.equals(FailureType.parking)) {         //停机判断
            if(airport.equals(endAirport) && endTime>sTime-Parameter.SCENE_NEIGHBOR && endTime<eTime+Parameter.SCENE_NEIGHBOR)
        	//if(airport.equals(endAirport) && endTime>sTime && endTime<eTime)
                return true;
        }
        else if(type.equals(FailureType.landing)){    //降落判断
        	if(airport.equals(endAirport) && endTime>sTime-Parameter.SCENE_NEIGHBOR && endTime<eTime+Parameter.SCENE_NEIGHBOR)
        	//if(airport.equals(endAirport) && endTime>sTime && endTime<eTime)
                return true;
        }
        else if(type.equals(FailureType.takeoff)){    //起飞判断
        	if(airport.equals(startAirport) && startTime>sTime-Parameter.SCENE_NEIGHBOR && startTime<eTime+Parameter.SCENE_NEIGHBOR)
        	//if(airport.equals(startAirport) && startTime>sTime && startTime<eTime)
                return true;
        }
        return false;
    }
    
    /**
     * 根据传进来的数据，判断起飞是否落在受影响的场景内
     * @param flightId       //暂时没有航班故障做处理
     * @param airplaneId     //暂时没有飞机故障做处理
     * @param startAirport
     * @param startTime
     * @return
     */
    public boolean isStartInScene(int flightId, int airplaneId, Airport startAirport, int startTime){
        if(type.equals(FailureType.takeoff)){    //起飞故障判断
        	if(airport.equals(startAirport) && startTime>sTime && startTime<eTime)
        	//if(airport.equals(startAirport) && startTime>sTime && startTime<eTime)
                return true;
        }
        return false;
    }

    /**
     * 根据传进来的数据，判断降落是否落在受影响的场景内
     * @param flightId       //暂时没有航班故障做处理
     * @param airplaneId     //暂时没有飞机故障做处理
     * @param endAirport
     * @param endTime
     * @return
     */
    public boolean isEndInScene(int flightId, int airplaneId, Airport endAirport, int endTime){
       if(type.equals(FailureType.landing)){    //降落故障判断
    	   if(airport.equals(endAirport) && endTime>sTime && endTime<eTime)
    	   //if(airport.equals(endAirport) && endTime>sTime && endTime<eTime)
               return true;
        }
        return false;
    }

    /**
     * 根据传进来的数据，判断停机时间段是否落在受影响的场景内
     * @param flightId
     * @param airplaneId
     * @param airport
     * @param startTime
     * @param endTime
     * @return
     */
    public boolean isStopInScene(int flightId, int airplaneId, Airport airport, int startTime, int endTime){
        if(type.equals(FailureType.parking)){    //机场停机判断
        	if(this.airport.equals(airport) && startTime<eTime && endTime>sTime)
                return true;
        		//if(this.airport.equals(airport) && ((!(endTime<sTime)) && (!(startTime>eTime)))) //old version
        }
        return false;
    }
    
    /**
     * 根据传进来的数据，判断停机时间段是否落在受影响的场景的邻域
     * @param flightId
     * @param airplaneId
     * @param airport
     * @param startTime
     * @param endTime
     * @return
     */
    public boolean isRelatedToStopScene(int flightId, int airplaneId, Airport airport, int startTime, int endTime){
        if(type.equals(FailureType.parking)){    //机场停机判断
        	if(this.airport.equals(airport) && startTime<eTime+Parameter.SCENE_NEIGHBOR && endTime>sTime-Parameter.SCENE_NEIGHBOR)
                return true;
        		//if(this.airport.equals(airport) && ((!(endTime<sTime)) && (!(startTime>eTime)))) //old version
        }
        return false;
    }
}