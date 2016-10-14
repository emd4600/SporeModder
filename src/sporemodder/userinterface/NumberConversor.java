package sporemodder.userinterface;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Component;

public class NumberConversor extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2844072415048991157L;
	
	private static final String[] DATA_TYPES_STR = {"int8", "uint8", "int16", "uint16", "int32", "uint32", "int64", "float", "double"};

	private JLabel lblHexadecimal;
	private JTextField tfHexadecimal;
	private JLabel lblDecimal;
	private JTextField tfDecimal;
	private JComboBox<String> cbDataType;
	
	public NumberConversor() {
		super();
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createTitledBorder("Number conversion"));
		
		lblHexadecimal = new JLabel("Hexadecimal:");
		lblHexadecimal.setAlignmentX(Component.CENTER_ALIGNMENT);
		tfHexadecimal = new JTextField();
		tfHexadecimal.addKeyListener(new KLNumberConversor(true));
		tfHexadecimal.setColumns(16);
		lblDecimal = new JLabel("Decimal:");
		lblDecimal.setAlignmentX(Component.CENTER_ALIGNMENT);
		tfDecimal = new JTextField();
		tfDecimal.addKeyListener(new KLNumberConversor(false));
		tfDecimal.setColumns(16);
		
		cbDataType = new JComboBox<String>(DATA_TYPES_STR);
		//cbDataType = new JComboBox();
		cbDataType.setSelectedIndex(4);
		cbDataType.addItemListener(new ILNumberTypes());

		add(Box.createRigidArea(new Dimension(0, 6)));
		add(lblHexadecimal);
		add(tfHexadecimal);
		add(Box.createRigidArea(new Dimension(0, 10)));
		add(lblDecimal);
		add(tfDecimal);
		add(Box.createRigidArea(new Dimension(0, 10)));
		add(cbDataType);
	}
	
	private static String convertToHex(String input, String mode) {
		if (mode.equals("int8")) {
			return Integer.toHexString(((int)Byte.parseByte(input)) & 0xFF);
		}
		else if (mode.equals("uint8")) {
			return Integer.toHexString(Byte.toUnsignedInt(Byte.parseByte(input)));
		}
		else if (mode.equals("int16")) {
			return Integer.toHexString(((int)Short.parseShort(input)) & 0xFFFF);
		}
		else if (mode.equals("uint16")) {
			return Integer.toHexString(Short.toUnsignedInt(Short.parseShort(input)));
		}
		else if (mode.equals("int32")) {
			return Integer.toHexString(Integer.parseInt(input));
		}
		else if (mode.equals("uint32")) {
			return Long.toHexString(Long.parseLong(input));
		}
		else if (mode.equals("int64")) {
			return Long.toHexString(Long.parseLong(input));
		}
		else if (mode.equals("float")) {
			return Integer.toHexString(Float.floatToRawIntBits(Float.parseFloat(input)));
		}
		else if (mode.equals("double")) {
			return Long.toHexString(Double.doubleToRawLongBits(Double.parseDouble(input)));
		}
		
		return null;
	}
	
	private static String convertToDecimal(String input, String mode) {
		if (mode.equals("int8")) {
			return Byte.toString(Byte.parseByte(input, 16));
		}
		else if (mode.equals("uint8")) {
			short num = Short.parseShort(input, 16);
			if (num < 0 || num > 255) return null;
			return Short.toString(num);
		}
		else if (mode.equals("int16")) {
			short num = Short.parseShort(input, 16);
			return Short.toString(num);
		}
		else if (mode.equals("uint16")) {
			int num = Integer.parseInt(input, 16);
			if (num < 0 || num > 65535) return null;
			return Integer.toString(num);
		}
		else if (mode.equals("int32")) {
			return Integer.toString(Integer.parseUnsignedInt(input, 16));
		}
		else if (mode.equals("uint32")) {
			long num = Long.parseLong(input, 16);
			if (num < 0 || num > 4294967295L) return null;
			return Long.toString(num);
		}
		else if (mode.equals("int64")) {
			return Long.toString(Long.parseLong(input, 16));
		}
		else if (mode.equals("float")) {
			return Float.toString(Float.intBitsToFloat(Integer.parseUnsignedInt(input, 16)));
		}
		else if (mode.equals("double")) {
			return Double.toString(Double.longBitsToDouble(Long.parseUnsignedLong(input, 16)));
		}
		
		return null;
	}
	
	private class KLNumberConversor implements KeyListener {
		private boolean isHex;
		
		public KLNumberConversor(boolean isHex) {
			this.isHex = isHex;
		}

		@Override
		public void keyPressed(KeyEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
			if (isHex) {
				if (tfHexadecimal.getText().length() > 0) {
					try {
						String num = convertToDecimal(tfHexadecimal.getText(), (String)cbDataType.getSelectedItem());
						if (num == null) {
							tfHexadecimal.setBackground(Color.RED);
							return;
						}
						tfDecimal.setText(num);
						tfHexadecimal.setBackground(Color.WHITE);
					} catch (NumberFormatException e) {
						tfHexadecimal.setBackground(Color.RED);
					}
				}
				else {
					tfDecimal.setText("");
					tfHexadecimal.setBackground(Color.WHITE);
				}
			} else {
				if (tfDecimal.getText().length() > 0) {
					try {
						String num = convertToHex(tfDecimal.getText(), (String)cbDataType.getSelectedItem());
						if (num == null) {
							tfDecimal.setBackground(Color.RED);
							return;
						}
						tfHexadecimal.setText(num);
						tfDecimal.setBackground(Color.WHITE);
					} catch (NumberFormatException e) {
						tfDecimal.setBackground(Color.RED);
					}
				}
				else {
					tfHexadecimal.setText("");
					tfDecimal.setBackground(Color.WHITE);
				}
			}
		}

		@Override
		public void keyTyped(KeyEvent e) {
			char c = e.getKeyChar();
			
			if (isHex) {
				if (!Character.isDigit(c) && c != 'a' && c != 'A' && c != 'b' && c != 'B' && c != 'c' && c != 'C' && c != 'd'
						&& c != 'D' && c != 'e' && c != 'E' && c != 'f' && c != 'F') {
					e.consume();
				}
			} else {
				if (!Character.isDigit(c)) {
					String currentData = (String)cbDataType.getSelectedItem();
					if (currentData.equals("float") || currentData.equals("double")) {
						if (c != '.' && c != '-') e.consume();
					}
					else if (currentData.equals("int8") || currentData.equals("int16") || currentData.equals("int32") || currentData.equals("int64")) {
						if (c != '-') e.consume();
					}
				}
			}
		}
		
	}
	
	private class ILNumberTypes implements ItemListener {

		@Override
		public void itemStateChanged(ItemEvent arg0) {
			tfHexadecimal.setText("");
			tfDecimal.setText("");
		}
		
	}
}
