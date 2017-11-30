package sporemodder.files.formats.effects;

import java.io.IOException;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.utilities.Hasher;

public class MixEventEffect extends EffectComponent {
	
	public static final int TYPE = 0x2D;
	public static final int MIN_VERSION = 1;
	public static final int MAX_VERSION = 2;
	public static final String KEYWORD = "mixEvent";
	
	private int field_8;
	private float field_C = 1.0f;
	private float field_10;

	public MixEventEffect(int type, int version) {
		super(type, version);
	}
	
	public MixEventEffect(MixEventEffect effect) {
		super(effect);
		
		field_8 = effect.field_8;
		field_C = effect.field_C;
		field_10 = effect.field_10;
	}

	@Override
	public boolean read(InputStreamAccessor in) throws IOException {
		field_8 = in.readInt();
		field_C = in.readFloat();
		if (version >= 2) {
			field_10 = in.readFloat();
		}
		
		return true;
	}

	@Override
	public boolean write(OutputStreamAccessor out) throws IOException {
		out.writeInt(field_8);
		out.writeFloat(field_C);
		if (version >= 2) {
			out.writeFloat(field_10);
		}
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
		return true;
	}
	
	@Override
	public ArgScriptCommand toCommand() {
		ArgScriptCommand c = new ArgScriptCommand(KEYWORD, Hasher.getFileName(field_8, "0x"), Float.toString(field_C), Float.toString(field_10));
		
		return c;
	}

	@Override
	public void parseInline(ArgScriptCommand command) throws ArgScriptException {
		List<String> args = command.getArguments(1, 3);
		field_8 = Hasher.getFileHash(args.get(0));
		if (args.size() > 1) {
			field_C = Float.parseFloat(args.get(1));
			if (args.size() > 2) {
				field_10 = Float.parseFloat(args.get(2));
			}
		}
	}

	// For Syntax Highlighting
	public static String[] getEnumTags() {
		return new String[] {};
	}
	
	public static String[] getBlockTags() {
		return new String[] {
		};
	}
	
	public static String[] getOptionTags() {
		return new String[] {
		};
	}
	
	public static String[] getCommandTags() {
		return new String[] {
			KEYWORD,
		};
	}
}
