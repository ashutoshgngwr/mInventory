package com.github.ashutoshgngwr.minventory.util;

import java.sql.Connection;
import java.sql.SQLException;

public class DBUtils {
	private static Connection conn;

	public static void setConnection(Connection conn) {
		DBUtils.conn = conn;
	}
	
	public static void close() {
		if(conn != null) {
			try {
				conn.close();
			} catch (SQLException ignore) {
				
			}
		}
	}
}