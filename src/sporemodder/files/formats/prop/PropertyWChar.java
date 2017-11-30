package sporemodder.files.formats.prop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.utilities.Hasher;

public class PropertyWChar extends Property {
	private char value;
	public static final String singularName = "wchar";
	public static final String pluralName = "wchars";
	public static final int PROP_TYPE = 0x0003;
	public static final int itemSize = 2;
	
	public PropertyWChar(int name, int type, int flags) {
		super(name, type, flags);
	}
	public PropertyWChar(String name) {
		super(name, PROP_TYPE);
	}
	public PropertyWChar(String name, char value) {
		super(name, PROP_TYPE);
		this.value = value;
	}

	/**
	  * It reads the wChar part of a property in a .prop and it writes the result in a .prop.xml
	  *
	  * @param doc The output XML document.
	  * @param in The input .prop file.
	  * @param PropFlags The PropFlags of property.
	  * @param PropIdentifierStr The property id in a string.
	  * @param rootElement The root XML element (always <properties>, only to append other elements)
	  */
//	public static void ReadPropWriteXML(Document doc, StreamAccessor in, short PropFlags, String PropIdentifierStr, Element rootElement) throws IOException { //Probar con try catch para no perder el archivo entero
//		if (PropFlags == 0x9C || PropFlags == 0x30) {
//			Element elemWChars = doc.createElement("wChars");
//			elemWChars.setAttribute("name", PropIdentifierStr);
//			int ArrayCount = in.readInt();
//			int ArrayItemSize = in.readInt();
//			for (int b = 0; b < ArrayCount; b++) {
//				char wCharValueWrong = in.readChar();
//				char wCharValue = Endiannes.charFromLittleEndian(wCharValueWrong);
//				Element elemWChar = doc.createElement("wChar");
//				elemWChar.appendChild(doc.createTextNode(Character.toString(wCharValue)));
//				elemWChars.appendChild(elemWChar);
//			}
//			rootElement.appendChild(elemWChars);
//		} else {
//			char wCharValueWrong = in.readChar();
//			char wCharValue = Endiannes.charFromLittleEndian(wCharValueWrong);
//			Element elemWChar = doc.createElement("wChar");
//			elemWChar.setAttribute("name", PropIdentifierStr);
//			elemWChar.appendChild(doc.createTextNode(Character.toString(wCharValue)));
//			rootElement.appendChild(elemWChar);
//		}
//	}
	/**
	  * It reads an wchar property in a .prop.xml and it writes the result in a .prop
	  *
	  * @param doc The output XML document.
	  * @param out The output .prop file.
	  * @param eElement The element of the property.
	  * @param eElementName The eElement name string.
	  */
//	public static void ReadXMLWriteProp(Document doc, StreamAccessor out, Element eElement, String eElementName) throws IOException { //Probar con try catch para no perder el archivo entero
//		if (eElementName == "wchars") {
//			String PropIdentifierStr = eElement.getAttribute("name");
//			int PropIdentifier = Hasher.getPropertyIDInt(PropIdentifierStr);
//			NodeList nlWChars = eElement.getChildNodes();
//			out.writeInt(PropIdentifier);
//			out.writeShort(0x0003);
//			out.writeShort(0x0030); //El SporeMaster lo hace así. Habría que averiguar que diferencia hay entre escribir 0x30 y 0x9C
//			int ArrayNumber = 0;
//			for (int a = 0; a < nlWChars.getLength(); a++) {
//				if (nlWChars.item(a).getNodeType() == Node.ELEMENT_NODE) {
//					ArrayNumber += 1;
//				}
//			}
//			out.writeInt(ArrayNumber);
//			out.writeInt(1);
//			for (int i = 0; i < nlWChars.getLength(); i++) {
//				Node nWChar = nlWChars.item(i);
//				System.out.println(nWChar.getNodeName());
//				if (nWChar.getNodeType() == Node.ELEMENT_NODE) {
//					char wcharValue = (char)(nWChar.getTextContent()).charAt(0);
//					Character.
//					out.writeChar(wcharValue);
//				}
//			}
//		} else {
//			String PropIdentifierStr = eElement.getAttribute("name");
//			int PropIdentifier = Hasher.getPropertyIDInt(PropIdentifierStr);
//			out.writeInt(PropIdentifier);
//			out.writeShort(0x0003);
//			out.writeShort(0);
//			Node nWChar = eElement.getFirstChild();
//			char wcharValue = (char)Endiannes.charToLittleEndian((nWChar.getTextContent()).charAt(0));
//			out.writeChar(wcharValue);
//		}

	@Override
	public void readProp(InputStreamAccessor in, boolean array) throws IOException {
		value = in.readChar();
	}

	@Override
	public void writeXML(Document doc, Element elem) {
		elem.appendChild(doc.createTextNode(Character.toString(value)));
	}

	@Override
	public void readXML(Element elem) {
		//TODO
	}

	@Override
	public void writeProp(OutputStreamAccessor out, boolean array) throws IOException {
		out.writeUShort(Character.getNumericValue(value));
	}
	@Override
	public String getSingularName() {
		return singularName;
	}
	@Override
	public String getPluralName() {
		return pluralName;
	}
	
	public static Property processNewProperty(Element elem) throws InstantiationException, IllegalAccessException, IOException {
		if (elem.getNodeName().length() > 5) {
			ArrayProperty<PropertyWChar> array = new ArrayProperty<PropertyWChar>(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
			array.valueType = PropertyWChar.class;
			array.arrayItemSize = 2;
			
			NodeList nl = elem.getChildNodes();
			array.numValues = 0;
			List<Integer> indices = new ArrayList<Integer>();
			for (int n = 0; n < nl.getLength(); n++) {
				if (nl.item(n).getNodeType() == Node.ELEMENT_NODE) {
					array.numValues++;
					indices.add(n);
				}
			}
			array.setValues(new ArrayList<PropertyWChar>());
			for (int a = 0; a < array.numValues; a++) {
				PropertyWChar prop = new PropertyWChar(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
				prop.readXML((Element)nl.item(indices.get(a)));
				array.addValue(prop);
			}
			return array;
		} else {
			PropertyWChar prop = new PropertyWChar(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0);
			prop.readXML(elem);
			return prop;
		}
	}

	@Override
	public String toString(boolean array) {
		if (array) {
			return "\t\t<wchar>" + Character.toString(value) + "</wchar>" + PROPMain.eol;
		} else {
			return "\t<char name=\"" + Hasher.getPropName(this.name) + "\">" + Character.toString(value) + "</char>" + PROPMain.eol;
		}
	}
	
	
	public void setValue(char value) {
		this.value = value;
	}
	
	public char getValue() {
		return value;
	}
	
	@SuppressWarnings("unused")
	public static void fastConvert(OutputStreamAccessor stream, Attributes attributes, String text) throws IOException {
		stream.writeShort((int)text.charAt(0));
	}
}
