/**
 * � Dmitry Boyko, 2016
 */

import javax.swing.JPanel;
import javax.swing.JRadioButton;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import java.awt.Dimension;
import javax.swing.JComboBox;

/**
 * ������ "Connect"
 */
public class ConnectPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private MainFrame mainFrame;
	private ClientUDP clientUDP = null;
	public String showNick = "";
	
	public JComboBox<String> cmbHostIP;
	private JToggleButton btnHostIP;
	public JSpinner spnPort;
	public JTextField txtNick;
	public JRadioButton rbGuest;
	public JRadioButton rbHost;
	public JTextField txtLastName;
	public JTextField txtName;
	public JTextField txtPatronymic;
	private JButton btnConnect; 
	public JButton btnDisconnect;
	public JSpinner spnDistribution;
	public JToggleButton btnDistribution;	
	public JToggleButton btnAdmission;
	
	/**
	 * Create the panel.
	 */
	public ConnectPanel() {
		setSize(new Dimension(600, 325));
		setPreferredSize(new Dimension(600, 325));
		setMinimumSize(new Dimension(600, 325));
		ConnectPanel panel = this;		
		setLayout(null);
		
		JLabel lblHostIP = new JLabel("��� ����� ��� IP �����");
		lblHostIP.setBounds(10, 11, 244, 20);
		panel.add(lblHostIP);
		
		cmbHostIP = new JComboBox<String>();
		cmbHostIP.setEditable(true);
		lblHostIP.setLabelFor(cmbHostIP);
		cmbHostIP.setModel( new DefaultComboBoxModel<String>(new String[] {"127.0.0.1"}) );
		cmbHostIP.setBounds(264, 11, 161, 20);
		cmbHostIP.setInputVerifier(new InputVerifier() {
			@Override
			public boolean verify(JComponent input) {
				if(mainFrame.isFullScreen()) {
					JTextField field = (JTextField)input;
					if( field.getText().isEmpty() ) {
						mainFrame.setMessage("��������� ���� 'IP �����'");
						input.getToolkit().beep();
						return false;
					}
					mainFrame.setMessage("");
				}
				return true;
			}
		} );
		panel.add(cmbHostIP);
		
		btnHostIP = new JToggleButton("");
		btnHostIP.setToolTipText("��������� ���� ������� �� UDP ���������");
		btnHostIP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				pressButtonHostIP();
			}
		});
		btnHostIP.setIcon(new ImageIcon( getClass().getResource("globe-network.png")) );
		btnHostIP.setBounds(426, 8, 28, 25);
		panel.add(btnHostIP);
		
		JLabel lblPort = new JLabel("����");
		lblPort.setBounds(10, 36, 244, 20);
		panel.add(lblPort);
		
		spnPort = new JSpinner();
		lblPort.setLabelFor(spnPort);
		spnPort.setBounds(264, 36, 65, 20);
		spnPort.setModel(new SpinnerNumberModel(49149, 1, 49151, 1));
		spnPort.setInputVerifier(new InputVerifier() {
			@Override
			public boolean verify(JComponent input) {
				if(mainFrame.isFullScreen()) {
					JSpinner field = (JSpinner)input;
					if( (int)field.getValue() <= 0 ) {
						mainFrame.setMessage("��������� ���� '����'");
						input.getToolkit().beep();
						return false;
					}
					mainFrame.setMessage("");
				}
				return true;
			}
		} );
		panel.add(spnPort);
		
		JLabel lblNick = new JLabel("���");
		lblNick.setBounds(10, 62, 245, 20);
		panel.add(lblNick);
		
		txtNick = new JTextField();
		lblNick.setLabelFor(txtNick);
		txtNick.setBounds(265, 62, 161, 20);
		txtNick.setColumns(20);
		txtNick.setText( System.getProperty("user.name") );
		txtNick.setInputVerifier(new InputVerifier() {
			@Override
			public boolean verify(JComponent input) {
				if(mainFrame.isFullScreen()) {
					JTextField field = (JTextField)input;
					if( field.getText().isEmpty() ) {
						mainFrame.setMessage("��������� ���� '���'");
						input.getToolkit().beep();
						return false;
					}
					mainFrame.setMessage("");
				}
				return true;
			}
		} );		
		panel.add(txtNick);
		
		btnConnect = new JButton("���������� ����������");
		btnConnect.setIcon(new ImageIcon( getClass().getResource("plug--plus.png")) );
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnConnect();
			}	
		});
		
		rbGuest = new JRadioButton("������");
		rbGuest.setVisible(false);
		rbGuest.setBounds(10, 168, 190, 23);
		rbGuest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mainFrame.clearMessage();
				btnHostIP.setEnabled(true);
			}
		});
		rbGuest.setSelected(true);
		panel.add(rbGuest);
		
		rbHost = new JRadioButton("������");
		rbHost.setVisible(false);
		rbHost.setBounds(265, 171, 241, 20);
		rbHost.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnHostButton();
			}
		});
		panel.add(rbHost);

		ButtonGroup group = new ButtonGroup();		
		group.add(rbGuest);
		group.add(rbHost);

		btnConnect.setBounds(11, 198, 225, 23);
		panel.add(btnConnect);
		
		btnDisconnect = new JButton("��������� ������");		
		btnDisconnect.setIcon(new ImageIcon( getClass().getResource("plug--minus.png")) );
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(mainFrame.fullScreenMode || mainFrame.showConfirmDialog("�� ������������� ������ ��������� ����������?")) {
					btnDisconnect();
				}
			}
		});
		btnDisconnect.setEnabled(false);
		btnDisconnect.setBounds(264, 198, 224, 23);
		panel.add(btnDisconnect);
		
		JLabel lblDistribution = new JLabel("���� ��� �����������");
		lblDistribution.setEnabled(true);
		lblDistribution.setBounds(10, 250, 189, 20);
		panel.add(lblDistribution);
		
		spnDistribution = new JSpinner();
		spnDistribution.setModel(new SpinnerNumberModel(49150, 1, 49151, 1));
		spnDistribution.setBounds(264, 250, 65, 20);
		panel.add(spnDistribution);
		
		btnDistribution = new JToggleButton("������� �����������");
		btnDistribution.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(btnDistribution.isSelected()) {
					spnDistribution.setEnabled(false);
					mainFrame.startTimer();
				} else {
					spnDistribution.setEnabled(true);
					mainFrame.stopTimer();
				}
			}
		});
		btnDistribution.setEnabled(false);
		btnDistribution.setIcon(new ImageIcon( getClass().getResource("computer--arrow.png")) );
		btnDistribution.setToolTipText("�������� ����� �������� ����������� �� UDP ���������");
		btnDistribution.setBounds(10, 277, 226, 23);
		panel.add(btnDistribution);
		
		JLabel lblLastName = new JLabel("�������");
		lblLastName.setBounds(10, 93, 244, 17);
		panel.add(lblLastName);
		
		txtLastName = new JTextField();
		lblLastName.setLabelFor(txtLastName);
		txtLastName.setBounds(264, 90, 190, 20);
		txtLastName.setColumns(10);
		panel.add(txtLastName);
		
		JLabel lblName = new JLabel("���");
		lblName.setBounds(10, 118, 244, 17);
		panel.add(lblName);
		
		txtName = new JTextField();
		lblName.setLabelFor(txtName);
		txtName.setBounds(264, 115, 161, 20);
		txtName.setColumns(10);
		panel.add(txtName);
		
		JLabel lblPatronymic = new JLabel("��������");
		lblPatronymic.setBounds(10, 143, 244, 18);
		panel.add(lblPatronymic);
		
		txtPatronymic = new JTextField();
		lblPatronymic.setLabelFor(txtPatronymic);
		txtPatronymic.setBounds(264, 140, 161, 20);
		txtPatronymic.setColumns(10);
		panel.add(txtPatronymic);
				
		btnAdmission = new JToggleButton("�������� ����");
		btnAdmission.setIcon(new ImageIcon( getClass().getResource("flag-yellow.png")) );
		btnAdmission.setEnabled(false);
		btnAdmission.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mainFrame.clearMessage();
				if(isPressedAdmissionButton()) {
					// ���� ���������� ������ "�������� ����"
					if(!btnAdmission()) {
						btnAdmission.setSelected(false);
					}
				} else {
					// ���� ���������� ������ "������ ����"
					enabledAfterAdmission(true);
					mainFrame.serverConn.sendCommandToAll(new InfoAdmission(false));
				}
			}
		});
		btnAdmission.setBounds(264, 277, 224, 23);
		panel.add(btnAdmission);
	}
	
	/**
	 * 
	 */
	public void setMainFrame(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}
	
	/**
	 * ������ �����-������ "������"
	 */	
	public void btnHostButton() {
		mainFrame.clearMessage();
		resetClientUDP();
		btnHostIP.setEnabled(false);
		cmbHostIP.removeAllItems();
		try {
			InetAddress addr = InetAddress.getLocalHost();
			String hostName = addr.getHostName();
			cmbHostIP.addItem( hostName );  // addr.getHostAddress()
			InetAddress[] allAddreses = InetAddress.getAllByName(hostName);
			for(int i = allAddreses.length; --i >= 0;) {
				cmbHostIP.addItem( allAddreses[i].getHostAddress() );
			}
		} catch (UnknownHostException eUnknownHost) {
			mainFrame.showErrorDialog(eUnknownHost.getMessage());
		}         
	}
	
	/**
	 * ���������� �������� ���� Host-IP �� UDP ��������� 
	 */
	synchronized public void setHostIP(String hostIP) {
		cmbHostIP.removeAllItems();
		cmbHostIP.addItem(hostIP);
		cmbHostIP.setSelectedIndex(0);
	}
	
	/**
	 * 
	 */
	synchronized public String getHostIP() {
		return (String)cmbHostIP.getSelectedItem();
	}
	
	/**
	 * 
	 */
	public String getListHosts() {
		String hosts = "";
		for(int i = 0; i < cmbHostIP.getItemCount(); i++) {
			hosts += cmbHostIP.getItemAt(i) + ";";
		}
		return hosts;
	}
	
	/**
	 * 
	 */
	public void setListHosts(String separateHosts) {
		String[] hosts = separateHosts.split(";");
		cmbHostIP.removeAllItems();
		for(int i = 0; i < hosts.length; i++) {
			String host = hosts[i].trim();
			if(!host.isEmpty()) {
				cmbHostIP.addItem(host);
			}
		}
		if(cmbHostIP.getItemCount() == 0) {
			cmbHostIP.addItem("127.0.0.1");
		}
		cmbHostIP.setSelectedIndex(0);
	}
	
	/**
	 * ��������� ����� UDP ����������
	 */
	public void resetClientUDP() {
		if(clientUDP != null) {
			clientUDP.finished = true;
			clientUDP = null;
		}
		btnHostIP.setSelected(false);
		btnDistribution.setSelected(false);
	}
		
	/**
	 * ������ ������ �������� ���� ������� �� UDP ���������
	 */
	void pressButtonHostIP() { 
		if(btnHostIP.isSelected()) {
			clientUDP = new ClientUDP(mainFrame);
			clientUDP.start();
		} else {
			resetClientUDP();
		}
	}

	/**
	 * ������ ������ "Connect"
	 */
	public void btnConnect() {
		mainFrame.clearMessage();
		mainFrame.playerPanel.playerTableModel.removeAll();
		
		if( getHostIP().isEmpty() ) {
			mainFrame.showErrorDialog("��������� ���� 'IP �����'");
			return;
		} else if( (Integer)spnPort.getValue() == 0 ) {
			mainFrame.showErrorDialog("��������� ���� '����'");
			return;
		} else if( txtNick.getText().isEmpty() ) {
			mainFrame.showErrorDialog("��������� ���� '���'");
			return;
		} else if( txtNick.getText().contains(";") ) {
			mainFrame.showErrorDialog("����������� ������ ';' � ���� '���'");
			return;
		}
		
    	int port = (int)spnPort.getValue();
    	
    	if(rbHost.isSelected()) {  // ���� ������
            // Try to set up a server if host
    		ServerSocket hostServer;
            try {            		
                hostServer = new ServerSocket(port);
                hostServer.setSoTimeout(100);
            } catch (IOException eConnect) {
            	mainFrame.cleanUp("������ ��������� ���������� c '" + eConnect.getMessage() + "'");
            	return;
            }	        		
            mainFrame.serverConn = new ServerConnection(hostServer, mainFrame);
            enabledAfterConnect(false);
            mainFrame.serverConn.start();
            
    	} else {  // ���� �����
    		if(txtLastName.getText().isEmpty()) {
    			mainFrame.showErrorDialog("��������� ���� '�������'");
    			return;
    		} else if (txtName.getText().isEmpty()) {
    			mainFrame.showErrorDialog("��������� ���� '���'");
    			return;    			
    		} else if (txtPatronymic.getText().isEmpty()) {
    			mainFrame.showErrorDialog("��������� ���� '��������'");
    			return;
    		}
    		resetClientUDP();
    		
    		// If guest, try to connect to the server
    		if(mainFrame.arguments.sleep > 0) {
	        	try {
					Thread.sleep(mainFrame.arguments.sleep);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    		}
    		Socket socket;
            try {
            	socket = new Socket(getHostIP(), port);
            } catch (UnknownHostException eUnknownHost) {
            	mainFrame.cleanUp("�� ������� ����� ������ '" + eUnknownHost.getMessage() + "'");
            	return;
            } catch (IOException eConnect) {
            	mainFrame.cleanUp("������ ��� ����������� � ������� '" + eConnect.getMessage() + "'");
            	return;
            }
            mainFrame.clientConn = new ClientConnection(socket, txtNick.getText(), mainFrame);
            enabledAfterConnect(false);
            mainFrame.clientConn.start();
    	}
    	
    	if(rbHost.isSelected()) {
    		mainFrame.setTitle("������� ������. " + "� ������� �����, 2016.");
    	} else {
    		setPlayerTitle(txtNick.getText());
    		if(mainFrame.isFullScreen()) {
    			mainFrame.setFullScreenMode(true);
    		}
    	}
	}

	/**
	 * 
	 */
	public void setPlayerTitle(String nick) {
		showNick = nick;
		mainFrame.setTitle("�����: " + nick + ". " );
	}
	
	/**
	 * ������ ������ "Disconnect"
	 */
	synchronized public void btnDisconnect() {
		mainFrame.clearMessage();
		if(rbGuest.isSelected() && mainFrame.clientConn != null) {
			mainFrame.clientConn.sendDisconnect();
		}
		mainFrame.cleanUp("");
		mainFrame.setModeBegin();
	}
	
	/**
	 * ���������� ������� ������� ������ "������� �����" 
	 */
	synchronized public boolean isPressedAdmissionButton() {
		return btnAdmission.isSelected();
	}
	
	/**
	 * �������� ����� � ������ 
	 */
	synchronized public void setAdmission(InfoAdmission infoAdmission) {
		btnDisconnect.setEnabled(!infoAdmission.isOver);			
	}

	/**
	 * ������ ������ "�������� ����"
	 */
	private boolean btnAdmission() {
		// ���� ���������� ������ "�������� ����"
		if(mainFrame.playerPanel.playerTableModel.getRowCount() < mainFrame.minGroupValue()) {
			mainFrame.showErrorDialog("��� ������� ���� ���������� �� ����� " + mainFrame.minGroupValue() + " �������.");
			btnAdmission.setSelected(false);
			return false;
		}
		resetClientUDP();
		enabledAfterAdmission(false);
		mainFrame.serverConn.sendCommandToAll(new InfoAdmission(true));
		return true;
	}
		
	/**
	 * ���������� ��������� ����� ������� ������ "�������� ����"
	 */
	public void enabledAfterAdmission(boolean enabled) {
		btnDisconnect.setEnabled(enabled);
		btnDistribution.setEnabled(enabled);
		spnDistribution.setEnabled(enabled);
		mainFrame.modesPanel.btnNewGame.setEnabled(!enabled);
		//btnAdmission.setText((enabled ? "�������� ����" : "������ �����"));
	}

	/** 
	 * ���������� ���������� ��������� ����� ������� ������ Connect/Disconnect
	 */
	public void enabledAfterConnect(boolean enabled) {
		btnConnect.setEnabled(enabled);				
        btnDisconnect.setEnabled(!enabled);
		cmbHostIP.setEditable(enabled);
		spnPort.setEnabled(enabled);
		if(!rbHost.isSelected()) {
			txtNick.setEditable(enabled);
		}
		txtLastName.setEditable(enabled);
		txtName.setEditable(enabled);
		txtPatronymic.setEditable(enabled);
		rbGuest.setEnabled(enabled);
		rbHost.setEnabled(enabled);
		if(rbHost.isSelected()) {
			mainFrame.enabledAfterStartGame(!enabled, true);
        	btnAdmission.setEnabled(!enabled);
        	btnHostIP.setEnabled(false);
        } else {
        	btnHostIP.setEnabled(enabled);
        }
	}
	
	/**
	 * ���������� ��������� ����� ������� ������ "����� ����"
	 */
	public void enabledAfterStartGame(boolean enabled, boolean enabledNick) {
		spnDistribution.setEnabled(enabled);
		btnDistribution.setEnabled(enabled);
		btnAdmission.setEnabled(enabled);
		/*if(rbHost.isSelected()) {
			txtNick.setEditable(enabled);
		} else if(rbHost.isSelected() && !enabled && enabledNick) {
			txtNick.setEditable(!enabled);
		}*/
	}
}
