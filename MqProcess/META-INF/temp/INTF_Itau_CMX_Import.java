/**$Header: v1.0 04/10/2016 $*/

/**
 * File Name:                  INTF_Itau_CMX_Import.java
 * Input File Name:            XML String with CMX Operation
 * Author:                     Ruben Echeverri R.
 * Creation Date:              04 Septiembre 2016
 * 
 * REVISION HISTORY
 * 	   Release:					
 *     Date:						   
 *     Description:            	
 *     Author:                 	
 *     
 *     
 *     
 *      
 * Script Type:     		   Task - Trade Manager Task
 * Purpose:                    Script starts a Socket server and receives a xml String from a Socket Client.
 * Assumptions:                The script is save with the following options:
 * 								Type: Main
 * 								Category: input String XML load data.
 * 
 * 
 */

package com.customer.INTF_Itau_CMX_Import;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import cl.com.itau.enums.EnumCurrency;
import cl.itau.enums.EnumStatus;
import cl.itau.enums.EnumTypeMessage;
import cl.itau.log.UTIL_Log;
import com.olf.openjvs.DBUserTable;
import com.olf.openjvs.DBaseTable;
import com.olf.openjvs.Debug;
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
import com.olf.openjvs.enums.INS_TYPE_ENUM;
import com.olf.openjvs.enums.OLF_RETURN_CODE;
import com.olf.openjvs.enums.SEARCH_CASE_ENUM;
import com.olf.openjvs.enums.SEARCH_ENUM;
import com.olf.openjvs.enums.SHM_USR_TABLES_ENUM;
import com.olf.openjvs.enums.TIME_FORMAT;
import com.olf.openjvs.enums.TOOLSET_ENUM;
import com.olf.openjvs.enums.TRANF_FIELD;
import com.olf.openjvs.enums.TRAN_STATUS_ENUM;
import com.olf.openjvs.enums.TRAN_TYPE_ENUM;

public class INTF_Itau_CMX_Import implements IScript {
	private final String DOC_SCRIPT_NAME = this.getClass().getSimpleName();
	private final String USER_CONFIGURABLE_VARIABLES = "USER_Configurable_Variables";
	private UTIL_Log Log = new UTIL_Log(DOC_SCRIPT_NAME);
	private Table tUserCnfVariables = Util.NULL_TABLE,
			tblInsertUserData	= Util.NULL_TABLE,
			tblUpDateUserData	= Util.NULL_TABLE;
	private String sErrorMessage = "";
	private int iCMX_Deal_Num = 0;
	private String sNameNumeroOpCmx = "Numero Operacion";
	private String sCMX = "CMX";
	private String sMedioTran = "Medio Transaccional";
	private int _iMilisegundos;
	private int _TEMPLATE_NUM = 0;
	private String sMessageresponse ="";
	
	private static int iDealNumToUserTab=0;
	private static int iTranNumToUserTab=0;
	
//	ArrayList<Integer> opGrabadas;

	
	 static ServerSocket socket1;
	 static int port;
	 static Socket connection;

	 static boolean first;
	 static StringBuffer process;
	 static String TimeStamp;

	// private static FxOnline fxOnline;

	public INTF_Itau_CMX_Import() {
		super();
	}

	public void execute(IContainerContext context) throws OException {
		tUserCnfVariables = Table.tableNew();
		// BOF
		try {
			Log.markStartScript();
			Log.setLOG_STATUS(EnumStatus.ON);
			
			loadUserCnfVariableTab();
			
			/*----------------------------------------------*/
			/* Begin Starting Socket Server                 */
			/*----------------------------------------------*/
			port = Str.strToInt(tUserCnfVariables.getString("valor", tUserCnfVariables.findString("variable", "socket_server_port",SEARCH_ENUM.FIRST_IN_GROUP)));
			Log.printMsg(EnumTypeMessage.INFO, "port of the Socket Server: "+port);
				
			socket1 = new ServerSocket(port);
//		    System.out.println("SocketServer Initialized");
		    int character;
		    while (true) {
		    	Log.printMsg(EnumTypeMessage.INFO, "Waiting for CMX XML...");
		    	 connection = socket1.accept();
		    	 
		          BufferedInputStream is = new BufferedInputStream(connection.getInputStream());
		          InputStreamReader isr = new InputStreamReader(is);
		          process = new StringBuffer();
		          
	          while((character = isr.read()) != 13) {
		            process.append((char)character);
		          }
		    	
		    
			/*----------------------------------------------*/
			/* End Starting Socket Server                   */
			/*----------------------------------------------*/
			
			

			Table tblXml = Table.xmlStringToTable(process.toString());

			if (Table.isTableValid(tblXml) != 1) {
				Log.printMsg(EnumTypeMessage.ERROR, "Xml Parse error : El XML "+ process.toString() + " No tiene el formato adecuado.");
				setErrorLog("Xml Parse error : El XML " + process.toString()+ " No tiene el formato adecuado.", "ERROR",0, 0);
			}
			
			Table campos = tblXml.getTable("campos", 1);
			// Get Template Number
			_TEMPLATE_NUM = getTemplate();
			Log.printMsg(EnumTypeMessage.INFO, "obtencion Milisegundos");

			_iMilisegundos = Str.strToInt(tUserCnfVariables.getString("valor",tUserCnfVariables.unsortedFindString("variable","milisegundos",SEARCH_CASE_ENUM.CASE_INSENSITIVE)));

			Log.printMsg(EnumTypeMessage.INFO, "Los Milisegundos son "+ _iMilisegundos);

			if (doCreateFxSpotDeal(campos)) {
				Log.printMsg(EnumTypeMessage.INFO,"Fx Spot Deals: create successfully");
				/*send response to client socket*/
				TimeStamp = new java.util.Date().toString();
		          String returnCode = "SocketServer responsed From Import:PROCESSED at "+ TimeStamp + (char) 13;
		          BufferedOutputStream os = new BufferedOutputStream(connection.getOutputStream());
		          OutputStreamWriter osw = new OutputStreamWriter(os, "US-ASCII");
		          osw.write(returnCode);
		          osw.flush();

			} else {
				Log.printMsg(EnumTypeMessage.ERROR,"Fx Spot Deals Create Error : " + sErrorMessage+ ". Check Log Desktop.");
				/*create file with XML content*/
				sMessageresponse="Fx Spot Deals Create Error : " + sErrorMessage;
				TimeStamp = new java.util.Date().toString();
		          String returnCode = "SocketServer responsed From Import:ERROR at "+ TimeStamp + ". "+sMessageresponse+(char) 13;
		          BufferedOutputStream os = new BufferedOutputStream(connection.getOutputStream());
		          OutputStreamWriter osw = new OutputStreamWriter(os, "US-ASCII");
		          osw.write(returnCode);
		          osw.flush();
				
			}


			tblXml.destroy();
			
		}

		} catch (OException e) {
			String msg = DOC_SCRIPT_NAME + " - XML Bac-Turing Import Error: "+ e.getMessage();
			Log.printMsg(EnumTypeMessage.ERROR, msg);
			// setErrorLog(msg,"ERROR",iBAC_TURING_Deal_Num,iFindur_Deal_Num);
			Log.printMsg(EnumTypeMessage.INFO,"Start Creating XML of Operations for FxOnlineConfirmacion");
			
			try {
			  TimeStamp = new java.util.Date().toString();
			  String returnCode = "SocketServer responsed From Import:ERROR at "+ TimeStamp +". "+msg+ (char) 13;
	          BufferedOutputStream os = new BufferedOutputStream(connection.getOutputStream());
	          OutputStreamWriter osw = new OutputStreamWriter(os, "US-ASCII");
	          osw.write(returnCode);
	          osw.flush();
			} catch (IOException e1) {
				Log.printMsg(EnumTypeMessage.ERROR,"IOException closing the connection: "+ e1);
			}
		} catch (Exception wse) {
			wse.printStackTrace();
			Log.printMsg(EnumTypeMessage.ERROR, wse.getMessage());
		} finally {
			Log.markEndScript();
			tUserCnfVariables.destroy();
			try {
		        connection.close();
		      }catch (IOException e) {
		    	  Log.printMsg(EnumTypeMessage.INFO,"IOException closing the connection: "+ e);
		      }catch (Exception e2) {
		    	  Log.printMsg(EnumTypeMessage.INFO,"Exception closing the connection: "+ e2);
			}
			
		}
		// EOF
	}

	/**
	 * End execute method
	 */
	
	/**
	 * 
	 * @param tEstruct
	 * @return
	 * @throws OException
	 */
	private boolean doCreateFxSpotDeal(Table tEstruct) throws OException {
		boolean myResult = true;
		int iToday, iIntPfolio, iIntBU, iExtBU, iExtLE;
		int iRetVal = 0;
		final int NUMBER_OF_FX;
		String sReference, sBuySell;

		String sBAC_Spread_USD_CLP;
		String sBAC_Spread_USD_DIV;

		double dSpreadUSDCLP = 0.0;
		double dSpreadUSDDIV = 0.0;
		double dPrice = 0.0;
		double dSpotRate = 0.0;
		double dSpotRateCmx = 0.0;
		double dAmount;
		int iIdNum = 0,version=0;

		iToday = OCalendar.today();
		NUMBER_OF_FX = tEstruct.getNumRows();

		// set the constants
		tUserCnfVariables.sortCol("variable");


		try {

			sReference = tUserCnfVariables.getString("valor", tUserCnfVariables.findString("variable", "reference",SEARCH_ENUM.FIRST_IN_GROUP));

			OConsole.oprint("In function----> sReference: "+sReference);
			
			for (int x = 1; x <= NUMBER_OF_FX; x++) {
				
				iCMX_Deal_Num = Str.strToInt(tEstruct.getString("opfNumeroOpFindur", x));

				// Create the table to insert the trades
				String sIndTipoOp = Str.stripBlanks(tEstruct.getString("opfIndTipoOp", x));//change BAC_Status  to opfIndTipoOp

				if ("I".equalsIgnoreCase(sIndTipoOp)) {//opfIndTipoOp = I - V=validado
					//validating operaration in user table:
					int[] iTransaction=funcCheckUserData(tEstruct.getString("opfNumeroOpFindur", 1));
					
					iIdNum = iTransaction[0];
					version = iTransaction[1];
					
					if(iIdNum>0){
						Log.printMsg(EnumTypeMessage.INFO, "CMX Number (" + iIdNum + ")  was already imported. \n");
						sErrorMessage="CMX Number (" + iIdNum + ")  was already imported. \n";
						myResult=false;
						continue;
						
					}else{
							Transaction tranFX = Transaction.retrieveCopy(_TEMPLATE_NUM);
							String sReferenceOP = "Imported from CMX - "+ tEstruct.getString("opfNumeroOpFindur", x);;
							
				
							
							//TODO fuente de estos 2 campos:
							sBAC_Spread_USD_CLP="0.0";
							sBAC_Spread_USD_DIV="0.0";
							
							dSpreadUSDCLP = Str.strToDouble(sBAC_Spread_USD_CLP.replace(",", "."));
							dSpreadUSDDIV = Str.strToDouble(sBAC_Spread_USD_DIV.replace(",", "."));
		
							dSpotRateCmx = Str.strToDouble(tEstruct.getString("opfPrecioCliente", x).replace(",", "."));//chenge BAC_Spot_Rate to opfPrecioCliente
		
							sBuySell = tEstruct.getString("opfTipoProducto", x);//CHANGE BAC_BuySell  TO 
		
							String sNombreCliente = tEstruct.getString("opfNombreCliente", x); //change BAC_Nombre_cliente to opfNombreCliente
							
							String [] parMonedas  = (tEstruct.getString("opfParMonedas", x)).split("/");
							
							String sCcy1 = parMonedas[0];
							String sCcy2 = parMonedas[1];
							
							int iCcy1 = Ref.getValue(SHM_USR_TABLES_ENUM.CURRENCY_TABLE, sCcy1);
							int iCcy2 = Ref.getValue(SHM_USR_TABLES_ENUM.CURRENCY_TABLE, sCcy2);
							
		
							String sIntBU = tUserCnfVariables.getString("valor", tUserCnfVariables.findString("variable", "int_Itau_BU",SEARCH_ENUM.FIRST_IN_GROUP));
							iIntBU = Ref.getValue(SHM_USR_TABLES_ENUM.BUNIT_TABLE, sIntBU);
							
							String pFolio = tUserCnfVariables.getString("valor", tUserCnfVariables.findString("variable", "portfolio",SEARCH_ENUM.FIRST_IN_GROUP));
							iIntPfolio = Ref.getValue(SHM_USR_TABLES_ENUM.PORTFOLIO_TABLE, pFolio);
		
							// Create Tran Info temporal Table
							Table tblTranInfo = Table.tableNew();
							tblTranInfo.addCol("type_name", COL_TYPE_ENUM.COL_STRING);
							tblTranInfo.addCol("value", COL_TYPE_ENUM.COL_STRING);
							tblTranInfo.addRowsWithValues("(Numero Operacion), ("+ Str.intToStr(iCMX_Deal_Num) + ")");
							tblTranInfo.addRowsWithValues("(ID Cliente), ("	+ tEstruct.getString("opfRutCliente", x)+ ")");//  Ojo...es opfNumPasaporte o opfRutCliente???   change BAC_Rut_Contraparte to opfRutCliente
							tblTranInfo.addRowsWithValues("(Spread USD/CLP), ("	+ sBAC_Spread_USD_CLP + ")");
							tblTranInfo.addRowsWithValues("(Spread USD/DIV), ("	+ sBAC_Spread_USD_DIV + ")");
							tblTranInfo.addRowsWithValues("(TC USD/CLP Cliente), ("+tEstruct.getString("opfTCCierreUSD", x).replace(",", ".")+")");
							tblTranInfo.addRowsWithValues("(TC USD/CLP Costo), ("+tEstruct.getString("opfCostoFondoUSD", x).replace(",", ".")+")");
							tblTranInfo.addRowsWithValues("(Nombre Cliente), ("	+ sNombreCliente + ")");
							tblTranInfo.addRowsWithValues("(Canal Transaccional), ("+ tEstruct.getString("opfCanalTrx", x) + ")");
							tblTranInfo.addRowsWithValues("(Medio Transaccional), ("+ tEstruct.getString("origen", x) + ")");
							tblTranInfo.addRowsWithValues("(Usuario Operacion), ("+ tEstruct.getString("opfUsuario", x) + ")");
							tblTranInfo.addRowsWithValues("(Sucursal), ("+ tEstruct.getString("opfCodSucOrigen", x) + ")");
							
			
							// 2.1 Added the new logic to field Indicador FX - para
							// cuando la operaciones es VCTO.FWD.E.F
							/*if (sReferenceOP.contains(sStringVctoTURING)) {
								tblTranInfo.addRowsWithValues("(Indicador FX), (EF)");
							}*/
		
	
							if (EnumCurrency.USD.toString().equalsIgnoreCase(sCcy1)&& EnumCurrency.CLP.toString().equalsIgnoreCase(sCcy2)) {
								
								dSpotRate = Str.strToDouble(tEstruct.getString("opfPrecioCliente", x));
								tblTranInfo.addRowsWithValues("(Paridad USD/DIV Costo), (1)");
								tblTranInfo.addRowsWithValues("(Paridad USD/DIV Cliente), (1)");
								dPrice = Str.strToDouble(tEstruct.getString("opfPrecioCliente", x)); //change BAC_Spot_Rate to opfPrecioCliente
		
								
							} else if (!EnumCurrency.CLP.toString().equalsIgnoreCase(sCcy1)&& EnumCurrency.USD.toString().equalsIgnoreCase(sCcy2)) {
								// "ENTRE ARBRITAJE : ");
								tblTranInfo.addRowsWithValues("(Paridad USD/DIV Cliente), ("+ tEstruct.getString("opfParCierreUSDME", x).replace(",", ".") + ")"); //change BAC_Spot_Rate to opfPrecioCliente
								tblTranInfo.addRowsWithValues("(Paridad USD/DIV Costo), ("+ tEstruct.getString("opfParCostoFondoUSDME", x).replace(",", ".")	+ ")");
								
								dSpotRate = Str.strToDouble(tEstruct.getString("opfPrecioCliente", x)); 
								dPrice = Str.strToDouble(tEstruct.getString("opfPrecioCliente", x));
		
								
							} else if (!EnumCurrency.USD.toString().equalsIgnoreCase(sCcy1)	&& EnumCurrency.CLP.toString().equalsIgnoreCase(sCcy2)) {
								tblTranInfo.addRowsWithValues("(Paridad USD/DIV Cliente), ("+ tEstruct.getString("opfParCierreUSDME", x).replace(",", ".") + ")"); //change BAC_Spot_Rate to opfPrecioCliente
								tblTranInfo.addRowsWithValues("(Paridad USD/DIV Costo), ("+ tEstruct.getString("opfParCostoFondoUSDME", x).replace(",", ".")	+ ")");
								
								dSpotRate = Str.strToDouble(tEstruct.getString("opfPrecioCliente", x)); //change BAC_Spot_Rate to opfPrecioCliente
								
								dPrice = Str.strToDouble(tEstruct.getString("opfPrecioCliente", x));
								
							}
		
							// External Values
							String sExtBU = tUserCnfVariables.getString("valor", tUserCnfVariables.findString("variable", "ext_Default_BU",SEARCH_ENUM.FIRST_IN_GROUP));
							String sExtLE = tUserCnfVariables.getString("valor", tUserCnfVariables.findString("variable", "ext_Default_LE",SEARCH_ENUM.FIRST_IN_GROUP));
							
							iExtBU = Ref.getValue(SHM_USR_TABLES_ENUM.BUNIT_TABLE, sExtBU);//getPartyValues("00", 0);
							iExtLE = Ref.getValue(SHM_USR_TABLES_ENUM.LENTITY_TABLE, sExtLE);//getPartyValues("00", 0);
		
							// Set the value_date and trade_date
							int iJBacDate = iToday;//OCalendar.parseString(tEstruct.getString("BAC_Trade_Date", x));
		
							// Set Amounts and Currencies
							if (sBuySell.equalsIgnoreCase("C"))
								dAmount = (Str.strToDouble(tEstruct.getString("opfMontoCompra", x).replace(",", ".")) * -1); //change BAC_Base_Amount to opfMontoCompra
							else
								dAmount = Str.strToDouble(tEstruct.getString("opfMontoVenta", x).replace(",", "."));// change BAC_Base_Amount to 
		
							int iNextGBD;
							int iNextGBD2;
							
							iNextGBD = parseMDYYYYtoJDate(tEstruct.getString("opfValutaCompra", x));//Str.strToInt(tEstruct.getString("opfValutaCompra", x));
							iNextGBD2=parseMDYYYYtoJDate(tEstruct.getString("opfValutaVenta", x));
		
							/*String sQuote = Str.formatAsDouble(dSpotRateCmx, 12, 4);
							String sRate = Str.formatAsDouble(dPrice, 12, 4);
							double dMargin = Math.abs(dSpotRateCmx - dPrice)* java.lang.Math.pow(10, 4);
							String sMargin = Str.formatAsDouble(dMargin, 12, 4);*/
							
							int[] iRounding = new int[3];
							int iRateDigits;
							
							iRounding = funcGetRateDigits(iCcy1, iCcy2);
		            		iRateDigits = iRounding[0];
							
		            		String sQuote = tEstruct.getString("opfPrecioCosto", x);
							double dQuote = Str.strToDouble(sQuote);
							String sRate = tEstruct.getString("opfPrecioCliente", x);
							double dRate  =	Str.strToDouble(sRate);
							
							double dMargin = java.lang.Math.abs(dQuote - dRate) * java.lang.Math.pow(10, iRateDigits);
							String sMargin = Str.formatAsDouble(dMargin, 12, 6);
		
							if(sBuySell.equalsIgnoreCase("V"))
								sBuySell="Sell";
							else
								sBuySell="Buy";
							
							setFXSpotValues(tranFX, tblTranInfo, sReferenceOP, iIntBU,
									iIntPfolio, iExtBU, iExtLE, sCcy1, sCcy2,
									iJBacDate, dAmount, dSpotRate, iNextGBD, iNextGBD2,
									sBuySell, sQuote, sRate, sMargin, iRateDigits);
							
		
							try {
								iRetVal = tranFX.insertByStatus(TRAN_STATUS_ENUM.TRAN_STATUS_PENDING);
								if (iRetVal == 1) {
									setErrorLog("Fx Spot " + "PENDING"+ " Successfully (TranNum: "+ tranFX.getTranNum() + ")", "OK",iCMX_Deal_Num, tranFX.getTranNum());
									tranFX.setField(TRANF_FIELD.TRANF_SETTLE_DATE.toInt(), 0,"", OCalendar.formatDateInt(iNextGBD));
									tranFX.setField(TRANF_FIELD.TRANF_FX_TERM_SETTLE_DATE.toInt(), 0, "", OCalendar.formatDateInt(iNextGBD2));
									iRetVal = tranFX.insertByStatus(TRAN_STATUS_ENUM.TRAN_STATUS_VALIDATED);
								}
								Debug.sleep(_iMilisegundos);
								if (iRetVal != 1) {
									iRetVal = tranFX.insertByStatus(TRAN_STATUS_ENUM.TRAN_STATUS_NEW);
									Debug.sleep(_iMilisegundos);
									if (iRetVal != 1) {
										setErrorLog("Fx Spot Insert Error", "ERROR",iCMX_Deal_Num, 0);
										myResult = false;
									} else {
										setErrorLog("Fx Spot " + "NEW"+ " Successfully (TranNum: "+ tranFX.getTranNum() + ")",	"OK", iCMX_Deal_Num,tranFX.getTranNum());
									}
								} else {
									setErrorLog("Fx Spot " + "VALIDATE"	+ " Successfully (TranNum: "+ tranFX.getTranNum() + ")", "OK",iCMX_Deal_Num, tranFX.getTranNum());
								}
								
								iDealNumToUserTab=tranFX.getTranNum(); //TODO validar valor de Dealtran
								iTranNumToUserTab=tranFX.getTranNum();
								
							} catch (OException e) {
								myResult = false;
								sErrorMessage = e.getMessage();
								setErrorLog("Fx Spot Deal create Error : "+ sErrorMessage, "ERROR", iCMX_Deal_Num,0);
								tblTranInfo.destroy();
								continue; // 1.8
							}
							tblTranInfo.destroy();
					}

				} else if ("A".equalsIgnoreCase(sIndTipoOp)) {// ELSE Anulados
					Transaction iTran = null;
					/* Array */
					int[] iTransaction = new int[3];
					String sCMX_Deal_Num = tEstruct.getString("opfNumeroOpFindur", x);

					iTransaction = funcGetTranNum(sNameNumeroOpCmx,sCMX_Deal_Num);

					int iTranNum = iTransaction[0];
					int iDealNum = iTransaction[1];
					int iTranStatus = iTransaction[2];
					
					iDealNumToUserTab=iDealNum;
					iTranNumToUserTab=iTranNum;

					if (iTranNum != 0) {
						iTran = Transaction.retrieve(iTranNum);
						Log.printMsg(EnumTypeMessage.INFO,"Tran Number Op a Cancelar: " + iTranNum);
						Log.printMsg(EnumTypeMessage.INFO,"Deal Number Op a Cancelar:" + iDealNum);
						Log.printMsg(EnumTypeMessage.INFO,"Tran Status	Op a Cancelar: " + iTranStatus);

						int iRet = iTran.insertByStatus(TRAN_STATUS_ENUM.TRAN_STATUS_CANCELLED);
						// Transaction.cancel(iTranNum, arg1);
						if (iRet == 0) {
							setErrorLog("Fx Spot  Error Cancelling OPS (TranNum: "+ iTranNum + "): ", "ERROR",iCMX_Deal_Num, iTranNum);
							myResult=false;
						} else {
							Log.printMsg(EnumTypeMessage.INFO,"Operation Canceled! : " + iDealNum);
							setErrorLog("Fx Spot Deal Cancelled OK ", "OK",iCMX_Deal_Num, iDealNum);
							myResult=true;
						}
						
					} else {
						Log.printMsg(EnumTypeMessage.INFO,"Can't Cancel Operation asocietad to Numero Operacion CMX: "+ iCMX_Deal_Num);
						setErrorLog("Fx Spot Deal Cancelled Error : Can't Find Operation with :"+ iCMX_Deal_Num, "OK",iCMX_Deal_Num, 0);
						myResult=false;
					}
				}
			}// END FOR
			
			if(myResult==true){
				tblInsertUserData	= Table.tableNew("Insert User Table Log CMX");
				tblUpDateUserData	= Table.tableNew("UpDate User Table Log CMX");
				
				/*-------------------------------------------------------*/
				/* Create this Table for Insert Log in the User Table    */
				/* Table should be use to Update User Table USER_CMX_LOG */
				/*-------------------------------------------------------*/
				tblInsertUserData = Table.tableNew("USER_CMX_Log");
				tblInsertUserData.addCols("S(origen) I(deal_num) I(tran_num) I(id_num) I(version_num) I(cod_error) S(mensaje_error)");
				tblInsertUserData.addCol("fecha_hora", COL_TYPE_ENUM.COL_DATE_TIME);

				tblUpDateUserData = Table.tableNew("USER_CMX_Log");
				tblUpDateUserData.addCols("S(origen) I(deal_num) I(tran_num) I(id_num) I(version_num) I(cod_error) S(mensaje_error)");
				tblUpDateUserData.addCol("fecha_hora", COL_TYPE_ENUM.COL_DATE_TIME);
				
				int[] iTransaction=funcCheckUserData(tEstruct.getString("opfNumeroOpFindur", 1));
//				int iIdNum = 0,version=0;
				iIdNum = iTransaction[0];
				version = iTransaction[1];
				
				if(version>0){
					/**Update the user table.*/
					/*-------------------------------------------------------*/
					/* Set values for UpDate User Data Table 				 */
					/*-------------------------------------------------------*/
					int iNumRow = tblUpDateUserData.addRow();
					tblUpDateUserData.setString("origen", iNumRow, sCMX);
					tblUpDateUserData.setInt("deal_num", iNumRow, iDealNumToUserTab);
					tblUpDateUserData.setInt("tran_num", iNumRow, iTranNumToUserTab);
					tblUpDateUserData.setInt("id_num", iNumRow, Str.strToInt(tEstruct.getString("opfNumeroOpFindur", 1)));
					tblUpDateUserData.setInt("version_num", iNumRow, (++version));
					tblUpDateUserData.setInt("cod_error", iNumRow, 0);
					tblUpDateUserData.setString("mensaje_error", iNumRow, "");
					tblUpDateUserData.setDateTimeByParts("fecha_hora", iNumRow,OCalendar.getServerDate(), Util.timeGetServerTime());
					
					tblUpDateUserData.group("id_num");
        			iRetVal = DBUserTable.update(tblUpDateUserData);
        			if( iRetVal != OLF_RETURN_CODE.OLF_RETURN_SUCCEED.jvsValue())
        				Log.printMsg(EnumTypeMessage.ERROR,	DBUserTable.dbRetrieveErrorInfo(iRetVal, "DBUserTable.update() failed" ));
					
				}else{
					/**Insert in User table*/
					/*-------------------------------------------------------*/
					/* Set values for Insert User Data Table 				 */
					/*-------------------------------------------------------*/
					int iNumRow = tblInsertUserData.addRow();
					tblInsertUserData.setString("origen", iNumRow, sCMX);
					tblInsertUserData.setInt("deal_num", iNumRow, iDealNumToUserTab);
					tblInsertUserData.setInt("tran_num", iNumRow, iTranNumToUserTab);
					tblInsertUserData.setInt("id_num", iNumRow, Str.strToInt(tEstruct.getString("opfNumeroOpFindur", 1)));
					tblInsertUserData.setInt("version_num", iNumRow, (++version));
					tblInsertUserData.setInt("cod_error", iNumRow, 0);
					tblInsertUserData.setString("mensaje_error", iNumRow, "");
					tblInsertUserData.setDateTimeByParts("fecha_hora", iNumRow,OCalendar.getServerDate(), Util.timeGetServerTime());
					
					iRetVal = DBUserTable.insert(tblInsertUserData);
        			if( iRetVal != OLF_RETURN_CODE.OLF_RETURN_SUCCEED.jvsValue())
        				Log.printMsg(EnumTypeMessage.ERROR,	DBUserTable.dbRetrieveErrorInfo(iRetVal, "DBUserTable.update() failed" ));
					
				}
				
				if(tblInsertUserData != Util.NULL_TABLE && Table.isTableValid(tblInsertUserData) == 1) tblInsertUserData.destroy();
				if(tblUpDateUserData != Util.NULL_TABLE && Table.isTableValid(tblUpDateUserData) == 1) tblUpDateUserData.destroy();
			}

		} catch (OException e) {
			myResult = false;
			sErrorMessage = e.getMessage();
		}
		setErrorLog(sErrorMessage, "ERROR ALL", iCMX_Deal_Num, 0);
		return myResult;

	}

	private void loadUserCnfVariableTab() throws OException {

		Table tUserCnfVariablePath = Table.tableNew();
		Table tUserCnfVariableFile = Table.tableNew();

		String sWhat = "variable, valor";
		String sFrom = USER_CONFIGURABLE_VARIABLES;
		String sWhere1 = "proceso = '" + DOC_SCRIPT_NAME
				+ "' And variable = 'path'";
		String sWhere2 = "proceso = '" + DOC_SCRIPT_NAME
				+ "' And variable = 'filename'";
		String sWhere3 = "proceso = '" + DOC_SCRIPT_NAME
				+ "' And (variable <> 'filename') And (variable <> 'path')";
		try {
			DBaseTable.loadFromDbWithSQL(tUserCnfVariablePath, sWhat, sFrom,
					sWhere1);
			DBaseTable.loadFromDbWithSQL(tUserCnfVariableFile, sWhat, sFrom,
					sWhere2);
			DBaseTable.loadFromDbWithSQL(tUserCnfVariables, sWhat, sFrom,
					sWhere3);
		} catch (OException exception) {
			String msg = "Failed to load Configurable Variables User Table from database.";
			Log.printMsg(EnumTypeMessage.ERROR, msg);
			setErrorLog(msg, "ERROR", 0, 0);
			tUserCnfVariableFile.destroy();
			tUserCnfVariablePath.destroy();
			tUserCnfVariables.destroy();
			Util.exitFail();
		}

	}

	private void setErrorLog(String sMessage, String sTipo, int iBac_DealNum,
			int iFindur_DealNum) throws OException {
		String sLogUserTable = "USER_CMX_Error_Log";//"USER_Bac_Turing_Error_Log";
		Table user_mytable;
		int numrow, iRetVal;

		int curr_date = OCalendar.getServerDate();
		int curr_time = Util.timeGetServerTime();

		user_mytable = Table.tableNew();
		// Set the name attribute of the table. This should be the name of the
		// database table
		user_mytable.setTableName(sLogUserTable);

		user_mytable.addCol("fecha", COL_TYPE_ENUM.COL_STRING);
		user_mytable.addCol("hora", COL_TYPE_ENUM.COL_STRING);
		user_mytable.addCol("tipo", COL_TYPE_ENUM.COL_STRING);
		user_mytable.addCol("bac_deal_num", COL_TYPE_ENUM.COL_INT);
		user_mytable.addCol("findur_tran_num", COL_TYPE_ENUM.COL_INT);
		user_mytable.addCol("error_log", COL_TYPE_ENUM.COL_STRING);

		// Insert some data into the table
		ODateTime dt = ODateTime.dtNew();
		dt.setDate(curr_date);
		dt.setTime(curr_time);

		numrow = user_mytable.addRow();
		user_mytable.setString("fecha", numrow, OCalendar.formatDateInt(
				curr_date, DATE_FORMAT.DATE_FORMAT_MINIMAL));
		user_mytable.setString("hora", numrow, Str.substr(Str.dtToString(dt,
				DATE_FORMAT.DATE_FORMAT_MINIMAL.toInt(),
				TIME_FORMAT.TIME_FORMAT_HMS24.toInt()), 8, 8));
		user_mytable.setString("tipo", numrow, sTipo);
		user_mytable.setInt("bac_deal_num", numrow, iBac_DealNum);
		user_mytable.setInt("findur_tran_num", numrow, iFindur_DealNum);
		user_mytable.setString("error_log", numrow, sMessage);

		iRetVal = DBUserTable.insert(user_mytable);
		if (iRetVal != OLF_RETURN_CODE.OLF_RETURN_SUCCEED.toInt()) {
			Log.printMsg(EnumTypeMessage.ERROR,
					DBUserTable.dbRetrieveErrorInfo(iRetVal,
							"DBUserTable.insert() failed"));
		}
	}


	/*****************************************************************************
	 * Name: funcGetTranNum Description: Get Transaction Number
	 * 
	 * @param String
	 *            sTypeName
	 * @param String
	 *            sValue
	 * @return Array integer iTransaction
	 * @throws OException
	 *****************************************************************************/
	private int[] funcGetTranNum(String sTypeName, String sValue)	throws OException {

		Log.printMsg(EnumTypeMessage.INFO, "**** funcGetTranNum ****");

		Table tblTranNum = Util.NULL_TABLE;
		int iTranNum = 0, iTranStatus, iDealNum;
		int[] iTransaction = new int[3];
		/*----------------------------------------------*/
		/* Retrieve TranNum of Tran Info */
		/*----------------------------------------------*/
		tblTranNum = Table.tableNew();
		/*String sSql = "Select ab.tran_num, "
				+ "ab.deal_tracking_num as deal_num, "
				+ "ab.tran_status "
				+ "From ab_tran ab "
				+ "Inner Join ab_tran_info_view abi on ab.tran_num = abi.tran_num "
				+ "Where abi.tran_num in " + "(Select tran_num "
				+ "From ab_tran_info_view " + "Where type_name = '" + sTypeName
				+ "' " + "and value = '" + Str.stripBlanks(sValue) + "') "
				+ "And abi.type_name = '" + sMedioTran + "' "
				+ "and abi.value = '" + sCMX + "' "
				+ "Order By ab.tran_num DESC";*/
		String sSql = "Select ab.tran_num, " +
				"ab.deal_tracking_num as deal_num, " +
				"ab.tran_status " +
				"From ab_tran ab " +
				"Inner Join ab_tran_info_view abi on ab.tran_num = abi.tran_num " +
				"Where abi.tran_num in " +
				    "(Select tran_num " +
				    "From ab_tran_info_view " +
				    "Where type_name = '" + sTypeName + "' " +
				    		"and value = '" + Str.stripBlanks(sValue) + "') " +
				"And abi.type_name = '" + sMedioTran + "' " +
				"and abi.value = '" + sCMX + "' " +
				"Order By ab.tran_num DESC";

		/*
		 * String sSql = "Select tran_num From ab_tran_info_view " +
		 * "Where type_name = '" + sTypeName + "' And value = '" +
		 * Str.stripBlanks(sValue) + "'" + " And type_name = '"+ sMedioTran +
		 * "' and value = '" + sCMX + "'" + " order by tran_num DESC";
		 */

		Log.printMsg(EnumTypeMessage.INFO, "(funcGetTranNum - " + sValue + ")\n" + sSql);
		
		DBaseTable.execISql(tblTranNum, sSql);
		if (tblTranNum.getNumRows() > 0) {
			iTranNum = tblTranNum.getInt("tran_num", 1);
			iDealNum = tblTranNum.getInt("deal_num", 1);
			iTranStatus = tblTranNum.getInt("tran_status", 1);
			iTransaction[0] = iTranNum;
			iTransaction[1] = iDealNum;
			iTransaction[2] = iTranStatus;
		}
		tblTranNum.destroy();

		return iTransaction;
	}
	
	/*****************************************************************************
    Name:           funcCheckUserData
    Description:    Check if this Transaction Number already exist in the User Data Table

     * @param 	String sOpNum - Operation Number
     * @return 	Integer iIdNum
     * @throws 	OException
    *****************************************************************************/
    private int[] funcCheckUserData(String sOpNum) throws OException
	{

    	Log.printMsg(EnumTypeMessage.INFO, "**** funcCheckUserData ****");
		
		Table tblUserData = Util.NULL_TABLE;
		String sQuery;
		int iRetVal, iIdNum = 0,version=1;
		int[] iTransaction = new int[2];
		
		tblUserData = Table.tableNew();

		sQuery = "Select id_num,version_num From USER_CMX_LOG Where id_num =" + Str.strToInt(sOpNum) +
				" and origen = '" + sCMX + "'" +
				" and cod_error = 0";

		Log.printMsg(EnumTypeMessage.INFO, "(funcCheckUserData)\n" + sQuery);

		iRetVal = DBaseTable.execISql(tblUserData, sQuery);
		if (tblUserData.getNumRows() > 0) {
			iIdNum = tblUserData.getInt("id_num", 1);
			version = tblUserData.getInt("version_num", 1);
			
			iTransaction[0] = iIdNum;
			iTransaction[1] = version;
			
		}

		/* Memory Clean Up */
		tblUserData.destroy();

		return iTransaction;

	}

	public int getTemplate() throws OException {

		Table tReturn = Util.NULL_TABLE;
		tReturn = Table.tableNew("Spot_Turing");

		StringBuffer sb = new StringBuffer();

//		sb.append("SELECT ab.tran_num");
//		sb.append(" FROM ab_tran ab, ab_tran_info_view ai ");
//		sb.append(" WHERE a.tran_num = ai.tran_num and ab.reference like '%TURING%' ");
//		sb.append(" AND ab.tran_status = " + TRAN_STATUS_ENUM.TRAN_STATUS_TEMPLATE.toInt());
		
		sb.append("SELECT a.tran_num");
		sb.append(" FROM ab_tran a, ab_tran_info_view ai ");
		sb.append(" WHERE a.tran_num = ai.tran_num");
		sb.append(" AND a.tran_status = " + TRAN_STATUS_ENUM.TRAN_STATUS_TEMPLATE.toInt());
		sb.append(" AND a.tran_type = " + TRAN_TYPE_ENUM.TRAN_TYPE_TRADING.toInt());
		sb.append(" AND a.ins_type = " + INS_TYPE_ENUM.fx_instrument.toInt());
		sb.append(" AND ai.type_name = '" + sMedioTran + "'");
		sb.append(" AND ai.value = '" + sCMX + "'");

		try {

			int iRet = DBaseTable.execISql(tReturn, sb.toString());
			if (iRet != OLF_RETURN_CODE.OLF_RETURN_SUCCEED.toInt()) {
				Log.printMsg(EnumTypeMessage.ERROR,	"Error : Can't retrieve template when reference is TURING");
				Log.markEndScript();
				Util.exitFail();

			} else if (tReturn.getNumRows() <= 0) {
				Log.printMsg(EnumTypeMessage.ERROR,"Error : Don't exists template when reference is TURING");
				Log.markEndScript();
				Util.exitFail();
			}

		} catch (OException exception) {

			Log.printMsg(EnumTypeMessage.ERROR,
					"ExecISql failed: \n " + sb.toString());
			Log.printMsg(EnumTypeMessage.ERROR,
					"Exception: \n " + exception.getMessage());
			tReturn.destroy();
			Util.exitFail();
		}

		return tReturn.getInt(1, 1);
	}

	private void setFXSpotValues(Transaction tranPtr, Table tTranInfo,
			String sReference, int iIntBU, int iIntPfolio, int iExtBU,
			int iExtLE, String sCurr1, String sCurr2, int iTradeDate,
			double dAmount, double dSpotRate, int iSettlDate1, int iSettlDate2,
			String sBuySell, String sQuote, String sRate, String sMargin, int iRateDigits)
			throws OException {

		tranPtr.setField(TRANF_FIELD.TRANF_REFERENCE.toInt(), 0, "", sReference);
		tranPtr.setField(TRANF_FIELD.TRANF_INTERNAL_BUNIT.toInt(), 0, "",Str.intToStr(iIntBU));

		tranPtr.setField(TRANF_FIELD.TRANF_INTERNAL_PORTFOLIO.toInt(), 0, "",Str.intToStr(iIntPfolio));
		
		
//		tranPtr.setField(TRANF_FIELD.TRANF_EXTERNAL_BUNIT.toInt(), 0, "",Str.intToStr(iExtBU));
//		tranPtr.setField(TRANF_FIELD.TRANF_EXTERNAL_BUNIT.toInt(), 0, "",Ref.getName(SHM_USR_TABLES_ENUM.PARTY_TABLE, iExtBU));
		tranPtr.setExternalBunit(iExtBU);
		
		
		
		tranPtr.setField(TRANF_FIELD.TRANF_EXTERNAL_LENTITY.toInt(), 0, "",	Str.intToStr(iExtLE));

		tranPtr.setField(TRANF_FIELD.TRANF_BASE_CURRENCY.toInt(), 0, "", sCurr1);
		tranPtr.setField(TRANF_FIELD.TRANF_BOUGHT_CURRENCY.toInt(), 0, "",sCurr2);

		tranPtr.setField(TRANF_FIELD.TRANF_FX_SPOT_RATE.toInt(), 0, "",	Str.doubleToStr(dSpotRate));
		tranPtr.setField(TRANF_FIELD.TRANF_PRICE.toInt(), 0, "",Str.doubleToStr(dSpotRate));

		tranPtr.setField(TRANF_FIELD.TRANF_FX_DATE.toInt(), 0, "",OCalendar.formatDateInt(iTradeDate));

		tranPtr.setField(TRANF_FIELD.TRANF_BUY_SELL.toInt(), 0, "", sBuySell);

		tranPtr.setField(TRANF_FIELD.TRANF_FX_D_AMT.toInt(), 0, "",Str.doubleToStr(Math.abs(dAmount)));
		tranPtr.setField(TRANF_FIELD.TRANF_CURRENCY_PAIR.toInt(), 0, "", sCurr1	+ "/" + sCurr2);
		
		
		
        
		if(sQuote != sRate)
        {
			tranPtr.setField(TRANF_FIELD.TRANF_FX_INTER_OR_CORP.toInt(), 0, "","Corporate");
			tranPtr.setField(TRANF_FIELD.TRANF_FX_SPOT_QUOTE.toInt(), 0, "",sQuote);
			tranPtr.setField(TRANF_FIELD.TRANF_FX_CORP_MARGIN.toInt(), 0, "",sMargin);
           double  dTempRate = tranPtr.getFieldDouble(TRANF_FIELD.TRANF_FX_SPOT_RATE.toInt(), 0, "");
            if(com.olf.openjvs.Math.round(dTempRate, iRateDigits) != com.olf.openjvs.Math.round(Str.strToDouble(sRate), iRateDigits))
            	tranPtr.setField(TRANF_FIELD.TRANF_FX_CORP_MARGIN.toInt(), 0, "","-" + sMargin);
        }
        else
        {
        	tranPtr.setField(TRANF_FIELD.TRANF_FX_INTER_OR_CORP.toInt(), 0, "","Interbank");
        	tranPtr.setField(TRANF_FIELD.TRANF_FX_SPOT_RATE.toInt(), 0, "",sQuote);
        }
		
		

//		tranPtr.setField(TRANF_FIELD.TRANF_FX_SPOT_QUOTE.toInt(), 0, "", sRate);
//		tranPtr.setField(TRANF_FIELD.TRANF_FX_SPOT_RATE.toInt(), 0, "", sQuote);
//		tranPtr.setField(TRANF_FIELD.TRANF_FX_CORP_MARGIN.toInt(), 0, "",sMargin);

		
		
		Table tTransactionInfo = tranPtr.getTranInfo();
//		tTransactionInfo.viewTable();
		if (tTranInfo != null && Table.isTableValid(tTranInfo) == 1) {
			int iNumInfoFields = tTranInfo.getNumRows();
			for (int j = 1; j <= iNumInfoFields; j++) {
				String sTypeName = tTranInfo.getString("type_name", j);
				String sValue = tTranInfo.getString("value", j);
				tTransactionInfo.setString("Value", tTransactionInfo.unsortedFindString("Type", sTypeName,
								SEARCH_CASE_ENUM.CASE_INSENSITIVE), sValue);
			}
		}

	}
	
	/*****************************************************************************
    Name:           parseMDYYYYtoJDate
    Description:    Convert String date Julian Date, the string should has
                    Date formad MDYY or MMDDYYYY with "-", "/" or "\" separators
 	 * @param	String sDate
     * @return 	int Julian Date
     * @throws 	OException
    *****************************************************************************/
    private int parseMDYYYYtoJDate(String sDate) throws OException
	{
        final int month = 0;
        final int day = 1;
        final int year = 2;
        int result = OCalendar.today();
        String separator = "";

        if(sDate.indexOf("/") > 0)
            separator = "/";
        else if(sDate.indexOf("-") > 0)
            separator = "-";
        else if(sDate.indexOf("\\") > 0)
            separator = "\\";
        else
            return result;

        String subDate[] = sDate.split(separator);
        if(subDate.length != 3)
            return result;

        if((subDate[day].length() != 1 && subDate[day].length() != 2)
                || (subDate[month].length() != 1 && subDate[month].length() != 2)
                || (subDate[year].length() != 2 && subDate[year].length() != 4))
            return result;

        // Fix Month to MM
        if(subDate[month].length() == 1)
            subDate[month] = "0" + subDate[month];

        // Fix Day to DD
        if(subDate[day].length() == 1)
            subDate[day] = "0" + subDate[day];

        // Fix Year to YYYY
        if(subDate[year].length() == 2)
            subDate[year] = "20" + subDate[year];


        // Convert to Julian Date using YYYYMMDD convert function
        result  = OCalendar.convertYYYYMMDDToJd(subDate[year] + subDate[month] + subDate[day]);

        return result;
	}
    
    /*****************************************************************************
    Name:           funcGetRateDigits
    Description:    Format the number of decimals.

     * @param   Integer iCurrency
     * @param   Integer iCurrency2
     * @return 	Integer iRateDigits
     * @throws OException
    *****************************************************************************/
	private int[] funcGetRateDigits(int iCurrency, int iCurrency2) throws OException
	{

//		INCGeneralItau.print_msg("INFO", "**** funcGetRateDigits ****");
		
		@SuppressWarnings("unused")
        int iRetVal, iRateDigits = 0, iCcy1Digits = 0, iCcy2Digits = 0;
		Table tblInsNum = Util.NULL_TABLE;

		/* Array */
		int[] iRounding = new int[3];

		tblInsNum = Table.tableNew();
		String sQuery = "Select ab.ins_num, ab.cflow_type, ab.reference, par.currency, par1.currency as currency2, par.nearby as rate_digits, " +
					"c.round as currency1_digits, c2.round as currency2_digits " +
					"From ab_tran ab " +
					"Left Join parameter par on ab.ins_num = par.ins_num " +
					"Left Join parameter par1 on ab.ins_num = par1.ins_num " +
					"Left Join header hea on ab.ins_num = hea.ins_num, " +
					"currency c, currency c2 " +
					"Where ab.tran_type = " + TRAN_TYPE_ENUM.TRAN_TYPE_HOLDING.jvsValue() +
					" And ab.tran_status = " +  TRAN_STATUS_ENUM.TRAN_STATUS_VALIDATED.jvsValue() +
					" And ab.toolset = " + TOOLSET_ENUM.FX_TOOLSET.jvsValue() +
					" And par.param_seq_num = 0 And par1.param_seq_num = 1" +
					" And c.id_number = " + iCurrency +
					" And c2.id_number = " + iCurrency2 +
					" And ((par.currency = " + iCurrency +
					" And par1.currency = " + iCurrency2 + ")" +
					" Or (par.currency = " + iCurrency2 +
                    " And par1.currency = " + iCurrency + "))" +
					" And hea.portfolio_group_id > 0";

		iRetVal = DBaseTable.execISql(tblInsNum, sQuery);

//		INCGeneralItau.print_msg("DEBUG", "(funcGetInsNum)\n" + sQuery);

		if (tblInsNum.getNumRows() > 0)
		{
			iRateDigits = tblInsNum.getInt("rate_digits", 1);
			iCcy1Digits = tblInsNum.getInt("currency1_digits", 1);
			iCcy2Digits = tblInsNum.getInt("currency2_digits", 1);

			iRounding[0] = iRateDigits;
			iRounding[1] = iCcy1Digits;
			iRounding[2] = iCcy2Digits;
		}
		tblInsNum.destroy();

		return iRounding;
	}

}
