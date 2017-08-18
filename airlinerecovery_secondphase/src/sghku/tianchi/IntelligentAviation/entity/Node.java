package sghku.tianchi.IntelligentAviation.entity;

import java.util.ArrayList;
import java.util.List;

//网络模型中的点
public class Node {
	public int id;
	//时空网络流模型中每一个点对应的机场和时间
	public Airport airport;   
	public int time;   
	//该点属于的飞机
	public Aircraft aircraft;
	
	//从该点流入和流出的flight arc，connecting arc 和 ground arc
	public List<FlightArc> flowinFlightArcList = new ArrayList<>();
	public List<FlightArc> flowoutFlightArcList = new ArrayList<>();
	
	public List<ConnectingArc> flowinConnectingArcList = new ArrayList<>();
	public List<ConnectingArc> flowoutConnectingArcList = new ArrayList<>();
	
	public List<GroundArc> flowinGroundArcList = new ArrayList<>();
	public List<GroundArc> flowoutGroundArcList = new ArrayList<>();
	
	//该点是否是source或者sink
	public boolean isSource = false;
	public boolean isSink = false;
} 
