/*
 *  Copyright 2010 BigData.mx
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.tegik.facelectr.ad_actionButton;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import mx.bigdata.sat.cfdi.v32.schema.Comprobante;
import mx.bigdata.sat.cfdi.v32.schema.Comprobante.Addenda;
import mx.bigdata.sat.cfdi.v32.schema.Comprobante.Conceptos;
import mx.bigdata.sat.cfdi.v32.schema.Comprobante.Conceptos.Concepto;
import mx.bigdata.sat.cfdi.v32.schema.Comprobante.Emisor;
import mx.bigdata.sat.cfdi.v32.schema.Comprobante.Impuestos;
import mx.bigdata.sat.cfdi.v32.schema.Comprobante.Impuestos.Retenciones;
import mx.bigdata.sat.cfdi.v32.schema.Comprobante.Impuestos.Retenciones.Retencion;
import mx.bigdata.sat.cfdi.v32.schema.Comprobante.Impuestos.Traslados;
import mx.bigdata.sat.cfdi.v32.schema.Comprobante.Impuestos.Traslados.Traslado;
import mx.bigdata.sat.cfdi.v32.schema.Comprobante.Receptor;
import mx.bigdata.sat.cfdi.v32.schema.ObjectFactory;
import mx.bigdata.sat.cfdi.v32.schema.TInformacionAduanera;
import mx.bigdata.sat.cfdi.v32.schema.TUbicacion;
import mx.bigdata.sat.cfdi.v32.schema.TUbicacionFiscal;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Expression;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.currency.ConversionRate;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceDiscount;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.model.common.invoice.InvoiceTax;
import org.openbravo.model.common.plm.AttributeInstance;
import java.util.Iterator;
//librerias para pedimentos
//import com.tegik.pedimentos.mexico.PedMexInvoiPedim;
//import com.tegik.pedimentos.mexico.PedMexPedimentos;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "emisor", "receptor", "conceptos", "impuestos", "complemento",
    "addenda" })
@XmlRootElement(name = "creadorFacturas")
// @XmlSeeAlso({com.tegik.addenda.estrellaRoja.Addenda.class})
public final class creadorFacturas {

  private static final Logger log = Logger.getLogger(creadorFacturas.class);
  private static final String nullString = null;
  public static Comprobante comp;
  public static String mensajeError;
  public static Boolean hayError;

  public creadorFacturas(String strInvoiceId, String tipoDoc) throws Exception {
    Invoice factura = OBDal.getInstance().get(Invoice.class, strInvoiceId);
    ObjectFactory of = new ObjectFactory();
    this.comp = of.createComprobante();
    this.hayError = false;
    this.mensajeError = null;

    // DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date date = new Date();

    comp.setLugarExpedicion(factura.getOrganization().getOrganizationInformationList().get(0)
        .getTdirmDirfiscal().getCityName()
        + ", "
        + factura.getOrganization().getOrganizationInformationList().get(0).getTdirmDirfiscal()
            .getRegion().getName());

    comp.setVersion("3.2");
    // comp.setNumCtaPago("4567"); //Campo opcional
    // comp.setSerie("Pendiente"); //Como enviar facturas con un numero de serie.
    int i = 0;
    while (!Character.isDigit(factura.getDocumentNo().charAt(i)))
      i++;

    log.info("Numero de caracteres de la factura // " + factura.getDocumentNo().length());
    // log.info("Caracter donde se encontró el primer número // " + i);
    // log.info("Esto es lo que debería tener la serie // " + factura.getDocumentNo().substring(0,
    // i));
    // log.info("Esto es lo que debería tener el folio // " + factura.getDocumentNo().substring(i));

    if (i == factura.getDocumentNo().length() || i == 0) {
      // log.info("No se hará nada para la serie");
      comp.setFolio(factura.getDocumentNo());
    } else {
      // log.info("Se debería crear la serie en el archivo de factura electrónica");
      comp.setSerie(factura.getDocumentNo().substring(0, i));
      comp.setFolio(factura.getDocumentNo().substring(i));
    }
    comp.setFecha(date);
    // comp.setFechaFolioFiscalOrig(date);
    if (factura.getFetFormadepago().equals("1")) {
      comp.setFormaDePago("Pago en una sola exhibición");
    } else if (factura.getFetFormadepago().equals("2")) {
      comp.setFormaDePago("Parcialidad " + factura.getFetParcialidad() + " de "
          + factura.getFetParcialidadtotal());
    }

    String referencia = factura.getBusinessPartner().getReferenceNo();
    if (referencia != null && !referencia.equals("")) {
      comp.setMetodoDePago(factura.getPaymentMethod().getName() + " - " + referencia);
    } else {
      comp.setMetodoDePago(factura.getPaymentMethod().getName());
    }
    // comp.setCondicionesDePago(factura.getPaymentTerms().getName()); //No es obligatorio
    BigDecimal gta = factura.getGrandTotalAmount();
    BigDecimal sla = factura.getSummedLineAmount();

    comp.setSubTotal(sla.abs());
    comp.setTotal(gta.abs());
    if (tipoDoc == "N") {
      if (gta.compareTo(new BigDecimal(0.0)) >= 0)
        comp.setTipoDeComprobante("egreso");
      else
        comp.setTipoDeComprobante("ingreso");
    } else {
      if (gta.compareTo(new BigDecimal(0.0)) >= 0)
        comp.setTipoDeComprobante("ingreso");
      else
        comp.setTipoDeComprobante("egreso");
    }
    comp.setMoneda(factura.getCurrency().getISOCode());

    // Cambiar el tipocambio ponerle un string direto
    final OBCriteria<ConversionRate> rateList = OBDal.getInstance().createCriteria(
        ConversionRate.class);
    rateList.add(Expression.eq(ConversionRate.PROPERTY_TOCURRENCY,
        OBDal.getInstance().get(Currency.class, "130")));
    rateList.add(Expression.eq(ConversionRate.PROPERTY_CURRENCY, factura.getCurrency()));
    rateList.add(Expression.eq(ConversionRate.PROPERTY_VALIDFROMDATE, date));
    rateList.add(Expression.eq(ConversionRate.PROPERTY_VALIDTODATE, date));

    for (ConversionRate CurrencyRate : rateList.list()) {
      comp.setTipoCambio(CurrencyRate.getMultipleRateBy().toString());
    }

    // Saca una lista de todos los descuentos y los suma.
    for (InvoiceDiscount descuento : factura.getInvoiceDiscountList()) {
      // descuento.
    }
    OBContext.setAdminMode(true);

    comp.setEmisor(createEmisor(of, factura));
    comp.setReceptor(createReceptor(of, factura));
    comp.setConceptos(createConceptos(of, factura));
    comp.setImpuestos(createImpuestos(of, factura));

    comp.setComplemento(of.createComprobanteComplemento());

    if (addendasInstaladas()) {
      comp.setAddenda(createAddenda(of, factura));
    }

    OBContext.restorePreviousMode();

    // log.info("ENCABEZADO7");

    // return comp;

  }

  private static Emisor createEmisor(ObjectFactory of, Invoice factura) {
    Emisor emisor = of.createComprobanteEmisor();
    emisor.setNombre(factura.getOrganization().getSocialName());
    emisor.setRfc(factura.getOrganization().getOrganizationInformationList().get(0).getTaxID());
    TUbicacionFiscal uf = of.createTUbicacionFiscal();
    uf.setCalle(factura.getOrganization().getOrganizationInformationList().get(0)
        .getLocationAddress().getAddressLine1());
    uf.setCodigoPostal(factura.getOrganization().getOrganizationInformationList().get(0)
        .getLocationAddress().getPostalCode());
    uf.setColonia(factura.getOrganization().getOrganizationInformationList().get(0)
        .getLocationAddress().getAddressLine2());
    uf.setEstado(factura.getOrganization().getOrganizationInformationList().get(0)
        .getLocationAddress().getRegion().getName());
    uf.setMunicipio(factura.getOrganization().getOrganizationInformationList().get(0)
        .getLocationAddress().getCityName());
    uf.setNoExterior(factura.getOrganization().getOrganizationInformationList().get(0)
        .getLocationAddress().getTdirmNumex());
    uf.setNoInterior(factura.getOrganization().getOrganizationInformationList().get(0)
        .getLocationAddress().getTdirmNumin());
    uf.setPais(factura.getOrganization().getOrganizationInformationList().get(0)
        .getLocationAddress().getCountry().getName());
    emisor.setDomicilioFiscal(uf);

    Comprobante.Emisor.RegimenFiscal re = of.createComprobanteEmisorRegimenFiscal();
    if (factura.getOrganization().getOrganizationInformationList().get(0).isFetPersonamoral()) {
      re.setRegimen("Persona Moral Regimen General");
    } else {
      re.setRegimen("PERSONA FISICA");
    }
    emisor.getRegimenFiscal().add(re);
    return emisor;
  }

  private static Receptor createReceptor(ObjectFactory of, Invoice factura) {
    Receptor receptor = of.createComprobanteReceptor();
    receptor.setNombre(factura.getBusinessPartner().getFetRazonsocial());
    receptor.setRfc(factura.getBusinessPartner().getTaxID());
    TUbicacion uf = of.createTUbicacion();
    uf.setCalle(factura.getPartnerAddress().getLocationAddress().getAddressLine1());
    uf.setCodigoPostal(factura.getPartnerAddress().getLocationAddress().getPostalCode());
    uf.setColonia(factura.getPartnerAddress().getLocationAddress().getAddressLine2());
    uf.setEstado(factura.getPartnerAddress().getLocationAddress().getRegion().getName());
    uf.setMunicipio(factura.getPartnerAddress().getLocationAddress().getCityName());
    uf.setNoExterior(factura.getPartnerAddress().getLocationAddress().getTdirmNumex());
    String numeroInterior = factura.getPartnerAddress().getLocationAddress().getTdirmNumin();
    if (numeroInterior != null) {
      if (!numeroInterior.equals(" ")) {
        uf.setNoInterior(factura.getPartnerAddress().getLocationAddress().getTdirmNumin());
      }
    }
    uf.setPais(factura.getPartnerAddress().getLocationAddress().getCountry().getName());
    receptor.setDomicilio(uf);
    return receptor;
  }

  private static Conceptos createConceptos(ObjectFactory of, Invoice factura) {
    Conceptos cps = of.createComprobanteConceptos();
    List<Concepto> listaConceptos = cps.getConcepto();
    for (InvoiceLine linea : factura.getInvoiceLineList()) {
      if (linea.getProduct() != null || linea.getAccount() != null) {
        Concepto c = of.createComprobanteConceptosConcepto();
        c.setUnidad(linea.getUOM().getName());
        BigDecimal lna = linea.getLineNetAmount().setScale(2, RoundingMode.HALF_UP);
        BigDecimal iqty = linea.getInvoicedQuantity().setScale(2, RoundingMode.HALF_UP);

        if (factura.getGrandTotalAmount().compareTo(new BigDecimal(0.0)) >= 0) {
          c.setImporte(lna);
          c.setCantidad(iqty);
          c.setValorUnitario(linea.getUnitPrice().setScale(2, RoundingMode.HALF_UP));
        } else {
          c.setImporte(lna.negate());
          if (lna.negate().compareTo(new BigDecimal(0.0)) >= 0) {
            c.setCantidad(iqty.abs());
            c.setValorUnitario(linea.getUnitPrice().abs().setScale(2, RoundingMode.HALF_UP));
          } else {
            c.setCantidad(iqty.abs().negate());
            c.setValorUnitario(linea.getUnitPrice().abs().setScale(2, RoundingMode.HALF_UP));
          }
        }
        String descripcionExtra = "";
        if (linea.getDescription() != null) {
          if (!linea.getDescription().equals("CARGO POR USADO")) {
            // log.info(linea.getLineNo().toString() + " -- " + linea.getDescription());
            descripcionExtra = linea.getDescription();
          }

        }

        if (linea.getProduct() != null) {
          if (linea.getClient().isFetUsardescripcion()) {
            c.setDescripcion(linea.getProduct().getDescription() + " " + descripcionExtra);
          } else {
            c.setDescripcion(linea.getProduct().getName() + " " + descripcionExtra);
          }

          c.setDescripcion(c.getDescripcion().trim());

          if (linea.getClient().isFetUsaridentificador()) {
            c.setNoIdentificacion(linea.getProduct().getSearchKey());
          }
        } else {
          c.setDescripcion(linea.getAccount().getName());
        }

        /*****************************************************************
         * CESAR
         ****************************************************************/

        if (pedimentosInstaladas()) {
          try {

            // Se obtiene la clase
            Class obtenerPedimentos = Class
                .forName("com.tegik.pedimentos.mexico.ObtenerPedimentos");
            // Se obtiene el constructor de la clase
            Constructor<Object> ctor = obtenerPedimentos
                .getDeclaredConstructor(org.openbravo.model.common.invoice.InvoiceLine.class);
            // Se crea el objeto instanciando el constructos
            Object instance = ctor.newInstance(linea);
            // Se manda a llamar a un método de la instancia y se castea a string
            List<TInformacionAduanera> listaPedimentos = (List<TInformacionAduanera>) obtenerPedimentos
                .getMethod("getPedimentos").invoke(instance);
            // agregar los pedimentos
            for (TInformacionAduanera pedimento : listaPedimentos) {
              c.getInformacionAduanera().add(pedimento);
            }
          } catch (Exception e) {
            // log.info(e.getMessage());
          }
        }

        // log.info("CSM // PEDIMENTOS // linea.getProduct().getAttributeSet()" +
        // linea.getProduct().getAttributeSet().getName());
        if (linea.getProduct() != null) {
          if (linea.getProduct().getAttributeSet() != null) {
            if (linea.getProduct().getAttributeSet().isFetInfoaduanera()) {
              // log.info("CSM // PEDIMENTOS // " + "SI APLICA INFORMACIÓN ADUANERA");
              if (linea.getAttributeSetValue() != null) {
                // log.info("CSM // PEDIMENTOS // " +
                // "SI HAY INFORMACIÓN DEL ATRIBUTO PARA INFORMACIÓN ADUANERA");
                TInformacionAduanera infoAdu = new TInformacionAduanera();

                final OBCriteria<AttributeInstance> attrList = OBDal.getInstance().createCriteria(
                    AttributeInstance.class);
                attrList.add(Expression.eq(AttributeInstance.PROPERTY_ATTRIBUTESETVALUE,
                    linea.getAttributeSetValue()));

                for (AttributeInstance atributo : attrList.list()) {

                  // log.info("CSM // PEDIMENTOS // atributo.getAttribute().getFetAtributopedimento() "
                  // + atributo.getAttribute().getFetAtributopedimento());
                  // log.info("CSM // PEDIMENTOS // atributo.getValue() " +
                  // atributo.getSearchKey());

                  if (atributo.getAttribute().getFetAtributopedimento().equals("F")) {

                    // log.info("CSM // PEDIMENTOS // atributo.getAttribute().getFetFormatofecha() "
                    // + atributo.getAttribute().getFetFormatofecha());

                    try {
                      DateFormat df = new SimpleDateFormat(atributo.getAttribute()
                          .getFetFormatofecha(), Locale.ENGLISH);
                      Date result = df.parse(atributo.getSearchKey());

                      Calendar calendario = Calendar.getInstance();
                      calendario.setTime(result);

                      // log.info("CSM // PEDIMENTOS // FECHA // c.get(Calendar.YEAR)" +
                      // calendario.get(Calendar.YEAR));
                      // log.info("CSM // PEDIMENTOS // FECHA // c.get(Calendar.MONTH)" +
                      // calendario.get(Calendar.MONTH));
                      // log.info("CSM // PEDIMENTOS // FECHA // c.get(Calendar.DAY)" +
                      // calendario.get(Calendar.DAY_OF_MONTH));
                      // log.info("CSM // PEDIMENTOS // FECHA // result.toString()" +
                      // result.toString());

                      int year, month, day;

                      year = result.getYear() + 1900;
                      // log.info("CSM // PEDIMENTOS // FECHA // year" + Integer.toString(year));
                      month = result.getMonth() + 1;
                      // log.info("CSM // PEDIMENTOS // FECHA // month" + Integer.toString(month));
                      // log.info("CSM // PEDIMENTOS // FECHA // day" + Integer.toString(day));
                      day = result.getDate();

                      // consultar lo siguiente
                      // https://github.com/bigdata-mx/factura-electronica/issues/37
                      DatatypeFactory factory = DatatypeFactory.newInstance();
                      XMLGregorianCalendar fecha = factory.newXMLGregorianCalendar(year, month,
                          day, DatatypeConstants.FIELD_UNDEFINED,
                          DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED,
                          DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED);

                      infoAdu.setFecha(fecha);
                    } catch (Exception excFechaPedimento) {
                      StringWriter wPedmiento = new StringWriter();
                      excFechaPedimento.printStackTrace(new PrintWriter(wPedmiento));
                      String errorPedimento = wPedmiento.toString();
                      // log.info(errorPedimento);

                    }
                  }

                  if (atributo.getAttribute().getFetAtributopedimento().equals("P")) {
                    infoAdu.setNumero(atributo.getSearchKey());
                  }

                  if (atributo.getAttribute().getFetAtributopedimento().equals("A")) {
                    infoAdu.setAduana(atributo.getSearchKey());
                  }

                }

                c.getInformacionAduanera().add(infoAdu);
              }
            }
          }
        }

        listaConceptos.add(c);
      }
    }
    return cps;
  }

  private static Impuestos createImpuestos(ObjectFactory of, Invoice factura) {
    Impuestos imps = of.createComprobanteImpuestos();
    Traslados trs = of.createComprobanteImpuestosTraslados();
    List<Traslado> listaImpuestos = trs.getTraslado();

    for (InvoiceTax lineaImpuesto : factura.getInvoiceTaxList()) {
      // if (lineaImpuesto.getTaxAmount().compareTo(new BigDecimal (0.0)) > 0)
      if ((lineaImpuesto.getTaxAmount().compareTo(new BigDecimal(0.0)) > 0 && factura
          .getSummedLineAmount().compareTo(new BigDecimal(0.0)) > 0)
          || (lineaImpuesto.getTaxAmount().compareTo(new BigDecimal(0.0)) < 0 && factura
              .getSummedLineAmount().compareTo(new BigDecimal(0.0)) < 0)) {
        Traslado t1 = of.createComprobanteImpuestosTrasladosTraslado();
        BigDecimal ta = lineaImpuesto.getTaxAmount();
        // t1.setImporte(lineaImpuesto.getTaxAmount());
        t1.setImporte(ta.abs());
        t1.setImpuesto(lineaImpuesto.getTax().getFetNombrefacturacion());
        t1.setTasa(lineaImpuesto.getTax().getRate());
        if (ta.abs().compareTo(new BigDecimal(0.0)) != 0) {
          listaImpuestos.add(t1);
        }
      }
    }

    Retenciones retenciones = of.createComprobanteImpuestosRetenciones();
    List<Retencion> listaRetenciones = retenciones.getRetencion();
    for (InvoiceTax lineaImpuesto : factura.getInvoiceTaxList()) {
      // if (lineaImpuesto.getTaxAmount().compareTo(new BigDecimal (0.0)) < 0)
      if ((lineaImpuesto.getTaxAmount().compareTo(new BigDecimal(0.0)) < 0 && factura
          .getSummedLineAmount().compareTo(new BigDecimal(0.0)) > 0)
          || (lineaImpuesto.getTaxAmount().compareTo(new BigDecimal(0.0)) > 0 && factura
              .getSummedLineAmount().compareTo(new BigDecimal(0.0)) < 0)) {
        Retencion r1 = of.createComprobanteImpuestosRetencionesRetencion();
        BigDecimal tax = lineaImpuesto.getTaxAmount();
        // r1.setImporte(lineaImpuesto.getTaxAmount());
        r1.setImporte(tax.abs());
        r1.setImpuesto(lineaImpuesto.getTax().getFetNombrefacturacion());
        if (tax.abs().compareTo(new BigDecimal(0.0)) != 0) {
          listaRetenciones.add(r1);
        }
      }
    }
    
    if (listaRetenciones.size() > 0) {
      imps.setRetenciones(retenciones);
      BigDecimal totalRetenidos = new BigDecimal("0");
      for (Iterator iterador = listaRetenciones.listIterator(); iterador.hasNext();) {
	  
	  Retencion retencionUnitaria = (Retencion) iterador.next();
	  totalRetenidos = totalRetenidos.add(retencionUnitaria.getImporte());
      }   
      imps.setTotalImpuestosRetenidos(totalRetenidos);
    }

    if (listaImpuestos.size() > 0) {
      imps.setTraslados(trs);
      BigDecimal totalTraslado = new BigDecimal("0");
      for (Iterator iterador = listaImpuestos.listIterator(); iterador.hasNext();) {
	  
	  Traslado trasladoUnitario = (Traslado) iterador.next();
	  totalTraslado = totalTraslado.add(trasladoUnitario.getImporte());
      }   
      imps.setTotalImpuestosTrasladados(totalTraslado);
    }

    return imps;

  }

  private Addenda createAddenda(ObjectFactory of, Invoice factura) {

    mx.bigdata.sat.cfdi.v32.schema.Comprobante.Addenda addenda = of.createComprobanteAddenda();
    List<Object> objetoAddenda = null;

    try {
      // Class claseAddenda =
      // ClassLoader.getSystemClassLoader().loadClass("com.tegik.addenda.module.proc.manejadorAddenda");
      Class claseAddenda = Class.forName("com.tegik.addenda.module.proc.manejadorAddenda");

      // Se obtiene el constructor de la clase
      Constructor<Object> ctor = claseAddenda.getDeclaredConstructor(String.class, Boolean.class);

      // Se crea el objeto instanciando el constructos
      Object instance = ctor.newInstance(factura.getId(), true);

      // Se manda a llamar a un método de la instancia y se castea a string
      objetoAddenda = (List<Object>) claseAddenda.getMethod("getObjetoAddenda").invoke(instance);
      this.hayError = (Boolean) claseAddenda.getMethod("getHayError").invoke(instance);
      this.mensajeError = (String) claseAddenda.getMethod("getMensajeError").invoke(instance);

      if (!hayError && objetoAddenda != null) {
        for (int pos = 0; pos < objetoAddenda.size(); pos++) {
          // log.info ("ContadorObjetosAddenda // " + pos);
          addenda.getAny().add(objetoAddenda.get(pos));
        }
      }
      // PROPERTY_DMPRODSUBFAMILIA
      // getComprasSubfamilia
      return addenda;
    } catch (Exception e) {
      StringWriter w = new StringWriter();
      e.printStackTrace(new PrintWriter(w));
      String errorAddenda = w.toString();
      // log.info(errorAddenda);
      this.comp = null;
      this.hayError = true;
      this.mensajeError = "Hubo un error al llamar al módulo de addendas, contacte al personal correspondiente";
      return addenda;
    }
  }

  private static Boolean addendasInstaladas() {
    try {
      Class.forName("com.tegik.addenda.module.proc.manejadorAddenda");
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private static Boolean pedimentosInstaladas() {
    try {
      Class.forName("com.tegik.pedimentos.mexico.ObtenerPedimentos");
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public String getMensajeError() {
    return this.mensajeError;
  }

  public Boolean getHayError() {
    return this.hayError;
  }

  public Comprobante getComprobante() {
    return this.comp;
  }

}
