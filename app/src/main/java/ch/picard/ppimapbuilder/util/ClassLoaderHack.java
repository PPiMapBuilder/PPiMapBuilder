/*   
 * This file is part of PPiMapBuilder.
 *
 * PPiMapBuilder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PPiMapBuilder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PPiMapBuilder.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2015 Echeverria P.C., Dupuis P., Cornut G., Gravouil K., Kieffer A., Picard D.
 * 
 */    	
    
package ch.picard.ppimapbuilder.util;

import jdk.nashorn.internal.codegen.CompilerConstants;

import java.util.concurrent.Callable;

/**
 * This is to fix JVM's factory auto-discovery, etc..
 * (things such as JAXB and StAX might not always work
 * within OSGI environment...)
 *
 * @author rodche
 *         Found at : https://github.com/cytoscape/cytoscape-impl/blob/develop/biopax-impl/src/main/java/org/cytoscape/biopax/internal/util/ClassLoaderHack.java
 */
public class ClassLoaderHack {
	public static final <T> T runWithHack(Callable<T> callable, Class<?> clazz) throws Exception {
		Thread thread = Thread.currentThread();
		ClassLoader loader = thread.getContextClassLoader();
		try {
			thread.setContextClassLoader(clazz.getClassLoader());
			return callable.call();
		} finally {
			thread.setContextClassLoader(loader);
		}
	}

	public static final void runWithHack(ThrowingRunnable runnable, Class<?> clazz) throws Exception {
	    runWithHack((Callable<Void>) runnable, clazz);
	}

	public static final <T> T runWithClojure(Callable<T> callable) throws Exception {
		return runWithHack(callable, clojure.core__init.class);
	}

	public abstract static class ThrowingRunnable implements Callable<Void> {
		public abstract void run() throws Exception;

		@Override
		public Void call() throws Exception {
			run();
			return null;
		}
	}
}