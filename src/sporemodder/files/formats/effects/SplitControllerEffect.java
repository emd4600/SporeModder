package sporemodder.files.formats.effects;

import java.io.IOException;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.utilities.Hasher;

public class SplitControllerEffect extends EffectComponent {
	
	public static final int TYPE = 0x29;
	public static final int MIN_VERSION = 1;
	public static final int MAX_VERSION = 1;
	public static final String KEYWORD = "splitController";
	
	// This effect doesn't have anything

	public SplitControllerEffect(int type, int version) {
		super(type, version);
	}
	
	public SplitControllerEffect(SplitControllerEffect effect) {
		super(effect);
	}

	@Override
	public boolean read(InputStreamAccessor in) throws IOException {
		return true;
	}

	@Override
	public boolean write(OutputStreamAccessor out) throws IOException {
		return true;
	}
	
	@Override
	public boolean isInline(ArgScriptCommand command) {
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
	public ArgScriptCommand toCommand() {
		ArgScriptCommand c = new ArgScriptCommand(KEYWORD);
		
		return c;
	}

	@Override
	public void parseInline(ArgScriptCommand command) throws ArgScriptException {
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
