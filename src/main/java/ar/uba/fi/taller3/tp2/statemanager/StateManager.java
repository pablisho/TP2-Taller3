package ar.uba.fi.taller3.tp2.statemanager;

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
import ar.uba.fi.taller3.tp2.domain.StateRepository;

/**
 * Process for managing the state of the orders.
 *
 */
public class StateManager {

	private static final String STATE_CHANGERS = "state_changers";
	private static final String STATE_CHECKERS = "state_checkers";

	private int numberOfStateChangers = 1;
	private int numberOfStateCheckers = 1;

	private StateRepository state;
	private ExecutorService executor;
	private List<ChangeStateConsumer> changeStateConsumers = new ArrayList<ChangeStateConsumer>();
	private List<CheckStateConsumer> checkStateConsumers = new ArrayList<CheckStateConsumer>();
	
	public static void main(String[] args) throws TimeoutException, IOException{
		StateManager stateManager =  new StateManager();
		stateManager.loadProperties();
		stateManager.startThreads();
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
		stateManager.finish();
		Log.log("Finished");
		scanner.close();
	}
	
	public StateManager(){
		state = new StateRepository("state");
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

		numberOfStateChangers = Integer.parseInt(properties.getProperty(STATE_CHANGERS));
		numberOfStateCheckers = Integer.parseInt(properties.getProperty(STATE_CHECKERS));
	}
	
	private void startThreads() throws TimeoutException, IOException {
		executor = Executors.newSingleThreadExecutor();
		for (int i = 0; i < numberOfStateChangers; i++) {
			ChangeStateConsumer consumer =  new ChangeStateConsumer(state);
			executor.execute(consumer);
			changeStateConsumers.add(consumer);
		}
		for (int i = 0; i < numberOfStateCheckers; i++) {
			CheckStateConsumer consumer = new CheckStateConsumer(state);
			executor.execute(consumer);
			checkStateConsumers.add(consumer);
		}
	}
	
	private void finish(){
		executor.shutdownNow();
		for(ChangeStateConsumer consumer : changeStateConsumers){
			consumer.shutdown();
		}
		for(CheckStateConsumer consumer : checkStateConsumers){
			consumer.shutdown();
		}
	}
}
