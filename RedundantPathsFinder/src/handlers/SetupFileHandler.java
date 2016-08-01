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
* Description:	By reading an associated properties file the program is able to ascertain the kafka broker location and port
* 				as well as the location of the gateway setup xml that is the target of the monitoring.
*/

public class SetupFileHandler {
	
	public static Map<String, String> setting = new HashMap<String,String>();

	public static void readSettingsFile() throws InterruptedException {
		Properties prop = new Properties();
		InputStream input = null;
		int varCount = 8; // Default number of entries without maintenance roll back

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
			setting.put("dbHost", prop.getProperty("db_host"));
			setting.put("dbPort", prop.getProperty("db_port"));
			setting.put("dbName", prop.getProperty("db_name"));
			setting.put("dbUsername", prop.getProperty("db_username"));
			setting.put("dbPassword", prop.getProperty("db_password"));

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
		DBHandler.address = "jdbc:mysql://" + setting.get("dbHost") + ":" + setting.get("dbPort") + "/" + setting.get("dbName")
		+ "?user=" + setting.get("dbUsername") + "&password=" + setting.get("dbPassword");
		//jdbc:mysql://192.168.10.128/healthtest?user=root&password=iPods123
    }
}
