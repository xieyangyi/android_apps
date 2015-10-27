package com.fsl.fslclubs.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

public class DbDao {
	private Connection conn;
	private String driver;
	private String url;
	private String username;
	private String password;
	
	public DbDao() {
		Properties prop = new Properties();
		try {
			prop.load(this.getClass().getClassLoader().getResourceAsStream("DBConfig.properties"));
			this.driver = prop.getProperty("driver");
			this.url = prop.getProperty("url");
			this.username = prop.getProperty("username");
			this.password = prop.getProperty("password");			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public ResultSet query(String sql, Object...args) 
		throws Exception {
		PreparedStatement pstmt = getConnection().prepareStatement(sql);
		System.out.println("get connection success!");
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				pstmt.setObject(i+1, args[i]);
			}
		}
		return pstmt.executeQuery();
	}
	
	public boolean insert(String sql, Object...args)
		throws Exception {
		
		PreparedStatement pstmt = getConnection().prepareStatement(sql);
		System.out.println("get connection success!");
		for(int i = 0; i < args.length; i++) {
			pstmt.setObject(i+1, args[i]);
		}
		
		if(pstmt.executeUpdate() != 1)
			return false;
		else
			return true;
	}
	
	public void modify(String sql, Object...args) 
		throws Exception {
		
		PreparedStatement pstmt = getConnection().prepareStatement(sql);
		System.out.println("get connection success!");
		for (int i = 0; i < args.length; i++) {
			pstmt.setObject(i+1, args[i]);
		}
		
		pstmt.executeUpdate();
		pstmt.close();
	}
	
	private Connection getConnection()
			throws Exception {
			if(conn == null) {
				Class.forName(driver);
				conn = DriverManager.getConnection(url, username, password);
			}
			return conn;
		}
		
}
