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
		super(argConNRI, argConNFS, "dbspositions", "Данные_NRI_mdb", "Посл_изм", new ForNRITableMap());
		this.prop = prop;
	}
	private static class ForNRITableMap extends ColumnMap {
		/**
		 * Конструктор
		 */
		ForNRITableMap() {
			super();
			// addMap( MSAccess, NRI, isKey)
			addMap("Код", "idrecord", true);
			addMap("№DAMPS", "dampsid", false);
			addMap("№GSM", "gsmid", false);
			//addMap("%WLAM", "wlanid", false);
			addMap("Название", "name", false);
			addMap("Название2", "name2", false);
			addMap("Разм_БС", "apparattype", false);
			addMap("Завод", "containertype", false);
			addMap("Тип_БС", "placetype", false);
			addMap("Где_БС", "apparatplace", false);
			addMap("Разм_опор", "antennaplace", false); //oporaplace
			addMap("Сущ", "isouropora", false);
			addMap("Тип_опор", "oporatype", false);
			addMap("Где_ант", "oporaplace", false); //antennaplace
			addMap("Высота", "heightopora", false);
			addMap("Отв_Фиш", "fiootvexpl", false);
			addMap("Тав№", "tabnumotvexpl", false);
			addMap("Эфир", "statebs", false);
			addMap("Демонтаж", "datederrick", false);
			addMap("Выезд", "dateonsitereview", false);
			addMap("Дата_марш_карт", "lastupdmarshkarta", false);
			addMap("Дата_список", "lastupdlistprohod", false);
			addMap("Посл_изм", "lastupdposition", false);

			// Колонки, предопределенные в таблице NRI
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
		//Вызов сторонней процедуры:
		//long time = System.currentTimeMillis() - 2 * (24 * 60 * 60 * 1000); //минус два дня
		//Date lastUpdateDate = new Date(time);
		long time;
		if (getLastUpdateDate() != null) {
			time = getLastUpdateDate().getTime();
		} else {
			System.out.println("getLastUpdateDate() == null");
			time = System.currentTimeMillis() - 2 * (24 * 60 * 60 * 1000); //минус два дня;
		}
		java.sql.Date lastUpdateDate = new java.sql.Date(time);
		System.out.println("init lastUpdateDate="+lastUpdateDate);		
		try {
			String blobDirPath = prop.getProperty("loadWayMapsDirPath");			
			System.out.println("Вызов сторонней процедуры loadWayMaps:");			
			com.hps.beeline.loader.MainProcessor.loadWayMaps(new Integer(qry.getQueryId()), getConTARGET_DB(), getConLOG_DB(), lastUpdateDate, blobDirPath);
			blobDirPath = prop.getProperty("loadPassListsDirPath");
			System.out.println("Вызов сторонней процедуры loadPassLists:");
			com.hps.beeline.loader.MainProcessor.loadPassLists(new Integer(qry.getQueryId()), getConTARGET_DB(), getConLOG_DB(), lastUpdateDate, blobDirPath);
			
		} catch (Exception e) {
			System.out.println("Вызов сторонней процедуры ERROR: " + e.getMessage());
			e.printStackTrace(System.out);
			return false;			
		}
		return true;
	}

}
