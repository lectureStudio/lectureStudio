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

package org.lecturestudio.media.audio;

import org.lecturestudio.core.audio.AudioFormat;

public final class PcmCodecs {

	private PcmCodecs() { }


	public static PcmCodec getCodec(AudioFormat format) {
		switch (format.getEncoding()) {
			case S16LE:
				return Signed16BitIntLE;
			case S16BE:
				return Signed16BitIntBE;
			case S24LE:
				return Signed24BitIntLE;
			case S24BE:
				return Signed24BitIntBE;
			case S32LE:
				return Signed32BitIntLE;
			case S32BE:
				return Signed32BitIntBE;
			case FLOAT32LE:
				return Float32BitLE;
			case FLOAT32BE:
				return Float32BitBE;
		}

		throw new IllegalArgumentException("Unsupported audio format: " + format);
	}



	private static final PcmCodec Signed16BitIntBE = new PcmCodec() {

		@Override
		public void encode(double src, byte[] dst, int dstPos) {
			int buf = (int) (src * Short.MAX_VALUE);
			dst[dstPos] = (byte) (buf >> 8 & 255);
			dst[dstPos + 1] = (byte) (buf & 255);
		}

		@Override
		public double decode(byte[] src, int srcPos) {
			return (double) (src[srcPos] << 8 |
					(src[srcPos + 1] & 255)) / (double) Short.MAX_VALUE;
		}
	};



	private static final PcmCodec Signed16BitIntLE = new PcmCodec() {

		@Override
		public void encode(double src, byte[] dst, int dstPos) {
			int buf = (int) (src * Short.MAX_VALUE);
			dst[dstPos] = (byte) (buf & 255);
			dst[dstPos + 1] = (byte) (buf >> 8 & 255);
		}

		@Override
		public double decode(byte[] src, int srcPos) {
			return (double) ((src[srcPos] & 255) |
					src[srcPos + 1] << 8) / (double) Short.MAX_VALUE;
		}
	};



	private static final PcmCodec Signed24BitIntBE = new PcmCodec() {

		@Override
		public void encode(double src, byte[] dst, int dstPos) {
			int buf = (int) (src * 8388607.0D);
			dst[dstPos] = (byte) (buf >> 16 & 255);
			dst[dstPos + 1] = (byte) (buf >> 8 & 255);
			dst[dstPos + 2] = (byte) (buf & 255);
		}

		@Override
		public double decode(byte[] src, int srcPos) {
			return (double) (src[srcPos] << 16 |
					(src[srcPos + 1] & 255) << 8 |
					(src[srcPos + 2] & 255)) / 8388607.0D;
		}
	};



	private static final PcmCodec Signed24BitIntLE = new PcmCodec() {

		@Override
		public void encode(double src, byte[] dst, int dstPos) {
			int buf = (int) (src * 8388607.0D);
			dst[dstPos] = (byte) (buf & 255);
			dst[dstPos + 1] = (byte) (buf >> 8 & 255);
			dst[dstPos + 2] = (byte) (buf >> 16 & 255);
		}

		@Override
		public double decode(byte[] src, int srcPos) {
			return (double) ((src[srcPos] & 255) |
					(src[srcPos + 1] & 255) << 8 |
					src[srcPos + 2] << 16) / 8388607.0D;
		}
	};



	private static final PcmCodec Signed32BitIntBE = new PcmCodec() {

		@Override
		public void encode(double src, byte[] dst, int dstPos) {
			int buf = (int) (src * Integer.MAX_VALUE);
			dst[dstPos] = (byte) (buf >> 24 & 255);
			dst[dstPos + 1] = (byte) (buf >> 16 & 255);
			dst[dstPos + 2] = (byte) (buf >> 8 & 255);
			dst[dstPos + 3] = (byte) (buf & 255);
		}

		@Override
		public double decode(byte[] src, int srcPos) {
			return (double) (src[srcPos] << 24 |
					(src[srcPos + 1] & 255) << 16 |
					(src[srcPos + 2] & 255) << 8 |
					(src[srcPos + 3] & 255)) / (double) Integer.MAX_VALUE;
		}
	};



	private static final PcmCodec Signed32BitIntLE = new PcmCodec() {

		@Override
		public void encode(double src, byte[] dst, int dstPos) {
			int buf = (int) (src * Integer.MAX_VALUE);
			dst[dstPos] = (byte) (buf & 255);
			dst[dstPos + 1] = (byte) (buf >> 8 & 255);
			dst[dstPos + 2] = (byte) (buf >> 16 & 255);
			dst[dstPos + 3] = (byte) (buf >> 24 & 255);
		}

		@Override
		public double decode(byte[] src, int srcPos) {
			return (double) ((src[srcPos] & 255) |
					(src[srcPos + 1] & 255) << 8 |
					(src[srcPos + 2] & 255) << 16 |
					src[srcPos + 3] << 24) / (double) Integer.MAX_VALUE;
		}
	};



	private static final PcmCodec Float32BitBE = new PcmCodec() {

		@Override
		public void encode(double src, byte[] dst, int dstPos) {
			int buf = Float.floatToIntBits((float) src);
			dst[dstPos] = (byte) (buf >> 24 & 255);
			dst[dstPos + 1] = (byte) (buf >> 16 & 255);
			dst[dstPos + 2] = (byte) (buf >> 8 & 255);
			dst[dstPos + 3] = (byte) (buf & 255);
		}

		@Override
		public double decode(byte[] src, int srcPos) {
			return Float.intBitsToFloat((src[srcPos] & 255) << 24 |
					(src[srcPos + 1] & 255) << 16 |
					(src[srcPos + 2] & 255) << 8 |
					(src[srcPos + 3] & 255));
		}
	};



	private static final PcmCodec Float32BitLE = new PcmCodec() {

		@Override
		public void encode(double src, byte[] dst, int dstPos) {
			int buf = Float.floatToIntBits((float) src);
			dst[dstPos] = (byte) (buf & 255);
			dst[dstPos + 1] = (byte) (buf >> 8 & 255);
			dst[dstPos + 2] = (byte) (buf >> 16 & 255);
			dst[dstPos + 3] = (byte) (buf >> 24 & 255);
		}

		@Override
		public double decode(byte[] src, int srcPos) {
			return Float.intBitsToFloat((src[srcPos] & 255) |
					(src[srcPos + 1] & 255) << 8 |
					(src[srcPos + 2] & 255) << 16 |
					(src[srcPos + 3] & 255) << 24);
		}
	};



	private static final PcmCodec Float64BitBE = new PcmCodec() {

		@Override
		public void encode(double src, byte[] dst, int dstPos) {
			long buf = Double.doubleToLongBits(src);
			int lo = (int) buf;
			int hi = (int) (buf >> 32);

			dst[dstPos] = (byte) (hi >> 24 & 255);
			dst[dstPos + 1] = (byte) (hi >> 16 & 255);
			dst[dstPos + 2] = (byte) (hi >> 8 & 255);
			dst[dstPos + 3] = (byte) (hi & 255);
			dst[dstPos + 4] = (byte) (lo >> 24 & 255);
			dst[dstPos + 5] = (byte) (lo >> 16 & 255);
			dst[dstPos + 6] = (byte) (lo >> 8 & 255);
			dst[dstPos + 7] = (byte) (lo & 255);
		}

		@Override
		public double decode(byte[] src, int srcPos) {
			int hi = (src[srcPos] & 255) << 24 |
					(src[srcPos + 1] & 255) << 16 |
					(src[srcPos + 2] & 255) << 8 |
					(src[srcPos + 3] & 255);

			int lo = (src[srcPos + 4] & 255) << 24 |
					(src[srcPos + 5] & 255) << 16 |
					(src[srcPos + 6] & 255) << 8 |
					(src[srcPos + 7] & 255);

			return Double.longBitsToDouble((long) lo | (long) hi << 32);
		}
	};



	private static final PcmCodec Float64BitLE = new PcmCodec() {

		@Override
		public void encode(double src, byte[] dst, int dstPos) {
			long buf = Double.doubleToLongBits(src);
			int lo = (int) buf;
			int hi = (int) (buf >> 32);

			dst[dstPos] = (byte) (lo & 255);
			dst[dstPos + 1] = (byte) (lo >> 8 & 255);
			dst[dstPos + 2] = (byte) (lo >> 16 & 255);
			dst[dstPos + 3] = (byte) (lo >> 24 & 255);
			dst[dstPos + 4] = (byte) (hi & 255);
			dst[dstPos + 5] = (byte) (hi >> 8 & 255);
			dst[dstPos + 6] = (byte) (hi >> 16 & 255);
			dst[dstPos + 7] = (byte) (hi >> 24 & 255);
		}

		@Override
		public double decode(byte[] src, int srcPos) {
			int lo = (src[srcPos] & 255) |
					(src[srcPos + 1] & 255) << 8 |
					(src[srcPos + 2] & 255) << 16 |
					(src[srcPos + 3] & 255) << 24;

			int hi = (src[srcPos + 4] & 255) |
					(src[srcPos + 5] & 255) << 8 |
					(src[srcPos + 6] & 255) << 16 |
					(src[srcPos + 7] & 255) << 24;

			return Double.longBitsToDouble((long) lo | (long) hi << 32);
		}
	};
}
