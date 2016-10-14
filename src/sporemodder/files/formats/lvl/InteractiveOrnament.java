package sporemodder.files.formats.lvl;

import java.io.IOException;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.FileStructure;

public class InteractiveOrnament extends FileStructure implements GameplayMarker {
	protected static final int HASH = 0xBEDADA93;
	
	public void read(InputStreamAccessor in) throws IOException {
		for (int i = 0; i < 34; i++) expect(in.readInt(), 0, "LVL-IO-0", in.getFilePointer());
	}
	
	public void print() {
	}

	@Override
	public void write(OutputStreamAccessor out) throws IOException {
		out.write(new byte[34*4]);
	}
}
