package handlers;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itrsgroup.openaccess.Connection;
import com.itrsgroup.openaccess.OpenAccess;

import objects.GatewaySetupFile;

/*
* Create by: 	Connor Morley
* Date: 		July 2016
* Title: 		HealthChecker
* Version:		0.9
*/


public class MainAutoCheck {

	public static Logger log = LoggerFactory.getLogger(MainAutoCheck.class);

	//static String clusterURL;
	public static String gwXMLpath;
	//static int repeatInterval;
	//static int sleepInterval;
	
	public static boolean initialRun = true;
	public static int runInstance = 0;
	public static String gateway = "testingGateway";
	public static boolean changeDetected = false;
	
	public static String filePath = "";
	public static String fileName = "";

	//static Connection conn;

	static Calendar cal;
	public static DateFormat dateFormat;
	public static Date firstRun;

	public static Date finalRun;

	public static GatewaySetupFile gatewaySetupFile; // initialised every new run

	public final static ArrayList<String> allPaths = new ArrayList<String>(); // all unique paths (path = location+xpath) found in the setup so far (gateway.setup.file + include files) - contains paths from all previous runs since the start of the program

	public static void main(String[] args) throws InterruptedException, IOException {

		log.info("---Gateway Health Check Started---");

		//clusterURL = "geneos.cluster://192.168.220.54:2551?username=admin&password=admin";
		//gwXMLpath = ".\\gateway.setup.xml";
		//repeatInterval = 20;
		//sleepInterval = 99999;

		//conn = OpenAccess.connect(clusterURL);

		dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
		cal = Calendar.getInstance();
		firstRun = cal.getTime();
		cal.add(Calendar.SECOND, 99999);
		finalRun = cal.getTime();

		SetupFileHandler.readSettingsFile();
		filePath = gwXMLpath; // Test purposes only
		System.out.println(filePath);
		InitiateWatch();
		//QuartzScheduler.startScheduler(repeatInterval, sleepInterval, conn);

	}
	
	public static void InitiateWatch() throws InterruptedException, IOException{
		Path path = Paths.get(filePath.substring(0, filePath.lastIndexOf("\\") + 1));
		System.out.println(path);
		
		fileName = filePath.substring(filePath.lastIndexOf("\\") + 1);
		
		final WatchService watchService = FileSystems.getDefault().newWatchService();
		final WatchKey watchKey = path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
	    while (true) {
	    	if(initialRun == true)
	    		RepetitiveRun.run();
	        final WatchKey wk = watchService.take();
	        for (WatchEvent<?> event : wk.pollEvents()) {
	            final Path changed = (Path) event.context();
	            System.out.println(changed);
	            System.out.println(fileName);
	            if (changed.endsWith(fileName)) {
	                System.out.println("FILE ALTERATION DETECTED");
	                RepetitiveRun.run();
	            }
	        }
	        boolean valid = wk.reset();
	        if (!valid) {
	            System.out.println("Key has been unregistered");
	        }
	    }
		
	}

	static int convertToMilliseconds(String s) {
		int i = Integer.parseInt(s);
		i = i * 1000;
		return i;
	}
}
