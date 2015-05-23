package com.example.westsnow.util;

public class SnailException extends Exception {

	public static final String EX_DESP_MapNotExist = "Map does not exist.";
	public static final String EX_DESP_LocationNotExist = "Can not access current location.";
	public static final String EX_DESP_JsonNull = "Json is null.";
	public static final String EX_DESP_PathNotExist = "No Path Found.";
	public static final String EX_DESP_LocationNameNotExist = "No Location Found.";
	public static final String EX_DESP_ReachedDestination = "Reach Destination.";
	public static final String EX_DESP_NoInternet = "Can not connect internet.";


	private String m_strExDesp;
	private String m_strParam1, m_strParam2;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public SnailException(String strExDesp) {
		
		this(strExDesp, null, null);
		
	}

	public SnailException(String strExDesp, String strParam1) {
		
		this(strExDesp, strParam1, null);
		
	}

	public SnailException(String strExDesp, String strParam1, String strParam2) {
		
		m_strExDesp = strExDesp;
		m_strParam1 = strParam1;
		m_strParam2 = strParam2;
	}

	public String getExDesp() {
		
		String strExDesp = m_strExDesp;
		
		if (m_strParam1 != null) {
			strExDesp = strExDesp + " param1 = [" + m_strParam1 + "]";
		}
		
		if (m_strParam2 != null) {
			strExDesp = strExDesp + " param2 = [" + m_strParam2 + "]";
		}
		
		return strExDesp;
	}
	
	public String toString() {
		
		return getExDesp();
	}
}
