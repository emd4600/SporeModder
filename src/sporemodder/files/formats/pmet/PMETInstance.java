package sporemodder.files.formats.pmet;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import sporemodder.files.InputStreamAccessor;
import sporemodder.utilities.Hasher;

public class PMETInstance {
	public int[] tgi = new int[3]; //folder, name, type
	public long date;
	public long downloadDate;
	public String downloadFDate;
	public String fDate;
	public String author;
	public String name;
	public String description;
	public String[] tags;
	public CreationType type;
	
	public enum CreationType {
		ADVENTURE(0x366A930D, new int[] {0x408A0000, 0x408A0001, 0x408A0002, 0x408A0003, 0x408A0004}),
		CREATURE(0x2B978C46, new int[] {0x40626200, 0x40626201, 0x40627100, 0x40622900, 0x40622901, 0x4062E500}),
		BUILDING(0x2399BE55, new int[] {0x40636200, 0x40636201, 0x40636202, 0x40632900, 0x40632901, 0x40632902}),
		VEHICLE(0x24682294, new int[] {0x40646200, 0x40646201, 0x40646202, 0x40646203, 0x40642900, 0x40642901, 0x40642902}),
		CELL(0x3D97A8E4, new int[] {0x40616200, 0x40616201, 0x40616202}), //TODO
		UFO(0x476A98C7, new int[] {0x40656200, 0x40656201, 0x40656202, 0x40656203, 0x40652900, 0x40652901, 0x40652902}),
		PLANT(0x438F6347, new int[] {0x40666200, 0x40666201, 0x40666202, 0x40666203, 0x40667100, 0x40662801, 
				0x40662800, 0x40662900, 0x40662901, 0x40662C00, 0x40662C01});
		
		public int type = -1;
		public int[] folders;
		private CreationType(int typeId) {
			type = typeId;
		}
		private CreationType(int typeId, int[] folders) {
			type = typeId;
			this.folders = folders;
		}
		public static CreationType getByType(int typeId) {
			for (CreationType t : CreationType.values()) {
				if (t.type == typeId) {
					return t;
				}
			}
			return null;
		}
	}
	
	public void read(InputStreamAccessor in) throws IOException {
		
		in.skipBytes(12); //GUID
		tgi[2] = in.readInt();
		tgi[0] = in.readInt();
		tgi[1] = in.readInt();
		type = PMETInstance.CreationType.getByType(tgi[2]);
		in.skipBytes(28); //?
		
		date = (in.readLong() - 62135773201l) * 1000; //62125920000l   15:53           4294967296
		DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy   HH:mm:ss");
		fDate = formatter.format(new Date(date));
		
		downloadDate = (in.readLong() - 62135773201l) * 1000; //62125920000l   15:53           4294967296
		DateFormat formatter2 = new SimpleDateFormat("dd-MM-yyyy   HH:mm:ss");
		downloadFDate = formatter2.format(new Date(downloadDate));
		
		in.skipBytes(12); //?
		
		author = in.readLEString16(in.readInt());
		name = in.readLEString16(in.readInt());
		description = in.readLEString16(in.readInt());
		
		int tagCount = in.readInt();
		tags = new String[tagCount];
		for (int i = 0; i < tagCount; i++) {
			tags[i] = in.readString8(in.readInt());
		}
	}
	
	public void print() {
		System.out.println("Creature " + name + ", made by " + author + " at " + fDate + ":");
		System.out.println("\t" + description);
		for (String tag : tags) {
			System.out.print(tag + ", ");
		}
		System.out.println();
		System.out.println(Hasher.getFileName(tgi[0]) + "/" + Hasher.getFileName(tgi[1]) +
				"." + Hasher.getTypeName(tgi[2]));
		System.out.println("Type " + type);
		System.out.println();
	}
}
