package com.hps.july.sync.nfs.adapters;

import java.sql.*;
import java.util.*;
import com.hps.july.sync.*;
import com.hps.july.cdbc.lib.*;
import com.hps.july.util.*;
import com.hps.july.util.TimeCounter;

/**
 * @author Dmitry Dneprov
 * Адаптер для переноса курсов валют.
 * Сильно отличается от стандартного, т.к. курсы формируются хранимой процедурой
 */
public class CurrencyRatesAdaptor extends SimpleCollaboration {

	private java.util.Date rateDate;

	private static class CurrencyRatesMap extends ColumnMap {
		/**
		 * Конструктор
		 */
		CurrencyRatesMap() {
			super();
			//         NFS              NRI            isKey
			addMap("idcurrencynfs", null, true);
			addMap("ratetype", "ratetype", true);
			addMap("idcurrencynri", "currency", true);
			addMap("rdate", "date", true);
			addMap("rate", "value", false);
		}
	}

	public CurrencyRatesAdaptor(DB argConNRI, DB argConNFS) {
		super(argConNRI, argConNFS, "rates", "APPS.XX_NRI_CURRENCY_VW", "last_update_date", new CurrencyRatesMap());
	}

	/// Найти и заполнить поля объект из системы NFS
	protected RowDBObject findObjectJOIN_DB(RowDBObject argObj) {
//System.out.println("from CurrencyRatesAdapter.findObjectJOIN_DB argObj="+ argObj.dump());//temp		
		RowDBObject result = null;
		ResultSet rs = null;
		java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("dd.MM.yyyy");
		StringBuffer buf = new StringBuffer();
		buf.append("SELECT (to_date('" + df.format(rateDate) + "','dd.mm.yyyy')) AS rdate, ");
		buf.append("xxifc.xxnri_pkg.get_rate_sql('");
		buf.append(argObj.getKeysColumns().get("idcurrencynfs").toString().trim());
		buf.append("', 'RUR', (to_date('" + df.format(rateDate) + "','dd.mm.yyyy')), '1000') AS rate from dual");
		try {
			setPreparedSelectJOIN_DB(getConJOIN_DB().prepareStatement(buf.toString()));
		} catch (SQLException e) {
			System.out.println("Cannot Prepare SQL=" + buf.toString());
			e.printStackTrace(System.out);
		}
		try {
			if (getPreparedSelectJOIN_DB() != null) {
				rs = getPreparedSelectJOIN_DB().executeQuery();
				if (rs != null) {
					if (rs.next()) {
						result = new RowDBObject();
                        // Store keys в правильном порядке
                        result.addKeyColumn("idcurrencynfs", argObj.getKeysColumns().get("idcurrencynfs"));
                        result.addKeyColumn("idcurrencynri", argObj.getKeysColumns().get("idcurrencynri"));
                        result.addKeyColumn("ratetype", argObj.getKeysColumns().get("ratetype"));
                        result.addKeyColumn("rdate", argObj.getKeysColumns().get("rdate"));						
						if (!populateColumns(result, getColMap().getColumnMapJOIN_DB2TARGET_DB().keySet(), rs))
							result = null;
//System.out.println("from CurrencyRatesAdapter.findObjectJOIN_DB result="+ result.dump());//temp									
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
		try {
			rs.close();
		} catch (Exception e) {
		}
		try {
			getPreparedSelectJOIN_DB().close();
		} catch (Exception e) {
		}
		setPreparedSelectJOIN_DB(null);
		return result;
	}

	/// Проверка изменения всех объектов в NFS и обновление в NRI - содержательная часть
	protected void doFindChangesJOIN_DBUpdateTARGET_DB() {
		boolean result = true;
		if ((getConJOIN_DB() != null) && (getConTARGET_DB() != null)) {
			QueryProcessing.startProcessing(getConTARGET_DB(), getQuery());
			if (getQuery().getStartDate() == null) {
				result = false;
				QueryProcessing.addLogMessage(
					getConTARGET_DB(),
					getQuery(),
					QueryProcessing.MSG_ERROR,
					"Не задана начальная дата, с которой обновлять курсы");
			}
			if (getQuery().getEndDate() == null) {
				result = false;
				QueryProcessing.addLogMessage(
					getConTARGET_DB(),
					getQuery(),
					QueryProcessing.MSG_ERROR,
					"Не задана конечная дата, до которой обновлять курсы");
			}
			if (result) {
				int defaultRateType = Utils.getNamedValueInt(getConTARGET_DB(), getQuery(), Utils.DEFAULT_RATE);
//System.out.println("defaultRateType= " + defaultRateType);//temp				
				GregorianCalendar gc = new GregorianCalendar();
				gc.setTime(getQuery().getStartDate());
				rateDate = gc.getTime();
				java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("MM,dd,yyyy");
				while (rateDate.getTime() <= getQuery().getEndDate().getTime()) {
					rateDate = gc.getTime();
					CDBCResultSet changesSet = new CDBCResultSet();

					changesSet
						.executeQuery(
							getConTARGET_DB(),
							"SELECT MDY("
								+ df.format(rateDate)
								+ ") AS rdate, *, "
								+ defaultRateType
								+ " ratetype FROM nfs_lnkCurrency WHERE flagmain='Y'",
							new Object[] {
					}, 0);
					ListIterator it = changesSet.listIterator();
					while (it.hasNext()) {
						CDBCRowObject ro = (CDBCRowObject) it.next();
						RowDBObject objJOIN_DB = new RowDBObject();
						populateKeysCDBC(objJOIN_DB, getColMap().getKeysMapJOIN_DB2TARGET_DB().keySet(), ro);
//System.out.println("from CurrencyRatesAdapter.doFindChangesJOIN_DBUpdateTARGET_DB : objJOIN_DB=" + objJOIN_DB.dump());//temp
						if (!findJOIN_DBUpdateTARGET_DB(objJOIN_DB)) {
							String msg =
								"Ошибка переноса данных: " + getTableNameJOIN_DB() + ", Ключевые поля: " + objJOIN_DB;
							System.out.println(msg);
							QueryProcessing.addLogMessage(
								getConTARGET_DB(),
								getQuery(),
								QueryProcessing.MSG_ERROR,
								msg);
							result = false;
						}
					}
					gc.add(GregorianCalendar.DAY_OF_MONTH, 1);
				}
			}
		} else {
			result = false;
		}
		compareJOIN_DBandTARGET_DBCounts();
		if (result)
			markSuccessTransfer();
		else
			markErrorTransfer();
	}

	/// Проверка существования объекта в системе TARGET_DB
	protected boolean isExistObjectTARGET_DB(RowDBObject argObj) {
		boolean result = false;
		ResultSet rs = null;
		StringBuffer buf = new StringBuffer();
		if (getPreparedSelectTARGET_DB() == null) {
			buf.append("SELECT * FROM ");
			buf.append(getTableNameTARGET_DB());
			buf.append(" WHERE ");
			buf.append(generateClause(getColMap().getKeysMapJOIN_DB2TARGET_DB().values(), " AND "));
//System.out.println("CurrencyRetesAd.isExistObjectTARGET_DB buf.toString()" + buf.toString());//temp			
			try {
				setPreparedSelectTARGET_DB(getConTARGET_DB().prepareStatement(buf.toString()));
			} catch (SQLException e) {
				System.out.println("isExistObjectTARGET_DB.Cannot Prepare SQL=" + buf.toString());
				e.printStackTrace(System.out);
			}
		}
		Vector list = new Vector(getColMap().getKeysMapJOIN_DB2TARGET_DB().keySet());
		list.removeElement("idcurrencynfs");
		if (setParams(getPreparedSelectTARGET_DB(), list, argObj.getKeysColumns(), 1)) {
			try {
				rs = getPreparedSelectTARGET_DB().executeQuery();
				if (rs.next())
					result = true;
			} catch (SQLException e) {
				System.out.println("CurrencyRatesAdaptor Error executing SQL=" + buf.toString());
				System.out.println("CurrencyRatesAdaptor SQL Error=" + e.getErrorCode() + ", Message=" + e.getMessage());
				//e.printStackTrace(System.out);
			} catch (Exception e) {
				System.out.println("CurrencyRatesAdaptor ERROR: " + e.getMessage());
				e.printStackTrace(System.out);
			}
		}
		try {
			rs.close();
		} catch (Exception e) {
		}
		return result;
	}
	
	/// Добавление объекта в систему TARGET_DB
	protected boolean performInsertTARGET_DB(RowDBObject argObj) {
//System.out.println("CurrencyRatesAdaptor.performInsertTARGET_DB()");		
		boolean result = false;
		StringBuffer buf = new StringBuffer();
		if (getPreparedInsertTARGET_DB() == null) {
			buf.append("INSERT INTO ");
			buf.append(getTableNameTARGET_DB());
			buf.append(" ( ");
			buf.append(generateColumnNameParam(getColMap().getKeysMapJOIN_DB2TARGET_DB().values(), true, true));
			buf.append(generateColumnNameParam(getColMap().getColumnMapJOIN_DB2TARGET_DB().values(), false, true));
			//buf.append(generateColumnNameParam(getColMap().predefinedColumnsTARGET_DB.keySet(), false, true));
			buf.append(" ) VALUES ( ");
			buf.append(generateColumnNameParam(getColMap().getKeysMapJOIN_DB2TARGET_DB().values(), true, false));
			buf.append(generateColumnNameParam(getColMap().getColumnMapJOIN_DB2TARGET_DB().values(), false, false));
			//buf.append(generateColumnNameParam(colMap.predefinedColumnsTARGET_DB.values(), false, false));
			buf.append(" )");
			try {
				setPreparedInsertTARGET_DB(getConTARGET_DB().prepareStatement(buf.toString()));
			} catch (SQLException e) {
				System.out.println("Cannot Prepare INSERT SQL=" + buf.toString());
				e.printStackTrace(System.out);
			} catch (Exception e) {
				System.out.println("ERROR: " + e.getMessage());
				e.printStackTrace(System.out);
			}
		}
		Vector list = new Vector(getColMap().getKeysMapJOIN_DB2TARGET_DB().keySet());
		list.removeElement("idcurrencynfs");
				
		setParams(getPreparedInsertTARGET_DB(), list, argObj.getKeysColumns(), 1);
		setParams(
			getPreparedInsertTARGET_DB(),
			getColMap().getColumnMapJOIN_DB2TARGET_DB().keySet(),
			argObj.getColumns(),
			list.size() + 1);

		try {
			getPreparedInsertTARGET_DB().executeUpdate();
			result = true;
		} catch (SQLException e) {
			System.out.println("CurrencyRatesAdaptor Error executing INSERT SQL = " + buf.toString());
			System.out.println("CurrencyRatesAdaptor SQL Error=" + e.getErrorCode() + ", Message=" + e.getMessage());
		} catch (Exception e) {
			System.out.println("CurrencyRatesAdaptor ERROR: " + e.getMessage());
			e.printStackTrace(System.out);
		}
		return result;
	}

	/// Обновление записи в системе TARGET_DB
	protected boolean performUpdateTARGET_DB(RowDBObject argObj) {
//System.out.println("CurrencyRatesAdaptor.performUpdateTARGET_DB()");//temp		
		boolean result = false;
		StringBuffer buf = new StringBuffer();
		if (getPreparedUpdateTARGET_DB() == null) {
			buf.append("UPDATE ");
			buf.append(getTableNameTARGET_DB());
			buf.append(" SET ");
			buf.append(generateClause(getColMap().getColumnMapJOIN_DB2TARGET_DB().values(), " , "));
			//buf.append(generateSetPredefinedSeparatorTARGET_DB(argObj));
			//buf.append(generateClause(getColMap().predefinedColumnsTARGET_DB().keySet(), " , "));
			buf.append(" WHERE ");
			buf.append(generateClause(getColMap().getKeysMapJOIN_DB2TARGET_DB().values(), " AND "));
			try {
				setPreparedUpdateTARGET_DB(getConTARGET_DB().prepareStatement(buf.toString()));
			} catch (SQLException e) {
				System.out.println("CurrencyRatesAdaptor  Cannot Prepare UPDATE SQL=" + buf.toString());
				System.out.println("CurrencyRatesAdaptor  SQL Error=" + e.getErrorCode() + ", Message=" + e.getMessage());
				//e.printStackTrace(System.out);
			} catch (Exception e) {
				System.out.println("CurrencyRatesAdaptor  ERROR: " + e.getMessage());
				e.printStackTrace(System.out);
			}
		}
		setParams(getPreparedUpdateTARGET_DB(), getColMap().getColumnMapJOIN_DB2TARGET_DB().keySet(), argObj.getColumns(), 1);

		Vector list = new Vector(getColMap().getKeysMapJOIN_DB2TARGET_DB().keySet());
		list.removeElement("idcurrencynfs");		
		setParams(
			getPreparedUpdateTARGET_DB(),
			list,
			argObj.getKeysColumns(),
			getColMap().getColumnMapJOIN_DB2TARGET_DB().keySet().size() + 1);
		try {
			getPreparedUpdateTARGET_DB().executeUpdate();
			result = true;
		} catch (SQLException e) {
			System.out.println("CurrencyRatesAdaptor  Error executing UPDATE SQL = " + buf.toString());
			System.out.println("CurrencyRatesAdaptor  SQL Error=" + e.getErrorCode() + ", Message=" + e.getMessage());
			//e.printStackTrace(System.out);
		} catch (Exception e) {
			System.out.println("CurrencyRatesAdaptor ERROR: " + e.getMessage());
			e.printStackTrace(System.out);
		}
		return result;
	}
}
