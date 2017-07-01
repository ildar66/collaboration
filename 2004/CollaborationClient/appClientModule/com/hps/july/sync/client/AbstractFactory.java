package com.hps.july.sync.client;

import java.util.Properties;

import com.hps.july.sync.Collaboration;
import com.hps.july.sync.Query;
import com.hps.july.sync.DB;
import com.hps.july.sync.dbase.adapters.AllBsAdaptor;
/**
 * @author Shafigullin Ildar
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public abstract class AbstractFactory {
	//источники данных для Collaboration:
	public final static int APP_NFS = 1;
	public final static int APP_MSAccess = 2;
	public final static int APP_DBase = 3;
	public final static int APP_NRI = 4;
	public final static int APP_Interbase = 5;

	public static AbstractFactory getFactory(int whichFactory) {

		switch (whichFactory) {
			case APP_NFS :
				return new FactoryNFS();
			case APP_MSAccess :
				return new FactoryMSAccess();
			case APP_DBase :
				return new FactoryDBase();
			case APP_NRI :
				return new FactoryNRI();
			case APP_Interbase :
				return new FactoryInterbase();				
			default :
				return null;
		}
	}

	public abstract Collaboration getAdapter(Query qry, DB logDB, Properties prop);

}
