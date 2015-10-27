package com.fsl.fslclubs.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fsl.fslclubs.dao.*;

public class SignupActivityServlet extends HttpServlet {
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		
		response.setContentType("text/html; charset=GBK");
		PrintWriter out = response.getWriter();
		request.setCharacterEncoding("GBK");
		UserDao dao = new UserDao();
		String responseStr;

		String activity_id = request.getParameter("activityId");
		String user_id = request.getParameter("userId");
		responseStr = dao.signup_activity(activity_id, user_id);
		out.print(responseStr);
				
		out.flush();
		out.close();		
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException {
		doGet(request, response);
	}
	
	public void init() throws ServletException {
		
	}
	
	public SignupActivityServlet() {
		super();
	}
	
	public void destroy() {
		super.destroy();
	}
}
