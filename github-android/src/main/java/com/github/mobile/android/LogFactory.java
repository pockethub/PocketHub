package com.github.mobile.android;

import static android.util.Log.d;
import static android.util.Log.e;
import static android.util.Log.i;
import static android.util.Log.isLoggable;
import static android.util.Log.v;
import static android.util.Log.w;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;

/**
 * Log
 */
public class LogFactory extends org.apache.commons.logging.LogFactory {

	private final String TAG = "GHLog";

	private final Log log = new Log() {

		public void warn(Object message, Throwable throwable) {
			w(TAG, String.valueOf(message), throwable);
		}

		public void warn(Object message) {
			w(TAG, String.valueOf(message));
		}

		public void trace(Object message, Throwable throwable) {
			v(TAG, String.valueOf(message), throwable);
		}

		public void trace(Object message) {
			v(TAG, String.valueOf(message));
		}

		public boolean isWarnEnabled() {
			return isLoggable(TAG, android.util.Log.WARN);
		}

		public boolean isTraceEnabled() {
			return isLoggable(TAG, android.util.Log.VERBOSE);
		}

		public boolean isInfoEnabled() {
			return isLoggable(TAG, android.util.Log.INFO);
		}

		public boolean isFatalEnabled() {
			return isLoggable(TAG, android.util.Log.ERROR);
		}

		public boolean isErrorEnabled() {
			return isLoggable(TAG, android.util.Log.ERROR);
		}

		public boolean isDebugEnabled() {
			return isLoggable(TAG, android.util.Log.DEBUG);
		}

		public void info(Object message, Throwable throwable) {
			i(TAG, String.valueOf(message), throwable);
		}

		public void info(Object message) {
			i(TAG, String.valueOf(message));
		}

		public void fatal(Object message, Throwable throwable) {
			e(TAG, String.valueOf(message), throwable);
		}

		public void fatal(Object message) {
			e(TAG, String.valueOf(message));
		}

		public void error(Object message, Throwable throwable) {
			e(TAG, String.valueOf(message), throwable);
		}

		public void error(Object message) {
			e(TAG, String.valueOf(message));
		}

		public void debug(Object message, Throwable throwable) {
			d(TAG, String.valueOf(message), throwable);
		}

		public void debug(Object message) {
			d(TAG, String.valueOf(message));
		}
	};

	public Object getAttribute(String name) {
		return null;
	}

	public String[] getAttributeNames() {
		return null;
	}

	public Log getInstance(@SuppressWarnings("rawtypes") Class clazz) throws LogConfigurationException {
		return log;
	}

	public Log getInstance(String name) throws LogConfigurationException {
		return log;
	}

	public void release() {
	}

	public void removeAttribute(String name) {
	}

	public void setAttribute(String name, Object value) {
	}
}
