/*
 * Copyright (C) 2020 TU Darmstadt, Department of Computer Science,
 * Embedded Systems and Applications Group.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.lecturestudio.core.io;

import java.io.Closeable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigestRandomAccessFile implements DataOutput, DataInput, Closeable {

	private MessageDigest digest;

	private RandomAccessFile file;

	/**
	 *  Creates a new instance of {@link DigestRandomAccessFile} with a new {@link RandomAccessFile} as {@link #file}
	 *  and a new {@link MessageDigest} returned by {@link java.security.MessageDigest#getInstance(String)}
	 *  as {@link #digest}.
	 *
	 * @param name The system-dependent filename.
	 *                (used for {@link java.io.RandomAccessFile#RandomAccessFile(String, String)})
	 * @param mode The access mode.
	 *                (used for {@link java.io.RandomAccessFile#RandomAccessFile(String, String)})
	 * @param algorithm The name of the algorithm requested.
	 *                  (used for {@link java.security.MessageDigest#getInstance(String)})
	 */
	public DigestRandomAccessFile(String name, String mode, String algorithm)
			throws FileNotFoundException, NoSuchAlgorithmException {
		this.file = new RandomAccessFile(name, mode);
		this.digest = MessageDigest.getInstance(algorithm);
	}

	/**
	 * Creates a new instance of {@link DigestRandomAccessFile} with a new {@link RandomAccessFile} as {@link #file}
	 * and a new {@link MessageDigest} returned by {@link java.security.MessageDigest#getInstance(String)}
	 * as {@link #digest}.
	 *
	 * @param file The file object.
	 *                (used for {@link java.io.RandomAccessFile#RandomAccessFile(File, String)})
	 * @param mode The access mode.
	 *                (used for {@link java.io.RandomAccessFile#RandomAccessFile(File, String)})
	 * @param algorithm The name of the algorithm requested.
	 *                  (used for {@link java.security.MessageDigest#getInstance(String)})
	 */
	public DigestRandomAccessFile(File file, String mode, String algorithm)
			throws FileNotFoundException, NoSuchAlgorithmException {
		this.file = new RandomAccessFile(file, mode);
		this.digest = MessageDigest.getInstance(algorithm);
	}

	/**
	 * Calls {@link MessageDigest#digest()} on {@link #digest} and returns it.
	 *
	 * @see MessageDigest#digest()
	 */
	public byte[] getDigest() {
		return digest.digest();
	}

	@Override
	public void write(byte[] b) throws IOException {
		file.write(b);

		digest.update(b);
	}

	@Override
	public void write(int b) throws IOException {
		file.write(b);

		digest.update(getIntBytes(b));
	}

	@Override
	public void write(byte[] b, int offset, int length) throws IOException {
		file.write(b, offset, length);

		digest.update(b, offset, length);
	}

	@Override
	public void writeInt(int v) throws IOException {
		file.writeInt(v);

		digest.update(getIntBytes(v));
	}

	@Override
	public void writeBoolean(boolean v) throws IOException {
		file.writeBoolean(v);

		digest.update((byte) (v ? 1 : 0));
	}

	@Override
	public void writeByte(int v) throws IOException {
		file.writeByte(v);

		digest.update((byte) v);
	}

	@Override
	public void writeShort(int v) throws IOException {
		file.writeShort(v);

		digest.update(getShortBytes(v));
	}

	@Override
	public void writeChar(int v) throws IOException {
		file.writeChar(v);

		digest.update(getShortBytes(v));
	}

	@Override
	public void writeLong(long v) throws IOException {
		file.writeLong(v);

		digest.update(getLongBytes(v));
	}

	@Override
	public void writeFloat(float v) throws IOException {
		file.writeFloat(v);

		digest.update(getIntBytes(Float.floatToIntBits(v)));
	}

	@Override
	public void writeDouble(double v) throws IOException {
		file.writeDouble(v);

		digest.update(getLongBytes(Double.doubleToLongBits(v)));
	}

	@Override
	public void writeBytes(String s) throws IOException {
		file.writeBytes(s);

		digest.update(s.getBytes());
	}

	@Override
	public void writeChars(String s) throws IOException {
		file.writeChars(s);

		char[] c = s.toCharArray();
		byte[] b = new byte[c.length];
		for (int i = 0, j = 0; i < s.length(); i++) {
			b[j++] = (byte) (c[i] >>> 8);
			b[j++] = (byte) (c[i]);
		}

		digest.update(b);
	}

	@Override
	public void writeUTF(String s) throws IOException {
		file.writeUTF(s);

		digest.update(s.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public void readFully(byte[] b) throws IOException {
		file.readFully(b);
	}

	@Override
	public void readFully(byte[] b, int offset, int length) throws IOException {
		file.readFully(b, offset, length);
	}

	@Override
	public int skipBytes(int n) throws IOException {
		return file.skipBytes(n);
	}

	@Override
	public boolean readBoolean() throws IOException {
		return file.readBoolean();
	}

	@Override
	public byte readByte() throws IOException {
		return file.readByte();
	}

	@Override
	public int readUnsignedByte() throws IOException {
		return file.readUnsignedByte();
	}

	@Override
	public short readShort() throws IOException {
		return file.readShort();
	}

	@Override
	public int readUnsignedShort() throws IOException {
		return file.readUnsignedShort();
	}

	@Override
	public char readChar() throws IOException {
		return file.readChar();
	}

	@Override
	public int readInt() throws IOException {
		return file.readInt();
	}

	@Override
	public long readLong() throws IOException {
		return file.readLong();
	}

	@Override
	public float readFloat() throws IOException {
		return file.readFloat();
	}

	@Override
	public double readDouble() throws IOException {
		return file.readDouble();
	}

	@Override
	public String readLine() throws IOException {
		return file.readLine();
	}

	@Override
	public String readUTF() throws IOException {
		return file.readUTF();
	}

	@Override
	public void close() throws IOException {
		file.close();
	}

	public void seek(long pos) throws IOException {
		file.seek(pos);
	}

	public long length() throws IOException {
		return file.length();
	}

	private byte[] getShortBytes(int value) {
		byte[] b = new byte[2];
		b[0] = (byte) ((value >>> 8) & 0xFF);
		b[1] = (byte) ((value) & 0xFF);

		return b;
	}

	private byte[] getIntBytes(int value) {
		return BitConverter.getBigEndianBytes(value);
	}

	private byte[] getLongBytes(long v) {
		byte[] b = new byte[8];
		b[0] = (byte) ((int) (v >>> 56) & 0xFF);
		b[1] = (byte) ((int) (v >>> 48) & 0xFF);
		b[2] = (byte) ((int) (v >>> 40) & 0xFF);
		b[3] = (byte) ((int) (v >>> 32) & 0xFF);
		b[4] = (byte) ((int) (v >>> 24) & 0xFF);
		b[5] = (byte) ((int) (v >>> 16) & 0xFF);
		b[6] = (byte) ((int) (v >>> 8) & 0xFF);
		b[7] = (byte) ((int) (v) & 0xFF);

		return b;
	}

}
