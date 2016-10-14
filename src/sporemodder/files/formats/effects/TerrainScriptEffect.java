package sporemodder.files.formats.effects;

import java.io.IOException;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.utilities.Hasher;

public class TerrainScriptEffect extends EffectComponent {

	public static final int TYPE = 0x21;
	public static final int MIN_VERSION = 1;
	public static final int MAX_VERSION = 1;
	public static final String KEYWORD = "terrainScript";
	
	private static final int FLAG_LOOP = 1;
	private static final int FLAG_HARDSTART = 2;
	private static final int FLAG_HARDSTOP = 4;
	private static final int FLAG_NOSTOP = 8;
	
	private int flags;  // 0x08  // & 0xF
	private int id_0;  // 0x0C
	private int id_1;  // 0x10
	private int id_2;  // 0x14
	
	public TerrainScriptEffect(int type, int version) {
		super(type, version);
	}

	public TerrainScriptEffect(TerrainScriptEffect effect) {
		super(effect);
		flags = effect.flags;
		id_0 = effect.id_0;
		id_1 = effect.id_1;
		id_2 = effect.id_2;
	}

	@Override
	public boolean read(InputStreamAccessor in) throws IOException {
		flags = in.readInt();  // & 0xF
		id_1 = in.readInt();
		id_2 = in.readInt();
		id_0 = in.readInt();
		return true;
	}

	@Override
	public boolean write(OutputStreamAccessor out) throws IOException {
		out.writeInt(flags);
		out.writeInt(id_1);
		out.writeInt(id_2);
		out.writeInt(id_0);
		return true;
	}

	@Override
	public boolean parse(ArgScriptBlock block) throws IOException,
			ArgScriptException {
		throw new UnsupportedOperationException(KEYWORD + " effect only supports inline version.");
	}

	@Override
	public boolean toBlock(ArgScriptBlock block) {
		throw new UnsupportedOperationException(KEYWORD + " effect only supports inline version.");
	}
	
	@Override
	public boolean supportsBlock() {
		return false;
	}
	
	@Override
	public boolean isInline(ArgScriptCommand command) {
		// this one is always inline, but it does have arguments
		return true;
	}
	
	@Override
	public void parseInline(ArgScriptCommand c) throws ArgScriptException {
		String arg = c.getSingleArgument();
		
		String[] splits = arg.split(".", 2);
		if (splits.length > 1) {
			id_0 = Hasher.getFileHash(splits[0]);
			id_1 = Hasher.getFileHash(splits[1]);
			id_2 = 0;
		}
		else {
			ResourceID res = new ResourceID(arg);
			id_0 = res.getNameID();
			id_1 = 0;
			id_2 = res.getGroupID() == -1 ? 0 : res.getGroupID();
		}
		if (c.hasFlag("loop")) flags |= FLAG_LOOP;
		if (c.hasFlag("noOverlap")) flags |= FLAG_HARDSTOP;
		if (c.hasFlag("hardStop")) flags |= FLAG_HARDSTOP;
		if (c.hasFlag("hardStart")) flags |= FLAG_HARDSTART;
		if (c.hasFlag("noStop")) flags |= FLAG_NOSTOP;
		if (c.hasFlag("overlap")) flags |= FLAG_NOSTOP;
	}

	@Override
	public ArgScriptCommand toCommand() {
		
		ArgScriptCommand c = new ArgScriptCommand(KEYWORD, new String[0]);
		
		if (id_0 == 0) {
			if (id_2 == 0) {
				c.addArgument(Hasher.getFileName(id_1));
			}
		}
		else {
			if (id_1 == 0) {
				if (id_2 == 0) {
					c.addArgument(Hasher.getFileName(id_0));
				}
				else {
					c.addArgument(Hasher.getFileName(id_0) + "!" + Hasher.getFileName(id_2));
				}
			}
		}
		
		if ((flags & FLAG_LOOP) == FLAG_LOOP) c.putFlag("loop");
		if ((flags & FLAG_HARDSTOP) == FLAG_HARDSTOP) c.putFlag("hardStop");
		if ((flags & FLAG_HARDSTART) == FLAG_HARDSTART) c.putFlag("hardStart");
		if ((flags & FLAG_NOSTOP) == FLAG_NOSTOP) c.putFlag("noStop");
		
		return c;
	}
	
	
	// For Syntax Highlighting
	public static String[] getEnumTags() {
		return new String[] {
		};
	}
	
	public static String[] getBlockTags() {
		return new String[] {
		};
	}
	
	public static String[] getOptionTags() {
		return new String[] {
			"loop", "hardStop", "hardStart", "noStop", "overlap", "noOverlap"
		};
	}
	
	public static String[] getCommandTags() {
		return new String[] {
			KEYWORD
		};
	}
}
