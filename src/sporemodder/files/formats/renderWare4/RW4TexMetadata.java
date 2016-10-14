package sporemodder.files.formats.renderWare4;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sporemodder.files.FileStructureException;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.renderWare4.D3DStateTypes.D3DRenderStateType;
import sporemodder.files.formats.renderWare4.D3DStateTypes.D3DSamplerStateType;
import sporemodder.files.formats.renderWare4.D3DStateTypes.D3DTextureStageStateType;

public class RW4TexMetadata extends RW4Section {
	public static class TextureSlot {
		public int slotNumber;
		public int sectionNumber;
		public List<D3DTextureStageStateType> stageStates = new ArrayList<D3DTextureStageStateType>();
		public List<D3DSamplerStateType> samplerStates = new ArrayList<D3DSamplerStateType>();
	}
	public static final int SKINPAINT = 0x16;
	public static final int MINERALPAINT = 0xF;
	public static final int type_code = 0x2000b;
	public static final int alignment = 16;
    public int sn1, sn2;
    public int size;
    public float[] colorMultiplier = new float[4];
    public RW4VertexFormat vertexFormat = new RW4VertexFormat(); //?
    public int unk1, unk2, unk3, unk4, unk5, unk6, unk7, unk8, unk9;
    public int materialGroup;
    public int projectionMode;
    public float tilingX, tilingY;
    public boolean usePaintTexture;
    
    public List<D3DRenderStateType> renderStates = new ArrayList<D3DRenderStateType>();
    public List<TextureSlot> textureSlots = new ArrayList<TextureSlot>();
    
	public void read(InputStreamAccessor in, List<RW4Section> sections) throws IOException {
		//TODO ALL THIS!!!
		size = in.readLEInt();
		expect(in.readLEInt(), 4, "RW4-TXM000", in.getFilePointer());
		expect(in.readLEInt(), 0x10801C, "RW4-TXM001", in.getFilePointer());
		expect(in.readLEInt(), 0x801C, "RW4-TXM002", in.getFilePointer());
		expect(in.readLEShort(), 1, "RW4-TXM003", in.getFilePointer());
		expect(in.readLEShort(), 14, "RW4-TXM004", in.getFilePointer());
		expect(in.readLEInt(), 0, "RW4-TXM005", in.getFilePointer());
		unk1 = in.readLEShort();
		
		expect(in.readLEInt(), 0x8000, "RW4-TXM006", in.getFilePointer());
		expect(in.readLEShort(), 0, "RW4-TXM007", in.getFilePointer());
		vertexFormat.read(in, sections);
		
		expect(in.readLEInt(), 3, "RW4-TXM008", in.getFilePointer());
		expect(in.readLEInt(), 4, "RW4-TXM009", in.getFilePointer());
		unk2 = in.readLEInt();
		expect(in.readLEInt(), 4, "RW4-TXM010", in.getFilePointer());
		expect(in.readLEInt(), 20, "RW4-TXM011", in.getFilePointer());
		unk3 = in.readLEInt();
		unk4 = in.readLEInt();
		expect(in.readLEInt(), 0, "RW4-TXM012", in.getFilePointer());
		expect(in.readLEInt(), 0x400000, "RW4-TXM013", in.getFilePointer());
		unk5 = in.readLEShort();
		expect(in.readLEShort(), 0x80, "RW4-TXM014", in.getFilePointer());
		unk6 = in.readByte();
		expect(in.readByte(), 2, "RW4-TXM015", in.getFilePointer());
		expect(in.readLEShort(), 0, "RW4-TXM016", in.getFilePointer());
		
		if (unk6 == SKINPAINT) { // skinpaint
			unk7 = in.readLEInt();
			unk8 = in.readByte();
			unk9 = in.readLEInt();
			expect(in.readLEInt(), 0, "RW4-TXM017-1", in.getFilePointer());
			expect(in.readLEShort(), 0, "RW4-TXM017-2", in.getFilePointer());
			expect(in.readByte(), 0, "RW4-TXM017-3", in.getFilePointer());
			
		} else if (unk6 == MINERALPAINT) {
			expect(in.readLEInt(), 8, "RW4-TXM017-MP-1", in.getFilePointer());
			materialGroup = in.readInt();
			expect(in.readLEInt(), 0x00C7E300, "RW4-TXM017-MP-2", in.getFilePointer());
			
			int flags = in.readLEInt();
			if (flags == 0x0211) { // textured material
				usePaintTexture = true;
				expect(in.readLEInt(), 0x14, "RW4-TXM017-MP-3", in.getFilePointer());
				projectionMode = in.readInt();
				tilingX = in.readLEFloat();
				tilingY = in.readLEFloat();
				expect(in.readLEInt(), 0, "RW4-TXM017-MP-4", in.getFilePointer());
				
			} else if (flags == 0x0244) { // textured material
				usePaintTexture = false;
				expect(in.readLEInt(), 4, "RW4-TXM017-MP-5", in.getFilePointer());
				
			} else {
				addError("RW4-TXM017-MP-6", in.getFilePointer());
			}
			expect(in.readLEInt(), 0, "RW4-TXM017-MP-7", in.getFilePointer());
			expect(in.readLEInt(), 0, "RW4-TXM017-MP-8", in.getFilePointer());
			expect(in.readLEInt(), 0, "RW4-TXM017-MP-9", in.getFilePointer());
			
			
		} else {
			addError("RW4-TXM017", in.getFilePointer());
		}
		
		colorMultiplier[0] = in.readLEFloat();
		colorMultiplier[1] = in.readLEFloat();
		colorMultiplier[2] = in.readLEFloat();
		colorMultiplier[3] = in.readLEFloat();
		expect(in.readByte(), 1, "RW4-TXM018", in.getFilePointer()); //maybe a boolean
		int unk = in.readByte();  // only 1 in Object model materials 
		expect(in.readShort(), 0, "RW4-TXM018-1", in.getFilePointer());
		expect(in.readInt(), 0, "RW4-TXM019", in.getFilePointer());
		expect(in.readInt(), 0, "RW4-TXM020", in.getFilePointer());
		expect(in.readInt(), 0, "RW4-TXM021", in.getFilePointer());
		expect(in.readInt(), 0, "RW4-TXM022", in.getFilePointer());
		expect(in.readByte(), 0, "RW4-TXM023", in.getFilePointer());
		System.out.println(in.getFilePointer());
		
		int id = in.readLEInt();
		while (id != -1) {
			//System.out.println(id);
			D3DRenderStateType state = D3DRenderStateType.getById(id);
			state.value = in.readLEInt();
			renderStates.add(state);
			id = in.readLEInt();
		}
		expect(in.readLEInt(), -1, "RW4-TXM024", in.getFilePointer());
		
		expect(in.readLEInt(), -1, "RW4-TXM025", in.getFilePointer());
		while (true) {
			TextureSlot slot = new TextureSlot();
			slot.slotNumber = in.readLEInt();
			if (slot.slotNumber == -1) {
				break;
			}
			slot.sectionNumber = in.readLEInt();
			
			int number = in.readLEInt();
			if (number == 9) {
				//?
				expect(in.readLEInt(), 1, "RW4-TXM026", in.getFilePointer());
			} else if (number != 0x3F) {
				addError("RW4-TXM026", in.getFilePointer());
			}
			
			int tssID = in.readLEInt();
			while (tssID != -1) {
				D3DTextureStageStateType state = D3DTextureStageStateType.getById(tssID);
				state.value = in.readLEInt();
				slot.stageStates.add(state);
				tssID = in.readLEInt();
				if (tssID == 1) {
					tssID = in.readLEInt(); //?
					//TODO break;
				}
			}
			int num = in.readLEInt();
			if (num == 0) {
				expect(in.readLEInt(), -1, "RW4-TXM027-1", in.getFilePointer());
				break;
			} else {
				expect(num, 0x73, "RW4-TXM027", in.getFilePointer());
			}
			int ssID = in.readLEInt();
			while (ssID != -1) {
				D3DSamplerStateType state = D3DSamplerStateType.getById(ssID);
				state.value = in.readLEInt();
				slot.samplerStates.add(state);
				ssID = in.readLEInt();
			}
			
			textureSlots.add(slot);
			//break; //TODO this only reads one texture slot
		}
		
//		in.expect(in.readLEInt(), 0xE, "RW4-TXM024; Wrong in pos: " + in.getFilePointer());
//		in.expect(in.readLEInt(), 0x1, "RW4-TXM025; Wrong in pos: " + in.getFilePointer());
//		in.expect(in.readLEInt(), 0xF, "RW4-TXM026; Wrong in pos: " + in.getFilePointer());
//		in.expect(in.readInt(), 0, "RW4-TXM027; Wrong in pos: " + in.getFilePointer());
//		in.expect(in.readLEInt(), 0x16, "RW4-TXM028; Wrong in pos: " + in.getFilePointer());
//		in.expect(in.readLEInt(), 0x2, "RW4-TXM029; Wrong in pos: " + in.getFilePointer());
//		in.expect(in.readLEInt(), 0x17, "RW4-TXM030; Wrong in pos: " + in.getFilePointer());
//		in.expect(in.readLEInt(), 0x4, "RW4-TXM031; Wrong in pos: " + in.getFilePointer());
//		in.expect(in.readLEInt(), 0x1B, "RW4-TXM031; Wrong in pos: " + in.getFilePointer());
		//in.expect(in.readInt(), 0, "RW4-TXM032; Wrong in pos: " + in.getFilePointer());
		//sn1 = in.readLEInt(); 
	}
	
//	@Deprecated
//	public int[] readTexturesections.get()InputStreamAccessor in) throws IOException {
//		//TODO TexMetadata....
//		in.seek(sectionInfo.pos + sectionInfo.size - 164);
//		int num = 1;
//		if (in.readByte() == -1) {
//			for (int i = 0; i < 11; i++) {
//				if (in.readByte() != -1) {
//					in.seek(sectionInfo.pos + sectionInfo.size - 208);
//					if (in.readByte() == -1) {
//						for (int f = 0; f < 11; f++) {
//							if (in.readByte() != -1) {
//								throw new IOException("TexMetadata TODO Exception");
//							}
//						}
//					} else {
//						throw new IOException("TexMetadata TODO Exception");
//					}
//					num = 2;
//					break;
//				}
//			}
//		}
//		int[] textures = new int[num];
////		in.expect(in.readLEInt(), 0, "RW4-TXM001");
////		textures[0] = in.readLEInt();
////		in.expect(in.readLEInt(), 63, "RW4-TXM001-"); in.expect(in.readLEInt(), 1, "RW4-TXM001--");
////		in.expect(in.readLEInt(), 4, "RW4-TXM001---"); in.expect(in.readLEInt(), 2, "RW4-TXM001----");
////		in.expect(in.readLEInt(), 2, "RW4-TXM001-----"); in.expect(in.readLEInt(), 3, "RW4-TXM001------");
////		in.expect(in.readLEInt(), 0, "RW4-TXM001-------"); in.expect(in.readLEInt(), 4, "RW4-TXM001--------");
////		in.expect(in.readLEInt(), 4, "RW4-TXM001"); in.expect(in.readLEInt(), 5, "RW4-TXM001");
////		in.expect(in.readLEInt(), 2, "RW4-TXM001"); in.expect(in.readLEInt(), 6, "RW4-TXM001");
////		in.expect(in.readLEInt(), 0, "RW4-TXM001"); in.expect(in.readLEInt(), -1, "RW4-TXM001");
////		in.expect(in.readLEInt(), 115, "RW4-TXM001"); in.expect(in.readLEInt(), 1, "RW4-TXM001");
////		in.expect(in.readLEInt(), 1, "RW4-TXM001"); in.expect(in.readLEInt(), 2, "RW4-TXM001");
////		in.expect(in.readLEInt(), 1, "RW4-TXM001"); in.expect(in.readLEInt(), 5, "RW4-TXM001");
////		in.expect(in.readLEInt(), 2, "RW4-TXM001"); in.expect(in.readLEInt(), 6, "RW4-TXM001");
////		in.expect(in.readLEInt(), 2, "RW4-TXM001"); in.expect(in.readLEInt(), 7, "RW4-TXM001");
////		in.expect(in.readLEInt(), 1, "RW4-TXM001"); in.expect(in.readLEInt(), -1, "RW4-TXM001");
////		in.expect(in.readLEInt(), 1, "RW4-TXM001");
////		if (num == 2) {
////			textures[1] = in.readLEInt();
////		}
//		return textures;
//	}
	@Override
	public void print() {
		System.out.println("### " + this.getClass().getSimpleName() + " section " + this.sectionInfo.number);
		System.out.println("\tsize: " + size);
		System.out.println("\tunk1: " + unk1 + "\tunk2: " + unk2);
		System.out.println("\tunk3: " + unk3 + "\tunk4: " + unk4);
		System.out.println("\tunk5: " + unk5 + "\tunk6: " + unk6);
		System.out.println("\tunk7: " + unk7 + "\tunk8: " + unk8);
		System.out.println("\tunk9: " + unk9);
		System.out.println("\tmaterialGroup: " + materialGroup);
		System.out.println("\tprojectionMode: " + projectionMode);
		System.out.println("\ttilingX: " + tilingX);
		System.out.println("\ttilingY: " + tilingY);
		
		System.out.println("\trenderStates: ");
		for (D3DRenderStateType state : renderStates) {
			System.out.println("\t\t" + state.toString() + "\t" + state.getValueToString());
		}
		
		for (TextureSlot ts : textureSlots) {
			System.out.println();
			System.out.println("\t\tslotNumber: " + ts.slotNumber);
			System.out.println("\t\tsectionNumber: " + ts.sectionNumber);
			for (D3DTextureStageStateType state : ts.stageStates) {
				System.out.println("\t\t\t" + state.toString() + "\t" + state.getValueToString());
			}
			for (D3DSamplerStateType state : ts.samplerStates) {
				System.out.println("\t\t\t" + state.toString() + "\t" + state.getValueToString());
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
	
	@Override
	public void write(OutputStreamAccessor out, List<RW4Section> sections) throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	
	public static void main(String[] args) {
		String path = "E:\\Eric\\SporeMaster 2.0 beta\\spore.unpacked\\tribal_outfit_parts~";
		String[] files = new File(path).list(new FilenameFilter() {
			@Override
			public boolean accept(File arg0, String arg1) {
				return arg1.endsWith(".prop.xml");
			}
		});
		
		for (int i = 0; i < files.length/4; i++) {
			System.out.println("Hasher.hashFNV(\"" + files[i*4 + 0].substring(0, files[i*4 + 0].lastIndexOf(".prop.xml")) + "\"), " + 
					"Hasher.hashFNV(\"" + files[i*4 + 1].substring(0, files[i*4 + 1].lastIndexOf(".prop.xml")) + "\"), " + 
					"Hasher.hashFNV(\"" + files[i*4 + 2].substring(0, files[i*4 + 2].lastIndexOf(".prop.xml")) + "\"), " + 
					"Hasher.hashFNV(\"" + files[i*4 + 3].substring(0, files[i*4 + 3].lastIndexOf(".prop.xml")) + "\"), ");
		}
	}
}
