package com.hps.july.sync.msaccess.adapters;

import java.sql.*;
import java.util.Properties;

import com.hps.july.sync.*;
import com.hps.july.cdbc.lib.CDBCResultSet;

/**
 * @author Shafigullin Ildar
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ForNRITableAdaptor extends SimpleCollaboration {
	Properties prop = null;
	public ForNRITableAdaptor(DB argConNRI, DB argConNFS, Properties prop) {
		super(argConNRI, argConNFS, "dbspositions", "������_NRI_mdb", "����_���", new ForNRITableMap());
		this.prop = prop;
	}
	private static class ForNRITableMap extends ColumnMap {
		/**
		 * �����������
		 */
		ForNRITableMap() {
			super();
			// addMap( MSAccess, NRI, isKey)
			addMap("���", "idrecord", true);
			addMap("�DAMPS", "dampsid", false);
			addMap("�GSM", "gsmid", false);
			//addMap("%WLAM", "wlanid", false);
			addMap("��������", "name", false);
			addMap("��������2", "name2", false);
			addMap("����_��", "apparattype", false);
			addMap("�����", "containertype", false);
			addMap("���_��", "placetype", false);
			addMap("���_��", "apparatplace", false);
			addMap("����_����", "antennaplace", false); //oporaplace
			addMap("���", "isouropora", false);
			addMap("���_����", "oporatype", false);
			addMap("���_���", "oporaplace", false); //antennaplace
			addMap("������", "heightopora", false);
			addMap("���_���", "fiootvexpl", false);
			addMap("���", "tabnumotvexpl", false);
			addMap("����", "statebs", false);
			addMap("��������", "datederrick", false);
			addMap("�����", "dateonsitereview", false);
			addMap("����_����_����", "lastupdmarshkarta", false);
			addMap("����_������", "lastupdlistprohod", false);
			addMap("����_���", "lastupdposition", false);

			// �������, ���������������� � ������� NRI
			//addPredefinedColumnNRI("", "");
		}
	}
	/**
	 * @see com.hps.july.sync.SimpleCollaboration#doPostTask()
	 */
	protected boolean doPostTask(Query qry) {
		Statement st = null;
		ResultSet rs = null;
		String procedureName = null;
		try {
			st = getConTARGET_DB().createStatement();
			rs = st.executeQuery("EXECUTE PROCEDURE dbsLoadSprav('ALL', " + qry.getQueryId() + ")");
			procedureName = "dbsLoadSprav";
			if (rs.next()) {
				int resCode = rs.getInt(1);
				if (resCode != 0) {
					String resMsg = rs.getString(2);
					QueryProcessing.addLogMessage(getConTARGET_DB(), getQuery(), QueryProcessing.MSG_ERROR, resMsg);
				}
			}
			procedureName = "dbsLoadPositions";
			rs = st.executeQuery("EXECUTE PROCEDURE dbsLoadPositions(" + qry.getQueryId() + ")");
			if (rs.next()) {
				int resCode = rs.getInt(1);
				if (resCode != 0) {
					String resMsg = rs.getString(2);
					QueryProcessing.addLogMessage(getConTARGET_DB(), getQuery(), QueryProcessing.MSG_ERROR, resMsg);
				}
			}
		} catch (SQLException e) {
			System.out.println("Error executing procedures in ForNRITableAdaptor.doPostTask(): " + procedureName);
			System.out.println("ERROR: code=" + e.getErrorCode() + ", msg=" + e.getMessage());
			return false;
		} catch (Exception e) {
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace(System.out);
			return false;
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
			} catch (Exception e) {
			}
		}
		//����� ��������� ���������:
		//long time = System.currentTimeMillis() - 2 * (24 * 60 * 60 * 1000); //����� ��� ���
		//Date lastUpdateDate = new Date(time);
		long time;
		if (getLastUpdateDate() != null) {
			time = getLastUpdateDate().getTime();
		} else {
			System.out.println("getLastUpdateDate() == null");
			time = System.currentTimeMillis() - 2 * (24 * 60 * 60 * 1000); //����� ��� ���;
		}
		java.sql.Date lastUpdateDate = new java.sql.Date(time);
		System.out.println("init lastUpdateDate="+lastUpdateDate);		
		try {
			String blobDirPath = prop.getProperty("loadWayMapsDirPath");			
			System.out.println("����� ��������� ��������� loadWayMaps:");			
			com.hps.beeline.loader.MainProcessor.loadWayMaps(new Integer(qry.getQueryId()), getConTARGET_DB(), getConLOG_DB(), lastUpdateDate, blobDirPath);
			blobDirPath = prop.getProperty("loadPassListsDirPath");
			System.out.println("����� ��������� ��������� loadPassLists:");
			com.hps.beeline.loader.MainProcessor.loadPassLists(new Integer(qry.getQueryId()), getConTARGET_DB(), getConLOG_DB(), lastUpdateDate, blobDirPath);
			
		} catch (Exception e) {
			System.out.println("����� ��������� ��������� ERROR: " + e.getMessage());
			e.printStackTrace(System.out);
			return false;			
		}
		return true;
	}

}
