package com.hps.july.sync.interbase.adapters;

import java.sql.*;
import com.hps.july.sync.*;

/**
 * @author Shafigullin Ildar
 * Адаптер для переноса контрактов.
 */
public class Dct_StatusAdaptor extends SimpleCollaboration {

	private static class Dct_StatusMap extends ColumnMap {
		Dct_StatusMap() {
			super();
			//     Interbase,     NRI,     isKey
			addMap("statusid", "statusid", true);
			
			addMap("statusname", "statusname", false);
			addMap("statuscomment", "statuscomment", false);
			addMap("statusunique", "statusunique", false);
		}
	}

	public Dct_StatusAdaptor(DB targerDB, DB sourseDB, DB sconLOG_DB) {
		super(targerDB, sourseDB, "kzldct_status", "DCT_STATUS", null, new Dct_StatusMap(), sconLOG_DB);
	}
}
