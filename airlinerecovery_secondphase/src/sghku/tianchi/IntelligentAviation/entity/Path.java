package sghku.tianchi.IntelligentAviation.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sghku.tianchi.IntelligentAviation.comparator.FlightArcComparator;
import sghku.tianchi.IntelligentAviation.comparator.FlightArcComparatorBasedOnTakeoffTime;

public class Path {
	public int id;
	public Aircraft aircraft;
	public List<FlightArc> flightArcList = new ArrayList<>();
	public List<ConnectingArc> connectingArcList = new ArrayList<>();
	public List<GroundArc> groundArcList = new ArrayList<>();
	public double value;
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(aircraft.id+","+value+",");
		List<FlightArc> fList = new ArrayList<>();
		fList.addAll(flightArcList);
		
		for(ConnectingArc arc:connectingArcList){
			fList.add(arc.firstArc);
			fList.add(arc.secondArc);
		}
		
		Collections.sort(fList, new FlightArcComparatorBasedOnTakeoffTime());
		
		for(FlightArc arc:fList){
			if(arc.flight == null){
			
			}else{
				if(arc.flight.isDeadhead){
					sb.append("d_"+arc.flight.leg.originAirport.id+"_"+arc.flight.leg.destinationAirport.id+"_"+arc.takeoffTime+"_"+arc.landingTime+",");
				}else if(arc.flight.isStraightened){
					sb.append("s_"+arc.flight.connectingFlightpair.firstFlight.id+"_"+arc.flight.connectingFlightpair.secondFlight.id+"_"+arc.takeoffTime+"_"+arc.landingTime+",");
				}else{
					sb.append("n_"+arc.flight.id+"_"+arc.takeoffTime+"_"+arc.landingTime+",");
				}
			}
		}
		
		return sb.toString();
	}
}
