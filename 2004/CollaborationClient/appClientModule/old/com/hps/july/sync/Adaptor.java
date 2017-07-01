package old.com.hps.july.sync;

/**
 * @author Dmitry Dneprov
 *
 * ����������� ������� ��� ������� � ������� � ��
 * ������������ ���������� ������
 */
public interface Adaptor {
    /// ���������� ������� � NRI
	public abstract boolean updateObjectNRI(BusinessObject argObj);

    /// ��������� ����� � ������������ �������� NRI
    public abstract boolean makeRelationNRI(BusinessObject argObj);

    /// �������� ������� � NRI
	public abstract boolean deleteObjectNRI(BusinessObject argObj);

    /// ����� ��������� � JOIN_DB � �� ������� � NRI
    public abstract void findChangesJOIN_DBUpdateNRI(Query argQry);

    /// ����� �������, ��������� � JOIN_DB � �� �������� � NRI
    public abstract void findDeletedJOIN_DBDeleteNRI(Query argQry);
}