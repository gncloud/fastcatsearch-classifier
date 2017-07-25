package org.fastcatsearch.common.data.type;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Streamable {
	public int readFrom(InputStream istream, byte[] buffer) throws IOException;
	public int writeTo(OutputStream ostream, byte[] buffer) throws IOException;
	public int streamLength();
}
