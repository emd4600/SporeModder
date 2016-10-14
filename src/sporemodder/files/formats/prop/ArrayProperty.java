package sporemodder.files.formats.prop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.utilities.Hasher;

public class ArrayProperty<T extends Property> extends Property {
	public int arrayItemSize;
	public List<T> values;
	public int numValues;
	public Class<? extends Property> valueType;
	
	public ArrayProperty(int name, int type, int flags) throws InstantiationException, IllegalAccessException {
		super(name, type, flags);
	}
	
	//Used when it has no values
	public ArrayProperty(String name, Class<? extends Property> type) throws InstantiationException, IllegalAccessException, IOException {
		super(name);
		valueType = type;
		this.type = Property.getPropType(type);
		this.flags = 0x30;
		try {
			arrayItemSize = type.getField("itemSize").getInt(null);
		} catch (IllegalArgumentException | NoSuchFieldException
				| SecurityException e) {
			e.printStackTrace();
		}
		values = new ArrayList<T>();
		
	}
	public ArrayProperty(String name, List<T> properties) throws InstantiationException, IllegalAccessException, IOException {
		super(name);
		this.values = properties;
		valueType = properties.get(0).getClass();
		this.type = Property.getPropType(valueType);
		this.flags = 0x30;
		this.values = properties;
		try {
			arrayItemSize = valueType.getField("itemSize").getInt(null);
		} catch (IllegalArgumentException | NoSuchFieldException
				| SecurityException e) {
			e.printStackTrace();
		}
		numValues = values.size();
	}
	
	public void readInfo(InputStreamAccessor in) throws IOException {
		numValues = in.readInt();
		arrayItemSize = in.readInt();
//		values = new Property[numValues];
		values = new ArrayList<T>();
	}
	@Override
	public void readProp(InputStreamAccessor in, boolean array) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void writeXML(Document doc, Element elem) {
		numValues = values.size();
		for (Property value : values) {
			Element newElem = doc.createElement(value.getSingularName());
			elem.appendChild(newElem);
			value.writeXML(doc, newElem);
		}
	}
	@Override
	public String toString(boolean array) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, IOException {
		String line = "\t<" + getPluralName() + " name=\"" + Hasher.getPropName(name) + "\">" + PROPMain.eol;
		for (Property prop : values) {
			line += prop.toString(true);
		}
		line += "\t</" + getPluralName() + ">" + PROPMain.eol;
		return line;
	}
	@Override
	public void readXML(Element elem) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void writeProp(OutputStreamAccessor out, boolean array) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public String getSingularName() {
		return singularName;
	}
	@Override
	public String getPluralName() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		return (String)valueType.getDeclaredField("pluralName").get(null);
	}
	
	
	public void setValues(List<T> properties) {
		values = properties;
	}
	
	public void setValueType(Class<? extends Property> c) {
		valueType = c;
	}
	
	public Class<? extends Property> getValueType() {
		return valueType;
	}
	
	public List<T> getValues() {
		return values;
	}
	
	public void addValue(T prop) {
		values.add(prop);
	}
}
