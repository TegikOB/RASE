/*
 ************************************************************************************
 * Copyright (C) 2009-2010 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.idl.service;

import java.io.StringWriter;
import java.io.Writer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.dom4j.io.SAXReader;
import org.openbravo.base.exception.OBException;
import org.openbravo.service.web.WebService;
import org.openbravo.service.web.WebServiceUtil;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.xml.EntityXMLConverter;
import org.openbravo.dal.xml.XMLConstants;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.idl.proc.IdlIntService;
import org.openbravo.idl.proc.IdlService;
import org.openbravo.idl.proc.Parameter;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.web.ResourceNotFoundException;
import org.dom4j.Document;
import org.dom4j.Element;

/**
 *
 * @author adrian
 */
public class IdlWebService implements WebService {

    // private static Logger log = Logger.getLogger(IdlWebService.class);

    @Override
    public void doPost(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {

        final String firstsegment = WebServiceUtil.getInstance().getFirstSegment(path);
        final VariablesSecureApp vars = new VariablesSecureApp(request);
        final ConnectionProvider conn = new DalConnectionProvider();

        IdlService service = IdlIntService.getInstance().getService(firstsegment);
        if (service == null) {
            throw new ResourceNotFoundException("Data load entity not found: " + firstsegment);
        } else {
            service.init(conn, vars);



            Parameter[] params = service.getParameters();

            // Save current organization
            Organization oldOrganization = OBContext.getOBContext().getCurrentOrganization();

            final SAXReader reader = new SAXReader();
            final Document document = reader.read(request.getInputStream());

            // check that the rootelement is the openbravo one
            final Element rootElement = document.getRootElement();
            if (!rootElement.getName().equals(XMLConstants.OB_ROOT_ELEMENT)) {
              throw new OBException("Root tag of the xml document should be: "
                  + XMLConstants.OB_ROOT_ELEMENT + ", but it is " + rootElement.getName());
            }

            // check that there exit one element to import.
            if (rootElement.elements() == null || rootElement.elements().size() != 1) {
              throw new OBException("Root tag of the xml document should have one child element.");
            }

            Element entityelement = (Element) rootElement.elements().get(0);
            IdlImportEntity impent = new IdlImportEntity(entityelement, params);

            if (impent.getOrganization() != null) {
                service.checkOrganization(firstsegment, impent.getOrganization());
            }

            if (impent.getTransactionalOrg() != null) {
                service.checkTransactionalOrganization(firstsegment, impent.getTransactionalOrg());
            }

            BaseOBObject obj = service.doInternalProcess(impent.getValues());

            // Restore current organization
            OBContext.getOBContext().setCurrentOrganization(oldOrganization);

            service.doPostProcess();

            if (obj == null) {
                throw new OBException("No object returned by process.");
            }

            // Build response

            try {
                final StringWriter sw = new StringWriter();
                final EntityXMLConverter exc = EntityXMLConverter.newInstance();
                exc.setOptionEmbedChildren(true);
                exc.setOptionIncludeChildren(true);
                exc.setOptionIncludeReferenced(false);
                exc.setOptionExportClientOrganizationReferences(true);
                exc.setOutput(sw);
                exc.process(obj);
                String xml = sw.toString();

                response.setContentType("text/xml");
                response.setCharacterEncoding("UTF-8");
                response.setHeader("Content-Encoding", "UTF-8");
                final Writer w = response.getWriter();
                w.write(xml);
                w.close();
            } catch (final Exception e) {
                throw new OBException(e);
            }
        }
    }

    @Override
    public void doPut(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void doDelete(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void doGet(String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
        throw new UnsupportedOperationException();
    }
}
