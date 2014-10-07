/*
 ************************************************************************************
 * Copyright (C) 2012-2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.mobile.core;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.openbravo.client.kernel.BaseComponent;
import org.openbravo.client.kernel.KernelUtils;

public class LibraryResource extends BaseComponent {

  private static final Logger log = Logger.getLogger(LibraryResource.class);

  private static final StringBuffer ENYO_PROD = new StringBuffer();
  private static final StringBuffer ENYO_DEV = new StringBuffer();
  private static final List<String> EXTERNAL_LIBS = new ArrayList<String>();
  private static final List<String> ENYO_LIBS = new ArrayList<String>();
  private static StringBuffer DEPS_LIST = null;
  private static final String FILE_PATH = "@context@/web/org.openbravo.mobile.core/";

  static {
    String openJs = "document.write('<scr'+'ipt src=\"" + FILE_PATH;
    String closeJs = ".js\"></scri'+'pt>');\n";

    String openCss = "document.write('<link rel=\"stylesheet\" type=\"text/css\" href=\""
        + FILE_PATH;
    String closeCss = ".css\">');\n";

    ENYO_PROD.append(openCss + "build/enyo" + closeCss);
    ENYO_PROD.append(openCss + "build/lib" + closeCss);
    ENYO_PROD.append(openJs + "build/enyo" + closeJs);
    ENYO_PROD.append(openJs + "build/lib" + closeJs);

    ENYO_DEV.append(openJs + "enyo/enyo" + closeJs);

    // Enyo libs
    ENYO_LIBS.add("layout");
    ENYO_LIBS.add("onyx");
    ENYO_LIBS.add("notification");

    // External dependencies
    EXTERNAL_LIBS.add("zepto-1.0"); // required by backbonejs (Router and history management)
    EXTERNAL_LIBS.add("moment-1.7.0");
    EXTERNAL_LIBS.add("underscore-1.3.3");
    EXTERNAL_LIBS.add("backbone-0.9.2");
    EXTERNAL_LIBS.add("bigdecimal-1.0.1");
    EXTERNAL_LIBS.add("core");
    EXTERNAL_LIBS.add("sha1");

  }

  @Override
  public String generate() {

    final boolean isInDevelopment = KernelUtils.getInstance()
        .getModule(MobileCoreConstants.MODULE_JAVAPACKAGE).isInDevelopment();
    final String resourceId = getParameter("_id");

    if ("Enyo".equals(resourceId)) {
      return replaceContext(isInDevelopment ? ENYO_DEV.toString() : ENYO_PROD.toString());
    } else if ("Deps".equals(resourceId)) {
      return getDependencies(isInDevelopment);
    }
    log.error("Unknown resource id " + resourceId);
    return "";
  }

  private String replaceContext(String resource) {
    return resource.replace("@context@",
        ((ServletContext) getParameters().get("_servletContext")).getContextPath());
  }

  private String getDependencies(boolean isInDevelopment) {

    if (DEPS_LIST != null) {
      return DEPS_LIST.toString();
    }

    DEPS_LIST = new StringBuffer();
    final StringBuffer enyoDeps = new StringBuffer();
    final StringBuffer externalDeps = new StringBuffer();

    if (isInDevelopment) {
      for (String enyoLib : ENYO_LIBS) {
        enyoDeps.append("'$lib/" + enyoLib + "',");
      }
    }

    for (String lib : EXTERNAL_LIBS) {
      externalDeps.append("'$lib/vendor/" + lib + (isInDevelopment ? "" : ".min") + ".js',");
    }

    DEPS_LIST.append("enyo.depends(").append(enyoDeps)
        .append(externalDeps.substring(0, externalDeps.length() - 1)).append(");");

    return DEPS_LIST.toString();
  }

  @Override
  public Object getData() {
    return new Object();
  }

  @Override
  public boolean bypassAuthentication() {
    return true;
  }

  public List<String> getAllResourceFiles() {
    List<String> resources = new ArrayList<String>();

    resources.add(replaceContext(FILE_PATH + "build/enyo.css"));
    resources.add(replaceContext(FILE_PATH + "build/lib.css"));
    resources.add(replaceContext(FILE_PATH + "build/enyo.js"));
    resources.add(replaceContext(FILE_PATH + "build/lib.js"));

    for (String lib : EXTERNAL_LIBS) {
      resources.add(replaceContext(FILE_PATH + "lib/vendor/" + lib + ".min.js"));
    }
    return resources;
  }
}
