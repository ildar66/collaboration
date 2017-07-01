package com.hps.july.sync;

import java.sql.*;
import java.util.*;
import com.hps.july.cdbc.lib.*;
import com.hps.july.util.TimeCounter;

/**
 * @author Shafigullin Ildar
 * 
 * Простой адаптер синхронизации таблиц БД.
 * Применим в простых случаях.
 * Производит обновление только тех записей, которые изменились
 * с момента последнего обновления
 */
public class SimpleCollaboration implements Collaboration {
	private String tableNameTARGET_DB;
	private String tableNameJOIN_DB;
	private Connection conTARGET_DB;
	private Connection conJOIN_DB;
	private Connection conLOG_DB;
	private ColumnMap colMap;

	private DB sconTARGET_DB;
	private DB sconJOIN_DB;
	private DB sconLOG_DB;
	private PreparedStatement preparedSelectJOIN_DB = null;
	private PreparedStatement preparedSelectTARGET_DB = null;
	private PreparedStatement preparedInsertTARGET_DB = null;
	private PreparedStatement preparedUpdateTARGET_DB = null;
	private PreparedStatement preparedDeleteTARGET_DB = null;
	private Query qry;
	private java.util.Date startQryTime;
	private int maxQryRecords;
	private final static long PROGRESS_UPDATE_TIME = 1 * 60 * 1000;
	private String lastUpdateDate_nameColumnJOIN_DB = null;

	public SimpleCollaboration(DB sourseTargerDB, DB sourseJoinDB, String argTableNameTARGET_DB, String argTableNameJOIN_DB, ColumnMap colMap) {
		conTARGET_DB = null;
		conJOIN_DB = null;
		sconTARGET_DB = sourseTargerDB;
		sconJOIN_DB = sourseJoinDB;
		tableNameTARGET_DB = argTableNameTARGET_DB;
		tableNameJOIN_DB = argTableNameJOIN_DB;
		this.colMap = colMap;
		sconLOG_DB = sconTARGET_DB;
	}

	public SimpleCollaboration(DB sourseTargerDB, DB sourseJoinDB, String argTableNameTARGET_DB, String argTableNameJOIN_DB, String lastUpdateDate_nameColumnJOIN_DB, ColumnMap colMap) {
		conTARGET_DB = null;
		conJOIN_DB = null;
		sconTARGET_DB = sourseTargerDB;
		sconJOIN_DB = sourseJoinDB;
		tableNameTARGET_DB = argTableNameTARGET_DB;
		tableNameJOIN_DB = argTableNameJOIN_DB;
		this.lastUpdateDate_nameColumnJOIN_DB = lastUpdateDate_nameColumnJOIN_DB;
		this.colMap = colMap;
		sconLOG_DB = sconTARGET_DB;
	}

	public SimpleCollaboration(
		DB sourseTargerDB,
		DB sourseJoinDB,
		String argTableNameTARGET_DB,
		String argTableNameJOIN_DB,
		String lastUpdateDate_nameColumnJOIN_DB,
		ColumnMap colMap,
		DB sconLOG_DB) {
		conTARGET_DB = null;
		conJOIN_DB = null;
		sconTARGET_DB = sourseTargerDB;
		sconJOIN_DB = sourseJoinDB;
		tableNameTARGET_DB = argTableNameTARGET_DB;
		tableNameJOIN_DB = argTableNameJOIN_DB;
		this.lastUpdateDate_nameColumnJOIN_DB = lastUpdateDate_nameColumnJOIN_DB;
		this.colMap = colMap;
		this.sconLOG_DB = sconLOG_DB;
	}

	public SimpleCollaboration(DB targerDB, DB logDB) {
		sconTARGET_DB = targerDB;
		sconLOG_DB = logDB;
	}
	/**
	 * 
	 * Класс для установки соответствия полей
	 * между системами JOIN_DB и TARGET_DB
	 */
	protected static class ColumnMap {
		protected HashMap keysMapJOIN_DB2TARGET_DB;
		protected HashMap columnMapJOIN_DB2TARGET_DB;
		protected HashMap predefinedColumnsTARGET_DB;

		/**
		 * Конструктор
		 * Унаследованный конструктор должен определять конкретное соответствие колонок
		 */
		protected ColumnMap() {
			keysMapJOIN_DB2TARGET_DB = new HashMap();
			columnMapJOIN_DB2TARGET_DB = new HashMap();
			predefinedColumnsTARGET_DB = new HashMap();
		}

		// Добавление соответствия колонок
		protected void addMap(String columnJOIN_DB, String columnTARGET_DB, boolean isKeyColumn) {
			String JOIN_DBVal = null;
			String TARGET_DBVal = null;
			if (columnJOIN_DB != null)
				JOIN_DBVal = columnJOIN_DB.toLowerCase();
			if (columnTARGET_DB != null)
				TARGET_DBVal = columnTARGET_DB.toLowerCase();

			if (isKeyColumn) {
				keysMapJOIN_DB2TARGET_DB.put(JOIN_DBVal, TARGET_DBVal);
			} else {
				columnMapJOIN_DB2TARGET_DB.put(JOIN_DBVal, TARGET_DBVal);
			}
		}

		// Добавление колонки с предопределенным значением
		protected void addPredefinedColumnTARGET_DB(String argKey, String argValue) {
			predefinedColumnsTARGET_DB.put(argKey.toLowerCase(), argValue);
		}
		/**
		 * Returns the columnMapJOIN_DB2TARGET_DB.
		 * @return HashMap
		 */
		public HashMap getColumnMapJOIN_DB2TARGET_DB() {
			return columnMapJOIN_DB2TARGET_DB;
		}

		/**
		 * Returns the keysMapJOIN_DB2TARGET_DB.
		 * @return HashMap
		 */
		public HashMap getKeysMapJOIN_DB2TARGET_DB() {
			return keysMapJOIN_DB2TARGET_DB;
		}

	}

	protected String generateClause(Collection params, String separator) {
		StringBuffer buf = new StringBuffer();
		Iterator it = params.iterator();
		boolean isFirst = true;
		while (it.hasNext()) {
			String key = (String) it.next();
			if (key == null)
				continue; //new
			if (!isFirst) {
				buf.append(separator);
			}
			isFirst = false;
			buf.append(key);
			buf.append(" = ? ");
		}
		return buf.toString();
	}

	/// Генерирует наименование или параметр колонки
	protected String generateColumnNameParam(Collection params, boolean argFirst, boolean isName) {
		StringBuffer buf = new StringBuffer();
		Iterator it = params.iterator();
		boolean isFirst = argFirst;
		while (it.hasNext()) {
			String key = (String) it.next();
			if (key == null)
				continue; //new			
			if (!isFirst) {
				buf.append(" , ");
			}
			isFirst = false;
			if (isName)
				buf.append(key);
			else
				buf.append("?");
		}
		return buf.toString();
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
				st.setObject(i, value);
			} catch (SQLException e) {
				result = false;
				//qry.addLogMessage(qry.MSG_ERROR, "Невозможно установить SQL параметр #" + i + ", key=" + key + ", value=" + value);
				System.out.println("Cannot set SQL parameter #" + i + ", key=" + key + ", value=" + value);
				System.out.println("SQL Error=" + e.getErrorCode() + ", Message=" + e.getMessage());
				//e.printStackTrace(System.out);
			}
			i++;
		}
		return result;
	}

	/// Заполнение ключевых полей бизнес-объекта из строки ResultSet
	protected boolean populateKeys(RowDBObject resObj, Collection keys, ResultSet rs) {
		boolean result = true;
		Iterator it = keys.iterator();
		while (it.hasNext()) {
			String keyName = (String) it.next();
			try {
				resObj.addKeyColumn(keyName, rs.getObject(keyName));
			} catch (SQLException e) {
				result = false;
				System.out.println("ERROR: Cannot get column: " + keyName);
				System.out.println("SQL Error=" + e.getErrorCode() + ", Message=" + e.getMessage());
				//e.printStackTrace(System.out);
				resObj.addKeyColumn(keyName, null);
			}
		}
		return result;
	}

	/// Заполнение полей бизнес-объекта из строки ResultSet
	protected boolean populateColumns(RowDBObject resObj, Collection columns, ResultSet rs) {
		boolean result = true;
		Iterator it = columns.iterator();
		while (it.hasNext()) {
			String colName = (String) it.next();
			try {
				resObj.addColumn(colName, rs.getObject(colName));
			} catch (SQLException e) {
				result = false;
				System.out.println("ERROR: Cannot get column: " + colName);
				System.out.println("SQL Error=" + e.getErrorCode() + ", Message=" + e.getMessage());
				//e.printStackTrace(System.out);
				resObj.addColumn(colName, null);
			}
		}
		//System.out.println("from populateColumns()" + resObj.dumpKeys());//temp
		return result;
	}

	/// Найти и заполнить поля объект из системы JOIN_DB
	protected RowDBObject findObjectJOIN_DB(RowDBObject argObj) {
		RowDBObject result = null;
		ResultSet rs = null;
		StringBuffer buf = new StringBuffer();
		if (preparedSelectJOIN_DB == null) {
			buf.append("SELECT * FROM ");
			buf.append(tableNameJOIN_DB);
			buf.append(" WHERE ");
			buf.append(generateClause(colMap.keysMapJOIN_DB2TARGET_DB.keySet(), " AND "));
			try {
				preparedSelectJOIN_DB = conJOIN_DB.prepareStatement(buf.toString());
				//System.out.println("preparedSelectJOIN_DB ="+buf.toString());//temp						
			} catch (SQLException e) {
				System.out.println("Cannot Prepare SQL=" + buf.toString());
				e.printStackTrace(System.out);
			}
		}
		if (setParams(preparedSelectJOIN_DB, colMap.keysMapJOIN_DB2TARGET_DB.keySet(), argObj.getKeysColumns(), 1)) {
			try {
				if (preparedSelectJOIN_DB != null) {
					rs = preparedSelectJOIN_DB.executeQuery();
					if (rs != null) {
						if (rs.next()) {
							//result = new RowDBObject();
							result = argObj;
							if (!populateColumns(result, colMap.columnMapJOIN_DB2TARGET_DB.keySet(), rs))
								result = null;
						} else {
							// JOIN_DB Object not found - issue warning
							QueryProcessing.addLogMessage(conLOG_DB, qry, QueryProcessing.MSG_WARNING, "Объект не найден в системе JOIN_DB по первичному ключу: " + argObj.dump());
						}
					}
				}
			} catch (SQLException e) {
				System.out.println("Error executing SQL=" + buf.toString());
				System.out.println("SQL Error=" + e.getErrorCode() + ", Message=" + e.getMessage());
				//e.printStackTrace(System.out);
			} catch (Exception e) {
				System.out.println("ERROR: " + e.getMessage());
				e.printStackTrace(System.out);
			}
		}
		try {
			rs.close();
		} catch (Exception e) {
		}
		return result;
	}

	/// Проверка существования объекта в системе TARGET_DB
	protected boolean isExistObjectTARGET_DB(RowDBObject argObj) {
		boolean result = false;
		ResultSet rs = null;
		StringBuffer buf = new StringBuffer();
		if (preparedSelectTARGET_DB == null) {
			buf.append("SELECT * FROM ");
			buf.append(tableNameTARGET_DB);
			buf.append(" WHERE ");
			buf.append(generateClause(colMap.keysMapJOIN_DB2TARGET_DB.values(), " AND "));
			//System.out.println("isExistObjectTARGET_DB buf.toString()" + buf.toString());//temp			
			try {
				preparedSelectTARGET_DB = conTARGET_DB.prepareStatement(buf.toString());
			} catch (SQLException e) {
				System.out.println("isExistObjectTARGET_DB.Cannot Prepare SQL=" + buf.toString());
				e.printStackTrace(System.out);
			}
		}
		if (setParams(preparedSelectTARGET_DB, colMap.keysMapJOIN_DB2TARGET_DB.keySet(), argObj.getKeysColumns(), 1)) {
			try {
				rs = preparedSelectTARGET_DB.executeQuery();
				if (rs.next())
					result = true;
			} catch (SQLException e) {
				System.out.println("Error executing SQL=" + buf.toString());
				System.out.println("SQL Error=" + e.getErrorCode() + ", Message=" + e.getMessage());
				//e.printStackTrace(System.out);
			} catch (Exception e) {
				System.out.println("ERROR: " + e.getMessage());
				e.printStackTrace(System.out);
			}
		}
		try {
			rs.close();
		} catch (Exception e) {
		}
		return result;
	}

	/// Проверка существования объекта в системе JOIN_DB
	private boolean isExistObjectJOIN_DB(RowDBObject argObj) {
		boolean result = false;
		ResultSet rs = null;
		StringBuffer buf = new StringBuffer();
		if (preparedSelectJOIN_DB == null) {
			buf.append("SELECT * FROM ");
			buf.append(tableNameJOIN_DB);
			buf.append(" WHERE ");
			buf.append(generateClause(colMap.keysMapJOIN_DB2TARGET_DB.keySet(), " AND "));
			try {
				preparedSelectJOIN_DB = conJOIN_DB.prepareStatement(buf.toString());
			} catch (SQLException e) {
				System.out.println("Cannot Prepare SQL=" + buf.toString());
				e.printStackTrace(System.out);
			}
		}
		if (setParams(preparedSelectJOIN_DB, colMap.keysMapJOIN_DB2TARGET_DB.values(), argObj.getKeysColumns(), 1)) {
			try {
				rs = preparedSelectJOIN_DB.executeQuery();
				if (rs.next())
					result = true;
			} catch (SQLException e) {
				System.out.println("Error executing SQL=" + buf.toString());
				System.out.println("SQL Error=" + e.getErrorCode() + ", Message=" + e.getMessage());
				//e.printStackTrace(System.out);
			} catch (Exception e) {
				System.out.println("ERROR: " + e.getMessage());
				e.printStackTrace(System.out);
			}
		}
		try {
			rs.close();
		} catch (Exception e) {
		}
		return result;
	}

	/// Генерация разделителя между основными и предопределенными колонками
	private String generateSetPredefinedSeparatorTARGET_DB(RowDBObject argObj) {
		if ((colMap.columnMapJOIN_DB2TARGET_DB.values().size() > 0) && (colMap.predefinedColumnsTARGET_DB.keySet().size() > 0))
			return " , ";
		return "";
	}

	/// Обновление записи в системе TARGET_DB
	protected boolean performUpdateTARGET_DB(RowDBObject argObj) {
		boolean result = false;
		StringBuffer buf = new StringBuffer();
		if (preparedUpdateTARGET_DB == null) {
			buf.append("UPDATE ");
			buf.append(tableNameTARGET_DB);
			buf.append(" SET ");
			buf.append(generateClause(colMap.columnMapJOIN_DB2TARGET_DB.values(), " , "));
			buf.append(generateSetPredefinedSeparatorTARGET_DB(argObj));
			buf.append(generateClause(colMap.predefinedColumnsTARGET_DB.keySet(), " , "));
			buf.append(" WHERE ");
			buf.append(generateClause(colMap.keysMapJOIN_DB2TARGET_DB.values(), " AND "));
			try {
				preparedUpdateTARGET_DB = conTARGET_DB.prepareStatement(buf.toString());
			} catch (SQLException e) {
				System.out.println("Cannot Prepare UPDATE SQL=" + buf.toString());
				System.out.println("SQL Error=" + e.getErrorCode() + ", Message=" + e.getMessage());
				//e.printStackTrace(System.out);
			} catch (Exception e) {
				System.out.println("ERROR: " + e.getMessage());
				e.printStackTrace(System.out);
			}
		}
		setParams(preparedUpdateTARGET_DB, colMap.columnMapJOIN_DB2TARGET_DB.keySet(), argObj.getColumns(), 1);
		//setPredefinedParamsTARGET_DB(preparedUpdateTARGET_DB, argObj, colMap.columnMapJOIN_DB2TARGET_DB.keySet().size() + 1);
		setParams(preparedUpdateTARGET_DB, colMap.predefinedColumnsTARGET_DB.keySet(), colMap.predefinedColumnsTARGET_DB, colMap.columnMapJOIN_DB2TARGET_DB.keySet().size() + 1);
		//setKeysParamsTARGET_DB(preparedUpdateTARGET_DB, argObj, argObj.valuesTARGET_DB.size() + argObj.colMap.predefinedColumnsTARGET_DB.size() + 1);
		setParams(
			preparedUpdateTARGET_DB,
			colMap.keysMapJOIN_DB2TARGET_DB.keySet(),
			argObj.getKeysColumns(),
			colMap.columnMapJOIN_DB2TARGET_DB.keySet().size() + colMap.predefinedColumnsTARGET_DB.keySet().size() + 1);
		try {
			preparedUpdateTARGET_DB.executeUpdate();
			result = true;
		} catch (SQLException e) {
			System.out.println("Error executing UPDATE SQL = " + buf.toString());
			System.out.println("SQL Error=" + e.getErrorCode() + ", Message=" + e.getMessage());
			//e.printStackTrace(System.out);
		} catch (Exception e) {
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace(System.out);
		}
		return result;
	}

	/// Добавление объекта в систему TARGET_DB
	protected boolean performInsertTARGET_DB(RowDBObject argObj) {
		boolean result = false;
		StringBuffer buf = new StringBuffer();
		if (preparedInsertTARGET_DB == null) {
			buf.append("INSERT INTO ");
			buf.append(tableNameTARGET_DB);
			buf.append(" ( ");
			buf.append(generateColumnNameParam(colMap.keysMapJOIN_DB2TARGET_DB.values(), true, true));
			buf.append(generateColumnNameParam(colMap.columnMapJOIN_DB2TARGET_DB.values(), false, true));
			buf.append(generateColumnNameParam(colMap.predefinedColumnsTARGET_DB.keySet(), false, true));
			buf.append(" ) VALUES ( ");
			buf.append(generateColumnNameParam(colMap.keysMapJOIN_DB2TARGET_DB.values(), true, false));
			buf.append(generateColumnNameParam(colMap.columnMapJOIN_DB2TARGET_DB.values(), false, false));
			buf.append(generateColumnNameParam(colMap.predefinedColumnsTARGET_DB.values(), false, false));
			buf.append(" )");
			try {
				preparedInsertTARGET_DB = conTARGET_DB.prepareStatement(buf.toString());
			} catch (SQLException e) {
				System.out.println("Cannot Prepare INSERT SQL=" + buf.toString());
				e.printStackTrace(System.out);
			} catch (Exception e) {
				System.out.println("ERROR: " + e.getMessage());
				e.printStackTrace(System.out);
			}
		}
		setParams(preparedInsertTARGET_DB, colMap.keysMapJOIN_DB2TARGET_DB.keySet(), argObj.getKeysColumns(), 1);
		setParams(preparedInsertTARGET_DB, colMap.columnMapJOIN_DB2TARGET_DB.keySet(), argObj.getColumns(), colMap.keysMapJOIN_DB2TARGET_DB.keySet().size() + 1);
		setParams(
			preparedInsertTARGET_DB,
			colMap.predefinedColumnsTARGET_DB.keySet(),
			colMap.predefinedColumnsTARGET_DB,
			colMap.keysMapJOIN_DB2TARGET_DB.keySet().size() + colMap.columnMapJOIN_DB2TARGET_DB.keySet().size() + 1);
		try {
			preparedInsertTARGET_DB.executeUpdate();
			result = true;
		} catch (SQLException e) {
			System.out.println("Error executing INSERT SQL = " + buf.toString());
			System.out.println("SQL Error=" + e.getErrorCode() + ", Message=" + e.getMessage());
		} catch (Exception e) {
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace(System.out);
		}
		return result;
	}

	/// Добавление/обновление объекта в системе TARGET_DB
	protected boolean updateObjectTARGET_DB(RowDBObject argObj) {
		//System.out.println("SimpleCollaboration.updateObjectTARGET_DB argObj=" + argObj.dump());//temp						
		boolean result = true;
		if (isExistObjectTARGET_DB(argObj)) {
			if (!performUpdateTARGET_DB(argObj)) {
				//System.out.println("performUpdateTARGET_DB = false");//temp
				result = false;
			}
		} else {
			if (!performInsertTARGET_DB(argObj)) {
				//System.out.println("performInsertTARGET_DB = false");//temp
				result = false;
			}
		}
		if (result) {
			if (!makeRelationTARGET_DB(argObj)) {
				//System.out.println("makeRelationTARGET_DB = false");//temp
				result = false;
			}
		}
		return result;
	}

	/// Установка связи с существующим объектом TARGET_DB
	protected boolean makeRelationTARGET_DB(RowDBObject argObj) {
		return true;
	}

	/// Удаление объекта в системе TARGET_DB
	private boolean deleteObjectTARGET_DB(RowDBObject argObj) {
		boolean result = false;
		PreparedStatement st = null;
		StringBuffer buf = new StringBuffer();
		buf.append("UPDATE ");
		buf.append(tableNameTARGET_DB);
		buf.append(" SET recordstatus = 'D' ");
		buf.append(" WHERE ");
		buf.append(generateClause(colMap.keysMapJOIN_DB2TARGET_DB.values(), " AND "));
		try {
			st = conTARGET_DB.prepareStatement(buf.toString());
		} catch (SQLException e) {
			System.out.println("Cannot Prepare UPDATE SQL=" + buf.toString());
			e.printStackTrace(System.out);
		} catch (Exception e) {
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace(System.out);
		}
		setParams(st, colMap.keysMapJOIN_DB2TARGET_DB.values(), argObj.getKeysColumns(), 1);
		try {
			st.executeUpdate();
			result = true;
		} catch (SQLException e) {
			System.out.println("Error executing UPDATE SQL = " + buf.toString());
			e.printStackTrace(System.out);
		} catch (Exception e) {
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace(System.out);
		}
		try {
			st.close();
		} catch (Exception e) {
		}
		return result;
	}

	/**
	 * Поиск объекта в системе JOIN_DB
	 * и передача в систему TARGET_DB
	 */
	protected boolean findJOIN_DBUpdateTARGET_DB(RowDBObject argObj) {
		boolean result = false;
		RowDBObject objJOIN_DB = findObjectJOIN_DB(argObj);
		if (objJOIN_DB != null) {
			result = updateObjectTARGET_DB(objJOIN_DB);
		} else {
			// Object not found in JOIN_DB due to changes in dataset
			//   treat this fact as warning only
			result = true;
		}
		return result;
	}

	/**
	 * Поиск объекта в системе JOIN_DB
	 * и удаление в системе TARGET_DB в случае отсутствия в JOIN_DB
	 */
	private boolean checkJOIN_DBDeleteTARGET_DB(RowDBObject argObj) {
		boolean result = false;
		if (!isExistObjectJOIN_DB(argObj)) {
			//System.out.println("No isExistObjectJOIN_DB ="+ argObj);//temp
			result = deleteObjectTARGET_DB(argObj);
		} else {
			//System.out.println("Yes isExistObjectJOIN_DB ="+ argObj);//temp			
			result = true;
		}
		return result;
	}

	/// Заполнение ключевых полей объекта из CDBCRowObject
	protected void populateKeysCDBC(RowDBObject resObj, Collection keys, CDBCRowObject rs) {
		Iterator it = keys.iterator();
		while (it.hasNext()) {
			String keyName = (String) it.next();
			Object value = rs.getColumn(keyName).asObject();
			//System.out.println("Populating: key=" + keyName + ", value=" + value.toString());
			resObj.addKeyColumn(keyName, value);
		}
	}

	/// Сформировать SQL для определения изменившихся записей в JOIN_DB
	protected String makeChangesSQLJOIN_DB() {
		StringBuffer result = new StringBuffer();
		result.append("SELECT * FROM ");
		result.append(tableNameJOIN_DB);
		java.sql.Timestamp lastdate = getLastUpdateDate();
		if (lastdate != null && lastUpdateDate_nameColumnJOIN_DB != null) {
			result.append(" WHERE ");
			result.append(lastUpdateDate_nameColumnJOIN_DB + " > ? ");
		}
		return result.toString();
	}

	/// Сформировать параметры SQL для определения изменившихся записей
	private Object[] getChangesSQLParams() {
		if (lastUpdateDate_nameColumnJOIN_DB == null)
			return null;
		java.sql.Timestamp lastdate = getLastUpdateDate();
		if (lastdate != null)
			return new Object[] { lastdate };
		else
			return new Object[] {
		};
	}

	/// Получить время последнего обновления таблицы
	protected java.sql.Timestamp getLastUpdateDate() {
		java.sql.Timestamp result = null;
		CDBCResultSet datesSet = new CDBCResultSet();
		datesSet.executeQuery(conLOG_DB, "SELECT * FROM join_db_nriupdate WHERE tablename = ? and idapp = ?", new Object[] { tableNameTARGET_DB, new Integer(qry.getIdApp())}, 0);
		ListIterator it = datesSet.listIterator();
		if (it.hasNext()) {
			Object resDate = ((CDBCRowObject) it.next()).getColumn("updatetime").asObject();
			result = (java.sql.Timestamp) resDate;
		}
		return result;
	}

	/// Сформировать дату последнего обновления
	private java.sql.Timestamp makeLastUpdateDate() {
		final long ONE_DAY_MSECS = 86400000;
		java.util.Date updDate = new java.util.Date();
		java.sql.Timestamp resDate = new java.sql.Timestamp(updDate.getTime() - ONE_DAY_MSECS);
		return resDate;
	}

	/// Обновление даты последнего обновления данных в системе TARGET_DB
	private void updateLastTransferDateTARGET_DB() {
		PreparedStatement st = null;
		String buf1 = "INSERT INTO join_db_nriupdate (tablename, idapp, updatetime) VALUES (?,?,?)";
		String buf2 = "UPDATE join_db_nriupdate SET updatetime = ? WHERE tablename = ? AND idapp = ?";

		CDBCResultSet crs = new CDBCResultSet();
		if (!crs.executeUpdate(conLOG_DB, buf1, new Object[] { tableNameTARGET_DB, new Integer(qry.getIdApp()), makeLastUpdateDate()})) {
			crs.executeUpdate(conLOG_DB, buf2, new Object[] { makeLastUpdateDate(), tableNameTARGET_DB, new Integer(qry.getIdApp())});
		}
	}

	/// Отметка успешного выполнения переноса
	protected void markSuccessTransfer() {
		updateLastTransferDateTARGET_DB();
		QueryProcessing.finishSuccess(conLOG_DB, qry);
		System.out.println("TRANSFER SUCCESS for Table " + tableNameTARGET_DB);
	}

	/// Отметка ошибки переноса данных
	protected void markErrorTransfer() {
		QueryProcessing.finishError(conLOG_DB, qry);
		System.out.println("TRANSFER ERROR for Table " + tableNameTARGET_DB);
	}

	/// Отметка успешного выполнения удаления
	private void markSuccessDelete() {
		QueryProcessing.finishSuccess(conLOG_DB, qry);
		System.out.println("DELETE SUCCESS for Table " + tableNameTARGET_DB);
	}

	/// Отметка ошибки переноса данных
	private void markErrorDelete() {
		QueryProcessing.finishError(conLOG_DB, qry);
		System.out.println("DELETE ERROR for Table " + tableNameTARGET_DB);
	}

	/// Отметка времени выполнения запроса
	private void startProgressIndicator(int argMaxRecords) {
		startQryTime = new java.util.Date();
		maxQryRecords = argMaxRecords;
	}

	/// Отметка времени выполнения запроса
	private void markProgressIndicator(int argCurRecord) {
		java.util.Date currQryTime;
		currQryTime = new java.util.Date();
		if ((currQryTime.getTime() - startQryTime.getTime()) > PROGRESS_UPDATE_TIME) {
			int percent = 100 * argCurRecord / maxQryRecords;
			System.out.println("PROGRESS for " + tableNameTARGET_DB + ": " + percent + " %, " + argCurRecord + " of " + maxQryRecords + " records transferred");
			startQryTime = currQryTime;
		}
	}

	/// Проверка изменения всех объектов в JOIN_DB и обновление в TARGET_DB - содержательная часть
	protected void doFindChangesJOIN_DBUpdateTARGET_DB() {
		boolean result = true;
		int curRow = 0;
		if ((conJOIN_DB != null) && (conTARGET_DB != null)) {
			QueryProcessing.startProcessing(conLOG_DB, qry);
			result = doBeforeTask(qry);
			if (result) {
				CDBCResultSet changesSet = new CDBCResultSet();
				String changesSQL = makeChangesSQLJOIN_DB();
				Object[] changesSQLParams = getChangesSQLParams();
				changesSet.executeQuery(conJOIN_DB, changesSQL, changesSQLParams, 0);
				ListIterator it = changesSet.listIterator();
				startProgressIndicator(changesSet.getRowsCount());
				while (it.hasNext()) {
					CDBCRowObject ro = (CDBCRowObject) it.next();
					RowDBObject objJOIN_DB = new RowDBObject();
					populateKeysCDBC(objJOIN_DB, colMap.keysMapJOIN_DB2TARGET_DB.keySet(), ro);
					//System.out.println("SimpleColl.doFindChangesJOIN_DBUpdateTARGET_DB() objJOIN_DB ="+objJOIN_DB.dump());//temp
					if (!findJOIN_DBUpdateTARGET_DB(objJOIN_DB)) {
						String msg = "Ошибка переноса данных: " + tableNameJOIN_DB + ", Ключевые поля: " + objJOIN_DB.dump();
						System.out.println(msg);
						QueryProcessing.addLogMessage(conLOG_DB, qry, QueryProcessing.MSG_ERROR, msg);
						result = false;
					}
					curRow++;
					markProgressIndicator(curRow);
				}
			}
		} else {
			result = false;
		}
		if (result) {
			compareJOIN_DBandTARGET_DBCounts();
			doPostTask(qry);
			markSuccessTransfer();			
		} else
			markErrorTransfer();
	}

	/// Проверка изменения всех объектов в JOIN_DB и обновление в TARGET_DB
	public void findChangesRowInSourseDB_UpdateTargerDB(Query argQry) {
		qry = argQry;
		TimeCounter tc = new TimeCounter(tableNameTARGET_DB);
		try {
			conJOIN_DB = DB.getConnection(sconJOIN_DB);
			conTARGET_DB = DB.getConnection(sconTARGET_DB);
			conLOG_DB = DB.getConnection(sconLOG_DB);
			doFindChangesJOIN_DBUpdateTARGET_DB();
		} finally {
			try {
				preparedSelectJOIN_DB.close();
			} catch (Exception e) {
			}
			try {
				preparedSelectTARGET_DB.close();
			} catch (Exception e) {
			}
			try {
				preparedInsertTARGET_DB.close();
			} catch (Exception e) {
			}
			try {
				preparedUpdateTARGET_DB.close();
			} catch (Exception e) {
			}
			preparedSelectJOIN_DB = null;
			preparedSelectTARGET_DB = null;
			preparedInsertTARGET_DB = null;
			preparedUpdateTARGET_DB = null;
			try {
				conTARGET_DB.close();
			} catch (Exception e) {
			}
			try {
				conJOIN_DB.close();
			} catch (Exception e) {
			}
			try {
				conLOG_DB.close();
			} catch (Exception e) {
			}
			conTARGET_DB = null;
			conJOIN_DB = null;
			conLOG_DB = null;
		}
		tc.finish("Transfer");
	}

	/// Сформировать SQL для определения всех записей в TARGET_DB
	private String makeAllSQLTARGET_DB() {
		StringBuffer result = new StringBuffer();
		result.append("SELECT * FROM ");
		result.append(tableNameTARGET_DB);
		return result.toString();
	}

	/// Проверка изменения всех объектов в JOIN_DB и обновление в TARGET_DB - содержательная часть
	private void doFindDeletedJOIN_DBDeleteTARGET_DB() {
		boolean result = true;
		int curRow = 0;
		if ((conJOIN_DB != null) && (conTARGET_DB != null)) {
			QueryProcessing.startProcessing(conLOG_DB, qry);
			CDBCResultSet allSet = new CDBCResultSet();
			String allSQL = makeAllSQLTARGET_DB();
			allSet.executeQuery(conTARGET_DB, allSQL, null, 0);
			ListIterator it = allSet.listIterator();
			startProgressIndicator(allSet.getRowsCount());
			while (it.hasNext()) {
				CDBCRowObject ro = (CDBCRowObject) it.next();
				RowDBObject objTARGET_DB = new RowDBObject();
				populateKeysCDBC(objTARGET_DB, colMap.keysMapJOIN_DB2TARGET_DB.values(), ro);
				if (!checkJOIN_DBDeleteTARGET_DB(objTARGET_DB)) {
					String msg = "Ошибка удаления данных: " + tableNameTARGET_DB + ", Ключевые поля: " + objTARGET_DB.dump();
					System.out.println(msg);
					QueryProcessing.addLogMessage(conLOG_DB, qry, QueryProcessing.MSG_ERROR, msg);
					result = false;
				}
				curRow++;
				markProgressIndicator(curRow);
			}
		} else {
			result = false;
		}
		compareJOIN_DBandTARGET_DBCounts();
		if (result)
			markSuccessDelete();
		else
			markErrorDelete();
	}

	/// Поиск записей, удаленных в JOIN_DB и их удаление в TARGET_DB
	public void findDeletedRowInSourseDB_DeleteTargerDB(Query argQry) {
		qry = argQry;
		TimeCounter tc = new TimeCounter(tableNameTARGET_DB);
		try {
			conJOIN_DB = DB.getConnection(sconJOIN_DB);
			conTARGET_DB = DB.getConnection(sconTARGET_DB);
			conLOG_DB = DB.getConnection(sconLOG_DB);
			doFindDeletedJOIN_DBDeleteTARGET_DB();
		} finally {
			try {
				preparedSelectJOIN_DB.close();
			} catch (Exception e) {
			}
			try {
				preparedSelectTARGET_DB.close();
			} catch (Exception e) {
			}
			try {
				preparedDeleteTARGET_DB.close();
			} catch (Exception e) {
			}
			preparedSelectJOIN_DB = null;
			preparedSelectTARGET_DB = null;
			preparedDeleteTARGET_DB = null;
			try {
				conTARGET_DB.close();
			} catch (Exception e) {
			}
			try {
				conJOIN_DB.close();
			} catch (Exception e) {
			}
			try {
				conLOG_DB.close();
			} catch (Exception e) {
			}
			conTARGET_DB = null;
			conJOIN_DB = null;
			conLOG_DB = null;
		}
		tc.finish("Delete");
	}

	/// Проверка совпадения количества записей в системах JOIN_DB и TARGET_DB
	protected void compareJOIN_DBandTARGET_DBCounts() {
		try {
			if ((conJOIN_DB != null) && (conTARGET_DB != null)) {
				CDBCResultSet countSet1 = new CDBCResultSet();
				CDBCResultSet countSet2 = new CDBCResultSet();
				countSet1.executeQuery(conJOIN_DB, "SELECT count(*) AS cnt FROM " + tableNameJOIN_DB, new Object[] {
				}, 0);
				countSet2.executeQuery(conTARGET_DB, "SELECT count(*) AS cnt FROM " + tableNameTARGET_DB + " WHERE recordstatus='A'", new Object[] {
				}, 0);
				ListIterator it1 = countSet1.listIterator();
				ListIterator it2 = countSet2.listIterator();
				if (it1.hasNext() && it2.hasNext()) {
					CDBCRowObject ro1 = (CDBCRowObject) it1.next();
					CDBCRowObject ro2 = (CDBCRowObject) it2.next();
					if (!ro1.getColumn("cnt").asObject().equals(ro2.getColumn("cnt").asObject())) {
						QueryProcessing.addLogMessage(
							conLOG_DB,
							qry,
							QueryProcessing.MSG_WARNING,
							"Количество записей в системе JOIN_DB: "
								+ tableNameJOIN_DB
								+ " = "
								+ ro1.getColumn("cnt").asString()
								+ ", в системе TARGET_DB: "
								+ tableNameTARGET_DB
								+ " = "
								+ ro2.getColumn("cnt").asString());
					}
				}
			}
		} catch (Exception e) {
			System.out.println("ERROR WHILE COUNT(*): " + e.getMessage());
		}
	}
	/**
	 * Returns the tableNameJOIN_DB.
	 * @return String
	 */
	protected String getTableNameJOIN_DB() {
		return tableNameJOIN_DB;
	}

	/**
	 * Returns the conTARGET_DB.
	 * @return Connection
	 */
	protected Connection getConTARGET_DB() {
		return conTARGET_DB;
	}

	/**
	 * Returns the qry.
	 * @return Query
	 */
	protected Query getQuery() {
		return qry;
	}

	/**
	 * Returns the preparedSelectJOIN_DB.
	 * @return PreparedStatement
	 */
	protected PreparedStatement getPreparedSelectJOIN_DB() {
		return preparedSelectJOIN_DB;
	}

	/**
	 * Sets the preparedSelectJOIN_DB.
	 * @param preparedSelectJOIN_DB The preparedSelectJOIN_DB to set
	 */
	protected void setPreparedSelectJOIN_DB(PreparedStatement preparedSelectJOIN_DB) {
		this.preparedSelectJOIN_DB = preparedSelectJOIN_DB;
	}

	/**
	 * Returns the conJOIN_DB.
	 * @return Connection
	 */
	protected Connection getConJOIN_DB() {
		return conJOIN_DB;
	}

	/**
	 * Returns the colMap.
	 * @return ColumnMap
	 */
	protected ColumnMap getColMap() {
		return colMap;
	}
	/**
	 * выполняет пост обработку:
	 */
	protected boolean doPostTask(Query qry) {
		return true;
	}
	/**
	 * выполняет пост обработку:
	 */
	protected boolean doBeforeTask(Query qry) {
		return true;
	}

	/**
	 * Returns the preparedSelectTARGET_DB.
	 * @return PreparedStatement
	 */
	protected PreparedStatement getPreparedSelectTARGET_DB() {
		return preparedSelectTARGET_DB;
	}

	/**
	 * Sets the preparedSelectTARGET_DB.
	 * @param preparedSelectTARGET_DB The preparedSelectTARGET_DB to set
	 */
	protected void setPreparedSelectTARGET_DB(PreparedStatement preparedSelectTARGET_DB) {
		this.preparedSelectTARGET_DB = preparedSelectTARGET_DB;
	}

	/**
	 * Returns the tableNameTARGET_DB.
	 * @return String
	 */
	protected String getTableNameTARGET_DB() {
		return tableNameTARGET_DB;
	}

	/**
	 * Returns the preparedInsertTARGET_DB.
	 * @return PreparedStatement
	 */
	protected PreparedStatement getPreparedInsertTARGET_DB() {
		return preparedInsertTARGET_DB;
	}

	/**
	 * Sets the preparedInsertTARGET_DB.
	 * @param preparedInsertTARGET_DB The preparedInsertTARGET_DB to set
	 */
	protected void setPreparedInsertTARGET_DB(PreparedStatement preparedInsertTARGET_DB) {
		this.preparedInsertTARGET_DB = preparedInsertTARGET_DB;
	}

	/**
	 * Returns the preparedUpdateTARGET_DB.
	 * @return PreparedStatement
	 */
	protected PreparedStatement getPreparedUpdateTARGET_DB() {
		return preparedUpdateTARGET_DB;
	}

	/**
	 * Sets the preparedUpdateTARGET_DB.
	 * @param preparedUpdateTARGET_DB The preparedUpdateTARGET_DB to set
	 */
	protected void setPreparedUpdateTARGET_DB(PreparedStatement preparedUpdateTARGET_DB) {
		this.preparedUpdateTARGET_DB = preparedUpdateTARGET_DB;
	}

	/**
	 * Returns the conLOG_DB.
	 * @return Connection
	 */
	public Connection getConLOG_DB() {
		return conLOG_DB;
	}

	/* (non-Javadoc)
	 * @see com.hps.july.sync.Collaboration#doTask(com.hps.july.sync.Query)
	 */
	public void doTask(Query argQry) {
		qry = argQry;
		boolean result = false;
		TimeCounter tc = new TimeCounter("doTask process");
		try {
			conTARGET_DB = DB.getConnection(sconTARGET_DB);
			conLOG_DB = DB.getConnection(sconLOG_DB);
			QueryProcessing.startProcessing(conLOG_DB, qry);
			if (conTARGET_DB != null) {
				result = doBeforeTask(qry);
			}
			if (result) {
				updateLastTransferDateTARGET_DB();
				QueryProcessing.finishSuccess(conLOG_DB, qry);
				doPostTask(qry);
			} else {
				QueryProcessing.addLogMessage(conLOG_DB, qry, QueryProcessing.MSG_ERROR, "ERROR doTask!!!");
				QueryProcessing.finishError(conLOG_DB, qry);
			}
		} catch (Exception e) {
			String msg = "ERROR doTask!!!";
			QueryProcessing.addLogMessage(conLOG_DB, qry, QueryProcessing.MSG_ERROR, msg);
			QueryProcessing.finishError(conLOG_DB, qry);
			System.out.println(msg);
			e.printStackTrace();
		} finally {
			try {
				conTARGET_DB.close();
				conTARGET_DB = null;
			} catch (Exception e) {
			}
			try {
				conLOG_DB.close();
				conLOG_DB = null;
			} catch (Exception e) {
			}
		}
		tc.finish("");
	}
}
