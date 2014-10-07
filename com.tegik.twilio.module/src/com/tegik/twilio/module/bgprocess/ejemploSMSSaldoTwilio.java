// assign the background process to a package that belongs to the 
// main package of the module this custom development belongs to  
package com.tegik.twilio.module.bgprocess;
 
import java.math.BigDecimal;
import java.util.Calendar;
 
import org.hibernate.Criteria;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.plm.Product;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.service.db.DalBaseProcess;
import org.quartz.JobExecutionException;

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.util.Date;
import java.util.Calendar;
import org.apache.commons.io.IOUtils;
import java.util.concurrent.TimeUnit;

import org.openbravo.model.ad.utility.Attachment;
import org.openbravo.model.ad.datamodel.Table;
import org.hibernate.criterion.Expression;
import org.openbravo.dal.core.OBContext;
import org.apache.log4j.Logger;
import org.openbravo.base.provider.OBProvider;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.SmsFactory;
import com.twilio.sdk.resource.instance.Sms;
import com.twilio.sdk.resource.list.SmsList;
import java.util.HashMap;
import java.util.Map;
import com.twilio.sdk.TwilioRestException;

import org.openbravo.model.common.invoice.Invoice
;
public class ejemploSMSSaldoTwilio extends DalBaseProcess {
 
  private ProcessLogger logger;
  private Logger log4j = Logger.getLogger(ejemploSMSSaldoTwilio.class);
  public static final String ACCOUNT_SID = "AC7f4fff039d862a0d254d36a9ad48033e";
  public static final String AUTH_TOKEN = "492910c54b316e8159e225297aa1bc7d";

  public void doExecute(ProcessBundle bundle) throws Exception {
      
    try {
    
	 final OBCriteria<Invoice> listaFacturas = OBDal.getInstance().createCriteria(Invoice.class);
	 listaFacturas.add(Expression.eq(Invoice.PROPERTY_SALESTRANSACTION, true));
	 //listaFacturas.add(Expression.eq(Invoice.PROPERTY_CLIENT, bundle.getContext().getClient()));
	 listaFacturas.add(Expression.gt(Invoice.PROPERTY_OUTSTANDINGAMOUNT, new BigDecimal("0")));
	 
	 BigDecimal total = new BigDecimal("0");
	 for (Invoice factura : listaFacturas.list())
	 {
	    log4j.info(factura.getDocumentNo());
	    log4j.info(factura.getOutstandingAmount().toString());
	    total = total.add(factura.getOutstandingAmount());
	    log4j.info("Total hasta el momento: " + total.toString());
	 }
    
	 TwilioRestClient client = new TwilioRestClient(ACCOUNT_SID, AUTH_TOKEN);
  
	 // Build a filter for the SmsList
	 Map<String, String> params = new HashMap<String, String>();
	 params.put("Body", "Twilio + Openbravo By Tegik. El total de cartera en Openbravo al momento es de: " + total.toString());
	 params.put("To", "+528180291458");
	 params.put("From", "+14695183441");
	    
	 SmsFactory messageFactory = client.getAccount().getSmsFactory();
	 Sms message = messageFactory.create(params);
	 log4j.info(message.getSid());

	}
	catch (TwilioRestException e) {
	    log4j.info(e.getErrorMessage());
	    throw new JobExecutionException(e.getMessage(), e);
	}	
        catch (Exception e)
        {
	    log4j.info("ERROR3");
            log4j.info("Error -- " + e.getMessage());
	    throw new JobExecutionException(e.getMessage(), e);
        }

   
}
}
