package com.hps.july.cdbc.lib;

/**
 * ������ ��� ������������� �������� �������.
 * Creation date: (10.03.2004 13:29:22)
 * @author: Dmitry Dneprov
 */
public class CDBCColumnObject {
	Object obj;
	/**
	 * CDBCColumnObject constructor.
	 */
	public CDBCColumnObject(Object aobj) {
		super();
		obj = aobj;
	}
	/**
	 * ���������� �������� ������� ��� ������.
	 * Creation date: (10.03.2004 14:50:28)
	 * @return java.lang.Object
	 */
	public Object asObject() {
		return obj;
	}
	/**
	 * ���������� �������� ������� ��� ������ + ������ trim.
	 * Creation date: (10.03.2004 14:46:30)
	 * @return java.lang.String
	 */
	public String asString() {
		if (obj != null) {
			return obj.toString().trim();
		}
		return "";
	}
}
