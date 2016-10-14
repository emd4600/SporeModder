package sporemodder.files.formats.pdr;

import java.io.IOException;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.formats.FileStructure;

public class PDRMain extends FileStructure {
	public int unk;
	public int entryCount;
	public void read(InputStreamAccessor in) throws IOException {
		expect(in.readInt(), 0x45415044, "PDR-H001", in.getFilePointer());
		unk = in.readLEInt(); //?
		expect(in.readLEInt(), 2, "PDR-H002", in.getFilePointer());
		expect(in.readLEInt(), in.length(), "PDR-H003", in.getFilePointer());
		entryCount = in.readLEInt();
		expect(in.readLEInt(), 0, "PDR-H004", in.getFilePointer());
	}
}
