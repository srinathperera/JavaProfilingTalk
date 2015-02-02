package org.wso2.profile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TunningSampleService{
	private static Random random = new Random();
	private static ConcurrentHashMap<String, String> leakCollector = new ConcurrentHashMap<String, String>();
	
	private final static ConcurrentLinkedQueue<String> workerQueue = new ConcurrentLinkedQueue<String>();
	
	static{
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true){
					try {
						workerQueue.poll();
						Thread.sleep(30000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
		}).start();
	}
	
	
	public String cpuHungryCall1(int count){
		BigInteger sum = BigInteger.valueOf(0);
		for(int i =0; i< 10000*count;i++){
			double value = Math.pow(i, random.nextDouble()); 
			sum = sum.and(BigInteger.valueOf((int)value));
		}
		return sum.toString();
	}
	
	public String cpuHungryCall2(int count){
		try {
			BigInteger sum = BigInteger.valueOf(0);
			sum = recusreAndWait(sum, 0, 100*count); 
			return sum.toString();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	
	public String memoryLeakCall(int size){
		try {
			char[] buffer = new char[size*10000]; 
			for(int i =0; i< buffer.length;i++){
				buffer[i] = (char)random.nextInt(256);
			}
			String str = String.valueOf(buffer);
			workerQueue.add(str);
			return String.valueOf(buffer.hashCode());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	
	public String memoryLeakByQueueCall(int size){
		try {
			char[] buffer = new char[size*10000]; 
			for(int i =0; i< buffer.length;i++){
				buffer[i] = (char)random.nextInt(256);
			}
			String str = String.valueOf(buffer);
			leakCollector.put(String.valueOf(System.currentTimeMillis()), str);
			return String.valueOf(str.hashCode());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}


	public String tooManyObjectsCall(int size){
		try {
			Object[] buffer = new Object[size*10000]; 
			for(int i =0; i< buffer.length;i++){
				buffer[i] = String.valueOf(random.nextInt(256));
			}
			String str = String.valueOf(buffer);
			return str;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public String blockedThreadsCall(int count){
		try {
			synchronized (leakCollector) {
				BigInteger sum = BigInteger.valueOf(0);
				for(int i =0; i< count;i++){
					double value = Math.pow(i, random.nextDouble()); 
					sum = sum.and(BigInteger.valueOf((int)value));
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return sum.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public String threadLeakCall(int count){
		try {
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						Thread.sleep(30000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}).start();
			return "thred started";
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	
	private BigInteger recusreAndWait(BigInteger sum, int i, int max){
			double value = Math.pow(i, random.nextDouble()); 
			if(i < max){
				i++; 
				sum = recusreAndWait(sum, i, max);
			}
			return sum.and(BigInteger.valueOf((int)value));
	}
	
	
	public String tooManyIOOpsCall(int size){
		try {
			File file = File.createTempFile(System.currentTimeMillis() + "a", "b");
			FileOutputStream out = new FileOutputStream(file);
			Object[] buffer = new Object[size*100]; 
			for(int i =0; i< buffer.length;i++){
				buffer[i] = String.valueOf(random.nextInt(256));
				out.write(buffer[i].toString().getBytes());
			}
			out.close();
			return "FilesWritten";
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public String tooManyNetworkOpsCall(int size){
		try {
			File file = File.createTempFile(System.currentTimeMillis() + "a", "b");
			FileOutputStream out = new FileOutputStream(file);
			String[] buffer = new String[size]; 
	        StringBuffer buf = new StringBuffer();

			for(int i =0; i< buffer.length;i++){
				URL oracle = new URL("http://wso2.com");
		        BufferedReader in = new BufferedReader(new InputStreamReader(oracle.openStream()));

		        String inputLine;
		        while ((inputLine = in.readLine()) != null){
		            buf.append(inputLine);
		        }
		        in.close();
			}
			out.close();
			return buf.toString();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}