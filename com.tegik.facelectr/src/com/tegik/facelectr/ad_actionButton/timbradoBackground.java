// assign the background process to a package that belongs to the 
// main package of the module this custom development belongs to  
package com.tegik.facelectr.ad_actionButton;

import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.service.db.DalBaseProcess;
import org.quartz.JobExecutionException;
import org.openbravo.erpCommon.utility.OBError;
import org.apache.log4j.Logger;

// the background process needs to extend DalBaseProcess since
// we will be using DAL objects to perform database operations
public class timbradoBackground extends DalBaseProcess {

  private static final Logger log4j = Logger.getLogger(timbradoBackground.class);

  static int counter = 0;

  private ProcessLogger logger;

  // abstract method doExecute needs to be implemented and carries
  // with itself the ProcessBundle object deriving from Openbravo Quartz
  // scheduler
  public void doExecute(ProcessBundle bundle) throws Exception {

    HttpSecureAppServlet serv = new HttpSecureAppServlet();

    logger = bundle.getLogger(); // this logger logs into the LOG column of
    // the AD_PROCESS_RUN database table
    // BigDecimal sumAmount = new BigDecimal(0);

    logger.log("Envío de correos Loop " + counter + "\n");
    log4j.info("Envío de correos Loop " + counter + "\n");

    try {
      final OBCriteria<Invoice> facturasList = OBDal.getInstance().createCriteria(Invoice.class);
      facturasList.add(Expression.isNull(Invoice.PROPERTY_FETDOCSTATUS));

      for (Invoice factura : facturasList.list()) {
          //enviadorCorreos enviador = new enviadorCorreos();
          // ServletConfig config2 = ;
          logger.log("Envío de correos Loop " + counter + factura.getDocumentNo() + "\n");
	  log4j.info("Envío de correos Loop " + counter + factura.getDocumentNo() + "\n");
          //enviador.init(serv.getServletConfig());
          facturaElectronica facturaProcess = new facturaElectronica();
          OBError myMessage = facturaProcess.facturar(null, null, serv.getServletConfig(), bundle.getContext().toVars(), factura.getId(), bundle);
          //String respuestaEnvio = enviador.solicitarEnvio(bundle.getContext().toVars(), factura,"Y", "Y");
          logger.log("Envío de correos Loop " + factura.getDocumentNo() + " // " + myMessage.getMessage() + "\n");
          log4j.info("Envío de correos Loop " + factura.getDocumentNo() + " // " + myMessage.getMessage() + "\n");
          
          counter++;
      }
      
      logger.log("Fin del loop");
      log4j.info("Fin del loop");

    } catch (Exception e) {
      // catch any possible exception and throw it as a Quartz
      // JobExecutionException
      throw new JobExecutionException(e.getMessage(), e);
    }
  }
}