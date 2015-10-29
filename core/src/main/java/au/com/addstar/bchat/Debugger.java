package au.com.addstar.bchat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.google.common.collect.Maps;

public class Debugger {
	public static final String Packet = "Packet";
	public static final String Backend = "Backend";
	public static final String Tab = "Tab";
	
	public static final List<String> KnownTypes = Arrays.asList(Packet, Backend);
	public static final List<String> KnownTypesProxy = Arrays.asList(Packet, Backend, Tab);
	
	private static Map<String, Logger> loggers = Maps.newHashMap();
	private static Logger parent;
	
	public static void initialize(Logger parentLogger) {
		parent = parentLogger;
	}
	
	/**
	 * Gets a debug logger with the specified name.
	 * <p>By default these loggers are disabled, logging to them does nothing.
	 * To enable them, set the level {@link Logger#setLevel(Level)} to something
	 * other than OFF</p>
	 * @param type A name to distinguish this logger
	 * @return The logger
	 */
	public static Logger getLogger(String type) {
		String id = "bungeechat.debug." + type.toLowerCase();
		
		if (loggers.containsKey(id)) {
			return loggers.get(id);
		}
		
		// Create logger
		Logger logger = Logger.getLogger(id);
		logger.setUseParentHandlers(false);
		logger.setLevel(Level.OFF);
		
		if (parent != null) {
			logger.addHandler(new Handler() {
				@Override
				public void publish(LogRecord record) {
					if (type.isEmpty()) {
						record.setMessage(String.format("[Debug] %s", record.getMessage()));
					} else {
						record.setMessage(String.format("[Debug-%s] %s", type, record.getMessage()));
					}
					// Make sure it will be logged
					if (record.getLevel().intValue() < Level.INFO.intValue()) {
						record.setLevel(Level.INFO);
					}
					parent.log(record);
				}
				
				@Override
				public void flush() {
				}
				
				@Override
				public void close() throws SecurityException {
				}
			});
		}
		
		loggers.put(id, logger);
		return logger;
	}
	
	/**
	 * Gets an existing logger. It will not create new ones
	 * @param type The name of the logger
	 * @return The logger or null
	 */
	public static Logger getLoggerNoCreate(String type) {
		String id = "bungeechat.debug." + type.toLowerCase();
		
		return loggers.get(id);
	}
	
	/**
	 * Creates a supplier that will format the string using the provided arguments
	 * @param format The format string. Same as {@code String#format}
	 * @param arguments The arguments for the format. If any of these are {@link Supplier}s then they will be resolved first
	 * @return A supplier to get the formatted string
	 */
	public static Supplier<String> format(final String format, final Object... arguments) {
		return new Supplier<String>() {
			@Override
			public String get() {
				// Resolve arguments
				Object[] resolved = new Object[arguments.length];
				for (int i = 0; i < arguments.length; ++i) {
					if (arguments[i] instanceof Supplier<?>) {
						resolved[i] = ((Supplier<?>)arguments[i]).get();
					} else {
						resolved[i] = arguments[i];
					}
				}
				
				return String.format(format, resolved);
			}
		};
	}
}
