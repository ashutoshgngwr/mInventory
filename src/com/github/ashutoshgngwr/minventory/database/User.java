package com.github.ashutoshgngwr.minventory.database;

import java.sql.ResultSet;
import java.sql.SQLException;

public class User extends DatabaseHandler.DbEntry {

	private int id, accessLevel;
	private String username, privileges, password;

	public User(String username, String password, int accessLevel) {
		this.username = username;
		this.password = password;
		this.accessLevel = accessLevel;
		this.setPrivilegesString();
	}

	protected User(ResultSet resultSet) throws SQLException {
		this(resultSet.getString(DatabaseHandler.COLUMN_USER_NAME).toLowerCase(), null,
				resultSet.getInt(DatabaseHandler.COLUMN_PRIVILIGES_ACCESS_LEVEL));
		this.id = resultSet.getInt(DatabaseHandler.COLUMN_USER_ID);
		this.setPrivilegesString();
		
	}

	public int getAccessLevel() {
		return this.accessLevel;
	}

	public int getId() {
		return this.id;
	}

	public String getPrivileges() {
		return this.privileges;
	}

	public String getUsername() {
		return this.username;
	}

	public boolean isAdmin() {
		return this.accessLevel == 3;
	}

	protected void setAccessLevel(int accessLevel) {
		this.accessLevel = accessLevel;
		this.setPrivilegesString();
	}

	protected void setId(int id) {
		this.id = id;
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

	public boolean setPassword(String password) {
		this.password = password;
		return true;
	}

	private boolean selectUserId(DatabaseHandler dbHandler) throws SQLException {
		ResultSet result = dbHandler.executeDQLQuery(
				"SELECT " + DatabaseHandler.COLUMN_USER_ID + " FROM " + DatabaseHandler.TABLE_USER + " WHERE name=?",
				this.username.toUpperCase());

		while (result.next()) {
			this.id = result.getInt(DatabaseHandler.COLUMN_USER_ID);
			System.out.println(id);
			return true;
		}
		return false;
	}

	@Override
	protected boolean create(DatabaseHandler dbHandler) throws SQLException {
		String dbPrivileges;
		switch (this.accessLevel) {
		case 0:
			dbPrivileges = "SELECT, INSERT";
			break;
		case 1:
			dbPrivileges = "SELECT, INSERT, UPDATE";
			break;
		case 2:
			dbPrivileges = "SELECT, INSERT, UPDATE, DELETE";
			break;
		default:
			dbPrivileges = "ALL";
		}

		return !dbHandler.executeDMLQuery("CREATE USER " + this.username.toUpperCase() + " PASSWORD ?", this.password)
				&& !dbHandler.executeDMLQuery("GRANT " + dbPrivileges + " ON " + DatabaseHandler.TABLE_PRODUCT + ", "
						+ DatabaseHandler.TABLE_TRANSACTION + ", " + DatabaseHandler.TABLE_PRIVILIGES + " TO "
						+ this.username.toUpperCase())
				&& this.selectUserId(dbHandler)
				&& !dbHandler.executeDMLQuery("INSERT INTO " + DatabaseHandler.TABLE_PRIVILIGES + " VALUES(?, ?);",
						this.id, this.accessLevel);
	}

	@Override
	protected boolean update(DatabaseHandler dbHandler) throws SQLException {
		return dbHandler.executeDMLQuery("SET PASSWORD ?", this.password) && this.setPassword(null);
	}

	@Override
	protected boolean delete(DatabaseHandler dbHandler) throws SQLException {
		return dbHandler
				.executeDMLQuery("DELETE FROM " + DatabaseHandler.TABLE_PRIVILIGES + " WHERE "
						+ DatabaseHandler.COLUMN_PRIVILIGES_USER_ID + "=?", this.id)
				&& dbHandler.executeDMLQuery("DROP USER " + this.username.toUpperCase());
	}
}
