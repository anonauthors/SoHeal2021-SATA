package main.java.context;

import java.util.ArrayList;

public class State {
	
	String name;
	Integer id;
	ArrayList<String> actionSet=new ArrayList<String>();
	
	public State(String name, Integer id){
		this.name=name;
		this.id=id;
	}
	
	public void setAction(String s){
		actionSet.add(s);
	}
	
	public ArrayList<String> getActionSet() {
		return actionSet;
	}
	
}
