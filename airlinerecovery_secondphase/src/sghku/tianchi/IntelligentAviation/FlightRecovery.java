package sghku.tianchi.IntelligentAviation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sghku.tianchi.IntelligentAviation.common.Parameter;
import sghku.tianchi.IntelligentAviation.entity.Aircraft;
import sghku.tianchi.IntelligentAviation.entity.Airport;
import sghku.tianchi.IntelligentAviation.entity.ConnectingFlightpair;
import sghku.tianchi.IntelligentAviation.entity.Connection;
import sghku.tianchi.IntelligentAviation.entity.Flight;
import sghku.tianchi.IntelligentAviation.entity.FlightSection;
import sghku.tianchi.IntelligentAviation.entity.FlightSectionItinerary;
import sghku.tianchi.IntelligentAviation.entity.Itinerary;
import sghku.tianchi.IntelligentAviation.entity.Leg;
import sghku.tianchi.IntelligentAviation.entity.Scenario;

public class FlightRecovery {

	public static void main(String[] args) {

		// Scenario scenario = new Scenario(Parameter.EXCEL_FILENAME,
		// Parameter.FLYTIME_FILENAME);
		Scenario scenario = new Scenario(Parameter.EXCEL_FILENAME);
		
		/*int n = 0;
		for(Flight f:scenario.flightList) {
			if(f.isIncludedInTimeWindow) {
				for(FlightSection fs:f.flightSectionList) {
					System.out.println(fs.flightSectionItineraryList.size());
					n += fs.flightSectionItineraryList.size();
				}
			}
		}
		System.out.println("n:"+n);*/

		
	}

}
