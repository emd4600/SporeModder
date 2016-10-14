package sporemodder.extras.spuiviewer;

import javax.swing.JPanel;

import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIFileResource;
import sporemodder.files.formats.spui.SPUIVectorSections.*;
import sporemodder.files.formats.spui.SPUINumberSections.*;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ImagePanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8630220839601882087L;

	private ImageBlock imageBlock;
	
	private ImageIcon imageIcon;
	private JLabel lblImage;
	private JLabel lblBlockName;

	/**
	 * Create the panel.
	 */
	private void init() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		lblImage = new JLabel();
		lblImage.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(lblImage);
		
		lblBlockName = new JLabel();
		lblBlockName.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(lblBlockName);
	}
	
	public ImagePanel(SPUIBlock block) throws ImageBlockException, IOException {
		init();

		setBlock(block);
	}
	
	public ImagePanel(ImageBlock block) throws ImageBlockException, IOException {
		init();

		setBlock(block);
	}


	public ImageBlock getBlock() {
		return imageBlock;
	}
	
	public void setBlock(SPUIBlock block) throws ImageBlockException, IOException {
		imageBlock = new ImageBlock(block);
		imageIcon = new ImageIcon(imageBlock.processImage());
		lblImage.setIcon(imageIcon);
		lblBlockName.setText("Block " + (SpuiViewer.ActiveSPUI.getResources().getValidResourcesCount() + SpuiViewer.ActiveSPUI.getBlocks().indexOf(block)));
	}
	
	public void setBlock(ImageBlock block) throws ImageBlockException, IOException {
		imageBlock = block;
		imageIcon = new ImageIcon(imageBlock.processImage());
		lblImage.setIcon(imageIcon);
		lblBlockName.setText("Block " + (SpuiViewer.ActiveSPUI.getResources().getValidResourcesCount() + SpuiViewer.ActiveSPUI.getBlocks().indexOf(block.getBlock())));
	}

	public void update() throws ImageBlockException, IOException {
		imageIcon = new ImageIcon(imageBlock.processImage());
		lblImage.setIcon(imageIcon);
	}
}
