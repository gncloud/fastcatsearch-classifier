package org.fastcatsearch.common.data.structure;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.fastcatsearch.common.data.type.Streamable;

public class ArrayUtility {
	public enum Types {
		BYTE,
		CHARACTER,
		INTEGER,
		FLOAT,
		LONG,
		DOUBLE
	}
	
	public static final int BYTES_CHAR = 2;
	public static final int BYTES_INTEGER = 4;
	public static final int BYTES_FLOAT = 4;
	public static final int BYTES_LONG = 8;
	public static final int BYTES_DOUBLE = 8;
	public static final int CHARS_INTEGER = 2;
	public static final int CHARS_LONG = 4;
	public static final int CHARS_FLOAT = 2;
	public static final int CHARS_DOUBLE = 4;
	//char
	public static int mapChar(char value, byte[] buf, int offset) {
		return mapChar(value, buf, offset, BYTES_CHAR);
	}
	public static int mapChar(char value, byte[] buf, int offset, int length) {
		int inx;
		for (inx = 0; inx < length && inx < BYTES_CHAR; inx++) {
			buf[offset + inx] = (byte) (value >> (inx << 3) & 0xff);
		}
		return inx;
	}
	public static char restoreChar(byte[] buf, int offset) {
		return restoreChar(buf, offset, BYTES_CHAR);
	}
	public static char restoreChar(byte[] buf, int offset, int length) {
		int ret = 0;
		int inx = 0;
		for (inx = 0; inx < length && inx < BYTES_CHAR; inx++) {
			ret = ret | ((int) (buf[offset + inx] & 0xff)) << (inx << 3);
		}
		return (char)ret;
	}
	//int
	public static int mapInteger(int value, byte[] buf, int offset) {
		return mapInteger(value, buf, offset, BYTES_INTEGER);
	}
	public static int mapInteger(int value, byte[] buf, int offset, int length) {
		int inx;
//if(value != 0) {
//System.out.println("MAP : "+value);
//}
		for (inx = 0; inx < length && inx < BYTES_INTEGER; inx++) {
			buf[offset + inx] = (byte) ((value >> (inx << 3)) & 0xff);
//if(value != 0) {
//System.out.println("BUF["+(offset+inx)+"] = "+(byte) ((value >> (inx << 3)) & 0xff));
//}
		}
		return inx;
	}
	public static int mapIntegerToChar(int value, char[] buf, int offset) {
		return mapIntegerToChar(value, buf, offset, CHARS_INTEGER);
	}
	public static int mapIntegerToChar(int value, char[] buf, int offset, int length) {
		int inx = 0;
		for (inx = 0; inx < length && inx < CHARS_INTEGER; inx++) {
			buf[offset + inx] = (char) (value >> (inx << 4) & 0xffff);
//System.out.println("INX:"+inx+" / LEN:"+length+" / CHR:"+CHARS_INTEGER+" / OFF:"+offset+" / BUF:"+((int)buf[offset+inx])+"!");
		}
		return inx;
	}
	public static int restoreInteger(byte[] buf, int offset) {
		return restoreInteger(buf, offset, BYTES_INTEGER);
	}
	public static int restoreInteger(byte[] buf, int offset, int length) {
		int ret = 0;
		int inx = 0;
		for (inx = 0; inx < length && inx < BYTES_INTEGER; inx++) {
			ret = ret | ((int) (buf[offset + inx] & 0xff)) << (inx << 3);
		}
		return ret;
	}
	public static int restoreIntegerFromChar(char[] buf, int offset) {
		return restoreIntegerFromChar(buf, offset, CHARS_INTEGER);
	}
	public static int restoreIntegerFromChar(char[] buf, int offset, int length) {
		int ret = 0;
		int inx = 0;
//System.out.println("LEN:"+length+" / CHR:"+CHARS_INTEGER);
		for (inx = 0; inx < length && inx < CHARS_INTEGER; inx++) {
			ret = ret | (((int) (buf[offset + inx] & 0xffff)) << (inx << 4));
//System.out.println("INX:"+inx+" / LEN:"+length+" / CHR:"+CHARS_INTEGER+" / OFF:"+offset+" / BUF:"+((int)buf[offset+inx])+"!");
		}
		return ret;
	}
	//float
	public static int mapFloat(float value, byte[] buf, int offset) {
		return mapFloat(value, buf, offset, BYTES_FLOAT);
	}
	public static int mapFloat(float fvalue, byte[] buf, int offset, int length) {
		int value = Float.floatToIntBits(fvalue);
		int inx;
		for (inx = 0; inx < length && inx < BYTES_FLOAT; inx++) {
			buf[offset + inx] = (byte) (value >> (inx << 3) & 0xff);
		}
		return inx;
	}
	public static float restoreFloat(byte[] buf, int offset) {
		return restoreFloat(buf, offset, BYTES_FLOAT);
	}
	public static float restoreFloat(byte[] buf, int offset, int length) {
		int ret = 0;
		int inx = 0;
		for (inx = 0; inx < length && inx < BYTES_FLOAT; inx++) {
			ret = ret | ((int) (buf[offset + inx] & 0xff)) << (inx << 3);
		}
		return Float.intBitsToFloat(ret);
	}
	//long
	public static int mapLong(long value, byte[] buf, int offset) {
		return mapLong(value, buf, offset, BYTES_LONG);
	}
	public static int mapLong(long value, byte[] buf, int offset, int length) {
		int inx;
		for (inx = 0; inx < length && inx < BYTES_LONG; inx++) {
			buf[offset + inx] = (byte) (value >> (inx << 3) & 0xff);
		}
		return inx;
	}
	public static int mapLongToChar(long value, char[] buf, int offset) {
		return mapLongToChar(value, buf, offset, CHARS_LONG);
	}
	public static int mapLongToChar(long value, char[] buf, int offset, int length) {
		int inx;
		for (inx = 0; inx < length && inx < CHARS_LONG; inx++) {
			buf[offset + inx] = (char) (value >> (inx << 4) & 0xffff);
		}
		return inx;
	}
	public static long restoreLong(byte[] buf, int offset) {
		return restoreLong(buf, offset, BYTES_LONG);
	}
	public static long restoreLong(byte[] buf, int offset, int length) {
		long ret = 0;
		int inx = 0;
		for (inx = 0; inx < length && inx < BYTES_LONG; inx++) {
			ret = ret | ((long) (buf[offset + inx] & 0xff)) << (inx << 3);
		}
		return ret;
	}
	public static int restoreLongFromChar(char[] buf, int offset) {
		return restoreIntegerFromChar(buf, offset, CHARS_LONG);
	}
	public static int restoreLongFromChar(char[] buf, int offset, int length) {
		int ret = 0;
		int inx = 0;
		for (inx = 0; inx < length && inx < CHARS_LONG; inx++) {
			ret = ret | ((int) (buf[offset + inx] & 0xffff)) << (inx << 4);
		}
		return ret;
	}
	//double
	public static int mapDouble(double value, byte[] buf, int offset) {
		return mapDouble(value, buf, offset, BYTES_DOUBLE);
	}
	public static int mapDouble(double fvalue, byte[] buf, int offset, int length) {
		long value = Double.doubleToLongBits(fvalue);
		int inx;
		for (inx = 0; inx < length && inx < BYTES_DOUBLE; inx++) {
			buf[offset + inx] = (byte) (value >> (inx << 3) & 0xff);
		}
		return inx;
	}
	public static double restoreDouble(byte[] buf, int offset) {
		return restoreDouble(buf, offset, BYTES_DOUBLE);
	}
	public static double restoreDouble(byte[] buf, int offset, int length) {
		long ret = 0;
		int inx = 0;
		for (inx = 0; inx < length && inx < BYTES_DOUBLE; inx++) {
			ret = ret | ((long) (buf[offset + inx] & 0xff)) << (inx << 3);
		}
		return Double.longBitsToDouble(ret);
	}
	public static int toIntBuffer(byte[] source, int sOffset, int[] target, int tOffset, int sLength) {
		int tinx = 0;
		for (int sinx = 0; sinx < sLength; sinx += BYTES_INTEGER, tinx++) {
			int spos = sOffset + sinx;
			target[tOffset + tinx] = 
				((int) source[spos] & 0xff)
				| ((source.length > ++spos) ? 
					(((int) source[spos] & 0xff) << 8) : 0)
				| ((source.length > ++spos) ? 
					(((int) source[spos] & 0xff) << 16) : 0)
				| ((source.length > ++spos) ? 
					(((int) source[spos] & 0xff) << 24) : 0);
		}
		return tinx;
	}
	public static void fromIntBuffer(int[] source, int sOffset, byte[] target, int tOffset, int tLength) {
		int sinx = 0;
		for (int tinx = 0; tinx < tLength; tinx += BYTES_INTEGER, sinx++) {
			int tpos = tOffset + tinx;
			int sValue = source[sOffset + sinx];
			target[tpos] = (byte) (sValue & 0xff);
			if(target.length > ++tpos) {
				target[tpos] = (byte) (sValue >> 8 & 0xff);
			}
			if(target.length > ++tpos) {
				target[tpos] = (byte) (sValue >> 16 & 0xff);
			}
			if(target.length > ++tpos) {
				target[tpos] = (byte) (sValue >> 24 & 0xff);
			}
		}
	}
	public static int toIntBuffer(Streamable data, int[] target, int offset, byte[] buffer) {
		int ret = 0;
		ByteArrayOutputStream ostream = null;
		try {
			ostream = new ByteArrayOutputStream();
			data.writeTo(ostream, buffer);
			byte[] source = ostream.toByteArray();
			ret = toIntBuffer(source, 0, target, offset, source.length);
		} catch (IOException ignore) {
		} finally {
			if (ostream != null) try { ostream.close(); } catch (Exception ignore) { }
		}
		return ret;
	}
	public static void fromIntBuffer(int[] source, int offset, int length, Streamable data, byte[] buffer) {
		byte[] target = new byte[length];
		ByteArrayInputStream istream = null;
		try {
			fromIntBuffer(source, offset, target, 0, length);
			istream = new ByteArrayInputStream(target);
			data.readFrom(istream, buffer);
		} catch (IOException ignore) { 
		} finally {
			if (istream != null) try { istream.close(); } catch (Exception ignore) { }
		}
	}
	public static void writeOutput(OutputStream ostream, byte[] sbuffer, byte[] tbuffer) throws IOException {
		if(sbuffer != null) {
			int base = 0;
			int pos = 0;
			int chunkLength = tbuffer.length;
			for (long available = sbuffer.length; available > 0; available -= chunkLength) {
				if(available < chunkLength) { chunkLength = (int) available; }
				pos = 0;
				int minx = 0;
				for (; minx < chunkLength; minx++) {
					tbuffer[pos] = (byte) sbuffer[base + minx];
					pos ++;
				}
				base += minx;
				ostream.write(tbuffer, 0, pos);
				if(available == chunkLength) { break; }
			}
		}
	}
	public static void writeOutput(OutputStream ostream, char[] sbuffer, byte[] tbuffer) throws IOException {
		if(sbuffer != null) {
			int base = 0;
			int pos = 0;
			int chunkLength = tbuffer.length / BYTES_CHAR;
			for (long available = sbuffer.length; available > 0; available -= chunkLength) {
				if(available < chunkLength) { chunkLength = (int) available; }
				pos = 0;
				int minx = 0;
				for (; minx < chunkLength; minx++) {
					ArrayUtility.mapChar(sbuffer[base + minx], tbuffer, pos);
					pos += BYTES_CHAR;
				}
				base += minx;
				ostream.write(tbuffer, 0, pos);
				if(available == chunkLength) { break; }
			}
		}
	}
	public static void writeOutput(OutputStream ostream, int[] sbuffer, byte[] tbuffer) throws IOException {
		if(sbuffer != null) {
			int base = 0;
			int pos = 0;
			int chunkLength = tbuffer.length / BYTES_INTEGER;
			for (long available = sbuffer.length; available > 0; available -= chunkLength) {
//System.out.println("AVAIL:"+available+" / "+chunkLength);
				if(available < chunkLength) { chunkLength = (int) available; }
				pos = 0;
				int minx = 0;
				for (; minx < chunkLength; minx++) {
					ArrayUtility.mapInteger(sbuffer[base + minx], tbuffer, pos);
//if(sbuffer[base+minx] != 0) {
////System.out.println("SBUF["+(base+minx)+"] = "+sbuffer[base+minx]+"");
//for(int inx=0;inx<ArrayUtility.BYTES_INTEGER;inx++) {
//	System.out.println("TBF["+(pos+inx)+"]="+tbuffer[pos+inx]);
//}
//}
					pos += BYTES_INTEGER;
				}
				base += minx;
				ostream.write(tbuffer, 0, pos);
				if(available == chunkLength) { break; }
			}
		}
	}
	public static void writeOutput(OutputStream ostream, float[] sbuffer, byte[] tbuffer) throws IOException {
		if(sbuffer != null) {
			int base = 0;
			int pos = 0;
			int chunkLength = tbuffer.length / BYTES_FLOAT;
			for (long available = sbuffer.length; available > 0; available -= chunkLength) {
				if(available < chunkLength) { chunkLength = (int) available; }
				pos = 0;
				int minx = 0;
				for (; minx < chunkLength; minx++) {
					ArrayUtility.mapFloat(sbuffer[base + minx], tbuffer, pos);
					pos += BYTES_FLOAT;
				}
				base += minx;
				ostream.write(tbuffer, 0, pos);
				if(available == chunkLength) { break; }
			}
		}
	}
	public static void writeOutput(OutputStream ostream, long[] sbuffer, byte[] tbuffer) throws IOException {
		if(sbuffer != null) {
			int base = 0;
			int pos = 0;
			int chunkLength = tbuffer.length / BYTES_LONG;
			for (long available = sbuffer.length; available > 0; available -= chunkLength) {
				if(available < chunkLength) { chunkLength = (int) available; }
				pos = 0;
				int minx = 0;
				for (; minx < chunkLength; minx++) {
					ArrayUtility.mapLong(sbuffer[base + minx], tbuffer, pos);
					pos += BYTES_LONG;
				}
				base += minx;
				ostream.write(tbuffer, 0, pos);
				if(available == chunkLength) { break; }
			}
		}
	}
	public static void writeOutput(OutputStream ostream, double[] sbuffer, byte[] tbuffer) throws IOException {
		if(sbuffer != null) {
			int base = 0;
			int pos = 0;
			int chunkLength = tbuffer.length / BYTES_DOUBLE;
			for (long available = sbuffer.length; available > 0; available -= chunkLength) {
				if(available < chunkLength) { chunkLength = (int) available; }
				pos = 0;
				int minx = 0;
				for (; minx < chunkLength; minx++) {
					ArrayUtility.mapDouble(sbuffer[base + minx], tbuffer, pos);
					pos += BYTES_DOUBLE;
				}
				base += minx;
				ostream.write(tbuffer, 0, pos);
				if(available == chunkLength) { break; }
			}
		}
	}
	public static int readInput(InputStream istream, byte[] sbuffer, byte[] tbuffer) throws IOException {
		int ret = 0;
		if (sbuffer != null) {
			int chunkSize = sbuffer.length;
			int rlen = 0;
			int pos = 0;
			int wOffset = 0;
			for (long available = tbuffer.length ; available > 0; available -= chunkSize) {
				if(available < chunkSize) { chunkSize = (int)available; }
				rlen = istream.read(sbuffer, 0, chunkSize);
				ret += rlen;
				pos = 0;
				for (int minx = 0; minx < rlen; minx ++) {
					tbuffer[wOffset] = sbuffer[pos];
					pos ++;
					wOffset++;
				}
				if(available == chunkSize) { break; }
			}
		}
		return ret;
	}
	public static int readInput(InputStream istream, byte[] sbuffer, char[] tbuffer) throws IOException {
		int ret = 0;
		if (sbuffer != null) {
			int chunkSize = (sbuffer.length / ArrayUtility.BYTES_CHAR) * ArrayUtility.BYTES_CHAR;
			int rlen = 0;
			int pos = 0;
			int wOffset = 0;
			for (long available = tbuffer.length * ArrayUtility.BYTES_CHAR; available > 0; available -= chunkSize) {
				if(available < chunkSize) { chunkSize = (int)available; }
				rlen = istream.read(sbuffer, 0, chunkSize);
				ret += rlen;
				pos = 0;
				for (int minx = 0; minx < rlen; minx += ArrayUtility.BYTES_CHAR) {
					tbuffer[wOffset] = ArrayUtility.restoreChar(sbuffer, pos);
					pos += ArrayUtility.BYTES_CHAR;
					wOffset++;
				}
				if(available == chunkSize) { break; }
			}
		}
		return ret;
	}
	public static int readInput(InputStream istream, byte[] sbuffer, int[] tbuffer) throws IOException {
		int ret = 0;
		if (sbuffer != null) {
			int chunkSize = (sbuffer.length / ArrayUtility.BYTES_INTEGER) * ArrayUtility.BYTES_INTEGER;
			int rlen = 0;
			int pos = 0;
			int wOffset = 0;
//StringBuilder sb = new StringBuilder();
//int readed = 0;
			for (long available = tbuffer.length * ArrayUtility.BYTES_INTEGER; available > 0; available -= chunkSize) {
				if(available < chunkSize) { chunkSize = (int)available; }
				rlen = istream.read(sbuffer, 0, chunkSize);
				ret += rlen;
				pos = 0;
				for (int minx = 0; minx < rlen; minx += ArrayUtility.BYTES_INTEGER) {
					tbuffer[wOffset] = ArrayUtility.restoreInteger(sbuffer, pos);
//for(int inx=0;inx<4;inx++) { if(sbuffer[pos+inx] != 0) { sb.append("["+(pos+inx)+"]="+sbuffer[pos+inx]); } }
//if(tbuffer[wOffset] != 0) { sb.append("["+wOffset+"/"+pos+"]="+tbuffer[wOffset]); }
//readed++;
					pos += ArrayUtility.BYTES_INTEGER;
					wOffset++;
				}
				if(available == chunkSize) { break; }
			}
//System.out.println("READED:["+readed+"]"+sb);
		}
		return ret;
	}
	public static int readInput(InputStream istream, byte[] sbuffer, float[] tbuffer) throws IOException {
		int ret = 0;
		if (sbuffer != null) {
			int chunkSize = (sbuffer.length / ArrayUtility.BYTES_FLOAT) * ArrayUtility.BYTES_FLOAT;
			int rlen = 0;
			int pos = 0;
			int wOffset = 0;
			for (long available = tbuffer.length * ArrayUtility.BYTES_FLOAT; available > 0; available -= chunkSize) {
				if(available < chunkSize) { chunkSize = (int)available; }
				rlen = istream.read(sbuffer, 0, chunkSize);
				ret += rlen;
				pos = 0;
				for (int minx = 0; minx < rlen; minx += ArrayUtility.BYTES_FLOAT) {
					tbuffer[wOffset] = ArrayUtility.restoreFloat(sbuffer, pos);
					pos += ArrayUtility.BYTES_FLOAT;
					wOffset++;
				}
				if(available == chunkSize) { break; }
			}
		}
		return ret;
	}
	public static int readInput(InputStream istream, byte[] sbuffer, long[] tbuffer) throws IOException {
		int ret = 0;
		if (sbuffer != null) {
			int chunkSize = (sbuffer.length / ArrayUtility.BYTES_LONG) * ArrayUtility.BYTES_LONG;
			int rlen = 0;
			int pos = 0;
			int wOffset = 0;
			for (long available = tbuffer.length * ArrayUtility.BYTES_LONG; available > 0; available -= chunkSize) {
				if(available < chunkSize) { chunkSize = (int)available; }
				rlen = istream.read(sbuffer, 0, chunkSize);
				ret += rlen;
				pos = 0;
				for (int minx = 0; minx < rlen; minx += ArrayUtility.BYTES_LONG) {
					tbuffer[wOffset] = ArrayUtility.restoreLong(sbuffer, pos);
					pos += ArrayUtility.BYTES_LONG;
					wOffset++;
				}
				if(available == chunkSize) { break; }
			}
		}
		return ret;
	}
	public static int readInput(InputStream istream, byte[] sbuffer, double[] tbuffer) throws IOException {
		int ret = 0;
		if (sbuffer != null) {
			int chunkSize = (sbuffer.length / ArrayUtility.BYTES_DOUBLE) * ArrayUtility.BYTES_DOUBLE;
			int rlen = 0;
			int pos = 0;
			int wOffset = 0;
			for (long available = tbuffer.length * ArrayUtility.BYTES_DOUBLE; available > 0; available -= chunkSize) {
				if(available < chunkSize) { chunkSize = (int)available; }
				rlen = istream.read(sbuffer, 0, chunkSize);
				ret += rlen;
				pos = 0;
				for (int minx = 0; minx < rlen; minx += ArrayUtility.BYTES_DOUBLE) {
					tbuffer[wOffset] = ArrayUtility.restoreDouble(sbuffer, pos);
					pos += ArrayUtility.BYTES_DOUBLE;
					wOffset++;
				}
				if(available == chunkSize) { break; }
			}
		}
		return ret;
	}
	
	public static int growup(int num) {
		if(num == 0) {
			return num = 10;
		} else if(num < 10) {
			return num * 2;
		} else if(num < 100) {
			return (int)(num * 1.5);
		} else if(num < 1000) {
			return (int)(num * 1.2);
		} else {
			return (int)(num * 1.05);
		}
	}
}