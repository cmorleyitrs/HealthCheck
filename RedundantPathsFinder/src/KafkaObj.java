import java.util.ArrayList;

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
