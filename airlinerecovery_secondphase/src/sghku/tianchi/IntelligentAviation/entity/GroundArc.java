package sghku.tianchi.IntelligentAviation.entity;

//地面arc
public class GroundArc {
	public int id;
	//头尾点
	public Node fromNode;  
	public Node toNode;   
	
	//标记该地面arc是否是source或者sink
	public boolean isSource = false;
	public boolean isSink = false;
	
	public int flow;
	public double fractionalFlow;
	
	//检测该地面arc是否违反约束
	public boolean checkViolation(){
		boolean vio = false;
		//判断停机时间是否在停机时间段内
		for(Failure scene:fromNode.airport.failureList){
			if(scene.isStopInScene(0, 0, fromNode.airport, fromNode.time, toNode.time)){
				vio = true;
				break;
			}
		}
		
		return vio;
	}
}
