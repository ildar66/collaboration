package com.hps.july.cdbc.lib;

import java.util.*;
import java.sql.*;
import javax.sql.*;
 
/**
 * Объект для представления строки результата запроса.
 * Creation date: (10.03.2004 13:53:20)
 * @author: Dmitry Dneprov
 */
public class CDBCRowObject {
	HashMap headers;
	//LinkedList columns = new LinkedList();
	ArrayList columns = new ArrayList();
/**
 * CDBCResultSet constructor.
 */
public CDBCRowObject(HashMap aheaders) {
	super();
	headers = aheaders;
}
/**
 * Добавляет значение очередной колонки в строке.
 * Creation date: (10.03.2004 14:40:27)
 * @param colVal java.lang.Object
 */
public void addColumn(Object colVal) {
	columns.add(colVal);
}
/**
 * Возвращает значение колонки.
 * Creation date: (10.03.2004 14:08:12)
 * @return java.lang.Object
 * @param columnName java.lang.String
 */
public CDBCColumnObject getColumn(String columnName) {
	Integer  icolInd = (Integer)headers.get(columnName);
	if (icolInd != null) {
		return new CDBCColumnObject(columns.get(icolInd.intValue()));
	} else {
		System.out.println("CDBC ERROR: Column '" + columnName + "' not found");
		return new CDBCColumnObject(null);
	}
}
/**
 * Проверяет наличие колонки.
 * Creation date: (10.03.2004 14:08:12)
 * @return java.lang.Object
 * @param columnName java.lang.String
 */
public boolean hasColumn(String columnName) {
	Integer  icolInd = (Integer)headers.get(columnName);
	return (icolInd != null);
}
/**
 * Устанавливает значение колонки.
 * Creation date: (10.03.2004 14:08:12)
 * @return void
 * @param columnName java.lang.String
 * @param colVal CDBCColumnObject
 */
public void setColumnValue(String columnName, CDBCColumnObject colVal) throws java.lang.NoSuchFieldException {
	Integer  icolInd = (Integer)headers.get(columnName);
	if (icolInd != null) {
		columns.set(icolInd.intValue(), colVal);
	} else {
		throw new java.lang.NoSuchFieldException(columnName);
	}
}
}


