package com.hps.july.sync;

/**
 * @author Shafigullin Ildar
 *
 * Интерфейс для синхронизации баз данных.
 */
public interface Collaboration{
    /**
     *  Поиск изменений в SourseDB и их отражение в TargerDB:
     */
    public abstract void findChangesRowInSourseDB_UpdateTargerDB(Query argQry);
    
    /**
     * Поиск записей, удаленных в SourseDB и их удаление в TargerDB:
     */
    public abstract void findDeletedRowInSourseDB_DeleteTargerDB(Query argQry);
    
	/**
	 * вызов сторонних процедур и прочее:
	 */
	public abstract void doTask(Query argQry);    
}