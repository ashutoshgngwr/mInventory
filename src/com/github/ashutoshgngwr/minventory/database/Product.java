package com.github.ashutoshgngwr.minventory.database;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Product extends DatabaseHandler.DbEntry {

	private long id = -1, quantity;
	private String name;

	protected Product(ResultSet resultSet) throws SQLException {
		this(resultSet.getString(DatabaseHandler.COLUMN_PRODUCT_NAME),
				resultSet.getInt(DatabaseHandler.COLUMN_PRODUCT_QUANTITY));
		this.id = resultSet.getInt(DatabaseHandler.COLUMN_PRODUCT_ID);
	}

	public Product(String name, long quantity) {
		this.name = name;
		this.quantity = quantity;
	}

	public long getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public long getQuantity() {
		return this.quantity;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}

	private boolean selectId(DatabaseHandler dbHandler) throws SQLException {
		ResultSet result = dbHandler.executeDQLQuery("SELECT LAST_INSERT_ID() AS " + DatabaseHandler.COLUMN_PRODUCT_ID);
		System.out.println(result);
		while (result.next()) {
			this.id = result.getInt(DatabaseHandler.COLUMN_PRODUCT_ID);
			return true;
		}

		return false;
	}

	@Override
	protected boolean create(DatabaseHandler dbHandler) throws SQLException {
		return !dbHandler.executeDMLQuery("INSERT INTO " + DatabaseHandler.TABLE_PRODUCT + " VALUES(NULL, ?, ?)",
				this.name, this.quantity) && this.selectId(dbHandler);
	}

	@Override
	protected boolean update(DatabaseHandler dbHandler) throws SQLException {
		return !dbHandler.executeDMLQuery("UPDATE " + DatabaseHandler.TABLE_PRODUCT + " SET "
				+ DatabaseHandler.COLUMN_PRODUCT_NAME + "=?, " + DatabaseHandler.COLUMN_PRODUCT_QUANTITY + "=? WHERE "
				+ DatabaseHandler.COLUMN_PRODUCT_ID + "=?", this.name, this.quantity, this.id);
	}

	@Override
	protected boolean delete(DatabaseHandler dbHandler) throws SQLException {
		return !dbHandler.executeDMLQuery(
				"DELETE FROM " + DatabaseHandler.TABLE_PRODUCT + " WHERE " + DatabaseHandler.COLUMN_PRODUCT_ID + "=?",
				this.id);
	}
}
