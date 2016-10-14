package sporemodder.files.formats.lvl;

import java.io.IOException;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;

public interface GameplayMarker {
	public void read(InputStreamAccessor in) throws IOException;
	public void write(OutputStreamAccessor out) throws IOException;
	public void print();
}
