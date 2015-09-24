package ar.uba.fi.taller3.tp2;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

/**
 * Process for simulating adding stock.
 * 
 * @param args[0] the product id to add to.
 * @param args[1] the amount to add.
 */
public class AddStock {
	
	// Queue name.
	private static final String REQUEST_QUEUE_NAME = "add_stock";
	// RabbitMQ Host.
	private static final String HOST = "localhost";
	
	// RabbitMQ connection.
	private final Connection connection;
	// RabbitMQ channel.
	private final Channel channel;
	// RabbitMQ consumer.
	private final QueueingConsumer consumer;
	// Queue name for getting responses.
	private final String replyQueueName;
	// Json parser.
	private static Gson json = new Gson();

	public AddStock() throws IOException, TimeoutException  {
	    ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost(HOST);
	    connection = factory.newConnection();
	    channel = connection.createChannel();
	    // Get queue for responses.
	    replyQueueName = channel.queueDeclare().getQueue(); 
	    consumer = new QueueingConsumer(channel);
	    channel.basicConsume(replyQueueName, true, consumer);
	}

	public void sendMessage(String message) throws Exception {
		BasicProperties props = new BasicProperties
                .Builder()
                .replyTo(replyQueueName).deliveryMode(2)
                .build();
		// Send request.
		channel.basicPublish("", REQUEST_QUEUE_NAME, props, message.getBytes());
		// Wait for answer.
		QueueingConsumer.Delivery delivery = consumer.nextDelivery();
		String response = new String(delivery.getBody());
		Map<String,Double> stock = json.fromJson(response, HashMap.class);
		Log.log("Stock: ");
		for(Entry<String,Double> entry : stock.entrySet()){
			Log.log(entry.getKey() +": "+entry.getValue());
		}
	}

	public void close() throws Exception {
		connection.close();
	}

	public static void main(String[] args) throws Exception {
		AddStock addStock = new AddStock();
		Map<String,Double> stock = new HashMap<String,Double>();
		stock.put(args[0], Double.parseDouble(args[1]));
		addStock.sendMessage(json.toJson(stock));
		addStock.close();
	}

}
