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
package android_to_pc_presentation.android;

import java.io.DataInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

import android.util.Log;
import android_to_pc_presentation.shared.InputHistory;
import android_to_pc_presentation.shared.InputSyncPackage;
import android_to_pc_presentation.shared.InputSyncPackageList;
import android_to_pc_presentation.shared.Util;

public class InputSyncAndroid implements Runnable {

	protected LinkedBlockingQueue<InputSyncPackage> sendBuffer = new LinkedBlockingQueue<InputSyncPackage>();

	protected ObjectOutputStream outToServer;
	protected DataInputStream inFromServer;
	protected Socket clientSocket;

	/** rastgele hata periyodu */
	static final int RANDOM_FAULT_N = 0;

	protected void connect() throws Exception {
		Log.i("tnr", "connecting to pc...");
		clientSocket = new Socket(Config.PC_IP, Config.PC_PORT);
		clientSocket.setSoTimeout(Config.SYNC_TIMEOUT);
		outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
		inFromServer = new DataInputStream(clientSocket.getInputStream());
	}

	public void run() {
		Log.i("tnr", "InputSyncAndroid.run()");

		long lastSentObjectAck = -1;
		InputSyncPackageList objectList = new InputSyncPackageList();
		while (true) {
			try {
				InputSyncPackage o;

				/* gonderilmis olanlari bul ve temizle */
				int i = 0;
				long l = -1;
				for (InputSyncPackage p : objectList.list) {
					assert l < p.no;
					if (p.no > lastSentObjectAck)
						break;
					l = p.no;
					i++;
				}
				objectList.list.subList(0, i).clear();
				
				/* en fazla 30 veriyi paketle */
				while ((objectList.list.size() < 30) && (o = sendBuffer.poll()) != null)
					objectList.list.add(o);
				
				/* hic veri yoksa blocking olarak veri al */
				if (objectList.list.size() == 0) {
					o = sendBuffer.take();
					objectList.list.add(o);
				}

				/* gonder */
				connect();
				outToServer.writeObject(objectList);
				Util.randomFault(RANDOM_FAULT_N, 1);
				lastSentObjectAck = inFromServer.readLong();
				Util.randomFault(RANDOM_FAULT_N, 2);
				clientSocket.close();
				Log.i("tnr", "sent: " + objectList.list.size());

				/* listeyi temizle */
				objectList.list.clear();

				/* ekrandaki hata mesajini kaldir */
				UtilAndroid.setSyncError(false);

			} catch (java.net.ConnectException e) {
				/* baglanti giderse yarim saniye bekle */
				UtilAndroid.setSyncError(true);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {}
			} catch (java.net.SocketTimeoutException e) {
				UtilAndroid.setSyncError(true);
			} catch (Exception e) {
				UtilAndroid.setSyncError(true);
				Log.i("tnr", "client >> " + e);
				e.printStackTrace();
			} finally {
				try {
					clientSocket.close();
				} catch (Exception e2) {}
			}
		}
	}

	long lastSync = 0;
	long lastActivity = 0;

	public void sync(InputHistory inputHistory) {
		long curr = System.currentTimeMillis();
		if (curr - lastSync < Config.SYNC_MIN_TIME)
			return;

		for (Object event : inputHistory.events) {
			if (event == null && curr - lastActivity > Config.SYNC_REFRESH_STOP_MS) {
				continue;
			}
			if (event != null)
				lastActivity = curr;
			// Log.i("tnr", "sync");
			// UtilAndroid.logHeap();
			InputSyncPackage p = new InputSyncPackage(event);
			sendBuffer.offer(p);
		}
		inputHistory.events.clear();
		lastSync = curr;
	}

}
