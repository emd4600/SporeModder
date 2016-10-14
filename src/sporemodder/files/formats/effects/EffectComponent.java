package sporemodder.files.formats.effects;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.TreeMap;

import sporemodder.files.formats.FileStructure;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;


public abstract class EffectComponent extends FileStructure implements Effect {
	
	public void write(BufferedWriter out, int level) throws IOException {
		return;
	}
	
	public static final HashMap<Integer, Class<? extends EffectComponent>> SUPPORTED_COMPONENTS = new HashMap<Integer, Class<? extends EffectComponent>>();
	static {
		SUPPORTED_COMPONENTS.put(ParticleEffect.TYPE, ParticleEffect.class);
		SUPPORTED_COMPONENTS.put(MetaparticleEffect.TYPE, MetaparticleEffect.class);
		SUPPORTED_COMPONENTS.put(DecalEffect.TYPE, DecalEffect.class);
		SUPPORTED_COMPONENTS.put(SequenceEffect.TYPE, SequenceEffect.class);
		SUPPORTED_COMPONENTS.put(SoundEffect.TYPE, SoundEffect.class);
		SUPPORTED_COMPONENTS.put(ShakeEffect.TYPE, ShakeEffect.class);
		SUPPORTED_COMPONENTS.put(CameraEffect.TYPE, CameraEffect.class);
		SUPPORTED_COMPONENTS.put(ModelEffect.TYPE, ModelEffect.class);
		SUPPORTED_COMPONENTS.put(ScreenEffect.TYPE, ScreenEffect.class);
		SUPPORTED_COMPONENTS.put(LightEffect.TYPE, LightEffect.class);
		SUPPORTED_COMPONENTS.put(GameEffect.TYPE, GameEffect.class);
		SUPPORTED_COMPONENTS.put(FastParticleEffect.TYPE, FastParticleEffect.class);
		SUPPORTED_COMPONENTS.put(DistributeEffect.TYPE, DistributeEffect.class);
		SUPPORTED_COMPONENTS.put(RibbonEffect.TYPE, RibbonEffect.class);
		SUPPORTED_COMPONENTS.put(BrushEffect.TYPE, BrushEffect.class);
		SUPPORTED_COMPONENTS.put(TerrainScriptEffect.TYPE, TerrainScriptEffect.class);
		SUPPORTED_COMPONENTS.put(SkinpaintSettingsEffect.TYPE, SkinpaintSettingsEffect.class);
		SUPPORTED_COMPONENTS.put(SkinpaintDistributeEffect.TYPE, SkinpaintDistributeEffect.class);
		SUPPORTED_COMPONENTS.put(GameModelEffect.TYPE, GameModelEffect.class);
		SUPPORTED_COMPONENTS.put(SkinpaintParticleEffect.TYPE, SkinpaintParticleEffect.class);
		SUPPORTED_COMPONENTS.put(SkinpaintFloodEffect.TYPE, SkinpaintFloodEffect.class);
		SUPPORTED_COMPONENTS.put(VolumeEffect.TYPE, VolumeEffect.class);
		SUPPORTED_COMPONENTS.put(SplitControllerEffect.TYPE, SplitControllerEffect.class);
		SUPPORTED_COMPONENTS.put(TerrainDistributeEffect.TYPE, TerrainDistributeEffect.class);
		SUPPORTED_COMPONENTS.put(CloudEffect.TYPE, CloudEffect.class);
		SUPPORTED_COMPONENTS.put(GroundCoverEffect.TYPE, GroundCoverEffect.class);
		SUPPORTED_COMPONENTS.put(MixEventEffect.TYPE, MixEventEffect.class);
		SUPPORTED_COMPONENTS.put(TextEffect.TYPE, TextEffect.class);
	}
	
	protected int type;
	protected int version;
	protected int position;  // just for debugging
	
	// Used for parsing
	// <keyword, <effectName, effect>>
//	private TreeMap<Integer, List<Effect>> effects;
	private String name;
	protected EffectMain parent;
	
	public EffectComponent(int type, int version) {
		this.type = type;
		this.version = version;
	}
	
	/**
	 * Copies <code>effect</code>'s data into a completely new <code>EffectComponent</code>. The copy will be made in a way that editing one effect's data won't change the other's.
	 * @param effect The <code>EffectComponent</code> to be copied. 
	 */
	public EffectComponent(EffectComponent effect) {
		type = effect.type;
		version = effect.version;
	}
	
	
	public int getVersion() {
		return version;
	}
	
	public static int getMinVersion(Class<? extends EffectComponent> clazz) {
		Field keywordField;
		try {
			keywordField = clazz.getField("MIN_VERSION");
			if (keywordField != null) {
				return (int) keywordField.get(null);  // it's a static field so we use null;
			}
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	
	public static int getMaxVersion(Class<? extends EffectComponent> clazz) {
		Field keywordField;
		try {
			keywordField = clazz.getField("MAX_VERSION");
			if (keywordField != null) {
				return (int) keywordField.get(null);  // it's a static field so we use null;
			}
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	
	public boolean checkVersion() {
		return version >= getMinVersion(this.getClass()) && version <= getMaxVersion(this.getClass());
	}
	
	public boolean isInline(ArgScriptCommand command) {
		return command.getArgumentCount() == 0;
	}
	
	public abstract void parseInline(ArgScriptCommand command) throws ArgScriptException;
	
	// for those EffectComponents that only support inline mode
	@Override
	public ArgScriptCommand toCommand() {
		throw new UnsupportedOperationException("toCommand() not supported for EffectComponent.");
	}
	@Override
	public boolean supportsBlock() {
		return true;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void setName(String name) {
		this.name = name;
	}
	
//	@Override
//	public TreeMap<Integer, List<Effect>> getEffectMap() {
//		return effects;
//	}
//	
//	@Override
//	public void setEffectMap(TreeMap<Integer, List<Effect>> effects) {
//		this.effects = effects;
//	}
	
	public void setParent(EffectMain parent) {
		this.parent = parent;
	}
	
	@Override
	public Effect[] getEffects() {
		return new Effect[0];
	}
	
	@Override
	public int getType() {
		return type;
	}
	
	@Override
	public ArgScriptBlock toBlock() {
		ArgScriptBlock b = new ArgScriptBlock(EffectMain.getKeyword(type), name);
		
		toBlock(b);
		
		return b;
	}
	
	@Override
	public void fixEffectIndices(TreeMap<Integer, Integer> baseIndices) {
		// this depends on the implementation, but most of them do nothing
		return;
	}
	
	
	
	public static String[] getEnumTags(Class<?> clazz) {
		try {
			Method method = clazz.getMethod("getEnumTags", new Class<?>[0]);
			
			if (method == null) {
				throw new IllegalArgumentException("Given class doesn't have a 'public static String[] getEnumTags()' method.");
			}
			return (String[]) method.invoke(null, new Object[0]);
			
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return new String[0];
	}
	
	public static String[] getBlockTags(Class<?> clazz) {
		try {
			Method method = clazz.getMethod("getBlockTags", new Class<?>[0]);
			
			if (method == null) {
				throw new IllegalArgumentException("Given class doesn't have a 'public static String[] getBlockTags()' method.");
			}
			return (String[]) method.invoke(null, new Object[0]);
			
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return new String[0];
	}
	
	public static String[] getOptionTags(Class<?> clazz) {
		try {
			Method method = clazz.getMethod("getOptionTags", new Class<?>[0]);
			
			if (method == null) {
				throw new IllegalArgumentException("Given class doesn't have a 'public static String[] getOptionTags()' method.");
			}
			return (String[]) method.invoke(null, new Object[0]);
			
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return new String[0];
	}
	
	public static String[] getCommandTags(Class<?> clazz) {
		try {
			Method method = clazz.getMethod("getCommandTags", new Class<?>[0]);
			
			if (method == null) {
				throw new IllegalArgumentException("Given class doesn't have a 'public static String[] getCommandTags()' method.");
			}
			return (String[]) method.invoke(null, new Object[0]);
			
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return new String[0];
	}
	
	
	public static EffectColor[] copyArray(EffectColor[] src) {
		EffectColor[] dst = new EffectColor[src.length];
		for (int i = 0; i < dst.length; i++) {
			dst[i] = new EffectColor(src[i]);
		}
		return dst;
	}
	
	
	public static float[] copyArray(float[] src) {
		float[] dst = new float[src.length];
		System.arraycopy(src, 0, dst, 0, dst.length);
		return dst;
	}
	
	public static float[] copyArray(final float[] dst, final float[] src) {
		if (dst.length != src.length) {
			throw new IllegalArgumentException("Arrays must have the same length");
		}
		System.arraycopy(src, 0, dst, 0, dst.length);
		return dst;
	}
	
	public static int[] copyArray(final int[] dst, final int[] src) {
		if (dst.length != src.length) {
			throw new IllegalArgumentException("Arrays must have the same length");
		}
		System.arraycopy(src, 0, dst, 0, dst.length);
		return dst;
	}
	
	public static int[] copyArray(int[] src) {
		int[] dst = new int[src.length];
		System.arraycopy(src, 0, dst, 0, dst.length);
		return dst;
	}
}
