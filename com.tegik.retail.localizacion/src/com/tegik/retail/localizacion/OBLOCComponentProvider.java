package com.tegik.retail.localizacion;
 
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
 
import javax.enterprise.context.ApplicationScoped;
 
import org.openbravo.client.kernel.BaseComponentProvider;
import org.openbravo.client.kernel.BaseComponentProvider.ComponentResource.ComponentResourceType;
import org.openbravo.client.kernel.Component;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.retail.posterminal.POSUtils;
 
/**
 * @author Tegik
 * 
 */
@ApplicationScoped
@ComponentProvider.Qualifier(OBLOCComponentProvider.QUALIFIER)
public class OBLOCComponentProvider extends BaseComponentProvider {
  public static final String QUALIFIER = "OBLOC_Main";
  public static final String MODULE_JAVA_PACKAGE = "com.tegik.retail.localizacion";
 
  @Override
  public Component getComponent(String componentId, Map<String, Object> parameters) {
    throw new IllegalArgumentException("Component id " + componentId + " not supported.");
  }
 
  @Override
  public List<ComponentResource> getGlobalComponentResources() {
    final List<ComponentResource> globalResources = new ArrayList<ComponentResource>();
    final String prefix = "web/" + MODULE_JAVA_PACKAGE + "/js/model/";
  
    String[] resourceList = { "bplocation-properties","region"};
 
    for (String resource : resourceList) {
      globalResources.add(createComponentResource(ComponentResourceType.Static, prefix + resource
          + ".js", POSUtils.APP_NAME));
    }
	
	globalResources.add(createComponentResource(ComponentResourceType.Static, "web/com.tegik.retail.localizacion/js/windows/editcreatecustomerform-edited.js", POSUtils.APP_NAME));
	
	//globalResources.add(createComponentResource(ComponentResourceType.Static, "web/com.tegik.retail.localizacion/js/orgLoader.js", POSUtils.APP_NAME));
 
    return globalResources;
  }
}
