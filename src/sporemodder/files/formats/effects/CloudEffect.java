package sporemodder.files.formats.effects;

import java.io.IOException;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScriptCommand;

public class CloudEffect extends ParticleEffect {
	
	public static final int TYPE = 0x2B;
	public static final int MIN_VERSION = 3;
	public static final int MAX_VERSION = 3;
	public static final String KEYWORD = "cloud";

	public CloudEffect(int type, int version) {
		super(type, version);
	} 
	
	public CloudEffect(ParticleEffect effect) {
		super(effect);
	}
	
	@Override
	public boolean read(InputStreamAccessor in) throws IOException {
		return readMain(in);
	}
	
	@Override
	public boolean write(OutputStreamAccessor out) throws IOException {
		return writeMain(out);
	}

	@Override
	public void parseInline(ArgScriptCommand command) {
		throw new UnsupportedOperationException("Inline cloud effect is not supported.");
	}
	
	// For Syntax Highlighting
	public static String[] getBlockTags() {
		return new String[] {
			KEYWORD
		};
	}
}
