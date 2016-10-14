package sporemodder.files.formats.bem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import sporemodder.files.FileStreamAccessor;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.formats.FileStructure;

public class BEMMain extends FileStructure {
	
	private byte[] decompress(InputStreamAccessor in, int size) throws IOException 
	{
		byte[] dst = new byte[size];
		byte[] buffer = null;
		int i = 0;
		int o = 0;
		int bufferLength = 0;
		byte setByte = 0;
		
		while (o < size)
		{
			// Spore won't accept blocks bigger than 2048 bytes, so a section might use more than one block
			if (i >= bufferLength) {
				buffer = null;
			}
			if (buffer == null)
			{
				i = 0;
				bufferLength = in.readLEShort();
				expect(bufferLength, 1, CompareFunc.GREATER_EQUAL, "BEM-S001", in.getFilePointer());
				expect(bufferLength, 2048, CompareFunc.LESS_EQUAL, "BEM-S002", in.getFilePointer());
				buffer = new byte[bufferLength];
				in.read(buffer);
			}
			
//			if (i >= bufferLength) {
//				System.out.println("Breaking. o = " + o + "\ti = " + i);
//				break;
//			}
//			System.out.println(Integer.toHexString(buffer[i]));
			if ((buffer[i] & 0xFF) == 0xFE) 
			{
				i++;
				// We must throw an error here, because if not it will throw an ArrayIndexOutOfBounds error later
				if (i >= bufferLength) throw new IOException("BEM-S003; Couldn't decompress. Position=" + in.getFilePointer());
				
				if (buffer[i] == 0) 
				{
					dst[o++] = (byte) 0xFE;
					setByte = (byte) 0xFE;
				}
				else
				{
					int setSize = buffer[i] & 0xFF;
					// We must throw an error here, because if not it will throw an ArrayIndexOutOfBounds error later
					if (setSize > size - o) throw new IOException("BEM-S004; Couldn't decompress. Position=" + in.getFilePointer());
					Arrays.fill(dst, o, o + setSize, setByte);
					o += setSize;
				}
			}
			else
			{
				dst[o++] = buffer[i];
				setByte = buffer[i];
			}
			
			i++;
		}
		
		return dst;
	}
	
	public void read(InputStreamAccessor in, String outPath) throws IOException 
	{
		int magic = in.readLEInt();
		expect(magic, 0x48657ED3, "BEM-H001", in.getFilePointer());
		
		int version = in.readLEInt();
		expect(version, 3, CompareFunc.GREATER, "BEM-H002", in.getFilePointer());
		expect(version, 5, CompareFunc.LESS_EQUAL, "BEM-H003", in.getFilePointer());
		
		int unk1 = in.readLEInt();
		expect(unk1, 1024, CompareFunc.LESS_EQUAL, "BEM-H004", in.getFilePointer());
		int unk2 = in.readLEInt();
		expect(unk2, 128, CompareFunc.LESS_EQUAL, "BEM-H005", in.getFilePointer());
		
		int sectionSize = 128;
		if (version >= 5) sectionSize -= 60;
		
		Files.write(new File(outPath + "section1.bem").toPath(), decompress(in, sectionSize), StandardOpenOption.CREATE);
		Files.write(new File(outPath + "section2.bem").toPath(), decompress(in, unk1 * 472), StandardOpenOption.CREATE);
	}
	
	public static void main(String[] args) throws IOException {
//		String path = "E:\\Eric\\SporeMaster 2.0 beta\\Spore_Pack_03.package.unpacked\\palette_editorModel~\\";
		String path = "C:\\Users\\Eric\\Desktop\\Creature BEM Test\\";
//		String fileName = "#8172B87D.bem";
//		String fileName = "#14B34197.bem";
		String fileName = "#14B8A790.bem";
		
		FileStreamAccessor in = new FileStreamAccessor(path + fileName, "r");
		
		try {
			BEMMain bem = new BEMMain();
			bem.read(in, path + fileName + "_");
			
			bem.printErrors();	
		} finally {
			in.close();
		}
	}
}
