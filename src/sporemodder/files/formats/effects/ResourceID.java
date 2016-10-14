package sporemodder.files.formats.effects;

import java.io.IOException;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.utilities.Hasher;

public class ResourceID {
	private int groupID = -1;
	private int nameID = -1;
	
	public ResourceID(int groupID, int nameID) {
		this.groupID = groupID;
		this.nameID = nameID;
	}
	
	public ResourceID(ResourceID other) {
		if (other != null) {
			this.groupID = other.groupID;
			this.nameID = other.nameID;
		}
	}
	
	public ResourceID() {}
	
	public ResourceID(InputStreamAccessor in) throws IOException {
		read(in);
	}
	
	public ResourceID(String str) {
		parse(str);
	}
	
	public void copy(ResourceID other) {
		if (other != null) {
			this.groupID = other.groupID;
			this.nameID = other.nameID;
		}
	}
	
	public boolean compare(ResourceID other) {
		return groupID == other.groupID && nameID == other.nameID;
	}
	
	public void read(InputStreamAccessor in) throws IOException {
		groupID = in.readInt();
		nameID = in.readInt();
	}
	
	public void flip() {
		int old = nameID;
		nameID = groupID;
		groupID = old;
	}
	
	public void write(OutputStreamAccessor out) throws IOException {
		out.writeInt(groupID);
		out.writeInt(nameID);
	}

	public int getGroupID() {
		return groupID;
	}

	public int getNameID() {
		return nameID;
	}

	public void setGroupID(int groupID) {
		this.groupID = groupID;
	}

	public void setNameID(int nameID) {
		this.nameID = nameID;
	}
	
	public boolean isDefault() {
		return groupID == -1 && nameID == -1;
	}
	
	public boolean isZero() {
		return groupID == 0 && nameID == 0;
	}
	
	@Override
	public String toString() {
		// this could be confusing
		/*if (groupID == 0 && nameID == 0) {
			return "terrain";
		}
		else*/ if (groupID == 1 && nameID == 0) {
			return "water";
		}
		
		StringBuilder sb = new StringBuilder();
		if (groupID != 0) {
			sb.append(Hasher.getFileName(groupID));
			sb.append("!");
		}
		sb.append(Hasher.getFileName(nameID));
		
		return sb.toString();
	}
	
	public void parse(String str) {
		String[] splits = str.split("!", 2);
		
		if (splits.length == 1) {
			groupID = 0;
			nameID = Hasher.getFileHash(splits[0]);
		}
		else {
			groupID = Hasher.getFileHash(splits[0]);
			nameID = Hasher.getFileHash(splits[1]);
		}
	}
	
	// allows special case 'terrain' and 'water'
	public void parseSpecial(String str) throws IOException {
		if (str.equals("terrain")) {
			groupID = 0;
			nameID = 0;
		}
		else if (str.equals("water")) {
			groupID = 1;
			nameID = 0;
		}
		else {
			String[] splits = str.split("!", 2);
			
			if (splits.length == 1) {
				groupID = 0;
				nameID = Hasher.getFileHash(splits[0]);
			}
			else {
				groupID = Hasher.getFileHash(splits[0]);
				nameID = Hasher.getFileHash(splits[1]);
			}
		}
		
	}
}
