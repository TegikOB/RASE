/*
 ************************************************************************************
 * Copyright (C) 2012-2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.mobile.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.openbravo.client.kernel.BaseTemplateComponent;
import org.openbravo.client.kernel.KernelUtils;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.client.kernel.StaticResourceComponent;
import org.openbravo.client.kernel.Template;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.module.Module;

/**
 * 
 * @author iperdomo
 */

@RequestScoped
public class MobileCoreApplicationCacheComponent extends BaseTemplateComponent {

  @Inject
  private StaticResourceComponent src;

  private static final String TEMPLATE_ID = "7A911850D921448EA5AC2E6F4D5FDE2D";

  @Inject
  private LibraryResource libraryResource;

  private String version = null;

  @Override
  protected Template getComponentTemplate() {
    return OBDal.getInstance().get(Template.class, TEMPLATE_ID);
  }

  public String getVersion() {

    if (version != null) {
      return version;
    }

    StringBuffer versionString = new StringBuffer();
    final List<Module> modules = KernelUtils.getInstance().getModulesOrderedByDependency();
    for (Module module : modules) {
      versionString.append(module.getVersion());
    }
    version = DigestUtils.md5Hex(versionString.toString());

    return version;
  }

  public String getNetwork() {
    return "*";
  }

  public List<String> getAssets() {
    List<String> assets = new ArrayList<String>();
    String relativePath = "web" + File.separatorChar + "org.openbravo.mobile.core";
    final File directory = new File(RequestContext.getServletContext().getRealPath(
        relativePath + "/assets"));

    final Iterator<File> it = FileUtils.iterateFiles(directory, null, true);
    while (it.hasNext()) {
      final File f = (File) it.next();
      String fileCompletePath = f.getPath();
      assets.add("../../"
          + fileCompletePath.substring(fileCompletePath.indexOf(relativePath)).replace("\\", "/"));
    }
    return assets;
  }

  @Override
  public String getContentType() {
    return "text/cache-manifest";
  }

  @Override
  public String getETag() {
    return UUID.randomUUID().toString();
  }

  @Override
  public boolean isJavaScriptComponent() {
    return false;
  }

  public List<String> getLibraryList() {
    libraryResource.setParameters(getParameters());
    return libraryResource.getAllResourceFiles();
  }

  /**
   * Gets the list of application specific resources to be cached. Override this method if your
   * application has any
   * 
   */
  public List<String> getAppList() {
    return new ArrayList<String>();
  }

  /**
   * Gets the list of images to be cached. Override this method if your application has any
   * 
   */
  public List<String> getImageFileList() {
    return new ArrayList<String>();
  }

  /**
   * Gets the list of CSSs to be cached. Override this method if your application has any
   * 
   */
  public List<String> getcssFileList() {
    return new ArrayList<String>();
  }

  @Override
  public boolean bypassAuthentication() {
    return true;
  }

  public boolean getNotInDevelopment() {
    for (Module module : KernelUtils.getInstance().getModulesOrderedByDependency()) {
      if (module.isInDevelopment()) {
        return false;
      }
    }
    return true;
  }

  public String getAppName() {
    return null;
  }

  public String getGenFileName() {
    Map<String, Object> params = getParameters();
    String filePath = MobileStaticResourceComponent.getStaticResourceFileName();
    if (params.containsKey("_appName")) {
      filePath += (String) params.get("_appName");
    }
    return filePath;
  }
}
