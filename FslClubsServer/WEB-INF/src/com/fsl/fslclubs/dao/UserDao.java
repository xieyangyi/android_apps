package com.fsl.fslclubs.dao;

import java.sql.*;

public class UserDao {
	private final String LOGIN_PASSWORD_WRONG = "1";
	private final String LOGIN_PHONE_NOT_EXIST = "2";	
	private final String CHECK_PHONE_EXIST_TRUE = "1";
	private final String CHECK_PHONE_EXIST_FALSE = "2";	
	private final String REGISTER_SUCEESS = "1";
	private final String REGISTER_FAILED = "2";	
	private final String JOIN_CLUB_SUCCESS = "1";
	private final String JOIN_CLUB_FAILED = "2";
	private final String SAVE_USER_SUCEESS = "1";
	private final String SAVE_USER_FAILED = "2";
	private final String GET_ACTIVITY_INFO_SUCCESS = "1";
	private final String GET_ACTIVITY_INFO_FAILED = "2";
	private final String JOIN_ACTIVITY_SUCCESS = "1";
	private final String CHECK_CLUB_MEMBER_FAILED = "2";

	public String login(String phone, String password) {
		String sql = "select * from user_tbl where phone=?";
		String result = null;
		
		DbDao dbDao = new DbDao();
		try {
			ResultSet resultSet = dbDao.query(sql, phone);
			System.out.println("query finished");
			if(resultSet.next()) {				
				if(resultSet.getString("password").equals(password)) {
					result = (resultSet.getInt("id") + ";" 
							+ resultSet.getString("phone") + ";" 
							+ resultSet.getString("password") + ";" 
							+ resultSet.getString("name") + ";" 
							+ resultSet.getString("club") + ";" 
							+ resultSet.getString("sex") + ";" 
							+ resultSet.getString("email") + ";" 
							+ resultSet.getString("coreid") + ";" 
							+ resultSet.getString("signature") + ";" 
							+ resultSet.getString("legal_id") + ";" 
							+ resultSet.getString("activity") + ";"
					);
				} else {
					result = LOGIN_PASSWORD_WRONG; 			// password wrong
				}
			} else {
				result = LOGIN_PHONE_NOT_EXIST;         // phone wrong, may be register first
			}
		} catch (Exception e) {
			e.printStackTrace();			// network error, return null
		}
		
		System.out.println(result);
		return result;
	}	
	
	public String check_phone_exist(String phone) {
		String sql = "select id from user_tbl where phone=?";
		String result = null;
		
		DbDao dbDao = new DbDao();
		try {
			ResultSet resultSet = dbDao.query(sql, phone);
			System.out.println("query finished");
			if(resultSet.next())
				result = CHECK_PHONE_EXIST_TRUE;			    // phone exist
			else
				result = CHECK_PHONE_EXIST_FALSE;			// phone not exist
		} catch (Exception e) {
			e.printStackTrace();		// network error, return null
		}
		
		return result;
	}
	
	public String register(String phone, String password, String name) {
		String sql = "insert into user_tbl (phone, password, name) values(?, ?, ?)";
		String result = null;
		
		System.out.println("phone:" + phone + " password:" + password + " name:" + name);
		DbDao dbDao = new DbDao();
		try {
			boolean res = dbDao.insert(sql, phone, password, name);
			System.out.println("insert finished");
			if(res) {
				result = REGISTER_SUCEESS;
			} else {
				result = REGISTER_FAILED;		
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
				
		return result;
	}
	
	public String join_club(String clubId, String userId, String username) {
		// update club_tbl
		// get member_id from table first
		String sql = "select member_id from club_tbl where id=?";
		String originUsername = null;
		String updateUsername = null;
		
		DbDao dbDao = new DbDao();
		try {
			ResultSet resultSet = dbDao.query(sql, clubId);
			System.out.println("query finished");
			if(resultSet.next()) {
				originUsername = resultSet.getString("member_id");
			} else {
				System.out.println("club id not found!");
			}
		} catch (Exception e) {
			e.printStackTrace();		// network error, return null
		}
		
		// check whether userId already in member_id
		if(originUsername != null && originUsername.contains(username)) {
			System.out.println("user already in the club");
			return JOIN_CLUB_SUCCESS;
		}
		
		// modify member_id of the table
		if(originUsername != null && !originUsername.equals("null")) {
			updateUsername = originUsername + "," + username;
		} else {
			updateUsername = username;
		}
		sql = "update club_tbl set member_id=? where id=?";
		
		try {
			dbDao.modify(sql, updateUsername, clubId);
			System.out.println("modify finished");
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		// update user table
		// get member_id from table first
		sql = "select club from user_tbl where id=?";
		String originClub = null;
		String updateClub = null;
		
		try {
			ResultSet resultSet = dbDao.query(sql, userId);
			System.out.println("query finished");
			if(resultSet.next()) {
				originClub = resultSet.getString("club");
			} else {
				System.out.println("user id not found!");
			}
		} catch (Exception e) {
			e.printStackTrace();		// network error, return null
		}
		
		// check whether userId already in member_id
		if(originClub != null && originClub.contains(clubId)) {
			System.out.println("user already in the club");
			return JOIN_CLUB_SUCCESS;
		}
		
		// modify member_id of the table
		if(originClub != null && !originClub.equals("null")) {
			updateClub = originClub + "," + clubId;
		} else {
			updateClub = clubId;
		}
		sql = "update user_tbl set club=? where id=?";
		
		try {
			dbDao.modify(sql, updateClub, userId);
			System.out.println("modify finished");
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return JOIN_CLUB_SUCCESS;
	}
	
	public String update_user(String phone, String password, String name, 
			String club, String sex, String email, String coreid, 
			String signature, String legal_id) {
		
		String sql = "update user_tbl "
				+ "set password=?, name=?, club=?, sex=?, email=?, "
				+ "coreid=?, signature=?, legal_id=?"
				+ "where phone=?";
		
		DbDao dbDao = new DbDao();
		try {
			dbDao.modify(sql, password, name, club, sex, email, coreid, signature, legal_id, phone);
			System.out.println("modify finished");
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return SAVE_USER_SUCEESS;
	}
	
	public String getActivityInfo(String id) {
		
		String result = null;
		DbDao dbDao = new DbDao();
		ResultSet resultSet;
			
		if (id.equals("0")) {
			// get the latest activity if id=0
			String sql = "select * from activity_tbl where id=(select max(id) from activity_tbl);";
			try {
				resultSet = dbDao.query(sql, null);
				if(resultSet.next()) {
					result = resultSet.getInt("id") + ","
							+ resultSet.getString("name") + "," 
							+ resultSet.getString("icon") + "," 
							+ resultSet.getString("website") + ","
							+ resultSet.getString("address") + ","
							+ resultSet.getString("time") + ","
							+ resultSet.getString("expire_time");
				} else
					result = GET_ACTIVITY_INFO_FAILED;			// activity id not found
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("get max activity id failed");
			}
		} else {
			// get activity info by id when id > 0
			String sql = "select * from activity_tbl where id=?"; 
			try {
				resultSet = dbDao.query(sql, id);
				if(resultSet.next()) {
					result = resultSet.getInt("id") + ","
							+ resultSet.getString("name") + "," 
							+ resultSet.getString("icon") + "," 
							+ resultSet.getString("website") + ","
							+ resultSet.getString("address") + ","
							+ resultSet.getString("time") + ","
							+ resultSet.getString("expire_time");
				} else
					result = GET_ACTIVITY_INFO_FAILED;			// activity id not found
			} catch (Exception e) {
				e.printStackTrace();		// network error, return null
			}
		}
		
		return result;
	}
	
	public String signup_activity(String activity_id, String userId) {
		// get member_id from table first
		String sql = "select user from activity_tbl where id=?";
		String originUser = null;
		String updateUser = null;
		
		DbDao dbDao = new DbDao();
		try {
			ResultSet resultSet = dbDao.query(sql, activity_id);
			System.out.println("query finished");
			if(resultSet.next()) {
				originUser = resultSet.getString("user");
			} else {
				System.out.println("user not found!");
			}
		} catch (Exception e) {
			e.printStackTrace();		// network error, return null
		}
		
		// check whether userId already in member_id
		if(originUser != null && originUser.contains(userId)) {
			System.out.println("user already in the club");
			return JOIN_ACTIVITY_SUCCESS;
		}
		
		// modify member_id of the table
		if(originUser != null && !originUser.equals("null") && !originUser.equals("")) {
			updateUser = originUser + "," + userId;
		} else {
			updateUser = userId;
		}
		sql = "update activity_tbl set user=? where id=?";
		
		try {
			dbDao.modify(sql, updateUser, activity_id);
			System.out.println("modify finished");
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		// update user table
		// get member_id from table first
		sql = "select activity from user_tbl where id=?";
		String originActivity = null;
		String updateActivity = null;
		
		try {
			ResultSet resultSet = dbDao.query(sql, userId);
			System.out.println("query finished");
			if(resultSet.next()) {
				originActivity = resultSet.getString("activity");
			} else {
				System.out.println("user id not found!");
			}
		} catch (Exception e) {
			e.printStackTrace();		// network error, return null
		}
		
		// check whether userId already in member_id
		if(originActivity != null && originActivity.contains(activity_id)) {
			System.out.println("user already in the activity");
			return JOIN_ACTIVITY_SUCCESS;
		}
		
		// modify activity of the table
		if(originActivity != null && !originActivity.equals("null") && !originActivity.equals("")) {
			updateActivity = originActivity + "," + activity_id;
		} else {
			updateActivity = activity_id;
		}
		sql = "update user_tbl set activity=? where id=?";
		
		try {
			dbDao.modify(sql, updateActivity, userId);
			System.out.println("modify finished");
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return JOIN_ACTIVITY_SUCCESS;
	}
	
public String checkClubMember(String clubId) {
		
		String result = null;
		DbDao dbDao = new DbDao();
		ResultSet resultSet;
			
		// get club members using club id
		String sql = "select member_id from club_tbl where id=?"; 
		try {
			resultSet = dbDao.query(sql, clubId);
			if(resultSet.next()) {
				result = resultSet.getString("member_id");
				if (result == null)
					result = "";
			} else
				result = CHECK_CLUB_MEMBER_FAILED;			// activity id not found
		} catch (Exception e) {
			e.printStackTrace();		// network error, return null
		}
		
		return result;
	}
}
