/*
 ************************************************************************************
 * Copyright (C) 2011 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 ************************************************************************************
 */

package org.openbravo.utility.zoho.process;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Zip;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.service.db.DalBaseProcess;
import org.openbravo.utility.commons.csv.CSVGenerator;
import org.openbravo.utility.commons.csv.CSVGenerator.BooleanType;
import org.openbravo.utility.zoho.ZohoImport;

public class ImportData extends DalBaseProcess {
  private ProcessLogger logger;
  static final private Logger log4j = Logger.getLogger(ImportData.class);

  @Override
  public void doExecute(ProcessBundle bundle) throws Exception {
    logger = bundle.getLogger();

    try {
      String zohoImportId = (String) bundle.getParams().get("zohoProcess");
      ZohoImport zohoImport = OBDal.getInstance().get(ZohoImport.class, zohoImportId);

      bundle.getProcessId();
      if (!zohoImport.isActive()) {
        throw new OBException("Skipping inactive import rule " + zohoImport.getName());
      }

      if (!zohoImport.getObuzohoConfig().isActive()) {
        throw new OBException("Skipping import rule " + zohoImport.getName()
            + " because its config " + zohoImport.getObuzohoConfig().getName() + " is inactive.");
      }

      File dir = new File(OBPropertiesProvider.getInstance().getOpenbravoProperties()
          .getProperty("source.path")
          + "/zoho-imports");
      dir.mkdir();
      String fileNamePrefix = zohoImport.getName();

      Query q = getQuery(zohoImport);

      logger.logln("Generating csv file");
      CSVGenerator csvgen = new CSVGenerator(dir, fileNamePrefix);
      csvgen.setBooleanType(BooleanType.YES_NO);

      if (zohoImport.getColumnAliases() != null) {
        Map<String, String> aliases = new HashMap<String, String>();
        JSONObject jsonAliases = new JSONObject(zohoImport.getColumnAliases());

        JSONArray aliasNames = jsonAliases.names();
        for (int i = 0; i < aliasNames.length(); i++) {
          String name = aliasNames.getString(i);
          aliases.put(name, jsonAliases.getString(name));
        }
        csvgen.setColumnAliases(aliases);
      }

      File csvFile;
      if (zohoImport.isDatetimeFormat())
        csvFile = csvgen.generateCSV(q, true);
      else
        csvFile = csvgen.generateCSV(q, false);

      Zip.zip(new File[] { csvFile }, csvFile.getAbsolutePath() + ".zip", csvFile.getParentFile()
          .getAbsolutePath());
      logger.logln("CSV file stored in " + csvFile.getAbsolutePath() + ".zip");

      File zippedFile = new File(csvFile.getAbsolutePath() + ".zip");

      logger.logln("Importing to Zoho Reports");
      ZohoIntegration zi = new ZohoIntegration(zohoImport);
      zi.setAdditionalLogger(logger);
      zi.uploadCSV(zippedFile);
      logger.logln("Successfully imported");
      csvFile.delete();
      zippedFile.delete();
    } catch (Exception e) {
      logger.logln("Error importing: " + e.getMessage());
      log4j.error("Error importing to Zoho", e);
      throw e;
    }
  }

  private Query getQuery(ZohoImport zohoImport) {
    logger.logln("Getting hql for csv importation");
    String hql = zohoImport.getHQL();
    Query q = OBDal.getInstance().getSession().createQuery(hql);

    if (hql.contains(":lastSuccessImport")) {

      StringBuilder thql = new StringBuilder();
      thql.append("select r.startTime\n");
      thql.append("from ProcessRequest p, ProcessRun r\n");
      thql.append("where r.processRequest = p\n");
      thql.append("and p.process.id = 'FF8081812E0979D5012E097EC9900017'\n");
      thql.append("and p.obuzohoImport = :zohoImport\n");
      thql.append("and r.status = 'SUC'\n");
      thql.append("order by r.startTime desc\n");
      Query qt = OBDal.getInstance().getSession().createQuery(thql.toString());
      qt.setParameter("zohoImport", zohoImport);
      qt.setMaxResults(1);

      Date lastSuccess;
      if (qt.list().isEmpty()) {
        lastSuccess = new Date(0);
      } else {
        lastSuccess = (Date) qt.list().get(0);
      }
      q.setParameter("lastSuccessImport", lastSuccess);
      logger.logln("HQL contains lastSuccessImport param, setting value " + lastSuccess);
    }
    return q;
  }
}
