package sporemodder.userinterface;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import sporemodder.MainApp;
import sporemodder.files.FileStreamAccessor;
import sporemodder.files.FileStructureException;
import sporemodder.files.formats.dds.DDSImageReader;
import sporemodder.files.formats.dds.DDSImageReaderSpi;
import sporemodder.files.formats.dds.DDSTexture;
import sporemodder.files.formats.renderWare4.RW4Main;
import sporemodder.files.formats.renderWare4.RW4TexMetadata;
import sporemodder.files.formats.renderWare4.RW4Texture;
import sporemodder.userinterface.dialogs.AdvancedFileChooser;
import sporemodder.userinterface.dialogs.AdvancedFileChooser.ChooserType;

public class UITexturePatcher extends JDialog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2340850491756490634L;
	
	private static final FileNameExtensionFilter RW4_FILTER = new FileNameExtensionFilter("Spore RenderWare4 (*.rw4)", "rw4");
	private static final FileNameExtensionFilter DDS_FILTER = new FileNameExtensionFilter("DirectDraw Surface (*.dds)", "dds");
	private static final FileNameExtensionFilter PNG_FILTER = new FileNameExtensionFilter("Portable Network Graphics (*.png)", "png");
	private static final int ICON_SIZE = 64;
	private static final int PREVIEW_SIZE = 256;
	
	private List<RW4Texture> textures;
	private RW4Main rw4;
	private String originalPath;
	
	private List<DDSTexture> ddsTextures = new ArrayList<DDSTexture>();
	private List<BufferedImage> images = new ArrayList<BufferedImage>();
	private List<JLabel> imageLabels = new ArrayList<JLabel>();
	private List<String> textureTypes = new ArrayList<String>();
	
	private JList<JLabel> imagesList;
	
	private JPanel previewPanel;
	private JLabel lblImage;
	
	private JPanel infoPanel;
	private JLabel lblDimensions;
	private JLabel lblTextureType;
	
	private JButton btnExport;
	private JButton btnExportPNG;
	private JButton btnReplace;
	private JButton btnReset;
	
	private JPanel buttonsPanel;
	private JButton btnSave;
	private JButton btnCancel;
	
	public UITexturePatcher(RW4Main rw4Main, String path) {
		
		super(MainApp.getUserInterface());
		setModalityType(ModalityType.TOOLKIT_MODAL);
		setTitle("Texture Patcher");
		setBounds(100, 100, 600, 191);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);
		
		getContentPane().setLayout(new BorderLayout());
		
		rw4 = rw4Main;
		originalPath = path;
		textures = rw4.<RW4Texture>getSections(RW4Texture.class);
		
		if (textures.size() == 0) {
			JOptionPane.showMessageDialog(MainApp.getUserInterface(), "Error reading RW4 file: \nInput file has no textures.", "Error", JOptionPane.ERROR_MESSAGE);
			dispose();
		}
		
		for (RW4Texture texture : textures) {
			DDSTexture tex = texture.toDDSTexture();
			try {
				DDSImageReader reader = new DDSImageReader(new DDSImageReaderSpi());
				BufferedImage image = DDSTexture.toBufferedImage(tex, reader);
				
				ImageIcon icon = scaleImage(image, ICON_SIZE);
				JLabel label = new JLabel(icon);
				
				images.add(image);
				imageLabels.add(label);
				textureTypes.add(reader.ddsHeader.getFormat().toString());
				ddsTextures.add(tex);
				
			} catch (IOException e) {
				JOptionPane.showMessageDialog(MainApp.getUserInterface(), "Error reading texture: \n" + e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
				
			}
		}
		
//		panelImages = new JPanel();
//		scrollPane = new JScrollPane(panelImages);
		
		JLabel[] labels = new JLabel[imageLabels.size()];
		imageLabels.toArray(labels);
		imagesList = new JList<JLabel>(labels);
		imagesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		imagesList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		imagesList.setVisibleRowCount(1);
		imagesList.setCellRenderer(new TextureIconRenderer());
		imagesList.setFixedCellWidth(76);
		imagesList.setFixedCellHeight(76);
		imagesList.addListSelectionListener(new LSLImage());
		
		//imagesListModel = new DefaultListModel<JLabel>();
		
//		for (JLabel label : imageLabels) {
//			panelImages.add(label);
//		}
		
		previewPanel = new JPanel();
		previewPanel.setLayout(new BorderLayout());
		
		lblImage = new JLabel();
		lblImage.setPreferredSize(new Dimension(PREVIEW_SIZE, PREVIEW_SIZE));
		
		infoPanel = new JPanel();
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
		
		lblDimensions = new JLabel("Dimensions: ");
		lblTextureType = new JLabel("Texture type:");
		
		btnExport = new JButton("Export");
		btnExport.setEnabled(false);
		btnExport.addActionListener(new ALExport());
		btnExportPNG = new JButton("Export as PNG");
		btnExportPNG.addActionListener(new ALExportPNG());
		btnExportPNG.setEnabled(false);
		btnReplace = new JButton("Replace");
		btnReplace.addActionListener(new ALReplace());
		btnReplace.setEnabled(false);
		btnReset = new JButton("Reset");
		btnReset.addActionListener(new ALReset());
		btnReset.setEnabled(false);
		
		infoPanel.add(lblDimensions);
		infoPanel.add(lblTextureType);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		JPanel subpanel = new JPanel();
		
		subpanel.add(btnExport);
		subpanel.add(btnExportPNG);
		subpanel.add(btnReplace);
		subpanel.add(btnReset);
		
		panel.add(infoPanel, BorderLayout.NORTH);
		panel.add(subpanel, BorderLayout.SOUTH);
		
		previewPanel.add(lblImage, BorderLayout.CENTER);
		previewPanel.add(panel, BorderLayout.SOUTH);
		
		
		buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		btnSave = new JButton("Save");
		btnSave.addActionListener(new ALSave());
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ALCancel());
		
		buttonsPanel.add(btnSave);
		buttonsPanel.add(btnCancel);
		
		
		getContentPane().add(imagesList, BorderLayout.NORTH);
		getContentPane().add(previewPanel, BorderLayout.CENTER);
		getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
		
		pack();
		setVisible(true);
	}
	
	private static ImageIcon scaleImage(BufferedImage image, int maxSize) {
		int width = image.getWidth();
		int height = image.getHeight();
		float ratio = 0;
		
		if (width > height) {
			ratio = (float) maxSize / width;
		}
		else {
			ratio = (float) maxSize / height;
		}
		
		return new ImageIcon(image.getScaledInstance((int)(width * ratio), (int)(height * ratio), Image.SCALE_SMOOTH));
	}
	
	private static class TextureIconRenderer extends DefaultListCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8785657594143306730L;
		
		private static final Border SELECTED_BORDER = BorderFactory.createLineBorder(Color.red);
		private static final Border DEFAULT_BORDER = BorderFactory.createEmptyBorder();

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			JLabel label = (JLabel)value;
			if (isSelected) {
				label.setBorder(SELECTED_BORDER);
			}
			else {
				label.setBorder(DEFAULT_BORDER);
			}
			
			return label;
		}
	}
	
	private class LSLImage implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			BufferedImage image = images.get(imagesList.getSelectedIndex());
			ImageIcon icon = scaleImage(image, PREVIEW_SIZE);
			
			lblDimensions.setText("Dimensions: " + image.getWidth() + "x" + image.getHeight() + " px");
			lblTextureType.setText("Texture type: " + textureTypes.get(imagesList.getSelectedIndex()));
			
			lblImage.setIcon(icon);
			
			btnExport.setEnabled(true);
			btnExportPNG.setEnabled(true);
			btnReplace.setEnabled(true);
			btnReset.setEnabled(true);
		}
	}
	
	private class ALExport implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			AdvancedFileChooser fileChooser = new AdvancedFileChooser(null, UITexturePatcher.this, JFileChooser.FILES_ONLY, false,
					"untitled-" + textures.get(imagesList.getSelectedIndex()).sectionInfo.number + ".dds", ChooserType.SAVE, DDS_FILTER);
			String path = fileChooser.launch();
			
			if (path != null) {
				try (FileStreamAccessor out = new FileStreamAccessor(path, "rw", true)) {
					ddsTextures.get(imagesList.getSelectedIndex()).write(out);
					
				} catch (IOException e) {
					JOptionPane.showMessageDialog(UITexturePatcher.this, "Error writing DDS file: \n" + e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
				}			
			}
		}
	}
	
	private class ALExportPNG implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			AdvancedFileChooser fileChooser = new AdvancedFileChooser(null, UITexturePatcher.this, JFileChooser.FILES_ONLY, false, 
					"untitled-" + textures.get(imagesList.getSelectedIndex()).sectionInfo.number + ".png", ChooserType.SAVE, PNG_FILTER);
			String path = fileChooser.launch();
			
			if (path != null) {
				try {
					ImageIO.write(images.get(imagesList.getSelectedIndex()), "PNG", new File(path));
					
				} catch (IOException e) {
					JOptionPane.showMessageDialog(UITexturePatcher.this, "Error writing DDS file: \n" + e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}
	
	private class ALReplace implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			AdvancedFileChooser fileChooser = new AdvancedFileChooser(null, UITexturePatcher.this, JFileChooser.FILES_ONLY, false, ChooserType.OPEN, DDS_FILTER);
			String path = fileChooser.launch();
			
			if (path != null) {
				int index = imagesList.getSelectedIndex();
				
				try (FileStreamAccessor in = new FileStreamAccessor(path, "r")) {
					DDSTexture tex = new DDSTexture();
					tex.read(in);
					
					if (!tex.hasSameProperties(ddsTextures.get(index))) {
						JOptionPane.showMessageDialog(UITexturePatcher.this, "DDS texture must have the same dimensions and texture type as the original one.", 
								"Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					ddsTextures.set(index, tex);
					DDSImageReader reader = new DDSImageReader(new DDSImageReaderSpi());
					images.set(index, DDSTexture.toBufferedImage(tex, reader));
					
					imageLabels.get(index).setIcon(scaleImage(images.get(index), ICON_SIZE));
					
					lblImage.setIcon(scaleImage(images.get(index), PREVIEW_SIZE));
					
				} catch (IOException e) {
					JOptionPane.showMessageDialog(UITexturePatcher.this, "Error reading DDS file: \n" + e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}
	
	private class ALReset implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			int index = imagesList.getSelectedIndex();
			
			List<RW4Texture> sections = rw4.<RW4Texture>getSections(RW4Texture.class);
			textures.set(index, sections.get(index));
			
			try {
				DDSTexture tex = sections.get(index).toDDSTexture();
				ddsTextures.set(index, tex);
				DDSImageReader reader = new DDSImageReader(new DDSImageReaderSpi());
				images.set(index, DDSTexture.toBufferedImage(tex, reader));
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(UITexturePatcher.this, "Error reseting texture: \n" + e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			imageLabels.get(index).setIcon(scaleImage(images.get(index), ICON_SIZE));
			
			lblImage.setIcon(scaleImage(images.get(index), PREVIEW_SIZE));
		}
	}
	
	private class ALSave implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			AdvancedFileChooser fileChooser = new AdvancedFileChooser(null, UITexturePatcher.this, JFileChooser.FILES_ONLY, false,
					"untitled.rw4",ChooserType.SAVE, RW4_FILTER);
			String path = fileChooser.launch();
			
			if (path != null) {
				FileChannel inChannel = null;
				FileChannel outChannel = null;
				try (FileInputStream in = new FileInputStream(originalPath);
						FileStreamAccessor out = new FileStreamAccessor(path, "rw", true)) 
				{
					inChannel = in.getChannel();
					outChannel = out.getChannel();
					inChannel.transferTo(0, inChannel.size(), outChannel);
					
					for (int i = 0; i < textures.size(); i++) 
					{
						RW4Texture rw4Texture = textures.get(i);
						DDSTexture texture = ddsTextures.get(i);
						
						out.seek(rw4Texture.texData.sectionInfo.pos);
						out.write(texture.getData());
						
						out.seek(rw4Texture.sectionInfo.pos);
						rw4Texture.fromDDSTexture(texture);
						rw4Texture.write(out, null);
					}
					
					dispose();
				} 
				catch (IOException e) {
					JOptionPane.showMessageDialog(UITexturePatcher.this, "Error saving patched model: \n" + e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				finally {
					try {
						if (inChannel != null) inChannel.close();
						if (outChannel != null) outChannel.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private class ALCancel implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			dispose();
		}
	}
	
	public static UITexturePatcher findFile() {
		AdvancedFileChooser fileChooser = new AdvancedFileChooser(null, MainApp.getUserInterface(), JFileChooser.FILES_ONLY, false, ChooserType.OPEN, RW4_FILTER);
		String path = fileChooser.launch();
		
		if (path != null) {
			try {
				RW4Main rw4 = new RW4Main();
				try (FileStreamAccessor in = new FileStreamAccessor(path, "r")) {
					rw4.read(in, RW4TexMetadata.class);
				} catch (InstantiationException | IllegalAccessException e) {
					JOptionPane.showMessageDialog(MainApp.getUserInterface(), "Error reading RW4 file: \n" + e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				}
				
				return new UITexturePatcher(rw4, path);
				
			} catch (IOException e) {
				JOptionPane.showMessageDialog(MainApp.getUserInterface(), "Error reading RW4 file: \n" + e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
				
				return null;
			}
		}
		return null;
	}
}
