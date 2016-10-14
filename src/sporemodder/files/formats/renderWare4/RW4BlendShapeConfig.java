package sporemodder.files.formats.renderWare4;

import java.io.IOException;
import java.util.List;

import sporemodder.files.FileStructureException;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.utilities.Hasher;

public class RW4BlendShapeConfig extends RW4Section {
	
	public static final int type_code = 0xff0002;
	public static final int alignment = 4;
	
	private int name;
	private int shapeCount;
	private boolean usesBones;
	private int[] shapeNames;
	

	@Override
	public void read(InputStreamAccessor in, List<RW4Section> sections) throws IOException {
		expect(in.readLEInt(), 0, "BSC001", in.getFilePointer());
		expect(in.readLEInt(), 0x494650, "BSC002", in.getFilePointer());
		
        int integer = in.readLEInt();
        if (integer == 0x00800000) usesBones = true;
        else if (integer == 0x00800001) usesBones = false;
        else addError("BSC003", in.getFilePointer());
        
        shapeCount = in.readLEInt();
        
        if (usesBones)
        	expect(in.readLEInt(), 0x00800001, "BSC004", in.getFilePointer());
        else
        	expect(in.readLEInt(), 0x00800002, "BSC004", in.getFilePointer());
        
        expect(in.readLEInt(), shapeCount, "BSC005", in.getFilePointer());
        name = in.readLEInt();

        shapeNames = new int[shapeCount];
        
        for (int i = 0; i < shapeCount; i++)
            expect(in.readLEInt(), 0, "BSC006", in.getFilePointer());

        for (int i = 0; i < shapeCount; i++)
            shapeNames[i] = in.readLEInt();
	}

	@Override
	public void write(OutputStreamAccessor out, List<RW4Section> sections) throws IOException {
		out.writeInt(0);
		out.writeLEInt(0x494650);
		out.writeLEInt(usesBones ? 0x00800000 : 0x00800001);
		out.writeLEInt(shapeNames.length);
		out.writeLEInt(usesBones ? 0x00800001 : 0x00800002);
		out.writeLEInt(shapeNames.length);
		out.writeLEInt(name);
		for (int i = 0; i < shapeNames.length; i++) {
			out.writeLEInt(0);
		}
		for (int i = 0; i < shapeNames.length; i++) {
			out.writeLEInt(shapeNames[i]);
		}
	}
	
	@Override
	public void print() {
		System.out.println("### " + this.getClass().getSimpleName() + " section " + this.sectionInfo.number);
		System.out.println("\tname: " + Hasher.getFileName(name));
		System.out.println("\tshapeCount: " + shapeCount);
		for (int shape : shapeNames) 
			System.out.println("\t\tshapeName: " + Hasher.getFileName(shape));
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
