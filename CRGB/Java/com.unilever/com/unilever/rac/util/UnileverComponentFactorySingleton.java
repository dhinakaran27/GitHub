package com.unilever.rac.util;

import com.teamcenter.rac.kernel.SoaUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.query.SavedQueryService;
import com.teamcenter.services.rac.query._2006_03.SavedQuery.GetSavedQueriesResponse;
import com.teamcenter.services.rac.query._2006_03.SavedQuery.SavedQueryObject;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.SavedQueryInput;
import com.unilever.rac.util.UnileverUtility;

public class UnileverComponentFactorySingleton 
{
	
	   private static UnileverComponentFactorySingleton singleton 	= null;	   
	   private static GetSavedQueriesResponse response 				= null;
	   private static SavedQueryService savedQueryService			= null;
	   
	   /* A private Constructor prevents any other 
	    * class from instantiating.
	    */
	   private UnileverComponentFactorySingleton()
	   { 
		   //Don't do anything
	   }
	   
	   /* Static 'instance' method */
	   public static UnileverComponentFactorySingleton getInstance( ) 
	   {
		   if(singleton == null)
		   {
			   try 
			   {
				   singleton = new UnileverComponentFactorySingleton( );
				   response = SavedQueryService.getService(UnileverUtility.getSession()).getSavedQueries();
				   savedQueryService =  SavedQueryService.getService(UnileverUtility.getSession());
			   } 
			   catch (ServiceException e) 
			   {
				   // TODO Auto-generated catch block
				   e.printStackTrace();
			   }
		   }
		   
		   return singleton;
	   }
	   
	   /**
		 * Method to get the saved query object array from the SavedQuertService to execute the query only when we have the service already available
		 * using this Singleton class' static method.
		 * 
		 * @param queryName 	Saved Query Name
		 * 
		 * @return savedQueries	The SavedQueryObject array 
		 */
	   public static SavedQueryObject[] getSavedQueryParams(String queryName) throws TCException, ServiceException
	   {
		   	TCComponentQuery query = null;
		   	
		   	//GetSavedQueriesResponse response = SavedQueryService.getService(UnileverUtility.getSession()).getSavedQueries();
		   	if(response != null)
		   	{
				SoaUtil.checkPartialErrors( response.serviceData );
				SavedQueryObject[] savedQueries = response.queries;
				
				for (SavedQueryObject savedQueryObject : savedQueries) 
				{
					if( savedQueryObject.name.equalsIgnoreCase(queryName) )
					{
						query = savedQueryObject.query;
						break;
					}
				}
				
				if( query != null )
				{
					return savedQueries;				
				}
		   	}
			
			return null;
	   }
	   
	   /**
		 * Method to get the saved query object results from the SavedQuertService to execute the query only when we have the service already available
		 * using this Singleton class' static method.
		 * 
		 * @param queryName 	Saved Query Name
		 * @param entries    	Saved Query Entries
		 * @param values    	Saved Query Values
		 * 
		 * @return savedQueryResults	The SavedQueryResults array 
		 */
	   public TCComponent[] executeQueryOptimised( String queryName, String[] entries, String[] values ) throws TCException
	   {
			if( queryName == null || queryName.length() == 0 || entries == null || values == null )
			{
				return null;
			}
			try 
			{
				TCComponentQuery query = null;
				
				//GetSavedQueriesResponse response = SavedQueryService.getService(UnileverUtility.getSession()).getSavedQueries();
				if(response != null)
			   	{
					SoaUtil.checkPartialErrors( response.serviceData );
					SavedQueryObject[] savedQueries = response.queries;
					
					for (SavedQueryObject savedQueryObject : savedQueries) 
					{
						if( savedQueryObject.name.equalsIgnoreCase(queryName) )
						{
							query = savedQueryObject.query;
							break;
						}
					}
					
					if( query != null )
					{
						SavedQueryInput[] input = new SavedQueryInput[1];
						
						input[0] = new SavedQueryInput();
						
						input[0].query = query;
						input[0].entries = entries;
						input[0].values = values;
						
						if(savedQueryService != null)
						{
							savedQueryService.executeSavedQueries(input);
							SoaUtil.checkPartialErrors( savedQueryService.executeSavedQueries(input).serviceData );							
							return savedQueryService.executeSavedQueries(input).arrayOfResults[0].objects;
						}
					}
			   	}				
			} 
			catch (TCException e)
			{
			}
			
			return null;
			
		}
	   /* Other methods protected by singleton-ness */
	   public static void demoMethod( ) 
	   {
	      System.out.println("demoMethod for singleton"); 
	   }
	

}
