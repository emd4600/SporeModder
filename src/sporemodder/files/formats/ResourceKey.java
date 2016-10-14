package sporemodder.files.formats;

import sporemodder.utilities.Hasher;

public class ResourceKey {
	
	private int groupID = -1;
	private int instanceID = -1;
	private int typeID = -1;
	
	public ResourceKey() {
		
	}
	
	public ResourceKey(ResourceKey other) {
		copy(other);
	}
	
	
	public ResourceKey(int groupID, int instanceID, int typeID) {
		this.groupID = groupID;
		this.instanceID = instanceID;
		this.typeID = typeID;
	}
	
	public ResourceKey(String groupID, String instanceID, String typeID) {
		this.groupID = Hasher.getFileHash(groupID);
		this.instanceID = Hasher.getFileHash(instanceID);
		this.typeID = Hasher.getTypeHash(typeID);
	}
	
	public void copy(ResourceKey other) {
		groupID = other.groupID;
		instanceID = other.instanceID;
		typeID = other.typeID;
	}
	
	@Override
	public String toString() {
		return Hasher.getFileName(groupID) + "!" + Hasher.getFileName(instanceID) + "." + Hasher.getTypeName(typeID);
	}
	
	public int getGroupID() {
		return groupID;
	}
	public int getInstanceID() {
		return instanceID;
	}
	public int getTypeID() {
		return typeID;
	}
	
	public void setGroupID(int groupID) {
		this.groupID = groupID;
	}
	public void setGroupID(String groupID) {
		this.groupID = Hasher.getFileHash(groupID);
	}
	
	public void setInstanceID(int instanceID) {
		this.instanceID = instanceID;
	}
	public void setInstanceID(String instanceID) {
		this.instanceID = Hasher.getFileHash(instanceID);
	}
	
	public void setTypeID(int typeID) {
		this.typeID = typeID;
	}
	public void setTypeID(String typeID) {
		this.typeID = Hasher.getTypeHash(typeID);
	}

	public void parse(String str) {
		String[] spl = str.split("\\.");
		String[] groupFile = spl[0].split("!");
		groupID = Hasher.getFileHash(groupFile[0]);
		instanceID = Hasher.getFileHash(groupFile[1]);
		typeID = Hasher.getTypeHash(spl[1]);
	}
	
}
