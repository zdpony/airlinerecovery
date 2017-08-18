package sghku.tianchi.IntelligentAviation.comparator;

import java.util.Comparator;

import sghku.tianchi.IntelligentAviation.entity.Flight;

public class FlightComparator implements Comparator<Flight> {

	@Override
	public int compare(Flight arg0, Flight arg1) {
		// TODO Auto-generated method stub
		if(arg0.initialTakeoffT < arg1.initialTakeoffT){
			return -1;
		}else if(arg0.initialTakeoffT > arg1.initialTakeoffT){
			return 1;
		}else{
			return 0;
		}
	}

}
