package ar.uba.fi.taller3.tp2.stockmanager;

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
import ar.uba.fi.taller3.tp2.domain.StockRepository;

/**
 * Process for managing the stock of products.
 * @author pablo
 *
 */
public class StockManager {

	private static final String STOCK_ADDERS = "stock_adders";
	private static final String STOCK_REDUCERS = "stock_reducers";

	private int numberOfStockAdders = 1;
	private int numberOfStockReducers = 1;

	private StockRepository stock;
	private ExecutorService executor;
	List<AddStockConsumer> addStockConsumers = new ArrayList<AddStockConsumer>();
	List<ReduceStockConsumer> reduceStockConsumers = new ArrayList<ReduceStockConsumer>();

	public static void main(String[] args) throws TimeoutException, IOException {
		StockManager stockManager = new StockManager();
		stockManager.loadProperties();
		stockManager.startThreads();
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
		stockManager.finish();
		Log.log("Finished");
		scanner.close();
	}

	private StockManager() throws TimeoutException, IOException {
		stock = new StockRepository("stock.json");
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

		numberOfStockAdders = Integer.parseInt(properties.getProperty(STOCK_ADDERS));
		numberOfStockReducers = Integer.parseInt(properties.getProperty(STOCK_REDUCERS));
	}

	private void startThreads() throws TimeoutException, IOException {
		executor = Executors.newSingleThreadExecutor();
		for (int i = 0; i < numberOfStockAdders; i++) {
			AddStockConsumer consumer =  new AddStockConsumer(stock);
			executor.execute(consumer);
			addStockConsumers.add(consumer);
		}
		for (int i = 0; i < numberOfStockReducers; i++) {
			ReduceStockConsumer consumer = new ReduceStockConsumer(stock);
			executor.execute(consumer);
			reduceStockConsumers.add(consumer);
		}
	}
	
	private void finish(){
		executor.shutdownNow();
		for(AddStockConsumer consumer : addStockConsumers){
			consumer.shutdown();
		}
		for(ReduceStockConsumer consumer : reduceStockConsumers){
			consumer.shutdown();
		}
	}

}
