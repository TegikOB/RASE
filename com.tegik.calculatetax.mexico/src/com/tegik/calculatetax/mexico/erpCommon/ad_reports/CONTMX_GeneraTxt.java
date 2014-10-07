/*
 * 
 * The contents of this file are subject to the Openbravo Public License Version
 * 1.0 (the "License"), being the Mozilla Public License Version 1.1 with a
 * permitted attribution clause; you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.openbravo.com/legal/license.html Software distributed under the
 * License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing rights and limitations under the License. The Original Code is
 * Openbravo ERP. The Initial Developer of the Original Code is Openbravo SLU All
 * portions are Copyright (C) 2001-2009 Openbravo SLU All Rights Reserved.
 * Contributor(s): ______________________________________.
 */
package com.tegik.calculatetax.mexico.erpCommon.ad_reports;

import java.io.IOException;
import java.util.HashMap;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;

//para imprimir csv
import java.math.BigDecimal;
import java.sql.*;

import com.tegik.calculatetax.mexico.contmx_diot_encab;
import org.openbravo.model.ad.utility.Attachment;
import org.openbravo.model.ad.ui.Tab;

import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.dal.core.OBContext;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.erpCommon.utility.Utility;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.File;
import java.io.StringWriter;
import java.io.PrintWriter;

import java.io.IOException;
import java.io.FileWriter;  
import java.io.BufferedWriter;

/*
   import net.sf.jasperreports.engine.JRException;
   import net.sf.jasperreports.engine.JRExporterParameter;
   import net.sf.jasperreports.engine.JRParameter;
   import net.sf.jasperreports.engine.JasperExportManager;
   import net.sf.jasperreports.engine.JasperFillManager;
   import net.sf.jasperreports.engine.JasperPrint;
   import net.sf.jasperreports.engine.JasperReport;
   import net.sf.jasperreports.engine.exportJRCsvExporter;
 */

public class CONTMX_GeneraTxt extends HttpSecureAppServlet {
	private static final long serialVersionUID = 1L;

	public void init(ServletConfig config) {
		super.init(config);
		boolHist = false;
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
	       ServletException {
		       VariablesSecureApp vars = new VariablesSecureApp(request);

		       if (vars.commandIn("DEFAULT")) {
			       log4j.info("LA~ Obtengo variables");

			       String strDiotEncabId = vars.getStringParameter("inpcontmxDiotEncabId");

			       // construct the reload path so that upon completion of the process
			       // the main editing window is reloaded and the resulting message is
			       // shown
			       String strWindow = vars.getStringParameter("inpwindowId");
			       String strTab = vars.getStringParameter("inpTabId");
			       String strWindowPath = Utility.getTabURL(this, strTab, "R");
			       if (strWindowPath.equals(""))
				       strWindowPath = strDefaultServlet;


			       if (log4j.isDebugEnabled())
				       log4j.debug("strDiotEncabId: " + strDiotEncabId);

			       //Se genera el archivo y se adjunta a los registros
			       OBError myMessage = creaTxt(vars, strDiotEncabId, strTab);
			       // set resulting message and reload main editing window
			       vars.setMessage(strTab, myMessage);
			       printPageClosePopUp(response, vars, strWindowPath);

		       } else
			       pageError(response);
	       }


	public String getServletInfo() {
		return "Servlet that print Movements between warehouses document";
	} // End of getServletInfo() method


	OBError creaTxt(VariablesSecureApp vars, String strDiotEncabId, String strTab) throws IOException, ServletException {
		try
		{
			OBError myMessage = new OBError();
			//Cargas la diot del DAL de Openbravo
			contmx_diot_encab diot = OBDal.getInstance().get(contmx_diot_encab.class,strDiotEncabId);
			Tab tabDiot = OBDal.getInstance().get(Tab.class,strTab);

			String attachpath = OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty("attach.path"); //Attachment path
			String sep = System.getProperty("file.separator"); //File separator

			File archivo = new File(attachpath + sep + tabDiot.getTable().getId() + "-" + strDiotEncabId + sep + diot.getSeq() + "-" + diot.getDatei() + ".txt");
			File path = new File(attachpath + sep + tabDiot.getTable().getId() + "-" + strDiotEncabId + sep);

			log4j.info("LA~ archivo = " + archivo.toString());
			log4j.info("LA~ path = " + path.toString());

			//Checa si el directorio existe antes de guardar el comprobante.
			boolean exists = path.exists();
			if (!exists) {
				// Si no existe, creamos el directorio.
				path.mkdirs();
			}

			try {
				// A partir del objeto File creamos el fichero físicamente
				if (archivo.createNewFile())
					log4j.info("LA~ Archivo creado ");
				else
					log4j.info("LA~ Archivo no creado ");
			} catch (Exception e) {
				log4j.error("LA~ error creando archivo  "+ e);
			}

			//Aqui se ejecuta el copy

			try {
				//Create Connection
				Connection con = null;
				con = getTransactionConnection();
				//jasperPrint = JasperFillManager.fillReport(jasperReport, designParameters, con);
				Statement cmd = con.createStatement();
				String sql = "SELECT coalesce(cd.tercero,'') tercero, coalesce(cd.operacion,'') operacion, coalesce(cd.rfc,'') rfc, coalesce(cd.fiscal,'') fiscal, coalesce(cd.extranjero,'') extranjero, coalesce(cc.countrycode,'') pais, coalesce(cd.nacionalidad,'') nacionalidad, coalesce(cd.valordieseis,0) valordieseis, coalesce(cd.valorquince,0) valorquince, coalesce(cd.montoquince,0)montoquince, coalesce(cd.valoronce,0) valoronce, coalesce(cd.valordiez,0) valordiez, coalesce(cd.montoonce,0) montoonce, coalesce(cd.valorimpquince,0) valorimpquince, coalesce(cd.montoimpquince,0) montoimpquince, coalesce(cd.valorimppnce,0) valorimppnce, coalesce(cd.montoimponce,0) montoimponce, coalesce(cd.valorexcento,0) valorexcento, coalesce(cd.valorcero,0) valorcero, coalesce(cd.valorsiniva,0) valorsiniva, coalesce(cd.ivaretenido,0) ivaretenido, coalesce(cd.ivadevoluciones,0) ivadevoluciones FROM contmx_diot cd  LEFT JOIN c_country cc ON cd.c_country_id = cc.c_country_id where cd.contmx_diot_encab_id ='" + strDiotEncabId + "'";

				ResultSet rs = cmd.executeQuery(sql);

				FileWriter fw =  new FileWriter(archivo);
				BufferedWriter out = new BufferedWriter(fw);

				// Escrivimos sobre el archivo
				while (rs.next()) {


					String tercero = rs.getString("tercero");
					String operacion = rs.getString("operacion");
					String rfc = rs.getString("rfc");
					String fiscal = rs.getString("fiscal");
					String extranjero = rs.getString("extranjero");
					String pais = rs.getString("pais");
					String nacionalidad = rs.getString("nacionalidad");
					BigDecimal valordieseis = rs.getBigDecimal("valordieseis").setScale(2, BigDecimal.ROUND_HALF_UP);
					BigDecimal valorquince = rs.getBigDecimal("valorquince").setScale(2, BigDecimal.ROUND_HALF_UP);
					BigDecimal montoquince = rs.getBigDecimal("montoquince").setScale(2, BigDecimal.ROUND_HALF_UP);
					BigDecimal valoronce = rs.getBigDecimal("valoronce").setScale(2, BigDecimal.ROUND_HALF_UP);
					BigDecimal valordiez = rs.getBigDecimal("valordiez").setScale(2, BigDecimal.ROUND_HALF_UP);
					BigDecimal montoonce = rs.getBigDecimal("montoonce").setScale(2, BigDecimal.ROUND_HALF_UP);
					BigDecimal valorimpquince = rs.getBigDecimal("valorimpquince").setScale(2, BigDecimal.ROUND_HALF_UP);
					BigDecimal montoimpquince = rs.getBigDecimal("montoimpquince").setScale(2, BigDecimal.ROUND_HALF_UP);
					BigDecimal valorimppnce = rs.getBigDecimal("valorimppnce").setScale(2, BigDecimal.ROUND_HALF_UP);
					BigDecimal montoimponce = rs.getBigDecimal("montoimponce").setScale(2, BigDecimal.ROUND_HALF_UP);
					BigDecimal valorexcento = rs.getBigDecimal("valorexcento").setScale(2, BigDecimal.ROUND_HALF_UP);
					BigDecimal valorcero = rs.getBigDecimal("valorcero").setScale(2, BigDecimal.ROUND_HALF_UP);
					BigDecimal valorsiniva = rs.getBigDecimal("valorsiniva").setScale(2, BigDecimal.ROUND_HALF_UP);
					BigDecimal ivaretenido = rs.getBigDecimal("ivaretenido").setScale(2, BigDecimal.ROUND_HALF_UP);
					BigDecimal ivadevoluciones = rs.getBigDecimal("ivadevoluciones").setScale(2, BigDecimal.ROUND_HALF_UP);
			String linea;
log4j.info(tercero);
			if(tercero.equals("04"))  {
			 linea = tercero + "|" + operacion  + "|" + rfc + "|" + fiscal + "|" + extranjero + "|" + pais + "|" + nacionalidad + "|" + valordieseis.intValue() + "|" + valorquince.intValue() + "|" + montoquince.intValue() + "|" + valoronce.intValue() + "|" + valordiez.intValue() + "|" + montoonce.intValue() + "||||||" + valorcero.intValue() + "|" + valorsiniva.intValue() + "|" + ivaretenido.intValue() + "|" + ivadevoluciones.intValue() + "\n";
}
			else 
			linea = tercero + "|" + operacion  + "|" + rfc + "|" + fiscal + "|" + extranjero + "|" + pais + "|" + nacionalidad + "|" + valordieseis.intValue() + "|" + valorquince.intValue() + "|" + montoquince.intValue() + "|" + valoronce.intValue() + "|" + valordiez.intValue() + "|" + montoonce.intValue() + "|" + valorimpquince.intValue() + "|" +  montoimpquince.intValue() + "|" + valorimppnce.intValue() + "|" + montoimponce.intValue() + "|" + valorexcento.intValue() + "|" + valorcero.intValue() + "|" + valorsiniva.intValue() + "|" + ivaretenido.intValue() + "|" + ivadevoluciones.intValue() + "\n";

					out.write(linea);

				}

				//close file and connection
				out.close();
				cmd.close();
				con.close();
			} catch (final Exception e) {
				log4j.error("Error en copy " + e);
			}finally {
				log4j.info("Ejecutando codigo de limpieza ");
			}


			String strSubirArchivo = subirArchivo(vars, strTab, diot, tabDiot);


			//String strTimbrar = "OK";

			String mensaje = strSubirArchivo;


			//RunWS.Run();  //Manda a llamar en la clase RunWS 


			//Mensajes de suceso o error.
			if (strSubirArchivo.equals("OK")){
				myMessage.setMessage("Se ha creado el archivo exitosamente");
				myMessage.setMessage(mensaje);
				myMessage.setType("Success");
				myMessage.setTitle("Titulo");
				return myMessage;
			}
			else {
				myMessage.setMessage(mensaje);
				myMessage.setType("Error");
				myMessage.setTitle("Error en la creación del archivo diot");
				return myMessage;
			}




		}
		catch(Exception e){
			StringWriter w = new StringWriter();
			e.printStackTrace(new PrintWriter(w));

			OBError myMessage = new OBError();
			myMessage.setMessage(w.toString());
			myMessage.setType("Error");
			myMessage.setTitle("Error en la creación de la diot");
			return myMessage;
		}
	}


	String subirArchivo(VariablesSecureApp vars, String strTab, contmx_diot_encab diot, Tab tabdiot) throws IOException, ServletException {
		try
		{
			OBContext.setAdminMode(true); // Para poder crear el Attachment

			log4j.info("se guarda en base de datos");

			final Attachment archivoDAL = OBProvider.getInstance().get(Attachment.class);
			//Se agregan las propiedades del attachment
			archivoDAL.setClient(diot.getClient());
			archivoDAL.setOrganization(diot.getOrganization());	
			archivoDAL.setActive(true);
			archivoDAL.setCreationDate(new Date());
			archivoDAL.setUpdated(new Date());
			archivoDAL.setCreatedBy(diot.getCreatedBy());
			archivoDAL.setUpdatedBy(diot.getUpdatedBy());
			archivoDAL.setName(diot.getSeq()  + "-" + diot.getDatei() + ".txt");
			//archivoDAL.setDataType("103");
			long secuencia = 10; // Falta saber como sacar la secuencia correcta, siempre se están subiendo con la secuencia 10.
			archivoDAL.setSequenceNumber(secuencia);
			archivoDAL.setText("Archivo de diot correctamente");
			archivoDAL.setTable(tabdiot.getTable());
			archivoDAL.setRecord(diot.getId());
			//Se guarda el attachment
			OBDal.getInstance().save(archivoDAL); // Guarda el attachment
			OBDal.getInstance().flush();



			return "OK";
		}
		catch(Exception e){

			log4j.error("Subir archivo error = "+ e);
			StringWriter w = new StringWriter();
			e.printStackTrace(new PrintWriter(w));
			return w.toString();
		}
		finally {
			OBContext.restorePreviousMode();
		}
	}
}
