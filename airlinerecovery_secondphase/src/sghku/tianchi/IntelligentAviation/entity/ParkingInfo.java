package sghku.tianchi.IntelligentAviation.entity;

import java.util.ArrayList;
import java.util.List;

public class ParkingInfo {
	/*****************zd******************/
	public int id;
	public Airport airport;
	public int startTime;
	public int endTime;
	public int parkingLimit;
	
	public List<FlightArc> overlappedFlightArcList = new ArrayList<>();
	public List<GroundArc> overlappedGroundArcList = new ArrayList<>();
}
