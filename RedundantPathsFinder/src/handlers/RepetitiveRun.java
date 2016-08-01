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
* Description:	Adapted from the original, the class no longer uses OA and instead implements a kafka thread in response to
* 				detected alteration in the gateway xml resulting in a new or removed target xpath. Upon detection the running thread
* 				is terminated, the associated kafka object recreated with the new targets and finally a new monitoring thread is started.
*/

public class RepetitiveRun {

	static int queriedPaths;
	static String includesInfo;
	static boolean noErrorCallback = true;
	static Future kafThread;
	public static ExecutorService executor = Executors.newFixedThreadPool(200);
	public static KafkaObj current;

	public static void run() {
		queriedPaths = 0;
		MainAutoCheck.log.info("Starting a new run...");
		try {
			FileReaderWriter.writeOutputToFile(executePaths());
		} catch (Exception e) {
			MainAutoCheck.log.error("Something is wrong!");
		}
		MainAutoCheck.log.info("Run finished. " + queriedPaths + " XPaths have been queried.");
	}

	private static String executePaths() {
		MainAutoCheck.log.info("Reading setup file: " + MainAutoCheck.gwXMLpath);
		FileReaderWriter.readSetupFile(MainAutoCheck.gwXMLpath);
		SourceFile.findXpaths(MainAutoCheck.gatewaySetupFile);
		includesInfo = GatewaySetupFile.findIncludeFiles(MainAutoCheck.gatewaySetupFile);
		IncludeFile.findXpathsInIncludes();
		if (MainAutoCheck.changeDetected == true || MainAutoCheck.initialRun == true) {
			recreateXpaths();
			if (MainAutoCheck.initialRun == true) {
				Callable<Long> worker = new executeQueries();
				Future<Long> thread = executor.submit(worker);
				kafThread = thread;
			} else {
				kafThread.cancel(true);
				Callable<Long> worker = new executeQueries();
				Future<Long> thread = executor.submit(worker);
				kafThread = thread;
			}
		}
		if (MainAutoCheck.initialRun == true)
			MainAutoCheck.initialRun = false;
		String outputString = FileReaderWriter.setupHeadlinesForOutput(includesInfo);
		MainAutoCheck.log.debug(outputString);
		return outputString;
	}

	public static void recreateXpaths() {
		if (current != null)
			current = null;
		current = new KafkaObj();
		;
	}

	public static class executeQueries implements Callable<Long> {
		
		public Long call() {
			Long x = 1l;
			do {
				int check = callThread();
				if (check == 0)
					break;
			} while (1 == 1);
			System.out.println("Thread should termiante here due to return!!!!!");
			return x;
		}
		
		public static int callThread() throws InterruptException {
			return KafkaThread.runKafka();
		}
	}

	static void setNoErrorCallback(boolean b) {
		noErrorCallback = b;
	}

}
