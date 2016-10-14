package sporemodder.files.formats.pdr;

import java.io.IOException;

import sporemodder.files.FileStreamAccessor;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.StreamAccessor;
import sporemodder.files.formats.FileStructure;

public class PDRObject extends FileStructure {
	private String name = null;
	private String fullName = null;
	private String extension = null;
	private int size;
	private byte[] content;
	
	//TODO This is not correctly supported
	public void read(InputStreamAccessor in, String path) throws IOException {
		int nameLength = in.readLEInt();
		byte[] nameBytes = new byte[nameLength];
		in.read(nameBytes);
		name = new String(nameBytes).trim();
		
		byte[] extensionBytes = new byte[4];
		in.read(extensionBytes);
		extension = new String(extensionBytes).trim();
		
		fullName = name + "." + extension;
		
		size = in.readLEInt();
		content = new byte[size];
		in.read(content);
		
		System.out.println("name: " + fullName);
		System.out.println("size: " + size);
		
		FileStreamAccessor out = new FileStreamAccessor(path + fullName, "rw", true);
		out.write(content);
		out.close();
	}
}
