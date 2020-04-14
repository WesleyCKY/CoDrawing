import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import java.awt.Color;
import javax.swing.border.LineBorder;

enum PaintMode {
	Pixel, Area
};

public class UI extends JFrame {
	private JTextField msgField;
	private JTextArea chatArea;
	private JPanel pnlColorPicker;
	private JPanel inputName; // Panel for them to input names
	private JPanel paintPanel;
	private JToggleButton tglPen;
	private JToggleButton tglBucket;
	private LinkedList<Point> paintedAreaList; // Save painted Area
	private JButton saveBtn; // button of saving
	private JButton importBtn; // button of importing
	private JButton clearBtn; // button of clearing all pixels
	private JToggleButton eraserBtn;

	private static UI instance;
	private int selectedColor = -543230; // golden

	private int numCol = 50;
	private int numRow = 50;

	private int[][] data = new int[50][50]; // pixel color data array

	int blockSize; // 8, 16, 32
	private String type;

	private JLabel timeLabel = new JLabel("00:00:00");;

	PaintMode paintMode = PaintMode.Pixel;

	boolean isPen;
	boolean isEraser;
	boolean isBucket;

	/**
	 * get the instance of UI. Singleton design pattern.
	 * 
	 * @return
	 */
	public static UI getInstance(int numCol, int numRow, String name, String type, int blockSize) {
		if (instance == null)
			instance = new UI(numCol, numRow, name, type, blockSize);

		return instance;
	}

	/**
	 * private constructor. To create an instance of UI, call UI.getInstance()
	 * instead.
	 */
	private UI(int numCol, int numRow, String name, String type, int blockSize) {
		this.numCol = numCol;
		this.numRow = numRow;
		this.type = type;
		this.blockSize = blockSize;

		System.out.println("Number of column: " + this.numCol);
		System.out.println("Number of row: " + this.numRow);

		data = new int[numCol][numRow];

		System.out.println("Title: " + name);

		setTitle(name);

		JPanel basePanel = new JPanel();
		getContentPane().add(basePanel, BorderLayout.CENTER);
		basePanel.setLayout(new BorderLayout(0, 0));

		paintPanel = new JPanel() {

			// refresh the paint panel
			@Override
			public void paint(Graphics g) {
				super.paint(g);

				Graphics2D g2 = (Graphics2D) g; // Graphics2D provides the setRenderingHints method

				// enable anti-aliasing
				RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setRenderingHints(rh);

				// clear the paint panel using black
				g2.setColor(Color.black); // blackground
				g2.fillRect(0, 0, this.getWidth(), this.getHeight());

				// draw and fill circles with the specific colors stored in the data array
				for (int x = 0; x < data.length; x++) {
					for (int y = 0; y < data[0].length; y++) {
						g2.setColor(new Color(data[x][y])); // fill in color in circle

						if (type.equals("circle")) {

							g2.fillArc(blockSize * x, blockSize * y, blockSize, blockSize, 0, 360);
							g2.setColor(Color.darkGray); // draw the circles' boundaries
							g2.drawArc(blockSize * x, blockSize * y, blockSize, blockSize, 0, 360);

						} else if (type.equals("square")) {

							g2.fillRect(blockSize * x, blockSize * y, 15, 15);
							g2.setColor(Color.darkGray); // draw the circles' boundaries
							g2.drawRect(blockSize * x, blockSize * y, 15, 15);

						}

						// g2.fillArc(blockSize * x, blockSize * y, blockSize, blockSize, 0, 360);
						// g2.fillRect(blockSize * x, blockSize * y, 15, 15);
						// g2.setColor(Color.darkGray); // draw the circles' boundaries
						// g2.drawArc(blockSize * x, blockSize * y, blockSize, blockSize, 0, 360);
						// g2.drawRect(blockSize * x, blockSize * y, 15, 15);
					}
				}
			}
		};

		paintPanel.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {

				System.out.println(e.getButton());

				if (e.getButton() == 2) {
					clearPixels();
				} else if (e.getButton() == 3) {

					isPen = tglPen.isSelected();
					isBucket = tglBucket.isSelected();
					isEraser = eraserBtn.isSelected();

					System.out.println(isPen);
					System.out.println(isBucket);
					System.out.println(isEraser);

					if (isPen == true && isBucket == false && isEraser == false) {
						tglPen.setSelected(false);
						tglBucket.setSelected(true);
						eraserBtn.setSelected(false);
						paintMode = PaintMode.Area;
					} else if (isPen == false && isBucket == true && isEraser == false) {
						tglPen.setSelected(false);
						tglBucket.setSelected(false);
						eraserBtn.setSelected(true);
						paintMode = PaintMode.Pixel;
						setEraser();

					} else if (isPen == false && isBucket == false && isEraser == true) {
						tglPen.setSelected(true);
						tglBucket.setSelected(false);
						eraserBtn.setSelected(false);
						paintMode = PaintMode.Pixel;
					}
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			// handle the mouse-up event of the paint panel
			@Override
			public void mouseReleased(MouseEvent e) {
				if (paintMode == PaintMode.Area && e.getX() >= 0 && e.getY() >= 0 && e.getButton() == 1) {
					System.out.println("Hi");
					paintedAreaList = paintArea(e.getX() / blockSize, e.getY() / blockSize);
				}
			}
		});

		paintPanel.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent e) {
				System.out.println("Mouse type: " + e.getModifiersEx());

				if (paintMode == PaintMode.Pixel && e.getX() >= 0 && e.getY() >= 0 && e.getModifiersEx() == 1024) // left
					paintPixel(e.getX() / blockSize, e.getY() / blockSize);
			}

			@Override
			public void mouseMoved(MouseEvent e) {
			}

		});

		paintPanel.setPreferredSize(new Dimension(data.length * blockSize, data[0].length * blockSize));

		JScrollPane scrollPaneLeft = new JScrollPane(paintPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		basePanel.add(scrollPaneLeft, BorderLayout.CENTER);

		JPanel toolPanel = new JPanel();
		basePanel.add(toolPanel, BorderLayout.NORTH);
		toolPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		// set initial background color of color picker
		pnlColorPicker = new JPanel();
		pnlColorPicker.setPreferredSize(new Dimension(24, 24));
		pnlColorPicker.setBackground(new Color(selectedColor));
		pnlColorPicker.setBorder(new LineBorder(new Color(0, 0, 0)));

		// show the color picker
		pnlColorPicker.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				ColorPicker picker = ColorPicker.getInstance(UI.instance, numCol, numRow, name, type, blockSize);
				Point location = pnlColorPicker.getLocationOnScreen();
				location.y += pnlColorPicker.getHeight();
				picker.setLocation(location);
				picker.setVisible(true);
			}

		});

		toolPanel.add(pnlColorPicker);

		tglPen = new JToggleButton("Pen");
		tglPen.setSelected(true);
		toolPanel.add(tglPen);

		tglBucket = new JToggleButton("Bucket");
		toolPanel.add(tglBucket);

		eraserBtn = new JToggleButton("Eraser");
		toolPanel.add(eraserBtn);

		saveBtn = new JButton("Save");
		toolPanel.add(saveBtn);

		importBtn = new JButton("Import");
		toolPanel.add(importBtn);

		clearBtn = new JButton("Clear All");
		toolPanel.add(clearBtn);

		toolPanel.add(timeLabel);

		Thread t = new Thread(() -> {
			while (true) {
				Date dd = new Date();
				timeLabel.setText(dd.getHours() + " : " + dd.getMinutes() + " : " + dd.getSeconds());

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} // 1000 = 1 second
			}

		});

		t.start();

		// perform save function
		saveBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String filename = JOptionPane.showInputDialog("Please input the name of file: ");

				if (filename == null) {
					return;
				}

				saveFile(filename);
			}
		});

		// perform import function
		importBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// Create a file chooser
				final JFileChooser fc = new JFileChooser();

				// In response to a button click:
				int returnVal = fc.showOpenDialog(null);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					importFile(file);
				}

				// String filename = JOptionPane.showInputDialog("Please input the name of file:
				// ");
				// importFile(filename);
			}
		});

		clearBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				clearPixels();
			}
		});

		// change the paint mode to PIXEL mode
		tglPen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				tglPen.setSelected(true);
				tglBucket.setSelected(false);
				eraserBtn.setSelected(false);
				paintMode = PaintMode.Pixel;
			}
		});

		// change the paint mode to AREA mode
		tglBucket.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				tglPen.setSelected(false);
				tglBucket.setSelected(true);
				eraserBtn.setSelected(false);
				paintMode = PaintMode.Area;
			}
		});

		eraserBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				tglPen.setSelected(false);
				tglBucket.setSelected(false);
				eraserBtn.setSelected(true);
				paintMode = PaintMode.Pixel;
				setEraser();
			}
		});

		JPanel msgPanel = new JPanel();

		getContentPane().add(msgPanel, BorderLayout.EAST);

		msgPanel.setLayout(new BorderLayout(0, 0));

		msgField = new JTextField(); // text field for inputting message

		msgPanel.add(msgField, BorderLayout.SOUTH);

		// handle key-input event of the message field
		msgField.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == 10) { // if the user press ENTER
					try {
						sendMsg(msgField.getText());
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					onTextInputted(msgField.getText());
					msgField.setText("");
				}
			}

		});

		chatArea = new JTextArea(); // the read only text area for showing messages
		chatArea.setEditable(false);
		chatArea.setLineWrap(true);

		JScrollPane scrollPaneRight = new JScrollPane(chatArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPaneRight.setPreferredSize(new Dimension(300, this.getHeight()));
		msgPanel.add(scrollPaneRight, BorderLayout.CENTER);

		this.setSize(new Dimension(1000, 600));
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	/**
	 * it will be invoked if the user selected the specific color through the color
	 * picker
	 * 
	 * @param colorValue
	 *            - the selected color
	 */
	public void selectColor(int colorValue) { // choose a color from colorPicker

		// System.out.println("Color Value: " + colorValue);
		selectedColor = colorValue;
		pnlColorPicker.setBackground(new Color(colorValue)); // set color's picker background

	}

	/**
	 * it will be invoked if the user inputted text in the message field
	 * 
	 * @param text
	 *            - user inputted text
	 */
	public void onTextInputted(String text) {
		chatArea.setText(chatArea.getText() + text + "\n");
	}

	/**
	 * change the color of a specific pixel
	 * 
	 * @param col,
	 *            row - the position of the selected pixel
	 */
	public void paintPixel(int col, int row) {
		if (col >= data.length || row >= data[0].length)
			return;

		data[col][row] = selectedColor; // color of each pixel

		// System.out.println("Selected Color: " + selectedColor);
		// System.out.println(data[col][row]); // print pixel
		// System.out.println(col + " " + row);

		paintPanel.repaint(col * blockSize, row * blockSize, blockSize, blockSize);
		// send method send the changed pixel to SimpleServer class for performing
		// differential updates
		try {
			StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
			// System.out.println(stacktrace);
			StackTraceElement e = stacktrace[2];
			String methodName = e.getMethodName();

			if (methodName.equals("mouseDragged")) // distinguish paintPixel() method called by mouseDragged or
													// receiveData in SimpleClient class
				send(data[col][row], col, row);
			else
				return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
		// System.out.println("*****************************************");
		// System.out.println(stacktrace);
		// StackTraceElement e = stacktrace[2];//maybe this number needs to be corrected
		// String methodName = e.getMethodName();
		// System.out.println(methodName);
		//
		// if (methodName.equals("mouseDragged")) {
		// try {
		// send(data[col][row], col, row);
		// } catch (IOException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// } //
		// }
	}

	/**
	 * change the color of a specific area
	 * 
	 * @param col,
	 *            row - the position of the selected pixel
	 * @return a list of modified pixels
	 */
	public LinkedList<Point> paintArea(int col, int row) {
		LinkedList<Point> filledPixels = new LinkedList<Point>();

		if (col >= data.length || row >= data[0].length)
			return filledPixels;

		int oriColor = data[col][row];
		LinkedList<Point> buffer = new LinkedList<Point>();

		if (oriColor != selectedColor) {
			buffer.add(new Point(col, row));

			while (!buffer.isEmpty()) {
				Point p = buffer.removeFirst();
				int x = p.x;
				int y = p.y;

				if (data[x][y] != oriColor)
					continue;

				data[x][y] = selectedColor;
				filledPixels.add(p);

				if (x > 0 && data[x - 1][y] == oriColor)
					buffer.add(new Point(x - 1, y));
				if (x < data.length - 1 && data[x + 1][y] == oriColor)
					buffer.add(new Point(x + 1, y));
				if (y > 0 && data[x][y - 1] == oriColor)
					buffer.add(new Point(x, y - 1));
				if (y < data[0].length - 1 && data[x][y + 1] == oriColor)
					buffer.add(new Point(x, y + 1));

			}
			paintPanel.repaint();

			try {
				StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
				System.out.println(stacktrace);
				StackTraceElement e = stacktrace[2];
				String methodName = e.getMethodName();

				if (methodName == "mouseReleased")
					send(filledPixels, selectedColor);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return filledPixels;
	}

	/**
	 * set pixel data and block size
	 * 
	 * @param data
	 * @param blockSize
	 */
	public void setData(int[][] data, int blockSize) {
		this.data = data;
		this.blockSize = blockSize;
		paintPanel.setPreferredSize(new Dimension(data.length * blockSize, data[0].length * blockSize));
		paintPanel.repaint();
	}

	public int[][] getData() {
		return data;
	}

	// send method send the changed pixel to SimpleServer class for performing
	// differential updates
	public void send(int pixel, int col, int row) throws IOException {
		// System.out.println("send pixel");
		KidPaint.send(pixel, col, row);
		// System.out.println("In UI send(), Sent!!!");
	}

	public void send(LinkedList<Point> list, int pixel) throws IOException {
		// SimpleClient.send(pixel, col, row);
		// System.out.println("send whole");
		// for (Point p: list) {
		// SimpleClient.send(data[p.x][p.y], p.x, p.y);
		// }
		KidPaint.send(list, pixel);
	}

	public void sendMsg(String msg) throws IOException {
		KidPaint.sendMsg(msg);
	}

	public void saveFile(String filename) {
		try {
			File file = new File(filename);
			FileOutputStream fout = new FileOutputStream(file);
			DataOutputStream out = new DataOutputStream(fout);

			System.out.println("Saving...");

			out.writeInt(numCol);
			out.writeInt(numRow);

			for (int col = 0; col < numCol; col++) {
				for (int row = 0; row < numRow; row++) {
					out.writeInt(data[col][row]);
					System.out.println("Column: " + col + ", Row: " + row);
				}
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void importFile(File file) {
		try {
			FileInputStream fin = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fin);
			int value;

			int importCol;
			int importRow;

			System.out.println("Importing...");

			importCol = in.readInt();
			importRow = in.readInt();

			for (int col = 0; col < importCol; col++) {
				for (int row = 0; row < importRow; row++) {
					value = in.readInt();
					selectColor(value);
					System.out.println("Column: " + col + ", Row: " + row);
					paintPixel(col, row);

					if (numCol <= col || numRow <= row) {
						continue;
					}

					System.out.println("Col: " + col + ", Row: " + row);

					send(value, col, row);
				}
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void clearPixels() {
		int value = 0;
		for (int col = 0; col < numCol; col++) {
			for (int row = 0; row < numRow; row++) {
				selectColor(value);
				// System.out.println("!!!!!!!Pixel: " + value);
				paintPixel(col, row);

				try {
					send(value, col, row);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public void setEraser() {
		selectColor(0);
	}
}
