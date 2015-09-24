package ar.uba.fi.taller3.tp2;

/**
 * Thread safe usage of standard output.
 *
 */
public class Log {

	public synchronized static void log(String message){
		System.out.println(message);
	}
	
}
