package sporemodder.files.formats.prop;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.utilities.Hasher;
import sporemodder.utilities.Matrix;

public class PropertyTransform extends Property {
	private final float[] pos = new float[3];
	private float scale = 1.0f;
	private final Matrix matrix = Matrix.getIdentity();
	private int flag;
	public static final String singularName = "transform";
	public static final String pluralName = "transforms";
	public static final int PROP_TYPE = 0x0038;
	public static final int itemSize = 56;
	
	public boolean convertToEuler = false; //TODO Dynamic
	
	public PropertyTransform(int name, int type, int flags) {
		super(name, type, flags);
	}
	public PropertyTransform(String name) {
		super(name, PROP_TYPE);
	}
	public PropertyTransform(String name, float[] pos, float scale, Matrix rotation) {
		super(name, PROP_TYPE);
		this.pos[0] = pos[0];
		this.pos[1] = pos[1];
		this.pos[2] = pos[2];
		this.scale = scale;
		this.matrix.copy(rotation);
		flag = 0x06000300; //TODO Fix this
	}
	
	@Override
	public String toString(boolean array) {
		NumberFormat nf = Hasher.getDecimalFormat("#.#######");
		if (array) {
			if (!getDebugMode()) {
				float[] eulers = new Matrix(matrix).toEulerDegrees();
				return "\t\t<transform flags=\"" + Hasher.hashToHex(flag) + "\" pos=\"" + nf.format(pos[0]) + ", " + nf.format(pos[1]) + 
						", " + nf.format(pos[2]) + "\" scale=\"" + nf.format(scale) + "\" rot_x=\"" + nf.format(eulers[0]) + 
						 "\" rot_y=\"" + nf.format(eulers[1]) + "\" rot_z=\"" + nf.format(eulers[2]) + "\" />" + PROPMain.eol;
			} else {
				return "\t\t<transform flags=\"" + Hasher.hashToHex(flag) + "\" pos=\"" + nf.format(pos[0]) + ", " + nf.format(pos[1]) + 
						", " + nf.format(pos[2]) + "\" scale=\"" + nf.format(scale) + "\" row1=\"" + nf.format(matrix.m[0][0]) + 
						", " + nf.format(matrix.m[0][1]) + ", " + nf.format(matrix.m[0][2]) + "\" row2=\"" + nf.format(matrix.m[1][0]) + 
						", " + nf.format(matrix.m[1][1]) + ", " + nf.format(matrix.m[1][2]) + "\" row3=\"" + nf.format(matrix.m[2][0]) + 
						", " + nf.format(matrix.m[2][1]) + ", " + nf.format(matrix.m[2][2]) + "\" />" + PROPMain.eol;
			}
		} else {
			throw new UnsupportedOperationException("PROP001; Non-array transform is unimplemented");
		}
	}
	@Override
	public void readProp(InputStreamAccessor in, boolean array) throws IOException {
		if (array) {
			flag = in.readInt();
			for (int i = 0; i < 3; i++) {
				pos[i] = in.readLEFloat();
			}
			scale = in.readLEFloat();
			matrix.readLE(in);
			if (size != 56) {
				throw new IOException("PROP001; Non-56 size transform is unimplemented");
			}
		} else {
			throw new IOException("PROP001; Non-array transform is unimplemented");
		}
	}
	@Override
	public void writeXML(Document doc, Element elem) {
		elem.setAttribute("flags", Hasher.hashToHex(flag));
		elem.setAttribute("pos", Float.toString(pos[0]) + ", " + Float.toString(pos[1]) + ", " + Float.toString(pos[2]));
		elem.setAttribute("scale", Float.toString(scale));
		elem.setAttribute("row1", Float.toString(matrix.m[0][0]) + ", " + Float.toString(matrix.m[0][1]) + ", " + Float.toString(matrix.m[0][2]));
		elem.setAttribute("row2", Float.toString(matrix.m[1][0]) + ", " + Float.toString(matrix.m[1][1]) + ", " + Float.toString(matrix.m[1][2]));
		elem.setAttribute("row3", Float.toString(matrix.m[2][0]) + ", " + Float.toString(matrix.m[2][1]) + ", " + Float.toString(matrix.m[2][2]));
	}
	@Override
	public void readXML(Element elem) throws IOException {
		flag = Hasher.getFileHash(elem.getAttribute("flags"));
		String[] splits1 = elem.getAttribute("pos").split(",\\s*?");
		pos[0] = Float.parseFloat(splits1[0]);
		pos[1] = Float.parseFloat(splits1[1]);
		pos[2] = Float.parseFloat(splits1[2]);
		
		scale = Float.parseFloat(elem.getAttribute("scale"));
		
		String value = null;
		
		final double[] euler = new double[3];
		boolean bUsesEuler = false;
		
		value = elem.getAttribute("euler");
		if (!value.isEmpty()) {
			String[] splits = value.split(",\\s*?");
			if (splits.length != 3) {
				throw new IOException("PROP File: Expected 3 values in attribute 'euler' in property type 'transform'.");
			}
			
			euler[0] = Math.toRadians(Double.parseDouble(splits[0]));
			euler[1] = Math.toRadians(Double.parseDouble(splits[1]));
			euler[2] = Math.toRadians(Double.parseDouble(splits[2]));
			bUsesEuler = true;
		}
		
		value = elem.getAttribute("rot_x");
		if (value.isEmpty()) value = elem.getAttribute("rotX");
		if (!value.isEmpty()) {
			euler[0] = Math.toRadians(Double.parseDouble(value));
			bUsesEuler = true;
		}
		
		value = elem.getAttribute("rot_y");
		if (value.isEmpty()) value = elem.getAttribute("rotY");
		if (!value.isEmpty()) {
			euler[1] = Math.toRadians(Double.parseDouble(value));
			bUsesEuler = true;
		}
		
		value = elem.getAttribute("rot_z");
		if (value.isEmpty()) value = elem.getAttribute("rotZ");
		if (!value.isEmpty()) {
			euler[2] = Math.toRadians(Double.parseDouble(value));
			bUsesEuler = true;
		}
		
		if (bUsesEuler) {
			matrix.rotate(euler[0], euler[1], euler[2]);
		}
		
		value = elem.getAttribute("row1");
		if (!value.isEmpty()) {
			String[] splits2 = value.split(",\\s*?");
			matrix.m[0][0] = Float.parseFloat(splits2[0]);
			matrix.m[0][1] = Float.parseFloat(splits2[1]);
			matrix.m[0][2] = Float.parseFloat(splits2[2]);
		}
		
		value = elem.getAttribute("row2");
		if (!value.isEmpty()) {
			String[] splits2 = value.split(",\\s*?");
			matrix.m[1][0] = Float.parseFloat(splits2[0]);
			matrix.m[1][1] = Float.parseFloat(splits2[1]);
			matrix.m[1][2] = Float.parseFloat(splits2[2]);
		}
		
		value = elem.getAttribute("row3");
		if (!value.isEmpty()) {
			String[] splits2 = value.split(",\\s*?");
			matrix.m[2][0] = Float.parseFloat(splits2[0]);
			matrix.m[2][1] = Float.parseFloat(splits2[1]);
			matrix.m[2][2] = Float.parseFloat(splits2[2]);
		}
		
	}
	@Override
	public void writeProp(OutputStreamAccessor out, boolean array) throws IOException {
		if (array) {
			out.writeInt(flag);
			for (float f : pos) {
				out.writeLEFloat(f);
			}
			out.writeLEFloat(scale);
//			for (float[] f : matrix) {
//				out.writeLEFloat(f[0]);
//				out.writeLEFloat(f[1]);
//				out.writeLEFloat(f[2]);
//			}
			for (int i = 0; i < 3; i++) out.writeLEFloat(matrix.m[i][0]);
			for (int i = 0; i < 3; i++) out.writeLEFloat(matrix.m[i][1]);
			for (int i = 0; i < 3; i++) out.writeLEFloat(matrix.m[i][2]);
		} else {
			throw new IOException("PROP001; Non-array transform is unimplemented");
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
		if (elem.getNodeName().length() > 9) {
			ArrayProperty<PropertyTransform> array = new ArrayProperty<PropertyTransform>(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
			array.valueType = PropertyTransform.class;
			array.arrayItemSize = 56;
			
			NodeList nl = elem.getChildNodes();
			array.numValues = 0;
			List<Integer> indices = new ArrayList<Integer>();
			for (int n = 0; n < nl.getLength(); n++) {
				if (nl.item(n).getNodeType() == Node.ELEMENT_NODE) {
					array.numValues++;
					indices.add(n);
				}
			}
			array.setValues(new ArrayList<PropertyTransform>());
			for (int a = 0; a < array.numValues; a++) {
				PropertyTransform prop = new PropertyTransform(Hasher.getPropHash(elem.getAttribute("name")), PROP_TYPE, 0x30);
				prop.readXML((Element)nl.item(indices.get(a)));
				array.addValue(prop);
			}
			return array;
		} else {
			throw new IOException("PROP001; Non-array transform is unimplemented");
		}
	}
	
	
	public void setPosition(float[] position) {
		this.pos[0] = position[0];
		this.pos[1] = position[1];
		this.pos[2] = position[2];
	}
	public void setScale(float scale) {
		this.scale = scale;
	}
	public void setMatrix(Matrix matrix) {
		this.matrix.copy(matrix);
	}
	
	public float[] getPosition() {
		return pos;
	}
	public float getScale() {
		return scale;
	}
	public Matrix getMatrix() {
		return new Matrix(matrix);
	}
	
	
	@SuppressWarnings("unused")
	public static void fastConvert(OutputStreamAccessor stream, Attributes attributes, String text) throws SAXException, IOException {
		final float[] offset = new float[3];
		float scale = 1.0f;
		final Matrix matrix = Matrix.getIdentity();
		
		final double[] euler = new double[3];  // alternative way to set rotation
		boolean bUsesEuler = false;
		
		int flags = 0x06000300;  // this means all the transformations are processed
		
		String value = null;  // for attributes;
		
		
		value = attributes.getValue("flags");
		if (value != null) flags = Hasher.decodeInt(value);
		
		value = attributes.getValue("pos");
		if (value == null) value = attributes.getValue("offset");
		if (value != null) {
			String[] splits = value.split(",\\s*?");
			if (splits.length != 3) {
				throw new SAXException("PROP File: Expected 3 values in attribute 'offset' in property type 'transform'.");
			}
			
			
			offset[0] = Float.parseFloat(splits[0]);
			offset[1] = Float.parseFloat(splits[1]);
			offset[2] = Float.parseFloat(splits[2]);
		}
		
		value = attributes.getValue("scale");
		if (value != null) scale = Float.parseFloat(value);
		
		
		// euler angles
		value = attributes.getValue("euler");
		if (value != null) {
			String[] splits = value.split(",\\s*?");
			if (splits.length != 3) {
				throw new SAXException("PROP File: Expected 3 values in attribute 'euler' in property type 'transform'.");
			}
			
			euler[0] = Math.toRadians(Double.parseDouble(splits[0]));
			euler[1] = Math.toRadians(Double.parseDouble(splits[1]));
			euler[2] = Math.toRadians(Double.parseDouble(splits[2]));
			bUsesEuler = true;
		}
		
		value = attributes.getValue("rot_x");
		if (value == null) value = attributes.getValue("rotX");
		if (value != null) {
			euler[0] = Math.toRadians(Double.parseDouble(value));
			bUsesEuler = true;
		}
		
		value = attributes.getValue("rot_y");
		if (value == null) value = attributes.getValue("rotY");
		if (value != null) {
			euler[1] = Math.toRadians(Double.parseDouble(value));
			bUsesEuler = true;
		}
		
		value = attributes.getValue("rot_z");
		if (value == null) value = attributes.getValue("rotZ");
		if (value != null) {
			euler[2] = Math.toRadians(Double.parseDouble(value));
			bUsesEuler = true;
		}
		
		if (bUsesEuler) {
			matrix.rotate(euler[0], euler[1], euler[2]);
		}
		
		// raw matrix
		value = attributes.getValue("row1");
		if (value != null) {
			String[] splits = value.split(",\\s*?");
			if (splits.length != 3) {
				throw new SAXException("PROP File: Expected 3 values in attribute 'row1' in property type 'transform'.");
			}
			
			matrix.m[0][0] = Float.parseFloat(splits[0]);
			matrix.m[0][1] = Float.parseFloat(splits[1]);
			matrix.m[0][2] = Float.parseFloat(splits[2]);
		}
		
		value = attributes.getValue("row2");
		if (value != null) {
			String[] splits = value.split(",\\s*?");
			if (splits.length != 3) {
				throw new SAXException("PROP File: Expected 3 values in attribute 'row2' in property type 'transform'.");
			}
			
			matrix.m[1][0] = Float.parseFloat(splits[0]);
			matrix.m[1][1] = Float.parseFloat(splits[1]);
			matrix.m[1][2] = Float.parseFloat(splits[2]);
		}
		
		value = attributes.getValue("row3");
		if (value != null) {
			String[] splits = value.split(",\\s*?");
			if (splits.length != 3) {
				throw new SAXException("PROP File: Expected 3 values in attribute 'row3' in property type 'transform'.");
			}
			
			matrix.m[2][0] = Float.parseFloat(splits[0]);
			matrix.m[2][1] = Float.parseFloat(splits[1]);
			matrix.m[2][2] = Float.parseFloat(splits[2]);
		}
		

		stream.writeInt(flags);
		stream.writeLEFloats(offset);
		stream.writeLEFloat(scale);
		for (int i = 0; i < 3; i++) stream.writeLEFloat(matrix.m[i][0]);
		for (int i = 0; i < 3; i++) stream.writeLEFloat(matrix.m[i][1]);
		for (int i = 0; i < 3; i++) stream.writeLEFloat(matrix.m[i][2]);
	}
}
