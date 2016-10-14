package sporemodder.files.formats.effects;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import sporemodder.MainApp;
import sporemodder.files.FileStreamAccessor;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.FileFormatStructure;
import sporemodder.files.formats.FileStructure;
import sporemodder.files.formats.FileStructureError;
import sporemodder.files.formats.argscript.ArgScript;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.utilities.Hasher;

public class EffectMain extends FileStructure implements FileFormatStructure {
	
	/* particle	0x1 *
	 * metaParticle	0x2
	 * decal	0x3 *
	 * sequence	0x4 *
	 * sound	0x5 *
	 * shake	0x6 *
	 * camera	0x7 *
	 * model	0x8 *
	 * screen	0x9 *
	 * light	0xA *
	 * game		0xB *
	 * fastParticle	0xC *
	 * distribute	0xD
	 * ribbon	0xE *
	 * 
	 * brush	0x20 *
	 * terrainScript	0x21 *
	 * skinpaintSettings	0x22 *
	 * skinpaintDistribute	0x23 *
	 * 
	 * gameModel	0x25
	 * skinpaintParticle	0x26 *
	 * skinpaintFlood	0x27 *
	 * volume	0x28 *
	 * splitController	0x29 *
	 * terrainDistribute	0x2A *
	 * cloud	0x2B *
	 * groundCover	0x2C *
	 * mixEvent	0x2D *
	 * 
	 * text		0x2F *
	 */
	

	// All these classes must have getEnumTags(), getBlockTags(), getCommandTags() and getOptionTags()!!
	public static final Class<?>[] SUPPORTED_SH_STRUCTS = new Class<?>[] {
		BrushEffect.class,
		ModelEffect.class,
		ModelAnim.class,
		VisualEffectBlock.class,
		VisualEffect.class,
		CameraEffect.class,
		TextureSlot.class,
		DecalEffect.class,
		SkinpaintDistributeEffect.class,
		SkinpaintSettingsEffect.class,
		SequenceEffect.class,
		SkinpaintParticleEffect.class,
		LightEffect.class,
		RibbonEffect.class,
		TextEffect.class,
		SoundEffect.class,
		ShakeEffect.class,
		GameEffect.class,
		TerrainScriptEffect.class,
		VolumeEffect.class,
		ParticlePathPoint.class,
		ParticleEffect.class,
		Surface.class,
		MapResource.class,
		TerrainDistributeEffect.class,
		TerrainDistributeLevel.class,
		MixEventEffect.class,
		GroundCoverEffect.class,
		SplitControllerEffect.class,
		CloudEffect.class,
		FastParticleEffect.class,
		MaterialResource.class,
		SplitModelKernelResource.class,
		ScreenEffect.class,
		SkinpaintFloodEffect.class,
		DistributeEffect.class,
		MetaparticleEffect.class,
		GameModelEffect.class
	};
	
	protected static final int IMPORT_MASK = 0x7F000000;
	
	protected static final String UNKNOWN_KEYWORD = "UNKNOWN";
	
	private static final int SUPPORTED_VERSION = 4;
	
	private int version = SUPPORTED_VERSION;
	private TreeMap<Integer, List<Effect>> effects;
	private List<String> imports = new ArrayList<String>();
	// <Hash, index>
	private HashMap<Integer, Integer> exports = new HashMap<Integer, Integer>();
	
	public EffectMain() {
		effects = new TreeMap<Integer, List<Effect>>();
	}
	
	public EffectMain(InputStreamAccessor in) throws IOException {
		this();
		read(in);
	}
	
	public TreeMap<Integer, List<Effect>> getEffectMap() {
		return effects;
	}
	
	public List<String> getImports() {
		return imports;
	}
	
	public HashMap<Integer, Integer> getExports() {
		return exports;
	}
	
	public void addEffect(Effect effect, int type) {
		addEffect(effects, effect, type);
	}
	
	public static void addEffect(TreeMap<Integer, List<Effect>> effectMap, Effect effect, int type) {
		List<Effect> effectList = effectMap.get(type);
		
		if (effectList == null) {
			effectList = new ArrayList<Effect>();
			effectList.add(effect);
			effectMap.put(type, effectList);
		}
		else {
			effectList.add(effect);
		}
	}
	
	public int getEffectTypeCount(int type) {
		List<Effect> effectList = effects.get(type);
		return effectList == null ? 0 : effectList.size();
	}
	
	public List<Effect> getVisualEffects() {
		List<Effect> list = effects.get(VisualEffect.TYPE);
		if (list == null) {
			list = new ArrayList<Effect>();
			effects.put(VisualEffect.TYPE, list);
		}
		return list;
	}
	
	public void read(InputStreamAccessor in) throws IOException {
		
		version = in.readShort();
		if (version != SUPPORTED_VERSION) {
			throw new IOException("Unsupported EFFDIR version. Only version " + SUPPORTED_VERSION + " is supported.");
		}
		
		int type = in.readShort();
		while (type != -1) {
			
			effects.put(type, readComponents(in, type));
			
			type = in.readShort();
		}
		
		//TODO RESOURCES
		type = in.readShort();
		while(type != -1) {
			int version = in.readShort();
			int size = in.readInt();
			int pos = in.getFilePointer();
			int count = in.readInt();
			
			System.out.println("Resource -type " + Integer.toHexString(type) + " -version " + version + " -size " + size + " -count " + count);
			
			if (type == MapResource.TYPE) {
				List<Effect> resources = new ArrayList<Effect>();
				for (int i = 0; i < count; i++) {
					MapResource map = new MapResource();
					map.read(in);
					resources.add(map);
				}
				effects.put(Resource.TYPE_MASK | type, resources);
			}
			else if (type == MaterialResource.TYPE) {
				List<Effect> resources = new ArrayList<Effect>();
				for (int i = 0; i < count; i++) {
					MaterialResource map = new MaterialResource();
					map.read(in);
					resources.add(map);
				}
				effects.put(Resource.TYPE_MASK | type, resources);
			}
			else if (type == SplitModelKernelResource.TYPE) {
				List<Effect> resources = new ArrayList<Effect>();
				for (int i = 0; i < count; i++) {
					SplitModelKernelResource map = new SplitModelKernelResource(i);
					map.read(in);
					resources.add(map);
				}
				effects.put(Resource.TYPE_MASK | type, resources);
			}
			
			in.seek(pos + size);
			
			type = in.readShort();
		}
		
		expect(in.readShort(), 1, "EFF-002", in.getFilePointer());  // Unexpected VisualEffect version
		List<Effect> visualEffects = getVisualEffects();
		int visualEffectCount = in.readInt();
		for (int i = 0; i < visualEffectCount; i++) {
			//TODO get effect name from EffectList
			VisualEffect effect = new VisualEffect("effect-" + i);
			effect.read(in);
			effect.setParent(this);
//			effect.setEffectMap(effects);
			visualEffects.add(effect);
		}
		effects.put(0, visualEffects);
		
		int effectIndex = in.readInt();
		while (effectIndex != -1) {
			expect(in.readInt(), 0, "EFF-LIST-001", in.getFilePointer());
			int hash = in.readInt();
			if ((effectIndex & IMPORT_MASK) != IMPORT_MASK) {
				VisualEffect effect = (VisualEffect) visualEffects.get(effectIndex & 0xFFFFFF);
				effect.setName(Hasher.getFileName(hash));
				effect.setIsExported(true);
			}
			exports.put(hash, effectIndex);
			
			effectIndex = in.readInt();
		}
		
		in.readInt();  // String assignments, not used?
		
		int importCount = in.readInt();
		for (int i = 0; i < importCount; i++) {
			in.readInt();
			imports.add(Hasher.getFileName(in.readInt()));
		}
	}
	
	private List<Effect> readComponents(InputStreamAccessor in, int type) throws IOException {
		List<Effect> components = new ArrayList<Effect>();
		
		int version = in.readShort();
		int size = in.readInt();
		int count = in.readInt();
		
		String keyword = EffectMain.getKeyword(type);
		int position = in.getFilePointer();
		
		System.out.println(String.format("%1$s (0x%2$h) version: %3$d    pos: %4$d    size: %5$d    count: %6$d", 
				keyword, type, version, position, size, count));
		
		if (EffectComponent.SUPPORTED_COMPONENTS.containsKey(type)) {
			try {
				Class<? extends EffectComponent> clazz = EffectComponent.SUPPORTED_COMPONENTS.get(type);
				Constructor<? extends EffectComponent> constructor = clazz.getConstructor(int.class, int.class);
				
				if (version < EffectComponent.getMinVersion(clazz) || version > EffectComponent.getMaxVersion(clazz)) {
					addError("Effect version outside of range for effect component type " + keyword, position);
					in.seek(position + size - 4);
					return components;
				}
				
				for (int i = 0; i < count; i++) {
					EffectComponent component = constructor.newInstance(type, version);
					component.setName(String.format("%s-%d", keyword, i));
					component.position = in.getFilePointer();
					component.read(in);
//					component.setEffectMap(effectMap);
					component.setParent(this);
					components.add(component);
					List<FileStructureError> errors = component.getErrors();
					if (errors != null && errors.size() > 0) {
						System.out.println(FileStructureError.getErrorsString(errors));
					}
				}
				
				in.seek(position + size - 4);
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		// Unsupported type, we'll skip it
		else {
			System.err.println("Unsupported effect type 0x" + Integer.toHexString(type));
			in.seek(position + size - 4);  // it seeks after reading the size, not the count
		}
		
		return components;
	}
	
	public void write(OutputStreamAccessor out) throws IOException {
		out.writeShort(version);
		
		for (Map.Entry<Integer, List<Effect>> entry : effects.entrySet()) {
			if (entry.getKey() == VisualEffect.TYPE || (entry.getKey() & Resource.TYPE_MASK) == Resource.TYPE_MASK) continue;
			List<Effect> list = entry.getValue();
			
			int version = 1;
			int type = -1;
			if (list.size() > 0) {
				if (list.get(0) instanceof EffectComponent) {
					EffectComponent component = (EffectComponent) list.get(0);
					version = component.version;
					type = component.type;
				}
				
				out.writeShort(type);
				out.writeShort(version);
				out.writeInt(0);  // size
				int position = out.getFilePointer();
				out.writeInt(list.size());
				
				for (Effect effect : list) {
					effect.write(out);
				}
				
				int finalPos = out.getFilePointer();
				out.seek(position - 4);
				out.writeInt(finalPos - position);
				out.seek(finalPos);
			}
			else {
				continue;
			}
		}
		out.writeShort(-1);
		
		
		for (Map.Entry<Integer, List<Effect>> entry : effects.entrySet()) {
			if ((entry.getKey() & Resource.TYPE_MASK) == Resource.TYPE_MASK)  {
				List<Effect> list = entry.getValue();
				int version = 0;
				int type = entry.getKey() & ~Resource.TYPE_MASK;
				
				out.writeShort(type);
				out.writeShort(version);
				out.writeInt(0);  // size
				int position = out.getFilePointer();
				out.writeInt(list.size());
				
				for (Effect effect : list) {
					effect.write(out);
				}
				
				int finalPos = out.getFilePointer();
				out.seek(position - 4);
				out.writeInt(finalPos - position);
				out.seek(finalPos);
			}
		}
		out.writeShort(-1);
		
		List<Effect> visualEffects = getVisualEffects();
		out.writeShort(1);
		out.writeInt(visualEffects.size());
		
		for (Effect eff : visualEffects) {
			eff.write(out);
		}
		
//		for (Effect eff : visualEffects) {
//			VisualEffect visualEffect = (VisualEffect) eff;
//			if (visualEffect.isExported()) {
//				out.writeInt(getEffectIndex(effects, VisualEffect.TYPE, eff));
//				out.writeInt(0);
//				out.writeInt(Hasher.getFileHash(visualEffect.getName()));
//			}
//		}
		for (Map.Entry<Integer, Integer> entry : exports.entrySet()) {
			out.writeInt(entry.getValue());
			out.writeInt(0);
			out.writeInt(entry.getKey());
		}
		
		out.writeInt(-1);
		out.writeInt(-1);  // String assignments, not used?

		out.writeInt(imports.size());
		for (String name : imports) {
			out.writeInt(0);
			out.writeInt(Hasher.getFileHash(name));
		}
	}
	
	private void parseFile(File f) throws IOException, ArgScriptException {
		ArgScript reader = new ArgScript(f);
		reader.setParser(new EffectParser());
		reader.parse();
		
		EffectMain effectFile = new EffectMain();
//		List<String> imports = new ArrayList<String>();
//		TreeMap<Integer, List<Effect>> fileEffects = new TreeMap<Integer, List<Effect>>();
		TreeMap<Integer, Integer> typeOffsets = new TreeMap<Integer, Integer>();
		
		for (Map.Entry<Integer, List<Effect>> entry : effects.entrySet()) {
			int type = entry.getKey();
			typeOffsets.put(type, getEffectTypeCount(type));
		}
		
		// first we must get all the imports and resources
		
		Collection<ArgScriptCommand> commands = reader.getAllCommands();
		for (ArgScriptCommand c : commands) {
			if (c.getKeyword().equals("import")) {
				effectFile.getImports().add(c.getSingleArgument());
			}
			else {
				int type = getType(c.getKeyword());
				if (type != -1 && (type & Resource.TYPE_MASK) == Resource.TYPE_MASK) {
					Resource resource = null;
					int newType = type & ~Resource.TYPE_MASK;
					if (newType == MapResource.TYPE) {
						resource = new MapResource();
					}
					//TODO materials
					if (resource != null) {
						resource.parseCommand(c);
						addEffect(effectFile.getEffectMap(), resource, type);
					}
				}
			}
		}
		
		// an array to fix their indices later
		List<EffectComponent> effectComponents = new ArrayList<EffectComponent>();
		
		Collection<ArgScriptBlock> blocks = reader.getAllBlocks();
		for (ArgScriptBlock b : blocks) {
			
			int type = getType(b.getKeyword());
			List<String> args = b.getArguments(1, 3);
			if ((type & Resource.TYPE_MASK) == Resource.TYPE_MASK) {
				Resource resource = null;
				int newType = type & ~Resource.TYPE_MASK;
				if (newType == MapResource.TYPE) {
					resource = new MapResource();
				} else if (newType == MaterialResource.TYPE) {
					resource = new MaterialResource();
				}
				else if (newType == SplitModelKernelResource.TYPE) {
					resource = new SplitModelKernelResource(b.getSingleArgument());
				}
				if (resource != null) {
					resource.parse(b);
					addEffect(effectFile.getEffectMap(), resource, type);
				}
			}
			else if (EffectComponent.SUPPORTED_COMPONENTS.containsKey(type)) {
				EffectComponent effect = null;
				Class<? extends EffectComponent> clazz = EffectComponent.SUPPORTED_COMPONENTS.get(type);
				
				try {
					if (args.size() == 3) {
						Effect other = getEffect(effectFile.getEffectMap(), type, args.get(2));
						if (other == null) {
							throw new ArgScriptException("Can't extend effect component '" + args.get(2) + "' of type '" + b.getKeyword() + "' since it doesn't exist.");
						}
						Constructor<? extends EffectComponent> ctor = clazz.getConstructor(clazz);
						effect = ctor.newInstance(other);
					}
					else {
						Constructor<? extends EffectComponent> ctor = clazz.getConstructor(int.class, int.class);
						int version = 1;
						Field field = clazz.getField("MAX_VERSION");
						if (field != null) {
							version = field.getInt(null);
						}
						effect = ctor.newInstance(type, version);
					}
				} 
				catch (NoSuchMethodException | SecurityException | 
						InstantiationException | IllegalAccessException | 
						IllegalArgumentException | InvocationTargetException | NoSuchFieldException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					continue;
				} 
				
				if (effect != null) {
//					effect.setEffectMap(fileEffects);
					effect.setParent(effectFile);
					effect.setName(args.get(0));
					effect.parse(b);
					
//					int indexOffset = getEffectTypeCount(type);
//					if (indexOffset != -1) effect.fixEffectIndices(typeOffsets);
					
					effectComponents.add(effect);
					
					addEffect(effectFile.getEffectMap(), effect, type);
				}
			}
			else if (b.getKeyword().equals("effect")) {
				VisualEffect effect = new VisualEffect(args.get(0));
//				effect.setEffectMap(fileEffects);
				effect.setParent(effectFile);
				effect.parse(b);
				
				int indexOffset = getEffectTypeCount(type);
				if (indexOffset != -1) effect.fixEffectIndices(typeOffsets);
				
				addEffect(effectFile.getEffectMap(), effect, VisualEffect.TYPE);
			}
		}
		
		// fix EffectComponent indices
		for (EffectComponent effect : effectComponents) {
			effect.fixEffectIndices(typeOffsets);
		}
		
		int visualEffectOffset = getEffectTypeCount(VisualEffect.TYPE);
		for (ArgScriptCommand c : commands) {
			if (c.getKeyword().equals("export")) {
				List<String> args = c.getArguments(1, 2);
				VisualEffect effect = (VisualEffect) getEffect(effectFile.getEffectMap(), VisualEffect.TYPE, args.get(0));
				if (effect == null) {
					// maybe we're exporting an imported effect
					int indexOf = imports.indexOf(args.get(0));
					if (indexOf == -1) {
						throw new ArgScriptException("Can't export effect '" + args.get(0) + "' since it doesn't exist.");
					}
					else {
						if (args.size() < 2) {
							throw new ArgScriptException("Can't export effect '" + args.get(0) + "'. Exporting imported effects requires 2 arguments.");
						}
						exports.put(Hasher.getFileHash(args.get(1)), indexOf | IMPORT_MASK);
					}
				}
				else {
					effect.setIsExported(true);
					if (args.size() >= 2) {
						exports.put(Hasher.getFileHash(args.get(1)), visualEffectOffset + getEffectIndex(effectFile.getEffectMap(), VisualEffect.TYPE, effect));
					}
					else {
						exports.put(Hasher.getFileHash(effect.getName()), visualEffectOffset + getEffectIndex(effectFile.getEffectMap(), VisualEffect.TYPE, effect));
					}
				}
			}
		}
		
		// put our effects into the main effectMap
		for (Map.Entry<Integer, List<Effect>> entry : effectFile.getEffectMap().entrySet()) {
			int type = entry.getKey();
			List<Effect> list = effects.get(type);
			if (list == null) {
				list = new ArrayList<Effect>();
				effects.put(type, list);
			}
			list.addAll(entry.getValue());
		}
	}
	
	public void parse(File folder) throws IOException, ArgScriptException {
		File[] files = folder.listFiles();
		
		for (File f : files) {
			if (f.isFile()) {
				
				parseFile(f);
			}
		}
	}
	
	public void parse(String path) throws IOException, ArgScriptException {
		parse(new File(path));
	}
	
	public Effect getEffect(int type, int index) {
		if (type == VisualEffect.TYPE && (index & IMPORT_MASK) == IMPORT_MASK) {
			int ind = index & ~IMPORT_MASK;
			if (ind < 0 || ind >= imports.size()) {
				throw new IllegalArgumentException("Can't import effect index " + ind);
			}
			return new ImportedEffect(imports.get(ind));
		}
		return EffectMain.getEffect(effects, type, index);
	}
	
	public int getEffectIndex(int type, String name) throws ArgScriptException {
		return EffectMain.getEffectIndex(effects, imports, type, name);
	}
	
	// MapResource and MaterialResource, use getEffect for SplitModelKernelResource
	public Resource getResource(int type, ResourceID resourceID) {
		List<Effect> list = effects.get(type);
		if (list != null) {
			int size = list.size();
			for (int i = 0; i < size; i++) {
				Resource r = (Resource) list.get(i);
				if (r.resourceID.compare(resourceID)) {
					return r;
				}
			}
		}
		return null;
	}
	
	public static String getKeyword(int type) {
		if (type == VisualEffect.TYPE) {
			return VisualEffect.KEYWORD;
		} 
		else if ((type & Resource.TYPE_MASK) == Resource.TYPE_MASK) {
			int newType = type & ~Resource.TYPE_MASK;
			if (newType == MapResource.TYPE) return MapResource.KEYWORD;
			else if (newType == MaterialResource.TYPE) return MaterialResource.KEYWORD;
			else if (newType == SplitModelKernelResource.TYPE) return SplitModelKernelResource.KEYWORD;
		}
		else if (EffectComponent.SUPPORTED_COMPONENTS.containsKey(type)) {
			Field keywordField;
			try {
				keywordField = EffectComponent.SUPPORTED_COMPONENTS.get(type).getField("KEYWORD");
				if (keywordField != null) {
					return (String) keywordField.get(null);  // it's a static field so we use null;
				}
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return UNKNOWN_KEYWORD;
	}
	
	public static int getType(String keyword) {
		if (keyword.equals(VisualEffect.KEYWORD)) {
			return VisualEffect.TYPE;
		} 
		else if (keyword.equals(MapResource.KEYWORD)) {
			return MapResource.MASKED_TYPE;
		}
		else if (keyword.equals(MaterialResource.KEYWORD)) {
			return MaterialResource.MASKED_TYPE;
		}
		else if (keyword.equals(SplitModelKernelResource.KEYWORD)) {
			return SplitModelKernelResource.MASKED_TYPE;
		}
		//TODO materials
		else {
			for (Class<? extends EffectComponent> clazz : EffectComponent.SUPPORTED_COMPONENTS.values()) {
				Field keywordField;
				try {
					keywordField = clazz.getField("KEYWORD");
					// it's a static field so we use null;
					if (keywordField != null && keywordField.get(null).equals(keyword)) {
						return clazz.getField("TYPE").getInt(null); 
					}
				} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return -1;
	}
	
	public static final int getEffectIndex(TreeMap<Integer, List<Effect>> effectMap, int type, Effect effect) {
		if (effect == null) return -1;
		List<Effect> map = effectMap.get(type);
		if (map == null) {
			throw new IllegalArgumentException("Effect component '" + effect.getName() + "' of type " + getKeyword(type) + " doesn't exist.");
		}
		int index = 0;
		for (Effect eff : map) {
			if (eff == effect) {
				break;
			}
			index++;
		}
		
		if (index == map.size()) {
			throw new IllegalArgumentException("Effect component '" + effect.getName() + "' of type " + getKeyword(type) + " doesn't exist.");
		}
		
		return index;
	}
	
	public static final Effect getEffect(TreeMap<Integer, List<Effect>> effectMap, String keyword, String name) throws ArgScriptException {
		return getEffect(effectMap, getType(keyword), name);
	}
	
	public static final Effect getEffect(TreeMap<Integer, List<Effect>> effectMap, int type, String name) throws ArgScriptException {
		List<Effect> map = effectMap.get(type);
		for (Effect eff : map) {
			if (eff.getName().equals(name)) {
				return eff;
			}
		}
		return null;
	}
	
	public static final int getEffectIndex(TreeMap<Integer, List<Effect>> effectMap, String keyword, String name) throws ArgScriptException {
		List<Effect> map = effectMap.get(getType(keyword));
		int index = 0;
		for (Effect eff : map) {
			if (eff.getName().equals(name)) {
				break;
			}
			index++;
		}
		
		if (index == map.size()) {
			throw new ArgScriptException("Effect component '" + name + "' of type " + keyword + " doesn't exist.");
		}
		
		return index;
	}
	
	public static final int getEffectIndex(TreeMap<Integer, List<Effect>> effectMap, int type, String name) throws ArgScriptException {
		List<Effect> map = effectMap.get(type);
		int index = 0;
		for (Effect eff : map) {
			if (eff.getName().equals(name)) {
				break;
			}
			index++;
		}
		
		if (index == map.size()) {
			throw new ArgScriptException("Effect component '" + name + "' of type 0x" + Integer.toHexString(type) + " doesn't exist.");
		}
		
		return index;
	}
	
	public static final int getEffectIndex(TreeMap<Integer, List<Effect>> effectMap, List<String> imports, int type, String name) throws ArgScriptException {
		if (imports != null) {
			for (int i = 0; i < imports.size(); i++) {
				if (name.equals(imports.get(0))) {
					return i | IMPORT_MASK;
				}
			}
		}
		List<Effect> map = effectMap.get(type);
		int index = 0;
		for (Effect eff : map) {
			if (eff.getName().equals(name)) {
				break;
			}
			index++;
		}
		
		if (index == map.size()) {
			throw new ArgScriptException("Effect component '" + name + "' of type 0x" + Integer.toHexString(type) + " doesn't exist.");
		}
		
		return index;
	}
	
	public static final Effect getEffect(TreeMap<Integer, List<Effect>> effectMap, int type, int index) {
		List<Effect> list = effectMap.get(type);
		if (list != null) {
			return list.get(index);
		}
		return null;
	}
	
	@Deprecated
	private void writeEffectsPfxExtended(String path) throws IOException {
		for (Map.Entry<Integer, List<Effect>> componentType : effects.entrySet()) 
		{
			if (componentType.getKey() == 0) continue;
			String typeKeyword = getKeyword(componentType.getKey());
			
			List<Effect> typeComponents = componentType.getValue();
			int size = typeComponents.size();
			for (int i = 0; i < size; i++)
			{
				EffectComponent component = (EffectComponent) typeComponents.get(i);
				try (BufferedWriter out = new BufferedWriter(new FileWriter(path + component.getName() + ".pfx"))) 
				{
					if (component.supportsBlock()) {
						ArgScriptBlock block = new ArgScriptBlock(typeKeyword, component.getName());
						component.toBlock(block);
						out.write(block.toString(0));
						
						out.newLine();
						out.write(FileStructureError.getErrorsString(component.getErrors()));
						
						// for debugging only
						out.write("// pos: " + component.position);
					}
//					else {
//						//TODO if it doesn't support block, it should be written in the visual effect instead 
//						out.write(component.toCommand().toString());
//					}
				}
			}
		}
		
		//TODO Resources
		
		List<Effect> visualEffects = getVisualEffects();
		for (int i = 0; i < visualEffects.size(); i++) 
		{
			VisualEffect effect = (VisualEffect) visualEffects.get(i);
			try (BufferedWriter out = new BufferedWriter(new FileWriter(path + effect.getName() + ".pfx"))) 
			{
				writeEffectPfx(effect, out);
			}
		}
	}
	
	private static void writeEffectPfx(VisualEffect effect, BufferedWriter out) throws IOException {
		out.write(effect.toBlock().toString(0));
		out.newLine();
		
		if (effect.isExported()) {
			out.write("export " + effect.getName());
			out.newLine();
		}
		
		List<FileStructureError> errors = effect.getErrors();
		if (errors.size() > 0) out.write(FileStructureError.getErrorsString(errors, "// "));
		
		// for debugging only
		out.write("// pos: " + effect.position);
	}
	
	private static boolean alreadyMeasured = false;
//	private void writeEffectsPfxRecursive(Effect effect, BufferedWriter out, HashMap<Integer, BitSet> bitSets, List<Effect> writtenEffects) throws IOException {
////		if (!alreadyMeasured) {
////			// we get all the effects used by that effect
////			Effect[] usedEffects = effect.getEffects();
////			
////			for (Effect eff : usedEffects) {
////				// unknown effect types are returned as null
////				if (eff != null) {
////					writeEffectsPfxRecursive(eff, out, bitSets);
////				}
////			}
////			
////			if (effect.supportsBlock()) {
////				long timeToBlock1 = System.nanoTime();
////				ArgScriptBlock block = effect.toBlock();
////				timeToBlock1 = System.nanoTime() - timeToBlock1;
////				System.out.println("toBlock: " + timeToBlock1);
////				
////				long timeToString1 = System.nanoTime();
////				String str = block.toString(0);
////				timeToString1 = System.nanoTime() - timeToString1;
////				System.out.println("toString: " + timeToString1);
////				
////				long timeWrite1 = System.nanoTime();
////				out.write(str);
////				out.newLine();
////				timeWrite1 = System.nanoTime() - timeWrite1;
////				System.out.println("write: " + timeWrite1);
////			}
////			
////			long timeBitSets1 = System.nanoTime();
////			bitSets.get(effect.getType()).set(effects.get(effect.getType()).indexOf(effect));
////			timeBitSets1 = System.nanoTime() - timeBitSets1;
////			System.out.println("bitSets: " + timeBitSets1);
////			
////			alreadyMeasured = true;
////		}
////		else {
//			// we get all the effects used by that effect
//		if (effect instanceof ImportedEffect) {
//			
//		}
//		
//			Effect[] usedEffects = effect.getEffects();
//			
//			for (Effect eff : usedEffects) {
//				// unknown effect types are returned as null
//				if (eff != null && !writtenEffects.contains(eff)) {
//					writeEffectsPfxRecursive(eff, out, bitSets, writtenEffects);
//				}
//			}
//			
////			if (!(effect instanceof VisualEffect)) {
//				if (effect.supportsBlock()) {
//					ArgScriptBlock block = effect.toBlock();
//					out.write(block.toString(0));
//					out.newLine();
//				}
//				
////			}
//			
//			bitSets.get(effect.getType()).set(effects.get(effect.getType()).indexOf(effect));
//			writtenEffects.add(effect);
////		}
//	}
	
	private void writeEffectsPfxRecursive(Effect effect, BufferedWriter out, HashMap<Integer, BitSet> bitSets, List<Effect> writtenEffects) throws IOException {
		if (effect instanceof ImportedEffect) {
			out.write(effect.toCommand().toString());
			out.newLine();
			out.newLine();
		}
		else {
			Effect[] usedEffects = effect.getEffects();
			
			if (usedEffects != null) {
				for (Effect eff : usedEffects) {
					// unknown effect types are returned as null
					if (eff != null && !writtenEffects.contains(eff)) {
						writeEffectsPfxRecursive(eff, out, bitSets, writtenEffects);
					}
				}
			}
			
//			if (effect instanceof TerrainDistributeEffect) {
//				((TerrainDistributeEffect) effect).print();
//			}
			
			if (effect.supportsBlock()) {
				ArgScriptBlock block = effect.toBlock();
				out.write(block.toString(0));
				out.newLine();
			} else {
				if (effect instanceof MapResource) {
					out.write(effect.toCommand().toString());
					out.newLine();
					out.newLine();
				}
			}
				
			bitSets.get(effect.getType()).set(effects.get(effect.getType()).indexOf(effect));
			writtenEffects.add(effect);
		}
	}
	
	private void writeEffectsPfxRecursiveFast(Effect effect, BufferedWriter out, HashMap<Integer, BitSet> bitSets) throws IOException {
		// we get all the effects used by that effect
		Effect[] usedEffects = effect.getEffects();
		
		for (Effect eff : usedEffects) {
			// unknown effect types are returned as null
			if (eff != null) {
				writeEffectsPfxRecursiveFast(eff, out, bitSets);
			}
		}
		
		if (!(effect instanceof VisualEffect)) {
			if (effect.supportsBlock()) {
				effect.write(out, 1);
				out.newLine();
			}
		}
		
		bitSets.get(effect.getType()).set(effects.get(effect.getType()).indexOf(effect));
	}
	
//	private static void writeEffectBlocksPfx(VisualEffect effect, BufferedWriter out) throws IOException {
//		VisualEffectBlock[] blocks = effect.getEffectBlocks();
//		
//		for (VisualEffectBlock block : blocks) {
//			Effect eff = block.getEffect();
//			if (eff != null && eff.supportsBlock()) {
//				if (eff instanceof VisualEffect) {
//					VisualEffect visualEffect = (VisualEffect) eff;
//					
//					writeEffectBlocksPfx(visualEffect, out);
//					
//					writeEffectPfx(visualEffect, out);
//				}
//				else {
//					EffectComponent component = (EffectComponent) eff;
//					
//					ArgScriptBlock b = new ArgScriptBlock(EffectComponent.getKeyword(component.type), component.getName());
//					component.toBlock(b);
//					out.write(b.toString(0));
//					out.newLine();
//				}
//			}
//			
//		}
//	}
	
	private void writeEffectsPfxCompact(String path) throws IOException {
		
		//TODO Resources
		
		// Here we store whether the effects have been written or not, so we can write the ones that aren't used
		HashMap<Integer, BitSet> bitSets = new HashMap<Integer, BitSet>();
		for (Map.Entry<Integer, List<Effect>> entry : effects.entrySet()) {
			bitSets.put(entry.getKey(), new BitSet(entry.getValue().size()));
		}
		
		List<Effect> visualEffects = getVisualEffects();
//		for (int i = 0; i < visualEffects.size(); i++) 
//		{
//			VisualEffect effect = (VisualEffect) visualEffects.get(i);
//			if (effect.isExported()) {
//				try (BufferedWriter out = new BufferedWriter(new FileWriter(path + effect.getName() + ".pfx"))) 
//				{
//	//				writeEffectBlocksPfx(effect, out);
//	//
//	//				writeEffectPfx(effect, out);
//					List<Effect> writtenEffects = new ArrayList<Effect>();
//					writeEffectsPfxRecursive(effect, out, bitSets, writtenEffects);
//					
//					out.write("export " + effect.getName());
//					out.newLine();
//				}
//			}
//		}
		
		for (Map.Entry<Integer, Integer> entry : exports.entrySet()) {
				
			String name = Hasher.getFileName(entry.getKey());
			try (BufferedWriter out = new BufferedWriter(new FileWriter(path + name + ".pfx")))
			{
				int index = entry.getValue();
				if ((index & IMPORT_MASK) == IMPORT_MASK) {
					index = index & ~IMPORT_MASK;
					out.write("import " + imports.get(index));
					out.newLine();
					out.write("export " + imports.get(index) + " " + name);
					out.newLine();
				}
				else {
					VisualEffect effect = (VisualEffect) visualEffects.get(index);
					List<Effect> writtenEffects = new ArrayList<Effect>();
					writeEffectsPfxRecursive(effect, out, bitSets, writtenEffects);
					
					if (name.equals(effect.getName())) {
						out.write("export " + effect.getName());
					}
					else {
						out.write("export " + effect.getName() + " " + name);
					}
					out.newLine();
				}
			}
		}
		
		// first try to write the missing visual effects, then the rest
		// do them in reverse order, because usually effects use other effects with lower indices 
		BitSet missingVEffectsBitSets = bitSets.get(VisualEffect.TYPE);
		int visualEffectCount = visualEffects.size(); 
		for (int i = visualEffectCount - 1; i >= 0; i--) {
			if (!missingVEffectsBitSets.get(i)) {
				Effect effect = visualEffects.get(i);
				if (effect.supportsBlock()) {
					try (BufferedWriter out = new BufferedWriter(new FileWriter(path + effect.getName() + ".pfx"))) 
					{
						List<Effect> writtenEffects = new ArrayList<Effect>();
						writeEffectsPfxRecursive(effect, out, bitSets, writtenEffects);
					}
				}
			}
		}
		
		for (Map.Entry<Integer, BitSet> entry : bitSets.entrySet()) {
			if (entry.getKey() == VisualEffect.TYPE) continue;
			List<Effect> list = effects.get(entry.getKey());
			int effectCount = list.size();
			for (int i = effectCount - 1; i >= 0; i--) {
				if (!entry.getValue().get(i)) {
					
					Effect effect = list.get(i);
					if (effect.supportsBlock()) {
						try (BufferedWriter out = new BufferedWriter(new FileWriter(path + effect.getName() + ".pfx"))) 
						{
//							out.write(effect.toBlock().toString(0));
//							out.newLine();
//							
//							List<FileStructureError> errors = ((FileStructure)effect).getErrors();
//							if (errors.size() > 0) out.write(FileStructureError.getErrorsString(errors, "// "));
							
							List<Effect> writtenEffects = new ArrayList<Effect>();
							writeEffectsPfxRecursive(effect, out, bitSets, writtenEffects);
						}
					}
				}
			}
		}
	}
	
	public void writeEffectsPfx(String folder) throws IOException 
	{
		// append \\ at the end if it isn't present
		String path = folder.endsWith("\\") ? folder : folder + "\\";
		
//		if (compactMode) {
//			writeEffectsPfxCompact(path);
//		}
//		else {
//			writeEffectsPfxExtended(path);
//		}
		
		writeEffectsPfxCompact(path);
	}
	
	public void writeEffectsPfxFast(String folder) throws IOException 
	{
		// append \\ at the end if it isn't present
		String path = folder.endsWith("\\") ? folder : folder + "\\";
		
		writeEffectsPfxCompact(path);
	}
	
	public void writeLog(File folder) throws IOException {
		this.writeEffectsPfx(folder.getAbsolutePath());
	}
	
	public static int getEffectIndex(LinkedHashMap<String, LinkedHashMap<String, Integer>> effects, String name, String type) {
		LinkedHashMap<String, Integer> map = effects.get(type);
		if (map == null) return -1;
		else return map.get(name);
	}
	
	/* --- PACKING ---*/
	
	public static EffectMain packEffdir(File inputFolder, OutputStreamAccessor out) throws IOException, ArgScriptException {
		EffectMain effdir = new EffectMain();
		effdir.parse(inputFolder);
		effdir.write(out);
		return effdir;
	}
	
	public static EffectMain packEffdir(String inputFolder, OutputStreamAccessor out) throws IOException, ArgScriptException {
		return packEffdir(new File(inputFolder), out);
	}
	
	public static EffectMain packEffdir(File inputFolder, File outputFile) throws IOException, ArgScriptException {
		try (OutputStreamAccessor out = new FileStreamAccessor(outputFile, "rw", true)) {
			return packEffdir(inputFolder, out);
		} catch (Exception e) {
			throw e;
		}
	}
	
	public static EffectMain packEffdir(String inputFolder, File outputFile) throws IOException, ArgScriptException {
		try (OutputStreamAccessor out = new FileStreamAccessor(outputFile, "rw", true)) {
			return packEffdir(new File(inputFolder), outputFile);
		} catch (Exception e) {
			throw e;
		}
	}
	
	public static EffectMain packEffdir(File inputFolder, String outputFile) throws IOException, ArgScriptException {
		try (OutputStreamAccessor out = new FileStreamAccessor(outputFile, "rw", true)) {
			return packEffdir(inputFolder, out);
		} catch (Exception e) {
			throw e;
		}
	}
	
	public static EffectMain packEffdir(String inputFolder, String outputFile) throws IOException, ArgScriptException {
		try (OutputStreamAccessor out = new FileStreamAccessor(outputFile, "rw", true)) {
			return packEffdir(new File(inputFolder), out);
		} catch (Exception e) {
			throw e;
		}
	}
	
	
	/* --- UNPACING --- */
	
	public static EffectMain unpackEffdir(InputStreamAccessor in, String outputFolder) throws IOException {
		EffectMain effdir = new EffectMain();
		effdir.read(in);
		File folder = new File(outputFolder);
		if (!folder.exists()) {
			folder.mkdir();
		}
		effdir.writeEffectsPfx(outputFolder);
		return effdir;
	}
	
	public static EffectMain unpackEffdir(File inputFile, String outputFolder) throws IOException {
		try (InputStreamAccessor in = new FileStreamAccessor(inputFile, "r")) {
			return unpackEffdir(in, outputFolder);
		} catch (Exception e) {
			throw e;
		}
	}
	
	public static EffectMain unpackEffdir(String inputFile, String outputFolder) throws IOException {
		try (InputStreamAccessor in = new FileStreamAccessor(inputFile, "r")) {
			return unpackEffdir(in, outputFolder);
		} catch (Exception e) {
			throw e;
		}
	}
	
	
	/* --------------------------- */
	
	public static void main(String[] args) throws IOException {
		MainApp.init();
		
		long time1 = System.currentTimeMillis();
		
		String path = "E:\\Eric\\SporeMaster 2.0 beta\\spore.unpacked\\gameEffects_3~\\";
//		String file = "csa.effdir";
//		String file = "planet.effdir";
		String file = "games.effdir";
//		String file = "editors.effdir";
//		String file = "base.effdir";
		
//		String outputPath = "E:\\Eric\\SporeModder\\Projects\\Effects_Planet\\planet.effdir\\";
		String outputPath = "E:\\Eric\\SporeModder\\Projects\\Effects_Games\\games.effdir\\";
//		String outputPath = "E:\\Eric\\SporeModder\\Projects\\Effects_Editors\\editors.effdir\\";
//		String outputPath = "E:\\Eric\\SporeModder\\Projects\\Effects_CSA\\csa.effdir\\";
//		String outputPath = "E:\\Eric\\SporeModder\\Projects\\Effects_Base\\base.effdir\\";
		
		try (FileStreamAccessor in = new FileStreamAccessor(path + file, "r");)
		{
			EffectMain effdir = new EffectMain(in);
			
			effdir.writeEffectsPfx(outputPath);
			
//			effdir.writeLog("E:\\Eric\\SporeMaster 2.0 beta\\csa_effects.package.unpacked\\" + file + "\\");
		}
		
//		String inputFolder = "E:\\Eric\\SporeMaster 2.0 beta\\CustomSkinpaint.package.unpacked\\SkinpaintTest_Effects.effdir\\";
//		String outputFolder = "E:\\Eric\\SporeMaster 2.0 beta\\CustomSkinpaint.package.unpacked\\ep1_effects_3~\\";
//		String outputFile = "SkinpaintTest.effdir";
//		
//		try (FileStreamAccessor out = new FileStreamAccessor(outputFolder + outputFile, "rw")) {
//			
//			EffectMain effdir = new EffectMain();
//			effdir.parse(inputFolder);
//			effdir.write(out);
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		String inputFolder2 = "E:\\Eric\\SporeMaster 2.0 beta\\CustomSkinpaint.package.unpacked\\ep1_effects_3~\\";
//		String inputFile2 = "SkinpaintTest.effdir";
//		String outputFolder2 = "E:\\Eric\\SporeModder\\Projects\\SkinpaintTest\\unpacked_effects\\";
//		
//		try (FileStreamAccessor in = new FileStreamAccessor(inputFolder2 + inputFile2, "r")) {
//			EffectMain effdir = new EffectMain(in);
//			effdir.writeEffectsPfx(outputFolder2, true);
//		}
		
		System.out.println((System.currentTimeMillis() - time1) / 1000.0f + " seconds");
		
		
		/* --- SPEED TEST --- */
		
		
//		String path = "E:\\Eric\\SporeMaster 2.0 beta\\spore.unpacked\\gameEffects_3~\\";
////		String file = "csa.effdir";
//		String file = "planet.effdir";
//		
//		String outputPath = "E:\\Eric\\SporeModder\\Projects\\Effects_Planet\\planet_decals\\";
//		String outputPathFast = "E:\\Eric\\SporeModder\\Projects\\Effects_Planet\\planet_decals_fast\\";
////		String outputPath = "E:\\Eric\\SporeModder\\Projects\\Effects_CSA\\csa.effdir\\";
//		
//		long time1 = System.currentTimeMillis();
//		
//		try (FileStreamAccessor in = new FileStreamAccessor(path + file, "r");)
//		{
//			EffectMain effdir = new EffectMain(in);
//			
//			effdir.writeEffectsPfx(outputPath, true);
//		}
//		
//		time1 = System.currentTimeMillis() - time1;
//		
//		long time2 = System.currentTimeMillis();
//		
//		try (FileStreamAccessor in = new FileStreamAccessor(path + file, "r");)
//		{
//			EffectMain effdir = new EffectMain(in);
//			
//			effdir.writeEffectsPfxFast(outputPathFast);
//		}
//		
//		time2 = System.currentTimeMillis() - time2;
//		
//		System.out.println("ArgScript: " + time1 / 1000.0f + " seconds (" + time1 + " ms)");
//		System.out.println("Fast method: " + time2 / 1000.0f + " seconds (" + time2 + " ms)");
	}

	@Override
	public List<FileStructureError> getAllErrors() {
		return null;
	}
	
}
