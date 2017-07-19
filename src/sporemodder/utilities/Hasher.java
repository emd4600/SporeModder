package sporemodder.utilities;

import sporemodder.MainApp;
import sporemodder.utilities.names.NameRegistry;
import sporemodder.utilities.names.SimpleNameRegistry;

public class Hasher {
	
	public static SimpleNameRegistry UsedNames = null;
	
	/**
	 * Decodes the given String into the byte it represents. If str starts with <code>0x</code> or <code>#</code>, the 
	 * string will be treated as an hexadecimal number. 
	 * 
	 * @param str The string representation of the byte to decode.
	 * @return The byte value of the String str.
	 */
	public static byte decodeByte(String str) {
		byte result = 0;
		
		if (str.startsWith("0x")) {
			result = (byte) (Short.parseShort(str.substring(2), 16) & 0xFF);
		}
		else if (str.startsWith("#")) {
			result = (byte) (Short.parseShort(str.substring(1), 16) & 0xFF);
		}
		else if (str.endsWith("b")) {
			result = (byte) (Short.parseShort(str.substring(0, str.length() - 1), 2) & 0xFF);
		}
		else {
			result = Byte.parseByte(str);
		}
		
		return result;
	}
	/**
	 * Decodes the given String into the short it represents. If str starts with <code>0x</code> or <code>#</code>, the 
	 * string will be treated as an hexadecimal number. 
	 * 
	 * @param str The string representation of the short to decode.
	 * @return The short value of the String str.
	 */
	public static short decodeShort(String str) {
		short result = 0;
		
		if (str.startsWith("0x")) {
			result = (short) (Integer.parseInt(str.substring(2), 16) & 0xFFFF);
		}
		else if (str.startsWith("#")) {
			result = (short) (Integer.parseInt(str.substring(1), 16) & 0xFFFF);
		}
		else if (str.endsWith("b")) {
			result = (short) (Integer.parseInt(str.substring(0, str.length() - 1), 2) & 0xFFFF);
		}
		else {
			result = Short.parseShort(str);
		}
		
		return result;
	}
	/**
	 * Decodes the given String into the int it represents. If str starts with <code>0x</code> or <code>#</code>, the 
	 * string will be treated as an hexadecimal number. 
	 * 
	 * @param str The string representation of the int to decode.
	 * @return The int value of the String str.
	 */
	public static int decodeInt(String str) {
		int result = 0;
		
		if (str == null || str.length() == 0) {
			return 0;
		}
		
		if (str.startsWith("0x")) {
			result = Integer.parseUnsignedInt(str.substring(2), 16);
		}
		else if (str.startsWith("#")) {
			result = Integer.parseUnsignedInt(str.substring(1), 16);
		}
		else if (str.startsWith("$")) {
			//result = Hasher.stringToFNVHash(str.substring(1));
			result = Hasher.getFileHash(str.substring(1));
		}
		else if (str.endsWith("b")) {
			result = Integer.parseUnsignedInt(str.substring(0, str.length() - 1), 2);
		}
		else {
			result = Integer.parseInt(str);
		}
		
		return result;
	}
	
	public static String validateIntString(String text, int offset, String str) {
		if (str.length() == 0) {
			return str;
		}
		
		StringBuffer sb = new StringBuffer(text);
		sb.insert(offset, str);
		
		String result = sb.toString();
		
		if (result.equals("0x") || result.equals("#")) {
			// we need to be able to write this
			return str;
		}
		
		try {
			decodeInt(sb.toString());
			return str;
		}
		catch (NumberFormatException e) {
			return "";
		}
	}
	
	public static long decodeUInt(String str) {
		long result = 0;
		if (str.startsWith("0x")) {
			result = Long.parseLong(str.substring(2), 16);
		}
		else if (str.startsWith("#")) {
			result = Long.parseLong(str.substring(1), 16);
		}
		else if (str.startsWith("$")) {
			//result = Hasher.stringToFNVHash(str.substring(1));
			result = Hasher.getFileHash(str.substring(1)) & 0xFFFFFFFF;
		}
		else if (str.endsWith("b")) {
			result = Long.parseLong(str.substring(0, str.length() - 1), 2);
		}
		else {
			result = Integer.parseUnsignedInt(str) & 0xFFFFFFFFL;
		}
		
		return result;
	}
	
	public static String fillZeroInHexString(int num) {
		return String.format("%8s", Integer.toHexString(num)).replace(' ', '0');
	}
	
	public static int stringToFNVHash(String inputString) {
            char[] lower = inputString.toLowerCase().toCharArray();
            int rez = 0x811C9DC5;
            for (int i = 0; i < lower.length; i++) {
            	rez *= 0x1000193;
            	rez ^= lower[i];
            }
            return rez;
    }
	
	public static String hashToHex(int hash) {
		return "#" + fillZeroInHexString(hash).toUpperCase();
	}
	
	public static String hashToHex(int hash, String prefix) {
		return prefix + fillZeroInHexString(hash).toUpperCase();
	}
	
	
	public static String getFileName(int hash) {
		return getFileName(hash, "#");
	}
	
	public static String getTypeName(int hash) {
		return getTypeName(hash, "#");
	}
	
	public static String getPropName(int hash) {
		return getPropName(hash, "#");
	}
	
	public static String getFileName(int hash, String prefix) {
		String str = MainApp.getRegistry(NameRegistry.NAME_FILE).getName(hash);
		if (str != null) {
			return str;
		} else {
			if (UsedNames != null) {
				str = UsedNames.getName(hash);
				if (str != null) {
					return str;
				}
			}
			return prefix + fillZeroInHexString(hash).toUpperCase();
		}
	}
	
	public static String getTypeName(int hash, String prefix) {
		String str = MainApp.getRegistry(NameRegistry.NAME_TYPE).getName(hash);
		if (str != null) {
			return str;
		} else {
			return prefix + fillZeroInHexString(hash).toUpperCase();
		}
	}
	
	public static String getPropName(int hash, String prefix) {
		String str = MainApp.getRegistry(NameRegistry.NAME_PROP).getName(hash);
		if (str != null) {
			return str;
		} else {
			return prefix + fillZeroInHexString(hash).toUpperCase();
		}
	}
	
	
	public static int getFileHash(String name) {
		if (name == null) {
			return -1;
		}
		if (name.startsWith("#")) {
			if (name.length() == 1) {
				return -1;
			}
			return Integer.parseUnsignedInt(name.substring(1), 16);
		} else if (name.startsWith("0x")) {
			if (name.length() == 2) {
				return -1;
			}
			return Integer.parseUnsignedInt(name.substring(2), 16);
		} else {
			if (!name.endsWith("~")) {
				int hash = stringToFNVHash(name);
				if (UsedNames != null) {
					UsedNames.addAlias(name, hash);
				}
				return hash;
			} else {
				int i = MainApp.getRegistry(NameRegistry.NAME_FILE).getHash(name);
				if (i == -1) {
					throw new IllegalArgumentException("Unable to find " + name + " hash.  It doesn't exist or it's 0xFFFFFFFF");
				}
				if (UsedNames != null) {
					UsedNames.addAlias(name, i);
				}
				return i;
			}
		}
	}
	public static int getTypeHash(String name) {
		if (name.startsWith("#")) {
			return Integer.parseUnsignedInt(name.substring(1), 16);
		} else if (name.startsWith("0x")) {
			return Integer.parseUnsignedInt(name.substring(2), 16);
		} else {
			int i = MainApp.getRegistry(NameRegistry.NAME_TYPE).getHash(name);
			if (i == -1) {
				return stringToFNVHash(name);
			}
			return i;
		}
	}
	public static int getPropHash(String name) {
		if (name.startsWith("#")) {
			return Integer.parseUnsignedInt(name.substring(1), 16);
		} else if (name.startsWith("0x")) {
			return Integer.parseUnsignedInt(name.substring(2), 16);
		} else {
			int i = MainApp.getRegistry(NameRegistry.NAME_PROP).getHash(name);
			if (i == -1) {
				return stringToFNVHash(name);
			}
			return i;
		}
	}
	
	public static String getSPUIName(int hash) {
		String str = MainApp.getRegistry(NameRegistry.NAME_SPUI).getName(hash);
		if (str != null) {
			return str;
		} else {
			return "#" + fillZeroInHexString(hash).toUpperCase();
		}
	}
	
	public static String getGlobalName(int hash) {
		String str = MainApp.getRegistry(NameRegistry.NAME_FILE).getName(hash);
		if (str == null) {
			str = MainApp.getRegistry(NameRegistry.NAME_PROP).getName(hash);
			if (str == null) {
				str = MainApp.getRegistry(NameRegistry.NAME_TYPE).getName(hash);
				if (str == null) {
					return "#" + fillZeroInHexString(hash).toUpperCase();
				}
			}
		}
		return str;
	}
	
	public static int getGlobalHash(String name) {
		if (name.startsWith("#")) {
			return (int) Integer.parseUnsignedInt(name.substring(1), 16);
		} else if (name.startsWith("0x")) {
			return (int) Integer.parseUnsignedInt(name.substring(2), 16);
		} else {
			int i = MainApp.getRegistry(NameRegistry.NAME_FILE).getHash(name);
			if (i == -1) {
				i = MainApp.getRegistry(NameRegistry.NAME_PROP).getHash(name);
				if (i == -1) {
					i = MainApp.getRegistry(NameRegistry.NAME_TYPE).getHash(name);
					if (i == -1) {
						return stringToFNVHash(name);
					}
				}
			}
			return i;
		}
	}
	
	///////////////////////////
	///// CUSTOM REGISTRY /////
	///////////////////////////
	
	/**
	 * Returns the name equivalent of the given hash in the given registry, or an hexadecimal representation of the hash if no equivalent is found. 
	 * @param hash The hash to find.
	 * @param registry The registry where the hash will be searched.
	 * @return The string equivalent or an hexadecimal representation of the given hash in the given registry.
	 */
	public static String getName(int hash, NameRegistry registry) {
		String str = registry.getName(hash);
		if (str != null) {
			return str;
		} else {
			return "#" + fillZeroInHexString(hash).toUpperCase();
		}
	}
	
	
	public static int getHash(String name, NameRegistry registry) {
		if (name.startsWith("#")) {
			return (int) Integer.parseUnsignedInt(name.substring(1), 16);
		} else if (name.startsWith("0x")) {
			return (int) Integer.parseUnsignedInt(name.substring(2), 16);
		} else {
			int i = registry.getHash(name);
			if (i == -1) {
				return stringToFNVHash(name);
			}
			return i;
		}
	}
	
}
