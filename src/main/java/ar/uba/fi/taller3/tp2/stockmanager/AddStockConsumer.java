package ar.uba.fi.taller3.tp2.stockmanager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.MessageProperties;

import ar.uba.fi.taller3.tp2.Log;
import ar.uba.fi.taller3.tp2.RabbitMQConsumer;
import ar.uba.fi.taller3.tp2.domain.StockRepository;

/**
 * Consumer that receives request for adding stock.
 *
 */
public class AddStockConsumer extends RabbitMQConsumer implements Runnable {

	private static final String ADD_STOCK_QUEUE_NAME = "add_stock";

	private final Gson json = new Gson();
	
	private StockRepository stockRepository;
	
	public AddStockConsumer(StockRepository stock) throws TimeoutException, IOException {
		super();
		this.stockRepository = stock;
	}
	
	@Override
	public void doWork(String message, BasicProperties properties) throws IOException {
		try{
			Map<String, Double> stockMap = json.fromJson(message, HashMap.class);
			Map<String, Double> currentStock = stockRepository.addStock(stockMap);
			String response =  json.toJson(currentStock);
			getChannel().basicPublish("", properties.getReplyTo(), MessageProperties.PERSISTENT_TEXT_PLAIN, response.getBytes());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		try{
			declareQueues(ADD_STOCK_QUEUE_NAME);
			Log.log(" [*] Waiting for messages. To exit press CTRL+C ADD STOCK");
			startConsuming(ADD_STOCK_QUEUE_NAME);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
