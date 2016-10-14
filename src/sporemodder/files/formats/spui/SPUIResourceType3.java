package sporemodder.files.formats.spui;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;

public class SPUIResourceType3 implements SPUIResource {
	
	private static final Pattern PATTERN = Pattern.compile("\\[unk1=(\\d+),\\s*unk2=(\\d+),\\s*unk3=\\[([0-9,\\-\\s]*)\\],\\s*file=([^\\]]*)\\]");
	
	//TODO What is this resource for?
	// Only supported in version 3
	// most SPUIs use these
	private boolean useUnk;
	private int unk1;
	private int unk2;
	private int[] unk3;
	// if not useUnk
	private SPUIFileResource file; // ?
	
	@Override
	public void read(InputStreamAccessor in, int version) throws IOException {
		if (version >= 3) {
			useUnk = in.readBoolean();
			if (useUnk) {
				unk1 = in.readLEInt();
				unk2 = in.readLEInt();
				unk3 = new int[in.readLEInt()];
				in.readLEUShorts(unk3);
			}
		}
		// if version < 3 or useUnk == false
		if (!useUnk) {
			file = new SPUIFileResource();
			file.read(in, version);
		}
	}
	
	@Override
	public void write(OutputStreamAccessor out, int version) throws IOException {
		if (version >= 3) {
			out.writeBoolean(useUnk);
			if (useUnk) {
				out.writeLEInt(unk1);
				out.writeLEInt(unk2);
				out.writeLEInt(unk3.length);
				out.writeLEUShorts(unk3);
			}
		}
		if (!useUnk) {
			if (file == null) {
				file = new SPUIFileResource();
			}
			file.write(out, version);
		}
	}
	
	@Override
	public String getString() {
		StringBuilder sb = new StringBuilder("ResourceType3 [unk1=" + unk1 + ", unk2=" + unk2 + ", unk3="
				+ Arrays.toString(unk3) + ", file=");
		if (file == null) {
			sb.append("null");
		} else {
			sb.append("[");
			sb.append(file.getStringSimple());
			sb.append("]");
		}
		sb.append("]");
		return sb.toString();
	}
	
	@Override
	public ArgScriptCommand toCommand() {
		StringBuilder sb = new StringBuilder("[unk1=" + unk1 + ", unk2=" + unk2 + ", unk3="
				+ Arrays.toString(unk3) + ", file=");
		if (file == null) {
			sb.append("null");
		} else {
			sb.append("[");
			sb.append(file.getStringSimple());
			sb.append("]");
		}
		sb.append("]");
		return new ArgScriptCommand("ResourceType3", sb.toString());
	}

	@Override
	public void parse(String str) throws IOException {
		// [unk1=x, unk2=y, unk3=[z, w, ....], file=null] 
		//									   file=[group!file.type]];
		
		// Remove all white spaces, since we don't need them here and it will make parsing easier
		String trimmed = str.trim().replaceAll("\\s", "");
		String contents = trimmed.substring(1, trimmed.length()-1);
		
		// split by comma, if it isn't followed by a digit (so contents inside unk3 are not split)
		String[] split = contents.split(",(?!\\d)");
		
		for (String s : split) 
		{
			String[] spl = s.split("=");
			if (spl.length != 2) {
				System.err.println("Error parsing ResourceType3");
				return;
			}
			String var = spl[0];
			String value = spl[1];
			
			if (var.equals("unk1")) {
				unk1 = Integer.decode(value);
				useUnk = true;
			}
			else if (var.equals("unk2")) {
				unk2 = Integer.decode(value);
				useUnk = true;
			}
			else if (var.equals("unk3")) {
				useUnk = true;
				// [x,y,z,...]
				// First we remove the [], and then we split by comma
				String[] values = value.substring(1, value.length()-1).split(",");
				unk3 = new int[values.length];
				for (int i = 0; i < values.length; i++) {
					unk3[i] = Integer.decode(values[i]);
				}
			}
			else if (var.equals("file")) {
				if (value.equals("null")) {
					file = null;
				}
				else {
					useUnk = false;
					// [group!file.type]
					file = new SPUIFileResource();
					// We must remove the [];
					file.parseSimple(value.substring(1, value.length()-1));
				}
			}
		}
	}

	@Override
	public RESOURCE_TYPE getType() {
		return SPUIResource.RESOURCE_TYPE.TYPE3;
	}

	public boolean isUseUnk() {
		return useUnk;
	}

	public void setUseUnk(boolean useUnk) {
		this.useUnk = useUnk;
	}

	public int getUnk1() {
		return unk1;
	}

	public void setUnk1(int unk1) {
		this.unk1 = unk1;
	}

	public int getUnk2() {
		return unk2;
	}

	public void setUnk2(int unk2) {
		this.unk2 = unk2;
	}

	public int[] getUnk3() {
		return unk3;
	}

	public void setUnk3(int[] unk3) {
		this.unk3 = unk3;
	}

	public SPUIFileResource getFile() {
		return file;
	}

	public void setFile(SPUIFileResource file) {
		this.file = file;
	}

	@Override
	public void parse(ArgScriptCommand c) throws ArgScriptException, IOException {
		String str = c.getSingleArgument();
		Matcher matcher = PATTERN.matcher(str);
		matcher.find();
		
		//TODO improve this?
		useUnk = true;
		
		unk1 = (int) (Long.decode(matcher.group(1)) & 0xFFFFFFFF);
		unk2 = (int) (Long.decode(matcher.group(2)) & 0xFFFFFFFF);
		
		String[] splits = matcher.group(3).split(",\\s*");
		// for some reason when the string is empty it still returns an split
		if (splits.length != 1 || splits[0].length() != 0) {
			unk3 = new int[splits.length];
			for (int i = 0; i < splits.length; i++) {
				if (splits[i].length() > 0) {
					unk3[i] = Integer.decode(splits[i]);
				}
			}
		} else {
			unk3 = new int[0];
		}
		
		String fileStr = matcher.group(4);
		if (fileStr.equals("null")) {
			file = null;
		}
		else {
			useUnk = false;
			file = new SPUIFileResource();
			file.parseSimple(fileStr);
		}
	}
	
	
	
//	public static void main(String[] args) throws IOException {
//		String str = "[unk1=21, unk2=29, unk3=[96, 97, 116, 120, 136, 142, 156, 164, 176, 185, 196, 206, 216, 227, 237, 248, 257, 269, 278, 290, 298, 311, 319, 332, 341, 353, 362, 374, 384, 395, 406, 416, 428, 437, 449, 457, 471, 477, 493, 497, 516, 517], file=null]";
//		
//		SPUIResourceType3 resource = new SPUIResourceType3();
//		resource.parse(str);
//		
//		System.out.println();
//		System.out.println();
//		System.out.println(resource.getString());
//	}
	
//	public static void main(String[] args) {
//		ArgScriptCommand c = new ArgScriptCommand("ResourceType3 [unk1=21, unk2=29, unk3=[96, 97, 116, 120, 136, 142, 156, 164, 176, 185, 196, 206, 216, 227, 237, 248, 257, 269, 278, 290, 298, 311, 319, 332, 341, 353, 362, 374, 384, 395, 406, 416, 428, 437, 449, 457, 471, 477, 493, 497, 516, 517], file=null]");
//		System.out.println(c);
//		
//		List<String> list = c.getArguments();
//		String[] split = list.get(0).split(",(?!\\d)");
//		System.out.println(list.get(0));
//		
//		//Matcher matcher = Pattern.compile("\\[(unk1=\\d+),\\s*(unk2=\\d+),\\s*(unk3=\\[[^\\[\\]]\\]),\\s*(file=\\d+)\\]").matcher(list.get(0));
//		Matcher matcher = Pattern.compile("\\[unk1=(\\d+),\\s*unk2=(\\d+),\\s*unk3=\\[([0-9,\\s]*)\\],\\s*file=([^\\]]*)\\]").matcher(list.get(0));
//		matcher.find();
//		//System.out.println(matcher.find());
//		System.out.println(matcher.group(1));
//		System.out.println(matcher.group(2));
//		System.out.println(matcher.group(3));
//		System.out.println(matcher.group(4));
//	}

}
