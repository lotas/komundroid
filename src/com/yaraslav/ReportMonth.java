package com.yaraslav;

public class ReportMonth 
{
	private int mMonth;
	private int mYear;
	private String mTitle;
	
	public int getMonth() 
	{
		return mMonth; 
	}
	
	public int getYear()
	{
		return mYear;		
	}
	
	public String getTitle()
	{
		return mTitle;
	}

	
	public ReportMonth(int year, int month, String title)
	{
		mYear = year;
		mMonth = month;
		mTitle = title;
	}
	
	public void setMonth(int month)
	{
		mMonth = month;
	}
	
	public void setYear(int year)
	{
		mYear = year;
	}
	
	public void setTitle(String title)
	{
		mTitle = title;
	}
	
	@Override
	public String toString() 
	{
		return getTitle();
	}
}
