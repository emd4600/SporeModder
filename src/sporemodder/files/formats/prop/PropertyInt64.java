package sporemodder.files.formats.prop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.utilities.Hasher;

public class PropertyInt64 extends Property {
	private long value;
	public static final String singularName = "int64";
	public static final String pluralName = "int64s";
	public static final int PROP_TYPE = 0x000B;
	public static final int itemSize = 8;
	
	public PropertyInt64(int name, int type, int flags)
			throws InstantiationException, IllegalAccessException {
		super(name, type, flags);
		// TODO Auto-generated constructor stub
	}
	public PropertyInt64(String name) throws IOException {
		super(name, PROP_TYPE);
	}
	public PropertyInt64(String name, long value) throws IOException {
		super(name, PROP_TYPE);
		this.value = value;
	}
	
	@Override
	public String toString(boolean array) throws IOException {
		if (array) {
			return "\t\t<int64>" + Long.toString(value) + "</int64>" + PROPMain.eol;
		} else {
			return "\t<int64 name=\"" + Hasher.getPropName(this.name) + "\">" + Long.toString(value) + "</int64>" + PROPMain.eol;
		}
	}
	@Override
	public void readProp(InputStreamAccessor in, boolean array) throws IOException {
		value = in.readLong();
	}
	@Override
	public void writeXML(Document doc, Element elem) {
		elem.appendChild(doc.createTextNode(Long.toString(value)));
	}
	@Override
	public void readXML(Element elem) {
		value = Long.parseLong(elem.getTextContent());
	}
	@Override
	public void writeProp(OutputStreamAccessor out, boolean array) throws IOException {
		out.writeLong(value);
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
			ArrayProperty<PropertyInt64> array = new ArrayProperty<PropertyInt64>(Hasher.getPropHash(elem.getAttribute("name")), 0x000B, 0x30);
			array.valueType = PropertyInt64.class;
			array.arrayItemSize = 8;
			
			NodeList nl = elem.getChildNodes();
			array.numValues = 0;
			List<Integer> indices = new ArrayList<Integer>();
			for (int n = 0; n < nl.getLength(); n++) {
				if (nl.item(n).getNodeType() == Node.ELEMENT_NODE) {
					array.numValues++;
					indices.add(n);
				}
			}
			array.setValues(new ArrayList<PropertyInt64>());
			for (int a = 0; a < array.numValues; a++) {
				PropertyInt64 prop = new PropertyInt64(Hasher.getPropHash(elem.getAttribute("name")), 0x000B, 0x30);
				prop.readXML((Element)nl.item(indices.get(a)));
				array.addValue(prop);
			}
			return array;
		} else {
			PropertyInt64 prop = new PropertyInt64(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0);
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
