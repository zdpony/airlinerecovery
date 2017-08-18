package sghku.tianchi.IntelligentAviation.comparator;

import java.util.Comparator;

import sghku.tianchi.IntelligentAviation.entity.FlightArc;

public class FlightArcComparator implements Comparator<FlightArc> {

	@Override
	public int compare(FlightArc o1, FlightArc o2) {
		// TODO Auto-generated method stub
		
		if(o1.fromNode.time < o2.fromNode.time) {
			return -1;
		}else if(o1.fromNode.time > o2.fromNode.time) {
			return 1;
		}else {
			return 0;			
		}
	}

}
