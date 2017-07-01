package old.com.hps.july.sync;

import java.sql.*;

/**
 * @author Dmitry Dneprov
 * ������� ������� ���������� ������.
 * �������� � ������� �������.
 * ���������� ���������� ���� �������
 */

public class SimpleAllSyncAdaptor extends SimpleAdaptor {

    protected SimpleAllSyncAdaptor(SyncConnection argConNRI, SyncConnection argConJOIN_DB,String argTableNameNRI, String argTableNameJOIN_DB) {
        super(argConNRI, argConJOIN_DB, argTableNameNRI, argTableNameJOIN_DB);
    }

    /// ������������ SQL ��� ����������� ������������ ������� � JOIN_DB
    protected String makeChangesSQLJOIN_DB() {
        StringBuffer result = new StringBuffer();
        result.append("SELECT * FROM ");
        result.append(tableNameJOIN_DB);
        return result.toString();
    }

    /// ������������ ��������� SQL ��� ����������� ������������ �������
    protected Object [] getChangesSQLParams() {
        return null;
    }
}

