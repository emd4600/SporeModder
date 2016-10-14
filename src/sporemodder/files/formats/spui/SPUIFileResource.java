package sporemodder.files.formats.spui;

import java.io.IOException;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.ResourceKey;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.utilities.Hasher;

public class SPUIFileResource implements SPUIResource {
	private int fileID;
	private int typeID;
	private int groupID;
	// is it the first or the second resource type? 
	protected boolean isAtlas;
	
	public SPUIFileResource(ResourceKey key, boolean isAtlas) {
		this.isAtlas = isAtlas;
		if (key != null) {
			groupID = key.getGroupID();
			fileID = key.getInstanceID();
			typeID = key.getTypeID();
		}
	}

	public SPUIFileResource() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void read(InputStreamAccessor in, int version) throws IOException {
		fileID = in.readLEInt();
		typeID = in.readLEInt();
		groupID = in.readLEInt();
	}
	
	@Override
	public void write(OutputStreamAccessor out, int version) throws IOException {
		out.writeLEInt(fileID);
		out.writeLEInt(typeID);
		out.writeLEInt(groupID);
	}
	
	@Override
	public String getString() {
		return "FileResource " + (isAtlas ? "atlas " : "") + Hasher.getFileName(groupID) + "!" + Hasher.getFileName(fileID) + 
				"." + Hasher.getTypeName(typeID);
	}
	
	// Gets string without resource data (resource name and isAtlas)
	public String getStringSimple() {
		return Hasher.getFileName(groupID) + "!" + Hasher.getFileName(fileID) + 
				"." + Hasher.getTypeName(typeID);
	}
	
	// group!file.type
	protected void parseSimple(String str) throws IOException {
		String[] spl = str.split("\\.");
		String[] groupFile = spl[0].split("!");
		groupID = Hasher.getFileHash(groupFile[0]);
		fileID = Hasher.getFileHash(groupFile[1]);
		typeID = Hasher.getTypeHash(spl[1]);
	}
	
	@Override
	public void parse(String str) throws IOException {
		// isAtlas group!file.type
		
		String[] splits = str.split(" ");
		
		int i = 0;
		// isAtlas
		if (splits.length >= 2) {
			i = 1;
			if (splits[0].equals("atlas")) {
				isAtlas = true;
			} else {
				System.err.println("Unknwon token \"" + splits[0] + "\"");
			}
		}
		
		parseSimple(splits[i]);
	}

	@Override
	public RESOURCE_TYPE getType() {
		if (isAtlas) return SPUIResource.RESOURCE_TYPE.ATLAS;
		else return SPUIResource.RESOURCE_TYPE.IMAGE;
	}

	public int getFileID() {
		return fileID;
	}

	public void setFileID(int fileID) {
		this.fileID = fileID;
	}

	public int getTypeID() {
		return typeID;
	}

	public void setTypeID(int typeID) {
		this.typeID = typeID;
	}

	public int getGroupID() {
		return groupID;
	}

	public void setGroupID(int groupID) {
		this.groupID = groupID;
	}

	public boolean isAtlas() {
		return isAtlas;
	}

	public void setAtlas(boolean isAtlas) {
		this.isAtlas = isAtlas;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + fileID;
		result = prime * result + groupID;
		result = prime * result + (isAtlas ? 1231 : 1237);
		result = prime * result + typeID;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SPUIFileResource other = (SPUIFileResource) obj;
		if (fileID != other.fileID)
			return false;
		if (groupID != other.groupID)
			return false;
		if (isAtlas != other.isAtlas)
			return false;
		if (typeID != other.typeID)
			return false;
		return true;
	}

	@Override
	public void parse(ArgScriptCommand c) throws ArgScriptException {
		List<String> args = c.getArguments(1, 2);
		int index = 0;
		if (args.size() == 2) {
			if (args.get(index++).equals("atlas")) {
				isAtlas = true;
			}
		}
		ResourceKey key = new ResourceKey();
		key.parse(args.get(index));
		groupID = key.getGroupID();
		fileID = key.getInstanceID();
		typeID = key.getTypeID();
	}

	@Override
	public ArgScriptCommand toCommand() {
		if (isAtlas) {
			return new ArgScriptCommand("FileResource", "atlas", new ResourceKey(groupID, fileID, typeID).toString());
		} else {
			return new ArgScriptCommand("FileResource", new ResourceKey(groupID, fileID, typeID).toString());
		}
	}
	
	
//	public static void main(String[] args) {
//		String test = "block asd // comment test";
//		String test2 = "block test";
//		System.out.println(test.split("//")[0]);
//		System.out.println(test2.split("//")[0]);
//	}
}
