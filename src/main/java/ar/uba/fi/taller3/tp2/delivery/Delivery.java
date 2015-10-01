package ar.uba.fi.taller3.tp2.delivery;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import ar.uba.fi.taller3.tp2.Log;

/**
 * Process to simulate the delivery of orders.
 *
 * @param args[0] The amount of orders to deliver in every step.
 * @param args[1] The time in milliseconds to sleep between steps. 
 */
public class Delivery {

	public static void main(String[] args) throws IOException, TimeoutException {
		int step = Integer.parseInt(args[0]);
		long sleepTime = Long.parseLong(args[1]);
		DeliveryConsumer consumer =  new DeliveryConsumer(step, sleepTime);
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.execute(consumer);
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
		executor.shutdownNow();
		consumer.shutdown();
		Log.log("Finished");
		scanner.close();
	}

}
