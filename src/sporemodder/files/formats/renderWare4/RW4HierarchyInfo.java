package sporemodder.files.formats.renderWare4;

import java.io.IOException;
import java.util.List;

import sporemodder.files.FileStructureException;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.utilities.Hasher;

public class RW4HierarchyInfo extends RW4Section {
	public static final int type_code = 0x70002;
	public static final int alignment = 4;
	public class Item {
		public int index;
		public int name_fnv;// e.g. "joint1".fnv()
		public int flags;// probably; values are all 0 to 3.   &1 might be "leaf"
		public Item parent;
		public int parentIndex;
		public String nameStr;
		public void print() {
			if (parent != null)
				System.out.println("\t" + nameStr + "\t" + flags + "\t"+parent.nameStr);
			else System.out.println("\t" + nameStr + "\t" + flags);
		}
	}
	public Item[] items;
	public int id;// hash or guid, referenced by Anim to specify which bones to animate?
	public String idStr;
	
	@Override
	public void read(InputStreamAccessor in, List<RW4Section> sections) throws IOException {
		int p2 = in.readLEInt();
		int p3 = in.readLEInt();
		int p1 = in.readLEInt();
		int c1 = in.readLEInt();
		id = in.readLEInt();
		idStr = Hasher.getFileName(id);
		
		expect(in.readLEInt(), c1, "RW4-HI001", in.getFilePointer());
		if (p1 != in.getFilePointer()) System.out.println("RW4-HI002; Wrong pos: " + in.getFilePointer());
		if (p2 != p1 + (4*c1)) System.out.println("RW4-HI003; Wrong pos: " + in.getFilePointer());
		if (p3 != p1 + (8*c1)) System.out.println("RW4-HI004; Wrong pos: " + in.getFilePointer());
		items = new Item[c1];
		for (int i = 0; i < c1; i++) {
			items[i] = new Item();
			items[i].index = i;
			items[i].name_fnv = in.readLEInt();
			items[i].nameStr = Hasher.getFileName(items[i].name_fnv);
		}
		for (int i = 0; i < c1; i++) {
			items[i].flags = in.readLEInt();
		}
		for (int i = 0; i < c1; i++) {
			int pind = in.readLEInt();
            if (pind == -1) {
            	items[i].parent = null;
            	items[i].parentIndex = -1;
            } else {
            	items[i].parent = items[pind];
            	items[i].parentIndex = pind;
            }
		}
	}
	
	@Override
	public void write(OutputStreamAccessor out, List<RW4Section> sections) throws IOException {
		int pos = out.getFilePointer() + 24;
		out.writeLEInt(pos + 4 * items.length);
		out.writeLEInt(pos + 8 * items.length);
		out.writeLEInt(pos);
		out.writeLEInt(items.length);
		out.writeLEInt(id);
		out.writeLEInt(items.length);
		for (Item item : items) {
			out.writeLEInt(item.name_fnv);
		}
		for (Item item : items) {
			out.writeLEInt(item.flags);
		}
		for (Item item : items) {
			out.writeLEInt(item.parentIndex);
		}
	}
	
	@Override
	public void print() {
		System.out.println("### " + this.getClass().getSimpleName() + " section " + this.sectionInfo.number);
		System.out.println("\tid: " + idStr);
		for (Item i : items) {
			i.print();
		}
	}
	
	public void checkFlags() throws FileStructureException {
		for (Item i : items) {
			if (i.flags == 1) {
				if (i.parent == null) {
					throw new FileStructureException("　　Item " + i.nameStr + " flags 1 doesn't have parent");
				}
			}
			if (i.parent != null) {
				if (i.parent.flags == 1) {
					throw new FileStructureException("　　Item " + i.parent.nameStr + " flags 1 has children");
				}
			}
			if (i.flags == 3) {
				if (i.parent == null) {
					throw new FileStructureException("　　Item " + i.nameStr + " flags 3 doesn't have parent");
				}
			}
		}
	}
	
	@Override
	public int getSectionTypeCode() {
		return type_code;
	}
	@Override
	public int getSectionAlignment() {
		return alignment;
	}
}
