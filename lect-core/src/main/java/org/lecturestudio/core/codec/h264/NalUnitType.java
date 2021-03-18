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

package org.lecturestudio.core.codec.h264;

/**
 * Enumeration of all NAL unit types.
 *
 * @author Alex Andres
 */
public enum NalUnitType {

	RESERVED,

	/* Single NAL unit packet */
	CODE_SLICE_NON_IDR_PICTURE,
	CODE_SLICE_DATA_PARTITION_A,
	CODE_SLICE_DATA_PARTITION_B,
	CODE_SLICE_DATA_PARTITION_C,
	CODE_SLICE_IDR_PICTURE,
	SUPPLEMENTAL_ENHANCEMENT_INFORMATION,
	SEQUENCE_PARAMETER_SET,
	PICTURE_PARAMETER_SET,
	ACCESS_UNIT_DELIMITER,
	END_OF_SEQUENCE,
	END_OF_STREAM,
	FILTER_DATA,

	/* Single-time aggregation packet */
	STAP_A,
	STAP_B,

	/* Multi-time aggregation packet */
	MTAP16,
	MTAP24,

	/* Fragmentation unit */
	FU_A,
	FU_B,

	UNSPECIFIED;


	public static NalUnitType parse(int value) {
		switch (value) {
			case 1:
				return CODE_SLICE_NON_IDR_PICTURE;
			case 2:
				return CODE_SLICE_DATA_PARTITION_A;
			case 3:
				return CODE_SLICE_DATA_PARTITION_B;
			case 4:
				return CODE_SLICE_DATA_PARTITION_C;
			case 5:
				return CODE_SLICE_IDR_PICTURE;
			case 6:
				return SUPPLEMENTAL_ENHANCEMENT_INFORMATION;
			case 7:
				return SEQUENCE_PARAMETER_SET;
			case 8:
				return PICTURE_PARAMETER_SET;
			case 9:
				return ACCESS_UNIT_DELIMITER;
			case 10:
				return END_OF_SEQUENCE;
			case 11:
				return END_OF_STREAM;
			case 12:
				return FILTER_DATA;

			case 24:
				return STAP_A;
			case 25:
				return STAP_B;

			case 26:
				return MTAP16;
			case 27:
				return MTAP24;

			case 28:
				return FU_A;
			case 29:
				return FU_B;

			case 0:
			case 30:
			case 31:
				return RESERVED;

			default:
				return UNSPECIFIED;
		}
	}

}
