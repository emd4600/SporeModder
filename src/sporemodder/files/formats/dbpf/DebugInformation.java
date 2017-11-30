package sporemodder.files.formats.dbpf;

import java.io.IOException;

import sporemodder.files.MemoryOutputStream;
import sporemodder.files.formats.prop.ArrayProperty;
import sporemodder.files.formats.prop.PROPMain;
import sporemodder.files.formats.prop.PropertyKey;
import sporemodder.files.formats.prop.PropertyString16;

public class DebugInformation {
	
	public static final String FOLDER_NAME = "_SporeModder";

	private String projectName;
	private String inputPath;
	
	private final ArrayProperty<PropertyString16> fileNames = new ArrayProperty<PropertyString16>("modFilePaths", PropertyString16.class);
	private final ArrayProperty<PropertyKey> fileKeys = new ArrayProperty<PropertyKey>("modFileKeys", PropertyKey.class);
	
	public DebugInformation(String projectName, String inputPath) {
		this.projectName = projectName;
		this.inputPath = inputPath;
	}
	
	public void addFile(String folderName, String fileName, int groupID, int instanceID, int typeID) {
		
		fileNames.addValue(new PropertyString16(null, folderName + "\\" + fileName));
		fileKeys.addValue(new PropertyKey(null, groupID, instanceID, typeID));
	}
	
	public void saveInformation(DBPFPackingTask dbpfTask) throws IOException {
		
		DBPFItem item = new DBPFItem();
		
		item.key.setGroupID(FOLDER_NAME);
		item.key.setInstanceID(projectName);
		item.key.setTypeID("prop");
		
		try (MemoryOutputStream stream = new MemoryOutputStream()) {
			
			PROPMain prop = new PROPMain();
			prop.add(new PropertyString16("modDebugPath", inputPath));
			prop.add(fileNames);
			prop.add(fileKeys);
			
			prop.write(stream);
			
			dbpfTask.writeFile(item, stream.getRawData(), stream.length());
			dbpfTask.addFile(item);
		}
		
	}
}
