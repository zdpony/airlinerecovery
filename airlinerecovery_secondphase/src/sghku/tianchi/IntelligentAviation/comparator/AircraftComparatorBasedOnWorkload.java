package sghku.tianchi.IntelligentAviation.comparator;

import java.util.Comparator;

import sghku.tianchi.IntelligentAviation.entity.Aircraft;

public class AircraftComparatorBasedOnWorkload implements Comparator<Aircraft> {

	@Override
	public int compare(Aircraft arg0, Aircraft arg1) {
		// TODO Auto-generated method stub
		if(arg0.totalFlyTime < arg1.totalFlyTime) {
			return -1;
		}else if(arg0.totalFlyTime > arg1.totalFlyTime) {
			return 1;
		}else {
			return 0;
		}
	}

}
