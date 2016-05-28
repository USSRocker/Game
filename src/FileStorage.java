/**
 * © Dmitry Boyko, 2016
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
 * Сохранить журналы игры.
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
	 * Статический инициализатор класса.
	 */	
	static {
		fileSystem = FileSystems.getDefault();
		
		local = new Locale("ru","RU");
		dateFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, local); 
		timeFormat = DateFormat.getTimeInstance(DateFormat.DEFAULT, local);
		
		String resultHead = 
				"Номер игры" + splitter +
				"Группа" + splitter +
				"Дата начала игры" + splitter + 
				"Время начала игры" + splitter + 
				"Продожительность игры" + splitter + 
				"Кол-во зарегестрированных игроков" + splitter + 
				"Режим игры" + splitter +
				"Играть до кол-ва оставшихся/выбывших игроков" + splitter +
				"Начальное кол-во жизней" + splitter + 
				"Время на ход" + splitter +
				"Доп.ход за потерю жизни" + splitter +
				"Делить на группы" + splitter +
				"Минимальная группа" + splitter;
		resultHeadByEnd   = resultHead      + "Победитель" + "\n";
		resultHeadByFirst = resultHeadByEnd + "Проигравший" + "\n";
		
		castingHead = 
				"Номер игры" + splitter +
				"Группа" + splitter +
				"Порядковый номер" + splitter +
				"Ник игрока" + splitter +
				"Фамлия" + splitter +
				"Имя" + splitter +
				"Отчество" + splitter +
				"Имя компьютера" + splitter +
				"Имя пользователя" + splitter +
				"Отображаемый ник" + splitter +
				"\n";
		
		movesHead = 
				"Номер игры" + splitter +
				"Группа" + splitter +
				"Номер хода" + splitter +
				"Ник игрока" + splitter +
				"Отображаемый ник игрока" + splitter +
				"Кол-во жизней игрока" + splitter +
				"Убитый игрок" + splitter +
				"Отображаемый ник убитого игрока" + splitter +
				"Кол-во жизней убитого игрока" + splitter +
				"Дата начала хода" + splitter +
				"Время начала хода" + splitter +
				"Продожительность хода" + splitter +
				"\n";
	}
	
	/**
	 * Конструктор 
	 */
	FileStorage(MainFrame aMainframe) {
		mainFrame = aMainframe;
	}
	
	/**
	 * Инициализировать на новую игру
	 */
	public void init() {
		startGame = new Date();
		counter = 0;
	}
	
	/**
	 * Сохранить все результаты сессии игры.
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
	 * Сохранить результат текущей игры.
	 */	
	private void saveResult(String baseName) {
		ConfigPanel configPanel = mainFrame.configPanel;
		
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(toUnsignedString(counter) + splitter);             // Номер игры
		stringBuilder.append(toUnsignedString(group.numOfGroup) + splitter);    // Группа

		stringBuilder.append(dateFormat.format(startGame) + splitter);          // Дата начала игры
		stringBuilder.append(timeFormat.format(startGame) + splitter);          // Время начала игры
		long gameTime = (finishGame.getTime() - startGame.getTime()) / 1000;	
		stringBuilder.append(toUnsignedString((int)gameTime) + splitter);       // Продожительность игры
		
		stringBuilder.append(toUnsignedString(mainFrame.playerPanel.getPlayerCount()) + splitter);  // Количество игроков
		
		String txtGameMode = ConfigPanel.arrayGameMode[configPanel.cbGameMode.getSelectedIndex()];
		stringBuilder.append(txtGameMode + splitter);  // Игровой режим
		stringBuilder.append(toUnsignedString((int)configPanel.spnPlayerCount.getValue()) + splitter);  // Кол-во оставшихся/выбывших игроков
		
		stringBuilder.append(toUnsignedString(mainFrame.getLives()) + splitter);        // Количество жизней
		stringBuilder.append(toUnsignedString(mainFrame.getSeconds()) + splitter);      // Время на ход

		float addMove = (float)configPanel.spnAddMove.getValue();
		NumberFormat numberFormat = NumberFormat.getNumberInstance(); 
		numberFormat.setMaximumFractionDigits(2); 
		//DecimalFormat myFormatter = new DecimalFormat(pattern);
		stringBuilder.append(numberFormat.format(addMove) + splitter);  // Добавленные ходы
		
		if(configPanel.cbSplitOnGroups.isSelected()) {  
			stringBuilder.append("Да" + splitter);   // Разделять на группы
			stringBuilder.append(toUnsignedString((int)configPanel.spnMinGroup.getValue()) + splitter);  // Минимальная группа
		} else {
			stringBuilder.append("Нет" + splitter);  // Разделять на группы
			stringBuilder.append(splitter);          // Минимальная группа
		}
		
		if(mainFrame.isToEndMode()) {
			stringBuilder.append(victor + splitter);      // Победитель
			stringBuilder.append(showVictor + splitter);  // Победитель
		} else {
			stringBuilder.append(loser + splitter);       // Проигравший
			stringBuilder.append(showLoser + splitter);   // Проигравший
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
	 * Сохранить жеребьевку игры.
	 */	
	private void saveCasting(String baseName) {
		StringBuilder stringBuilder = new StringBuilder();
		Iterator<PlayerRow> itr = mainFrame.playerPanel.getTableIterator();
	    while (itr.hasNext()) {
	    	PlayerRow player = itr.next();
	    	if(player.group != group.numOfGroup) {
	    		continue;
	    	}
	    	stringBuilder.append(toUnsignedString(counter) + splitter);        // Номер игры
	    	stringBuilder.append(toUnsignedString(player.group) + splitter);   // Группа
	    	stringBuilder.append(toUnsignedString(player.number) + splitter);  // Порядковый номер
	    	stringBuilder.append(player.nick + splitter);                      // Ник игрока
	    	stringBuilder.append(player.lastName + splitter);                  // Фамилия 
	    	stringBuilder.append(player.name + splitter);                      // Имя
	    	stringBuilder.append(player.patronymic + splitter);                // Отчество
	    	stringBuilder.append(player.computerName + splitter);              // Имя компьютера
	    	stringBuilder.append(player.userName + splitter);                  // Имя пользователя	    	
	    	stringBuilder.append(player.showNick + splitter);                  // Отображаемый ник	    	
			stringBuilder.append("\n");
	    }		
		
		Path path = fileSystem.getPath(baseName + "-Casting" + version + ext);
		save(path, stringBuilder.toString(), castingHead);
	}
	
	/**
	 * Сохранить ходы игры.
	 */	
	private void saveMoves(String baseName) {
		StringBuilder stringBuilder = new StringBuilder();
		for(int i = 0; i < group.playerMoves.size(); i++) {
			PlayerMove playerMove = group.playerMoves.get(i);
	    	stringBuilder.append(toUnsignedString(counter) + splitter);           // Номер игры
	    	stringBuilder.append(toUnsignedString(group.numOfGroup) + splitter);  // Номер Группы
	    	stringBuilder.append(toUnsignedString(i + 1) + splitter);             // Номер хода
	    	
	    	stringBuilder.append(playerMove.movedPlayer.nick + splitter);                     // Ник игрока
	    	stringBuilder.append(playerMove.movedPlayer.showNick + splitter);                 // Отображаемый ник игрока
	    	stringBuilder.append(toUnsignedString(playerMove.movedPlayer.lives) + splitter);  // Кол-во жизней игрока
	    	
	    	stringBuilder.append(playerMove.killedPlayer.nick + splitter);                     // Ник убитого игрока
	    	stringBuilder.append(playerMove.killedPlayer.showNick + splitter);                 // Отображаемый ник убитого игрока
	    	stringBuilder.append(toUnsignedString(playerMove.killedPlayer.lives) + splitter);  // Кол-во жизней убитого игрока

	    	long gameTime = (playerMove.finishDate.getTime() - playerMove.startDate.getTime()) / 1000;	
			stringBuilder.append(dateFormat.format(playerMove.startDate) + splitter);   // Дата начала игры
			stringBuilder.append(timeFormat.format(playerMove.startDate) + splitter);   // Время начала игры
	    	stringBuilder.append(toUnsignedString((int)gameTime) + splitter);           // Продожительность хода

	    	stringBuilder.append("\n");
		}
		
		Path path = fileSystem.getPath(baseName + "-Moves" + version + ext);
		save(path, stringBuilder.toString(), movesHead);
	}
	
	/**
	 * Сохранить данные в конец файла.
	 */	
	private void save(Path path, String data, String header) {
		FileChannel fileChannel;
		try {
			fileChannel = FileChannel.open(path, APPEND, CREATE, SYNC);
		} catch (IOException eOpen) {
			showError("Ошибка открытия файла", path, eOpen);
			return;
		}
	
		if(lockFile(fileChannel, path)) {
			saveString(fileChannel, path, data, header);
		}
		
		try {
			fileChannel.close();
		} catch (IOException eClose) {
			showError("Ошибка закрытия файла", path, eClose);
		}
	}
	
	/**
	 * Сохранить данные из строки.
	 */	
	private boolean saveString(FileChannel fileChannel, Path path, String data, String header) {
		long size = 0L;
		try {
			size = fileChannel.size();
		} catch (IOException eSize) {
			showError("Ошибка чтения размера файла", path, eSize);
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
			showError("Ошибка записи файла:", path, eWrite);
			return false;
		}
		
		try {
			fileChannel.force(true);
		} catch (IOException eForce) {
			showError("Ошибка сброса данных на диск", path, eForce);
			return false;
		}	
		return true;		
	}
		
	/**
	 * Сохранить счетчик игр с перезаписью файла.
	 */	
	private boolean saveCounter(String baseName) {
		Path path = fileSystem.getPath(baseName + "-Counter.csv");
		
		FileChannel fileChannel;
		try {
			fileChannel = FileChannel.open(path, READ, WRITE, CREATE, SYNC);
		} catch (IOException eOpen) {
			showError("Ошибка открытия файла", path, eOpen);
			return false;
		}
	
		boolean result = false;
		if(lockFile(fileChannel, path)) {
			result = changeCounter(fileChannel, path);
		}
		
		try {
			fileChannel.close();
		} catch (IOException eClose) {
			showError("Ошибка закрытия файла", path, eClose);
			return false;
		}
		return result;
	}
	
	/**
	 * Блокировать файл.
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
                Object[] options = { "Да", "Нет!" };
                int n = JOptionPane.showOptionDialog(mainFrame, "Повторить операцию сохранения?",
                                "Ошибка блокировки файла:" + path.toString() + " msg=" + eLock.getMessage(), 
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
	 * Изменить содержимое файла со счетчиком игр.
	 */	
	private boolean changeCounter(FileChannel fileChannel, Path path) {
		ByteBuffer byteBuffer = ByteBuffer.allocate(16);  // stringBuilder.length()	
		byteBuffer.clear();
		try {
			fileChannel.read(byteBuffer);
		} catch (IOException eRead) {
			showError("Ошибка чтения файла", path, eRead);
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
				mainFrame.setMessage("Ошибка конвертация строки к числу:" + source + " msg=" + eNumberFormat.getMessage());
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
			showError("Ошибка позиционирования в файле", path, ePosition);
			return false;
		}
		
		try {
			fileChannel.write(byteBuffer);
		} catch (IOException eWrite) {
			showError("Ошибка записи файла", path, eWrite);
			return false;
		}
		
		try {
			fileChannel.force(true);
		} catch (IOException eForce) {
			showError("Ошибка сброса данных на диск", path, eForce);
			return false;
		}	
		return true;
	}
	
	/**
	 * Вывести сообщение об ошибке
	 */
	public void showError(String msg, Path path, IOException exception) {
		mainFrame.setMessage(msg + ": " + path.toString() + " msg=" + exception.getMessage());		
	}
	
	/**
	 * Преобразовать число к строке 
	 */
	static private String toUnsignedString(int value) {
		return Integer.toUnsignedString(value).trim();
	}
}
