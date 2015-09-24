package ar.uba.fi.taller3.tp2.delivery;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;

import ar.uba.fi.taller3.tp2.Log;
import ar.uba.fi.taller3.tp2.domain.Order;
import ar.uba.fi.taller3.tp2.domain.OrderState;
import ar.uba.fi.taller3.tp2.domain.OrderState.State;

/**
 * Consumer for processing the deliveries.
 *
 */
public class DeliveryConsumer implements Runnable {
	// Queue names.
	private static final String DELIVERY_QUEUE_NAME = "delivery";
	private static final String STATE_QUEUE_NAME = "state";
	// RabbitMQ Host.
	private static final String HOST = "localhost";
	// RabbitMQ connection.
	private final Connection connection;
	// RabbitMQ channel.
	private final Channel channel;
	// RabbitMQ consumer.
	private final QueueingConsumer consumer;
	// Json parser
	private Gson json = new Gson();
	
	private boolean finished = false;
	// Amount of orders to process in every step.
	private int step;
	// Time in milliseconds to sleep between steps.
	private long sleeptime;
	
	public DeliveryConsumer(int step, long sleepTime) throws IOException, TimeoutException  {
	    ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost(HOST);
	    connection = factory.newConnection();
	    channel = connection.createChannel();
	    consumer = new QueueingConsumer(channel);
	    channel.queueDeclare(DELIVERY_QUEUE_NAME, true, false, false, null);
	    channel.queueDeclare(STATE_QUEUE_NAME, true, false, false, null);
	    channel.basicConsume(DELIVERY_QUEUE_NAME, true, consumer);
	    this.step = step;
	    this.sleeptime = sleepTime;
	}
	
	@Override
	public void run() {
		int count = 0;
		while(!finished){
			try{
				// Get order.
				Delivery del = consumer.nextDelivery();
				String message =  new String(del.getBody());
				Log.log("Received: " +message);
				Order o = json.fromJson(message, Order.class);
				OrderState state = new OrderState(o);
				// Change state.
				state.setState(State.ENTREGADA);
				String request = json.toJson(state);
				channel.basicPublish("", STATE_QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN, request.getBytes());
				// Check if the step has finished.
				if(count % step == 0){
					Thread.sleep(sleeptime);
				}
				count++;
			} catch(Exception e){
				finished = true;
				e.printStackTrace();
			}
		}
	}
	
	public void shutdown(){
		finished = true;
		try {
			channel.close();
			connection.close();
		} catch (IOException | TimeoutException e) {
			e.printStackTrace();
		}
	}

}
