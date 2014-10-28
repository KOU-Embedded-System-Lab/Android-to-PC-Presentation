package android_to_pc_presentation.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class InputSyncPackageList implements Serializable {
	private static final long serialVersionUID = 2093383543862663752L;
	
	public ArrayList<InputSyncPackage> list;
		
	public InputSyncPackageList() {
		this.list = new ArrayList<>();
	}
	
}
