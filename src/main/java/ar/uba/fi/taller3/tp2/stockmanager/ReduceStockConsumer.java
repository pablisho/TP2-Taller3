package ar.uba.fi.taller3.tp2.stockmanager;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.MessageProperties;

import ar.uba.fi.taller3.tp2.Log;
import ar.uba.fi.taller3.tp2.RabbitMQConsumer;
import ar.uba.fi.taller3.tp2.domain.Order;
import ar.uba.fi.taller3.tp2.domain.OrderState;
import ar.uba.fi.taller3.tp2.domain.OrderState.State;
import ar.uba.fi.taller3.tp2.domain.StockRepository;

/**
 * Consumer that receives request for reducing the stock.
 *
 */
public class ReduceStockConsumer extends RabbitMQConsumer implements Runnable {
	private static final String STOCK_QUEUE_NAME = "stock";
	private static final String STATE_QUEUE_NAME = "state";
	private static final String DELIVERY_QUEUE_NAME = "delivery";

	private final Gson json = new Gson();

	private StockRepository stock;

	public ReduceStockConsumer(StockRepository stock) throws TimeoutException, IOException {
		super();
		this.stock = stock;
	}

	public void doWork(String message, BasicProperties properties) throws IOException {
		Order o = json.fromJson(message, Order.class);
		boolean result = stock.applyOrder(o);
		OrderState state = new OrderState(o);
		state.setState(result ? State.ACEPTADA : State.RECHAZADA);
		String stateMessage = json.toJson(state);
		getChannel().basicPublish("", STATE_QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN,
				stateMessage.getBytes());
		if(result){
			getChannel().basicPublish("", DELIVERY_QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN,
					message.getBytes());
		}
	}

	@Override
	public void run() {
		try {
			declareQueues(STOCK_QUEUE_NAME, STATE_QUEUE_NAME, DELIVERY_QUEUE_NAME);
			Log.log(" [*] Waiting for messages. To exit press CTRL+C");
			startConsuming(STOCK_QUEUE_NAME);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
