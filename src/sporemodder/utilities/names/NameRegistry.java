package sporemodder.utilities.names;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import sporemodder.utilities.Hasher;

public class NameRegistry {
	
	public static final String NAME_FILE = "FILE";
	public static final String NAME_TYPE = "TYPE";
	public static final String NAME_PROP = "PROPERTY";
	public static final String NAME_SPUI = "SPUI";
	
	protected static final String DEFAULT_GROUP = "$_DEFAULT_GROUP";
	
	protected static final String TOKEN_END = "end";
	protected static final String TOKEN_IF = "if";
	protected static final String TOKEN_ELSE = "else";
	protected static final String TOKEN_OBSOLETE = "obsolete";
	protected static final String TOKEN_GROUP = "group";
	protected static final String TOKEN_ENDGROUP = "endgroup";  // are we going to use this one?

	protected final List<ConditionedNameRegistry> subregs = new ArrayList<ConditionedNameRegistry>();
	
	private final HashMap<String, NameRegistry> groups = new HashMap<String, NameRegistry>();
	private final HashMap<String, Integer> hashes = new HashMap<String, Integer>();
	private final HashMap<Integer, String> names = new HashMap<Integer, String>();
	
	public NameRegistry(String path) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(path));
		try {
			read(in);
		} finally {
			in.close();
		}
	}
	
	public NameRegistry(BufferedReader in) throws IOException {
		read(in);
	}
	
	public NameRegistry() {
		// TODO Auto-generated constructor stub
	}
	
	public NameRegistry getGroup(String group) {
		return groups.get(group);	
	}
	
	public boolean addName(String name) {	
		names.put(Hasher.stringToFNVHash(name), name);
		
		return true;
	}
	
	public boolean addAlias(String name, int hash) {
		hashes.put(name, hash);
		names.put(hash, name);
		
		return true;
	}
	
	public String getName(int hash) {
		String name = null;
		name = names.get(hash);
		if (name == null) {
			for (NameRegistry registry : groups.values()) {
				String result = registry.getName(hash);
				if (result != null) return result;
			}
			for (ConditionedNameRegistry registry : subregs) {
				String result = registry.getName(hash);
				if (result != null) return result;
			}
		}
		return name;
	}
	
	public int getHash(String name) {
		Integer hash = hashes.get(name);
		if (hash == null) {
			for (NameRegistry registry : groups.values()) {
				int result = registry.getHash(name);
				if (result != -1) return result;
			}
			for (ConditionedNameRegistry registry : subregs) {
				int result = registry.getHash(name);
				if (result != -1) return result;
			}
			return -1;
		}
		return hash;
	}
	
	public void clear() {
		hashes.clear();
		names.clear();
		groups.clear();
	}
	
	public List<String> getNames() {
		List<String> result = new ArrayList<String>(names.values());
		for (ConditionedNameRegistry registry : subregs) {
			result.addAll(registry.getNames());
		}
		return result;
	}
	
	protected void read(BufferedReader in) throws IOException {
		String line;

		while ((line = in.readLine()) != null) {
			
			String str = line.split("//")[0].trim();
			
			if (str.length() == 0) continue;
			
			// Special case, tokens
			if (str.startsWith("#")) {
				String tokenStr = str.substring(1);
				
				if (tokenStr.startsWith(TOKEN_OBSOLETE)) {
					clear();
				}
				else if (tokenStr.startsWith(TOKEN_IF)) {
					ConditionedNameRegistry registry = new ConditionedNameRegistry(in, tokenStr.substring(2).trim());
					subregs.add(registry);
				}
				else if (tokenStr.startsWith(TOKEN_GROUP)) {
					NameRegistry reg = new NameRegistry(in);
					groups.put(tokenStr.split(" ")[1], reg);
				}
				else if (tokenStr.startsWith(TOKEN_END)) {
					// Stop reading this name registry. This works with nested registries too
					break;
				}
			}
			else {
				parseEntry(str);
			}
		}
		
//		System.out.println(groups);
	}
	
	protected void parseEntry(String str) {
		String[] strings = str.split("\t");
		String name = strings[0].trim();
		if (strings.length < 2) {
			int hash = Hasher.stringToFNVHash(name);
			names.put(hash, name);
		} else {
			String hashStr = strings[1].trim();
			int hash = Hasher.decodeInt(hashStr);
			names.put(hash, name);
			hashes.put(name, hash);
		}
	}
	
	private static final int TEST_COUNT = 5;
	private static final List<Integer> TEST_HASHES = Arrays.asList(
			// creature, creature_editor_palette~, genericDirLitAnimblendTileParticle_shader, sfx_war_missile3_launch
			0x9EA3031A, 0x406B6B0C, 0xB26C413E, 0x5016B9A3
			);
	private static final List<Long> TIME_NORMAL = new ArrayList<Long>();
	private static final List<Long> TIME_FAST = new ArrayList<Long>();
	
//	public static void main(String[] args) throws IOException {
//		String path = "E:\\Eric\\Eclipse Projects\\SporeModder\\reg_file.txt";
//		OldNameRegistry normal = new OldNameRegistry(path);
//		NameRegistry fast = new NameRegistry(path);
//		
//		for (int i = 0; i < TEST_COUNT; i++) {
//			for (int hash : TEST_HASHES) {
//				long time1 = System.nanoTime();
//				String name1 = normal.getName(hash);
//				time1 = System.nanoTime() - time1;
//				
//				long time2 = System.nanoTime();
//				String name2 = fast.getName(hash);
//				time2 = System.nanoTime() - time2;
//				
//				System.out.println(name1 + "\tnormal: " + time1);
//				System.out.println(name2 + "\tfast:   " + time2);
//				TIME_NORMAL.add(time1);
//				TIME_FAST.add(time2);
//			}
//		}
//		
//		System.out.println();
//		System.out.println(" -- AVERAGES -- ");
//		System.out.println();
//		long sum = 0;
//		for (Long l : TIME_NORMAL) sum += l;
//		double averageNormal = (sum / (double) TIME_NORMAL.size());
//		System.out.println("Average normal: " + averageNormal);
//		
//		sum = 0;
//		for (Long l : TIME_FAST) sum += l;
//		double averageFast = (sum / (double) TIME_FAST.size());
//		System.out.println("Average fast:   " + averageFast);
//		
//		System.out.println("DoubleMapNameRegistry is " + averageNormal / averageFast + " times faster than NameRegistry.");
//	}
}
