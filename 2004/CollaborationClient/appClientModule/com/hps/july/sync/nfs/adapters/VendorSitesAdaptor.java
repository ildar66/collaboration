package com.hps.july.sync.nfs.adapters;

import java.sql.*;
import com.hps.july.sync.*;

/**
 * @author Dmitry Dneprov
 * Адаптер для переноса отделений поставщиков.
 */
public class VendorSitesAdaptor extends SimpleCollaboration {

    private static class VendorSitesMap extends ColumnMap {
        /**
         * Конструктор
         */
        VendorSitesMap() {
            super();
            //         NFS              NRI            isKey
            addMap("VENDOR_SITE_ID", "idVendorSiteNfs", true);
            addMap("VENDOR_ID", "idVendorNfs", false);
            addMap("ORG_ID", "idOrgNfs", false);
            addMap("VENDOR_SITE_CODE", "vendorSiteCode", false);
            addMap("SITE_NAME", "name", false);
            addMap("MATCH_OPTION", "matchOption", false);
            addMap("addr", "address", false);
            addMap("kpp", "kpp", false);

            addMap("creation_date", "datecreate", false);
            addMap("created_by", "usercreate", false);
            addMap("last_update_date", "datemodify", false);
            addMap("last_updated_by", "usermodify", false);

            // Колонки, предопределенные в таблице NRI
            addPredefinedColumnTARGET_DB("flagworknri", "N");
            addPredefinedColumnTARGET_DB("recordstatus", "A");
        }
    }

    public VendorSitesAdaptor(DB argConNRI, DB argConNFS) {
        super(argConNRI, argConNFS, "nfs_vendorsites", "APPS.XX_NRI_VENDOR_SITES_VW", "last_update_date", new VendorSitesMap());
    }
}
