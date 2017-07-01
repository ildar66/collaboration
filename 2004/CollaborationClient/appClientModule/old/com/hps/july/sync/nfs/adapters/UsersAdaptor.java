package old.com.hps.july.sync.nfs.adapters;

import java.sql.*;
import old.com.hps.july.sync.*;

/**
 * @author Dmitry Dneprov
 * ������� ��� �������� ������.
 */
public class UsersAdaptor extends SimpleAllSyncAdaptor {

    public class UsersMap extends ColumnMap {
        /**
         * �����������
         */
        UsersMap() {
            super();
            //         NFS              NRI            isKey
            addMap("person_id", "personId", true);
            addMap("user_id", "idUserNfs", false);
            addMap("user_name", "login", false);
            addMap("full_name", "fullName", false);

            // �������, ���������������� � ������� NRI
            addPredefinedColumnNRI("flagworknri", "N");
            addPredefinedColumnNRI("recordstatus", "A");
        }
    }

    public UsersAdaptor(SyncConnection argConNRI, SyncConnection argConNFS) {
        super(argConNRI, argConNFS, "nfs_users", "APPS.XX_NRI_USERS_VW");
        setColumnMap(new UsersMap());
    }
}
