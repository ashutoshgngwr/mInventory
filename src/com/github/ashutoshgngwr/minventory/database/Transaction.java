package com.github.ashutoshgngwr.minventory.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

public class Transaction extends DatabaseHandler.DbEntry {

	private String productName, trader, tradeType, username;
	private long productId, quantity;
	private Timestamp time;

	public Transaction(long productId, long quantity, String trader, String username) {
		this.productId = productId;
		this.quantity = quantity;
		this.trader = trader;
		this.username = username;
		this.tradeType = quantity < 0 ? "sold" : "bought";
	}

	protected Transaction(ResultSet resultSet) throws SQLException {
		this(resultSet.getInt(DatabaseHandler.COLUMN_TRANSACTION_PRODUCT_ID),
				resultSet.getInt(DatabaseHandler.COLUMN_TRANSACTION_QUANTITY),
				resultSet.getString(DatabaseHandler.COLUMN_TRANSACTION_TRADER),
				resultSet.getString(DatabaseHandler.COLUMN_TRANSACTION_USERNAME));

		this.productName = resultSet.getString(DatabaseHandler.COLUMN_PRODUCT_NAME);
		this.time = resultSet.getTimestamp(DatabaseHandler.COLUMN_TRANSACTION_TIME);
	}

	public long getProductId() {
		return this.productId;
	}

	public String getProductName() {
		return this.productName;
	}

	public long getQuantity() {
		return Math.abs(this.quantity);
	}

	public String getTime() {
		return this.time.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd MMM, hh:mma"));
	}

	public String getTrader() {
		return this.trader;
	}

	public String getTradeType() {
		return this.tradeType;
	}

	public String getUser() {
		return this.username;
	}

	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}

	public void setTrader(String trader) {
		this.trader = trader;
	}
	
	private boolean selectTimestamp(DatabaseHandler dbHandler) throws SQLException {
		ResultSet resultSet = dbHandler.executeDQLQuery("SELECT " + DatabaseHandler.COLUMN_TRANSACTION_TIME + " FROM "
				+ DatabaseHandler.TABLE_TRANSACTION + " ORDER BY " + DatabaseHandler.COLUMN_TRANSACTION_TIME + " DESC LIMIT 1");
		while(resultSet.next()) {
			this.time = resultSet.getTimestamp(DatabaseHandler.COLUMN_TRANSACTION_TIME);
			return true;
		}
		return false;
	}

	@Override
	protected boolean create(DatabaseHandler dbHandler) throws SQLException {
		return !dbHandler.executeDMLQuery(
				"INSERT INTO " + DatabaseHandler.TABLE_TRANSACTION + " VALUES(NOW(), ?, ?, ?, ?)", this.productId,
				this.trader, this.quantity, this.username) && this.selectTimestamp(dbHandler);
	}

	@Override
	protected boolean update(DatabaseHandler dbHandler) throws SQLException {
		return !dbHandler.executeDMLQuery("UPDATE " + DatabaseHandler.TABLE_TRANSACTION + " SET "
				+ DatabaseHandler.COLUMN_TRANSACTION_QUANTITY + "=?, " + DatabaseHandler.COLUMN_TRANSACTION_TRADER
				+ "=? " + "WHERE " + DatabaseHandler.COLUMN_TRANSACTION_TIME + "=?", this.quantity, this.trader,
				this.time);
	}

	@Override
	protected boolean delete(DatabaseHandler dbHandler) throws SQLException {
		return !dbHandler.executeDMLQuery("DELETE FROM " + DatabaseHandler.TABLE_TRANSACTION + " WHERE "
				+ DatabaseHandler.COLUMN_TRANSACTION_TIME + "=?", this.time);
	}
}
