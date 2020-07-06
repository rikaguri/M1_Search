package com.asreader.sdevice.dongle.custom;

public class CellData
{
	String name;
	String value;
	String rssi;
	String rfm;
	String time;

	public CellData()
	{

	}

	public CellData(String name,String value)
	{
		this.name = name;
		this.value = value;
	}

	public String getRssi()
	{
		return rssi;
	}

	public void setRssi(String rssi)
	{
		this.rssi = rssi;
	}


	public String getRFM()
	{
		return rfm;
	}

	public void setRFM(String rfm)
	{
		this.rfm = rfm;
	}

	public String getTime(){return time;}


	public void setImage()
	{

	}


	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public String getValue()
	{
		return value;
	}
	public void setValue(String value)
	{
		this.value = value;
	}



}
