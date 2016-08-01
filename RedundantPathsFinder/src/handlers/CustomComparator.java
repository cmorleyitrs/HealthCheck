package handlers;
import java.util.Comparator;

import objects.Path;

/*
* Create by: 	Milka
* Date: 		
* Title: 		HealthChecker
* Version:		0.9
*/

public class CustomComparator implements Comparator<Path>{

	@Override
	public int compare(Path o1, Path o2) {
		Integer i1 = new Integer(o1.getCurrentMatches());
		Integer i2 = new Integer(o2.getCurrentMatches());
		return i1.compareTo(i2);
	}

}
