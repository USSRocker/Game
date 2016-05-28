/**
 * © Dmitry Boyko, 2016
 */

import java.io.EOFException;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

/**
 * Один канал сетевого сервера.
 */
public class ServerClient extends ObjectThread {
	public PlayerRow playerRow = null; 
	public String nick;
	public Group group;
	private ServerConnection serverConn;
	
	ServerClient(Socket aSocket, ServerConnection srvConn) {
		super("NewSocket", srvConn.mainFrame);
		socket = aSocket;
		serverConn = srvConn;
		setPriority(NORM_PRIORITY - 1);
	}
	
	public void run() {
		boolean loop = true;
        if(!initStreams()) {
        	loop = false;
        }
        while(!serverConn.finished && loop) {		
            Object infoCommand;
			try {
				infoCommand = objInStream.readObject();
				mainFrame.setMessage("");
			} catch(EOFException eEOF) {
				break;
			} catch(StreamCorruptedException eStreamCorrupted) {
				outMessage("Информация в потоке неконсистентна", eStreamCorrupted);
	        	continue;
			} catch(InvalidClassException eInvalidClass) {
				outMessage("Ошибка сериализации класса", eInvalidClass);
	        	continue;
			} catch(OptionalDataException eOptionalData) { 
				outMessage("Данные в потоке не являются объектом", eOptionalData);
	        	continue;
			} catch(ClassNotFoundException eClassNotFound) {
				outMessage("Ошибка поиска класса", eClassNotFound);
	        	continue;
			} catch(IOException eReadObject) {
				outMessage("Ошибка чтения класса", eReadObject);
	        	continue;
			}

			// Команда на установление соединения	
	    	if(infoCommand instanceof InfoConnect) {
    			InfoConnect infoConnect = (InfoConnect)infoCommand;
    			nick = infoConnect.nick;
    			if(serverConn.findClient(nick) != null) {
    				sendCommand( new InfoUserExists() );
    				break;
    			} else if(mainFrame.connectPanel.isPressedAdmissionButton()) {
    				sendCommand( new InfoGameHasBegun() );
    				break;
    			}
    			this.setName(nick);
    	        playerRow = serverConn.addClient(this, infoConnect);    	        
                sendCommand( new InfoUserList(serverConn.getUsers()) );
                sendCmdToAllExclude(new InfoNewUser(nick));  // Рассылаем всем команду о добавление нового пользователя
        	    
        	// Команда на разрыв соединения        	    
	    	} else if (infoCommand instanceof InfoDisconnect) {
	    		serverConn.removeClient(this);
	    		sendCommandToAll(new InfoRemoveUser(nick));  // Рассылаем всем команду об удаление пользователя
                break;

            // Ход: 'Убить игрока'
	    	} else if(infoCommand instanceof InfoKillPlayer) {
	    		InfoKillPlayer killPlayer = (InfoKillPlayer)infoCommand;
	    		
	    		PlayerRow killedPlayerRow = mainFrame.playerPanel.killPlayerFromTable(killPlayer.killNick);
	    		int lives = killedPlayerRow.lives; 
	    		sendCommandToPlayers(group.players, killPlayer);

	    		PlayerMove move = new PlayerMove(nick, killPlayer.killNick, group.startMove, mainFrame);
	    		group.addMove(move);
	    		mainFrame.movesPanel.movesTableModel.addMove(move);
	    		sendCommandToPlayers(group.players, move);
	    		
	    		// Если игрок выбыл из игры
	    		if(lives == 0) {
	    			group.depthPlayer(killPlayer.killNick);
	    		}
	    		
	    		// Если группа закончила игру
	    		if(lives == 0 && group.isEndGame(mainFrame)) {
	    			group.finished = true;
	    			PlayerRow user;
	    			String msg = "Группа: " + group.numOfGroup + ". ";
	    			mainFrame.fileStorage.finishGame = new Date();
	    			if(mainFrame.isToEndMode()) {
	    				PlayerRow victorPlayerRow = group.getLivePlayer();
	    				user = victorPlayerRow;
	    				mainFrame.fileStorage.victor = victorPlayerRow.nick;
	    				mainFrame.fileStorage.showVictor = victorPlayerRow.showNick;
	    				msg += "Победитель: ";
	    				sendCommandToPlayers(group.players, new InfoStopGame(msg, user));
	    			} else {
	    				user = killedPlayerRow;
	    				mainFrame.fileStorage.loser = killedPlayerRow.nick;
	    				mainFrame.fileStorage.showLoser = killedPlayerRow.showNick;
	    				msg += "Проигравший: ";
	    				sendCommandToPlayers(group.players, new InfoStopGame(msg, user));
	    			}
	    			mainFrame.fileStorage.Save(group);
	    			mainFrame.finishedGroup(group, msg, user);
	    			// Закончена ли игра для всех групп?
	    			if(serverConn.isEndGame()) {
	    				mainFrame.finishedGame(msg, user);	    				
	    			}
	    		} else {
	    			PlayerRow nextPlayerRow = group.nextMove(mainFrame);
	    			sendCommandToPlayers(group.players, new InfoNextMove(group, nextPlayerRow));
	    		}

            // Ход: Таймаут
	    	} else if(infoCommand instanceof InfoTimeout) {
	    		mainFrame.setMessage("Превышено время ожидания хода: " + playerRow.showNick);
	    		sendCmdToAllExclude(infoCommand);
	    	}
        }       
        closeSocket();
	}
	
	/**
	 * Передать команду всем клиентам
	 */
	private void sendCommandToAll(Object command) {
		Iterator<ServerClient> itr = serverConn.clients.iterator();
	    while (itr.hasNext()) {
	    	ServerClient client = itr.next();
	    	client.sendCommand(command);
	    }		
	}	
	
	/**
	 * Передать команду пользователям
	 */
	public void sendCommandToPlayers(ArrayList<Player> players, Object command) {
		for(int i = 0; i < players.size(); i++) {
	    	ServerClient client = serverConn.findClient(players.get(i).playerRow.nick);
	    	client.sendCommand(command);			
		}
	}
		
	/**
	 * Передать команду всем клиентам за исключением текущего
	 */
	private void sendCmdToAllExclude(Object command) {
		Iterator<ServerClient> itr = serverConn.clients.iterator();
	    while (itr.hasNext()) {
	    	ServerClient client = itr.next();
	    	if(client != this) {
	    		client.sendCommand(command);
	    	}
	    }		
	}
}