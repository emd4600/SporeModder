package sporemodder.files.formats.spui;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScript.ArgScriptType;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.argscript.ArgScriptOptionable;
import sporemodder.utilities.Hasher;

public class SPUIComplexSections {
	public static class SectionSectionList extends SPUISection {
		public static final int TYPE = 0x14;
		public static final String TEXT_CODE = "sections";
		
		public int unk;
		public SPUISection[][] sections;
		
		@Override
		public void read(InputStreamAccessor in) throws IOException {
			unk = in.readLEInt();
			sections = new SPUISection[count][];
			for (int i = 0; i < sections.length; i++) {
				
				sections[i] = new SPUISection[in.readLEShort()];
				for (int j = 0; j < sections[i].length; j++) {
					sections[i][j] = SPUISection.readSection(in);
				}
			}
		}
		
		@Override
		public void write(OutputStreamAccessor out) throws IOException {
			writeGeneral(out);
			out.writeLEInt(unk);
			for (SPUISection[] sec : sections) {
				out.writeLEShort(sec.length);
				for (SPUISection s : sec) {
					s.write(out);
				}
			}
		}
		
		@Override
		public String getString() {
			String lineSeparator = System.getProperty("line.separator");
			StringBuilder sb = new StringBuilder();
			sb.append(TEXT_CODE);
			sb.append(" ");
			sb.append(unk);
			
			for (SPUISection[] sec : sections) {
				sb.append(lineSeparator);
				sb.append("\t\tblock");
				for (SPUISection s : sec) {
					sb.append(lineSeparator);
					sb.append(s.toString("\t\t\t"));
				}
				sb.append(lineSeparator);
			}
			
			sb.append(lineSeparator);
			sb.append("\tend");
			
			return sb.toString();
		}

		@Override
		public void parse(String str) throws IOException {
//			// unk
//			// 		section
//			//		section
//
//			string[] lines = str.split("\n");
//			
//			unk = integer.parseint(lines[0].trim());
//			
//			sections = new spuisection[lines.length-1][];
//			
//			list<spuisection[]> list = new arraylist<spuisection[]>();
//			
//			for (int i = 1; i < lines.length; i++) {
//				string line = lines[i].trim();
//				if (line.startswith(equals(""))) {
//					
//				}
//				
//				list<string> l = new arraylist<string>();
//				l.add(lines[i]);
//				sections[i-1] = spuisection.readsectiontxt(l, new spuiparser());
//			}
		}

		@Override
		public void parse(ArgScriptOptionable as) throws ArgScriptException, IOException {
			if (as.getType() != ArgScriptType.BLOCK) {
				throw new ArgScriptException("Wrong format in 'sections' property, blocks must be used.");
			}
			unk = Integer.parseInt(as.getLastArgument());
			
			ArgScriptBlock block = (ArgScriptBlock) as; 
			
			Collection<ArgScriptBlock> blocks = block.getAllBlocks();
			// old version, doesn't use blocks
			if (blocks.size() == 0)
			{
				sections = new SPUISection[1][];
				count = 1;
				Collection<ArgScriptCommand> commands = block.getAllCommands();
				sections[0] = new SPUISection[commands.size()];
				int i = 0;
				for (ArgScriptCommand c : commands) {
					sections[0][i++] = SPUISection.parseSection(c); 
				}
			}
			else
			{
				sections = new SPUISection[blocks.size()][];
				count = sections.length;
				int j = 0;
				for (ArgScriptBlock b : blocks) {
					List<ArgScriptOptionable> commands = b.getAllOptionables();
					sections[j] = new SPUISection[commands.size()];
					int i = 0;
					for (ArgScriptOptionable c : commands) {
						sections[j][i++] = SPUISection.parseSection(c); 
					}
					j++;
				}
			}
		}

		@Override
		public ArgScriptOptionable toArgScript() {
			ArgScriptBlock block = new ArgScriptBlock(getChannelString(), TEXT_CODE, Integer.toString(unk));
			
			for (SPUISection[] secs : sections) {
				ArgScriptBlock b = new ArgScriptBlock("block");
				for (SPUISection s : secs) {
					b.putOptionable(s.toArgScript());
				}
				block.putBlock(b);
			}
			
			return block;
		}
	}
	
	public static class SectionText extends SPUISection {
		public static class LocalizedText {
			public int tableID = -1;
			public int instanceID = -1;
			public String text = "";
			
			private void read(InputStreamAccessor in) throws IOException {
				int len = in.readLEShort();
				if (len == -1) {
					tableID = in.readLEInt();
					instanceID = in.readLEInt();
				} else {
					text = in.readLEString16(len);
				}
			}
			private void write(OutputStreamAccessor out) throws IOException {
				if (tableID == -1 && instanceID == -1) {
					out.writeLEShort(text.length());
					out.writeLEString16(text);
				} else {
					out.writeLEShort(-1);
					out.writeLEInt(tableID);
					out.writeLEInt(instanceID);
				}
			}
			@Override
			public String toString() {
				if (text.length() == 0) {
					return "(" + Hasher.getFileName(tableID) + "!" + Hasher.getFileName(instanceID) + ")";
				} else {
					return "\"" + text + "\"";
				}
			}
			
			private void parse(String str) throws IOException {
				// (tableID!instanceID)
				// "This is my text"
				
				if (str.startsWith("(")) {
					// (tableID!instanceID)
					String[] splits = str.substring(1, str.length()-1).split("!");
					tableID = Hasher.getFileHash(splits[0]);
					instanceID = Hasher.getFileHash(splits[1]);
				}
				else {
					text = str.substring(1, str.length()-1);
				}
			}
		}
		public static final int TYPE = 0x12;
		public static final String TEXT_CODE = "text";
		
		public LocalizedText[] data;
		 
		public void read(InputStreamAccessor in) throws IOException {
			data = new LocalizedText[count];
			for (int i = 0; i < count; i++) {
				data[i] = new LocalizedText();
				data[i].read(in);
			}
		}
		
		@Override
		public void write(OutputStreamAccessor out) throws IOException {
			writeGeneral(out);
			for (LocalizedText text : data) {
				text.write(out);
			}
		}
		
		@Override
		public String getString() {
			StringBuilder b = new StringBuilder();
			b.append(TEXT_CODE);
			b.append(" ");
			b.append(Arrays.toString(data));
			return b.toString();
		}

		@Override
		public void parse(String str) throws IOException {
			// [(tableID!instanceID), "This is my text"]
			// Remove the [] and split by comma, if preceded by ]. 
			// We don't remove spaces to keep the text, but there must be some better way to do this
			//TODO There's no support for , inside text using this
			String[] splits = str.substring(1, str.length()-1).split(",");
			
			data = new LocalizedText[splits.length];
			count = splits.length;
			for (int i = 0; i < splits.length; i++) {
				data[i] = new LocalizedText();
				// we remove the unnecessary spaces here, after splitting by comma, so we don't remove text spaces too
				data[i].parse(splits[i].trim());
			}
		}

		@Override
		public void parse(ArgScriptOptionable as) throws ArgScriptException, IOException {
			
			String str = as.getLastArgument();
			String[] splits = str.substring(1, str.length() - 1).split(",\\s+");
			
			data = new LocalizedText[splits.length];
			count = splits.length;
			for (int i = 0; i < splits.length; i++) {
				data[i] = new LocalizedText();
				// we remove the unnecessary spaces here, after splitting by comma, so we don't remove text spaces too
				data[i].parse(splits[i].trim());
			}
		}

		@Override
		public ArgScriptOptionable toArgScript() {
			ArgScriptCommand c = new ArgScriptCommand(getChannelString(), TEXT_CODE, SPUIParser.createList(data));
			
			return c;
		}
	}
	
//	public static void main(String[] args) throws IOException {
//		NameRegistry.read();
//		String str = "[(0x012353!creature), \"Test text\"]";
//		
//		SectionText sec = new SectionText();
//		sec.parse(str);
//		System.out.println(sec.getString());
//	}
}
