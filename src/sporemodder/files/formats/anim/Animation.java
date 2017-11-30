package sporemodder.files.formats.anim;

import java.io.IOException;

import sporemodder.MainApp;
import sporemodder.files.FileStreamAccessor;
import sporemodder.files.InputStreamAccessor;
import sporemodder.utilities.Hasher;

public class Animation {
	
	private static final int kMaxPath = 260;

	private int version;
	private int animID;
	
	public void read(byte[] data) throws IOException {
		
		DataReader in = new DataReader(data);
		
		int header = in.getInt(0);
		
		if (header != 0x4D494E41) {
			throw new UnsupportedOperationException("Unknown magic value (" + Hasher.hashToHex(header, "0x") + ") in animation.");
		}
		
		// fileSize
		
		version = in.getInt(8);
		if (version > 25 || version < 6) {
			throw new UnsupportedOperationException("Bad version (" + version + ") in animation.");
		}
		
		if (version < 14) {
			//TODO: sub_9A8600
			// it's to get the real file size
		}
		
		// in.skipBytes(kMaxPath);  // the original filepath
		
		animID = in.getInt(0x110);
		// /* 114h */	uint32_t groupID;
		//TODO the things between, we are at 0x114 now
		

		// There's an array of 'count' pointers (so offsets in the file) at 'offset'
		int arrayCount = in.getInt(0x14C);
		int arrayOffset = in.getInt(0x150);
		
		
		int count = in.getInt(0x144);
		int offset = in.getInt(0x148);
		
		for (int i = 0; i < count; i++) {
			
			int blockOffset = in.getInt(offset, i);
			DataReader blockData = new DataReader(in, blockOffset);
			
			//TODO: sub_9A8550
			
			int field_DC = blockData.getInt(0xDC);
			if (field_DC <= 0) continue;
			
			for (int j = 0; j < field_DC; j++) {
				
			}
		}
	}
	
	public void print() {
		System.out.println(Hasher.getFileName(animID));
	}
	
	public static void main(String[] args) throws IOException {
		
		MainApp.init();
		
		String inputPath = "E:\\Eric\\SporeModder\\Projects\\Spore_EP1_Data\\animations~\\EP1_warrior_missile_v1.animation";
		
		try (InputStreamAccessor in = new FileStreamAccessor(inputPath, "r")) {
			
			Animation anim = new Animation();
			
			anim.read(in);
			
			anim.print();
		}
	}
}
