package sporemodder.files.formats.effects;

import sporemodder.files.formats.argscript.ArgScript.ArgScriptParser;
import sporemodder.files.formats.argscript.ArgScript.ArgScriptType;

public class EffectParser implements ArgScriptParser {
	
	private static final String[] LEVEL1_BLOCK_KEYWORDS = new String[] {
		"select", "particleSelect", "modifier", "level", "filterChain"
	};

	@Override
	public ArgScriptType checkElement(String keyword, String line, int level) {
		
		if (level == 0) {
			if (keyword.equals("export") || keyword.equals("import") || keyword.equals(MapResource.KEYWORD)) {
				return ArgScriptType.COMMAND;
			}
			else {
				return ArgScriptType.BLOCK;
			}
		}
		else if (level == 1) {
			for (String s : LEVEL1_BLOCK_KEYWORDS) {
				if (keyword.equals(s)) {
					return ArgScriptType.BLOCK;
				}
			}
		}
			
		return ArgScriptType.COMMAND;
	}

}
