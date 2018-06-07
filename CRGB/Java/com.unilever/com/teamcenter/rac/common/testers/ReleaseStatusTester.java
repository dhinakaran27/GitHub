package com.teamcenter.rac.common.testers;

import java.util.StringTokenizer;

import org.eclipse.core.expressions.PropertyTester;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCException;

public class ReleaseStatusTester extends PropertyTester {

	@Override
	public boolean test(Object paramObject1, String paramString, Object[] paramArrayOfObject, Object paramObject2) {
		boolean decision = false;
		
		TCComponent mComp = (TCComponent)paramObject1;
		StringTokenizer mTokenizer = new StringTokenizer( (String)paramObject2, "," );
		String mStatus = null;
		
		try {
			mStatus = mComp.getTCProperty( "release_status_list" ).toString();
		} catch (TCException e) {
			e.printStackTrace();
		}
		
		while( mTokenizer.hasMoreTokens( ))
		{
			if( mTokenizer.nextToken( ).equalsIgnoreCase( mStatus))
			{
				decision =  true;
				break;
			}
		}
		
		return decision;
	}

}
