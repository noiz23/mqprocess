/**$Header: v 1.0, 16:00  20-Sep-2016 $*/


/**
File Name:                  INTF_Cmx_Export_Ws.java
Export File Name:           XML String
Author:                     Ruben Dario Echeverri R.
Creation Date:              20-Sep-2016

REVISION HISTORY
    Date:                   20-Sep-2016
    Description:            Initial version
    Author:                 Ruben Dario Echeverri.



Script Type:                Main
Purpose:                    General scripting for export data to Itau CMX System using a WS client.
Assumptions:                The script is save with the following options:
                            Type: Main
                            Category: --Trade Listing Load Data
Instructions:               This script must be configured on the Trader Manager as a Operation Service: 
							Service Type: Trading
							Post Script
							Execution: Post Process Service

*/
package com.costumer.OPS_ITAU_CMX_EXPORT;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.axis.AxisFault;

import cl.vmetrix.webservices.proxy.ProxyServiceImpl;
import cl.vmetrix.webservices.proxy.ProxyServiceImplPortBindingStub;
import cl.vmetrix.webservices.proxy.ProxyServiceImplService;
import cl.vmetrix.webservices.proxy.ProxyServiceImplServiceLocator;

import com.customer.JVS_INC_General_Itau.JVS_INC_General_Itau;
import com.olf.openjvs.DBUserTable;
import com.olf.openjvs.DBaseTable;
import com.olf.openjvs.IContainerContext;
import com.olf.openjvs.IScript;
import com.olf.openjvs.Math;
import com.olf.openjvs.OCalendar;
import com.olf.openjvs.OConsole;
import com.olf.openjvs.ODateTime;
import com.olf.openjvs.OException;
import com.olf.openjvs.Ref;
import com.olf.openjvs.Str;
import com.olf.openjvs.Table;
import com.olf.openjvs.Transaction;
import com.olf.openjvs.Util;
import com.olf.openjvs.enums.COL_TYPE_ENUM;
import com.olf.openjvs.enums.DATE_FORMAT;
import com.olf.openjvs.enums.DATE_LOCALE;
import com.olf.openjvs.enums.OLF_RETURN_CODE;
import com.olf.openjvs.enums.SEARCH_ENUM;
import com.olf.openjvs.enums.SHM_USR_TABLES_ENUM;
import com.olf.openjvs.enums.TOOLSET_ENUM;
import com.olf.openjvs.enums.TRAN_STATUS_ENUM;
import com.olf.openjvs.enums.TRAN_TYPE_ENUM;

public class OPS_Itau_Cmx_Export implements IScript
{

    private static final String FND_TO_CMX = "FndToCmx";
	
	private JVS_INC_General_Itau INCGeneralItau;
    public OPS_Itau_Cmx_Export()
    {
        INCGeneralItau = new JVS_INC_General_Itau();
        
        try
        {
            iUSD = Ref.getValue(SHM_USR_TABLES_ENUM.CURRENCY_TABLE, USD);
            iCLP = Ref.getValue(SHM_USR_TABLES_ENUM.CURRENCY_TABLE, CLP);
        }
        catch (OException e)
        {
        	INCGeneralItau.print_err(INCGeneralItau.USER_getScriptName() + " - " + e.getMessage());
        }
    }


    //*****************************************************************************
    //* Private Variables
    //*****************************************************************************

    //******************** USER CONFIGURABLE PARAMETERS ***************************
    private String sPlugInName          = "OPS_Itau_Cmx_Export";
    private String sCanalTran           = "Canal Transaccional";
    private String sMedioTran           = "Medio Transaccional";
    private String sUTCanalTran         = "USER_CanalTransaccional";
    private String sUTMedioTran         = "USER_MedioTransaccional";
//    private String sUTCountry           = "USER_Country";
    private String sConfVariable	= "USER_Configurable_Variables";
    private String sCostoFondoUSD       = JVS_INC_General_Itau.tipo_cambio_costo_str; //"Tipo Cambio USD/CLP Costo";
    private String sTCCierreUSD         = JVS_INC_General_Itau.tipo_cambio_cliente_str; //"Tipo Cambio USD/CLP Cliente";
    private String sParCostoFondoUSDME  = JVS_INC_General_Itau.paridad_usd_div_costo_str; //"Paridad USD/DIV o DIV/USD Costo";
    private String sParCierreUSDME      = JVS_INC_General_Itau.paridad_usd_div_cierre_str; //"Paridad USD/DIV o DIV/USD Cierre";
    private String sIndicadorFX         = "Indicador FX";
    private String sCMX                 = "CMX";
    private String sFND                 = "FND";
    private String sFINDUR              = "FINDUR";
    private String sRUT                 = "RUT"; /* LE RUT */
    private String sSequencia           = "Sequencia"; /* Sequencia BU */
    private String sTipoInst            = "Tipo Institucion Financiera";
    private String sHedge               = "Hedge";
    private String sEF             		= "EF"; /* Entrega Fisica */
    @SuppressWarnings("unused")
    private String sHorarioEspecial     = "Horario Especial";
    @SuppressWarnings("unused")
    private String sToday               = "";
    @SuppressWarnings("unused")
    private String sTomorrow            = "";
    @SuppressWarnings("unused")
    private String sYesterday           = "";
    @SuppressWarnings("unused")
    private String sTwoDaysBefore       = "";
//    private int    iNumeroOpFindur;
    private int    iToday;
    private final String USD = "USD";
    private final String CLP = "CLP";
    private int iUSD;
    private int iCLP;
    private String sMedioTranExport;
    private String sProxyService 		= "";

    //******************** USER CONFIGURABLE PARAMETERS ***************************

    @SuppressWarnings("unused")
	@Override
    public void execute(IContainerContext context) throws OException
    {


        /*----------------------------------------------*/
        /* Variables                                    */
        /*----------------------------------------------*/

        Table   tblFXDealsCancelled     = Util.NULL_TABLE,
                tblFXDealsCMX           = Util.NULL_TABLE,
                tblSettleInfo           = Util.NULL_TABLE,
                tblTranList				= Util.NULL_TABLE,
                tblDealVerHistory       = Util.NULL_TABLE,
                tblDealHistory          = Util.NULL_TABLE,
                tblCMXLog               = Util.NULL_TABLE,
                tblInsertUserData       = Util.NULL_TABLE,
                tblUpDateUserData       = Util.NULL_TABLE,
                tblIndicadorFX          = Util.NULL_TABLE,
                tblUserVariable         = Util.NULL_TABLE,
                tblListHolding          = Util.NULL_TABLE,
                tblError                = Util.NULL_TABLE,
                tblErrorDetail          = Util.NULL_TABLE,
                tblVariable		= Util.NULL_TABLE;


        int iRetVal, iRow,  iNumRows = 0, iNumRow, iYesterday, iTomorrow, iTwoDaysBefore;
        int iTranNum, iDealNum, iVersionNum, iCodError = 0, iExitFail = 0;


        String sQuery, 
        		sXMLDeal, 
        		sMessageError="", 
        		sMessageRet = "", 
        		sMsgRet = "";

        /*----------------------------------------------*/
        /* Starting Script LOG File                     */
        /*----------------------------------------------*/
        INCGeneralItau.mark_start_script(sPlugInName);

        /*----------------------------------------------*/
        /* Get Dates                                    */
        /*----------------------------------------------*/
        iToday = OCalendar.today();
        sToday = OCalendar.formatDateInt(iToday, DATE_FORMAT.DATE_FORMAT_ISO8601_EXTENDED, DATE_LOCALE.DATE_LOCALE_US);

        iYesterday = OCalendar.getLgbd(iToday);
        sYesterday = OCalendar.formatDateInt(iYesterday, DATE_FORMAT.DATE_FORMAT_ISO8601_EXTENDED, DATE_LOCALE.DATE_LOCALE_US);

        iTomorrow = OCalendar.getNgbd(iToday); // get the next good business day
        sTomorrow = OCalendar.formatDateInt(iTomorrow, DATE_FORMAT.DATE_FORMAT_ISO8601_EXTENDED, DATE_LOCALE.DATE_LOCALE_US);

        iTwoDaysBefore = OCalendar.getLgbd(iYesterday);
        sTwoDaysBefore = OCalendar.formatDateInt(iTwoDaysBefore, DATE_FORMAT.DATE_FORMAT_ISO8601_EXTENDED, DATE_LOCALE.DATE_LOCALE_US);

        /*----------------------------------------------*/
        /* Initialize Tables in memory                  */
        /*----------------------------------------------*/
		tblSettleInfo        = Table.tableNew("Settlement Information");
        tblTranList          = Table.tableNew("Transaction Number List");
        tblDealHistory       = Table.tableNew("Deal History");
        tblDealVerHistory    = Table.tableNew("Deal Version History");
        tblCMXLog            = Table.tableNew("USER Table CMX Log");
        
        /*----------------------------------------------*/
		/* Retrieve Arguments Table to obtain the tran_num			*/
		/*----------------------------------------------*/
        Transaction Tran = Util.NULL_TRAN;
		int iTranNum_;
		Table tAllDeals = Table.tableNew();
        
		Table tArgt = context.getArgumentsTable();
		//Tran = tArgt.getTable("Deal Info", 1).getTran("tran_ptr", 1);
		
		tAllDeals = tArgt.getTable("Deal Info", 1);		
		iTranNum_ = tAllDeals.getInt("tran_num", 1);
		
		Tran = Transaction.retrieve(iTranNum_);
		//OConsole.oprint("\nDeal: " + Tran.getTranNum());

        /*-------------------------------------------------------*/
        /* Create this Table for Update Log in the User Table    */
        /* Table should be use to Update User Table USER_CMX_LOG */
        /*-------------------------------------------------------*/
        tblInsertUserData = Table.tableNew("USER_CMX_LOG");
        tblInsertUserData.addCols("S(origen) I(deal_num) I(tran_num) I(id_num) I(version_num) I(cod_error) S(mensaje_error)");
        tblInsertUserData.addCol("fecha_hora", COL_TYPE_ENUM.COL_DATE_TIME);

        tblUpDateUserData = tblInsertUserData.cloneTable();

        
        /*----------------------------------------------*/
		/* Retrieve List of Configuration				*/
		/*----------------------------------------------*/
		tblVariable = Table.tableNew("User Configurable Variables");
		DBaseTable.execISql(tblVariable, "select variable, valor from " + sConfVariable + " Where sistema = '" + sFINDUR + "' And proceso = 'CMX_EXPORT' order by variable");

		iNumRows = tblVariable.getNumRows();
		if ( iNumRows < 1)
		{
			INCGeneralItau.print_err(INCGeneralItau.USER_getScriptName() + " Por favor revisar la tabla " + sConfVariable);
            Util.exitFail(INCGeneralItau.USER_getScriptName() + " Failed to reload FX Table without CMX.");
			}
		
		iRow = tblVariable.findString("variable", "medio_tran_export", SEARCH_ENUM.FIRST_IN_GROUP);
		sMedioTranExport = tblVariable.getString("valor", iRow);

		//Getting server and port of Web Service
		iRow = tblVariable.findString("variable", "proxy_Integ_fnd_cmx", SEARCH_ENUM.FIRST_IN_GROUP);
		sProxyService = tblVariable.getString("valor", iRow);
		
		INCGeneralItau.print_msg("INFO", "Server:port of Integration CMX WS: "+ sProxyService);
		
        //*****************************************************************************
        //* Retrieve List of FX Deals and defining the following fields:
        //* Fields 1, 3, 6, 7, 8, 30, 31
        //*****************************************************************************

        /*----------------------------------------------*/
        /*  Retrieve FX Deal                   */
        /*----------------------------------------------*/
        INCGeneralItau.print_msg("INFO", "Retrieve FX Deal Cancelled");
        tblFXDealsCancelled = Table.tableNew("Table FX Deals Cancelled");
        iRetVal = funcLoadFXDeal(tblFXDealsCancelled,Tran.getTranNum());
        if (iRetVal == OLF_RETURN_CODE.OLF_RETURN_SUCCEED.jvsValue())
            iNumRows = tblFXDealsCancelled.getNumRows();

        /*----------------------------------------------*/
        /*  Retrieve Deal not in CMX           */
        /*----------------------------------------------*/
        INCGeneralItau.print_msg("INFO","Retrieve  FX Deal Validated not in CMX");
        tblFXDealsCMX = Table.tableNew("Table FX Deals not in CMX");
        iRetVal = funcLoadFXDealNotCMX(tblFXDealsCMX, Tran.getTranNum());
        if (iRetVal == OLF_RETURN_CODE.OLF_RETURN_SUCCEED.jvsValue())
        {
            iNumRows = tblFXDealsCancelled.getNumRows();
            if (iNumRows > 0)
            {
                INCGeneralItau.print_msg("INFO", "Copy FX Deals list without logging in TranInfo Table and Deals FX that are not CMX.");
                /* Union List FX Deals without CMX and List FX Deals without Transaction Info Table */

                tblFXDealsCMX.select(tblFXDealsCancelled, "DISTINCT,*", "deal_num GT 0");
            }
        }
        else
        {
            INCGeneralItau.print_err(INCGeneralItau.USER_getScriptName() + " Failed to retrieve list of FX, which not went sent to CMX.");
            Util.exitFail(INCGeneralItau.USER_getScriptName() + " Failed to reload FX Table without CMX.");
        }
        /* Check FX Table is empty */
        if (tblFXDealsCMX.getNumRows() <= 0)
        {
            /* FX Table Deals is empty */
        	INCGeneralItau.print_msg("INFO", INCGeneralItau.USER_getScriptName() + " FX Deals was not found to export. ");
        }else{

	        //*****************************************************************************
	        //* Start process for Get/Set Fields
	        //*****************************************************************************
	
	        /*--------------------------------------------------------------*/
	        /*  Retrieve List of Operation Type                             */
	        /*  Fields 29 - Indicador Tipo Operaci�n                        */
	        /*  Delete Operation Type equal C - Deal FX already sent to CMX */
	        /*--------------------------------------------------------------*/
	        /* Retrieve List USER Table CMX Log */
	        funcLoadCMXLog(tblCMXLog);
	
	        /* Retrieve List Deal History */
	        funcLoadDealHistory(tblDealHistory);
	
	        /* Retrieve List Deal Version History */
	        funcLoadDealVerHistory(tblDealVerHistory);
	
	        /* Set Version on the Deal History Table */
	        tblDealHistory.select(tblDealVerHistory, "version", "tran_num EQ $tran_num");
	
	        /* Set Version on the Deal History Table */
	        tblFXDealsCMX.select(tblDealHistory, "version", "deal_tracking_num EQ $deal_num");
	
	        /* Operation Type Ingreso and Anulaci�n are defined in SQL Query */
	        /* Set Deal Operation Type to Modificada */
	        /* Transaction Status equal Validated and with different transaction number and version number */
	        tblFXDealsCMX.select(tblCMXLog, "Modificada(opfIndTipoOp)", "deal_num EQ $deal_num And tran_num NE $tran_num " +
	                "And tran_status NE " + TRAN_STATUS_ENUM.TRAN_STATUS_CANCELLED.jvsValue());
	
	        tblFXDealsCMX.select(tblCMXLog, "Modificada(opfIndTipoOp)", "deal_num EQ $deal_num And tran_num EQ $tran_num " +
	                "And version_num NE $version And tran_status NE " + TRAN_STATUS_ENUM.TRAN_STATUS_CANCELLED.jvsValue());
	
	        /* Set Deal Operation Type to Enviada - Deal already sent */
	        tblFXDealsCMX.select(tblCMXLog, "Enviada(opfIndTipoOp)", "deal_num EQ $deal_num And tran_num EQ $tran_num " +
	                "And version_num EQ $version And cod_error EQ " + 0);
	
	        /* Delete Operation Type equal 'E' - Deal already sent */
	        tblFXDealsCMX.deleteWhereString("opfIndTipoOp", "E");
	
	        /* Check FX Table is empty */
	        if (tblFXDealsCMX.getNumRows() <= 0)
	        {
	            /* FX Table Deals is empty */
	            INCGeneralItau.print_msg("INFO", INCGeneralItau.USER_getScriptName() + "FX Deals was not found to export.");	            
	        }else{
		        /*--------------------------------------------------------------*/
		        /*  Retrieve List of Instrument Number / Holding                */
		        /*  Field 2 (Char) - Par Monedas                                */
		        /*--------------------------------------------------------------*/
		        tblListHolding = Table.tableNew("List Holding / Instrument Number");
		        funcRetrieveListHolding(tblListHolding);
		        tblFXDealsCMX.select(tblListHolding, "reference(opfParMonedas)", "ins_num EQ $ins_num");
		
		        /*--------------------------------------------------------------*/
		        /*  Function for set Tran Info TranInfo for Canal Transaccional */
		        /*  Field 4 (Char)                                              */
		        /*--------------------------------------------------------------*/
		        funcLoadTranInfo(tblFXDealsCMX, "opfCanalTrx", sCanalTran);
		        /* Set the Canal Transaccional from the User Table sUTCanalTran */
		        funcLoadUserTable(tblFXDealsCMX, sUTCanalTran, "CMX", "CanalTransaccional", "opfCanalTrx");
		
		        /*--------------------------------------------------------------*/
		        /*  Function for set of TranInfo for Medio Transaccional        */
		        /*  Field 5 (Char)                                              */
		        /*--------------------------------------------------------------*/
		        funcLoadTranInfo(tblFXDealsCMX, "opfMedioTrx", sMedioTran);
		        /* Set the Medio Transaccional from the User Table sUTMedioTran */
		        funcLoadUserTable(tblFXDealsCMX, sUTMedioTran, "CMX", "MedioTransaccional", "opfMedioTrx");
		
		        /*--------------------------------------------------------------*/
		        /*  Function for set Party Info for RUT of the Client-By Lentity*/
		        /*  Field 9 (Char)                                              */
		        /*--------------------------------------------------------------*/
		        funcLoadPartyInfo(tblFXDealsCMX, "replace(value, '-', '') as value", "external_lentity", "opfRutCliente", sRUT);
		
		        /*--------------------------------------------------------------*/
		        /*  Function for set Party Info Sequencia - By Bunit            */
		        /*  Field 10 (Numeric)                                          */
		        /*--------------------------------------------------------------*/
		        funcLoadPartyInfo(tblFXDealsCMX, "value", "external_bunit", "opfSecuenciaCliente", sSequencia);
		
		        /*--------------------------------------------------------------*/
		        /*  Function for set Party Info Tipo Instituci�n - By Lentity   */
		        /*  Field 11 (Char)                                             */
		        /*--------------------------------------------------------------*/
		        funcLoadTipoInst(tblFXDealsCMX, "value", "external_lentity", "opfTipoCliente", sTipoInst);
		
		        /*--------------------------------------------------------------*/
		        /*  Function for set Tran Info Tipo Cambio USD/CLP Cliente      */
		        /*  Field 19 (Char)                                             */
		        /*--------------------------------------------------------------*/
		        funcLoadTranInfo(tblFXDealsCMX, "opfTCCierreUSD", sTCCierreUSD);
		
		        /*--------------------------------------------------------------*/
		        /*  Retrieve List of Tran Info Paridad USD/DIV o DIV/USD Cierre */
		        /*  Field 20 (Char)                                               */
		        /*--------------------------------------------------------------*/
		        funcLoadTranInfo(tblFXDealsCMX, "opfParCierreUSDME", sParCierreUSDME);
		
		        /*--------------------------------------------------------------*/
		        /*  Retrieve List of Tran Info Tipo Cambio USD/CLP Costo        */
		        /*  Field 21 (Char)                                             */
		        /*--------------------------------------------------------------*/
		        funcLoadTranInfo(tblFXDealsCMX, "opfCostoFondoUSD", sCostoFondoUSD);
		
		        /*--------------------------------------------------------------*/
		        /*  Retrieve List of Tran Info Paridad USD/DIV o DIV/USD Costo  */
		        /*  Field 22 (Char)                                             */
		        /*--------------------------------------------------------------*/
		        funcLoadTranInfo(tblFXDealsCMX, "opfParCostoFondoUSDME", sParCostoFondoUSDME);
		
		        /*--------------------------------------------------------------*/
		        /*  Retrieve value to Country / Pais Pago                       */
		        /*  Field 31 (Char)                                             */
		        /*--------------------------------------------------------------*/
		        //funcLoadCountry(tblFXDealsCMX, "opfPaisPago");
		        /* Set the Country ID from the User Table sUTCountry */
		        //funcLoadUserTable(tblFXDealsCMX, sUTCountry, "id_BCCH", "name", "opfPaisPago");
		        /* opfPaisPago fixed code 997 */
		
		        /*--------------------------------------------------------------*/
		        /*  Retrieve List of TranInfo for FX Indicator                  */
		        /*  Field 32 (Char) - Indicador FX                              */
		        /*--------------------------------------------------------------*/
		        tblIndicadorFX = Table.tableNew("Tran Info - Indicador FX");
		        sQuery = "Select tran_num, type_id, type_name, value, " +
		                 "CASE WHEN value = '" + sEF + "' THEN 'EF' " +
		                 "WHEN value = '" + sHedge + "' THEN 'FX' " +
		                 "END as 'valueFX' " +
		                 "FROM ab_tran_info_view WHERE type_name = '" + sIndicadorFX + "' Order By tran_num";
		        INCGeneralItau.print_msg("DEBUG", "(Indicador FX)\n" + sQuery);
		        iRetVal = DBaseTable.execISql(tblIndicadorFX, sQuery);
		        if ( iRetVal == OLF_RETURN_CODE.OLF_RETURN_SUCCEED.jvsValue())
		        {
		            /* Get/Set values for Indicador FX  Field 32 (Char)*/
		            tblFXDealsCMX.select(tblIndicadorFX, "valueFX(opfIndicadorFX)", "tran_num EQ $tran_num");
		
		        }else{
		            INCGeneralItau.print_msg("INFO", "Indicador FX - Table result with FX Indicator is empty.");
		        }
		
		        /*--------------------------------------------------------------*/
		        /*  Retrieve List of Payment Method and Currency B/S            */
		        /*  Fields 13, 14, 15, 16, 25, 26, 27, 28,                      */
		        /*--------------------------------------------------------------*/
		        tblTranList.select(tblFXDealsCMX, "tran_num", "tran_num GT 0");
		        tblSettleInfo = INCGeneralItau.getSettleInfoWithValutaFX(tblTranList);

		        
                /*----------------------------------------------*/
                /* Set Currency Names                           */
                /*----------------------------------------------*/
		        int ccy_id;
		        String ccy_name;
		        tblSettleInfo.addCol("currency_compra_str", COL_TYPE_ENUM.COL_STRING);
		        tblSettleInfo.addCol("currency_venta_str", COL_TYPE_ENUM.COL_STRING);
                for(int i = 1; i <= tblSettleInfo.getNumRows(); i++)
                {
                    // Currency Compra
                    ccy_id = tblSettleInfo.getInt("currency_compra", i);
                    ccy_name = Ref.getName(SHM_USR_TABLES_ENUM.CURRENCY_TABLE, ccy_id);
                    tblSettleInfo.setString("currency_compra_str", i, ccy_name);
                    
                    // Currency Venta
                    ccy_id = tblSettleInfo.getInt("currency_venta", i);
                    ccy_name = Ref.getName(SHM_USR_TABLES_ENUM.CURRENCY_TABLE, ccy_id);
                    tblSettleInfo.setString("currency_venta_str", i, ccy_name);
                }
		        
		        
                /*----------------------------------------------*/
                /* Set Settlement Info                          */
                /*----------------------------------------------*/
		        tblFXDealsCMX.select(tblSettleInfo,"pmt_bic(opfFPagoCompra), stl_compra(opfValutaCompra), pmt_cli(opfFPagoVenta), stl_venta(opfValutaVenta)," +
		        				"currency_compra_str(opfMonedaCompra), currency_venta_str(opfMonedaVenta), amount_compra(opfMontoCompra), amount_venta(opfMontoVenta)", 
		        				"tran_num EQ $tran_num"); 
		        
		        		        		
		        /*--------------------------------------------------------------*/
		        /*  Format the All numbers of decimals                          */
		        /*                                                              */
		        /*--------------------------------------------------------------*/
		        funcFormatRateDigits(tblFXDealsCMX);
		        
		        /*----------------------------------------------------*/
		        /* Function to Formatting Table to XML and Tags names */
		        /*----------------------------------------------------*/
		        funcTableFormatting(tblFXDealsCMX);
		
		        /*----------------------------------------------------*/
		        /* Function to Get Deal and export XML                */
		        /* with Tags names                                    */
		        /*----------------------------------------------------*/
		        iNumRows = tblFXDealsCMX.getNumRows();
		        INCGeneralItau.print_msg("DEBUG", "Number of Operations to sent: " + iNumRows);
		        if (iNumRows > 0 )
		        {
		        	tblFXDealsCMX.convertColToString(tblFXDealsCMX.getColNum("opfValutaCompra"));
		        	tblFXDealsCMX.convertColToString(tblFXDealsCMX.getColNum("opfValutaVenta"));
		        	 for (iRow = 1; iRow <= iNumRows; iRow++)
			            {
			                iDealNum = tblFXDealsCMX.getInt("deal_num", iRow);
			                iTranNum = tblFXDealsCMX.getInt("tran_num", iRow);
			                iVersionNum = tblFXDealsCMX.getInt("version", iRow);
			                Table   tblFXXML     = Table.tableNew("campos");
			                
			                tblFXXML.select(tblFXDealsCMX, "opfOrigen(origen),opfParMonedas,opfTipoProducto,opfCanalTrx," +
			                		"opfMedioTrx,opfUsuario,opfNumeroOpFindur,opfCodSucOrigen,opfRutCliente,opfSecuenciaCliente," +
			                		"opfTipoCliente,opfNombreCliente,opfMonedaCompra,opfMonedaVenta,opfMontoCompra,opfMontoVenta," +
			                		"opfEquivMontoCompraCLP,opfEquivMontoVentaCLP,opfTCCierreUSD,opfParCierreUSDME,opfCostoFondoUSD," +
			                		"opfParCostoFondoUSDME,opfPrecioCliente,opfPrecioCosto,opfFPagoCompra,opfFPagoVenta,opfValutaCompra," +
			                		"opfValutaVenta,opfIndTipoOp,opfNumPasaporte,opfPaisPago,opfIndicadorFX", "deal_num EQ "+iDealNum+
			                		" And tran_num EQ "+iTranNum+" And version EQ "+iVersionNum);
			               
			                //Sending XML to WS.
			                String xmlstring=tblFXXML.tableToXMLString(0, 0, "", "", 0, 1, 0, 0, 0);
				        	
				        	OConsole.oprint(xmlstring);
				        	//Begin sent operations to WS
				        	boolean result=false;
				        	try {
								ProxyServiceImplService w = new ProxyServiceImplServiceLocator();
								ProxyServiceImpl ws = new ProxyServiceImplPortBindingStub(new URL(w.getProxyServiceImplPortAddress(sProxyService)),w);
								sMessageError="";
								result = ws.processMessage(FND_TO_CMX, 2,funcFormatXML(xmlstring));
								
								if (result){
									sMessageError="XML sent successfully";
									INCGeneralItau.print_msg("INFO", "This operation was sent successfully: [DealNum=" + iDealNum + " TranNum=" + iTranNum + " Version="+ iVersionNum +"]" );
								}
								else{
									sMessageError="XML could not be sent";
									iCodError =-1;
									INCGeneralItau.print_msg("INFO", "Could not be sent this operation: [DealNum=" + iDealNum + " TranNum=" + iTranNum + " Version="+ iVersionNum +"]" );
								}
								
				        	} catch (AxisFault e) {
				        		INCGeneralItau.print_err(INCGeneralItau.USER_getScriptName() + " AxisFault. Failed to sent XML to CMX. "+ e);
				        		sMessageError = "AxisFault. Failed to sent XML to CMX.";
				        		iCodError =-1;
							} catch (MalformedURLException e) {
								INCGeneralItau.print_err(INCGeneralItau.USER_getScriptName() + " MalformedURLException. Failed to sent XML to CMX. "+ e);
								sMessageError = "MalformedURLException. Failed to sent XML to CMX.";
								iCodError =-1;
							} catch (RemoteException e) {
								INCGeneralItau.print_err(INCGeneralItau.USER_getScriptName() + " RemoteException. Failed to sent XML to CMX. "+ e);
								sMessageError = "RemoteException. Failed to sent XML to CMX.";
								iCodError =-1;
							}finally{
								Tran.destroy();
							}
							
				        	 /*-------------------------------------------------------*/
			                /* Check if already exist in the User Data Table          */
			                /*-------------------------------------------------------*/
			                if (funcCheckUserData(iDealNum) > 0)
			                {
			                	/*-------------------------------------------------------*/
			                    /* Set values for UpDate User Data Table                  */
			                    /*-------------------------------------------------------*/
			                    iNumRow = tblUpDateUserData.addRow();
			                    tblUpDateUserData.setString("origen", iNumRow, sFND);
			                    tblUpDateUserData.setInt("deal_num", iNumRow, iDealNum);
			                    tblUpDateUserData.setInt("tran_num", iNumRow, iTranNum);
			                    tblUpDateUserData.setInt("id_num", iNumRow, 0);
			                    tblUpDateUserData.setInt("version_num", iNumRow, iVersionNum);
			                    tblUpDateUserData.setInt("cod_error", iNumRow, iCodError);
			                    tblUpDateUserData.setString("mensaje_error", iNumRow, sMessageError);
			                    tblUpDateUserData.setDateTimeByParts("fecha_hora", iNumRow,OCalendar.getServerDate(), Util.timeGetServerTime());
			
			                }else{
			                	/*-------------------------------------------------------*/
			                    /* Set values for Insert User Data Table                  */
			                    /*-------------------------------------------------------*/
			                    iNumRow = tblInsertUserData.addRow();
			                    tblInsertUserData.setString("origen", iNumRow, sFND);
			                    tblInsertUserData.setInt("deal_num", iNumRow, iDealNum);
			                    tblInsertUserData.setInt("tran_num", iNumRow, iTranNum);
			                    tblInsertUserData.setInt("id_num", iNumRow, 0);
			                    tblInsertUserData.setInt("version_num", iNumRow, iVersionNum);
			                    tblInsertUserData.setInt("cod_error", iNumRow, iCodError);
			                    tblInsertUserData.setString("mensaje_error", iNumRow, sMessageError);
			                    tblInsertUserData.setDateTimeByParts("fecha_hora", iNumRow,
			                    OCalendar.getServerDate(), Util.timeGetServerTime());
			                }

				        	//End
			                
			                if(tblFXXML != null && Table.isTableValid(tblFXXML) == 1) tblFXXML.destroy();
			                
			            }
		        	 	/*-------------------------------------------------------*/
			            /* Insert Database User Table Log CMX                      */
			            /*-------------------------------------------------------*/
			            if (tblInsertUserData.getNumRows() > 0)
			            {
			                iRetVal = DBUserTable.insert(tblInsertUserData);
			                if( iRetVal != OLF_RETURN_CODE.OLF_RETURN_SUCCEED.jvsValue())
			                    INCGeneralItau.print_err(INCGeneralItau.USER_getScriptName() +
			                            DBUserTable.dbRetrieveErrorInfo(iRetVal, "DBUserTable.insert() failed" ));
			            }
			            /*-------------------------------------------------------*/
			            /* UpDate Database User Table Log CMX                        */
			            /*-------------------------------------------------------*/
			            // Add grouping
			            tblUpDateUserData.group("deal_num");
			            if (tblUpDateUserData.getNumRows() > 0)
			            {
	                        iRetVal = DBUserTable.update(tblUpDateUserData);
			                if( iRetVal != OLF_RETURN_CODE.OLF_RETURN_SUCCEED.jvsValue())
			                    INCGeneralItau.print_err(INCGeneralItau.USER_getScriptName() +
			                            DBUserTable.dbRetrieveErrorInfo(iRetVal, "DBUserTable.update() failed" ));
			            }

		        }
		        else
		        {
		            INCGeneralItau.print_msg("INFO", INCGeneralItau.USER_getScriptName() + " Failed export Table to XML, Fx Deals Table is empty!!!");
		        }
	        }
        }
        /*----------------------------------------------*/
        /* Memory Clean Up                               */
        /*----------------------------------------------*/
        if(tblFXDealsCancelled != null && Table.isTableValid(tblFXDealsCancelled) == 1) tblFXDealsCancelled.destroy();
        if(tblFXDealsCMX != null && Table.isTableValid(tblFXDealsCMX) == 1) tblFXDealsCMX.destroy();
        if(tblIndicadorFX != null && Table.isTableValid(tblIndicadorFX) == 1) tblIndicadorFX.destroy();
        if(tblSettleInfo != null && Table.isTableValid(tblSettleInfo) == 1) tblSettleInfo.destroy();
        if(tblTranList != null && Table.isTableValid(tblTranList) == 1) tblTranList.destroy();
        if(tblDealVerHistory != null && Table.isTableValid(tblDealVerHistory) == 1) tblDealVerHistory.destroy();
        if(tblDealHistory != null && Table.isTableValid(tblDealHistory) == 1) tblDealHistory.destroy();
        if(tblCMXLog != null && Table.isTableValid(tblCMXLog) == 1) tblCMXLog.destroy();
        if(tblInsertUserData != null && Table.isTableValid(tblInsertUserData) == 1) tblInsertUserData.destroy();
        if(tblUpDateUserData != null && Table.isTableValid(tblUpDateUserData) == 1) tblUpDateUserData.destroy();
        if(tblUserVariable != null && Table.isTableValid(tblUserVariable) == 1) tblUserVariable.destroy();
        if(tblListHolding != null && Table.isTableValid(tblListHolding) == 1) tblListHolding.destroy();
        if(tblError != null && Table.isTableValid(tblError) == 1) tblError.destroy();
        if(tblErrorDetail != null && Table.isTableValid(tblErrorDetail) == 1) tblErrorDetail.destroy();

        INCGeneralItau.mark_end_script();

        if (iExitFail == 1)
            Util.exitFail();
    }

    /******************************* END MAIN ***********************************/
    
    /*****************************************************************************
    Name:           funcFormatXML
    Description:    Format the XML to send to the WS. Adds the <b>root</b> tag and the <b>origen</b> tag

     * @param  String Xml with the initial xml String.
     * @return String sValue with the XML complete and ready to send to the WS.
    *****************************************************************************/
    
    String funcFormatXML(String Xml){
    	StringBuffer sb = new StringBuffer();
    	String temp = Xml.substring(21);//("<?xml version=\"1.0\"?>", "");
    	Date hoy = new Date();
    	SimpleDateFormat sdf = new SimpleDateFormat("dd/M/yyyy");
    	DateFormat hourFormat = new SimpleDateFormat("HH:mm:ss a");
    	
    	String fecha = sdf.format(hoy);
    	String hour =hourFormat.format(hoy);
    	sb.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
    	sb.append("<x:root xmlns:x=\"urn:RegistroFindur\">");
    	sb.append(temp);
    	sb.append("<origen fecha=\""+fecha+"\" hora=\""+hour+"\" sistema=\""+sFND+"\"/>");
    	sb.append("</x:root>");
    	
    	
    	return sb.toString();
    }
    
    
    
    
    /*****************************************************************************
    Name:           funcGetValueFromUser
    Description:    Get value from User Table - USER_Configure_Variable

     * @param  Table tblVariable
     * @param  String sVariable
     * @param  String sColunm
     * @return String sValue
     * @throws OException
    *****************************************************************************/
  /*  String funcGetValueFromUser(Table tblVariable, String sColunm, String sVariable) throws OException
    {
        String sValue = "";
        int iRow = 0;
        tblVariable.sortCol(sColunm);
        iRow = tblVariable.findString(sColunm, sVariable, SEARCH_ENUM.FIRST_IN_GROUP);

        if (iRow > 0)
            sValue = tblVariable.getString("valor", iRow);

        return sValue;
    }*/
    /*****************************************************************************
    Name:           funcCheckUserData
    Description:    Check if this Deal number already exist in the User Data Table

     * @param     Integer iDealNum
     * @return     Integer iTranNum
     * @throws     OException
    *****************************************************************************/
    int funcCheckUserData(int iDealNum) throws OException
    {
        Table tblUserData = Util.NULL_TABLE;
        String sQuery;
        int iRetVal, iTranNum = 0;

        tblUserData = Table.tableNew();

        sQuery = "Select deal_num, tran_num From USER_CMX_LOG Where deal_num =" + iDealNum + " And origen = '" + sFND + "'";
//                "and origem = '" + sFND + "'";

        INCGeneralItau.print_msg("DEBUG", "(funcCheckUserData)\n" + sQuery);

        iRetVal = DBaseTable.execISql(tblUserData, sQuery);
        if (iRetVal == OLF_RETURN_CODE.OLF_RETURN_SUCCEED.jvsValue())
        {
//             Get value to TranNum
            iTranNum = tblUserData.getInt("tran_num", 1);
        }
//         Memory Clean Up 
        tblUserData.destroy();

        return iTranNum;

    }


    /*****************************************************************************
    Name:           funcLoadTipoInst
    Description:    Get values From Table ab_tran_info_view and USER_Tipo_Institucion_financeira
                        to FX Deals Table

     * @param     Table tblFXDeals - Table with FX Deals
     * @param    String sTblFieldValue - Table field (value) must have a different format
     * @param    String sFieldWhere - Field should be use clause Where.
     * @param    String sField - Is a Field in the FX Deal Table
     * @param     String sParameter
     * @throws     OException
    *****************************************************************************/
    void funcLoadTipoInst(Table tblFXDeals, String sTblFieldValue, String sFieldWhere, String sField, String sParameter) throws OException
    //funcLoadTipoInst(tblFXDealsCMX, "value", "external_lentity", "opfTipoCliente", "Tipo Instituci�n Financiera");
    {
    	
    	INCGeneralItau.print_msg("INFO", "**** funcLoadTipoInst");
    	
        Table tblPartyInfo = Util.NULL_TABLE;
        String sQuery, sWhat, sWhere;
        int iRetVal;

        /* Initialize Table */
        tblPartyInfo = Table.tableNew("Party Info - " + sParameter);

        sQuery = "Select paiv.party_id, paiv.type_name, paiv." + sTblFieldValue + " , paiv.type_id, paiv.int_ext, utif.id  " +
                    "From party_info_view paiv " +
                    "Inner Join USER_Tipo_Institucion_financiera utif on paiv.value = utif.tipo_empresa " +
                    "Where paiv.type_name = '" + sParameter + "' Order By paiv.party_id";

        INCGeneralItau.print_msg("DEBUG", "(" + sParameter + ")\n" + sQuery);

        iRetVal = DBaseTable.execISql(tblPartyInfo, sQuery);
        if (iRetVal == OLF_RETURN_CODE.OLF_RETURN_SUCCEED.jvsValue())
        {
            /* Get/Set values from Transaction Info to Deal FX Table*/
            sWhat = "id(" + sField + ")";
            sWhere = "party_id EQ $" + sFieldWhere;
            tblFXDeals.select(tblPartyInfo, sWhat, sWhere);
        }else{
            INCGeneralItau.print_msg("INFO", " Party Info - Table result with " + sParameter + " is empty.");
        }
        /* Memory Clean Up */
        tblPartyInfo.destroy();
    }


    /*****************************************************************************
    Name:           funcLoadPartyInfo
    Description:    Get/Set values From Table ab_tran_info_view to FX Deals Table

     * @param     Table tblFXDeals - Table with FX Deals
     * @param    String sTblFieldValue - Table field (value) must have a different format
     * @param    String sFieldWhere - Field should be use clause Where.
     * @param    String sField - Is a Field in the FX Deal Table
     * @param     String sParameter
     * @throws     OException
    *****************************************************************************/
    void funcLoadPartyInfo(Table tblFXDeals, String sTblFieldValue, String sFieldWhere, String sField, String sParameter) throws OException
    {
    	
    	INCGeneralItau.print_msg("INFO", "**** funcLoadPartyInfo");
    	
        Table tblPartyInfo = Util.NULL_TABLE;
        String sQuery, sWhat, sWhere;
        int iRetVal;

        /* Initialize Table */
        tblPartyInfo = Table.tableNew("Party Info - " + sParameter);

        sQuery = "Select party_id, type_name, " + sTblFieldValue + ", type_id, int_ext " +
        "From party_info_view Where type_name = '" + sParameter + "' Order By party_id";

        iRetVal = DBaseTable.execISql(tblPartyInfo, sQuery);
        if (iRetVal == OLF_RETURN_CODE.OLF_RETURN_SUCCEED.jvsValue())
        {
            /* Get/Set values from Transaction Info to Deal FX Table*/
            sWhat = "value(" + sField + ")";
            sWhere = "party_id EQ $" + sFieldWhere;
            tblFXDeals.select(tblPartyInfo, sWhat, sWhere);
        }else{
            INCGeneralItau.print_msg("INFO", " Party Info - Table result with " + sParameter + " is empty.");
        }
        /* Memory Clean Up */
        tblPartyInfo.destroy();
    }
    /*****************************************************************************
    Name:           funcLoadTranInfo
    Description:    Get/Set values From Table ab_tran_info_view to FX Deals Table

     * @param    Table tblFXDeals - Table with FX Deals
     * @param    String sField - Is a Field in the FX Deal Table
     * @param    String sParameter
     * @throws   OException
    *****************************************************************************/
    void funcLoadTranInfo(Table tblFXDeals, String sField, String sParameter) throws OException
    {
    	INCGeneralItau.print_msg("INFO", "**** funcLoadTranInfo");
    	
        Table tblTranInfo = Util.NULL_TABLE;
        String sQuery, sWhat;
        int iRetVal;

        /* Initialize Table */
        tblTranInfo = Table.tableNew("Tran Info - " + sParameter);

        sQuery = "Select tran_num, type_id, type_name, value " +
        "FROM ab_tran_info_view WHERE type_name = '" + sParameter + "' Order By tran_num";

        INCGeneralItau.print_msg("DEBUG", "(" + sParameter + ")\n" + sQuery);

        iRetVal = DBaseTable.execISql(tblTranInfo, sQuery);
        if (iRetVal == OLF_RETURN_CODE.OLF_RETURN_SUCCEED.jvsValue())
        {
        	/* Get/Set values from Transaction Info to Deal FX Table*/
            sWhat = "value(" + sField + ")";       
            tblFXDeals.select(tblTranInfo, sWhat, "tran_num EQ $tran_num");
        }else{
            INCGeneralItau.print_msg("INFO", " Tran Info - Table result with " + sParameter + " is empty.");
        }
        /* Memory Clean Up */
        tblTranInfo.destroy();
    }
    /*****************************************************************************
    Name:           funcLoadUserTable
    Description:    Get/Set values From Table ab_tran_info_view to FX Deals Table

     * @param     Table tblFXDeals - Table with FX Deals
     * @param     String sTable - UserTable Name
     * @param     String sFieldTo - Is a Field to select in the User Table
     * @param     String sFieldFrom - Is a Field to select in the User Table
     * @param     String sDestField - Destine field on the FX Deals Table
     * @throws    OException
    *****************************************************************************/
    void funcLoadUserTable(Table tblFXDeals, String sTable, String sFieldTo, String sFieldFrom, String sDestField) throws OException
    {
    	INCGeneralItau.print_msg("INFO", "**** funcLoadUserTable");
    	
        Table tblUserTable = Util.NULL_TABLE;
        String sQuery, sWhat, sWhere;
        int iRetVal;

        /* Initialize Table */
        tblUserTable = Table.tableNew("User Table - " + sTable);

        sQuery = "Select " + sFieldTo + ", " + sFieldFrom + " FROM " + sTable;

        INCGeneralItau.print_msg("DEBUG", "(" + sQuery + ")\n" + sQuery);

        iRetVal = DBaseTable.execISql(tblUserTable, sQuery);
        if (iRetVal == OLF_RETURN_CODE.OLF_RETURN_SUCCEED.jvsValue())
        {
            /* Get/Set values from Transaction Info to Deal FX Table*/
            sWhat = sFieldTo + "(" + sDestField + ")";
            sWhere = sFieldFrom + " EQ $" + sDestField;
            tblFXDeals.select(tblUserTable, sWhat, sWhere);
        }else{
            INCGeneralItau.print_msg("INFO", " User Table - Table result with " + sTable + " is empty.");
        }
        /* Memory Clean Up */
        tblUserTable.destroy();
    }
    /*****************************************************************************
    Name:           funcLoadCountry
    Description:    Get/Set values From Table Country

     * @param     Table tblFXDeals - Table with FX Deals
     * @param    String sField - Is a Field in the FX Deal Table
     * @throws     OException
    *****************************************************************************/
    void funcLoadCountry(Table tblFXDeals, String sField) throws OException
    {
    	INCGeneralItau.print_msg("INFO", "**** funcLoadCountry");
    	
        Table tblCountry = Util.NULL_TABLE;
        String sQuery, sWhat;
        int iRetVal;

        /* Initialize Table */
        tblCountry = Table.tableNew("Country");

        sQuery = "Select name, id_number FROM country";

        INCGeneralItau.print_msg("DEBUG", "(" + sQuery + ")\n" + sQuery);

        iRetVal = DBaseTable.execISql(tblCountry, sQuery);
        if (iRetVal == OLF_RETURN_CODE.OLF_RETURN_SUCCEED.jvsValue())
        {
            /* Get/Set values from Transaction Info to Deal FX Table*/
            sWhat = "name(" + sField + ")";
            tblFXDeals.select(tblCountry, sWhat, "id_number EQ $countryId");
        }else{
            INCGeneralItau.print_msg("INFO", "Table result with Country is empty.");
        }
        /* Memory Clean Up */
        tblCountry.destroy();
    }
    /*****************************************************************************
    Name:           funcGetDealToXML
    Description:    Retrieve List Deal Version History

     * @param     Table tblFXDeals - Table with FX Deals
     * @param    Integer iRow - Line/Row number of the Deal
     * @return     String sXMLDeal
     * @throws     OException
    *****************************************************************************/
    String funcGetDealToXML(Table tblFXDeals, int iRow) throws OException
    {
    	INCGeneralItau.print_msg("INFO", "**** funcGetDealToXML");

        /*----------------------------------------------*/
        /* Variables                                    */
        /*----------------------------------------------*/
        String     sopfOrigen, sopfParMonedas, sopfTipoProducto, sopfCanalTrx, sopfMedioTrx, sopfUsuario,
        sopfNumeroOpFindur, sopfCodSucOrigen, sopfRutCliente, sopfSecuenciaCliente, sopfTipoCliente,
        sopfNombreCliente, sopfMonedaCompra, sopfMonedaVenta, sopfMontoCompra, sopfMontoVenta,
        sopfEquivMontoCompraCLP, sopfEquivMontoVentaCLP, sopfTCCierreUSD, sopfParCierreUSDME, sopfCostoFondoUSD,
        sopfParCostoFondoUSDME, sopfPrecioCliente, sopfPrecioCosto, sopfFPagoCompra, sopfFPagoVenta,
        sopfValutaCompra, sopfValutaVenta, sopfIndTipoOp, sopfNumPasaporte, sopfPaisPago, sopfIndicadorFX;

        String sXMLDeal = null, sFecha, sHora;

        int iRateDigits, iTCRateDigits, iParRateDigits, iCcy1Digits, iCcy2Digits, iCcyBuyDigits, iCcySellDigits; 
        
        double  dMontoCompra, dMontoVenta, dPrecioCliente, dPrecioCosto, 
        		dTCCierreUSD, dParCierreUSDME, dCostoFondoUSD, dParCostoFondoUSDME;

        /*----------------------------------------------*/
        /* Get Value from Table                         */
        /*----------------------------------------------*/
        
        tblFXDeals.convertColToString(tblFXDeals.getColNum("opfValutaCompra"));
        tblFXDeals.convertColToString(tblFXDeals.getColNum("opfValutaVenta"));
        
        sopfOrigen				= tblFXDeals.getString("opfOrigen", iRow);
        sopfParMonedas          = tblFXDeals.getString("opfParMonedas", iRow);
        sopfTipoProducto        = tblFXDeals.getString("opfTipoProducto", iRow);
        sopfCanalTrx            = tblFXDeals.getString("opfCanalTrx", iRow);
        sopfMedioTrx            = tblFXDeals.getString("opfMedioTrx", iRow);
        sopfUsuario             = tblFXDeals.getString("opfUsuario", iRow);
        sopfNumeroOpFindur      = Str.intToStr(tblFXDeals.getInt("opfNumeroOpFindur", iRow));
        sopfCodSucOrigen        = Str.intToStr(tblFXDeals.getInt("opfCodSucOrigen", iRow));
        sopfRutCliente          = tblFXDeals.getString("opfRutCliente", iRow);
        sopfSecuenciaCliente    = tblFXDeals.getString("opfSecuenciaCliente", iRow);
        sopfTipoCliente         = Str.intToStr(tblFXDeals.getInt("opfTipoCliente", iRow));
        sopfNombreCliente       = tblFXDeals.getString("opfNombreCliente", iRow);
        sopfNombreCliente       = sopfNombreCliente.replace(",", " -");
        sopfMonedaCompra        = tblFXDeals.getString("opfMonedaCompra", iRow);
        sopfMonedaVenta         = tblFXDeals.getString("opfMonedaVenta", iRow);
        
        /* Get Digits */
        iRateDigits = tblFXDeals.getInt("rate_digits", iRow);
        iTCRateDigits = tblFXDeals.getInt("tc_rate_digits", iRow);
        iParRateDigits = tblFXDeals.getInt("par_rate_digits", iRow);
        iCcy1Digits = tblFXDeals.getInt("ccy1_digits", iRow);
        iCcy2Digits = tblFXDeals.getInt("ccy2_digits", iRow);

        if(sopfTipoProducto.equalsIgnoreCase("C"))
        {
            iCcyBuyDigits = iCcy1Digits;
            iCcySellDigits = iCcy2Digits;
        }
        else
        {
            iCcyBuyDigits = iCcy2Digits;
            iCcySellDigits = iCcy1Digits;
        }
        
        
        /*  Rounding numerical operations  */
        dMontoCompra 			= funcRoundingNumericalOp(tblFXDeals.getDouble("opfMontoCompra",iRow), iCcyBuyDigits);
        dMontoVenta 			= funcRoundingNumericalOp(tblFXDeals.getDouble("opfMontoVenta",iRow), iCcySellDigits);
        dPrecioCliente 			= funcRoundingNumericalOp(tblFXDeals.getDouble("opfPrecioCliente",iRow), iRateDigits);
        dPrecioCosto   			= funcRoundingNumericalOp(tblFXDeals.getDouble("opfPrecioCosto",iRow), iRateDigits);   
        
		dTCCierreUSD 			= funcRoundingNumericalOp(funcConvertStrToDbl(tblFXDeals.getString("opfTCCierreUSD", iRow)), iTCRateDigits);
		dParCierreUSDME 		= funcRoundingNumericalOp(funcConvertStrToDbl(tblFXDeals.getString("opfParCierreUSDME", iRow)), iParRateDigits);
		dCostoFondoUSD 			= funcRoundingNumericalOp(funcConvertStrToDbl(tblFXDeals.getString("opfCostoFondoUSD", iRow)), iTCRateDigits);
		dParCostoFondoUSDME 	= funcRoundingNumericalOp(funcConvertStrToDbl(tblFXDeals.getString("opfParCostoFondoUSDME", iRow)), iParRateDigits);
		
		/* Convert Double to String to Xml */ 
        sopfMontoCompra         = Str.doubleToStr(dMontoCompra, iCcyBuyDigits);
        sopfMontoVenta          = Str.doubleToStr(dMontoVenta, iCcySellDigits);
        sopfPrecioCliente       = Str.doubleToStr(dPrecioCliente, iRateDigits);
        sopfPrecioCosto         = Str.doubleToStr(dPrecioCosto, iRateDigits);
        
        sopfTCCierreUSD         = Str.doubleToStr(dTCCierreUSD, iTCRateDigits);
        sopfParCierreUSDME      = Str.doubleToStr(dParCierreUSDME, iParRateDigits);
        sopfCostoFondoUSD       = Str.doubleToStr(dCostoFondoUSD, iTCRateDigits);
        sopfParCostoFondoUSDME  = Str.doubleToStr(dParCostoFondoUSDME, iParRateDigits);
        
        sopfEquivMontoCompraCLP = Str.intToStr(tblFXDeals.getInt("opfEquivMontoCompraCLP", iRow));
        sopfEquivMontoVentaCLP  = Str.intToStr(tblFXDeals.getInt("opfEquivMontoVentaCLP", iRow));
        
        sopfFPagoCompra         = tblFXDeals.getString("opfFPagoCompra", iRow);
        sopfFPagoVenta          = tblFXDeals.getString("opfFPagoVenta", iRow);
        sopfValutaCompra        = tblFXDeals.getString("opfValutaCompra", iRow);
        sopfValutaVenta         = tblFXDeals.getString("opfValutaVenta", iRow);
        sopfIndTipoOp           = tblFXDeals.getString("opfIndTipoOp", iRow);
        sopfNumPasaporte        = tblFXDeals.getString("opfNumPasaporte", iRow);
        sopfPaisPago            = tblFXDeals.getString("opfPaisPago", iRow);
        sopfIndicadorFX         = tblFXDeals.getString("opfIndicadorFX", iRow);

        sFecha = OCalendar.formatDateInt(iToday, DATE_FORMAT.DATE_FORMAT_ISO8601_EXTENDED, DATE_LOCALE.DATE_LOCALE_US);
        
        ODateTime dt = ODateTime.dtNew();
        dt.setTime(Util.timeGetServerTime());
        //sHora = Util.timeGetServerTimeHMS().trim();
        sHora = dt.formatForDbAccess().substring(11);

        sXMLDeal = sopfOrigen + "," + sopfParMonedas + "," + sopfTipoProducto + "," + sopfCanalTrx + "," + sopfMedioTrx + ","
                + sopfUsuario + "," + sopfNumeroOpFindur + "," + sopfCodSucOrigen + "," + sopfRutCliente + "," + sopfSecuenciaCliente + ","
                + sopfTipoCliente + "," + sopfNombreCliente + "," +  sopfMonedaCompra + "," + sopfMonedaVenta + "," + sopfMontoCompra + ","
                + sopfMontoVenta + "," + sopfEquivMontoCompraCLP + "," + sopfEquivMontoVentaCLP + "," + sopfTCCierreUSD + ","
                + sopfParCierreUSDME + "," + sopfCostoFondoUSD + "," + sopfParCostoFondoUSDME + "," + sopfPrecioCliente + ","
                + sopfPrecioCosto + "," + sopfFPagoCompra + "," + sopfFPagoVenta + "," + sopfValutaCompra + "," + sopfValutaVenta + ","
                + sopfIndTipoOp + "," + sopfNumPasaporte + "," + sopfPaisPago + "," + sopfIndicadorFX + "," + sFecha + "," + sHora + ","
                + sFINDUR;
        INCGeneralItau.print_msg("DEBUG", "(funcLoadCMXLog)\n" + sXMLDeal);

        return sXMLDeal;
    }
    
    
    /*****************************************************************************
    Name:           funcLoadCMXLog
    Description:    Retrieve List Deal Version History

     * @param     Table tblCMXLog - empty table passed from main
     * @return     OLF_RETURN_APP_FAILURE -12
                 OLF_RETURN_FATAL_ERROR -13
                OLF_RETURN_RETRYABLE_ERROR -14
                OLF_RETURN_SUCCEED 1
     * @throws     OException
    *****************************************************************************/
    int funcLoadCMXLog(Table tblCMXLog) throws OException
    {
    	INCGeneralItau.print_msg("INFO", "**** funcLoadCMXLog");
    	
        int iRetVal;

        String sQuery;
        /*----------------------------------------------*/
        /*  Retrieve List of Payment Method                */
        /*----------------------------------------------*/
        sQuery = "Select log.origen, log.deal_num, log.tran_num, log.version_num, log.cod_error, log.mensaje_error, "+
                    "log.fecha_hora, ab.tran_status,  'I' as Ingreso, 'A' as Anulacion, 'M' as Modificada, 'E' as Enviada "+
                    "From USER_CMX_LOG log "+
                    "Left Join ab_tran ab on log.deal_num = ab.deal_tracking_num "+
                    "Where ab.tran_status in ("+ TRAN_STATUS_ENUM.TRAN_STATUS_VALIDATED.jvsValue() +
                    ", " + TRAN_STATUS_ENUM.TRAN_STATUS_CANCELLED.jvsValue()+")" +
                    " And ab.toolset = " + TOOLSET_ENUM.FX_TOOLSET.jvsValue() +
                    " And ab.tran_type =" + TRAN_TYPE_ENUM.TRAN_TYPE_TRADING.jvsValue() +
                    " And origen = '" + sFND + "' order by log.tran_num";



        iRetVal = DBaseTable.execISql(tblCMXLog, sQuery);

        INCGeneralItau.print_msg("DEBUG", "(funcLoadCMXLog)\n" + sQuery);
        
//        OConsole.oprint("Query funcLoadCMXLog :\n"+sQuery);

        return iRetVal;
    }
    /*****************************************************************************
    Name:           funcLoadDealVerHistory
    Description:    Retrieve List Deal Version History

     * @param     Table tblDealVerHistory - empty table passed from main
     * @return     OLF_RETURN_APP_FAILURE -12
                 OLF_RETURN_FATAL_ERROR -13
                OLF_RETURN_RETRYABLE_ERROR -14
                OLF_RETURN_SUCCEED 1
     * @throws     OException
    *****************************************************************************/
    int funcLoadDealVerHistory(Table tblDealVerHistory) throws OException
    {
    	INCGeneralItau.print_msg("INFO", "**** funcLoadDealVerHistory");
    	
        int iRetVal;

        String sQuery;
        /*----------------------------------------------*/
        /*  Retrieve List of History by Version            */
        /*----------------------------------------------*/
        sQuery = "Select tran_num, max(version_number) as version "+
                    "From ab_tran_history_view " +
                    "Where tran_status in ("+ TRAN_STATUS_ENUM.TRAN_STATUS_VALIDATED.jvsValue() +
                    " ," + TRAN_STATUS_ENUM.TRAN_STATUS_CANCELLED.jvsValue() + ")" +
                    " Group By tran_num Order by tran_num";

        iRetVal = DBaseTable.execISql(tblDealVerHistory, sQuery);

        INCGeneralItau.print_msg("DEBUG", "(funcLoadDealVerHistory)\n" + sQuery);

        return iRetVal;
    }
    /*****************************************************************************
    Name:           funcLoadDealHistory
    Description:    Retrieve List Deal Number History

     * @param     Table tblDealHistory - empty table passed from main
     * @return     OLF_RETURN_APP_FAILURE -12
                 OLF_RETURN_FATAL_ERROR -13
                OLF_RETURN_RETRYABLE_ERROR -14
                OLF_RETURN_SUCCEED 1
     * @throws     OException
    *****************************************************************************/
    int funcLoadDealHistory(Table tblDealHistory) throws OException
    {
    	INCGeneralItau.print_msg("INFO", "**** funcLoadDealHistory");
    	
        int iRetVal;

        String sQuery;
        /*----------------------------------------------*/
        /*  Retrieve List of History by Tran Num        */
        /*----------------------------------------------*/
        sQuery = "Select deal_tracking_num, max(tran_num) as tran_num, '' as tipo_operation  "+
                    "From ab_tran_history_view " +
                    "Where tran_status in ("+ TRAN_STATUS_ENUM.TRAN_STATUS_VALIDATED.jvsValue() +
                    " ," + TRAN_STATUS_ENUM.TRAN_STATUS_CANCELLED.jvsValue() + ")" +
                    " Group By deal_tracking_num Order by tran_num";

        iRetVal = DBaseTable.execISql(tblDealHistory, sQuery);

        INCGeneralItau.print_msg("DEBUG", "(funcLoadDealHistory)\n" + sQuery);

        return iRetVal;
    }
   
    /*****************************************************************************
    Name:           funcRetrieveListHolding
    Description:    Retrieve List Holding (Instrument Number and Reference) to get a Par Monedas

     * @param     Table tblListHolding - empty table passed from main
     * @return     OLF_RETURN_APP_FAILURE -12
                 OLF_RETURN_FATAL_ERROR -13
                OLF_RETURN_RETRYABLE_ERROR -14
                OLF_RETURN_SUCCEED 1
     * @throws     OException
    *****************************************************************************/
    int funcRetrieveListHolding(Table tblListHolding) throws OException
    {
    	INCGeneralItau.print_msg("INFO", "**** funcRetrieveListHolding");
    	
        int iRetVal;

        String sQuery;
        /*----------------------------------------------*/
        /*  Retrieve List of Payment Method                */
        /*----------------------------------------------*/
        sQuery = "Select ins_num, reference From ab_tran" +
                    " Where tran_type = " + TRAN_TYPE_ENUM.TRAN_TYPE_HOLDING.jvsValue() +
                    " And tran_status = " + TRAN_STATUS_ENUM.TRAN_STATUS_VALIDATED.jvsValue() +
                    " And toolset = " + TOOLSET_ENUM.FX_TOOLSET.jvsValue();

        iRetVal = DBaseTable.execISql(tblListHolding, sQuery);

        INCGeneralItau.print_msg("DEBUG", "(funcLoadPMTF)\n" + sQuery);

        return iRetVal;
    }
    
    /*****************************************************************************
    Name:           funcLoadFXDeal
    Description:    Retrieve the FX Deal Canceled for the tranNum parameter.

     * @param Table tblFXDeals - empty table passed from main.
     * @param tranNum - tran_num of the operation passed from main.
     * @return OLF_RETURN_APP_FAILURE -12
     *          OLF_RETURN_FATAL_ERROR -13
                OLF_RETURN_RETRYABLE_ERROR -14
                OLF_RETURN_SUCCEED 1
     * @throws OException
    *****************************************************************************/
    int funcLoadFXDeal(Table tblFXDeals, int tranNum) throws OException
    {
    	INCGeneralItau.print_msg("INFO", "**** funcLoadFXDeal");
    	
        String sQuery;
        int iRetVal, iFourDaysBefore;
        
        iFourDaysBefore = funcCalcDaysBefore(4); //4 days
        
        sQuery =    "Select ab.tran_num as tran_num, ab.deal_tracking_num as deal_num, ab.ins_num, " +
                    "ab.external_lentity, ab.external_bunit, ab.tran_status, pad.country as countryId, " +
                    "fx.ccy1 as currency1, fx.ccy2 as currency2, " +
                    "fx.d_amt as monto_ccy1, fx.c_amt as monto_ccy2, " +
                    "'' as pmt_bco, '' as pmt_cli, " +
                    "0 as valuta_bco, 0 as valuta_cli, " +
                    "ab.settle_date, " +
                    "'" + sFND + "' as opfOrigen, " +
                    "'' as opfParMonedas, " +
                    "case when ab.buy_sell = 0 then 'C' " +
                    "when ab.buy_sell = 1 then 'V' " +
                    "end as opfTipoProducto, " +
                    "'' as opfCanalTrx, " + /* USER_CanalTransaccional */
                    "'' as opfMedioTrx, " + /* USER_MedioTransaccional */
                    "per.name as 'opfUsuario', " +
                    "ab.deal_tracking_num as opfNumeroOpFindur, " +
                    "0 as opfCodSucOrigen, " +
                    "'' as opfRutCliente, " +
                    "'' as opfSecuenciaCliente, " +
                    "0 as opfTipoCliente, " + /* ID USER_Tipo_Institucion_financiera */
                    "par.long_name as opfNombreCliente, " +
                    "'' as  opfMonedaCompra, " +
                    "'' as opfMonedaVenta, " +
                    "fx.d_amt as opfMontoCompra, " +
                    "fx.c_amt as opfMontoVenta, " +
                    "0 as opfEquivMontoCompraCLP, " +
                    "0 as opfEquivMontoVentaCLP, " +
                    "'0.00' as opfTCCierreUSD, " +
                    "'0.00' as opfParCierreUSDME, " +
                    "'0.00' as opfCostoFondoUSD, " +
                    "'0.00' as opfParCostoFondoUSDME, " +
                    "fx.rate as opfPrecioCliente, " +
                    "fx.quote as opfPrecioCosto, " +
                    "'' as opfFPagoCompra,  " +
                    "'' as opfFPagoVenta,  " +
                    "ab.settle_date as opfValutaCompra,  " +
                    "fx.term_settle_date as opfValutaVenta,  " +
                    "'A' as 'opfIndTipoOp', " +
                    "'' as opfNumPasaporte, " +
                    "'997' as 'opfPaisPago', " + /* ID USER_Country */
                    "'' as 'opfIndicadorFX' " +
                    "From ab_tran ab  " +
                    "Inner Join fx_tran_aux_data fx on ab.tran_num = fx.tran_num  " +
                    "Inner Join personnel per on ab.internal_contact = per.id_number  " +
                    "Inner Join party par on ab.external_lentity = par.party_id  " +
                    "Inner Join party_address pad on ab.external_lentity = pad.party_id " +

                    // // Get Transaction Cancelled up to four business days before of the event date.
                    // "Inner Join ab_tran_event ate on ab.tran_num = ate.tran_num " +
                    // "And ate.event_type = " + CFLOW_TYPE.CASH_CFLOW.toInt() + " " +
                    // "And ate.ins_para_seq_num = 0 " +
                    // "And ate.event_date >= " + iFourDaysBefore + " " + 
                    // "And ate.event_date <= " + iToday + " " +

                    // To bring only cancelled deals already sent to CMX
                    " Inner Join USER_CMX_LOG log on ab.deal_tracking_num = log.deal_num and log.cod_error = 0 "+

                    "Where ab.tran_num in (Select tran_num From ab_tran_info_view " +
                                           "Where type_name = '"+ sMedioTran + "' and value not in ('" + sCMX + "','" + sMedioTranExport + "')) " +
                    " And ab.tran_status = " + TRAN_STATUS_ENUM.TRAN_STATUS_CANCELLED.jvsValue() +
                    " And ab.toolset = " + TOOLSET_ENUM.FX_TOOLSET.jvsValue() +
                    " And ab.tran_type =" + TRAN_TYPE_ENUM.TRAN_TYPE_TRADING.jvsValue() +

                    //Ignore NEM operations
                    " And ab.internal_lentity <> ab.external_lentity " +

                    // Check Horario Especial
                    " And ab.trade_date >= '" + OCalendar.formatJdForDbAccess(iFourDaysBefore) + "'" + 
                    " And ab.trade_date <= '" + OCalendar.formatJdForDbAccess(iToday) + "'" + //" + iToday + " " +
                    " And ab.tran_num = "+ tranNum +
                    " order by ab.tran_num desc";  
//        OConsole.oprint("Anulados : \n"+ sQuery);

        iRetVal = DBaseTable.execISql(tblFXDeals, sQuery);

        INCGeneralItau.print_msg("DEBUG", "(funcLoadFXDeal)\n" + sQuery);

        return iRetVal;
    }

    /*****************************************************************************
    Name:           funcLoadFXDealNotCMX
    Description:    Retrieve the FX Deal, that don't was successfully sent to CMX, for the given tranNum parameter.

     * @param Table tblFXDeals - empty table passed from main.
     * @param tranNum - tran_num of the operation passed from main.
     * @return OLF_RETURN_APP_FAILURE -12
     *         OLF_RETURN_FATAL_ERROR -13
               OLF_RETURN_RETRYABLE_ERROR -14
               OLF_RETURN_SUCCEED 1
     * @throws OException
    *****************************************************************************/
    int funcLoadFXDealNotCMX(Table tblFXDealsCMX, int tranNum) throws OException
    {
    	INCGeneralItau.print_msg("INFO", "**** funcLoadFXDealNotCMX");
    	
        String sQuery;
        int iRetVal;
        /* Load Deals TranInfo <> CMX */
        sQuery =    "Select ab.tran_num as tran_num, ab.deal_tracking_num as deal_num, ab.ins_num, " +
                    "ab.external_lentity, ab.external_bunit, ab.tran_status, pad.country as countryId, " +
                    "fx.ccy1 as currency1, fx.ccy2 as currency2, " +
                    "fx.d_amt as monto_ccy1, fx.c_amt as monto_ccy2, " +
                    "'' as pmt_bco, '' as pmt_cli, " +
                    "0 as valuta_bco, 0 as valuta_cli, " +
                    "ab.settle_date, " +
                    "'" + sFND + "' as opfOrigen, " +
                    "'' as opfParMonedas, " +
                    "case when ab.buy_sell = 0 then 'C' " +
                    "when ab.buy_sell = 1 then 'V' " +
                    "end as opfTipoProducto, " +
                    "'' as opfCanalTrx, " +
                    "'' as opfMedioTrx, " +
                    "per.name as 'opfUsuario', " +
                    "ab.deal_tracking_num as opfNumeroOpFindur, " +
                    "0 as opfCodSucOrigen, " +
                    "'' as opfRutCliente, " +
                    "'' as opfSecuenciaCliente, " +
                    "0 as opfTipoCliente, " +
                    "par.long_name as opfNombreCliente, " +
                    "'' as  opfMonedaCompra, " +
                    "'' as opfMonedaVenta, " +
                    "fx.d_amt as opfMontoCompra, " +
                    "fx.c_amt as opfMontoVenta, " +
                    "0 as opfEquivMontoCompraCLP, " +
                    "0 as opfEquivMontoVentaCLP, " +
                    "'0.00' as opfTCCierreUSD, " +
                    "'0.00' as opfParCierreUSDME, " +
                    "'0.00' as opfCostoFondoUSD, " +
                    "'0.00' as opfParCostoFondoUSDME, " +
                    "fx.rate as opfPrecioCliente, " +
                    "fx.quote as opfPrecioCosto, " +
                    "'' as opfFPagoCompra,  " +
                    "'' as opfFPagoVenta,  " +
                    "ab.settle_date as opfValutaCompra,  " +
                    "fx.term_settle_date as opfValutaVenta,  " +
                    "'I' as 'opfIndTipoOp', " +
                    "'' as opfNumPasaporte, " +
                    "'997' as 'opfPaisPago',  " +
                    "'' as 'opfIndicadorFX'  " +
                    "From ab_tran ab " +
                    "Inner Join fx_tran_aux_data fx on ab.tran_num = fx.tran_num  " +
                    "Inner Join personnel per on ab.internal_contact = per.id_number  " +
                    "Inner Join party par on ab.external_lentity = par.party_id  " +
                    "Inner Join party_address pad on ab.external_lentity = pad.party_id " +

                    // // Check Horario Especial
                    // "Inner Join ab_tran_event ate on ab.tran_num = ate.tran_num " +
                    // "And ate.event_type = " + CFLOW_TYPE.CASH_CFLOW.toInt() + " " +
                    // "And ate.ins_para_seq_num = 0 " +
                    // "And ate.event_date = " + iToday + " " +

//                  "Where ab.tran_num in (Select tran_num From ab_tran_info_view Where type_name = '"+ sMedioTran + "' And value <> '"+ sCMX + "') " +
					//	Check Horario Especial
//					" And ab.trade_date = '" + OCalendar.formatJdForDbAccess(iToday) + "'" +
                    
					"Where ((ab.trade_date = '" + OCalendar.formatJdForDbAccess(iToday) + "' " +
                    		"and ab.tran_num in (Select tran_num From ab_tran_info_view Where type_name = '"+ sMedioTran + "' And value not in ('"+ sCMX + "','" + sMedioTranExport + "'))) " +
                    	" or (ab.settle_date= '" + OCalendar.formatJdForDbAccess(iToday) + "' " +
                    	"and ab.tran_num in (Select tran_num From ab_tran_info_view Where type_name = '"+ sMedioTran + "' And value = '" + sMedioTranExport + "')))" +
                    
                    " And ab.tran_status =" + TRAN_STATUS_ENUM.TRAN_STATUS_VALIDATED.jvsValue() +
                    " And ab.toolset = " + TOOLSET_ENUM.FX_TOOLSET.jvsValue() +
                    " And ab.tran_type =" + TRAN_TYPE_ENUM.TRAN_TYPE_TRADING.jvsValue() +
                    //" And ab.tran_num not in (Select tran_num From USER_CMX_LOG Where origen = '" + sFND + "' And cod_error <> -999)" +
                    " And ab.tran_num not in (Select tran_num From USER_CMX_LOG Where origen = '" + sFND + "' And cod_error = 0)" + //0 = Deal sent to CMX

                    //Ignore NEM operations
                    " And ab.internal_lentity <> ab.external_lentity " +
                    " And ab.tran_num = " + tranNum +
                    " Order by ab.tran_num desc";

        iRetVal = DBaseTable.execISql(tblFXDealsCMX, sQuery);

        INCGeneralItau.print_msg("DEBUG", "(funcLoadFXDealNotCMX)\n" + sQuery);

        return iRetVal;
    }
    /*****************************************************************************
    Name:           funcTableFormatting
    Description:    This function sets XML format tags name

     * @param Table tblFormatXML
     * @throws OException
    *****************************************************************************/
    void funcTableFormatting(Table tblFormatXML) throws OException
    {
    	INCGeneralItau.print_msg("INFO", "**** funcTableFormatting");
    	

        /* Format Reference */
    	tblFormatXML.colConvertIntToDateTime("opfValutaCompra");
    	tblFormatXML.colConvertIntToDateTime("opfValutaVenta");
        tblFormatXML.setColFormatAsDate("opfValutaCompra", DATE_FORMAT.DATE_FORMAT_ISO8601_EXTENDED);//DATE_FORMAT.DATE_FORMAT_ISO8601_EXTENDED
        tblFormatXML.setColFormatAsDate("opfValutaVenta", DATE_FORMAT.DATE_FORMAT_ISO8601_EXTENDED);

        /* Set Table Name */
        tblFormatXML.setTableName("campos");

        /* Delete Column Table */
        tblFormatXML.delCol("tran_status");
        tblFormatXML.delCol("external_lentity");
        tblFormatXML.delCol("external_bunit");
        tblFormatXML.delCol("monto_ccy1");
        tblFormatXML.delCol("monto_ccy2");
        tblFormatXML.delCol("pmt_bco");
        tblFormatXML.delCol("valuta_bco");
        tblFormatXML.delCol("pmt_cli");
        tblFormatXML.delCol("valuta_cli");
        tblFormatXML.delCol("settle_date");
        tblFormatXML.delCol("countryId");

    }


    /**
     * Get string value with the correct precision
     *
     * @param sValue
     * @return
     * @throws OException
     */
    double STR_findStrPrec(String sValue) throws OException
    {
    	INCGeneralItau.print_msg("INFO", "**** STR_findStrPrec");
    	
       int iLen;
       int iFind;
       int iPrec;
       double dValue = -1.0;

       iLen = Str.len(sValue);
       iFind = Str.findLastSubString(sValue, ".");
       if(iFind <= 0)
          iFind = Str.findLastSubString(sValue, ",");

       if(iFind <= 0)
          iPrec = 0;
       else
          iPrec = iLen - iFind - 1;

       dValue = Str.strToDouble(sValue, 1);


       //return Str.doubleToStr(dValue, iPrec);
       return Math.round(dValue, iPrec);
    }
    /*****************************************************************************
    Name:           funcCalcDaysBefore
    Description:    Calculate days before today

     * @param  Integer iNumDays - Number of the days
     * @return Integer iFourDaysBefore
     * @throws OException
    *****************************************************************************/
    int funcCalcDaysBefore(int iNumDays) throws OException
    {
    	INCGeneralItau.print_msg("INFO", "**** funcCalcDaysBefore");
    	
    	int i, iStartDay = iToday;
    	int iFourDaysBefore = 0;
    	//String sDate = "";
    	
    	for(i =1 ; i <= iNumDays; i++)
    	{
    		iStartDay = OCalendar.getLgbd(iStartDay);
    	}
    	iFourDaysBefore = iStartDay;
        
    	return iFourDaysBefore;
    }
    /*****************************************************************************
    Name:           funcFormatRateDigits
    Description:    Format the number of decimals.
     
     * @param  Table tblInsert
     * @return 	OLF_RETURN_APP_FAILURE -12
     			OLF_RETURN_FATAL_ERROR -13
				OLF_RETURN_RETRYABLE_ERROR -14
				OLF_RETURN_SUCCEED 1 
     * @throws OException                   
    *****************************************************************************/
	int funcFormatRateDigits(Table tblFXDealsCMX) throws OException
	{
    	INCGeneralItau.print_msg("INFO", "**** funcFormatRateDigits");
    	
		int iRetVal;
		Table tblInsNum = Util.NULL_TABLE;
		
		tblInsNum = Table.tableNew();
		String sQuery = "Select ab.ins_num, " +
				        "ab.cflow_type, " +
				        "ab.reference, " +
				        "par.currency as currency1, " +
				        "par1.currency as currency2, " +
				        "par.nearby as rate_digits," +
				        "c.round as currency1_digits, c2.round as currency2_digits "+
					"From ab_tran ab " +
					"Left Join parameter par on ab.ins_num = par.ins_num " +
					"Left Join parameter par1 on ab.ins_num = par1.ins_num " +
					"Left Join header hea on ab.ins_num = hea.ins_num," +
					"currency c, currency c2 " +
					"Where ab.tran_type = " + TRAN_TYPE_ENUM.TRAN_TYPE_HOLDING.jvsValue() + 
					" And ab.tran_status = " +  TRAN_STATUS_ENUM.TRAN_STATUS_VALIDATED.jvsValue() + 
					" And ab.toolset = " + TOOLSET_ENUM.FX_TOOLSET.jvsValue() +
					" And par.param_seq_num = 0 And par1.param_seq_num = 1" +
					" And hea.portfolio_group_id > 0 " +
					" AND c.id_number = par.currency AND c2.id_number = par1.currency";
		
		iRetVal = DBaseTable.execISql(tblInsNum, sQuery);
		
		INCGeneralItau.print_msg("DEBUG", "(funcGetInsNum)\n" + sQuery);
		
		if (tblInsNum.getNumRows() > 0)
		{
			//tblFXDealsCMX.select(tblInsNum, "rate_digits", "currency EQ $currency1 AND currency2 EQ $currency2");
		    tblFXDealsCMX.addCol("rate_digits", COL_TYPE_ENUM.COL_INT);
		    tblFXDealsCMX.addCol("tc_rate_digits", COL_TYPE_ENUM.COL_INT);
		    tblFXDealsCMX.addCol("par_rate_digits", COL_TYPE_ENUM.COL_INT);
		    tblFXDealsCMX.addCol("ccy1_digits", COL_TYPE_ENUM.COL_INT);
		    tblFXDealsCMX.addCol("ccy2_digits", COL_TYPE_ENUM.COL_INT);
		    
		    int iCcy1;
		    int iCcy2;
		    int iNumRows = tblFXDealsCMX.getNumRows();
		    for(int i = 1; i <= iNumRows; i++)
		    {
		        iCcy1 = tblFXDealsCMX.getInt("currency1", i);
		        iCcy2 = tblFXDealsCMX.getInt("currency2", i);
		        tblFXDealsCMX.setInt("rate_digits", i, funGetRateDigits(tblInsNum, iCcy1, iCcy2));
		        tblFXDealsCMX.setInt("tc_rate_digits", i, funGetRateDigits(tblInsNum, iUSD, iCLP));
		        tblFXDealsCMX.setInt("par_rate_digits", i, funGetRateDigits(tblInsNum, iCcy1, iUSD));
		        tblFXDealsCMX.setInt("ccy1_digits", i, funGetCcyDigits(tblInsNum, iCcy1));
		        tblFXDealsCMX.setInt("ccy2_digits", i, funGetCcyDigits(tblInsNum, iCcy2));
		    }
		}
		tblInsNum.destroy();
		
		return iRetVal;
	}
	
	
	int funGetCcyDigits(Table tbl, int ccy) throws OException
    {
    	INCGeneralItau.print_msg("INFO", "**** funGetCcyDigits");
    	

        int iC1, iC2;
        int iResult = 2;
        int iNumRows = tbl.getNumRows();
        
        for (int i = 1; i <= iNumRows; i++)
        {
            iC1 = tbl.getInt("currency1", i);
            iC2 = tbl.getInt("currency2", i);
            if(iC1 == ccy)
            {
                iResult = tbl.getInt("currency1_digits", i);
                break;
            }
            else if(iC2 == ccy)
            {
                iResult = tbl.getInt("currency2_digits", i);
                break;
            }
        }
        
        return iResult;
    }

    int funGetRateDigits(Table tbl, int ccy1, int ccy2) throws OException
    {
    	INCGeneralItau.print_msg("INFO", "**** funGetRateDigits");
    	

        int iC1, iC2;
        int iResult = 2;
        int iNumRows = tbl.getNumRows();
        
        for (int i = 1; i <= iNumRows; i++)
        {
            iC1 = tbl.getInt("currency1", i);
            iC2 = tbl.getInt("currency2", i);
            if((iC1 == ccy1 && iC2 == ccy2) || (iC1 == ccy2 && iC2 == ccy1))
            {
                iResult = tbl.getInt("rate_digits", i);
                break;
            }
        }
        
        return iResult;
    }

    /*****************************************************************************
    Name:           funcConvertStrToDbl
    Description:    Convert String to Double.
     
     * @param  String sValue
     * @return Double dValue
     * @throws OException                   
    *****************************************************************************/
	double funcConvertStrToDbl(String sValue) throws OException
	{
		double dValue = 0;
		
		dValue = Double.valueOf(sValue.trim()).doubleValue();
		
		return dValue;
	
	}
	/*****************************************************************************
    Name:           funcRoundingNumericalOp
    Description:    Rounding function for numerical operations 
     
     * @param  Double dValue
     * @param  Integer iDecimal
     * @return Double dValue
     * @throws OException                   
    *****************************************************************************/
	double funcRoundingNumericalOp(double dValue,int iDecimal) { 
		
	    BigDecimal bd; 
	    
	    bd = BigDecimal.valueOf(dValue);
	    
	    return bd.setScale(iDecimal,RoundingMode.HALF_UP).doubleValue();  
	}


}
