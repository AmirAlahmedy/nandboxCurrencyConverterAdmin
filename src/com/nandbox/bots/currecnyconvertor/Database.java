package com.nandbox.bots.currecnyconvertor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
public class Database {
	
	public Connection connection = null;
	

	public Database(String dbName) throws SQLException
	{
		Connection connection = DriverManager.getConnection("jdbc:sqlite:"+dbName+".db");
		this.connection = connection;
	}
	
	public void createTable() throws SQLException
	{
		String sql = "CREATE TABLE IF NOT EXISTS messages (\n"
                + "	chatId varchar(255),\n"
                + "	message varchar(255) NOT NULL,\n"
                + "	time varchar(255) NOT NULL \n,"
                + "	sentToday varchar(255) NOT NULL \n"
                + ");";
		Statement s = this.connection.createStatement();
		s.execute(sql);
	}
	
	
	
	public void insertMessage(String chatId,String message,String time,String sentToday) throws SQLException
	{
		String sql = "insert into messages values (?,?,?,?)";
		
		PreparedStatement pstmt = this.connection.prepareStatement(sql);
        pstmt.setString(1, chatId);
        pstmt.setString(2, message);
        pstmt.setString(3, time);
        pstmt.setString(4,sentToday);
        pstmt.executeUpdate();
	}
	
	public void deleteMessage(int rowId) throws SQLException
	{
		 String sql = "DELETE FROM messages WHERE rowId = ?";
		
		PreparedStatement pstmt = this.connection.prepareStatement(sql);
        pstmt.setInt(1, rowId);
        pstmt.executeUpdate();
	}
	
	public  ArrayList<ArrayList<String>> getAllMessages() throws SQLException
	{
		String sql = "Select chatId,message,time,sentToday from messages";
		PreparedStatement pstmt = this.connection.prepareStatement(sql);
        
        ResultSet rs = pstmt.executeQuery();
        ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
       
       while (rs.next()) {
    	   ArrayList<String> currentAlert = new ArrayList<String>();
    	   currentAlert.add(rs.getString("chatId"));
    	   currentAlert.add(rs.getString("message"));
    	   currentAlert.add(rs.getString("time"));
    	   currentAlert.add(rs.getString("sentToday"));
    	   
    	   result.add(currentAlert);
       }
       return result;
	}
	
	public void resetSentToday() throws SQLException
	{
		String sql = "Update messages Set sentToday = '0'";
		PreparedStatement pstmt = this.connection.prepareStatement(sql);
		pstmt.executeUpdate();

	}
	
	public void setSentToday(String chatId,String message,String time) throws SQLException
	{
		String sql = "Update messages Set sentToday = '1' where chatId = ? and message = ? and time = ?";
		PreparedStatement pstmt = this.connection.prepareStatement(sql);
		pstmt.setString(1, chatId);
        pstmt.setString(2, message);
        pstmt.setString(3, time);
		pstmt.executeUpdate();
	}

}

