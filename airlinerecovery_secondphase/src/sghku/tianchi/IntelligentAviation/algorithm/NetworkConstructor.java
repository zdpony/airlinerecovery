package sghku.tianchi.IntelligentAviation.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sghku.tianchi.IntelligentAviation.common.Parameter;
import sghku.tianchi.IntelligentAviation.comparator.NodeComparator;
import sghku.tianchi.IntelligentAviation.entity.Aircraft;
import sghku.tianchi.IntelligentAviation.entity.Airport;
import sghku.tianchi.IntelligentAviation.entity.ConnectingArc;
import sghku.tianchi.IntelligentAviation.entity.Failure;
import sghku.tianchi.IntelligentAviation.entity.FailureType;
import sghku.tianchi.IntelligentAviation.entity.Flight;
import sghku.tianchi.IntelligentAviation.entity.FlightArc;
import sghku.tianchi.IntelligentAviation.entity.FlightSection;
import sghku.tianchi.IntelligentAviation.entity.FlightSectionItinerary;
import sghku.tianchi.IntelligentAviation.entity.GroundArc;
import sghku.tianchi.IntelligentAviation.entity.Leg;
import sghku.tianchi.IntelligentAviation.entity.ConnectingFlightpair;
import sghku.tianchi.IntelligentAviation.entity.Node;
import sghku.tianchi.IntelligentAviation.entity.ParkingInfo;
import sghku.tianchi.IntelligentAviation.entity.TransferPassenger;

/**
 * @author ych
 * Construct a feasible route (a sequence of flights) for all aircraft
 *
 */
public class NetworkConstructor {
	
	//第一种生成方法，根据原始schedule大范围生成arc
	public void generateArcForFlight(Aircraft aircraft, Flight f, int gap){
		FlightArc arc = null;
		
		//2.1 check whether f can be brought forward and generate earliness arcs
		
		int startIndex = 0;
		int endIndex = 0;
		
		if(f.isDeadhead) {
			//如果是调机航班，则只能小范围延误
			endIndex = Parameter.NORMAL_DELAY_TIME/gap;
		}else {
			if(f.isAffected){
				if(f.isDomestic){
					endIndex = Parameter.MAX_DELAY_DOMESTIC_TIME/gap;
				}else{
					endIndex = Parameter.MAX_DELAY_INTERNATIONAL_TIME/gap;
				}
			}else{
				endIndex = Parameter.NORMAL_DELAY_TIME/gap;
			}
		}
		
		
		if(f.isAllowtoBringForward){
			startIndex = Parameter.MAX_LEAD_TIME/gap;
		}
		
		int flyTime = f.flyTime;
		
		if(f.isDeadhead) {
			flyTime = f.leg.flytimeArray[aircraft.type-1];
		}else if(f.isStraightened) {
			flyTime = f.leg.flytimeArray[aircraft.type-1];
			if(flyTime <= 0) {
				flyTime = f.connectingFlightpair.firstFlight.initialLandingT-f.connectingFlightpair.firstFlight.initialTakeoffT + f.connectingFlightpair.secondFlight.initialLandingT-f.connectingFlightpair.secondFlight.initialTakeoffT;
			}
		}
	
		/*int currentFlightSectionIndex = 0;
		FlightSection currentFlightSection = f.flightSectionList.get(currentFlightSectionIndex);*/
	
		for(int i=-startIndex;i<=endIndex;i++){
			
			arc = new FlightArc();
			arc.flight = f;
			arc.aircraft = aircraft;
			if(i < 0) {
				arc.earliness = -i*gap;
			}else {
				arc.delay = i*gap;
			}
			
			arc.takeoffTime = f.initialTakeoffT+i*gap;
			arc.landingTime = arc.takeoffTime+flyTime;
			
			arc.readyTime = arc.landingTime + Parameter.MIN_BUFFER_TIME;
									
			if(!arc.checkViolation()){
				
				//如果是调剂航班，不需要做任何处理
				if(f.isDeadhead) {
					
				}else if(f.isStraightened) {
					//如果是联程拉直，将该arc加到对应的两段航班中
					f.connectingFlightpair.firstFlight.flightarcList.add(arc);
					f.connectingFlightpair.secondFlight.flightarcList.add(arc);
					
					//联程拉直航班则没有对应的flight section
					
					//联程拉直乘客容量
					arc.passengerCapacity = aircraft.passengerCapacity;
					//要减去对应的联程乘客
					arc.passengerCapacity = arc.passengerCapacity - f.connectedPassengerNumber;
					//其他乘客全部被取消，所以不需要考虑
					arc.passengerCapacity = Math.max(0, arc.passengerCapacity);
				}else {
					f.flightarcList.add(arc);
						
					//将该arc加入到对应的flight section中
					/*if(arc.takeoffTime >= currentFlightSection.startTime && arc.takeoffTime <= currentFlightSection.endTime) {
						currentFlightSection.flightArcList.add(arc);
					}else {
						boolean isContinue = true;
						while(isContinue) {
							currentFlightSectionIndex++;
							currentFlightSection = f.flightSectionList.get(currentFlightSectionIndex);
							
							if(arc.takeoffTime >= currentFlightSection.startTime && arc.takeoffTime <= currentFlightSection.endTime) {
								currentFlightSection.flightArcList.add(arc);
								isContinue = false;
							}
						}
					}*/
					for(FlightSection currentFlightSection:f.flightSectionList) {
						if(arc.takeoffTime >= currentFlightSection.startTime && arc.takeoffTime <= currentFlightSection.endTime) {
							currentFlightSection.flightArcList.add(arc);
							break;
						}
					}
					
					//乘客容量
					arc.passengerCapacity = aircraft.passengerCapacity;
					
					//减去转乘乘客
					for(TransferPassenger tp:arc.flight.passengerTransferList) {
						arc.passengerCapacity = arc.passengerCapacity - tp.volume;
					}
					arc.passengerCapacity = Math.max(0, arc.passengerCapacity);
				}
				
				aircraft.flightArcList.add(arc);				
				arc.calculateCost();
			}
		}
	}

	public void generateArcForConnectingFlightPair(Aircraft aircraft, ConnectingFlightpair cf, int gap, boolean isGenerateArcForEachFlight){
		//otherwise, create a set of connecting arcs for this connecting flight
		List<FlightArc> firstFlightArcList = new ArrayList<>();
		
		int connectionTime = Math.min(cf.secondFlight.initialTakeoffT-cf.firstFlight.initialLandingT, Parameter.MIN_BUFFER_TIME);
		
		if(!aircraft.tabuLegs.contains(cf.firstFlight.leg) && !aircraft.tabuLegs.contains(cf.secondFlight.leg)){
			//only if this leg is not in the tabu list of the corresponding aircraft
			FlightArc arc = null;
	
			//2.1 check whether f can be brought forward and generate earliness arcs
			int startIndex = 0;

			if(cf.firstFlight.isAllowtoBringForward){
				startIndex = Parameter.MAX_LEAD_TIME/gap;				
			}

			//2.3 generate delay arcs
			int endIndex = 0;
			if(cf.isAffected){
				if(cf.firstFlight.isDomestic){
					endIndex = Parameter.MAX_DELAY_DOMESTIC_TIME/gap;								
				}else{
					endIndex = Parameter.MAX_DELAY_INTERNATIONAL_TIME/gap;		
				}
			}else{
				endIndex = Parameter.NORMAL_DELAY_TIME/gap;		
			}
			
			for(int i=-startIndex;i<=endIndex;i++){
				
				arc = new FlightArc();
				arc.flight = cf.firstFlight;
				arc.aircraft = aircraft;
				if(i < 0) {
					arc.earliness = -i*gap;	
				}else {
					arc.delay = i*gap;
				}
			
				arc.takeoffTime = cf.firstFlight.initialTakeoffT+i*gap;
				arc.landingTime = arc.takeoffTime+cf.firstFlight.flyTime;
				arc.readyTime = arc.landingTime + connectionTime;
										
				if(!arc.checkViolation()){
					firstFlightArcList.add(arc);
				}
			}
		}
		
		for(FlightArc firstArc:firstFlightArcList){
			
			if(cf.secondFlight.isAllowtoBringForward){
				int startIndex = Parameter.MAX_LEAD_TIME/gap;

				for(int i=startIndex;i>0;i--){
					if(cf.secondFlight.initialTakeoffT-gap*i >= firstArc.readyTime){
						FlightArc secondArc = new FlightArc();
						secondArc.flight = cf.secondFlight;
						secondArc.aircraft = aircraft;
						secondArc.earliness = i*gap;
						secondArc.takeoffTime = cf.secondFlight.initialTakeoffT-secondArc.earliness;
						secondArc.landingTime = secondArc.takeoffTime+cf.secondFlight.flyTime;
						secondArc.readyTime = secondArc.landingTime + Parameter.MIN_BUFFER_TIME;

						if(!secondArc.checkViolation()){
							ConnectingArc ca = new ConnectingArc();
							ca.firstArc = firstArc;
							ca.secondArc = secondArc;
							ca.aircraft = aircraft;
							
							aircraft.connectingArcList.add(ca);
							
							cf.firstFlight.connectingarcList.add(ca);
							cf.secondFlight.connectingarcList.add(ca);
							ca.connectingFlightPair = cf;
							
							ca.calculateCost();
						}
					}
					
				}
			}
							
			int endIndex = 0;
			if(cf.isAffected){
				if(cf.secondFlight.isDomestic){
					endIndex = Parameter.MAX_DELAY_DOMESTIC_TIME/gap;								
				}else{
					endIndex = Parameter.MAX_DELAY_INTERNATIONAL_TIME/gap;		
				}
			}else{
				endIndex = Parameter.NORMAL_DELAY_TIME/gap;
			}
			
			for(int i=0;i<=endIndex;i++){
				if(cf.secondFlight.initialTakeoffT+gap*i >= firstArc.readyTime){
					
					FlightArc secondArc = new FlightArc();
					secondArc.flight = cf.secondFlight;
					secondArc.aircraft = aircraft;
					secondArc.delay = i*gap;
					secondArc.takeoffTime = cf.secondFlight.initialTakeoffT+secondArc.delay;
					secondArc.landingTime = secondArc.takeoffTime+cf.secondFlight.flyTime;
					secondArc.readyTime = secondArc.landingTime + Parameter.MIN_BUFFER_TIME;

					if(!secondArc.checkViolation()){
						ConnectingArc ca = new ConnectingArc();
						ca.firstArc = firstArc;
						ca.secondArc = secondArc;
						
						aircraft.connectingArcList.add(ca);
						
						cf.firstFlight.connectingarcList.add(ca);
						cf.secondFlight.connectingarcList.add(ca);
						ca.connectingFlightPair = cf;
						
						ca.aircraft = aircraft;
						ca.calculateCost();
						
						break;
					}
				}			
			}
			
		}
		
		for(ConnectingArc arc:aircraft.connectingArcList) {
			//设置第一个arc
			arc.firstArc.isIncludedInConnecting = true;
			arc.firstArc.connectingArc = arc;
			
			arc.firstArc.passengerCapacity = aircraft.passengerCapacity;
			//减去联程乘客
			arc.firstArc.passengerCapacity = arc.firstArc.passengerCapacity - cf.firstFlight.connectedPassengerNumber;
			//减去转机乘客
			for(TransferPassenger tp:arc.firstArc.flight.passengerTransferList) {
				arc.firstArc.passengerCapacity = arc.firstArc.passengerCapacity - tp.volume;				
			}
			arc.firstArc.passengerCapacity = Math.max(0, arc.firstArc.passengerCapacity);
			
			for(FlightSection currentFlightSection:cf.firstFlight.flightSectionList) {
				if(arc.firstArc.takeoffTime >= currentFlightSection.startTime && arc.firstArc.takeoffTime <= currentFlightSection.endTime) {
					currentFlightSection.flightArcList.add(arc.firstArc);
					break;
				}
			}
			
			//设置第二个arc
			arc.secondArc.isIncludedInConnecting = true;
			arc.secondArc.connectingArc = arc;
			
			arc.secondArc.passengerCapacity = aircraft.passengerCapacity;
			//减去联程乘客
			arc.secondArc.passengerCapacity = arc.secondArc.passengerCapacity - cf.secondFlight.connectedPassengerNumber;
			//减去转机乘客
			for(TransferPassenger tp:arc.secondArc.flight.passengerTransferList) {
				arc.secondArc.passengerCapacity = arc.secondArc.passengerCapacity - tp.volume;				
			}
			arc.secondArc.passengerCapacity = Math.max(0, arc.secondArc.passengerCapacity);
			
			for(FlightSection currentFlightSection:cf.secondFlight.flightSectionList) {
				if(arc.secondArc.takeoffTime >= currentFlightSection.startTime && arc.secondArc.takeoffTime <= currentFlightSection.endTime) {
					currentFlightSection.flightArcList.add(arc.secondArc);
					break;
				}
			}
				
		}
		
		//3. 为每一个flight生成arc，可以单独取消联程航班中的一段
		if(isGenerateArcForEachFlight) {
			if(!aircraft.tabuLegs.contains(cf.firstFlight.leg)){
				generateArcForFlight(aircraft, cf.firstFlight, gap);
			}
			
			if(!aircraft.tabuLegs.contains(cf.secondFlight.leg)){
				generateArcForFlight(aircraft, cf.secondFlight, gap);
			}
		}
	}
	
	//生成点和地面arc
	public void generateNodes(List<Aircraft> aircraftList, List<Airport> airportList){
		//2. generate nodes for each arc		
		for(Aircraft aircraft:aircraftList){
			//1. clear node map for each airport
			for(int i=0;i<airportList.size();i++){
				Airport a = airportList.get(i);
				aircraft.nodeMapArray[i] = new HashMap<>();
				aircraft.nodeListArray[i] = new ArrayList<>();
			}
		
			for(FlightArc flightArc:aircraft.flightArcList){
				
				Airport departureAirport = flightArc.flight.leg.originAirport;
				Airport arrivalAirport = flightArc.flight.leg.destinationAirport;
				
				Node node = aircraft.nodeMapArray[departureAirport.id-1].get(flightArc.takeoffTime);
						
				if(node == null){
					node = new Node();
					node.airport = departureAirport;
					node.time = flightArc.takeoffTime;
					aircraft.nodeMapArray[departureAirport.id-1].put(flightArc.takeoffTime, node);
				}
				
				node.flowoutFlightArcList.add(flightArc);
				flightArc.fromNode = node;
				
				node = aircraft.nodeMapArray[arrivalAirport.id-1].get(flightArc.readyTime);
				if(node == null){
					node = new Node();
					node.airport = arrivalAirport;
					node.time = flightArc.readyTime;
					aircraft.nodeMapArray[arrivalAirport.id-1].put(flightArc.readyTime, node);
				
				}
				
				node.flowinFlightArcList.add(flightArc);
				flightArc.toNode = node;
			}
			
			for(ConnectingArc flightArc:aircraft.connectingArcList){
				
				Airport departureAirport = flightArc.firstArc.flight.leg.originAirport;
				Airport arrivalAirport = flightArc.secondArc.flight.leg.destinationAirport;
				
				int takeoffTime = flightArc.firstArc.takeoffTime;
				int readyTime = flightArc.secondArc.readyTime;
				
				Node node = aircraft.nodeMapArray[departureAirport.id-1].get(takeoffTime);
						
				if(node == null){
					node = new Node();
					node.airport = departureAirport;
					node.time = takeoffTime;
					aircraft.nodeMapArray[departureAirport.id-1].put(takeoffTime, node);
				}
				
				node.flowoutConnectingArcList.add(flightArc);
				flightArc.fromNode = node;
				
				node = aircraft.nodeMapArray[arrivalAirport.id-1].get(readyTime);
				if(node == null){
					node = new Node();
					node.airport = arrivalAirport;
					node.time = readyTime;
					aircraft.nodeMapArray[arrivalAirport.id-1].put(readyTime, node);
				
				}
				
				node.flowinConnectingArcList.add(flightArc);
				flightArc.toNode = node;
			}
			
			//生成source和sink点
			Node sourceNode = new Node();
			aircraft.sourceNode = sourceNode;
			
			Node sinkNode = new Node();
			aircraft.sinkNode = sinkNode;
					
			//3. sort nodes of each airport
			
			for(int i=0;i<airportList.size();i++){
				for(Integer key:aircraft.nodeMapArray[i].keySet()){
					aircraft.nodeListArray[i].add(aircraft.nodeMapArray[i].get(key));
				}
				
				Collections.sort(aircraft.nodeListArray[i], new NodeComparator());
				
				for(int j=0;j<aircraft.nodeListArray[i].size()-1;j++){
					Node n1 = aircraft.nodeListArray[i].get(j);
					Node n2 = aircraft.nodeListArray[i].get(j+1);
					
					GroundArc groundArc = new GroundArc();
					groundArc.fromNode = n1;
					groundArc.toNode = n2;
					
					if(!groundArc.checkViolation()){
						n1.flowoutGroundArcList.add(groundArc);
						n2.flowinGroundArcList.add(groundArc);
						
						aircraft.groundArcList.add(groundArc);
					}
				}
				
			}
			
			//4. construct source and sink arcs
			//如果至少有一个对应该机场的点生成
			if(aircraft.nodeListArray[aircraft.initialLocation.id-1].size() > 0) {
				Node firstNode = aircraft.nodeListArray[aircraft.initialLocation.id-1].get(0);
				
				GroundArc arc = new GroundArc();
				arc.fromNode = sourceNode;
				arc.toNode = firstNode;
				arc.isSource = true;
				sourceNode.flowoutGroundArcList.add(arc);
				firstNode.flowinGroundArcList.add(arc);
				aircraft.groundArcList.add(arc);
			}
			
			//对停机限制的机场飞机可以刚开停靠
			if(Parameter.restrictedAirportSet.contains(aircraft.initialLocation)){
				if(aircraft.nodeListArray[aircraft.initialLocation.id-1].size() > 0){
					for(int j=1;j<aircraft.nodeListArray[aircraft.initialLocation.id-1].size();j++){
						Node n = aircraft.nodeListArray[aircraft.initialLocation.id-1].get(j);
						
						GroundArc arc = new GroundArc();
						arc.fromNode = sourceNode;
						arc.toNode = n;
						arc.isSource = true;
						sourceNode.flowoutGroundArcList.add(arc);
						n.flowinGroundArcList.add(arc);
						aircraft.groundArcList.add(arc);
					}
				}				
			}
			
			

			for(Airport airport:airportList){
				if(aircraft.nodeListArray[airport.id-1].size() > 0){
					if(Parameter.restrictedAirportSet.contains(airport)){
						for(Node lastNode:aircraft.nodeListArray[airport.id-1]){
							GroundArc arc = new GroundArc();
							arc.fromNode = lastNode;
							arc.toNode = sinkNode;
							arc.isSink = true;
							lastNode.flowoutGroundArcList.add(arc);
							sinkNode.flowinGroundArcList.add(arc);
							aircraft.groundArcList.add(arc);
							
							lastNode.airport.sinkArcList.add(arc);
						}
					}else{
						Node lastNode = aircraft.nodeListArray[airport.id-1].get(aircraft.nodeListArray[airport.id-1].size()-1);
						
						GroundArc arc = new GroundArc();
						arc.fromNode = lastNode;
						arc.toNode = sinkNode;
						arc.isSink = true;
						lastNode.flowoutGroundArcList.add(arc);
						sinkNode.flowinGroundArcList.add(arc);
						aircraft.groundArcList.add(arc);
						
						lastNode.airport.sinkArcList.add(arc);
					}					
				}
			}
			
			//4.2 生成直接从source node连接到sink node的arc
			GroundArc arc = new GroundArc();
			arc.fromNode = sourceNode;
			arc.toNode = sinkNode;
			arc.isSink = true;
			sourceNode.flowoutGroundArcList.add(arc);
			sinkNode.flowinGroundArcList.add(arc);
			aircraft.groundArcList.add(arc);
			
			aircraft.initialLocation.sinkArcList.add(arc);
			
			//5. construct turn-around arc
			arc = new GroundArc();
			arc.fromNode = sinkNode;
			arc.toNode = sourceNode;
			sinkNode.flowoutGroundArcList.add(arc);
			sourceNode.flowinGroundArcList.add(arc);
			aircraft.groundArcList.add(arc);
			aircraft.turnaroundArc = arc;
		}
		
	}
	
}
