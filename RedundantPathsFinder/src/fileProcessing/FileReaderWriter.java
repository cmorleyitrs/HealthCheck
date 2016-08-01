package fileProcessing;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import handlers.MainAutoCheck;
import objects.GatewaySetupFile;

/*
* Create by: 	Connor Morley
* Date: 		July 2016
* Title: 		HealthChecker
* Version:		0.9
* Description:	Class used to read the gateway xml files and ready for examination. Also includes output message generation
* 				which is no longer user. Consider removing.
*/

public class FileReaderWriter {

	public static void readSetupFile(String gwSetupFile) {

		try {
			Document doc = readFile(gwSetupFile);
			MainAutoCheck.log.debug("Creating a new gatewaySetupFile object");
			MainAutoCheck.gatewaySetupFile = new GatewaySetupFile(doc); // create a new
																// gatewaySetupFile
																// object every
																// new Run

		} catch (Exception e) {
			MainAutoCheck.log.error("Couldn't find gateway setup file.");
		}
	}

	static Document readFile(String s) throws ParserConfigurationException,
			SAXException, IOException {
		File file = new File(s);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(file);
		doc.getDocumentElement().normalize();
		return doc;
	}

	public static void writeOutputToFile(String outputString)
			throws IOException {
		MainAutoCheck.log.info("Writing to file: output.csv");
		BufferedWriter writer = new BufferedWriter(new FileWriter("output.csv"));
		writer.write(outputString);
		writer.close();
	}

	public static String setupHeadlinesForOutput(String includesInfo) {
		String outputString = "";
		outputString = outputString
				+ "row,currentMatches,maxMatchesSoFar,Location,XPath";
		outputString = outputString + "\n<!>Last run completed on:,"
				+ MainAutoCheck.dateFormat.format(new Date());
		outputString = outputString + "\n<!>First run completed on:,"
				+ MainAutoCheck.dateFormat.format(MainAutoCheck.firstRun);
		outputString = outputString + "\n<!>Final run scheduled for:,"
				+ MainAutoCheck.dateFormat.format(MainAutoCheck.finalRun);
		outputString = outputString + "\n<!>Include files found/configured:,"
				+ includesInfo;
		return outputString;
	}

}
