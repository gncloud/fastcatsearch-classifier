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

public class BasicStringHashSet implements Streamable, Clonable<BasicStringHashSet>, Copyable<BasicStringHashSet> {
	
	protected static final Logger logger = LoggerFactory.getLogger(BasicStringHashSet.class);
	static { logger.trace("LOADED : {}", BasicStringHashSet.class); }
	
	private HashFunctions hash;
	
	private int[] hashBucket;
	private int[] collisions;
	private int[] dataOffset;
	private char[] dataStore;
	
	private int dataCount;
	private int dataStoreOffset;
	
	private boolean ignoreCase;
	
	protected BasicStringHashSet(BasicStringHashSet one, boolean twin) {
		this.hash = one.hash;
		this.dataCount = one.dataCount;
		this.dataStoreOffset = one.dataStoreOffset;
		this.ignoreCase = one.ignoreCase;
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
	
	public BasicStringHashSet() {
		this(0);
	}
	
	public BasicStringHashSet(int tableSize) {
		this(tableSize, tableSize * 3);
	}
	
	public BasicStringHashSet(int tableSize, int hashBucketSize) {
		this(HashFunctions.HashType.RSHash, tableSize, hashBucketSize);
	}
	
	public BasicStringHashSet(HashFunctions.HashType hashType, int tableSize, int hashBucketSize) {
		if(hashBucketSize == 0) { hashBucketSize = 100; }
		hash = HashFunctions.getInstance(hashType, hashBucketSize);
		hashBucket = new int[hashBucketSize];
		dataStore = new char[tableSize * 5];
		dataOffset = new int[tableSize];
		collisions = new int[tableSize];
		dataCount = 0;
		ignoreCase = true;
	}
	
	public boolean isIgnoreCase() {
		return ignoreCase;
	}

	public void setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}
	
	public int dataCount() {
		return dataCount;
	}
	
	public int tableSize() {
		return dataOffset.length;
	}
	
	public Integer getId(CharSequence str) {
		int id = 0;
		if(str instanceof VString) {
			VString vstr = (VString)str;
			int hashValue = hash.hashValue(vstr.buffer(), vstr.offset(), vstr.length());
			id = hashBucket[hashValue];
			while (id > 0) {
				if(isSame(vstr.buffer(), vstr.offset(), vstr.length(), id)) {
					break;
				}
				growSize(id);
				id = collisions[id];
			}
		} else {
			return getId(new VString(str));
		}
		return id;
	}
	
	private boolean isSame(char[] buf, int offset, int length, int id) {
		if(id > 0) {
			//compare data with dataStore 
			int sOffset = dataOffset[id-1];
			int sLength = 0;
			if(id < dataCount) {
				//data exists in somewhere of this table
				sLength = dataOffset[id] - dataOffset[id-1];
			} else {
				//currently last data in this table
				sLength = dataStoreOffset - sOffset;
			}
			VString vstr = new VString(dataStore, sOffset, sLength);
			vstr.setIgnoreCase(ignoreCase);
			return vstr.equals(buf, offset, length);
		}
		return false;
	}
	
	public Integer put(CharSequence str) {
		if(str instanceof VString) {
			VString vstr = (VString) str;
			return put(vstr.buffer(),vstr.offset(),vstr.length());
		}
		return put(new VString(str));
	}
	
	public synchronized int put(char[] buffer, int offset, int length) {
		int hashValue = hash.hashValue(buffer, offset, length);
		int id = hashBucket[hashValue];
		int collisionId = 0;
		while (id > 0) {
			if(isSame(buffer, offset, length, id)) {
				break;
			}
			collisionId = id;
			growSize(id);
			id = collisions[id];
		}
		if(id == 0) {
			//new value
			id = getNextId();
			if ((dataStoreOffset + length) >= dataStore.length) {
				int dataStoreSize = ArrayUtility.growup(dataStore.length + length);
				dataStore = Arrays.copyOf(dataStore, dataStoreSize);
			}
			dataOffset[id-1] = dataStoreOffset;
			System.arraycopy(buffer, offset, dataStore, dataStoreOffset, length);
			if(collisionId != 0) {
				//has collisions
				collisions[collisionId] = id;
			} else {
				//no collisions
				hashBucket[hashValue] = id;
			}
			dataStoreOffset += length;
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
	//protected void tableExpands (int tableSize) { }
	
	public void growSize(int size) {
		if(dataOffset.length <= size) {
			int newSize = ArrayUtility.growup(size+1);
			dataOffset = Arrays.copyOf(dataOffset, newSize);
			collisions = Arrays.copyOf(collisions, newSize);
		}
	}
	
	public VString get(int id) {
		VString ret = null;
		if(id > 0) {
			int offset = dataOffset[id-1];
			int length = 0;
			if(id < dataCount) {
				length = dataOffset[id] - dataOffset[id-1];
			} else if(id == dataCount) {
				length = dataStoreOffset - dataOffset[id-1];
			} else {
				return null;
			}
			ret = new VString(dataStore, offset, length);
			ret.setIgnoreCase(ignoreCase);
		}
		return ret;
	}

	@Override
	public BasicStringHashSet clone() {
		return new BasicStringHashSet(this, false);
	}

	@Override
	public synchronized int readFrom(InputStream istream, byte[] buffer) throws IOException {
		int ret = 0;
		int pos = 0;
		if (buffer == null || buffer.length < 512) { buffer = new byte[512]; }
		ret += istream.read(buffer, 0, ArrayUtility.BYTES_INTEGER * 5 + 2);
		int hashBucketSize = ArrayUtility.restoreInteger(buffer, pos);
		int dataStoreSize = ArrayUtility.restoreInteger(buffer, pos += ArrayUtility.BYTES_INTEGER);
		int tableSize = ArrayUtility.restoreInteger(buffer, pos += ArrayUtility.BYTES_INTEGER);
		this.dataCount = ArrayUtility.restoreInteger(buffer, pos += ArrayUtility.BYTES_INTEGER);
		this.dataStoreOffset = ArrayUtility.restoreInteger(buffer, pos += ArrayUtility.BYTES_INTEGER);
		this.ignoreCase = buffer[pos += ArrayUtility.BYTES_INTEGER] == ((byte)0xff);
		int strLen = buffer[pos += 1];
		ret += istream.read(buffer, 0, strLen);
		this.hash = HashFunctions.getInstanceFromClassName(new String(buffer, 0, strLen), hashBucketSize);
		this.hashBucket = new int[hashBucketSize];
		this.dataStore = new char[dataStoreSize];
		this.collisions = new int[tableSize];
		this.dataOffset = new int[tableSize];
		
		ret += ArrayUtility.readInput(istream, buffer, this.hashBucket);
		ret += ArrayUtility.readInput(istream, buffer, this.collisions);
		ret += ArrayUtility.readInput(istream, buffer, this.dataOffset);
		ret += ArrayUtility.readInput(istream, buffer, this.dataStore);
//logger.debug("STORE:{} / RET:{}/{} / SIZE:{}", dataStore.length, ret, streamLength(), strLen);
		//if(ret != streamLength()) { throw new IOException("CHECKLENGTH MISMATCHED"); }
		return ret;
	}

	@Override
	public synchronized int writeTo(OutputStream ostream, byte[] buffer) throws IOException {
		if (buffer == null || buffer.length < 512) { buffer = new byte[512]; }
		int pos = 0;
		byte[] hashCls = hash.getClass().getName().getBytes();
		ArrayUtility.mapInteger(this.hashBucket.length, buffer, pos);
		ArrayUtility.mapInteger(this.dataStore.length, buffer, pos += ArrayUtility.BYTES_INTEGER);
		ArrayUtility.mapInteger(this.dataOffset.length, buffer, pos += ArrayUtility.BYTES_INTEGER);
		ArrayUtility.mapInteger(this.dataCount, buffer, pos += ArrayUtility.BYTES_INTEGER);
		ArrayUtility.mapInteger(this.dataStoreOffset, buffer, pos += ArrayUtility.BYTES_INTEGER);
		buffer[pos += ArrayUtility.BYTES_INTEGER] = (byte) (ignoreCase ? 0xff : 0);
		buffer[pos +=1 ] = (byte) hashCls.length;
		ostream.write(buffer, 0, pos +=1 );
		ostream.write(hashCls, 0, hashCls.length);
//int ret = pos + hashCls.length;
		ArrayUtility.writeOutput(ostream, this.hashBucket, buffer);
//ret += hashBucket.length * ArrayUtility.BYTES_INTEGER;
		ArrayUtility.writeOutput(ostream, this.collisions, buffer);
//ret += collisions.length * ArrayUtility.BYTES_INTEGER;
		ArrayUtility.writeOutput(ostream, this.dataOffset, buffer);
//ret += dataOffset.length * ArrayUtility.BYTES_INTEGER;
		ArrayUtility.writeOutput(ostream, this.dataStore, buffer);
//ret += dataStore.length * ArrayUtility.BYTES_CHAR;
//logger.debug("STORE:{} / RET:{}/{} / SIZE:{}", dataStore.length, ret, streamLength(), hashCls.length);
		return streamLength();
	}

	@Override
	public synchronized int streamLength() {
		return ArrayUtility.BYTES_INTEGER * 5 + 2
			+ hash.getClass().getName().length()
			+ hashBucket.length * ArrayUtility.BYTES_INTEGER
			+ collisions.length * ArrayUtility.BYTES_INTEGER
			+ dataOffset.length * ArrayUtility.BYTES_INTEGER
			+ dataStore.length * ArrayUtility.BYTES_CHAR;
	}

	@Override
	public BasicStringHashSet copy() {
		return clone();
	}

	@Override
	public BasicStringHashSet cloneOf() {
		return new BasicStringHashSet(this, true);
	}

	@Override
	public void cloneTo(BasicStringHashSet instance) {
		instance.hashBucket = this.hashBucket;
		instance.collisions = this.collisions;
		instance.dataOffset = this.dataOffset;
		instance.dataStore = this.dataStore;
	}
}