import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import net.codejava.sound.AudioPlayer;

//client
public class KidPaint extends JFrame {
	private String serverIP;
	private int serverPort;
	private Socket tcpSocket; // tcp socket used for data transmission between users and server
	private static DataOutputStream out; // OutputStream used for sending data
	private static DataInputStream in; // InputStream used for receiving the data sent by server
	private int[][] downloadedSketchData;
	private String name;
	private UI ui;
	private static JTextField textField = new JTextField();
	private byte[] buffer = new byte[1024];
	private byte[] studioBuffer = new byte[1024];
	private ArrayList<String> studio = new ArrayList<>(); // List of studios
	private int numCol = 50; // default size
	private int numRow = 50;
	private int time = 1000 * 60 * 10;

	private int blockSize;

	private JRadioButton circle = new JRadioButton("Circle");
	private JRadioButton square = new JRadioButton("Square");

	private JRadioButton low = new JRadioButton("Low Resolution");
	private JRadioButton med = new JRadioButton("Medium Resolution");
	private JRadioButton high = new JRadioButton("High Resolution");
	JFrame frame = new JFrame();
	
	public KidPaint() throws IOException {

		Timer timer = new Timer();

		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				JOptionPane.showMessageDialog(frame, "Time is up!");
				System.exit(0);
			}
		}, time);

		inputName(); // Input name window
	}

	public boolean receiveStudioList() {
		int count = 0;
		int listSize = 0;
		int nameSize = 0;
		int len;

		System.out.println("In receiveStudioList()...");

		try {
			listSize = in.readInt();
			System.out.println("Received studio list size!!!");
			System.out.println("Size: " + listSize);

			while (listSize > 0) {
				System.out.println("In list loop...");
				len = in.readInt();
				System.out.println("Received length: " + len);

				in.read(studioBuffer, 0, len);
				System.out.println("Received studioName: " + new String(studioBuffer, 0, len));
				studio.add(new String(studioBuffer, 0, len)); // Add new received studio to the list

				// count++;
				listSize--;
				System.out.println("Received studio: " + new String(studioBuffer, 0, len));
			}

			// if (count == listSize) {
			System.out.println("Receive studio list!!!");
			return true;
			// } else
			// return false; // if received studio not equal to the expected size

		} catch (IOException e) {
			e.printStackTrace();
		}
		return false; // if received studio not equal to the expected size
	}

	public void udpConnection(String name) throws UnknownHostException, IOException {
		String msg = "Name: " + name;
		String content; // read server content
		InetAddress broadcastAddr = InetAddress.getByName("255.255.255.255");

		// DatagramSocket receivedSocket = new DatagramSocket(4321); // socket used for
		// receving
		// DatagramSocket sentSocket = new DatagramSocket(9876);
		DatagramSocket socket = new DatagramSocket(0);
		DatagramPacket receivedPacket = new DatagramPacket(new byte[1024], 1024);
		DatagramPacket sentPacket = new DatagramPacket(msg.getBytes(), msg.length(), broadcastAddr, 1234);

		// Send a request to server using UDP
		// sentSocket.send(sentPacket);
		socket.send(sentPacket);

		// System.out.println("Sent!!!");

		// System.out.println("Listening...");
		// receivedSocket.receive(receivedPacket); // Receive server's response
		socket.receive(receivedPacket);

		// System.out.println(new String(receivedPacket.getData(), 0,
		// receivedPacket.getLength()));
		// System.out.println("Received");

		// Receive back a packet from the server, which contains serverIP and serverPort
		String receivedMsg = new String(receivedPacket.getData(), 0, receivedPacket.getLength());
		serverIP = receivedMsg.substring(0, receivedMsg.indexOf(","));
		serverPort = Integer.parseInt(receivedMsg.substring(receivedMsg.indexOf(",") + 2));

		// System.out.println("IP:" + serverIP);
		// System.out.println("Port:" + serverPort);

		// System.out.println("yeah");

		// receivedSocket.close();
		// sentSocket.close();
		socket.close();

		establishTcp(); // establish TCP connection

		studio();
		// download(); // download sketch data
		//
		// Thread receiveDataThread = new Thread(() -> {
		// receiveData();
		// });
		//
		// receiveDataThread.start();

		// Thread receiveMsgThread = new Thread(() -> {
		// System.out.println("In receive msg thread");
		// receiveMsg();
		// });
		//
		//// receiveMsgThread.start();

	}

	public void establishTcp() throws UnknownHostException, IOException {
		System.out.println("establishing tcp connection...");
		// Setup TCP Connection with server

		System.out.println(serverPort);
		System.out.println(serverIP);

		tcpSocket = new Socket(serverIP, serverPort);
		System.out.println("tcp Connect!");

		// Receive the data sent by server
		in = new DataInputStream(tcpSocket.getInputStream());
		System.out.println("Set up inputStream!");

		// Send data to server
		out = new DataOutputStream(tcpSocket.getOutputStream());

	}

	public static void send(int pixel, int col, int row) throws IOException {
		// System.out.println("In send()...");
		out.writeBoolean(true); // true if send pixel
		System.out.println(true);
		out.writeInt(pixel); // send pixel
		System.out.println("Sent Pixel!");
		out.writeInt(col); // send pixel_col
		System.out.println("Sent Pixel Col!");
		out.writeInt(row); // send pixel_row
		System.out.println("Sent Pixel Row!");
	}

	public static void send(LinkedList<Point> list, int pixel) throws IOException {
		byte buffer[] = new byte[1024];
		int i = 0;
		for (Point p : list) {
			out.writeBoolean(true);
			out.writeInt(pixel); // color
			out.writeInt((int) p.getX());
			out.writeInt((int) p.getY());
			// System.out.println("getX(): " + (int) p.getX() + ", getY(): " + (int)
			// p.getY());
			i++;
		}
		// System.out.println("Total: " + i);
	}

	public static void sendMsg(String msg) {
		try {
			msg = textField.getText() + ": " + msg;
			// System.out.println("Message length is " + msg.length());
			out.writeBoolean(false); // false if send msg
			out.writeInt(msg.length());
			out.write(msg.getBytes(), 0, msg.length());
			// System.out.println(false);
			// System.out.println("Sent msg already!!!!!");
		} catch (IOException e) {
			// textArea.append("Unable to send message to the server!\n");
			e.printStackTrace();
		}
	}

	public void receiveData() {
		try {
			// System.out.println("In receiveDataThread...");
			while (true) {

				int pixel;
				int col;
				int row;

				if (in.readBoolean()) { // if pixel, return true

					pixel = in.readInt();
					col = in.readInt();
					row = in.readInt();

					// System.out.println("In SimpleClient receiveData(), Pixel: " + pixel);
					// System.out.println("In SimpleClient receiveData(), Col: " + col);
					// System.out.println("In SimpleClient receiveData(), Row: " + row);

					// textArea.append(new String(buffer, 0, len) + "\n");
					ui.selectColor(pixel);
					// System.out.println("!!!!!!!Pixel: " + pixel);
					ui.paintPixel(col, row);

				} else { // if msg, return false
					int len = in.readInt();
					in.read(buffer, 0, len);

					System.out.println(new String(buffer, 0, len) + "\n");

					ui.onTextInputted(new String(buffer, 0, len));
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void receiveMsg() {
		try {
			byte[] buffer = new byte[1024];

			while (true) {
				if (!in.readBoolean()) {
					int len = in.readInt();
					in.read(buffer, 0, len);

					// System.out.println(new String(buffer, 0, len) + "\n");

					ui.onTextInputted(new String(buffer, 0, len) + "\n");

				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void inputName() {

		this.setSize(new Dimension(320, 240));

		Container container = this.getContentPane(); // Create Container to store text field
		container.setLayout(new GridLayout(3, 0)); // Set layout using FlowLayout

		JLabel label = new JLabel("Please enter your name: ");

		JButton submit = new JButton("Submit");
		
		container.add(label);
		container.add(textField);
		container.add(submit);

		setDefaultCloseOperation(EXIT_ON_CLOSE);

		// Once submit name, activate the UI window
		submit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// System.out.println("Hi!" + textField.getText());
				try {
					udpConnection(textField.getText()); // Create new client once input the user name

				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				setVisible(false); // If submitted, close input name window
			}
		});
		setVisible(true);
		// ui = UI.getInstance(); // get the instance of UI
		// ui.setData(new int[50][50], 20); // set the data array and block size.
		// comment this statement to use the default data array and block size.

		// ui.setVisible(true);
		// setVisible(false); // If submitted, close input name window
	}

	public void studio() {

		if (receiveStudioList()) {
//			JFrame frame = new JFrame();
			frame.setVisible(true);
			frame.setSize(300, 600);

			frame.setLayout(new GridLayout(studio.size() + 15, 0));
			// this.setSize(new Dimension(320, 240));
			// Container container = this.getContentPane();
			// container.setLayout(new GridLayout(studio.size() + 3, 0)); // use the size of
			// list to
			// // determine the number of
			// // buttons in the panel, extra 3
			// // for label, create studio
			// // btn, and textfield
			JLabel label1 = new JLabel("Please create or choose studios");
			JTextField text = new JTextField();
			JButton create = new JButton("Create studio");
			JLabel row = new JLabel("Please input the number of rows");
			JLabel col = new JLabel("Please input the number of columns");
			JTextField numRow = new JTextField();
			JTextField numCol = new JTextField();

			ButtonGroup group = new ButtonGroup();

			group.add(circle);
			group.add(square);

			circle.setSelected(true);

			ButtonGroup resBtnGroup = new ButtonGroup();
			resBtnGroup.add(low);
			resBtnGroup.add(med);
			resBtnGroup.add(high);

			med.setSelected(true);

			JLabel type = new JLabel("Please choose the type of sketch");

			JLabel label2 = new JLabel("Choose studios");

			create.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) { // If clicked, send "create studio" TCP package
					try {
						int col = Integer.parseInt(numCol.getText());
						int row = Integer.parseInt(numRow.getText());
						byte[] buffer = new byte[1024];

						String studioName = text.getText();

						System.out.println("Text: " + studioName);

						if (studioName.equals("")) {
							JOptionPane.showMessageDialog(frame,
									"Studio name cannot be empty. Please enter the name again.");
							return;
						}

						for (String tmp : studio) {
							if (tmp.equals(studioName)) {
								JOptionPane.showMessageDialog(frame, "Studio name exists. Please enter another name.");
								return;
							}
						}

						createStudio(text.getText(), col, row);

						System.out.println("circle selected: " + circle.isSelected());
						System.out.println("square selected: " + square.isSelected());

						String type;

						if (circle.isSelected())
							type = "circle";
						else
							type = "square";

						if (low.isSelected()) {
							blockSize = 32;
						} else if (high.isSelected()) {
							blockSize = 8;
						} else
							blockSize = 16;

						ui = UI.getInstance(col, row, text.getText(), type, blockSize);

						frame.setVisible(false); // If created, close studio window
						ui.setVisible(true);

						Thread receiveDataThread = new Thread(() -> {
							receiveData();
						});

						receiveDataThread.start();

					} catch (NumberFormatException e1) {

						String studioName = text.getText();

						if (studioName.equals("")) {
							JOptionPane.showMessageDialog(frame,
									"Studio name cannot be empty. Please enter the name again.");
							return;
						}

						for (String tmp : studio) {
							if (tmp.equals(studioName)) {
								JOptionPane.showMessageDialog(frame, "Studio name exists. Please enter another name.");
								return;
							}
						}

						System.out.println("circle selected: " + circle.isSelected());
						System.out.println("square selected: " + square.isSelected());

						createStudio(text.getText(), 50, 50);

						String type;

						if (circle.isSelected())
							type = "circle";
						else
							type = "square";

						if (low.isSelected()) {
							blockSize = 32;
						} else if (high.isSelected()) {
							blockSize = 8;
						} else
							blockSize = 16;

						ui = UI.getInstance(50, 50, text.getText(), type, blockSize);

						Thread receiveDataThread = new Thread(() -> {
							receiveData();
						});

						receiveDataThread.start();

						frame.setVisible(false); // If created, close studio window
						ui.setVisible(true);

					}
				}
			});

			frame.add(type);
			frame.add(circle);
			frame.add(square);

			frame.add(new JLabel("Please select resolution of the sketch"));
			frame.add(low);
			frame.add(med);
			frame.add(high);

			frame.add(label1);
			System.out.println("Added label");
			frame.add(text);
			System.out.println("Added text");
			frame.add(row);
			frame.add(numRow);
			frame.add(col);
			frame.add(numCol);

			frame.add(create);

			System.out.println("Added Create Button");
			// frame.add(add(new JSeparator(SwingConstants.HORIZONTAL)));
			frame.add(label2);

			for (int i = 0; i < studio.size(); i++) {
				JButton btn = new JButton(studio.get(i)); // Create buttons

				btn.addActionListener(new ActionListener() { // Create actionListener for each button
					public void actionPerformed(ActionEvent e) {

						try {
							System.out.println("In select studio...");

							int col;
							int row;

							out.write(("select " + btn.getText()).getBytes()); // If clicked, send a "select
																				// studio" TCP package

							System.out.println("circle selected: " + circle.isSelected());
							System.out.println("square selected: " + square.isSelected());

							col = in.readInt();
							System.out.println("Received col: " + col);
							row = in.readInt();
							System.out.println("Received row: " + row);

							System.out.println("In select, receive col: " + col);
							System.out.println("In select, receive row: " + row);

							String type;

							if (circle.isSelected())
								type = "circle";
							else
								type = "square";

							if (low.isSelected()) {
								blockSize = 32;
							} else if (high.isSelected()) {
								blockSize = 8;
							} else
								blockSize = 16;

							ui = UI.getInstance(col, row, btn.getText(), type, blockSize);

							Thread receiveDataThread = new Thread(() -> {
								receiveData();
							});

							receiveDataThread.start();

							// includes the name of studio
							frame.setVisible(false); // If submitted, close choose studio window
							ui.setVisible(true);

						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}

				});
				frame.add(btn); // Add button to the panel
				System.out.println("Studio Button");
			}

			frame.setDefaultCloseOperation(EXIT_ON_CLOSE);

			// System.out.println(frame);
			setVisible(true);

			// Thread receiveDataThread = new Thread(() -> {
			// receiveData();
			// });
			//
			// receiveDataThread.start();

		} else {
			System.out.println("Error occurred! Size of list is not expected.");
			System.exit(0);
		}
	}

	public void createStudio(String text, int col, int row) {
		try {
			out.write(("create " + text).getBytes());
			out.writeInt(col);
			out.writeInt(row);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws UnknownHostException {
		try {
			AudioPlayer music = new AudioPlayer();
			music.play("startupmusic.wav");
			
			KidPaint name = new KidPaint();
			// name.setVisible(true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
