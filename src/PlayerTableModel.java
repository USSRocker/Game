/**
 * � Dmitry Boyko, 2016
 */

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;

import javax.swing.table.AbstractTableModel;

/**
 * ������ ������� �� ������� �������
 */
class PlayerRow {
	public static final int columnGroup = 0, columnNumber = 1, columnPlayer = 2, columnLives = 3, 
			columnKillByCicle = 4, columnBonusMoves = 5;
	public int group;            // ����� ������
	public int number;           // ���������� �����
	public String nick;          // ��� ������
	public String showNick;      // ������������ ��� ������
	public int lives;            // ���-�� ������ ������
	public int killByCycle;      // ���-�� ��� ���� �� ����
	public float bonusMoves;     // �������� ���� 
	public String lastName;      // �������
	public String name;          // ���
	public String patronymic;	 // ��������
	public String computerName;  // ��� ����������
	public String userName;      // ��� ������������ ����������
	
	public Object getValue(int column) {
		switch(column) {
			case columnGroup:
				return group;
			case columnNumber:
				return number;
			case columnPlayer:
				return showNick;
			case columnLives:
				return lives;
			case columnKillByCicle:
				return killByCycle;
			case columnBonusMoves:
				return bonusMoves;
			default:
				return null;
		}
	}
}

/**
 * ��������� ������ ������ �������
 */
public class PlayerTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;

	public ArrayList<PlayerRow> rows = new ArrayList<PlayerRow>();
	private final String[] headings = new String[] {"������", "� ������", "������", "�����", "������ � ������", "������"};
	private final Class<?>[] columnTypes = new Class[] {Integer.class, Integer.class, String.class, Integer.class, 
			Integer.class, Float.class};
	private final boolean[] columnEditables = new boolean[] {false, false, false, false, false, false};
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return columnTypes[columnIndex];
	}
	
	@Override
	public String getColumnName(int columnIndex) {
		return headings[columnIndex];
	}
	
	@Override
	public boolean isCellEditable(int row, int column) {
		return columnEditables[column];
	}
	
	public int getColumnCount() {
		return headings.length;
	}
	
	synchronized public int getRowCount() {
		return rows.size();
	}
	
	public Object getValueAt(int row, int column) {
		return rows.get(row).getValue(column);
	}
	
	/**
	 * �������� ������ ������ �� �������
	 */
	public PlayerRow addNewPlayer(InfoConnect connect) {
		PlayerRow player = new PlayerRow();
		player.number = rows.size() + 1;
		player.nick = connect.nick;
		player.showNick = connect.nick;
		player.lastName = connect.lastName;
		player.name = connect.name;
		player.patronymic = connect.patronymic;
		player.computerName = connect.computerName;
		player.userName = connect.userName;
		rows.add(player);
		fireTableDataChanged();
		return player;
	}

	/**
	 * �������� ������ ������ �� ��������
	 */
	public void addNewPlayer(String nick) {
		PlayerRow player = new PlayerRow();
		player.number = rows.size() + 1;
		player.nick = nick;
		player.showNick = nick;
		rows.add(player);
		fireTableDataChanged();
	}
	
	/**
	 * ������������� ������� ���������� �����������
	 */
	public void initPlayers(InfoStartGame startGame, MainFrame mainFrame) {
		removeAll();
		for(int i = 0; i < startGame.users.size(); i++) {
			PlayerRow player = new PlayerRow();
			player.group = startGame.group;
			player.number = rows.size() + 1;
			player.nick = startGame.users.get(i).nick;
			player.showNick = startGame.users.get(i).showNick;
			player.lives = startGame.lives;
			rows.add(player);
			if( player.nick.equals(mainFrame.connectPanel.txtNick.getText()) ) {
				mainFrame.connectPanel.setPlayerTitle(player.showNick);
			}
		}		
		fireTableDataChanged();
	}
	
	/**
	 * ���������� ����� �������
	 */
	public int livePlayers(ArrayList<String> filterNicks) {
		int players = 0;
		for(int i = 0; i < getRowCount(); i++) {
			PlayerRow playerRow = rows.get(i);
			if(filterNicks != null && !filterNicks.contains(playerRow.nick)) {
				continue;
			}
			if(playerRow.lives > 0) {
				players++;
			}
		}
		return players;
	}
		
	/**
	 * ����������� ������ �������
	 */
	private void recalcNumbers() {
		for(int i = 0; i < getRowCount(); i++) {
			rows.get(i).number = i + 1;
		}
	}
	
	/**
	 * ����� ������ �� ����
	 */
	public PlayerRow findPlayer(String nick) {
		Iterator<PlayerRow> itr = rows.iterator();
	    while (itr.hasNext()) {
	    	PlayerRow player = itr.next();
	    	if(player.nick.equals(nick)) {
	    		return player;
	    	}
	    }
	    return null;
	}

	/**
	 * ����� ������� � ������� �� ����
	 */
	public int findPlayerPosition(String nick) {
		for(int i = 0; i < getRowCount(); i++) {
			if(nick.equals(rows.get(i).nick)) {
				return i;
			}
		}
	    return -1;
	}
	
	/**
	 * ������� ������ �� ����
	 */
	public void removePlayer(String nick) {
		PlayerRow row = findPlayer(nick);
		if(row != null) {
			rows.remove(row);
			recalcNumbers();
			fireTableDataChanged();
		}
	}
	
	/**
	 * ������� ���� �������
	 */
	public void removeAll() {
		rows.clear();
		fireTableDataChanged();			
	}
	
	/**
	 * ��������� ��������� ������ �������
	 */
	public void randomizePlayers(int lives) {
		Random rand = new Random(Math.abs((new Date()).getTime()));
		@SuppressWarnings("unchecked")
		ArrayList<PlayerRow> srcRows = (ArrayList<PlayerRow>) rows.clone();
		rows.clear();
		int j = 0;
		for(int i = srcRows.size(); i > 0; i--) {
			int n = rand.nextInt(i);
			PlayerRow player = srcRows.get(n);
			srcRows.remove(n);
			rows.add(player);
			player.group = 1;
			player.lives = lives;
			player.number = ++j; 
			player.bonusMoves = 0;
			player.killByCycle = 0;
		}
		fireTableDataChanged();
	}
	
	/**
	 * ��������� ��������� ����� �������
	 */
	public void randomizeGroups(int minGroup) {
		//Random rand = new Random(Math.abs((new Date()).getTime()));
		int users = getRowCount();
		int curUser = 0;
		int curGroup = 0;
		while(users > 0) {
			int groupSize;
			if(users != minGroup) {
				groupSize = minGroup; // + rand.nextInt(users - minGroup); 
				users -= groupSize;
				if(users < minGroup) {
					groupSize += users;
					users = 0;
				}
			} else {
				groupSize = minGroup;
				users = 0;
			}
			curGroup++;
			for(int i = 0; i < groupSize; i++) {
				rows.get(curUser++).group = curGroup;
			}
		}
	}	
}