/**
 * � Dmitry Boyko, 2016
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
 * ������� ����� ��� ������ � ������� ��������� �� �����
 */
public class MessageThread extends Thread {
	public MainFrame mainFrame;
	
	/**
	 * �����������
	 */
	MessageThread(String name, MainFrame mainFrame) {
		super(name);
		this.mainFrame = mainFrame;
	}
	
	/**
	 * ������� ��������� �� �����
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
 * ������� ����� � �������� ������/������ ������ �� ������  
 */
class ObjectThread extends MessageThread {
	protected Socket socket;
	protected ObjectInputStream objInStream = null;
	protected ObjectOutputStream objOutStream = null;
	
	/**
	 * �����������
	 */
	ObjectThread(String name, MainFrame mainFrame) {
		super(name, mainFrame);
	}
	
	/**
	 * ���������������� ������ ������/������
	 */
	public boolean initStreams()
	{
    	//������������ ������ � ������ ���, ����� ���� ����� ������������ ���������.
        try {
        	objOutStream = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException eOutputStream) {
        	outMessage("������ ��������� ��������� ������", eOutputStream);
        	return false;
        }
        try {
        	objInStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException eInputStream) {
        	outMessage("������ ��������� �������� ������", eInputStream);
        	return false;
        }        
        return true;
	}
		
	/**
	 * �������� �������
	 */
    synchronized public boolean sendCommand(Object answerCommand) {
        try {
        	objOutStream.writeObject(answerCommand);
        	objOutStream.flush();
        } catch (SocketException eSocket) {
        	outMessage("������: ����� ������", eSocket);
        	return false; 
        } catch (IOException eWriteObject) {
        	outMessage("������ �������� ��������� ���������", eWriteObject);
        	return false; 
        }        	
    	return true; 
    }
    
    /**
     * ������� �����
     */
	public void closeSocket() {
		if (objInStream != null) {
			  try {
				  objInStream.close();
			  } catch (IOException e) { 
				  outMessage("������ �������� �������� ������", e);
			  }
			  objInStream = null; 
		}
		if (objOutStream != null) {
			  try {
				  objOutStream.close();
			  } catch (SocketException eSocket) {
				  outMessage("������: ����� ������", eSocket);
			  } catch (IOException e) { 
				  outMessage("������ �������� ��������� ������", e);
			  }
			  objOutStream = null; 
		}		
		if (socket != null) {
			  try {
			        socket.close();
			  } catch (IOException e) { 
				  outMessage("������ �������� ������", e);				  
			  }
			  socket = null;
		}		
	}	
}

/**
 * ���������� �� ������ 
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
 *  ������� �� ������������ ����������
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
			mainFrame.setMessage("������ ��������� ����� ����������: " + eUnknownHost.getMessage());
		}
	}
}

/**
 * ������� �� ������ ����������
 */
class InfoDisconnect implements Serializable {
	static final long serialVersionUID = 1;
	InfoDisconnect() {
	}
}

/**
 * ������� �� ��������� ������ �������������
 */
class InfoUserList implements Serializable {
	static final long serialVersionUID = 1;
	public String[] users;
	InfoUserList(String[] aUsers) {
		users = aUsers;
	}
}

/**
 * ������� �� ���������� ������ ������������
 */
class InfoNewUser implements Serializable {
	static final long serialVersionUID = 1;
	public String newUser;
	InfoNewUser(String aNewUser) {
		newUser = aNewUser;
	}
}

/**
 * ������� �� �������� ������������
 */
class InfoRemoveUser implements Serializable {
	static final long serialVersionUID = 1;
	public String removeUser;
	InfoRemoveUser(String aRemoveUser) {
		removeUser = aRemoveUser;
	}
}

/**
 * ������� � ����������� �� ������: ������������ ����������
 */
class InfoUserExists implements Serializable {
	static final long serialVersionUID = 1;	
}

/**
 * ������� ������� ����
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
 * ������� �������� ����
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
 * ������� ���������� ����
 */
class InfoBreakGame implements Serializable {
	static final long serialVersionUID = 1;
	InfoBreakGame() {
	}
}

/**
 * �������: ����� ������
 */
class InfoKillPlayer implements Serializable {
	static final long serialVersionUID = 1;
	public String killNick;
	InfoKillPlayer(PlayerRow playerRow) {
		killNick = playerRow.nick;
	}
}

/**
 * �������: �������� ���
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
 * �������: ��� ������
 */
class InfoFinishtMove implements Serializable {
	static final long serialVersionUID = 1;
	InfoFinishtMove() {
	}
}

/**
 * �������: ������� ����
 */
class InfoAdmission implements Serializable {
	static final long serialVersionUID = 1;
	public boolean isOver;
	InfoAdmission(boolean isOver) {
		this.isOver = isOver;
	}
}

/**
 * �������: ���� ��������
 */
class InfoGameHasBegun implements Serializable {
	static final long serialVersionUID = 1;
	InfoGameHasBegun() {
	}
}

/**
 * �������: ��������/��������� ������������� �����
 */
class InfoFullScreen implements Serializable {
	static final long serialVersionUID = 1;
	public boolean mode;
	InfoFullScreen(boolean aMode) {
		mode = aMode;
	}
}

/**
 * �������: �������
 */
class InfoTimeout implements Serializable {
	static final long serialVersionUID = 1;
	
	String showNick;
	InfoTimeout(String showNick) {
		this.showNick = showNick;
	}
}

