/**
 * � Dmitry Boyko, 2016
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.channels.IllegalBlockingModeException;
import java.util.Date;

/**
 * ��� ������
 */
class PlayerMove implements Serializable {
	private static final long serialVersionUID = 1L;
	public InfoPlayerMove movedPlayer;   // ���������� �����
	public InfoPlayerMove killedPlayer;  // ��������� �����
	public Date startDate;               // ����� ������ ����
	public Date finishDate;              // ����� ��������� ����
	
	PlayerMove(String movedNick, String killedNick, Date start, MainFrame mainFrame) {
		movedPlayer = new InfoPlayerMove(mainFrame, movedNick);
		killedPlayer = new InfoPlayerMove(mainFrame, killedNick);
		startDate = start;
		finishDate = new Date();
	}
}

/**
 * ����� 
 */
class Player {
	public PlayerRow playerRow;
	public boolean death = false;
	
	Player(PlayerRow playerRow) {
		this.playerRow = playerRow;
	}
}

/**
 * ������ �������
 */
class Group {
	public int numOfGroup;	
	public int currentRound = 1;
	public int activePlayer = 0;
	public boolean finished = false;
	public Date startMove = new Date();	
	public ArrayList<Player> players = new ArrayList<Player>();
	public ArrayList<PlayerMove> playerMoves = new ArrayList<PlayerMove>();

	/**
	 * ������� ��������� ���
	 */
	synchronized public PlayerRow nextMove(MainFrame mainFrame) {
		startMove = new Date();	

		Player player = players.get(activePlayer);
		PlayerRow playerRow = player.playerRow;
		if(playerRow.bonusMoves > 0.9999) {
			playerRow.bonusMoves -= 1.0f;
			mainFrame.playerPanel.playerTableModel.fireTableDataChanged();
			return playerRow;
		}
		
		if(playerRow.killByCycle > 0) {
			playerRow.killByCycle = 0;
			mainFrame.playerPanel.playerTableModel.fireTableDataChanged();
		}
		
		Player nextPlayer;
		do {
			if(++activePlayer >= players.size()) {
				currentRound++;
				activePlayer = 0;
			}
			nextPlayer = players.get(activePlayer);
		} while(nextPlayer.death);
		return nextPlayer.playerRow;
	}
	
	/**
	 * ������ �������� ���
	 */
	synchronized public PlayerRow getActivePlayer() {
		return players.get(activePlayer).playerRow;
	}
	
	/**
	 * �������� ��� � ������ �����
	 */	
	synchronized public void addMove(PlayerMove playerMove) {
		playerMoves.add(playerMove);
	}
	
	/**
	 * ��������� ������ 
	 */
	synchronized public void depthPlayer(String killNick) {
		for(int i = 0; i < players.size(); i++) {
			Player player = players.get(i);
			if(player.playerRow.nick.equals(killNick)) {
				player.death = true;
				break;
			}
		}
	}
	
	/**
	 * 
	 */
	synchronized public PlayerRow getLivePlayer() {
		for(int i = 0; i < players.size(); i++) {
			Player player = players.get(i);
			if(!player.death) {
				return player.playerRow;
			}
		}
		return null;
	}
			
	/**
	 * ������ ������ �����
	 */
	synchronized public ArrayList<String> getNicks() {
		ArrayList<String> nicks = new ArrayList<String>();
		for(int i = 0; i < players.size(); i++) {
			nicks.add(players.get(i).playerRow.nick);
		}		
		return nicks;
	}
	
	/**
	 * ����������, ����������� �� ����?
	 */	
	synchronized public boolean isEndGame(MainFrame mainFrame) {
		ArrayList<String> nicks = getNicks();
		int playerCount = (int)mainFrame.configPanel.spnPlayerCount.getValue();
		if(mainFrame.isToEndMode()) {
			return mainFrame.playerPanel.playerTableModel.livePlayers(nicks) <= playerCount;
		} 
		return mainFrame.playerPanel.playerTableModel.livePlayers(nicks) + playerCount < nicks.size() + 1;
	}	
}

/**
 * ������� ������.
 */
public class ServerConnection extends MessageThread {
	private ServerSocket hostServer;
	public boolean finished = false;
	public ArrayList<ServerClient> clients = new ArrayList<ServerClient>();
	public ArrayList<Group> groups = new ArrayList<Group>();
	
	/**
	 * ����������� 
	 */
	ServerConnection(ServerSocket aHostServer, MainFrame aMainFrame) {
		super("Server", aMainFrame);
		hostServer = aHostServer;
	}
	
	/**
	 * �������� ���� ������
	 */
	public void run() {
		while(!finished) {
			Socket socket;
	        try {
				socket = hostServer.accept();
	        } catch(IllegalBlockingModeException eIllegalBlockingMode) {
	        	outMessage("������: ����� � ����������������� ������", eIllegalBlockingMode);
				continue;
	        } catch (SocketTimeoutException eTimeout) {
	        	continue;
	        } catch(SecurityException eSecurity) {
	        	outMessage("������ ���� ������� Security Manager", eSecurity);
				continue;
			} catch (IOException eAccept) {
				outMessage("������ ��������� ������ �������", eAccept);
				continue;
			}
	        ServerClient srvClient = new ServerClient(socket, this); 
	        srvClient.start();
		}
		closeHostServer();
		mainFrame.setModeBegin();
	}

	/**
	 * ���������������� ����� ����  
	 */
	public void initGame(ArrayList<PlayerRow> players) {
		// ��������� ������
		groups.clear();
		Group curGroup = null;
		for(int i = 0; i < players.size(); i++) {
			PlayerRow playerRow = players.get(i);
			if(curGroup == null || curGroup.numOfGroup != playerRow.group) {
				curGroup = new Group();
				curGroup.numOfGroup = playerRow.group;
				groups.add(curGroup);
			}
			Player player = new Player(playerRow);
			curGroup.players.add(player);
		}
		// ��������� ���������� ���������� � �������� � �������� ������� �������
		for(int i = 0; i < groups.size(); i++) {
			Group group = groups.get(i);
			InfoStartGame startGame = new InfoStartGame(group, mainFrame);
			for(int j = 0; j < group.players.size(); j++) {
				String nick = group.players.get(j).playerRow.nick;
				ServerClient client = findClient(nick);
				client.group = group;
				client.sendCommand( startGame );
				client.sendCommand( new InfoNextMove(group, null) );
			}
		}
		mainFrame.fileStorage.init( );			
		outMessage("����� ����.", null);		
	}
	
	/**
	 * �������� ���������� ����������
	 */
	synchronized public PlayerRow addClient(ServerClient client, InfoConnect infoConnect) {
		clients.add(client);
        return mainFrame.playerPanel.addNewPlayer(infoConnect);		
	}
	
	/**
	 * ������� ���������� ����������
	 */
	synchronized public void removeClient(ServerClient client) {
		clients.remove(client);
        mainFrame.playerPanel.removePlayer(client.nick);		
	}
	
	/**
	 * ����������, ����������� �� ���� �� ���� �������?
	 */	
	synchronized public boolean isEndGame() {
		for(int i = 0; i < groups.size(); i++) {
			if(!groups.get(i).finished) {
				return false;
			}
		}
		return true;
	}
			
	/**
	 * ����� ������� �� ���� 
	 */
	synchronized public ServerClient findClient(String nick) {
		Iterator<ServerClient> itr = clients.iterator();
	    while (itr.hasNext()) {
	    	ServerClient client = itr.next();
	    	if(client.nick.equals(nick)) {
	    		return client;
	    	}
	    }
	    return null;
	}
	
	/**
	 * ������ ������ ������������� 
	 */
	synchronized public String[] getUsers() {
		String[] users = new String[clients.size()];
		for(int i = 0; i < clients.size(); i++) {
			users[i] = clients.get(i).nick;
		}
		return users;
	}
	
	/**
	 * �������� ������� ���� ��������
	 */
	synchronized public void sendCommandToAll(Object command) {
		Iterator<ServerClient> itr = clients.iterator();
	    while (itr.hasNext()) {
	    	ServerClient client = itr.next();
	    	client.sendCommand(command);
	    }		
	}
	
	/**
	 * �������� ������� �������������
	 */
	synchronized public void sendCommandToUsers(ArrayList<String> users, Object command) {
		for(int i = 0; i < users.size(); i++) {
	    	ServerClient client = findClient(users.get(i));
	    	client.sendCommand(command);			
		}
	}
	
	/**
	 * ������� ��������� ����������
	 */
	public void closeHostServer() {
		Iterator<ServerClient> itr = clients.iterator();
	    while (itr.hasNext()) {
	    	ServerClient client = itr.next();
	    	client.closeSocket();
	    }
	    clients.clear();
		if(hostServer != null) {
			  try {
				  hostServer.close();
		      } catch (IOException e) { 
		      }
	    	  hostServer = null; 
		}	    
	}	
}

