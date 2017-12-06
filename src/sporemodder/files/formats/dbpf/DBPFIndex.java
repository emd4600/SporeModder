package sporemodder.files.formats.dbpf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.FileStructure;

public class DBPFIndex extends FileStructure {
	public int groupID = -1;
	public int typeID = -1;
	public int itemSize = 28;
	public List<DBPFItem> items;
	public int itemsPos;
	
	public int dbpfType;
	
	public void read(InputStreamAccessor in, int dbpfType) throws IOException {
		int typeInt = in.readLEInt();
		itemSize = (dbpfType == DBPFHeader.TYPE_DBPF) ? 28 : 32;
		
		this.dbpfType = dbpfType;
		
		// type id
		if ((typeInt & (1 << 0)) == 1 << 0)
		{
			itemSize -= 4;
			typeID = in.readLEInt();
		}

		// group id
		if ((typeInt & (1 << 1)) == 1 << 1)
		{
			itemSize -= 4;
			groupID = in.readLEInt();
		}

		// unknown value
		if ((typeInt & (1 << 2)) == 1 << 2)
		{
			in.readLEInt();
		}
		
		itemsPos = in.getFilePointer();
	}
	
	public void write(OutputStreamAccessor out) throws IOException {
		out.writeLEInt(4);
		out.writeInt(0);
		for (DBPFItem item : items) {
			item.writeInfo(out);
		}
	}
	
	public void readItemsInfo(InputStreamAccessor in, int itemCount) throws IOException {
		items = new ArrayList<DBPFItem>(itemCount);
		
		for (int i = 0; i < itemCount; i++) {
			in.seek(itemsPos + (i * itemSize));
			DBPFItem item = new DBPFItem();
			item.key.setGroupID(groupID);
			item.key.setTypeID(typeID);
			item.readInfo(in, dbpfType);
			items.add(item);
		}
	}
}
