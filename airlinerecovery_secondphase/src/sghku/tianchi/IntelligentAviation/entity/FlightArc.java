package sghku.tianchi.IntelligentAviation.entity;

import java.util.ArrayList;
import java.util.List;

import sghku.tianchi.IntelligentAviation.common.ExcelOperator;
import sghku.tianchi.IntelligentAviation.common.Parameter;

public class FlightArc {
	public int id;
	//头尾点
	public Node fromNode;  
	public Node toNode;  
	//该arc对应的成本
	public double cost;   
	//该arc的提前时间
	public int earliness;     
	//该arc的延误时间
	public int delay;   
	//该arc对应的航班
	public Flight flight;    //flight that flies this arc
	//该arc对应的执行飞机
	public Aircraft aircraft;  //aircraft that uses this arc

	//该arc对应的起飞时间
	public int takeoffTime;
	//该arc对应的降落时间
	public int landingTime;
	//该arc对应的下次准备好的飞行时间
	public int readyTime;
	
	public int passengerCapacity = 0;
	
	public int flow;
	public double fractionalFlow;
	
	//标记一个arc是否属于一个connecting arc
	public boolean isIncludedInConnecting = false;
	public ConnectingArc connectingArc = null;
		
	/****************ych******************/
 	public String writtenTimeTk;
 	public String writtenTimeLd;
	
	//打印信息
	public String getTime(){
		return "["+takeoffTime+","+landingTime+","+readyTime+"]";
	}
	
	public String toString(){
	
		return id+","+takeoffTime+"->"+landingTime+"->"+readyTime+"  "+flight.leg.originAirport.id+":"+flight.leg.destinationAirport.id;
	}
	
	//计算该arc的成本
	public void calculateCost(){
		if(flight.isStraightened){
			cost += ExcelOperator.getFlightTypeChangeParam(flight.connectingFlightpair.firstFlight.initialAircraftType, aircraft.type)*flight.connectingFlightpair.firstFlight.importance;
			
			if(flight.connectingFlightpair.firstFlight.initialAircraft.id != aircraft.id) {
				if(flight.connectingFlightpair.firstFlight.initialTakeoffT <= Parameter.aircraftChangeThreshold) {
					cost += Parameter.aircraftChangeCostLarge*flight.connectingFlightpair.firstFlight.importance;
				}else {
					cost += Parameter.aircraftChangeCostSmall*flight.connectingFlightpair.firstFlight.importance;
				}
			}
			
			cost += Parameter.COST_EARLINESS/60.0*earliness*flight.connectingFlightpair.firstFlight.importance;
			cost += Parameter.COST_DELAY/60.0*delay*flight.connectingFlightpair.firstFlight.importance;
			cost += Parameter.COST_STRAIGHTEN*(flight.connectingFlightpair.firstFlight.importance+flight.connectingFlightpair.secondFlight.importance);
			
			//如果是联程拉直航班，则只需要考虑联程拉直的乘客对应的delay
			int actualNum =  Math.min(aircraft.passengerCapacity, flight.connectingFlightpair.firstFlight.connectedPassengerNumber);
			int cancelNum = flight.connectingFlightpair.firstFlight.connectedPassengerNumber - actualNum;
			
			cost += actualNum*ExcelOperator.getPassengerDelayParameter(delay);
			cost += cancelNum*Parameter.passengerCancelCost;
			
		}else if(flight.isDeadhead){
			cost += Parameter.COST_DEADHEAD;
		}else{
			cost += earliness/60.0*Parameter.COST_EARLINESS*flight.importance;
			cost += delay/60.0*Parameter.COST_DELAY*flight.importance;

			cost += ExcelOperator.getFlightTypeChangeParam(flight.initialAircraftType, aircraft.type)*flight.importance;
			
			if(flight.initialAircraft.id != aircraft.id) {
				if(flight.initialTakeoffT <= Parameter.aircraftChangeThreshold) {
					cost += Parameter.aircraftChangeCostLarge*flight.importance;
				}else {
					cost += Parameter.aircraftChangeCostSmall*flight.importance;
				}
			}
			
			//首先考虑联程乘客
			int actualNum = 0;
			if(flight.isIncludedInConnecting) {
				actualNum += flight.connectingFlightpair.firstFlight.connectedPassengerNumber;
			}
			actualNum += flight.passengerNumber;
			
			int cancelNum = Math.max(0, actualNum-aircraft.passengerCapacity);
			actualNum =  actualNum - cancelNum;
			
			cost += actualNum*ExcelOperator.getPassengerDelayParameter(delay);
			cost += cancelNum*Parameter.passengerCancelCost;
		}
	}
	
	//检查该arc是否违反约束
	public boolean checkViolation(){
		boolean vio = false;
		//check airport fault
		
		Leg leg = flight.leg;
		
		//判断台风场景限制(起飞和降落限制)
		List<Failure> failureList = new ArrayList<>();
		failureList.addAll(leg.originAirport.failureList);
		failureList.addAll(leg.destinationAirport.failureList);
		
		for(Failure scene:failureList){
        	if(scene.isInScene(0, 0, leg.originAirport, leg.destinationAirport, takeoffTime, landingTime)) {
                vio = true;
                break;
            }
		}
		
		if(!vio){
			//判断停机时间是否在台风停机故障内
			for(Failure scene:leg.destinationAirport.failureList){
				if(scene.isStopInScene(0, 0, leg.destinationAirport, landingTime, readyTime)){
					vio = true;
					break;
				}
			}
		}
		if(!vio){
			//判断是否在机场关闭时间内
			for(ClosureInfo ci:leg.originAirport.closedSlots){
				if(takeoffTime>ci.startTime && takeoffTime<ci.endTime){
					vio = true;
					break;
				}
			}
			
			for(ClosureInfo ci:leg.destinationAirport.closedSlots){
				if(landingTime>ci.startTime && landingTime<ci.endTime){
					vio = true;
					break;
				}
			}
		}
		
		return vio;
	}
	
	//如果该arc选择，更新对应的aircraft， flight的信息
	public void update() {

		this.aircraft.flightList.add(this.flight);
		this.flight.aircraft = this.aircraft;
		
		this.flight.isCancelled = false;
		//更新航班时间
		this.flight.actualTakeoffT = takeoffTime;
		this.flight.actualLandingT = landingTime;
		
	}
}
