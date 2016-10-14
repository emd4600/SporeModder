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

public class PropertyKey extends Property {
	private int[] values = new int[3];
	private int unk = 0; //Sometimes present when not array, I think
	public static final String singularName = "key";
	public static final String pluralName = "keys";
	public static final int PROP_TYPE = 0x0020;
	public static final int itemSize = 12;
	
	public PropertyKey(int name, int type, int flags)
			throws InstantiationException, IllegalAccessException {
		super(name, type, flags);
		// TODO Auto-generated constructor stub
	}
	public PropertyKey(String name) throws IOException {
		super(name, PROP_TYPE);
	}
	public PropertyKey(String name, int group, int file, int type) throws IOException {
		super(name, PROP_TYPE);
		this.values[0] = group;
		this.values[1] = file;
		this.values[2] = type;
	}
	public PropertyKey(String name, int ... args) throws IOException {
		super(name, PROP_TYPE);
		for (int i = 0; i < args.length; i++) {
			this.values[i] = args[i];
		}
	}

	@Override
	public String toString(boolean array) throws IOException {
		if (array) {
			String str = "\t\t<key ";
			if (values[0] != 0) str += "groupid=\"" + Hasher.getFileName(values[0]) + "\" ";
			/*if (values[1] != 0)*/ str += "instanceid=\"" + Hasher.getFileName(values[1]) + "\" ";
			if (values[2] != 0) str += "typeid=\"" + Hasher.getTypeName(values[2]) + "\" ";
			if (getDebugMode() && unk != 0) str += "unknown" + Hasher.getTypeName(unk) + "\" ";
			str += "/>" + PROPMain.eol;
			return str;
		} else {
			String str = "\t<key name=\"" + Hasher.getPropName(this.name) + "\" ";
			if (values[0] != 0) str += "groupid=\"" + Hasher.getFileName(values[0]) + "\" ";
			/*if (values[1] != 0)*/ str += "instanceid=\"" + Hasher.getFileName(values[1]) + "\" ";
			if (values[2] != 0) str += "typeid=\"" + Hasher.getTypeName(values[2]) + "\" ";
			if (getDebugMode() && unk != 0) str += "unknown=\"" + Hasher.getTypeName(unk) + "\" ";
			str += "/>" + PROPMain.eol;
			return str;
		}
	}
	@Override
	public void readProp(InputStreamAccessor in, boolean array) throws IOException {
		if (array) {
			values[1] = in.readLEInt();
			values[2] = in.readLEInt();
			values[0] = in.readLEInt();
		} else {
			values[1] = in.readLEInt();
			values[2] = in.readLEInt();
			values[0] = in.readLEInt();
			unk = in.readLEInt();
		}
	}
	@Override
	public void writeXML(Document doc, Element elem) {
		elem.appendChild(doc.createTextNode(Hasher.getFileName(values[0])+
				"/"+Hasher.getFileName(values[1])+"."+Hasher.getTypeName(values[2])));
	}
	@Override
	public void readXML(Element elem) throws IOException {
		String str1 = elem.getAttribute("groupid");
		if (str1.length() > 0) {
			values[0] = Hasher.getFileHash(str1);
		}
		String str2 = elem.getAttribute("instanceid");
		if (str2.length() > 0) {
			values[1] = Hasher.getFileHash(str2);
		}
		String str3 = elem.getAttribute("typeid");
		if (str3.length() > 0) {
			values[2] = Hasher.getTypeHash(str3);
		}
		
		String str4 = elem.getAttribute("unknown");
		if (str4.length() > 0) {
			unk = Hasher.getFileHash(str4);
		}
	}
	@Override
	public void writeProp(OutputStreamAccessor out, boolean array) throws IOException {
		out.writeLEInt(values[1]);
		out.writeLEInt(values[2]);
		out.writeLEInt(values[0]);
		if (!array) {
			out.writeLEInt(unk);
		}
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
		if (elem.getNodeName().length() > 3) {
			ArrayProperty<PropertyKey> array = new ArrayProperty<PropertyKey>(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
			array.valueType = PropertyKey.class;
			array.arrayItemSize = 12;
			
			NodeList nl = elem.getChildNodes();
			array.numValues = 0;
			List<Integer> indices = new ArrayList<Integer>();
			for (int n = 0; n < nl.getLength(); n++) {
				if (nl.item(n).getNodeType() == Node.ELEMENT_NODE) {
					array.numValues++;
					indices.add(n);
				}
			}
			array.setValues(new ArrayList<PropertyKey>());
			for (int a = 0; a < array.numValues; a++) {
				PropertyKey prop = new PropertyKey(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
				prop.readXML((Element)nl.item(indices.get(a)));
				array.addValue(prop);
			}
			return array;
		} else {
			PropertyKey prop = new PropertyKey(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0);
			prop.readXML(elem);
			return prop;
		}
	}
	
	
	public void setKey(int group, int name, int type) {
		this.values[0] = group;
		this.values[1] = group;
		this.values[2] = group;
	}
	
	public int[] getValue() {
		return values;
	}
	
	
	public void setUnk(int unk) {
		this.unk = unk;
	}
	
	public int getUnk() {
		return unk;
	}
}
