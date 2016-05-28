/**
 * © Dmitry Boyko, 2016
 */

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.ListModel;

/**
 * Базовый класс для потока с выводом сообщений на экран
 */
public class MessageThread extends Thread {
	public MainFrame mainFrame;
	
	/**
	 * Конструктор
	 */
	MessageThread(String name, MainFrame mainFrame) {
		super(name);
		this.mainFrame = mainFrame;
	}
	
	/**
	 * Вывести сообщение на экран
	 */
	protected void outMessage(String msg, Throwable except) {
		String outMsg = msg;
		if( except != null) {
			outMsg = outMsg + ": '" + except.getMessage() + "'";
		}
		mainFrame.setMessage(outMsg);
	}
}

/**
 * Фоновый поток с потоками чтения/записи данных из сокета  
 */
class ObjectThread extends MessageThread {
	protected Socket socket;
	protected ObjectInputStream objInStream = null;
	protected ObjectOutputStream objOutStream = null;
	
	/**
	 * Конструктор
	 */
	ObjectThread(String name, MainFrame mainFrame) {
		super(name, mainFrame);
	}
	
	/**
	 * Инициализировать потоки чтения/записи
	 */
	public boolean initStreams()
	{
    	//Конвертируем потоки в другой тип, чтобы было легче обрабатывать сообщения.
        try {
        	objOutStream = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException eOutputStream) {
        	outMessage("Ошибка получения выходного потока", eOutputStream);
        	return false;
        }
        try {
        	objInStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException eInputStream) {
        	outMessage("Ошибка получения входного потока", eInputStream);
        	return false;
        }        
        return true;
	}
		
	/**
	 * Передать команду
	 */
    synchronized public boolean sendCommand(Object answerCommand) {
        try {
        	objOutStream.writeObject(answerCommand);
        	objOutStream.flush();
        } catch (SocketException eSocket) {
        	outMessage("Ошибка: Сокет закрыт", eSocket);
        	return false; 
        } catch (IOException eWriteObject) {
        	outMessage("Ошибка передачи ответного сообщения", eWriteObject);
        	return false; 
        }        	
    	return true; 
    }
    
    /**
     * Закрыть сокет
     */
	public void closeSocket() {
		if (objInStream != null) {
			  try {
				  objInStream.close();
			  } catch (IOException e) { 
				  outMessage("Ошибка закрытия входного потока", e);
			  }
			  objInStream = null; 
		}
		if (objOutStream != null) {
			  try {
				  objOutStream.close();
			  } catch (SocketException eSocket) {
				  outMessage("Ошибка: Сокет закрыт", eSocket);
			  } catch (IOException e) { 
				  outMessage("Ошибка закрытия выходного потока", e);
			  }
			  objOutStream = null; 
		}		
		if (socket != null) {
			  try {
			        socket.close();
			  } catch (IOException e) { 
				  outMessage("Ошибка закрытия сокета", e);				  
			  }
			  socket = null;
		}		
	}	
}

/**
 * Информация по игроку 
 */
class InfoPlayer implements Serializable {
	static final long serialVersionUID = 1;
	public String nick;
	public String showNick;
	
	InfoPlayer(PlayerRow playerRow) {
		nick = playerRow.nick;
		showNick = playerRow.showNick;
	}
}

/**
 *  Команда на установления соединения
 */
class InfoConnect implements Serializable {
	static final long serialVersionUID = 1L;
	public String nick;
	public String lastName;
	public String name;
	public String patronymic;
	public String userName;
	public String computerName;	
	InfoConnect(String nick, MainFrame mainFrame) {
		ConnectPanel connectPanel = mainFrame.connectPanel;				
		this.nick = nick;
		lastName = connectPanel.txtLastName.getText();
		name = connectPanel.txtName.getText();
		patronymic = connectPanel.txtPatronymic.getText();
		userName = System.getProperty("user.name");
		try {
			computerName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException eUnknownHost) {
			mainFrame.setMessage("Ошибка получения имени компьютера: " + eUnknownHost.getMessage());
		}
	}
}

/**
 * Команда на разрыв соединения
 */
class InfoDisconnect implements Serializable {
	static final long serialVersionUID = 1;
	InfoDisconnect() {
	}
}

/**
 * Команда на получение списка пользователей
 */
class InfoUserList implements Serializable {
	static final long serialVersionUID = 1;
	public String[] users;
	InfoUserList(String[] aUsers) {
		users = aUsers;
	}
}

/**
 * Команда на добавление нового пользователя
 */
class InfoNewUser implements Serializable {
	static final long serialVersionUID = 1;
	public String newUser;
	InfoNewUser(String aNewUser) {
		newUser = aNewUser;
	}
}

/**
 * Команда на удаление пользователя
 */
class InfoRemoveUser implements Serializable {
	static final long serialVersionUID = 1;
	public String removeUser;
	InfoRemoveUser(String aRemoveUser) {
		removeUser = aRemoveUser;
	}
}

/**
 * Команда с информацией об ошибке: Пользователь существует
 */
class InfoUserExists implements Serializable {
	static final long serialVersionUID = 1;	
}

/**
 * Команда запуска игры
 */
class InfoStartGame implements Serializable {
	static final long serialVersionUID = 1;
	public int group;
	public ArrayList<InfoPlayer> users = new ArrayList<InfoPlayer>();
	public int gameMode;
	public int playerCount;
	public int lives;
	public int seconds;
	public float addMove;
	public boolean splitOnGroups;
	public int minGroup;
	public int playerShowMode;
	public ArrayList<String> templateNicks = new ArrayList<String>();
	
	InfoStartGame(Group group, MainFrame mainFrame) {
		this.group = group.numOfGroup;
		for(int i = 0; i < group.players.size(); i++) {
			PlayerRow playerRow = group.players.get(i).playerRow;
			users.add(new InfoPlayer(playerRow));
		}
		ConfigPanel configPanel = mainFrame.configPanel;
		gameMode = configPanel.cbGameMode.getSelectedIndex();
		playerCount = (int)configPanel.spnPlayerCount.getValue();
		lives = mainFrame.getLives();
		seconds = mainFrame.getSeconds();
		addMove = (float)configPanel.spnAddMove.getValue();
		splitOnGroups = configPanel.cbSplitOnGroups.isSelected();
		minGroup = (int)configPanel.spnMinGroup.getValue();
		playerShowMode = mainFrame.modesPanel.cmbShowNick.getSelectedIndex();
		templateNicks.clear();
		ListModel<String> nickList = mainFrame.modesPanel.lstNicksList.getModel();
		for(int i = 0; i < nickList.getSize(); i++) {
			templateNicks.add(nickList.getElementAt(i));
		}
	}
}

/**
 * Команда останова игры
 */
class InfoStopGame implements Serializable {
	static final long serialVersionUID = 1;
	public String text;
	public InfoPlayer player;
	InfoStopGame(String text, PlayerRow player) {
		this.text = text;
		this.player = new InfoPlayer(player);
	}
}

/**
 * Команда прерывания игры
 */
class InfoBreakGame implements Serializable {
	static final long serialVersionUID = 1;
	InfoBreakGame() {
	}
}

/**
 * Команда: Убить игрока
 */
class InfoKillPlayer implements Serializable {
	static final long serialVersionUID = 1;
	public String killNick;
	InfoKillPlayer(PlayerRow playerRow) {
		killNick = playerRow.nick;
	}
}

/**
 * Команда: передать ход
 */
class InfoNextMove implements Serializable {
	static final long serialVersionUID = 1;
	public int round;
	public int activePlayer;
	public String activeNick;
	public String activeShowNick;
	public float addMove;
	InfoNextMove(Group group, PlayerRow nextPlayerRow) {
		round = group.currentRound;
		activePlayer = group.activePlayer;
		PlayerRow playerRow = group.getActivePlayer();
		activeNick = playerRow.nick;
		activeShowNick = playerRow.showNick;
		if(nextPlayerRow == null) {
			this.addMove = 0.0f;
		} else {
			this.addMove = nextPlayerRow.bonusMoves;
		}
	}
}

/**
 * Команда: ход сделан
 */
class InfoFinishtMove implements Serializable {
	static final long serialVersionUID = 1;
	InfoFinishtMove() {
	}
}

/**
 * Команда: Окончен приём
 */
class InfoAdmission implements Serializable {
	static final long serialVersionUID = 1;
	public boolean isOver;
	InfoAdmission(boolean isOver) {
		this.isOver = isOver;
	}
}

/**
 * Команда: Игра началась
 */
class InfoGameHasBegun implements Serializable {
	static final long serialVersionUID = 1;
	InfoGameHasBegun() {
	}
}

/**
 * Команда: Включить/Выключить полноэкранный режим
 */
class InfoFullScreen implements Serializable {
	static final long serialVersionUID = 1;
	public boolean mode;
	InfoFullScreen(boolean aMode) {
		mode = aMode;
	}
}

/**
 * Команда: Таймаут
 */
class InfoTimeout implements Serializable {
	static final long serialVersionUID = 1;
	
	String showNick;
	InfoTimeout(String showNick) {
		this.showNick = showNick;
	}
}

