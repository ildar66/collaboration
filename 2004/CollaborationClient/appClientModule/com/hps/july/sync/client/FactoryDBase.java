package com.hps.july.sync.client;

import java.util.Properties;

import com.hps.july.sync.Collaboration;
import com.hps.july.sync.Query;
import com.hps.july.sync.DB;
import com.hps.july.sync.dbase.adapters.AllBsAdaptor;
/**
 * @author Shafigullin Ildar
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class FactoryDBase extends AbstractFactory{
	public static final String QRY_ALLBS = "ALLBS";
	public Collaboration getAdapter(Query qry, DB logDB, Properties prop) {
		DB sourseDB = new DB(prop, "NRI");
		DB targetDB = new DB(prop, "dBase");		
		Collaboration adaptor = null;
		if (qry.isQueryType(QRY_ALLBS)) {
			adaptor = new AllBsAdaptor(targetDB, sourseDB, logDB);
		}
		return adaptor;
	}
}
