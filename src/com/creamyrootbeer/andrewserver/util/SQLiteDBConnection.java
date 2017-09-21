package com.creamyrootbeer.andrewserver.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteDBConnection {
	private Connection conn = null;
	private boolean initialized = false;
	private Statement stmt;

	public SQLiteDBConnection(String name) {
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:" + name);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (conn != null) {
			initialized = true;
		}
	}

	public boolean runCommand(String sql) {
		try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(sql);
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public ResultSet query(String query) {
		try {
			this.stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			return rs;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void closeStmt() {
		try {
			this.stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void createTable(String tableName, String args) {
		boolean existent;
		try {
			Statement stmt = conn.createStatement();
			stmt.executeQuery("SELECT * FROM " + tableName).close();
			stmt.close();
			existent = true;
		} catch (Exception e) {
			existent = false;
		}

		if (!existent) {
			try {
				Statement stmt = conn.createStatement();
				stmt.executeUpdate("CREATE TABLE " + tableName + " (" + args + ");");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public Connection getConn() {
		return conn;
	}

	public boolean getInitialized() {
		return initialized;
	}

	public void closeDB() {
		try {
			this.conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.initialized = false;
	}
}
