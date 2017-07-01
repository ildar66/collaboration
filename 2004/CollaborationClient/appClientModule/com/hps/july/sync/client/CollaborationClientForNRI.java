package com.hps.july.sync.client;

import com.hps.july.sync.*;
import com.hps.july.sync.nfs.adapters.*;
import com.hps.july.sync.msaccess.adapters.*;

import java.sql.*;
import java.util.*;
import java.io.*;

import javax.ejb.CreateException;
import javax.naming.InitialContext;  
import javax.naming.NamingException;
import javax.sql.DataSource;
//import com.hps.july.sync.utility.*;
//import com.hps.july.sync.session.*;

/**
 * @author Shafigullin Ildar
 *
 * базовый абстракный класс для реализации Алгоритм переноса данных из NFS в NRI
 */
public class CollaborationClientForNRI {
	// Интервал между опросами таблицы запросов (в миллисекундах)
	public static final long SERVICE_TIME = 10 * 1000;

	/** Основной метод
	 * 
	 *  Процедура тестирования:
	 *  delete from informix.join_db_nriupdate
	 *  insert into informix.join_db_query values(_, 1, 'R', current, null, null, "ALLUSERS", null, null) 
	 *  insert into informix.join_db_query values(_, 2, 'R', current, null, null, "ALLPOSITIONS", null, null) 
	 *  insert into informix.join_db_query values(_, 3, 'R', current, null, null, "ALLALLBS", null, null) 
	 *  insert into join_db_query (idquery, reqstate, posttime, starttime, finishtime, reqtype, selstartdate, selenddate, idapp) values (1017, "R", current, null, null, "ALLRATES", today-4, today, 1)
	 */
	public void process() throws IOException, FileNotFoundException {
		System.out.println("Starting CollaborationClientForNRI");
		//Properties prop = readSyncProperties("C:/workspases/WSAD_Collaboration/CollaborationClient/appClientModule/sync.cfg");
		Properties prop = readSyncProperties("sync.cfg");
		DB logDB = new DB(prop, "NRI");

		while (true) {
			Query qry = QueryProcessing.findLatestQuery(logDB);
			if (qry != null) {
				Collaboration adaptor = null;
				AbstractFactory factory = AbstractFactory.getFactory(qry.getIdApp());
				//System.out.println("factory.getClass()=" + factory.getClass());//temp
				if (factory != null)
					adaptor = factory.getAdapter(qry, logDB, prop);
				if (adaptor != null) {
					processRequest(qry, adaptor);
					//System.out.println("adaptor.getClass()=" + adaptor.getClass());//temp
				} else {
					QueryProcessing.startProcessing(logDB, qry);
					QueryProcessing.addLogMessage(logDB, qry, QueryProcessing.MSG_ERROR, "Неизвестный тип запроса - запрос игнорируется");
					QueryProcessing.finishError(logDB, qry);
				}
			} else {
				try {
					Thread.sleep(SERVICE_TIME);
				} catch (InterruptedException e) {
					System.out.println("Interrupt received - exiting");
					break;
				}
			}
		}
	}

	private void processRequest(Query qry, Collaboration adaptor) {
		if (qry.isDelQuery())
			adaptor.findDeletedRowInSourseDB_DeleteTargerDB(qry);
		else if (qry.isAllQuery())
			adaptor.findChangesRowInSourseDB_UpdateTargerDB(qry);
		else
			adaptor.doTask(qry);
	}

	public static Properties readSyncProperties(String fileName) throws IOException, FileNotFoundException {
		Properties prop = new Properties();
		try {
			File fl = new File(fileName);
			FileInputStream inStream = new FileInputStream(fl);
			try {
				prop.load(inStream);
				prop.list(System.out);
				return prop;
			} catch (IOException ioExc) {
				System.out.println("Cannot Load properties from file: " + fileName);
				throw ioExc;
			}
		} catch (FileNotFoundException e) {
			System.out.println("Config file " + fileName + " not found");
			throw e;
		}
	}

	public static void main(String[] args) throws IOException, FileNotFoundException {
		new CollaborationClientForNRI().process();
	}
}
