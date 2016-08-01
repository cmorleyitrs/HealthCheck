package handlers;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import objects.IncludeFile;
import objects.Path;

/*
* Create by: 	Connor Morley
* Date: 		July 2016
* Title: 		HealthChecker
* Version:		0.9
* Description:	Slight adaptation that includes a cleanup of the db when change is detected and the incremenation of the
* 				cycle instance for reporting purposes. 
*/

public class SourceFile {

	protected ArrayList<Path> paths;
	protected Document doc;

	public Document getDoc() {
		return this.doc;
	}

	public void addPath(Path path) {
		this.paths.add(path);
	}

	public ArrayList<Path> getPaths() {
		return this.paths;
	}

	static void findXpaths(SourceFile g) { // This is where checks are made and then fed to Patgh to be added to lists!

		try {

			NodeList nodeLstXpath = g.getDoc().getElementsByTagName("xpath");
			NodeList nodeLstTarget = g.getDoc().getElementsByTagName("target");
			NodeList nodeLstPathAlias = g.getDoc().getElementsByTagName(
					"pathAlias");
			NodeList nodeLstContext = g.getDoc()
					.getElementsByTagName("context");

			for (int i = 0; i < nodeLstXpath.getLength(); i++) {
				Path.addPath(nodeLstXpath.item(i), g);
			}

			for (int i = 0; i < nodeLstTarget.getLength(); i++) {
				Path.addPath(nodeLstTarget.item(i), g);
			}

			for (int i = 0; i < nodeLstPathAlias.getLength(); i++) {
				Path.addPath(nodeLstPathAlias.item(i), g);
			}

			for (int i = 0; i < nodeLstContext.getLength(); i++) {
				Path.addPath(nodeLstContext.item(i), g);
			}
			int removed = DBHandler.checkRemovals();
			if(removed != 0)
			{
			DBHandler.cleanUp();
			MainAutoCheck.changeDetected = true;
			}
			
			MainAutoCheck.runInstance++;
			
		} catch (Exception e) {
			MainAutoCheck.log.error("Something's wrong, couldn't find XPaths...");
		}
	}

	public static String findParentsAndDisabled(Node node, String xpath) {
		if (node.getParentNode() == null) {
			return xpath;
		}

		if (!((Element) node).getAttribute("name").isEmpty())
			xpath = "/" + node.getNodeName() + "\""
					+ ((Element) node).getAttribute("name") + "\"" + xpath;
		else
			xpath = "/" + node.getNodeName()
					+ ((Element) node).getAttribute("name") + xpath;

		if (((Element) node).getAttribute("disabled").contains("true"))
			xpath = xpath + "-disabled\"true\"";

		node = node.getParentNode();
		return findParentsAndDisabled(node, xpath);
	}

	public static void findXpathsInIncludes() {
		for (IncludeFile file : MainAutoCheck.gatewaySetupFile.getIncludes()) {
			findXpaths(file); // find xpaths in each include file
			MainAutoCheck.log.info("Path instances in " + file.getName()
					+ " currently found: " + file.getPaths().size());
		}
	}

}
