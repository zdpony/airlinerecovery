package sghku.tianchi.IntelligentAviation.entity;

import java.util.ArrayList;
import java.util.List;

//乘客行程
public class Itinerary {
	public int id;
	public Flight flight;
	public int volume;
	
	public List<Flight> candidateFlightList = new ArrayList<>();
	
	public List<FlightSectionItinerary> flightSectionItineraryList = new ArrayList<>();
}
