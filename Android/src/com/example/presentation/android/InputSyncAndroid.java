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
package com.example.presentation.android;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

import com.example.presentation.Config;
import com.example.presentation.shared.InputHistory;
import com.example.presentation.shared.InputSyncPackage;
import com.example.presentation.shared.Util;

import android.util.Log;
import android.widget.Toast;

public class InputSyncAndroid implements Runnable {

	protected LinkedBlockingQueue<InputSyncPackage> sendBuffer = new LinkedBlockingQueue<InputSyncPackage>();
	
	protected ObjectOutputStream outToServer;
	protected BufferedReader inFromServer;
	protected Socket clientSocket;
	
	/** rastgele hata periyodu */
	static final int RANDOM_FAULT_N = 0;
	
	protected void connect() throws Exception {
		Log.i("tnr", "connecting to pc...");
		clientSocket = new Socket(Config.PC_IP, Config.PC_PORT);
		outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
		inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	}
	

	public void run() {
		Log.i("tnr", "InputSyncAndroid.run()");
		while (true) {
			try {
				while (true) {
					Object o = sendBuffer.take();
					Log.i("tnr", "sending object");
					connect();
					outToServer.writeObject(o);
					Util.randomFault(RANDOM_FAULT_N, 1);
					String serverResponse = inFromServer.readLine();
					if (!serverResponse.equals("ok")) {
						Log.i("tnr", "serverResponse: " + serverResponse);
						UtilAndroid.errorMessage(serverResponse);
					}

					Util.randomFault(RANDOM_FAULT_N, 2);
					clientSocket.close();
				}
			} catch (Exception e) {
				System.out.println("client >> " + e);
				
			} finally {
				try {
					clientSocket.close();
				} catch (Exception e2) {}
			}
		}
	}
	
	public void sync(InputHistory inputHistory) {
		
		for (Object event : inputHistory.events) {
			InputSyncPackage p = new InputSyncPackage(event);
			sendBuffer.offer(p);
		}
		inputHistory.events.clear();
	}

}
