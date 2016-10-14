package sporemodder.files.formats.dbpf;

import java.io.IOException;
import java.util.List;

import sporemodder.files.ByteArrayStreamAccessor;
import sporemodder.files.FileStreamAccessor;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.ReadWriteStreamAccessor;
import sporemodder.files.formats.ConvertAction;
import sporemodder.files.formats.FileStructure;
import sporemodder.utilities.Hasher;
import sporemodder.utilities.UnpackSpeedTest;

public class DBPFItem extends FileStructure {
	public static boolean DEBUG = false;
	
	public boolean isCompressed;
	public int chunkOffset, memSize, compressedSize;
	public String path;
	public int name;
	public int group;
	public int type;
	public short unk = 1;
	
	@Override
	public String toString() {
		return Hasher.getFileName(group) + "\\" + Hasher.getFileName(name) + "." + Hasher.getTypeName(type);
	}
	
	public void readInfo(InputStreamAccessor in, int dbpfType) throws IOException {
		if (type == -1) {
			type = in.readLEInt();
		}
		if (group == -1) {
			group = in.readLEInt();
		}
		name = in.readLEInt();
		chunkOffset = in.readLEInt(); //when DBBF, long
		if (dbpfType == DBPFHeader.TYPE_DBBF) in.skipBytes(4);
		compressedSize = in.readLEInt() & 0x7FFFFFFF;
		memSize = in.readLEInt(); //Decompressed size
		switch(in.readShort()) {
			case 0: isCompressed = false;
					break;
			case -1: isCompressed = true;
					break;
			default: throw new IOException("Unknown compression label on position " + in.getFilePointer());
		}
		unk = in.readLEShort();
	}
	
	public void writeInfo(OutputStreamAccessor out) throws IOException {
		//TODO This only supports one index type
		//TODO Add support for DBBFs too
		out.writeLEInt(type);
		out.writeLEInt(group);
		out.writeLEInt(name);
		out.writeLEInt(chunkOffset);
		out.writeLEInt(compressedSize | 0x80000000);
		out.writeLEInt(memSize);
		out.writeLEShort(isCompressed ? 0xFFFF : 0);
		out.writeLEShort(unk);
	}
	
	public ByteArrayStreamAccessor processFile(InputStreamAccessor in) throws IOException 
	{
//		ByteArrayStreamAccessor stream = new  ByteArrayStreamAccessor(memSize);
		ByteArrayStreamAccessor stream;
		in.seek(chunkOffset);
		
		if (isCompressed) {
//			if (!DEBUG) {
//				long time1 = System.nanoTime();
//				RefPackCompression.decompress(in, stream);
//				stream.seek(0);
//				UnpackSpeedTest.TIMES.add(System.nanoTime() - time1);
////				System.out.println("decompress time: " + (System.nanoTime() - time1));
//			}
//			else {
//				long time1 = System.nanoTime();
//				byte[] arr = new byte[compressedSize];
//				in.read(arr);
//				RefPackCompression.decompressFast(arr, stream.toByteArray());
//				stream.seek(0);
//				UnpackSpeedTest.TIMES_FAST.add(System.nanoTime() - time1);
////				System.out.println("decompress time fast: " + (System.nanoTime() - time1));
//			}
			stream = new  ByteArrayStreamAccessor(memSize);
			byte[] arr = new byte[compressedSize];
			in.read(arr);
			RefPackCompression.decompressFast(arr, stream.toByteArray());
		}
		else {
			byte[] UncompressedFileBuffer = new byte[memSize];
			in.read(UncompressedFileBuffer);
			stream = new ByteArrayStreamAccessor(UncompressedFileBuffer);
		}
		
		return stream;
	}
	
	public void processNormalFile(InputStreamAccessor in, String outPath) throws IOException {
		ReadWriteStreamAccessor out = null;
		try {
			if (!isCompressed) {
				out = new FileStreamAccessor(outPath+"\\"+Hasher.getFileName(group)
						+"\\"+Hasher.getFileName(name)+"."+Hasher.getTypeName(type), "rw", true);
				byte[] UncompressedFileBuffer = new byte[memSize];
				in.seek(chunkOffset);
				in.read(UncompressedFileBuffer);
				out.write(UncompressedFileBuffer);
			} else {
				out = new ByteArrayStreamAccessor(memSize);
				in.seek(chunkOffset);
				RefPackCompression.decompress(in, out);
				((ByteArrayStreamAccessor) out).writeToFile(outPath+"\\"+Hasher.getFileName(group)
				+"\\"+Hasher.getFileName(name)+"."+Hasher.getTypeName(type));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			out.close();
		}
	}
	public void processNormalFile(InputStreamAccessor in, ReadWriteStreamAccessor out) throws IOException {
		if (!isCompressed) {
			byte[] UncompressedFileBuffer = new byte[memSize];
			in.seek(chunkOffset);
			in.read(UncompressedFileBuffer);
			out.write(UncompressedFileBuffer);
		} else {
			in.seek(chunkOffset);
			RefPackCompression.decompress(in, out);
		}
	}
//	public void processAdvData(InputStreamAccessor in, String outPath) throws IOException, FileStructureException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException, ParserConfigurationException, TransformerException, TransformerFactoryConfigurationError {
//		if (!isCompressed) {
//			FileStreamAccessor out = new FileStreamAccessor(outPath+"\\"+Hasher.getFileName(group)
//					+"\\"+Hasher.getFileName(name)+"."+Hasher.getTypeName(type), "rw");
//			try {
//				in.seek(chunkOffset);
//				byte[] arr = new byte[memSize];
//				in.read(arr); //TODO
//				out.write(arr);
//				//Convert
//				out.seek(0);
//				AdventureDecoder.decodeAdventure(out, outPath+"\\"+Hasher.getFileName(group)
//						+"\\"+Hasher.getFileName(name));
//			} finally {
//				out.close();
//			}
//		} else {
//			ByteListStreamAccessor out = new ByteListStreamAccessor();
//			try {
//				in.seek(chunkOffset);
//				RefPackCompression.decompress(in, out);
//				out.seek(0);
//				out.writeToFile(outPath+"\\"+Hasher.getFileName(group)
//						+"\\"+Hasher.getFileName(name)+"."+convertTo[3]);
//				out.seek(0);
//				AdventureDecoder.readAdvData(out, outPath+"\\"+Hasher.getFileName(group)
//						+"\\"+Hasher.getFileName(name));
//			} finally {
//				out.close();
//			}
//		}
//	}
//	public void processRW4Texture(InputStreamAccessor in, String outPath) throws IOException, FileStructureException, InstantiationException, IllegalAccessException {
//		if (!isCompressed) {
//			in.seek(chunkOffset);
//			in.setBaseOffset(chunkOffset);
//			//Convert
//			DDSTexture texture = RW4ToTexture.rw4ToTexture(in);
//			if (texture != null) {
//				FileStreamAccessor out = new FileStreamAccessor(outPath+"\\"+Hasher.getFileName(group)
//						+"\\"+Hasher.getFileName(name)+"."+convertTo[2], "rw"); //TODO remove convertTo and use name registries
//				try {
//					texture.write(out);
//				} finally {
//					out.close();
//				}
//			}
//			in.setBaseOffset(0);
//		} else {
//			in.seek(chunkOffset);
//			ByteArrayStreamAccessor out = new ByteArrayStreamAccessor(memSize);
//			RefPackCompression.decompress(in, out);
//			DDSTexture texture = RW4ToTexture.rw4ToTexture(out);
//			if (texture != null) {
//				FileStreamAccessor outFile = new FileStreamAccessor(outPath+"\\"+Hasher.getFileName(group)
//						+"\\"+Hasher.getFileName(name)+"."+convertTo[2], "rw");
//				try {
//					texture.write(outFile);
//				} finally {
//					outFile.close();
//				}
//			}
//		}
//	}
//	public void processProp(InputStreamAccessor in, String outPath) throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException, FileStructureException, ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException {
//		if (!isCompressed) {
//			in.seek(chunkOffset);
//			in.setBaseOffset(chunkOffset);
//			//Convert
//			try (FileStreamAccessor out = new FileStreamAccessor(outPath+"\\"+Hasher.getFileName(group)
//					+"\\"+Hasher.getFileName(name)+"."+convertTo[1], "rw")) {
//				PROPMain.propToXml(in, out);
//			}
//			in.setBaseOffset(0);
//		} else {
//			ByteArrayStreamAccessor decompressOut = new ByteArrayStreamAccessor(memSize);
//			try {
//				in.seek(chunkOffset);
//				RefPackCompression.decompress(in, decompressOut);
//				try (FileStreamAccessor out = new FileStreamAccessor(outPath+"\\"+Hasher.getFileName(group)
//						+"\\"+Hasher.getFileName(name)+"."+convertTo[1], "rw")) {
//					PROPMain.propToXml(decompressOut, out);
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			} finally {
//				decompressOut.close();
//			}
//		}
//	}
//	public void processTLSA(InputStreamAccessor in, String outPath) throws IOException, FileStructureException {
//		if (!isCompressed) {
//			in.seek(chunkOffset);
//			in.setBaseOffset(chunkOffset);
//			BufferedWriter out2 = new BufferedWriter(new FileWriter(outPath+"\\"+Hasher.getFileName(group)
//					+"\\"+Hasher.getFileName(name)+"."+convertTo[0]));
//			//Convert
//			try {
//				TLSAConverter.convertTLSA(in, out2);
//			} catch (Exception e) {
//				e.printStackTrace();
//			} finally {
//				System.out.println("tlsa finished");
//				in.setBaseOffset(0);
//				out2.close();
//			}
//		} else {
//			ByteArrayStreamAccessor out = new ByteArrayStreamAccessor(memSize);
//			BufferedWriter out2 = new BufferedWriter(new FileWriter(outPath+"\\"+Hasher.getFileName(group)
//					+"\\"+Hasher.getFileName(name)+"."+convertTo[0]));
//			try {
//				in.seek(chunkOffset);
//				RefPackCompression.decompress(in, out);
//				//Convert
//				TLSAConverter.convertTLSA(out, out2);
//			} finally {
//				out.close();
//				out2.close();
//			}
//		}
//	}
	
	public void print() {
		System.out.println(Hasher.getFileName(group) + "!" + Hasher.getFileName(name) + "." + Hasher.getTypeName(type));
		System.out.println("chunkOffset: " + chunkOffset);
		System.out.println("compressedSize: " + compressedSize);
		System.out.println("memSize: " + memSize);
		System.out.println("isCompressed: " + isCompressed);
	}
	
	public static void main(String[] args) throws IOException {
		
		long time1 = System.currentTimeMillis();
		try (ByteArrayStreamAccessor arr = new ByteArrayStreamAccessor(2048 * 16)) {
		
			for (int i = 0; i < (2048 * 16)/4; i++) {
				arr.writeInt(i);
			}
			
			arr.writeToFile("E:\\Test1.rw4");
		}
		
		System.out.println(System.currentTimeMillis() - time1);
		
		
		long time2 = System.currentTimeMillis();
		
		try (FileStreamAccessor in = new FileStreamAccessor("E:\\Test2.rw4", "rw")) {
			
			for (int i = 0; i < (2048 * 16)/4; i++) {
				in.writeInt(i);
			}
		}
		
		
		System.out.println(System.currentTimeMillis() - time2);
	}
}
