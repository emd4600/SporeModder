package sporemodder.files.formats.prop;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import sporemodder.files.FileStreamAccessor;
import sporemodder.files.FileStructureException;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.FileFormatStructure;
import sporemodder.files.formats.FileStructureError;
import sporemodder.utilities.Hasher;

public class PROPMain implements FileFormatStructure {
	public static enum ReadMode {BINARY, XML};
	
	public static String eol = System.getProperty("line.separator");
	
	public List<Property> properties = new ArrayList<Property>();
	
	public PROPMain() {
		properties = new ArrayList<Property>();
	}
	public PROPMain(int capacity) {
		properties = new ArrayList<Property>(capacity);
	}
	public PROPMain(PROPMain other) {
		properties = new ArrayList<Property>(other.properties);
	}
	public PROPMain(InputStreamAccessor in) throws IOException {
		try {
			readProp(in);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException
				| NoSuchFieldException | FileStructureException e) {
			System.err.println("Couldn't parse binary prop!");
			e.printStackTrace();
		}
	}
	public PROPMain(String path, ReadMode readMode) throws IOException {
		if (readMode == ReadMode.BINARY) {
			try (FileStreamAccessor in = new FileStreamAccessor(path, "r")) {
				readProp(in);
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException
					| NoSuchFieldException | FileStructureException e) {
				System.err.println("Couldn't parse binary prop!");
				e.printStackTrace();
			}
		} else if (readMode == ReadMode.XML) {
			InputStream in = new FileInputStream(path);
			try {
				readXML(in);
			} catch (DOMException | IllegalArgumentException
					| IllegalAccessException | NoSuchFieldException
					| SecurityException | InstantiationException
					| ParserConfigurationException
					| TransformerFactoryConfigurationError
					| TransformerException | SAXException e) {
				System.err.println("Couldn't parse XML prop!");
				e.printStackTrace();
			} finally {
				in.close();
			}
		}
	}
	
	public void readProp(InputStreamAccessor in) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException, IOException, FileStructureException {
		readProp(in, false);
	}
	
	@SuppressWarnings("unchecked")
	public void readProp(InputStreamAccessor in, boolean debugMode) throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException, FileStructureException {
		int propCount = in.readInt();
//		propCount = 1;
		properties = new ArrayList<Property>(propCount);
		for (int i = 0; i < propCount; i++) {
			//System.out.println(in.getFilePointer());
			int name = in.readInt();
			//System.out.println(Hasher.getPropName(name));
			int type = in.readShort();
			int flags = in.readShort();
			Property prop;
			if ((flags & 0x30) == 0) {
				if (type != 0) {
					prop =  (Property) Property.getPropType(type).getConstructor(new Class<?>[]{int.class, int.class, int.class}).newInstance(name, type, flags);
					prop.setDebugMode(debugMode);
					prop.readProp(in, false);
				} else {
					prop = null;
					in.skipBytes(16);
				}
			} else if ((flags & 0x40) == 0) {
				if (type != 0) {
					@SuppressWarnings("rawtypes")
					ArrayProperty array = new ArrayProperty(name, type, flags);
					Class<? extends Property> valueType = Property.getPropType(type);
					//System.out.println(valueType);
					array.setDebugMode(debugMode);
					array.setValueType(valueType);
					array.readInfo(in);
					for (int a = 0; a < array.numValues; a++) {
						Property p = valueType.getConstructor(new Class<?>[]{int.class, int.class, int.class}).newInstance(name, type, flags);
						p.size = array.arrayItemSize;
						p.setDebugMode(debugMode);
						p.readProp(in, true);
						array.addValue(p);
					}
					prop = array;
				} else {
					prop = null;
					in.skipBytes(16);
				}
			} else {
				throw new IOException("PROP000; Unknown property flag: " + flags);
			}
			properties.add(prop);
		}
	}
	
	@Deprecated
	public void writeXML(String outPath) throws ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException, IOException, DOMException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		Element root = doc.createElement("properties");
		doc.appendChild(root);
		
		
		for (Property prop : properties) {
			if (prop != null) {
				Element elem;
				elem = prop.getClass() == ArrayProperty.class ? doc.createElement(prop.getPluralName()) : doc.createElement(prop.getSingularName());
				root.appendChild(elem);
				elem.setAttribute("name", prop.nameStr);
				prop.writeXML(doc, elem);
			}
		}
		
		
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(outPath).getAbsolutePath());
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		transformer.transform(source, result);
	}
	
	public void writeXML(OutputStreamAccessor out) throws IOException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		//TODO some way to disable this?
		removeAutolocales();
		
		out.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>" + eol +
				  "<properties>" + eol);
		
		List<Property> strings = new ArrayList<Property>();
		List<Property> nonStrings = new ArrayList<Property>();
		Property description = null; //Special one
		Property parent = null; //Special one
		for (Property prop : properties) {
			if (prop == null) {
//				properties.remove(i);
				continue;
			}
			if (prop.name == 0x00B2CCCA) {
				prop.nameStr = Hasher.getPropName(prop.name);
				description = prop;
			} else if (prop.name == 0x00B2CCCB) {
				prop.nameStr = Hasher.getPropName(prop.name);
				parent = prop;
			} else {
				prop.nameStr = Hasher.getPropName(prop.name);
				if (prop.nameStr.startsWith("#")) nonStrings.add(prop);
				else strings.add(prop);
			}
		}
		
		
		Collections.sort(strings, new Comparator<Property>() {
            @Override
            public int compare(final Property object1, final Property object2) {
                return object1.nameStr.toLowerCase().compareTo(object2.nameStr.toLowerCase());
            }
      	});
		Collections.sort(nonStrings, new Comparator<Property>() {
            @Override
            public int compare(final Property object1, final Property object2) {
                return (object1.name & 0xffffffffL) > (object2.name & 0xffffffffL) ? +1 : (object1.name & 0xffffffffL) < (object2.name & 0xffffffffL) ? -1 : 0;
            }
      	});
		List<Property> newProps = new ArrayList<Property>();
		newProps.add(parent);
		newProps.add(description);
		newProps.addAll(strings);
		newProps.addAll(nonStrings);
		
		for (Property prop : newProps) {
			if (prop != null) {
				boolean isArray = prop instanceof ArrayProperty;
				out.write(prop.toString(isArray));
			}
		}
		
		out.write("</properties>");
	}
	
	
	public void readXML(InputStream in) throws ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException, IOException, DOMException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, SAXException, InstantiationException {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
		Element root = doc.getDocumentElement();
		
		
		properties = new ArrayList<Property>();
		NodeList items = root.getChildNodes();
		int size = items.getLength();
		for (int i = 0; i < size; i++) {
			Node item = items.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
//				long time1 = System.currentTimeMillis();
				
				Element elem = (Element) item;
				String name = item.getNodeName();
				
				if (name.startsWith("bbox")) 			properties.add(PropertyBBox.processNewProperty(elem));
				else if (name.startsWith("bool")) 		properties.add(PropertyBool.processNewProperty(elem));
				else if (name.startsWith("char"))		properties.add(PropertyChar.processNewProperty(elem));
				else if (name.startsWith("colorRGBA"))	properties.add(PropertyColorRGBA.processNewProperty(elem));
				else if (name.startsWith("colorRGB"))	properties.add(PropertyColorRGB.processNewProperty(elem));
				else if (name.startsWith("double"))		properties.add(PropertyDouble.processNewProperty(elem));
				else if (name.startsWith("float")) 		properties.add(PropertyFloat.processNewProperty(elem));
				else if (name.startsWith("int16")) 		properties.add(PropertyInt16.processNewProperty(elem));
				else if (name.startsWith("int32")) 		properties.add(PropertyInt32.processNewProperty(elem));
				else if (name.startsWith("int64")) 		properties.add(PropertyInt64.processNewProperty(elem));
				else if (name.startsWith("int8")) 		properties.add(PropertyInt8.processNewProperty(elem));
				else if (name.startsWith("key")) 		properties.add(PropertyKey.processNewProperty(elem));
				else if (name.startsWith("string16")) 	properties.add(PropertyString16.processNewProperty(elem));
				else if (name.startsWith("string8")) 	properties.add(PropertyString8.processNewProperty(elem));
				else if (name.startsWith("text")) 		properties.add(PropertyText.processNewProperty(elem));
				else if (name.startsWith("transform")) 	properties.add(PropertyTransform.processNewProperty(elem));
				else if (name.startsWith("uint16")) 	properties.add(PropertyUint16.processNewProperty(elem));
				else if (name.startsWith("uint32")) 	properties.add(PropertyUint32.processNewProperty(elem));
				else if (name.startsWith("uint64")) 	properties.add(PropertyUint64.processNewProperty(elem));
				else if (name.startsWith("uint8")) 		properties.add(PropertyUint8.processNewProperty(elem));
				else if (name.startsWith("vector2"))	properties.add(PropertyVector2.processNewProperty(elem));
				else if (name.startsWith("vector3")) 	properties.add(PropertyVector3.processNewProperty(elem));
				else if (name.startsWith("vector4")) 	properties.add(PropertyVector4.processNewProperty(elem));
				else if (name.startsWith("wchar")) 		properties.add(PropertyWChar.processNewProperty(elem));
				
			}
		}
//		propCount = properties.size();
//		this.properties = new ArrayList<Property>[propCount];
//		this.properties = properties.toArray(this.properties);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void write(OutputStreamAccessor out) throws IOException {
//		for (Property prop : properties) {
//			System.out.println(Hasher.getPropName(prop.name) + "\t" + Hasher.hashToHex(prop.name));
//		}
		
		List<Property> newProps = new ArrayList<Property>(properties);
		Collections.sort(newProps, new Comparator<Property>() {
            @Override
            public int compare(final Property object1, final Property object2) {
                return (object1.name & 0xffffffffL) > (object2.name & 0xffffffffL) ? +1 : (object1.name & 0xffffffffL) < (object2.name & 0xffffffffL) ? -1 : 0;
            }
      	});
		
//		System.out.println();
//		for (Property prop : newProps) {
//			System.out.println(Hasher.getPropName(prop.name) + "\t" + Hasher.hashToHex(prop.name));
//		}
		
		out.writeInt(properties.size());
		for (Property prop : newProps) {
			out.writeInt(prop.name);
			out.writeShort(prop.type);
			out.writeShort(prop.flags);
			if (prop instanceof ArrayProperty) {
				ArrayProperty array = (ArrayProperty) prop;
				out.writeInt(array.values.size());
				out.writeInt(array.arrayItemSize);
				@SuppressWarnings("unchecked")
				List<Property> props = array.getValues();
				for (Property p : props) {
					p.writeProp(out, true);
				}
			} else {
				prop.writeProp(out, false);
			}
		}
	}
	
	/**
	 * Returns the first property whose name matches the given one. If there no properties matching the filter, it will return null.
	 * @param hash The name of the property to return.
	 * @return The first property whose name matches the given one, or null if no property matches it.
	 */
	public Property get(String name) {
		for (Property prop : properties) {
			if (prop != null) {
				if (prop.nameStr.equals(name)) {
					return prop;
				}
			}
		}
		return null;
	}
	
	/**
	 * Returns the first property whose hash matches the given one. If there no properties matching the filter, it will return null.
	 * @param hash The hash of the property to return.
	 * @return The first property whose hash matches the given one, or null if no property matches it.
	 */
	public Property get(int hash) {
		for (Property prop : properties) {
			if (prop != null) {
				if (prop.name == hash) {
					return prop;
				}
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private <T extends Property> T extracted(Property prop, Class<T> clazz) {
		if (prop == null) {
			return null;
		} else {
			if (clazz.isInstance(prop)) {
				return (T)prop;
			} else {
				return null;
			}
		}
	}
	
	/** Safe equivalent to <code>(T)get(String name)</code>. Returns the first property whose name matches the given one, but only if it's of the type
	 * given; if it isn't or there is no property with that name, this method will return null.
	 * <p>
	 * This is only for single properties. For array properties, use <code>&lt;T extends Property&gt;getArrayProperty(String name, Class&lt;T&gt; clazz)</code>.
	 * 
	 * @param name The name of the property to find.
	 * @param clazz The class of the property type the property is expected to have. For example, PropertyInt32.class.
	 * @return The Property of type T with the given hash or null if it hasn't been found.
	 */
	public <T extends Property> T getProperty(String name, Class<T> clazz) {
		Property prop = get(name);
		
		return extracted(prop, clazz);
	}
	/** Safe equivalent to <code>(T)get(int name)</code>. Returns the first property whose hash matches the given one, but only if it's of the type
	 * given; if it isn't or there is no property with that hash, this method will return null.
	 * <p>
	 * This is only for single properties. For array properties, use <code>&lt;T extends Property&gt;getArrayProperty(int hash, Class&lt;T&gt; clazz)</code>.
	 * 
	 * @param name The hash of the property to find.
	 * @param clazz The class of the property type the property is expected to have. For example, PropertyInt32.class.
	 * @return The Property of type T with the given hash or null if it hasn't been found.
	 */
	public <T extends Property> T getProperty(int hash, Class<T> clazz) {
		Property prop = get(hash);
		
		return extracted(prop, clazz);
	}
	
	/** Safe equivalent to <code>(ArrayProperty&lt;T&gt;)get(String name)</code>. Returns the first array property whose name matches the given one, 
	 * but only if it's values are of the type given; if they aren't or there is no array property with that name, this method will return null.
	 * <p>
	 * This is only for array properties. For single properties, use <code>&lt;T extends Property&gt;getProperty(String name, Class&lt;T&gt; clazz)</code>.
	 * 
	 * @param name The name of the property to find.
	 * @param clazz The class of the property type the property is expected to have. For example, PropertyInt32.class.
	 * @return The ArrayProperty with values of type T with the given name or null if it hasn't been found.
	 */
	public <T extends Property> ArrayProperty<T> getArrayProperty(String name, Class<T> valuesClazz) {
		Property prop = get(name);
		
		return extractedArray(prop, valuesClazz);
	}
	/** Safe equivalent to <code>(ArrayProperty&lt;T&gt;)get(String name)</code>. Returns the first array property whose hash matches the given one, 
	 * but only if it's values are of the type given; if they aren't or there is no array property with that hash, this method will return null.
	 * <p>
	 * This is only for array properties. For single properties, use <code>&lt;T extends Property&gt;getProperty(String name, Class&lt;T&gt; clazz)</code>.
	 * 
	 * @param name The hash of the property to find.
	 * @param clazz The class of the property type the property is expected to have. For example, PropertyInt32.class.
	 * @return The ArrayProperty with values of type T with the given hash or null if it hasn't been found.
	 */
	public <T extends Property> ArrayProperty<T> getArrayProperty(int hash, Class<T> valuesClazz) {
		Property prop = get(hash);
		
		return extractedArray(prop, valuesClazz);
	}
	@SuppressWarnings("unchecked")
	private <T extends Property> ArrayProperty<T> extractedArray(Property prop, Class<T> valuesClazz) {
		if (prop != null) {
			if (ArrayProperty.class.isInstance(prop)) {
				if (((ArrayProperty<?>)prop).valueType == valuesClazz) {
					return (ArrayProperty<T>)prop;
				}
			}
			return null;
		} else {
			return null;
		}
	}
	
	public boolean contains(Property property) {
		return properties.contains(property);
	}
	public boolean contains(String name) {
		for (Property p : properties)
			if (p.nameStr.equals(name))
				return true;
		return false;
	}
	public boolean contains(int name) {
		for (Property p : properties)
			if (p.name == name)
				return true;
		return false;
	}
	
	public void add(Property property) {
		for (Property p : properties)
			if (p != null && p.name == property.name)
				return;
		properties.add(property);
	}
	
	public void remove(String name) {
		Property toRemove = null;
		for (Property prop : properties) {
			if (prop != null) {
				if (prop.nameStr.equals(name)) {
					toRemove = prop;
					break;
				}
			}
		}
		
		if (toRemove != null)
			properties.remove(toRemove);
	}
	
	/**
	 * Removes the first property found with the given hash, if present. This only removes one property, even if there is more than one property with
	 * the given hash.
	 * @param hash
	 */
	public void remove(int hash) {
		//To avoid a ConcurrentModificationException
		Property toRemove = null;
		for (Property prop : properties) {
			if (prop != null) {
				if (prop.name == hash) {
					toRemove = prop;
					break;
				}
			}
		}
		
		if (toRemove != null)
			properties.remove(toRemove);
	}
	
	/**
	 * Removes the given property of the properties list, if present.
	 *  
	 * @param property The property to remove from the list.
	 */
	public void remove(Property property) {
		properties.remove(property);
	}
	
	/** 
	 * Sorts the properties by their hash value, so the property with the lower hash comes first, and the property with the biggest hash comes later.
	 */
	public void orderByHash() {
//		List<Property> newProps = new ArrayList<Property>(properties);
		Collections.sort(properties, new Comparator<Property>() {
            @Override
            public int compare(final Property object1, final Property object2) {
                return (object1.name & 0xffffffffL) > (object2.name & 0xffffffffL) ? +1 : (object1.name & 0xffffffffL) < (object2.name & 0xffffffffL) ? -1 : 0;
            }
      	});
	}
	
	
	/**
	 * Sorts the properties alphabetically, by their registry name. Those properties without a registry name are put at the end of the list, where they will
	 * be ordered numerically, by their hash (so the lower hash comes first).
	 */
	public void orderByName() {
		List<Property> strings = new ArrayList<Property>();
		List<Property> nonStrings = new ArrayList<Property>();
		Property description = null; //Special one
		Property parent = null; //Special one
		for (Property prop : properties) {
			if (prop == null) {
//				properties.remove(i);
				continue;
			}
			if (prop.name == 0x00B2CCCA) {
				prop.nameStr = Hasher.getPropName(prop.name);
				description = prop;
			} else if (prop.name == 0x00B2CCCB) {
				prop.nameStr = Hasher.getPropName(prop.name);
				parent = prop;
			} else {
				prop.nameStr = Hasher.getPropName(prop.name);
				if (prop.nameStr.startsWith("#")) nonStrings.add(prop);
				else strings.add(prop);
			}
		}
		
		
		Collections.sort(strings, new Comparator<Property>() {
            @Override
            public int compare(final Property object1, final Property object2) {
                return object1.nameStr.toLowerCase().compareTo(object2.nameStr.toLowerCase());
            }
      	});
		Collections.sort(nonStrings, new Comparator<Property>() {
            @Override
            public int compare(final Property object1, final Property object2) {
                return (object1.name & 0xffffffffL) > (object2.name & 0xffffffffL) ? +1 : (object1.name & 0xffffffffL) < (object2.name & 0xffffffffL) ? -1 : 0;
            }
      	});
		List<Property> newProps = new ArrayList<Property>();
		newProps.add(parent);
		newProps.add(description);
		newProps.addAll(strings);
		newProps.addAll(nonStrings);
		
		properties = newProps;
	}
	
	/**
	 * Removes all null properties of this PROPMain instance. Null properties are usually caused by the property type 0.
	 */
	public void removeNulls() {
		//To avoid a ConcurrentModificationException
		List<Property> propertiesToRemove = new ArrayList<Property>();
		for (Property prop : properties) {
			if (prop == null) {
				propertiesToRemove.add(prop);
			}
		}
		
		for (Property prop : propertiesToRemove) {
			properties.remove(prop);
		}
	}
	
	/**
	 * Adds all the <strong>other</strong>'s properties to this PROPMain instance. By default, if a property already exists in the PROPMain, it overrides it, even if they are
	 * of a different type; the parameter `preserveThis` can be used to avoid that.
	 * 
	 * @param other The other PROPMain whose properties will be added to this instance.
	 * @param preserveThis If true, it won't override this instance properties.
	 */
	public void merge(PROPMain other, boolean preserveThis) {
		for (Property p : other.properties) {
			if (p != null) {
				if (preserveThis) {
					if (!contains(p.name)) properties.add(p);
				} else {
					remove(p.name);
					properties.add(p);
				}
			}
		}
	}
	
	
	public void write(String path) throws IOException {
		try (FileStreamAccessor out = new FileStreamAccessor(path, "rw", true)) {
			write(out);
		}
	}
	
	public String createAutolocaleFile(int tableID) {
		boolean hasAutolocale = false;
				
		StringBuilder sb = new StringBuilder();
		sb.append("# This file was autogenerated by SporeModder");
		sb.append(eol);
		int index = 1;
		for (Property p : properties) {
			if (p.type == PropertyText.PROP_TYPE) {
				// text properties are always in arrays
				@SuppressWarnings("unchecked")
				ArrayProperty<PropertyText> array = (ArrayProperty<PropertyText>) p;
				
				if (array.values != null) {
					for (PropertyText property : array.values) {
						int[] locale = property.getLocale();
						if (locale[0] == 0 && locale[1] == 0) {
							sb.append(Hasher.hashToHex(index, "0x") + " " + property.getText());
							sb.append(eol);
							
							property.setLocale(tableID, index);
							index++;
							hasAutolocale = true;
						}
					}
				}
			}
		}
		
		if (!hasAutolocale)
		{
			return null;
		}
		
		return sb.toString();
	}
	
	public void removeAutolocales() {
		for (Property p : properties) {
			if (p.type == PropertyText.PROP_TYPE) { 
				// text properties are always in arrays
				@SuppressWarnings("unchecked")
				ArrayProperty<PropertyText> array = (ArrayProperty<PropertyText>) p;
				
				if (array.values != null) {
					for (PropertyText property : array.values) {
						int[] locale = property.getLocale();
						String tableID = Hasher.getFileName(locale[0]);
						if (tableID.startsWith("auto_")) {
							// remove autolocale
							property.setLocale(0, 0);
						}
					}
				}
			}
		}
	}
	
	public static PROPMain propToXml(InputStreamAccessor in, OutputStreamAccessor out, boolean debugMode) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException, IOException, FileStructureException {
		PROPMain main = new PROPMain();
		main.readProp(in,debugMode);
		main.writeXML(out);
		return main;
	}
	
	public static PROPMain propToXml(InputStreamAccessor in, OutputStreamAccessor out) throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, FileStructureException, ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException, NoSuchFieldException {
		return propToXml(in, out, false);
	}
	
	public static PROPMain propToXml(String inPath, String outPath, boolean debugMode) throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, FileStructureException, ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException, NoSuchFieldException {
		try (FileStreamAccessor in = new FileStreamAccessor(inPath, "r");
				FileStreamAccessor out = new FileStreamAccessor(outPath, "rw", true);) {
			return propToXml(in, out, debugMode);
		}
	}
	
	public static PROPMain propToXml(String inPath, String outPath) throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, FileStructureException, ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException, NoSuchFieldException {
		try (FileStreamAccessor in = new FileStreamAccessor(inPath, "r");
				FileStreamAccessor out = new FileStreamAccessor(outPath, "rw", true);) {
			return propToXml(in, out, false);
		}
	}
	
	public static PROPMain propToXml(File inFile, File outFile, boolean debugMode) throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, FileStructureException, ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException, NoSuchFieldException {
		try (FileStreamAccessor in = new FileStreamAccessor(inFile, "r");
				FileStreamAccessor out = new FileStreamAccessor(outFile, "rw", true);) {
			return propToXml(in, out, debugMode);
		}
	}
	
	public static PROPMain propToXml(File inFile, File outFile) throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, FileStructureException, ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException, NoSuchFieldException {
		try (FileStreamAccessor in = new FileStreamAccessor(inFile, "r");
				FileStreamAccessor out = new FileStreamAccessor(outFile, "rw", true);) {
			return propToXml(in, out, false);
		}
	}
	
	
	public static PROPMain xmlToProp(InputStream in, OutputStreamAccessor out) throws DOMException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException, IOException, SAXException, InstantiationException {
		PROPMain main = new PROPMain();
		main.readXML(in);
		main.write(out);
		return main;
	}
	
	public static PROPMain xmlToProp(String inPath, String outPath) throws DOMException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException, IOException, SAXException, InstantiationException {
		try (InputStream in = new FileInputStream(inPath); 
				FileStreamAccessor out = new FileStreamAccessor(outPath, "rw", true)) {
			return xmlToProp(in, out);
		}
	}
	
	public static PROPMain xmlToProp(File inFile, File outFile) throws DOMException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException, IOException, SAXException, InstantiationException {
		try (InputStream in = new FileInputStream(inFile); 
				FileStreamAccessor out = new FileStreamAccessor(outFile, "rw", true)) {
			return xmlToProp(in, out);
		}
	}
	
	@Override
	public List<FileStructureError> getAllErrors() {
		return null;
	}
}
