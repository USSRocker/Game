/**
 * © Dmitry Boyko, 2016
 */

import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JProgressBar;
import java.awt.Dimension;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

/**
 * Информация по ходу игроку 
 */
class InfoPlayerMove implements Serializable {
	private static final long serialVersionUID = 1L;
	public String nick;            // Ник игрока
	public String showNick;        // Отображаемый ник игрока
	public int lives;              // Кол-во жизней игрока

	InfoPlayerMove(MainFrame mainFrame, String nick) {
		this.nick = nick;
		PlayerRow playerRow = mainFrame.playerPanel.findPlayerFromTable(nick);
		showNick = playerRow.showNick;
		lives = playerRow.lives;			
	}
}

/**
 * Строка таблицы с ходами игроков
 */
class MoveRow {
	public static final int columnNumber = 0, columnNick = 1, columnLives = 2, columnKilledNick = 3, columnKilledLives = 4;
	public Integer number;               // Порядковый номер
	public InfoPlayerMove movedPlayer;   // Походивший игрок
	public InfoPlayerMove killedPlayer;  // Игрок теряющий жизнь 
	
	MoveRow(InfoPlayerMove movedPlayer, InfoPlayerMove killedPlayer) {
		this.movedPlayer = movedPlayer;
		this.killedPlayer = killedPlayer;
	}
	
	public Object getValue(int column) {
		switch(column) {
			case columnNumber:
				return number;
			case columnNick:
				return movedPlayer.showNick;
			case columnLives:
				return movedPlayer.lives;
			case columnKilledNick:
				return killedPlayer.showNick;
			case columnKilledLives:
				return killedPlayer.lives;
			default:
				return null;
		}
	}
}

/**
 * Табличная модель списка игроков
 */
class MovesTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;

	public ArrayList<MoveRow> rows = new ArrayList<MoveRow>();
	private final String[] headings = new String[] {"№ хода", "Активный игрок", "Жизни", "Раненый игрок", "Жизни*"};
	private final Class<?>[] columnTypes = new Class[] {Integer.class, String.class, Integer.class, String.class, Integer.class};
	private final boolean[] columnEditables = new boolean[] {false, false, false, false, false};
	
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
	 * Добавить ход в журнал 
	 */
	public void addMove(PlayerMove move) {
		MoveRow moveRow = new MoveRow(move.movedPlayer, move.killedPlayer);
		moveRow.number = getRowCount() + 1;	
		rows.add(moveRow);
		fireTableDataChanged();
	}	
}
	
/**
 * Панель с ходами игроков 
 */
public class MovesPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	public MovesTableModel movesTableModel;
	public JTable tblMoves;
	public JProgressBar progressBar;
	//private MainFrame mainFrame;
	
	/**
	 * Конструктор
	 */
	public MovesPanel() {
		setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);
		
		movesTableModel = new MovesTableModel();
		tblMoves = new JTable(movesTableModel);
		tblMoves.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblMoves.getColumnModel().getColumn(0).setPreferredWidth(50);
		tblMoves.getColumnModel().getColumn(0).setMaxWidth(100);
		tblMoves.getColumnModel().getColumn(1).setPreferredWidth(150);
		tblMoves.getColumnModel().getColumn(1).setMaxWidth(400);
		tblMoves.getColumnModel().getColumn(2).setPreferredWidth(50);
		tblMoves.getColumnModel().getColumn(2).setMaxWidth(100);
		tblMoves.getColumnModel().getColumn(3).setPreferredWidth(150);
		tblMoves.getColumnModel().getColumn(3).setMaxWidth(400);
		tblMoves.getColumnModel().getColumn(4).setPreferredWidth(50);
		tblMoves.getColumnModel().getColumn(4).setMaxWidth(100);
		scrollPane.setViewportView(tblMoves);

		
		progressBar = new JProgressBar();
		progressBar.setPreferredSize(new Dimension(146, 18));
		progressBar.setMinimumSize(new Dimension(10, 18));
		add(progressBar, BorderLayout.SOUTH);
	}
	
	/**
	 * 
	 */
	public void init(MainFrame mainFrame) {
		//this.mainFrame = mainFrame;
	}
}
