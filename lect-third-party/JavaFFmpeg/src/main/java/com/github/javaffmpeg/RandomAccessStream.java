package com.github.javaffmpeg;

import java.io.IOException;

public interface RandomAccessStream {

	public long read(byte data[], int offset, int length) throws IOException;
	
	public long seek(long offset) throws IOException;
	
	public void reset() throws IOException;
	
}
