package sghku.tianchi.IntelligentAviation.comparator;

import java.util.Comparator;

import sghku.tianchi.IntelligentAviation.entity.Flight;
import sghku.tianchi.IntelligentAviation.entity.Node;

public class NodeComparator implements Comparator<Node> {

	@Override
	public int compare(Node arg0, Node arg1) {
		// TODO Auto-generated method stub
		if(arg0.time < arg1.time){
			return -1;
		}else if(arg0.time > arg1.time){
			return 1;
		}else{
			return 0;
		}
	}

}
