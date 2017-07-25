package org.fastcatsearch.common.data.structure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class HashFunctions {
	protected static final Logger logger = LoggerFactory.getLogger(HashFunctions.class);

	public enum HashType {
		RSHash,
		JSHash,
		DJBHash,
		PJWHash,
		ELFHash,
		BKDRHash,
		SDBMHash,
		DEKHash,
		APHash
	}

	private int hashBucketSize;
	
	public static final HashFunctions getInstance(HashType hashType, int hashBucketSize) {
		String clsName = hashType.name();
		String clsStr = HashFunctions.class.getName() + "$" + clsName;
		return getInstanceFromClassName(clsStr, hashBucketSize);
		
	}
	
	public static HashFunctions getInstanceFromClassName(String clsStr, int hashBucketSize) {
		HashFunctions ret = null;
		try {
			ret = (HashFunctions) Class.forName(clsStr).newInstance();
		} catch (InstantiationException e) {
			logger.error("", e);
		} catch (IllegalAccessException e) {
			logger.error("", e);
		} catch (ClassNotFoundException e) {
			logger.error("", e);
		}
		if(ret != null) {
			ret.setHashBucketSize(hashBucketSize);
		}
		return ret;
	}
	
	protected abstract int rawHashValue(char[] buffer, int offset, int length);
	
	public int hashValue(char[] buffer, int offset, int length) {
		int ret = rawHashValue(buffer, offset, length);
		ret = (ret & 0x7fffffff) % getHashBucketSize();
		return ret;
	}
	
	public int getHashBucketSize() {
		return hashBucketSize;
	}

	public void setHashBucketSize(int hashBucketSize) {
		this.hashBucketSize = hashBucketSize;
	}

	public static class RSHash extends HashFunctions {
		@Override
		protected int rawHashValue(char[] buffer, int offset, int length) {
			int b = 378551;
			int a = 63689;
			int end = offset + length;
			int hashValue = 0;
			for( int inx = offset ; inx < end ; inx++ ) {
				hashValue = hashValue * a + ((int) buffer[inx]);
				a = a * b;
			}
			return hashValue;
		}
	}

	public static class JSHash extends HashFunctions {
		@Override
		protected int rawHashValue(char[] buffer, int offset, int length) {
			int end = offset + length;
			int hashValue = 1315423911;
			for ( int inx = offset ; inx < end ; inx++ ) {
				hashValue ^= ((hashValue << 5) + ((int) buffer[inx]) + (hashValue >> 2));
			}
			return hashValue;
		}
	}

	public static class PJWHash extends HashFunctions {
		private final static int BIT_LENGTH_INT = 4*8;
		private final static int THREE_QUARTERS_INT = BIT_LENGTH_INT*3/4;
		private final static int ONE_EIGHT_INT = BIT_LENGTH_INT/8;
		private final static int HIGH_BITS_INT = (0xFFFFFFFF) << (BIT_LENGTH_INT-ONE_EIGHT_INT);
		//private final static int BIT_LENGTH_LONG = 8*8;
		//private final static int THREE_QUARTERS_LONG = BIT_LENGTH_LONG*3/4;
		//private final static int ONE_EIGHT_LONG = BIT_LENGTH_LONG/8;
		//private final static long HIGH_BITS_LONG = (0xFFFFFFFFFFFFFFFFL) << (BIT_LENGTH_LONG-ONE_EIGHT_LONG);    
		@Override
		protected int rawHashValue(char[] buffer, int offset, int length) {
			int end = offset + length;
			int hashValue = 0;
			int test = 0;
			for(int inx = offset; inx < end; inx++) {
				hashValue = (hashValue << ONE_EIGHT_INT) + ((int) buffer[inx]);
				if((test = hashValue & HIGH_BITS_INT)  != 0) {
					hashValue = (( hashValue ^ (test >> THREE_QUARTERS_INT)) & (~HIGH_BITS_INT));
				}
			}
			return hashValue;
		}
	}

	public static class DJBHash extends HashFunctions {
		@Override
		protected int rawHashValue(char[] buffer, int offset, int length){
			int end = offset + length;
			int hashValue = 5381;
			for(int inx=offset; inx < end; inx++) {
				hashValue = ((hashValue << 5) + hashValue) + ((int) buffer[inx]);
			}
			return hashValue;
		}
	}
	
	public static class ELFHash extends HashFunctions {
		@Override
		protected int rawHashValue(char[] buffer, int offset, int length) {
			int end = offset + length;
			int hashValue = 0;
			int x = 0;
			for(int inx=offset; inx < end; inx++) {
				hashValue = (hashValue << 4) + ((int) buffer[inx]);
				if((x = hashValue & 0xf0000000) != 0) {
					hashValue ^= (x>>24);
					hashValue &= ~x;
				}
			}
			return hashValue;
		}
	}
	
	public static class BKDRHash extends HashFunctions {
		@Override
		protected int rawHashValue(char[] buffer, int offset, int length) {
			int seed = 131;
			int end = offset + length;
			int hashValue = 0;
			for(int inx=offset; inx < end; inx++) {
				hashValue = (hashValue * seed) + ((int) buffer[inx]);
			}
			return hashValue;
		}
	}
	
	public static class SDBMHash extends HashFunctions {
		@Override
		protected int rawHashValue(char[] buffer, int offset, int length) {
			int hashValue = 0;
			int end = offset + length;
			for(int inx=offset; inx < end; inx++) {
				hashValue = ((int) buffer[inx]) + (hashValue << 6) + (hashValue << 16) - hashValue;
			}
			return hashValue;
		}
	}
	
	public static class DEKHash extends HashFunctions {
		@Override
		protected int rawHashValue(char[] buffer, int offset, int length) {
			int end = offset + length;
			int hashValue = end;
			for(int inx=offset; inx < end; inx++) {
				hashValue = ((hashValue << 5) ^ (hashValue >> 27)) ^ ((int) buffer[inx]);
			}
			return hashValue;
		}
	}
	
	public static class APHash extends HashFunctions {
		@Override
		protected int rawHashValue(char[] buffer, int offset, int length) {
			int end = offset + length;
			int hashValue = end;
			for(int inx=offset; inx < end; inx++) {
				hashValue ^= ((inx & 1) == 0) ? ((hashValue << 7) ^ ((int) buffer[inx]) ^ (hashValue >> 3))
					: (~((hashValue << 11) ^ ((int) buffer[inx]) ^ (hashValue >> 5)));
			}
			return hashValue;
		}
	}
}