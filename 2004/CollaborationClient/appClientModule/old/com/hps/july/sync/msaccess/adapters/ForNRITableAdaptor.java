package old.com.hps.july.sync.msaccess.adapters;

import java.sql.*;
import old.com.hps.july.sync.*;
import com.hps.july.cdbc.lib.CDBCResultSet;

/**
 * @author Ildar
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ForNRITableAdaptor extends SimpleAdaptor {
    public ForNRITableAdaptor(SyncConnection argConNRI, SyncConnection argConNFS) {
        super(argConNRI, argConNFS, "dbspositions", "���_NRI_�������");
        setColumnMap(new ForNRITableMap());
    }
    private class ForNRITableMap extends ColumnMap {
        /**
         * �����������
         */
        ForNRITableMap() {
            super();
            // addMap( MSAccess, NRI, isKey)
            addMap("���", "idrecord", true);
            addMap("�DAMPS", "dampsid", false);
            addMap("�GSM", "gsmid", false);
            //addMap("%WLAM", "wlanid", false);
            addMap("��������", "name", false);
            addMap("��������2", "name2", false);
            addMap("����_��", "apparattype", false);
            addMap("�����", "containertype", false);
            addMap("���_��", "placetype", false);
            addMap("���_��", "apparatplace", false);
            addMap("����_����", "oporaplace", false);
            addMap("���", "isouropora", false);
            addMap("���_����", "oporatype", false);
            addMap("���_���", "antennaplace", false);
            addMap("������", "heightopora", false);
            addMap("���_���", "fiootvexpl", false);
            addMap("���", "tabnumotvexpl", false);
            addMap("����", "statebs", false);
            addMap("��������", "datederrick", false);
            addMap("�����", "dateonsitereview", false);
            addMap("����_����_����", "lastupdmarshkarta", false);
            addMap("����_������", "lastupdlistprohod", false);
            addMap("����_���", "lastupdposition", false);                                                           

            // �������, ���������������� � ������� NRI
            //addPredefinedColumnNRI("", "");
        }
    }    
}
