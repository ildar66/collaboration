package com.hps.july.sync;

import java.sql.*;
import java.util.*;
import com.hps.july.cdbc.lib.*;

/**
 * @author Shafigullin Ildar
 * Запрос на синхронизацию данных.
 */
public class Query {
	private String reqType;
	private int queryId;
	private java.sql.Date startDate;
	private java.sql.Date endDate;
	private int idApp;

	/// Конструктор
	public Query() {
	}

	public boolean isQueryType(String argQueryType) {
		return getReqType().indexOf(argQueryType) > 0;
	}

	public boolean isDelQuery() {
		return getReqType().indexOf("DEL") == 0;
	}
	
	public boolean isAllQuery() {
		return getReqType().indexOf("ALL") == 0;
	}	

	public void setReqType(String argReqType) {
		reqType = argReqType;
	}

	public String getReqType() {
		return reqType;
	}

	public void setQueryId(int argQueryId) {
		queryId = argQueryId;
	}

	public int getQueryId() {
		return queryId;
	}

	/// Дата начала интервала обработки записей
	public void setStartDate(java.sql.Date argStartDate) {
		startDate = argStartDate;
	}

	/// Дата начала интервала обработки записей
	public java.sql.Date getStartDate() {
		return startDate;
	}

	/// Дата окончания интервала обработки записей
	public void setEndDate(java.sql.Date argEndDate) {
		endDate = argEndDate;
	}

	/// Дата окончания интервала обработки записей
	public java.sql.Date getEndDate() {
		return endDate;
	}

	/**
	 * Returns the idApp.
	 * @return int
	 */
	public int getIdApp() {
		return idApp;
	}

	/**
	 * Sets the idApp.
	 * @param idApp The idApp to set
	 */
	public void setIdApp(int idApp) {
		this.idApp = idApp;
	}

}
