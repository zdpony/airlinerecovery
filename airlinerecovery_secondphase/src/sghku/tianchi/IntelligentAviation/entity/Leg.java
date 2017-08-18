package sghku.tianchi.IntelligentAviation.entity;

import java.util.ArrayList;
import java.util.List;

import sghku.tianchi.IntelligentAviation.common.Parameter;

//航段
public class Leg {
	
	public int id;
	public Airport originAirport;
	public Airport destinationAirport;
	
	//不同型号飞机执行该航段的飞行时间
	public int[] flytimeArray = new int[Parameter.TOTAL_AIRCRAFTTYPE_NUM];
	
	public List<Flight> flightList = new ArrayList<>();
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Leg other = (Leg) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	public String toString(){
		return "Leg "+id+" ("+originAirport.id+"->"+destinationAirport.id+")";
	}
}