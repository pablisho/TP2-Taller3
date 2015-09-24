package ar.uba.fi.taller3.tp2.logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP.BasicProperties;

import ar.uba.fi.taller3.tp2.Log;
import ar.uba.fi.taller3.tp2.RabbitMQConsumer;
import ar.uba.fi.taller3.tp2.domain.Order;

/**
 * Logger consumer. It reads orders from the 'log' queue and writes them to a
 * file.
 * 
 * @author pablo
 *
 */
public class LoggerConsumer extends RabbitMQConsumer implements Runnable {

	private static final String LOG_QUEUE_NAME = "log";

	private final Gson json = new Gson();
	private PrintWriter writer;

	public LoggerConsumer() throws IOException, TimeoutException {
		super();
		writer = new PrintWriter(new FileWriter(new File("log.txt"), true));
	}

	@Override
	public void doWork(String message, BasicProperties properties) {
		Order o = json.fromJson(message, Order.class);
		writer.println(o.toString());
		writer.flush();
	}

	@Override
	public void run() {
		try {
			declareQueues(LOG_QUEUE_NAME);
			Log.log(" [*] Waiting for messages. To exit press CTRL+C");
			startConsuming(LOG_QUEUE_NAME);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
