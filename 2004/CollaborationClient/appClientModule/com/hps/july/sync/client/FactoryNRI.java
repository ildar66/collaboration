package com.hps.july.sync.client;

import java.util.Properties;

import com.hps.july.sync.Collaboration;
import com.hps.july.sync.Query;
import com.hps.july.sync.DB;
import com.hps.july.sync.dbase.adapters.AllBsAdaptor;
import com.hps.july.sync.nri.adapters.BalanceExcelFilesAdapter;
import com.hps.july.sync.nri.adapters.LoadPassListsAdapter;
import com.hps.july.sync.nri.adapters.LoadWayMapsAdapter;
/**
 * @author Shafigullin Ildar
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class FactoryNRI extends AbstractFactory {
	public static final String QRY_BALANCESTORE = "BALANCESTORE";
	public static final String QRY_WAYMAPDOC = "WAYMAPDOC";
	public static final String QRY_LISTPASSDOC = "LISTPASSDOC";
	public Collaboration getAdapter(Query qry, DB logDB, Properties prop) {
		DB targetDB = new DB(prop, "NRI");
		Collaboration adaptor = null;
		if (qry.isQueryType(QRY_BALANCESTORE)) {
			adaptor = new BalanceExcelFilesAdapter(targetDB, logDB, prop);
		} else if (qry.isQueryType(QRY_WAYMAPDOC)) {
			adaptor = new LoadWayMapsAdapter(targetDB, logDB, prop);
		} else if (qry.isQueryType(QRY_LISTPASSDOC)) {
			adaptor = new LoadPassListsAdapter(targetDB, logDB, prop);
		}
		return adaptor;
	}
}
