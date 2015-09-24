package ar.uba.fi.taller3.tp2.orderreceiver;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import ar.uba.fi.taller3.tp2.Log;

/**
 * Process for receiving orders and assigning the id. It's the entry point of the app.
 *
 */
public class OrderReceiver {

	private static final String ORDER_RECEIVERS = "order_receivers";
	
	private int numberOfOrderReceivers;
	private ExecutorService executor;
	private List<OrderReceiverConsumer> orderReceivers =  new ArrayList<OrderReceiverConsumer>();
	
	public static void main(String[] args) throws IOException, TimeoutException {
		OrderReceiver orderReceiver = new OrderReceiver();
		orderReceiver.loadProperties();
		orderReceiver.startThreads();
		Scanner scanner = new Scanner(System.in);
		String line;
		boolean finish = false;
		while(!finish){
			line = scanner.nextLine();
			if(line.equals("exit")){
				finish= true;
			}
		}
		Log.log("Finishing..");
		orderReceiver.finish();
		Log.log("Finished");
		scanner.close();
	}
	
	/**
	 * Load properties from config.properties file
	 */
	private void loadProperties() {
		Properties properties = new Properties();
		FileInputStream in;
		try {
			in = new FileInputStream("./config.properties");
			properties.load(in);
			in.close();
		} catch (FileNotFoundException e1) {
			Log.log("Could not load properties.");
			e1.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			Log.log("Could not load properties.");
			return;
		}

		numberOfOrderReceivers = Integer.parseInt(properties.getProperty(ORDER_RECEIVERS));
	}
	
	private void startThreads() throws TimeoutException, IOException {
		executor = Executors.newSingleThreadExecutor();
		for (int i = 0; i < numberOfOrderReceivers; i++) {
			OrderReceiverConsumer consumer =  new OrderReceiverConsumer();
			executor.execute(consumer);
			orderReceivers.add(consumer);
		}
	}
	
	private void finish(){
		executor.shutdownNow();
		for(OrderReceiverConsumer consumer : orderReceivers){
			consumer.shutdown();
		}
	}

}
