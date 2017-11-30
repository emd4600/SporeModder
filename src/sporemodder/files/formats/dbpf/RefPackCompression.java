package sporemodder.files.formats.dbpf;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import sporemodder.files.ByteArrayStreamAccessor;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.ReadWriteStreamAccessor;

public class RefPackCompression {
	public static class CompressorOutput {
		byte[] data;
		int lengthInBytes;
	}
	private static final int[] crctab = new int[] // size 256
		{
			0x0000, 0xc0c1, 0xc181, 0x0140, 0xc301, 0x03c0, 0x0280, 0xc241,
			0xc601, 0x06c0, 0x0780, 0xc741, 0x0500, 0xc5c1, 0xc481, 0x0440,
			0xcc01, 0x0cc0, 0x0d80, 0xcd41, 0x0f00, 0xcfc1, 0xce81, 0x0e40,
			0x0a00, 0xcac1, 0xcb81, 0x0b40, 0xc901, 0x09c0, 0x0880, 0xc841,
			0xd801, 0x18c0, 0x1980, 0xd941, 0x1b00, 0xdbc1, 0xda81, 0x1a40,
			0x1e00, 0xdec1, 0xdf81, 0x1f40, 0xdd01, 0x1dc0, 0x1c80, 0xdc41,
			0x1400, 0xd4c1, 0xd581, 0x1540, 0xd701, 0x17c0, 0x1680, 0xd641,
			0xd201, 0x12c0, 0x1380, 0xd341, 0x1100, 0xd1c1, 0xd081, 0x1040,
			0xf001, 0x30c0, 0x3180, 0xf141, 0x3300, 0xf3c1, 0xf281, 0x3240,
			0x3600, 0xf6c1, 0xf781, 0x3740, 0xf501, 0x35c0, 0x3480, 0xf441,
			0x3c00, 0xfcc1, 0xfd81, 0x3d40, 0xff01, 0x3fc0, 0x3e80, 0xfe41,
			0xfa01, 0x3ac0, 0x3b80, 0xfb41, 0x3900, 0xf9c1, 0xf881, 0x3840,
			0x2800, 0xe8c1, 0xe981, 0x2940, 0xeb01, 0x2bc0, 0x2a80, 0xea41,
			0xee01, 0x2ec0, 0x2f80, 0xef41, 0x2d00, 0xedc1, 0xec81, 0x2c40,
			0xe401, 0x24c0, 0x2580, 0xe541, 0x2700, 0xe7c1, 0xe681, 0x2640,
			0x2200, 0xe2c1, 0xe381, 0x2340, 0xe101, 0x21c0, 0x2080, 0xe041,
			0xa001, 0x60c0, 0x6180, 0xa141, 0x6300, 0xa3c1, 0xa281, 0x6240,
			0x6600, 0xa6c1, 0xa781, 0x6740, 0xa501, 0x65c0, 0x6480, 0xa441,
			0x6c00, 0xacc1, 0xad81, 0x6d40, 0xaf01, 0x6fc0, 0x6e80, 0xae41,
			0xaa01, 0x6ac0, 0x6b80, 0xab41, 0x6900, 0xa9c1, 0xa881, 0x6840,
			0x7800, 0xb8c1, 0xb981, 0x7940, 0xbb01, 0x7bc0, 0x7a80, 0xba41,
			0xbe01, 0x7ec0, 0x7f80, 0xbf41, 0x7d00, 0xbdc1, 0xbc81, 0x7c40,
			0xb401, 0x74c0, 0x7580, 0xb541, 0x7700, 0xb7c1, 0xb681, 0x7640,
			0x7200, 0xb2c1, 0xb381, 0x7340, 0xb101, 0x71c0, 0x7080, 0xb041,
			0x5000, 0x90c1, 0x9181, 0x5140, 0x9301, 0x53c0, 0x5280, 0x9241,
			0x9601, 0x56c0, 0x5780, 0x9741, 0x5500, 0x95c1, 0x9481, 0x5440,
			0x9c01, 0x5cc0, 0x5d80, 0x9d41, 0x5f00, 0x9fc1, 0x9e81, 0x5e40,
			0x5a00, 0x9ac1, 0x9b81, 0x5b40, 0x9901, 0x59c0, 0x5880, 0x9841,
			0x8801, 0x48c0, 0x4980, 0x8941, 0x4b00, 0x8bc1, 0x8a81, 0x4a40,
			0x4e00, 0x8ec1, 0x8f81, 0x4f40, 0x8d01, 0x4dc0, 0x4c80, 0x8c41,
			0x4400, 0x84c1, 0x8581, 0x4540, 0x8701, 0x47c0, 0x4680, 0x8641,
			0x8201, 0x42c0, 0x4380, 0x8341, 0x4100, 0x81c1, 0x8081, 0x4040,
		};
	
	public static int getDecompressedSize(InputStreamAccessor in) throws IOException {
		byte cType = in.readByte();
		in.skipBytes(1);
		
		//System.out.println(in.getFilePointer());
		
		int decompSize = 0;
		
		if (cType == 0x10 || cType == 0x50) {
			int var1 = in.readByte() & 0xFF;
			int var2 = in.readByte() & 0xFF;
			int var3 = in.readByte() & 0xFF;
			decompSize = var1 << 16 | var2 << 8 | var3;
//			decompSize = (in.readByte() & 0xFF) << 16 | (in.readByte() & 0xFF) << 8 | (in.readByte() & 0xFF);
			in.skipBytes(-5);
			return decompSize;
		} else {
			in.skipBytes(-2);
			return -1;
		}
	}
	
	//TODO it might be faster to use byte arrays instead. 
	public static void decompress(InputStreamAccessor in, ReadWriteStreamAccessor out) throws IOException {
		byte cType = in.readByte();
		in.skipBytes(1);
		
		// 10FB & 1FFF
		// We somehow extract the decompSize length from that operation
		
		if (cType == 0x10 || cType == 0x50) {
			int decompSize = (in.readByte() & 0xFF) << 16 | (in.readByte() & 0xFF) << 8 | (in.readByte() & 0xFF);//bb.getInt();
			int size = 0;
			//System.out.println(decompSize);
			while(size < decompSize) {
				int numPlainData = 0;
				int numToCopy = 0;
				int copyOffset = 0;
				
				int controlChar = in.readUByte();
				//Detects the control character
				if (controlChar >= 252) {
					numPlainData = controlChar & 0x03;
				} else if (controlChar >= 224) {
					numPlainData = ((controlChar & 0x1F) << 2 ) + 4;
				} else if (controlChar >= 192) {
					byte[] CCArray = {(byte)controlChar, in.readByte(), in.readByte(), in.readByte()};
					numPlainData =  controlChar & 0x03;
					numToCopy = ((controlChar & 0x0C) << 6 )  + Byte.toUnsignedInt(CCArray[3]) + 5;
					copyOffset = ((controlChar & 0x10) << 12 ) + (Byte.toUnsignedInt(CCArray[1]) << 8 ) + Byte.toUnsignedInt(CCArray[2]) + 1;
				} else if (controlChar >= 128) {
					byte[] CCArray = {0, (byte)controlChar, in.readByte(), in.readByte()};
					numPlainData = ((Byte.toUnsignedInt(CCArray[2]) & 0xC0) >> 6) & 0x03;
					numToCopy = (controlChar & 0x3F) + 4;
					copyOffset = ((Byte.toUnsignedInt(CCArray[2]) & 0x3F) << 8) + Byte.toUnsignedInt(CCArray[3]) + 1;
				} else {
					byte[] CCArray = {(byte)controlChar, in.readByte()};
					numPlainData = controlChar & 0x03;
					numToCopy = ((controlChar & 0x1C) >> 2) + 3;
					copyOffset = ((controlChar & 0x60) << 3) + Byte.toUnsignedInt(CCArray[1]) + 1;
				}
				
				//Writes data
				if(numPlainData > 0) {
					byte[] arr = new byte[numPlainData];
					in.read(arr);
					out.write(arr);
					size += numPlainData;
				}
//				int length = out.length();
				if (numToCopy > copyOffset) {
					for (int c = 0; c < numToCopy; c++) {
						out.seek(size - copyOffset); //out.seek(out.length() - copyOffset+c);
						byte byte1 = out.readByte();
						out.seek(size);
						out.writeByte(byte1);
						size += 1;
					}
//					System.out.println("BIGGER");
				} else {
					out.seek(size - copyOffset);
					byte[] arr = new byte[numToCopy];
					out.read(arr);
					out.seek(size);
					out.write(arr);
					size += numToCopy;
				}
				
			}
		} else {
			throw new IOException("Unknown compression type at position " + in.getFilePointer());
		}
		out.seek(0);
	}
	
	public static void decompressFast(byte[] in, byte[] out) throws IOException {
		int pin = 0;
//		int pout = 0;
		byte cType = in[pin++];
		pin++;
		
		// 10FB & 1FFF
		// We somehow extract the decompSize length from that operation
		
		if (cType == 0x10 || cType == 0x50) {
			int decompSize = (in[pin++] & 0xFF) << 16 | (in[pin++] & 0xFF) << 8 | (in[pin++] & 0xFF);//bb.getInt();
			int size = 0;
			//System.out.println(decompSize);
			while(size < decompSize) {
				int numPlainData = 0;
				int numToCopy = 0;
				int copyOffset = 0;
				
				int controlChar = in[pin++] & 0xFF;
				//Detects the control character
				if (controlChar >= 252) {
					numPlainData = controlChar & 0x03;
				} else if (controlChar >= 224) {
					numPlainData = ((controlChar & 0x1F) << 2 ) + 4;
				} else if (controlChar >= 192) {
					byte[] CCArray = {(byte)controlChar, in[pin++], in[pin++], in[pin++]};
					numPlainData =  controlChar & 0x03;
					numToCopy = ((controlChar & 0x0C) << 6 )  + Byte.toUnsignedInt(CCArray[3]) + 5;
					copyOffset = ((controlChar & 0x10) << 12 ) + (Byte.toUnsignedInt(CCArray[1]) << 8 ) + Byte.toUnsignedInt(CCArray[2]) + 1;
				} else if (controlChar >= 128) {
					byte[] CCArray = {0, (byte)controlChar, in[pin++], in[pin++]};
					numPlainData = ((Byte.toUnsignedInt(CCArray[2]) & 0xC0) >> 6) & 0x03;
					numToCopy = (controlChar & 0x3F) + 4;
					copyOffset = ((Byte.toUnsignedInt(CCArray[2]) & 0x3F) << 8) + Byte.toUnsignedInt(CCArray[3]) + 1;
				} else {
					byte[] CCArray = {(byte)controlChar, in[pin++]};
					numPlainData = controlChar & 0x03;
					numToCopy = ((controlChar & 0x1C) >> 2) + 3;
					copyOffset = ((controlChar & 0x60) << 3) + Byte.toUnsignedInt(CCArray[1]) + 1;
				}
				
				//Writes data
				if(numPlainData > 0) {
					System.arraycopy(in, pin, out, size, numPlainData);  // use pout instead of size ?
					pin += numPlainData;
//					pout += numPlainData;
					size += numPlainData;
				}
//				int length = out.length();
				if (numToCopy > copyOffset) {
					for (int c = 0; c < numToCopy; c++) {
						out[size] = out[size - copyOffset];
						size++;
					}
					
//					System.arraycopy(out, size - copyOffset, out, size, numToCopy);
					
//					System.arraycopy(out, size - copyOffset, out, size, copyOffset);
//					size += copyOffset;
//					System.arraycopy(out, size, out, size + copyOffset, numToCopy - copyOffset);
//					size += numToCopy - copyOffset;
				} else {
//					out.seek(size - copyOffset);
//					byte[] arr = new byte[numToCopy];
//					out.read(arr);
//					out.seek(size);
//					out.write(arr);
//					size += numToCopy;
					
					System.arraycopy(out, size - copyOffset, out, size, numToCopy);
					size += numToCopy;
				}
				
			}
		} else {
			throw new IOException("Unknown compression type at position " + pin);
		}
	}
	
	@Deprecated
	public static void decompressOld(InputStreamAccessor in, ReadWriteStreamAccessor out, int compressedSize) throws IOException {
		while(in.getFilePointer() < compressedSize) {
			int numPlainData = 0;
			int numToCopy = 0;
			int copyOffset = 0;
			
			int controlChar = in.readUByte();
			//Detects the control character
			if (controlChar >= 252) {
				numPlainData = controlChar & 0x03;
			} else if (controlChar >= 224) {
				numPlainData = ((controlChar & 0x1F) << 2 ) + 4;
			} else if (controlChar >= 192) {
				byte[] CCArray = {(byte)controlChar, in.readByte(), in.readByte(), in.readByte()};
				numPlainData =  controlChar & 0x03;
				numToCopy = ((controlChar & 0x0C) << 6 )  + Byte.toUnsignedInt(CCArray[3]) + 5;
				copyOffset = ((controlChar & 0x10) << 12 ) + (Byte.toUnsignedInt(CCArray[1]) << 8 ) + Byte.toUnsignedInt(CCArray[2]) + 1;
			} else if (controlChar >= 128) {
				byte[] CCArray = {0, (byte)controlChar, in.readByte(), in.readByte()};
				numPlainData = ((Byte.toUnsignedInt(CCArray[2]) & 0xC0) >> 6) & 0x03;
				numToCopy = (controlChar & 0x3F) + 4;
				copyOffset = ((Byte.toUnsignedInt(CCArray[2]) & 0x3F) << 8) + Byte.toUnsignedInt(CCArray[3]) + 1;
			} else {
				byte[] CCArray = {(byte)controlChar, in.readByte()};
				numPlainData = controlChar & 0x03;
				numToCopy = ((controlChar & 0x1C) >> 2) + 3;
				copyOffset = ((controlChar & 0x60) << 3) + Byte.toUnsignedInt(CCArray[1]) + 1;
			}
			
			//Writes data
			if(numPlainData > 0) {
				byte[] arr = new byte[numPlainData];
				in.read(arr);
				out.write(arr);
//				size += numPlainData;
			}
			int length = out.length();
			if (numToCopy > copyOffset) {
				for (int c = 0; c < numToCopy; c++) {
					out.seek(out.length() - copyOffset); //out.seek(out.length() - copyOffset+c);
					byte byte1 = out.readByte();
					out.seek(out.length());
					out.writeByte(byte1);
				}
//					System.out.println("BIGGER");
			} else {
				out.seek(length - copyOffset);
				byte[] arr = new byte[numToCopy];
				out.read(arr);
				out.seek(length);
				out.write(arr);
			}
			
//			size += numToCopy;
		}
		out.seek(0);
	}
	
	
	public static void compress(byte[] input, int inputLength, CompressorOutput out) throws IOException {
		
		int len;
		int tlen; //uint
		int tcost; //uint
		int run = 0; //uint
		int toffset; //uint
		int boffset; //uint
		int blen; //uint
		int bcost; //uint
		int mlen; //uint
		int tptr; // const u_int8*
		int cptr = 0; // const u_int8*, pointer to input buffer
		int rptr = 0; // const u_int8*, pointer to input buffer
		int to = 0; // u_int8*. Pointer to outputBuffer
		
		int countliterals = 0;
		int countshort = 0;
		int countlong = 0;
		int countvlong = 0;
		int hash;
		int hoffset;
		long minhoffset;
		int i; // offset in input
		int[] link; // int32 *
		int[] hashtbl; // int32 *
		int hashptr = 0; // int32 *
		
		len = inputLength;
		out.data = new byte[len * 2 + 8192]; 
		
		byte[] in = new byte[len + 2]; //we need more size?
		System.arraycopy(input, 0, in, 0, len);
		
		// Write size into the stream
		for (i = 0; i < 3; i++, to++) {
			out.data[to] = (byte)(len >> ((2-i) * 8) & 0xFF);
		}
		
		hashtbl = new int[65536];
		link = new int[131072];
		
		for (i = 0; i < 65536/16; ++i) {
			hashtbl[hashptr + 0] = hashtbl[hashptr + 1] = hashtbl[hashptr + 2] = hashtbl[hashptr + 3] =
			hashtbl[hashptr + 4] = hashtbl[hashptr + 5] = hashtbl[hashptr + 6] = hashtbl[hashptr + 7] =
			hashtbl[hashptr + 8] = hashtbl[hashptr + 9] = hashtbl[hashptr + 10] = hashtbl[hashptr + 11] =
			hashtbl[hashptr + 12] = hashtbl[hashptr + 13] = hashtbl[hashptr + 14] = hashtbl[hashptr + 15] = -1;
			hashptr += 16;
		}
		
		while (len > 0)
		{
			boffset = 0;
			blen = bcost = 2;
			mlen = min(len, 1028);
			tptr = cptr - 1;
			hash = hash(in, cptr); // cptr points to input buffer
			hoffset = hashtbl[hash];
			minhoffset = max(cptr - 131071, 0);
			
			if (hoffset >= minhoffset)
			{
				do
				{
					tptr = hoffset; // tptr points to input buffer
					if (in[cptr + blen] == in[tptr + blen])
					{
						// cptr and tptr point to input buffer
						tlen = matchlen(in, cptr, in, tptr, mlen);
						if (tlen > blen)
						{
							toffset = (cptr-1)-tptr;
							// two byte long form
							if (toffset < 1024 && tlen <= 10) {
								tcost = 2;
							//three byte long form
							} else if (toffset < 16384 && tlen <= 67) {
								tcost = 3;
							// four byte very long form
							} else {
								tcost = 4;
							}
							
							if (tlen - tcost + 4 > blen - bcost + 4)
							{
								blen = tlen;
								bcost = tcost;
								boffset = toffset;
								if (blen >= 1028) {
									break;
								}
							}
						}
					}
					
				} while ((hoffset = link[hoffset & 131071]) >= minhoffset);
			}
			
			if (bcost >= blen)
			{
				hoffset = cptr;
				link[hoffset & 131071] = hashtbl[hash];
				hashtbl[hash] = hoffset;
				
				++run;
				++cptr;
				--len;
			}
			else
			{
				// literal block of data
				while (run > 3)
				{
					tlen = min(112, run & ~3);
					run -= tlen;
					out.data[to++] = (byte)(0xE0 + (tlen >> 2) -1);
					// memcpy(to, rptr, tlen)
					System.arraycopy(in, rptr, out.data, to, tlen);
					rptr += tlen;
					to += tlen;
					++countliterals;
				}
				// two byte long form
				if (bcost == 2)
				{
					out.data[to++] = (byte)(((boffset >> 8) << 5) + ((blen - 3) << 2) + run);
					out.data[to++] = (byte)boffset;
					++countshort;
				}
				// three byte long form
				else if (bcost == 3)
				{
					out.data[to++] = (byte) (0x80 + (blen - 4));
					out.data[to++] = (byte) ((run<<6) + (boffset>>8));
					out.data[to++] = (byte) boffset;
					++countlong;
				}
				// four byte very long form
				else
				{
					out.data[to++] = (byte) (0xC0 + ((boffset >> 16) << 4) + 
							(((blen - 5) >> 8) << 2) + run);
					out.data[to++] = (byte) (boffset >> 8);
					out.data[to++] = (byte) (boffset);
					out.data[to++] = (byte) (blen - 5);
					++countvlong;
				}
				
				if (run != 0)
				{
					// memcpy(to, rptr, run);
					System.arraycopy(in, rptr, out.data, to, run);
					to += run;
					run = 0;
				}
				
				//TODO quick?
				
				{
					for (i = 0; i < blen; ++i)
					{
						hash = hash(in, cptr);
						// hoffset = (cptr-static_cast<u_int8 *>(input.buffer));
						hoffset = cptr;
						link[hoffset & 131071] = hashtbl[hash];
						hashtbl[hash] = hoffset;
						++cptr;
					}
				}
				
				rptr = cptr;
				len -= blen;
			}
		}
		// no match at end, use literal
		while (run > 3)
		{
			tlen = min(112, run & ~3);
			run -= tlen;
			out.data[to++] = (byte) (0xE0 + (tlen >> 2) - 1);
			// memcpy(to,rptr,tlen);
			System.arraycopy(in, rptr, out.data, to, tlen);
			rptr += tlen;
			to += tlen;
		}
		
		// end of stream command + 0..3 literal
		out.data[to++] = (byte) (0xFC + run);
		if (run != 0)
		{
			// memcpy(to,rptr,run);
			System.arraycopy(in, rptr, out.data, to, run);
			to += run;
		}
		
		out.lengthInBytes = to + 2;
		//TODO Optimize this?
		// We add 0x10FB here. This might no be the most efficient method, but the original code didn't add it at all, so maybe adding it
		// at the beginning of the method causes some trouble.
		byte[] newData = new byte[out.lengthInBytes];
		newData[0] = 0x10;
		newData[1] = (byte) 0xFB;
		System.arraycopy(out.data, 0, newData, 2, to);
		out.data = newData;
	}
	
	private static int min(int a, int b) {
		return a > b ? b : a;
	}
	
	private static int max(int a, int b) {
		return a > b ? a : b;
	}
	
	static int counter = 0;
	private static int hash(byte[] array, int ptr) {
		int crc = 0;
		
		//System.out.println(counter);
		
		// What is this? We are getting bytes out of the file but it seems we don't care.
		int var1 = ptr >= array.length ? 0xFD : array[ptr]; //0xFD is just a guess based on nothing
		int var2 = ptr >= array.length ? 0xFD : array[ptr]; //0xFD is just a guess based on nothing
		int var3 = ptr >= array.length ? 0xFD : array[ptr]; //0xFD is just a guess based on nothing
		
		crc = crctab[var1 & 0xFF];
		crc = crctab[(crc ^ var2) & 0xFF] ^ (crc >> 8);
//		int test = (crc ^ array[ptr + 2]) & 0xFF;
//		int test2 = crctab[(crc ^ array[ptr + 2]) & 0xFF];
//		int test3 = (crc >> 8);
//		int test4 = crctab[(crc ^ array[ptr + 2]) & 0xFF] ^ (crc >> 8);
		crc = crctab[(crc ^ var3) & 0xFF] ^ (crc >> 8);
		
		counter++;
		
		return crc;
	}
	
	private static int matchlen(byte[] source, int s, byte[] dst, int d, int maxmatch) {
		int current;
		
		for (current = 0; current < maxmatch && source[s++] == dst[d++]; ++current);
		
		return current;
	}
	
	
//	public static void main(String[] args) throws IOException {
//		String path = "C:\\Users\\Eric\\Desktop\\";
//		String inputFile = "compressed.rw4";
//		String outputFile1 = "decompressed_01.rw4";
//		String outputFile2 = "decompressed_02.rw4";
//		String outputFile3 = "decompressed_03.rw4";
//		String outputFile4 = "decompressed_04.rw4";
//		String outputFile5 = "decompressed_05.rw4";
//		String outputFile6 = "decompressed_06.rw4";
//		
//		StreamAccessor in = null;
//		StreamAccessor out = null;
//		
//		long firstTime;
//		long testTime1 = 0;
//		long testTime2 = 0;
//		long testTime3 = 0;
//		long testTime4 = 0;
//		long testTime5 = 0;
//		long testTime6 = 0;
//		
////		// TEST 1 -- Using FileStreamAccessors
////		firstTime = System.currentTimeMillis();
////		in = new FileStreamAccessor(path + inputFile, "r");
////		out = new FileStreamAccessor(path + outputFile1, "rw");
////		
////		try {
////			decompress(in, out, in.length());
////		} finally {
////			in.close();
////			out.close();
////		}
////		testTime1 = System.currentTimeMillis() - firstTime;
////		
////		// TEST 2 -- Input is a FileStreamAccessor, output a ByteArrayStreamAccessor
////		firstTime = System.currentTimeMillis();
////		in = new FileStreamAccessor(path + inputFile, "r");
////		out = new ByteArrayStreamAccessor(924);
////		
////		try {
////			decompress(in, out, in.length());
////			((ByteArrayStreamAccessor) out).writeToFile(path + outputFile2);
////		} finally {
////			in.close();
////			out.close();
////		}
////		testTime2 = System.currentTimeMillis() - firstTime;
////		
////		// TEST 3 -- ByteArrayStreamAccessor
////		firstTime = System.currentTimeMillis();
////		in = new ByteArrayStreamAccessor(path + inputFile);
////		out = new ByteArrayStreamAccessor(924);
////		
////		try {
////			decompress(in, out, in.length());
////			((ByteArrayStreamAccessor) out).writeToFile(path + outputFile2);
////		} finally {
////			in.close();
////			out.close();
////		}
////		testTime3 = System.currentTimeMillis() - firstTime;
//		
//		
//		// TEST 4 -- Using FileStreamAccessors using decompressAndWrite
//		firstTime = System.currentTimeMillis();
//		in = new FileStreamAccessor(path + inputFile, "r");
//		out = new FileStreamAccessor(path + outputFile1, "rw");
//		
//		try {
//			decompress(in, out);
//		} finally {
//			in.close();
//			out.close();
//		}
//		testTime4 = System.currentTimeMillis() - firstTime;
//		
//		// TEST 5 -- Input is a FileStreamAccessor, output a ByteArrayStreamAccessor using decompressAndWrite
//		firstTime = System.currentTimeMillis();
//		in = new FileStreamAccessor(path + inputFile, "r");
//		out = new ByteArrayStreamAccessor(924);
//		
//		try {
//			decompress(in, out);
//			((ByteArrayStreamAccessor) out).writeToFile(path + outputFile2);
//		} finally {
//			in.close();
//			out.close();
//		}
//		testTime5 = System.currentTimeMillis() - firstTime;
//		
//		// TEST 6 -- ByteArrayStreamAccessor using decompressAndWrite
//		firstTime = System.currentTimeMillis();
//		in = new ByteArrayStreamAccessor(path + inputFile);
//		out = new ByteArrayStreamAccessor(924);
//		
//		try {
//			decompress(in, out);
//			((ByteArrayStreamAccessor) out).writeToFile(path + outputFile3);
//		} finally {
//			in.close();
//			out.close();
//		}
//		testTime6 = System.currentTimeMillis() - firstTime;
//		
//		System.out.println("TEST 1 -- \t" + testTime1 + " ms");
//		System.out.println("TEST 2 -- \t" + testTime2 + " ms");
//		System.out.println("TEST 3 -- \t" + testTime3 + " ms");
//		System.out.println("TEST 4 -- \t" + testTime4 + " ms");
//		System.out.println("TEST 5 -- \t" + testTime5 + " ms");
//		System.out.println("TEST 6 -- \t" + testTime6 + " ms");
//	}
	
//	public static void main(String[] args) throws IOException {
////		ByteArrayStreamAccessor in = new ByteArrayStreamAccessor("C:\\Users\\Eric\\Desktop\\compressed_test_01.rw4");
////		ByteArrayStreamAccessor out = new ByteArrayStreamAccessor(getDecompressedSize(in));
////		
////		try {
////			decompress(in, out);
//////			out.writeToFile("C:\\Users\\Eric\\Desktop\\decompressed_cpp_01.prop");
////		} finally {
////			in.close();
////			out.writeToFile("C:\\Users\\Eric\\Desktop\\decompressed_test_01.rw4");
////			out.close();
////		}
//		
//		ByteArrayStreamAccessor in = new ByteArrayStreamAccessor("C:\\Users\\Eric\\Desktop\\decompressed_01.rw4");
//		ByteArrayStreamAccessor out = null;
//		CompressorOutput cOut = new CompressorOutput();
//		try {
//			compress(in.toByteArray(), cOut);
//			
//			out = new ByteArrayStreamAccessor(cOut.lengthInBytes);
//			out.write(cOut.data, 0, cOut.lengthInBytes);
//			out.writeToFile("C:\\Users\\Eric\\Desktop\\compressed_test_01.rw4");
//		} finally {
//			in.close();
//			if (out != null) out.close();
//		}
//	}
	
	public static void main(String[] args) throws IOException {
		byte[] out = new byte[104989];
		byte[] in = Files.readAllBytes(new File("C:\\Users\\Eric\\Desktop\\compressed.rw4").toPath());
		RefPackCompression.decompressFast(in, out);
		
		Files.write(new File("C:\\Users\\Eric\\Desktop\\uncompressed.rw4").toPath(), out, StandardOpenOption.CREATE);
	}
}
