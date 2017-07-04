
package controller;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Collectors;
import org.opencv.core.Mat;

public class Broker {

	public ArrayBlockingQueue<Mat> queue = new ArrayBlockingQueue<>(1000);
	public ArrayBlockingQueue<String> faces = new ArrayBlockingQueue<>(1000);
	public Boolean continueProducing = Boolean.TRUE;


	public void put(Mat data) throws InterruptedException {
		this.queue.offer(data);
	}


	public Mat get() throws InterruptedException {
		// return this.queue.poll(1, TimeUnit.SECONDS);
		return this.queue.take();
	}


	public void putFace(String data) throws InterruptedException {
		this.faces.offer(data);
	}


	public String getFace() throws InterruptedException {
		return this.faces.take();
	}


	public List<String> getFiveImages() throws InterruptedException {
		return faces.stream()
				.limit(5)
				.collect(Collectors.toList());
	}
}
