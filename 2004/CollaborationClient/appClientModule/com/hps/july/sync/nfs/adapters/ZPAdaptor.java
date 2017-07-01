package com.hps.july.sync.nfs.adapters;

import java.sql.*;
import com.hps.july.sync.*;

/**
 * @author Dmitry Dneprov
 * ������� ��� �������� ����������.
 */
public class ZPAdaptor extends SimpleCollaboration {

    private static class ZPMap extends ColumnMap {
        /**
         * �����������
         */
        ZPMap() {
            super();
            //         NFS              NRI            isKey
            addMap("po_header_id", "idZpNfs", true);
            addMap("vendor_id", "idVendorNfs", false);
            addMap("vendor_site_id", "idVendorSiteNfs", false);
            addMap("org_id", "idOrgNfs", false);
            addMap("zp_num", "numZp", false);
            addMap("contract_po_header_id", "idContractNfs", false);
            addMap("authorization_status", "stateZp", false);
            addMap("sum_without_vat", "sumWithoutVat", false);
            addMap("currency_code", "currZp", false);
            addMap("vat", "sumVat", false);
            addMap("comments", "comments", false);

            addMap("creation_date", "datecreate", false);
            addMap("created_by", "usercreate", false);
            addMap("last_update_date", "datemodify", false);
            addMap("last_updated_by", "usermodify", false);

            // �������, ���������������� � ������� NRI
            addPredefinedColumnTARGET_DB("flagworknri", "N");
        }
    }

    public ZPAdaptor(DB argConNRI, DB argConNFS) {
        super(argConNRI, argConNFS, "nfs_zp", "APPS.XX_NRI_ZP_VW", "last_update_date", new ZPMap());
     }
}
