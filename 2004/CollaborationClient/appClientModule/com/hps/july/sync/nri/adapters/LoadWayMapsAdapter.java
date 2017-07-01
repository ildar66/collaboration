/*
 * Created on 22.11.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.hps.july.sync.nri.adapters;

import java.util.Properties;

import com.hps.beeline.excelLoader.BalanceExcelFilesProcessor;
import com.hps.july.sync.DB;
import com.hps.july.sync.Query;
import com.hps.july.sync.SimpleCollaboration;
/**
 * @author ildar
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments 
 */
public class LoadWayMapsAdapter extends SimpleCollaboration  {
	Properties prop = null;
	/**
	 * @param targerDB
	 * @param logDB
	 * @param prop
	 */
	public LoadWayMapsAdapter(DB targerDB, DB logDB, Properties prop) {
		//super(targerDB, logDB);
		super(targerDB, null, "_WAYMAPDOC", null, null, null, logDB);
		this.prop = prop;
	}

	/* (non-Javadoc)
	 * @see com.hps.july.sync.SimpleCollaboration#doBeforeTask(com.hps.july.sync.Query)
	 */
	protected boolean doBeforeTask(Query qry) {
		//Вызов сторонней процедуры:
		String blobDirPath = prop.getProperty("loadWayMapsDirPath");
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
			System.out.println("Вызов сторонней процедуры loadWayMaps:");
			com.hps.beeline.loader.MainProcessor.loadWayMaps(new Integer(qry.getQueryId()), getConTARGET_DB(), getConLOG_DB(), lastUpdateDate, blobDirPath);
			return true;
		} catch (Exception e) {
			System.out.println("LoadWayMapsAdapter.doBeforeTask  :Вызов сторонней процедуры ERROR: " + e.getMessage());
			e.printStackTrace(System.out);
			return false;
		}
	}

}
