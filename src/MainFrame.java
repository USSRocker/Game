/**
 * � Dmitry Boyko, 2016
 */

import java.awt.BorderLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import java.awt.Dimension;
import java.awt.DisplayMode;

import javax.swing.Timer;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Date;
import java.awt.event.ActionEvent;
import javax.swing.border.BevelBorder;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Component;
import java.awt.Toolkit;
import javax.swing.JTabbedPane;

public class MainFrame extends JFrame {	
	private static final long serialVersionUID = 1L;
	public static final String msgHostIP = "HOST-IP:";
	
	public Arguments arguments = null;
	public ServerConnection serverConn = null;
	public ClientConnection clientConn = null;
	public FileStorage fileStorage = null;
	private Timer timer = null;
	private Date startMove;
	public boolean fullScreenMode = false;
	private String previousNick = "";
	
	private JPanel panel;
	public ConnectPanel connectPanel;
	public ConfigPanel configPanel;
	public ModesPanel modesPanel;
	public PlayerPanel playerPanel;
	public MovesPanel movesPanel;

	private JLabel lblMessage;
	
	/**
	 * Create the frame.
	 */
	public MainFrame() {
		arguments = new Arguments();

		setTitle("� ������� �����, 2016. ����.");
		setMinimumSize(new Dimension(1024, 600));
		setBounds(100, 100, 1024, 781);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);  // EXIT_ON_CLOSE		
		setIconImage(Toolkit.getDefaultToolkit().getImage( getClass().getResource("puzzle.png") ));
		setPreferredSize(new Dimension(1024, 600));
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				if(arguments.confirm && (fullScreenMode || !showConfirmDialog("������� ����?")) ) {
					return;
				}
				connectPanel.btnDisconnect();
                event.getWindow().setVisible(false);
                System.exit(0);
			}
		});
		
		JPanel contentPane = new JPanel();
		contentPane.setAutoscrolls(true);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JSplitPane splitMain = new JSplitPane();
		splitMain.setAutoscrolls(true);
		splitMain.setAlignmentX(Component.RIGHT_ALIGNMENT);
		splitMain.setResizeWeight(0.5);
		contentPane.add(splitMain, BorderLayout.CENTER);
		
		panel = new JPanel();
		panel.setAutoscrolls(true);
		panel.setSize(new Dimension(450, 100));
		panel.setPreferredSize(new Dimension(635, 400));
		panel.setMinimumSize(new Dimension(610, 400));
		splitMain.setLeftComponent(panel);
		panel.setLayout(null);
		
		connectPanel = new ConnectPanel();
		connectPanel.setMainFrame(this);
		connectPanel.setBounds(10, 5, 600, 325);		
		panel.add(connectPanel);
		
		configPanel = new ConfigPanel();
		configPanel.setMainFrame(this);
		configPanel.setBounds(10, 335, 600, 325);				
		panel.add(configPanel);
		
		modesPanel = new ModesPanel();
		modesPanel.setMainFrame(this);
		modesPanel.setBounds(10, 660, 600, 280);
		panel.add(modesPanel);

    	GraphicsDevice graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        DisplayMode displayMode = graphicsDevice.getDisplayMode();
        if(displayMode.getHeight() < 1024) {
        	setTabbedPane();
        }
		
		JSplitPane splitRight = new JSplitPane();
		splitRight.setMinimumSize(new Dimension(385, 500));
		splitRight.setPreferredSize(new Dimension(400, 300));
		splitRight.setResizeWeight(0.5);
		splitRight.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitMain.setRightComponent(splitRight);

		playerPanel = new PlayerPanel();
		playerPanel.init(this);
		splitRight.setLeftComponent(playerPanel);

		movesPanel = new MovesPanel();
		movesPanel.init(this);
 		splitRight.setRightComponent(movesPanel);
		
		lblMessage = new JLabel("");
		lblMessage.setPreferredSize(new Dimension(100, 18));
		lblMessage.setMinimumSize(new Dimension(14, 100));
		lblMessage.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblMessage.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		lblMessage.setBounds(0, 0, 447, 17);
		contentPane.add(lblMessage, BorderLayout.SOUTH);
	}	

	/**
	 * ���������� ������ � ����������
	 */
	public void setTabbedPane() {
    	panel.remove(connectPanel);
    	panel.remove(configPanel);
    	panel.remove(modesPanel);
    	
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setPreferredSize(new Dimension(600, 350));
		tabbedPane.setBounds(5, 11, 600, 350);		
		panel.add(tabbedPane);
		
		tabbedPane.addTab("����������", null, connectPanel, null);   	
		tabbedPane.addTab("��������� ����", null, configPanel, null);
		tabbedPane.addTab("����������� �������", null, modesPanel, null);
	}
	
	/**
	 * ���� ������������� �������� ����
	 */
	public void postInitialize() {
		if(arguments.useTabs) {
			setTabbedPane();
		}
		setVisible(true);							
		fileStorage = new FileStorage(this);
		if(arguments.connect) {
			connectPanel.btnConnect();
		}
	}

	/**
	 * ���������� �������������� �����
	 */
	synchronized public void setModeBegin() {
        if(connectPanel.rbHost.isSelected()) {
        	serverConn = null;
        } else {
        	clientConn = null;
        }
        connectPanel.enabledAfterConnect(true);
        
		if(isFullScreen()) {
			setFullScreenMode(false);
		}		        
	}

	/**
	 * ������ ������ "����� ����" �� �������
	 */
	public boolean btnStartGame() {
		// ���� ���������� ������ "����� ����"
		if(configPanel.cbSplitOnGroups.isSelected() && minGroupValue() > playerPanel.getPlayerCount()) {
			showErrorDialog("�������� ���� '����������� ������' �� ������ ���� ������ ���������� �������!");
			return false;
		}
		if(playerPanel.getPlayerCount() <= (int)configPanel.spnPlayerCount.getValue()) {
			showErrorDialog("�������� ���� '" + getPlayerCountText() + "' �� ������ ���� ������ ���������� �������!");
			return false;
		}
		String dir = configPanel.txtDir.getText();
		if(!dir.isEmpty()) {
			File file = new File(dir);
			if(!file.isDirectory()) {
				setMessage("���� ��� ���������� ������ �� �������� �����������: " + dir);
				return false;
			} else if(!file.exists()) {
				setMessage("���������� ��� ���������� ������ �� ����������: " + dir);
				return false;					
			}
		}	
		if(!playerPanel.setPlayerNames()) {
			return false;
		}
		movesPanel.movesTableModel.rows.clear();
		movesPanel.movesTableModel.fireTableDataChanged();
		enabledAfterStartGame(false, false);
		configPanel.spnMinGroup.setEnabled(false);
		playerPanel.btnMove.setEnabled(false);
		if(isSplitOnGroups()) {
			playerPanel.playerTableModel.randomizeGroups(minGroupValue());
		}
		serverConn.initGame(playerPanel.playerTableModel.rows);
		movesPanel.progressBar.setMaximum(getSeconds());
		startTimer();
		return true;
	}

	/**
	 * ���������� ��������� ����� ������� ������ "����� ����"
	 */
	public void enabledAfterStartGame(boolean enabled, boolean enabledNick) {
		connectPanel.enabledAfterStartGame(enabled, enabledNick);
		configPanel.enabledAfterStartGame(enabled);
		modesPanel.enabledAfterStartGame(enabled);
	}
	
	/**
	 * ������ ������ "������� ���"
	 */		
	public void btnMove() {
		clearMessage();
		int row = playerPanel.tblPlayers.getSelectedRow();
		if(row < 0) {
			showWarning("�������� ���� ������ � �������!");
			return;
		}
		PlayerRow playerRow = playerPanel.getPlayerRow(row);
		String killNick = playerRow.nick;
		if(killNick.equals(connectPanel.txtNick.getText())) {
			showWarning("������ �������� ����!");
			return;			
		}
		if(playerRow.lives <= 0) {
			showWarning("������ �������� ������ �� �������� ������!");
			return;						
		}
		playerPanel.btnMove.setEnabled(false);
		clientConn.sendCommand(new InfoKillPlayer(playerRow));
	}

	/**
	 * ����� ���� � ������ 
	 */
	synchronized public void startGame(InfoStartGame startGame) {
		previousNick = "";
		configPanel.startGame(startGame);
		modesPanel.startGame(startGame);
		movesPanel.movesTableModel.rows.clear();
		movesPanel.movesTableModel.fireTableDataChanged();
		movesPanel.progressBar.setMaximum(startGame.seconds);
		playerPanel.playerTableModel.initPlayers(startGame, this);
		fileStorage.startGame = new Date();
	}
	
	/**
	 * ��������� ��� � �������
	 */
	synchronized public void nextMove(InfoNextMove nextMove) {
		String startMsg = (nextMove.round == 1 && nextMove.activePlayer == 0 ? "����� ����." : "");
		setMessage(startMsg + " �����: " + nextMove.round + " �����: " + nextMove.activeShowNick);
		int pos = playerPanel.playerTableModel.findPlayerPosition(nextMove.activeNick);
		
		PlayerRow playerRow = playerPanel.playerTableModel.rows.get(pos);
		if(!previousNick.isEmpty() && !previousNick.contentEquals(nextMove.activeNick)) {
			PlayerRow prevPlayerRow = playerPanel.playerTableModel.findPlayer(previousNick);
			prevPlayerRow.killByCycle = 0;
		}
		previousNick = nextMove.activeNick;
		playerRow.bonusMoves = nextMove.addMove;
		playerPanel.playerTableModel.fireTableDataChanged();
		playerPanel.setTableSelection(pos);
		
		startMove = new Date();		
		movesPanel.progressBar.setValue(0);
		if(connectPanel.rbGuest.isSelected() && nextMove.activeNick.equals(connectPanel.txtNick.getText())) {
			playerPanel.btnMove.setEnabled(true);
			startTimer();
		}		
	}

	/**
	 * ���������� ������
	 */
	synchronized public void startTimer() {
		ActionListener taskPerformer = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if(fullScreenMode) {
					toFront();
				}
				if(connectPanel.rbHost.isSelected() && serverConn != null && !connectPanel.isPressedAdmissionButton()) {
					try {
						byte[] data = (msgHostIP + connectPanel.getHostIP()).getBytes("UTF-8");
						InetAddress addr = InetAddress.getByName("255.255.255.255");
						DatagramPacket packet = new DatagramPacket(data, data.length, addr, (int)connectPanel.spnDistribution.getValue());
						DatagramSocket socket = new DatagramSocket();
						socket.setBroadcast(true);
						try {
							socket.send(packet);
						} catch (IOException eSend) {
							showException("�������� UDP ������", eSend);
						}
						socket.close();
					} catch (IOException except) {
						showException("������� UDP ���������", except);
					}
				}
				if(playerPanel.btnMove.isEnabled()) {
					Date currDate = new Date();
					int thinkTime = (int)( (currDate.getTime() - startMove.getTime()) / 1000 );
					movesPanel.progressBar.setValue(thinkTime);
					if(thinkTime > getSeconds()) {
						setMessage("��������� ����� �������� ����!");
						if(clientConn != null) {
							clientConn.sendCommand(new InfoTimeout(connectPanel.showNick));
						}
						return;
					}
				} else { 
					movesPanel.progressBar.setValue(0);
				}
				timer.restart();
			}
		};		
		if(timer == null) {
			timer = new Timer(500, taskPerformer);
		}
		timer.start(); 		
	}
	
	/*
    InetAddress getBroadcastAddress() throws IOException {
    	Context ctx = Thread.getApplicationContext();
        WifiManager wifi = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        if(dhcp == null)
            return InetAddress.getByName("255.255.255.255");
        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }*/
    
	/**
	 * ���������� ���� ��������
	 */	
	synchronized public void breakGame() {
		playerPanel.btnMove.setEnabled(false);
		stopTimer();
		setMessage("���� �������� ��������.");
	}
		
	/**
	 * ������� ������� ������ ���� "�� ����������"
	 */	
	synchronized public boolean isToEndMode() {
		if(configPanel.cbGameMode.getSelectedIndex() == 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * ������ ����� ����� �� ������ ����
	 */
	public String getPlayerCountText() {
		return (isToEndMode() ? "���-�� ���������� �������" : "���-�� �������� �������");
	}

	/**
	 * ������� ������� �������� �� ������ 
	 */
	synchronized public boolean isSplitOnGroups() { 
		return configPanel.cbSplitOnGroups.isSelected();
	}
	
	/**
	 * 
	 */
	synchronized public int minGroupValue() {
		return (int)configPanel.spnMinGroup.getValue();
	}
	
	/**
	 * ������ ���������� ������ � ����
	 */	
	public int getLives() { 
		return (int)configPanel.spnLives.getValue();
	}
	
	/**
	 * ������ ���������� ������ �� ���
	 */	
	public int getSeconds() {
		return (int)configPanel.spnSeconds.getValue();
	}
	
	/**
	 * ����� ���� � ������
	 */	
	synchronized public void finishedGroup(Group group, String text, PlayerRow player) {
		setMessage("���� ������ " + group.numOfGroup + " ���������. " + text + " " + player.showNick);
	}
	
	/**
	 * 
	 */	
	synchronized public void stopGame(InfoStopGame stopGame) {
		stopTimer();
		movesPanel.progressBar.setValue(0);
		setMessage("���� ���������. " + stopGame.text + " " + stopGame.player.showNick);
	}
	
	/**
	 * ����� ���� �� �������
	 */	
	synchronized public void finishedGame(String text, PlayerRow player) {
		finishServerGame();
		modesPanel.btnNewGame.setSelected(false);
		setMessage("���� ���������. " + text + " " + player.showNick);
	}
	
	/**
	 * 
	 */
	public void finishServerGame() {
    	enabledAfterStartGame(true, false);
    	configPanel.spnMinGroup.setEnabled(configPanel.cbSplitOnGroups.isSelected());
		stopTimer();		
	}
	
	/**
	 * ���������� ������
	 */
	public void stopTimer() {
		if(timer != null) {
			timer.stop();
		}		
		movesPanel.progressBar.setValue(0);
	}
	
	/**
	 * ������������ �������� ��� ������ �� ���� 
	 */	
	//@SuppressWarnings("deprecation")
	public void cleanUp(String msg) {
		stopTimer();
		connectPanel.resetClientUDP();
		if(msg != null && !msg.isEmpty()) {
			showWarning(msg);			
		}
		if(serverConn != null)
		{
			serverConn.finished = true;
			try {
				Thread.sleep(200);
			} catch (InterruptedException eInterrupted) {
				setMessage("������ ��������� ������: " + eInterrupted.getMessage());
			}
			serverConn = null;
		}
		clientConn = null;
	}	

	/**
	 * ���������� ������������� ����� 
	 */
	public void setFullScreenMode(boolean fullScreen) {
		fullScreenMode = fullScreen;
        GraphicsDevice graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		if(!graphicsDevice.isFullScreenSupported()) {
			return;
		}
        if (!isDisplayable()){
        	setUndecorated(fullScreen);           
        }
        if(fullScreen) {
    		setResizable(false);
        	graphicsDevice.setFullScreenWindow(this);
        } else {
        	graphicsDevice.setFullScreenWindow(null);
    		setResizable(true);
        }
        if(isAlwaysOnTopSupported()) {
        	setAlwaysOnTop(fullScreen);
        }
	}

	/**
	 * ������� ������� ������ �������������� ������
	 */
	public boolean isFullScreen() {
		return arguments.fullScreen && connectPanel.rbGuest.isSelected();
	}
	
	/**
	 * ������� ��������������
	 */
	public void showWarning(String warning) {
		if(fullScreenMode) {
			setMessage(warning);
		} else {
			JOptionPane.showMessageDialog(this, warning, "��������������", JOptionPane.WARNING_MESSAGE);
		}
	}

	/**
	 * ������� ��������� �� ������
	 */
	public void showErrorDialog(String error) {
		if(fullScreenMode) {
			setMessage("������: " + error);
		} else {
			JOptionPane.showMessageDialog(this, error, "������", JOptionPane.ERROR_MESSAGE);
		}	
	}

	/**
	 * �������� ���������� ���� � ������� �������������
	 */
	public boolean showConfirmDialog(String msg) {
		Object[] options = { "��", "���!" };
        int n = JOptionPane.showOptionDialog(this, msg, "�������������", JOptionPane.YES_NO_OPTION,
        		JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
        return n == 0;
	}	

	/**
	 * ������� ������ � �����������
	 */
	synchronized public void showException(String msg, Exception except) {
		lblMessage.setText( (msg.isEmpty() ? "" : msg + ": ") + except.getMessage() );
		lblMessage.setToolTipText( except.getStackTrace().toString() );
	}
	
	/**
	 * �������� ������ ���������
	 */
	synchronized public void clearMessage() {
		lblMessage.setText("");
		lblMessage.setToolTipText("");
	}
	
	/**
	 * ������� ����� � ������ ���������
	 */
	synchronized public void setMessage(String msg) {
		lblMessage.setText(msg);
		lblMessage.setToolTipText(msg);
	}	
}
