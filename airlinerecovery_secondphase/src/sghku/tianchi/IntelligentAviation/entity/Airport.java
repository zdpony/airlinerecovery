package sghku.tianchi.IntelligentAviation.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Airport {
	
	public int id;
	public List<Failure> failureList = new ArrayList<Failure>();
	public List<ClosureInfo> closedSlots = new ArrayList<ClosureInfo>();
	
	public List<ParkingInfo> parkingInfoList = new ArrayList<>();	
	
	public boolean isDomestic = false;
	
	//该机场最终需要停靠的飞机的数量
	public int finalAircraftNumber;
	
	//该机场对应的所有的sink arc
	public List<GroundArc> sinkArcList = new ArrayList<>();
		
	//初始化该机场对应的网络模型
	public void init(){
		sinkArcList.clear();
	}
	
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
		Airport other = (Airport) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	
}