package ar.uba.fi.taller3.tp2.domain;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import ar.uba.fi.taller3.tp2.domain.OrderState.State;

/**
 * Thread safe manager for orders states.
 *
 */
public class StateRepository {
	// The folder to store the order states.
	private String folderName;
	// The state database is splitted, so we have different locks for different sections.
	private List<String> lockList = new ArrayList<String>();

	public StateRepository(String folderName) {
		this.folderName = folderName;
	}

	public void changeState(OrderState orderState) {
		// We lock on the first 2 characters of the order id.
		String lockCandidate = orderState.getOrderId().substring(0, 2);
		synchronized (getLock(lockCandidate)) {
			File file = new File(folderName + File.separator + lockCandidate);
			file.getParentFile().mkdirs();
			// Get current state.
			long currentTimeStamp = 0;
			if(file.exists()){
				String line;
				try (InputStream fis = new FileInputStream(file);
						InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
						BufferedReader br = new BufferedReader(isr);) {
					while ((line = br.readLine()) != null) {
						if(line.equals(orderState.getOrderId())){
							line = br.readLine();
							if(line != null){
								currentTimeStamp = Long.parseLong(line);
							}
						}
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			// Change it
			if(orderState.getTimeStamp() > currentTimeStamp){
				try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)))) {
				    out.println(orderState.getOrderId());
				    out.println(orderState.getTimeStamp());
				    out.println(orderState.getState());
				}catch (IOException e) {
				    e.printStackTrace();
				}
			}
		}
	}
	
	public void checkState(OrderState orderState) {
		String lockCandidate = orderState.getOrderId().substring(0, 2);
		synchronized (getLock(lockCandidate)) {
			File file = new File(folderName + File.separator + lockCandidate);
			file.getParentFile().mkdirs();
			// Get current state.
			if(file.exists()){
				String line;
				try (InputStream fis = new FileInputStream(file);
						InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
						BufferedReader br = new BufferedReader(isr);) {
					while ((line = br.readLine()) != null) {
						if(line.equals(orderState.getOrderId())){
							line = br.readLine();
							if(line != null){
								orderState.setTimestamp(Long.parseLong(line));
							}
							line = br.readLine();
							if(line != null){
								orderState.setState(State.valueOf(line));
							}
						}
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private synchronized String getLock(String lockCandidate) {
		int lockIndex = lockList.indexOf(lockCandidate);
		if (lockIndex != -1) {
			return lockList.get(lockIndex);
		} else {
			lockList.add(lockCandidate);
			return lockCandidate;
		}
	}

	
}
