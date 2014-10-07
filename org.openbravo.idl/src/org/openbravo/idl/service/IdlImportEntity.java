/*
 ************************************************************************************
 * Copyright (C) 2009-2010 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
*/

package org.openbravo.idl.service;

import org.dom4j.Element;
import org.openbravo.idl.proc.Parameter;

/**
 *
 * @author adrian
 */
public class IdlImportEntity {

    private String organization;
    private String transactionalorg;
    private Object[] values;

    public IdlImportEntity(Element elem, Parameter[] params) {

        organization = getElementText(elem, "Organization");
        transactionalorg = getElementText(elem, "TransactionalOrg");

        values = new Object[params.length];

        for (int i = 0; i < params.length; i++) {
            values[i] = getElementText(elem, params[i].getParameter());
        }
    }

    private static String getElementText(Element e, String child) {

        Element elemchild = e.element(child);
        return elemchild == null || elemchild.getText() == null || elemchild.getText().equals("")
                ? null
                : elemchild.getText();
    }

    public String getOrganization() {
        return organization;
    }

    public String getTransactionalOrg() {
        return transactionalorg;
    }

    public Object[] getValues() {
        return values;
    }
}
