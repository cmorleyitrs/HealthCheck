package objects;
import java.util.ArrayList;

import handlers.DBHandler;

/*
* Create by: 	Connor Morley
* Date: 		July 2016
* Title: 		HealthChecker
* Version:		0.9
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
