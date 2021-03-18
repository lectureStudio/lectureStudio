package com.github.javaffmpeg;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class RandomAccessFileStream implements RandomAccessStream {

	private final RandomAccessFile raFile;
	
	
	public RandomAccessFileStream(String filePath, String mode) throws FileNotFoundException {
		this.raFile = new RandomAccessFile(filePath, mode);
	}
	
	@Override
	public synchronized long read(byte[] data, int offset, int length) throws IOException {
		return raFile.read(data, offset, length);
	}

	@Override
	public synchronized long seek(long offset) throws IOException {
		raFile.seek(offset);
		return offset;
	}

	@Override
	public synchronized void reset() throws IOException {
		raFile.seek(0);
	}

}
