package sporemodder.files.formats.prop;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.w3c.dom.DOMException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import sporemodder.MainApp;
import sporemodder.files.FileStreamAccessor;
import sporemodder.files.FileStructureException;
import sporemodder.files.MemoryOutputStream;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.dbpf.PerformanceViewer;
import sporemodder.utilities.Hasher;
import sporemodder.utilities.performance.Profiler;

public class FastPropConverter extends DefaultHandler {
	
	private static final int kPropertyInfoSize = 8;
	private static final int kArrayPropertyInfoSize = 16;
	
	private static final int kPropertyBufferSize = 512; 
	
	private static final Comparator<Integer> DESCENDING_COMPARATOR = new Comparator<Integer>() {
		@Override
        public int compare(Integer o1, Integer o2) {
			return Integer.compareUnsigned(o1, o2);
        }
    };
	
	private static final class PropertyData {
		byte[] data;
		int length;
		
		public PropertyData(MemoryOutputStream stream) {
			data = stream.getRawData();
			length = stream.length();
		}
	}
	
	private MemoryOutputStream stream;  // temporary stream
	
	private final TreeMap<Integer, PropertyData> propertiesData = new TreeMap<Integer, PropertyData>(DESCENDING_COMPARATOR);

	private int nTotalSize = 4;
	
	private boolean bInsideProperties = false;
	
	private int propertyID = 0;
	private int nCurrentType = -1;
	private int nFlags = 0;
	
	private boolean bIsArray = false;
	private boolean bIsInArrayData = false;  // are we inside the data of an array?
	private int nArrayItemCount = 0;
	private int nArrayItemSize = 0;
	
	// complex (vector/color) properties
	private boolean bIsComplexProperty = false;
	private boolean bIsInComplexComponent = false;
	private float[] complexPropertyData;
	
	
	private Attributes attributes;
	
	// for error diagnosing
	private String lastQName;
	private String lastPropertyName;
	
	
	private List<String> autoLocaleStrings;
	private String autoLocaleName;
	
	private FastPropConverter() {}
	
	
	@Override
	public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException {
		
		this.attributes = attributes;
		this.lastQName = qName;
		
		if (qName.equalsIgnoreCase("properties")) {
			bInsideProperties = true;
		}
		else {
			if (!bInsideProperties) {
				throw new SAXException("PROP File: Properties must be inside <properties> tag.");
			}
			
			// we are not parsing an array property
			if (nCurrentType == -1) {
				
				String name = qName.toLowerCase();
				
				nCurrentType = getType(name);
				bIsArray = name.endsWith("s");
				nFlags = 0;
				
				
				if (nCurrentType == -1) {
					throw new SAXException("PROP File: Unrecognised property type '" + qName + "'.");
				}
				
				if (requiresArray(nCurrentType) && !bIsArray) {
					throw new SAXException("PROP File: Properties of type '" + qName + "' must be an array.");
				}
				
				lastPropertyName = attributes.getValue("name");
				if (lastPropertyName == null) {
					throw new SAXException("PROP File: Property must have a 'name' attribute.");
				}
				
				propertyID = Hasher.getPropHash(lastPropertyName);
				
				// create a new byte array to store the data
				stream.reset(kPropertyBufferSize);
				
				stream.setLength(bIsArray ? kArrayPropertyInfoSize : kPropertyInfoSize);
				stream.seek(bIsArray ? kArrayPropertyInfoSize : kPropertyInfoSize);
				
				
				// we only do this for single properties; arrays do it every time 
				if (!bIsArray) {
					if (isComplexType(nCurrentType)) {
						complexPropertyData = createComplexArray(nCurrentType);
						bIsComplexProperty = true;
					}
					else {
						bIsComplexProperty = false;
					}
					
					// parse types that don't require content (like key)
					if (!requiresContent(nCurrentType) && !bIsComplexProperty) {
						convert(stream, attributes, null);
					}
				}
				else {
					nFlags |= 0x30;
					
					nArrayItemSize = getItemSize(nCurrentType);
					
					if (isComplexType(nCurrentType)) {
						bIsComplexProperty = true;
					}
					else {
						bIsComplexProperty = false;
					}
				}
				
				return;
			}
			else if (bIsArray) {
				// we have a nCurrentType and bIsArray, therefore we are parsing an array property
				
				// are we already parsing a value?
				if (!bIsInArrayData) {
					bIsInArrayData = true;
					
					// reset the vector/color data
					if (bIsComplexProperty) {
						complexPropertyData = createComplexArray(nCurrentType);
					}
					
					nArrayItemCount++;
					
					// parse types that don't require content (like key)
					if (!requiresContent(nCurrentType) && !bIsComplexProperty) {
						convert(stream, attributes, null);
					}
					
					return;
				}
				else {
					// we are parsing a value, so this must be a component.
					if (bIsComplexProperty && !bIsInComplexComponent) {
						bIsInComplexComponent = true;
						return;
					}
					// don't return so we throw an error
				}
				
			}
			// If we are parsing a complex property, then this is a component
			else if (bIsComplexProperty) {
				bIsInComplexComponent = true;
				return;
			}
			
			throw new SAXException("PROP File: Property '" + lastPropertyName + "' has not been closed correctly. "
					+ "Are you missing the '</" + lastQName + "'> tag?");
		}
	}
	
	@Override
	public void endElement(String uri, String localName,String qName) throws SAXException {
		if (!bInsideProperties) {
			throw new SAXException("PROP File: Properties must be inside <properties> tag.");
		}
		
		if (qName.equalsIgnoreCase("properties")) {
			if (bIsInArrayData || bIsArray) {
				throw new SAXException("PROP File: Property '" + lastPropertyName + "' has not been closed correctly. "
						+ "Are you missing the '</" + lastQName + ">' tag?");
			}
			bInsideProperties = false;
			return;
		}
		
		// We finish the property ONLY if the closed tag was actually a property, and not a component like 'x'
		if (!bIsInComplexComponent) {
			
			if (bIsInArrayData) {
				bIsInArrayData = false;
				
				// vector/color properties still need to be converted at this point
				if (bIsComplexProperty) {
					convert(stream, null, null);
				}
			}
			else {
				// arrays do this on every value, not when the array itself is closed 
				if (!bIsArray) {
					// vector/color properties still need to be converted at this point
					if (bIsComplexProperty) {
						convert(stream, null, null);
					}
				}
				
				// write property info
				writePropertyInfo();
				
				// store the data
				PropertyData data = new PropertyData(stream);
				propertiesData.put(propertyID, data);
				
				nTotalSize += data.length;
				
				// reset information
				nCurrentType = -1;
				
				bIsArray = false;
				bIsInArrayData = false;
				nArrayItemCount = 0;
				nArrayItemSize = 0;
				
				// complex (vector/color) properties
				bIsComplexProperty = false;
				bIsInComplexComponent = false;
				complexPropertyData = null;
			}
		}
		else {
			// we have finished a component tag, so we are no longer inside a component
			bIsInComplexComponent = false;
		}
	}
	
	private void writePropertyInfo() throws SAXException {
		try {
			stream.seek(0);
			stream.writeInt(propertyID);
			stream.writeShort(nCurrentType);
			stream.writeShort(nFlags);
			if (bIsArray) {
				stream.writeInt(nArrayItemCount);
				stream.writeInt(nArrayItemSize);
			}
		}
		catch (Exception e) {
			throw new SAXException(e);
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		String text = new String(ch, start, length);
		text = text.trim();
		if (text.length() == 0) return;
		
		if (nCurrentType == -1) {
			// a comment
			if (text.startsWith("#")) return;
			throw new SAXException("PROP File: Data must be inside a property.");
		}
		else {
			if (bIsArray) {
				if (!bIsInArrayData && !bIsInComplexComponent) {
					if (requiresContent(nCurrentType)) {
						throw new SAXException("PROP File: Data in the array property '" + lastPropertyName + "' must be contained inside properties."
								+ "Are you missing the <" + lastQName + "></" + lastQName + "> tags?");
					}
					else {
						throw new SAXException("PROP File: Incorrect usage of property type '" + lastQName + "' in property '" + lastPropertyName + "'.");
					}
				}
			}
			
			if (bIsComplexProperty) {
				if (bIsInComplexComponent) {
					parseComplexComponent(text);
				}
				else {
					throw new SAXException("PROP File: Data in the " + lastQName + " property '" + lastPropertyName + "' must be contained inside component tags (<x></x> etc).");
				}
			}
			else {
				convert(stream, attributes, text);
			}
		}
	}
	
	
	private static float[] createComplexArray(int type) {
		if (type == PropertyVector2.PROP_TYPE) return new float[2];
		else if (type == PropertyVector3.PROP_TYPE) return new float[3];
		else if (type == PropertyVector4.PROP_TYPE) return new float[4];
		else if (type == PropertyColorRGB.PROP_TYPE) return new float[3];
		else if (type == PropertyColorRGBA.PROP_TYPE) return new float[] {0, 0, 0, 1f};
		else if (type == PropertyBBox.PROP_TYPE) return new float[6];
		else return null;
	}
	
	private void parseComplexComponent(String text) throws SAXException {
		// BBox uses min/max, the others use xyzw/rgba
		if (nCurrentType == PropertyBBox.PROP_TYPE) {
			
			if (lastQName.equalsIgnoreCase("min"))
			{
				String[] splits = text.split(", ");
				complexPropertyData[0] = Float.parseFloat(splits[0]);
				complexPropertyData[1] = Float.parseFloat(splits[1]);
				complexPropertyData[2] = Float.parseFloat(splits[2]);
				
				return;
			}
			else if (lastQName.equalsIgnoreCase("max"))
			{
				String[] splits = text.split(", ");
				complexPropertyData[3] = Float.parseFloat(splits[0]);
				complexPropertyData[4] = Float.parseFloat(splits[1]);
				complexPropertyData[5] = Float.parseFloat(splits[2]);
				
				return;
			}
		}
		else {
			if (lastQName.equalsIgnoreCase("x") || lastQName.equalsIgnoreCase("r")) 
			{
				complexPropertyData[0] = Float.parseFloat(text);
				return;
			}
			else if (lastQName.equalsIgnoreCase("y") || lastQName.equalsIgnoreCase("g")) 
			{
				complexPropertyData[1] = Float.parseFloat(text);
				return;
			}
			else if (lastQName.equalsIgnoreCase("z") || lastQName.equalsIgnoreCase("b")) 
			{
				
				if (nCurrentType != PropertyVector3.PROP_TYPE && nCurrentType != PropertyVector4.PROP_TYPE
						&& nCurrentType != PropertyColorRGB.PROP_TYPE && nCurrentType != PropertyColorRGBA.PROP_TYPE) {
					
					throw new SAXException("PROP File: Property '" + lastPropertyName + "': z/b component is only accepted in vector3/vector4/colorRGB/colorRGBA properties.");
				}
					
				complexPropertyData[2] = Float.parseFloat(text);
				return;
			}
			else if (lastQName.equalsIgnoreCase("w") || lastQName.equalsIgnoreCase("a")) 
			{
				
				if (nCurrentType != PropertyVector4.PROP_TYPE && nCurrentType != PropertyColorRGBA.PROP_TYPE) {
					
					throw new SAXException("PROP File: Property '" + lastPropertyName + "': w/a component is only accepted in vector4/colorRGBA properties.");
				}
					
				complexPropertyData[3] = Float.parseFloat(text);
				
				return;
			}
			
		}
		
		throw new SAXException("PROP File: Property '" + lastPropertyName + "': Unknown component '" + lastQName + "'.");
	}
	
	private void convert(MemoryOutputStream stream, Attributes attributes, String text) throws SAXException {
		try {
			switch (nCurrentType) {
			case PropertyBool.PROP_TYPE: PropertyBool.fastConvert(stream, attributes, text); break;
			case PropertyChar.PROP_TYPE: PropertyChar.fastConvert(stream, attributes, text); break;
			case PropertyDouble.PROP_TYPE: PropertyDouble.fastConvert(stream, attributes, text); break;
			case PropertyFloat.PROP_TYPE: PropertyFloat.fastConvert(stream, attributes, text); break;
			case PropertyInt16.PROP_TYPE: PropertyInt16.fastConvert(stream, attributes, text); break;
			case PropertyInt32.PROP_TYPE: PropertyInt32.fastConvert(stream, attributes, text); break;
			case PropertyInt64.PROP_TYPE: PropertyInt64.fastConvert(stream, attributes, text); break;
			case PropertyInt8.PROP_TYPE: PropertyInt8.fastConvert(stream, attributes, text); break;
			case PropertyKey.PROP_TYPE: PropertyKey.fastConvert(stream, attributes, text, bIsArray); break;
			case PropertyString8.PROP_TYPE: PropertyString8.fastConvert(stream, attributes, text); break;
			case PropertyString16.PROP_TYPE: PropertyString16.fastConvert(stream, attributes, text); break;
			case PropertyUint16.PROP_TYPE: PropertyUint16.fastConvert(stream, attributes, text); break;
			case PropertyUint32.PROP_TYPE: PropertyUint32.fastConvert(stream, attributes, text); break;
			case PropertyUint64.PROP_TYPE: PropertyUint64.fastConvert(stream, attributes, text); break;
			case PropertyUint8.PROP_TYPE: PropertyUint8.fastConvert(stream, attributes, text); break;
			case PropertyWChar.PROP_TYPE: PropertyWChar.fastConvert(stream, attributes, text); break;
			case PropertyTransform.PROP_TYPE: PropertyTransform.fastConvert(stream, attributes, text); break;
			case PropertyText.PROP_TYPE:
				PropertyText.fastConvert(stream, attributes, text, autoLocaleStrings, autoLocaleName);
				
				break;
			
			case PropertyVector2.PROP_TYPE:
			case PropertyVector3.PROP_TYPE:
			case PropertyVector4.PROP_TYPE:
			case PropertyColorRGB.PROP_TYPE:
			case PropertyColorRGBA.PROP_TYPE:
				stream.writeLEFloats(complexPropertyData);
				
				if (!bIsArray) {
					for (int i = complexPropertyData.length; i < 4; i++) {
						stream.writePadding(4);
					}
				}
				break;
				
			case PropertyBBox.PROP_TYPE:
				stream.writeLEFloats(complexPropertyData);
				break;
				
			}
			
		} catch (Exception e) {
			throw new SAXException(e);
		}
	}
	
	private static int getItemSize(int type) throws SAXException {
		switch (type) {
		case PropertyBool.PROP_TYPE: return PropertyBool.itemSize;
		case PropertyChar.PROP_TYPE: return PropertyChar.itemSize;
		case PropertyDouble.PROP_TYPE: return PropertyDouble.itemSize;
		case PropertyFloat.PROP_TYPE: return PropertyFloat.itemSize;
		case PropertyInt16.PROP_TYPE: return PropertyInt16.itemSize;
		case PropertyInt32.PROP_TYPE: return PropertyInt32.itemSize;
		case PropertyInt64.PROP_TYPE: return PropertyInt64.itemSize;
		case PropertyInt8.PROP_TYPE: return PropertyInt8.itemSize;
		case PropertyKey.PROP_TYPE: return PropertyKey.itemSize;
		case PropertyString8.PROP_TYPE: return PropertyString8.itemSize;
		case PropertyString16.PROP_TYPE: return PropertyString16.itemSize;
		case PropertyUint16.PROP_TYPE: return PropertyUint16.itemSize;
		case PropertyUint32.PROP_TYPE: return PropertyUint32.itemSize;
		case PropertyUint64.PROP_TYPE: return PropertyUint64.itemSize;
		case PropertyUint8.PROP_TYPE: return PropertyUint8.itemSize;
		case PropertyWChar.PROP_TYPE: return PropertyWChar.itemSize;
		case PropertyVector2.PROP_TYPE: return PropertyVector2.itemSize;
		case PropertyVector3.PROP_TYPE: return PropertyVector3.itemSize;
		case PropertyVector4.PROP_TYPE: return PropertyVector4.itemSize;
		case PropertyColorRGB.PROP_TYPE: return PropertyColorRGB.itemSize;
		case PropertyColorRGBA.PROP_TYPE: return PropertyColorRGBA.itemSize;
		case PropertyTransform.PROP_TYPE: return PropertyTransform.itemSize;
		case PropertyText.PROP_TYPE: return PropertyText.itemSize;
		case PropertyBBox.PROP_TYPE: return PropertyBBox.itemSize;
		default: return 0;
		}
	}

	private static int getType(String keyword) {
		if (keyword.startsWith("bbox")) 			return PropertyBBox.PROP_TYPE;
		else if (keyword.startsWith("bool")) 		return PropertyBool.PROP_TYPE;
		else if (keyword.startsWith("char"))		return PropertyChar.PROP_TYPE;
		else if (keyword.startsWith("colorrgba"))	return PropertyColorRGBA.PROP_TYPE;
		else if (keyword.startsWith("colorrgb"))	return PropertyColorRGB.PROP_TYPE;
		else if (keyword.startsWith("double"))		return PropertyDouble.PROP_TYPE;
		else if (keyword.startsWith("float")) 		return PropertyFloat.PROP_TYPE;
		else if (keyword.startsWith("int16")) 		return PropertyInt16.PROP_TYPE;
		else if (keyword.startsWith("int32")) 		return PropertyInt32.PROP_TYPE;
		else if (keyword.startsWith("int64")) 		return PropertyInt64.PROP_TYPE;
		else if (keyword.startsWith("int8")) 		return PropertyInt8.PROP_TYPE;
		else if (keyword.startsWith("key")) 		return PropertyKey.PROP_TYPE;
		else if (keyword.startsWith("string16")) 	return PropertyString16.PROP_TYPE;
		else if (keyword.startsWith("string8")) 	return PropertyString8.PROP_TYPE;
		else if (keyword.startsWith("text")) 		return PropertyText.PROP_TYPE;
		else if (keyword.startsWith("transform")) 	return PropertyTransform.PROP_TYPE;
		else if (keyword.startsWith("uint16")) 		return PropertyUint16.PROP_TYPE;
		else if (keyword.startsWith("uint32")) 		return PropertyUint32.PROP_TYPE;
		else if (keyword.startsWith("uint64")) 		return PropertyUint64.PROP_TYPE;
		else if (keyword.startsWith("uint8")) 		return PropertyUint8.PROP_TYPE;
		else if (keyword.startsWith("vector2"))		return PropertyVector2.PROP_TYPE;
		else if (keyword.startsWith("vector3")) 	return PropertyVector3.PROP_TYPE;
		else if (keyword.startsWith("vector4")) 	return PropertyVector4.PROP_TYPE;
		else if (keyword.startsWith("wchar")) 		return PropertyWChar.PROP_TYPE;
		else return -1;
	}
	
	
	/** Returns false if the type is a single-line property, true if it is not. */
	private static boolean isComplexType(int type) {
		return (type == PropertyBBox.PROP_TYPE || type == PropertyColorRGBA.PROP_TYPE || type == PropertyColorRGB.PROP_TYPE
				|| type == PropertyVector2.PROP_TYPE || type == PropertyVector3.PROP_TYPE || type == PropertyVector4.PROP_TYPE);
	}
	
	private static boolean requiresArray(int type) {
		return type == PropertyText.PROP_TYPE || type == PropertyTransform.PROP_TYPE || type == PropertyBBox.PROP_TYPE;
	}
	
	private static boolean requiresContent(int type) {
		return type != PropertyKey.PROP_TYPE && !isComplexType(type) /* && !requiresArray(type) */;
	}
	
	public static MemoryOutputStream xmlToProp(InputStream in) throws ParserConfigurationException, SAXException, IOException {
		return (MemoryOutputStream) xmlToProp(in, null, null, null);
	}
	
	public static OutputStreamAccessor xmlToProp(InputStream in, OutputStreamAccessor out, List<String> autoLocaleStrings, String autoLocaleName) throws ParserConfigurationException, SAXException, IOException {
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		
		FastPropConverter converter = new FastPropConverter();
		
		try (MemoryOutputStream stream = new MemoryOutputStream(0)) { 

			converter.stream = stream;
			converter.autoLocaleStrings = autoLocaleStrings;
			converter.autoLocaleName = autoLocaleName;
		
			parser.parse(in, converter);
		}
		
		if (out == null) {
			out = new MemoryOutputStream(converter.nTotalSize);
		}
		
		out.writeInt(converter.propertiesData.size());
		
		for (PropertyData propertyData : converter.propertiesData.values()) {
			out.write(propertyData.data, 0, propertyData.length);
		}
		
		return out;
	}
	
	
	///
	// PERFORMANCE TESTING
	///
	
	
	private static final int kNumTests = 100;
	private static final String kProfile = "General";
	
	public static void main(String[] args) throws FileNotFoundException, IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException, FileStructureException, ParserConfigurationException, SAXException, DOMException, TransformerFactoryConfigurationError, TransformerException {
		MainApp.init();
		
//		String inputPath = "E:\\Eric\\SporeModder\\Projects\\EP1_PatchData\\lighting_properties~\\example.prop.xml";
		String inputPath = "C:\\Users\\Eric\\Desktop\\text.prop.xml";
		
		String outputPath1 = "C:\\Users\\Eric\\Desktop\\example_old.prop";
		String outputPath2 = "C:\\Users\\Eric\\Desktop\\example_new.prop";
		
		try (InputStream in = new FileInputStream(inputPath);
				FileStreamAccessor out = new FileStreamAccessor(outputPath1, "rw", true)) {
			
			
			try (MemoryOutputStream stream = FastPropConverter.xmlToProp(in)) {
				stream.writeInto(out);
			}
			
		}
		
		try (FileStreamAccessor in = new FileStreamAccessor(outputPath1, "r");
				MemoryOutputStream out = new MemoryOutputStream()) {
			
			PROPMain prop = new PROPMain();
			prop.readProp(in);
			
			prop.writeXML(out);
			
			// print result
			System.out.println(new String(out.getRawData(), 0, out.length()));
		}
		
		
		Profiler oldProfiler = new Profiler();
		Profiler newProfiler = new Profiler();
		
		for (int i = 0; i < kNumTests; i++) {
			
			oldProfiler.startMeasure(kProfile);
			PROPMain.xmlToProp(inputPath, outputPath1);
			oldProfiler.endMeasure(kProfile);
			
			newProfiler.startMeasure(kProfile);
			try (InputStream in = new FileInputStream(inputPath);
					FileStreamAccessor out = new FileStreamAccessor(outputPath2, "rw", true)) {
				
				
				try (MemoryOutputStream stream = FastPropConverter.xmlToProp(in)) {
					stream.writeInto(out);
				}
				
			}
			newProfiler.endMeasure(kProfile);
		}
		
		PerformanceViewer dbpfViewer = new PerformanceViewer(oldProfiler, newProfiler, new String[] {}, kProfile);
		dbpfViewer.setVisible(true);
	}
}
