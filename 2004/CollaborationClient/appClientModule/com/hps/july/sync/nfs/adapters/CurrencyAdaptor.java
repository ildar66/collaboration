package com.hps.july.sync.nfs.adapters;

import java.sql.*;
import com.hps.july.sync.*;

/**
 * @author Dmitry Dneprov
 * Адаптер для переноса валют.
 */
public class CurrencyAdaptor extends SimpleCollaboration {

    private static class CurrencyMap extends ColumnMap {
        /**
         * Конструктор
         */
        CurrencyMap() {
            super();
            //         NFS              NRI            isKey
            addMap("currency_code", "idcurrencynfs", true);
            addMap("name", "name", false);

            addMap("creation_date", "datecreate", false);
            addMap("created_by", "usercreate", false);
            addMap("last_update_date", "datemodify", false);
            addMap("last_updated_by", "usermodify", false);

            // Колонки, предопределенные в таблице NRI
            addPredefinedColumnTARGET_DB("flagworknri", "N");
            addPredefinedColumnTARGET_DB("recordstatus", "A");
        }
    }

    public CurrencyAdaptor(DB argConNRI, DB argConNFS) {
        super(argConNRI, argConNFS, "nfs_currency", "APPS.XX_NRI_CURRENCY_VW", "last_update_date", new CurrencyMap());
    }
}
