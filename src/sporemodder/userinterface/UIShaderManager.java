package sporemodder.userinterface;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import sporemodder.MainApp;
import sporemodder.files.FileStreamAccessor;
import sporemodder.files.formats.shaders.CompiledShader;
import sporemodder.files.formats.shaders.CompiledShader.StandardShaderEntry;
import sporemodder.files.formats.shaders.CompiledShaders;
import sporemodder.files.formats.shaders.DebugShader;
import sporemodder.files.formats.shaders.ShaderConstant;
import sporemodder.files.formats.shaders.ShaderEntry;
import sporemodder.files.formats.shaders.ShaderManager;
import sporemodder.files.formats.shaders.ShaderManager.ManagerEntry;
import sporemodder.files.formats.shaders.ShaderManager.ManagerEntryShader;
import sporemodder.files.formats.shaders.SourceManager;
import sporemodder.utilities.Hasher;

public class UIShaderManager {
	
	private static final int MAX_LOADED_SOURCES = 64;
	private static final String FXC_PATH = "E:\\Eric\\Spore DLL Injection\\Shaders\\#40212004\\Decompiler\\fxc.exe";

	private JFrame frame;
	private JTree tree;
	private JTabbedPane tabbedPane;
	
	private File file;
	private CompiledShaders shaders;
	private final SourceManager sourceManager = new SourceManager(MAX_LOADED_SOURCES, FXC_PATH);
	private JTextArea taInfo;
	
	public UIShaderManager() {
		frame = new JFrame();
		frame.setSize(800, 600);
		frame.setLocationRelativeTo(null);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(5, 5, 5, 5));
		frame.getContentPane().add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		tree = new JTree();
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent arg0) {
				treeSelectionChanged();
			}
		});
		frame.getContentPane().add(tree, BorderLayout.SOUTH);
		
		JSplitPane splitPane = new JSplitPane();
		panel_1.add(splitPane);
		
		JPanel panel = new JPanel();
		splitPane.setRightComponent(panel);
		panel.setLayout(new BorderLayout(0, 0));
		
		JPanel infoPanel = new JPanel();
		infoPanel.setBorder(new EmptyBorder(4, 4, 4, 4));
		panel.add(infoPanel, BorderLayout.SOUTH);
		infoPanel.setLayout(new BorderLayout(0, 0));
		
		taInfo = new JTextArea("");
		taInfo.setEditable(false);
		taInfo.setLineWrap(false);
		infoPanel.add(taInfo);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		panel.add(tabbedPane, BorderLayout.CENTER);
		
		JScrollPane scrollPane_1 = new JScrollPane(tree);
		splitPane.setLeftComponent(scrollPane_1);
		splitPane.setDividerLocation(400);
	}
	
	private void loadTree() {
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Compiled Shaders");
		DefaultMutableTreeNode nSingleShaders = new DefaultMutableTreeNode("Standard Shaders");
		DefaultMutableTreeNode nShaderManagers = new DefaultMutableTreeNode("Shader Managers");
		DefaultMutableTreeNode nDebugVertexShaders = new DefaultMutableTreeNode("Debug Vertex Shaders");
		DefaultMutableTreeNode nDebugPixelShaders = new DefaultMutableTreeNode("Debug Pixel Shaders");
		
		DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
		tree.setModel(treeModel);
		
		treeModel.insertNodeInto(nSingleShaders, rootNode, rootNode.getChildCount());
		treeModel.insertNodeInto(nShaderManagers, rootNode, rootNode.getChildCount());
		treeModel.insertNodeInto(nDebugVertexShaders, rootNode, rootNode.getChildCount());
		treeModel.insertNodeInto(nDebugPixelShaders, rootNode, rootNode.getChildCount());
		
		CompiledShader[] singleShaders = shaders.getStandardShaders();
		for (CompiledShader shader : singleShaders) {
			// CompiledShaders can have more than one entry, but since they usually only have one, we will only show them if there's more than 1
			DefaultMutableTreeNode nShader = new DefaultMutableTreeNode(shader);
			treeModel.insertNodeInto(nShader, nSingleShaders, nSingleShaders.getChildCount());
			
			List<StandardShaderEntry> entries = shader.getEntries();
			if (entries.size() > 1) {
				for (StandardShaderEntry entry : entries) {
					treeModel.insertNodeInto(new DefaultMutableTreeNode(entry), nShader, nShader.getChildCount());
				}
			}
		}
		
		ShaderManager[] shaderManagers = shaders.getShaderManagers();
		for (ShaderManager manager : shaderManagers) {
			
			DefaultMutableTreeNode nManager = new DefaultMutableTreeNode(manager);
			treeModel.insertNodeInto(nManager, nShaderManagers, nShaderManagers.getChildCount());
			
			for (ManagerEntry entry : manager.entries) {
				treeModel.insertNodeInto(new DefaultMutableTreeNode(entry), nManager, nManager.getChildCount());
			}
		}
		
		DebugShader[] debugVertexShaders = shaders.getDebugVertexShaders();
		for (int i = 0; i < debugVertexShaders.length; i++) {
			treeModel.insertNodeInto(new DefaultMutableTreeNode(debugVertexShaders[i]), nDebugVertexShaders, nDebugVertexShaders.getChildCount());
		}
		
		DebugShader[] debugPixelShaders = shaders.getDebugPixelShaders();
		for (int i = 0; i < debugPixelShaders.length; i++) {
			treeModel.insertNodeInto(new DefaultMutableTreeNode(debugPixelShaders[i]), nDebugPixelShaders, nDebugPixelShaders.getChildCount());
		}
		
		tree.expandRow(0);
	}
	
	private void treeSelectionChanged() {
		TreePath selectionPath = tree.getSelectionPath();
		
		if (selectionPath != null) {
			Object obj = ((DefaultMutableTreeNode) selectionPath.getLastPathComponent()).getUserObject();
			
			if (obj instanceof CompiledShader) {
				List<StandardShaderEntry> entries = ((CompiledShader) obj).getEntries();
				// Only activate if it only has 1 entry, if not it should be activated by selecting the entry itself
				if (entries.size() == 1) {
					compiledShaderEntrySelected(entries.get(0));
				}
			}
			else if (obj instanceof StandardShaderEntry) {
				compiledShaderEntrySelected((StandardShaderEntry) obj);
			}
			else if (obj instanceof ShaderManager) {
				List<ManagerEntry> entries = ((ShaderManager) obj).entries;
				// Only activate if it only has 1 entry, if not it should be activated by selecting the entry itself
				if (entries.size() == 1) {
					shaderManagerEntrySelected((ManagerEntry) entries.get(0));
				}
			}
			else if (obj instanceof ManagerEntry) {
				shaderManagerEntrySelected((ManagerEntry) obj);
			}
			else if (obj instanceof DebugShader) {
				debugShaderSelected((DebugShader) obj);
			}
		}
	}
	
	private String getDebugShaderInfoText(DebugShader dbgShader) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("field_12C: ");
		sb.append(dbgShader.getField_12C());
		
		return sb.toString();
	}
	
	private void debugShaderSelected(DebugShader dbgShader) {
		tabbedPane.removeAll();
		
		taInfo.setText(getDebugShaderInfoText(dbgShader));
		
		JTextArea taShader = new JTextArea();
		taShader.setEditable(false);
		taShader.setLineWrap(false);
		
		JTextArea taConstants = new JTextArea();
		taConstants.setEditable(false);
		taConstants.setLineWrap(false);
		
		try {
			taShader.setText(dbgShader.getShaderSource(sourceManager));
			
			taConstants.setText(getConstantsText(dbgShader.getConstants(), dbgShader.getStartRegisters()));
			
			// If we don't do this, the caret will be at the end of the file
			taShader.setCaretPosition(0);
			taConstants.setCaretPosition(0);
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame, "Error reading/writing shader source", "Error", JOptionPane.ERROR_MESSAGE);
		} catch (InterruptedException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame, "Error converting shader", "Error", JOptionPane.ERROR_MESSAGE);
		}
		
		tabbedPane.addTab("Shader", new JScrollPane(taShader));
		tabbedPane.addTab("Shader Constants", new JScrollPane(taConstants));
	}
	
	private void shaderManagerEntrySelected(ManagerEntry managerEntry) {
		tabbedPane.removeAll();
		
		taInfo.setText(getInfoText(managerEntry.getParent()));
		
		JTable tblVertexShaders = new JTable(new ManagerTableModel(managerEntry.vertexShaders));
		
		JTable tblPixelShaders = new JTable(new ManagerTableModel(managerEntry.pixelShaders));
		
		tabbedPane.addTab("Vertex Shaders", new JScrollPane(tblVertexShaders));
		tabbedPane.addTab("Pixel Shaders", new JScrollPane(tblPixelShaders));
	}
	
	private void compiledShaderEntrySelected(StandardShaderEntry entry) {
		tabbedPane.removeAll();
		
		taInfo.setText(getInfoText(entry.getParent()));
		
		JTextArea taVertexShader = new JTextArea();
		taVertexShader.setEditable(false);
		taVertexShader.setLineWrap(false);
		
		JTextArea taPixelShader = new JTextArea();
		taPixelShader.setEditable(false);
		taPixelShader.setLineWrap(false);
		
		JTextArea taVertexConstants = new JTextArea();
		taVertexConstants.setEditable(false);
		taVertexConstants.setLineWrap(false);
		
		JTextArea taPixelConstants = new JTextArea();
		taPixelConstants.setEditable(false);
		taPixelConstants.setLineWrap(false);
		
		try {
			taVertexShader.setText(entry.getVertexShaderSource(sourceManager));
			taPixelShader.setText(entry.getPixelShaderSource(sourceManager));
			
			taVertexConstants.setText(getConstantsText(entry.getVertexShaderConstants(), null));
			taPixelConstants.setText(getConstantsText(entry.getPixelShaderConstants(), null));
			
			// If we don't do this, the caret will be at the end of the file
			taVertexShader.setCaretPosition(0);
			taPixelShader.setCaretPosition(0);
			taVertexConstants.setCaretPosition(0);
			taPixelConstants.setCaretPosition(0);
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame, "Error reading/writing shader source", "Error", JOptionPane.ERROR_MESSAGE);
		} catch (InterruptedException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame, "Error converting shader", "Error", JOptionPane.ERROR_MESSAGE);
		}
		
		tabbedPane.addTab("Vertex Shader", new JScrollPane(taVertexShader));
		tabbedPane.addTab("Pixel Shader", new JScrollPane(taPixelShader));
		tabbedPane.addTab("Vertex Shader Constants", new JScrollPane(taVertexConstants));
		tabbedPane.addTab("Pixel Shader Constants", new JScrollPane(taPixelConstants));
	}
	
	private String getConstantsText(ShaderConstant[] constants, int[] startRegisters) {
		StringBuilder sb = new StringBuilder();
		String lineSeparator = System.getProperty("line.separator");
		
		for (int i = 0; i < constants.length; i++) {
			//TODO get this name from a list;
			String constantName = null;
			if (constantName == null) {
				constantName = "const" + constants[i].register;
			}
			
			sb.append("extern uniform ");
			sb.append(constantName);
			sb.append(" : register(c");
			sb.append(startRegisters == null ? constants[i].register : startRegisters[i]);
			sb.append(")");
			sb.append("  // 0x");
			sb.append(Integer.toHexString(constants[i].constantIndex));
			sb.append(" 0x");
			sb.append(Integer.toHexString(constants[i].field_2));
			sb.append(" 0x");
			sb.append(Integer.toHexString(constants[i].field_8));
			sb.append(lineSeparator);
		}
		
		return sb.toString();
	}
	
	private String getInfoText(ShaderEntry entry) {
		StringBuilder sb = new StringBuilder();
		String lineSeparator = System.getProperty("line.separator");
		
		sb.append("Entry ID: ");
		sb.append(Hasher.getFileName(entry.getEntryID()));
		sb.append(lineSeparator);
		
		sb.append("var_28: ");
		sb.append(entry.getVar_28());
		sb.append(lineSeparator);
		
		sb.append("var_24: ");
		sb.append(entry.getVar_24());
		sb.append(lineSeparator);
		
		sb.append("var_2C: ");
		sb.append(entry.getVar_2C());
		sb.append(lineSeparator);
		
		sb.append("var_30: 0x");
		sb.append(Integer.toHexString(entry.getVar_30()));
		sb.append(lineSeparator);
		
		sb.append("var_34: 0x");
		sb.append(Integer.toHexString(entry.getVar_34()));
		sb.append(lineSeparator);
		
		sb.append("field_44: 0x");
		sb.append(Integer.toHexString(entry.getField_44()));
		sb.append(lineSeparator);
		
		return sb.toString();
	}
	
	public void setFile(File file) {
		this.file = file;
		try (FileStreamAccessor in = new FileStreamAccessor(file, "r")) {
			
			shaders = new CompiledShaders();
			shaders.read(in);
			loadTree();
			
			frame.setTitle(this.file.getName() + " - " + shaders.getName());
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame, "File not found", "Error", JOptionPane.ERROR_MESSAGE);
			
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame, "Error reading file", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void display(int defaultCloseOperation) {
		frame.setDefaultCloseOperation(defaultCloseOperation);
		frame.setVisible(true);
	}
	
	private static class ManagerTableModel extends AbstractTableModel {
		private static final String[] COLUMN_NAMES = new String[] {
				"field_0", "field_1", "field_2", "field_4", "field_6", "field_8", "field_C", "field_10", "field_14", "field_18"
		}; 
		
		private ManagerEntryShader[] shaders;
		
		public ManagerTableModel(ManagerEntryShader[] shaders) {
			this.shaders = shaders;
		}
		
		@Override
		public int getColumnCount() {
			return COLUMN_NAMES.length; 
		}
		
		@Override
        public int getRowCount() {
        	return shaders.length;
        }
		
		@Override
		public String getColumnName(int column) {
			return COLUMN_NAMES[column];
		}
		
		@Override
        public Object getValueAt(int row, int col) { 
			switch (col) {
			case 0: return shaders[row].field_0;
			case 1: return shaders[row].field_1;
			case 2: return shaders[row].field_2;
			case 3: return shaders[row].field_4;
			case 4: return shaders[row].field_6;
			case 5: return shaders[row].field_8;
			case 6: return shaders[row].field_C;
			case 7: return shaders[row].field_10;
			case 8: return shaders[row].field_14;
			case 9: return shaders[row].field_18;
			default: return null;
			}
		}
	}
	
	public static void main(String[] args) {
		MainApp.init();
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		UIShaderManager shaderManager = new UIShaderManager();
		shaderManager.setFile(new File("E:\\Eric\\Spore DLL Injection\\Shaders\\GA Shaders\\#40212004\\#00000003.rw4"));
		shaderManager.display(JFrame.EXIT_ON_CLOSE);
	}
}
