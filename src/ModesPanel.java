/**
 * © Dmitry Boyko, 2016
 */

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileFilter;

/**
 * 
 */
public class ModesPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private MainFrame mainFrame;

	public JToggleButton btnNewGame;
	private JLabel lblNicksPath;
	public JTextField txtNicksPath;
	public JComboBox<String> cmbShowNick;
	public JComboBox<String> cmbNicksFile;
	private JButton btnNicksPath;
	public JList<String> lstNicksList;
	public DefaultListModel<String> modelNicksList;

	/**
	 * Create the panel.
	 */
	public ModesPanel() {
		setMinimumSize(new Dimension(600, 281));
		setBounds(0, 674, 600, 289);
		ModesPanel panel = this;		
		setLayout(null);

		btnNewGame = new JToggleButton("Старт игры");
		btnNewGame.setIcon(new ImageIcon( getClass().getResource("tick-circle.png")) );
		btnNewGame.setMinimumSize(new Dimension(115, 23));
		btnNewGame.setPreferredSize(new Dimension(115, 23));
		btnNewGame.setEnabled(false);
		btnNewGame.setBounds(10, 11, 225, 23);
		btnNewGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mainFrame.clearMessage();
				if(btnNewGame.isSelected()) {
					// Если нажимается кнопка "Старт игры"
					if(!mainFrame.btnStartGame()) {
						btnNewGame.setSelected(false);
					}
				} else {
					// Если отжимается кнопка "Старт игры"	
		            if (mainFrame.showConfirmDialog("Прервать игру?")) {
		            	mainFrame.finishServerGame();
		            	mainFrame.serverConn.sendCommandToAll(new InfoBreakGame());
		            } else {
		            	btnNewGame.setSelected(true);
		            }			
				}				
			}
		});
		panel.add(btnNewGame);
		
		JLabel lblShowNick = new JLabel("Отображение игроков");
		lblShowNick.setBounds(10, 71, 244, 20);
		panel.add(lblShowNick);
		
		cmbShowNick = new JComboBox<String>();
		cmbShowNick.setEnabled(false);
		cmbShowNick.setModel(new DefaultComboBoxModel<String>(new String[] {"По нику", "По номеру", "По ФИО", "По списку заготовок"}));
		lblShowNick.setLabelFor(cmbShowNick);
		cmbShowNick.setBounds(264, 71, 212, 20);
		panel.add(cmbShowNick);
		
		JLabel lblCharset = new JLabel("Кодовая страница");
		lblCharset.setBounds(10, 102, 244, 19);
		panel.add(lblCharset);
		
		cmbNicksFile = new JComboBox<String>();
		cmbNicksFile.setEnabled(false);
		cmbNicksFile.setModel(new DefaultComboBoxModel<String>(new String[] {"UTF-8", "WINDOWS-1251"}));
		cmbNicksFile.setBounds(264, 102, 134, 20);
		panel.add(cmbNicksFile);
		
		lblNicksPath = new JLabel("Путь к файлу со списком заготовок");
		lblNicksPath.setBounds(10, 132, 244, 20);
		panel.add(lblNicksPath);
		
		txtNicksPath = new JTextField();
		txtNicksPath.setEnabled(false);
		txtNicksPath.setBounds(10, 163, 547, 20);
		panel.add(txtNicksPath);
		txtNicksPath.setColumns(10);
		
		btnNicksPath = new JButton("");
		btnNicksPath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				choiceNicksFile();
			}
		});
		btnNicksPath.setIcon(new ImageIcon(MainFrame.class.getResource("/javax/swing/plaf/metal/icons/ocean/file.gif")));
		btnNicksPath.setEnabled(false);
		btnNicksPath.setBounds(561, 158, 28, 29);
		panel.add(btnNicksPath);
		
		modelNicksList = new DefaultListModel<String>();
		
		JScrollPane scrollNicksList = new JScrollPane();
		scrollNicksList.setBounds(10, 196, 547, 82);
		panel.add(scrollNicksList);
		lstNicksList = new JList<String>();
		scrollNicksList.setViewportView(lstNicksList);
		lstNicksList.setModel( modelNicksList );
		lstNicksList.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		lstNicksList.setBackground(UIManager.getColor("Button.background"));
	}
	
	/**
	 * 
	 */
	public void setMainFrame(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}
	
	/**
	 * Выбрать файл со списком заготовок
	 */
	private void choiceNicksFile() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		fileChooser.addChoosableFileFilter(new FileFilter() {
			@Override
			public boolean accept(File file) {
		        if (file.isDirectory()) {
		            return true;
		        } 
		        return file.getName().toLowerCase().endsWith(".txt");
			}
	
			@Override
			public String getDescription() {
				return "TXT Текстовые файлы (*.txt)";
			}
		});
		
		//fileChooser.setAcceptAllFileFilterUsed(false);
		if(!txtNicksPath.getText().isEmpty()) {
			File dirFile = new File(txtNicksPath.getText());
			fileChooser.setCurrentDirectory(dirFile);
		}
		int result = fileChooser.showOpenDialog(this);
		if (result != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File choiceFile = fileChooser.getSelectedFile();
		txtNicksPath.setText(choiceFile.getAbsolutePath());
	}
	
	/**
	 * 
	 */
	public void enabledAfterStartGame(boolean enabled) {
		txtNicksPath.setEnabled(enabled);
		btnNicksPath.setEnabled(enabled);
		cmbShowNick.setEnabled(enabled);
		cmbNicksFile.setEnabled(enabled);		
	}
	
	/**
	 * 
	 */
	public void startGame(InfoStartGame startGame) {
		cmbShowNick.setSelectedIndex(startGame.playerShowMode);
		modelNicksList.clear();
		for(int i = 0; i < startGame.templateNicks.size(); i++) {
			modelNicksList.addElement(startGame.templateNicks.get(i));
		}		
	}
}
