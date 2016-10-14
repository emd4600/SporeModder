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

public class PropertyBool extends Property {
	private boolean value;
	public static final String singularName = "bool";
	public static final String pluralName = "bools";
	public static final int PROP_TYPE = 0x0001;
	public static final int itemSize = 1;
	
	public PropertyBool(int name, int type, int flags)
			throws InstantiationException, IllegalAccessException {
		super(name, type, flags);
		// TODO Auto-generated constructor stub
	}
	public PropertyBool(String name) throws IOException {
		super(name, PROP_TYPE);
	}
	public PropertyBool(String name, boolean value) throws IOException {
		super(name, PROP_TYPE);
		this.value = value;
	}
	
	@Override
	public String toString(boolean array) throws IOException {
		if (array) {
			return "\t\t<bool>" + Boolean.toString(value) + "</bool>" + PROPMain.eol;
		} else {
			return "\t<bool name=\"" + Hasher.getPropName(this.name) + "\">" + Boolean.toString(value) + "</bool>" + PROPMain.eol;
		}
	}
	@Override
	public void readProp(InputStreamAccessor in, boolean array) throws IOException {
		value = in.readBoolean();
	}
	@Override
	public void writeXML(Document doc, Element elem) {
		elem.appendChild(doc.createTextNode(Boolean.toString(value)));
	}
	@Override
	public void readXML(Element elem) {
		value = Boolean.parseBoolean(elem.getTextContent());
	}
	@Override
	public void writeProp(OutputStreamAccessor out, boolean array) throws IOException {
		out.writeBoolean(value);
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
		if (elem.getNodeName().length() > 4) {
			ArrayProperty<PropertyBool> array = new ArrayProperty<PropertyBool>(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
			array.valueType = PropertyBool.class;
			array.arrayItemSize = 1;
			
			NodeList nl = elem.getChildNodes();
			array.numValues = 0;
			List<Integer> indices = new ArrayList<Integer>();
			for (int n = 0; n < nl.getLength(); n++) {
				if (nl.item(n).getNodeType() == Node.ELEMENT_NODE) {
					array.numValues++;
					indices.add(n);
				}
			}
			array.setValues(new ArrayList<PropertyBool>());
			for (int a = 0; a < array.numValues; a++) {
				PropertyBool prop = new PropertyBool(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
				prop.readXML((Element)nl.item(indices.get(a)));
				array.addValue(prop);
			}
			return array;
		} else {
			PropertyBool prop = new PropertyBool(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0);
			prop.readXML(elem);
			return prop;
		}
	}
	
	
	public void setValue(boolean value) {
		this.value = value;
	}
	
	public boolean getValue() {
		return value;
	}
}
