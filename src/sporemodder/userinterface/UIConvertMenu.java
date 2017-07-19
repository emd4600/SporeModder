package sporemodder.userinterface;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;

import sporemodder.MainApp;
import sporemodder.extras.spuieditor.SPUIEditor;
import sporemodder.files.FileStreamAccessor;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIMain;
import sporemodder.userinterface.dialogs.UIDialogEffects;
import sporemodder.userinterface.dialogs.UIDialogGait;
import sporemodder.userinterface.dialogs.UIDialogPctp;
import sporemodder.userinterface.dialogs.UIDialogProp;
import sporemodder.userinterface.dialogs.UIDialogRast;
import sporemodder.userinterface.dialogs.UIDialogRw4;
import sporemodder.userinterface.dialogs.UIDialogSpui;
import sporemodder.userinterface.dialogs.UIDialogTlsa;
import sporemodder.userinterface.dialogs.UIDialogUncompiledShaders;
import sporemodder.utilities.Hasher;
import sporemodder.utilities.names.SimpleNameRegistry;

public class UIConvertMenu extends JMenu {
	
	private JMenuItem mntmProp;
	private JMenuItem mntmRw4;
	private JMenuItem mntmTlsa;
	private JMenuItem mntmRast;
	private JMenuItem mntmEffects;
	private JMenuItem mntmSpui;
	private JMenuItem mntmPctp;
	private JMenuItem mntmGait;
	
	private JMenuItem mntmTexturePatcher;
	private JMenuItem mntmSpuiEditor;
	
	private JMenuItem mntmUncompiledShadersUnpacker;
	private JMenuItem mntmCompiledShadersViewer;
	private JMenuItem mntmMaterialsViewer;

	public UIConvertMenu(String name) {
		super(name);
		
		///////////////////////////////////////////////////
		mntmProp = new JMenuItem("Spore Property List (PROP)");
		mntmProp.setMnemonic(KeyEvent.VK_P);
		mntmProp.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				new UIDialogProp();
			}
			
		});
		add(mntmProp);
		
		///////////////////////////////////////////////////
		mntmRw4 = new JMenuItem("Spore RenderWare4 Texture (RW4)");
		mntmRw4.setMnemonic(KeyEvent.VK_R);
		mntmRw4.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new UIDialogRw4();
			}
		});
		add(mntmRw4);

		///////////////////////////////////////////////////
		mntmTlsa = new JMenuItem("Spore Animation List (TLSA)");
		mntmTlsa.setMnemonic(KeyEvent.VK_T);
		mntmTlsa.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new UIDialogTlsa();
			}
		});
		add(mntmTlsa);
		
		////////////////////////////////////////////////////
				
		mntmEffects = new JMenuItem("Spore Effects (EFFDIR/PFX)");
		mntmEffects.setMnemonic(KeyEvent.VK_E);
		mntmEffects.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new UIDialogEffects();
			}
		});
		
		add(mntmEffects);
		
		////////////////////////////////////////////////////
		
		mntmSpui = new JMenuItem("Spore User Interface (SPUI)");
		mntmSpui.setMnemonic(KeyEvent.VK_S);
		mntmSpui.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new UIDialogSpui();
			}
		});
		
		add(mntmSpui);
		
		////////////////////////////////////////////////////
				
		mntmPctp = new JMenuItem("Spore Capability List (PCTP)");
		mntmPctp.setMnemonic(KeyEvent.VK_C);
		mntmPctp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new UIDialogPctp();
			}
		});
		
		add(mntmPctp);
		
		////////////////////////////////////////////////////
		
		mntmGait = new JMenuItem("Spore Gait (GAIT)");
		mntmGait.setMnemonic(KeyEvent.VK_G);
		mntmGait.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new UIDialogGait();
			}
		});
		
		add(mntmGait);
		
		////////////////////////////////////////////////////
				
		mntmRast = new JMenuItem("Spore RAST Texture (RAST)");
		mntmRast.setMnemonic(KeyEvent.VK_A);
		mntmRast.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new UIDialogRast();
			}
		});
		
		add(mntmRast);
		
		
		add(new JSeparator());
		///////////////////////////////////////////////////
		
		mntmTexturePatcher = new JMenuItem("Texture patcher");
		mntmTexturePatcher.setMnemonic(KeyEvent.VK_X);
		mntmTexturePatcher.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UITexturePatcher.findFile();
			}
		});
		add(mntmTexturePatcher);
		
		mntmSpuiEditor = new JMenuItem("SPUI Editor");
		mntmSpuiEditor.setEnabled(false);
		//mntmSpuiImageEditor.setMnemonic(KeyEvent.VK_X);
		mntmSpuiEditor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				File file = MainApp.getActiveFile();
				boolean isTextSPUI = false;
				
				if (file == null) {
					return;
				}
				
				if (file.isDirectory()) {
					
					String name = JOptionPane.showInputDialog(MainApp.getUserInterface(), "Insert the new file name:", "untitled.spui");
					if (name == null) {
						return;
					}
					
					FileUtils.createNewFile(name, FileUtils.getParentNode());
					
					file = MainApp.getActiveFile();
				}

				SPUIMain spui = new SPUIMain();
				if (file.getName().endsWith(".spui_t")) {
					isTextSPUI = true;
					
					try (BufferedReader in = new BufferedReader(new FileReader(file))) {
						spui.parse(in);
					} catch (IOException | ArgScriptException ex) {
						ex.printStackTrace();
						JOptionPane.showMessageDialog(MainApp.getUserInterface(), "Error reading SPUI file:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
				} else {
					isTextSPUI = false;
					
					try (InputStreamAccessor in = new FileStreamAccessor(file, "r")) {
						spui.read(in);
					} catch (IOException ex) {
						ex.printStackTrace();
						JOptionPane.showMessageDialog(MainApp.getUserInterface(), "Error reading SPUI file:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				
				String filePath = MainApp.getActiveFilePath();
					
				try {
					if (Hasher.UsedNames == null) {
						System.out.println("Creating new registry");
						Hasher.UsedNames = new SimpleNameRegistry();
					}
					
					SPUIEditor frame = new SPUIEditor(spui, MainApp.getCurrentProject().getProjectName() + " - " + filePath, filePath, 
							file, isTextSPUI, MainApp.getActiveNode().isMod, new AbstractAction() {
						
						@Override
						public void actionPerformed(ActionEvent e) {
							MainApp.getCurrentProject().saveNames();
						}
					});
//					frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);  // are we sure?
					if (!frame.hadErrors()) {
						frame.setVisible(true);
					}
					
					// update file viewer
					MainApp.setActiveFile(MainApp.getActiveFilePath());
					
				} catch (InvalidBlockException | IOException ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(MainApp.getUserInterface(), "Error opening SPUI editor:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		add(mntmSpuiEditor);
		
		
		add(new JSeparator());
		///////////////////////////////////////////////////
		/// MATERIAL RELATED DIALGOS ///
		
		mntmUncompiledShadersUnpacker = new JMenuItem("Uncompiled Shaders Unpacker");
		mntmUncompiledShadersUnpacker.setMnemonic(KeyEvent.VK_X);
		mntmUncompiledShadersUnpacker.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new UIDialogUncompiledShaders();
			}
		});
		add(mntmUncompiledShadersUnpacker);
		
		mntmCompiledShadersViewer = new JMenuItem("Compiled Shaders Viewer");
		mntmCompiledShadersViewer.setMnemonic(KeyEvent.VK_X);
		mntmCompiledShadersViewer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UIShaderManager.findFile();
			}
		});
		add(mntmCompiledShadersViewer);
		
		mntmMaterialsViewer = new JMenuItem("Materials Viewer");
		mntmMaterialsViewer.setMnemonic(KeyEvent.VK_X);
		mntmMaterialsViewer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					UIMaterialManager.showChooserDialog();
				} catch (InstantiationException | IllegalAccessException | IOException e1) {
					JOptionPane.showMessageDialog(MainApp.getUserInterface(), "Error: the material viewer could not be opened.", "Error", JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}
			}
		});
		add(mntmMaterialsViewer);
	}

	public void update() {
		String activeFilePath = MainApp.getActiveFilePath();
		File activeFile = MainApp.getActiveFile();
		if (activeFile != null && (activeFile.isDirectory() || activeFilePath.endsWith(".spui") || activeFilePath.endsWith(".spui_t"))) {
			mntmSpuiEditor.setEnabled(true);
		}
		else {
			mntmSpuiEditor.setEnabled(false);
		}
	}
}
