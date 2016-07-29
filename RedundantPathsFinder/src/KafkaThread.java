import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.InterruptException;
import org.json.JSONException;
import org.json.JSONObject;

import com.itrsgroup.openaccess.xpath.XPathBuilder;

public class KafkaThread {
	
	public static String topics = "geneos-metadata.severity";
	public static String groupID = "KafkaRedundancyCheck"; // AWAITING FINAL KAFKA RELEASE FOR WORKINTG VERSION BEFORE CONFIGURING ANALYSIS FILTER
	public static String kafkaServer = "192.168.220.54:9094";
	
	public static int runKafka(){
		try{
		Properties props = new Properties();
		props.put("bootstrap.servers", kafkaServer);
		props.put("group.id", groupID);
		props.put("enable.auto.commit", "true");
		props.put("auto.commit.interval.ms", "1000");
		props.put("session.timeout.ms", "30000");
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("fetch.min.bytes", "50000");
		props.put("receive.buffer.bytes", "262144");
		props.put("max.partition.fetch.bytes", "2097152");
		props.put("consumer.timeout.ms", "1");
		props.put("auto.offset.reset", "earliest");
		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props); //Kafka Consumer is defined with properties
		String[] tList = topics.split(",");
		consumer.subscribe(Arrays.asList(tList)); //Topics are assigned to the consumer so the brokers are aware of what data to provide
		while (true) {
			Thread.sleep(1000);
			ConsumerRecords<String, String> records = consumer.poll(1000); //Data is pulled from the Kafka brokers at this point.
			Map<String, Integer> updates = new HashMap<String, Integer>();
			for (ConsumerRecord<String, String> record : records) {
				try {
					JSONObject temp = new JSONObject(record.value()); //THERE ARE FOUR JSON OBJECTS IN ONE KAFKA MESSAGE!!!!
					JSONObject data = temp.getJSONObject("data");
					ArrayList<String> inherantPaths = new ArrayList<String>();
					if(data.has("name"))
					{
						inherantPaths = processTableMessage(record, data);
					}
					if(data.has("severity"))
					{
						inherantPaths = processSeverityMessage(record, data);
					}
					for(String path : inherantPaths)
					{
						ArrayList<String> test = MainAutoCheck.allPaths;
						if(MainAutoCheck.allPaths.contains(path))
						{
							System.out.println("HIT : " + path);
							if(!updates.containsKey(path)) // If there is no entry add it, in either case increment the hits int his update instance
								updates.put(path, 0);
							updates.put(path, updates.get(path) + 1);
						}
					}
					//System.out.println(record.toString()); //Default print of all throughput data, data is in JSON format.
				} catch (Exception e) {
					System.out.println(record.toString());
					e.printStackTrace();
				}
			}
			
			DBHandler.updateHits(updates);
		}}catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.out.println("Internal interrupt HERE!!!!");
			return 0;
		}
		
	}
	
	private static ArrayList<String> processSeverityMessage(ConsumerRecord<String, String> record, JSONObject data) throws JSONException, ParseException, InterruptException {
		ArrayList<String> inherantPaths = new ArrayList<String>();
		ArrayList<String> sections = new ArrayList<String>();
		JSONObject target = data.getJSONObject("target");
		int params = 0;
		String[] tParams = { "gateway", "probe", "managedEntity", "dataview" };
		if (target.has("gateway")) {
			sections.add(target.getString("gateway"));
			params++;
		}
		if (target.has("probe")) {
			sections.add(target.getString("probe"));
			params++;
		}
		if (target.has("managedEntity")) {
			sections.add(target.getString("managedEntity"));
			params++;
		}
		if (target.has("sampler")) {
			sections.add(target.getString("sampler"));
			params++;
		}
		if (target.has("type")) {
			sections.add(target.getString("type"));
			params++;
		}
		if (target.has("dataview")) {
			sections.add(target.getString("dataview"));
			params++;
		}
		if (target.has("row")) {
			sections.add(target.getString("row"));
			params++;
		}
		if (target.has("column")) {
			sections.add(target.getString("column"));
			params++;
		}
		if (params == 1)
			inherantPaths.add(XPathBuilder.xpath().gateway(sections.get(0)).get());
		if (params == 2)
			inherantPaths.add(XPathBuilder.xpath().gateway(sections.get(0)).probe(sections.get(1)).get());
		if (params == 3)
			inherantPaths.add(
					XPathBuilder.xpath().gateway(sections.get(0)).probe(sections.get(1)).entity(sections.get(2)).get());
		if (params == 4)
			inherantPaths.add(XPathBuilder.xpath().gateway(sections.get(0)).probe(sections.get(1))
					.entity(sections.get(2)).sampler(sections.get(3), sections.get(4)).get());
		if (params == 6)
			inherantPaths.add(XPathBuilder.xpath().gateway(sections.get(0)).probe(sections.get(1))
					.entity(sections.get(2)).sampler(sections.get(3), sections.get(4)).view(sections.get(5)).get());
		if (params == 7)
			inherantPaths.add(XPathBuilder.xpath().gateway(sections.get(0)).probe(sections.get(1))
					.entity(sections.get(2)).sampler(sections.get(3), sections.get(4)).view(sections.get(5))
					.row(sections.get(6)).get());
		if (params == 8)
			inherantPaths.add(XPathBuilder.xpath().gateway(sections.get(0)).probe(sections.get(1))
					.entity(sections.get(2)).sampler(sections.get(3), sections.get(4)).view(sections.get(5))
					.row(sections.get(6)).cell(sections.get(7)).get());
	return inherantPaths;
	}
	
	private static ArrayList<String> processTableMessage(ConsumerRecord<String, String> record, JSONObject data) throws JSONException, ParseException, InterruptException {
		JSONObject target = data.getJSONObject("target");
		JSONObject row = data.getJSONObject("row");
		String[] tParams = {"gateway", "probe", "managedEntity", "dataview", "name"}; //name is the row name resulting
		ArrayList<String> sections = new ArrayList<String>();
		if(target.get("gateway") != null)
			sections.add(target.getString("gateway"));
		if(target.get("probe") != null)
			sections.add(target.getString("probe"));
		if(target.get("managedEntity") != null)
			sections.add(target.getString("managedEntity"));
		if(target.get("dataview") != null)
			sections.add(target.getString("dataview"));
		if(data.get("name") != null)
			sections.add(data.getString("name")); //Row name
		
		ArrayList<String> columns = new ArrayList<String>();
		Iterator<?> keys = row.keys();
		while(keys.hasNext()) //While there is another key to look at add it to the column list. 
		{
			columns.add((String)keys.next());
		} 
		
		ArrayList<String> inherantPaths = new ArrayList<String>();
		ArrayList<String> wilds = new ArrayList<String>();
		wilds.add(XPathBuilder.xpath().gateway(sections.get(0)).probe(sections.get(1)).entity(sections.get(2)).view(sections.get(3)).get());
		wilds.add(XPathBuilder.xpath().gateway(sections.get(0)).probe(sections.get(1)).entity(sections.get(2)).get());
		wilds.add(XPathBuilder.xpath().gateway(sections.get(0)).probe(sections.get(1)).get());
		wilds.add(XPathBuilder.xpath().gateway(sections.get(0)).get());

		for(String wild : wilds)
		{
			if(!inherantPaths.contains(wild))
				inherantPaths.add(XPathBuilder.xpath().gateway(sections.get(0)).probe(sections.get(1)).entity(sections.get(2)).view(sections.get(3)).get());
		}
		for(String col : columns)
		{
			inherantPaths.add(XPathBuilder.xpath().gateway(sections.get(0)).probe(sections.get(1)).entity(sections.get(2)).view(sections.get(3)).row(sections.get(4)).cell(col).get());
		}
		
		// Cycle through the column results in the row object and then create xpaths for each resulting data item that result as part of the table information 
			 
			// THIS LEAVES A HOLE FOR THE ME AND DATAVIEW AND PROBE XPATHS IN THE REGISTER???????????
		// code to check the parameters and then check against catallogued xpaths for result and if necessary increase in markers.
		return inherantPaths;
	}
}
