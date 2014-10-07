/*
 ************************************************************************************
 * Copyright (C) 2009-2010 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package com.tegik.idl.module.proc;


import org.openbravo.idl.proc.Parameter;
import org.openbravo.idl.proc.Validator;
import java.math.BigDecimal;
import java.util.Calendar;

import org.openbravo.utils.*;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.idl.proc.DALUtils;
import org.openbravo.idl.proc.Value;
import org.openbravo.model.ad.access.*;
import org.openbravo.module.idljava.proc.IdlServiceJava;
import org.openbravo.model.common.enterprise.*;
import org.openbravo.model.common.businesspartner.*;
import org.openbravo.model.common.geography.*;

/**
 *
 * @author Malcolm Haslam
 */
public class direccionesProcess extends IdlServiceJava {

    @Override
    public String getEntityName() {
        return "Direcciones Tegik";
    }
    @Override
    public Parameter[] getParameters() {
        return new Parameter[] {
	     new Parameter("calle", Parameter.STRING),
	     new Parameter("numext", Parameter.STRING),
	     new Parameter("numint", Parameter.STRING),
	     new Parameter("col", Parameter.STRING),
	     new Parameter("cp", Parameter.STRING),
	     new Parameter("city", Parameter.STRING),
	     new Parameter("ctry", Parameter.STRING),
	     new Parameter("est", Parameter.STRING),
	     new Parameter("bp", Parameter.STRING),
	     new Parameter("fax", Parameter.STRING),
	     new Parameter("tel", Parameter.STRING),
	     new Parameter("telad", Parameter.STRING),
	     new Parameter("issend", Parameter.STRING),
	     new Parameter("ispay", Parameter.STRING),
	     new Parameter("isfac", Parameter.STRING),
	     new Parameter("isrem", Parameter.STRING)};
    }

    @Override
    protected Object[] validateProcess(Validator validator, String... values) throws Exception {
        return values;
    }

  @Override
  public BaseOBObject internalProcess(Object... values) throws Exception {
    return crearDirecciones((String) values[0], (String) values[1], (String) values[2], (String) values[3], (String) values[4], 
                            (String) values[5], (String) values[6], (String) values[7], (String) values[8], (String) values[9], 
                            (String) values[10], (String) values[11], (String) values[12], (String) values[13], (String) values[14], 
                            (String) values[15]);
  }

  public BaseOBObject crearDirecciones( final String callestr, final String numextstr, final String numintstr, 
					final String colstr, final String cpstr, final String citystr, final String ctrystr, final String eststr, 
					final String bpstr, final String faxstr, final String telstr, final String teladstr, final String issendstr, 
					final String ispaystr, final String isfacstr, final String isremstr) throws Exception {

BusinessPartner bpObject = findDALInstance(false, BusinessPartner.class, new Value("searchKey", bpstr));

    if (bpObject == null) {
	return null;
	//throw new OBException(Utility.messageBD(conn, "No existe el tercero: ", vars.getLanguage()) + bpstr);
    }
Country countryvalue = findDALInstance(false, Country.class, new Value("name", ctrystr));
    if (countryvalue == null) {
	throw new OBException(Utility.messageBD(conn, "No existe el país: ", vars.getLanguage()) + ctrystr);
    }
  
Region regionvalue = findDALInstance(false, Region.class, new Value("country", countryvalue), new Value("name", eststr));
    if (regionvalue == null && !eststr.equals("")) {
	throw new OBException(Utility.messageBD(conn, "No existe el estado: ", vars.getLanguage()) + eststr);
    }
boolean issend = false;
boolean ispay = false;
boolean isfac = false;
boolean isrem = false;

if (issendstr.equals("Y")){
  issend=true;
}
if (ispaystr.equals("Y")){
  ispay=true;
}
if (isfacstr.equals("Y")){
  isfac=true;
}
if (isremstr.equals("Y")){
  isrem=true;
}
    
    
   org.openbravo.model.common.geography.Location location = OBProvider.getInstance().get( org.openbravo.model.common.geography.Location.class);
          location.setOrganization(bpObject.getOrganization());
          location.setActive(true);
          location.setAddressLine1(callestr);
          location.setTdirmNumex(numextstr); 
          location.setTdirmNumin(numintstr);  
          location.setAddressLine2(colstr);
          location.setPostalCode(cpstr);
          location.setCityName(citystr);
          location.setCountry(countryvalue);
          if (regionvalue != null ){
	    location.setRegion(regionvalue);
          }
          OBDal.getInstance().save(location);
          OBDal.getInstance().flush();

   org.openbravo.model.common.businesspartner.Location bpLocation = OBProvider.getInstance().get(
              org.openbravo.model.common.businesspartner.Location.class);
          bpLocation.setOrganization(bpObject.getOrganization());
          bpLocation.setActive(true);
          bpLocation.setBusinessPartner(bpObject);
          bpLocation.setFax(faxstr);
          bpLocation.setPhone(telstr);
          bpLocation.setAlternativePhone(teladstr);
          bpLocation.setLocationAddress(location);
          bpLocation.setShipToAddress(issend);
          bpLocation.setPayFromAddress(ispay);
          bpLocation.setInvoiceToAddress(isfac);
          bpLocation.setRemitToAddress(isrem);
          OBDal.getInstance().save(bpLocation);
          OBDal.getInstance().flush();

    return bpLocation;
  }
}
