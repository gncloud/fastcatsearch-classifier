package org.fastcatsearch.common.data.type;

public interface Clonable<T> {
	public T cloneOf();
	public void cloneTo(T instance);
}