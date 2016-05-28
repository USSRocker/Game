/**
 * � Dmitry Boyko, 2016
 */

import java.nio.file.*;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import javax.swing.JOptionPane;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

import static java.nio.file.StandardOpenOption.*;

/**
 * ��������� ������� ����.
 */
public final class FileStorage {
	static private final String version = " v1.30";
	static private final String ext = ".csv";
	
	public Date startGame;
	public Date finishGame;
	public String victor;
	public String showVictor;
	public String loser;
	public String showLoser;
	
	private int counter = 0;
	private MainFrame mainFrame;
	private Group group;
	
	static private FileSystem fileSystem;
	static private Locale local;
	static private String codePage = "WINDOWS-1251";  // "UTF-8"
	static private DateFormat dateFormat;
	static private DateFormat timeFormat;
	static private String splitter = ";"; // "\t";

	static private String baseNameByEnd = "GameLogByEnd";
	static private String baseNameByFirst = "GameLogByFirst";
	static private String resultHeadByEnd;
	static private String resultHeadByFirst;
	static private String castingHead;
	static private String movesHead;
	
	/**
	 * ����������� ������������� ������.
	 */	
	static {
		fileSystem = FileSystems.getDefault();
		
		local = new Locale("ru","RU");
		dateFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, local); 
		timeFormat = DateFormat.getTimeInstance(DateFormat.DEFAULT, local);
		
		String resultHead = 
				"����� ����" + splitter +
				"������" + splitter +
				"���� ������ ����" + splitter + 
				"����� ������ ����" + splitter + 
				"���������������� ����" + splitter + 
				"���-�� ������������������ �������" + splitter + 
				"����� ����" + splitter +
				"������ �� ���-�� ����������/�������� �������" + splitter +
				"��������� ���-�� ������" + splitter + 
				"����� �� ���" + splitter +
				"���.��� �� ������ �����" + splitter +
				"������ �� ������" + splitter +
				"����������� ������" + splitter;
		resultHeadByEnd   = resultHead      + "����������" + "\n";
		resultHeadByFirst = resultHeadByEnd + "�����������" + "\n";
		
		castingHead = 
				"����� ����" + splitter +
				"������" + splitter +
				"���������� �����" + splitter +
				"��� ������" + splitter +
				"������" + splitter +
				"���" + splitter +
				"��������" + splitter +
				"��� ����������" + splitter +
				"��� ������������" + splitter +
				"������������ ���" + splitter +
				"\n";
		
		movesHead = 
				"����� ����" + splitter +
				"������" + splitter +
				"����� ����" + splitter +
				"��� ������" + splitter +
				"������������ ��� ������" + splitter +
				"���-�� ������ ������" + splitter +
				"������ �����" + splitter +
				"������������ ��� ������� ������" + splitter +
				"���-�� ������ ������� ������" + splitter +
				"���� ������ ����" + splitter +
				"����� ������ ����" + splitter +
				"���������������� ����" + splitter +
				"\n";
	}
	
	/**
	 * ����������� 
	 */
	FileStorage(MainFrame aMainframe) {
		mainFrame = aMainframe;
	}
	
	/**
	 * ���������������� �� ����� ����
	 */
	public void init() {
		startGame = new Date();
		counter = 0;
	}
	
	/**
	 * ��������� ��� ���������� ������ ����.
	 */	
	synchronized public void Save(Group group) {
		this.group = group;
		String baseName;
		if(mainFrame.isToEndMode()) {
			baseName = baseNameByEnd;
		} else {
			baseName = baseNameByFirst;
		}
		
		if(counter == 0 && !saveCounter(baseName)) {
			return;
		}
		saveResult(baseName);
		saveCasting(baseName);
		saveMoves(baseName);
	}
	
	/**
	 * ��������� ��������� ������� ����.
	 */	
	private void saveResult(String baseName) {
		ConfigPanel configPanel = mainFrame.configPanel;
		
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(toUnsignedString(counter) + splitter);             // ����� ����
		stringBuilder.append(toUnsignedString(group.numOfGroup) + splitter);    // ������

		stringBuilder.append(dateFormat.format(startGame) + splitter);          // ���� ������ ����
		stringBuilder.append(timeFormat.format(startGame) + splitter);          // ����� ������ ����
		long gameTime = (finishGame.getTime() - startGame.getTime()) / 1000;	
		stringBuilder.append(toUnsignedString((int)gameTime) + splitter);       // ���������������� ����
		
		stringBuilder.append(toUnsignedString(mainFrame.playerPanel.getPlayerCount()) + splitter);  // ���������� �������
		
		String txtGameMode = ConfigPanel.arrayGameMode[configPanel.cbGameMode.getSelectedIndex()];
		stringBuilder.append(txtGameMode + splitter);  // ������� �����
		stringBuilder.append(toUnsignedString((int)configPanel.spnPlayerCount.getValue()) + splitter);  // ���-�� ����������/�������� �������
		
		stringBuilder.append(toUnsignedString(mainFrame.getLives()) + splitter);        // ���������� ������
		stringBuilder.append(toUnsignedString(mainFrame.getSeconds()) + splitter);      // ����� �� ���

		float addMove = (float)configPanel.spnAddMove.getValue();
		NumberFormat numberFormat = NumberFormat.getNumberInstance(); 
		numberFormat.setMaximumFractionDigits(2); 
		//DecimalFormat myFormatter = new DecimalFormat(pattern);
		stringBuilder.append(numberFormat.format(addMove) + splitter);  // ����������� ����
		
		if(configPanel.cbSplitOnGroups.isSelected()) {  
			stringBuilder.append("��" + splitter);   // ��������� �� ������
			stringBuilder.append(toUnsignedString((int)configPanel.spnMinGroup.getValue()) + splitter);  // ����������� ������
		} else {
			stringBuilder.append("���" + splitter);  // ��������� �� ������
			stringBuilder.append(splitter);          // ����������� ������
		}
		
		if(mainFrame.isToEndMode()) {
			stringBuilder.append(victor + splitter);      // ����������
			stringBuilder.append(showVictor + splitter);  // ����������
		} else {
			stringBuilder.append(loser + splitter);       // �����������
			stringBuilder.append(showLoser + splitter);   // �����������
		}
		
		stringBuilder.append("\n");

		String resultHead;
		if(mainFrame.isToEndMode()) {
			resultHead = resultHeadByEnd;
		} else {
			resultHead = resultHeadByFirst;
		}

		Path path = fileSystem.getPath(configPanel.txtDir.getText(), baseName + "-Result" + version + ext);
		save(path, stringBuilder.toString(), resultHead);
	}
	
	/**
	 * ��������� ���������� ����.
	 */	
	private void saveCasting(String baseName) {
		StringBuilder stringBuilder = new StringBuilder();
		Iterator<PlayerRow> itr = mainFrame.playerPanel.getTableIterator();
	    while (itr.hasNext()) {
	    	PlayerRow player = itr.next();
	    	if(player.group != group.numOfGroup) {
	    		continue;
	    	}
	    	stringBuilder.append(toUnsignedString(counter) + splitter);        // ����� ����
	    	stringBuilder.append(toUnsignedString(player.group) + splitter);   // ������
	    	stringBuilder.append(toUnsignedString(player.number) + splitter);  // ���������� �����
	    	stringBuilder.append(player.nick + splitter);                      // ��� ������
	    	stringBuilder.append(player.lastName + splitter);                  // ������� 
	    	stringBuilder.append(player.name + splitter);                      // ���
	    	stringBuilder.append(player.patronymic + splitter);                // ��������
	    	stringBuilder.append(player.computerName + splitter);              // ��� ����������
	    	stringBuilder.append(player.userName + splitter);                  // ��� ������������	    	
	    	stringBuilder.append(player.showNick + splitter);                  // ������������ ���	    	
			stringBuilder.append("\n");
	    }		
		
		Path path = fileSystem.getPath(baseName + "-Casting" + version + ext);
		save(path, stringBuilder.toString(), castingHead);
	}
	
	/**
	 * ��������� ���� ����.
	 */	
	private void saveMoves(String baseName) {
		StringBuilder stringBuilder = new StringBuilder();
		for(int i = 0; i < group.playerMoves.size(); i++) {
			PlayerMove playerMove = group.playerMoves.get(i);
	    	stringBuilder.append(toUnsignedString(counter) + splitter);           // ����� ����
	    	stringBuilder.append(toUnsignedString(group.numOfGroup) + splitter);  // ����� ������
	    	stringBuilder.append(toUnsignedString(i + 1) + splitter);             // ����� ����
	    	
	    	stringBuilder.append(playerMove.movedPlayer.nick + splitter);                     // ��� ������
	    	stringBuilder.append(playerMove.movedPlayer.showNick + splitter);                 // ������������ ��� ������
	    	stringBuilder.append(toUnsignedString(playerMove.movedPlayer.lives) + splitter);  // ���-�� ������ ������
	    	
	    	stringBuilder.append(playerMove.killedPlayer.nick + splitter);                     // ��� ������� ������
	    	stringBuilder.append(playerMove.killedPlayer.showNick + splitter);                 // ������������ ��� ������� ������
	    	stringBuilder.append(toUnsignedString(playerMove.killedPlayer.lives) + splitter);  // ���-�� ������ ������� ������

	    	long gameTime = (playerMove.finishDate.getTime() - playerMove.startDate.getTime()) / 1000;	
			stringBuilder.append(dateFormat.format(playerMove.startDate) + splitter);   // ���� ������ ����
			stringBuilder.append(timeFormat.format(playerMove.startDate) + splitter);   // ����� ������ ����
	    	stringBuilder.append(toUnsignedString((int)gameTime) + splitter);           // ���������������� ����

	    	stringBuilder.append("\n");
		}
		
		Path path = fileSystem.getPath(baseName + "-Moves" + version + ext);
		save(path, stringBuilder.toString(), movesHead);
	}
	
	/**
	 * ��������� ������ � ����� �����.
	 */	
	private void save(Path path, String data, String header) {
		FileChannel fileChannel;
		try {
			fileChannel = FileChannel.open(path, APPEND, CREATE, SYNC);
		} catch (IOException eOpen) {
			showError("������ �������� �����", path, eOpen);
			return;
		}
	
		if(lockFile(fileChannel, path)) {
			saveString(fileChannel, path, data, header);
		}
		
		try {
			fileChannel.close();
		} catch (IOException eClose) {
			showError("������ �������� �����", path, eClose);
		}
	}
	
	/**
	 * ��������� ������ �� ������.
	 */	
	private boolean saveString(FileChannel fileChannel, Path path, String data, String header) {
		long size = 0L;
		try {
			size = fileChannel.size();
		} catch (IOException eSize) {
			showError("������ ������ ������� �����", path, eSize);
			return false;
		}
		if(size == 0L) {
			data = header + data;
		}
		
 		CharBuffer charBuffer = CharBuffer.allocate(data.length());
		charBuffer.clear();
		charBuffer.put(data);
		charBuffer.flip();
		
		ByteBuffer byteBuffer = ByteBuffer.allocate(data.length() * 2);	
		byteBuffer.clear();
		
		CharsetEncoder encoder = Charset.forName(codePage).newEncoder();
		byteBuffer.clear();
		encoder.encode(charBuffer, byteBuffer, false);
		byteBuffer.flip();
	
		try {
			fileChannel.write(byteBuffer);
		} catch (IOException eWrite) {
			showError("������ ������ �����:", path, eWrite);
			return false;
		}
		
		try {
			fileChannel.force(true);
		} catch (IOException eForce) {
			showError("������ ������ ������ �� ����", path, eForce);
			return false;
		}	
		return true;		
	}
		
	/**
	 * ��������� ������� ��� � ����������� �����.
	 */	
	private boolean saveCounter(String baseName) {
		Path path = fileSystem.getPath(baseName + "-Counter.csv");
		
		FileChannel fileChannel;
		try {
			fileChannel = FileChannel.open(path, READ, WRITE, CREATE, SYNC);
		} catch (IOException eOpen) {
			showError("������ �������� �����", path, eOpen);
			return false;
		}
	
		boolean result = false;
		if(lockFile(fileChannel, path)) {
			result = changeCounter(fileChannel, path);
		}
		
		try {
			fileChannel.close();
		} catch (IOException eClose) {
			showError("������ �������� �����", path, eClose);
			return false;
		}
		return result;
	}
	
	/**
	 * ����������� ����.
	 */	
	private boolean lockFile(FileChannel fileChannel, Path path) {
		int i = 0;
		while(true) {
			try {
				fileChannel.lock();
				break;
			} catch (IOException eLock) {
				if(++i < 10) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
					continue;
				}
                Object[] options = { "��", "���!" };
                int n = JOptionPane.showOptionDialog(mainFrame, "��������� �������� ����������?",
                                "������ ���������� �����:" + path.toString() + " msg=" + eLock.getMessage(), 
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if(n == 0) {
                	i = 0;
                	continue;
                }
				return false;
			}
		}
		return true;
	}

	/**
	 * �������� ���������� ����� �� ��������� ���.
	 */	
	private boolean changeCounter(FileChannel fileChannel, Path path) {
		ByteBuffer byteBuffer = ByteBuffer.allocate(16);  // stringBuilder.length()	
		byteBuffer.clear();
		try {
			fileChannel.read(byteBuffer);
		} catch (IOException eRead) {
			showError("������ ������ �����", path, eRead);
			return false;
		}
		byteBuffer.flip();
	
		CharBuffer charBuffer = CharBuffer.allocate(8);
		CharsetDecoder decoder = Charset.forName(codePage).newDecoder();
		decoder.decode(byteBuffer, charBuffer, false);
		charBuffer.flip();		
		
		String source = charBuffer.toString().trim();
		if(!source.isEmpty()) {
			try {
				counter = Integer.parseUnsignedInt(source);
			} catch(NumberFormatException eNumberFormat) {
				mainFrame.setMessage("������ ����������� ������ � �����:" + source + " msg=" + eNumberFormat.getMessage());
				return false;
			}
		}
		counter++;
		
		String target = Integer.toString(counter).trim();
		charBuffer.clear();
		charBuffer.put(target);
		charBuffer.flip();
		
		CharsetEncoder encoder = Charset.forName(codePage).newEncoder();
		byteBuffer.clear();
		encoder.encode(charBuffer, byteBuffer, false);
		byteBuffer.flip();
	
		try {
			fileChannel.position(0L);
		} catch (IOException ePosition) {
			showError("������ ���������������� � �����", path, ePosition);
			return false;
		}
		
		try {
			fileChannel.write(byteBuffer);
		} catch (IOException eWrite) {
			showError("������ ������ �����", path, eWrite);
			return false;
		}
		
		try {
			fileChannel.force(true);
		} catch (IOException eForce) {
			showError("������ ������ ������ �� ����", path, eForce);
			return false;
		}	
		return true;
	}
	
	/**
	 * ������� ��������� �� ������
	 */
	public void showError(String msg, Path path, IOException exception) {
		mainFrame.setMessage(msg + ": " + path.toString() + " msg=" + exception.getMessage());		
	}
	
	/**
	 * ������������� ����� � ������ 
	 */
	static private String toUnsignedString(int value) {
		return Integer.toUnsignedString(value).trim();
	}
}
