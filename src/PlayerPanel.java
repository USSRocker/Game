/**
 * � Dmitry Boyko, 2016
 */

import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.util.Iterator;
import java.util.TreeMap;

import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;

/**
 * ������ �� ������� �������
 */
public class PlayerPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private MainFrame mainFrame;
	private ModesPanel modesPanel;
	
	public PlayerTableModel playerTableModel;
	public JTable tblPlayers;
	public JButton btnMove;

	/**
	 * Create the panel.
	 */
	public PlayerPanel() {
		setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);

		playerTableModel = new PlayerTableModel();
		tblPlayers = new JTable(playerTableModel);
		tblPlayers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblPlayers.getColumnModel().getColumn(0).setPreferredWidth(50);
		tblPlayers.getColumnModel().getColumn(0).setMaxWidth(100);
		tblPlayers.getColumnModel().getColumn(1).setPreferredWidth(50);
		tblPlayers.getColumnModel().getColumn(1).setMaxWidth(100);
		tblPlayers.getColumnModel().getColumn(2).setPreferredWidth(150);
		tblPlayers.getColumnModel().getColumn(2).setMaxWidth(400);
		tblPlayers.getColumnModel().getColumn(3).setPreferredWidth(50);
		tblPlayers.getColumnModel().getColumn(3).setMaxWidth(100);
		tblPlayers.getColumnModel().getColumn(4).setPreferredWidth(75);
		tblPlayers.getColumnModel().getColumn(4).setMaxWidth(150);
		tblPlayers.getColumnModel().getColumn(5).setPreferredWidth(50);
		tblPlayers.getColumnModel().getColumn(5).setMaxWidth(100);
		scrollPane.setViewportView(tblPlayers);
		
		/*class MoveAction extends AbstractAction {
			private static final long serialVersionUID = 1L;
			public MoveAction(String text, ImageIcon icon, String desc, Integer mnemonic) {
		        super(text, icon);
		        putValue(SHORT_DESCRIPTION, desc);
		        putValue(MNEMONIC_KEY, mnemonic);
		    }
		    public void actionPerformed(ActionEvent e) {
		    	mainFrame.btnMove();
		    }
		}
		
		MoveAction moveAction = new MoveAction("Go left", null, "This is the left button.", new Integer(KeyEvent.VK_L));*/
		
		btnMove = new JButton("������� ���. <Alt+G>");
		btnMove.setIcon(new ImageIcon( getClass().getResource("lightning.png")) );
		btnMove.setEnabled(false);
		btnMove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mainFrame.btnMove();
			}
		});
		btnMove.setMnemonic(KeyEvent.VK_G);
		
		/*Object THE_KEY = "Key";
		btnMove.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, KeyEvent.CTRL_MASK), THE_KEY);
		btnMove.getActionMap().put(THE_KEY, moveAction);
		btnMove.setAction(moveAction);*/
		
		add(btnMove, BorderLayout.SOUTH);
	}

	/**
	 * ���������������� ������
	 */
	public void init(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
		modesPanel = mainFrame.modesPanel;
	}
	
	/**
	 * ���������� ������ �������
	 */	
	synchronized public void setPlayerList(String[] nicks) {
		playerTableModel.removeAll();
		for(int i = 0; i < nicks.length; i++) {
			playerTableModel.addNewPlayer(nicks[i]);
		}
	}
	
	/**
	 * �������� ������ ������ �� ����
	 */	
	synchronized public void addNewPlayer(String nick) {
		playerTableModel.addNewPlayer(nick);
	}
	
	/**
	 * �������� ������ ������
	 */	
	synchronized public PlayerRow addNewPlayer(InfoConnect infoConnect) {
		return playerTableModel.addNewPlayer(infoConnect);
	}
	
	/**
	 * ������� ������
	 */	
	synchronized public void removePlayer(String aNick) {
		playerTableModel.removePlayer(aNick);
	}

	/**
	 * ������ ������ �� ������� �� ������ ������
	 */	
	synchronized public PlayerRow getPlayerRow(int n) {
		if(playerTableModel.getRowCount() > n) {
			return playerTableModel.rows.get(n);
		}
		return null;
	}
	
	/**
	 * ����� ������ � ������� �� ����
	 */	
	synchronized public PlayerRow findPlayerFromTable(String nick) {
		return playerTableModel.findPlayer(nick);
	}
	
	/**
	 * ��������� ����� ������ � ������� 
	 */	
	synchronized public PlayerRow killPlayerFromTable(String nick) {
		PlayerRow player = playerTableModel.findPlayer(nick);
		player.lives--;
		player.killByCycle++;
		player.bonusMoves += (float)mainFrame.configPanel.spnAddMove.getValue();
		playerTableModel.fireTableDataChanged();
		return player;
	}
	
	/**
	 * ������ ���������� ������������ �������
	 */	
	public int getPlayerCount() {
		return playerTableModel.getRowCount();
	}
	
	/**
	 * ������ ���������� ������ ������
	 */	
	public int getPlayerLives(String nick) {
		PlayerRow player = playerTableModel.findPlayer(nick);
		if(player == null) {
			return -1;
		}
		return player.lives;
	}
	
	/**
	 * ������ �������� �� ������� �������
	 */	
	public Iterator<PlayerRow> getTableIterator() {
		return playerTableModel.rows.iterator();
	}
		
	/**
	 * ���������� ��������� ������ � ������� �������
	 */	
	public void setTableSelection(int row) {
		if(row >= 0 && row < tblPlayers.getRowCount()) {
			tblPlayers.setRowSelectionInterval(row, row);			
		}
	}
	
	/**
	 * ���������� ����������� �����
	 */
	public boolean setPlayerNames() {
		playerTableModel.randomizePlayers(mainFrame.getLives());		
		modesPanel.modelNicksList.clear();
		Iterator<PlayerRow> iter = getTableIterator();
		switch(modesPanel.cmbShowNick.getSelectedIndex()) {
			case 1:  // �� ������
				int n = 0;
			    while (iter.hasNext()) {
			    	PlayerRow player = iter.next();
			    	player.showNick = mainFrame.connectPanel.txtNick.getText() + (++n);
			    }				
				break;

			case 2:  // �� ���
			    while (iter.hasNext()) {
			    	PlayerRow player = iter.next();
			    	player.showNick = player.lastName + " " + player.name + " " + player.patronymic;
			    }				
				break;

			case 3:	 // �� ������ ���������
				if(!readNicksFromFile()) {
					return false;
				}
				for(int i = 0; i < modesPanel.modelNicksList.size() && iter.hasNext(); i++) {
			    	PlayerRow player = iter.next();
			    	player.showNick = modesPanel.modelNicksList.getElementAt(i);
				}
				break;

			default:  // 1 - �� ����
			    while (iter.hasNext()) {
			    	PlayerRow player = iter.next();
			    	player.showNick = player.nick;
			    }				
				break;
		}
		return true;
	}	
	
	/**
	 * ��������� ��������� ����� �� �����
	 */
	private boolean readNicksFromFile() {
		String strNicksFile = modesPanel.txtNicksPath.getText().trim();
		if(strNicksFile.isEmpty()) {
			mainFrame.setMessage("�� ������ ���� � ������ ����� �� ������� ��������� �����");
			return false;
		}

		File file = new File(strNicksFile);
		if(!file.exists() || !file.canRead()) {
			mainFrame.setMessage("���� �� ������� ����� �� ����������: " + strNicksFile);
			return false;
		}
		
		boolean success = true;
		FileInputStream fileStream = null;
		BufferedReader bufferedReader = null;
		try {
			fileStream = new FileInputStream(file);
			String charset = (String)modesPanel.cmbNicksFile.getSelectedItem();
			InputStreamReader streamReader = new InputStreamReader(fileStream, charset);
			bufferedReader = new BufferedReader(streamReader);
			String line;
			while( (line = bufferedReader.readLine()) != null ) {
				line = line.trim();
				if(!line.isEmpty()) {
					modesPanel.modelNicksList.addElement(line);
				}
			}
		} catch (IOException except) {
			success = false;
			mainFrame.showException("������ ������ ������" + strNicksFile, except);
		}
		
		if(bufferedReader != null) {
			try {
				bufferedReader.close();
			} catch (IOException eBufferClose) {
				mainFrame.showException("������ �������� ������:" + strNicksFile, eBufferClose);
			}
		}

		if(fileStream != null) {
			try {
				fileStream.close();
			} catch (IOException eStreamClose) {
				mainFrame.showException("������ �������� ������:" + strNicksFile, eStreamClose);
			}
		}
		
		if(modesPanel.modelNicksList.getSize() < getPlayerCount()) {
			mainFrame.setMessage("����� ����� � ������ (" + modesPanel.modelNicksList.getSize() + 
				") ������ ���� �� ������ ����� ������� (" + getPlayerCount() + ")!");
			return false;
		}

		TreeMap<String, String> sortedMap = new TreeMap<String, String>();
		for(int i = 0; i < modesPanel.modelNicksList.size(); i++) {
			String templateNick = modesPanel.modelNicksList.getElementAt(i);
			if(sortedMap.containsKey(templateNick.toUpperCase())) {
				mainFrame.setMessage("� ������ ��������� �������� ����: " + templateNick);
				return false;
			}
			sortedMap.put(templateNick.toUpperCase(), templateNick);
		}
		return success;
	}	
}

