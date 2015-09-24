package ar.uba.fi.taller3.tp2.statemanager;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.MessageProperties;

import ar.uba.fi.taller3.tp2.Log;
import ar.uba.fi.taller3.tp2.RabbitMQConsumer;
import ar.uba.fi.taller3.tp2.domain.OrderState;
import ar.uba.fi.taller3.tp2.domain.StateRepository;

/**
 * Consumer that receives request for checking the state of an order.
 *
 */
public class CheckStateConsumer extends RabbitMQConsumer implements Runnable {

	private static final String CHECK_STATE_QUEUE_NAME = "check_state";

	private final Gson json = new Gson();
	
	private StateRepository stateRepository;
	
	public CheckStateConsumer(StateRepository state) throws TimeoutException, IOException {
		super();
		this.stateRepository = state;
	}
	
	@Override
	public void doWork(String message, BasicProperties properties) throws IOException {
		OrderState o = json.fromJson(message, OrderState.class);
		stateRepository.checkState(o);
		String response =  json.toJson(o);
		Log.log(response);
		getChannel().basicPublish("", properties.getReplyTo(), MessageProperties.PERSISTENT_TEXT_PLAIN, response.getBytes());
	}
	
	@Override
	public void run() {
		try{
			declareQueues(CHECK_STATE_QUEUE_NAME);
			Log.log(" [*] Waiting for messages. To exit press CTRL+C");
			startConsuming(CHECK_STATE_QUEUE_NAME);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
