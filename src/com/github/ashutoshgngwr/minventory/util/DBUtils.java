package com.github.ashutoshgngwr.minventory.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.github.ashutoshgngwr.minventory.models.Item;
import com.github.ashutoshgngwr.minventory.models.LogItem;
import com.github.ashutoshgngwr.minventory.models.User;

public class DBUtils {
	private static Connection conn;

	public static void setConnection(Connection conn) {
		DBUtils.conn = conn;
	}

	public static void close() {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException ignore) {
				ignore.printStackTrace();
			}
			conn = null;
		}
	}

	public static void createTables() throws SQLException {
		if (conn == null)
			return;

		conn.createStatement()
				.execute("CREATE TABLE IF NOT EXISTS item (`id` INT UNSIGNED NOT NULL AUTO_INCREMENT, "
						+ "`name` VARCHAR(128) NOT NULL, `quantity` INT UNSIGNED NOT NULL DEFAULT '0', "
						+ "`deleted` INT UNSIGNED NOT NULL DEFAULT '0', " + "PRIMARY KEY (`id`), " + "UNIQUE(`name`))");

		conn.createStatement()
				.execute("CREATE TABLE IF NOT EXISTS log ( `time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
						+ "`item_id` INT UNSIGNED NOT NULL , `to_from` VARCHAR(120) NOT NULL, "
						+ "`quantity` INT NOT NULL, `deleted` INT UNSIGNED NOT NULL DEFAULT '0', PRIMARY KEY (`time`))");

		conn.createStatement().execute("CREATE TABLE IF NOT EXISTS privileges ( `user_id` INT UNSIGNED NOT NULL, "
				+ "`access_level` TINYINT UNSIGNED DEFAULT '0', PRIMARY KEY (`user_id`))");
	}

	public static List<LogItem> listLog(int offset, int limit) throws SQLException {
		if (conn == null)
			return new ArrayList<>();

		ResultSet resultSet = conn.createStatement().executeQuery(
				"SELECT log.time AS time, log.to_from AS to_from, log.quantity AS quantity, item.name AS name, item.deleted "
						+ "FROM item, log WHERE item.id = log.item_id AND log.deleted = 0 ORDER BY log.time DESC "
						+ "LIMIT " + limit + " OFFSET " + offset);
		List<LogItem> results = new ArrayList<>();

		while (resultSet.next())
			results.add(new LogItem(resultSet.getString("name"), resultSet.getString("time"),
					resultSet.getString("to_from"), resultSet.getInt("quantity"),
					resultSet.getInt("item.deleted") == 1));

		return results;
	}

	public static void cleanLogTable() throws SQLException {
		if (conn == null)
			return;

		conn.createStatement().execute("DELETE FROM item WHERE id in "
				+ "(SELECT item_id FROM log WHERE DATEDIFF('DAY', NOW(), log.time) > 15)");
		conn.createStatement().execute("DELETE FROM log WHERE DATEDIFF('DAY', NOW(), log.time) > 15");
	}

	public static List<Item> searchProduct(String searchString) throws SQLException {
		if (conn == null)
			return new ArrayList<>();

		PreparedStatement statement = conn.prepareStatement("SELECT id, name, quantity FROM item WHERE "
				+ "LOWER(name) LIKE CONCAT(LOWER(?), '%') AND deleted = 0 LIMIT 5");
		statement.setString(1, searchString);

		ResultSet resultSet = statement.executeQuery();
		List<Item> results = new ArrayList<>();
		while (resultSet.next())
			results.add(new Item(resultSet.getInt("id"), resultSet.getString("name"), resultSet.getInt("quantity")));

		statement.close();
		return results;
	}

	public static Item addItem(String itemName) throws SQLException {
		if (conn == null)
			return null;

		PreparedStatement statement = conn.prepareStatement("INSERT INTO item VALUES(NULL, ?, '0', '0')");
		statement.setString(1, itemName);
		statement.execute();
		statement.close();

		statement = conn.prepareStatement("SELECT id FROM item WHERE name=?");
		statement.setString(1, itemName);
		ResultSet resultSet = statement.executeQuery();

		if (resultSet.next()) {
			Item item = new Item(resultSet.getInt("id"), itemName, 0);
			statement.close();
			return item;
		}

		return null;
	}

	public static void addLog(int itemId, String toFrom, int quantity) throws SQLException {
		if (conn == null)
			return;

		PreparedStatement statement = conn.prepareStatement("INSERT INTO log VALUES(NOW(), ?, ?, ?, 0)");
		statement.setInt(1, itemId);
		statement.setString(2, toFrom);
		statement.setInt(3, quantity);
		statement.execute();
		statement.close();

		statement = conn.prepareStatement("UPDATE item SET quantity = quantity + ? WHERE id=?");
		statement.setInt(1, quantity);
		statement.setInt(2, itemId);
		statement.execute();
		statement.close();
	}

	public static void updateLogTable(String timestamp, String key, String value) throws SQLException {
		if (conn == null)
			return;

		PreparedStatement statement = conn.prepareStatement("UPDATE log SET " + key + "=? WHERE time=?");
		statement.setString(1, value);
		statement.setString(2, timestamp);
		statement.execute();
		statement.close();
	}

	public static void updateItemTable(int itemId, String key, String value) throws SQLException {
		if (conn == null)
			return;

		PreparedStatement statement = conn.prepareStatement("UPDATE item SET " + key + "=? WHERE id=?");
		statement.setString(1, value);
		statement.setInt(2, itemId);
		statement.execute();
		statement.close();
	}

	public static void deleteRow(String table, String key, String value) throws SQLException {
		if (conn == null)
			return;

		PreparedStatement statement = conn.prepareStatement("UPDATE " + table + " SET deleted='1' WHERE " + key + "=?");
		statement.setString(1, value);
		statement.execute();
		statement.close();
	}

	public static List<Item> listItems(int offset, int limit) throws SQLException {
		if (conn == null)
			return new ArrayList<>();

		ResultSet resultSet = conn.createStatement().executeQuery(
				"SELECT * FROM item WHERE deleted = 0 ORDER BY id LIMIT " + limit + " OFFSET " + offset + "");
		List<Item> results = new ArrayList<>();

		while (resultSet.next())
			results.add(new Item(resultSet.getInt("id"), resultSet.getString("name"), resultSet.getInt("quantity")));

		return results;
	}

	public static List<User> listUsers() throws SQLException {
		if (conn == null)
			return new ArrayList<>();

		ResultSet resultSet = conn.createStatement()
				.executeQuery("SELECT users.id AS id, users.name AS name, privileges.access_level AS "
						+ "access_level FROM INFORMATION_SCHEMA.USERS AS users, privileges WHERE "
						+ "users.id = privileges.user_id ORDER BY access_level DESC");

		List<User> results = new ArrayList<>();
		while (resultSet.next())
			results.add(
					new User(resultSet.getInt("id"), resultSet.getString("name"), resultSet.getInt("access_level")));

		return results;
	}

	public static void setAdminUserPrivileges() throws SQLException {
		if (conn == null)
			return;

		ResultSet resultSet = conn.createStatement()
				.executeQuery("SELECT id FROM INFORMATION_SCHEMA.USERS WHERE admin='true'");

		if (resultSet.next())
			conn.createStatement().execute("INSERT INTO privileges VALUES(" + resultSet.getInt("id") + ", 3)");
	}

	public static User getCurrentUser(String username) throws SQLException {
		if (conn == null)
			return null;

		ResultSet resultSet = conn.createStatement()
				.executeQuery("SELECT users.id AS id, users.name AS name, privileges.access_level AS "
						+ "access_level FROM INFORMATION_SCHEMA.USERS AS users, privileges WHERE users.name='"
						+ username.toUpperCase() + "' AND users.id = privileges.user_id");
		if (resultSet.next())
			return new User(resultSet.getInt("id"), resultSet.getString("name"), resultSet.getInt("access_level"));
		return null;
	}

	public static void deleteUser(String username) throws SQLException {
		if (conn == null)
			return;

		username = username.toUpperCase();
		PreparedStatement statement = conn.prepareStatement(
				"DELETE FROM privileges WHERE user_id in (SELECT id FROM INFORMATION_SCHEMA.USERS WHERE name=?)");
		statement.setString(1, username);
		statement.execute();
		statement.close();

		conn.createStatement().execute("DROP USER " + username);
	}

	public static void createUser(String username, String password, int accessLevel) throws SQLException {
		if (conn == null || accessLevel < 0 || accessLevel > 3)
			return;

		String privileges;
		switch (accessLevel) {
		case 0:
			privileges = "SELECT, INSERT";
			break;
		case 1:
			privileges = "SELECT, INSERT, UPDATE";
			break;
		case 2:
			privileges = "SELECT, INSERT, UPDATE, DELETE";
			break;
		default:
			privileges = "ALL";
		}

		username = username.toUpperCase();

		PreparedStatement statement = conn.prepareStatement("CREATE USER " + username + " PASSWORD ?");
		statement.setString(1, password);
		statement.execute();
		statement.close();

		conn.createStatement().execute("GRANT " + privileges + " ON item, log, privileges TO " + username);

		statement = conn.prepareStatement("SELECT id FROM INFORMATION_SCHEMA.USERS WHERE name=?");
		statement.setString(1, username);
		ResultSet resultSet = statement.executeQuery();

		if (resultSet.next()) {
			int id = resultSet.getInt("id");
			statement.close();
			statement = conn.prepareStatement("INSERT INTO privileges VALUES(?, ?)");
			statement.setInt(1, id);
			statement.setInt(2, accessLevel);
			statement.execute();
		}

		statement.close();
	}

	public static void changePassword(String newPassword) throws SQLException {
		if (conn == null)
			return;

		PreparedStatement statement = conn.prepareStatement("SET PASSWORD ?");
		statement.setString(1, newPassword);
		statement.execute();
		statement.close();
	}
}