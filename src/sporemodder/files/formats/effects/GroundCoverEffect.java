package sporemodder.files.formats.effects;

import sporemodder.files.formats.argscript.ArgScriptCommand;

public class GroundCoverEffect extends ParticleEffect {
	
	public static final int TYPE = 0x2C;
	public static final int MIN_VERSION = 1;
	public static final int MAX_VERSION = 1;
	public static final String KEYWORD = "groundCover";

	public GroundCoverEffect(int type, int version) {
		super(type, version);
	} 
	
	public GroundCoverEffect(ParticleEffect effect) {
		super(effect);
	}

	@Override
	public void parseInline(ArgScriptCommand command) {
		throw new UnsupportedOperationException("Inline groundCover effect is not supported.");
	}
	
	// For Syntax Highlighting
	public static String[] getBlockTags() {
		return new String[] {
			KEYWORD
		};
	}
}
