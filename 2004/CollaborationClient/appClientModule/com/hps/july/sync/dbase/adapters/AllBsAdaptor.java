package com.hps.july.sync.dbase.adapters;

import com.hps.july.sync.*;
import com.ibm.math.BigDecimal;

import java.sql.*;
import java.util.*;

/**
 * @author Shafigullin Ildar
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class AllBsAdaptor extends SimpleCollaboration {

	private static class AllBsMap extends ColumnMap {
		/**
		 * Конструктор ColumnMap 
		 */
		AllBsMap() {
			super();
			// NRI(allPosForBelyakov), dBase(target), isKey
			addMap("Id", "Id", true);

			addMap("Gsm", "Gsm", false);
			addMap("Dcs", "Dcs", false);
			addMap("Damps", "Damps", false);
			addMap("Gsm_name", "Gsm_name", false);
			addMap("Damps_name", "Damps_name", false);
			addMap("Region", "Region", false);
			addMap("Address", "Address", false);
			addMap("Latitude", "Latitude", false);
			addMap("Longitude", "Longitude", false);
			addMap("Brief", "Brief", false);
			addMap("Responsib", "Responsib", false);
			addMap("Finder", "Finder", false);

		}
	}

	/**
	 * Constructor for AllBsAdaptor.
	 * @param sourseTargerDB
	 * @param sourseJoinDB
	 * @param sconLOG_DB
	 */
	public AllBsAdaptor(DB targerDB, DB sourseDB, DB sconLOG_DB) {
		super(targerDB, sourseDB, "allbs", "allPosForBelyakov", null, new AllBsMap(), sconLOG_DB);
	}

	/**
	 * @see com.hps.july.sync.SimpleCollaboration#doBeforeTask(Query)
	 */
	protected boolean doBeforeTask(Query qry) {
		Statement st = null;
		ResultSet rs = null;
		try {
			//"SET EXCLUSIVE ON":
			st = getConTARGET_DB().createStatement();
			st.execute("SET EXCLUSIVE ON");
			if (st != null)
				st.close();
			//EXECUTE PROCEDURE getAllPosForBelyakov:	
			st = getConJOIN_DB().createStatement();
			rs = st.executeQuery("EXECUTE PROCEDURE getAllPosForBelyakov(" + qry.getIdApp() + ")");
			if (rs.next()) {
				int resCode = rs.getInt(1);
				if (resCode != 0) {
					String resMsg = rs.getString(2);
					QueryProcessing.addLogMessage(getConLOG_DB(), getQuery(), QueryProcessing.MSG_ERROR, resMsg);
					return false;
				}
			}
			if (st != null)
				st.close();
			//удаляем allbs:
			st = getConTARGET_DB().createStatement();
			st.executeUpdate("delete from allbs");
			st = getConTARGET_DB().createStatement();
			st.executeUpdate("pack");
			return true;
		} catch (SQLException e) {
			System.out.println("Error executing procedure getAllPosForBelyakov = ");
			System.out.println("ERROR: code=" + e.getErrorCode() + ", msg=" + e.getMessage());
		} catch (Exception e) {
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace(System.out);
		} finally {
			//System.out.println("finally ok!!"); //temp
			try {
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
			} catch (Exception e) {
			}
		}
		return false;
	}
	/// Обобщенная процедура установки SQL-параметров
	protected boolean setParams(PreparedStatement st, Collection params, HashMap hashMap, int argStartParam) {
		boolean result = true;
		Iterator it = params.iterator();
		int i = argStartParam;
		while (it.hasNext()) {
			String key = (String) it.next();
			Object value = hashMap.get(key);
			try {
				if (value != null)
					st.setObject(i, value);
				else {
					if (key.equals("gsm") || key.equals("dcs") || key.equals("damps") || key.equals("latitude") || key.equals("longitude")) {
						st.setDouble(i, 0);
						//System.out.println("set Types.NUM for " + key);
					} else {
						//st.setNull(i, Types.CHAR);
						//System.out.println("set Types.CHAR for " + key);
						st.setString(i, "");
					}
				}
			} catch (SQLException e) {
				result = false;
				//qry.addLogMessage(qry.MSG_ERROR, "Невозможно установить SQL параметр #" + i + ", key=" + key + ", value=" + value);
				System.out.println("AllBsAdaptor Cannot set SQL parameter #" + i + ", key=" + key + ", value=" + value);
				System.out.println("AllBsAdaptor SQL Error=" + e.getErrorCode() + ", Message=" + e.getMessage());
				//e.printStackTrace(System.out);
			}
			i++;
		}
		return result;
	}
	/**
	 * @see com.hps.july.sync.Collaboration#findDeletedRowInSourseDB_DeleteTargerDB(Query)
	 */
	public void findDeletedRowInSourseDB_DeleteTargerDB(Query qry) {
		QueryProcessing.startProcessing(getConLOG_DB(), qry);
		QueryProcessing.addLogMessage(getConLOG_DB(), qry, QueryProcessing.MSG_ERROR, "findDeletedRowInSourseDB_DeleteTargerDB not Supported from AllBsAdaptor!!!");
		QueryProcessing.finishError(getConLOG_DB(), qry);
	}

}
