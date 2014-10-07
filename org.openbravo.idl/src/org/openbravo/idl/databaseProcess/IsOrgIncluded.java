package org.openbravo.idl.databaseProcess;

import javax.enterprise.context.ApplicationScoped;

import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;
import org.openbravo.client.kernel.ApplicationInitializer;
import org.openbravo.dal.service.OBDal;

@ApplicationScoped
public class IsOrgIncluded implements ApplicationInitializer {

  public void initialize() {
    OBDal.getInstance().registerSQLFunction("ad_isorgincluded",
        new StandardSQLFunction("ad_isorgincluded", StandardBasicTypes.STRING));
  }
}