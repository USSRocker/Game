/**
 * © Dmitry Boyko, 2016
 */

import java.io.EOFException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Сетевой клиент.
 */
public class ClientConnection extends ObjectThread {
	private String nick;
	
	/**
	 * Конструктор 	
	 */
	ClientConnection(Socket socket, String nick, MainFrame mainFrame) {
		super(nick, mainFrame);
		this.socket = socket;
		this.nick = nick;
	}
	
	/**
	 * 	
	 */
	public void run() {
		boolean loop = true;
        if(!initStreams()) {
        	loop = false;
        } else if(!sendNick()) {
        	loop = false;
        }		
		while(loop) {
			Object infoCommand;
            try {
            	infoCommand = objInStream.readObject();
            	mainFrame.setMessage("");
			} catch(EOFException eEOF) {
				outMessage("Сервер разорвал соединение", null);
				break;            	
			} catch (ClassNotFoundException eClassNotFound) {
				outMessage("Ошибка поиска класса", eClassNotFound);
	        	continue;
			} catch (IOException eReadObject) {
				outMessage("Ошибка чтения класса", eReadObject);
				continue;
			}
            if(infoCommand == null) {
            	continue;
            	
	        } else if (infoCommand instanceof InfoNextMove) {
	        	InfoNextMove nextMove = (InfoNextMove)infoCommand;
	        	mainFrame.nextMove(nextMove);
	        	
	        } else if (infoCommand instanceof InfoKillPlayer) {
	        	mainFrame.playerPanel.killPlayerFromTable(((InfoKillPlayer)infoCommand).killNick);
	        	
	        } else if(infoCommand instanceof PlayerMove) {
	        	mainFrame.movesPanel.movesTableModel.addMove((PlayerMove)infoCommand);
	        	
            } else if(infoCommand instanceof InfoUserExists) {
            	outMessage("Ошибка. Пользователь с таким ником уже существует!", null);
            	break;
            	
            } else if(infoCommand instanceof InfoNewUser) {
            	mainFrame.playerPanel.addNewPlayer(((InfoNewUser)infoCommand).newUser);
            	
            } else if(infoCommand instanceof InfoRemoveUser) {
            	InfoRemoveUser infoRemoveUser = (InfoRemoveUser)infoCommand;
            	if(infoRemoveUser.removeUser.compareTo(mainFrame.connectPanel.txtNick.getText()) == 0) {
            		break;
            	} else {
            		mainFrame.playerPanel.removePlayer(infoRemoveUser.removeUser);
            	}
            	
            } else if(infoCommand instanceof InfoUserList) {
            	mainFrame.playerPanel.setPlayerList(((InfoUserList)infoCommand).users);
            	
            } else if (infoCommand instanceof InfoTimeout) {
            	InfoTimeout infoTimeout = (InfoTimeout)infoCommand;
            	mainFrame.setMessage("Превышено время ожидания хода: " + infoTimeout.showNick);
            	
	        } else if (infoCommand instanceof InfoStartGame) {
	        	mainFrame.startGame((InfoStartGame)infoCommand);
	        	
	        } else if (infoCommand instanceof InfoStopGame) {
	        	mainFrame.stopGame((InfoStopGame)infoCommand);
	        	
	        } else if (infoCommand instanceof InfoBreakGame) {
	        	mainFrame.breakGame();
	        	
	        } else if (infoCommand instanceof InfoAdmission) {
	        	mainFrame.connectPanel.setAdmission((InfoAdmission)infoCommand);

	        } else if (infoCommand instanceof InfoDisconnect) {
	        	break;

	        } else if (infoCommand instanceof InfoGameHasBegun) {
            	outMessage("Игра уже началась!", null);
	        	break;
            }
		}
		closeSocket();
    	mainFrame.setModeBegin();
	}
	
	/**
	 * 	
	 */
	public boolean sendNick() {
		return sendCommand(new InfoConnect(nick, mainFrame) );
	}
	
	/**
	 * 	
	 */
	public boolean sendDisconnect() {
		return sendCommand(new InfoDisconnect());
	}
}

/**
 * 
 */
class ClientUDP extends MessageThread {
	public boolean finished = false;
	
	ClientUDP(MainFrame mainFrame) {
		super("ClientUDP", mainFrame);
	}
	
	/**
	 * 	
	 */
	public void run() {	
		/*MulticastSocket socket = new MulticastSocket(4446);
		InetAddress group = InetAddress.getByName("203.0.113.0");
		socket.joinGroup(group);*/
		
		DatagramSocket socket = null;
		InetAddress inetAddress = null;
		try {
			inetAddress = InetAddress.getByName("0.0.0.0");
		} catch (UnknownHostException eAddress) {
			mainFrame.showException("Ошибка получение адреса", eAddress);
		}
		try {
			socket = new DatagramSocket((int)mainFrame.connectPanel.spnDistribution.getValue(), inetAddress);
			//socket.setSoTimeout(100);
			while(!finished) {
				try {
					DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
					socket.receive(packet);
					if(packet.getLength() == 0) {
						continue;
					}
					String message = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
					if(message.startsWith(MainFrame.msgHostIP)) {
						mainFrame.connectPanel.setHostIP( message.substring(MainFrame.msgHostIP.length()) );
						mainFrame.connectPanel.resetClientUDP();
						break;
					}
		        } catch (SocketTimeoutException eTimeout) {
		        	continue;
		        } catch (IOException except) {
		        	mainFrame.showException("Прием UDP пакета", except);
		        }
			}
		} catch (SocketException except) {
			mainFrame.showException("Открытие DatagramSocket", except);
		}
		if(socket != null) {
			socket.close();
		}
	}
}
