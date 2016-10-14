package sporemodder.files.formats.dbpf;

import java.io.IOException;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.FileStructure;

public class DBPFHeader extends FileStructure {
	public int majVersion = 3;
	public int minVersion = 0;
	public int indMajVersion = 0; 
	public int indMinVersion = 3;
	public int indCount, indOffset, indSize;
	public static final int TYPE_DBPF = 0x46504244;
	public static final int TYPE_DBBF = 0x46424244;
	public int type;
	public void readDBPF(InputStreamAccessor in) throws IOException {
		majVersion = in.readLEInt();
		minVersion = in.readLEInt();
		
		in.skipBytes(20);
		indMajVersion = in.readLEInt();
		indCount = in.readLEInt();
		in.skipBytes(4);
		indSize = in.readLEInt();
		in.skipBytes(12);
		indMinVersion = in.readLEInt();
		indOffset = in.readLEInt();
		
		in.seek(indOffset);
	}
	
	public void readDBBF(InputStreamAccessor in) throws IOException {
		majVersion = in.readLEInt();
		minVersion = in.readLEInt();
		
		in.skipBytes(20);
		indMajVersion = in.readLEInt();
		indCount = in.readLEInt();
		indSize = in.readLEInt();
		in.skipBytes(8);
		indMinVersion = in.readLEInt();
		indOffset = in.readLEInt();
		
		in.seek(indOffset);
	}
	
	public void read(InputStreamAccessor in) throws IOException {
		int magic = in.readLEInt();
		if (magic == TYPE_DBPF) {
			type = TYPE_DBPF;
			readDBPF(in);
		} else if (magic == TYPE_DBBF) {
			type = TYPE_DBBF;
			readDBBF(in);
		} else {
			addError("DBPF-H001", in.getFilePointer());
		}
	}
	
	public void print() {
		System.out.println((type == TYPE_DBPF ? "DBPF" : "DBBF") + " v" + majVersion + "." + minVersion);
		System.out.println("Num entries: " + indCount);
		System.out.println("Index size: " + indSize);
		System.out.println("Index version: " + indMajVersion + "." + indMinVersion);
		System.out.println("Index offset: " + indOffset);
	}
	
	public void write(OutputStreamAccessor out) throws IOException {
		out.writeLEInt(TYPE_DBPF);
		out.writeLEInt(majVersion);
		out.writeLEInt(minVersion);
		out.writePadding(20); // ?
		out.writeLEInt(indMajVersion);
		out.writeLEInt(indCount);
		out.writePadding(4);
		out.writeLEInt(indSize);
		out.writePadding(12);
		out.writeLEInt(indMinVersion);
		out.writeLEInt(indOffset);
		out.writePadding(28);
	}
}
