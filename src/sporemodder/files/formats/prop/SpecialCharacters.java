package sporemodder.files.formats.prop;

import java.util.HashMap;
import java.util.Map;

public class SpecialCharacters {

	private static final HashMap<Character, String> CHARACTERS = new HashMap<Character, String>();
	static
	{
		CHARACTERS.put('!', "&#33;");
		CHARACTERS.put('"', "&#34;");
		CHARACTERS.put('#', "&#35;");
		CHARACTERS.put('$', "&#36;");
		CHARACTERS.put('%', "&#37;");
		CHARACTERS.put('&', "&#38;");
		CHARACTERS.put('\'', "&#39;");
		CHARACTERS.put('(', "&#40;");
		CHARACTERS.put(')', "&#41;");
		CHARACTERS.put('*', "&#42;");
		CHARACTERS.put('+', "&#43;");
		CHARACTERS.put(',', "&#44;");
		CHARACTERS.put('-', "&#45;");
		CHARACTERS.put('.', "&#46;");
		CHARACTERS.put('/', "&#47;");
		CHARACTERS.put(':', "&#58;");
		CHARACTERS.put(';', "&#59;");
		CHARACTERS.put('<', "&#60;");
		CHARACTERS.put('=', "&#61;");
		CHARACTERS.put('>', "&#62;");
		CHARACTERS.put('?', "&#63;");
		CHARACTERS.put('@', "&#64;");
		CHARACTERS.put('[', "&#91;");
		CHARACTERS.put('\\', "&#92;");
		CHARACTERS.put(']', "&#93;");
		CHARACTERS.put('^', "&#94;");
		CHARACTERS.put('_', "&#95;");
		CHARACTERS.put('`', "&#96;");
		CHARACTERS.put('{', "&#123;");
		CHARACTERS.put('|', "&#124");
		CHARACTERS.put('}', "&#125;");
		CHARACTERS.put('~', "&#126;");
	}
	
	
	public static String fixStringLiteral(String text) {
		for (Map.Entry<Character, String> entry : CHARACTERS.entrySet()) {
			text.replace(entry.getKey().toString(), entry.getValue());
		}
		
		return text;
	}
}
