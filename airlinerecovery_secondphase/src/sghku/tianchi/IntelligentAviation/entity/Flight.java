package sghku.tianchi.IntelligentAviation.entity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sghku.tianchi.IntelligentAviation.common.Parameter;

public class Flight {

	public int id;
	public Date date;
	public boolean isDomestic;
	public int flightNo;
	public Date takeoffTime;
	public Date landingTime;
	public int passengerNumber;
	public int connectedPassengerNumber;
	public int passengerCapacity;
	public double importance;

	
	//初始执行该航班的飞机型号
	public int initialAircraftType;
	public Aircraft initialAircraft;
	
	//换算成整数值的起飞和降落时间
	public int initialTakeoffT; 
	public int initialLandingT; 

	//该航班所对应的故障
	public List<Failure> faultList = new ArrayList<Failure>();
	
	//该航班是否允许提前
	public boolean isAllowtoBringForward = false;
	//该航班是否受到故障情景影响
	public boolean isAffected = false;
	//该航班是否包含在某一个联程航班中
	public boolean isIncludedInConnecting = false;
	public Flight brotherFlight = null;

	//该航班最早和最晚可能的起飞时间
	public int earliestPossibleTime = 0;
	public int latestPossibleTime = 0;
	//该航班的飞行时间
	public int flyTime = 0;
	//该航班是否是调机航班
	public boolean isDeadhead = false;
	//该航班是否是联程拉直航班
	public boolean isStraightened = false;
	//该航班是否是联程航班(并没有拉直)
	public boolean isConnecting = false;
	
	//如果属于联程拉直航班，那么该航班对应的联程航班
	public ConnectingFlightpair connectingFlightpair = null;
	
	
	/**
	 * 以下属性为网络模型所对应的属性
	 */
	//该航班所对应的flight arc和connecting arc
	public List<FlightArc> flightarcList = new ArrayList<>();
	public List<ConnectingArc> connectingarcList = new ArrayList<>();

	//在CPLEX模型中该航班对应的ID
	public int IDInCPLEXModel;
		
	/**
	 * 以下属性为最终航班状态
	 */
	//该航班对应的航段
	public Leg leg;
	//初始执行该航班的飞机
	public Aircraft aircraft;
	//该航班是否被取消
	public boolean isCancelled = false;	
	
		
	//最终的起飞和降落时间
	public int actualTakeoffT; 
	public int actualLandingT; 
	
	public boolean isFixed = false;
	
	//流入和流出的connection
	public List<Connection> flowinConnectionList = new ArrayList<>();
	public List<Connection> flowoutConnectionList = new ArrayList<>();
	
	//构造ghost航班来标记source和sink
	public boolean isSource = false;
	public boolean isSink = false;
	
	//标记是否处于时间窗
	public boolean isIncludedInTimeWindow = false;
	
	public Set<Integer> discreteTimePointSet = new HashSet<>();
	public List<Integer> discreteTimePointList = new ArrayList<>();
	
	public List<FlightSection> flightSectionList = new ArrayList<>();
	
	public List<TransferPassenger> passengerTransferList = new ArrayList<>();
		
	//初始化该航班所对应的网络模型
	public void init() {
		flightarcList.clear();
		connectingarcList.clear();
	}
	
	//初始化最早，最晚以及飞行时间
	public void initFlyTime() {
		//计算最早可能的起飞时间
		if(isAllowtoBringForward) {
			earliestPossibleTime = initialTakeoffT - Parameter.MAX_LEAD_TIME;
		}else {
			earliestPossibleTime = initialTakeoffT;
		}
		//计算最晚可能的起飞时间
		if(isDomestic) {
			latestPossibleTime = initialTakeoffT + Parameter.MAX_DELAY_DOMESTIC_TIME;
		}else {
			latestPossibleTime = initialTakeoffT + Parameter.MAX_DELAY_INTERNATIONAL_TIME;
		}
		
		//计算飞行时间
		flyTime = initialLandingT-initialTakeoffT;
	}
	
	//初始化航班的最终状态
	public void initStatus() {
		isCancelled = false;
		actualTakeoffT = initialTakeoffT; 
		actualLandingT = initialLandingT; 
	}

	//检测该航班可否被提前
	public void checkBringForward() {
		if (isDomestic) {
			boolean affectFlag = false;
			for (Failure scene : leg.originAirport.failureList) {
				if (scene.isStartInScene(this.id, this.aircraft.id, this.leg.originAirport, this.initialTakeoffT)) {
					affectFlag = true;
					break;
				}
			}
			this.isAllowtoBringForward = affectFlag;
		} else {
			this.isAllowtoBringForward = false;
		}
	}
	
	// 检测航班是否受到影响
	public void checkAffected() {
	
		// 判断是否在台风场景限制的邻域(起飞和降落限制)
		boolean vio = false;
		List<Failure> failureList = new ArrayList<>();
		failureList.addAll(leg.originAirport.failureList);
		failureList.addAll(leg.destinationAirport.failureList);

		for (Failure scene : failureList) {
			if (scene.isRelatedToScene(0, 0, leg.originAirport, leg.destinationAirport, initialTakeoffT, initialLandingT)) {
				vio = true;
				break;
			}
		}

		if (!vio) {
			// 判断停机时间是否在台风停机故障的邻域
			for (Failure scene : leg.destinationAirport.failureList) {
				if (scene.isRelatedToStopScene(0, 0, leg.destinationAirport, initialLandingT, initialLandingT+Parameter.MIN_BUFFER_TIME)) {
					vio = true;
					break;
				}
			}
		}
		if (!vio) {
			// 判断是否在机场关闭时间内
			for (ClosureInfo ci : leg.originAirport.closedSlots) {
				if (initialTakeoffT > ci.startTime-Parameter.SCENE_NEIGHBOR && initialTakeoffT < ci.endTime+Parameter.SCENE_NEIGHBOR) {
					vio = true;
					break;
				}
			}

			for (ClosureInfo ci : leg.destinationAirport.closedSlots) {
				if (initialLandingT > ci.startTime-Parameter.SCENE_NEIGHBOR && initialLandingT < ci.endTime+Parameter.SCENE_NEIGHBOR) {
					vio = true;
					break;
				}
			}
		}

		this.isAffected = vio;
	}

	//打印该航班信息
	public String toString() {
		return id + "," + leg.originAirport.id + "," + leg.destinationAirport.id + "," + initialTakeoffT + "," + initialLandingT + ","
				+ aircraft.id;
	}

	//计算航班在当前方案下的成本
	public double calculateCost(){
		double cost = 0;
		int earliness = 0;
		int delay = 0;
		
		if(this.isStraightened){
			earliness = Math.max(0, this.connectingFlightpair.firstFlight.initialTakeoffT - this.actualTakeoffT);
			delay = Math.max(0, this.actualTakeoffT - this.connectingFlightpair.firstFlight.initialTakeoffT);
			
			if(this.connectingFlightpair.firstFlight.initialAircraftType != aircraft.type){
				cost += Parameter.COST_AIRCRAFTTYPE_VARIATION*this.connectingFlightpair.firstFlight.importance;
			}
			
			cost += Parameter.COST_EARLINESS/60.0*earliness*this.connectingFlightpair.firstFlight.importance;
			cost += Parameter.COST_DELAY/60.0*delay*this.connectingFlightpair.firstFlight.importance;
			cost += Parameter.COST_STRAIGHTEN*(this.connectingFlightpair.firstFlight.importance+this.connectingFlightpair.secondFlight.importance);
			
		}else if(this.isDeadhead){
			cost += Parameter.COST_DEADHEAD;
		}else{
			earliness = Math.max(0, this.initialTakeoffT - this.actualTakeoffT);
			delay = Math.max(0, this.actualTakeoffT - this.initialTakeoffT);
						
			cost += earliness/60.0*Parameter.COST_EARLINESS*this.importance;
			cost += delay/60.0*Parameter.COST_DELAY*this.importance;

			if(this.initialAircraftType != aircraft.type){
				cost += Parameter.COST_AIRCRAFTTYPE_VARIATION*this.importance;
			}
		}
		
		return cost;
	}
}