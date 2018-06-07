#define TC_MACROS_H
#include <itk/mem.h>
#include <property/prop.h>
#include <tc/tc.h>
#include <tc/tc_startup.h>
#include <tc/tc_util.h>
#include <tc\emh.h>
#include <tccore/aom.h>
#include <tccore/aom_prop.h>
#include <user_exits\epm_toolkit_utils.h>

using namespace std;


int ITK_user_main(int argc,char* argv[])
{	
	int ifail = 0 ;

	char *value = NULL;
	tag_t tObject = NULLTAG ;
	
	int *ptr = (int * ) malloc(sizeof( int ) * 2 );	

	AOM_ask_value_string(tObject,"",&value);
	

	return 0 ;
}

