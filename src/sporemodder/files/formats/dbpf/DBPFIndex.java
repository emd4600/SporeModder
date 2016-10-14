package sporemodder.files.formats.dbpf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.FileStructure;

public class DBPFIndex extends FileStructure {
	public enum IndexTypes {TYPE, TYPE_GROUP, ALL}
	public IndexTypes indexType = IndexTypes.ALL;
	public int group = -1;
	public int type = -1;
	public int itemSize = 28;
	public List<DBPFItem> items;
	public int itemsPos;
	
	public void read(InputStreamAccessor in, int dbpfType) throws IOException {
		int typeInt = in.readLEInt();
		if (typeInt == 4) {
			indexType = IndexTypes.ALL;
			itemSize = (dbpfType == DBPFHeader.TYPE_DBPF) ? 28 : 32;
		} else if (typeInt == 5 || typeInt == 6) {
			indexType = IndexTypes.TYPE;
			itemSize = 24;
			type = in.readLEInt();
		} else if (typeInt == 7) {
			indexType = IndexTypes.TYPE_GROUP;
			itemSize = 20;
			type = in.readLEInt();
			group = in.readLEInt();
		} else {
			addError("DBPF-I001", in.getFilePointer());
		}
		in.skipBytes(4);
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
			item.group = group;
			item.type = type;
			item.readInfo(in, type);
			items.add(item);
		}
	}
}
