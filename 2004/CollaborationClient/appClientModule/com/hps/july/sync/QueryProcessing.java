package com.hps.july.sync;

import java.sql.*;
import java.util.*;
import com.hps.july.cdbc.lib.*;

/**
 * @author Shafigullin Ildar
 * 
 * ���������-���������� �������� �� ������������� ������.
 */
public class QueryProcessing {
	public static final String MSG_INFO = "I";
	public static final String MSG_ERROR = "E";
	public static final String MSG_WARNING = "W";

	/// ���������� ������ �������� ��������������� �������
	public static Query findLatestQuery(DB logDB) {
		Query result = null;
		CDBCResultSet datesSet = new CDBCResultSet();
		Connection acon = DB.getConnection(logDB);
		datesSet.executeQuery(acon, "SELECT * FROM JOIN_DB_query WHERE reqstate = 'R' ORDER BY posttime ASC, idquery ASC", null, 0);
		ListIterator it = datesSet.listIterator();
		if (it.hasNext()) {
			result = new Query();
			CDBCRowObject ro = (CDBCRowObject) it.next();
			result.setReqType(ro.getColumn("reqtype").asString());
			Integer qryId = (Integer) ro.getColumn("idquery").asObject();
			result.setQueryId(qryId.intValue());
			result.setStartDate((java.sql.Date) ro.getColumn("selstartdate").asObject());
			result.setEndDate((java.sql.Date) ro.getColumn("selenddate").asObject());
			Integer idApp = (Integer) ro.getColumn("idapp").asObject();
			result.setIdApp(idApp.intValue());
		}
		try {
			acon.close();
		} catch (Exception e) {
		}
		return result;
	}

	/// �������� ������ ��������� �������
	public static boolean startProcessing(DB logDB, Query query) {
		boolean result = true;
		CDBCResultSet crs = new CDBCResultSet();
		String sql = "UPDATE JOIN_DB_query SET reqstate='O', starttime=? WHERE idquery=?";
		Connection acon = DB.getConnection(logDB);
		if (!crs.executeUpdate(acon, sql, new Object[] { new java.sql.Date(new java.util.Date().getTime()), new Integer(query.getQueryId())}))
			result = false;
		if (!addLogMessage(acon, query, MSG_INFO, "������ ��������� �������"))
			result = false;
		try {
			acon.close();
		} catch (Exception e) {
		}
		return result;
	}
	
	/// �������� ������ ��������� �������
	public static boolean startProcessing(Connection acon, Query query) {
		boolean result = true;
		CDBCResultSet crs = new CDBCResultSet();
		String sql = "UPDATE JOIN_DB_query SET reqstate='O', starttime=? WHERE idquery=?";
		if (!crs.executeUpdate(acon, sql, new Object[] { new java.sql.Date(new java.util.Date().getTime()), new Integer(query.getQueryId())}))
			result = false;
		if (!addLogMessage(acon, query, MSG_INFO, "������ ��������� �������"))
			result = false;

		return result;
	}	

		static boolean finishSuccess(Connection acon, Query query) {
		boolean result = true;
		CDBCResultSet crs = new CDBCResultSet();
		String sql = "UPDATE JOIN_DB_query SET reqstate='Y', finishtime=? WHERE idquery=?";
		if (!crs.executeUpdate(acon, sql, new Object[] { new java.sql.Date(new java.util.Date().getTime()), new Integer(query.getQueryId())}))
			result = false;
		if (!addLogMessage(acon, query, MSG_INFO, "�������� ��������� ��������� �������"))
			result = false;
		return result;
	}	

	/// �������� ������ ��� ������������ � �������
	public static boolean finishError(DB logDB, Query query) {
		boolean result = true;
		CDBCResultSet crs = new CDBCResultSet();
		String sql = "UPDATE JOIN_DB_query SET reqstate='E', finishtime=? WHERE idquery=?";
		Connection acon = DB.getConnection(logDB);
		if (!crs.executeUpdate(acon, sql, new Object[] { new java.sql.Date(new java.util.Date().getTime()), new Integer(query.getQueryId())}))
			result = false;
		if (!addLogMessage(acon, query, MSG_INFO, "��������� ��������� ��������� �������"))
			result = false;
		try {
			acon.close();
		} catch (Exception e) {
		}
		return result;
	}
	
	/// �������� ������ ��� ������������ � �������
	public static boolean finishError(Connection acon, Query query) {
		boolean result = true;
		CDBCResultSet crs = new CDBCResultSet();
		String sql = "UPDATE JOIN_DB_query SET reqstate='E', finishtime=? WHERE idquery=?";
		if (!crs.executeUpdate(acon, sql, new Object[] { new java.sql.Date(new java.util.Date().getTime()), new Integer(query.getQueryId())}))
			result = false;
		if (!addLogMessage(acon, query, MSG_INFO, "��������� ��������� ��������� �������"))
			result = false;
		return result;
	}	

	/// �������� ��������� � ������
	public static boolean addLogMessage(DB logDB, Query query, String argMessageType, String argMessageText) {
		CDBCResultSet crs = new CDBCResultSet();
		String sql = "INSERT INTO JOIN_DB_querylog(idquery, typemsg, txtmsg) VALUES (?, ?, ?)";
		Connection acon = DB.getConnection(logDB);
		boolean result = crs.executeUpdate(acon, sql, new Object[] { new Integer(query.getQueryId()), argMessageType, argMessageText });
		try {
			acon.close();
		} catch (Exception e) {
		}
		return result;
	}
	
	/// �������� ��������� � ������
	public static boolean addLogMessage(Connection acon, Query query, String argMessageType, String argMessageText) {
		CDBCResultSet crs = new CDBCResultSet();
		String sql = "INSERT INTO JOIN_DB_querylog(idquery, typemsg, txtmsg) VALUES (?, ?, ?)";
		return  crs.executeUpdate(acon, sql, new Object[] { new Integer(query.getQueryId()), argMessageType, argMessageText });
	}	

}
