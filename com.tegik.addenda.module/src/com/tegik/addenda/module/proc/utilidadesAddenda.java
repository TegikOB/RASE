package com.tegik.addenda.module.proc;

/**
 * @author Carlos Salinas
 */
import java.io.*;
import org.apache.log4j.Logger;
import java.io.StringWriter;
import java.io.PrintWriter;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBCriteria;
import org.hibernate.criterion.Expression;
import com.tegik.addenda.module.*;
import org.openbravo.model.common.businesspartner.BusinessPartner;

public class utilidadesAddenda {
  Invoice factura = null;
  public static final Logger log = Logger.getLogger(utilidadesAddenda.class);
  
  public utilidadesAddenda(String invoiceId)
  {
    this.factura = OBDal.getInstance().get(Invoice.class,invoiceId);
  }
  
  public Invoice getFactura()
  {
    return this.factura;
  }
  
  public String sacaJavaAddenda()
  {
      String javaAddenda = null;
  
      BusinessPartner bp = factura.getBusinessPartner();
      if (bp.getAdnAddenda() == null){
	  return javaAddenda;
      }
      else
      {
	  javaAddenda = bp.getAdnAddenda().getJavaclass();
      }
      
      log.info("CSM // SACAJAVAADDENDA // " + javaAddenda);
      return javaAddenda;
  }
  
  public String sacaPaqueteJavaAddenda()
  {
      String paqueteReturn = sacaJavaAddenda();
      if (paqueteReturn == null)
      {
	return paqueteReturn;
      }
      else
      {
	paqueteReturn = paqueteReturn.substring(0, paqueteReturn.lastIndexOf("."));
	return paqueteReturn;
      }
  }
  
  public Boolean validarJavaAddenda()
  {
	  String javaAddenda = sacaJavaAddenda();
	  if (javaAddenda == null)
	  {
	  return true;
	  }
	  
	  try {
	      Class.forName(sacaJavaAddenda());
	      return true;
	  }
	  catch(Exception e) {
	      return false;
	  }
  }
  
  public String obtenerDatoAddenda(String encabezado, String etiqueta)
  {
      //Trata de obtener el dato directo de la factura      
      final OBCriteria<com.tegik.addenda.module.data.adninvoicevalues> listaValores = OBDal.getInstance().createCriteria(com.tegik.addenda.module.data.adninvoicevalues.class);
	listaValores.add(Expression.eq(com.tegik.addenda.module.data.adninvoicevalues.PROPERTY_INVOICE, factura));
	
      String valorRegreso = null;
      
      int contador = 0;
	      
      for (com.tegik.addenda.module.data.adninvoicevalues valorAddenda : listaValores.list()) {
	    if (valorAddenda.getADNEtiquetas().getEncabezado().equals(encabezado) && valorAddenda.getADNEtiquetas().getEtiqueta().equals(etiqueta))
	    {
		contador ++;
		valorRegreso = valorAddenda.getValor();
	    }
      }
      
      if (contador > 1)
      {
	log.info("Se encontró más de un valor con el encabezado -- " + encabezado + " y la etiqueta -- " + etiqueta + " en los valores por defecto de la factura");
      }
      
      if (contador == 0) 
      {
	  final OBCriteria<com.tegik.addenda.module.data.ADNValues> listaValores2 = OBDal.getInstance().createCriteria(com.tegik.addenda.module.data.ADNValues.class);
	  listaValores2.add(Expression.eq(com.tegik.addenda.module.data.ADNValues.PROPERTY_ADNADDENDA, factura.getBusinessPartner().getAdnAddenda()));
	      
	  for (com.tegik.addenda.module.data.ADNValues valorAddenda : listaValores2.list()) {
		if (valorAddenda.getADNEtiquetas().getEncabezado().equals(encabezado) && valorAddenda.getADNEtiquetas().getEtiqueta().equals(etiqueta))
		{
		    contador ++;
		    valorRegreso = valorAddenda.getValor();
		}
	  }
      }
      
       if (contador > 1)
       {
	  log.info("Se encontró más de un valor con el encabezado -- " + encabezado + " y la etiqueta -- " + etiqueta + " en los valores por defecto de la addenda.");
       }
       
       if (contador == 0)
       {
	  log.info("No se encontró ningún valor con el encabezado -- " + encabezado + " y la etiqueta -- " + etiqueta + " en los valores por defecto de la factura ni de la addenda.");
       }
      
      return valorRegreso;
  }
  
}
