package sporemodder.files.formats.dbpf;

import java.io.IOException;

import sporemodder.files.ByteArrayStreamAccessor;
import sporemodder.files.FileStreamAccessor;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.ReadWriteStreamAccessor;
import sporemodder.files.formats.FileStructure;
import sporemodder.files.formats.ResourceKey;
import sporemodder.utilities.Hasher;

public class DBPFItem extends FileStructure {
	public static boolean DEBUG = false;
	
	public boolean isCompressed;
	public int chunkOffset, memSize, compressedSize;
	public String path;
	public final ResourceKey key = new ResourceKey();
	public short unk = 1;
	
	@Override
	public String toString() {
		return Hasher.getFileName(key.getGroupID()) + "\\" + Hasher.getFileName(key.getInstanceID()) + "." + Hasher.getTypeName(key.getTypeID());
	}
	
	public void readInfo(InputStreamAccessor in, int dbpfType) throws IOException {
		if (key.getTypeID() == -1) {
			key.setTypeID(in.readLEInt());
		}
		if (key.getGroupID() == -1) {
			key.setGroupID(in.readLEInt());
		}
		key.setInstanceID(in.readLEInt());
		
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
		out.writeLEInt(key.getTypeID());
		out.writeLEInt(key.getGroupID());
		out.writeLEInt(key.getInstanceID());
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
	
	public void print() {
		System.out.println(Hasher.getFileName(key.getGroupID()) + "!" + Hasher.getFileName(key.getInstanceID()) + "." + Hasher.getTypeName(key.getTypeID()));
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
