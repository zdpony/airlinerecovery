package sghku.tianchi.IntelligentAviation.comparator;

import java.util.Comparator;

import sghku.tianchi.IntelligentAviation.entity.Aircraft;

public class AircraftComparator implements Comparator<Aircraft> {

	@Override
	public int compare(Aircraft arg0, Aircraft arg1) {
		// TODO Auto-generated method stub
		
		if(arg0.unitCost > arg1.unitCost) {
			return -1;
		}else if(arg0.unitCost < arg1.unitCost) {
			return 1;
		}else {
			return 0;			
		}
	}

}
