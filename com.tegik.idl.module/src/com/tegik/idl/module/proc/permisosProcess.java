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
import org.openbravo.model.ad.system.*;
import org.hibernate.criterion.Expression;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.idl.proc.DALUtils;
import org.openbravo.idl.proc.Value;
import org.openbravo.model.ad.access.*;
import org.openbravo.model.ad.ui.*;
import org.openbravo.module.idljava.proc.IdlServiceJava;

/*
/**
 *
 * @author Malcolm Haslam
 */

public class permisosProcess extends IdlServiceJava {

    @Override
    public String getEntityName() {
        return "Permisos Tegik";
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] {
            new Parameter("rol", Parameter.STRING),
            new Parameter("tipo", Parameter.STRING),
            new Parameter("Name", Parameter.STRING),
            new Parameter("Description", Parameter.STRING),
            new Parameter("language", Parameter.STRING),
            new Parameter("escrit", Parameter.STRING)};
          
   }

    @Override
    protected Object[] validateProcess(Validator validator, String... values) throws Exception {
        return values;
    }

  @Override
  public BaseOBObject internalProcess(Object... values) throws Exception {

    return createPermiso((String) values[0], (String) values[1], (String) values[2],
        (String) values[3], (String) values[4], (String) values[5]);
  }

  public BaseOBObject createPermiso(final String strrol, final String strtipo,
      final String strname, final String strdescription,final String strlanguage, final String strescrit)
      throws Exception {
      boolean esescritura = false;
     if (!strescrit.equals("N")){
      if (strescrit.equals("E")){
	esescritura=true;
      }
       Role rol =   findDALInstance(false, Role.class, new Value("name", strrol));
       if (rol == null) {
	  throw new OBException(Utility.messageBD(conn, "No existe el rol " + strrol, vars.getLanguage()));
       }
       if (strtipo.equals("Ventana")){
	   Window win= OBProvider.getInstance().get(Window.class);
            WindowTrl wint=findDALInstance(false, WindowTrl.class, new Value("name", strname)); 
            final OBCriteria<WindowTrl> wintlist = OBDal.getInstance().createCriteria(WindowTrl.class);
            wintlist.add(Expression.eq(WindowTrl.PROPERTY_NAME, strname));
            wintlist.add(Expression.eq(WindowTrl.PROPERTY_LANGUAGE, findDALInstance(false, Language.class, new Value("language", strlanguage))));
            for (WindowTrl wObject : wintlist.list()) {
	      win=wObject.getWindow();
	    }
	    if (win.getName() == null) {
	      throw new OBException(Utility.messageBD(conn, "No existe la ventana " + strname, vars.getLanguage()));
	    }
	
	    final OBCriteria<WindowAccess> walist = OBDal.getInstance().createCriteria(WindowAccess.class);
            walist.add(Expression.eq(WindowAccess.PROPERTY_ROLE, rol));
            walist.add(Expression.eq(WindowAccess.PROPERTY_WINDOW, win));
            if( walist.list().size()==0){
	      WindowAccess wa = OBProvider.getInstance().get(WindowAccess.class);
	      wa.setActive(true);
	      wa.setRole(rol);
	      wa.setWindow(win);
	      wa.setEditableField(esescritura);
	      OBDal.getInstance().save(wa);
	      OBDal.getInstance().flush();
	      // End process
	      OBDal.getInstance().commitAndClose();
	      return wa;
            };
	    
       }else{
	    org.openbravo.model.ad.ui.Process prc=OBProvider.getInstance().get(org.openbravo.model.ad.ui.Process.class);
            ProcessTrl wint=findDALInstance(false, ProcessTrl.class, new Value("name", strname)); 
            final OBCriteria<ProcessTrl> prctlist = OBDal.getInstance().createCriteria(ProcessTrl.class);
            prctlist.add(Expression.eq(ProcessTrl.PROPERTY_NAME, strname));
            prctlist.add(Expression.eq(ProcessTrl.PROPERTY_LANGUAGE, findDALInstance(false, Language.class, new Value("language", strlanguage))));
            for (ProcessTrl pObject : prctlist.list()) {
	      prc=pObject.getProcess();
	    } 
	    if (prc.getName() == null) {
	      throw new OBException(Utility.messageBD(conn, "No existe el reporte/proceso " + strname, vars.getLanguage()));
	    }
	    final OBCriteria<ProcessAccess> palist = OBDal.getInstance().createCriteria(ProcessAccess.class);
            palist.add(Expression.eq(ProcessAccess.PROPERTY_CLIENT, rol.getClient()));
            palist.add(Expression.eq(ProcessAccess.PROPERTY_ROLE, rol));
            palist.add(Expression.eq(ProcessAccess.PROPERTY_PROCESS, prc));
            if( palist.list().size()==0){
	      ProcessAccess pa = OBProvider.getInstance().get(ProcessAccess.class);
	      pa.setActive(true);
	      pa.setRole(rol);
	      pa.setProcess(prc);
	      pa.setEditableField(esescritura);
	      OBDal.getInstance().save(pa);
	      OBDal.getInstance().flush();
	      // End process
	      OBDal.getInstance().commitAndClose();
	      return pa;
            };
	    
       }
      }
      return null;
      }
     }