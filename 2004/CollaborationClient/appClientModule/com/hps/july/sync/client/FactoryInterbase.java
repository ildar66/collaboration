package com.hps.july.sync.client;

import java.util.Properties;

import com.hps.july.sync.Collaboration;
import com.hps.july.sync.Query;
import com.hps.july.sync.DB;
import com.hps.july.sync.interbase.adapters.*;
/**
 * @author Shafigullin Ildar
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class FactoryInterbase extends AbstractFactory{
	public static final String QRY_ANTENNS = "ANTENNS";
	public static final String QRY_DCT_STATUS = "DCT_STATUS";
	public static final String QRY_GATESTATIONS = "GATESTATIONS";
	public Collaboration getAdapter(Query qry, DB logDB, Properties prop) {
		DB sourseDB = new DB(prop, "Interbase");
		DB targetDB = new DB(prop, "NRI");		
		Collaboration adaptor = null;
		if (qry.isQueryType(QRY_ANTENNS)) {
			adaptor = new AntennsAdaptor(targetDB, sourseDB, logDB);
		}
		if (qry.isQueryType(QRY_DCT_STATUS)) {
			adaptor = new Dct_StatusAdaptor(targetDB, sourseDB, logDB);
		}
		if (qry.isQueryType(QRY_GATESTATIONS)) {
			adaptor = new GateStationsAdaptor(targetDB, sourseDB, logDB);
		}
		return adaptor;
	}
}
