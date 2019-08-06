package com.scrapexpress.easydatavalidator.sample;

import java.util.List;

public class Staff {
	
	private String name;
	
	private  int age;
	
	private boolean male;
	
	private List<String> features;
	
	private Staff leader;
	
	private List<Staff> teammates;
	
	public Staff() {
		
		
	}

	protected String getName() {
		return name;
	}

	protected int getAge() {
		return age;
	}

	protected boolean isMale() {
		return male;
	}

	protected List<String> getFeatures() {
		return features;
	}

	protected Staff getLeader() {
		return leader;
	}

	protected List<Staff> getTeammates() {
		return teammates;
	}

	protected void setName(String name) {
		this.name = name;
	}

	protected void setAge(int age) {
		this.age = age;
	}

	protected void setMale(boolean male) {
		this.male = male;
	}

	protected void setFeatures(List<String> features) {
		this.features = features;
	}

	protected void setLeader(Staff leader) {
		this.leader = leader;
	}

	protected void setTeammates(List<Staff> teammates) {
		this.teammates = teammates;
	}
	
	
	

}
