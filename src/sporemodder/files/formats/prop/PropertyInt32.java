package sporemodder.files.formats.prop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;

import sporemodder.MainApp;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.utilities.Hasher;
import sporemodder.utilities.names.NameRegistry;

public class PropertyInt32 extends Property {
	private int value;
	public static final String singularName = "int32";
	public static final String pluralName = "int32s";
	public static final int PROP_TYPE = 0x0009;
	public static final int itemSize = 4;
	
	public PropertyInt32(int name, int type, int flags) {
		super(name, type, flags);
	}
	public PropertyInt32(String name) {
		super(name, PROP_TYPE);
	}
	public PropertyInt32(String name, int value) {
		super(name, PROP_TYPE);
		this.value = value;
	}
	
	@Override
	public String toString(boolean array) {
		String str = null;
		if (!getDebugMode() && value != 0) {
			str = MainApp.getRegistry(NameRegistry.NAME_FILE).getName(value);
			if (str == null && Hasher.UsedNames != null)
			{
				str = Hasher.UsedNames.getName((int)(value & 0xFFFFFFFF));
			}
			
			if (str != null)
			{
				str = "$" + str;
			}
			else if (value < -10000000 || value > 0x00FFFFFF) {
				str = Hasher.hashToHex(value, "0x");
			}
		}
		if (str == null) {
			str = Integer.toString(value);
		}
		
		if (array) {
			return "\t\t<int32>" + str + "</int32>" + PROPMain.eol;
		} else {
			return "\t<int32 name=\"" + Hasher.getPropName(this.name) + "\">" + str + "</int32>" + PROPMain.eol;
		}
	}
	@Override
	public void readProp(InputStreamAccessor in, boolean array) throws IOException {
		value = in.readInt();
	}
	@Override
	public void writeXML(Document doc, Element elem) {
		elem.appendChild(doc.createTextNode(Integer.toString(value)));
	}
	@Override
	public void readXML(Element elem) {
		value = Hasher.decodeInt(elem.getTextContent());
	}
	@Override
	public void writeProp(OutputStreamAccessor out, boolean array) throws IOException {
		out.writeInt(value);
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
			ArrayProperty<PropertyInt32> array = new ArrayProperty<PropertyInt32>(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
			array.valueType = PropertyInt32.class;
			array.arrayItemSize = 4;
			
			NodeList nl = elem.getChildNodes();
			array.numValues = 0;
			List<Integer> indices = new ArrayList<Integer>();
			for (int n = 0; n < nl.getLength(); n++) {
				if (nl.item(n).getNodeType() == Node.ELEMENT_NODE) {
					array.numValues++;
					indices.add(n);
				}
			}
			array.setValues(new ArrayList<PropertyInt32>());
			for (int a = 0; a < array.numValues; a++) {
				PropertyInt32 prop = new PropertyInt32(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
				prop.readXML((Element)nl.item(indices.get(a)));
				array.addValue(prop);
			}
			return array;
		} else {
			PropertyInt32 prop = new PropertyInt32(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0);
			prop.readXML(elem);
			return prop;
		}
	}
	
	
	public void setValue(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}
	
	@SuppressWarnings("unused")
	public static void fastConvert(OutputStreamAccessor stream, Attributes attributes, String text) throws IOException {
		stream.writeInt(Hasher.decodeInt(text));
	}
}
