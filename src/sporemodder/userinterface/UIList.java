package sporemodder.userinterface;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class UIList extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8622346686508283057L;
	
	public interface ElementOptions {
		public String[] getOptions(boolean isAdded);
	}
	
	//TODO Add presets system 
	private JPanel elementsPanel;
	private DefaultListModel<String> listModel;
	private JList<String> listElements;
	private JComboBox<String> comboBox;
	private JTextField textField;
	private String defaultText = "default";
	
	private JPanel buttonsPane;
	private JButton btnAdd;
	private JButton btnRemove;
	private JButton btnConfig;  // only if elementConfigAL is not null
	private JButton btnUp;
	private JButton btnDown;
	
	private ElementOptions elementOptions = null;
	private ActionListener elementConfigAL = null;
	
	public UIList() {
		init();
	}
	
	public UIList(ElementOptions optionsHandler, ActionListener elementConfigAL) {
		elementOptions = optionsHandler;
		this.elementConfigAL = elementConfigAL;
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		elementsPanel = new JPanel(new BorderLayout(0, 5));
		
		listModel = new DefaultListModel<String>();
		listElements = new JList<String>(listModel);
		listElements.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listElements.addListSelectionListener(new LSLElement());
		elementsPanel.add(listElements, BorderLayout.CENTER);
		
		// Don't use options, just use strings
		if (elementOptions == null) {
			textField = new JTextField();
			textField.setEnabled(false);
			textField.getDocument().addDocumentListener(new DLTextField());
			elementsPanel.add(textField, BorderLayout.SOUTH);
		} else {
			comboBox = new JComboBox<String>();
			comboBox.setEnabled(false);
			comboBox.addActionListener(new ALComboBox());
			elementsPanel.add(comboBox, BorderLayout.SOUTH);
		}
		
		buttonsPane = new JPanel();
		buttonsPane.setLayout(new GridLayout(5, 1));
		
		add(elementsPanel, BorderLayout.CENTER);
		add(buttonsPane, BorderLayout.EAST);
		
		btnAdd = new JButton("+");
		btnAdd.addActionListener(new ALAdd());
		btnRemove = new JButton("-");
		btnRemove.addActionListener(new ALRemove());
		btnRemove.setEnabled(false);
		btnUp = new JButton("\u0245");
		btnUp.addActionListener(new ALMoveUp());
		btnUp.setEnabled(false);
		btnDown = new JButton("V");
		btnDown.addActionListener(new ALMoveDown());
		btnDown.setEnabled(false);
		
		buttonsPane.add(btnAdd);
		buttonsPane.add(btnRemove);
		buttonsPane.add(btnUp);
		buttonsPane.add(btnDown);
		
		if (elementConfigAL != null) {
			ImageIcon iconConfig = createImageIcon("/sporemodder/userinterface/images/config_20.png", "Save");
			btnConfig = new JButton(iconConfig);
			//btnConfig.setPreferredSize(new Dimension(18, 18));
			btnConfig.addActionListener(elementConfigAL);
			btnConfig.setEnabled(false);
			buttonsPane.add(btnConfig);
		}
	}
	
	private void updateButtons() {
		btnRemove.setEnabled(listModel.size() > 0 && !listElements.isSelectionEmpty());
		if (listModel.size() > 1) {
			int index = listElements.getSelectedIndex();
			btnUp.setEnabled(index > 0);
			btnDown.setEnabled(index < listModel.size() - 1);
		} else {
			btnUp.setEnabled(false);
			btnDown.setEnabled(false);
		}
		if (btnConfig != null) btnConfig.setEnabled(listModel.size() > 0 && !listElements.isSelectionEmpty());
	}
	
	public List<String> getElements() {
		int size = listModel.getSize();
		List<String> list = new ArrayList<String>(size);
		for (int i = 0; i < size; i++) {
			list.add(listModel.get(i));
		}
		return list;
	}
	
	public String getSelectedElement() {
		return listElements.getSelectedValue();
	}
	
	public void addElement(String str) {
		listModel.addElement(str);
		updateButtons();
	}
	
	public void setSelectedElement(String str) {
		listElements.setSelectedValue(str, true);
		updateButtons();
	}
	
	
	public String getDefaultText() {
		return defaultText;
	}

	public void setDefaultText(String defaultText) {
		this.defaultText = defaultText;
	}



	private class LSLElement implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			if (elementOptions == null) {
				textField.setEnabled(true);
				textField.setText(listElements.getSelectedValue());
			}
			else {
				comboBox.setEnabled(true);
				comboBox.setModel(new DefaultComboBoxModel<String>(elementOptions.getOptions(false)));
				comboBox.setSelectedItem(listElements.getSelectedValue());
			}
			updateButtons();
		}
	}
	
	private class DLTextField implements DocumentListener {
		private void action() {
			if (listElements.getSelectedIndex() != -1) {
				listModel.set(listElements.getSelectedIndex(), textField.getText());
			}
		}
		@Override
		public void changedUpdate(DocumentEvent arg0) {
			action();
		}

		@Override
		public void insertUpdate(DocumentEvent arg0) {
			action();
		}

		@Override
		public void removeUpdate(DocumentEvent arg0) {
			action();
		}
	}
	
	private class ALComboBox implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (listElements.getSelectedIndex() != -1) {
				listModel.set(listElements.getSelectedIndex(), (String)comboBox.getSelectedItem());
			}
		}
	}
	
	private class ALAdd implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			int index = listElements.getSelectedIndex();
			boolean insert = index < listModel.size() && index != -1;
			
			if (elementOptions == null) {
				if (insert) listModel.add(index+1, defaultText);
				else listModel.addElement(defaultText);
			}
			else {
				String[] options = elementOptions.getOptions(true);
				if (options.length > 0) {
					if (insert) listModel.add(index+1, options[0]);
					else listModel.addElement(options[0]);
				} else {
					return;
				}
			}
			
			if (insert) listElements.setSelectedIndex(index + 1);
			else listElements.setSelectedIndex(listModel.getSize()-1);
			
			updateButtons();
		}
	}
	
	private class ALRemove implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			int ind = listElements.getSelectedIndex();
			listModel.remove(ind);
			int index = ind - 1;
			
			if (elementOptions == null) {
				if (index < 0) textField.setEnabled(false);
				textField.setText("");
			} else {
				if (index < 0) comboBox.setEnabled(false);
				else comboBox.setSelectedItem(listModel.get(index));
			}
			if (index == -1) index = 0;
			listElements.setSelectedIndex(index);
			updateButtons();
		}
	}
	
	private class ALMoveUp implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			int index = listElements.getSelectedIndex();
			String currentValue = listElements.getSelectedValue();
			String previousValue = listModel.get(index - 1);
			
			listModel.set(index, previousValue);
			listModel.set(index - 1, currentValue);
			
			listElements.setSelectedIndex(index - 1);
			
			updateButtons();
		}
	}
	
	private class ALMoveDown implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			int index = listElements.getSelectedIndex();
			String currentValue = listElements.getSelectedValue();
			String afterwardsValue = listModel.get(index + 1);
			
			listModel.set(index, afterwardsValue);
			listModel.set(index + 1, currentValue);
			
			listElements.setSelectedIndex(index + 1);
			
			updateButtons();
		}
	}
	
	/** Returns an ImageIcon, or null if the path was invalid. */
	protected ImageIcon createImageIcon(String path,
	                                           String description) {
	    java.net.URL imgURL = getClass().getResource(path);
	    if (imgURL != null) {
	        return new ImageIcon(imgURL, description);
	    } else {
	        System.err.println("Couldn't find file: " + path);
	        return null;
	    }
	}
	
}
