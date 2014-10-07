/*
 ************************************************************************************
 * Copyright (C) 2009-2010 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.idl.form;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.idl.IDL_Entities;
import org.openbravo.idl.IDL_Entities_Trl;
import org.openbravo.idl.proc.IdlIntService;
import org.openbravo.idl.proc.IdlService;
import org.openbravo.xmlEngine.XmlDocument;

public class IDL_import extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  static final int THRESHOLD = 1000;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      printPage(response, vars);

    } else if (vars.commandIn("VALIDATE") || vars.commandIn("SAVE")) {

      OBError myMessage = new OBError();

      String loggingText;
      String errText;

      String entityId = vars.getStringParameter("inpidlentityId");

      OBContext.setAdminMode();
      try {
        final IDL_Entities selectedEntity = OBDal.getInstance().get(IDL_Entities.class, entityId);

        if (selectedEntity == null) {
          // Entity not found
          myMessage.setType("Error");
          myMessage.setTitle(Utility.messageBD(this, "IDL_ERROR", vars.getLanguage()));
          myMessage.setMessage(Utility.messageBD(this, "IDL_ENTITY_NOT_FOUND", vars.getLanguage())
              + "<br>" + entityId);
          loggingText = "";
          errText = "";
        } else {

          // Get selected entity
          IdlService service = IdlIntService.getInstance().getService(selectedEntity.getName());
          service.init(this, vars);

          if (service == null) {
            // Service not found;
            myMessage.setType("Error");
            myMessage.setTitle(Utility.messageBD(this, "IDL_ERROR", vars.getLanguage()));
            myMessage.setMessage(Utility.messageBD(this, "IDL_IMPORT_SERVICE_NOT_FOUND",
                vars.getLanguage())
                + "<br>" + selectedEntity.getName());
            loggingText = "";
            errText = "";
          } else {

            PrintStream oldout = System.out;
            ByteArrayOutputStream newout = new ByteArrayOutputStream();
            System.setOut(new PrintStream(newout));

            PrintStream olderr = System.err;
            ByteArrayOutputStream newerr = new ByteArrayOutputStream();
            System.setErr(new PrintStream(newerr));

            boolean result = service.executeProcess();

            System.out.flush();
            loggingText = newout.toString();
            System.setOut(oldout);

            System.err.flush();
            errText = newerr.toString();
            System.setErr(olderr);

            boolean success = result
                && service.getRecordsRejected() + service.getRecordsNotProcessed() == 0;

            String info = "";
            for (String s : service.getResultMessage()) {
              info += s + "<br>";
            }

            if (success) {
              myMessage.setType("Success");
              myMessage.setTitle(Utility.messageBD(this, "IDL_SUCCESS", vars.getLanguage()));
              myMessage.setMessage(info);
            } else {
              myMessage.setType("Error");
              myMessage.setTitle(Utility.messageBD(this, "IDL_ERROR", vars.getLanguage()));
              myMessage.setMessage(Utility.messageBD(this, "IDL_VIEW_LOG", vars.getLanguage())
                  + "<br>" + info);
            }
          }
        }
      } finally {
        OBContext.restorePreviousMode();
      }

      Pattern pattern = Pattern.compile("(@.*?@)");
      Matcher matcher = pattern.matcher(loggingText);
      if (matcher.find()) {
        for (int i = 0; i < matcher.groupCount(); i++) {
          loggingText = loggingText.replace(
              matcher.group(i).toString(),
              matcher.group(i).replaceAll(
                  "@.*?@",
                  Utility.messageBD(this, matcher.group(i).replace("@", "").toString(),
                      vars.getLanguage())));
        }

      }
      vars.setSessionValue("logMessage", loggingText);
      vars.setSessionValue("errMessage", errText);
      vars.setMessage("IDL_import", myMessage);

      printPageResult(response, vars);
    } else {
      pageError(response);
    }
  }

  void printPageResult(HttpServletResponse response, VariablesSecureApp vars) throws IOException,
      ServletException {
    XmlDocument xmlDocument = null;
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/idl/form/FileImport_Result")
        .createXmlDocument();
    response.setContentType("text/html; charset=UTF-8");

    String strJS = "\n getFrame('appFrame').setProcessingMode('window', false); \n"
        + "getFrame('appFrame').document.getElementById('buttonRefresh').onclick();\n";

    xmlDocument.setParameter("result", strJS);
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPage(HttpServletResponse response, VariablesSecureApp vars) throws IOException,
      ServletException {

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/idl/form/IDL_import")
        .createXmlDocument();

    // Refreshing values
    xmlDocument.setParameter("jsinpidlentityId",
        "var jsinpidlentityId = \"" + vars.getStringParameter("inpidlentityId") + "\";");

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "IDL_import", false, "", "", "", false,
        "ad_forms", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.idl.form.IDL_import");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "IDL_import.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "IDL_import.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("IDL_import");
      vars.removeMessage("IDL_import");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }

      String log = vars.getSessionValue("logMessage");
      vars.removeSessionValue("logMessage");
      xmlDocument.setParameter("inplog", log);

      String err = vars.getSessionValue("errMessage");
      vars.removeSessionValue("errMessage");
      xmlDocument.setParameter("inperr", err);

      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");

      try {
        FieldProvider[] showEntities = FieldProviderFactory.getFieldProviderArray(getEntities()
            .toArray());
        for (int i = 0; i < showEntities.length; i++) {
          FieldProviderFactory.setField(showEntities[i], "name", showEntities[i].getField("name"));
        }

        xmlDocument.setData("reportEntity_IDL", "liststructure", showEntities);

      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      response.setContentType("text/html; charset=UTF-8");
      PrintWriter out = response.getWriter();
      out.println(xmlDocument.print());
      out.close();
    }

  }

  public String getServletInfo() {
    return "Servlet that presents the files-importing process";
    // end of getServletInfo() method
  }

  private List<CEntity> getEntities() {
    List<CEntity> entities = new ArrayList<CEntity>();
    OBContext.setAdminMode();
    try {
      final OBCriteria<IDL_Entities> obc = OBDal.getInstance().createCriteria(IDL_Entities.class);
      obc.add(Expression.ne("name", "All Entities"));
      List<IDL_Entities> iniEntities = obc.list();
      OBCriteria<IDL_Entities_Trl> obctrl = OBDal.getInstance().createCriteria(
          IDL_Entities_Trl.class);
      obctrl.add(Restrictions.eq("language", OBContext.getOBContext().getLanguage()));
      List<IDL_Entities_Trl> trlEntities = obctrl.list();

      for (IDL_Entities entity : iniEntities) {
        boolean found = false;
        CEntity centity = new CEntity();
        centity.setId(entity.getId());
        for (IDL_Entities_Trl trlEntity : trlEntities) {
          if (entity.getId().equals(DalUtil.getId(trlEntity.getEntityRef()))) {
            found = true;
            centity.setName(trlEntity.getName());
          }
        }
        if (!found) {
          centity.setName(entity.getName());
        }
        entities.add(centity);
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return entities;
  }

  public class CEntity {
    String name;
    String id;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getIdentifier() {
      return name;
    }
  }
}
