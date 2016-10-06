package cz.mzk.recordmanager.server.util.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class FaultTolerantProxy {
	
	private static class FaultTolerantProxyImpl<T>  implements InvocationHandler {

		private final T target;

		private final int maxRetries;

		private final int sleepBetweenFailures;

		protected FaultTolerantProxyImpl(T target, int maxRetries, int sleepBetweenFailures) {
			super();
			this.target = target;
			this.maxRetries = maxRetries;
			this.sleepBetweenFailures = sleepBetweenFailures;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			Exception lastException = null;
			int pause = sleepBetweenFailures;
			for (int i = 0; i!= this.maxRetries; i++) {
				try {
					return method.invoke(target, args);
				} catch (Exception ex) {
					lastException = ex;
					Thread.sleep(pause);
					pause *= 2;
				}
			}
			throw lastException;
		}

	}

	@SuppressWarnings("unchecked")
	public static <T> T create(T target, int maxRetries, int sleepBetweenFailures) {
		return (T) java.lang.reflect.Proxy.newProxyInstance(target.getClass().getClassLoader(),
				target.getClass().getInterfaces(), new FaultTolerantProxyImpl<T>(target, maxRetries, sleepBetweenFailures));
	}

}
