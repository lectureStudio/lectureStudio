package com.github.javaffmpeg;

import java.io.IOException;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.avformat.Read_packet_Pointer_BytePointer_int;
import org.bytedeco.javacpp.avformat.Seek_Pointer_long_int;

public class AVInputStream extends Pointer {

	public static int AVERROR_EOF = -0x5fb9b0bb;
	
	private RandomAccessStream stream;
	
	private byte[] buffer;
	
	Read_packet_Pointer_BytePointer_int readFunc;
	
	Seek_Pointer_long_int seekFunc;
	
	
	public AVInputStream(RandomAccessStream stream) {
		this.stream = stream;
		this.buffer = new byte[32 * 1024];
		
		this.readFunc = new ReadFunc();
		this.seekFunc = new SeekFunc();
	}
	
	
	private class ReadFunc extends Read_packet_Pointer_BytePointer_int {

		@Override
		public int call(Pointer opaque, BytePointer outBuffer, int bufferSize) {
			long read = 0;
			
			try {
				read = stream.read(buffer, 0, bufferSize);
				
				if (read > 0) {
					outBuffer.put(buffer, 0, bufferSize).position(0);
				}
			}
			catch (IOException e) {
				e.printStackTrace();
				return -1;
			}
			
//			System.out.println("read: " + bufferSize + " / " + read);
			
//			if (read == 0)
//				return AVERROR_EOF;
			
			return (int) Math.min(read, bufferSize);
		}
	}
	
	private class SeekFunc extends Seek_Pointer_long_int {
		
		@Override
		public long call(Pointer opaque, long offset, int whence) {
			//System.out.println("seek: " + offset + " / " + whence);
			
			long skipped = 0;
			
			try {
				skipped = stream.seek(offset);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			
			return skipped;
		}
	}
	
}
