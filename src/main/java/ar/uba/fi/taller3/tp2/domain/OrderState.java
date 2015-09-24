package ar.uba.fi.taller3.tp2.domain;

/**
 * Representation of the state of an order.
 *
 */
public class OrderState {
	
	public enum State{
		RECIBIDA,
		ACEPTADA,
		RECHAZADA,
		ENTREGADA
	}
	
	private String orderId;
	private State state;
	private long timestamp;

	public  OrderState(String orderId, State state, long timestamp){
		this.orderId = orderId;
		this.state = state;
		this.timestamp = timestamp;
	}
	
	public OrderState(Order order){
		this.orderId = order.getOrderId();
		this.timestamp = System.nanoTime();
	}
	
	public void setState(State state){
		this.state = state;
	}
	
	public String getOrderId(){
		return orderId;
	}

	public long getTimeStamp() {
		return timestamp;
	}

	public State getState() {
		return state;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
}
