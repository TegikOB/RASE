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

/**
 *
 * @author Malcolm Haslam
 */
public class userRolProcess extends IdlServiceJava {

    @Override
    public String getEntityName() {
        return "User Roles";
    }
    @Override
    public Parameter[] getParameters() {
        return new Parameter[] {
            new Parameter("usuario", Parameter.STRING),
	    new Parameter("rol", Parameter.STRING)};
    }

    @Override
    protected Object[] validateProcess(Validator validator, String... values) throws Exception {
        return values;
    }

  @Override
  public BaseOBObject internalProcess(Object... values) throws Exception {
    return crearUserRol((String) values[0], (String) values[1]);
  }

  public BaseOBObject crearUserRol(final String usuariostr, final String rolstr) throws Exception {

  Role rolObject = findDALInstance(false, Role.class, new Value("name", rolstr));

    if (rolObject == null) {
	throw new OBException(Utility.messageBD(conn, "No existe el rol: ", vars.getLanguage()) + rolstr);
    }

    User usrObject = findDALInstance(false, User.class, new Value("username", usuariostr));

    if (usrObject == null) {
	throw new OBException(Utility.messageBD(conn, "No existe el usuario: ", vars.getLanguage()) + usuariostr);
    }
    
   
    UserRoles userRolObject = OBProvider.getInstance().get(UserRoles.class);

    userRolObject.setUserContact(usrObject);
    userRolObject.setRole(rolObject);
    
    OBDal.getInstance().save(userRolObject);
    OBDal.getInstance().flush();
    
    // End process
    OBDal.getInstance().commitAndClose();

    return userRolObject;
  }
}
