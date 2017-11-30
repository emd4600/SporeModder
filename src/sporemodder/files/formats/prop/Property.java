package sporemodder.files.formats.prop;

import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import sporemodder.files.FileStructureException;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.utilities.Hasher;

public abstract class Property {
	public int type;
	public int flags;
	public int name;
//	public int arrayCount, arrayItemSize;
	public String nameStr;
	
	public int size; //To use in text and transform
	
	private boolean debugMode;
	
	public static final String singularName = "fixMe";
	public static final String pluralName = "fixMes";
	
	public Property(int name, int type, int flags) {
		this.name = name;
		nameStr = Hasher.getPropName(name);
		this.type = type;
		this.flags = flags;
	}
	public Property(int name, Class<? extends Property> type, boolean isArray) {
		this.name = name;
		nameStr = Hasher.getPropName(name);
		this.type = getPropType(type);
		this.flags = isArray ? 0x30 : 0;
	}
	public Property(int name) {
		this.name = name;
		nameStr = Hasher.getPropName(name);
//		this.type = getPropType(type);
//		this.flags = isArray ? 0x30 : 0;
	}
	public Property(String name) {
		this.name = Hasher.getPropHash(name);
		nameStr = name;
	}
	public Property(String name, int type) {
		if (name != null) 
			this.name = Hasher.getPropHash(name);
		nameStr = name;
		this.type = type;
//		this.flags = isArray ? 0x30 : 0;
	}
	
	public void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}
	public boolean getDebugMode() {
		return debugMode;
	}
	
	public abstract void readProp(InputStreamAccessor in, boolean array) throws IOException;
	public abstract void writeXML(Document doc, Element elem);
	public abstract void readXML(Element elem) throws IOException;
	public abstract void writeProp(OutputStreamAccessor out, boolean array) throws IOException;
	
	public abstract String toString(boolean array);
	
	public abstract String getSingularName();
	public abstract String getPluralName();
	
	public static Class<? extends Property> getPropType(int type) throws FileStructureException {
		switch(type) {
		case 0x0001 :	return PropertyBool.class;
		case 0x0002 :	return PropertyChar.class;
		case 0x0003 :	return PropertyWChar.class;
		case 0x0005 :	return PropertyInt8.class;
		case 0x0006 :	return PropertyUint8.class;
		case 0x0007 :	return PropertyInt16.class;
		case 0x0008 :	return PropertyUint16.class;
		case 0x0009 :	return PropertyInt32.class;
		case 0x000A :	return PropertyUint32.class;
		case 0x000B :	return PropertyInt64.class;
		case 0x000C :	return PropertyUint64.class;
		case 0x000D :	return PropertyFloat.class;
		case 0x000E :	return PropertyDouble.class;
		case 0x000F :	throw new FileStructureException("PROP001; tPtrType");
		case 0x0010 :	throw new FileStructureException("PROP001; tVoidType");
		case 0x0011 :	throw new FileStructureException("PROP001; tIUnknownRCType");
		case 0x0012 :	return PropertyString8.class;
		case 0x0013 :	return PropertyString16.class;
		case 0x0020 :	return PropertyKey.class;
		case 0x0021 :	throw new FileStructureException("PROP001; tFlags");
		case 0x0022 :	return PropertyText.class;
		case 0x0030 :	return PropertyVector2.class;
		case 0x0031 :	return PropertyVector3.class;
		case 0x0032 :	return PropertyColorRGB.class;
		case 0x0033 :	return PropertyVector4.class;
		case 0x0034 :	return PropertyColorRGBA.class;
		case 0x0035 :	throw new FileStructureException("PROP001; tmatrix2Type");
		case 0x0036 :	throw new FileStructureException("PROP001; tmatrix3Type");
		case 0x0037 :	throw new FileStructureException("PROP001; tmatrix4Type");
		case 0x0038 :	return PropertyTransform.class;
		case 0x0039 :	return PropertyBBox.class;
		default:	throw new FileStructureException("PROP001; Unknown property type identifier: " + type);
		}
	}
	
	public static int getPropType(Class<? extends Property> type) {
		if (type == PropertyBool.class) return 0x0001;
		else if (type == PropertyChar.class) return 0x0002;
		else if (type == PropertyWChar.class) return 0x0003;
		else if (type == PropertyInt8.class) return 0x0005;
		else if (type == PropertyUint8.class) return 0x0006;
		else if (type == PropertyInt16.class) return 0x0007;
		else if (type == PropertyUint16.class) return 0x0001;
		else if (type == PropertyInt32.class) return 0x0008;
		else if (type == PropertyUint32.class) return 0x000A;
		else if (type == PropertyInt64.class) return 0x000B;
		else if (type == PropertyUint64.class) return 0x000C;
		else if (type == PropertyFloat.class) return 0x000D;
		else if (type == PropertyDouble.class) return 0x000E;
		else if (type == PropertyString8.class) return 0x0012;
		else if (type == PropertyString16.class) return 0x0013;
		else if (type == PropertyKey.class) return 0x0020;
		else if (type == PropertyText.class) return 0x0022;
		else if (type == PropertyVector2.class) return 0x0030;
		else if (type == PropertyVector3.class) return 0x0031;
		else if (type == PropertyColorRGB.class) return 0x0032;
		else if (type == PropertyVector4.class) return 0x0033;
		else if (type == PropertyColorRGBA.class) return 0x0034;
		else if (type == PropertyTransform.class) return 0x0038;
		else if (type == PropertyBBox.class) return 0x0039;
		return -1;
	}
}
