/**
 * � Dmitry Boyko, 2016
 */

import java.awt.EventQueue;
import java.io.File;

import javax.swing.JOptionPane;

/**
 * ������� ���������� �� ��������� ������.
 */
public final class Arguments {
	public int users = 0;
	public boolean connect = false;
	public int sleep = 0;
	public boolean server = false;
	public boolean fullScreen = true;
	public boolean confirm = true;
	public boolean useTabs = false;
	public boolean randGroups = false;
	private int locX = -1;
	private int locY = -1;
	private int stepX = 200;
	private int stepY = 200;
	private int countX = 4;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame mainFrame = new MainFrame();
					if(isServerFile()) {
						mainFrame.arguments.setServerMode(mainFrame.connectPanel);
					}
					mainFrame.arguments.parse(mainFrame, args);
					mainFrame.postInitialize();
					mainFrame.arguments.generateClientFrame(mainFrame);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * ���������� �������, ��� ����������� ���� ������� 'game_server.jar'.
	 */
	static private boolean isServerFile() {
		String mainFile = Arguments.class.getProtectionDomain().getCodeSource().getLocation().getFile();
		if(!mainFile.isEmpty() && !mainFile.endsWith("/")) {
			File file = new File(mainFile);
			if(file.getName().equalsIgnoreCase("GAME_SERVER.JAR")) {
				return true;
			}
		}		
		return false;
	}
	
	/**
	 * �������������� ��������� �������
	 */
	private void generateClientFrame(MainFrame mainFrame) {
		int x = 0, y = 0, count = 0;
		for(int i = 0; i < users; i++) {
			MainFrame clientFrame = new MainFrame();
			copyParametersToClient(clientFrame.arguments);

			ConnectPanel connectPanel = clientFrame.connectPanel;
			connectPanel.txtNick.setText( connectPanel.txtNick.getText() + (i + 1) );
			connectPanel.txtLastName.setText("LastName"  + (i + 1));
			connectPanel.txtName.setText("Name"  + (i + 1));
			connectPanel.txtPatronymic.setText("Patronymic"  + (i + 1));
			
			clientFrame.setLocation(x, y);
			clientFrame.postInitialize();
			x += stepX;
			if(++count >= countX) {
				count = 0;
				x = 0;
				y += stepY;
			}
		}		
	}
	
	/**
	 * ����������� ���������� �������
	 */
	private void copyParametersToClient(Arguments clientArguments) {
		clientArguments.connect = connect;
		clientArguments.fullScreen = fullScreen;
		clientArguments.useTabs = useTabs;
	}
	
	/**
	 * ����������� ��������� ������
	 */
	public void parse(MainFrame mainFrame, String[] args) {
		for(int i = 0; i < args.length; i++) {
			String param = args[i];
			String value = "";
			
			int pos = param.indexOf('=');
			if(pos < 0) {
				pos = param.indexOf(':');
			}
			
			if(pos == 0) {
				value = param.substring(pos + 1);
				param = "";
			} else if(pos == param.length() - 1) {
				param = param.substring(0, pos);
			} else if(pos > 0) {
				value = param.substring(pos + 1);
				param = param.substring(0, pos);					
			}
			
			try {
				setParamValue(mainFrame, args[i], param, value);
			} catch (NumberFormatException eNumberFormat) {
				JOptionPane.showMessageDialog(null, "������ �������������� �����:" + value + " � ���������:" + param);
			}

			if(locX >= 0 && locY >= 0) {
				mainFrame.setLocation(locX, locY);
			}
			/*if(mainFrame.isFullScreen()) {
				mainFrame.setFullScreenMode(true);
			}*/
		}
	}
	
	/**
	 * ��������� ���������� �� ��������� �� ��������� ������
	 */
	public void setParamValue(MainFrame mainFrame, String arg, String param, String value) {
		ConnectPanel connectPanel = mainFrame.connectPanel;
		ConfigPanel configPanel = mainFrame.configPanel;
		switch(param.toUpperCase()) {
			case "SERVER":  // ����� �������
				if(value.compareToIgnoreCase("OFF") == 0 || value.compareToIgnoreCase("NO") == 0) {
					connectPanel.rbGuest.setVisible(true);
					connectPanel.rbHost.setVisible(true);
					break;
				} else if(value.isEmpty() || value.compareToIgnoreCase("ON") == 0) {
					setServerMode(connectPanel);
					break;
				}
				
			case "HOST-IP":  // ����
				if(!value.isEmpty()) {
					connectPanel.setHostIP(value);
				}
				break;
				
			case "PORT":  // ����� �����
				if(!value.isEmpty()) {
					int port = Integer.parseUnsignedInt(value);
					if(port > 0) {
						connectPanel.spnPort.setValue(port);
					}
				}
				break;
				
			case "NICK":  // ���
				if(!value.isEmpty()) {
					connectPanel.txtNick.setText(value);
				}
				break;
				
			case "CONNECT":  // ������������� ���������� ����������
				if(value.isEmpty() || value.compareToIgnoreCase("ON") == 0) {
					connect = true;
				}
				break;
				
			case "GAME-MODE":  // ������� ����� {0 - �� ���������� ������; 1 - �� ������� ������ }
				if(!value.isEmpty()) {
					int gameMode = Integer.parseUnsignedInt(value);
					if(gameMode > 1) {
						JOptionPane.showMessageDialog(null, "�������� '����� ����' ����� ��������� �������� ������ 0 ��� 1:" + arg);
					} else {
						configPanel.cbGameMode.setSelectedIndex(gameMode);
					}
				}
				break;
				
			case "PLAYERCOUNT":
				if(!value.isEmpty()) {
					configPanel.spnPlayerCount.setValue(Integer.parseUnsignedInt(value));
				}
				break;
				
			case "LIVES":  // ���-�� ������
				if(!value.isEmpty()) {
					int lives = Integer.parseUnsignedInt(value);
					if(lives > 0) {
						configPanel.spnLives.setValue(lives);
					}
				}
				break;
				
			case "SECONDS":  // ���-�� ������� �� ����������� ����
				if(!value.isEmpty()) {
					int seconds = Integer.parseUnsignedInt(value);
					if(seconds > 0) {
						configPanel.spnSeconds.setValue(seconds);
					}
				}
				break;
				
			case "ADDMOVE":
				if(!value.isEmpty()) {
					float addMove = Float.parseFloat(value);
					if(addMove < 0.0 || addMove > 1.0) {
						JOptionPane.showMessageDialog(null, "�������� '���.��� �� ������ �����' ����� ��������� �������� � ��������� �� 0 �� 1:" + arg);							
					} else {
						configPanel.spnAddMove.setValue(addMove);							
					}
				}
				break;
				
			case "GROUPS":  // ��������� ������������� ������� �� ������
				if(value.isEmpty() || value.compareToIgnoreCase("ON") == 0 || value.compareToIgnoreCase("YES") == 0) {
					configPanel.cbSplitOnGroups.setSelected(true);
				} else if(value.compareToIgnoreCase("OFF") == 0 || value.compareToIgnoreCase("NO") == 0) { 
					configPanel.cbSplitOnGroups.setSelected(false);
				}
				break;
			
			case "RAND_GROUPS":  // ��������� ������������ ������� � ������
				if(value.isEmpty() || value.compareToIgnoreCase("ON") == 0 || value.compareToIgnoreCase("YES") == 0) {
					randGroups = true;
				}
				break;
				
			case "MINGROUP":  // ����������� ����� ������� � ������
				if(!value.isEmpty()) {
					int minGroup = Integer.parseUnsignedInt(value);
					if(minGroup >= 3) {
						configPanel.spnMinGroup.setValue(minGroup);
					}
				}
				break;
				
			case "USERS":  // ���-�� ������������� ������������ �������
				if(!value.isEmpty()) {
					users = Integer.parseUnsignedInt(value);
				}
				break;
				
			case "DIR":  // ���� ��� ���������� ������
				if(!value.isEmpty()) {
					configPanel.txtDir.setText(value);
				}
				break;
				
			case "FULLSCREEN":  // ������������� �����
				if(value.compareToIgnoreCase("OFF") == 0 || value.compareToIgnoreCase("NO") == 0) {
					fullScreen = false;
				}
				break;
				
			case "TABS":  // �������������� ������������� ����������� �� ���������
				if(value.isEmpty() || value.compareToIgnoreCase("ON") == 0 || value.compareToIgnoreCase("YES") == 0) {
					useTabs = true;
				}
				break;
				
			case "SLEEP":  // ����� ����� �����������
				if(!value.isEmpty()) {
					sleep = Integer.parseUnsignedInt(value);
				}
				break;
				
			case "LOCX":  // ���������� X ������ ���� ������
				if(!value.isEmpty()) {
					locX = Integer.parseUnsignedInt(value);
				}
				break;
	
			case "LOCY":  // ���������� Y ������ ���� ������
				if(!value.isEmpty()) {
					locY = Integer.parseUnsignedInt(value);
				}
				break;
	
			case "CONFIRM":  // ����������� ������������� ��� ������ �� ���������
				if(value.compareToIgnoreCase("OFF") == 0 || value.compareToIgnoreCase("NO") == 0) {
					confirm = false;
				}
				break;
			
			case "STEPX":  // ��� �� ���������� X ��� �������������� ��������� ������� 
				if(!value.isEmpty()) {
					stepX = Integer.parseUnsignedInt(value);
				}
				break;
	
			case "STEPY":  // ��� �� ���������� Y ��� �������������� ��������� �������
				if(!value.isEmpty()) {
					stepY = Integer.parseUnsignedInt(value);
				}
				break;
	
			case "COUNTX":  // ���-�� ��������� ������� �� ���������� X ��� �������������� ��������� ������� 
				if(!value.isEmpty()) {
					countX = Integer.parseUnsignedInt(value);
				}
				break;
	
			default:
				JOptionPane.showMessageDialog(null, "�������������� �������� ��������� ������:" + arg);
				break;
		}		
	}

	/**
	 * 
	 */
	private void setServerMode(ConnectPanel connectPanel) {
		server = true;
		connectPanel.rbGuest.setVisible(true);
		connectPanel.rbHost.setVisible(true);
		connectPanel.rbHost.setSelected(true);
		connectPanel.btnHostButton();		
	}
}
