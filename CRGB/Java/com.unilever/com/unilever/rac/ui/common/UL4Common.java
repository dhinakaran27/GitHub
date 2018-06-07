
package com.unilever.rac.ui.common;

public class UL4Common 
{	
	/* BO */
	public static String DATASET    			  = "Dataset";
	public static String FORM     			 	  = "U4_MaterialClassificationForm";
	public static String PROJECTREV		    	  = "U4_ProjectRevision";
	public static String TCPROJECT                = "TC_Project";
	public static String PAMREVISION              = "U4_PAMRevision";
	public static String PMLREVISION              = "U4_PMLRevision";
	public static String PACKREVISION             = "U4_ComponentRevision";
	public static String STRUCTPDF				  = "U4_StructuredPDF";
	public static String COMPLIANCERULE_FORM	  = "U4_ComplianceRuleForm";
	public static String COMPONENTPROPERTY_FORM	  = "U4_ComponentPropertyForm";
	public static String ENV_PACKAGINGING_MATL_FORM = "U4_EnvPackagingMatlForm";
	
	/* Attribute */
	public static String NAME                     = "name" ;
	public static String ROLENAME		          = "role_name";
	public static String OBJECT_NAME		 	  = "object_name";
	public static String OBJECT_ID		 	      = "object_id";
	public static String OBJECT_DESC		 	  = "object_desc";
	public static String OBJECT_TYPE		 	  = "object_type";
	public static String ITEMID                   = "item_id" ;
	public static String REVID                    = "item_revision_id" ;
	public static String COMP_CLASS			 	  = "u4_component_class";
	public static String PAM_FRAME			 	  = "u4_pam_frame_type";
	public static String SHAPE					  = "u4_shape";
	public static String TECHNOLOGY				  = "u4_technology";
	public static String MFG_PROCESS		  	  = "u4_manufacturing_processes"; 
	public static String WEIGHT_VOLUME		  	  = "u4_cu_weight_volume";	
	public static String SALES_COUNTRY		  	  = "u4_sales_country";
	public static final String COMPONENT_DESCRIPTION = "u4_component_description";
	public static String TCPROJECTIDS             = "project_ids";
	public static String []HIDDENCLASSATTR        = { "u4_component_class_shape" ,"u4_component_class_mfgpro" , "u4_component_class_wv"};
	public static String DSREVISIONNUMBER		  = "revision_number";
	public static String OWNER					  = "owning_user";
	public static String OWNING_GROUP			  = "owning_group";
	public static String CREATIONDATE			  = "creation_date";
	public static String COMP_COMMODITY		 	  = "u4_component_commodity";
	public static String MAT_CLASS				  = "u4_material_class";
	public static String MAT_COMMODITY		 	  = "u4_material_commodity";
	public static String DELIVERED_FORM		 	  = "u4_delivered_form";
	public static String TEMPLATE_ID			  = "u4_template_id";
	public static String TEMPLATE_LAST_SYNC_REV_ID	= "u4_last_sync_revision_id";
	public static String INTERSPEC_ID             = "u4_interspec_id";
	public static String RELEASED_DATE			  = "date_released";
	public static String BASE_UOM			  	  = "u4_base_uom";
	public static final String RELEASED_STATUS 	  = "Released";
	public static final String PROPERTY_ATTRIBUTE_NAME  = "u4_property" ;
	public static final String TARGET_ATTRIBUTE_NAME    = "u4_target" ;
	public static final String MIN_ATTRIBUTE_NAME       = "u4_min" ;
	public static final String MAX_ATTRIBUTE_NAME       = "u4_max" ;
	public static final String REASONFORISSUE			= "u4_reason_for_issue";
	
	/* Relations */
	public static String DEVRELATION        	  = "U4_DevelopmentRelation";
	public static String GMCFORMRELATION    	  = "U4_MatlClassRelation";
	public static String IMANSPECIFICATION        = "IMAN_specification";
	public static String IMANREFERENCE            = "IMAN_reference";
	public static String ENVIRONMENTALREPORT      = "U4_EnvReportRelation";
	public static String PAMSPECIFICATION         = "U4_PAMSpecification";
	public static String SUSTAINBILITY_RELATION   = "U4_SustainabilityRelation";
	public static String ENVIRNOMENTAL_RELATION   = "U4_EnvironmentalRelation";
	public static String WEIGHTRELATION 		  = "U4_WeightRelation";
	
	/* Misc */
	public static String PREF_ENVIRONMENTAL_REPORT_TYPE    = 	"U4_EnvironmentalReportType";
	public static String PREF_PRIMARY_PACK_TYPE    ="UL_primary_packaging_list";
	public static String PREF_MATLCOMD_SALESCOUTNRY		    = "U4_MaterialCommodity_NonCompliant_SalesCountry";
	public static String PREF_GMCPACKCOMPONENT    = "U4_GMCPackComponentType";
	public static String PREF_HIDDENATTRIBUTE     = "U4_GMCPackCompHiddenAttribute";
	public static String STYLESHEETNAME			  = "U4_MaterialClassificationForm";
	public static String PAMREPORTXSL        	  = "U4_PAMSeecReport.xsl";
	public static String PMLRESINREPORTXSL        = "U4_PMLResinSpecReport.xsl";
	public static String PMLSUBSTRATEREPORTXSL    = "U4_PMLSubstrateSpecReport.xsl";
	public static String PMLDECORATIONREPORTXSL   = "U4_DecorationRevisionSpecReport.xsl";
	public static String PMLPAPERNBOARD           = "U4_PaperNBoardRevisionSpecReport.xsl";
	public static String TOTALIMPREPORTXLSX    	  = "TPIR_Template";
	public static String QRY_GROUPMEMBER          = "Admin - Group/Role Membership";
	public static String PDF				      = "PDF";
	public static String DBA				      = "DBA";
	public static String PAMSPECTRANSFERMODE      = "unconfiguredDataFileExport";
	public static String PAMREPORTTRANSFERMODE    = "U4_PAMReportDataExport";
	public static String PMLREPORTTRANSFERMODE    = "U4_PMLReportDataExport";
	
	/** Added for New CAD Component */	
	public static final String ITEM_ID_ATTR_DISP_NAME	  			= "Item ID";
	public static final String OBJ_NAME_ATTR_DISP_NAME				= "Name";
	public static final String REV_ID_ATTR_DISP_NAME	 			= "Revision ID";

	public static final String UNIQUE_DESC_ATTR_DISP_NAME			= "Unique Descriptor";
	public static final String DESCRIPTION_ATTR_DISP_NAME			= "Description";
	
	public static final String CAD_COMPONENT_ITEM_TYPE				= "U4_CADComponent";
	public static final String CAD_COMPONENT_ITEM_REV				= "U4_CADComponentRevision";
	
	public static final String CAD_COMPONENT_SIZE_ATTR				= "u4_size";
	public static final String CAD_COMPONENT_UNIQUE_DESC_ATTR		= "u4_unique_descriptor";
	public static final String CAD_COMPONENT_NAME					= "u4_name";
	
	public static final String PACK_COMPONENT_USES_TEMPLATE_ATTRIBUTE_NAME		= "u4_pack_comp_uses_template";
	
	/** Added for EPAMTableComposite element for object type - Approved Additive Details Form 
	 * 
	 */
	public static final String APPROVED_ADD_DETAILS_FORM_TYPE_NAME	=	"U4_ApprvdAdditiveDtlForm";
	
	//Added by Usha for PnPSpec Report
	public static String PNPREVISION              = "U4_PnPSpecRevision";
	public static String DDEREVISION              = "U4_DDERevision";
	public static String PNPREPORTXSL        	  = "U4_PnPSpecReport.xsl";
	public static String PNPSPECTRANSFERMODE      = "unconfiguredDataFileExport";
	public static String PNPREPORTTRANSFERMODE    = "U4_PnPReportDataExport";
	public static String PNPSPECIFICATION         = "U4_PnPSpecificationRelation";
	
	//Added for PNP List of PAM Report
	public static String PNPLISTOFPAMREPORTXSL        	  = "U4_PnPListOfPAMReport.xsl";
	public static String PNPLISTOFPAMREPORTTRANSFERMODE   = "U4_PnPLOPReportDataExport";
		
	public static final String SPECIFIED_VALUE 			  = "Specified value ";
	public static final String NOT_A_MEMBER 			  = " is not a number";

	/** Added for Component Save As and DDE Save As features*/	
	public static final String COMPONENT_ITEM_TYPE 					= "U4_Component";
	public static final String DDE_ITEM_TYPE	  					= "U4_DDE";
	public static final String QRY_TYPE_PARAM	  					= "Type";
	public static final String QRY_PROJECT_PARAM					= "Project";
	public static final String GENERAL_QRY_NAME 					= "General...";
	public static final String IMAN_MASTER_FORM_REV_TYPE			= "IMAN_master_form_rev";
	public static final String IMAN_MASTER_FORM_TYPE				= "IMAN_master_form";
	public static final String FINAL_RELATION						= "U4_FinalRelation";
	public static final String STAGE_ATTR							= "u4_stage";
	public static final String DESIGN_REALISATION_VALUE				= "Design Realisation";
	public static final String PACKAGING_CONCEPT_CREATION_VALUE		= "Packaging Concept Creation";
	public static final String LEAD_CONCEPT_REJECTED_STATUS 		= "Lead Concept Rejected";
	public static final String PRODUCT_DESIGN_REJECTED_STATUS 		= "Product Design Rejected";
	public static final String PILOT_LOCK_REJECTED_STATUS	 		= "Pilot Lock Rejected";
	
	/** Added for CR#88 */		
	public static final String PAM_STAGE_PILOT_LOCK 				= "Pilot";
	public static final String PAM_STAGE_CAPABILITY_BUILD	 		= "Capability Build";
	public static final String PnP_TO_PAM_CU_RELATION 				= "U4_PnPToPAMCURelation";
	public static final String PnP_TO_PAM_DU_RELATION 				= "U4_PnPToPAMDURelation";
	public static final String PnP_TO_PAM_CASEUNIT_RELATION 		= "U4_PnPToPAMCaseUnitRelation";
	public static final String OBSOLETE_RELEASED_STATUS	 			= "Obsolete";
	public static final String RETIRED_RELEASED_STATUS	 			= "Retired";
	
	
	
	public static final String U4_PAMSPEC_TECHNOLOGY_PREFERENCE		= "U4_pamspec_technology";
	public static final String U4_PACK_COMPONENT_PREFERENCE			= "U4_PackComponent_Template";
	public static final String REVISION_LIST_ATTR					= "revision_list";
	public static final String RELEASE_STATUS_LIST_ATTR				= "release_status_list";
	public static final String PAM_SPECIFICATION_RELATION			= "U4_PAMSpecification";
	public static String[] PAM_RELATION_NAMES						= { "U4_ArticleRelation","U4_AssociatedSpecRelation","U4_ClosureRelation","U4_CompPropertyRelation",
																		"U4_EnvironmentalRelation","U4_GeneralInfoRelation","U4_IntegratedLabelRelation","U4_LegacyMaterialsRelation",
																		"U4_MaterialsRelation","U4_PackageDetailsRelation","U4_RcvgSiteUsgCondRelation","U4_ReferencesRelation",
																		"U4_RemarksRelation","U4_TechnicalDrawingRelation" };
	
	public static String[] PACK_COMPONENT_RELATION_NAMES			= { "U4_MatlClassRelation","U4_BaseExternalDiameterRelation","U4_BaseExternalLengthRelation","U4_BaseExternalWidthRelation",
		"U4_BoreDiameterIDimRelation","U4_BoreDiameterRelation","U4_BottomWidthRelation","U4_BrimfulVolumeRelation",
		"U4_CrestOfThreadE_Dimension","U4_CrestOfThreadRelation","U4_CrestOfThreadT_Dimension","U4_CurlExtDiameterRelation",
		"U4_CurlIntDiameterRelation","U4_DiameterLateralSealRelation","U4_DiameterRelation","U4_DiameterThreadRelation",
		"U4_DisplacementForceRelation","U4_DistanceBosstoStemTipRelation","U4_DistanceCuptoStemTipRelation","U4_DrawingNoRelation",
		"U4_EcogorgeDiameterRelation","U4_EcogorgeHeightRelation","U4_EndWidthRelation","U4_ExternalDiameterRelation",
		"U4_ExternalDimensionRelation","U4_ExternalLengthRelation","U4_ExternalWidthRelation","U4_EyeMarkRelation",
		"U4_FlatBlankSizeRelation","U4_GrooveHeightRelation","U4_GussetWdthBottomRelation","U4_GussetWidthRelation",
		"U4_GussetWidthSideRelation","U4_HeightRelation","U4_InnerDiamtrBeadRelation","U4_InsertOrificeDiameterRelation",
		"U4_InternalDiameterRelation","U4_InternalDimensionRelation","U4_InternalHeightRelation","U4_InternalLengthRelation",
		"U4_InternalWidthRelation","U4_LayerNo1Relation","U4_LayerNo2Relation","U4_LengthRelation","U4_LipRelation",
		"U4_LockingRingDiameterRelation","U4_LockingRingHeightRelation","U4_MajorAxisExternalRelation","U4_MajorAxisHousingRelation",
		"U4_MajorAxisInternalRelation","U4_MajorAxisRelation","U4_MinSkirtThickRelation","U4_MinorAxisExternalRelation",
		"U4_MinorAxisHousingRelation","U4_MinorAxisInternalRelation","U4_MinorAxisRelation","U4_NeckFinishHeightH_Dimension",
		"U4_NeckFinishSlopeRelation","U4_NeckRingDiaExtRelation","U4_NeckRingDiaIntRelation","U4_NeckSnapRingHtRelation",
		"U4_OnOffMechanismRelation","U4_OrificeDiameterRelation","U4_OverallDiameterRelation","U4_OverallExtDiameterRelation",
		"U4_OverallHeightRelation","U4_OverallLengthRelation","U4_OverallWidthRelation","U4_PackagingDimensionsRelation",
		"U4_PitchRelation","U4_ReelWebWidthRelation","U4_RepeatLengthRelation","U4_RestrictedTailPceDiaRelation",
		"U4_RetentionDiaRelation","U4_RetentionLengthRelation","U4_RetentionWidthRelation","U4_RootOfThreadE_Dimension",
		"U4_RootOfThreadRelation","U4_RootOfThreadT_Dimension","U4_SealWdthVerticalRelation","U4_SealWidthBottomRelation",
		"U4_SealWidthFinSealRelation","U4_SealWidthTopRelation","U4_SealingSurfaceRelation","U4_SnapRingDiameterRelation",
		"U4_SnapRingHeightRelation","U4_StackingPitchRelation","U4_StartOfThreadS_Dimension","U4_ThicknessRelation",
		"U4_ThreadPitchRelation","U4_TopExternalDiameterRelation","U4_TopExternalLengthRelation","U4_TopExternalWidthRelation",
		"U4_UnwindCodeRelation","U4_ValveLengthRelation","U4_ValveWidthRelation","U4_VapourPhaseTapDiaRelation","U4_VolumeRelation",
		"U4_WaistWidthRelation","U4_WidthRelation"};
	
	public static final String U4_CTM_APPROVAL_REQD_BY_COMMODITY_PREF    = "U4_CTM_Approval_Reqd_By_Commodity";
	public static final String U4_OPEN_ON_CREATE_PREF                    = "U4_OpenAfterCreatePref";
	
	public static final String XML_RENDERING_STYLESHEET                  = "XMLRenderingStylesheet";
	public static final String PROD_NOTICE_CREATE_STYLESHEET_NAME        = "U4_ProdNoticeCreate";
	public static final String PILOT_NOTICE_CREATE_STYLESHEET_NAME       = "U4_PilotNoticeCreate";
    
	public static final String U4_MATERIAL_CLASSIFICATION_FORM           = "U4_MaterialClassificationForm";
    public static final String PROD_NOTICE_ITEM_COMP_TYPE                = "U4_ProdNotice";
    public static final String PROD_NOTICE_ITEMREV_COMP_TYPE             = "U4_ProdNoticeRevision";
    
    public static final String PILOT_NOTICE_ITEM_COMP_TYPE               = "U4_PilotNotice";
    public static final String PILOT_NOTICE_ITEMREV_COMP_TYPE            = "U4_PilotNoticeRevision";
    
    public static final String ALL_CTM_USERS_LOV_NAME                    = "U4_AllCTMLOV";
    public static final String ALL_PACKAGING_DEVELOPERS_LOV_NAME         = "U4_AllPackagingDeveloperLOV";
    
    public static final String REASON_FOR_CHANGE_ATTR                    = "revision:u4_reason_for_change";
    public static final String TYPE_OF_CHANGE_ATTR                       = "revision:u4_type_of_change";
    public static final String PROJECT_ASSOCIATION_ATTR                  = "revision:u4_project_association";
    public static final String COMPONENT_UPDATER_ATTR                    = "revision:u4_component_updater";
    public static final String PAM_UPDATER_ATTR                          = "revision:u4_pam_updater";
    public static final String RELEASE_APPROVER_ATTR_CTM                 = "revision:u4_release_approver1";
    public static final String RELEASE_APPROVER_ATTR_PAD                 = "revision:u4_release_approver";
    
    public static final String IS_PRIMARY_ATTR                           = "u4_IsPrimary";
    
    public static final String PROBLEM_ITEM_REL_NAME                     = "CMHasProblemItem";
	public static final String GENERALINFORELATION 						 = "U4_GeneralInfoRelation";
    
}