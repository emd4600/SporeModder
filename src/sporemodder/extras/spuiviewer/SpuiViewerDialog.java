package sporemodder.extras.spuiviewer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Image;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import sporemodder.MainApp;
import sporemodder.files.formats.argscript.ArgScript;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.userinterface.dialogs.AdvancedFileChooser;
import sporemodder.userinterface.dialogs.AdvancedFileChooser.ChooserType;
import sporemodder.utilities.Project;

public class SpuiViewerDialog extends JDialog {

	// use this class instead of SpuiViewer if you are calling this from a program
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5441936990903738979L;
	
	private JPanel contentPane;
	private JList<ImagePanel> imageList;
	private DefaultListModel<ImagePanel> imageListModel;
	private JLabel lblUVCoords;
	private JLabel lblAtlasName;
	private JLabel lblAtlasIcon;
	private JPanel infoPanel;
	private JLabel lblDimensions;
	private JMenuItem mntmExportAsSpuit;
	private JMenuItem mntmSave;
	private JMenuItem mntmExportAsSpui;
	private JMenu mnEdit;
	private JMenuItem mntmCreateImage;
	private JMenuItem mntmEditImage;

	public SpuiViewerDialog(Window parent, File file) {
		super(parent);
		
		SpuiViewer.ActiveFrame = this;
		setModalityType(ModalityType.TOOLKIT_MODAL);
		setTitle(file == null ? "SPUI Image Editor" : "SPUI Image Editor - " + file.getName());
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 661, 396);
		setLocationRelativeTo(null);
		setDropTarget(new DropTarget() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -3122992994581700014L;
			
			@Override
			public synchronized void drop(DropTargetDropEvent event) {
				event.acceptDrop(DnDConstants.ACTION_COPY);
				try {
					
					@SuppressWarnings("unchecked")
					List<File> droppedFiles = (List<File>) event.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
					File file = droppedFiles.get(0);
					String fileName = file.getName();
					
					if (fileName.endsWith(".spui")) {
						
						setActiveFile(file, false);
						
					} else if (fileName.endsWith(".spui_t")) {
						
						setActiveFile(file, true);
						
					} else {
						event.rejectDrop();
					}
					
				} catch (UnsupportedFlavorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException | ArgScriptException e) {
					JOptionPane.showMessageDialog(SpuiViewer.ActiveFrame, Messages.getString("SpuiViewer.loadSpuiError") + "\n" + e.toString(), 
							"Error", JOptionPane.ERROR_MESSAGE);
				}
				
			}
		});
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu(Messages.getString("SpuiViewer.mnFile.text")); //$NON-NLS-1$
		mnFile.setMnemonic(KeyEvent.VK_F);
		menuBar.add(mnFile);
		
		mntmSave = new JMenuItem(Messages.getString("SpuiViewer.mntmSave.text")); //$NON-NLS-1$
		mntmSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		mntmSave.setMnemonic(KeyEvent.VK_S);
		mntmSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveAction();
			}
		});
		mntmSave.setEnabled(false);
		mnFile.add(mntmSave);
		
		mntmExportAsSpuit = new JMenuItem(Messages.getString("SpuiViewer.mntmExportAsSpuit.text")); //$NON-NLS-1$
		mntmExportAsSpuit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exportAction(true);
			}
		});
		mntmExportAsSpuit.setEnabled(false);
		mnFile.add(mntmExportAsSpuit);
		
		mntmExportAsSpui = new JMenuItem(Messages.getString("SpuiViewer.mntmExportAsSpui.text")); //$NON-NLS-1$
		mntmExportAsSpui.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exportAction(false);
			}
		});
		mntmExportAsSpui.setEnabled(false);
		mnFile.add(mntmExportAsSpui);
		
		mnEdit = new JMenu(Messages.getString("SpuiViewer.mnEdit.text")); //$NON-NLS-1$
		mnEdit.setMnemonic(KeyEvent.VK_E);
		menuBar.add(mnEdit);
		
//		mntmCreateImage = new JMenuItem(Messages.getString("SpuiViewer.mntmCreateImage.text")); //$NON-NLS-1$
//		mntmCreateImage.addActionListener(new ActionListener() {
//
//			@Override
//			public void actionPerformed(ActionEvent arg0) {
//				//new ImageEditDialog(SpuiViewerDialog.this, null);
//				ImageBlock newBlock = ImageEditDialog.createImageEditDialog(SpuiViewerDialog.this);
//				if (newBlock != null) {
//					try {
//						imageListModel.addElement(new ImagePanel(newBlock));
//						imageList.setSelectedIndex(imageListModel.getSize() - 1);
//					} catch (ImageBlockException | IOException e) {
//						JOptionPane.showMessageDialog(SpuiViewer.ActiveFrame, Messages.getString("ImagePanel.processImage.errorLoading"), "Error", JOptionPane.ERROR_MESSAGE);
//						e.printStackTrace();
//					}
//				}
//			}
//			
//		});
//		mnEdit.add(mntmCreateImage);
		
		mntmEditImage = new JMenuItem(Messages.getString("SpuiViewer.mntmEditImage.text")); //$NON-NLS-1$
		mntmEditImage.setEnabled(false);
		mntmEditImage.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				ImagePanel panel = imageList.getSelectedValue();
				if (panel != null) {
					new ImageEditDialog(SpuiViewerDialog.this, panel.getBlock());
					try {
						panel.update();
						imageList.setFixedCellHeight(0);
						imageList.setFixedCellWidth(0);
						imageList.setFixedCellHeight(-1);
						imageList.setFixedCellWidth(-1);
						imageList.revalidate();
						imageList.repaint();
						//imageList.revalidate();
					} catch (ImageBlockException | IOException e) {
						JOptionPane.showMessageDialog(SpuiViewer.ActiveFrame, "Error", Messages.getString("ImagePanel.processImage.errorLoading") + "\n" + e.toString(), JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			
		});
		mnEdit.add(mntmEditImage);
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JScrollPane scrollPane = new JScrollPane();
		contentPane.add(scrollPane, BorderLayout.CENTER);
		
		imageList = new JList<ImagePanel>();
		imageListModel = new DefaultListModel<ImagePanel>();
		imageList.setModel(imageListModel);
		imageList.setCellRenderer(new ImageListCellRenderer(imageList));
		imageList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				updateInfoPanel();
				mntmEditImage.setEnabled(!imageList.isSelectionEmpty());
			}
			
		});
		scrollPane.setViewportView(imageList);
		
		infoPanel = new JPanel();
		infoPanel.setVisible(false);
		infoPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		contentPane.add(infoPanel, BorderLayout.EAST);
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
		
		Component verticalGlue = Box.createVerticalGlue();
		infoPanel.add(verticalGlue);
		
		lblAtlasIcon = new JLabel(""); //$NON-NLS-1$
		lblAtlasIcon.setAlignmentX(Component.LEFT_ALIGNMENT);
		infoPanel.add(lblAtlasIcon);
		
		lblAtlasName = new JLabel(""); //$NON-NLS-1$
		lblAtlasName.setAlignmentX(Component.LEFT_ALIGNMENT);
		infoPanel.add(lblAtlasName);
		
		lblUVCoords = new JLabel(""); //$NON-NLS-1$
		lblUVCoords.setAlignmentX(Component.LEFT_ALIGNMENT);
		FontMetrics metrics = lblUVCoords.getFontMetrics(lblUVCoords.getFont());
		lblUVCoords.setPreferredSize(new Dimension(metrics.stringWidth(
				lblUVCoords.getText() + "(" + SpuiViewer.FORMAT_UV + ", " + SpuiViewer.FORMAT_UV + ", " + SpuiViewer.FORMAT_UV + ", " + SpuiViewer.FORMAT_UV + ")"), metrics.getHeight()));
		infoPanel.add(lblUVCoords);
		
		lblDimensions = new JLabel(""); //$NON-NLS-1$
		lblDimensions.setAlignmentX(Component.LEFT_ALIGNMENT);
		infoPanel.add(lblDimensions);
		
		Component verticalGlue_1 = Box.createVerticalGlue();
		infoPanel.add(verticalGlue_1);
		
		if (file != null) {
			try {
				setActiveFile(file, file.getName().endsWith(".spui_t"));
			} catch (IOException | ArgScriptException e) {
				JOptionPane.showMessageDialog(SpuiViewer.ActiveFrame, Messages.getString("SpuiViewer.loadSpuiError") + "\n" + e.toString(), 
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		
		setVisible(true);
	}

	
	// Sets the active file, reads it and updates the title and the images
	private void setActiveFile(File file, boolean isTextSPUI) throws IOException, ArgScriptException {
		SpuiViewer.IsTextSPUI = isTextSPUI;
		SpuiViewer.ActiveFile = file;
		SpuiViewer.ActiveSPUI = isTextSPUI ? ResourceLoader.loadSpuiText(file) : ResourceLoader.loadSpui(file);
		updateImages();
		setTitle("SPUI Image Editor - " + file.getName());
		mntmExportAsSpuit.setEnabled(true);
		mntmExportAsSpui.setEnabled(true);
		mntmSave.setEnabled(true);
		repaint();
	}

	private void updateImages() {
		if (SpuiViewer.ActiveSPUI != null) {
			imageListModel.clear();
			List<SPUIBlock> blocks = SpuiViewer.ActiveSPUI.getBlocks();
			int index = 0;
			
			Exception exception = null;
			List<Integer> failedBlocks = new ArrayList<Integer>();
			
			for (SPUIBlock block : blocks) {
				if (block.getResource().getHash() == 0x01BE6B15) {
					try {
						ImagePanel panel = new ImagePanel(block);
						imageListModel.addElement(panel);
					} catch (ImageBlockException | IOException e) {
						failedBlocks.add(index);
						exception = e;
					}
				}
				index++;
			}
			
			if (exception != null) {
				JOptionPane.showMessageDialog(SpuiViewer.ActiveFrame, "The following Image blocks couldn't be parsed:  " + failedBlocks.toString() + "\n" + exception.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
			}
			
//				imageList.revalidate();
//				imageList.repaint();
		}
	}
	
	private void openAction() {
		AdvancedFileChooser chooser = new AdvancedFileChooser(null, this, JFileChooser.FILES_ONLY, false, ChooserType.OPEN, 
				SpuiViewer.FILEFILTER_SPUI, SpuiViewer.FILEFILTER_SPUI_T);
		
		String path = chooser.launch();
		if (path != null) {
			try {
				setActiveFile(new File(path), path.endsWith(".spui_t"));
			} catch (IOException | ArgScriptException e) {
				JOptionPane.showMessageDialog(SpuiViewer.ActiveFrame, Messages.getString("SpuiViewer.loadSpuiError") + "\n" + e.toString(), 
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	// Saves to the active file
	private void saveAction() {

		if (SpuiViewer.ActiveFile.exists()) {
			int result = JOptionPane.showConfirmDialog(SpuiViewer.ActiveFrame, Messages.getString("SpuiViewer.saveOverwrite"), "Overwrite file?", JOptionPane.OK_CANCEL_OPTION);
			if (result == JOptionPane.OK_OPTION) {
				try {
					if (SpuiViewer.IsTextSPUI) {
						ResourceLoader.saveSpuiText(SpuiViewer.ActiveSPUI, SpuiViewer.ActiveFile);
					} else {
						ResourceLoader.saveSpui(SpuiViewer.ActiveSPUI, SpuiViewer.ActiveFile);
					}
				} catch (IOException e) {
					JOptionPane.showMessageDialog(SpuiViewer.ActiveFrame, Messages.getString("SpuiViewer.writeSpuiError") + "\n" + e.toString(), 
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}
	
	private void exportAction(boolean isTextSpui) {
		AdvancedFileChooser chooser = new AdvancedFileChooser(null, this, JFileChooser.FILES_ONLY, false, ChooserType.SAVE,
				SpuiViewer.FILEFILTER_SPUI, SpuiViewer.FILEFILTER_SPUI_T);
		
		String path = chooser.launch();
		if (path != null) {
			try {
				if (isTextSpui) {
					ResourceLoader.saveSpuiText(SpuiViewer.ActiveSPUI, new File(path));
				} else {
					ResourceLoader.saveSpui(SpuiViewer.ActiveSPUI, new File(path));
				}
			} catch (IOException e) {
				JOptionPane.showMessageDialog(SpuiViewer.ActiveFrame, Messages.getString("SpuiViewer.writeSpuiError") + "\n" + e.toString(), 
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	private void updateInfoPanel() {
		int activeIndex = imageList.getAnchorSelectionIndex();
		if (activeIndex != -1) {
			
			ImagePanel imagePanel = imageListModel.getElementAt(activeIndex);
			ImageBlock imageBlock = imagePanel.getBlock();
			try {
				//TODO correct size
				lblAtlasIcon.setIcon(new ImageIcon(
						scaleImage(ResourceLoader.loadImage(imageBlock.getFileResource(), SpuiViewer.ActiveFile.getParentFile()), 128)));
			} catch (IOException e) {
				JOptionPane.showMessageDialog(SpuiViewer.ActiveFrame, Messages.getString("ImagePanel.processImage.errorLoading"), "Error", JOptionPane.ERROR_MESSAGE);
				infoPanel.setVisible(false);
				return;
			}
			
			lblAtlasName.setText(imageBlock.getFileResource().getStringSimple());
			
			Dimension dimensions = imageBlock.getDimensions();
			if (dimensions != null) {
				lblDimensions.setText(Messages.getString("SpuiViewer.lblDimensions.text") + 
						ArgScript.createList(Integer.toString(dimensions.width), Integer.toString(dimensions.height)));
			} else {
				lblDimensions.setText(Messages.getString("SpuiViewer.lblDimensions.text") + "Automatic");
			}
			
			float[] uvs = imageBlock.getUVCoordinates();
			if (uvs != null) {
				lblUVCoords.setText("UVs: " + createFloatList(uvs, SpuiViewer.FORMAT_UV));
			} else {
				lblUVCoords.setText("");
			}
			
			infoPanel.setVisible(true);
			
		} else {
			infoPanel.setVisible(false);
		}
	}
	
	private static String createFloatList(float[] floats, String format) {
		if (floats == null) {
			return "null";
		}
		
		int iMax = floats.length -1;
		if (iMax == -1) {
			return "()";
		}
		
		DecimalFormatSymbols dms = new DecimalFormatSymbols(Locale.US);
		dms.setDecimalSeparator('.');
		dms.setGroupingSeparator(',');
		NumberFormat nf = new DecimalFormat(format, dms);
		
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for (int i = 0; ; i++) {
			sb.append(nf.format(floats[i]));
			if (i == iMax) {
				return sb.append(")").toString();
			}
			sb.append(", ");
		}
	}
	
	public static Image scaleImage(BufferedImage originalImage, int max) {
		int width = originalImage.getWidth();
		int height = originalImage.getHeight();
		
		if (width <= max && height <= max) {
			return originalImage;
		}
		
		int bigger = width > height ? width : height;
		// cast so it doesn't do integer division
		float ratio = max / (float) bigger;
		
		return originalImage.getScaledInstance((int) (width * ratio), (int) (height * ratio), BufferedImage.SCALE_SMOOTH);
	}
}
