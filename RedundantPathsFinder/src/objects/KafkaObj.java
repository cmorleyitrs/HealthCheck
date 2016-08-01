package objects;
import java.util.ArrayList;

import handlers.DBHandler;

/*
* Create by: 	Connor Morley
* Date: 		July 2016
* Title: 		HealthChecker
* Version:		0.9
* Description:	Object is used in conjunction with the kafka thread in order to store the xpath targets that are to be monitored.
* 				As the external data of a thread must be final, the use of this object allows for easy disposal and recreation 
* 				when a new thread is created/required.
*/

public class KafkaObj {
	
	private final ArrayList<String> xpaths;
	
	public KafkaObj(){
		this.xpaths = new ArrayList<String>(getX());
	}
	
	public ArrayList<String> getXpaths(){
		return xpaths;
	}

	private ArrayList<String> getX(){
		ArrayList<String> res = new ArrayList<String>(DBHandler.getCurrentXpaths());
		return res;
	}
	
}
