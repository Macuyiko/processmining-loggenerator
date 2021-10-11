package org.processmining.plugins.kutoolbox.parallelfor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Loop {
	public static void main(String[] args) {
		Loop.withIndex(0, 9, new Loop.Each() {
			public Object run(int i) {
				System.out.println(i * 10);
				return i;
			}
		});
	}
	
	public interface Each {
		Object run(int i);
	}

	private static final int CPUs = Runtime.getRuntime().availableProcessors();

	public static void withIndex(int start, int stop, final Each body) {
		int chunksize = (stop - start + CPUs - 1) / CPUs;
		int loops = (stop - start + chunksize - 1) / chunksize;
		ExecutorService executor = Executors.newFixedThreadPool(CPUs);
		final CountDownLatch latch = new CountDownLatch(loops);
		for (int i = start; i < stop;) {
			final int lo = i;
			i += chunksize;
			final int hi = (i < stop) ? i : stop;
			executor.submit(new Runnable() {
				public void run() {
					for (int i = lo; i < hi; i++)
						body.run(i);
					latch.countDown();
				}
			});
		}
		try {
			latch.await();
		} catch (InterruptedException e) {
		}
		executor.shutdown();
	}
	
	public static boolean withStopCondition(int start, int stop, final Object stopCondition, final Each body) {
		ExecutorService executor = Executors.newFixedThreadPool(CPUs);
		CompletionService<Object> service = new ExecutorCompletionService<Object>(executor);
		int chunksize = 10;
		List<Future<Object>> futures = new ArrayList<Future<Object>>();
		
		boolean stopConditionReached = false;
		for (int i = start; i < stop;) {
			try {
				futures = new ArrayList<Future<Object>>();
				final int lo = i;
				i += chunksize;
				final int hi = (i < stop) ? i : stop;
				
				for (int k = lo; k < hi; k++) {
					final int p = k;
					Callable<Object> callable = new Callable<Object>() {
						public Object call() throws Exception {
							return body.run(p);
						}
					};
					futures.add(service.submit(callable));
				}

				for (int j = 0; j < futures.size(); ++j) {
					try {
						Object r = service.take().get();
						if (r.equals(stopCondition)) {
							stopConditionReached = true;
							break;
						}
					} catch (ExecutionException ignore) {
					} catch (InterruptedException ignore) {
					}
				}
				
				if (stopConditionReached)
					break;
			} finally {
				for (Future<Object> f : futures)
					f.cancel(true);
			}
		}
		executor.shutdown();
		return stopConditionReached;
	}

}
