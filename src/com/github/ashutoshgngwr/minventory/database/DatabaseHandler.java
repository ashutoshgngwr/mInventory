package com.github.ashutoshgngwr.minventory.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.github.ashutoshgngwr.minventory.Main;

public class DatabaseHandler {

	protected static abstract class DbEntry {

		protected abstract boolean create(DatabaseHandler dbHandler) throws SQLException;

		protected abstract boolean update(DatabaseHandler dbHandler) throws SQLException;

		protected abstract boolean delete(DatabaseHandler dbHandler) throws SQLException;
	}

	protected static final String TABLE_USER = "INFORMATION_SCHEMA.USERS";
	protected static final String TABLE_PRODUCT = "product";
	protected static final String TABLE_TRANSACTION = "transaction";
	protected static final String TABLE_PRIVILIGES = "priviliges";
	protected static final String COLUMN_PRODUCT_ID = "id";
	protected static final String COLUMN_PRODUCT_NAME = "name";
	protected static final String COLUMN_PRODUCT_QUANTITY = "quantity";
	protected static final String COLUMN_TRANSACTION_TIME = "time";
	protected static final String COLUMN_TRANSACTION_PRODUCT_ID = "product_id";
	protected static final String COLUMN_TRANSACTION_TRADER = "trader";
	protected static final String COLUMN_TRANSACTION_QUANTITY = "quantity";
	protected static final String COLUMN_TRANSACTION_USERNAME = "username";
	protected static final String COLUMN_PRIVILIGES_USER_ID = "user_id";
	protected static final String COLUMN_PRIVILIGES_ACCESS_LEVEL = "access_level";
	protected static final String COLUMN_USER_ID = "id";
	protected static final String COLUMN_USER_NAME = "name";

	private static DatabaseHandler mInstance;

	private Connection dbConnection;

	public static DatabaseHandler connect(String username, String password)
			throws SQLException, ClassNotFoundException {
		if (mInstance == null)
			mInstance = new DatabaseHandler(username, password);

		return mInstance;
	}

	public static DatabaseHandler getInstance() {
		if (mInstance == null)
			throw new IllegalStateException("Connection link to database doesn't exist.");
		return mInstance;
	}

	private DatabaseHandler(String username, String password) throws ClassNotFoundException, SQLException {
		Class.forName("org.h2.Driver");
		this.dbConnection = DriverManager.getConnection("jdbc:h2:./data/all." + Main.APP_NAME, username, password);
		this.createTables();
	}

	private void createTables() throws SQLException {
		if (this.isConnectionClosed())
			return;

		this.executeDMLQuery("CREATE TABLE IF NOT EXISTS " + TABLE_PRODUCT + " (" + "`" + COLUMN_PRODUCT_ID
				+ "` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, " + "`" + COLUMN_PRODUCT_NAME
				+ "` VARCHAR(128) NOT NULL, " + "`" + COLUMN_PRODUCT_QUANTITY
				+ "` BIGINT UNSIGNED NOT NULL DEFAULT '0', " + "PRIMARY KEY (`" + COLUMN_PRODUCT_ID + "`), "
				+ "UNIQUE(`" + COLUMN_PRODUCT_NAME + "`));"

				+ "CREATE TABLE IF NOT EXISTS " + TABLE_TRANSACTION + "(" + "`" + COLUMN_TRANSACTION_TIME
				+ "` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " + "`" + COLUMN_TRANSACTION_PRODUCT_ID
				+ "` BIGINT UNSIGNED NOT NULL , " + "`" + COLUMN_TRANSACTION_TRADER + "` VARCHAR(128) NOT NULL, " + "`"
				+ COLUMN_TRANSACTION_QUANTITY + "` BIGINT NOT NULL, `" + COLUMN_TRANSACTION_USERNAME
				+ "` VARCHAR(32) NOT NULL, PRIMARY KEY (`" + COLUMN_TRANSACTION_TIME + "`));"

				+ " CREATE TABLE IF NOT EXISTS " + TABLE_PRIVILIGES + "(" + "`" + COLUMN_PRIVILIGES_USER_ID
				+ "` INT UNSIGNED NOT NULL, " + "`" + COLUMN_PRIVILIGES_ACCESS_LEVEL
				+ "` TINYINT UNSIGNED DEFAULT '0', " + "PRIMARY KEY (`" + COLUMN_PRIVILIGES_USER_ID + "`));");
	}

	protected ResultSet executeDQLQuery(String sql, Object... args) throws SQLException {
		System.out.println(sql); // TODO remove
		if (this.isConnectionClosed())
			return null;

		PreparedStatement preparedStatement = this.dbConnection.prepareStatement(sql);
		this.bindArgs(preparedStatement, args);
		return preparedStatement.executeQuery();
	}

	protected boolean executeDMLQuery(String sql, Object... args) throws SQLException {
		System.out.println(sql); // TODO remove
		if (this.isConnectionClosed())
			return false;

		PreparedStatement preparedStatement = this.dbConnection.prepareStatement(sql);
		this.bindArgs(preparedStatement, args);
		return preparedStatement.execute();
	}
	
	private void bindArgs(PreparedStatement preparedStatement, Object... args) throws SQLException {
		for (int i = 0; i < args.length; i++) {
			if (args[i] instanceof String)
				preparedStatement.setString(i + 1, (String) args[i]);
			else if (args[i] instanceof Integer)
				preparedStatement.setDouble(i + 1, (Integer) args[i]);
			else if (args[i] instanceof Long)
				preparedStatement.setDouble(i + 1, (Long) args[i]);
			else if (args[i] instanceof Float)
				preparedStatement.setDouble(i + 1, (Float) args[i]);
			else if (args[i] instanceof Double)
				preparedStatement.setDouble(i + 1, (Double) args[i]);
			else if (args[i] instanceof Timestamp)
				preparedStatement.setTimestamp(i + 1, (Timestamp) args[i]);
		}
	}

	public void create(DbEntry dbEntry) throws SQLException {
		dbEntry.create(this);
	}

	public void update(DbEntry dbEntry) throws SQLException {
		dbEntry.update(this);
	}

	public void delete(DbEntry dbEntry) throws SQLException {
		dbEntry.delete(this);
	}

	public void setAdminUserPrivileges() throws SQLException {
		ResultSet resultSet = this
				.executeDQLQuery("SELECT " + COLUMN_USER_ID + " FROM " + TABLE_USER + " WHERE admin='true'");

		if (resultSet.next())
			this.executeDMLQuery("INSERT INTO " + TABLE_PRIVILIGES + " VALUES(?, 3)", resultSet.getInt(COLUMN_USER_ID));
	}

	protected boolean isConnectionClosed() throws SQLException {
		return this.dbConnection == null || this.dbConnection.isClosed();
	}

	public void closeConnection() throws SQLException {
		if (this.isConnectionClosed())
			return;

		this.dbConnection.close();
		mInstance = null;
	}

	public User getCurrentUser(String username) throws SQLException {
		ResultSet resultSet = this.executeDQLQuery("SELECT " + TABLE_USER + "." + COLUMN_USER_ID + " AS "
				+ COLUMN_USER_ID + ", " + TABLE_USER + "." + COLUMN_USER_NAME + " AS " + COLUMN_USER_NAME + ", "
				+ TABLE_PRIVILIGES + "." + COLUMN_PRIVILIGES_ACCESS_LEVEL + " AS " + COLUMN_PRIVILIGES_ACCESS_LEVEL
				+ " FROM " + TABLE_USER + ", " + TABLE_PRIVILIGES + " WHERE " + TABLE_USER + "." + COLUMN_USER_NAME
				+ "=? AND " + TABLE_USER + "." + COLUMN_USER_ID + "=" + TABLE_PRIVILIGES + "."
				+ COLUMN_PRIVILIGES_USER_ID, username.toUpperCase());
		while (resultSet.next())
			return new User(resultSet);

		return null;
	}

	public void cleanTables() throws SQLException {
		this.executeDMLQuery("DELETE FROM " + TABLE_TRANSACTION + " WHERE " + "DATEDIFF('DAY', NOW(), "
				+ COLUMN_TRANSACTION_TIME + ") > 15;");
	}

	public void deleteAllTransactionsFor(long id) throws SQLException {
		this.executeDMLQuery("DELETE FROM " + TABLE_TRANSACTION + " WHERE " + COLUMN_TRANSACTION_PRODUCT_ID + "=?", id);
	}

	public Product getProduct(long productId) throws SQLException {
		ResultSet resultSet = this.executeDQLQuery("SELECT " + DatabaseHandler.COLUMN_PRODUCT_ID + ", "
				+ DatabaseHandler.COLUMN_PRODUCT_NAME + ", " + DatabaseHandler.COLUMN_PRODUCT_QUANTITY + " FROM "
				+ DatabaseHandler.TABLE_PRODUCT + " WHERE " + DatabaseHandler.COLUMN_PRODUCT_ID + "=?", productId);
		
		while(resultSet.next())
			return new Product(resultSet);
		
		return null;
	}

	public List<Product> searchProduct(String productName) throws SQLException {
		ResultSet resultSet = this.executeDQLQuery("SELECT " + COLUMN_PRODUCT_ID + ", " + COLUMN_PRODUCT_NAME + ", "
				+ COLUMN_PRODUCT_QUANTITY + " FROM " + TABLE_PRODUCT + " WHERE LOWER(" + COLUMN_PRODUCT_NAME
				+ ") LIKE CONCAT(LOWER(?), '%') LIMIT 3", productName);
		List<Product> results = new ArrayList<Product>(3);
		while (resultSet.next())
			results.add(new Product(resultSet));

		return results;
	}

	public List<Product> listProducts(int offset, int limit) throws SQLException {
		ResultSet resultSet = this.executeDQLQuery(
				"SELECT * FROM " + TABLE_PRODUCT + " ORDER BY id LIMIT " + limit + " OFFSET " + offset);
		List<Product> results = new ArrayList<>(limit);
		while (resultSet.next())
			results.add(new Product(resultSet));

		return results;
	}

	public List<User> listAllUsers() throws SQLException {
		ResultSet resultSet = this.executeDQLQuery("SELECT " + TABLE_USER + "." + COLUMN_USER_ID + " AS "
				+ COLUMN_USER_ID + ", " + TABLE_USER + "." + COLUMN_USER_NAME + " AS " + COLUMN_USER_NAME + ", "
				+ TABLE_PRIVILIGES + "." + COLUMN_PRIVILIGES_ACCESS_LEVEL + " AS " + COLUMN_PRIVILIGES_ACCESS_LEVEL
				+ " FROM " + TABLE_USER + ", " + TABLE_PRIVILIGES + " WHERE " + TABLE_USER + "." + COLUMN_USER_ID + "="
				+ TABLE_PRIVILIGES + "." + COLUMN_PRIVILIGES_USER_ID + " ORDER BY " + COLUMN_PRIVILIGES_ACCESS_LEVEL
				+ " DESC");

		List<User> results = new ArrayList<>();
		while (resultSet.next())
			results.add(new User(resultSet));

		return results;
	}

	public List<Transaction> listTransactions(int offset, int limit) throws SQLException {
		ResultSet resultSet = this.executeDQLQuery("SELECT " + TABLE_TRANSACTION + "." + COLUMN_TRANSACTION_TIME
				+ " AS " + COLUMN_TRANSACTION_TIME + ", " + TABLE_TRANSACTION + "." + COLUMN_TRANSACTION_TRADER + " AS "
				+ COLUMN_TRANSACTION_TRADER + ", " + TABLE_TRANSACTION + "." + COLUMN_TRANSACTION_QUANTITY + " AS "
				+ COLUMN_TRANSACTION_QUANTITY + ", " + TABLE_TRANSACTION + "." + COLUMN_TRANSACTION_USERNAME + " AS "
				+ COLUMN_TRANSACTION_USERNAME + ", " + TABLE_PRODUCT + "." + COLUMN_PRODUCT_ID + " AS "
				+ COLUMN_TRANSACTION_PRODUCT_ID + ", " + TABLE_PRODUCT + "." + COLUMN_PRODUCT_NAME + " AS "
				+ COLUMN_PRODUCT_NAME + " FROM " + TABLE_PRODUCT + ", " + TABLE_TRANSACTION + " WHERE " + TABLE_PRODUCT
				+ "." + COLUMN_PRODUCT_ID + "=" + TABLE_TRANSACTION + "." + COLUMN_TRANSACTION_PRODUCT_ID + " ORDER BY "
				+ TABLE_TRANSACTION + "." + COLUMN_TRANSACTION_TIME + " DESC LIMIT " + limit + " OFFSET "
				+ offset);
		List<Transaction> results = new ArrayList<>(limit);
		while (resultSet.next())
			results.add(new Transaction(resultSet));

		return results;
	}
}