package tk.nomis_tech.ppimapbuilder.util;

/**
 * This is to fix JVM's factory auto-discovery, etc..
 * (things such as JAXB and StAX might not always work
 * within OSGI environment...)
 *
 * @author rodche
 *         Found at : https://github.com/cytoscape/cytoscape-impl/blob/develop/biopax-impl/src/main/java/org/cytoscape/biopax/internal/util/ClassLoaderHack.java
 */
public class ClassLoaderHack {
	public static final void runWithHack(ThrowingRunnable runnable, Class<?> clazz) throws Exception {
		Thread thread = Thread.currentThread();
		ClassLoader loader = thread.getContextClassLoader();
		try {
			thread.setContextClassLoader(clazz.getClassLoader());
			runnable.run();
		} finally {
			thread.setContextClassLoader(loader);
		}
	}

	public interface ThrowingRunnable {
		public void run() throws Exception;
	}
}