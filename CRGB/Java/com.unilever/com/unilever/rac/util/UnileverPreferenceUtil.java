/**===================================================================================
 *             
 *                     Unpublished - All rights reserved
 * ===================================================================================
 * File Description: UnileverPreferenceUtil.java
 * This is a utility class preference related operations like create, modify, delete
 * basic functions in Teamcenter to be carried for Unilever activities.
 * 
 * ===================================================================================
 * Revision History
 * ===================================================================================
 * Date         	Name       			  TCEng-Release  	Description of Change
 * ------------   --------------------	  -------------    ---------------------------
 * 17-Jul-2014	  Jayateertha M Inamdar   TC10.1.1.1        Initial Version
 * 
 *  $HISTORY$
 * ===================================================================================*/

package com.unilever.rac.util;

import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;


public class UnileverPreferenceUtil
{

	/** The Preference service*/
	private static TCPreferenceService prefServices = null;

	/**
	 * Returns String array of values of the given preference name 
	 * and scope 
	 * 
	 * @return String[] The values
	 */
	public static String[] getStringPreferenceValues(int scope,String prefName)
	{
		String prefValues[] = null;

		if( prefServices == null )
		{
			prefServices = UnileverUtility.getPreferenceService();
			if( prefServices == null )
			{
				return null;
			}
		}
		prefValues = prefServices.getStringArray(scope, prefName);
		if(prefValues.length > 0 && prefValues[0].trim().length() > 0 )
		{
			return prefValues;
		}
		return null;
	}

	/**
	 * Returns String value of the given preference name 
	 * and scope 
	 * 
	 * @return String[] The values
	 */
	public static String getStringPreferenceValue(int scope,String prefName)
	{
		String prefValue = null;

		if( prefServices == null )
		{
			prefServices = UnileverUtility.getPreferenceService();
			if( prefServices == null )
			{
				return null;
			}
		}
		prefValue = prefServices.getString(scope, prefName);
		if( prefValue != null && prefValue.trim().length() > 0 )
		{
			return prefValue;
		}

		return null;
	}
	
	/**
     * Returns boolean value of the given preference name 
     * 
     * @return boolean The value
     */
    public static boolean getLogicalPreferenceValue( int scope, String prefName ) {
        Boolean prefValue = null;

        prefServices = UnileverUtility.getPreferenceService();
        if ( prefServices != null ) {
            prefValue = prefServices.getLogicalValue( prefName );
        }

        return prefValue.booleanValue();
    }

	/**
	 * Returns integer of values of the given preference name 
	 * and scope 
	 * @return Integer the integer preference value
	 */
	public static int getIntPreferenceValues(int scope,String prefName,int defaultValue)
	{
		if( prefServices == null )
		{
			prefServices = UnileverUtility.getPreferenceService();
			if( prefServices == null )
			{
				return 0;
			}
		}

		return prefServices.getInt(scope, prefName, 0);
	}

	/**
	 *  Method to Create/Update the preference with specified arguments
	 *  
	 *  @param scope The scope of preference
	 *  @param prefname The name of preference
	 *  @param desc The description
	 *  @param type The type of preference
	 *  @param isArray Is preference a array type
	 *  @param prefCatgory The Category
	 *  @param values The values to be stored
	 */
	public static boolean createUpdatePreference(int scope,String prefName,String desc,String type,boolean isArray,String prefCatgory,String[] values)
	{
		boolean bResult = true;
		try
		{
			if(values==null)
			{
				values = new String[0];
			}
			UnileverPreferenceUtil.modifyPreference(scope, prefName, desc, type, isArray, 
					prefCatgory, values );	
		}
		catch (Exception e)
		{
			bResult = false;
			MessageBox.post(e.getMessage(),getString(ERROR_TITLE),MessageBox.ERROR);
		}	
		return bResult;
	}


	/**
	 * Function to make a SOA call to create\modify preference
	 * 
	 * @param scopeInt
	 * @param preferenceName
	 * @param preferenceDesc
	 * @param type
	 * @param isArray
	 * @param prefCatgory
	 * @param values
	 */
	public static void modifyPreference(int scopeInt,String preferenceName,String preferenceDesc,String type,boolean isArray,String prefCatgory,String[] values)
	{
		//modifyPreference(UnileverUtility.getSession(), scopeInt, preferenceName, preferenceDesc, type, isArray, prefCatgory, values );
	}

	/**
	 * Function to delete the preference
	 * 
	 * @param scope The Scope of preference
	 * @param prefName The name of Preference
	 * @return Status The Status of deletion of prefrence 
	 */
	public static boolean deletePreference(int scope,String prefName)
	{

		try 
		{
			if( prefServices == null )
			{
				prefServices = UnileverUtility.getPreferenceService();
				if( prefServices == null )
				{
					return false;
				}
			}
			prefServices.removeEntry(scope, prefName);
		}
		catch (TCException e) 
		{
		}
		return true;
	}

	/**
	 * Method to get Registry value for the input string
	 * It value is not found then an empty string is returned
	 * 
	 * @param Registry key
	 * @return Registry value
	 */
	private static String getString(String key)
	{
		Registry reg = Registry.getRegistry(THISCLASS);
		String value = "";
		if(reg.getString(key) != null)
		{
			value = reg.getString(key);
		}
		return value;
	}

	private static final String ERROR_TITLE = "ERROR.TITLE";
	private static final String THISCLASS = "com.unilever.rac.util.util";

	/** String preference. */
	public static final String PREF_preference_string = "STRING";

	/** Logical preference. */
	public static final String PREF_preference_logical = "LOGICAL";

	/** Integer preference. */
	public static final String PREF_preference_integer = "INTEGER";

	/** Double preference. */
	public static final String PREF_preference_double = "DOUBLE";

	/** Date preference. */
	public static final String PREF_preference_date = "DATE";
}
