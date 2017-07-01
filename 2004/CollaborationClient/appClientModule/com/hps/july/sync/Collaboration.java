package com.hps.july.sync;

/**
 * @author Shafigullin Ildar
 *
 * ��������� ��� ������������� ��� ������.
 */
public interface Collaboration{
    /**
     *  ����� ��������� � SourseDB � �� ��������� � TargerDB:
     */
    public abstract void findChangesRowInSourseDB_UpdateTargerDB(Query argQry);
    
    /**
     * ����� �������, ��������� � SourseDB � �� �������� � TargerDB:
     */
    public abstract void findDeletedRowInSourseDB_DeleteTargerDB(Query argQry);
    
	/**
	 * ����� ��������� �������� � ������:
	 */
	public abstract void doTask(Query argQry);    
}