package com.unilever.rac.pam;

import java.util.Vector;

public class TMDetails {
	
	public String name="";
	
	public String desc="";
	
	public String url="";
	
	public Vector<String> csets;

	public TMDetails()
	{
		csets = new Vector<String>();		
	}
}