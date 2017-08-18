package sghku.tianchi.IntelligentAviation.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sghku.tianchi.IntelligentAviation.common.Parameter;

public class Aircraft {

	public int id;
	public int type;
	public int passengerCapacity;
	public List<Leg> tabuLegs = new ArrayList<Leg>();
	public List<Failure> faultList = new ArrayList<Failure>();

	//飞机的初始机场
	public Airport initialLocation;
	
	//下面属性是构建网络模型时候需要的属性
	
	//所有单个航班
	public List<Flight> singleFlightList = new ArrayList<>();
	//所有联程航班
	public List<ConnectingFlightpair> connectingFlightList = new ArrayList<>();
	//所有调剂航班
	public List<Flight> deadheadFlightList = new ArrayList<>();
	//所有联程拉直航班
	public List<Flight> straightenedFlightList = new ArrayList<>();
	
	//该飞机对应的飞行arc
	public List<FlightArc> flightArcList = new ArrayList<>();
 	//该飞机对应的联程航班arc
	public List<ConnectingArc> connectingArcList = new ArrayList<>();
	//该飞机对应的地面arc
 	public List<GroundArc> groundArcList = new ArrayList<>();
	//每一个飞机网络里面维持平衡的回转arc
 	public GroundArc turnaroundArc;
 	
 	//该飞机对应的网络中点集合
	public Map<Integer, Node>[] nodeMapArray = new HashMap[Parameter.TOTAL_AIRPORT_NUM];
	public List<Node>[] nodeListArray = new ArrayList[Parameter.TOTAL_AIRPORT_NUM];
	
	//该飞机网络里面的source和sink点
	public Node sourceNode;
	public Node sinkNode;
	
	//最终飞机选择的航班列表
	public List<Flight> flightList = new ArrayList<>();
	
	//飞机成本
	public double unitCost;
	public double totalCost;
	public double totalFlyTime;
	
	//用于在tabu search中标记该飞机是否被tabu
	public int tabuIndex = 0;
	
	public boolean isFixed = false;
	public Airport fixedDestination = null;
	
	//计算两个飞机之间swap的概率
	public int calculateSimilarity(Aircraft a2){
		int totalRelatedValue = 0;
		for(Flight f1:flightList){
			for(Flight f2:a2.flightList){
				if(f1.leg.originAirport.equals(f2.leg.originAirport)){
					if(Math.abs(f1.initialTakeoffT-f2.initialTakeoffT) <= Parameter.SWAP_LIMIT){
						if(!tabuLegs.contains(f2.leg) && !a2.tabuLegs.contains(f1.leg)){
							totalRelatedValue++;
						}
					}
				}
			}
		}
		
		return totalRelatedValue;
	}
	
	//初始化该飞机的网络模型
	public void init(){
		singleFlightList.clear();
		connectingFlightList.clear();
		deadheadFlightList.clear();
		straightenedFlightList.clear();
				
		flightArcList.clear();
		connectingArcList.clear();
		for(int i=0;i<Parameter.TOTAL_AIRPORT_NUM;i++){
			if(nodeMapArray[i] != null){
				nodeMapArray[i].clear();				
			}
			
			if(nodeListArray[i] != null){
				nodeListArray[i].clear();
			}
			
		}
		
		sourceNode = null;
		sinkNode = null;
		
		groundArcList.clear();
		turnaroundArc = null;
	}
	
	//生成调机航班
	public List<Flight> generateDeadheadFlight(List<Leg> legList, List<Flight> candidateFlightList) {
		List<Flight> deadheadFlightList = new ArrayList<>();
		
		for(int i=0;i<candidateFlightList.size()-1;i++) {
			Flight fi = candidateFlightList.get(i);

			for(int j=i+1;j<Math.min(candidateFlightList.size(), i+Parameter.DEADHEAD_SCALE);j++) {
				Flight fj = candidateFlightList.get(j);
				
				//检查fi的出发机场是否和fj的到达机场一样
				if(fi.leg.originAirport.equals(fj.leg.destinationAirport)){
					continue;
				}
				
				//检查fi和fj是否属于同一个联程航班
				if(fi.isIncludedInConnecting && fj.isIncludedInConnecting && fi.flightNo == fj.flightNo){
					continue;
				}
				
				//出发和到达都必须是国内机场
				if(!fi.isDomestic || !fj.isDomestic) {
					continue;
				}
				
				int legId = (fi.leg.originAirport.id-1)*Parameter.TOTAL_AIRPORT_NUM + fj.leg.destinationAirport.id-1;
				Leg leg = legList.get(legId);
				
				if(!this.tabuLegs.contains(leg)) {
					//如果该航段没有被该飞机禁飞，则生成一个调机航班
				
					int flyTime = leg.flytimeArray[type-1];
					//如果飞行时间为正数
					if(flyTime > 0) {
						Flight deadheadFlight = new Flight();
						deadheadFlight.isDeadhead = true;
						deadheadFlight.leg = leg;
						
						deadheadFlight.flyTime = flyTime;
								
						deadheadFlight.initialTakeoffT = fi.initialTakeoffT;
						deadheadFlight.initialLandingT = deadheadFlight.initialTakeoffT + flyTime;
						
						deadheadFlight.isAllowtoBringForward = fi.isAllowtoBringForward;
						deadheadFlight.isAffected = fi.isAffected;
						deadheadFlight.earliestPossibleTime = fi.earliestPossibleTime;
						deadheadFlight.latestPossibleTime = fi.latestPossibleTime;
						
						deadheadFlightList.add(deadheadFlight);
					}					
				}	
			}
		}
		
		return deadheadFlightList;
	}
	
	//生成联程拉直航班
 	public Flight generateStraightenedFlight(ConnectingFlightpair cp) {
 		//当允许联程拉直的时候
 		if(cp.isAllowStraighten) {
 			int flyTime = cp.straightenLeg.flytimeArray[type-1];
			
			if(flyTime <= 0){  // if cannot retrieve fly time
				flyTime = cp.firstFlight.initialLandingT-cp.firstFlight.initialTakeoffT+ cp.secondFlight.initialLandingT-cp.secondFlight.initialTakeoffT;
			}
			
			//生成联程拉直航班
			
			Flight straightenedFlight = new Flight();
			straightenedFlight.isStraightened = true;
			straightenedFlight.connectingFlightpair = cp;
			straightenedFlight.leg = cp.straightenLeg;
			
			straightenedFlight.flyTime = flyTime;
					
			straightenedFlight.initialTakeoffT = cp.firstFlight.initialTakeoffT;
			straightenedFlight.initialLandingT = straightenedFlight.initialTakeoffT + flyTime;
			
			straightenedFlight.isAllowtoBringForward = cp.firstFlight.isAllowtoBringForward;
			straightenedFlight.isAffected = cp.firstFlight.isAffected;
			straightenedFlight.isDomestic = true;
			straightenedFlight.earliestPossibleTime = cp.firstFlight.earliestPossibleTime;
			straightenedFlight.latestPossibleTime = cp.firstFlight.latestPossibleTime;
			
			return straightenedFlight;
 		}
 		
 		return null;
 	}
	
	//检查某一个飞机是否可以飞某一趟航班
	public boolean checkFlyViolation(Flight f) {
		if(tabuLegs.contains(f.leg)) {
			return true;
		}else {
			if(f.isDeadhead) {
				int flyTime = f.leg.flytimeArray[type-1];
				
				if(flyTime <= 0) {
					return true;
				}
			}
		}	
		
		return false;
	}
	
	public boolean checkFlyViolation(ConnectingFlightpair cp) {
		
		boolean vio1 = tabuLegs.contains(cp.firstFlight.leg);
		boolean vio2 = tabuLegs.contains(cp.secondFlight.leg);
		
		if(vio1 || vio2) {
			return true;
		}else {
			return false;
		}
		
	}
	
	//计算当前飞机方案的成本和总共飞行时间
	public void updateCost() {
		unitCost = 0;
		totalCost = 0;
		if(flightList.size() > 0) {
			for(Flight f : flightList) {
				totalCost += f.calculateCost();
			}
			
			//unitCost = totalCost /flightList.size();
			unitCost = totalCost;
		}	
		
		totalFlyTime = 0;
		
		for(Flight f:flightList) {
			totalFlyTime += f.actualLandingT-f.actualTakeoffT;
		}
	}
	
}