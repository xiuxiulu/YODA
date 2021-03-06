package edu.cmu.sv.domain.smart_house.GUI;

import edu.cmu.sv.domain.smart_house.SmartHouseCommandLineSystem;

public abstract class GUIElectronic extends GUIThing {
	private boolean state; // true -> on, false -> off
	
	public GUIElectronic(String name, GUIRoom room, boolean state, String correspondingURI) {
		super(name, room, correspondingURI);
		this.state = state;
	}

	public void toggleSwitch() {
		state = state ? false : true;
		if (Simulator.runningGUI)
			((MainFrame) SmartHouseCommandLineSystem.frame).refreshGUI();
	}
	
	public boolean getState() {
		return state;
	}
	public void setState(boolean newState) {state = newState;}

}