/**===================================================================================
 *             
 *                     Unpublished - All rights reserved
 * ===================================================================================
 * File Description: UnileverUtility.java
 * This is a utility class for all the basic functions in Teamcenter to be carried
 * for Unilever activities.
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.teamcenter.rac.aif.kernel.AbstractAIFSession;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.KernelPlugin;
import com.teamcenter.rac.kernel.ListOfValuesInfo;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;
import com.teamcenter.rac.kernel.TCComponentType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.services.ISessionService;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.OSGIUtil;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.services.rac.core.LOVService;
import com.teamcenter.services.rac.core._2013_05.LOV;
import com.teamcenter.services.rac.core._2013_05.LOV.InitialLovData;
import com.teamcenter.services.rac.core._2013_05.LOV.LOVInput;
import com.teamcenter.services.rac.core._2013_05.LOV.LOVSearchResults;
import com.teamcenter.services.rac.core._2013_05.LOV.LOVValueRow;
import com.teamcenter.services.rac.core._2013_05.LOV.LovFilterData;
import com.unilever.rac.ui.common.UL4Common;


public class UnileverUtility 
{

    public static boolean isPerfMonitorTriggered = true;
	
	/**
     * return the preference service
     *
     * @return TCPreferenceServcie
     */
    public static TCPreferenceService getPreferenceService()
    {
        TCPreferenceService prefService = (TCPreferenceService) OSGIUtil.getService(
                KernelPlugin.getDefault(), TCPreferenceService.class );
        if( prefService == null && getSession() != null )
        {
        	prefService = getSession().getPreferenceService();
        }
        
        return prefService;
    }
    
    /**
     * Return the Session
     * 
     * @return The Session
     */
    public static TCSession getSession()
    {
    	TCSession session = null;
    	try 
    	{
			ISessionService sessionService = (ISessionService) OSGIUtil.getService(
			        KernelPlugin.getDefault(), ISessionService.class );
			if( sessionService != null )
			{
				session =(TCSession)sessionService.getSession(TCSession.class.getName());
			}
		}
    	catch (Exception e) 
    	{
    		//log.error("Problem occured while getting the session", e);
    		System.out.println("Problem occured while getting the session: \n Ex Details: " + e);
		}
		
		try 
		{
			if( session == null )
			{
				session = (TCSession)AIFUtility.getSessionManager().getSession(TCSession.class.getName()); 
			}
		}
		catch (Exception e) 
		{
			//log.error("Problem occured while getting the session", e);
			System.out.println("Problem occured while getting the session: \n Ex Details: " + e);
		}
		
		if( session == null )
		{
			Registry reg = Registry.getRegistry("com.unilever.rac.util.util" );
			MessageBox.post( reg.getString("session.MSG"), reg.getString("ERROR.TITLE"),MessageBox.ERROR);
		}
		
    	return session;
    }
    
    /**
     * Gets the AbstractAIFSession.
     *
     * @return The Default Session
     */
    public static AbstractAIFSession getDefaultSession()
    {
        return AIFUtility.getSessionManager().getActivePerspectiveSession();
    }
	
	/**
     * Calculates the time taken for an action and prints the time in RAClog.
     *
     * @return void
     */
	public static void getPerformanceTime(long startTime, String customMsg)
    {
    	long endTime = System.currentTimeMillis();
		
		long difference = endTime - startTime;
		
		System.out.println(" PERFORMANCE MONITORING  : " + customMsg + " Duration=" + difference +" ms\n");
    }
	
	/**
     * Calculates the time taken for an action and prints the time in RAClog.
     *
     * @return void
     */
    
    public static void getPerformanceMonitorPrefValue( TCSession session )
	{
		TCPreferenceService pref = session.getPreferenceService();
		String prefValue = pref.getStringValue("U4_Performance_Monitor");
		
		if(prefValue.equalsIgnoreCase("ON"))
		{
			UnileverUtility.isPerfMonitorTriggered = true;
		}		
		
		return ;
	}
    
    /**
     * Gets the LOV values from the input LOV name
     * @param componentType 
     * @param lovName2 
     * @param String LOV Name
     * @return String[] String array of LOV values
     */
    public static String[] getLOVValueArrayBySoa( TCComponentType componentType, String propName, String lovName ) {

        String name = "";
        if( propName.startsWith( "revision:" ) ) {
            name = propName.substring( propName.indexOf( ":" ) + 1, propName.length() );
        }
        
        List< String > list = new ArrayList< String >();
        
        LOVInput lovInput = new LOVInput();
        lovInput.boName = componentType.getClassName();
        lovInput.operationName = "Create";
        lovInput.owningObject = null;
        
        LovFilterData lovFilterData = new LovFilterData();
        lovFilterData.filterString = "";
        lovFilterData.maxResults = 9999;
        lovFilterData.numberToReturn = 9999;
        lovFilterData.order = 1;
        
        InitialLovData paramInitialLovData = new InitialLovData();       
        paramInitialLovData.lov = null; 
        paramInitialLovData.propertyName = name;
        paramInitialLovData.lovInput = lovInput;
        paramInitialLovData.filterData = lovFilterData;

        LOV lovService = LOVService.getService( (TCSession) getDefaultSession() );
        LOVSearchResults searchResults = lovService.getInitialLOVValues( paramInitialLovData );
        
        LOVValueRow[] lovValues = searchResults.lovValues;
        for ( LOVValueRow lovValueRow : lovValues ) {
            Map< String, String[] > propValueMap = lovValueRow.propInternalValues;
            Iterator< String > itr = propValueMap.keySet().iterator();
            while ( itr.hasNext() ) {
                String key = (String) itr.next();
                String[] valArr = (String[]) propValueMap.get( key );
                String value = valArr[0];
                
                list.add( value );
            }
        }
        
        String[] strArr = convertListToArray( list );
        
        return strArr;
    }
        
    private static String[] convertListToArray( List< String > list ) {
        String[] arr = new String[ list.size() ];
        for ( int i = 0; i < arr.length; i++ ) {
            arr[ i ] = list.get( i );
        }
        
        return arr;
    }
}
