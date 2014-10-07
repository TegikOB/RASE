/*
 ************************************************************************************
 * Copyright (C) 2009-2010 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.idl.proc;

import java.io.File;
import java.sql.BatchUpdateException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.commons.fileupload.FileItem;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.reference.PInstanceProcessData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.idl.IDL_Entities;
import org.openbravo.idl.IDL_Fields;
import org.openbravo.model.ad.access.RoleOrganization;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.plm.Attribute;
import org.openbravo.model.common.plm.AttributeInstance;
import org.openbravo.model.common.plm.AttributeSet;
import org.openbravo.model.common.plm.AttributeSetInstance;
import org.openbravo.model.common.plm.AttributeValue;
import org.openbravo.service.db.CallProcess;

public abstract class IdlService {

  protected ConnectionProvider conn;
  protected VariablesSecureApp vars;
  protected Organization rowOrganization;
  protected Organization rowTransactionalOrg;

  private Map<IdlCacheObject, BaseOBObject> cache;
  public static final int MAX_CACHE_SIZE = 1000;

  private boolean insert = false;

  private int processed;
  private int notprocessed;
  private int rejected;
  private boolean cancelled;

  private Map<String, ReferenceList> reflists;

  public final void init(ConnectionProvider conn, VariablesSecureApp vars) {
    this.conn = conn;
    this.vars = vars;
    // Initialize
    this.rowOrganization = OBContext.getOBContext().getCurrentOrganization();
    this.rowTransactionalOrg = OBContext.getOBContext().getCurrentOrganization();

    reflists = new HashMap<String, ReferenceList>();
    cache = new HashMap<IdlCacheObject, BaseOBObject>();
  }

  public final boolean executeProcess() {

    boolean result;
    FileItem fi = vars.getMultiFile("inpFile");
    insert = vars.commandIn("SAVE");

    if (fi != null && fi.getSize() > 0L) {

      try {
        File fifile = File.createTempFile("fi_", "csv");
        fi.write(fifile);

        // init counters
        processed = 0;
        notprocessed = 0;
        rejected = 0;
        cancelled = false;

        // Save current organization
        Organization oldOrganization = OBContext.getOBContext().getCurrentOrganization();

        boolean exitcode = executeImport(fifile.getPath(), insert);

        // Restore current organization
        OBContext.getOBContext().setCurrentOrganization(oldOrganization);

        if (insert && getRecordsRejected() == 0 && getRecordsNotProcessed() == 0) {
          postProcess();
        }

        // remove temporal file
        fifile.delete();

        if (exitcode) {
          result = true;
        } else {
          System.out.println(Utility.messageBD(conn, "IDL_PROCESS_FAILURE", vars.getLanguage()));
          processed = 0;
          notprocessed = 0;
          rejected = 0;
          result = false;
        }
      } catch (Exception e) {
        System.out.println(e.getMessage());
        e.printStackTrace(System.err);
        processed = 0;
        notprocessed = 0;
        rejected = 0;
        result = false;
      }
    } else {
      System.out.println(Utility.messageBD(conn, "IDL_INPUT_FILE_NOT_FOUND", vars.getLanguage()));
      processed = 0;
      notprocessed = 0;
      rejected = 0;
      result = false;
    }

    for (String s : getResultMessage()) {
      System.out.println(s);
    }
    return result;
  }

  protected abstract boolean executeImport(String filename, boolean insert) throws Exception;

  private String getExceptionMessage(Throwable t) {
    if (t.getCause() instanceof BatchUpdateException
        && ((BatchUpdateException) t.getCause()).getNextException() != null) {
      final BatchUpdateException bue = (BatchUpdateException) t.getCause();
      return bue.getNextException().getMessage();
    }
    return t.getMessage();
  }

  public final int getRecordsProcessed() {
    return processed;
  }

  public final int getRecordsNotProcessed() {
    return notprocessed;
  }

  public final int getRecordsRejected() {
    return rejected;
  }

  public final String[] getResultMessage() {

    ArrayList<String> a = new ArrayList<String>();

    a.add(Utility.messageBD(conn, "IDL_ROWS_READ", vars.getLanguage())
        + Integer.toString(processed + notprocessed + rejected));
    a.add(Utility.messageBD(conn, insert ? "IDL_ROWS_PROCESSED" : "IDL_ROWS_VALIDATED",
        vars.getLanguage())
        + Integer.toString(processed));
    if (notprocessed > 0) {
      a.add(Utility.messageBD(conn, "IDL_ROWS_NOT_PROCESSED", vars.getLanguage())
          + Integer.toString(notprocessed));
    }
    a.add(Utility.messageBD(conn, "IDL_ROWS_REJECTED", vars.getLanguage())
        + Integer.toString(rejected));

    return a.toArray(new String[a.size()]);
  }

  public final void logRecordError(String message, Object... values) {

    if (cancelled) {
      notprocessed++;
    } else {
      StringBuffer st = new StringBuffer();

      for (Object value : values) {
        if (st.length() > 0) {
          st.append('|');
        }
        if (value == null || value.toString().isEmpty()) {
          st.append("<NULL>");
        } else {
          st.append(value.toString());
        }
      }
      System.out.println(st.toString());
      System.out.println(message);
      rejected++;
      if (insert) {
        cancelled = true;
      }
    }
  }

  public void finishRecordProcess(Object... values) throws Exception {

    if (cancelled) {
      notprocessed++;
    } else {
      if (insert) {
        try {
          internalProcess(values);
          processed++;
        } catch (Exception e) {
          OBDal.getInstance().rollbackAndClose();
          // this method already updates the counters and status
          logRecordError(getExceptionMessage(e), values);
          e.printStackTrace(System.err);
        }
      } else {
        processed++;
      }
    }
  }

  public BaseOBObject doInternalProcess(Object... values) throws Exception {
    // intended to be called by the Web service
    return internalProcess(values);
  }

  public void doPostProcess() throws Exception {
    // intended to be called by the Web service
    postProcess();
  }

  // public void setOrganization(Organization org) {
  // this.rowOrganization = org;
  // }
  //
  // public void setTransactionalOrg(Organization org) {
  // this.rowTransactionalOrg = org;
  // }

  public Organization checkOrganization(String entity, String organizationName) {

    if (organizationName == null) {
      return null;
    } else {
      // Check if the Organization exist
      String defaultOrg = searchDefaultValue(entity, "Organization",
          organizationName.isEmpty() ? null : organizationName);
      Organization org = DALUtils.findOrganization(defaultOrg);

      if (org == null) {
        return null;
      } else {
        // Check if has access
        OBCriteria<RoleOrganization> obc = OBDal.getInstance().createCriteria(
            RoleOrganization.class);
        obc.add(Restrictions.eq("role", OBContext.getOBContext().getRole()));
        obc.add(Restrictions.eq("client", OBContext.getOBContext().getCurrentClient()));
        obc.add(Restrictions.eq("organization", org));
        List<RoleOrganization> roleAccessList = obc.list();
        if (roleAccessList != null && roleAccessList.size() > 0) {
          rowOrganization = org;
          return org;
        } else {
          return null;
        }
      }
    }
  }

  public Organization checkTransactionalOrganization(String entity, String organizationName) {

    if (organizationName == null) {
      return null;
    } else {
      // Check if the Organization exist
      String defaultOrg = searchDefaultValue(entity, "TransactionalOrg",
          organizationName.isEmpty() ? null : organizationName);
      Organization org = DALUtils.findOrganization(defaultOrg);

      if (org == null) {
        return null;
      } else {
        // Check if has access
        OBCriteria<RoleOrganization> obc = OBDal.getInstance().createCriteria(
            RoleOrganization.class);
        obc.add(Restrictions.eq("role", OBContext.getOBContext().getRole()));
        obc.add(Restrictions.eq("client", OBContext.getOBContext().getCurrentClient()));
        obc.add(Restrictions.eq("organization", org));
        List<RoleOrganization> roleAccessList = obc.list();
        if (roleAccessList != null && roleAccessList.size() > 0) {
          rowTransactionalOrg = org;
          return org;
        } else {
          return null;
        }
      }
    }
  }

  public <T extends BaseOBObject> T findDALInstanceOrderByOrg(boolean isTrans, Class<T> clazz,
      Value... values) {

    String treeOrgId = (isTrans) ? this.rowTransactionalOrg.getId() : this.rowOrganization.getId();
    Organization org = OBDal.getInstance().get(Organization.class, treeOrgId);
    String clientId = org.getClient().getId();

    String hql = "as dataset where ad_isorgincluded('" + treeOrgId + "',dataset.organization.id,'"
        + clientId + "' ) <> -1 ";
    for (Value value : values) {
      if (value.getValue() == null) {
        hql += " and (dataset." + value.getField() + " is NULL)";
      } else if ("true".equals(value.getValue().toString())
          || "false".equals(value.getValue().toString())) {
        hql += " and (dataset." + value.getField() + " = " + value.getValue() + ")";
      } else {
        hql += " and (dataset." + value.getField() + " = '" + value.getValue() + "')";
      }
    }
    hql += " order by ad_isorgincluded('" + treeOrgId + "',dataset.organization.id,'" + clientId
        + "' ) ";
    OBQuery<T> query = OBDal.getInstance().createQuery(clazz, hql);

    List<T> datasets = query.list();
    if (datasets != null && datasets.size() > 0) {
      return datasets.get(0);
    } else {
      return null;
    }

  }

  public <T extends BaseOBObject> T findDALInstance(boolean isTrans, Class<T> clazz,
      Value... values) {
    IdlCacheObject currObj = new IdlCacheObject(values, clazz.getName());
    if (values.length >= 1) {
      if (cache.containsKey(currObj)) {
        return (T) cache.get(currObj);
      }
    }

    // create an OBCriteria object and add a filter
    final OBCriteria<T> obCriteria = OBDal.getInstance().createCriteria(clazz);

    String treeOrgId = (isTrans) ? this.rowTransactionalOrg.getId() : this.rowOrganization.getId();

    // Only visible organizations
    obCriteria.add(Restrictions.in("organization.id", OBContext.getOBContext()
        .getOrganizationStructureProvider().getNaturalTree(treeOrgId)));
    obCriteria.setFilterOnReadableOrganization(false);

    for (Value value : values) {
      if (value.getValue() == null) {
        obCriteria.add(Restrictions.isNull(value.getField()));
      } else {
        obCriteria.add(Restrictions.eq(value.getField(), value.getValue()));
      }
    }
    obCriteria.setMaxResults(1);
    // perform the actual query returning a typed list
    final List<T> listt = obCriteria.list();

    if (listt != null && listt.size() > 0) {
      if (cache.size() < MAX_CACHE_SIZE) {
        cache.put(currObj, listt.get(0));
      }
      return listt.get(0);
    } else {
      return null;
    }
  }

  public String searchDefaultValue(String entity, String field, String paramDefvalue) {

    if (paramDefvalue == null || paramDefvalue.equals("")) {
      if (field == null) {
        return null;
      } else {
        IDL_Entities selectEntity = findDALInstance(true, IDL_Entities.class, new Value("name",
            entity));

        // Search field for the specific entity
        IDL_Fields selectField = findDALInstance(true, IDL_Fields.class, new Value("name", field),
            new Value("entityRef", selectEntity));

        if (selectField == null || selectField.getDefaultValue() == null
            || selectField.getDefaultValue().equals("")) {

          IDL_Entities asteriskEntity = findDALInstance(true, IDL_Entities.class, new Value("name",
              "All Entities"));
          // Search field in other entities
          selectField = findDALInstance(true, IDL_Fields.class, new Value("name", field),
              new Value("entityRef", asteriskEntity));

          if (selectField == null || selectField.getDefaultValue() == null
              || selectField.getDefaultValue().equals("")) {
            return null;
          } else {
            return selectField.getDefaultValue();
          }
        } else {
          return selectField.getDefaultValue();
        }
      }
    } else {
      return paramDefvalue;
    }

  }

  public String getReferenceValue(String reference, String name) {
    ReferenceList r = reflists.get(reference + "_" + vars.getLanguage());
    if (r == null) {
      r = ReferenceList.getReferenceListByName(reference, vars.getLanguage());
      if (r == null) {
        throw new OBException();
      }
      reflists.put(reference + "_" + vars.getLanguage(), r);
    }
    return r.getReferenceValue(name);
  }

  public void callProcess(String process, String id) throws OBException, ServletException {

    ProcessInstance processInstance = CallProcess.getInstance().call(process, id, null);
    if (processInstance.getErrorMsg() != null && processInstance.getErrorMsg().trim().length() > 0) {
      final PInstanceProcessData pData = new PInstanceProcessData();
      pData.errormsg = processInstance.getErrorMsg();
      pData.result = processInstance.getResult().toString();
      pData.pMsg = processInstance.getRecordID();
      OBError message = Utility.getProcessInstanceMessage(conn, vars,
          new PInstanceProcessData[] { pData });
      throw new OBException(message.getMessage());
    }
  }

  public String Utility_messageBD(String messageid) {
    return Utility.messageBD(conn, messageid, vars.getLanguage());
  }

  public Validator getValidator(String entity) {
    return new Validator(this, entity);
  }

  public Parameter[] getParameters() {
    // this method must be overwritten by classes that extends IdlService for
    return null;
  }

  protected abstract BaseOBObject internalProcess(Object... values) throws Exception;

  protected void postProcess() throws Exception {
  }

  public AttributeSetInstance getAttributeSetInstance(AttributeSet attset, String attributevalue) {

    AttributeSetInstance attsetinst = null;
    if (attset.isSerialNo() || attset.isLot() || attset.isExpirationDate()) {
      attsetinst = OBProvider.getInstance().get(AttributeSetInstance.class);
      attsetinst.setActive(true);
      attsetinst.setOrganization(attset.getOrganization());
      attsetinst.setAttributeSet(attset); // not directly readable
    }

    if (attset.isSerialNo()) {
      attsetinst.setDescription("#" + attributevalue);
      attsetinst.setSerialNo(attributevalue);
      OBDal.getInstance().save(attsetinst);
      OBDal.getInstance().flush();
    } else if (attset.isLot()) {
      attsetinst.setDescription("L" + attributevalue);
      attsetinst.setLotName(attributevalue);
      OBDal.getInstance().save(attsetinst);
      OBDal.getInstance().flush();
    } else if (attset.isExpirationDate()) {
      String dateFormatString = OBPropertiesProvider.getInstance().getOpenbravoProperties()
          .getProperty("dateFormat.java");
      SimpleDateFormat format = new SimpleDateFormat(dateFormatString);
      Date d = null;
      try {
        d = format.parse(attributevalue);
      } catch (ParseException ex) {
        ex.printStackTrace();
      }
      attsetinst.setDescription(attributevalue);
      attsetinst.setExpirationDate(d);
      OBDal.getInstance().save(attsetinst);
      OBDal.getInstance().flush();
    } else if (attset.getAttributeUseList().size() == 1) {

      Attribute att = attset.getAttributeUseList().get(0).getAttribute();

      AttributeValue attvalue = findDALInstance(false, AttributeValue.class, new Value(
          AttributeValue.PROPERTY_ATTRIBUTE, att), new Value("name", attributevalue));

      if (attvalue != null) {
        attsetinst = findDALInstance(false, AttributeSetInstance.class, new Value(
            "attributeSet.id", attset.getId()), new Value("description", attvalue.getName()));

        if (attsetinst == null) {
          attsetinst = OBProvider.getInstance().get(AttributeSetInstance.class);
          attsetinst.setActive(true);
          attsetinst.setOrganization(attset.getOrganization());
          attsetinst.setAttributeSet(attset); // not directly readable
          attsetinst.setDescription(attvalue.getName());
          AttributeInstance attinst = OBProvider.getInstance().get(AttributeInstance.class);
          attinst.setActive(true);
          attinst.setOrganization(attset.getOrganization());
          attinst.setAttributeSetValue(attsetinst);
          attinst.setAttribute(attvalue.getAttribute());
          attinst.setAttributeValue(attvalue);

          attsetinst.getAttributeInstanceList().add(attinst);
          OBDal.getInstance().save(attsetinst);
          OBDal.getInstance().flush();
        }
      }
    }
    return attsetinst;
  }
}
