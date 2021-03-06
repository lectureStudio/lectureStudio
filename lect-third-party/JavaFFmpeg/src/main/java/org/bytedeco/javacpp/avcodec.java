// Targeted by JavaCPP version 1.1

package org.bytedeco.javacpp;

import java.nio.*;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

import static org.bytedeco.javacpp.avutil.*;
import static org.bytedeco.javacpp.swresample.*;

public class avcodec extends org.bytedeco.javacpp.presets.avcodec {
    static { Loader.load(); }

// Parsed from <libavcodec/avcodec.h>

/*
 * copyright (c) 2001 Fabrice Bellard
 *
 * This file is part of FFmpeg.
 *
 * FFmpeg is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * FFmpeg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with FFmpeg; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

// #ifndef AVCODEC_AVCODEC_H
// #define AVCODEC_AVCODEC_H

/**
 * @file
 * @ingroup libavc
 * Libavcodec external API header
 */

// #include <errno.h>
// #include "libavutil/samplefmt.h"
// #include "libavutil/attributes.h"
// #include "libavutil/avutil.h"
// #include "libavutil/buffer.h"
// #include "libavutil/cpu.h"
// #include "libavutil/channel_layout.h"
// #include "libavutil/dict.h"
// #include "libavutil/frame.h"
// #include "libavutil/log.h"
// #include "libavutil/pixfmt.h"
// #include "libavutil/rational.h"

// #include "version.h"

/**
 * @defgroup libavc Encoding/Decoding Library
 * @{
 *
 * @defgroup lavc_decoding Decoding
 * @{
 * @}
 *
 * @defgroup lavc_encoding Encoding
 * @{
 * @}
 *
 * @defgroup lavc_codec Codecs
 * @{
 * @defgroup lavc_codec_native Native Codecs
 * @{
 * @}
 * @defgroup lavc_codec_wrappers External library wrappers
 * @{
 * @}
 * @defgroup lavc_codec_hwaccel Hardware Accelerators bridge
 * @{
 * @}
 * @}
 * @defgroup lavc_internal Internal
 * @{
 * @}
 * @}
 *
 */

/**
 * @defgroup lavc_core Core functions/structures.
 * @ingroup libavc
 *
 * Basic definitions, functions for querying libavcodec capabilities,
 * allocating core structures, etc.
 * @{
 */


/**
 * Identify the syntax and semantics of the bitstream.
 * The principle is roughly:
 * Two decoders with the same ID can decode the same streams.
 * Two encoders with the same ID can encode compatible streams.
 * There may be slight deviations from the principle due to implementation
 * details.
 *
 * If you add a codec ID to this list, add it so that
 * 1. no value of a existing codec ID changes (that would break ABI),
 * 2. it is as close as possible to similar codecs
 *
 * After adding new codec IDs, do not forget to add an entry to the codec
 * descriptor list and bump libavcodec minor version.
 */
/** enum AVCodecID */
public static final int
    AV_CODEC_ID_NONE = 0,

    /* video codecs */
    AV_CODEC_ID_MPEG1VIDEO = 1,
    /** preferred ID for MPEG-1/2 video decoding */
    AV_CODEC_ID_MPEG2VIDEO = 2,
// #if FF_API_XVMC
    AV_CODEC_ID_MPEG2VIDEO_XVMC = 3,
// #endif /* FF_API_XVMC */
    AV_CODEC_ID_H261 = 4,
    AV_CODEC_ID_H263 = 5,
    AV_CODEC_ID_RV10 = 6,
    AV_CODEC_ID_RV20 = 7,
    AV_CODEC_ID_MJPEG = 8,
    AV_CODEC_ID_MJPEGB = 9,
    AV_CODEC_ID_LJPEG = 10,
    AV_CODEC_ID_SP5X = 11,
    AV_CODEC_ID_JPEGLS = 12,
    AV_CODEC_ID_MPEG4 = 13,
    AV_CODEC_ID_RAWVIDEO = 14,
    AV_CODEC_ID_MSMPEG4V1 = 15,
    AV_CODEC_ID_MSMPEG4V2 = 16,
    AV_CODEC_ID_MSMPEG4V3 = 17,
    AV_CODEC_ID_WMV1 = 18,
    AV_CODEC_ID_WMV2 = 19,
    AV_CODEC_ID_H263P = 20,
    AV_CODEC_ID_H263I = 21,
    AV_CODEC_ID_FLV1 = 22,
    AV_CODEC_ID_SVQ1 = 23,
    AV_CODEC_ID_SVQ3 = 24,
    AV_CODEC_ID_DVVIDEO = 25,
    AV_CODEC_ID_HUFFYUV = 26,
    AV_CODEC_ID_CYUV = 27,
    AV_CODEC_ID_H264 = 28,
    AV_CODEC_ID_INDEO3 = 29,
    AV_CODEC_ID_VP3 = 30,
    AV_CODEC_ID_THEORA = 31,
    AV_CODEC_ID_ASV1 = 32,
    AV_CODEC_ID_ASV2 = 33,
    AV_CODEC_ID_FFV1 = 34,
    AV_CODEC_ID_4XM = 35,
    AV_CODEC_ID_VCR1 = 36,
    AV_CODEC_ID_CLJR = 37,
    AV_CODEC_ID_MDEC = 38,
    AV_CODEC_ID_ROQ = 39,
    AV_CODEC_ID_INTERPLAY_VIDEO = 40,
    AV_CODEC_ID_XAN_WC3 = 41,
    AV_CODEC_ID_XAN_WC4 = 42,
    AV_CODEC_ID_RPZA = 43,
    AV_CODEC_ID_CINEPAK = 44,
    AV_CODEC_ID_WS_VQA = 45,
    AV_CODEC_ID_MSRLE = 46,
    AV_CODEC_ID_MSVIDEO1 = 47,
    AV_CODEC_ID_IDCIN = 48,
    AV_CODEC_ID_8BPS = 49,
    AV_CODEC_ID_SMC = 50,
    AV_CODEC_ID_FLIC = 51,
    AV_CODEC_ID_TRUEMOTION1 = 52,
    AV_CODEC_ID_VMDVIDEO = 53,
    AV_CODEC_ID_MSZH = 54,
    AV_CODEC_ID_ZLIB = 55,
    AV_CODEC_ID_QTRLE = 56,
    AV_CODEC_ID_TSCC = 57,
    AV_CODEC_ID_ULTI = 58,
    AV_CODEC_ID_QDRAW = 59,
    AV_CODEC_ID_VIXL = 60,
    AV_CODEC_ID_QPEG = 61,
    AV_CODEC_ID_PNG = 62,
    AV_CODEC_ID_PPM = 63,
    AV_CODEC_ID_PBM = 64,
    AV_CODEC_ID_PGM = 65,
    AV_CODEC_ID_PGMYUV = 66,
    AV_CODEC_ID_PAM = 67,
    AV_CODEC_ID_FFVHUFF = 68,
    AV_CODEC_ID_RV30 = 69,
    AV_CODEC_ID_RV40 = 70,
    AV_CODEC_ID_VC1 = 71,
    AV_CODEC_ID_WMV3 = 72,
    AV_CODEC_ID_LOCO = 73,
    AV_CODEC_ID_WNV1 = 74,
    AV_CODEC_ID_AASC = 75,
    AV_CODEC_ID_INDEO2 = 76,
    AV_CODEC_ID_FRAPS = 77,
    AV_CODEC_ID_TRUEMOTION2 = 78,
    AV_CODEC_ID_BMP = 79,
    AV_CODEC_ID_CSCD = 80,
    AV_CODEC_ID_MMVIDEO = 81,
    AV_CODEC_ID_ZMBV = 82,
    AV_CODEC_ID_AVS = 83,
    AV_CODEC_ID_SMACKVIDEO = 84,
    AV_CODEC_ID_NUV = 85,
    AV_CODEC_ID_KMVC = 86,
    AV_CODEC_ID_FLASHSV = 87,
    AV_CODEC_ID_CAVS = 88,
    AV_CODEC_ID_JPEG2000 = 89,
    AV_CODEC_ID_VMNC = 90,
    AV_CODEC_ID_VP5 = 91,
    AV_CODEC_ID_VP6 = 92,
    AV_CODEC_ID_VP6F = 93,
    AV_CODEC_ID_TARGA = 94,
    AV_CODEC_ID_DSICINVIDEO = 95,
    AV_CODEC_ID_TIERTEXSEQVIDEO = 96,
    AV_CODEC_ID_TIFF = 97,
    AV_CODEC_ID_GIF = 98,
    AV_CODEC_ID_DXA = 99,
    AV_CODEC_ID_DNXHD = 100,
    AV_CODEC_ID_THP = 101,
    AV_CODEC_ID_SGI = 102,
    AV_CODEC_ID_C93 = 103,
    AV_CODEC_ID_BETHSOFTVID = 104,
    AV_CODEC_ID_PTX = 105,
    AV_CODEC_ID_TXD = 106,
    AV_CODEC_ID_VP6A = 107,
    AV_CODEC_ID_AMV = 108,
    AV_CODEC_ID_VB = 109,
    AV_CODEC_ID_PCX = 110,
    AV_CODEC_ID_SUNRAST = 111,
    AV_CODEC_ID_INDEO4 = 112,
    AV_CODEC_ID_INDEO5 = 113,
    AV_CODEC_ID_MIMIC = 114,
    AV_CODEC_ID_RL2 = 115,
    AV_CODEC_ID_ESCAPE124 = 116,
    AV_CODEC_ID_DIRAC = 117,
    AV_CODEC_ID_BFI = 118,
    AV_CODEC_ID_CMV = 119,
    AV_CODEC_ID_MOTIONPIXELS = 120,
    AV_CODEC_ID_TGV = 121,
    AV_CODEC_ID_TGQ = 122,
    AV_CODEC_ID_TQI = 123,
    AV_CODEC_ID_AURA = 124,
    AV_CODEC_ID_AURA2 = 125,
    AV_CODEC_ID_V210X = 126,
    AV_CODEC_ID_TMV = 127,
    AV_CODEC_ID_V210 = 128,
    AV_CODEC_ID_DPX = 129,
    AV_CODEC_ID_MAD = 130,
    AV_CODEC_ID_FRWU = 131,
    AV_CODEC_ID_FLASHSV2 = 132,
    AV_CODEC_ID_CDGRAPHICS = 133,
    AV_CODEC_ID_R210 = 134,
    AV_CODEC_ID_ANM = 135,
    AV_CODEC_ID_BINKVIDEO = 136,
    AV_CODEC_ID_IFF_ILBM = 137;
public static final int AV_CODEC_ID_IFF_BYTERUN1 = AV_CODEC_ID_IFF_ILBM;
public static final int
    AV_CODEC_ID_KGV1 = 138,
    AV_CODEC_ID_YOP = 139,
    AV_CODEC_ID_VP8 = 140,
    AV_CODEC_ID_PICTOR = 141,
    AV_CODEC_ID_ANSI = 142,
    AV_CODEC_ID_A64_MULTI = 143,
    AV_CODEC_ID_A64_MULTI5 = 144,
    AV_CODEC_ID_R10K = 145,
    AV_CODEC_ID_MXPEG = 146,
    AV_CODEC_ID_LAGARITH = 147,
    AV_CODEC_ID_PRORES = 148,
    AV_CODEC_ID_JV = 149,
    AV_CODEC_ID_DFA = 150,
    AV_CODEC_ID_WMV3IMAGE = 151,
    AV_CODEC_ID_VC1IMAGE = 152,
    AV_CODEC_ID_UTVIDEO = 153,
    AV_CODEC_ID_BMV_VIDEO = 154,
    AV_CODEC_ID_VBLE = 155,
    AV_CODEC_ID_DXTORY = 156,
    AV_CODEC_ID_V410 = 157,
    AV_CODEC_ID_XWD = 158,
    AV_CODEC_ID_CDXL = 159,
    AV_CODEC_ID_XBM = 160,
    AV_CODEC_ID_ZEROCODEC = 161,
    AV_CODEC_ID_MSS1 = 162,
    AV_CODEC_ID_MSA1 = 163,
    AV_CODEC_ID_TSCC2 = 164,
    AV_CODEC_ID_MTS2 = 165,
    AV_CODEC_ID_CLLC = 166,
    AV_CODEC_ID_MSS2 = 167,
    AV_CODEC_ID_VP9 = 168,
    AV_CODEC_ID_AIC = 169,
    AV_CODEC_ID_ESCAPE130 = 170,
    AV_CODEC_ID_G2M = 171,
    AV_CODEC_ID_WEBP = 172,
    AV_CODEC_ID_HNM4_VIDEO = 173,
    AV_CODEC_ID_HEVC = 174;
public static final int AV_CODEC_ID_H265 = AV_CODEC_ID_HEVC;
public static final int
    AV_CODEC_ID_FIC = 175,
    AV_CODEC_ID_ALIAS_PIX = 176,
    AV_CODEC_ID_BRENDER_PIX = 177,
    AV_CODEC_ID_PAF_VIDEO = 178,
    AV_CODEC_ID_EXR = 179,
    AV_CODEC_ID_VP7 = 180,
    AV_CODEC_ID_SANM = 181,
    AV_CODEC_ID_SGIRLE = 182,
    AV_CODEC_ID_MVC1 = 183,
    AV_CODEC_ID_MVC2 = 184,
    AV_CODEC_ID_HQX = 185,
    AV_CODEC_ID_TDSC = 186,
    AV_CODEC_ID_HQ_HQA = 187,
    AV_CODEC_ID_HAP = 188,
    AV_CODEC_ID_DDS = 189,
    AV_CODEC_ID_DXV = 190,
    AV_CODEC_ID_SCREENPRESSO = 191,
    AV_CODEC_ID_RSCC = 192,

    AV_CODEC_ID_Y41P =  0x8000,
    AV_CODEC_ID_AVRP =  0x8000 + 1,
    AV_CODEC_ID_012V =  0x8000 + 2,
    AV_CODEC_ID_AVUI =  0x8000 + 3,
    AV_CODEC_ID_AYUV =  0x8000 + 4,
    AV_CODEC_ID_TARGA_Y216 =  0x8000 + 5,
    AV_CODEC_ID_V308 =  0x8000 + 6,
    AV_CODEC_ID_V408 =  0x8000 + 7,
    AV_CODEC_ID_YUV4 =  0x8000 + 8,
    AV_CODEC_ID_AVRN =  0x8000 + 9,
    AV_CODEC_ID_CPIA =  0x8000 + 10,
    AV_CODEC_ID_XFACE =  0x8000 + 11,
    AV_CODEC_ID_SNOW =  0x8000 + 12,
    AV_CODEC_ID_SMVJPEG =  0x8000 + 13,
    AV_CODEC_ID_APNG =  0x8000 + 14,
    AV_CODEC_ID_DAALA =  0x8000 + 15,
    AV_CODEC_ID_CFHD =  0x8000 + 16,

    /* various PCM "codecs" */
    /** A dummy id pointing at the start of audio codecs */
    AV_CODEC_ID_FIRST_AUDIO =  0x10000,
    AV_CODEC_ID_PCM_S16LE =  0x10000,
    AV_CODEC_ID_PCM_S16BE =  0x10000 + 1,
    AV_CODEC_ID_PCM_U16LE =  0x10000 + 2,
    AV_CODEC_ID_PCM_U16BE =  0x10000 + 3,
    AV_CODEC_ID_PCM_S8 =  0x10000 + 4,
    AV_CODEC_ID_PCM_U8 =  0x10000 + 5,
    AV_CODEC_ID_PCM_MULAW =  0x10000 + 6,
    AV_CODEC_ID_PCM_ALAW =  0x10000 + 7,
    AV_CODEC_ID_PCM_S32LE =  0x10000 + 8,
    AV_CODEC_ID_PCM_S32BE =  0x10000 + 9,
    AV_CODEC_ID_PCM_U32LE =  0x10000 + 10,
    AV_CODEC_ID_PCM_U32BE =  0x10000 + 11,
    AV_CODEC_ID_PCM_S24LE =  0x10000 + 12,
    AV_CODEC_ID_PCM_S24BE =  0x10000 + 13,
    AV_CODEC_ID_PCM_U24LE =  0x10000 + 14,
    AV_CODEC_ID_PCM_U24BE =  0x10000 + 15,
    AV_CODEC_ID_PCM_S24DAUD =  0x10000 + 16,
    AV_CODEC_ID_PCM_ZORK =  0x10000 + 17,
    AV_CODEC_ID_PCM_S16LE_PLANAR =  0x10000 + 18,
    AV_CODEC_ID_PCM_DVD =  0x10000 + 19,
    AV_CODEC_ID_PCM_F32BE =  0x10000 + 20,
    AV_CODEC_ID_PCM_F32LE =  0x10000 + 21,
    AV_CODEC_ID_PCM_F64BE =  0x10000 + 22,
    AV_CODEC_ID_PCM_F64LE =  0x10000 + 23,
    AV_CODEC_ID_PCM_BLURAY =  0x10000 + 24,
    AV_CODEC_ID_PCM_LXF =  0x10000 + 25,
    AV_CODEC_ID_S302M =  0x10000 + 26,
    AV_CODEC_ID_PCM_S8_PLANAR =  0x10000 + 27,
    AV_CODEC_ID_PCM_S24LE_PLANAR =  0x10000 + 28,
    AV_CODEC_ID_PCM_S32LE_PLANAR =  0x10000 + 29,
    AV_CODEC_ID_PCM_S16BE_PLANAR =  0x10000 + 30,
    /* new PCM "codecs" should be added right below this line starting with
     * an explicit value of for example 0x10800
     */

    /* various ADPCM codecs */
    AV_CODEC_ID_ADPCM_IMA_QT =  0x11000,
    AV_CODEC_ID_ADPCM_IMA_WAV =  0x11000 + 1,
    AV_CODEC_ID_ADPCM_IMA_DK3 =  0x11000 + 2,
    AV_CODEC_ID_ADPCM_IMA_DK4 =  0x11000 + 3,
    AV_CODEC_ID_ADPCM_IMA_WS =  0x11000 + 4,
    AV_CODEC_ID_ADPCM_IMA_SMJPEG =  0x11000 + 5,
    AV_CODEC_ID_ADPCM_MS =  0x11000 + 6,
    AV_CODEC_ID_ADPCM_4XM =  0x11000 + 7,
    AV_CODEC_ID_ADPCM_XA =  0x11000 + 8,
    AV_CODEC_ID_ADPCM_ADX =  0x11000 + 9,
    AV_CODEC_ID_ADPCM_EA =  0x11000 + 10,
    AV_CODEC_ID_ADPCM_G726 =  0x11000 + 11,
    AV_CODEC_ID_ADPCM_CT =  0x11000 + 12,
    AV_CODEC_ID_ADPCM_SWF =  0x11000 + 13,
    AV_CODEC_ID_ADPCM_YAMAHA =  0x11000 + 14,
    AV_CODEC_ID_ADPCM_SBPRO_4 =  0x11000 + 15,
    AV_CODEC_ID_ADPCM_SBPRO_3 =  0x11000 + 16,
    AV_CODEC_ID_ADPCM_SBPRO_2 =  0x11000 + 17,
    AV_CODEC_ID_ADPCM_THP =  0x11000 + 18,
    AV_CODEC_ID_ADPCM_IMA_AMV =  0x11000 + 19,
    AV_CODEC_ID_ADPCM_EA_R1 =  0x11000 + 20,
    AV_CODEC_ID_ADPCM_EA_R3 =  0x11000 + 21,
    AV_CODEC_ID_ADPCM_EA_R2 =  0x11000 + 22,
    AV_CODEC_ID_ADPCM_IMA_EA_SEAD =  0x11000 + 23,
    AV_CODEC_ID_ADPCM_IMA_EA_EACS =  0x11000 + 24,
    AV_CODEC_ID_ADPCM_EA_XAS =  0x11000 + 25,
    AV_CODEC_ID_ADPCM_EA_MAXIS_XA =  0x11000 + 26,
    AV_CODEC_ID_ADPCM_IMA_ISS =  0x11000 + 27,
    AV_CODEC_ID_ADPCM_G722 =  0x11000 + 28,
    AV_CODEC_ID_ADPCM_IMA_APC =  0x11000 + 29,
    AV_CODEC_ID_ADPCM_VIMA =  0x11000 + 30,
// #if FF_API_VIMA_DECODER
    AV_CODEC_ID_VIMA =  AV_CODEC_ID_ADPCM_VIMA,
// #endif

    AV_CODEC_ID_ADPCM_AFC =  0x11800,
    AV_CODEC_ID_ADPCM_IMA_OKI =  0x11800 + 1,
    AV_CODEC_ID_ADPCM_DTK =  0x11800 + 2,
    AV_CODEC_ID_ADPCM_IMA_RAD =  0x11800 + 3,
    AV_CODEC_ID_ADPCM_G726LE =  0x11800 + 4,
    AV_CODEC_ID_ADPCM_THP_LE =  0x11800 + 5,
    AV_CODEC_ID_ADPCM_PSX =  0x11800 + 6,
    AV_CODEC_ID_ADPCM_AICA =  0x11800 + 7,

    /* AMR */
    AV_CODEC_ID_AMR_NB =  0x12000,
    AV_CODEC_ID_AMR_WB =  0x12000 + 1,

    /* RealAudio codecs*/
    AV_CODEC_ID_RA_144 =  0x13000,
    AV_CODEC_ID_RA_288 =  0x13000 + 1,

    /* various DPCM codecs */
    AV_CODEC_ID_ROQ_DPCM =  0x14000,
    AV_CODEC_ID_INTERPLAY_DPCM =  0x14000 + 1,
    AV_CODEC_ID_XAN_DPCM =  0x14000 + 2,
    AV_CODEC_ID_SOL_DPCM =  0x14000 + 3,

    AV_CODEC_ID_SDX2_DPCM =  0x14800,

    /* audio codecs */
    AV_CODEC_ID_MP2 =  0x15000,
    /** preferred ID for decoding MPEG audio layer 1, 2 or 3 */
    AV_CODEC_ID_MP3 =  0x15000 + 1,
    AV_CODEC_ID_AAC =  0x15000 + 2,
    AV_CODEC_ID_AC3 =  0x15000 + 3,
    AV_CODEC_ID_DTS =  0x15000 + 4,
    AV_CODEC_ID_VORBIS =  0x15000 + 5,
    AV_CODEC_ID_DVAUDIO =  0x15000 + 6,
    AV_CODEC_ID_WMAV1 =  0x15000 + 7,
    AV_CODEC_ID_WMAV2 =  0x15000 + 8,
    AV_CODEC_ID_MACE3 =  0x15000 + 9,
    AV_CODEC_ID_MACE6 =  0x15000 + 10,
    AV_CODEC_ID_VMDAUDIO =  0x15000 + 11,
    AV_CODEC_ID_FLAC =  0x15000 + 12,
    AV_CODEC_ID_MP3ADU =  0x15000 + 13,
    AV_CODEC_ID_MP3ON4 =  0x15000 + 14,
    AV_CODEC_ID_SHORTEN =  0x15000 + 15,
    AV_CODEC_ID_ALAC =  0x15000 + 16,
    AV_CODEC_ID_WESTWOOD_SND1 =  0x15000 + 17,
    /** as in Berlin toast format */
    AV_CODEC_ID_GSM =  0x15000 + 18,
    AV_CODEC_ID_QDM2 =  0x15000 + 19,
    AV_CODEC_ID_COOK =  0x15000 + 20,
    AV_CODEC_ID_TRUESPEECH =  0x15000 + 21,
    AV_CODEC_ID_TTA =  0x15000 + 22,
    AV_CODEC_ID_SMACKAUDIO =  0x15000 + 23,
    AV_CODEC_ID_QCELP =  0x15000 + 24,
    AV_CODEC_ID_WAVPACK =  0x15000 + 25,
    AV_CODEC_ID_DSICINAUDIO =  0x15000 + 26,
    AV_CODEC_ID_IMC =  0x15000 + 27,
    AV_CODEC_ID_MUSEPACK7 =  0x15000 + 28,
    AV_CODEC_ID_MLP =  0x15000 + 29,
    AV_CODEC_ID_GSM_MS =  0x15000 + 30, /* as found in WAV */
    AV_CODEC_ID_ATRAC3 =  0x15000 + 31,
// #if FF_API_VOXWARE
    AV_CODEC_ID_VOXWARE =  0x15000 + 32,
// #endif
    AV_CODEC_ID_APE =  0x15000 + 33,
    AV_CODEC_ID_NELLYMOSER =  0x15000 + 34,
    AV_CODEC_ID_MUSEPACK8 =  0x15000 + 35,
    AV_CODEC_ID_SPEEX =  0x15000 + 36,
    AV_CODEC_ID_WMAVOICE =  0x15000 + 37,
    AV_CODEC_ID_WMAPRO =  0x15000 + 38,
    AV_CODEC_ID_WMALOSSLESS =  0x15000 + 39,
    AV_CODEC_ID_ATRAC3P =  0x15000 + 40,
    AV_CODEC_ID_EAC3 =  0x15000 + 41,
    AV_CODEC_ID_SIPR =  0x15000 + 42,
    AV_CODEC_ID_MP1 =  0x15000 + 43,
    AV_CODEC_ID_TWINVQ =  0x15000 + 44,
    AV_CODEC_ID_TRUEHD =  0x15000 + 45,
    AV_CODEC_ID_MP4ALS =  0x15000 + 46,
    AV_CODEC_ID_ATRAC1 =  0x15000 + 47,
    AV_CODEC_ID_BINKAUDIO_RDFT =  0x15000 + 48,
    AV_CODEC_ID_BINKAUDIO_DCT =  0x15000 + 49,
    AV_CODEC_ID_AAC_LATM =  0x15000 + 50,
    AV_CODEC_ID_QDMC =  0x15000 + 51,
    AV_CODEC_ID_CELT =  0x15000 + 52,
    AV_CODEC_ID_G723_1 =  0x15000 + 53,
    AV_CODEC_ID_G729 =  0x15000 + 54,
    AV_CODEC_ID_8SVX_EXP =  0x15000 + 55,
    AV_CODEC_ID_8SVX_FIB =  0x15000 + 56,
    AV_CODEC_ID_BMV_AUDIO =  0x15000 + 57,
    AV_CODEC_ID_RALF =  0x15000 + 58,
    AV_CODEC_ID_IAC =  0x15000 + 59,
    AV_CODEC_ID_ILBC =  0x15000 + 60,
    AV_CODEC_ID_OPUS =  0x15000 + 61,
    AV_CODEC_ID_COMFORT_NOISE =  0x15000 + 62,
    AV_CODEC_ID_TAK =  0x15000 + 63,
    AV_CODEC_ID_METASOUND =  0x15000 + 64,
    AV_CODEC_ID_PAF_AUDIO =  0x15000 + 65,
    AV_CODEC_ID_ON2AVC =  0x15000 + 66,
    AV_CODEC_ID_DSS_SP =  0x15000 + 67,

    AV_CODEC_ID_FFWAVESYNTH =  0x15800,
    AV_CODEC_ID_SONIC =  0x15800 + 1,
    AV_CODEC_ID_SONIC_LS =  0x15800 + 2,
    AV_CODEC_ID_EVRC =  0x15800 + 3,
    AV_CODEC_ID_SMV =  0x15800 + 4,
    AV_CODEC_ID_DSD_LSBF =  0x15800 + 5,
    AV_CODEC_ID_DSD_MSBF =  0x15800 + 6,
    AV_CODEC_ID_DSD_LSBF_PLANAR =  0x15800 + 7,
    AV_CODEC_ID_DSD_MSBF_PLANAR =  0x15800 + 8,
    AV_CODEC_ID_4GV =  0x15800 + 9,
    AV_CODEC_ID_INTERPLAY_ACM =  0x15800 + 10,
    AV_CODEC_ID_XMA1 =  0x15800 + 11,
    AV_CODEC_ID_XMA2 =  0x15800 + 12,

    /* subtitle codecs */
    /** A dummy ID pointing at the start of subtitle codecs. */
    AV_CODEC_ID_FIRST_SUBTITLE =  0x17000,
    AV_CODEC_ID_DVD_SUBTITLE =  0x17000,
    AV_CODEC_ID_DVB_SUBTITLE =  0x17000 + 1,
    /** raw UTF-8 text */
    AV_CODEC_ID_TEXT =  0x17000 + 2,
    AV_CODEC_ID_XSUB =  0x17000 + 3,
    AV_CODEC_ID_SSA =  0x17000 + 4,
    AV_CODEC_ID_MOV_TEXT =  0x17000 + 5,
    AV_CODEC_ID_HDMV_PGS_SUBTITLE =  0x17000 + 6,
    AV_CODEC_ID_DVB_TELETEXT =  0x17000 + 7,
    AV_CODEC_ID_SRT =  0x17000 + 8,

    AV_CODEC_ID_MICRODVD   =  0x17800,
    AV_CODEC_ID_EIA_608 =  0x17800 + 1,
    AV_CODEC_ID_JACOSUB =  0x17800 + 2,
    AV_CODEC_ID_SAMI =  0x17800 + 3,
    AV_CODEC_ID_REALTEXT =  0x17800 + 4,
    AV_CODEC_ID_STL =  0x17800 + 5,
    AV_CODEC_ID_SUBVIEWER1 =  0x17800 + 6,
    AV_CODEC_ID_SUBVIEWER =  0x17800 + 7,
    AV_CODEC_ID_SUBRIP =  0x17800 + 8,
    AV_CODEC_ID_WEBVTT =  0x17800 + 9,
    AV_CODEC_ID_MPL2 =  0x17800 + 10,
    AV_CODEC_ID_VPLAYER =  0x17800 + 11,
    AV_CODEC_ID_PJS =  0x17800 + 12,
    AV_CODEC_ID_ASS =  0x17800 + 13,
    AV_CODEC_ID_HDMV_TEXT_SUBTITLE =  0x17800 + 14,

    /* other specific kind of codecs (generally used for attachments) */
    /** A dummy ID pointing at the start of various fake codecs. */
    AV_CODEC_ID_FIRST_UNKNOWN =  0x18000,
    AV_CODEC_ID_TTF =  0x18000,

    AV_CODEC_ID_BINTEXT    =  0x18800,
    AV_CODEC_ID_XBIN =  0x18800 + 1,
    AV_CODEC_ID_IDF =  0x18800 + 2,
    AV_CODEC_ID_OTF =  0x18800 + 3,
    AV_CODEC_ID_SMPTE_KLV =  0x18800 + 4,
    AV_CODEC_ID_DVD_NAV =  0x18800 + 5,
    AV_CODEC_ID_TIMED_ID3 =  0x18800 + 6,
    AV_CODEC_ID_BIN_DATA =  0x18800 + 7,


    /** codec_id is not known (like AV_CODEC_ID_NONE) but lavf should attempt to identify it */
    AV_CODEC_ID_PROBE =  0x19000,

    /** _FAKE_ codec to indicate a raw MPEG-2 TS
                                * stream (only used by libavformat) */
    AV_CODEC_ID_MPEG2TS =  0x20000,
    /** _FAKE_ codec to indicate a MPEG-4 Systems
                                * stream (only used by libavformat) */
    AV_CODEC_ID_MPEG4SYSTEMS =  0x20001,
    /** Dummy codec for streams containing only metadata information. */
    AV_CODEC_ID_FFMETADATA =  0x21000,
    /** Passthrough codec, AVFrames wrapped in AVPacket */
    AV_CODEC_ID_WRAPPED_AVFRAME =  0x21001;

/**
 * This struct describes the properties of a single codec described by an
 * AVCodecID.
 * @see avcodec_descriptor_get()
 */
public static class AVCodecDescriptor extends Pointer {
    static { Loader.load(); }
    /** Default native constructor. */
    public AVCodecDescriptor() { allocate(); }
    /** Native array allocator. Access with {@link Pointer#position(int)}. */
    public AVCodecDescriptor(int size) { allocateArray(size); }
    /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
    public AVCodecDescriptor(Pointer p) { super(p); }
    private native void allocate();
    private native void allocateArray(int size);
    @Override public AVCodecDescriptor position(int position) {
        return (AVCodecDescriptor)super.position(position);
    }

    public native @Cast("AVCodecID") int id(); public native AVCodecDescriptor id(int id);
    public native @Cast("AVMediaType") int type(); public native AVCodecDescriptor type(int type);
    /**
     * Name of the codec described by this descriptor. It is non-empty and
     * unique for each codec descriptor. It should contain alphanumeric
     * characters and '_' only.
     */
    @MemberGetter public native @Cast("const char*") BytePointer name();
    /**
     * A more descriptive name for this codec. May be NULL.
     */
    @MemberGetter public native @Cast("const char*") BytePointer long_name();
    /**
     * Codec properties, a combination of AV_CODEC_PROP_* flags.
     */
    public native int props(); public native AVCodecDescriptor props(int props);
    /**
     * MIME type(s) associated with the codec.
     * May be NULL; if not, a NULL-terminated array of MIME types.
     * The first item is always non-NULL and is the preferred MIME type.
     */
    @MemberGetter public native @Cast("const char*") BytePointer mime_types(int i);
    @MemberGetter public native @Cast("const char*const*") PointerPointer mime_types();
    /**
     * If non-NULL, an array of profiles recognized for this codec.
     * Terminated with FF_PROFILE_UNKNOWN.
     */
    @MemberGetter public native @Const AVProfile profiles();
}

/**
 * Codec uses only intra compression.
 * Video codecs only.
 */
public static final int AV_CODEC_PROP_INTRA_ONLY =    (1 << 0);
/**
 * Codec supports lossy compression. Audio and video codecs only.
 * @note a codec may support both lossy and lossless
 * compression modes
 */
public static final int AV_CODEC_PROP_LOSSY =         (1 << 1);
/**
 * Codec supports lossless compression. Audio and video codecs only.
 */
public static final int AV_CODEC_PROP_LOSSLESS =      (1 << 2);
/**
 * Codec supports frame reordering. That is, the coded order (the order in which
 * the encoded packets are output by the encoders / stored / input to the
 * decoders) may be different from the presentation order of the corresponding
 * frames.
 *
 * For codecs that do not have this property set, PTS and DTS should always be
 * equal.
 */
public static final int AV_CODEC_PROP_REORDER =       (1 << 3);
/**
 * Subtitle codec is bitmap based
 * Decoded AVSubtitle data can be read from the AVSubtitleRect->pict field.
 */
public static final int AV_CODEC_PROP_BITMAP_SUB =    (1 << 16);
/**
 * Subtitle codec is text based.
 * Decoded AVSubtitle data can be read from the AVSubtitleRect->ass field.
 */
public static final int AV_CODEC_PROP_TEXT_SUB =      (1 << 17);

/**
 * @ingroup lavc_decoding
 * Required number of additionally allocated bytes at the end of the input bitstream for decoding.
 * This is mainly needed because some optimized bitstream readers read
 * 32 or 64 bit at once and could read over the end.<br>
 * Note: If the first 23 bits of the additional bytes are not 0, then damaged
 * MPEG bitstreams could cause overread and segfault.
 */
public static final int AV_INPUT_BUFFER_PADDING_SIZE = 32;

/**
 * @ingroup lavc_encoding
 * minimum encoding buffer size
 * Used to avoid some checks during header writing.
 */
public static final int AV_INPUT_BUFFER_MIN_SIZE = 16384;

// #if FF_API_WITHOUT_PREFIX
/**
 * @deprecated use AV_INPUT_BUFFER_PADDING_SIZE instead
 */
public static final int FF_INPUT_BUFFER_PADDING_SIZE = 32;

/**
 * @deprecated use AV_INPUT_BUFFER_MIN_SIZE instead
 */
public static final int FF_MIN_BUFFER_SIZE = 16384;
// #endif /* FF_API_WITHOUT_PREFIX */

/**
 * @ingroup lavc_encoding
 * motion estimation type.
 * @deprecated use codec private option instead
 */
// #if FF_API_MOTION_EST
/** enum Motion_Est_ID */
public static final int
    /** no search, that is use 0,0 vector whenever one is needed */
    ME_ZERO = 1,
    ME_FULL = 2,
    ME_LOG = 3,
    ME_PHODS = 4,
    /** enhanced predictive zonal search */
    ME_EPZS = 5,
    /** reserved for experiments */
    ME_X1 = 6,
    /** hexagon based search */
    ME_HEX = 7,
    /** uneven multi-hexagon search */
    ME_UMH = 8,
    /** transformed exhaustive search algorithm */
    ME_TESA = 9,
    /** iterative search */
    ME_ITER= 50;
// #endif

/**
 * @ingroup lavc_decoding
 */
/** enum AVDiscard */
public static final int
    /* We leave some space between them for extensions (drop some
     * keyframes for intra-only or drop just some bidir frames). */
    /** discard nothing */
    AVDISCARD_NONE    = -16,
    /** discard useless packets like 0 size packets in avi */
    AVDISCARD_DEFAULT = 0,
    /** discard all non reference */
    AVDISCARD_NONREF  = 8,
    /** discard all bidirectional frames */
    AVDISCARD_BIDIR   = 16,
    /** discard all non intra frames */
    AVDISCARD_NONINTRA= 24,
    /** discard all frames except keyframes */
    AVDISCARD_NONKEY  = 32,
    /** discard all */
    AVDISCARD_ALL     = 48;

/** enum AVAudioServiceType */
public static final int
    AV_AUDIO_SERVICE_TYPE_MAIN              = 0,
    AV_AUDIO_SERVICE_TYPE_EFFECTS           = 1,
    AV_AUDIO_SERVICE_TYPE_VISUALLY_IMPAIRED = 2,
    AV_AUDIO_SERVICE_TYPE_HEARING_IMPAIRED  = 3,
    AV_AUDIO_SERVICE_TYPE_DIALOGUE          = 4,
    AV_AUDIO_SERVICE_TYPE_COMMENTARY        = 5,
    AV_AUDIO_SERVICE_TYPE_EMERGENCY         = 6,
    AV_AUDIO_SERVICE_TYPE_VOICE_OVER        = 7,
    AV_AUDIO_SERVICE_TYPE_KARAOKE           = 8,
    /** Not part of ABI */
    AV_AUDIO_SERVICE_TYPE_NB = 9;

/**
 * @ingroup lavc_encoding
 */
public static class RcOverride extends Pointer {
    static { Loader.load(); }
    /** Default native constructor. */
    public RcOverride() { allocate(); }
    /** Native array allocator. Access with {@link Pointer#position(int)}. */
    public RcOverride(int size) { allocateArray(size); }
    /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
    public RcOverride(Pointer p) { super(p); }
    private native void allocate();
    private native void allocateArray(int size);
    @Override public RcOverride position(int position) {
        return (RcOverride)super.position(position);
    }

    public native int start_frame(); public native RcOverride start_frame(int start_frame);
    public native int end_frame(); public native RcOverride end_frame(int end_frame);
    public native int qscale(); public native RcOverride qscale(int qscale); // If this is 0 then quality_factor will be used instead.
    public native float quality_factor(); public native RcOverride quality_factor(float quality_factor);
}

// #if FF_API_MAX_BFRAMES
/**
 * @deprecated there is no libavcodec-wide limit on the number of B-frames
 */
public static final int FF_MAX_B_FRAMES = 16;
// #endif

/* encoding support
   These flags can be passed in AVCodecContext.flags before initialization.
   Note: Not everything is supported yet.
*/

/**
 * Allow decoders to produce frames with data planes that are not aligned
 * to CPU requirements (e.g. due to cropping).
 */
public static final int AV_CODEC_FLAG_UNALIGNED =       (1 <<  0);
/**
 * Use fixed qscale.
 */
public static final int AV_CODEC_FLAG_QSCALE =          (1 <<  1);
/**
 * 4 MV per MB allowed / advanced prediction for H.263.
 */
public static final int AV_CODEC_FLAG_4MV =             (1 <<  2);
/**
 * Output even those frames that might be corrupted.
 */
public static final int AV_CODEC_FLAG_OUTPUT_CORRUPT =  (1 <<  3);
/**
 * Use qpel MC.
 */
public static final int AV_CODEC_FLAG_QPEL =            (1 <<  4);
/**
 * Use internal 2pass ratecontrol in first pass mode.
 */
public static final int AV_CODEC_FLAG_PASS1 =           (1 <<  9);
/**
 * Use internal 2pass ratecontrol in second pass mode.
 */
public static final int AV_CODEC_FLAG_PASS2 =           (1 << 10);
/**
 * loop filter.
 */
public static final int AV_CODEC_FLAG_LOOP_FILTER =     (1 << 11);
/**
 * Only decode/encode grayscale.
 */
public static final int AV_CODEC_FLAG_GRAY =            (1 << 13);
/**
 * error[?] variables will be set during encoding.
 */
public static final int AV_CODEC_FLAG_PSNR =            (1 << 15);
/**
 * Input bitstream might be truncated at a random location
 * instead of only at frame boundaries.
 */
public static final int AV_CODEC_FLAG_TRUNCATED =       (1 << 16);
/**
 * Use interlaced DCT.
 */
public static final int AV_CODEC_FLAG_INTERLACED_DCT =  (1 << 18);
/**
 * Force low delay.
 */
public static final int AV_CODEC_FLAG_LOW_DELAY =       (1 << 19);
/**
 * Place global headers in extradata instead of every keyframe.
 */
public static final int AV_CODEC_FLAG_GLOBAL_HEADER =   (1 << 22);
/**
 * Use only bitexact stuff (except (I)DCT).
 */
public static final int AV_CODEC_FLAG_BITEXACT =        (1 << 23);
/* Fx : Flag for h263+ extra options */
/**
 * H.263 advanced intra coding / MPEG-4 AC prediction
 */
public static final int AV_CODEC_FLAG_AC_PRED =         (1 << 24);
/**
 * interlaced motion estimation
 */
public static final int AV_CODEC_FLAG_INTERLACED_ME =   (1 << 29);
public static final long AV_CODEC_FLAG_CLOSED_GOP =      (1L << 31);

/**
 * Allow non spec compliant speedup tricks.
 */
public static final int AV_CODEC_FLAG2_FAST =           (1 <<  0);
/**
 * Skip bitstream encoding.
 */
public static final int AV_CODEC_FLAG2_NO_OUTPUT =      (1 <<  2);
/**
 * Place global headers at every keyframe instead of in extradata.
 */
public static final int AV_CODEC_FLAG2_LOCAL_HEADER =   (1 <<  3);

/**
 * timecode is in drop frame format. DEPRECATED!!!!
 */
public static final int AV_CODEC_FLAG2_DROP_FRAME_TIMECODE = (1 << 13);

/**
 * Input bitstream might be truncated at a packet boundaries
 * instead of only at frame boundaries.
 */
public static final int AV_CODEC_FLAG2_CHUNKS =         (1 << 15);
/**
 * Discard cropping information from SPS.
 */
public static final int AV_CODEC_FLAG2_IGNORE_CROP =    (1 << 16);

/**
 * Show all frames before the first keyframe
 */
public static final int AV_CODEC_FLAG2_SHOW_ALL =       (1 << 22);
/**
 * Export motion vectors through frame side data
 */
public static final int AV_CODEC_FLAG2_EXPORT_MVS =     (1 << 28);
/**
 * Do not skip samples and export skip information as frame side data
 */
public static final int AV_CODEC_FLAG2_SKIP_MANUAL =    (1 << 29);

/* Unsupported options :
 *              Syntax Arithmetic coding (SAC)
 *              Reference Picture Selection
 *              Independent Segment Decoding */
/* /Fx */
/* codec capabilities */

/**
 * Decoder can use draw_horiz_band callback.
 */
public static final int AV_CODEC_CAP_DRAW_HORIZ_BAND =     (1 <<  0);
/**
 * Codec uses get_buffer() for allocating buffers and supports custom allocators.
 * If not set, it might not use get_buffer() at all or use operations that
 * assume the buffer was allocated by avcodec_default_get_buffer.
 */
public static final int AV_CODEC_CAP_DR1 =                 (1 <<  1);
public static final int AV_CODEC_CAP_TRUNCATED =           (1 <<  3);
/**
 * Encoder or decoder requires flushing with NULL input at the end in order to
 * give the complete and correct output.
 *
 * NOTE: If this flag is not set, the codec is guaranteed to never be fed with
 *       with NULL data. The user can still send NULL data to the public encode
 *       or decode function, but libavcodec will not pass it along to the codec
 *       unless this flag is set.
 *
 * Decoders:
 * The decoder has a non-zero delay and needs to be fed with avpkt->data=NULL,
 * avpkt->size=0 at the end to get the delayed data until the decoder no longer
 * returns frames.
 *
 * Encoders:
 * The encoder needs to be fed with NULL data at the end of encoding until the
 * encoder no longer returns data.
 *
 * NOTE: For encoders implementing the AVCodec.encode2() function, setting this
 *       flag also means that the encoder must set the pts and duration for
 *       each output packet. If this flag is not set, the pts and duration will
 *       be determined by libavcodec from the input frame.
 */
public static final int AV_CODEC_CAP_DELAY =               (1 <<  5);
/**
 * Codec can be fed a final frame with a smaller size.
 * This can be used to prevent truncation of the last audio samples.
 */
public static final int AV_CODEC_CAP_SMALL_LAST_FRAME =    (1 <<  6);

// #if FF_API_CAP_VDPAU
/**
 * Codec can export data for HW decoding (VDPAU).
 */
public static final int AV_CODEC_CAP_HWACCEL_VDPAU =       (1 <<  7);
// #endif

/**
 * Codec can output multiple frames per AVPacket
 * Normally demuxers return one frame at a time, demuxers which do not do
 * are connected to a parser to split what they return into proper frames.
 * This flag is reserved to the very rare category of codecs which have a
 * bitstream that cannot be split into frames without timeconsuming
 * operations like full decoding. Demuxers carring such bitstreams thus
 * may return multiple frames in a packet. This has many disadvantages like
 * prohibiting stream copy in many cases thus it should only be considered
 * as a last resort.
 */
public static final int AV_CODEC_CAP_SUBFRAMES =           (1 <<  8);
/**
 * Codec is experimental and is thus avoided in favor of non experimental
 * encoders
 */
public static final int AV_CODEC_CAP_EXPERIMENTAL =        (1 <<  9);
/**
 * Codec should fill in channel configuration and samplerate instead of container
 */
public static final int AV_CODEC_CAP_CHANNEL_CONF =        (1 << 10);
/**
 * Codec supports frame-level multithreading.
 */
public static final int AV_CODEC_CAP_FRAME_THREADS =       (1 << 12);
/**
 * Codec supports slice-based (or partition-based) multithreading.
 */
public static final int AV_CODEC_CAP_SLICE_THREADS =       (1 << 13);
/**
 * Codec supports changed parameters at any point.
 */
public static final int AV_CODEC_CAP_PARAM_CHANGE =        (1 << 14);
/**
 * Codec supports avctx->thread_count == 0 (auto).
 */
public static final int AV_CODEC_CAP_AUTO_THREADS =        (1 << 15);
/**
 * Audio encoder supports receiving a different number of samples in each call.
 */
public static final int AV_CODEC_CAP_VARIABLE_FRAME_SIZE = (1 << 16);
/**
 * Codec is intra only.
 */
public static final int AV_CODEC_CAP_INTRA_ONLY =       0x40000000;
/**
 * Codec is lossless.
 */
public static final int AV_CODEC_CAP_LOSSLESS =         0x80000000;


// #if FF_API_WITHOUT_PREFIX
/**
 * Allow decoders to produce frames with data planes that are not aligned
 * to CPU requirements (e.g. due to cropping).
 */
public static final int CODEC_FLAG_UNALIGNED = AV_CODEC_FLAG_UNALIGNED;
public static final int CODEC_FLAG_QSCALE = AV_CODEC_FLAG_QSCALE;
public static final int CODEC_FLAG_4MV =    AV_CODEC_FLAG_4MV;
public static final int CODEC_FLAG_OUTPUT_CORRUPT = AV_CODEC_FLAG_OUTPUT_CORRUPT;
public static final int CODEC_FLAG_QPEL =   AV_CODEC_FLAG_QPEL;
// #if FF_API_GMC
/**
 * @deprecated use the "gmc" private option of the libxvid encoder
 */
/** Use GMC. */
public static final int CODEC_FLAG_GMC =    0x0020;
// #endif
// #if FF_API_MV0
/**
 * @deprecated use the flag "mv0" in the "mpv_flags" private option of the
 * mpegvideo encoders
 */
public static final int CODEC_FLAG_MV0 =    0x0040;
// #endif
// #if FF_API_INPUT_PRESERVED
/**
 * @deprecated passing reference-counted frames to the encoders replaces this
 * flag
 */
public static final int CODEC_FLAG_INPUT_PRESERVED = 0x0100;
// #endif
public static final int CODEC_FLAG_PASS1 =           AV_CODEC_FLAG_PASS1;
public static final int CODEC_FLAG_PASS2 =           AV_CODEC_FLAG_PASS2;
public static final int CODEC_FLAG_GRAY =            AV_CODEC_FLAG_GRAY;
// #if FF_API_EMU_EDGE
/**
 * @deprecated edges are not used/required anymore. I.e. this flag is now always
 * set.
 */
public static final int CODEC_FLAG_EMU_EDGE =        0x4000;
// #endif
public static final int CODEC_FLAG_PSNR =            AV_CODEC_FLAG_PSNR;
public static final int CODEC_FLAG_TRUNCATED =       AV_CODEC_FLAG_TRUNCATED;

// #if FF_API_NORMALIZE_AQP
/**
 * @deprecated use the flag "naq" in the "mpv_flags" private option of the
 * mpegvideo encoders
 */
public static final int CODEC_FLAG_NORMALIZE_AQP =  0x00020000;
// #endif
public static final int CODEC_FLAG_INTERLACED_DCT = AV_CODEC_FLAG_INTERLACED_DCT;
public static final int CODEC_FLAG_LOW_DELAY =      AV_CODEC_FLAG_LOW_DELAY;
public static final int CODEC_FLAG_GLOBAL_HEADER =  AV_CODEC_FLAG_GLOBAL_HEADER;
public static final int CODEC_FLAG_BITEXACT =       AV_CODEC_FLAG_BITEXACT;
public static final int CODEC_FLAG_AC_PRED =        AV_CODEC_FLAG_AC_PRED;
public static final int CODEC_FLAG_LOOP_FILTER =    AV_CODEC_FLAG_LOOP_FILTER;
public static final int CODEC_FLAG_INTERLACED_ME =  AV_CODEC_FLAG_INTERLACED_ME;
public static final long CODEC_FLAG_CLOSED_GOP =     AV_CODEC_FLAG_CLOSED_GOP;
public static final int CODEC_FLAG2_FAST =          AV_CODEC_FLAG2_FAST;
public static final int CODEC_FLAG2_NO_OUTPUT =     AV_CODEC_FLAG2_NO_OUTPUT;
public static final int CODEC_FLAG2_LOCAL_HEADER =  AV_CODEC_FLAG2_LOCAL_HEADER;
public static final int CODEC_FLAG2_DROP_FRAME_TIMECODE = AV_CODEC_FLAG2_DROP_FRAME_TIMECODE;
public static final int CODEC_FLAG2_IGNORE_CROP =   AV_CODEC_FLAG2_IGNORE_CROP;

public static final int CODEC_FLAG2_CHUNKS =        AV_CODEC_FLAG2_CHUNKS;
public static final int CODEC_FLAG2_SHOW_ALL =      AV_CODEC_FLAG2_SHOW_ALL;
public static final int CODEC_FLAG2_EXPORT_MVS =    AV_CODEC_FLAG2_EXPORT_MVS;
public static final int CODEC_FLAG2_SKIP_MANUAL =   AV_CODEC_FLAG2_SKIP_MANUAL;

/* Unsupported options :
 *              Syntax Arithmetic coding (SAC)
 *              Reference Picture Selection
 *              Independent Segment Decoding */
/* /Fx */
/* codec capabilities */

/** Decoder can use draw_horiz_band callback. */
public static final int CODEC_CAP_DRAW_HORIZ_BAND = AV_CODEC_CAP_DRAW_HORIZ_BAND;
/**
 * Codec uses get_buffer() for allocating buffers and supports custom allocators.
 * If not set, it might not use get_buffer() at all or use operations that
 * assume the buffer was allocated by avcodec_default_get_buffer.
 */
public static final int CODEC_CAP_DR1 =             AV_CODEC_CAP_DR1;
public static final int CODEC_CAP_TRUNCATED =       AV_CODEC_CAP_TRUNCATED;
// #if FF_API_XVMC
/* Codec can export data for HW decoding. This flag indicates that
 * the codec would call get_format() with list that might contain HW accelerated
 * pixel formats (XvMC, VDPAU, VAAPI, etc). The application can pick any of them
 * including raw image format.
 * The application can use the passed context to determine bitstream version,
 * chroma format, resolution etc.
 */
public static final int CODEC_CAP_HWACCEL =         0x0010;
// #endif /* FF_API_XVMC */
/**
 * Encoder or decoder requires flushing with NULL input at the end in order to
 * give the complete and correct output.
 *
 * NOTE: If this flag is not set, the codec is guaranteed to never be fed with
 *       with NULL data. The user can still send NULL data to the public encode
 *       or decode function, but libavcodec will not pass it along to the codec
 *       unless this flag is set.
 *
 * Decoders:
 * The decoder has a non-zero delay and needs to be fed with avpkt->data=NULL,
 * avpkt->size=0 at the end to get the delayed data until the decoder no longer
 * returns frames.
 *
 * Encoders:
 * The encoder needs to be fed with NULL data at the end of encoding until the
 * encoder no longer returns data.
 *
 * NOTE: For encoders implementing the AVCodec.encode2() function, setting this
 *       flag also means that the encoder must set the pts and duration for
 *       each output packet. If this flag is not set, the pts and duration will
 *       be determined by libavcodec from the input frame.
 */
public static final int CODEC_CAP_DELAY =           AV_CODEC_CAP_DELAY;
/**
 * Codec can be fed a final frame with a smaller size.
 * This can be used to prevent truncation of the last audio samples.
 */
public static final int CODEC_CAP_SMALL_LAST_FRAME = AV_CODEC_CAP_SMALL_LAST_FRAME;
// #if FF_API_CAP_VDPAU
/**
 * Codec can export data for HW decoding (VDPAU).
 */
public static final int CODEC_CAP_HWACCEL_VDPAU =    AV_CODEC_CAP_HWACCEL_VDPAU;
// #endif
/**
 * Codec can output multiple frames per AVPacket
 * Normally demuxers return one frame at a time, demuxers which do not do
 * are connected to a parser to split what they return into proper frames.
 * This flag is reserved to the very rare category of codecs which have a
 * bitstream that cannot be split into frames without timeconsuming
 * operations like full decoding. Demuxers carring such bitstreams thus
 * may return multiple frames in a packet. This has many disadvantages like
 * prohibiting stream copy in many cases thus it should only be considered
 * as a last resort.
 */
public static final int CODEC_CAP_SUBFRAMES =        AV_CODEC_CAP_SUBFRAMES;
/**
 * Codec is experimental and is thus avoided in favor of non experimental
 * encoders
 */
public static final int CODEC_CAP_EXPERIMENTAL =     AV_CODEC_CAP_EXPERIMENTAL;
/**
 * Codec should fill in channel configuration and samplerate instead of container
 */
public static final int CODEC_CAP_CHANNEL_CONF =     AV_CODEC_CAP_CHANNEL_CONF;
// #if FF_API_NEG_LINESIZES
/**
 * @deprecated no codecs use this capability
 */
public static final int CODEC_CAP_NEG_LINESIZES =    0x0800;
// #endif
/**
 * Codec supports frame-level multithreading.
 */
public static final int CODEC_CAP_FRAME_THREADS =    AV_CODEC_CAP_FRAME_THREADS;
/**
 * Codec supports slice-based (or partition-based) multithreading.
 */
public static final int CODEC_CAP_SLICE_THREADS =    AV_CODEC_CAP_SLICE_THREADS;
/**
 * Codec supports changed parameters at any point.
 */
public static final int CODEC_CAP_PARAM_CHANGE =     AV_CODEC_CAP_PARAM_CHANGE;
/**
 * Codec supports avctx->thread_count == 0 (auto).
 */
public static final int CODEC_CAP_AUTO_THREADS =     AV_CODEC_CAP_AUTO_THREADS;
/**
 * Audio encoder supports receiving a different number of samples in each call.
 */
public static final int CODEC_CAP_VARIABLE_FRAME_SIZE = AV_CODEC_CAP_VARIABLE_FRAME_SIZE;
/**
 * Codec is intra only.
 */
public static final int CODEC_CAP_INTRA_ONLY =       AV_CODEC_CAP_INTRA_ONLY;
/**
 * Codec is lossless.
 */
public static final int CODEC_CAP_LOSSLESS =         AV_CODEC_CAP_LOSSLESS;

/**
 * HWAccel is experimental and is thus avoided in favor of non experimental
 * codecs
 */
public static final int HWACCEL_CODEC_CAP_EXPERIMENTAL =     0x0200;
// #endif /* FF_API_WITHOUT_PREFIX */

// #if FF_API_MB_TYPE
//The following defines may change, don't expect compatibility if you use them.
public static final int MB_TYPE_INTRA4x4 =   0x0001;
public static final int MB_TYPE_INTRA16x16 = 0x0002; //FIXME H.264-specific
public static final int MB_TYPE_INTRA_PCM =  0x0004; //FIXME H.264-specific
public static final int MB_TYPE_16x16 =      0x0008;
public static final int MB_TYPE_16x8 =       0x0010;
public static final int MB_TYPE_8x16 =       0x0020;
public static final int MB_TYPE_8x8 =        0x0040;
public static final int MB_TYPE_INTERLACED = 0x0080;
public static final int MB_TYPE_DIRECT2 =    0x0100; //FIXME
public static final int MB_TYPE_ACPRED =     0x0200;
public static final int MB_TYPE_GMC =        0x0400;
public static final int MB_TYPE_SKIP =       0x0800;
public static final int MB_TYPE_P0L0 =       0x1000;
public static final int MB_TYPE_P1L0 =       0x2000;
public static final int MB_TYPE_P0L1 =       0x4000;
public static final int MB_TYPE_P1L1 =       0x8000;
public static final int MB_TYPE_L0 =         (MB_TYPE_P0L0 | MB_TYPE_P1L0);
public static final int MB_TYPE_L1 =         (MB_TYPE_P0L1 | MB_TYPE_P1L1);
public static final int MB_TYPE_L0L1 =       (MB_TYPE_L0   | MB_TYPE_L1);
public static final int MB_TYPE_QUANT =      0x00010000;
public static final int MB_TYPE_CBP =        0x00020000;
//Note bits 24-31 are reserved for codec specific use (h264 ref0, mpeg1 0mv, ...)
// #endif

/**
 * Pan Scan area.
 * This specifies the area which should be displayed.
 * Note there may be multiple such areas for one frame.
 */
public static class AVPanScan extends Pointer {
    static { Loader.load(); }
    /** Default native constructor. */
    public AVPanScan() { allocate(); }
    /** Native array allocator. Access with {@link Pointer#position(int)}. */
    public AVPanScan(int size) { allocateArray(size); }
    /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
    public AVPanScan(Pointer p) { super(p); }
    private native void allocate();
    private native void allocateArray(int size);
    @Override public AVPanScan position(int position) {
        return (AVPanScan)super.position(position);
    }

    /**
     * id
     * - encoding: Set by user.
     * - decoding: Set by libavcodec.
     */
    public native int id(); public native AVPanScan id(int id);

    /**
     * width and height in 1/16 pel
     * - encoding: Set by user.
     * - decoding: Set by libavcodec.
     */
    public native int width(); public native AVPanScan width(int width);
    public native int height(); public native AVPanScan height(int height);

    /**
     * position of the top left corner in 1/16 pel for up to 3 fields/frames
     * - encoding: Set by user.
     * - decoding: Set by libavcodec.
     */
    public native @Name("position") short _position(int i, int j); public native AVPanScan _position(int i, int j, short _position);
    @MemberGetter public native @Cast("int16_t(*)[2]") @Name("position") ShortPointer _position();
}

/**
 * This structure describes the bitrate properties of an encoded bitstream. It
 * roughly corresponds to a subset the VBV parameters for MPEG-2 or HRD
 * parameters for H.264/HEVC.
 */
public static class AVCPBProperties extends Pointer {
    static { Loader.load(); }
    /** Default native constructor. */
    public AVCPBProperties() { allocate(); }
    /** Native array allocator. Access with {@link Pointer#position(int)}. */
    public AVCPBProperties(int size) { allocateArray(size); }
    /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
    public AVCPBProperties(Pointer p) { super(p); }
    private native void allocate();
    private native void allocateArray(int size);
    @Override public AVCPBProperties position(int position) {
        return (AVCPBProperties)super.position(position);
    }

    /**
     * Maximum bitrate of the stream, in bits per second.
     * Zero if unknown or unspecified.
     */
    public native int max_bitrate(); public native AVCPBProperties max_bitrate(int max_bitrate);
    /**
     * Minimum bitrate of the stream, in bits per second.
     * Zero if unknown or unspecified.
     */
    public native int min_bitrate(); public native AVCPBProperties min_bitrate(int min_bitrate);
    /**
     * Average bitrate of the stream, in bits per second.
     * Zero if unknown or unspecified.
     */
    public native int avg_bitrate(); public native AVCPBProperties avg_bitrate(int avg_bitrate);

    /**
     * The size of the buffer to which the ratecontrol is applied, in bits.
     * Zero if unknown or unspecified.
     */
    public native int buffer_size(); public native AVCPBProperties buffer_size(int buffer_size);

    /**
     * The delay between the time the packet this structure is associated with
     * is received and the time when it should be decoded, in periods of a 27MHz
     * clock.
     *
     * UINT64_MAX when unknown or unspecified.
     */
    public native @Cast("uint64_t") long vbv_delay(); public native AVCPBProperties vbv_delay(long vbv_delay);
}

// #if FF_API_QSCALE_TYPE
public static final int FF_QSCALE_TYPE_MPEG1 = 0;
public static final int FF_QSCALE_TYPE_MPEG2 = 1;
public static final int FF_QSCALE_TYPE_H264 =  2;
public static final int FF_QSCALE_TYPE_VP56 =  3;
// #endif

/**
 * The decoder will keep a reference to the frame and may reuse it later.
 */
public static final int AV_GET_BUFFER_FLAG_REF = (1 << 0);

/**
 * @defgroup lavc_packet AVPacket
 *
 * Types and functions for working with AVPacket.
 * @{
 */
/** enum AVPacketSideDataType */
public static final int
    AV_PKT_DATA_PALETTE = 0,
    AV_PKT_DATA_NEW_EXTRADATA = 1,

    /**
     * An AV_PKT_DATA_PARAM_CHANGE side data packet is laid out as follows:
     * @code
     * u32le param_flags
     * if (param_flags & AV_SIDE_DATA_PARAM_CHANGE_CHANNEL_COUNT)
     *     s32le channel_count
     * if (param_flags & AV_SIDE_DATA_PARAM_CHANGE_CHANNEL_LAYOUT)
     *     u64le channel_layout
     * if (param_flags & AV_SIDE_DATA_PARAM_CHANGE_SAMPLE_RATE)
     *     s32le sample_rate
     * if (param_flags & AV_SIDE_DATA_PARAM_CHANGE_DIMENSIONS)
     *     s32le width
     *     s32le height
     * @endcode
     */
    AV_PKT_DATA_PARAM_CHANGE = 2,

    /**
     * An AV_PKT_DATA_H263_MB_INFO side data packet contains a number of
     * structures with info about macroblocks relevant to splitting the
     * packet into smaller packets on macroblock edges (e.g. as for RFC 2190).
     * That is, it does not necessarily contain info about all macroblocks,
     * as long as the distance between macroblocks in the info is smaller
     * than the target payload size.
     * Each MB info structure is 12 bytes, and is laid out as follows:
     * @code
     * u32le bit offset from the start of the packet
     * u8    current quantizer at the start of the macroblock
     * u8    GOB number
     * u16le macroblock address within the GOB
     * u8    horizontal MV predictor
     * u8    vertical MV predictor
     * u8    horizontal MV predictor for block number 3
     * u8    vertical MV predictor for block number 3
     * @endcode
     */
    AV_PKT_DATA_H263_MB_INFO = 3,

    /**
     * This side data should be associated with an audio stream and contains
     * ReplayGain information in form of the AVReplayGain struct.
     */
    AV_PKT_DATA_REPLAYGAIN = 4,

    /**
     * This side data contains a 3x3 transformation matrix describing an affine
     * transformation that needs to be applied to the decoded video frames for
     * correct presentation.
     *
     * See libavutil/display.h for a detailed description of the data.
     */
    AV_PKT_DATA_DISPLAYMATRIX = 5,

    /**
     * This side data should be associated with a video stream and contains
     * Stereoscopic 3D information in form of the AVStereo3D struct.
     */
    AV_PKT_DATA_STEREO3D = 6,

    /**
     * This side data should be associated with an audio stream and corresponds
     * to enum AVAudioServiceType.
     */
    AV_PKT_DATA_AUDIO_SERVICE_TYPE = 7,

    /**
     * This side data contains quality related information from the encoder.
     * @code
     * u32le quality factor of the compressed frame. Allowed range is between 1 (good) and FF_LAMBDA_MAX (bad).
     * u8    picture type
     * u8    error count
     * u16   reserved
     * u64le[error count] sum of squared differences between encoder in and output
     * @endcode
     */
    AV_PKT_DATA_QUALITY_STATS = 8,

    /**
     * This side data contains an integer value representing the stream index
     * of a "fallback" track.  A fallback track indicates an alternate
     * track to use when the current track can not be decoded for some reason.
     * e.g. no decoder available for codec.
     */
    AV_PKT_DATA_FALLBACK_TRACK = 9,

    /**
     * This side data corresponds to the AVCPBProperties struct.
     */
    AV_PKT_DATA_CPB_PROPERTIES = 10,

    /**
     * Recommmends skipping the specified number of samples
     * @code
     * u32le number of samples to skip from start of this packet
     * u32le number of samples to skip from end of this packet
     * u8    reason for start skip
     * u8    reason for end   skip (0=padding silence, 1=convergence)
     * @endcode
     */
    AV_PKT_DATA_SKIP_SAMPLES= 70,

    /**
     * An AV_PKT_DATA_JP_DUALMONO side data packet indicates that
     * the packet may contain "dual mono" audio specific to Japanese DTV
     * and if it is true, recommends only the selected channel to be used.
     * @code
     * u8    selected channels (0=mail/left, 1=sub/right, 2=both)
     * @endcode
     */
    AV_PKT_DATA_JP_DUALMONO = 71,

    /**
     * A list of zero terminated key/value strings. There is no end marker for
     * the list, so it is required to rely on the side data size to stop.
     */
    AV_PKT_DATA_STRINGS_METADATA = 72,

    /**
     * Subtitle event position
     * @code
     * u32le x1
     * u32le y1
     * u32le x2
     * u32le y2
     * @endcode
     */
    AV_PKT_DATA_SUBTITLE_POSITION = 73,

    /**
     * Data found in BlockAdditional element of matroska container. There is
     * no end marker for the data, so it is required to rely on the side data
     * size to recognize the end. 8 byte id (as found in BlockAddId) followed
     * by data.
     */
    AV_PKT_DATA_MATROSKA_BLOCKADDITIONAL = 74,

    /**
     * The optional first identifier line of a WebVTT cue.
     */
    AV_PKT_DATA_WEBVTT_IDENTIFIER = 75,

    /**
     * The optional settings (rendering instructions) that immediately
     * follow the timestamp specifier of a WebVTT cue.
     */
    AV_PKT_DATA_WEBVTT_SETTINGS = 76,

    /**
     * A list of zero terminated key/value strings. There is no end marker for
     * the list, so it is required to rely on the side data size to stop. This
     * side data includes updated metadata which appeared in the stream.
     */
    AV_PKT_DATA_METADATA_UPDATE = 77;

public static final int AV_PKT_DATA_QUALITY_FACTOR = AV_PKT_DATA_QUALITY_STATS; //DEPRECATED

public static class AVPacketSideData extends Pointer {
    static { Loader.load(); }
    /** Default native constructor. */
    public AVPacketSideData() { allocate(); }
    /** Native array allocator. Access with {@link Pointer#position(int)}. */
    public AVPacketSideData(int size) { allocateArray(size); }
    /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
    public AVPacketSideData(Pointer p) { super(p); }
    private native void allocate();
    private native void allocateArray(int size);
    @Override public AVPacketSideData position(int position) {
        return (AVPacketSideData)super.position(position);
    }

    public native @Cast("uint8_t*") BytePointer data(); public native AVPacketSideData data(BytePointer data);
    public native int size(); public native AVPacketSideData size(int size);
    public native @Cast("AVPacketSideDataType") int type(); public native AVPacketSideData type(int type);
}

/**
 * This structure stores compressed data. It is typically exported by demuxers
 * and then passed as input to decoders, or received as output from encoders and
 * then passed to muxers.
 *
 * For video, it should typically contain one compressed frame. For audio it may
 * contain several compressed frames. Encoders are allowed to output empty
 * packets, with no compressed data, containing only side data
 * (e.g. to update some stream parameters at the end of encoding).
 *
 * AVPacket is one of the few structs in FFmpeg, whose size is a part of public
 * ABI. Thus it may be allocated on stack and no new fields can be added to it
 * without libavcodec and libavformat major bump.
 *
 * The semantics of data ownership depends on the buf field.
 * If it is set, the packet data is dynamically allocated and is
 * valid indefinitely until a call to av_packet_unref() reduces the
 * reference count to 0.
 *
 * If the buf field is not set av_packet_ref() would make a copy instead
 * of increasing the reference count.
 *
 * The side data is always allocated with av_malloc(), copied by
 * av_packet_ref() and freed by av_packet_unref().
 *
 * @see av_packet_ref
 * @see av_packet_unref
 */
public static class AVPacket extends Pointer {
    static { Loader.load(); }
    /** Default native constructor. */
    public AVPacket() { allocate(); }
    /** Native array allocator. Access with {@link Pointer#position(int)}. */
    public AVPacket(int size) { allocateArray(size); }
    /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
    public AVPacket(Pointer p) { super(p); }
    private native void allocate();
    private native void allocateArray(int size);
    @Override public AVPacket position(int position) {
        return (AVPacket)super.position(position);
    }

    /**
     * A reference to the reference-counted buffer where the packet data is
     * stored.
     * May be NULL, then the packet data is not reference-counted.
     */
    public native AVBufferRef buf(); public native AVPacket buf(AVBufferRef buf);
    /**
     * Presentation timestamp in AVStream->time_base units; the time at which
     * the decompressed packet will be presented to the user.
     * Can be AV_NOPTS_VALUE if it is not stored in the file.
     * pts MUST be larger or equal to dts as presentation cannot happen before
     * decompression, unless one wants to view hex dumps. Some formats misuse
     * the terms dts and pts/cts to mean something different. Such timestamps
     * must be converted to true pts/dts before they are stored in AVPacket.
     */
    public native long pts(); public native AVPacket pts(long pts);
    /**
     * Decompression timestamp in AVStream->time_base units; the time at which
     * the packet is decompressed.
     * Can be AV_NOPTS_VALUE if it is not stored in the file.
     */
    public native long dts(); public native AVPacket dts(long dts);
    public native @Cast("uint8_t*") BytePointer data(); public native AVPacket data(BytePointer data);
    public native int size(); public native AVPacket size(int size);
    public native int stream_index(); public native AVPacket stream_index(int stream_index);
    /**
     * A combination of AV_PKT_FLAG values
     */
    public native int flags(); public native AVPacket flags(int flags);
    /**
     * Additional packet data that can be provided by the container.
     * Packet can contain several types of side information.
     */
    public native AVPacketSideData side_data(); public native AVPacket side_data(AVPacketSideData side_data);
    public native int side_data_elems(); public native AVPacket side_data_elems(int side_data_elems);

    /**
     * Duration of this packet in AVStream->time_base units, 0 if unknown.
     * Equals next_pts - this_pts in presentation order.
     */
    public native long duration(); public native AVPacket duration(long duration);

    /** byte position in stream, -1 if unknown */
    public native long pos(); public native AVPacket pos(long pos);

// #if FF_API_CONVERGENCE_DURATION
    /**
     * @deprecated Same as the duration field, but as int64_t. This was required
     * for Matroska subtitles, whose duration values could overflow when the
     * duration field was still an int.
     */
    public native @Deprecated long convergence_duration(); public native AVPacket convergence_duration(long convergence_duration);
// #endif
}
/** The packet contains a keyframe */
public static final int AV_PKT_FLAG_KEY =     0x0001;
/** The packet content is corrupted */
public static final int AV_PKT_FLAG_CORRUPT = 0x0002;

/** enum AVSideDataParamChangeFlags */
public static final int
    AV_SIDE_DATA_PARAM_CHANGE_CHANNEL_COUNT  =  0x0001,
    AV_SIDE_DATA_PARAM_CHANGE_CHANNEL_LAYOUT =  0x0002,
    AV_SIDE_DATA_PARAM_CHANGE_SAMPLE_RATE    =  0x0004,
    AV_SIDE_DATA_PARAM_CHANGE_DIMENSIONS     =  0x0008;
/**
 * @}
 */

@Opaque public static class AVCodecInternal extends Pointer {
    /** Empty constructor. */
    public AVCodecInternal() { }
    /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
    public AVCodecInternal(Pointer p) { super(p); }
}

/** enum AVFieldOrder */
public static final int
    AV_FIELD_UNKNOWN = 0,
    AV_FIELD_PROGRESSIVE = 1,
    AV_FIELD_TT = 2,          //< Top coded_first, top displayed first
    AV_FIELD_BB = 3,          //< Bottom coded first, bottom displayed first
    AV_FIELD_TB = 4,          //< Top coded first, bottom displayed first
    AV_FIELD_BT = 5;          //< Bottom coded first, top displayed first

/**
 * main external API structure.
 * New fields can be added to the end with minor version bumps.
 * Removal, reordering and changes to existing fields require a major
 * version bump.
 * Please use AVOptions (av_opt* / av_set/get*()) to access these fields from user
 * applications.
 * sizeof(AVCodecContext) must not be used outside libav*.
 */
public static class AVCodecContext extends Pointer {
    static { Loader.load(); }
    /** Default native constructor. */
    public AVCodecContext() { allocate(); }
    /** Native array allocator. Access with {@link Pointer#position(int)}. */
    public AVCodecContext(int size) { allocateArray(size); }
    /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
    public AVCodecContext(Pointer p) { super(p); }
    private native void allocate();
    private native void allocateArray(int size);
    @Override public AVCodecContext position(int position) {
        return (AVCodecContext)super.position(position);
    }

    /**
     * information on struct for av_log
     * - set by avcodec_alloc_context3
     */
    @MemberGetter public native @Const AVClass av_class();
    public native int log_level_offset(); public native AVCodecContext log_level_offset(int log_level_offset);

    public native @Cast("AVMediaType") int codec_type(); public native AVCodecContext codec_type(int codec_type); /* see AVMEDIA_TYPE_xxx */
    @MemberGetter public native @Const AVCodec codec();
// #if FF_API_CODEC_NAME
    /**
     * @deprecated this field is not used for anything in libavcodec
     */
    public native @Cast("char") @Deprecated byte codec_name(int i); public native AVCodecContext codec_name(int i, byte codec_name);
    @MemberGetter public native @Cast("char*") @Deprecated BytePointer codec_name();
// #endif
    public native @Cast("AVCodecID") int codec_id(); public native AVCodecContext codec_id(int codec_id); /* see AV_CODEC_ID_xxx */

    /**
     * fourcc (LSB first, so "ABCD" -> ('D'<<24) + ('C'<<16) + ('B'<<8) + 'A').
     * This is used to work around some encoder bugs.
     * A demuxer should set this to what is stored in the field used to identify the codec.
     * If there are multiple such fields in a container then the demuxer should choose the one
     * which maximizes the information about the used codec.
     * If the codec tag field in a container is larger than 32 bits then the demuxer should
     * remap the longer ID to 32 bits with a table or other structure. Alternatively a new
     * extra_codec_tag + size could be added but for this a clear advantage must be demonstrated
     * first.
     * - encoding: Set by user, if not then the default based on codec_id will be used.
     * - decoding: Set by user, will be converted to uppercase by libavcodec during init.
     */
    public native @Cast("unsigned int") int codec_tag(); public native AVCodecContext codec_tag(int codec_tag);

// #if FF_API_STREAM_CODEC_TAG
    /**
     * @deprecated this field is unused
     */
    public native @Cast("unsigned int") @Deprecated int stream_codec_tag(); public native AVCodecContext stream_codec_tag(int stream_codec_tag);
// #endif

    public native Pointer priv_data(); public native AVCodecContext priv_data(Pointer priv_data);

    /**
     * Private context used for internal data.
     *
     * Unlike priv_data, this is not codec-specific. It is used in general
     * libavcodec functions.
     */
    public native AVCodecInternal internal(); public native AVCodecContext internal(AVCodecInternal internal);

    /**
     * Private data of the user, can be used to carry app specific stuff.
     * - encoding: Set by user.
     * - decoding: Set by user.
     */
    public native Pointer opaque(); public native AVCodecContext opaque(Pointer opaque);

    /**
     * the average bitrate
     * - encoding: Set by user; unused for constant quantizer encoding.
     * - decoding: Set by user, may be overwritten by libavcodec
     *             if this info is available in the stream
     */
    public native long bit_rate(); public native AVCodecContext bit_rate(long bit_rate);

    /**
     * number of bits the bitstream is allowed to diverge from the reference.
     *           the reference can be CBR (for CBR pass1) or VBR (for pass2)
     * - encoding: Set by user; unused for constant quantizer encoding.
     * - decoding: unused
     */
    public native int bit_rate_tolerance(); public native AVCodecContext bit_rate_tolerance(int bit_rate_tolerance);

    /**
     * Global quality for codecs which cannot change it per frame.
     * This should be proportional to MPEG-1/2/4 qscale.
     * - encoding: Set by user.
     * - decoding: unused
     */
    public native int global_quality(); public native AVCodecContext global_quality(int global_quality);

    /**
     * - encoding: Set by user.
     * - decoding: unused
     */
    public native int compression_level(); public native AVCodecContext compression_level(int compression_level);
public static final int FF_COMPRESSION_DEFAULT = -1;

    /**
     * AV_CODEC_FLAG_*.
     * - encoding: Set by user.
     * - decoding: Set by user.
     */
    public native int flags(); public native AVCodecContext flags(int flags);

    /**
     * AV_CODEC_FLAG2_*
     * - encoding: Set by user.
     * - decoding: Set by user.
     */
    public native int flags2(); public native AVCodecContext flags2(int flags2);

    /**
     * some codecs need / can use extradata like Huffman tables.
     * mjpeg: Huffman tables
     * rv10: additional flags
     * mpeg4: global headers (they can be in the bitstream or here)
     * The allocated memory should be AV_INPUT_BUFFER_PADDING_SIZE bytes larger
     * than extradata_size to avoid problems if it is read with the bitstream reader.
     * The bytewise contents of extradata must not depend on the architecture or CPU endianness.
     * - encoding: Set/allocated/freed by libavcodec.
     * - decoding: Set/allocated/freed by user.
     */
    public native @Cast("uint8_t*") BytePointer extradata(); public native AVCodecContext extradata(BytePointer extradata);
    public native int extradata_size(); public native AVCodecContext extradata_size(int extradata_size);

    /**
     * This is the fundamental unit of time (in seconds) in terms
     * of which frame timestamps are represented. For fixed-fps content,
     * timebase should be 1/framerate and timestamp increments should be
     * identically 1.
     * This often, but not always is the inverse of the frame rate or field rate
     * for video.
     * - encoding: MUST be set by user.
     * - decoding: the use of this field for decoding is deprecated.
     *             Use framerate instead.
     */
    public native @ByRef AVRational time_base(); public native AVCodecContext time_base(AVRational time_base);

    /**
     * For some codecs, the time base is closer to the field rate than the frame rate.
     * Most notably, H.264 and MPEG-2 specify time_base as half of frame duration
     * if no telecine is used ...
     *
     * Set to time_base ticks per frame. Default 1, e.g., H.264/MPEG-2 set it to 2.
     */
    public native int ticks_per_frame(); public native AVCodecContext ticks_per_frame(int ticks_per_frame);

    /**
     * Codec delay.
     *
     * Encoding: Number of frames delay there will be from the encoder input to
     *           the decoder output. (we assume the decoder matches the spec)
     * Decoding: Number of frames delay in addition to what a standard decoder
     *           as specified in the spec would produce.
     *
     * Video:
     *   Number of frames the decoded output will be delayed relative to the
     *   encoded input.
     *
     * Audio:
     *   For encoding, this field is unused (see initial_padding).
     *
     *   For decoding, this is the number of samples the decoder needs to
     *   output before the decoder's output is valid. When seeking, you should
     *   start decoding this many samples prior to your desired seek point.
     *
     * - encoding: Set by libavcodec.
     * - decoding: Set by libavcodec.
     */
    public native int delay(); public native AVCodecContext delay(int delay);


    /* video only */
    /**
     * picture width / height.
     *
     * @note Those fields may not match the values of the last
     * AVFrame outputted by avcodec_decode_video2 due frame
     * reordering.
     *
     * - encoding: MUST be set by user.
     * - decoding: May be set by the user before opening the decoder if known e.g.
     *             from the container. Some decoders will require the dimensions
     *             to be set by the caller. During decoding, the decoder may
     *             overwrite those values as required while parsing the data.
     */
    public native int width(); public native AVCodecContext width(int width);
    public native int height(); public native AVCodecContext height(int height);

    /**
     * Bitstream width / height, may be different from width/height e.g. when
     * the decoded frame is cropped before being output or lowres is enabled.
     *
     * @note Those field may not match the value of the last
     * AVFrame outputted by avcodec_decode_video2 due frame
     * reordering.
     *
     * - encoding: unused
     * - decoding: May be set by the user before opening the decoder if known
     *             e.g. from the container. During decoding, the decoder may
     *             overwrite those values as required while parsing the data.
     */
    public native int coded_width(); public native AVCodecContext coded_width(int coded_width);
    public native int coded_height(); public native AVCodecContext coded_height(int coded_height);

// #if FF_API_ASPECT_EXTENDED
public static final int FF_ASPECT_EXTENDED = 15;
// #endif

    /**
     * the number of pictures in a group of pictures, or 0 for intra_only
     * - encoding: Set by user.
     * - decoding: unused
     */
    public native int gop_size(); public native AVCodecContext gop_size(int gop_size);

    /**
     * Pixel format, see AV_PIX_FMT_xxx.
     * May be set by the demuxer if known from headers.
     * May be overridden by the decoder if it knows better.
     *
     * @note This field may not match the value of the last
     * AVFrame outputted by avcodec_decode_video2 due frame
     * reordering.
     *
     * - encoding: Set by user.
     * - decoding: Set by user if known, overridden by libavcodec while
     *             parsing the data.
     */
    public native @Cast("AVPixelFormat") int pix_fmt(); public native AVCodecContext pix_fmt(int pix_fmt);

// #if FF_API_MOTION_EST
    /**
     * This option does nothing
     * @deprecated use codec private options instead
     */
    public native @Deprecated int me_method(); public native AVCodecContext me_method(int me_method);
// #endif

    /**
     * If non NULL, 'draw_horiz_band' is called by the libavcodec
     * decoder to draw a horizontal band. It improves cache usage. Not
     * all codecs can do that. You must check the codec capabilities
     * beforehand.
     * When multithreading is used, it may be called from multiple threads
     * at the same time; threads might draw different parts of the same AVFrame,
     * or multiple AVFrames, and there is no guarantee that slices will be drawn
     * in order.
     * The function is also used by hardware acceleration APIs.
     * It is called at least once during frame decoding to pass
     * the data needed for hardware render.
     * In that mode instead of pixel data, AVFrame points to
     * a structure specific to the acceleration API. The application
     * reads the structure and can change some fields to indicate progress
     * or mark state.
     * - encoding: unused
     * - decoding: Set by user.
     * @param height the height of the slice
     * @param y the y position of the slice
     * @param type 1->top field, 2->bottom field, 3->frame
     * @param offset offset into the AVFrame.data from which the slice should be read
     */
    public static class Draw_horiz_band_AVCodecContext_AVFrame_IntPointer_int_int_int extends FunctionPointer {
        static { Loader.load(); }
        /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
        public    Draw_horiz_band_AVCodecContext_AVFrame_IntPointer_int_int_int(Pointer p) { super(p); }
        protected Draw_horiz_band_AVCodecContext_AVFrame_IntPointer_int_int_int() { allocate(); }
        private native void allocate();
        public native void call(AVCodecContext s,
                                @Const AVFrame src, IntPointer offset,
                                int y, int type, int height);
    }
    public native Draw_horiz_band_AVCodecContext_AVFrame_IntPointer_int_int_int draw_horiz_band(); public native AVCodecContext draw_horiz_band(Draw_horiz_band_AVCodecContext_AVFrame_IntPointer_int_int_int draw_horiz_band);

    /**
     * callback to negotiate the pixelFormat
     * @param fmt is the list of formats which are supported by the codec,
     * it is terminated by -1 as 0 is a valid format, the formats are ordered by quality.
     * The first is always the native one.
     * @note The callback may be called again immediately if initialization for
     * the selected (hardware-accelerated) pixel format failed.
     * @warning Behavior is undefined if the callback returns a value not
     * in the fmt list of formats.
     * @return the chosen format
     * - encoding: unused
     * - decoding: Set by user, if not set the native format will be chosen.
     */
    public static class Get_format_AVCodecContext_IntPointer extends FunctionPointer {
        static { Loader.load(); }
        /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
        public    Get_format_AVCodecContext_IntPointer(Pointer p) { super(p); }
        protected Get_format_AVCodecContext_IntPointer() { allocate(); }
        private native void allocate();
        public native @Cast("AVPixelFormat") int call(AVCodecContext s, @Cast("const AVPixelFormat*") IntPointer fmt);
    }
    public native Get_format_AVCodecContext_IntPointer get_format(); public native AVCodecContext get_format(Get_format_AVCodecContext_IntPointer get_format);

    /**
     * maximum number of B-frames between non-B-frames
     * Note: The output will be delayed by max_b_frames+1 relative to the input.
     * - encoding: Set by user.
     * - decoding: unused
     */
    public native int max_b_frames(); public native AVCodecContext max_b_frames(int max_b_frames);

    /**
     * qscale factor between IP and B-frames
     * If > 0 then the last P-frame quantizer will be used (q= lastp_q*factor+offset).
     * If < 0 then normal ratecontrol will be done (q= -normal_q*factor+offset).
     * - encoding: Set by user.
     * - decoding: unused
     */
    public native float b_quant_factor(); public native AVCodecContext b_quant_factor(float b_quant_factor);

// #if FF_API_RC_STRATEGY
    /** @deprecated use codec private option instead */
    public native @Deprecated int rc_strategy(); public native AVCodecContext rc_strategy(int rc_strategy);
public static final int FF_RC_STRATEGY_XVID = 1;
// #endif

// #if FF_API_PRIVATE_OPT
    /** @deprecated use encoder private options instead */
    public native @Deprecated int b_frame_strategy(); public native AVCodecContext b_frame_strategy(int b_frame_strategy);
// #endif

    /**
     * qscale offset between IP and B-frames
     * - encoding: Set by user.
     * - decoding: unused
     */
    public native float b_quant_offset(); public native AVCodecContext b_quant_offset(float b_quant_offset);

    /**
     * Size of the frame reordering buffer in the decoder.
     * For MPEG-2 it is 1 IPB or 0 low delay IP.
     * - encoding: Set by libavcodec.
     * - decoding: Set by libavcodec.
     */
    public native int has_b_frames(); public native AVCodecContext has_b_frames(int has_b_frames);

// #if FF_API_PRIVATE_OPT
    /** @deprecated use encoder private options instead */
    public native @Deprecated int mpeg_quant(); public native AVCodecContext mpeg_quant(int mpeg_quant);
// #endif

    /**
     * qscale factor between P and I-frames
     * If > 0 then the last p frame quantizer will be used (q= lastp_q*factor+offset).
     * If < 0 then normal ratecontrol will be done (q= -normal_q*factor+offset).
     * - encoding: Set by user.
     * - decoding: unused
     */
    public native float i_quant_factor(); public native AVCodecContext i_quant_factor(float i_quant_factor);

    /**
     * qscale offset between P and I-frames
     * - encoding: Set by user.
     * - decoding: unused
     */
    public native float i_quant_offset(); public native AVCodecContext i_quant_offset(float i_quant_offset);

    /**
     * luminance masking (0-> disabled)
     * - encoding: Set by user.
     * - decoding: unused
     */
    public native float lumi_masking(); public native AVCodecContext lumi_masking(float lumi_masking);

    /**
     * temporary complexity masking (0-> disabled)
     * - encoding: Set by user.
     * - decoding: unused
     */
    public native float temporal_cplx_masking(); public native AVCodecContext temporal_cplx_masking(float temporal_cplx_masking);

    /**
     * spatial complexity masking (0-> disabled)
     * - encoding: Set by user.
     * - decoding: unused
     */
    public native float spatial_cplx_masking(); public native AVCodecContext spatial_cplx_masking(float spatial_cplx_masking);

    /**
     * p block masking (0-> disabled)
     * - encoding: Set by user.
     * - decoding: unused
     */
    public native float p_masking(); public native AVCodecContext p_masking(float p_masking);

    /**
     * darkness masking (0-> disabled)
     * - encoding: Set by user.
     * - decoding: unused
     */
    public native float dark_masking(); public native AVCodecContext dark_masking(float dark_masking);

    /**
     * slice count
     * - encoding: Set by libavcodec.
     * - decoding: Set by user (or 0).
     */
    public native int slice_count(); public native AVCodecContext slice_count(int slice_count);

// #if FF_API_PRIVATE_OPT
    /** @deprecated use encoder private options instead */
    public native @Deprecated int prediction_method(); public native AVCodecContext prediction_method(int prediction_method);
public static final int FF_PRED_LEFT =   0;
public static final int FF_PRED_PLANE =  1;
public static final int FF_PRED_MEDIAN = 2;
// #endif

    /**
     * slice offsets in the frame in bytes
     * - encoding: Set/allocated by libavcodec.
     * - decoding: Set/allocated by user (or NULL).
     */
    public native IntPointer slice_offset(); public native AVCodecContext slice_offset(IntPointer slice_offset);

    /**
     * sample aspect ratio (0 if unknown)
     * That is the width of a pixel divided by the height of the pixel.
     * Numerator and denominator must be relatively prime and smaller than 256 for some video standards.
     * - encoding: Set by user.
     * - decoding: Set by libavcodec.
     */
    public native @ByRef AVRational sample_aspect_ratio(); public native AVCodecContext sample_aspect_ratio(AVRational sample_aspect_ratio);

    /**
     * motion estimation comparison function
     * - encoding: Set by user.
     * - decoding: unused
     */
    public native int me_cmp(); public native AVCodecContext me_cmp(int me_cmp);
    /**
     * subpixel motion estimation comparison function
     * - encoding: Set by user.
     * - decoding: unused
     */
    public native int me_sub_cmp(); public native AVCodecContext me_sub_cmp(int me_sub_cmp);
    /**
     * macroblock comparison function (not supported yet)
     * - encoding: Set by user.
     * - decoding: unused
     */
    public native int mb_cmp(); public native AVCodecContext mb_cmp(int mb_cmp);
    /**
     * interlaced DCT comparison function
     * - encoding: Set by user.
     * - decoding: unused
     */
    public native int ildct_cmp(); public native AVCodecContext ildct_cmp(int ildct_cmp);
public static final int FF_CMP_SAD =    0;
public static final int FF_CMP_SSE =    1;
public static final int FF_CMP_SATD =   2;
public static final int FF_CMP_DCT =    3;
public static final int FF_CMP_PSNR =   4;
public static final int FF_CMP_BIT =    5;
public static final int FF_CMP_RD =     6;
public static final int FF_CMP_ZERO =   7;
public static final int FF_CMP_VSAD =   8;
public static final int FF_CMP_VSSE =   9;
public static final int FF_CMP_NSSE =   10;
public static final int FF_CMP_W53 =    11;
public static final int FF_CMP_W97 =    12;
public static final int FF_CMP_DCTMAX = 13;
public static final int FF_CMP_DCT264 = 14;
public static final int FF_CMP_CHROMA = 256;

    /**
     * ME diamond size & shape
     * - encoding: Set by user.
     * - decoding: unused
     */
    public native int dia_size(); public native AVCodecContext dia_size(int dia_size);

    /**
     * amount of previous MV predictors (2a+1 x 2a+1 square)
     * - encoding: Set by user.
     * - decoding: unused
     */
    public native int last_predictor_count(); public native AVCodecContext last_predictor_count(int last_predictor_count);

// #if FF_API_PRIVATE_OPT
    /** @deprecated use encoder private options instead */
    public native @Deprecated int pre_me(); public native AVCodecContext pre_me(int pre_me);
// #endif

    /**
     * motion estimation prepass comparison function
     * - encoding: Set by user.
     * - decoding: unused
     */
    public native int me_pre_cmp(); public native AVCodecContext me_pre_cmp(int me_pre_cmp);

    /**
     * ME prepass diamond size & shape
     * - encoding: Set by user.
     * - decoding: unused
     */
    public native int pre_dia_size(); public native AVCodecContext pre_dia_size(int pre_dia_size);

    /**
     * subpel ME quality
     * - encoding: Set by user.
     * - decoding: unused
     */
    public native int me_subpel_quality(); public native AVCodecContext me_subpel_quality(int me_subpel_quality);

// #if FF_API_AFD
    /**
     * DTG active format information (additional aspect ratio
     * information only used in DVB MPEG-2 transport streams)
     * 0 if not set.
     *
     * - encoding: unused
     * - decoding: Set by decoder.
     * @deprecated Deprecated in favor of AVSideData
     */
    public native @Deprecated int dtg_active_format(); public native AVCodecContext dtg_active_format(int dtg_active_format);
public static final int FF_DTG_AFD_SAME =         8;
public static final int FF_DTG_AFD_4_3 =          9;
public static final int FF_DTG_AFD_16_9 =         10;
public static final int FF_DTG_AFD_14_9 =         11;
public static final int FF_DTG_AFD_4_3_SP_14_9 =  13;
public static final int FF_DTG_AFD_16_9_SP_14_9 = 14;
public static final int FF_DTG_AFD_SP_4_3 =       15;
// #endif /* FF_API_AFD */

    /**
     * maximum motion estimation search range in subpel units
     * If 0 then no limit.
     *
     * - encoding: Set by user.
     * - decoding: unused
     */
    public native int me_range(); public native AVCodecContext me_range(int me_range);

// #if FF_API_QUANT_BIAS
    /**
     * @deprecated use encoder private option instead
     */
    public native @Deprecated int intra_quant_bias(); public native AVCodecContext intra_quant_bias(int intra_quant_bias);
public static final int FF_DEFAULT_QUANT_BIAS = 999999;

    /**
     * @deprecated use encoder private option instead
     */
    public native @Deprecated int inter_quant_bias(); public native AVCodecContext inter_quant_bias(int inter_quant_bias);
// #endif

    /**
     * slice flags
     * - encoding: unused
     * - decoding: Set by user.
     */
    public native int slice_flags(); public native AVCodecContext slice_flags(int slice_flags);
/** draw_horiz_band() is called in coded order instead of display */
public static final int SLICE_FLAG_CODED_ORDER =    0x0001;
/** allow draw_horiz_band() with field slices (MPEG2 field pics) */
public static final int SLICE_FLAG_ALLOW_FIELD =    0x0002;
/** allow draw_horiz_band() with 1 component at a time (SVQ1) */
public static final int SLICE_FLAG_ALLOW_PLANE =    0x0004;

// #if FF_API_XVMC
    /**
     * XVideo Motion Acceleration
     * - encoding: forbidden
     * - decoding: set by decoder
     * @deprecated XvMC doesn't need it anymore.
     */
    public native @Deprecated int xvmc_acceleration(); public native AVCodecContext xvmc_acceleration(int xvmc_acceleration);
// #endif /* FF_API_XVMC */

    /**
     * macroblock decision mode
     * - encoding: Set by user.
     * - decoding: unused
     */
    public native int mb_decision(); public native AVCodecContext mb_decision(int mb_decision);
/** uses mb_cmp */
public static final int FF_MB_DECISION_SIMPLE = 0;
/** chooses the one which needs the fewest bits */
public static final int FF_MB_DECISION_BITS =   1;
/** rate distortion */
public static final int FF_MB_DECISION_RD =     2;

    /**
     * custom intra quantization matrix
     * - encoding: Set by user, can be NULL.
     * - decoding: Set by libavcodec.
     */
    public native @Cast("uint16_t*") ShortPointer intra_matrix(); public native AVCodecContext intra_matrix(ShortPointer intra_matrix);

    /**
     * custom inter quantization matrix
     * - encoding: Set by user, can be NULL.
     * - decoding: Set by libavcodec.
     */
    public native @Cast("uint16_t*") ShortPointer inter_matrix(); public native AVCodecContext inter_matrix(ShortPointer inter_matrix);

// #if FF_API_PRIVATE_OPT
    /** @deprecated use encoder private options instead */
    public native @Deprecated int scenechange_threshold(); public native AVCodecContext scenechange_threshold(int scenechange_threshold);

    /** @deprecated use encoder private options instead */
    public native @Deprecated int noise_reduction(); public native AVCodecContext noise_reduction(int noise_reduction);
// #endif

// #if FF_API_MPV_OPT
    /**
     * @deprecated this field is unused
     */
    public native @Deprecated int me_threshold(); public native AVCodecContext me_threshold(int me_threshold);

    /**
     * @deprecated this field is unused
     */
    public native @Deprecated int mb_threshold(); public native AVCodecContext mb_threshold(int mb_threshold);
// #endif

    /**
     * precision of the intra DC coefficient - 8
     * - encoding: Set by user.
     * - decoding: Set by libavcodec
     */
    public native int intra_dc_precision(); public native AVCodecContext intra_dc_precision(int intra_dc_precision);

    /**
     * Number of macroblock rows at the top which are skipped.
     * - encoding: unused
     * - decoding: Set by user.
     */
    public native int skip_top(); public native AVCodecContext skip_top(int skip_top);

    /**
     * Number of macroblock rows at the bottom which are skipped.
     * - encoding: unused
     * - decoding: Set by user.
     */
    public native int skip_bottom(); public native AVCodecContext skip_bottom(int skip_bottom);

// #if FF_API_MPV_OPT
    /**
     * @deprecated use encoder private options instead
     */
    public native @Deprecated float border_masking(); public native AVCodecContext border_masking(float border_masking);
// #endif

    /**
     * minimum MB lagrange multipler
     * - encoding: Set by user.
     * - decoding: unused
     */
    public native int mb_lmin(); public native AVCodecContext mb_lmin(int mb_lmin);

    /**
     * maximum MB lagrange multipler
     * - encoding: Set by user.
     * - decoding: unused
     */
    public native int mb_lmax(); public native AVCodecContext mb_lmax(int mb_lmax);

// #if FF_API_PRIVATE_OPT
    /**
     * @deprecated use encoder private options instead
     */
    public native @Deprecated int me_penalty_compensation(); public native AVCodecContext me_penalty_compensation(int me_penalty_compensation);
// #endif

    /**
     *
     * - encoding: Set by user.
     * - decoding: unused
     */
    public native int bidir_refine(); public native AVCodecContext bidir_refine(int bidir_refine);

// #if FF_API_PRIVATE_OPT
    /** @deprecated use encoder private options instead */
    public native @Deprecated int brd_scale(); public native AVCodecContext brd_scale(int brd_scale);
// #endif

    /**
     * minimum GOP size
     * - encoding: Set by user.
     * - decoding: unused
     */
    public native int keyint_min(); public native AVCodecContext keyint_min(int keyint_min);

    /**
     * number of reference frames
     * - encoding: Set by user.
     * - decoding: Set by lavc.
     */
    public native int refs(); public native AVCodecContext refs(int refs);

// #if FF_API_PRIVATE_OPT
    /** @deprecated use encoder private options instead */
    public native @Deprecated int chromaoffset(); public native AVCodecContext chromaoffset(int chromaoffset);
// #endif

// #if FF_API_UNUSED_MEMBERS
    /**
     * Multiplied by qscale for each frame and added to scene_change_score.
     * - encoding: Set by user.
     * - decoding: unused
     */
    public native @Deprecated int scenechange_factor(); public native AVCodecContext scenechange_factor(int scenechange_factor);
// #endif

    /**
     *
     * Note: Value depends upon the compare function used for fullpel ME.
     * - encoding: Set by user.
     * - decoding: unused
     */
    public native int mv0_threshold(); public native AVCodecContext mv0_threshold(int mv0_threshold);

// #if FF_API_PRIVATE_OPT
    /** @deprecated use encoder private options instead */
    public native @Deprecated int b_sensitivity(); public native AVCodecContext b_sensitivity(int b_sensitivity);
// #endif

    /**
     * Chromaticity coordinates of the source primaries.
     * - encoding: Set by user
     * - decoding: Set by libavcodec
     */
    public native @Cast("AVColorPrimaries") int color_primaries(); public native AVCodecContext color_primaries(int color_primaries);

    /**
     * Color Transfer Characteristic.
     * - encoding: Set by user
     * - decoding: Set by libavcodec
     */
    public native @Cast("AVColorTransferCharacteristic") int color_trc(); public native AVCodecContext color_trc(int color_trc);

    /**
     * YUV colorspace type.
     * - encoding: Set by user
     * - decoding: Set by libavcodec
     */
    public native @Cast("AVColorSpace") int colorspace(); public native AVCodecContext colorspace(int colorspace);

    /**
     * MPEG vs JPEG YUV range.
     * - encoding: Set by user
     * - decoding: Set by libavcodec
     */
    public native @Cast("AVColorRange") int color_range(); public native AVCodecContext color_range(int color_range);

    /**
     * This defines the location of chroma samples.
     * - encoding: Set by user
     * - decoding: Set by libavcodec
     */
    public native @Cast("AVChromaLocation") int chroma_sample_location(); public native AVCodecContext chroma_sample_location(int chroma_sample_location);

    /**
     * Number of slices.
     * Indicates number of picture subdivisions. Used for parallelized
     * decoding.
     * - encoding: Set by user
     * - decoding: unused
     */
    public native int slices(); public native AVCodecContext slices(int slices);

    /** Field order
     * - encoding: set by libavcodec
     * - decoding: Set by user.
     */
    public native @Cast("AVFieldOrder") int field_order(); public native AVCodecContext field_order(int field_order);

    /* audio only */
    /** samples per second */
    public native int sample_rate(); public native AVCodecContext sample_rate(int sample_rate);
    /** number of audio channels */
    public native int channels(); public native AVCodecContext channels(int channels);

    /**
     * audio sample format
     * - encoding: Set by user.
     * - decoding: Set by libavcodec.
     */
    /** sample format */
    public native @Cast("AVSampleFormat") int sample_fmt(); public native AVCodecContext sample_fmt(int sample_fmt);

    /* The following data should not be initialized. */
    /**
     * Number of samples per channel in an audio frame.
     *
     * - encoding: set by libavcodec in avcodec_open2(). Each submitted frame
     *   except the last must contain exactly frame_size samples per channel.
     *   May be 0 when the codec has AV_CODEC_CAP_VARIABLE_FRAME_SIZE set, then the
     *   frame size is not restricted.
     * - decoding: may be set by some decoders to indicate constant frame size
     */
    public native int frame_size(); public native AVCodecContext frame_size(int frame_size);

    /**
     * Frame counter, set by libavcodec.
     *
     * - decoding: total number of frames returned from the decoder so far.
     * - encoding: total number of frames passed to the encoder so far.
     *
     *   @note the counter is not incremented if encoding/decoding resulted in
     *   an error.
     */
    public native int frame_number(); public native AVCodecContext frame_number(int frame_number);

    /**
     * number of bytes per packet if constant and known or 0
     * Used by some WAV based audio codecs.
     */
    public native int block_align(); public native AVCodecContext block_align(int block_align);

    /**
     * Audio cutoff bandwidth (0 means "automatic")
     * - encoding: Set by user.
     * - decoding: unused
     */
    public native int cutoff(); public native AVCodecContext cutoff(int cutoff);

    /**
     * Audio channel layout.
     * - encoding: set by user.
     * - decoding: set by user, may be overwritten by libavcodec.
     */
    public native @Cast("uint64_t") long channel_layout(); public native AVCodecContext channel_layout(long channel_layout);

    /**
     * Request decoder to use this channel layout if it can (0 for default)
     * - encoding: unused
     * - decoding: Set by user.
     */
    public native @Cast("uint64_t") long request_channel_layout(); public native AVCodecContext request_channel_layout(long request_channel_layout);

    /**
     * Type of service that the audio stream conveys.
     * - encoding: Set by user.
     * - decoding: Set by libavcodec.
     */
    public native @Cast("AVAudioServiceType") int audio_service_type(); public native AVCodecContext audio_service_type(int audio_service_type);

    /**
     * desired sample format
     * - encoding: Not used.
     * - decoding: Set by user.
     * Decoder will decode to this format if it can.
     */
    public native @Cast("AVSampleFormat") int request_sample_fmt(); public native AVCodecContext request_sample_fmt(int request_sample_fmt);

    /**
     * This callback is called at the beginning of each frame to get data
     * buffer(s) for it. There may be one contiguous buffer for all the data or
     * there may be a buffer per each data plane or anything in between. What
     * this means is, you may set however many entries in buf[] you feel necessary.
     * Each buffer must be reference-counted using the AVBuffer API (see description
     * of buf[] below).
     *
     * The following fields will be set in the frame before this callback is
     * called:
     * - format
     * - width, height (video only)
     * - sample_rate, channel_layout, nb_samples (audio only)
     * Their values may differ from the corresponding values in
     * AVCodecContext. This callback must use the frame values, not the codec
     * context values, to calculate the required buffer size.
     *
     * This callback must fill the following fields in the frame:
     * - data[]
     * - linesize[]
     * - extended_data:
     *   * if the data is planar audio with more than 8 channels, then this
     *     callback must allocate and fill extended_data to contain all pointers
     *     to all data planes. data[] must hold as many pointers as it can.
     *     extended_data must be allocated with av_malloc() and will be freed in
     *     av_frame_unref().
     *   * otherwise exended_data must point to data
     * - buf[] must contain one or more pointers to AVBufferRef structures. Each of
     *   the frame's data and extended_data pointers must be contained in these. That
     *   is, one AVBufferRef for each allocated chunk of memory, not necessarily one
     *   AVBufferRef per data[] entry. See: av_buffer_create(), av_buffer_alloc(),
     *   and av_buffer_ref().
     * - extended_buf and nb_extended_buf must be allocated with av_malloc() by
     *   this callback and filled with the extra buffers if there are more
     *   buffers than buf[] can hold. extended_buf will be freed in
     *   av_frame_unref().
     *
     * If AV_CODEC_CAP_DR1 is not set then get_buffer2() must call
     * avcodec_default_get_buffer2() instead of providing buffers allocated by
     * some other means.
     *
     * Each data plane must be aligned to the maximum required by the target
     * CPU.
     *
     * @see avcodec_default_get_buffer2()
     *
     * Video:
     *
     * If AV_GET_BUFFER_FLAG_REF is set in flags then the frame may be reused
     * (read and/or written to if it is writable) later by libavcodec.
     *
     * avcodec_align_dimensions2() should be used to find the required width and
     * height, as they normally need to be rounded up to the next multiple of 16.
     *
     * Some decoders do not support linesizes changing between frames.
     *
     * If frame multithreading is used and thread_safe_callbacks is set,
     * this callback may be called from a different thread, but not from more
     * than one at once. Does not need to be reentrant.
     *
     * @see avcodec_align_dimensions2()
     *
     * Audio:
     *
     * Decoders request a buffer of a particular size by setting
     * AVFrame.nb_samples prior to calling get_buffer2(). The decoder may,
     * however, utilize only part of the buffer by setting AVFrame.nb_samples
     * to a smaller value in the output frame.
     *
     * As a convenience, av_samples_get_buffer_size() and
     * av_samples_fill_arrays() in libavutil may be used by custom get_buffer2()
     * functions to find the required data size and to fill data pointers and
     * linesize. In AVFrame.linesize, only linesize[0] may be set for audio
     * since all planes must be the same size.
     *
     * @see av_samples_get_buffer_size(), av_samples_fill_arrays()
     *
     * - encoding: unused
     * - decoding: Set by libavcodec, user can override.
     */
    public static class Get_buffer2_AVCodecContext_AVFrame_int extends FunctionPointer {
        static { Loader.load(); }
        /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
        public    Get_buffer2_AVCodecContext_AVFrame_int(Pointer p) { super(p); }
        protected Get_buffer2_AVCodecContext_AVFrame_int() { allocate(); }
        private native void allocate();
        public native int call(AVCodecContext s, AVFrame frame, int flags);
    }
    public native Get_buffer2_AVCodecContext_AVFrame_int get_buffer2(); public native AVCodecContext get_buffer2(Get_buffer2_AVCodecContext_AVFrame_int get_buffer2);

    /**
     * If non-zero, the decoded audio and video frames returned from
     * avcodec_decode_video2() and avcodec_decode_audio4() are reference-counted
     * and are valid indefinitely. The caller must free them with
     * av_frame_unref() when they are not needed anymore.
     * Otherwise, the decoded frames must not be freed by the caller and are
     * only valid until the next decode call.
     *
     * - encoding: unused
     * - decoding: set by the caller before avcodec_open2().
     */
    public native int refcounted_frames(); public native AVCodecContext refcounted_frames(int refcounted_frames);

    /* - encoding parameters */
    /** amount of qscale change between easy & hard scenes (0.0-1.0) */
    public native float qcompress(); public native AVCodecContext qcompress(float qcompress);
    /** amount of qscale smoothing over time (0.0-1.0) */
    public native float qblur(); public native AVCodecContext qblur(float qblur);

    /**
     * minimum quantizer
     * - encoding: Set by user.
     * - decoding: unused
     */
    public native int qmin(); public native AVCodecContext qmin(int qmin);

    /**
     * maximum quantizer
     * - encoding: Set by user.
     * - decoding: unused
     */
    public native int qmax(); public native AVCodecContext qmax(int qmax);

    /**
     * maximum quantizer difference between frames
     * - encoding: Set by user.
     * - decoding: unused
     */
    public native int max_qdiff(); public native AVCodecContext max_qdiff(int max_qdiff);

// #if FF_API_MPV_OPT
    /**
     * @deprecated use encoder private options instead
     */
    public native @Deprecated float rc_qsquish(); public native AVCodecContext rc_qsquish(float rc_qsquish);

    public native @Deprecated float rc_qmod_amp(); public native AVCodecContext rc_qmod_amp(float rc_qmod_amp);
    public native @Deprecated int rc_qmod_freq(); public native AVCodecContext rc_qmod_freq(int rc_qmod_freq);
// #endif

    /**
     * decoder bitstream buffer size
     * - encoding: Set by user.
     * - decoding: unused
     */
    public native int rc_buffer_size(); public native AVCodecContext rc_buffer_size(int rc_buffer_size);

    /**
     * ratecontrol override, see RcOverride
     * - encoding: Allocated/set/freed by user.
     * - decoding: unused
     */
    public native int rc_override_count(); public native AVCodecContext rc_override_count(int rc_override_count);
    public native RcOverride rc_override(); public native AVCodecContext rc_override(RcOverride rc_override);

// #if FF_API_MPV_OPT
    /**
     * @deprecated use encoder private options instead
     */
    @MemberGetter public native @Deprecated @Cast("const char*") BytePointer rc_eq();
// #endif

    /**
     * maximum bitrate
     * - encoding: Set by user.
     * - decoding: Set by user, may be overwritten by libavcodec.
     */
    public native long rc_max_rate(); public native AVCodecContext rc_max_rate(long rc_max_rate);

    /**
     * minimum bitrate
     * - encoding: Set by user.
     * - decoding: unused
     */
    public native long rc_min_rate(); public native AVCodecContext rc_min_rate(long rc_min_rate);

// #if FF_API_MPV_OPT
    /**
     * @deprecated use encoder private options instead
     */
    public native @Deprecated float rc_buffer_aggressivity(); public native AVCodecContext rc_buffer_aggressivity(float rc_buffer_aggressivity);

    public native @Deprecated float rc_initial_cplx(); public native AVCodecContext rc_initial_cplx(float rc_initial_cplx);
// #endif

    /**
     * Ratecontrol attempt to use, at maximum, <value> of what can be used without an underflow.
     * - encoding: Set by user.
     * - decoding: unused.
     */
    public native float rc_max_available_vbv_use(); public native AVCodecContext rc_max_available_vbv_use(float rc_max_available_vbv_use);

    /**
     * Ratecontrol attempt to use, at least, <value> times the amount needed to prevent a vbv overflow.
     * - encoding: Set by user.
     * - decoding: unused.
     */
    public native float rc_min_vbv_overflow_use(); public native AVCodecContext rc_min_vbv_overflow_use(float rc_min_vbv_overflow_use);

    /**
     * Number of bits which should be loaded into the rc buffer before decoding starts.
     * - encoding: Set by user.
     * - decoding: unused
     */
    public native int rc_initial_buffer_occupancy(); public native AVCodecContext rc_initial_buffer_occupancy(int rc_initial_buffer_occupancy);

// #if FF_API_CODER_TYPE
public static final int FF_CODER_TYPE_VLC =       0;
public static final int FF_CODER_TYPE_AC =        1;
public static final int FF_CODER_TYPE_RAW =       2;
public static final int FF_CODER_TYPE_RLE =       3;
// #if FF_API_UNUSED_MEMBERS
public static final int FF_CODER_TYPE_DEFLATE =   4;
// #endif /* FF_API_UNUSED_MEMBERS */
    /**
     * @deprecated use encoder private options instead
     */
    public native @Deprecated int coder_type(); public native AVCodecContext coder_type(int coder_type);
// #endif /* FF_API_CODER_TYPE */

// #if FF_API_PRIVATE_OPT
    /** @deprecated use encoder private options instead */
    public native @Deprecated int context_model(); public native AVCodecContext context_model(int context_model);
// #endif

// #if FF_API_MPV_OPT
    /**
     * @deprecated use encoder private options instead
     */
    public native @Deprecated int lmin(); public native AVCodecContext lmin(int lmin);

    /**
     * @deprecated use encoder private options instead
     */
    public native @Deprecated int lmax(); public native AVCodecContext lmax(int lmax);
// #endif

// #if FF_API_PRIVATE_OPT
    /** @deprecated use encoder private options instead */
    public native @Deprecated int frame_skip_threshold(); public native AVCodecContext frame_skip_threshold(int frame_skip_threshold);

    /** @deprecated use encoder private options instead */
    public native @Deprecated int frame_skip_factor(); public native AVCodecContext frame_skip_factor(int frame_skip_factor);

    /** @deprecated use encoder private options instead */
    public native @Deprecated int frame_skip_exp(); public native AVCodecContext frame_skip_exp(int frame_skip_exp);

    /** @deprecated use encoder private options instead */
    public native @Deprecated int frame_skip_cmp(); public native AVCodecContext frame_skip_cmp(int frame_skip_cmp);
// #endif /* FF_API_PRIVATE_OPT */

    /**
     * trellis RD quantization
     * - encoding: Set by user.
     * - decoding: unused
     */
    public native int trellis(); public native AVCodecContext trellis(int trellis);

// #if FF_API_PRIVATE_OPT
    /** @deprecated use encoder private options instead */
    public native @Deprecated int min_prediction_order(); public native AVCodecContext min_prediction_order(int min_prediction_order);

    /** @deprecated use encoder private options instead */
    public native @Deprecated int max_prediction_order(); public native AVCodecContext max_prediction_order(int max_prediction_order);

    /** @deprecated use encoder private options instead */
    public native @Deprecated long timecode_frame_start(); public native AVCodecContext timecode_frame_start(long timecode_frame_start);
// #endif

// #if FF_API_RTP_CALLBACK
    /**
     * @deprecated unused
     */
    /* The RTP callback: This function is called    */
    /* every time the encoder has a packet to send. */
    /* It depends on the encoder if the data starts */
    /* with a Start Code (it should). H.263 does.   */
    /* mb_nb contains the number of macroblocks     */
    /* encoded in the RTP payload.                  */
    public static class Rtp_callback_AVCodecContext_Pointer_int_int extends FunctionPointer {
        static { Loader.load(); }
        /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
        public    Rtp_callback_AVCodecContext_Pointer_int_int(Pointer p) { super(p); }
        protected Rtp_callback_AVCodecContext_Pointer_int_int() { allocate(); }
        private native void allocate();
        public native @Deprecated void call(AVCodecContext avctx, Pointer data, int size, int mb_nb);
    }
    public native Rtp_callback_AVCodecContext_Pointer_int_int rtp_callback(); public native AVCodecContext rtp_callback(Rtp_callback_AVCodecContext_Pointer_int_int rtp_callback);
// #endif

// #if FF_API_PRIVATE_OPT
    /** @deprecated use encoder private options instead */
    public native @Deprecated int rtp_payload_size(); public native AVCodecContext rtp_payload_size(int rtp_payload_size);   /* The size of the RTP payload: the coder will  */
                            /* do its best to deliver a chunk with size     */
                            /* below rtp_payload_size, the chunk will start */
                            /* with a start code on some codecs like H.263. */
                            /* This doesn't take account of any particular  */
                            /* headers inside the transmitted RTP payload.  */
// #endif

// #if FF_API_STAT_BITS
    /* statistics, used for 2-pass encoding */
    public native @Deprecated int mv_bits(); public native AVCodecContext mv_bits(int mv_bits);
    public native @Deprecated int header_bits(); public native AVCodecContext header_bits(int header_bits);
    public native @Deprecated int i_tex_bits(); public native AVCodecContext i_tex_bits(int i_tex_bits);
    public native @Deprecated int p_tex_bits(); public native AVCodecContext p_tex_bits(int p_tex_bits);
    public native @Deprecated int i_count(); public native AVCodecContext i_count(int i_count);
    public native @Deprecated int p_count(); public native AVCodecContext p_count(int p_count);
    public native @Deprecated int skip_count(); public native AVCodecContext skip_count(int skip_count);
    public native @Deprecated int misc_bits(); public native AVCodecContext misc_bits(int misc_bits);

    /** @deprecated this field is unused */
    public native @Deprecated int frame_bits(); public native AVCodecContext frame_bits(int frame_bits);
// #endif

    /**
     * pass1 encoding statistics output buffer
     * - encoding: Set by libavcodec.
     * - decoding: unused
     */
    public native @Cast("char*") BytePointer stats_out(); public native AVCodecContext stats_out(BytePointer stats_out);

    /**
     * pass2 encoding statistics input buffer
     * Concatenated stuff from stats_out of pass1 should be placed here.
     * - encoding: Allocated/set/freed by user.
     * - decoding: unused
     */
    public native @Cast("char*") BytePointer stats_in(); public native AVCodecContext stats_in(BytePointer stats_in);

    /**
     * Work around bugs in encoders which sometimes cannot be detected automatically.
     * - encoding: Set by user
     * - decoding: Set by user
     */
    public native int workaround_bugs(); public native AVCodecContext workaround_bugs(int workaround_bugs);
/** autodetection */
public static final int FF_BUG_AUTODETECT =       1;
// #if FF_API_OLD_MSMPEG4
public static final int FF_BUG_OLD_MSMPEG4 =      2;
// #endif
public static final int FF_BUG_XVID_ILACE =       4;
public static final int FF_BUG_UMP4 =             8;
public static final int FF_BUG_NO_PADDING =       16;
public static final int FF_BUG_AMV =              32;
// #if FF_API_AC_VLC
/** Will be removed, libavcodec can now handle these non-compliant files by default. */
public static final int FF_BUG_AC_VLC =           0;
// #endif
public static final int FF_BUG_QPEL_CHROMA =      64;
public static final int FF_BUG_STD_QPEL =         128;
public static final int FF_BUG_QPEL_CHROMA2 =     256;
public static final int FF_BUG_DIRECT_BLOCKSIZE = 512;
public static final int FF_BUG_EDGE =             1024;
public static final int FF_BUG_HPEL_CHROMA =      2048;
public static final int FF_BUG_DC_CLIP =          4096;
/** Work around various bugs in Microsoft's broken decoders. */
public static final int FF_BUG_MS =               8192;
public static final int FF_BUG_TRUNCATED =       16384;

    /**
     * strictly follow the standard (MPEG4, ...).
     * - encoding: Set by user.
     * - decoding: Set by user.
     * Setting this to STRICT or higher means the encoder and decoder will
     * generally do stupid things, whereas setting it to unofficial or lower
     * will mean the encoder might produce output that is not supported by all
     * spec-compliant decoders. Decoders don't differentiate between normal,
     * unofficial and experimental (that is, they always try to decode things
     * when they can) unless they are explicitly asked to behave stupidly
     * (=strictly conform to the specs)
     */
    public native int strict_std_compliance(); public native AVCodecContext strict_std_compliance(int strict_std_compliance);
/** Strictly conform to an older more strict version of the spec or reference software. */
public static final int FF_COMPLIANCE_VERY_STRICT =   2;
/** Strictly conform to all the things in the spec no matter what consequences. */
public static final int FF_COMPLIANCE_STRICT =        1;
public static final int FF_COMPLIANCE_NORMAL =        0;
/** Allow unofficial extensions */
public static final int FF_COMPLIANCE_UNOFFICIAL =   -1;
/** Allow nonstandardized experimental things. */
public static final int FF_COMPLIANCE_EXPERIMENTAL = -2;

    /**
     * error concealment flags
     * - encoding: unused
     * - decoding: Set by user.
     */
    public native int error_concealment(); public native AVCodecContext error_concealment(int error_concealment);
public static final int FF_EC_GUESS_MVS =   1;
public static final int FF_EC_DEBLOCK =     2;
public static final int FF_EC_FAVOR_INTER = 256;

    /**
     * debug
     * - encoding: Set by user.
     * - decoding: Set by user.
     */
    public native int debug(); public native AVCodecContext debug(int debug);
public static final int FF_DEBUG_PICT_INFO =   1;
public static final int FF_DEBUG_RC =          2;
public static final int FF_DEBUG_BITSTREAM =   4;
public static final int FF_DEBUG_MB_TYPE =     8;
public static final int FF_DEBUG_QP =          16;
// #if FF_API_DEBUG_MV
/**
 * @deprecated this option does nothing
 */
public static final int FF_DEBUG_MV =          32;
// #endif
public static final int FF_DEBUG_DCT_COEFF =   0x00000040;
public static final int FF_DEBUG_SKIP =        0x00000080;
public static final int FF_DEBUG_STARTCODE =   0x00000100;
// #if FF_API_UNUSED_MEMBERS
public static final int FF_DEBUG_PTS =         0x00000200;
// #endif /* FF_API_UNUSED_MEMBERS */
public static final int FF_DEBUG_ER =          0x00000400;
public static final int FF_DEBUG_MMCO =        0x00000800;
public static final int FF_DEBUG_BUGS =        0x00001000;
// #if FF_API_DEBUG_MV
/** only access through AVOptions from outside libavcodec */
public static final int FF_DEBUG_VIS_QP =      0x00002000;
/** only access through AVOptions from outside libavcodec */
public static final int FF_DEBUG_VIS_MB_TYPE = 0x00004000;
// #endif
public static final int FF_DEBUG_BUFFERS =     0x00008000;
public static final int FF_DEBUG_THREADS =     0x00010000;
public static final int FF_DEBUG_GREEN_MD =    0x00800000;
public static final int FF_DEBUG_NOMC =        0x01000000;

// #if FF_API_DEBUG_MV
    /**
     * debug
     * Code outside libavcodec should access this field using AVOptions
     * - encoding: Set by user.
     * - decoding: Set by user.
     */
    public native int debug_mv(); public native AVCodecContext debug_mv(int debug_mv);
public static final int FF_DEBUG_VIS_MV_P_FOR =  0x00000001; //visualize forward predicted MVs of P frames
public static final int FF_DEBUG_VIS_MV_B_FOR =  0x00000002; //visualize forward predicted MVs of B frames
public static final int FF_DEBUG_VIS_MV_B_BACK = 0x00000004; //visualize backward predicted MVs of B frames
// #endif

    /**
     * Error recognition; may misdetect some more or less valid parts as errors.
     * - encoding: unused
     * - decoding: Set by user.
     */
    public native int err_recognition(); public native AVCodecContext err_recognition(int err_recognition);

/**
 * Verify checksums embedded in the bitstream (could be of either encoded or
 * decoded data, depending on the codec) and print an error message on mismatch.
 * If AV_EF_EXPLODE is also set, a mismatching checksum will result in the
 * decoder returning an error.
 */
public static final int AV_EF_CRCCHECK =  (1<<0);
/** detect bitstream specification deviations */
public static final int AV_EF_BITSTREAM = (1<<1);
/** detect improper bitstream length */
public static final int AV_EF_BUFFER =    (1<<2);
/** abort decoding on minor error detection */
public static final int AV_EF_EXPLODE =   (1<<3);

/** ignore errors and continue */
public static final int AV_EF_IGNORE_ERR = (1<<15);
/** consider things that violate the spec, are fast to calculate and have not been seen in the wild as errors */
public static final int AV_EF_CAREFUL =    (1<<16);
/** consider all spec non compliances as errors */
public static final int AV_EF_COMPLIANT =  (1<<17);
/** consider things that a sane encoder should not do as an error */
public static final int AV_EF_AGGRESSIVE = (1<<18);


    /**
     * opaque 64bit number (generally a PTS) that will be reordered and
     * output in AVFrame.reordered_opaque
     * - encoding: unused
     * - decoding: Set by user.
     */
    public native long reordered_opaque(); public native AVCodecContext reordered_opaque(long reordered_opaque);

    /**
     * Hardware accelerator in use
     * - encoding: unused.
     * - decoding: Set by libavcodec
     */
    public native AVHWAccel hwaccel(); public native AVCodecContext hwaccel(AVHWAccel hwaccel);

    /**
     * Hardware accelerator context.
     * For some hardware accelerators, a global context needs to be
     * provided by the user. In that case, this holds display-dependent
     * data FFmpeg cannot instantiate itself. Please refer to the
     * FFmpeg HW accelerator documentation to know how to fill this
     * is. e.g. for VA API, this is a struct vaapi_context.
     * - encoding: unused
     * - decoding: Set by user
     */
    public native Pointer hwaccel_context(); public native AVCodecContext hwaccel_context(Pointer hwaccel_context);

    /**
     * error
     * - encoding: Set by libavcodec if flags & AV_CODEC_FLAG_PSNR.
     * - decoding: unused
     */
    public native @Cast("uint64_t") long error(int i); public native AVCodecContext error(int i, long error);
    @MemberGetter public native @Cast("uint64_t*") LongPointer error();

    /**
     * DCT algorithm, see FF_DCT_* below
     * - encoding: Set by user.
     * - decoding: unused
     */
    public native int dct_algo(); public native AVCodecContext dct_algo(int dct_algo);
public static final int FF_DCT_AUTO =    0;
public static final int FF_DCT_FASTINT = 1;
public static final int FF_DCT_INT =     2;
public static final int FF_DCT_MMX =     3;
public static final int FF_DCT_ALTIVEC = 5;
public static final int FF_DCT_FAAN =    6;

    /**
     * IDCT algorithm, see FF_IDCT_* below.
     * - encoding: Set by user.
     * - decoding: Set by user.
     */
    public native int idct_algo(); public native AVCodecContext idct_algo(int idct_algo);
public static final int FF_IDCT_AUTO =          0;
public static final int FF_IDCT_INT =           1;
public static final int FF_IDCT_SIMPLE =        2;
public static final int FF_IDCT_SIMPLEMMX =     3;
public static final int FF_IDCT_ARM =           7;
public static final int FF_IDCT_ALTIVEC =       8;
// #if FF_API_ARCH_SH4
public static final int FF_IDCT_SH4 =           9;
// #endif
public static final int FF_IDCT_SIMPLEARM =     10;
// #if FF_API_UNUSED_MEMBERS
public static final int FF_IDCT_IPP =           13;
// #endif /* FF_API_UNUSED_MEMBERS */
public static final int FF_IDCT_XVID =          14;
// #if FF_API_IDCT_XVIDMMX
public static final int FF_IDCT_XVIDMMX =       14;
// #endif /* FF_API_IDCT_XVIDMMX */
public static final int FF_IDCT_SIMPLEARMV5TE = 16;
public static final int FF_IDCT_SIMPLEARMV6 =   17;
// #if FF_API_ARCH_SPARC
public static final int FF_IDCT_SIMPLEVIS =     18;
// #endif
public static final int FF_IDCT_FAAN =          20;
public static final int FF_IDCT_SIMPLENEON =    22;
// #if FF_API_ARCH_ALPHA
public static final int FF_IDCT_SIMPLEALPHA =   23;
// #endif
public static final int FF_IDCT_SIMPLEAUTO =    128;

    /**
     * bits per sample/pixel from the demuxer (needed for huffyuv).
     * - encoding: Set by libavcodec.
     * - decoding: Set by user.
     */
     public native int bits_per_coded_sample(); public native AVCodecContext bits_per_coded_sample(int bits_per_coded_sample);

    /**
     * Bits per sample/pixel of internal libavcodec pixel/sample format.
     * - encoding: set by user.
     * - decoding: set by libavcodec.
     */
    public native int bits_per_raw_sample(); public native AVCodecContext bits_per_raw_sample(int bits_per_raw_sample);

// #if FF_API_LOWRES
    /**
     * low resolution decoding, 1-> 1/2 size, 2->1/4 size
     * - encoding: unused
     * - decoding: Set by user.
     * Code outside libavcodec should access this field using:
     * av_codec_{get,set}_lowres(avctx)
     */
     public native int lowres(); public native AVCodecContext lowres(int lowres);
// #endif

// #if FF_API_CODED_FRAME
    /**
     * the picture in the bitstream
     * - encoding: Set by libavcodec.
     * - decoding: unused
     *
     * @deprecated use the quality factor packet side data instead
     */
    public native @Deprecated AVFrame coded_frame(); public native AVCodecContext coded_frame(AVFrame coded_frame);
// #endif

    /**
     * thread count
     * is used to decide how many independent tasks should be passed to execute()
     * - encoding: Set by user.
     * - decoding: Set by user.
     */
    public native int thread_count(); public native AVCodecContext thread_count(int thread_count);

    /**
     * Which multithreading methods to use.
     * Use of FF_THREAD_FRAME will increase decoding delay by one frame per thread,
     * so clients which cannot provide future frames should not use it.
     *
     * - encoding: Set by user, otherwise the default is used.
     * - decoding: Set by user, otherwise the default is used.
     */
    public native int thread_type(); public native AVCodecContext thread_type(int thread_type);
/** Decode more than one frame at once */
public static final int FF_THREAD_FRAME =   1;
/** Decode more than one part of a single frame at once */
public static final int FF_THREAD_SLICE =   2;

    /**
     * Which multithreading methods are in use by the codec.
     * - encoding: Set by libavcodec.
     * - decoding: Set by libavcodec.
     */
    public native int active_thread_type(); public native AVCodecContext active_thread_type(int active_thread_type);

    /**
     * Set by the client if its custom get_buffer() callback can be called
     * synchronously from another thread, which allows faster multithreaded decoding.
     * draw_horiz_band() will be called from other threads regardless of this setting.
     * Ignored if the default get_buffer() is used.
     * - encoding: Set by user.
     * - decoding: Set by user.
     */
    public native int thread_safe_callbacks(); public native AVCodecContext thread_safe_callbacks(int thread_safe_callbacks);

    /**
     * The codec may call this to execute several independent things.
     * It will return only after finishing all tasks.
     * The user may replace this with some multithreaded implementation,
     * the default implementation will execute the parts serially.
     * @param count the number of things to execute
     * - encoding: Set by libavcodec, user can override.
     * - decoding: Set by libavcodec, user can override.
     */
    public static class Func_AVCodecContext_Pointer extends FunctionPointer {
        static { Loader.load(); }
        /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
        public    Func_AVCodecContext_Pointer(Pointer p) { super(p); }
        protected Func_AVCodecContext_Pointer() { allocate(); }
        private native void allocate();
        public native int call(AVCodecContext c2, Pointer arg);
    }
    public static class Execute_AVCodecContext_Func_AVCodecContext_Pointer_Pointer_IntPointer_int_int extends FunctionPointer {
        static { Loader.load(); }
        /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
        public    Execute_AVCodecContext_Func_AVCodecContext_Pointer_Pointer_IntPointer_int_int(Pointer p) { super(p); }
        protected Execute_AVCodecContext_Func_AVCodecContext_Pointer_Pointer_IntPointer_int_int() { allocate(); }
        private native void allocate();
        public native int call(AVCodecContext c, Func_AVCodecContext_Pointer func, Pointer arg2, IntPointer ret, int count, int size);
    }
    public native Execute_AVCodecContext_Func_AVCodecContext_Pointer_Pointer_IntPointer_int_int execute(); public native AVCodecContext execute(Execute_AVCodecContext_Func_AVCodecContext_Pointer_Pointer_IntPointer_int_int execute);

    /**
     * The codec may call this to execute several independent things.
     * It will return only after finishing all tasks.
     * The user may replace this with some multithreaded implementation,
     * the default implementation will execute the parts serially.
     * Also see avcodec_thread_init and e.g. the --enable-pthread configure option.
     * @param c context passed also to func
     * @param count the number of things to execute
     * @param arg2 argument passed unchanged to func
     * @param ret return values of executed functions, must have space for "count" values. May be NULL.
     * @param func function that will be called count times, with jobnr from 0 to count-1.
     *             threadnr will be in the range 0 to c->thread_count-1 < MAX_THREADS and so that no
     *             two instances of func executing at the same time will have the same threadnr.
     * @return always 0 currently, but code should handle a future improvement where when any call to func
     *         returns < 0 no further calls to func may be done and < 0 is returned.
     * - encoding: Set by libavcodec, user can override.
     * - decoding: Set by libavcodec, user can override.
     */
    public static class Func_AVCodecContext_Pointer_int_int extends FunctionPointer {
        static { Loader.load(); }
        /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
        public    Func_AVCodecContext_Pointer_int_int(Pointer p) { super(p); }
        protected Func_AVCodecContext_Pointer_int_int() { allocate(); }
        private native void allocate();
        public native int call(AVCodecContext c2, Pointer arg, int jobnr, int threadnr);
    }
    public static class Execute2_AVCodecContext_Func_AVCodecContext_Pointer_int_int_Pointer_IntPointer_int extends FunctionPointer {
        static { Loader.load(); }
        /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
        public    Execute2_AVCodecContext_Func_AVCodecContext_Pointer_int_int_Pointer_IntPointer_int(Pointer p) { super(p); }
        protected Execute2_AVCodecContext_Func_AVCodecContext_Pointer_int_int_Pointer_IntPointer_int() { allocate(); }
        private native void allocate();
        public native int call(AVCodecContext c, Func_AVCodecContext_Pointer_int_int func, Pointer arg2, IntPointer ret, int count);
    }
    public native Execute2_AVCodecContext_Func_AVCodecContext_Pointer_int_int_Pointer_IntPointer_int execute2(); public native AVCodecContext execute2(Execute2_AVCodecContext_Func_AVCodecContext_Pointer_int_int_Pointer_IntPointer_int execute2);

    /**
     * noise vs. sse weight for the nsse comparison function
     * - encoding: Set by user.
     * - decoding: unused
     */
     public native int nsse_weight(); public native AVCodecContext nsse_weight(int nsse_weight);

    /**
     * profile
     * - encoding: Set by user.
     * - decoding: Set by libavcodec.
     */
     public native int profile(); public native AVCodecContext profile(int profile);
public static final int FF_PROFILE_UNKNOWN = -99;
public static final int FF_PROFILE_RESERVED = -100;

public static final int FF_PROFILE_AAC_MAIN = 0;
public static final int FF_PROFILE_AAC_LOW =  1;
public static final int FF_PROFILE_AAC_SSR =  2;
public static final int FF_PROFILE_AAC_LTP =  3;
public static final int FF_PROFILE_AAC_HE =   4;
public static final int FF_PROFILE_AAC_HE_V2 = 28;
public static final int FF_PROFILE_AAC_LD =   22;
public static final int FF_PROFILE_AAC_ELD =  38;
public static final int FF_PROFILE_MPEG2_AAC_LOW = 128;
public static final int FF_PROFILE_MPEG2_AAC_HE =  131;

public static final int FF_PROFILE_DTS =         20;
public static final int FF_PROFILE_DTS_ES =      30;
public static final int FF_PROFILE_DTS_96_24 =   40;
public static final int FF_PROFILE_DTS_HD_HRA =  50;
public static final int FF_PROFILE_DTS_HD_MA =   60;
public static final int FF_PROFILE_DTS_EXPRESS = 70;

public static final int FF_PROFILE_MPEG2_422 =    0;
public static final int FF_PROFILE_MPEG2_HIGH =   1;
public static final int FF_PROFILE_MPEG2_SS =     2;
public static final int FF_PROFILE_MPEG2_SNR_SCALABLE =  3;
public static final int FF_PROFILE_MPEG2_MAIN =   4;
public static final int FF_PROFILE_MPEG2_SIMPLE = 5;

public static final int FF_PROFILE_H264_CONSTRAINED =  (1<<9);  // 8+1; constraint_set1_flag
public static final int FF_PROFILE_H264_INTRA =        (1<<11); // 8+3; constraint_set3_flag

public static final int FF_PROFILE_H264_BASELINE =             66;
public static final int FF_PROFILE_H264_CONSTRAINED_BASELINE = (66|FF_PROFILE_H264_CONSTRAINED);
public static final int FF_PROFILE_H264_MAIN =                 77;
public static final int FF_PROFILE_H264_EXTENDED =             88;
public static final int FF_PROFILE_H264_HIGH =                 100;
public static final int FF_PROFILE_H264_HIGH_10 =              110;
public static final int FF_PROFILE_H264_HIGH_10_INTRA =        (110|FF_PROFILE_H264_INTRA);
public static final int FF_PROFILE_H264_HIGH_422 =             122;
public static final int FF_PROFILE_H264_HIGH_422_INTRA =       (122|FF_PROFILE_H264_INTRA);
public static final int FF_PROFILE_H264_HIGH_444 =             144;
public static final int FF_PROFILE_H264_HIGH_444_PREDICTIVE =  244;
public static final int FF_PROFILE_H264_HIGH_444_INTRA =       (244|FF_PROFILE_H264_INTRA);
public static final int FF_PROFILE_H264_CAVLC_444 =            44;

public static final int FF_PROFILE_VC1_SIMPLE =   0;
public static final int FF_PROFILE_VC1_MAIN =     1;
public static final int FF_PROFILE_VC1_COMPLEX =  2;
public static final int FF_PROFILE_VC1_ADVANCED = 3;

public static final int FF_PROFILE_MPEG4_SIMPLE =                     0;
public static final int FF_PROFILE_MPEG4_SIMPLE_SCALABLE =            1;
public static final int FF_PROFILE_MPEG4_CORE =                       2;
public static final int FF_PROFILE_MPEG4_MAIN =                       3;
public static final int FF_PROFILE_MPEG4_N_BIT =                      4;
public static final int FF_PROFILE_MPEG4_SCALABLE_TEXTURE =           5;
public static final int FF_PROFILE_MPEG4_SIMPLE_FACE_ANIMATION =      6;
public static final int FF_PROFILE_MPEG4_BASIC_ANIMATED_TEXTURE =     7;
public static final int FF_PROFILE_MPEG4_HYBRID =                     8;
public static final int FF_PROFILE_MPEG4_ADVANCED_REAL_TIME =         9;
public static final int FF_PROFILE_MPEG4_CORE_SCALABLE =             10;
public static final int FF_PROFILE_MPEG4_ADVANCED_CODING =           11;
public static final int FF_PROFILE_MPEG4_ADVANCED_CORE =             12;
public static final int FF_PROFILE_MPEG4_ADVANCED_SCALABLE_TEXTURE = 13;
public static final int FF_PROFILE_MPEG4_SIMPLE_STUDIO =             14;
public static final int FF_PROFILE_MPEG4_ADVANCED_SIMPLE =           15;

public static final int FF_PROFILE_JPEG2000_CSTREAM_RESTRICTION_0 =   0;
public static final int FF_PROFILE_JPEG2000_CSTREAM_RESTRICTION_1 =   1;
public static final int FF_PROFILE_JPEG2000_CSTREAM_NO_RESTRICTION =  2;
public static final int FF_PROFILE_JPEG2000_DCINEMA_2K =              3;
public static final int FF_PROFILE_JPEG2000_DCINEMA_4K =              4;

public static final int FF_PROFILE_VP9_0 =                            0;
public static final int FF_PROFILE_VP9_1 =                            1;
public static final int FF_PROFILE_VP9_2 =                            2;
public static final int FF_PROFILE_VP9_3 =                            3;

public static final int FF_PROFILE_HEVC_MAIN =                        1;
public static final int FF_PROFILE_HEVC_MAIN_10 =                     2;
public static final int FF_PROFILE_HEVC_MAIN_STILL_PICTURE =          3;
public static final int FF_PROFILE_HEVC_REXT =                        4;

    /**
     * level
     * - encoding: Set by user.
     * - decoding: Set by libavcodec.
     */
     public native int level(); public native AVCodecContext level(int level);
public static final int FF_LEVEL_UNKNOWN = -99;

    /**
     * Skip loop filtering for selected frames.
     * - encoding: unused
     * - decoding: Set by user.
     */
    public native @Cast("AVDiscard") int skip_loop_filter(); public native AVCodecContext skip_loop_filter(int skip_loop_filter);

    /**
     * Skip IDCT/dequantization for selected frames.
     * - encoding: unused
     * - decoding: Set by user.
     */
    public native @Cast("AVDiscard") int skip_idct(); public native AVCodecContext skip_idct(int skip_idct);

    /**
     * Skip decoding for selected frames.
     * - encoding: unused
     * - decoding: Set by user.
     */
    public native @Cast("AVDiscard") int skip_frame(); public native AVCodecContext skip_frame(int skip_frame);

    /**
     * Header containing style information for text subtitles.
     * For SUBTITLE_ASS subtitle type, it should contain the whole ASS
     * [Script Info] and [V4+ Styles] section, plus the [Events] line and
     * the Format line following. It shouldn't include any Dialogue line.
     * - encoding: Set/allocated/freed by user (before avcodec_open2())
     * - decoding: Set/allocated/freed by libavcodec (by avcodec_open2())
     */
    public native @Cast("uint8_t*") BytePointer subtitle_header(); public native AVCodecContext subtitle_header(BytePointer subtitle_header);
    public native int subtitle_header_size(); public native AVCodecContext subtitle_header_size(int subtitle_header_size);

// #if FF_API_ERROR_RATE
    /**
     * @deprecated use the 'error_rate' private AVOption of the mpegvideo
     * encoders
     */
    public native @Deprecated int error_rate(); public native AVCodecContext error_rate(int error_rate);
// #endif

// #if FF_API_VBV_DELAY
    /**
     * VBV delay coded in the last frame (in periods of a 27 MHz clock).
     * Used for compliant TS muxing.
     * - encoding: Set by libavcodec.
     * - decoding: unused.
     * @deprecated this value is now exported as a part of
     * AV_PKT_DATA_CPB_PROPERTIES packet side data
     */
    public native @Cast("uint64_t") @Deprecated long vbv_delay(); public native AVCodecContext vbv_delay(long vbv_delay);
// #endif

// #if FF_API_SIDEDATA_ONLY_PKT
    /**
     * Encoding only and set by default. Allow encoders to output packets
     * that do not contain any encoded data, only side data.
     *
     * Some encoders need to output such packets, e.g. to update some stream
     * parameters at the end of encoding.
     *
     * @deprecated this field disables the default behaviour and
     *             it is kept only for compatibility.
     */
    public native @Deprecated int side_data_only_packets(); public native AVCodecContext side_data_only_packets(int side_data_only_packets);
// #endif

    /**
     * Audio only. The number of "priming" samples (padding) inserted by the
     * encoder at the beginning of the audio. I.e. this number of leading
     * decoded samples must be discarded by the caller to get the original audio
     * without leading padding.
     *
     * - decoding: unused
     * - encoding: Set by libavcodec. The timestamps on the output packets are
     *             adjusted by the encoder so that they always refer to the
     *             first sample of the data actually contained in the packet,
     *             including any added padding.  E.g. if the timebase is
     *             1/samplerate and the timestamp of the first input sample is
     *             0, the timestamp of the first output packet will be
     *             -initial_padding.
     */
    public native int initial_padding(); public native AVCodecContext initial_padding(int initial_padding);

    /**
     * - decoding: For codecs that store a framerate value in the compressed
     *             bitstream, the decoder may export it here. { 0, 1} when
     *             unknown.
     * - encoding: unused
     */
    public native @ByRef AVRational framerate(); public native AVCodecContext framerate(AVRational framerate);

    /**
     * Nominal unaccelerated pixel format, see AV_PIX_FMT_xxx.
     * - encoding: unused.
     * - decoding: Set by libavcodec before calling get_format()
     */
    public native @Cast("AVPixelFormat") int sw_pix_fmt(); public native AVCodecContext sw_pix_fmt(int sw_pix_fmt);

    /**
     * Timebase in which pkt_dts/pts and AVPacket.dts/pts are.
     * Code outside libavcodec should access this field using:
     * av_codec_{get,set}_pkt_timebase(avctx)
     * - encoding unused.
     * - decoding set by user.
     */
    public native @ByRef AVRational pkt_timebase(); public native AVCodecContext pkt_timebase(AVRational pkt_timebase);

    /**
     * AVCodecDescriptor
     * Code outside libavcodec should access this field using:
     * av_codec_{get,set}_codec_descriptor(avctx)
     * - encoding: unused.
     * - decoding: set by libavcodec.
     */
    @MemberGetter public native @Const AVCodecDescriptor codec_descriptor();

// #if !FF_API_LOWRES
// #endif

    /**
     * Current statistics for PTS correction.
     * - decoding: maintained and used by libavcodec, not intended to be used by user apps
     * - encoding: unused
     */
    public native long pts_correction_num_faulty_pts(); public native AVCodecContext pts_correction_num_faulty_pts(long pts_correction_num_faulty_pts); /** Number of incorrect PTS values so far */
    public native long pts_correction_num_faulty_dts(); public native AVCodecContext pts_correction_num_faulty_dts(long pts_correction_num_faulty_dts); /** Number of incorrect DTS values so far */
    public native long pts_correction_last_pts(); public native AVCodecContext pts_correction_last_pts(long pts_correction_last_pts);       /** PTS of the last frame */
    public native long pts_correction_last_dts(); public native AVCodecContext pts_correction_last_dts(long pts_correction_last_dts);       /** DTS of the last frame

    /**
     * Character encoding of the input subtitles file.
     * - decoding: set by user
     * - encoding: unused
     */
    public native @Cast("char*") BytePointer sub_charenc(); public native AVCodecContext sub_charenc(BytePointer sub_charenc);

    /**
     * Subtitles character encoding mode. Formats or codecs might be adjusting
     * this setting (if they are doing the conversion themselves for instance).
     * - decoding: set by libavcodec
     * - encoding: unused
     */
    public native int sub_charenc_mode(); public native AVCodecContext sub_charenc_mode(int sub_charenc_mode);
/** do nothing (demuxer outputs a stream supposed to be already in UTF-8, or the codec is bitmap for instance) */
public static final int FF_SUB_CHARENC_MODE_DO_NOTHING =  -1;
/** libavcodec will select the mode itself */
public static final int FF_SUB_CHARENC_MODE_AUTOMATIC =    0;
/** the AVPacket data needs to be recoded to UTF-8 before being fed to the decoder, requires iconv */
public static final int FF_SUB_CHARENC_MODE_PRE_DECODER =  1;

    /**
     * Skip processing alpha if supported by codec.
     * Note that if the format uses pre-multiplied alpha (common with VP6,
     * and recommended due to better video quality/compression)
     * the image will look as if alpha-blended onto a black background.
     * However for formats that do not use pre-multiplied alpha
     * there might be serious artefacts (though e.g. libswscale currently
     * assumes pre-multiplied alpha anyway).
     * Code outside libavcodec should access this field using AVOptions
     *
     * - decoding: set by user
     * - encoding: unused
     */
    public native int skip_alpha(); public native AVCodecContext skip_alpha(int skip_alpha);

    /**
     * Number of samples to skip after a discontinuity
     * - decoding: unused
     * - encoding: set by libavcodec
     */
    public native int seek_preroll(); public native AVCodecContext seek_preroll(int seek_preroll);

// #if !FF_API_DEBUG_MV
// #endif

    /**
     * custom intra quantization matrix
     * Code outside libavcodec should access this field using av_codec_g/set_chroma_intra_matrix()
     * - encoding: Set by user, can be NULL.
     * - decoding: unused.
     */
    public native @Cast("uint16_t*") ShortPointer chroma_intra_matrix(); public native AVCodecContext chroma_intra_matrix(ShortPointer chroma_intra_matrix);

    /**
     * dump format separator.
     * can be ", " or "\n      " or anything else
     * Code outside libavcodec should access this field using AVOptions
     * (NO direct access).
     * - encoding: Set by user.
     * - decoding: Set by user.
     */
    public native @Cast("uint8_t*") BytePointer dump_separator(); public native AVCodecContext dump_separator(BytePointer dump_separator);

    /**
     * ',' separated list of allowed decoders.
     * If NULL then all are allowed
     * - encoding: unused
     * - decoding: set by user through AVOPtions (NO direct access)
     */
    public native @Cast("char*") BytePointer codec_whitelist(); public native AVCodecContext codec_whitelist(BytePointer codec_whitelist);

    /*
     * Properties of the stream that gets decoded
     * To be accessed through av_codec_get_properties() (NO direct access)
     * - encoding: unused
     * - decoding: set by libavcodec
     */
    public native @Cast("unsigned") int properties(); public native AVCodecContext properties(int properties);
public static final int FF_CODEC_PROPERTY_LOSSLESS =        0x00000001;
public static final int FF_CODEC_PROPERTY_CLOSED_CAPTIONS = 0x00000002;

    /**
     * Additional data associated with the entire coded stream.
     *
     * - decoding: unused
     * - encoding: may be set by libavcodec after avcodec_open2().
     */
    public native AVPacketSideData coded_side_data(); public native AVCodecContext coded_side_data(AVPacketSideData coded_side_data);
    public native int nb_coded_side_data(); public native AVCodecContext nb_coded_side_data(int nb_coded_side_data);

}

public static native @ByVal AVRational av_codec_get_pkt_timebase(@Const AVCodecContext avctx);
public static native void av_codec_set_pkt_timebase(AVCodecContext avctx, @ByVal AVRational val);

public static native @Const AVCodecDescriptor av_codec_get_codec_descriptor(@Const AVCodecContext avctx);
public static native void av_codec_set_codec_descriptor(AVCodecContext avctx, @Const AVCodecDescriptor desc);

public static native @Cast("unsigned") int av_codec_get_codec_properties(@Const AVCodecContext avctx);

public static native int av_codec_get_lowres(@Const AVCodecContext avctx);
public static native void av_codec_set_lowres(AVCodecContext avctx, int val);

public static native int av_codec_get_seek_preroll(@Const AVCodecContext avctx);
public static native void av_codec_set_seek_preroll(AVCodecContext avctx, int val);

public static native @Cast("uint16_t*") ShortPointer av_codec_get_chroma_intra_matrix(@Const AVCodecContext avctx);
public static native void av_codec_set_chroma_intra_matrix(AVCodecContext avctx, @Cast("uint16_t*") ShortPointer val);
public static native void av_codec_set_chroma_intra_matrix(AVCodecContext avctx, @Cast("uint16_t*") ShortBuffer val);
public static native void av_codec_set_chroma_intra_matrix(AVCodecContext avctx, @Cast("uint16_t*") short[] val);

/**
 * AVProfile.
 */
public static class AVProfile extends Pointer {
    static { Loader.load(); }
    /** Default native constructor. */
    public AVProfile() { allocate(); }
    /** Native array allocator. Access with {@link Pointer#position(int)}. */
    public AVProfile(int size) { allocateArray(size); }
    /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
    public AVProfile(Pointer p) { super(p); }
    private native void allocate();
    private native void allocateArray(int size);
    @Override public AVProfile position(int position) {
        return (AVProfile)super.position(position);
    }

    public native int profile(); public native AVProfile profile(int profile);
    /** short name for the profile */
    @MemberGetter public native @Cast("const char*") BytePointer name();
}

@Opaque public static class AVCodecDefault extends Pointer {
    /** Empty constructor. */
    public AVCodecDefault() { }
    /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
    public AVCodecDefault(Pointer p) { super(p); }
}

/**
 * AVCodec.
 */
public static class AVCodec extends Pointer {
    static { Loader.load(); }
    /** Default native constructor. */
    public AVCodec() { allocate(); }
    /** Native array allocator. Access with {@link Pointer#position(int)}. */
    public AVCodec(int size) { allocateArray(size); }
    /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
    public AVCodec(Pointer p) { super(p); }
    private native void allocate();
    private native void allocateArray(int size);
    @Override public AVCodec position(int position) {
        return (AVCodec)super.position(position);
    }

    /**
     * Name of the codec implementation.
     * The name is globally unique among encoders and among decoders (but an
     * encoder and a decoder can share the same name).
     * This is the primary way to find a codec from the user perspective.
     */
    @MemberGetter public native @Cast("const char*") BytePointer name();
    /**
     * Descriptive name for the codec, meant to be more human readable than name.
     * You should use the NULL_IF_CONFIG_SMALL() macro to define it.
     */
    @MemberGetter public native @Cast("const char*") BytePointer long_name();
    public native @Cast("AVMediaType") int type(); public native AVCodec type(int type);
    public native @Cast("AVCodecID") int id(); public native AVCodec id(int id);
    /**
     * Codec capabilities.
     * see AV_CODEC_CAP_*
     */
    public native int capabilities(); public native AVCodec capabilities(int capabilities);
    /** array of supported framerates, or NULL if any, array is terminated by {0,0} */
    @MemberGetter public native @Const AVRational supported_framerates();
    /** array of supported pixel formats, or NULL if unknown, array is terminated by -1 */
    @MemberGetter public native @Cast("const AVPixelFormat*") IntPointer pix_fmts();
    /** array of supported audio samplerates, or NULL if unknown, array is terminated by 0 */
    @MemberGetter public native @Const IntPointer supported_samplerates();
    /** array of supported sample formats, or NULL if unknown, array is terminated by -1 */
    @MemberGetter public native @Cast("const AVSampleFormat*") IntPointer sample_fmts();
    /** array of support channel layouts, or NULL if unknown. array is terminated by 0 */
    @MemberGetter public native @Cast("const uint64_t*") LongPointer channel_layouts();
    /** maximum value for lowres supported by the decoder, no direct access, use av_codec_get_max_lowres() */
    public native @Cast("uint8_t") byte max_lowres(); public native AVCodec max_lowres(byte max_lowres);
    /** AVClass for the private context */
    @MemberGetter public native @Const AVClass priv_class();
    /** array of recognized profiles, or NULL if unknown, array is terminated by {FF_PROFILE_UNKNOWN} */
    @MemberGetter public native @Const AVProfile profiles();

    /*****************************************************************
     * No fields below this line are part of the public API. They
     * may not be used outside of libavcodec and can be changed and
     * removed at will.
     * New public fields should be added right above.
     *****************************************************************
     */
    public native int priv_data_size(); public native AVCodec priv_data_size(int priv_data_size);
    public native AVCodec next(); public native AVCodec next(AVCodec next);
    /**
     * @name Frame-level threading support functions
     * @{
     */
    /**
     * If defined, called on thread contexts when they are created.
     * If the codec allocates writable tables in init(), re-allocate them here.
     * priv_data will be set to a copy of the original.
     */
    public static class Init_thread_copy_AVCodecContext extends FunctionPointer {
        static { Loader.load(); }
        /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
        public    Init_thread_copy_AVCodecContext(Pointer p) { super(p); }
        protected Init_thread_copy_AVCodecContext() { allocate(); }
        private native void allocate();
        public native int call(AVCodecContext arg0);
    }
    public native Init_thread_copy_AVCodecContext init_thread_copy(); public native AVCodec init_thread_copy(Init_thread_copy_AVCodecContext init_thread_copy);
    /**
     * Copy necessary context variables from a previous thread context to the current one.
     * If not defined, the next thread will start automatically; otherwise, the codec
     * must call ff_thread_finish_setup().
     *
     * dst and src will (rarely) point to the same context, in which case memcpy should be skipped.
     */
    public static class Update_thread_context_AVCodecContext_AVCodecContext extends FunctionPointer {
        static { Loader.load(); }
        /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
        public    Update_thread_context_AVCodecContext_AVCodecContext(Pointer p) { super(p); }
        protected Update_thread_context_AVCodecContext_AVCodecContext() { allocate(); }
        private native void allocate();
        public native int call(AVCodecContext dst, @Const AVCodecContext src);
    }
    public native Update_thread_context_AVCodecContext_AVCodecContext update_thread_context(); public native AVCodec update_thread_context(Update_thread_context_AVCodecContext_AVCodecContext update_thread_context);
    /** @} */

    /**
     * Private codec-specific defaults.
     */
    @MemberGetter public native @Const AVCodecDefault defaults();

    /**
     * Initialize codec static data, called from avcodec_register().
     */
    public static class Init_static_data_AVCodec extends FunctionPointer {
        static { Loader.load(); }
        /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
        public    Init_static_data_AVCodec(Pointer p) { super(p); }
        protected Init_static_data_AVCodec() { allocate(); }
        private native void allocate();
        public native void call(AVCodec codec);
    }
    public native Init_static_data_AVCodec init_static_data(); public native AVCodec init_static_data(Init_static_data_AVCodec init_static_data);

    public static class Init_AVCodecContext extends FunctionPointer {
        static { Loader.load(); }
        /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
        public    Init_AVCodecContext(Pointer p) { super(p); }
        protected Init_AVCodecContext() { allocate(); }
        private native void allocate();
        public native int call(AVCodecContext arg0);
    }
    public native Init_AVCodecContext init(); public native AVCodec init(Init_AVCodecContext init);
    public static class Encode_sub_AVCodecContext_BytePointer_int_AVSubtitle extends FunctionPointer {
        static { Loader.load(); }
        /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
        public    Encode_sub_AVCodecContext_BytePointer_int_AVSubtitle(Pointer p) { super(p); }
        protected Encode_sub_AVCodecContext_BytePointer_int_AVSubtitle() { allocate(); }
        private native void allocate();
        public native int call(AVCodecContext arg0, @Cast("uint8_t*") BytePointer buf, int buf_size,
                          @Const AVSubtitle sub);
    }
    public native Encode_sub_AVCodecContext_BytePointer_int_AVSubtitle encode_sub(); public native AVCodec encode_sub(Encode_sub_AVCodecContext_BytePointer_int_AVSubtitle encode_sub);
    /**
     * Encode data to an AVPacket.
     *
     * @param      avctx          codec context
     * @param      avpkt          output AVPacket (may contain a user-provided buffer)
     * @param[in]  frame          AVFrame containing the raw data to be encoded
     * @param[out] got_packet_ptr encoder sets to 0 or 1 to indicate that a
     *                            non-empty packet was returned in avpkt.
     * @return 0 on success, negative error code on failure
     */
    public static class Encode2_AVCodecContext_AVPacket_AVFrame_IntPointer extends FunctionPointer {
        static { Loader.load(); }
        /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
        public    Encode2_AVCodecContext_AVPacket_AVFrame_IntPointer(Pointer p) { super(p); }
        protected Encode2_AVCodecContext_AVPacket_AVFrame_IntPointer() { allocate(); }
        private native void allocate();
        public native int call(AVCodecContext avctx, AVPacket avpkt, @Const AVFrame frame,
                       IntPointer got_packet_ptr);
    }
    public native Encode2_AVCodecContext_AVPacket_AVFrame_IntPointer encode2(); public native AVCodec encode2(Encode2_AVCodecContext_AVPacket_AVFrame_IntPointer encode2);
    public static class Decode_AVCodecContext_Pointer_IntPointer_AVPacket extends FunctionPointer {
        static { Loader.load(); }
        /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
        public    Decode_AVCodecContext_Pointer_IntPointer_AVPacket(Pointer p) { super(p); }
        protected Decode_AVCodecContext_Pointer_IntPointer_AVPacket() { allocate(); }
        private native void allocate();
        public native int call(AVCodecContext arg0, Pointer outdata, IntPointer outdata_size, AVPacket avpkt);
    }
    public native Decode_AVCodecContext_Pointer_IntPointer_AVPacket decode(); public native AVCodec decode(Decode_AVCodecContext_Pointer_IntPointer_AVPacket decode);
    public static class Close_AVCodecContext extends FunctionPointer {
        static { Loader.load(); }
        /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
        public    Close_AVCodecContext(Pointer p) { super(p); }
        protected Close_AVCodecContext() { allocate(); }
        private native void allocate();
        public native int call(AVCodecContext arg0);
    }
    public native @Name("close") Close_AVCodecContext _close(); public native AVCodec _close(Close_AVCodecContext _close);
    /**
     * Flush buffers.
     * Will be called when seeking
     */
    public static class Flush_AVCodecContext extends FunctionPointer {
        static { Loader.load(); }
        /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
        public    Flush_AVCodecContext(Pointer p) { super(p); }
        protected Flush_AVCodecContext() { allocate(); }
        private native void allocate();
        public native void call(AVCodecContext arg0);
    }
    public native Flush_AVCodecContext flush(); public native AVCodec flush(Flush_AVCodecContext flush);
    /**
     * Internal codec capabilities.
     * See FF_CODEC_CAP_* in internal.h
     */
    public native int caps_internal(); public native AVCodec caps_internal(int caps_internal);
}

public static native int av_codec_get_max_lowres(@Const AVCodec codec);

@Opaque public static class MpegEncContext extends Pointer {
    /** Empty constructor. */
    public MpegEncContext() { }
    /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
    public MpegEncContext(Pointer p) { super(p); }
}

/**
 * @defgroup lavc_hwaccel AVHWAccel
 * @{
 */
public static class AVHWAccel extends Pointer {
    static { Loader.load(); }
    /** Default native constructor. */
    public AVHWAccel() { allocate(); }
    /** Native array allocator. Access with {@link Pointer#position(int)}. */
    public AVHWAccel(int size) { allocateArray(size); }
    /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
    public AVHWAccel(Pointer p) { super(p); }
    private native void allocate();
    private native void allocateArray(int size);
    @Override public AVHWAccel position(int position) {
        return (AVHWAccel)super.position(position);
    }

    /**
     * Name of the hardware accelerated codec.
     * The name is globally unique among encoders and among decoders (but an
     * encoder and a decoder can share the same name).
     */
    @MemberGetter public native @Cast("const char*") BytePointer name();

    /**
     * Type of codec implemented by the hardware accelerator.
     *
     * See AVMEDIA_TYPE_xxx
     */
    public native @Cast("AVMediaType") int type(); public native AVHWAccel type(int type);

    /**
     * Codec implemented by the hardware accelerator.
     *
     * See AV_CODEC_ID_xxx
     */
    public native @Cast("AVCodecID") int id(); public native AVHWAccel id(int id);

    /**
     * Supported pixel format.
     *
     * Only hardware accelerated formats are supported here.
     */
    public native @Cast("AVPixelFormat") int pix_fmt(); public native AVHWAccel pix_fmt(int pix_fmt);

    /**
     * Hardware accelerated codec capabilities.
     * see HWACCEL_CODEC_CAP_*
     */
    public native int capabilities(); public native AVHWAccel capabilities(int capabilities);

    /*****************************************************************
     * No fields below this line are part of the public API. They
     * may not be used outside of libavcodec and can be changed and
     * removed at will.
     * New public fields should be added right above.
     *****************************************************************
     */
    public native AVHWAccel next(); public native AVHWAccel next(AVHWAccel next);

    /**
     * Allocate a custom buffer
     */
    public static class Alloc_frame_AVCodecContext_AVFrame extends FunctionPointer {
        static { Loader.load(); }
        /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
        public    Alloc_frame_AVCodecContext_AVFrame(Pointer p) { super(p); }
        protected Alloc_frame_AVCodecContext_AVFrame() { allocate(); }
        private native void allocate();
        public native int call(AVCodecContext avctx, AVFrame frame);
    }
    public native Alloc_frame_AVCodecContext_AVFrame alloc_frame(); public native AVHWAccel alloc_frame(Alloc_frame_AVCodecContext_AVFrame alloc_frame);

    /**
     * Called at the beginning of each frame or field picture.
     *
     * Meaningful frame information (codec specific) is guaranteed to
     * be parsed at this point. This function is mandatory.
     *
     * Note that buf can be NULL along with buf_size set to 0.
     * Otherwise, this means the whole frame is available at this point.
     *
     * @param avctx the codec context
     * @param buf the frame data buffer base
     * @param buf_size the size of the frame in bytes
     * @return zero if successful, a negative value otherwise
     */
    public static class Start_frame_AVCodecContext_BytePointer_int extends FunctionPointer {
        static { Loader.load(); }
        /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
        public    Start_frame_AVCodecContext_BytePointer_int(Pointer p) { super(p); }
        protected Start_frame_AVCodecContext_BytePointer_int() { allocate(); }
        private native void allocate();
        public native int call(AVCodecContext avctx, @Cast("const uint8_t*") BytePointer buf, @Cast("uint32_t") int buf_size);
    }
    public native Start_frame_AVCodecContext_BytePointer_int start_frame(); public native AVHWAccel start_frame(Start_frame_AVCodecContext_BytePointer_int start_frame);

    /**
     * Callback for each slice.
     *
     * Meaningful slice information (codec specific) is guaranteed to
     * be parsed at this point. This function is mandatory.
     * The only exception is XvMC, that works on MB level.
     *
     * @param avctx the codec context
     * @param buf the slice data buffer base
     * @param buf_size the size of the slice in bytes
     * @return zero if successful, a negative value otherwise
     */
    public static class Decode_slice_AVCodecContext_BytePointer_int extends FunctionPointer {
        static { Loader.load(); }
        /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
        public    Decode_slice_AVCodecContext_BytePointer_int(Pointer p) { super(p); }
        protected Decode_slice_AVCodecContext_BytePointer_int() { allocate(); }
        private native void allocate();
        public native int call(AVCodecContext avctx, @Cast("const uint8_t*") BytePointer buf, @Cast("uint32_t") int buf_size);
    }
    public native Decode_slice_AVCodecContext_BytePointer_int decode_slice(); public native AVHWAccel decode_slice(Decode_slice_AVCodecContext_BytePointer_int decode_slice);

    /**
     * Called at the end of each frame or field picture.
     *
     * The whole picture is parsed at this point and can now be sent
     * to the hardware accelerator. This function is mandatory.
     *
     * @param avctx the codec context
     * @return zero if successful, a negative value otherwise
     */
    public static class End_frame_AVCodecContext extends FunctionPointer {
        static { Loader.load(); }
        /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
        public    End_frame_AVCodecContext(Pointer p) { super(p); }
        protected End_frame_AVCodecContext() { allocate(); }
        private native void allocate();
        public native int call(AVCodecContext avctx);
    }
    public native End_frame_AVCodecContext end_frame(); public native AVHWAccel end_frame(End_frame_AVCodecContext end_frame);

    /**
     * Size of per-frame hardware accelerator private data.
     *
     * Private data is allocated with av_mallocz() before
     * AVCodecContext.get_buffer() and deallocated after
     * AVCodecContext.release_buffer().
     */
    public native int frame_priv_data_size(); public native AVHWAccel frame_priv_data_size(int frame_priv_data_size);

    /**
     * Called for every Macroblock in a slice.
     *
     * XvMC uses it to replace the ff_mpv_decode_mb().
     * Instead of decoding to raw picture, MB parameters are
     * stored in an array provided by the video driver.
     *
     * @param s the mpeg context
     */
    public static class Decode_mb_MpegEncContext extends FunctionPointer {
        static { Loader.load(); }
        /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
        public    Decode_mb_MpegEncContext(Pointer p) { super(p); }
        protected Decode_mb_MpegEncContext() { allocate(); }
        private native void allocate();
        public native void call(MpegEncContext s);
    }
    public native Decode_mb_MpegEncContext decode_mb(); public native AVHWAccel decode_mb(Decode_mb_MpegEncContext decode_mb);

    /**
     * Initialize the hwaccel private data.
     *
     * This will be called from ff_get_format(), after hwaccel and
     * hwaccel_context are set and the hwaccel private data in AVCodecInternal
     * is allocated.
     */
    public static class Init_AVCodecContext extends FunctionPointer {
        static { Loader.load(); }
        /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
        public    Init_AVCodecContext(Pointer p) { super(p); }
        protected Init_AVCodecContext() { allocate(); }
        private native void allocate();
        public native int call(AVCodecContext avctx);
    }
    public native Init_AVCodecContext init(); public native AVHWAccel init(Init_AVCodecContext init);

    /**
     * Uninitialize the hwaccel private data.
     *
     * This will be called from get_format() or avcodec_close(), after hwaccel
     * and hwaccel_context are already uninitialized.
     */
    public static class Uninit_AVCodecContext extends FunctionPointer {
        static { Loader.load(); }
        /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
        public    Uninit_AVCodecContext(Pointer p) { super(p); }
        protected Uninit_AVCodecContext() { allocate(); }
        private native void allocate();
        public native int call(AVCodecContext avctx);
    }
    public native Uninit_AVCodecContext uninit(); public native AVHWAccel uninit(Uninit_AVCodecContext uninit);

    /**
     * Size of the private data to allocate in
     * AVCodecInternal.hwaccel_priv_data.
     */
    public native int priv_data_size(); public native AVHWAccel priv_data_size(int priv_data_size);
}

/**
 * Hardware acceleration should be used for decoding even if the codec level
 * used is unknown or higher than the maximum supported level reported by the
 * hardware driver.
 *
 * It's generally a good idea to pass this flag unless you have a specific
 * reason not to, as hardware tends to under-report supported levels.
 */
public static final int AV_HWACCEL_FLAG_IGNORE_LEVEL = (1 << 0);

/**
 * Hardware acceleration can output YUV pixel formats with a different chroma
 * sampling than 4:2:0 and/or other than 8 bits per component.
 */
public static final int AV_HWACCEL_FLAG_ALLOW_HIGH_DEPTH = (1 << 1);

/**
 * @}
 */

// #if FF_API_AVPICTURE
/**
 * @defgroup lavc_picture AVPicture
 *
 * Functions for working with AVPicture
 * @{
 */

/**
 * Picture data structure.
 *
 * Up to four components can be stored into it, the last component is
 * alpha.
 * @deprecated use AVFrame or imgutils functions instead
 */
public static class AVPicture extends Pointer {
    static { Loader.load(); }
    /** Default native constructor. */
    public AVPicture() { allocate(); }
    /** Native array allocator. Access with {@link Pointer#position(int)}. */
    public AVPicture(int size) { allocateArray(size); }
    /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
    public AVPicture(Pointer p) { super(p); }
    private native void allocate();
    private native void allocateArray(int size);
    @Override public AVPicture position(int position) {
        return (AVPicture)super.position(position);
    }

    /** pointers to the image data planes */
    public native @Cast("uint8_t*") @Deprecated BytePointer data(int i); public native AVPicture data(int i, BytePointer data);
    @MemberGetter public native @Cast("uint8_t**") @Deprecated PointerPointer data();
    /** number of bytes per line */
    public native @Deprecated int linesize(int i); public native AVPicture linesize(int i, int linesize);
    @MemberGetter public native @Deprecated IntPointer linesize();
}

/**
 * @}
 */
// #endif

/** enum AVSubtitleType */
public static final int
    SUBTITLE_NONE = 0,

    /** A bitmap, pict will be set */
    SUBTITLE_BITMAP = 1,

    /**
     * Plain text, the text field must be set by the decoder and is
     * authoritative. ass and pict fields may contain approximations.
     */
    SUBTITLE_TEXT = 2,

    /**
     * Formatted text, the ass field must be set by the decoder and is
     * authoritative. pict and text fields may contain approximations.
     */
    SUBTITLE_ASS = 3;

public static final int AV_SUBTITLE_FLAG_FORCED = 0x00000001;

public static class AVSubtitleRect extends Pointer {
    static { Loader.load(); }
    /** Default native constructor. */
    public AVSubtitleRect() { allocate(); }
    /** Native array allocator. Access with {@link Pointer#position(int)}. */
    public AVSubtitleRect(int size) { allocateArray(size); }
    /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
    public AVSubtitleRect(Pointer p) { super(p); }
    private native void allocate();
    private native void allocateArray(int size);
    @Override public AVSubtitleRect position(int position) {
        return (AVSubtitleRect)super.position(position);
    }

    /** top left corner  of pict, undefined when pict is not set */
    public native int x(); public native AVSubtitleRect x(int x);
    /** top left corner  of pict, undefined when pict is not set */
    public native int y(); public native AVSubtitleRect y(int y);
    /** width            of pict, undefined when pict is not set */
    public native int w(); public native AVSubtitleRect w(int w);
    /** height           of pict, undefined when pict is not set */
    public native int h(); public native AVSubtitleRect h(int h);
    /** number of colors in pict, undefined when pict is not set */
    public native int nb_colors(); public native AVSubtitleRect nb_colors(int nb_colors);

// #if FF_API_AVPICTURE
    /**
     * @deprecated unused
     */
    public native @Deprecated @ByRef AVPicture pict(); public native AVSubtitleRect pict(AVPicture pict);
// #endif
    /**
     * data+linesize for the bitmap of this subtitle.
     * Can be set for text/ass as well once they are rendered.
     */
    public native @Cast("uint8_t*") BytePointer data(int i); public native AVSubtitleRect data(int i, BytePointer data);
    @MemberGetter public native @Cast("uint8_t**") PointerPointer data();
    public native int linesize(int i); public native AVSubtitleRect linesize(int i, int linesize);
    @MemberGetter public native IntPointer linesize();

    public native @Cast("AVSubtitleType") int type(); public native AVSubtitleRect type(int type);

    /** 0 terminated plain UTF-8 text */
    public native @Cast("char*") BytePointer text(); public native AVSubtitleRect text(BytePointer text);

    /**
     * 0 terminated ASS/SSA compatible event line.
     * The presentation of this is unaffected by the other values in this
     * struct.
     */
    public native @Cast("char*") BytePointer ass(); public native AVSubtitleRect ass(BytePointer ass);

    public native int flags(); public native AVSubtitleRect flags(int flags);
}

public static class AVSubtitle extends Pointer {
    static { Loader.load(); }
    /** Default native constructor. */
    public AVSubtitle() { allocate(); }
    /** Native array allocator. Access with {@link Pointer#position(int)}. */
    public AVSubtitle(int size) { allocateArray(size); }
    /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
    public AVSubtitle(Pointer p) { super(p); }
    private native void allocate();
    private native void allocateArray(int size);
    @Override public AVSubtitle position(int position) {
        return (AVSubtitle)super.position(position);
    }

    public native @Cast("uint16_t") short format(); public native AVSubtitle format(short format); /* 0 = graphics */
    public native @Cast("uint32_t") int start_display_time(); public native AVSubtitle start_display_time(int start_display_time); /* relative to packet pts, in ms */
    public native @Cast("uint32_t") int end_display_time(); public native AVSubtitle end_display_time(int end_display_time); /* relative to packet pts, in ms */
    public native @Cast("unsigned") int num_rects(); public native AVSubtitle num_rects(int num_rects);
    public native AVSubtitleRect rects(int i); public native AVSubtitle rects(int i, AVSubtitleRect rects);
    @MemberGetter public native @Cast("AVSubtitleRect**") PointerPointer rects();
    /** Same as packet pts, in AV_TIME_BASE */
    public native long pts(); public native AVSubtitle pts(long pts);
}

/**
 * If c is NULL, returns the first registered codec,
 * if c is non-NULL, returns the next registered codec after c,
 * or NULL if c is the last one.
 */
public static native AVCodec av_codec_next(@Const AVCodec c);

/**
 * Return the LIBAVCODEC_VERSION_INT constant.
 */
public static native @Cast("unsigned") int avcodec_version();

/**
 * Return the libavcodec build-time configuration.
 */
public static native @Cast("const char*") BytePointer avcodec_configuration();

/**
 * Return the libavcodec license.
 */
public static native @Cast("const char*") BytePointer avcodec_license();

/**
 * Register the codec codec and initialize libavcodec.
 *
 * @warning either this function or avcodec_register_all() must be called
 * before any other libavcodec functions.
 *
 * @see avcodec_register_all()
 */
public static native void avcodec_register(AVCodec codec);

/**
 * Register all the codecs, parsers and bitstream filters which were enabled at
 * configuration time. If you do not call this function you can select exactly
 * which formats you want to support, by using the individual registration
 * functions.
 *
 * @see avcodec_register
 * @see av_register_codec_parser
 * @see av_register_bitstream_filter
 */
public static native void avcodec_register_all();

/**
 * Allocate an AVCodecContext and set its fields to default values. The
 * resulting struct should be freed with avcodec_free_context().
 *
 * @param codec if non-NULL, allocate private data and initialize defaults
 *              for the given codec. It is illegal to then call avcodec_open2()
 *              with a different codec.
 *              If NULL, then the codec-specific defaults won't be initialized,
 *              which may result in suboptimal default settings (this is
 *              important mainly for encoders, e.g. libx264).
 *
 * @return An AVCodecContext filled with default values or NULL on failure.
 * @see avcodec_get_context_defaults
 */
public static native AVCodecContext avcodec_alloc_context3(@Const AVCodec codec);

/**
 * Free the codec context and everything associated with it and write NULL to
 * the provided pointer.
 */
public static native void avcodec_free_context(@Cast("AVCodecContext**") PointerPointer avctx);
public static native void avcodec_free_context(@ByPtrPtr AVCodecContext avctx);

/**
 * Set the fields of the given AVCodecContext to default values corresponding
 * to the given codec (defaults may be codec-dependent).
 *
 * Do not call this function if a non-NULL codec has been passed
 * to avcodec_alloc_context3() that allocated this AVCodecContext.
 * If codec is non-NULL, it is illegal to call avcodec_open2() with a
 * different codec on this AVCodecContext.
 */
public static native int avcodec_get_context_defaults3(AVCodecContext s, @Const AVCodec codec);

/**
 * Get the AVClass for AVCodecContext. It can be used in combination with
 * AV_OPT_SEARCH_FAKE_OBJ for examining options.
 *
 * @see av_opt_find().
 */
public static native @Const AVClass avcodec_get_class();

/**
 * Get the AVClass for AVFrame. It can be used in combination with
 * AV_OPT_SEARCH_FAKE_OBJ for examining options.
 *
 * @see av_opt_find().
 */
public static native @Const AVClass avcodec_get_frame_class();

/**
 * Get the AVClass for AVSubtitleRect. It can be used in combination with
 * AV_OPT_SEARCH_FAKE_OBJ for examining options.
 *
 * @see av_opt_find().
 */
public static native @Const AVClass avcodec_get_subtitle_rect_class();

/**
 * Copy the settings of the source AVCodecContext into the destination
 * AVCodecContext. The resulting destination codec context will be
 * unopened, i.e. you are required to call avcodec_open2() before you
 * can use this AVCodecContext to decode/encode video/audio data.
 *
 * @param dest target codec context, should be initialized with
 *             avcodec_alloc_context3(NULL), but otherwise uninitialized
 * @param src source codec context
 * @return AVERROR() on error (e.g. memory allocation error), 0 on success
 */
public static native int avcodec_copy_context(AVCodecContext dest, @Const AVCodecContext src);

/**
 * Initialize the AVCodecContext to use the given AVCodec. Prior to using this
 * function the context has to be allocated with avcodec_alloc_context3().
 *
 * The functions avcodec_find_decoder_by_name(), avcodec_find_encoder_by_name(),
 * avcodec_find_decoder() and avcodec_find_encoder() provide an easy way for
 * retrieving a codec.
 *
 * @warning This function is not thread safe!
 *
 * @note Always call this function before using decoding routines (such as
 * @ref avcodec_decode_video2()).
 *
 * @code
 * avcodec_register_all();
 * av_dict_set(&opts, "b", "2.5M", 0);
 * codec = avcodec_find_decoder(AV_CODEC_ID_H264);
 * if (!codec)
 *     exit(1);
 *
 * context = avcodec_alloc_context3(codec);
 *
 * if (avcodec_open2(context, codec, opts) < 0)
 *     exit(1);
 * @endcode
 *
 * @param avctx The context to initialize.
 * @param codec The codec to open this context for. If a non-NULL codec has been
 *              previously passed to avcodec_alloc_context3() or
 *              avcodec_get_context_defaults3() for this context, then this
 *              parameter MUST be either NULL or equal to the previously passed
 *              codec.
 * @param options A dictionary filled with AVCodecContext and codec-private options.
 *                On return this object will be filled with options that were not found.
 *
 * @return zero on success, a negative value on error
 * @see avcodec_alloc_context3(), avcodec_find_decoder(), avcodec_find_encoder(),
 *      av_dict_set(), av_opt_find().
 */
public static native int avcodec_open2(AVCodecContext avctx, @Const AVCodec codec, @Cast("AVDictionary**") PointerPointer options);
public static native int avcodec_open2(AVCodecContext avctx, @Const AVCodec codec, @ByPtrPtr AVDictionary options);

/**
 * Close a given AVCodecContext and free all the data associated with it
 * (but not the AVCodecContext itself).
 *
 * Calling this function on an AVCodecContext that hasn't been opened will free
 * the codec-specific data allocated in avcodec_alloc_context3() /
 * avcodec_get_context_defaults3() with a non-NULL codec. Subsequent calls will
 * do nothing.
 */
public static native int avcodec_close(AVCodecContext avctx);

/**
 * Free all allocated data in the given subtitle struct.
 *
 * @param sub AVSubtitle to free.
 */
public static native void avsubtitle_free(AVSubtitle sub);

/**
 * @}
 */

/**
 * @addtogroup lavc_packet
 * @{
 */

/**
 * Allocate an AVPacket and set its fields to default values.  The resulting
 * struct must be freed using av_packet_free().
 *
 * @return An AVPacket filled with default values or NULL on failure.
 *
 * @note this only allocates the AVPacket itself, not the data buffers. Those
 * must be allocated through other means such as av_new_packet.
 *
 * @see av_new_packet
 */
public static native AVPacket av_packet_alloc();

/**
 * Create a new packet that references the same data as src.
 *
 * This is a shortcut for av_packet_alloc()+av_packet_ref().
 *
 * @return newly created AVPacket on success, NULL on error.
 *
 * @see av_packet_alloc
 * @see av_packet_ref
 */
public static native AVPacket av_packet_clone(AVPacket src);

/**
 * Free the packet, if the packet is reference counted, it will be
 * unreferenced first.
 *
 * @param packet packet to be freed. The pointer will be set to NULL.
 * @note passing NULL is a no-op.
 */
public static native void av_packet_free(@Cast("AVPacket**") PointerPointer pkt);
public static native void av_packet_free(@ByPtrPtr AVPacket pkt);

/**
 * Initialize optional fields of a packet with default values.
 *
 * Note, this does not touch the data and size members, which have to be
 * initialized separately.
 *
 * @param pkt packet
 */
public static native void av_init_packet(AVPacket pkt);

/**
 * Allocate the payload of a packet and initialize its fields with
 * default values.
 *
 * @param pkt packet
 * @param size wanted payload size
 * @return 0 if OK, AVERROR_xxx otherwise
 */
public static native int av_new_packet(AVPacket pkt, int size);

/**
 * Reduce packet size, correctly zeroing padding
 *
 * @param pkt packet
 * @param size new size
 */
public static native void av_shrink_packet(AVPacket pkt, int size);

/**
 * Increase packet size, correctly zeroing padding
 *
 * @param pkt packet
 * @param grow_by number of bytes by which to increase the size of the packet
 */
public static native int av_grow_packet(AVPacket pkt, int grow_by);

/**
 * Initialize a reference-counted packet from av_malloc()ed data.
 *
 * @param pkt packet to be initialized. This function will set the data, size,
 *        buf and destruct fields, all others are left untouched.
 * @param data Data allocated by av_malloc() to be used as packet data. If this
 *        function returns successfully, the data is owned by the underlying AVBuffer.
 *        The caller may not access the data through other means.
 * @param size size of data in bytes, without the padding. I.e. the full buffer
 *        size is assumed to be size + AV_INPUT_BUFFER_PADDING_SIZE.
 *
 * @return 0 on success, a negative AVERROR on error
 */
public static native int av_packet_from_data(AVPacket pkt, @Cast("uint8_t*") BytePointer data, int size);
public static native int av_packet_from_data(AVPacket pkt, @Cast("uint8_t*") ByteBuffer data, int size);
public static native int av_packet_from_data(AVPacket pkt, @Cast("uint8_t*") byte[] data, int size);

// #if FF_API_AVPACKET_OLD_API
/**
 * @warning This is a hack - the packet memory allocation stuff is broken. The
 * packet is allocated if it was not really allocated.
 *
 * @deprecated Use av_packet_ref
 */
public static native @Deprecated int av_dup_packet(AVPacket pkt);
/**
 * Copy packet, including contents
 *
 * @return 0 on success, negative AVERROR on fail
 */
public static native int av_copy_packet(AVPacket dst, @Const AVPacket src);

/**
 * Copy packet side data
 *
 * @return 0 on success, negative AVERROR on fail
 */
public static native int av_copy_packet_side_data(AVPacket dst, @Const AVPacket src);

/**
 * Free a packet.
 *
 * @deprecated Use av_packet_unref
 *
 * @param pkt packet to free
 */
public static native @Deprecated void av_free_packet(AVPacket pkt);
// #endif
/**
 * Allocate new information of a packet.
 *
 * @param pkt packet
 * @param type side information type
 * @param size side information size
 * @return pointer to fresh allocated data or NULL otherwise
 */
public static native @Cast("uint8_t*") BytePointer av_packet_new_side_data(AVPacket pkt, @Cast("AVPacketSideDataType") int type,
                                 int size);

/**
 * Wrap an existing array as a packet side data.
 *
 * @param pkt packet
 * @param type side information type
 * @param data the side data array. It must be allocated with the av_malloc()
 *             family of functions. The ownership of the data is transferred to
 *             pkt.
 * @param size side information size
 * @return a non-negative number on success, a negative AVERROR code on
 *         failure. On failure, the packet is unchanged and the data remains
 *         owned by the caller.
 */
public static native int av_packet_add_side_data(AVPacket pkt, @Cast("AVPacketSideDataType") int type,
                            @Cast("uint8_t*") BytePointer data, @Cast("size_t") long size);
public static native int av_packet_add_side_data(AVPacket pkt, @Cast("AVPacketSideDataType") int type,
                            @Cast("uint8_t*") ByteBuffer data, @Cast("size_t") long size);
public static native int av_packet_add_side_data(AVPacket pkt, @Cast("AVPacketSideDataType") int type,
                            @Cast("uint8_t*") byte[] data, @Cast("size_t") long size);

/**
 * Shrink the already allocated side data buffer
 *
 * @param pkt packet
 * @param type side information type
 * @param size new side information size
 * @return 0 on success, < 0 on failure
 */
public static native int av_packet_shrink_side_data(AVPacket pkt, @Cast("AVPacketSideDataType") int type,
                               int size);

/**
 * Get side information from packet.
 *
 * @param pkt packet
 * @param type desired side information type
 * @param size pointer for side information size to store (optional)
 * @return pointer to data if present or NULL otherwise
 */
public static native @Cast("uint8_t*") BytePointer av_packet_get_side_data(AVPacket pkt, @Cast("AVPacketSideDataType") int type,
                                 IntPointer size);
public static native @Cast("uint8_t*") ByteBuffer av_packet_get_side_data(AVPacket pkt, @Cast("AVPacketSideDataType") int type,
                                 IntBuffer size);
public static native @Cast("uint8_t*") byte[] av_packet_get_side_data(AVPacket pkt, @Cast("AVPacketSideDataType") int type,
                                 int[] size);

public static native int av_packet_merge_side_data(AVPacket pkt);

public static native int av_packet_split_side_data(AVPacket pkt);

public static native @Cast("const char*") BytePointer av_packet_side_data_name(@Cast("AVPacketSideDataType") int type);

/**
 * Pack a dictionary for use in side_data.
 *
 * @param dict The dictionary to pack.
 * @param size pointer to store the size of the returned data
 * @return pointer to data if successful, NULL otherwise
 */
public static native @Cast("uint8_t*") BytePointer av_packet_pack_dictionary(AVDictionary dict, IntPointer size);
public static native @Cast("uint8_t*") ByteBuffer av_packet_pack_dictionary(AVDictionary dict, IntBuffer size);
public static native @Cast("uint8_t*") byte[] av_packet_pack_dictionary(AVDictionary dict, int[] size);
/**
 * Unpack a dictionary from side_data.
 *
 * @param data data from side_data
 * @param size size of the data
 * @param dict the metadata storage dictionary
 * @return 0 on success, < 0 on failure
 */
public static native int av_packet_unpack_dictionary(@Cast("const uint8_t*") BytePointer data, int size, @Cast("AVDictionary**") PointerPointer dict);
public static native int av_packet_unpack_dictionary(@Cast("const uint8_t*") BytePointer data, int size, @ByPtrPtr AVDictionary dict);
public static native int av_packet_unpack_dictionary(@Cast("const uint8_t*") ByteBuffer data, int size, @ByPtrPtr AVDictionary dict);
public static native int av_packet_unpack_dictionary(@Cast("const uint8_t*") byte[] data, int size, @ByPtrPtr AVDictionary dict);


/**
 * Convenience function to free all the side data stored.
 * All the other fields stay untouched.
 *
 * @param pkt packet
 */
public static native void av_packet_free_side_data(AVPacket pkt);

/**
 * Setup a new reference to the data described by a given packet
 *
 * If src is reference-counted, setup dst as a new reference to the
 * buffer in src. Otherwise allocate a new buffer in dst and copy the
 * data from src into it.
 *
 * All the other fields are copied from src.
 *
 * @see av_packet_unref
 *
 * @param dst Destination packet
 * @param src Source packet
 *
 * @return 0 on success, a negative AVERROR on error.
 */
public static native int av_packet_ref(AVPacket dst, @Const AVPacket src);

/**
 * Wipe the packet.
 *
 * Unreference the buffer referenced by the packet and reset the
 * remaining packet fields to their default values.
 *
 * @param pkt The packet to be unreferenced.
 */
public static native void av_packet_unref(AVPacket pkt);

/**
 * Move every field in src to dst and reset src.
 *
 * @see av_packet_unref
 *
 * @param src Source packet, will be reset
 * @param dst Destination packet
 */
public static native void av_packet_move_ref(AVPacket dst, AVPacket src);

/**
 * Copy only "properties" fields from src to dst.
 *
 * Properties for the purpose of this function are all the fields
 * beside those related to the packet data (buf, data, size)
 *
 * @param dst Destination packet
 * @param src Source packet
 *
 * @return 0 on success AVERROR on failure.
 *
 */
public static native int av_packet_copy_props(AVPacket dst, @Const AVPacket src);

/**
 * Convert valid timing fields (timestamps / durations) in a packet from one
 * timebase to another. Timestamps with unknown values (AV_NOPTS_VALUE) will be
 * ignored.
 *
 * @param pkt packet on which the conversion will be performed
 * @param tb_src source timebase, in which the timing fields in pkt are
 *               expressed
 * @param tb_dst destination timebase, to which the timing fields will be
 *               converted
 */
public static native void av_packet_rescale_ts(AVPacket pkt, @ByVal AVRational tb_src, @ByVal AVRational tb_dst);

/**
 * @}
 */

/**
 * @addtogroup lavc_decoding
 * @{
 */

/**
 * Find a registered decoder with a matching codec ID.
 *
 * @param id AVCodecID of the requested decoder
 * @return A decoder if one was found, NULL otherwise.
 */
public static native AVCodec avcodec_find_decoder(@Cast("AVCodecID") int id);

/**
 * Find a registered decoder with the specified name.
 *
 * @param name name of the requested decoder
 * @return A decoder if one was found, NULL otherwise.
 */
public static native AVCodec avcodec_find_decoder_by_name(@Cast("const char*") BytePointer name);
public static native AVCodec avcodec_find_decoder_by_name(String name);

/**
 * The default callback for AVCodecContext.get_buffer2(). It is made public so
 * it can be called by custom get_buffer2() implementations for decoders without
 * AV_CODEC_CAP_DR1 set.
 */
public static native int avcodec_default_get_buffer2(AVCodecContext s, AVFrame frame, int flags);

// #if FF_API_EMU_EDGE
/**
 * Return the amount of padding in pixels which the get_buffer callback must
 * provide around the edge of the image for codecs which do not have the
 * CODEC_FLAG_EMU_EDGE flag.
 *
 * @return Required padding in pixels.
 *
 * @deprecated CODEC_FLAG_EMU_EDGE is deprecated, so this function is no longer
 * needed
 */
public static native @Cast("unsigned") @Deprecated int avcodec_get_edge_width();
// #endif

/**
 * Modify width and height values so that they will result in a memory
 * buffer that is acceptable for the codec if you do not use any horizontal
 * padding.
 *
 * May only be used if a codec with AV_CODEC_CAP_DR1 has been opened.
 */
public static native void avcodec_align_dimensions(AVCodecContext s, IntPointer width, IntPointer height);
public static native void avcodec_align_dimensions(AVCodecContext s, IntBuffer width, IntBuffer height);
public static native void avcodec_align_dimensions(AVCodecContext s, int[] width, int[] height);

/**
 * Modify width and height values so that they will result in a memory
 * buffer that is acceptable for the codec if you also ensure that all
 * line sizes are a multiple of the respective linesize_align[i].
 *
 * May only be used if a codec with AV_CODEC_CAP_DR1 has been opened.
 */
public static native void avcodec_align_dimensions2(AVCodecContext s, IntPointer width, IntPointer height,
                               IntPointer linesize_align);
public static native void avcodec_align_dimensions2(AVCodecContext s, IntBuffer width, IntBuffer height,
                               IntBuffer linesize_align);
public static native void avcodec_align_dimensions2(AVCodecContext s, int[] width, int[] height,
                               int[] linesize_align);

/**
 * Converts AVChromaLocation to swscale x/y chroma position.
 *
 * The positions represent the chroma (0,0) position in a coordinates system
 * with luma (0,0) representing the origin and luma(1,1) representing 256,256
 *
 * @param xpos  horizontal chroma sample position
 * @param ypos  vertical   chroma sample position
 */
public static native int avcodec_enum_to_chroma_pos(IntPointer xpos, IntPointer ypos, @Cast("AVChromaLocation") int pos);
public static native int avcodec_enum_to_chroma_pos(IntBuffer xpos, IntBuffer ypos, @Cast("AVChromaLocation") int pos);
public static native int avcodec_enum_to_chroma_pos(int[] xpos, int[] ypos, @Cast("AVChromaLocation") int pos);

/**
 * Converts swscale x/y chroma position to AVChromaLocation.
 *
 * The positions represent the chroma (0,0) position in a coordinates system
 * with luma (0,0) representing the origin and luma(1,1) representing 256,256
 *
 * @param xpos  horizontal chroma sample position
 * @param ypos  vertical   chroma sample position
 */
public static native @Cast("AVChromaLocation") int avcodec_chroma_pos_to_enum(int xpos, int ypos);

/**
 * Decode the audio frame of size avpkt->size from avpkt->data into frame.
 *
 * Some decoders may support multiple frames in a single AVPacket. Such
 * decoders would then just decode the first frame and the return value would be
 * less than the packet size. In this case, avcodec_decode_audio4 has to be
 * called again with an AVPacket containing the remaining data in order to
 * decode the second frame, etc...  Even if no frames are returned, the packet
 * needs to be fed to the decoder with remaining data until it is completely
 * consumed or an error occurs.
 *
 * Some decoders (those marked with AV_CODEC_CAP_DELAY) have a delay between input
 * and output. This means that for some packets they will not immediately
 * produce decoded output and need to be flushed at the end of decoding to get
 * all the decoded data. Flushing is done by calling this function with packets
 * with avpkt->data set to NULL and avpkt->size set to 0 until it stops
 * returning samples. It is safe to flush even those decoders that are not
 * marked with AV_CODEC_CAP_DELAY, then no samples will be returned.
 *
 * @warning The input buffer, avpkt->data must be AV_INPUT_BUFFER_PADDING_SIZE
 *          larger than the actual read bytes because some optimized bitstream
 *          readers read 32 or 64 bits at once and could read over the end.
 *
 * @note The AVCodecContext MUST have been opened with @ref avcodec_open2()
 * before packets may be fed to the decoder.
 *
 * @param      avctx the codec context
 * @param[out] frame The AVFrame in which to store decoded audio samples.
 *                   The decoder will allocate a buffer for the decoded frame by
 *                   calling the AVCodecContext.get_buffer2() callback.
 *                   When AVCodecContext.refcounted_frames is set to 1, the frame is
 *                   reference counted and the returned reference belongs to the
 *                   caller. The caller must release the frame using av_frame_unref()
 *                   when the frame is no longer needed. The caller may safely write
 *                   to the frame if av_frame_is_writable() returns 1.
 *                   When AVCodecContext.refcounted_frames is set to 0, the returned
 *                   reference belongs to the decoder and is valid only until the
 *                   next call to this function or until closing or flushing the
 *                   decoder. The caller may not write to it.
 * @param[out] got_frame_ptr Zero if no frame could be decoded, otherwise it is
 *                           non-zero. Note that this field being set to zero
 *                           does not mean that an error has occurred. For
 *                           decoders with AV_CODEC_CAP_DELAY set, no given decode
 *                           call is guaranteed to produce a frame.
 * @param[in]  avpkt The input AVPacket containing the input buffer.
 *                   At least avpkt->data and avpkt->size should be set. Some
 *                   decoders might also require additional fields to be set.
 * @return A negative error code is returned if an error occurred during
 *         decoding, otherwise the number of bytes consumed from the input
 *         AVPacket is returned.
 */
public static native int avcodec_decode_audio4(AVCodecContext avctx, AVFrame frame,
                          IntPointer got_frame_ptr, @Const AVPacket avpkt);
public static native int avcodec_decode_audio4(AVCodecContext avctx, AVFrame frame,
                          IntBuffer got_frame_ptr, @Const AVPacket avpkt);
public static native int avcodec_decode_audio4(AVCodecContext avctx, AVFrame frame,
                          int[] got_frame_ptr, @Const AVPacket avpkt);

/**
 * Decode the video frame of size avpkt->size from avpkt->data into picture.
 * Some decoders may support multiple frames in a single AVPacket, such
 * decoders would then just decode the first frame.
 *
 * @warning The input buffer must be AV_INPUT_BUFFER_PADDING_SIZE larger than
 * the actual read bytes because some optimized bitstream readers read 32 or 64
 * bits at once and could read over the end.
 *
 * @warning The end of the input buffer buf should be set to 0 to ensure that
 * no overreading happens for damaged MPEG streams.
 *
 * @note Codecs which have the AV_CODEC_CAP_DELAY capability set have a delay
 * between input and output, these need to be fed with avpkt->data=NULL,
 * avpkt->size=0 at the end to return the remaining frames.
 *
 * @note The AVCodecContext MUST have been opened with @ref avcodec_open2()
 * before packets may be fed to the decoder.
 *
 * @param avctx the codec context
 * @param[out] picture The AVFrame in which the decoded video frame will be stored.
 *             Use av_frame_alloc() to get an AVFrame. The codec will
 *             allocate memory for the actual bitmap by calling the
 *             AVCodecContext.get_buffer2() callback.
 *             When AVCodecContext.refcounted_frames is set to 1, the frame is
 *             reference counted and the returned reference belongs to the
 *             caller. The caller must release the frame using av_frame_unref()
 *             when the frame is no longer needed. The caller may safely write
 *             to the frame if av_frame_is_writable() returns 1.
 *             When AVCodecContext.refcounted_frames is set to 0, the returned
 *             reference belongs to the decoder and is valid only until the
 *             next call to this function or until closing or flushing the
 *             decoder. The caller may not write to it.
 *
 * @param[in] avpkt The input AVPacket containing the input buffer.
 *            You can create such packet with av_init_packet() and by then setting
 *            data and size, some decoders might in addition need other fields like
 *            flags&AV_PKT_FLAG_KEY. All decoders are designed to use the least
 *            fields possible.
 * @param[in,out] got_picture_ptr Zero if no frame could be decompressed, otherwise, it is nonzero.
 * @return On error a negative value is returned, otherwise the number of bytes
 * used or zero if no frame could be decompressed.
 */
public static native int avcodec_decode_video2(AVCodecContext avctx, AVFrame picture,
                         IntPointer got_picture_ptr,
                         @Const AVPacket avpkt);
public static native int avcodec_decode_video2(AVCodecContext avctx, AVFrame picture,
                         IntBuffer got_picture_ptr,
                         @Const AVPacket avpkt);
public static native int avcodec_decode_video2(AVCodecContext avctx, AVFrame picture,
                         int[] got_picture_ptr,
                         @Const AVPacket avpkt);

/**
 * Decode a subtitle message.
 * Return a negative value on error, otherwise return the number of bytes used.
 * If no subtitle could be decompressed, got_sub_ptr is zero.
 * Otherwise, the subtitle is stored in *sub.
 * Note that AV_CODEC_CAP_DR1 is not available for subtitle codecs. This is for
 * simplicity, because the performance difference is expect to be negligible
 * and reusing a get_buffer written for video codecs would probably perform badly
 * due to a potentially very different allocation pattern.
 *
 * Some decoders (those marked with CODEC_CAP_DELAY) have a delay between input
 * and output. This means that for some packets they will not immediately
 * produce decoded output and need to be flushed at the end of decoding to get
 * all the decoded data. Flushing is done by calling this function with packets
 * with avpkt->data set to NULL and avpkt->size set to 0 until it stops
 * returning subtitles. It is safe to flush even those decoders that are not
 * marked with CODEC_CAP_DELAY, then no subtitles will be returned.
 *
 * @note The AVCodecContext MUST have been opened with @ref avcodec_open2()
 * before packets may be fed to the decoder.
 *
 * @param avctx the codec context
 * @param[out] sub The Preallocated AVSubtitle in which the decoded subtitle will be stored,
 *                 must be freed with avsubtitle_free if *got_sub_ptr is set.
 * @param[in,out] got_sub_ptr Zero if no subtitle could be decompressed, otherwise, it is nonzero.
 * @param[in] avpkt The input AVPacket containing the input buffer.
 */
public static native int avcodec_decode_subtitle2(AVCodecContext avctx, AVSubtitle sub,
                            IntPointer got_sub_ptr,
                            AVPacket avpkt);
public static native int avcodec_decode_subtitle2(AVCodecContext avctx, AVSubtitle sub,
                            IntBuffer got_sub_ptr,
                            AVPacket avpkt);
public static native int avcodec_decode_subtitle2(AVCodecContext avctx, AVSubtitle sub,
                            int[] got_sub_ptr,
                            AVPacket avpkt);

/**
 * @defgroup lavc_parsing Frame parsing
 * @{
 */

/** enum AVPictureStructure */
public static final int
    AV_PICTURE_STRUCTURE_UNKNOWN = 0,      //< unknown
    AV_PICTURE_STRUCTURE_TOP_FIELD = 1,    //< coded as top field
    AV_PICTURE_STRUCTURE_BOTTOM_FIELD = 2, //< coded as bottom field
    AV_PICTURE_STRUCTURE_FRAME = 3;        //< coded as frame

public static class AVCodecParserContext extends Pointer {
    static { Loader.load(); }
    /** Default native constructor. */
    public AVCodecParserContext() { allocate(); }
    /** Native array allocator. Access with {@link Pointer#position(int)}. */
    public AVCodecParserContext(int size) { allocateArray(size); }
    /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
    public AVCodecParserContext(Pointer p) { super(p); }
    private native void allocate();
    private native void allocateArray(int size);
    @Override public AVCodecParserContext position(int position) {
        return (AVCodecParserContext)super.position(position);
    }

    public native Pointer priv_data(); public native AVCodecParserContext priv_data(Pointer priv_data);
    public native AVCodecParser parser(); public native AVCodecParserContext parser(AVCodecParser parser);
    public native long frame_offset(); public native AVCodecParserContext frame_offset(long frame_offset); /* offset of the current frame */
    public native long cur_offset(); public native AVCodecParserContext cur_offset(long cur_offset); /* current offset
                           (incremented by each av_parser_parse()) */
    public native long next_frame_offset(); public native AVCodecParserContext next_frame_offset(long next_frame_offset); /* offset of the next frame */
    /* video info */
    public native int pict_type(); public native AVCodecParserContext pict_type(int pict_type); /* XXX: Put it back in AVCodecContext. */
    /**
     * This field is used for proper frame duration computation in lavf.
     * It signals, how much longer the frame duration of the current frame
     * is compared to normal frame duration.
     *
     * frame_duration = (1 + repeat_pict) * time_base
     *
     * It is used by codecs like H.264 to display telecined material.
     */
    public native int repeat_pict(); public native AVCodecParserContext repeat_pict(int repeat_pict); /* XXX: Put it back in AVCodecContext. */
    public native long pts(); public native AVCodecParserContext pts(long pts);     /* pts of the current frame */
    public native long dts(); public native AVCodecParserContext dts(long dts);     /* dts of the current frame */

    /* private data */
    public native long last_pts(); public native AVCodecParserContext last_pts(long last_pts);
    public native long last_dts(); public native AVCodecParserContext last_dts(long last_dts);
    public native int fetch_timestamp(); public native AVCodecParserContext fetch_timestamp(int fetch_timestamp);

public static final int AV_PARSER_PTS_NB = 4;
    public native int cur_frame_start_index(); public native AVCodecParserContext cur_frame_start_index(int cur_frame_start_index);
    public native long cur_frame_offset(int i); public native AVCodecParserContext cur_frame_offset(int i, long cur_frame_offset);
    @MemberGetter public native LongPointer cur_frame_offset();
    public native long cur_frame_pts(int i); public native AVCodecParserContext cur_frame_pts(int i, long cur_frame_pts);
    @MemberGetter public native LongPointer cur_frame_pts();
    public native long cur_frame_dts(int i); public native AVCodecParserContext cur_frame_dts(int i, long cur_frame_dts);
    @MemberGetter public native LongPointer cur_frame_dts();

    public native int flags(); public native AVCodecParserContext flags(int flags);
public static final int PARSER_FLAG_COMPLETE_FRAMES =           0x0001;
public static final int PARSER_FLAG_ONCE =                      0x0002;
/** Set if the parser has a valid file offset */
public static final int PARSER_FLAG_FETCHED_OFFSET =            0x0004;
public static final int PARSER_FLAG_USE_CODEC_TS =              0x1000;

    /** byte offset from starting packet start */
    public native long offset(); public native AVCodecParserContext offset(long offset);
    public native long cur_frame_end(int i); public native AVCodecParserContext cur_frame_end(int i, long cur_frame_end);
    @MemberGetter public native LongPointer cur_frame_end();

    /**
     * Set by parser to 1 for key frames and 0 for non-key frames.
     * It is initialized to -1, so if the parser doesn't set this flag,
     * old-style fallback using AV_PICTURE_TYPE_I picture type as key frames
     * will be used.
     */
    public native int key_frame(); public native AVCodecParserContext key_frame(int key_frame);

// #if FF_API_CONVERGENCE_DURATION
    /**
     * @deprecated unused
     */
    public native @Deprecated long convergence_duration(); public native AVCodecParserContext convergence_duration(long convergence_duration);
// #endif

    // Timestamp generation support:
    /**
     * Synchronization point for start of timestamp generation.
     *
     * Set to >0 for sync point, 0 for no sync point and <0 for undefined
     * (default).
     *
     * For example, this corresponds to presence of H.264 buffering period
     * SEI message.
     */
    public native int dts_sync_point(); public native AVCodecParserContext dts_sync_point(int dts_sync_point);

    /**
     * Offset of the current timestamp against last timestamp sync point in
     * units of AVCodecContext.time_base.
     *
     * Set to INT_MIN when dts_sync_point unused. Otherwise, it must
     * contain a valid timestamp offset.
     *
     * Note that the timestamp of sync point has usually a nonzero
     * dts_ref_dts_delta, which refers to the previous sync point. Offset of
     * the next frame after timestamp sync point will be usually 1.
     *
     * For example, this corresponds to H.264 cpb_removal_delay.
     */
    public native int dts_ref_dts_delta(); public native AVCodecParserContext dts_ref_dts_delta(int dts_ref_dts_delta);

    /**
     * Presentation delay of current frame in units of AVCodecContext.time_base.
     *
     * Set to INT_MIN when dts_sync_point unused. Otherwise, it must
     * contain valid non-negative timestamp delta (presentation time of a frame
     * must not lie in the past).
     *
     * This delay represents the difference between decoding and presentation
     * time of the frame.
     *
     * For example, this corresponds to H.264 dpb_output_delay.
     */
    public native int pts_dts_delta(); public native AVCodecParserContext pts_dts_delta(int pts_dts_delta);

    /**
     * Position of the packet in file.
     *
     * Analogous to cur_frame_pts/dts
     */
    public native long cur_frame_pos(int i); public native AVCodecParserContext cur_frame_pos(int i, long cur_frame_pos);
    @MemberGetter public native LongPointer cur_frame_pos();

    /**
     * Byte position of currently parsed frame in stream.
     */
    public native long pos(); public native AVCodecParserContext pos(long pos);

    /**
     * Previous frame byte position.
     */
    public native long last_pos(); public native AVCodecParserContext last_pos(long last_pos);

    /**
     * Duration of the current frame.
     * For audio, this is in units of 1 / AVCodecContext.sample_rate.
     * For all other types, this is in units of AVCodecContext.time_base.
     */
    public native int duration(); public native AVCodecParserContext duration(int duration);

    public native @Cast("AVFieldOrder") int field_order(); public native AVCodecParserContext field_order(int field_order);

    /**
     * Indicate whether a picture is coded as a frame, top field or bottom field.
     *
     * For example, H.264 field_pic_flag equal to 0 corresponds to
     * AV_PICTURE_STRUCTURE_FRAME. An H.264 picture with field_pic_flag
     * equal to 1 and bottom_field_flag equal to 0 corresponds to
     * AV_PICTURE_STRUCTURE_TOP_FIELD.
     */
    public native @Cast("AVPictureStructure") int picture_structure(); public native AVCodecParserContext picture_structure(int picture_structure);

    /**
     * Picture number incremented in presentation or output order.
     * This field may be reinitialized at the first picture of a new sequence.
     *
     * For example, this corresponds to H.264 PicOrderCnt.
     */
    public native int output_picture_number(); public native AVCodecParserContext output_picture_number(int output_picture_number);

    /**
     * Dimensions of the decoded video intended for presentation.
     */
    public native int width(); public native AVCodecParserContext width(int width);
    public native int height(); public native AVCodecParserContext height(int height);

    /**
     * Dimensions of the coded video.
     */
    public native int coded_width(); public native AVCodecParserContext coded_width(int coded_width);
    public native int coded_height(); public native AVCodecParserContext coded_height(int coded_height);

    /**
     * The format of the coded data, corresponds to enum AVPixelFormat for video
     * and for enum AVSampleFormat for audio.
     *
     * Note that a decoder can have considerable freedom in how exactly it
     * decodes the data, so the format reported here might be different from the
     * one returned by a decoder.
     */
    public native int format(); public native AVCodecParserContext format(int format);
}

public static class AVCodecParser extends Pointer {
    static { Loader.load(); }
    /** Default native constructor. */
    public AVCodecParser() { allocate(); }
    /** Native array allocator. Access with {@link Pointer#position(int)}. */
    public AVCodecParser(int size) { allocateArray(size); }
    /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
    public AVCodecParser(Pointer p) { super(p); }
    private native void allocate();
    private native void allocateArray(int size);
    @Override public AVCodecParser position(int position) {
        return (AVCodecParser)super.position(position);
    }

    public native int codec_ids(int i); public native AVCodecParser codec_ids(int i, int codec_ids);
    @MemberGetter public native IntPointer codec_ids(); /* several codec IDs are permitted */
    public native int priv_data_size(); public native AVCodecParser priv_data_size(int priv_data_size);
    public static class Parser_init_AVCodecParserContext extends FunctionPointer {
        static { Loader.load(); }
        /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
        public    Parser_init_AVCodecParserContext(Pointer p) { super(p); }
        protected Parser_init_AVCodecParserContext() { allocate(); }
        private native void allocate();
        public native int call(AVCodecParserContext s);
    }
    public native Parser_init_AVCodecParserContext parser_init(); public native AVCodecParser parser_init(Parser_init_AVCodecParserContext parser_init);
    /* This callback never returns an error, a negative value means that
     * the frame start was in a previous packet. */
    public static class Parser_parse_AVCodecParserContext_AVCodecContext_PointerPointer_IntPointer_BytePointer_int extends FunctionPointer {
        static { Loader.load(); }
        /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
        public    Parser_parse_AVCodecParserContext_AVCodecContext_PointerPointer_IntPointer_BytePointer_int(Pointer p) { super(p); }
        protected Parser_parse_AVCodecParserContext_AVCodecContext_PointerPointer_IntPointer_BytePointer_int() { allocate(); }
        private native void allocate();
        public native int call(AVCodecParserContext s,
                            AVCodecContext avctx,
                            @Cast("const uint8_t**") PointerPointer poutbuf, IntPointer poutbuf_size,
                            @Cast("const uint8_t*") BytePointer buf, int buf_size);
    }
    public native Parser_parse_AVCodecParserContext_AVCodecContext_PointerPointer_IntPointer_BytePointer_int parser_parse(); public native AVCodecParser parser_parse(Parser_parse_AVCodecParserContext_AVCodecContext_PointerPointer_IntPointer_BytePointer_int parser_parse);
    public static class Parser_close_AVCodecParserContext extends FunctionPointer {
        static { Loader.load(); }
        /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
        public    Parser_close_AVCodecParserContext(Pointer p) { super(p); }
        protected Parser_close_AVCodecParserContext() { allocate(); }
        private native void allocate();
        public native void call(AVCodecParserContext s);
    }
    public native Parser_close_AVCodecParserContext parser_close(); public native AVCodecParser parser_close(Parser_close_AVCodecParserContext parser_close);
    public static class Split_AVCodecContext_BytePointer_int extends FunctionPointer {
        static { Loader.load(); }
        /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
        public    Split_AVCodecContext_BytePointer_int(Pointer p) { super(p); }
        protected Split_AVCodecContext_BytePointer_int() { allocate(); }
        private native void allocate();
        public native int call(AVCodecContext avctx, @Cast("const uint8_t*") BytePointer buf, int buf_size);
    }
    public native Split_AVCodecContext_BytePointer_int split(); public native AVCodecParser split(Split_AVCodecContext_BytePointer_int split);
    public native AVCodecParser next(); public native AVCodecParser next(AVCodecParser next);
}

public static native AVCodecParser av_parser_next(@Const AVCodecParser c);

public static native void av_register_codec_parser(AVCodecParser parser);
public static native AVCodecParserContext av_parser_init(int codec_id);

/**
 * Parse a packet.
 *
 * @param s             parser context.
 * @param avctx         codec context.
 * @param poutbuf       set to pointer to parsed buffer or NULL if not yet finished.
 * @param poutbuf_size  set to size of parsed buffer or zero if not yet finished.
 * @param buf           input buffer.
 * @param buf_size      input length, to signal EOF, this should be 0 (so that the last frame can be output).
 * @param pts           input presentation timestamp.
 * @param dts           input decoding timestamp.
 * @param pos           input byte position in stream.
 * @return the number of bytes of the input bitstream used.
 *
 * Example:
 * @code
 *   while(in_len){
 *       len = av_parser_parse2(myparser, AVCodecContext, &data, &size,
 *                                        in_data, in_len,
 *                                        pts, dts, pos);
 *       in_data += len;
 *       in_len  -= len;
 *
 *       if(size)
 *          decode_frame(data, size);
 *   }
 * @endcode
 */
public static native int av_parser_parse2(AVCodecParserContext s,
                     AVCodecContext avctx,
                     @Cast("uint8_t**") PointerPointer poutbuf, IntPointer poutbuf_size,
                     @Cast("const uint8_t*") BytePointer buf, int buf_size,
                     long pts, long dts,
                     long pos);
public static native int av_parser_parse2(AVCodecParserContext s,
                     AVCodecContext avctx,
                     @Cast("uint8_t**") @ByPtrPtr BytePointer poutbuf, IntPointer poutbuf_size,
                     @Cast("const uint8_t*") BytePointer buf, int buf_size,
                     long pts, long dts,
                     long pos);
public static native int av_parser_parse2(AVCodecParserContext s,
                     AVCodecContext avctx,
                     @Cast("uint8_t**") @ByPtrPtr ByteBuffer poutbuf, IntBuffer poutbuf_size,
                     @Cast("const uint8_t*") ByteBuffer buf, int buf_size,
                     long pts, long dts,
                     long pos);
public static native int av_parser_parse2(AVCodecParserContext s,
                     AVCodecContext avctx,
                     @Cast("uint8_t**") @ByPtrPtr byte[] poutbuf, int[] poutbuf_size,
                     @Cast("const uint8_t*") byte[] buf, int buf_size,
                     long pts, long dts,
                     long pos);

/**
 * @return 0 if the output buffer is a subset of the input, 1 if it is allocated and must be freed
 * @deprecated use AVBitStreamFilter
 */
public static native int av_parser_change(AVCodecParserContext s,
                     AVCodecContext avctx,
                     @Cast("uint8_t**") PointerPointer poutbuf, IntPointer poutbuf_size,
                     @Cast("const uint8_t*") BytePointer buf, int buf_size, int keyframe);
public static native int av_parser_change(AVCodecParserContext s,
                     AVCodecContext avctx,
                     @Cast("uint8_t**") @ByPtrPtr BytePointer poutbuf, IntPointer poutbuf_size,
                     @Cast("const uint8_t*") BytePointer buf, int buf_size, int keyframe);
public static native int av_parser_change(AVCodecParserContext s,
                     AVCodecContext avctx,
                     @Cast("uint8_t**") @ByPtrPtr ByteBuffer poutbuf, IntBuffer poutbuf_size,
                     @Cast("const uint8_t*") ByteBuffer buf, int buf_size, int keyframe);
public static native int av_parser_change(AVCodecParserContext s,
                     AVCodecContext avctx,
                     @Cast("uint8_t**") @ByPtrPtr byte[] poutbuf, int[] poutbuf_size,
                     @Cast("const uint8_t*") byte[] buf, int buf_size, int keyframe);
public static native void av_parser_close(AVCodecParserContext s);

/**
 * @}
 * @}
 */

/**
 * @addtogroup lavc_encoding
 * @{
 */

/**
 * Find a registered encoder with a matching codec ID.
 *
 * @param id AVCodecID of the requested encoder
 * @return An encoder if one was found, NULL otherwise.
 */
public static native AVCodec avcodec_find_encoder(@Cast("AVCodecID") int id);

/**
 * Find a registered encoder with the specified name.
 *
 * @param name name of the requested encoder
 * @return An encoder if one was found, NULL otherwise.
 */
public static native AVCodec avcodec_find_encoder_by_name(@Cast("const char*") BytePointer name);
public static native AVCodec avcodec_find_encoder_by_name(String name);

/**
 * Encode a frame of audio.
 *
 * Takes input samples from frame and writes the next output packet, if
 * available, to avpkt. The output packet does not necessarily contain data for
 * the most recent frame, as encoders can delay, split, and combine input frames
 * internally as needed.
 *
 * @param avctx     codec context
 * @param avpkt     output AVPacket.
 *                  The user can supply an output buffer by setting
 *                  avpkt->data and avpkt->size prior to calling the
 *                  function, but if the size of the user-provided data is not
 *                  large enough, encoding will fail. If avpkt->data and
 *                  avpkt->size are set, avpkt->destruct must also be set. All
 *                  other AVPacket fields will be reset by the encoder using
 *                  av_init_packet(). If avpkt->data is NULL, the encoder will
 *                  allocate it. The encoder will set avpkt->size to the size
 *                  of the output packet.
 *
 *                  If this function fails or produces no output, avpkt will be
 *                  freed using av_packet_unref().
 * @param[in] frame AVFrame containing the raw audio data to be encoded.
 *                  May be NULL when flushing an encoder that has the
 *                  AV_CODEC_CAP_DELAY capability set.
 *                  If AV_CODEC_CAP_VARIABLE_FRAME_SIZE is set, then each frame
 *                  can have any number of samples.
 *                  If it is not set, frame->nb_samples must be equal to
 *                  avctx->frame_size for all frames except the last.
 *                  The final frame may be smaller than avctx->frame_size.
 * @param[out] got_packet_ptr This field is set to 1 by libavcodec if the
 *                            output packet is non-empty, and to 0 if it is
 *                            empty. If the function returns an error, the
 *                            packet can be assumed to be invalid, and the
 *                            value of got_packet_ptr is undefined and should
 *                            not be used.
 * @return          0 on success, negative error code on failure
 */
public static native int avcodec_encode_audio2(AVCodecContext avctx, AVPacket avpkt,
                          @Const AVFrame frame, IntPointer got_packet_ptr);
public static native int avcodec_encode_audio2(AVCodecContext avctx, AVPacket avpkt,
                          @Const AVFrame frame, IntBuffer got_packet_ptr);
public static native int avcodec_encode_audio2(AVCodecContext avctx, AVPacket avpkt,
                          @Const AVFrame frame, int[] got_packet_ptr);

/**
 * Encode a frame of video.
 *
 * Takes input raw video data from frame and writes the next output packet, if
 * available, to avpkt. The output packet does not necessarily contain data for
 * the most recent frame, as encoders can delay and reorder input frames
 * internally as needed.
 *
 * @param avctx     codec context
 * @param avpkt     output AVPacket.
 *                  The user can supply an output buffer by setting
 *                  avpkt->data and avpkt->size prior to calling the
 *                  function, but if the size of the user-provided data is not
 *                  large enough, encoding will fail. All other AVPacket fields
 *                  will be reset by the encoder using av_init_packet(). If
 *                  avpkt->data is NULL, the encoder will allocate it.
 *                  The encoder will set avpkt->size to the size of the
 *                  output packet. The returned data (if any) belongs to the
 *                  caller, he is responsible for freeing it.
 *
 *                  If this function fails or produces no output, avpkt will be
 *                  freed using av_packet_unref().
 * @param[in] frame AVFrame containing the raw video data to be encoded.
 *                  May be NULL when flushing an encoder that has the
 *                  AV_CODEC_CAP_DELAY capability set.
 * @param[out] got_packet_ptr This field is set to 1 by libavcodec if the
 *                            output packet is non-empty, and to 0 if it is
 *                            empty. If the function returns an error, the
 *                            packet can be assumed to be invalid, and the
 *                            value of got_packet_ptr is undefined and should
 *                            not be used.
 * @return          0 on success, negative error code on failure
 */
public static native int avcodec_encode_video2(AVCodecContext avctx, AVPacket avpkt,
                          @Const AVFrame frame, IntPointer got_packet_ptr);
public static native int avcodec_encode_video2(AVCodecContext avctx, AVPacket avpkt,
                          @Const AVFrame frame, IntBuffer got_packet_ptr);
public static native int avcodec_encode_video2(AVCodecContext avctx, AVPacket avpkt,
                          @Const AVFrame frame, int[] got_packet_ptr);

public static native int avcodec_encode_subtitle(AVCodecContext avctx, @Cast("uint8_t*") BytePointer buf, int buf_size,
                            @Const AVSubtitle sub);
public static native int avcodec_encode_subtitle(AVCodecContext avctx, @Cast("uint8_t*") ByteBuffer buf, int buf_size,
                            @Const AVSubtitle sub);
public static native int avcodec_encode_subtitle(AVCodecContext avctx, @Cast("uint8_t*") byte[] buf, int buf_size,
                            @Const AVSubtitle sub);


/**
 * @}
 */

// #if FF_API_AVCODEC_RESAMPLE
/**
 * @defgroup lavc_resample Audio resampling
 * @ingroup libavc
 * @deprecated use libswresample instead
 *
 * @{
 */
@Opaque public static class ReSampleContext extends Pointer {
    /** Empty constructor. */
    public ReSampleContext() { }
    /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
    public ReSampleContext(Pointer p) { super(p); }
}
@Opaque public static class AVResampleContext extends Pointer {
    /** Empty constructor. */
    public AVResampleContext() { }
    /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
    public AVResampleContext(Pointer p) { super(p); }
}

/**
 *  Initialize audio resampling context.
 *
 * @param output_channels  number of output channels
 * @param input_channels   number of input channels
 * @param output_rate      output sample rate
 * @param input_rate       input sample rate
 * @param sample_fmt_out   requested output sample format
 * @param sample_fmt_in    input sample format
 * @param filter_length    length of each FIR filter in the filterbank relative to the cutoff frequency
 * @param log2_phase_count log2 of the number of entries in the polyphase filterbank
 * @param linear           if 1 then the used FIR filter will be linearly interpolated
                           between the 2 closest, if 0 the closest will be used
 * @param cutoff           cutoff frequency, 1.0 corresponds to half the output sampling rate
 * @return allocated ReSampleContext, NULL if error occurred
 */
public static native @Deprecated ReSampleContext av_audio_resample_init(int output_channels, int input_channels,
                                        int output_rate, int input_rate,
                                        @Cast("AVSampleFormat") int sample_fmt_out,
                                        @Cast("AVSampleFormat") int sample_fmt_in,
                                        int filter_length, int log2_phase_count,
                                        int linear, double cutoff);

public static native @Deprecated int audio_resample(ReSampleContext s, ShortPointer output, ShortPointer input, int nb_samples);
public static native @Deprecated int audio_resample(ReSampleContext s, ShortBuffer output, ShortBuffer input, int nb_samples);
public static native @Deprecated int audio_resample(ReSampleContext s, short[] output, short[] input, int nb_samples);

/**
 * Free resample context.
 *
 * @param s a non-NULL pointer to a resample context previously
 *          created with av_audio_resample_init()
 */
public static native @Deprecated void audio_resample_close(ReSampleContext s);


/**
 * Initialize an audio resampler.
 * Note, if either rate is not an integer then simply scale both rates up so they are.
 * @param filter_length length of each FIR filter in the filterbank relative to the cutoff freq
 * @param log2_phase_count log2 of the number of entries in the polyphase filterbank
 * @param linear If 1 then the used FIR filter will be linearly interpolated
                 between the 2 closest, if 0 the closest will be used
 * @param cutoff cutoff frequency, 1.0 corresponds to half the output sampling rate
 */
public static native @Deprecated AVResampleContext av_resample_init(int out_rate, int in_rate, int filter_length, int log2_phase_count, int linear, double cutoff);

/**
 * Resample an array of samples using a previously configured context.
 * @param src an array of unconsumed samples
 * @param consumed the number of samples of src which have been consumed are returned here
 * @param src_size the number of unconsumed samples available
 * @param dst_size the amount of space in samples available in dst
 * @param update_ctx If this is 0 then the context will not be modified, that way several channels can be resampled with the same context.
 * @return the number of samples written in dst or -1 if an error occurred
 */
public static native @Deprecated int av_resample(AVResampleContext c, ShortPointer dst, ShortPointer src, IntPointer consumed, int src_size, int dst_size, int update_ctx);
public static native @Deprecated int av_resample(AVResampleContext c, ShortBuffer dst, ShortBuffer src, IntBuffer consumed, int src_size, int dst_size, int update_ctx);
public static native @Deprecated int av_resample(AVResampleContext c, short[] dst, short[] src, int[] consumed, int src_size, int dst_size, int update_ctx);


/**
 * Compensate samplerate/timestamp drift. The compensation is done by changing
 * the resampler parameters, so no audible clicks or similar distortions occur
 * @param compensation_distance distance in output samples over which the compensation should be performed
 * @param sample_delta number of output samples which should be output less
 *
 * example: av_resample_compensate(c, 10, 500)
 * here instead of 510 samples only 500 samples would be output
 *
 * note, due to rounding the actual compensation might be slightly different,
 * especially if the compensation_distance is large and the in_rate used during init is small
 */
public static native @Deprecated void av_resample_compensate(AVResampleContext c, int sample_delta, int compensation_distance);
public static native @Deprecated void av_resample_close(AVResampleContext c);

/**
 * @}
 */
// #endif

// #if FF_API_AVPICTURE
/**
 * @addtogroup lavc_picture
 * @{
 */

/**
 * @deprecated unused
 */
public static native @Deprecated int avpicture_alloc(AVPicture picture, @Cast("AVPixelFormat") int pix_fmt, int width, int height);

/**
 * @deprecated unused
 */
public static native @Deprecated void avpicture_free(AVPicture picture);

/**
 * @deprecated use av_image_fill_arrays() instead.
 */
public static native @Deprecated int avpicture_fill(AVPicture picture, @Cast("const uint8_t*") BytePointer ptr,
                   @Cast("AVPixelFormat") int pix_fmt, int width, int height);
public static native @Deprecated int avpicture_fill(AVPicture picture, @Cast("const uint8_t*") ByteBuffer ptr,
                   @Cast("AVPixelFormat") int pix_fmt, int width, int height);
public static native @Deprecated int avpicture_fill(AVPicture picture, @Cast("const uint8_t*") byte[] ptr,
                   @Cast("AVPixelFormat") int pix_fmt, int width, int height);

/**
 * @deprecated use av_image_copy_to_buffer() instead.
 */
public static native @Deprecated int avpicture_layout(@Const AVPicture src, @Cast("AVPixelFormat") int pix_fmt,
                     int width, int height,
                     @Cast("unsigned char*") BytePointer dest, int dest_size);
public static native @Deprecated int avpicture_layout(@Const AVPicture src, @Cast("AVPixelFormat") int pix_fmt,
                     int width, int height,
                     @Cast("unsigned char*") ByteBuffer dest, int dest_size);
public static native @Deprecated int avpicture_layout(@Const AVPicture src, @Cast("AVPixelFormat") int pix_fmt,
                     int width, int height,
                     @Cast("unsigned char*") byte[] dest, int dest_size);

/**
 * @deprecated use av_image_get_buffer_size() instead.
 */
public static native @Deprecated int avpicture_get_size(@Cast("AVPixelFormat") int pix_fmt, int width, int height);

/**
 * @deprecated av_image_copy() instead.
 */
public static native @Deprecated void av_picture_copy(AVPicture dst, @Const AVPicture src,
                     @Cast("AVPixelFormat") int pix_fmt, int width, int height);

/**
 * @deprecated unused
 */
public static native @Deprecated int av_picture_crop(AVPicture dst, @Const AVPicture src,
                    @Cast("AVPixelFormat") int pix_fmt, int top_band, int left_band);

/**
 * @deprecated unused
 */
public static native @Deprecated int av_picture_pad(AVPicture dst, @Const AVPicture src, int height, int width, @Cast("AVPixelFormat") int pix_fmt,
            int padtop, int padbottom, int padleft, int padright, IntPointer color);
public static native @Deprecated int av_picture_pad(AVPicture dst, @Const AVPicture src, int height, int width, @Cast("AVPixelFormat") int pix_fmt,
            int padtop, int padbottom, int padleft, int padright, IntBuffer color);
public static native @Deprecated int av_picture_pad(AVPicture dst, @Const AVPicture src, int height, int width, @Cast("AVPixelFormat") int pix_fmt,
            int padtop, int padbottom, int padleft, int padright, int[] color);

/**
 * @}
 */
// #endif

/**
 * @defgroup lavc_misc Utility functions
 * @ingroup libavc
 *
 * Miscellaneous utility functions related to both encoding and decoding
 * (or neither).
 * @{
 */

/**
 * @defgroup lavc_misc_pixfmt Pixel formats
 *
 * Functions for working with pixel formats.
 * @{
 */

/**
 * Utility function to access log2_chroma_w log2_chroma_h from
 * the pixel format AVPixFmtDescriptor.
 *
 * This function asserts that pix_fmt is valid. See av_pix_fmt_get_chroma_sub_sample
 * for one that returns a failure code and continues in case of invalid
 * pix_fmts.
 *
 * @param[in]  pix_fmt the pixel format
 * @param[out] h_shift store log2_chroma_w
 * @param[out] v_shift store log2_chroma_h
 *
 * @see av_pix_fmt_get_chroma_sub_sample
 */

public static native void avcodec_get_chroma_sub_sample(@Cast("AVPixelFormat") int pix_fmt, IntPointer h_shift, IntPointer v_shift);
public static native void avcodec_get_chroma_sub_sample(@Cast("AVPixelFormat") int pix_fmt, IntBuffer h_shift, IntBuffer v_shift);
public static native void avcodec_get_chroma_sub_sample(@Cast("AVPixelFormat") int pix_fmt, int[] h_shift, int[] v_shift);

/**
 * Return a value representing the fourCC code associated to the
 * pixel format pix_fmt, or 0 if no associated fourCC code can be
 * found.
 */
public static native @Cast("unsigned int") int avcodec_pix_fmt_to_codec_tag(@Cast("AVPixelFormat") int pix_fmt);

/**
 * @deprecated see av_get_pix_fmt_loss()
 */
public static native int avcodec_get_pix_fmt_loss(@Cast("AVPixelFormat") int dst_pix_fmt, @Cast("AVPixelFormat") int src_pix_fmt,
                             int has_alpha);

/**
 * Find the best pixel format to convert to given a certain source pixel
 * format.  When converting from one pixel format to another, information loss
 * may occur.  For example, when converting from RGB24 to GRAY, the color
 * information will be lost. Similarly, other losses occur when converting from
 * some formats to other formats. avcodec_find_best_pix_fmt_of_2() searches which of
 * the given pixel formats should be used to suffer the least amount of loss.
 * The pixel formats from which it chooses one, are determined by the
 * pix_fmt_list parameter.
 *
 *
 * @param[in] pix_fmt_list AV_PIX_FMT_NONE terminated array of pixel formats to choose from
 * @param[in] src_pix_fmt source pixel format
 * @param[in] has_alpha Whether the source pixel format alpha channel is used.
 * @param[out] loss_ptr Combination of flags informing you what kind of losses will occur.
 * @return The best pixel format to convert to or -1 if none was found.
 */
public static native @Cast("AVPixelFormat") int avcodec_find_best_pix_fmt_of_list(@Cast("const AVPixelFormat*") IntPointer pix_fmt_list,
                                            @Cast("AVPixelFormat") int src_pix_fmt,
                                            int has_alpha, IntPointer loss_ptr);
public static native @Cast("AVPixelFormat") int avcodec_find_best_pix_fmt_of_list(@Cast("const AVPixelFormat*") IntBuffer pix_fmt_list,
                                            @Cast("AVPixelFormat") int src_pix_fmt,
                                            int has_alpha, IntBuffer loss_ptr);
public static native @Cast("AVPixelFormat") int avcodec_find_best_pix_fmt_of_list(@Cast("const AVPixelFormat*") int[] pix_fmt_list,
                                            @Cast("AVPixelFormat") int src_pix_fmt,
                                            int has_alpha, int[] loss_ptr);

/**
 * @deprecated see av_find_best_pix_fmt_of_2()
 */
public static native @Cast("AVPixelFormat") int avcodec_find_best_pix_fmt_of_2(@Cast("AVPixelFormat") int dst_pix_fmt1, @Cast("AVPixelFormat") int dst_pix_fmt2,
                                            @Cast("AVPixelFormat") int src_pix_fmt, int has_alpha, IntPointer loss_ptr);
public static native @Cast("AVPixelFormat") int avcodec_find_best_pix_fmt_of_2(@Cast("AVPixelFormat") int dst_pix_fmt1, @Cast("AVPixelFormat") int dst_pix_fmt2,
                                            @Cast("AVPixelFormat") int src_pix_fmt, int has_alpha, IntBuffer loss_ptr);
public static native @Cast("AVPixelFormat") int avcodec_find_best_pix_fmt_of_2(@Cast("AVPixelFormat") int dst_pix_fmt1, @Cast("AVPixelFormat") int dst_pix_fmt2,
                                            @Cast("AVPixelFormat") int src_pix_fmt, int has_alpha, int[] loss_ptr);

public static native @Cast("AVPixelFormat") @Deprecated int avcodec_find_best_pix_fmt2(@Cast("AVPixelFormat") int dst_pix_fmt1, @Cast("AVPixelFormat") int dst_pix_fmt2,
                                            @Cast("AVPixelFormat") int src_pix_fmt, int has_alpha, IntPointer loss_ptr);
public static native @Cast("AVPixelFormat") @Deprecated int avcodec_find_best_pix_fmt2(@Cast("AVPixelFormat") int dst_pix_fmt1, @Cast("AVPixelFormat") int dst_pix_fmt2,
                                            @Cast("AVPixelFormat") int src_pix_fmt, int has_alpha, IntBuffer loss_ptr);
public static native @Cast("AVPixelFormat") @Deprecated int avcodec_find_best_pix_fmt2(@Cast("AVPixelFormat") int dst_pix_fmt1, @Cast("AVPixelFormat") int dst_pix_fmt2,
                                            @Cast("AVPixelFormat") int src_pix_fmt, int has_alpha, int[] loss_ptr);
// #endif


public static native @Cast("AVPixelFormat") int avcodec_default_get_format(AVCodecContext s, @Cast("const AVPixelFormat*") IntPointer fmt);
public static native @Cast("AVPixelFormat") int avcodec_default_get_format(AVCodecContext s, @Cast("const AVPixelFormat*") IntBuffer fmt);
public static native @Cast("AVPixelFormat") int avcodec_default_get_format(AVCodecContext s, @Cast("const AVPixelFormat*") int[] fmt);

/**
 * @}
 */

// #if FF_API_SET_DIMENSIONS
/**
 * @deprecated this function is not supposed to be used from outside of lavc
 */
public static native @Deprecated void avcodec_set_dimensions(AVCodecContext s, int width, int height);
// #endif

/**
 * Put a string representing the codec tag codec_tag in buf.
 *
 * @param buf       buffer to place codec tag in
 * @param buf_size size in bytes of buf
 * @param codec_tag codec tag to assign
 * @return the length of the string that would have been generated if
 * enough space had been available, excluding the trailing null
 */
public static native @Cast("size_t") long av_get_codec_tag_string(@Cast("char*") BytePointer buf, @Cast("size_t") long buf_size, @Cast("unsigned int") int codec_tag);
public static native @Cast("size_t") long av_get_codec_tag_string(@Cast("char*") ByteBuffer buf, @Cast("size_t") long buf_size, @Cast("unsigned int") int codec_tag);
public static native @Cast("size_t") long av_get_codec_tag_string(@Cast("char*") byte[] buf, @Cast("size_t") long buf_size, @Cast("unsigned int") int codec_tag);

public static native void avcodec_string(@Cast("char*") BytePointer buf, int buf_size, AVCodecContext enc, int encode);
public static native void avcodec_string(@Cast("char*") ByteBuffer buf, int buf_size, AVCodecContext enc, int encode);
public static native void avcodec_string(@Cast("char*") byte[] buf, int buf_size, AVCodecContext enc, int encode);

/**
 * Return a name for the specified profile, if available.
 *
 * @param codec the codec that is searched for the given profile
 * @param profile the profile value for which a name is requested
 * @return A name for the profile if found, NULL otherwise.
 */
public static native @Cast("const char*") BytePointer av_get_profile_name(@Const AVCodec codec, int profile);

/**
 * Return a name for the specified profile, if available.
 *
 * @param codec_id the ID of the codec to which the requested profile belongs
 * @param profile the profile value for which a name is requested
 * @return A name for the profile if found, NULL otherwise.
 *
 * @note unlike av_get_profile_name(), which searches a list of profiles
 *       supported by a specific decoder or encoder implementation, this
 *       function searches the list of profiles from the AVCodecDescriptor
 */
public static native @Cast("const char*") BytePointer avcodec_profile_name(@Cast("AVCodecID") int codec_id, int profile);

public static class Func_AVCodecContext_Pointer extends FunctionPointer {
    static { Loader.load(); }
    /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
    public    Func_AVCodecContext_Pointer(Pointer p) { super(p); }
    protected Func_AVCodecContext_Pointer() { allocate(); }
    private native void allocate();
    public native int call(AVCodecContext c2, Pointer arg2);
}
public static native int avcodec_default_execute(AVCodecContext c, Func_AVCodecContext_Pointer func,Pointer arg, IntPointer ret, int count, int size);
public static native int avcodec_default_execute(AVCodecContext c, Func_AVCodecContext_Pointer func,Pointer arg, IntBuffer ret, int count, int size);
public static native int avcodec_default_execute(AVCodecContext c, Func_AVCodecContext_Pointer func,Pointer arg, int[] ret, int count, int size);
public static class Func_AVCodecContext_Pointer_int_int extends FunctionPointer {
    static { Loader.load(); }
    /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
    public    Func_AVCodecContext_Pointer_int_int(Pointer p) { super(p); }
    protected Func_AVCodecContext_Pointer_int_int() { allocate(); }
    private native void allocate();
    public native int call(AVCodecContext c2, Pointer arg2, int arg3, int arg4);
}
public static native int avcodec_default_execute2(AVCodecContext c, Func_AVCodecContext_Pointer_int_int func,Pointer arg, IntPointer ret, int count);
public static native int avcodec_default_execute2(AVCodecContext c, Func_AVCodecContext_Pointer_int_int func,Pointer arg, IntBuffer ret, int count);
public static native int avcodec_default_execute2(AVCodecContext c, Func_AVCodecContext_Pointer_int_int func,Pointer arg, int[] ret, int count);
//FIXME func typedef

/**
 * Fill AVFrame audio data and linesize pointers.
 *
 * The buffer buf must be a preallocated buffer with a size big enough
 * to contain the specified samples amount. The filled AVFrame data
 * pointers will point to this buffer.
 *
 * AVFrame extended_data channel pointers are allocated if necessary for
 * planar audio.
 *
 * @param frame       the AVFrame
 *                    frame->nb_samples must be set prior to calling the
 *                    function. This function fills in frame->data,
 *                    frame->extended_data, frame->linesize[0].
 * @param nb_channels channel count
 * @param sample_fmt  sample format
 * @param buf         buffer to use for frame data
 * @param buf_size    size of buffer
 * @param align       plane size sample alignment (0 = default)
 * @return            >=0 on success, negative error code on failure
 * @todo return the size in bytes required to store the samples in
 * case of success, at the next libavutil bump
 */
public static native int avcodec_fill_audio_frame(AVFrame frame, int nb_channels,
                             @Cast("AVSampleFormat") int sample_fmt, @Cast("const uint8_t*") BytePointer buf,
                             int buf_size, int align);
public static native int avcodec_fill_audio_frame(AVFrame frame, int nb_channels,
                             @Cast("AVSampleFormat") int sample_fmt, @Cast("const uint8_t*") ByteBuffer buf,
                             int buf_size, int align);
public static native int avcodec_fill_audio_frame(AVFrame frame, int nb_channels,
                             @Cast("AVSampleFormat") int sample_fmt, @Cast("const uint8_t*") byte[] buf,
                             int buf_size, int align);

/**
 * Reset the internal decoder state / flush internal buffers. Should be called
 * e.g. when seeking or when switching to a different stream.
 *
 * @note when refcounted frames are not used (i.e. avctx->refcounted_frames is 0),
 * this invalidates the frames previously returned from the decoder. When
 * refcounted frames are used, the decoder just releases any references it might
 * keep internally, but the caller's reference remains valid.
 */
public static native void avcodec_flush_buffers(AVCodecContext avctx);

/**
 * Return codec bits per sample.
 *
 * @param[in] codec_id the codec
 * @return Number of bits per sample or zero if unknown for the given codec.
 */
public static native int av_get_bits_per_sample(@Cast("AVCodecID") int codec_id);

/**
 * Return the PCM codec associated with a sample format.
 * @param be  endianness, 0 for little, 1 for big,
 *            -1 (or anything else) for native
 * @return  AV_CODEC_ID_PCM_* or AV_CODEC_ID_NONE
 */
public static native @Cast("AVCodecID") int av_get_pcm_codec(@Cast("AVSampleFormat") int fmt, int be);

/**
 * Return codec bits per sample.
 * Only return non-zero if the bits per sample is exactly correct, not an
 * approximation.
 *
 * @param[in] codec_id the codec
 * @return Number of bits per sample or zero if unknown for the given codec.
 */
public static native int av_get_exact_bits_per_sample(@Cast("AVCodecID") int codec_id);

/**
 * Return audio frame duration.
 *
 * @param avctx        codec context
 * @param frame_bytes  size of the frame, or 0 if unknown
 * @return             frame duration, in samples, if known. 0 if not able to
 *                     determine.
 */
public static native int av_get_audio_frame_duration(AVCodecContext avctx, int frame_bytes);


public static class AVBitStreamFilterContext extends Pointer {
    static { Loader.load(); }
    /** Default native constructor. */
    public AVBitStreamFilterContext() { allocate(); }
    /** Native array allocator. Access with {@link Pointer#position(int)}. */
    public AVBitStreamFilterContext(int size) { allocateArray(size); }
    /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
    public AVBitStreamFilterContext(Pointer p) { super(p); }
    private native void allocate();
    private native void allocateArray(int size);
    @Override public AVBitStreamFilterContext position(int position) {
        return (AVBitStreamFilterContext)super.position(position);
    }

    public native Pointer priv_data(); public native AVBitStreamFilterContext priv_data(Pointer priv_data);
    public native AVBitStreamFilter filter(); public native AVBitStreamFilterContext filter(AVBitStreamFilter filter);
    public native AVCodecParserContext parser(); public native AVBitStreamFilterContext parser(AVCodecParserContext parser);
    public native AVBitStreamFilterContext next(); public native AVBitStreamFilterContext next(AVBitStreamFilterContext next);
    /**
     * Internal default arguments, used if NULL is passed to av_bitstream_filter_filter().
     * Not for access by library users.
     */
    public native @Cast("char*") BytePointer args(); public native AVBitStreamFilterContext args(BytePointer args);
}


public static class AVBitStreamFilter extends Pointer {
    static { Loader.load(); }
    /** Default native constructor. */
    public AVBitStreamFilter() { allocate(); }
    /** Native array allocator. Access with {@link Pointer#position(int)}. */
    public AVBitStreamFilter(int size) { allocateArray(size); }
    /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
    public AVBitStreamFilter(Pointer p) { super(p); }
    private native void allocate();
    private native void allocateArray(int size);
    @Override public AVBitStreamFilter position(int position) {
        return (AVBitStreamFilter)super.position(position);
    }

    @MemberGetter public native @Cast("const char*") BytePointer name();
    public native int priv_data_size(); public native AVBitStreamFilter priv_data_size(int priv_data_size);
    public static class Filter_AVBitStreamFilterContext_AVCodecContext_BytePointer_PointerPointer_IntPointer_BytePointer_int_int extends FunctionPointer {
        static { Loader.load(); }
        /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
        public    Filter_AVBitStreamFilterContext_AVCodecContext_BytePointer_PointerPointer_IntPointer_BytePointer_int_int(Pointer p) { super(p); }
        protected Filter_AVBitStreamFilterContext_AVCodecContext_BytePointer_PointerPointer_IntPointer_BytePointer_int_int() { allocate(); }
        private native void allocate();
        public native int call(AVBitStreamFilterContext bsfc,
                      AVCodecContext avctx, @Cast("const char*") BytePointer args,
                      @Cast("uint8_t**") PointerPointer poutbuf, IntPointer poutbuf_size,
                      @Cast("const uint8_t*") BytePointer buf, int buf_size, int keyframe);
    }
    public native Filter_AVBitStreamFilterContext_AVCodecContext_BytePointer_PointerPointer_IntPointer_BytePointer_int_int filter(); public native AVBitStreamFilter filter(Filter_AVBitStreamFilterContext_AVCodecContext_BytePointer_PointerPointer_IntPointer_BytePointer_int_int filter);
    public static class Close_AVBitStreamFilterContext extends FunctionPointer {
        static { Loader.load(); }
        /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
        public    Close_AVBitStreamFilterContext(Pointer p) { super(p); }
        protected Close_AVBitStreamFilterContext() { allocate(); }
        private native void allocate();
        public native void call(AVBitStreamFilterContext bsfc);
    }
    public native @Name("close") Close_AVBitStreamFilterContext _close(); public native AVBitStreamFilter _close(Close_AVBitStreamFilterContext _close);
    public native AVBitStreamFilter next(); public native AVBitStreamFilter next(AVBitStreamFilter next);
}

/**
 * Register a bitstream filter.
 *
 * The filter will be accessible to the application code through
 * av_bitstream_filter_next() or can be directly initialized with
 * av_bitstream_filter_init().
 *
 * @see avcodec_register_all()
 */
public static native void av_register_bitstream_filter(AVBitStreamFilter bsf);

/**
 * Create and initialize a bitstream filter context given a bitstream
 * filter name.
 *
 * The returned context must be freed with av_bitstream_filter_close().
 *
 * @param name    the name of the bitstream filter
 * @return a bitstream filter context if a matching filter was found
 * and successfully initialized, NULL otherwise
 */
public static native AVBitStreamFilterContext av_bitstream_filter_init(@Cast("const char*") BytePointer name);
public static native AVBitStreamFilterContext av_bitstream_filter_init(String name);

/**
 * Filter bitstream.
 *
 * This function filters the buffer buf with size buf_size, and places the
 * filtered buffer in the buffer pointed to by poutbuf.
 *
 * The output buffer must be freed by the caller.
 *
 * @param bsfc            bitstream filter context created by av_bitstream_filter_init()
 * @param avctx           AVCodecContext accessed by the filter, may be NULL.
 *                        If specified, this must point to the encoder context of the
 *                        output stream the packet is sent to.
 * @param args            arguments which specify the filter configuration, may be NULL
 * @param poutbuf         pointer which is updated to point to the filtered buffer
 * @param poutbuf_size    pointer which is updated to the filtered buffer size in bytes
 * @param buf             buffer containing the data to filter
 * @param buf_size        size in bytes of buf
 * @param keyframe        set to non-zero if the buffer to filter corresponds to a key-frame packet data
 * @return >= 0 in case of success, or a negative error code in case of failure
 *
 * If the return value is positive, an output buffer is allocated and
 * is available in *poutbuf, and is distinct from the input buffer.
 *
 * If the return value is 0, the output buffer is not allocated and
 * should be considered identical to the input buffer, or in case
 * *poutbuf was set it points to the input buffer (not necessarily to
 * its starting address).
 */
public static native int av_bitstream_filter_filter(AVBitStreamFilterContext bsfc,
                               AVCodecContext avctx, @Cast("const char*") BytePointer args,
                               @Cast("uint8_t**") PointerPointer poutbuf, IntPointer poutbuf_size,
                               @Cast("const uint8_t*") BytePointer buf, int buf_size, int keyframe);
public static native int av_bitstream_filter_filter(AVBitStreamFilterContext bsfc,
                               AVCodecContext avctx, @Cast("const char*") BytePointer args,
                               @Cast("uint8_t**") @ByPtrPtr BytePointer poutbuf, IntPointer poutbuf_size,
                               @Cast("const uint8_t*") BytePointer buf, int buf_size, int keyframe);
public static native int av_bitstream_filter_filter(AVBitStreamFilterContext bsfc,
                               AVCodecContext avctx, String args,
                               @Cast("uint8_t**") @ByPtrPtr ByteBuffer poutbuf, IntBuffer poutbuf_size,
                               @Cast("const uint8_t*") ByteBuffer buf, int buf_size, int keyframe);
public static native int av_bitstream_filter_filter(AVBitStreamFilterContext bsfc,
                               AVCodecContext avctx, @Cast("const char*") BytePointer args,
                               @Cast("uint8_t**") @ByPtrPtr byte[] poutbuf, int[] poutbuf_size,
                               @Cast("const uint8_t*") byte[] buf, int buf_size, int keyframe);
public static native int av_bitstream_filter_filter(AVBitStreamFilterContext bsfc,
                               AVCodecContext avctx, String args,
                               @Cast("uint8_t**") @ByPtrPtr BytePointer poutbuf, IntPointer poutbuf_size,
                               @Cast("const uint8_t*") BytePointer buf, int buf_size, int keyframe);
public static native int av_bitstream_filter_filter(AVBitStreamFilterContext bsfc,
                               AVCodecContext avctx, @Cast("const char*") BytePointer args,
                               @Cast("uint8_t**") @ByPtrPtr ByteBuffer poutbuf, IntBuffer poutbuf_size,
                               @Cast("const uint8_t*") ByteBuffer buf, int buf_size, int keyframe);
public static native int av_bitstream_filter_filter(AVBitStreamFilterContext bsfc,
                               AVCodecContext avctx, String args,
                               @Cast("uint8_t**") @ByPtrPtr byte[] poutbuf, int[] poutbuf_size,
                               @Cast("const uint8_t*") byte[] buf, int buf_size, int keyframe);

/**
 * Release bitstream filter context.
 *
 * @param bsf the bitstream filter context created with
 * av_bitstream_filter_init(), can be NULL
 */
public static native void av_bitstream_filter_close(AVBitStreamFilterContext bsf);

/**
 * If f is NULL, return the first registered bitstream filter,
 * if f is non-NULL, return the next registered bitstream filter
 * after f, or NULL if f is the last one.
 *
 * This function can be used to iterate over all registered bitstream
 * filters.
 */
public static native AVBitStreamFilter av_bitstream_filter_next(@Const AVBitStreamFilter f);

/* memory */

/**
 * Same behaviour av_fast_malloc but the buffer has additional
 * AV_INPUT_BUFFER_PADDING_SIZE at the end which will always be 0.
 *
 * In addition the whole buffer will initially and after resizes
 * be 0-initialized so that no uninitialized data will ever appear.
 */
public static native void av_fast_padded_malloc(Pointer ptr, @Cast("unsigned int*") IntPointer size, @Cast("size_t") long min_size);
public static native void av_fast_padded_malloc(Pointer ptr, @Cast("unsigned int*") IntBuffer size, @Cast("size_t") long min_size);
public static native void av_fast_padded_malloc(Pointer ptr, @Cast("unsigned int*") int[] size, @Cast("size_t") long min_size);

/**
 * Same behaviour av_fast_padded_malloc except that buffer will always
 * be 0-initialized after call.
 */
public static native void av_fast_padded_mallocz(Pointer ptr, @Cast("unsigned int*") IntPointer size, @Cast("size_t") long min_size);
public static native void av_fast_padded_mallocz(Pointer ptr, @Cast("unsigned int*") IntBuffer size, @Cast("size_t") long min_size);
public static native void av_fast_padded_mallocz(Pointer ptr, @Cast("unsigned int*") int[] size, @Cast("size_t") long min_size);

/**
 * Encode extradata length to a buffer. Used by xiph codecs.
 *
 * @param s buffer to write to; must be at least (v/255+1) bytes long
 * @param v size of extradata in bytes
 * @return number of bytes written to the buffer.
 */
public static native @Cast("unsigned int") int av_xiphlacing(@Cast("unsigned char*") BytePointer s, @Cast("unsigned int") int v);
public static native @Cast("unsigned int") int av_xiphlacing(@Cast("unsigned char*") ByteBuffer s, @Cast("unsigned int") int v);
public static native @Cast("unsigned int") int av_xiphlacing(@Cast("unsigned char*") byte[] s, @Cast("unsigned int") int v);

// #if FF_API_MISSING_SAMPLE
/**
 * Log a generic warning message about a missing feature. This function is
 * intended to be used internally by FFmpeg (libavcodec, libavformat, etc.)
 * only, and would normally not be used by applications.
 * @param[in] avc a pointer to an arbitrary struct of which the first field is
 * a pointer to an AVClass struct
 * @param[in] feature string containing the name of the missing feature
 * @param[in] want_sample indicates if samples are wanted which exhibit this feature.
 * If want_sample is non-zero, additional verbage will be added to the log
 * message which tells the user how to report samples to the development
 * mailing list.
 * @deprecated Use avpriv_report_missing_feature() instead.
 */
public static native @Deprecated void av_log_missing_feature(Pointer avc, @Cast("const char*") BytePointer feature, int want_sample);
public static native @Deprecated void av_log_missing_feature(Pointer avc, String feature, int want_sample);

/**
 * Log a generic warning message asking for a sample. This function is
 * intended to be used internally by FFmpeg (libavcodec, libavformat, etc.)
 * only, and would normally not be used by applications.
 * @param[in] avc a pointer to an arbitrary struct of which the first field is
 * a pointer to an AVClass struct
 * @param[in] msg string containing an optional message, or NULL if no message
 * @deprecated Use avpriv_request_sample() instead.
 */
public static native @Deprecated void av_log_ask_for_sample(Pointer avc, @Cast("const char*") BytePointer msg);
public static native @Deprecated void av_log_ask_for_sample(Pointer avc, String msg);
// #endif /* FF_API_MISSING_SAMPLE */

/**
 * Register the hardware accelerator hwaccel.
 */
public static native void av_register_hwaccel(AVHWAccel hwaccel);

/**
 * If hwaccel is NULL, returns the first registered hardware accelerator,
 * if hwaccel is non-NULL, returns the next registered hardware accelerator
 * after hwaccel, or NULL if hwaccel is the last one.
 */
public static native AVHWAccel av_hwaccel_next(@Const AVHWAccel hwaccel);


/**
 * Lock operation used by lockmgr
 */
/** enum AVLockOp */
public static final int
  /** Create a mutex */
  AV_LOCK_CREATE = 0,
  /** Lock the mutex */
  AV_LOCK_OBTAIN = 1,
  /** Unlock the mutex */
  AV_LOCK_RELEASE = 2,
  /** Free mutex resources */
  AV_LOCK_DESTROY = 3;

/**
 * Register a user provided lock manager supporting the operations
 * specified by AVLockOp. The "mutex" argument to the function points
 * to a (void *) where the lockmgr should store/get a pointer to a user
 * allocated mutex. It is NULL upon AV_LOCK_CREATE and equal to the
 * value left by the last call for all other ops. If the lock manager is
 * unable to perform the op then it should leave the mutex in the same
 * state as when it was called and return a non-zero value. However,
 * when called with AV_LOCK_DESTROY the mutex will always be assumed to
 * have been successfully destroyed. If av_lockmgr_register succeeds
 * it will return a non-negative value, if it fails it will return a
 * negative value and destroy all mutex and unregister all callbacks.
 * av_lockmgr_register is not thread-safe, it must be called from a
 * single thread before any calls which make use of locking are used.
 *
 * @param cb User defined callback. av_lockmgr_register invokes calls
 *           to this callback and the previously registered callback.
 *           The callback will be used to create more than one mutex
 *           each of which must be backed by its own underlying locking
 *           mechanism (i.e. do not use a single static object to
 *           implement your lock manager). If cb is set to NULL the
 *           lockmgr will be unregistered.
 */
public static class Cb_PointerPointer_int extends FunctionPointer {
    static { Loader.load(); }
    /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
    public    Cb_PointerPointer_int(Pointer p) { super(p); }
    protected Cb_PointerPointer_int() { allocate(); }
    private native void allocate();
    public native int call(@Cast("void**") PointerPointer mutex, @Cast("AVLockOp") int op);
}
public static native int av_lockmgr_register(Cb_PointerPointer_int cb);
public static class Cb_Pointer_int extends FunctionPointer {
    static { Loader.load(); }
    /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
    public    Cb_Pointer_int(Pointer p) { super(p); }
    protected Cb_Pointer_int() { allocate(); }
    private native void allocate();
    public native int call(@Cast("void**") @ByPtrPtr Pointer mutex, @Cast("AVLockOp") int op);
}
public static native int av_lockmgr_register(Cb_Pointer_int cb);

/**
 * Get the type of the given codec.
 */
public static native @Cast("AVMediaType") int avcodec_get_type(@Cast("AVCodecID") int codec_id);

/**
 * Get the name of a codec.
 * @return  a static string identifying the codec; never NULL
 */
public static native @Cast("const char*") BytePointer avcodec_get_name(@Cast("AVCodecID") int id);

/**
 * @return a positive value if s is open (i.e. avcodec_open2() was called on it
 * with no corresponding avcodec_close()), 0 otherwise.
 */
public static native int avcodec_is_open(AVCodecContext s);

/**
 * @return a non-zero number if codec is an encoder, zero otherwise
 */
public static native int av_codec_is_encoder(@Const AVCodec codec);

/**
 * @return a non-zero number if codec is a decoder, zero otherwise
 */
public static native int av_codec_is_decoder(@Const AVCodec codec);

/**
 * @return descriptor for given codec ID or NULL if no descriptor exists.
 */
public static native @Const AVCodecDescriptor avcodec_descriptor_get(@Cast("AVCodecID") int id);

/**
 * Iterate over all codec descriptors known to libavcodec.
 *
 * @param prev previous descriptor. NULL to get the first descriptor.
 *
 * @return next descriptor or NULL after the last descriptor
 */
public static native @Const AVCodecDescriptor avcodec_descriptor_next(@Const AVCodecDescriptor prev);

/**
 * @return codec descriptor with the given name or NULL if no such descriptor
 *         exists.
 */
public static native @Const AVCodecDescriptor avcodec_descriptor_get_by_name(@Cast("const char*") BytePointer name);
public static native @Const AVCodecDescriptor avcodec_descriptor_get_by_name(String name);

/**
 * Allocate a CPB properties structure and initialize its fields to default
 * values.
 *
 * @param size if non-NULL, the size of the allocated struct will be written
 *             here. This is useful for embedding it in side data.
 *
 * @return the newly allocated struct or NULL on failure
 */
public static native AVCPBProperties av_cpb_properties_alloc(@Cast("size_t*") SizeTPointer size);

/**
 * @}
 */

// #endif /* AVCODEC_AVCODEC_H */


}
