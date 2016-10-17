package sporemodder.files.formats.spui;

import java.io.IOException;
import java.util.Arrays;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.argscript.ArgScriptOptionable;
import sporemodder.files.formats.spui.SPUIChannel.DISPLAY_TYPE;
import sporemodder.utilities.Hasher;

public class SPUINumberSections {
	public static class SectionBoolean extends SPUISection {
		public static final int TYPE = 0x02;
		public static final String TEXT_CODE = "bool";
		
		public boolean[] data;
		
		@Override
		public String getString() {
			StringBuilder b = new StringBuilder();
			b.append(TEXT_CODE);
			b.append(" ");
			b.append(Arrays.toString(data));
			return b.toString();
		}

		@Override
		public void read(InputStreamAccessor in) throws IOException {
			data = new boolean[count];
			in.readBooleans(data);
		}
		
		@Override
		public void write(OutputStreamAccessor out) throws IOException {
			writeGeneral(out);
			out.writeBooleans(data);
		}

		@Override
		public void parse(String str) {
			// [x, y, z, ...]
			// First, we remove [] and white spaces, then we split by comma
			String[] splits = str.substring(1, str.length()-1).replaceAll("\\s", "").split(",");
			data = new boolean[splits.length];
			count = splits.length;
			for (int i = 0; i < splits.length; i++) {
				data[i] = Boolean.parseBoolean(splits[i]);
			}
		}

		@Override
		public void parse(ArgScriptOptionable as) throws ArgScriptException {
			String[] splits = SPUIParser.parseList(as.getLastArgument());
			data = new boolean[splits.length];
			count = splits.length;
			for (int i = 0; i < splits.length; i++) {
				data[i] = Boolean.parseBoolean(splits[i]);
			}
		}

		@Override
		public ArgScriptOptionable toArgScript() {
			return new ArgScriptCommand(getChannelString(), TEXT_CODE, Arrays.toString(data));
		}
	}
	
	public static class SectionByte extends SPUISection {
		public static final int TYPE = 0x07;
		public static final String TEXT_CODE = "byte";
		
		public byte[] data;
		
		@Override
		public String getString() {
			StringBuilder b = new StringBuilder();
			b.append(TEXT_CODE);
			b.append(" ");
			b.append(Arrays.toString(data));
			return b.toString();
		}
		
		@Override
		public void read(InputStreamAccessor in) throws IOException {
			data = new byte[count];
			in.readBytes(data);
		}
		
		@Override
		public void write(OutputStreamAccessor out) throws IOException {
			writeGeneral(out);
			out.write(data);
		}
		
		@Override
		public void parse(String str) {
			// [x, y, z, ...]
			// First, we remove [] and white spaces, then we split by comma
			String[] splits = str.substring(1, str.length()-1).replaceAll("\\s", "").split(",");
			data = new byte[splits.length];
			count = splits.length;
			for (int i = 0; i < splits.length; i++) {
				// Byte.decode gives problems since negative values are expected to have - sign, even if they are in hexadecimal
				data[i] = Hasher.decodeByte(splits[i]);
			}
		}

		@Override
		public void parse(ArgScriptOptionable as) throws ArgScriptException {
			String[] splits = SPUIParser.parseList(as.getLastArgument());
			data = new byte[splits.length];
			count = splits.length;
			for (int i = 0; i < splits.length; i++) {
				// Byte.decode gives problems since negative values are expected to have - sign, even if they are in hexadecimal
				data[i] = Hasher.decodeByte(splits[i]);
			}
		}
		
		@Override
		public ArgScriptOptionable toArgScript() {
			return new ArgScriptCommand(getChannelString(), TEXT_CODE, Arrays.toString(data));
		}
	}
	
	public static class SectionShort extends SPUISection {
		public static final int TYPE = 0x13;
		public static final String TEXT_CODE = "short";
		
		public short[] data;
		
		public static short[] getValues(SectionShort section, short[] defaultValues, int count) throws InvalidBlockException {
			if (section != null) {
				if (count != -1 && section.data.length != count) {
					throw new InvalidBlockException("Wrong section length, expected " + count + " values.", section);
				}
				return section.data;
			} else {
				return defaultValues;
			}
		}
		
		private String shortToString(short s) {
			SPUIChannel c = SPUIChannel.getChannel(channel);
			if (c != null) {
				if (c.displayType == DISPLAY_TYPE.HEX) {
					return "0x" + Integer.toHexString(s);
				}
				else if (c.displayType == DISPLAY_TYPE.DECIMAL) {
					return Short.toString(s);
				}
			}
			if (s > 10000 || s < -10000) {
				return "0x" + Integer.toHexString(s);
			}
			return Short.toString(s);
		}
		
		@Override
		public String getString() {
			StringBuilder b = new StringBuilder();
			b.append(TEXT_CODE);
			b.append(" ");
			
			if (data == null) {
	            b.append("null");
				return b.toString();
			}
	        int iMax = data.length - 1;
	        if (iMax == -1) {
	            b.append("[]");
	            return b.toString();
	        }

	        b.append('[');
	        for (int i = 0; ; i++) {
	            b.append(shortToString(data[i]));
	            if (i == iMax)
	                return b.append(']').toString();
	            b.append(", ");
	        }
		}
		
		@Override
		public void read(InputStreamAccessor in) throws IOException {
			data = new short[count];
			in.readLEShorts(data);
		}
		
		@Override
		public void write(OutputStreamAccessor out) throws IOException {
			writeGeneral(out);
			for (short s : data) {
				out.writeLEShort(s);
			}
		}
		
		@Override
		public void parse(String str) {
			// [x, y, z, ...]
			// First, we remove [] and white spaces, then we split by comma
			String[] splits = str.substring(1, str.length()-1).replaceAll("\\s", "").split(",");
			data = new short[splits.length];
			count = splits.length;
			for (int i = 0; i < splits.length; i++) {
				// Short.decode gives problems since negative values are expected to have - sign, even if they are in hexadecimal
				data[i] = Hasher.decodeShort(splits[i]);
			}
		}

		@Override
		public void parse(ArgScriptOptionable as) throws ArgScriptException {
			String[] splits = SPUIParser.parseList(as.getLastArgument());
			data = new short[splits.length];
			count = splits.length;
			for (int i = 0; i < splits.length; i++) {
				// Short.decode gives problems since negative values are expected to have - sign, even if they are in hexadecimal
				data[i] = Hasher.decodeShort(splits[i]);
			}
		}
		
		@Override
		public ArgScriptOptionable toArgScript() {
			return new ArgScriptCommand(getChannelString(), TEXT_CODE, SPUIParser.createShortList(data, channel));
		}
	}
	
	//TODO ?
	public static class SectionInt2 extends SPUISection {
		public static final int TYPE = 0x05;
		public static final String TEXT_CODE = "int2";
		
		public int[] data;
		
		private String intToString(int i) {
			SPUIChannel c = SPUIChannel.getChannel(channel);
			if (c != null) {
				if (c.displayType == DISPLAY_TYPE.HEX) {
					return "0x" + Integer.toHexString(i);
				}
				else if (c.displayType == DISPLAY_TYPE.DECIMAL) {
					return Integer.toString(i);
				}
			}
			if (i > 1000000 || i < -10000) {
				return "0x" + Integer.toHexString(i);
			}
			return Integer.toString(i);
		}
		
		@Override
		public String getString() {
			StringBuilder b = new StringBuilder();
			b.append(TEXT_CODE);
			b.append(" ");
			
			if (data == null) {
	            b.append("null");
				return b.toString();
			}
	        int iMax = data.length - 1;
	        if (iMax == -1) {
	            b.append("[]");
	            return b.toString();
	        }

	        b.append('[');
	        for (int i = 0; ; i++) {
	            b.append(intToString(data[i]));
	            if (i == iMax)
	                return b.append(']').toString();
	            b.append(", ");
	        }
		}
		@Override
		public void read(InputStreamAccessor in) throws IOException {
			data = new int[count];
			in.readLEInts(data);
		}
		
		@Override
		public void write(OutputStreamAccessor out) throws IOException {
			writeGeneral(out);
			out.writeLEInts(data);
		}
		
		@Override
		public void parse(String str) {
			// [x, y, z, ...]
			// First, we remove [] and white spaces, then we split by comma
			String[] splits = str.substring(1, str.length()-1).replaceAll("\\s", "").split(",");
			data = new int[splits.length];
			count = splits.length;
			for (int i = 0; i < splits.length; i++) {
				// Integer.decode gives problems since negative values are expected to have - sign, even if they are in hexadecimal
				data[i] = Hasher.decodeInt(splits[i]);
			}
		}

		@Override
		public void parse(ArgScriptOptionable as) throws ArgScriptException {
			String[] splits = SPUIParser.parseList(as.getLastArgument());
			data = new int[splits.length];
			count = splits.length;
			for (int i = 0; i < splits.length; i++) {
				// Integer.decode gives problems since negative values are expected to have - sign, even if they are in hexadecimal
				data[i] = Hasher.decodeInt(splits[i]);
			}
		}
		
		@Override
		public ArgScriptOptionable toArgScript() {
			return new ArgScriptCommand(getChannelString(), TEXT_CODE, SPUIParser.createIntList(data, channel));
		}
	}
	
	public static class SectionInt extends SPUISection {
		public static final int TYPE = 0x09;
		public static final String TEXT_CODE = "int";
		
		public int[] data;
		
		public static int[] getValues(SectionInt section, int[] defaultValues, int count) throws InvalidBlockException {
			if (section != null) {
				if (section.data.length != count) {
					throw new InvalidBlockException("Wrong section length, expected " + count + " values.", section);
				}
				return section.data;
			} else {
				return defaultValues;
			}
		}
		
		private String intToString(int i) {
			SPUIChannel c = SPUIChannel.getChannel(channel);
			if (c != null) {
				if (c.displayType == DISPLAY_TYPE.HEX) {
					return "0x" + Integer.toHexString(i);
				}
				else if (c.displayType == DISPLAY_TYPE.DECIMAL) {
					return Integer.toString(i);
				}
			}
			if (i > 1000000 || i < -10000) {
				return "0x" + Integer.toHexString(i);
			}
			return Integer.toString(i);
		}
		
		@Override
		public String getString() {
			StringBuilder b = new StringBuilder();
			b.append(TEXT_CODE);
			b.append(" ");
			
			if (data == null) {
	            b.append("null");
				return b.toString();
			}
	        int iMax = data.length - 1;
	        if (iMax == -1) {
	            b.append("[]");
	            return b.toString();
	        }

	        b.append('[');
	        for (int i = 0; ; i++) {
	            b.append(intToString(data[i]));
	            if (i == iMax)
	                return b.append(']').toString();
	            b.append(", ");
	        }
		}
		
		@Override
		public void read(InputStreamAccessor in) throws IOException {
			data = new int[count];
			in.readLEInts(data);
		}
		
		@Override
		public void write(OutputStreamAccessor out) throws IOException {
			writeGeneral(out);
			out.writeLEInts(data);
		}
		
		@Override
		public void parse(String str) {
			// [x, y, z, ...]
			// First, we remove [] and white spaces, then we split by comma
			String[] splits = str.substring(1, str.length()-1).replaceAll("\\s", "").split(",");
			data = new int[splits.length];
			count = splits.length;
			for (int i = 0; i < splits.length; i++) {
				// Integer.decode gives problems since negative values are expected to have - sign, even if they are in hexadecimal
				data[i] = Hasher.decodeInt(splits[i]);
			}
		}

		@Override
		public void parse(ArgScriptOptionable as) throws ArgScriptException {
			String[] splits = SPUIParser.parseList(as.getLastArgument());
			data = new int[splits.length];
			count = splits.length;
			for (int i = 0; i < splits.length; i++) {
				// Integer.decode gives problems since negative values are expected to have - sign, even if they are in hexadecimal
				data[i] = Hasher.decodeInt(splits[i]);
			}
		}
		
		@Override
		public ArgScriptOptionable toArgScript() {
			return new ArgScriptCommand(getChannelString(), TEXT_CODE, SPUIParser.createIntList(data, channel));
		}
	}
	
	public static class SectionFloat extends SPUISection {
		public static final int TYPE = 0x0B;
		public static final String TEXT_CODE = "float";
		
		public float[] data;
		
		@Override
		public String getString() {
			StringBuilder b = new StringBuilder();
			b.append(TEXT_CODE);
			b.append(" ");
			b.append(Arrays.toString(data));
			return b.toString();
		}
		
		@Override
		public void read(InputStreamAccessor in) throws IOException {
			data = new float[count];
			in.readLEFloats(data);
		}

		@Override
		public void write(OutputStreamAccessor out) throws IOException {
			writeGeneral(out);
			out.writeLEFloats(data);
		}
		
		@Override
		public void parse(String str) {
			// [x, y, z, ...]
			// First, we remove [] and white spaces, then we split by comma
			String[] splits = str.substring(1, str.length()-1).replaceAll("\\s", "").split(",");
			data = new float[splits.length];
			count = splits.length;
			for (int i = 0; i < splits.length; i++) {
				data[i] = Float.parseFloat(splits[i]);
			}
		}

		@Override
		public void parse(ArgScriptOptionable as) throws ArgScriptException {
			String[] splits = SPUIParser.parseList(as.getLastArgument());
			data = new float[splits.length];
			count = splits.length;
			for (int i = 0; i < splits.length; i++) {
				data[i] = Float.parseFloat(splits[i]);
			}
		}
		
		@Override
		public ArgScriptOptionable toArgScript() {
			return new ArgScriptCommand(getChannelString(), TEXT_CODE, Arrays.toString(data));
		}
	}
	
//	public static void main(String[] args) {
//		String str = "[0xFF, 127, -43]";
//		
//		SectionByte2 b = new SectionByte2();
//		b.parse(str);
//		System.out.println(b.getString());
//	}
}
