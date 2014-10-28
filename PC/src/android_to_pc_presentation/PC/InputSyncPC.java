/*
 * Copyright (C) 2014 Taner Guven <tanerguven@gmail.com>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package android_to_pc_presentation.PC;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import android_to_pc_presentation.shared.InputHistory;
import android_to_pc_presentation.shared.InputSyncPackage;
import android_to_pc_presentation.shared.InputSyncPackageList;

public class InputSyncPC {

	SlideView slideView;
	String redrawFile;

	public InputSyncPC(SlideView slideView, String redrawFile) {
		this.slideView = slideView;
		this.redrawFile = redrawFile;
	}

	void runHistoryRecord(Object event) throws Exception {
		// System.out.println("runHistoryRecord");
		if (event.getClass() == InputHistory.ModeSelect.class) {
			InputHistory.ModeSelect rec = (InputHistory.ModeSelect) event;
			if (rec.isChangeSlide()) {
				slideView.doChangeSlide(rec.getValue());
			} else if (rec.isPaintColor()) {
				String strColor = Integer
						.toHexString(rec.getValue() & 0xffffff);
				while (strColor.length() < 6)
					strColor = "0" + strColor; // 6 karakterden kucukse basina
												// ekle
				slideView.df.setPaintColor("#00" + strColor);
			} else if (rec.isSelectPen()) {
				slideView.doSelectPen();
			} else if (rec.isSelectEraser()) {
				slideView.doSelectEraser();
			} else if (rec.isLoadSlides()) {
				slideView.doChangeSlide(0); // acik slaytin kaydedilmesi icni
				slideView.loadSlides(ConfigPC.PATH + "/", rec.getValue());
			} else if (rec.isStrokeWidth()) {
				slideView.setStrokeWidth(rec.getValue());
			} else {
				System.out.println("unknown modeselect");
			}
		} else if (event.getClass() == InputHistory.TouchRecord.class) {
			InputHistory.TouchRecord rec = (InputHistory.TouchRecord) event;
			// System.out.println("slideView.doTouchEvent" + rec.x + " " +
			// rec.y);
			slideView.doTouchEvent(rec.action, rec.x, rec.y);
		} else {
			System.out.println("InputSync.runHistoryRecord() unknown");
		}
	}

	private void doPackageReceived(InputSyncPackage p) throws Exception {
		if (p.modeSelect != null) {
			slideView.inputHistory.add(p.modeSelect);
			runHistoryRecord(p.modeSelect);
		} else if (p.touchRecord != null) {
			slideView.inputHistory.add(p.touchRecord);
			runHistoryRecord(p.touchRecord);
		}
	}

	private static class AppendableObjectOutputStream extends ObjectOutputStream {
		public AppendableObjectOutputStream(OutputStream out) throws IOException {
			super(out);
		}

		@Override
		protected void writeStreamHeader() throws IOException {
		}
	}

	long lastReceivedObject = -1;
	long lastReceivedObjectTime = 0;
	public void packageReceived(InputSyncPackageList list) throws Exception {
		System.out.println("packageReceived: " + list.list.size());

		int samePackageCount = 0;
		for (InputSyncPackage p : list.list) {
			if (p.no != 0 && p.no <= lastReceivedObject) {
				samePackageCount++;
				continue;
			}
			
			if (p.no == 0 && (System.currentTimeMillis() - lastReceivedObjectTime) < 500 )
				continue;
			
			lastReceivedObjectTime = System.currentTimeMillis();
			lastReceivedObject = p.no;
			doPackageReceived(p);

			File file = new File(redrawFile);
			ObjectOutputStream objectOutputStream;
			if (!file.exists())
				objectOutputStream = new ObjectOutputStream(new FileOutputStream(redrawFile));
			else
				objectOutputStream = new AppendableObjectOutputStream(new FileOutputStream(redrawFile, true));
			objectOutputStream.writeObject(p);
			objectOutputStream.flush();
		}
		System.out.println("received old package count: " + samePackageCount);
	}

	// public void redrawFromFile() {
	// if (redrawFile.equals(""))
	// return;
	// try {
	// FileInputStream fis = new FileInputStream(redrawFile);
	// ObjectInputStream objectInputStream = new ObjectInputStream(fis);
	// InputSyncPackage o;
	// while ( true ) {
	// o = (InputSyncPackage) objectInputStream.readObject();
	// doPackageReceived(o);
	// }
	// } catch (Exception e) {
	// System.out.println(e.toString());
	// e.printStackTrace();
	// }
	// }

	protected Socket connectionSocket;
	protected ObjectInputStream inFromClient;
	protected DataOutputStream outToClient;

	protected void connect(ServerSocket serverSocket) throws Exception {
		// System.out.println("PC waiting tablet connection...");
		connectionSocket = serverSocket.accept();
		inFromClient = new ObjectInputStream(connectionSocket.getInputStream());
		outToClient = new DataOutputStream(connectionSocket.getOutputStream());
		// System.out.println("tablet connected");
	}

	static final int FAULT_N = 0;

	protected LinkedBlockingQueue<InputSyncPackageList> receiveBuffer = new LinkedBlockingQueue<InputSyncPackageList>();

	/* network thread function */
	public boolean run() {
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(8041);
		} catch (Exception e) {
			return false;
		}

		while (true) {
			try {
				while (true) {
					connect(serverSocket);
					randomFault(FAULT_N, 1);

					InputSyncPackageList list = (InputSyncPackageList) inFromClient.readObject();
					randomFault(FAULT_N, 2);
					System.out.println("InputSyncPackage received: " + list.list);

					outToClient.writeLong(list.list.get(list.list.size() - 1).no);
					connectionSocket.close();

					receiveBuffer.add(list);
				}
			} catch (Exception e) {
				System.out.println("server >> " + e);
				try {
					connectionSocket.close();
				} catch (Exception e2) {}
			}
		}
	}

	/* ui thread function */
	public void run_ui() {
		InputSyncPackageList list = null;
		while (true) {
			try {
				if (list == null)
					list = receiveBuffer.take();
				packageReceived(list);
				list = null;
			} catch (Exception e) {
				System.out.println("error: " + e.toString());
			}
		}
	}

	protected void randomFault(int n, int no) throws Exception {
		if (n == 0)
			return;
		Random random = new Random();
		if (random.nextInt(n) == 0)
			throw new Exception("randomFault: " + no);
	}

}
