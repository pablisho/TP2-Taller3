package ar.uba.fi.taller3.tp2;

import java.util.Random;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.QueueingConsumer;

import ar.uba.fi.taller3.tp2.domain.Order;

/**
 * Process for simulating sending orders.
 * 
 * @param args[0] The amount of orders to send.
 */
public class OrderSender {

	// Queue name.
	private static final String REQUEST_QUEUE_NAME = "orders";
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

	public OrderSender() throws Exception {
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
		channel.basicPublish("", REQUEST_QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
		Log.log("Request msg: " + message);
	}

	public void close() throws Exception {
		connection.close();
	}

	public static void main(String[] args) throws Exception {
		OrderSender sender = new OrderSender();
		Gson gson = new Gson();
		Random randomGen = new Random();
		int orders = Integer.parseInt(args[0]);
		// Generate random orders and send them.
		for (int i = 0; i < orders; i++) {
			int userId = randomGen.nextInt(1000);
			int productId = randomGen.nextInt(4) + 1;
			int cant = randomGen.nextInt(10) + 1;
			Order o = new Order(userId, productId, cant);
			String msg = gson.toJson(o);
			sender.sendMessage(msg);
		}
		sender.close();
	}

}
