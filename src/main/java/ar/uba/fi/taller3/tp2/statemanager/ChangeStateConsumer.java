package ar.uba.fi.taller3.tp2.statemanager;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP.BasicProperties;

import ar.uba.fi.taller3.tp2.Log;
import ar.uba.fi.taller3.tp2.RabbitMQConsumer;
import ar.uba.fi.taller3.tp2.domain.OrderState;
import ar.uba.fi.taller3.tp2.domain.StateRepository;

/**
 * Consumer that receives requests for changing the state of an order.
 *
 */
public class ChangeStateConsumer extends RabbitMQConsumer implements Runnable {
	private static final String STATE_QUEUE_NAME = "state";
	
	private final Gson json = new Gson();
	
	private StateRepository stateRepository;

	public ChangeStateConsumer(StateRepository state) throws TimeoutException, IOException {
		super();
		this.stateRepository = state;
	}

	@Override
	public void doWork(String message, BasicProperties properties) throws IOException  {
		OrderState o = json.fromJson(message, OrderState.class);
		stateRepository.changeState(o);
	}

	@Override
	public void run(){
		try{
			declareQueues(STATE_QUEUE_NAME);
			Log.log(" [*] Waiting for messages. To exit press CTRL+C");
			startConsuming(STATE_QUEUE_NAME);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
}
