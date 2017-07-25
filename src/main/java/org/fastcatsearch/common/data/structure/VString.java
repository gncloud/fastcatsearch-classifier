package org.fastcatsearch.common.data.structure;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;

import org.fastcatsearch.common.data.type.Clonable;
import org.fastcatsearch.common.data.type.Copyable;
import org.fastcatsearch.common.data.type.Streamable;

public class VString implements CharSequence, Comparable<CharSequence>, Comparator<CharSequence>, Streamable, Clonable<VString>, Copyable<VString>  {
	
	private char[] buffer;
	private int offset;
	private int length;
	private boolean fixedHash;
	private boolean ignoreCase;
	private int hashCode;
	
	public VString() {
		buffer = new char[0];
		fixedHash = false;
		ignoreCase = false;
		offset = 0;
		length = 0;
		hashCode = 0;
	}
	
	public VString(CharSequence str) {
		this();
		if(str instanceof VString) {
			VString vstr = (VString) str;
			buffer = vstr.buffer;
			offset = vstr.offset;
			length = vstr.length;
			hashCode = vstr.hashCode;
		} else {
			buffer = str.toString().toCharArray();
			length = str.length();
			generateHashCode();
		}
	}
	
	public VString(char[] buffer, int offset, int length) {
		this();
		this.buffer = buffer;
		this.offset = offset;
		this.length = length;
		generateHashCode();
	}
	
	public void setBuffer(char[] buffer, int offset, int length) throws IndexOutOfBoundsException {
		this.buffer = buffer;
		fixedHash = true;
		setRange(offset, length);
		fixedHash = false;
		generateHashCode();
	}
	
	public void setRange(int offset, int length) throws IndexOutOfBoundsException {
		fixedHash = true;
		setOffset(offset);
		setLength(length);
		fixedHash = false;
		generateHashCode();
	}
	
	public void setOffset(int offset) throws IndexOutOfBoundsException { 
		if(offset < 0) { throw new IndexOutOfBoundsException("Invalid offset : "+offset+""); }
		if(offset + length > buffer.length) { throw new IndexOutOfBoundsException("Offset overflows : "+(offset+length)+" / "+buffer.length+""); }
		this.offset = offset; 
		generateHashCode();
	}
	public void setLength(int length) throws IndexOutOfBoundsException { 
		if(length < 0) { throw new IndexOutOfBoundsException("Invalid length : "+length+""); }
		if(offset + length > buffer.length) { throw new IndexOutOfBoundsException("Offset overflows : "+(offset+length)+" / "+buffer.length+""); }
		this.length = length; 
		generateHashCode();
	}
	
	public void fixHash(boolean fixedHash) {
		this.fixedHash = fixedHash;
	}
	
	public void setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}

	@Override
	public int compare(CharSequence str1, CharSequence str2) {
		boolean ignoreCase = this.ignoreCase;
		if((str2 instanceof VString) && ((VString)str2).ignoreCase) { ignoreCase = true; }
		int len1 = str1.length();
		int len2 = str2.length();
		int len = len1;
		if(len2 < len1) { len = len2; }
		for(int inx=0;inx<len; inx++) {
			char c1 = str1.charAt(inx);
			char c2 = str2.charAt(inx);
			
			if(ignoreCase && c1 >='A' && c1 <='Z') { c1 += 32; }
			if(ignoreCase && c2 >='A' && c2 <='Z') { c2 += 32; }
			
			if(c1 > c2) {
				return 1;
			} else if(c1 < c2) {
				return -1;
			}
		}
		if(len1 > len2) {
			return 1;
		} else if(len1 < len2) {
			return -1;
		}
		return 0;
	}
	
	public int compare(char[] buffer1, int offset1, int length1, 
			char[] buffer2, int offset2, int length2) {
		int length = length1;
		if(length2 < length1) { length = length2; }
		for(int inx=0;inx<length; inx++) {
			char c1 = buffer1[offset1+inx];
			char c2 = buffer2[offset2+inx];
			
			if(ignoreCase && c1 >='A' && c1 <='Z') { c1 += 32; }
			if(ignoreCase && c2 >='A' && c2 <='Z') { c2 += 32; }
			
			if(c1 > c2) {
				return 1;
			} else if(c1 < c2) {
				return -1;
			}
		}
		if(length1 > length2) {
			return 1;
		} else if(length1 < length2) {
			return -1;
		}
		return 0;
	}

	@Override public int compareTo(CharSequence o) { return compare(this, o); }
	
	public int compareTo(char[] buffer, int offset, int length) {
		return compare(buffer(), offset(), length(), buffer, offset, length);
	}
	
	public char[] buffer() { return buffer; }
	
	public int offset() { return offset; }

	@Override public int length() { return length; }

	@Override public char charAt(int index) { return buffer[offset + index]; }

	@Override
	public CharSequence subSequence(int start, int end) {
		int length = end - start;
		VString ret = new VString();
		ret.buffer = buffer;
		ret.offset = offset + start;
		ret.length = length;
		return ret;
	}
	@Override
	public VString clone() {
		VString ret = new VString();
		ret.buffer = Arrays.copyOf(buffer, buffer.length);
		ret.offset = offset;
		ret.length = length;
		ret.hashCode = hashCode;
		ret.fixedHash = false;
		return ret;
	}
	@Override
	public int hashCode() {
		return hashCode;
	}
	private void generateHashCode() {
		if(!fixedHash) {
			String str = toString();
			if(str != null) {
				this.hashCode = str.hashCode();
			}
		}
	}
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof CharSequence) {
			if(compareTo((CharSequence)obj) == 0)  {
				return true;
			}
		}
		return false;
	}
	public boolean equals(char[] buf, int offset, int length) {
		if(compareTo(buf, offset, length) == 0) {
			return true;
		}
		return false;
	}
	@Override
	public String toString() {
		if(offset >= 0 && (offset+length) <= buffer.length) {
			return new String(buffer, offset, length);
		}
		return null;
	}
	
	public boolean startsWith(CharSequence str) {
		boolean ignoreCase = this.ignoreCase;
		if((str instanceof VString) && ((VString)str).ignoreCase) { ignoreCase = true; }
		int len = str.length();
		if(len > length()) {
			return false;
		}
		for(int inx=0;inx<len; inx++) {
			char c1 = this.charAt(inx);
			char c2 = str.charAt(inx);
			if(ignoreCase && c1 >='A' && c1 <='Z') { c1 += 32; }
			if(ignoreCase && c2 >='A' && c2 <='Z') { c2 += 32; }
			if(c1 != c2) {
				return false;
			}
		}
		return true;
	}
	
	public boolean endsWith(CharSequence str) {
		boolean ignoreCase = this.ignoreCase;
		if((str instanceof VString) && ((VString)str).ignoreCase) { ignoreCase = true; }
		int len = str.length();
		if(len > length()) {
			return false;
		}
		for(int inx=0;inx<len; inx++) {
			char c1 = this.charAt(length() - inx - 1);
			char c2 = str.charAt(len - inx - 1);
			if(ignoreCase && c1 >='A' && c1 <='Z') { c1 += 32; }
			if(ignoreCase && c2 >='A' && c2 <='Z') { c2 += 32; }
			if(c1 != c2) {
				return false;
			}
		}
		return true;
	}
	
	public VString wholeString() {
		VString ret = copy();
		ret.setOffset(0);
		ret.setLength(ret.buffer.length);
		return ret;
	}

	@Override
	public synchronized int readFrom(InputStream istream, byte[] buffer) throws IOException {
		int ret = 0;
		if (buffer == null || buffer.length < 100) { buffer = new byte[100]; }
		ret += istream.read(buffer, 0, ArrayUtility.BYTES_INTEGER * 4 + 1);
		int pos = 0;
		this.offset = ArrayUtility.restoreInteger(buffer, pos);
		this.length = ArrayUtility.restoreInteger(buffer, pos += ArrayUtility.BYTES_INTEGER);
		this.hashCode = ArrayUtility.restoreInteger(buffer, pos += ArrayUtility.BYTES_INTEGER);
		this.ignoreCase = buffer[pos += ArrayUtility.BYTES_INTEGER] == 0xff;
		int bufferLength = ArrayUtility.restoreInteger(buffer, pos ++);
		this.buffer = new char[bufferLength];
		ret += ArrayUtility.readInput(istream, buffer, this.buffer);
		//if(ret != streamLength()) { throw new IOException("CHECKLENGTH MISMATCHED"); }
		return ret;
	}

	@Override
	public synchronized int writeTo(OutputStream ostream, byte[] buffer) throws IOException {
		if (buffer == null || buffer.length < 100) { buffer = new byte[100]; }
		int pos = 0;
		ArrayUtility.mapInteger(this.offset, buffer, pos);
		ArrayUtility.mapInteger(this.length, buffer, pos += ArrayUtility.BYTES_INTEGER);
		ArrayUtility.mapInteger(this.hashCode, buffer, pos += ArrayUtility.BYTES_INTEGER);
		buffer[pos += ArrayUtility.BYTES_INTEGER] = (byte) (ignoreCase ? 0xff : 0);
		ArrayUtility.mapInteger(this.buffer.length, buffer, pos ++);
		ostream.write(buffer, 0, pos + ArrayUtility.BYTES_INTEGER);
		ArrayUtility.writeOutput(ostream, this.buffer, buffer);
		return streamLength();
	}

	@Override
	public int streamLength() {
		return ArrayUtility.BYTES_INTEGER * 4 + 1 + this.buffer.length * ArrayUtility.BYTES_CHAR;
	}

	@Override
	public VString copy() {
		return this.clone();
	}

	@Override
	public VString cloneOf() {
		VString ret = new VString();
		ret.buffer = this.buffer;
		ret.offset = this.offset;
		ret.length = this.length;
		ret.ignoreCase = this.ignoreCase;
		ret.hashCode = this.hashCode;
		return ret;
	}

	@Override
	public void cloneTo(VString instance) {
		instance.buffer = this.buffer;
		instance.offset = this.offset;
		instance.length = this.length;
		instance.ignoreCase = this.ignoreCase;
		instance.hashCode = this.hashCode;
	}
}