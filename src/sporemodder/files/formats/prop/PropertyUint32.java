package sporemodder.files.formats.prop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sporemodder.MainApp;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.utilities.Hasher;
import sporemodder.utilities.names.NameRegistry;

public class PropertyUint32 extends Property {
	public long value;
	public static final String singularName = "uint32";
	public static final String pluralName = "uint32s";
	public static final int PROP_TYPE = 0x000A;
	public static final int itemSize = 4;
	
	public PropertyUint32(int name, int type, int flags)
			throws InstantiationException, IllegalAccessException {
		super(name, type, flags);
		// TODO Auto-generated constructor stub
	}
	public PropertyUint32(String name) throws IOException {
		super(name, PROP_TYPE);
	}
	public PropertyUint32(String name, long value) throws IOException {
		super(name, PROP_TYPE);
		this.value = value;
	}
	
	@Override
	public String toString(boolean array) throws IOException {
		String str = null;
		if (!getDebugMode() && value != 0) {
			str = MainApp.getRegistry(NameRegistry.NAME_FILE).getName((int)(value & 0xFFFFFFFF));
			if (str == null && Hasher.UsedNames != null)
			{
				str = Hasher.UsedNames.getName((int)(value & 0xFFFFFFFF));
			}
			
			if (str != null)
			{
				str = "$" + str;
			}
			else if (value > 0x00FFFFFF) {
				str = Hasher.hashToHex((int)(value & 0xFFFFFFFF), "0x");
			}
		}
		if (str == null) {
			str = Long.toString(value);
		}
		
		if (array) {
			return "\t\t<uint32>" + str + "</uint32>" + PROPMain.eol;
		} else {
			return "\t<uint32 name=\"" + Hasher.getPropName(this.name) + "\">" + str + "</uint32>" + PROPMain.eol;
		}
	}
	@Override
	public void readProp(InputStreamAccessor in, boolean array) throws IOException {
		value = in.readUInt();
	}
	@Override
	public void writeXML(Document doc, Element elem) {
		elem.appendChild(doc.createTextNode(Long.toString(value)));
	}
	@Override
	public void readXML(Element elem) {
		String str = elem.getTextContent();
		value = Hasher.decodeUInt(str);
	}
	@Override
	public void writeProp(OutputStreamAccessor out, boolean array) throws IOException {
		out.writeUInt(value);
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
		if (elem.getNodeName().length() > 6) {
			ArrayProperty<PropertyUint32> array = new ArrayProperty<PropertyUint32>(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
			array.valueType = PropertyUint32.class;
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
			array.setValues(new ArrayList<PropertyUint32>());
			for (int a = 0; a < array.numValues; a++) {
				PropertyUint32 prop = new PropertyUint32(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
				prop.readXML((Element)nl.item(indices.get(a)));
				array.addValue(prop);
			}
			return array;
		} else {
			PropertyUint32 prop = new PropertyUint32(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0);
			prop.readXML(elem);
			return prop;
		}
	}
	
	
	public void setValue(long value) {
		this.value = value;
	}
	
	public long getValue() {
		return value;
	}
}
