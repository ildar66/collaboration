package com.hps.july.sync.nfs.adapters;

import java.sql.*;
import com.hps.july.sync.*;

/**
 * @author Dmitry Dneprov
 * Адаптер для переноса счетов.
 */
public class UsersAdaptor extends SimpleCollaboration {

    private static class UsersMap extends ColumnMap {
        /**
         * Конструктор
         */
        UsersMap() {
            super();
            //         NFS              NRI            isKey
            addMap("person_id", "personId", true);
            addMap("user_id", "idUserNfs", false);
            addMap("user_name", "login", false);
            addMap("full_name", "fullName", false);

            // Колонки, предопределенные в таблице NRI
            addPredefinedColumnTARGET_DB("flagworknri", "N");
            addPredefinedColumnTARGET_DB("recordstatus", "A");
        }
    }

    public UsersAdaptor(DB argConNRI, DB argConNFS) {
        super(argConNRI, argConNFS, "nfs_users", "APPS.XX_NRI_USERS_VW", new UsersMap());
    }
}
