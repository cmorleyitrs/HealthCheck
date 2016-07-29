package handlers;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import threads.KafkaThread;

import java.util.Properties;


/*
* Create by: 	Connor Morley
* Date: 		July 2016
* Title: 		HealthChecker
* Version:		0.9
*/

public class SetupFileHandler {
	
	public static Map<String, String> setting = new HashMap<String,String>();

	public static void readSettingsFile() throws InterruptedException {
		Properties prop = new Properties();
		InputStream input = null;
		int varCount = 3; // Default number of entries without maintenance roll back

		try {
			String file = "";
			if (System.getProperty("os.name").contains("Windows"))
				file = ".\\settings.properties";
			if (System.getProperty("os.name").contains("Linux"))
				file = "./settings.properties";
			input = new FileInputStream(file);

			prop.load(input);

			setting.put("kafkaAddress", prop.getProperty("kafka_host"));
			setting.put("kafkaPort", prop.getProperty("kafka_port"));
			setting.put("gatewayXmlLocation", prop.getProperty("xml_path"));

			if (setting.size() != varCount || setting.containsValue(null)) {
				System.exit(0);
			}
			configureSetting();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
	
	public static void configureSetting() throws InterruptedException
    {
		KafkaThread.kafkaServer = setting.get("kafkaAddress") + ":" + setting.get("kafkaPort");
		MainAutoCheck.gwXMLpath = setting.get("gatewayXmlLocation");
    }
}
