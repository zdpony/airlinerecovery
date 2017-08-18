package sghku.tianchi.IntelligentAviation.comparator;

import java.util.Comparator;

import sghku.tianchi.IntelligentAviation.entity.LineValue;

public class LineValueComparator implements Comparator<LineValue> {

	@Override
	public int compare(LineValue o1, LineValue o2) {
		// TODO Auto-generated method stub
		if(o1.value > o2.value) {
			return -1;
		}else if(o1.value < o2.value) {
			return 1;
		}else {
			return 0;		
		}	
	}

}
