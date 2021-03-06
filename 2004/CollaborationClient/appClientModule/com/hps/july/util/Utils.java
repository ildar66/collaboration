package com.hps.july.util;

import com.hps.july.cdbc.lib.CDBCResultSet;
import com.hps.july.cdbc.lib.CDBCRowObject;
import com.hps.july.sync.Query;
import com.hps.july.sync.QueryProcessing;

import java.sql.Connection;
import java.util.ListIterator;

public class Utils {
	public final static String DEFAULT_RATE = "DEFAULT_RATE";

	/// ��������� ����� ������������ ���������
	public static int getNamedValueInt(
		Connection con,
		Query qry,
		String argName) {
		CDBCResultSet rs = new CDBCResultSet();
		rs.executeQuery(
			con,
			"SELECT intvalue FROM namedvalues WHERE id = ?",
			new Object[] { argName },
			0);
		Integer res = null;
		ListIterator it = rs.listIterator();
		if (it.hasNext()) {
			CDBCRowObject ro = (CDBCRowObject) it.next();
			res = (Integer) ro.getColumn("intvalue").asObject();
		} else {
			QueryProcessing.addLogMessage(con, qry,
				QueryProcessing.MSG_ERROR,
				"�� ������� ��������� ���������: " + argName);
		}
		if (res != null)
			return res.intValue();
		else
			return 0;
	}
}
