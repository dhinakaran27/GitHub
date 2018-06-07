
package com.unilever.rac.pam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCException;

public class PAMRuleValidate 
{	
	private String rule = null ;	
	public ArrayList<String> alMandatory = null ;	
	public ArrayList<String> alTMMORLWHD = null ;	
	public ArrayList<String> alMROW = null ;	
	public ArrayList<String> alMROWMandatory = null ;
	
	public PAMRuleValidate(TCComponentForm selectedComponent)
	{
		alMandatory = new ArrayList<String>();
		alTMMORLWHD = new ArrayList<String>();
		alMROW = new ArrayList<String>();
		alMROWMandatory = new ArrayList<String>();
		
		 try
		 {
			rule =selectedComponent.getStringProperty("u4_validation_rule");
			
			if(rule != null && rule.length() > 4)
			{
				parseRules();
			}
		 }
		 catch (TCException e1)
		 {
			e1.getMessage();
		 }	

	}
	
	private void parseRules()
	{
		Vector<String> rules = spiltRules(rule);	
		
		for(int inx=0 ; inx < rules.size() ; inx++ )
		{	
			String[] str = rules.get(inx).split(":");
			
			if(str.length == 2 )	
			{
				if(str[0].equalsIgnoreCase("M"))
				{
					alMandatory.addAll(Arrays.asList(str[1].split("&")));	
				}
				else if(str[0].equalsIgnoreCase("M-TMM") || str[0].equalsIgnoreCase("M-LWHD"))
				{
					alTMMORLWHD.addAll(Arrays.asList(str[1].split("\\|")));	
				}
				else if(str[0].equalsIgnoreCase("M-ROW"))
				{
					String[] val = str[1].split("\\?");
					
					if(val.length == 2 )
					{
						alMROW.addAll(Arrays.asList(val[0].split("\\|")));	
						alMROWMandatory.addAll(Arrays.asList(val[1].split("&")));							
					}
				}
			}				
		}		
	}

	
	private Vector<String> spiltRules(String rule)
	{
		Vector<String> rules = new Vector<String>();
		
		int left  = rule.indexOf("[") ;
		int right = rule.indexOf("]") ;
		int index = 0;

		while(left < right && left != -1)
		{	
			rules.add(rule.substring(++left, right)) ;
			left  = rule.indexOf("[",right) ;
			right  = rule.indexOf("]",left) ;
		}		
		
	    return rules;
	}
	
}