import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Studio {

	byte[] buffer = new byte[1024];
	int[][] data;
	String msg; // msg that will be returned to the client
	DatagramSocket socket;

	ServerSocket srvSocket;
	ArrayList<Socket> clist = new ArrayList<Socket>();

	Socket clientSocket;
	String studioName;
	int col;
	int row;

	public Studio(String studioName, int col, int row) throws IOException {
		this.studioName = studioName;
		this.col = col;
		this.row = row;
		this.data = new int[col][row];
	}

	public void handleCreater(Socket clientSocket) {
		System.out.println("In handleCreater...");
		System.out.println("Making thread...");
		synchronized (clist) {
			clist.add(clientSocket);
			System.out.printf("Total %d clients are connected!", clist.size());
		}

		Thread t = new Thread(() -> { // establish a new thread
			System.out.println("Calling create()...");
			try {
				create(clientSocket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			synchronized (clist) {
				System.out.println("Removed already!!!" + clientSocket.getPort());
				clist.remove(clientSocket);
			}
		});
		t.start();
	}

	public void handleClient(Socket clientSocket) {
		System.out.println("In handleClient...");
		System.out.println("Making thread...");
		synchronized (clist) {
			clist.add(clientSocket);
			System.out.printf("Total %d clients are connected!", clist.size());
		}

		Thread t = new Thread(() -> { // establish a new thread
			System.out.println("Calling select()...");
			try {
				select(clientSocket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			synchronized (clist) {
				System.out.println("Removed already!!!" + clientSocket.getPort());
				clist.remove(clientSocket);
			}
		});
		t.start();
	}

	public void create(Socket clientSocket) throws IOException {
		System.out.println("In crate method...");
		System.out.println("Accepted!");
		// send(clientSocket); // call send default sketch
		receive(clientSocket);
	}

	public void select(Socket clientSocket) throws IOException {
		System.out.println("In select method...");
		System.out.println("Accepted!");
		send(clientSocket); // call send default sketch
		receive(clientSocket);
	}

	public void receive(Socket clientSocket) throws IOException { // send color pixels
		try {
			DataInputStream in = new DataInputStream(clientSocket.getInputStream()); // each time set up the client
			DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream()); // create new input and output
																							// stream
			while (true) {
				boolean b = in.readBoolean();
				System.out.println("Read in the boolean : " + b);
				if (!b) {
					int len = in.readInt();
					in.read(buffer, 0, len);
					System.out.println("message: " + new String(buffer, 0, len));
					for (Socket s : clist) {
						if (s != clientSocket) { // not send differential update to clientSocket itself
							DataOutputStream sout = new DataOutputStream(s.getOutputStream());

							String str = new String(buffer, 0, len);
							sout.writeBoolean(b);
							System.out.println("The data is text" + b);
							sout.writeInt(str.length());
							System.out.println("The data length is " + str.length());
							sout.write(str.getBytes(), 0, str.length());
							System.out.println("The content of the text: " + str);
						}
					}
				} else {
					int pix = in.readInt(); // receive a pix
					System.out.println("In SimpleServer receive(), Pixel: " + pix);

					int pixCol = in.readInt();
					System.out.println("In SimpleServer receive(), Col: " + pixCol);

					int pixRow = in.readInt();
					System.out.println("In SimpleServer receive(), Row: " + pixRow);

					data[pixCol][pixRow] = pix;

					System.out.println("clientSocketList length: " + clist.size());

					for (Socket s : clist) {
						if (s != clientSocket) { // not send differential update to clientSocket itself

							// send differential update
							DataOutputStream sout = new DataOutputStream(s.getOutputStream());
							System.out.println(s.getPort());
							sout.writeBoolean(b);
							System.out.println("The data is pixel: " + b);
							sout.writeInt(pix); // client side need multiple thread to perform keep standby receiving
												// and
												// sending
							System.out.println("Sent pixel successfully, Pixel: " + pix);
							sout.writeInt(pixCol);
							System.out.println("Sent col successfully, col: " + pixCol);
							sout.writeInt(pixRow);
							System.out.println("Sent row successfully row: " + pixRow);

						}
					}
				}
			}
		} catch (IOException e) {
		}
		try {
			clientSocket.close();
		} catch (IOException e) {
		}

	}

	public void send(Socket clientSocket) throws IOException { // send default pixel
		DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
		for (int r = 0; r < row; r++) {
			for (int c = 0; c < col; c++) {
				if (data[c][r] != 0) {
					out.writeBoolean(true);
					out.writeInt(data[c][r]);
					out.writeInt(c);
					out.writeInt(r);
					System.out.println("Successfully sent a pixel!!!");
				}

			}
		}
	}

	public String getName() {
		return studioName;
	}

	public int returnCol() {
		System.out.println("The col: " + col);
		return col;
	}

	public int returnRow() {
		System.out.println("The row: " + row);
		return row;
	}

}
