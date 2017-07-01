package com.hps.july.sync;

import java.sql.*;
import java.util.Properties;

/**
 * @author Safigullin Ildar
 *
 * Инормация о базе данных
 */
public class DB {
	private String prefix;
	private String driver;
	private String conn;
	private String user;
	private String password;
	private String roleName;

	public DB(Properties prop, String prefix) {
		this.prefix = prefix;
		driver = prop.getProperty(prefix + "Driver");
		conn = prop.getProperty(prefix + "Conn");
		user = prop.getProperty(prefix + "User");
		password = prop.getProperty(prefix + "Password");
		roleName = prop.getProperty(prefix + "RoleName");
	}

	public static Connection getConnection(DB db) {
		Connection con = null;
		System.out.println("Getting Connection to " + db.prefix);
		try {
			Class.forName(db.driver);
		} catch (Exception e) {
			System.out.println("Cannot load jdbc driver, class=" + db.driver);
			e.printStackTrace(System.out);
		}
		try {
			if (db.roleName == null) {
				con = DriverManager.getConnection(db.conn, db.user, db.password);
			} else {
				Properties props = new Properties();
				props.setProperty("user", db.user);
				props.setProperty("password", db.password);
				props.setProperty("roleName", db.roleName);
				con = DriverManager.getConnection(db.conn, props);
			}
		} catch (Exception e) {
			System.out.println("Cannot get " + db.prefix + " connection");
			e.printStackTrace(System.out);
		}
		try {
			con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED); //new			
		} catch (Exception e) {
			System.out.println("Уровень транзакции не поддерживается: " + e.getMessage());
		}
		if (con != null)
			System.out.println(db.prefix + " Connection OK !");
		return con;
	}
}
