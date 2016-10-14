package sporemodder.files.formats.renderWare4;

import java.io.IOException;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.dds.DDSTexture;
import sporemodder.utilities.Hasher;

public class RW4Texture extends RW4Section {
	public static final int type_code = 0x20003;
	public static final int alignment = 4;
	public int unk1; //< dead pointer?
	public int textureType;
	public int width, height;
	public int mipmapInfo;
	public int texData_secNum;
	public RW4Buffer texData;
	@Override
	public void read(InputStreamAccessor in, List<RW4Section> sections) throws IOException {
		textureType = in.readLEInt();
		expect(in.readLEInt(), 8, "RW4-T001", in.getFilePointer());
		unk1 = in.readLEInt();
//		System.out.println("texture unk: " + Hasher.getPropertyIDString(unk1));
		width = in.readLEShort();
		height = in.readLEShort();
		mipmapInfo = in.readLEInt();
		expect(in.readInt(), 0, "RW4-T002", in.getFilePointer());
		expect(in.readInt(), 0, "RW4-T003", in.getFilePointer());
		texData_secNum = in.readLEInt();
		texData = (RW4Buffer) sections.get(texData_secNum);
	}
	@Override
	public void write(OutputStreamAccessor out, List<RW4Section> sections) throws IOException {
		if (sections != null) texData_secNum = sections.indexOf(texData);
		out.writeLEInt(textureType);
		out.writeLEInt(8);
		out.writeLEInt(unk1);
		out.writeLEShort(width);
		out.writeLEShort(height);
		out.writeLEInt(mipmapInfo);
		out.writeInt(0); out.writeInt(0);
		out.writeLEInt(texData_secNum);
	}
	@Override
	public void print() {
		System.out.println("### " + this.getClass().getSimpleName() + " section " + this.sectionInfo.number);
		System.out.println("\ttexture type: " + Hasher.hashToHex(textureType));
		System.out.println("\tdimensions: " + width + "x" + height + "px");
		System.out.println("\tdata section: " + texData_secNum);
		System.out.println("\tmipmap info: " + mipmapInfo + "\t" + Hasher.hashToHex(mipmapInfo));
		System.out.println("\tunk1: " + unk1 + "\t" + Hasher.hashToHex(unk1));
	}
	
	public DDSTexture toDDSTexture() {
		DDSTexture tex = new DDSTexture(height, width, mipmapInfo / 0x100, textureType, texData.data);
		
		return tex;
	}
	
	public void fromDDSTexture(DDSTexture texture) {
		width = texture.getWidth();
		height = texture.getHeight();
		textureType = texture.getTextureType();
		mipmapInfo = texture.getMipmapCount() * 0x100;
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
