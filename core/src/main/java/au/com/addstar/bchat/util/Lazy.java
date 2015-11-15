package au.com.addstar.bchat.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;

/**
 * A simple lazy initialization wrapper on a type.
 * You simply provide a supplier that creates an 
 * instance of T then get() will either use
 * the already created instance, or call the 
 * supplier 
 *
 * @param <T> The type to be handled
 */
public class Lazy<T> {
	private final Supplier<T> creationMethod;
	private T instance;
	
	public Lazy(Supplier<T> creationMethod) {
		this.creationMethod = creationMethod;
	}
	
	/**
	 * Gets or constructs the instance
	 * @return The instance.
	 */
	public T get() {
		if (instance == null) {
			instance = creationMethod.get();
			Preconditions.checkNotNull(instance);
		}
		return instance;
	}
	
	/**
	 * Checks if the instance has been assigned
	 * @return True if the instance is assigned
	 */
	public boolean isAssigned() {
		return instance != null;
	}
	
	/**
	 * A shorthand for creating this class
	 * @param creationMethod the supplier that provides the instance
	 * @return The lazy wrapper
	 */
	public static <T> Lazy<T> of(Supplier<T> creationMethod) {
		return new Lazy<T>(creationMethod);
	}
}
