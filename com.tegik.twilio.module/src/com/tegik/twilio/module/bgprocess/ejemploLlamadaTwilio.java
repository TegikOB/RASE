// assign the background process to a package that belongs to the 
// main package of the module this custom development belongs to  
package com.tegik.twilio.module.bgprocess;
 
import java.math.BigDecimal;
import java.util.Calendar;
 
import org.hibernate.Criteria;
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
import com.twilio.sdk.resource.instance.Account;
import com.twilio.sdk.resource.instance.Call;
import com.twilio.sdk.resource.factory.CallFactory;
import java.util.HashMap;
import java.util.Map;
import com.twilio.sdk.TwilioRestException;
 
public class ejemploLlamadaTwilio extends DalBaseProcess {
 
  private ProcessLogger logger;
  private Logger log4j = Logger.getLogger(ejemploLlamadaTwilio.class);
  public static final String ACCOUNT_SID = "AC7f4fff039d862a0d254d36a9ad48033e";
  public static final String AUTH_TOKEN = "492910c54b316e8159e225297aa1bc7d";

  public void doExecute(ProcessBundle bundle) throws Exception {
      
    try {
    
	TwilioRestClient client = new TwilioRestClient(ACCOUNT_SID, AUTH_TOKEN);
  
        Account mainAccount = client.getAccount();
        CallFactory callFactory = mainAccount.getCallFactory();
        Map<String, String> callParams = new HashMap<String, String>();
        callParams.put("To", "+528180291458"); // Replace with your phone number
        callParams.put("From", "+14695183441"); // Replace with a Twilio number
        callParams.put("Url", "http://demo.twilio.com/welcome/voice/");
        // Make the call
        Call call = callFactory.create(callParams);
        // Print the call SID (a 32 digit hex like CA123..)
        log4j.info(call.getSid());
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
