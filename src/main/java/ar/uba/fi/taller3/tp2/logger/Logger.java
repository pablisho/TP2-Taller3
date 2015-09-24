package ar.uba.fi.taller3.tp2.logger;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import ar.uba.fi.taller3.tp2.Log;

/**
 * Logger process.
 */
public class Logger {
	
	public static void main(String[] args) throws IOException, TimeoutException {
		LoggerConsumer logger = new LoggerConsumer();
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.execute(logger);
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
		logger.shutdown();
		Log.log("Finished");
		scanner.close();
	}
	
}
