package sghku.tianchi.IntelligentAviation.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sghku.tianchi.IntelligentAviation.common.Parameter;
import sghku.tianchi.IntelligentAviation.comparator.FlightComparator2;

//用来记录某一个循环的解
public class Solution {
	public double objValue;
	
	public List<Aircraft> involvedAircraftList = new ArrayList<>();
	
	public List<FlightArc> selectedFlightArcList = new ArrayList<>();
	public List<ConnectingArc> selectedConnectingArcList = new ArrayList<>();
	public List<Flight> cancelledFlightList = new ArrayList<>();
	
	
	public void update() {
		for(Aircraft aircraft:involvedAircraftList){
			aircraft.flightList.clear();
		}
		
		for(FlightArc fa:selectedFlightArcList) {
			if(fa.flight.isDeadhead){	
				
				Parameter.emptyFlightNum++;									
				
				fa.update();		
				
			}else if(fa.flight.isStraightened){
				if(fa.flight.connectingFlightpair.firstFlight.initialAircraftType != fa.aircraft.type){
					Parameter.flightTypeChangeNum += fa.flight.connectingFlightpair.firstFlight.importance;
				}
				Parameter.connectFlightStraightenNum += fa.flight.connectingFlightpair.firstFlight.importance+fa.flight.connectingFlightpair.secondFlight.importance;
				Parameter.totalFlightDelayHours += fa.delay/60.0*fa.flight.connectingFlightpair.firstFlight.importance;
				Parameter.totalFlightAheadHours += fa.earliness/60.0*fa.flight.connectingFlightpair.firstFlight.importance;

				fa.update();	
				
			}else{
				
				if(fa.flight.initialAircraftType != fa.aircraft.type){
					Parameter.flightTypeChangeNum += fa.flight.importance;
				}
				Parameter.totalFlightDelayHours += fa.delay/60.0*fa.flight.importance;
				Parameter.totalFlightAheadHours += fa.earliness/60.0*fa.flight.importance;
				
				fa.update();
		
			}
		}
		
		for(ConnectingArc arc:selectedConnectingArcList) {
			arc.update();
			
			FlightArc fa = arc.firstArc;
			if(fa.flight.initialAircraftType != fa.aircraft.type){
				Parameter.flightTypeChangeNum += fa.flight.importance;
			}
			Parameter.totalFlightDelayHours += fa.delay/60.0*fa.flight.importance;
			Parameter.totalFlightAheadHours += fa.earliness/60.0*fa.flight.importance;
			
			fa = arc.secondArc;
			if(fa.flight.initialAircraftType != fa.aircraft.type){
				Parameter.flightTypeChangeNum += fa.flight.importance;
			}
			Parameter.totalFlightDelayHours += fa.delay/60.0*fa.flight.importance;
			Parameter.totalFlightAheadHours += fa.earliness/60.0*fa.flight.importance;
		}
		
		for(Flight f:cancelledFlightList) {
			f.isCancelled = true;
			Parameter.cancelFlightNum += f.importance;
		}
		
		for(Aircraft a:involvedAircraftList) {
			Collections.sort(a.flightList, new FlightComparator2());
		}
	}
}
