package ar.uba.fi.taller3.tp2.domain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;

import ar.uba.fi.taller3.tp2.Log;

/**
 * Thread safe manager for the stock of products.
 *
 */
public class StockRepository {

	// The file to save the stock data.
	private File file;
	private RandomAccessFile writer;
	// Memory copy of the stock.
	private Map<String, Double> stockMap;
	private Gson json = new Gson();

	public StockRepository(String filename) throws FileNotFoundException {
		file = new File(filename);
		try {
			BufferedReader bfReader = new BufferedReader(new FileReader(file));
			String stock = bfReader.readLine();
			stockMap = json.fromJson(stock, HashMap.class);
			System.out.println(stockMap.toString());
			bfReader.close();
		} catch (Exception e) {
			e.printStackTrace();
			stockMap = new HashMap<String, Double>();
		}
		writer = new RandomAccessFile(file, "rws");
	}

	/**
	 * Apply an order.
	 * @return true if has been accepted, false if it has been rejected.
	 */
	public synchronized boolean applyOrder(Order order) {
		Log.log("Applying order: " + order.toString());
		Double quantity = stockMap.get("1");
		Log.log("Quantity " + quantity);
		if (quantity != null && quantity >= order.getQuantity()) {
			Log.log("Changing value");
			stockMap.put(order.getProductId().toString(), quantity - order.getQuantity());
			// Only write stock file if it has changed.
			saveStockToFile();
			return true;
		} else {
			return false;
		}
	}

	public synchronized Map<String, Double> addStock(Map<String, Double> stockMap) {
		// Change memory copy of stock
		for (Entry<String, Double> entry : stockMap.entrySet()) {
			Double stock = this.stockMap.get(entry.getKey());
			if (stock != null) {
				this.stockMap.put(entry.getKey(), stock + stockMap.get(entry.getKey()));
			} else {
				this.stockMap.put(entry.getKey(), stockMap.get(entry.getKey()));
			}
		}
		// Write it to file.
		saveStockToFile();
		return new HashMap<String, Double>(this.stockMap);
	}
	
	private void saveStockToFile(){
		try {
			writer.seek(0);
			String stock = json.toJson(stockMap) + System.lineSeparator();
			writer.write(stock.getBytes(), 0, stock.getBytes().length);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
