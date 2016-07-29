package handlers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.InterruptException;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.w3c.dom.Node;

import com.itrsgroup.openaccess.Callback;
import com.itrsgroup.openaccess.Closable;
import com.itrsgroup.openaccess.Connection;
import com.itrsgroup.openaccess.ErrorCallback;
import com.itrsgroup.openaccess.OpenAccess;
import com.itrsgroup.openaccess.dataset.DataSetChange;
import com.itrsgroup.openaccess.dataset.DataSetQuery;

import fileProcessing.FileReaderWriter;
import objects.GatewaySetupFile;
import objects.IncludeFile;
import objects.KafkaObj;
import threads.KafkaThread;

import static java.util.concurrent.TimeUnit.SECONDS;

/*
* Create by: 	Connor Morley
* Date: 		July 2016
* Title: 		HealthChecker
* Version:		0.9
*/

public class RepetitiveRun{

  // static ArrayList<Path> currentUniquePaths;
	static int queriedPaths;
	static String includesInfo;
	static boolean noErrorCallback = true;
	static Future kafThread;
	public static ExecutorService executor = Executors.newFixedThreadPool(200);
	public static KafkaObj current;

	//public void execute(JobExecutionContext arg0) throws JobExecutionException {

	public static void run(){
		queriedPaths = 0;

		MainAutoCheck.log.info("Starting a new run...");
		// currentUniquePaths = new ArrayList<Path>(); // like Main's allPaths but a new ArrayList is created every new Run

		try {
			FileReaderWriter.writeOutputToFile(executePaths());
		} catch (Exception e){
			MainAutoCheck.log.error("Something is wrong!");
		}

		// MainAutoCheck.log.info("Run finished. "+queriedPaths+"/"+currentUniquePaths.size()+" XPaths have been queried.");
		MainAutoCheck.log.info("Run finished. "+queriedPaths+" XPaths have been queried.");
	}

	private static String executePaths() {

		
		MainAutoCheck.log.info("Reading setup file: "+MainAutoCheck.gwXMLpath);
		FileReaderWriter.readSetupFile(MainAutoCheck.gwXMLpath);
		SourceFile.findXpaths(MainAutoCheck.gatewaySetupFile); // find xpaths in gateway.setup.xml

		includesInfo = GatewaySetupFile.findIncludeFiles(MainAutoCheck.gatewaySetupFile);
		IncludeFile.findXpathsInIncludes();  

		// MainAutoCheck.log.info("Total number of absolute paths found: "+currentUniquePaths.size());
		if(MainAutoCheck.changeDetected == true || MainAutoCheck.initialRun == true)
		{
			recreateXpaths();
			if(MainAutoCheck.initialRun == true)
			{
				Callable<Long> worker = new executeQueries();
				Future<Long> thread = executor.submit(worker);
				kafThread = thread;
			}
			else{
				kafThread.cancel(true);
				Callable<Long> worker = new executeQueries();
				Future<Long> thread = executor.submit(worker);
				kafThread = thread;
			}
		}
		
		if (MainAutoCheck.initialRun == true)
			MainAutoCheck.initialRun = false;

		//Collections.sort(currentUniquePaths, new CustomComparator());

		String outputString = FileReaderWriter.setupHeadlinesForOutput(includesInfo);

		// outputString = matchPaths(outputString);

		MainAutoCheck.log.debug(outputString);
		return outputString;
	}
	
	public static void recreateXpaths(){
		if(current != null)
			current = null;
		current = new KafkaObj();;
	}

/*	private String matchPaths(String outputString) {
		int rowNum = 0;
		for(Path p : currentUniquePaths){ // need a better data structure to avoid all that looping?
			for(Path pAll : MainAutoCheck.allPaths){
				if(pAll.equals(p)&&pAll.isValid()){
					// add the results to outputString
					outputString = outputString + "\n"+rowNum+","+pAll.getCurrentMatches()+","+pAll.getMaxMatches()+","+pAll.getLocationToolkitOutput()+","+pAll.getXPathToolkitOutput();
					rowNum++;
				}
			}
		}
		return outputString;
	}*/

	public static class executeQueries implements Callable<Long> { // Removed Global Connection CM
		
		public Long call(){
			Long x = 1l;
			do{
				int check = callThread();
				if(check == 0)
					break;
			}
			while(1 == 1);
			// Thread.currentThread().interrupt();
			System.out.println("Thread should termiante here due to return!!!!!");
			return x;
		}
		
		public static int callThread() throws InterruptException{
			return KafkaThread.runKafka();
		}
	}

	static void setNoErrorCallback(boolean b){
		noErrorCallback = b;
	}

}
