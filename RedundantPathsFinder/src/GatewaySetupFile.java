import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class GatewaySetupFile extends SourceFile {

	private ArrayList<IncludeFile> includeFiles;

	private static FileOutputStream fos;

	public GatewaySetupFile(Document doc){
		this.doc = doc;
		this.includeFiles = new ArrayList<IncludeFile>(); // create a new ArrayList every new Run
		this.paths = new ArrayList<Path>();
	}

	public void addInclude(IncludeFile file){
		this.includeFiles.add(file);
	}

	public ArrayList<IncludeFile> getIncludes(){
		return this.includeFiles;
	}

	static Document readFile(String s) throws ParserConfigurationException, SAXException, IOException{
		MainAutoCheck.log.info("Reading file "+s);
		File file = new File(s);	  
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(file);
		doc.getDocumentElement().normalize();
		return doc;
	}

	static String findIncludeFiles(GatewaySetupFile g){

		int includesFoundInSetup = 0;
		int includesReallyFound = 0;

		try {
			NodeList locationLst = g.getDoc().getElementsByTagName("location"); // not sure if <location> is used only for includes

			for(int i=0; i<locationLst.getLength(); i++){
				Node node = locationLst.item(i);
				String nodeText = node.getTextContent();
				Node parentNode = node.getParentNode();

				if(parentNode.getNodeName().equals("include")){ // if it's an include location

					if(((Element) parentNode).getAttribute("disabled").contains("true")){
						MainAutoCheck.log.debug("Ignoring include "+nodeText+" because it's been disabled");
					} else {

						includesFoundInSetup++;
						String notify = "New include found: "+nodeText;
						Document doc;
						try {  
							// if it's a url
							if (nodeText.startsWith("http://")
									|| nodeText.startsWith("https://")
									|| nodeText.startsWith("file://")) {
								notify = "New URL include found: " + nodeText;
								URL url = new URL(nodeText);
								ReadableByteChannel rbc = Channels
										.newChannel(url.openStream());
								nodeText = nodeText
										.replace("https://", "URLinclude--")
										.replaceAll("http://", "URLinclude--")
										.replaceAll("file://", "URLinclude--")
										.replaceAll("/", "--");
								fos = new FileOutputStream(nodeText);
								fos.getChannel().transferFrom(rbc, 0,
										Long.MAX_VALUE);
								MainAutoCheck.log.debug("Trying to get " + nodeText);
								doc = readFile(nodeText);
							} else if (!nodeText.startsWith("/")){	 // if it is a relative path (maybe the name begins with letter or number?)
								MainAutoCheck.log.debug("Trying to get "+MainAutoCheck.gwXMLpath.substring(0, MainAutoCheck.gwXMLpath.length()-17)+nodeText);  // gateway.setup.xml is 17 chars
								doc = readFile(MainAutoCheck.gwXMLpath.substring(0, MainAutoCheck.gwXMLpath.length()-17)+nodeText);
							} else { // if it is an absolute path
								MainAutoCheck.log.debug("Trying to get "+nodeText);
								doc = readFile(nodeText); 
							}

							MainAutoCheck.log.info(notify);
							MainAutoCheck.gatewaySetupFile.addInclude(new IncludeFile(node, doc)); // add the found include file to the gateway.setup.xml object

						} catch (Exception e) {
							MainAutoCheck.log.error("Couldn't get include file "+nodeText);
						}  
					}
				}
			}
			includesReallyFound = MainAutoCheck.gatewaySetupFile.getIncludes().size();

			if(includesReallyFound!=includesFoundInSetup)
				MainAutoCheck.log.warn("Include Files found: "+includesReallyFound+" out of "+includesFoundInSetup+" configured");
			else
				MainAutoCheck.log.info("Include Files found: "+includesReallyFound+" out of "+includesFoundInSetup+" configured");
		} catch(Exception e) {
			MainAutoCheck.log.error("Couldn't find include files");
		}
		return (includesReallyFound+"/"+includesFoundInSetup);
	}

}
