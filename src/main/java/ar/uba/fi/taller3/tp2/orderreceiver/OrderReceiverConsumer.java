package ar.uba.fi.taller3.tp2.orderreceiver;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.MessageProperties;

import ar.uba.fi.taller3.tp2.Log;
import ar.uba.fi.taller3.tp2.RabbitMQConsumer;
import ar.uba.fi.taller3.tp2.domain.Order;
import ar.uba.fi.taller3.tp2.domain.OrderState;
import ar.uba.fi.taller3.tp2.domain.OrderState.State;

/**
 * Consumer for getting orders from a queue, and sending them to the log, the
 * state and stock.
 * 
 *
 */
public class OrderReceiverConsumer extends RabbitMQConsumer implements Runnable {

	private static final String REQUEST_QUEUE_NAME = "orders";
	private static final String LOG_QUEUE_NAME = "log";
	private static final String STATE_QUEUE_NAME = "state";
	private static final String STOCK_QUEUE_NAME = "stock";

	private final Gson json = new Gson();

	public OrderReceiverConsumer() throws IOException, TimeoutException {
		super();
	}

	public void doWork(String message, BasicProperties properties) throws IOException {
		String uuid = UUID.randomUUID().toString();
		Order o = json.fromJson(message, Order.class);
		// Assign id.
		o.setOrderId(uuid);
		String logMessage = json.toJson(o);
		// Send to log.
		getChannel().basicPublish("", LOG_QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN, logMessage.getBytes());
		// Send to stock manager.
		getChannel().basicPublish("", STOCK_QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN, logMessage.getBytes());
		// Send to state manager.
		OrderState state = new OrderState(o);
		state.setState(State.RECIBIDA);
		String stateMessage = json.toJson(state);
		getChannel().basicPublish("", STATE_QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN,
				stateMessage.getBytes());
	}

	@Override
	public void run() {
		try {
			declareQueues(REQUEST_QUEUE_NAME, LOG_QUEUE_NAME, STATE_QUEUE_NAME, STOCK_QUEUE_NAME);
			Log.log(" [*] Waiting for messages. To exit press CTRL+C");
			startConsuming(REQUEST_QUEUE_NAME);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
