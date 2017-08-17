package com.github.ashutoshgngwr.minventory.models;

public class User {
	private int id, accessLevel;
	private String username, privileges;

	public User(int id, String username, int accessLevel) {
		this.id = id;
		this.username = username.toLowerCase();
		this.accessLevel = accessLevel;
		setPrivilegesString();
	}

	private void setPrivilegesString() {
		switch (this.accessLevel) {
		case 0:
			this.privileges = "View/Add";
			return;
		case 1:
			this.privileges = "View/Add/Edit";
			return;
		case 2:
			this.privileges = "View/Add/Edit/Delete";
			return;
		case 3:
			this.privileges = "Admin/All";
			return;
		}
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public boolean isAdmin() {
		return accessLevel == 3;
	}

	public int getAccessLevel() {
		return accessLevel;
	}

	public void setAccessLevel(int accessLevel) {
		this.accessLevel = accessLevel;
		setPrivilegesString();
	}

	public String getPrivileges() {
		return privileges;
	}
}
