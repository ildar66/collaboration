package com.hps.july.sync.client;

import java.util.Properties;

import com.hps.july.sync.Collaboration;
import com.hps.july.sync.Query;
import com.hps.july.sync.DB;
import com.hps.july.sync.nfs.adapters.AccountsAdaptor;
import com.hps.july.sync.nfs.adapters.BanksAdaptor;
import com.hps.july.sync.nfs.adapters.ContractsAdaptor;
import com.hps.july.sync.nfs.adapters.CurrencyAdaptor;
import com.hps.july.sync.nfs.adapters.CurrencyRatesAdaptor;
import com.hps.july.sync.nfs.adapters.PaysAdaptor;
import com.hps.july.sync.nfs.adapters.SFAdaptor;
import com.hps.july.sync.nfs.adapters.TaxesAdaptor;
import com.hps.july.sync.nfs.adapters.UsersAdaptor;
import com.hps.july.sync.nfs.adapters.VendorSitesAdaptor;
import com.hps.july.sync.nfs.adapters.VendorsAdaptor;
import com.hps.july.sync.nfs.adapters.ZPAdaptor;
/**
 * @author Shafigullin Ildar
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class FactoryNFS extends AbstractFactory {
	public static final String QRY_BANKS = "BANKS";
	public static final String QRY_VENDORS = "VENDORS";
	public static final String QRY_VENDORSITES = "SITES";
	public static final String QRY_ACCOUNTS = "ACCOUNTS";
	public static final String QRY_CURRENCY = "CURRENCY";
	public static final String QRY_USERS = "USERS";
	public static final String QRY_ZP = "ZP";
	public static final String QRY_SF = "SF";
	public static final String QRY_PAYS = "PAYS";
	public static final String QRY_CONTRACTS = "CONTRACTS";
	public static final String QRY_TAXRATES = "TAXES";
	public static final String QRY_CURRRATES = "RATES";
	/* (non-Javadoc)
	 * @see com.hps.july.sync.client.AbstractFactory#getAdapter(com.hps.july.sync.Query, com.hps.july.sync.DB, com.hps.july.sync.DB, com.hps.july.sync.DB, java.util.Properties)
	 */
	public Collaboration getAdapter(Query qry, DB logSourse, Properties prop) {
		DB sourseDB = new DB(prop, "NFS");
		DB targetDB = new DB(prop, "NRI");
		Collaboration adaptor = null;
		if (qry.isQueryType(QRY_BANKS)) {
			adaptor = new BanksAdaptor(targetDB, sourseDB);
		} else if (qry.isQueryType(QRY_CURRENCY)) {
			adaptor = new CurrencyAdaptor(targetDB, sourseDB);
		} else if (qry.isQueryType(QRY_VENDORSITES)) {
			adaptor = new VendorSitesAdaptor(targetDB, sourseDB);
		} else if (qry.isQueryType(QRY_VENDORS)) {
			adaptor = new VendorsAdaptor(targetDB, sourseDB);
		} else if (qry.isQueryType(QRY_ACCOUNTS)) {
			adaptor = new AccountsAdaptor(targetDB, sourseDB);
		} else if (qry.isQueryType(QRY_USERS)) {
			adaptor = new UsersAdaptor(targetDB, sourseDB);
		} else if (qry.isQueryType(QRY_ZP)) {
			adaptor = new ZPAdaptor(targetDB, sourseDB);
		} else if (qry.isQueryType(QRY_SF)) {
			adaptor = new SFAdaptor(targetDB, sourseDB);
		} else if (qry.isQueryType(QRY_PAYS)) {
			adaptor = new PaysAdaptor(targetDB, sourseDB);
		} else if (qry.isQueryType(QRY_CONTRACTS)) {
			adaptor = new ContractsAdaptor(targetDB, sourseDB);
		} else if (qry.isQueryType(QRY_TAXRATES)) {
			adaptor = new TaxesAdaptor(targetDB, sourseDB);
		} else if (qry.isQueryType(QRY_CURRRATES)) {
			adaptor = new CurrencyRatesAdaptor(targetDB, sourseDB);
		}
		return adaptor;
	}
}
