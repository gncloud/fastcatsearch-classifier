package org.fastcatsearch.common.data.type;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamableIterator<T extends Streamable & Clonable<T> & Copyable<T>> implements Iterator<T> {
	
	private static final Logger logger = LoggerFactory.getLogger(StreamableIterator.class);
	
	private InputStream istream;
	private T instance;
	private T element;
	private long streamLength = -1;
	private long currentPos;
	private byte[] buffer;
	
	public StreamableIterator(T instance, InputStream istream, long streamLength, byte[] buffer) {
		this.instance = instance;
		this.istream = istream;
		this.element = null;
		this.streamLength = streamLength;
		this.buffer = buffer;
	}

	@Override
	public synchronized boolean hasNext() {
		if(element == null) {
			element = parse();
		}
		return element != null;
	}

	@Override
	public synchronized T next() {
		if(element == null) {
			element = parse();
		}
		T ret = element;
		element = null;
		return ret;
	}
	
	private T parse() {
		T ret = null;
		Exception ex = null;
		try {
			if(streamLength == -1 || currentPos < streamLength) {
				int rlen = 0;
				if ((rlen = instance.readFrom(istream, buffer)) == instance.streamLength()) {
					currentPos += rlen;
					ret = instance.copy();
				}
			}
		} catch (IOException e) { ex = e;
		} finally {
			if (ex != null) {
				ret = null;
				logger.error("", ex);
			}
		}
		return ret;
	}

	@Override
	public void remove() {
		throw new RuntimeException ("NotImplemented");
	}
}