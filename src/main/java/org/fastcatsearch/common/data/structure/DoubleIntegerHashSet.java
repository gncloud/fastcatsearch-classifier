package org.fastcatsearch.common.data.structure;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.fastcatsearch.common.data.type.Clonable;
import org.fastcatsearch.common.data.type.Copyable;
import org.fastcatsearch.common.data.type.Streamable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DoubleIntegerHashSet implements Streamable, Clonable<DoubleIntegerHashSet>, Copyable<DoubleIntegerHashSet> {
	
	protected static final Logger logger = LoggerFactory.getLogger(DoubleIntegerHashSet.class);
	static { logger.trace("LOADED : {}", DoubleIntegerHashSet.class); }
	
	private HashFunctions hash;
	
	private int[] hashBucket;
	private int[] collisions;
	private int[] dataOffset;
	private long[] dataStore;
	
	private int dataCount;
	private int dataStoreOffset;
	
	private char[] hashBuffer = new char[4];
	
	protected DoubleIntegerHashSet(DoubleIntegerHashSet one, boolean twin) {
		this.hash = one.hash;
		this.dataCount = one.dataCount;
		this.dataStoreOffset = one.dataStoreOffset;
		if(twin) {
			this.hashBucket = one.hashBucket;
			this.collisions = one.collisions;
			this.dataOffset = one.dataOffset;
			this.dataStore = one.dataStore;
		} else {
			this.hashBucket = Arrays.copyOf(one.hashBucket, one.hashBucket.length);
			this.collisions = Arrays.copyOf(one.collisions, one.collisions.length);
			this.dataOffset = Arrays.copyOf(one.dataOffset, one.dataOffset.length);
			this.dataStore = Arrays.copyOf(one.dataStore, one.dataStore.length);
		}
	}
	
	public DoubleIntegerHashSet() {
		this(0);
	}
	
	public DoubleIntegerHashSet(int tableSize) {
		this(tableSize, tableSize * 3);
	}
	
	public DoubleIntegerHashSet(int tableSize, int hashBucketSize) {
		this(HashFunctions.HashType.RSHash, tableSize, hashBucketSize);
	}
	
	public DoubleIntegerHashSet(HashFunctions.HashType hashType, int tableSize, int hashBucketSize) {
		if(hashBucketSize == 0) { hashBucketSize = 100; }
		hash = HashFunctions.getInstance(hashType, hashBucketSize);
		hashBucket = new int[hashBucketSize];
		dataStore = new long[tableSize * 2];
		dataOffset = new int[tableSize];
		collisions = new int[tableSize];
		dataCount = 0;
	}
	
	public int dataCount() {
		return dataCount;
	}
	
	public int tableSize() {
		return dataOffset.length;
	}
	
	
	public Integer getId(int value1, int value2) {
		int id = 0;
		int hashValue = 0;
		synchronized(hashBuffer) {
			ArrayUtility.mapIntegerToChar(value1, hashBuffer, 0);
			ArrayUtility.mapIntegerToChar(value2, hashBuffer, 2);
			hashValue = hash.hashValue(hashBuffer, 0, hashBuffer.length);
		}
		id = hashBucket[hashValue];
		while (id > 0) {
			if(isSame(value1, value2, id)) {
				break;
			}
			growSize(id);
			id = collisions[id];
		}
		return id;
	}
	
	private boolean isSame(int value1, int value2, int id) {
		if(id > 0) {
			//compare data with dataStore 
			long data = (((long) value2) << 32) | ((long) value1);
			return dataStore[dataOffset[id-1]] == data;
		}
		return false;
	}
	
	public synchronized int put(int value1, int value2) {
		int id = 0;
		int hashValue = 0;
		synchronized(hashBuffer) {
			ArrayUtility.mapIntegerToChar(value1, hashBuffer, 0);
			ArrayUtility.mapIntegerToChar(value2, hashBuffer, 2);
			hashValue = hash.hashValue(hashBuffer, 0, hashBuffer.length);
		}
		id = hashBucket[hashValue];
		int collisionId = 0;
		while (id > 0) {
			if(isSame(value1, value2, id)) {
				break;
			}
			collisionId = id;
			growSize(id);
			id = collisions[id];
		}
		if(id == 0) {
			//new value
			id = getNextId();
			long data = (((long) value2) << 32) | ((long) value1);
			if ((dataStoreOffset + 1) >= dataStore.length) {
				int dataStoreSize = ArrayUtility.growup(dataStore.length + 1);
				dataStore = Arrays.copyOf(dataStore, dataStoreSize);
			}
			dataOffset[id-1] = dataStoreOffset;
			dataStore[dataStoreOffset] = data;
			if(collisionId != 0) {
				//has collisions
				collisions[collisionId] = id;
			} else {
				//no collisions
				hashBucket[hashValue] = id;
			}
			dataStoreOffset ++;
		}
		return id;
	}
	
	private synchronized int getNextId() {
		// data id increases sequencially
		// if array size smaller than dataCount
		// expand size of array
		growSize(dataCount);
		return ++dataCount;
	}
	
	public void growSize(int size) {
		if(dataOffset.length <= size) {
			int newSize = ArrayUtility.growup(size+1);
			dataOffset = Arrays.copyOf(dataOffset, newSize);
			collisions = Arrays.copyOf(collisions, newSize);
		}
	}
	
	public void get(int id, int[] ret) {
		if(id > 0) {
			int offset = dataOffset[id - 1];
			ret[0] = (int)(dataStore[offset] & 0xffffffffL);
			ret[1] = (int)((dataStore[offset] & 0xffffffff00000000L) >> 32);
		}
	}

	@Override
	public DoubleIntegerHashSet clone() {
		return new DoubleIntegerHashSet(this, false);
	}

	@Override
	public synchronized int readFrom(InputStream istream, byte[] buffer) throws IOException {
		int ret = 0;
		if (buffer == null || buffer.length < 512) { buffer = new byte[512]; }
		ret += istream.read(buffer, 0, ArrayUtility.BYTES_INTEGER * 5 + 1);
		int pos = 0;
		int hashBucketSize = ArrayUtility.restoreInteger(buffer, pos);
		int dataStoreSize = ArrayUtility.restoreInteger(buffer, pos += ArrayUtility.BYTES_INTEGER);
		int tableSize = ArrayUtility.restoreInteger(buffer, pos += ArrayUtility.BYTES_INTEGER);
		this.dataCount = ArrayUtility.restoreInteger(buffer, pos += ArrayUtility.BYTES_INTEGER);
		this.dataStoreOffset = ArrayUtility.restoreInteger(buffer, pos += ArrayUtility.BYTES_INTEGER);
		int strLen = buffer[pos += ArrayUtility.BYTES_INTEGER];
		ret += istream.read(buffer, 0, strLen);
		logger.trace("READ: HBSIZE:{} / DSSIZE:{} / TSIZE:{} / DCOUNT:{}", hashBucketSize, dataStoreSize, tableSize, dataCount);
		this.hash = HashFunctions.getInstanceFromClassName(new String(buffer, 0, strLen), hashBucketSize);
		this.hashBucket = new int[hashBucketSize];
		this.collisions = new int[tableSize];
		this.dataOffset = new int[tableSize];
		this.dataStore = new long[dataStoreSize];
		ret += ArrayUtility.readInput(istream, buffer, this.hashBucket);
		ret += ArrayUtility.readInput(istream, buffer, this.collisions);
		ret += ArrayUtility.readInput(istream, buffer, this.dataOffset);
		ret += ArrayUtility.readInput(istream, buffer, this.dataStore);
		//if(ret != streamLength()) { throw new IOException("CHECKLENGTH MISMATCHED"); }
		return ret;
	}

	@Override
	public synchronized int writeTo(OutputStream ostream, byte[] buffer) throws IOException {
		if (buffer == null || buffer.length < 512) { buffer = new byte[512]; }
		int pos = 0;
		byte[] hashCls = hash.getClass().getName().getBytes();
		logger.trace("WRITE: HBSIZE:{} / DSSIZE:{} / TSIZE:{} / DCOUNT:{}", hashBucket.length, dataStore.length, dataOffset.length, dataCount);
		ArrayUtility.mapInteger(this.hashBucket.length, buffer, pos);
		ArrayUtility.mapInteger(this.dataStore.length, buffer, pos += ArrayUtility.BYTES_INTEGER);
		ArrayUtility.mapInteger(this.dataOffset.length, buffer, pos += ArrayUtility.BYTES_INTEGER);
		ArrayUtility.mapInteger(this.dataCount, buffer, pos += ArrayUtility.BYTES_INTEGER);
		ArrayUtility.mapInteger(this.dataStoreOffset, buffer, pos += ArrayUtility.BYTES_INTEGER);
		buffer[pos += ArrayUtility.BYTES_INTEGER] = (byte) hashCls.length;
		ostream.write(buffer, 0, pos +=1);
		ostream.write(hashCls, 0, hashCls.length);
		ArrayUtility.writeOutput(ostream, this.hashBucket, buffer);
		ArrayUtility.writeOutput(ostream, this.collisions, buffer);
		ArrayUtility.writeOutput(ostream, this.dataOffset, buffer);
		ArrayUtility.writeOutput(ostream, this.dataStore, buffer);
		return streamLength();
	}

	@Override
	public synchronized int streamLength() {
		return ArrayUtility.BYTES_INTEGER * 5 + 1
			+ hash.getClass().getName().length()
			+ hashBucket.length * ArrayUtility.BYTES_INTEGER
			+ collisions.length * ArrayUtility.BYTES_INTEGER
			+ dataOffset.length * ArrayUtility.BYTES_INTEGER
			+ dataStore.length * ArrayUtility.BYTES_LONG;
	}

	@Override
	public DoubleIntegerHashSet copy() {
		return clone();
	}

	@Override
	public DoubleIntegerHashSet cloneOf() {
		return new DoubleIntegerHashSet(this, true);
	}

	@Override
	public void cloneTo(DoubleIntegerHashSet instance) {
		instance.hashBucket = this.hashBucket;
		instance.collisions = this.collisions;
		instance.dataOffset = this.dataOffset;
		instance.dataStore = this.dataStore;
	}
}