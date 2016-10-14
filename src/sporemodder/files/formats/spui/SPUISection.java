package sporemodder.files.formats.spui;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import sporemodder.MainApp;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.argscript.ArgScriptOptionable;
import sporemodder.files.formats.spui.SPUIComplexSections.*;
import sporemodder.files.formats.spui.SPUIVectorSections.*;
import sporemodder.files.formats.spui.SPUINumberSections.*;
import sporemodder.utilities.Hasher;
import sporemodder.utilities.names.NameRegistry;

public abstract class SPUISection {
	protected int channel; // ?
	protected int type; // ?
	protected int count; // ?
	
	// When parsing, we can't return the section, so we put it here
//	protected static SPUISection section;
	
	public abstract void read(InputStreamAccessor in) throws IOException;
	public abstract void write(OutputStreamAccessor out) throws IOException;
	public abstract String getString();
	public abstract void parse(String str) throws IOException;
	// it can either be a command or a block
	public abstract void parse(ArgScriptOptionable as) throws ArgScriptException, IOException;
	public abstract ArgScriptOptionable toArgScript();
	
	public SPUISection() {
		type = getType(this);
	}
	
	public static SPUISection readSection(InputStreamAccessor in) throws IOException {
		SPUISection sec = null;
		
		int channel = in.readLEInt();
		int type = in.readLEShort();
		int count = in.readLEShort();
		
		if (type == SectionBoolean.TYPE) {
			// boolean ?
			sec = new SectionBoolean();
		}
		else if (type == SectionByte.TYPE) {
			// byte
			sec = new SectionByte();
		}
		else if (type == SectionInt2.TYPE) {
			// int2 ?
			sec = new SectionInt2();
		}
		else if (type == SectionInt.TYPE) {
			// int
			sec = new SectionInt();
		}
		else if (type == SectionFloat.TYPE) {
			// float
			sec = new SectionFloat();
		}
		else if (type == SectionDimension.TYPE) {
			// vector2i, used for sprite size
			sec = new SectionDimension();
		}
		else if (type == SectionVec4.TYPE) {
			// vector4f
			sec = new SectionVec4();
		}
		else if (type == SectionVec2.TYPE) {
			// vector2f
			sec = new SectionVec2();
		}
		else if (type == SectionText.TYPE) {
			// LocalizedString
			sec = new SectionText();
		}
		else if (type == SectionShort.TYPE) {
			// short
			sec = new SectionShort();
		}
		else if (type == SectionSectionList.TYPE) {
			// multiple sections ?
			sec = new SectionSectionList();
		}
		else {
			//TODO error
			throw new IOException("Unknown section type in pos " + in.getFilePointer());
//			return null;
		}
		
		sec.channel = channel;
		sec.type = type;
		sec.count = count;
		sec.read(in);
		
		return sec;
	}
	
//	public SPUISection readSectionTxt(List<String> lines, SPUIParser parser) throws IOException {
//		
//		String str = lines.get(parser.e++);
////		// We remove the comments and the indentation
////		String str = line.split("//")[0].trim();
//		
//		// if the line is empty
//		if (str.length() == 0) {
//			return null;
//		}
//		
//		SPUISection section = null;
//		
//		// channel type data
//		String[] splits = str.split(" ", 3);
//		
//		int channel = Hasher.getHash(splits[0], MainApp.getRegistry(NameRegistry.NAME_SPUI));
//		
//		if (splits[1].equals(SectionBoolean.TEXT_CODE)) {
//			// byte again ?
//			section = new SectionBoolean();
//			section.type = SectionBoolean.TYPE;
//		}
//		else if (splits[1].equals(SectionByte.TEXT_CODE)) {
//			// byte
//			section = new SectionByte();
//			section.type = SectionByte.TYPE;
//		}
//		else if (splits[1].equals(SectionInt2.TEXT_CODE)) {
//			// int
//			section = new SectionInt2();
//			section.type = SectionInt2.TYPE;
//		}
//		else if (splits[1].equals(SectionInt.TEXT_CODE)) {
//			// int
//			section = new SectionInt();
//			section.type = SectionInt.TYPE;
//		}
//		else if (splits[1].equals(SectionFloat.TEXT_CODE)) {
//			// float
//			section = new SectionFloat();
//			section.type = SectionFloat.TYPE;
//		}
//		else if (splits[1].equals(SectionDimension.TEXT_CODE)) {
//			// vector2i, used for sprite size
//			section = new SectionDimension();
//			section.type = SectionDimension.TYPE;
//		}
//		else if (splits[1].equals(SectionVec4.TEXT_CODE)) {
//			// vector4f
//			section = new SectionVec4();
//			section.type = SectionVec4.TYPE;
//		}
//		else if (splits[1].equals(SectionVec2.TEXT_CODE)) {
//			// vector2f
//			section = new SectionVec2();
//			section.type = SectionVec2.TYPE;
//		}
//		else if (splits[1].equals(SectionText.TEXT_CODE)) {
//			// LocalizedString
//			section = new SectionText();
//			section.type = SectionText.TYPE;
//		}
//		else if (splits[1].equals(SectionShort.TEXT_CODE)) {
//			// short
//			section = new SectionShort();
//			section.type = SectionShort.TYPE;
//		}
//		else if (splits[1].equals(SectionSectionList.TEXT_CODE)) {
//			// multiple sections ?
//			section = new SectionSectionList();
//			section.type = SectionSectionList.TYPE;
//		}
//		else {
//			System.err.println("Unknown section type!");
//			return null;
//		}
//		
//		section.channel = channel;
//		
//		if (section.type == SectionSectionList.TYPE) {
//			StringBuilder sb = new StringBuilder(splits[2]);
//			
//			String l = null;
//			while(true) {
//				l = lines.get(parser.e++);
//				if (l.trim().equals("end")) break;
//				sb.append("\n" + l);
//			}
//			section.parse(sb.toString());
//		}
//		else {
//			section.parse(splits[2]);
//		}
//		
//		return section;
//	}
	
	public static SPUISection parseSection(ArgScriptOptionable as) throws ArgScriptException, IOException {
		SPUISection section = null;
		
		List<String> args = as.getArguments(2);
		
		String typeStr = args.get(0);
		
		int channel = Hasher.getHash(as.getKeyword(), MainApp.getRegistry(NameRegistry.NAME_SPUI));
		
		if (typeStr.equals(SectionBoolean.TEXT_CODE)) {
			// byte again ?
			section = new SectionBoolean();
			section.type = SectionBoolean.TYPE;
		}
		else if (typeStr.equals(SectionByte.TEXT_CODE)) {
			// byte
			section = new SectionByte();
			section.type = SectionByte.TYPE;
		}
		else if (typeStr.equals(SectionInt2.TEXT_CODE)) {
			// int
			section = new SectionInt2();
			section.type = SectionInt2.TYPE;
		}
		else if (typeStr.equals(SectionInt.TEXT_CODE)) {
			// int
			section = new SectionInt();
			section.type = SectionInt.TYPE;
		}
		else if (typeStr.equals(SectionFloat.TEXT_CODE)) {
			// float
			section = new SectionFloat();
			section.type = SectionFloat.TYPE;
		}
		else if (typeStr.equals(SectionDimension.TEXT_CODE)) {
			// vector2i, used for sprite size
			section = new SectionDimension();
			section.type = SectionDimension.TYPE;
		}
		else if (typeStr.equals(SectionVec4.TEXT_CODE)) {
			// vector4f
			section = new SectionVec4();
			section.type = SectionVec4.TYPE;
		}
		else if (typeStr.equals(SectionVec2.TEXT_CODE)) {
			// vector2f
			section = new SectionVec2();
			section.type = SectionVec2.TYPE;
		}
		else if (typeStr.equals(SectionText.TEXT_CODE)) {
			// LocalizedString
			section = new SectionText();
			section.type = SectionText.TYPE;
		}
		else if (typeStr.equals(SectionShort.TEXT_CODE)) {
			// short
			section = new SectionShort();
			section.type = SectionShort.TYPE;
		}
		else if (typeStr.equals(SectionSectionList.TEXT_CODE)) {
			// multiple sections ?
			section = new SectionSectionList();
			section.type = SectionSectionList.TYPE;
		}
		else {
			System.err.println("Unknown section type!");
			return null;
		}
		
		section.channel = channel;
		section.parse(as);
		
		return section;
	}
	
	protected void writeGeneral(OutputStreamAccessor out) throws IOException {
		out.writeLEInt(channel);
		out.writeLEShort(type);
		out.writeLEShort(count);
	}
	
	public String toString(String indentation) {
		return indentation + Hasher.getFileName(channel) + " " + getString();
	}
	public void print() {
		System.out.println(toString("\t"));
	}
	
	public String getChannelString() {
		return Hasher.getName(channel, MainApp.getRegistry(NameRegistry.NAME_SPUI));
	}
	
	public void writeTxt(BufferedWriter out, String indentation) throws IOException {
		//TODO we should improve this
		out.write(indentation + Hasher.getName(channel, MainApp.getRegistry(NameRegistry.NAME_SPUI)) + " " + getString());
		out.newLine();
	}
	public int getChannel() {
		return channel;
	}
	public void setChannel(int channel) {
		this.channel = channel;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getCount() {
		return count;
	}
	
	public static int getType(SPUISection section) {
		Field field;
		try {
			field = section.getClass().getField("TYPE");
			return field.getInt(null);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	
	public static void main(String[] args) {
		ArgScriptCommand c = new ArgScriptCommand("#EEC1B00A text [\"Maya Export\"]");
		System.out.println(c);
	}
}


