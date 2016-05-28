/**
 * © Dmitry Boyko, 2016
 */

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

public class ConfigPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private MainFrame mainFrame;
	public static final String[] arrayGameMode = new String[] {"До оставшихся", "До выбывших"};

	public JComboBox<String> cbGameMode;
	public JSpinner spnLives;
	public JSpinner spnSeconds;
	public JSpinner spnAddMove;
	public JCheckBox cbSplitOnGroups;
	public JSpinner spnMinGroup;
	public JTextField txtDir;
	private JButton btnDir;
	private JLabel lblPlayerCount;
	public JSpinner spnPlayerCount;

	/**
	 * Create the panel.
	 */
	public ConfigPanel() {
		ConfigPanel panel = this;		
		setPreferredSize(new Dimension(600, 325));
		setSize(new Dimension(600, 325));
		setMinimumSize(new Dimension(600, 325));
		setLayout(null);
		
		JLabel lblGameMode = new JLabel("Режим игры");
		lblGameMode.setBounds(10, 11, 247, 17);
		panel.add(lblGameMode);
		
		cbGameMode = new JComboBox<String>();
		cbGameMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setPlayerCountText();
			}
		});
		lblGameMode.setLabelFor(cbGameMode);
		cbGameMode.setEnabled(false);
		cbGameMode.setModel(new DefaultComboBoxModel<String>(arrayGameMode));
		cbGameMode.setBounds(267, 11, 161, 20);
		panel.add(cbGameMode);
		
		lblPlayerCount = new JLabel("Кол-во оставшихся игроков");
		lblPlayerCount.setBounds(10, 38, 241, 20);
		panel.add(lblPlayerCount);
		
		spnPlayerCount = new JSpinner();
		spnPlayerCount.setEnabled(false);
		lblPlayerCount.setLabelFor(spnPlayerCount);
		spnPlayerCount.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
		spnPlayerCount.setBounds(267, 38, 61, 20);
		lblPlayerCount.setLabelFor(spnPlayerCount);
		panel.add(spnPlayerCount);
		
		JLabel lblLives = new JLabel("Кол-во жизней");
		lblLives.setBounds(10, 84, 254, 20);
		panel.add(lblLives);
		
		spnLives = new JSpinner();
		lblLives.setLabelFor(spnLives);
		spnLives.setEnabled(false);
		spnLives.setModel(new SpinnerNumberModel(new Integer(5), new Integer(1), null, new Integer(1)));
		spnLives.setBounds(267, 84, 61, 20);
		panel.add(spnLives);
		
		JLabel lblSeconds = new JLabel("Время на ход в сек.");
		spnSeconds = new JSpinner();
		lblSeconds.setLabelFor(spnSeconds);
		lblSeconds.setBounds(11, 112, 246, 17);
		panel.add(lblSeconds);
		
		spnSeconds.setModel(new SpinnerNumberModel(60, 1, 600, 10));
		spnSeconds.setEnabled(false);
		spnSeconds.setBounds(267, 112, 61, 20);
		panel.add(spnSeconds);
		 
		JLabel lblAddMove = new JLabel("Доп.ход за потерю жизни");
		lblAddMove.setBounds(10, 137, 247, 17);
		panel.add(lblAddMove);
		
		spnAddMove = new JSpinner();
		spnAddMove.setEnabled(false);
		lblAddMove.setLabelFor(spnAddMove);
		spnAddMove.setModel(new SpinnerNumberModel(new Float(0), new Float(0), new Float(1), new Float(0.1)));
		spnAddMove.setBounds(267, 137, 61, 20);
		panel.add(spnAddMove);

		cbSplitOnGroups = new JCheckBox("Делить на группы");
		cbSplitOnGroups.setSelected(true);
		cbSplitOnGroups.setEnabled(false);
		cbSplitOnGroups.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				spnMinGroup.setEnabled(cbSplitOnGroups.isSelected());
			}
		});
		cbSplitOnGroups.setBounds(6, 177, 189, 20);
		panel.add(cbSplitOnGroups);
				
		JLabel lblMinGroup = new JLabel("Минимальная группа");
		lblMinGroup.setBounds(10, 205, 247, 20);
		panel.add(lblMinGroup);
		
		spnMinGroup = new JSpinner();
		lblMinGroup.setLabelFor(spnMinGroup);
		spnMinGroup.setEnabled(false);
		spnMinGroup.setModel(new SpinnerNumberModel(new Integer(3), new Integer(3), null, new Integer(1)));
		spnMinGroup.setBounds(267, 205, 61, 20);
		panel.add(spnMinGroup);
		
		JLabel lblDir = new JLabel("Путь для сохранения файлов");
		lblDir.setBounds(10, 250, 401, 20);
		panel.add(lblDir);
		lblDir.setLabelFor(txtDir);
		
		txtDir = new JTextField();
		txtDir.setEnabled(false);
		txtDir.setBounds(10, 272, 547, 20);
		panel.add(txtDir);
		txtDir.setColumns(255);
		
		btnDir = new JButton("");
		btnDir.setIcon(new ImageIcon( getClass().getResource("folder-open-document-text.png")) );
		btnDir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				choiceDirectory();
			}
		});
		btnDir.setEnabled(false);
		btnDir.setBounds(561, 267, 28, 29);
		panel.add(btnDir);				
	}

	public void setMainFrame(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}

	/**
	 * Установить текст метки по режиму игры
	 */
	public void setPlayerCountText() {
		lblPlayerCount.setText(mainFrame.getPlayerCountText());
	}

	/**
	 * Выбрать директорию сохранения файлов
	 */
	private void choiceDirectory() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setAcceptAllFileFilterUsed(false);
		if(!txtDir.getText().isEmpty()) {
			File dirFile = new File(txtDir.getText());
			fileChooser.setCurrentDirectory(dirFile);
		}
		int result = fileChooser.showSaveDialog(this);
		if (result != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File choiceFile = fileChooser.getSelectedFile();
		txtDir.setText(choiceFile.getAbsolutePath());
	}

	/**
	 * 
	 */
	public void enabledAfterStartGame(boolean enabled) {
		spnPlayerCount.setEnabled(enabled);
		spnAddMove.setEnabled(enabled);
		spnLives.setEnabled(enabled);
		spnSeconds.setEnabled(enabled);
		cbGameMode.setEnabled(enabled);
		cbSplitOnGroups.setEnabled(enabled);
		spnMinGroup.setEnabled(enabled);
		txtDir.setEnabled(enabled);
		btnDir.setEnabled(enabled);		
	}
	
	/**
	 * 
	 */
	public void startGame(InfoStartGame startGame) {
		cbGameMode.setSelectedIndex(startGame.gameMode);
		setPlayerCountText();
		spnPlayerCount.setValue(startGame.playerCount);
		spnLives.setValue(startGame.lives);
		spnSeconds.setValue(startGame.seconds);
		spnAddMove.setValue(startGame.addMove);
		cbSplitOnGroups.setSelected(startGame.splitOnGroups);
		spnMinGroup.setValue(startGame.minGroup);				
	}
}
