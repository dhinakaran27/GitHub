/**===================================================================================
 *             
 *                     Unpublished - All rights reserved
 * ===================================================================================
 * File Description: UnileverQueryUtil.java
 * This is a utility class used to perform query related operations like create, 
 * modify, delete operations to be carried out for Unilever activities.
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

import com.teamcenter.rac.kernel.SoaUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentQueryType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.query.SavedQueryService;
import com.teamcenter.services.rac.query._2006_03.SavedQuery.GetSavedQueriesResponse;
import com.teamcenter.services.rac.query._2006_03.SavedQuery.SavedQueryObject;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.ExecuteSavedQueriesResponse;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.SavedQueryInput;

public class UnileverQueryUtil
{
	/** The TCComponentQueryType */
	private static TCComponentQueryType queryType = null;
	
	/**
	 * function to initialize TCComponentQueryType
	 * @param session The Current session
	 */
	public static void initializeQueryType(TCSession session)
	{
		/** Get the query type name */
		String queryTypeName = QUERYTYPE;
		
		try 
		{
			/** get the query type */
			queryType = (TCComponentQueryType)session.getTypeService().getTypeComponent(queryTypeName);
		} 
		catch (TCException e) 
		{
			/** Show error message */
			String title = Registry.getRegistry(THISCLASS).getString(ERROR_TITLE);
			if(title != null)
				MessageBox.post(e.getMessage(),title,MessageBox.ERROR);	
		}
	}
	
	/**
	 * Function to create query.
	 * It initializes the query type,If query
	 * previously exists then it deletes the query,
	 * creates and saves the query.
	 * It returns the newly created query.
	 * 
	 * @param session 			The Current session
	 * @param queryName 		The Name of query to create
	 * @param desc  			The Description of query
	 * @param clause			The clause
	 * @return TCComponentQuery	The newly created query
	 */
	public static TCComponentQuery createQuery(TCSession session, String queryName,String desc, String searchClass, String clause)
	{
		try
		{
			initializeQueryType(session);
			
			/** Find the query if its exits */
			TCComponentQuery query = (TCComponentQuery)queryType.find(queryName);

			/** if query exists delete the query */
			if(query!=null)
			{
				deleteQuery(session, query);
			}
			/** Create the query */
			query = queryType.create(queryName, desc, searchClass, clause , 0);
			query.lock();
			query.save();
			query.unlock();
			/** Return newly created query */ 
			return query;
		}
		catch (TCException e)
		{
			String title = Registry.getRegistry(THISCLASS).getString(ERROR_TITLE);
			if(title != null)
				MessageBox.post(e.getMessage(),title,MessageBox.ERROR);	
		}
		/** If error occurs then return null*/
		return null;
	}

	/**
	 * Function to delete the query.
	 * This function finds the query 
	 * and deletes it.
	 * 
	 * @param session 	The Current session
	 * @param queryName The Query name
	 */

	public static void deleteQuery(TCSession session, TCComponentQuery query)
	{
		try 
		{
			/** If query type is not initialized */
			if(queryType == null)
			{
				initializeQueryType(session);
			}
			
			/** if query exists delete the query */
			if(query!=null)
			{
				query.delete();
			}
		}
		catch (TCException e)
		{
			String title = Registry.getRegistry(THISCLASS).getString(ERROR_TITLE);
			if(title != null)
				MessageBox.post(e.getMessage(),title,MessageBox.ERROR);	
		}
	}
	
	/**
	 * Function which returns query component.
	 * 
	 * @param session 	The Current session
	 * @param queryName The Query name to find
	 */

	
	public static TCComponentQuery getQuery ( TCSession session, String queryName )
    {
        TCComponentQuery query = null;
        
        if ( session == null ) 
        	return null;

        try
        {
            TCComponentQueryType queryComponentType = (TCComponentQueryType)session.getTypeComponent(QUERYTYPE);
            query = (TCComponentQuery)queryComponentType.find(queryName);
        }
        catch (TCException ex ) {}

        return query;
    }
	
	/**
	 * Function finds the dataset of type "XMLReportFormatter" and returns the named reference -
	 * xsl file attached to it.
	 * 
	 * @param session 	The Current session
	 * @param xslFileNameWithoutExt The file name to search dataset of type "XMLReportFormatter"
	 */


	public static TCComponentDataset getXSLDataset( TCSession session, String xslFileNameWithoutExt)
	{
		String name = "";

		try
		{	     
			TCComponentQuery query = getQuery(session, Registry.getRegistry(THISCLASS).getString("QUERY_NAME"));
			
			if(query != null)
			{
				String[] entryNames = new String[2];
				String[] valueNames = new String[2];
				
				entryNames[0] = Registry.getRegistry(THISCLASS).getString("QUERY_ENTRY_NAME_DATASET_NAME");
				valueNames[0] = xslFileNameWithoutExt;
				entryNames[1] = Registry.getRegistry(THISCLASS).getString("QUERY_ENTRY_NAME_DATASET_TYPE");
				valueNames[1] = Registry.getRegistry(THISCLASS).getString("QUERY_DATASET_TYPE_VALUE");				

				TCComponent[] results = query.execute(entryNames, valueNames);
	            for( TCComponent comp : results )
	            {
	            	name = comp.toString();
	            	if(xslFileNameWithoutExt.equals(name))
	            	{
	            		return ((TCComponentDataset) comp);
	            	}
	            }				
			}            
		}
		catch(TCException ex)
		{
			ex.printStackTrace();
		}
		
		return null;
	}
	
	public static TCComponent[] executeQuery( String queryName, String[] entries, String[] values ) throws TCException
	{
		if( queryName == null || queryName.length() == 0 || entries == null || values == null )
		{
			return null;
		}
		try 
		{
			TCComponentQuery query = null;
			
			GetSavedQueriesResponse response = SavedQueryService.getService(UnileverUtility.getSession()).getSavedQueries();
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
				
				ExecuteSavedQueriesResponse queryresponse =  SavedQueryService.getService(
						UnileverUtility.getSession()).executeSavedQueries(input);
				
				SoaUtil.checkPartialErrors( queryresponse.serviceData );
				
				return queryresponse.arrayOfResults[0].objects;
			}
		} 
		catch (ServiceException e)
		{
		}
		
		return null;
		
	}
	

	
	private static final String QUERYTYPE = "ImanQuery";
	private static final String ERROR_TITLE = "ERROR.TITLE";
	private static final String THISCLASS = "com.unilever.rac.util.util";
}
