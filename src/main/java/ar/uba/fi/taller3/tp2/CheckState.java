package ar.uba.fi.taller3.tp2;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import ar.uba.fi.taller3.tp2.domain.OrderState;
import ar.uba.fi.taller3.tp2.domain.OrderState.State;

/**
 * Process for checking the state of an order.
 * 
 * @param args[0] The order id to check the state.
 */
public class CheckState {
	
	// Queue name.
	private static final String REQUEST_QUEUE_NAME = "check_state";
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

	public CheckState() throws Exception {
	    ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost(HOST);
	    connection = factory.newConnection();
	    channel = connection.createChannel();

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
		OrderState o = json.fromJson(response, OrderState.class);
		if(o.getTimeStamp() > 0){
			Log.log("Estado: "+ o.getState());
		}else{
			Log.log("No existe esa orden");
		}
	}

	public void close() throws Exception {
		connection.close();
	}

	public static void main(String[] args) throws Exception {
		CheckState checkState = new CheckState();
		OrderState o = new OrderState(args[0], State.RECHAZADA, 0);
		checkState.sendMessage(json.toJson(o));
		checkState.close();
	}
}
