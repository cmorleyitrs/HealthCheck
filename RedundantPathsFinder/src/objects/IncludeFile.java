package objects;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import handlers.SourceFile;

/*
* Create by: 	Milka
* Date: 		
* Title: 		HealthChecker
* Version:		0.9
*/

public class IncludeFile extends SourceFile {

	private String locationInGSE;
	private Node includeNode;
		
	public IncludeFile(Node includeNode, Document doc){
		this.includeNode = includeNode;
		this.doc = doc;
		this.paths = new ArrayList<Path>(); 
		this.locationInGSE = SourceFile.findParentsAndDisabled(includeNode, "");
	}
	
	public Node getIncludeNode(){
		return this.includeNode;
	}
	
	public String getName(){
		return this.includeNode.getTextContent();
	}
	
	public String getGSELocation(){
		return this.locationInGSE;
	}
}
