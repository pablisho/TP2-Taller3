package ar.uba.fi.taller3.tp2;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;

/**
 * Abstract class to avoid repeated code in every RabbitMQ consumer.
 *
 */
public abstract class RabbitMQConsumer {
	// RabbitMQ host.
	private static final String HOST = "localhost";
	// RabbitMQ connection
	private final Connection connection;
	// RabbitMQ channel
	private final Channel channel;
	// RabbitMQ consumer
	private final Consumer consumer;

	public RabbitMQConsumer() throws IOException, TimeoutException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(HOST);
		connection = factory.newConnection();
		channel = connection.createChannel();
		consumer = new DefaultConsumer(channel);
	}

	/**
	 * Declare the necessary queues.
	 */
	public void declareQueues(String... queues) throws IOException {
		for (String s : queues) {
			channel.queueDeclare(s, true, false, false, null);
		}
		// Set prefetch count to 1
		channel.basicQos(1);
	}
	
	/**
	 * Start consuming from a queue.
	 */
	public void startConsuming(String queue) throws IOException {
		channel.basicConsume(queue, false, consumer);
	}
	
	/**
	 * Process message.
	 */
	public abstract void doWork(String message, BasicProperties properties) throws IOException;
	
	private class DefaultConsumer extends com.rabbitmq.client.DefaultConsumer{

		public DefaultConsumer(Channel channel) {
			super(channel);
		}
		
		@Override
		public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
				byte[] body) throws IOException {
			String message = new String(body, "UTF-8");
			Log.log(" [x] Received '" + message + "'");
			try {
				doWork(message, properties);
			} catch(Exception e){
				 e.printStackTrace();
			}finally{
				channel.basicAck(envelope.getDeliveryTag(), false);
			}
		}
	}
	
	protected Channel getChannel(){
		return channel;
	}
	
	public void shutdown(){
		try {
			channel.close();
			connection.close();
		} catch (IOException | TimeoutException e) {
			e.printStackTrace();
		}
	}
	
}
