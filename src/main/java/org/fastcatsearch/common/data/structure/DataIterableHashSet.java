package org.fastcatsearch.common.data.structure;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Iterator;

import org.fastcatsearch.common.data.type.Streamable;

public class DataIterableHashSet extends BasicStringHashSet {
	
	private int[] startOffset;
	private int[] lastOffset;
	private int[] data;
	
	private int dataOffset;
	
	protected DataIterableHashSet(DataIterableHashSet one, boolean twin) {
		super(one, twin);
		if(twin) {
			this.startOffset = one.startOffset;
			this.lastOffset = one.lastOffset;
			this.data = one.data;
		} else {
			this.startOffset = Arrays.copyOf(one.startOffset, one.startOffset.length);
			this.lastOffset = Arrays.copyOf(one.lastOffset, one.lastOffset.length);
			this.data = Arrays.copyOf(one.data, one.data.length);
		}
	}
	
	public DataIterableHashSet() {
		this(0);
	}
	
	public DataIterableHashSet(int tableSize) {
		this(tableSize, tableSize * 3);
	}
	
	public DataIterableHashSet(int tableSize, int hashBucketSize) {
		this(HashFunctions.HashType.RSHash, tableSize, hashBucketSize);
	}
	
	public DataIterableHashSet(HashFunctions.HashType hashType, int tableSize, int hashBucketSize) {
		super(hashType, tableSize, hashBucketSize);
		this.startOffset = new int[tableSize()];
		this.lastOffset = new int[tableSize()];
		this.data = new int[tableSize()*4];
	}
	
	
	public synchronized int store(CharSequence str, Streamable data) throws Exception {
		byte[] buffer = new byte[512];
		int dataLength = data.streamLength() + 2;
		if(dataOffset + dataLength >= this.data.length) {
			int newSize = ArrayUtility.growup(dataOffset + dataLength);
			this.data = Arrays.copyOf(this.data, newSize);
		}
		int id = put(str);
		if(startOffset.length <= id) {
			int size = ArrayUtility.growup(id);
			startOffset = Arrays.copyOf(startOffset, size);
			lastOffset = Arrays.copyOf(lastOffset, size);
		}
		
		if(id == this.dataCount() && this.startOffset[id] == 0) {
			//new data
			this.startOffset[id] = dataOffset;
		} else {
			int lastOffset = this.lastOffset[id];
			//next offset for previous data
			this.data[lastOffset] = dataOffset;
		}
		this.data[dataOffset + 1] = dataLength - 2;
		ArrayUtility.toIntBuffer(data, this.data, dataOffset + 2, buffer);
		this.lastOffset[id] = dataOffset;//(dataOffset += dataLength);
		this.dataOffset += dataLength;
		return dataLength;
	}
	
	
	@Override
	public DataIterableHashSet clone() {
		return new DataIterableHashSet(this, false);
	}
	
	@Override
	public synchronized int readFrom(InputStream istream, byte[] buffer) throws IOException {
		int ret = 0;
		if (buffer == null || buffer.length < 100) { buffer = new byte[100]; }
		ret += super.readFrom(istream, buffer);
		ret += istream.read(buffer, 0, ArrayUtility.BYTES_INTEGER * 3);
		int pos = 0;
		int tableSize = ArrayUtility.restoreInteger(buffer, pos);
		int dataSize = ArrayUtility.restoreInteger(buffer, pos += ArrayUtility.BYTES_INTEGER);
		this.dataOffset = ArrayUtility.restoreInteger(buffer, pos += ArrayUtility.BYTES_INTEGER);
		this.startOffset = new int[tableSize];
		this.lastOffset = new int[tableSize];
		this.data = new int[dataSize];
		ret += ArrayUtility.readInput(istream, buffer, this.startOffset);
		ret += ArrayUtility.readInput(istream, buffer, this.lastOffset);
		ret += ArrayUtility.readInput(istream, buffer, this.data);
		//if(ret != streamLength()) { throw new IOException("CHECKLENGTH MISMATCHED"); }
		return ret;
	}
	
	@Override
	public synchronized int writeTo(OutputStream ostream, byte[] buffer) throws IOException {
		if (buffer == null || buffer.length < 100) { buffer = new byte[100]; }
		super.writeTo(ostream, buffer);
		int pos = 0;
		ArrayUtility.mapInteger(this.startOffset.length, buffer, pos);
		ArrayUtility.mapInteger(this.data.length, buffer, pos += ArrayUtility.BYTES_INTEGER);
		ArrayUtility.mapInteger(this.dataOffset, buffer, pos += ArrayUtility.BYTES_INTEGER);
		ostream.write(buffer, 0, pos + ArrayUtility.BYTES_INTEGER);
		ArrayUtility.writeOutput(ostream, this.startOffset, buffer);
		ArrayUtility.writeOutput(ostream, this.lastOffset, buffer);
		ArrayUtility.writeOutput(ostream, this.data, buffer);
		return streamLength();
	}
	
	@Override
	public synchronized int streamLength() {
		return super.streamLength() 
			+ ArrayUtility.BYTES_INTEGER * 3
			+ startOffset.length * ArrayUtility.BYTES_INTEGER
			+ lastOffset.length * ArrayUtility.BYTES_INTEGER
			+ data.length * ArrayUtility.BYTES_INTEGER;
	}
	
	public <T extends Streamable> Iterator<T> find(CharSequence str, Class<T> cls) {
		int id = this.getId(str);
		int startOffset = this.startOffset[id];
		int lastOffset = this.lastOffset[id];
		return new IterableImpl<T>(cls, data, startOffset, lastOffset);
	}
	
	private class IterableImpl<T extends Streamable> implements Iterator<T> {
		private int[] data;
		private int currentOffset;
		private int lastOffset;
		private Class<T> cls;
		boolean hasNext;

		public IterableImpl(Class<T> cls, int[] data, int startOffset, int lastOffset) {
			this.cls = cls;
			this.data = data;
			this.currentOffset = startOffset;
			this.lastOffset = lastOffset;
			hasNext = startOffset <= lastOffset;
		}

		@Override
		public boolean hasNext() {
			return hasNext;
		}

		@Override
		public T next() {
			if(currentOffset == lastOffset) {
				hasNext = false;
			}
			
			byte[] buf = new byte[512];
			
			int nextOffset = data[currentOffset];
			int length = data[currentOffset + 1];
			T ret = null;
			Exception ex = null;
			try {
				ret = cls.newInstance();
				ArrayUtility.fromIntBuffer(data, currentOffset + 2, length, ret, buf);
			} catch (InstantiationException e) { ex = e;
			} catch (IllegalAccessException e) { ex = e;
			} finally {
				if (ex != null) {
					logger.error("", ex);
					throw new RuntimeException (ex);
				}
			}
			currentOffset = nextOffset;
			return ret;
		}

		@Override public void remove() { throw new RuntimeException ("NotImplemented"); }
	}

	@Override
	public DataIterableHashSet copy() {
		return clone();
	}

	@Override
	public DataIterableHashSet cloneOf() {
		return new DataIterableHashSet(this, true);
	}

	public void cloneTo(DataIterableHashSet instance) {
		super.cloneTo(instance);
		instance.startOffset = this.startOffset;
		instance.lastOffset = this.lastOffset;
		instance.data = this.data;
		instance.dataOffset = this.dataOffset;
	}
}