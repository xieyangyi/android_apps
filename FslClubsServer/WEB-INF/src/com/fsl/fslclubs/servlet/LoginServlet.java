package com.fsl.fslclubs.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fsl.fslclubs.dao.*;

public class LoginServlet extends HttpServlet {
	private final static int REQUEST_LOGIN = 1;
	private final static int REQUEST_IS_PHONE_EXSIT = 2;
	private final static int REQUEST_REGISTER = 3;
	private final static int REQUEST_JOIN_CLUB = 4;
	private final static int REQUEST_SAVE_USER = 5;
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		
		response.setContentType("text/html; charset=GBK");
		PrintWriter out = response.getWriter();
		request.setCharacterEncoding("GBK");
		int requestCode = Integer.parseInt(request.getParameter("requestCode"));
		UserDao dao = new UserDao();
		String responseStr;
		
		switch (requestCode) {
		case REQUEST_LOGIN:
			String phone = request.getParameter("phone");
			String password = request.getParameter("password");
			responseStr = dao.login(phone, password);
			out.print(responseStr);
			break;
		case REQUEST_IS_PHONE_EXSIT:
			phone = request.getParameter("phone");
			responseStr = dao.check_phone_exist(phone);
			out.print(responseStr);
			break;
		case REQUEST_REGISTER:
			phone = request.getParameter("phone");
			password = request.getParameter("password");
			String name = request.getParameter("name");
			responseStr = dao.register(phone, password, name);
			out.print(responseStr);
			break;
		case REQUEST_JOIN_CLUB:
			String clubId = request.getParameter("clubId");
			String userId = request.getParameter("userId");
			String username = request.getParameter("username");
			responseStr = dao.join_club(clubId, userId, username);
			System.out.print("responseStr:" + responseStr);
			out.print(responseStr);
			break;
		case REQUEST_SAVE_USER:
			phone = request.getParameter("phone");
			password = request.getParameter("password");
			name = request.getParameter("name");
			String club = request.getParameter("club");
			String sex = request.getParameter("sex");
			String email = request.getParameter("email");
			String coreid = request.getParameter("coreid");
			String signature = request.getParameter("signature");
			String legal_id = request.getParameter("legal_id");
			responseStr = dao.update_user(phone, password, name, club, 
					sex, email, coreid, signature, legal_id);
			out.print(responseStr);
			break;
		default:
			responseStr = "request code not exist";
			out.print(responseStr);
		}
				
		out.flush();
		out.close();		
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException {
		doGet(request, response);
	}
	
	public void init() throws ServletException {
		
	}
	
	public LoginServlet() {
		super();
	}
	
	public void destroy() {
		super.destroy();
	}
}
