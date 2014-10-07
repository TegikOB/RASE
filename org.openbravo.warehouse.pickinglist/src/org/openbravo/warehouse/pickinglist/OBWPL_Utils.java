/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.warehouse.pickinglist;

import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.utility.Sequence;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;

public class OBWPL_Utils {
  /**
   * Returns the DocumentType defined for the Organization (or parent organization tree) and
   * document category.
   * 
   * @param org
   *          the Organization for which the Document Type is defined. The Document Type can belong
   *          to the parent organization tree of the specified Organization.
   * @param docCategory
   *          the document category of the Document Type.
   * @return the Document Type
   */
  public static DocumentType getDocumentType(Organization org, String docCategory) {
    DocumentType outDocType = null;
    Client client = null;

    OBCriteria<DocumentType> obcDoc = OBDal.getInstance().createCriteria(DocumentType.class);
    obcDoc.setFilterOnReadableClients(false);
    obcDoc.setFilterOnReadableOrganization(false);

    if ("0".equals(org.getId())) {
      client = OBContext.getOBContext().getCurrentClient();
      if ("0".equals(client.getId())) {
        return null;
      }
    } else {
      client = org.getClient();
    }
    obcDoc.add(Restrictions.eq(DocumentType.PROPERTY_CLIENT, client));

    obcDoc
        .add(Restrictions.in("organization.id",
            OBContext.getOBContext().getOrganizationStructureProvider(org.getClient().getId())
                .getParentTree(org.getId(), true)));
    obcDoc.add(Restrictions.eq(DocumentType.PROPERTY_DOCUMENTCATEGORY, docCategory));
    obcDoc.addOrderBy(DocumentType.PROPERTY_DEFAULT, false);
    obcDoc.addOrderBy(DocumentType.PROPERTY_ID, false);
    List<DocumentType> docTypeList = obcDoc.list();
    if (docTypeList != null && docTypeList.size() > 0) {
      outDocType = docTypeList.get(0);
    }
    return outDocType;
  }

  /**
   * Returns the next sequence number of the Document Type defined for the Organization and document
   * category. The current number of the sequence is also updated.
   * 
   * @param docType
   *          Document type of the document
   * @return the next sequence number of the Document Type defined for the Organization and document
   *         category. Null if no sequence is found.
   */
  public static String getDocumentNo(DocumentType docType, String tableName) {
    String nextDocNumber = "";
    if (docType != null) {
      Sequence seq = docType.getDocumentSequence();
      if (seq == null && tableName != null) {
        OBCriteria<Sequence> obcSeq = OBDal.getInstance().createCriteria(Sequence.class);
        obcSeq.add(Restrictions.eq(Sequence.PROPERTY_NAME, tableName));
        if (obcSeq != null && obcSeq.list().size() > 0) {
          seq = obcSeq.list().get(0);
        }
      }
      if (seq != null) {
        if (seq.getPrefix() != null)
          nextDocNumber = seq.getPrefix();
        nextDocNumber += seq.getNextAssignedNumber().toString();
        if (seq.getSuffix() != null)
          nextDocNumber += seq.getSuffix();
        seq.setNextAssignedNumber(seq.getNextAssignedNumber() + seq.getIncrementBy());
        OBDal.getInstance().save(seq);
        // OBDal.getInstance().flush();
      }
    }

    return nextDocNumber;
  }

}
