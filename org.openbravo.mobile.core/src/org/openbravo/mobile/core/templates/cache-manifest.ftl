CACHE MANIFEST

NETWORK:
${data.network}

<#if data.notInDevelopment>
# Version: ${data.version}
CACHE:

# Core 3rd party libraries

../../org.openbravo.client.kernel/OBMOBC_Main/Lib?_id=Enyo
../../org.openbravo.client.kernel/OBMOBC_Main/Lib?_id=Deps

<#if data.appName != "">
# Static resources
../../org.openbravo.client.kernel/OBMOBC_Main/StaticResources?_appName=${data.appName}
../../web/js/gen/${data.genFileName}.js
</#if>

<#list data.assets as asset>
${asset}
</#list>

<#list data.libraryList as lib>
${lib}
</#list>


# App specific files
<#list data.appList as app>
${app}
</#list>

# Images
<#list data.imageFileList as imageFile>
${imageFile}
</#list>

# CSS
<#list data.cssFileList as cssFile>
${cssFile}
</#list>

</#if>