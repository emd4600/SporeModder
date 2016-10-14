package sporemodder.userinterface.fileview;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JComponent;

import sporemodder.userinterface.JGradientButton;

/**
 * This panel contains buttons for hiding/showing each image channel and for changing the background color.
 *
 */
public class ImageOptionsPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4760310863764010570L;
	
	private UIImagePanel imagePanel;
	private JLabel lblChannels;
	private JCheckBox cbAlpha;
	private JCheckBox cbRed;
	private JCheckBox cbGreen;
	private JCheckBox cbBlue;
	
	private JCheckBox cbBackgroundColor;
	private JGradientButton btnBackgroundColor;
	
	public ImageOptionsPanel(UIImagePanel imagePanel) {
		super();
		
		this.imagePanel = imagePanel;
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		lblChannels = new JLabel("Channels:");
		lblChannels.setBorder(BorderFactory.createEmptyBorder(4, 5, 2, 5));
		cbAlpha = new JCheckBox("Alpha", true);
		cbAlpha.addItemListener(new ILChannel());
		cbRed = new JCheckBox("Red", true);
		cbRed.addItemListener(new ILChannel());
		cbGreen = new JCheckBox("Green", true);
		cbGreen.addItemListener(new ILChannel());
		cbBlue = new JCheckBox("Blue", true);
		cbBlue.addItemListener(new ILChannel());
		
		cbBackgroundColor = new JCheckBox("Background Color", false);
		cbBackgroundColor.addItemListener(new ILBackgroundColor());
		btnBackgroundColor = new JGradientButton();
		btnBackgroundColor.setMinimumSize(new Dimension(24, 24));
		btnBackgroundColor.setMaximumSize(new Dimension(24, 24));
		btnBackgroundColor.setEnabled(false);
		btnBackgroundColor.addActionListener(new ALBackgroundColor());
		
		add(lblChannels);
		add(cbAlpha);
		add(cbRed);
		add(cbGreen);
		add(cbBlue);
		add(Box.createVerticalStrut(10));
		add(cbBackgroundColor);
		add(btnBackgroundColor);
	}
	
	private class ILChannel implements ItemListener {

		@Override
		public void itemStateChanged(ItemEvent arg0) {
			imagePanel.setChannels(cbAlpha.isSelected(), cbRed.isSelected(), cbGreen.isSelected(), cbBlue.isSelected());
		}
		
	}
	
	private class ILBackgroundColor implements ItemListener {

		@Override
		public void itemStateChanged(ItemEvent arg0) {
			boolean value = cbBackgroundColor.isSelected();
			btnBackgroundColor.setEnabled(value);
			if (!value) {
				imagePanel.setBackgroundColor(null);
				imagePanel.repaint();
			}
		}
		
	}
	
	private class ALBackgroundColor implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			Color color = JColorChooser.showDialog(imagePanel, "Choose Background Color", imagePanel.getBackgroundColor());
			if (color != null) {
				btnBackgroundColor.setColor(color);
				imagePanel.setBackgroundColor(color);
				imagePanel.repaint();
			}
		}
		
	}
	
}
