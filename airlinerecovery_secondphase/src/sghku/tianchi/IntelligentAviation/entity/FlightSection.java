package sghku.tianchi.IntelligentAviation.entity;

import java.util.ArrayList;
import java.util.List;

public class FlightSection {
	public int id;
	public Flight flight;
	public int startTime;
	public int endTime;
	
	public List<FlightSectionItinerary> flightSectionItineraryList = new ArrayList<>();
	public List<FlightArc> flightArcList = new ArrayList<>();
	//public List<ConnectingArc> connectingArcList = new ArrayList<>();
}
