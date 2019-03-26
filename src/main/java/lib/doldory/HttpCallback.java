package lib.doldory;

public interface HttpCallback<T> {
	
	public abstract void call(T result);

}
