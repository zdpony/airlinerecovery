package sghku.tianchi.IntelligentAviation.entity;

import java.util.ArrayList;
import java.util.List;

//联程航班
public class ConnectingFlightpair {
	public int id;
	//联程航班的第一段航班
 	public Flight firstFlight;
 	//联程航班的第二段航班
 	public Flight secondFlight;
 	//初始的时候执行该联程航班的飞机
 	public Aircraft initialAircraft;
 	//该联程航班拉直之后对应的leg
 	public Leg straightenLeg;
 	//该联程航班是否允许拉直
 	public boolean isAllowStraighten = false;
 	//该联程航班是否受到影响，只有受影响的航班才可以长时间延误
	public boolean isAffected = false;
 	

	//检测该联程航班是否可以被拉直
 	public void checkStraighten(){
 		
 		//当且仅当中间机场受影响时可拉直航班
 		if(firstFlight.isDomestic && secondFlight.isDomestic){
 			for(Failure scene:firstFlight.leg.destinationAirport.failureList){
 				if(scene.isEndInScene(firstFlight.id,
 						firstFlight.aircraft.id,
 						firstFlight.leg.destinationAirport,
 						firstFlight.initialLandingT)){
 	                
 					isAllowStraighten = true;
 	                break;
 	            }
 				
 				if(scene.isStartInScene(secondFlight.id,
 							secondFlight.aircraft.id,
 							secondFlight.leg.originAirport,
 							secondFlight.initialTakeoffT)){
					
 					isAllowStraighten = true;
 	                break;
 	            }
 			}
 		}
 	}
 	//检测该联程航班是否受到影响
 	public void checkAffected(){
 		if(firstFlight.isAffected || secondFlight.isAffected){
 			this.isAffected = true;
 		}
 	}
 	
 	
}
