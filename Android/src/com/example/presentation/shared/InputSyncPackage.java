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
package com.example.presentation.shared;

import java.io.Serializable;

public class InputSyncPackage implements Serializable {
	private static final long serialVersionUID = 19001867160978848L;

	public InputHistory.ModeSelect modeSelect;
	public InputHistory.TouchRecord touchRecord;

	public InputSyncPackage(Object object) {
		if (object == null)
			return;
		if (object.getClass() == InputHistory.ModeSelect.class)
			this.modeSelect = (InputHistory.ModeSelect)object;
		if (object.getClass() == InputHistory.TouchRecord.class)
			this.touchRecord = (InputHistory.TouchRecord)object;
	}
	
	public InputSyncPackage(InputHistory.ModeSelect modeSelect, InputHistory.TouchRecord touchRecord) {
		this.modeSelect = modeSelect;
		this.touchRecord = touchRecord;
	}
}
