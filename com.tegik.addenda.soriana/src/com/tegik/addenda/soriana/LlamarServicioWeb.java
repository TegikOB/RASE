package com.tegik.addenda.soriana;

import org.apache.log4j.Logger;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;

import com.tegik.addenda.soriana.sources.ExceptionServicioWeb;
import com.tegik.addenda.soriana.sources.ServicioWeb;

public class LlamarServicioWeb  extends DalBaseProcess {
  
  private static final Logger log = Logger.getLogger(LlamarServicioWeb.class);

  public void doExecute(ProcessBundle bundle) throws Exception {
    
    String InvoiceId = (String) bundle.getParams().get("C_Invoice_ID");
    Invoice factura = OBDal.getInstance().get(Invoice.class, InvoiceId);
       
    OBError msg = new OBError();
    msg.setType("Success"); 
    msg.setMessage("Se ha enviado la factura correctamente");
    
    try{
      ServicioWeb.call(factura);
      msg.setMessage(ServicioWeb.mensajeExito);
      
    } catch (ExceptionServicioWeb e){
      msg.setType("Error");
      msg.setMessage(e.getMessage());
    }
    
    bundle.setResult(msg);
    
  
  }

  

}
