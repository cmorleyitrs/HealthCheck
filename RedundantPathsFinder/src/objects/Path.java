package objects;
import java.sql.SQLException;

import org.w3c.dom.Node;

import handlers.DBHandler;
import handlers.MainAutoCheck;
import handlers.SourceFile;

/*
* Create by: 	Connor Morley
* Date: 		July 2016
* Title: 		HealthChecker
* Version:		0.9
*/

public class Path {

	private Node xpathNode;

	private int maxMatchesSoFar = 0;
	private int currentMatches = 0;

	private boolean isValid;

	private SourceFile sourceFile = null;

	private String locationInFile;

	private String uniqueIdentifier; // location (may have include) + path for

	// the Toolkit view - should not contain
	// any commas

	public Path(Node xpathNode, SourceFile sourceFile, String locationInFile) {
		this.xpathNode = xpathNode;
		this.sourceFile = sourceFile;
		this.locationInFile = locationInFile;

		this.uniqueIdentifier = getLocation() + " XPath: "
				+ getNode().getTextContent();
	}

	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (!(o instanceof Path))
			return false;

		Path other = (Path) o;
		if (!this.uniqueIdentifier.equals(other.uniqueIdentifier))
			return false;

		return true;
	}

	public int hashCode() {
		return (int) this.uniqueIdentifier.hashCode();
	}

	public String getUniqueIdentifier() {
		return this.uniqueIdentifier;
	}

	public String getUniqueIdentifierToolkitOutput() {
		return this.uniqueIdentifier.replace(",", "\\,");
	}

	public Node getNode() {
		return this.xpathNode;
	}

	public String getLocation() {
		if (sourceFile instanceof GatewaySetupFile)
			return "Location: " + this.locationInFile;
		else
			return "IncludeFile: " + ((IncludeFile) sourceFile).getName()
					+ " GSELocation: "
					+ ((IncludeFile) sourceFile).getGSELocation()
					+ " LocationInInclude: " + this.locationInFile;
	}

	public String getLocationToolkitOutput() {
		if (sourceFile instanceof GatewaySetupFile)
			return this.locationInFile.replace(",", "\\,");
		else
			return "IncludeFile: "
					+ ((IncludeFile) sourceFile).getName().replace(",", "\\,")
					+ " GSELocation: "
					+ ((IncludeFile) sourceFile).getGSELocation().replace(",",
							"\\,") + " LocationInInclude: "
					+ this.locationInFile.replace(",", "\\,");
	}

	public String getXPathToolkitOutput() {
		return getNode().getTextContent().replace(",", "\\,");
	}

	public void addMatches(int i) {

		this.currentMatches = i;

		if (i > this.maxMatchesSoFar)
			this.maxMatchesSoFar = i;
	}

	public void addCurrentMatches(int i) {

		this.currentMatches = i;
	}

	public int getMaxMatches() {
		return this.maxMatchesSoFar;
	}

	public int getCurrentMatches() {
		return this.currentMatches;
	}

	public void setValid(boolean v) {
		isValid = v;
	}

	public boolean isValid() {
		return isValid;
	}

	
//	  old isValidPath 
//	  static boolean isValidPath(String p) { if (!p.equals("/")
//	  && !p.equals("//") && !p.endsWith("/") && !p.contains("\n") &&
//	  !p.isEmpty() && !p.matches("(.*)?\\/row(\\[[^/]*\\])?") &&
//	  !p.matches("(.*)?\\/rows(\\[[^/]*\\])?") &&
//	  !p.matches("(.*)?\\/gateway(\\[[^/]*\\])?") &&
//	  !p.matches("(.*)?\\/geneos(\\[[^/]*\\])?") &&
//	  !p.matches("(.*)?\\/headlines(\\[[^/]*\\])?")) return true; return false;
//	  }
	 

	static boolean isValidPath(String p) {

		// remove all spaces
		p = p.replaceAll("\\s+", "");

		if (!p.endsWith("/")
				&& !p.contains("\n")
				&& !p.isEmpty()
				&& !p.equals("/geneos/gateway/directory/probe/managedEntity/sampler/dataview/rows/row/cell")
				&& !p.equals("/geneos/gateway/directory/probe/managedEntity/sampler/dataview//cell")
				&& !p.matches("(.*)?\\/row(\\[[^/]*\\])?")
				&& !p.matches("(.*)?\\/rows(\\[[^/]*\\])?")
				&& !p.matches("(.*)?\\/gateway(\\[[^/]*\\])?")
				&& !p.matches("(.*)?\\/geneos(\\[[^/]*\\])?")
				&& !p.matches("(.*)?\\/headlines(\\[[^/]*\\])?")
				&& p.contains("/geneos"))
			return true;
		MainAutoCheck.log.debug("Path is invalid - " + p);
		return false;
	}

	public static void addPath(Node node, SourceFile g) throws SQLException {
		Path newPath = new Path(node, g, SourceFile.findParentsAndDisabled(
				node, ""));
		if (node.getTextContent().startsWith("/")) { // if it's an absolute path

			if (!isValidPath(node.getTextContent())) {
				MainAutoCheck.log.warn("Invalid absolute path: Location: "
						+ newPath.getLocationToolkitOutput() + ", Path: "
						+ node.getTextContent());
				return;
			}

			if (newPath.getLocationToolkitOutput().contains("disabled\"true\"")) {
				MainAutoCheck.log
						.warn("Not adding XPath"
								+ newPath.getLocationToolkitOutput() + ", "
								+ node.getTextContent()
								+ " because it's been disabled");
				return;
			}
			g.addPath(newPath);

			//if (!MainAutoCheck.allPaths.contains(newPath)) // Addition to DB is HERE!!!!!!
			String path = newPath.uniqueIdentifier.substring(newPath.uniqueIdentifier.indexOf("XPath:") + 7); //Extract the xpath
			int check = DBHandler.checkEntry(path);
			
			if(check == 1)	
				//MainAutoCheck.allPaths.add(newPath);
				DBHandler.updateEntry(path);
				if(MainAutoCheck.initialRun == true)
					MainAutoCheck.allPaths.add(path);
				
			if(check == 2)
			{
				MainAutoCheck.allPaths.add(path);
				//System.out.println("SOMETHING IS HERE:    " + newPath.getNode().getTextContent());
				MainAutoCheck.changeDetected = true;
				DBHandler.addEntry(path, newPath.getNode().getTextContent()); // TextContent is xpath?????
			}
/*			if (!RepetitiveRun.currentUniquePaths.contains(newPath))
				RepetitiveRun.currentUniquePaths.add(newPath);*/
		} else {
			// Main.log.debug("path doesn't start with \"/\" - doing nothing for now "+node.toString()+node.getTextContent());
			if (!isValidPath(node.getTextContent())) {
				MainAutoCheck.log.debug("Invalid path: Location: "
						+ newPath.getLocationToolkitOutput() + ", Path: "
						+ node.getTextContent());
				return;
			}
			// TODO
			// if it's a relative path
			// do nothing for now...
		}
	}

}
