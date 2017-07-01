package com.hps.july.sync.interbase.adapters;

import java.sql.*;
import com.hps.july.sync.*;

/**
 * @author Shafigullin Ildar
 * Адаптер для переноса контрактов.
 */
public class AntennsAdaptor extends SimpleCollaboration {

	private static class AntennsMap extends ColumnMap {
		AntennsMap() {
			super();
			//     Interbase,     NRI,     isKey
			addMap("antennid", "antennid", true);
			
			addMap("antennname", "antennname", false);
			addMap("antennvendor", "antennvendor", false);
			addMap("antennfromfile", "antennfromfile", false);
			addMap("antennband", "antennband", false);
			addMap("antenntype", "antenntype", false);
			addMap("antennpolarization", "antennpolarization", false);
			addMap("antenngain", "antenngain", false);
			addMap("antennhplan", "antennhplan", false);
			addMap("antennvplan", "antennvplan", false);
			addMap("antennetilt", "antennetilt", false);
			addMap("antennhdiagram", "antennhdiagram", false);
			addMap("antennvdiagram", "antennvdiagram", false);
			addMap("antenndate", "antenndate", false);
		}
	}

	public AntennsAdaptor(DB targerDB, DB sourseDB, DB sconLOG_DB) {
		super(targerDB, sourseDB, "kzlantenns", "ANTENNS", "antenndate", new AntennsMap(), sconLOG_DB);
	}
}
