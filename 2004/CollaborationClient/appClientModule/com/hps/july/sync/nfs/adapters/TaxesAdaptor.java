package com.hps.july.sync.nfs.adapters;

import java.sql.*;
import com.hps.july.sync.*;

/**
 * @author Dmitry Dneprov
 * ������� ��� �������� ����������.
 */
public class TaxesAdaptor extends SimpleCollaboration {

    private static class TaxesMap extends ColumnMap {
        /**
         * �����������
         */
        TaxesMap() {
            super();
            //         NFS              NRI            isKey
            addMap("tax_id", "idTaxRate", true);
            addMap("org_id", "idOrgNfs", false);
            addMap("vat_code", "vatCode", false);
            addMap("description", "description", false);
            addMap("tax_rate", "taxrate", false);

            // �������, ���������������� � ������� NRI
            addPredefinedColumnTARGET_DB("flagworknri", "N");
            addPredefinedColumnTARGET_DB("recordstatus", "A");
        }
    }

    public TaxesAdaptor(DB argConNRI, DB argConNFS) {
        super(argConNRI, argConNFS, "nfs_taxrates", "APPS.XX_NRI_VAT_VW", new TaxesMap());
    }
}
