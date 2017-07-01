package com.hps.july.sync.interbase.adapters;

import java.sql.*;
import com.hps.july.sync.*;

/**
 * @author Shafigullin Ildar
 * Адаптер для переноса контрактов.
 */
public class GateStationsAdaptor extends SimpleCollaboration {

	private static class GateStationsMap extends ColumnMap {
		GateStationsMap() {
			super();
			//     Interbase,     NRI,     isKey
			addMap("recordid", "recordid", true);
			
			addMap("loadtime", "loadtime", false);
			addMap("bsid", "bsid", false);
			addMap("bsnumber", "bsnumber", false);
			addMap("bsname", "bsname", false);
			addMap("bsaddr", "bsaddr", false);
			addMap("bslg", "bslg", false);
			addMap("bslt", "bslt", false);
			addMap("bsvendor", "bsvendor", false);
			addMap("bscases", "bscases", false);
			addMap("bsstatusnum", "bsstatusnum", false);
			addMap("bsstatus", "bsstatus", false);
			addMap("cellid", "cellid", false);
			addMap("cellname", "cellname", false);
			addMap("cellsuffix", "cellsuffix", false);
			addMap("cellci", "cellci", false);
			addMap("cellbsc", "cellbsc", false);
			addMap("cellstatusnum", "cellstatusnum", false);
			addMap("cellstatus", "cellstatus", false);
			addMap("cellband", "cellband", false);
			addMap("cellcase", "cellcase", false);
			addMap("cellrack", "cellrack", false);
			addMap("celllac", "celllac", false);
			addMap("cellrac", "cellrac", false);
			addMap("celltrx", "celltrx", false);
			addMap("antposid", "antposid", false);
			addMap("antposantennid", "antposantennid", false);
			addMap("antpostype", "antpostype", false);
			addMap("antposdirect", "antposdirect", false);
			addMap("antposheight", "antposheight", false);
			addMap("antposazimuth", "antposazimuth", false);
			addMap("antposetilt", "antposetilt", false);
			addMap("antposmtilt", "antposmtilt", false);
		}
	}

	public GateStationsAdaptor(DB targerDB, DB sourseDB, DB sconLOG_DB) {
		super(targerDB, sourseDB, "kzlgatestations", "GATESTATIONS", "loadtime", new GateStationsMap(), sconLOG_DB);
	}
}
