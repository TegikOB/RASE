package com.tegik.addenda.module.proc;

/**
 * @author Carlos Salinas
 */
import java.io.*;
import org.apache.log4j.Logger;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.ArrayList;

public class manejadorAddenda {
  public String mensajeError;
  public Boolean hayError;
  public List<Object> objetoAddenda;
  public String paqueteJavaAddenda;
  public static final Logger log = Logger.getLogger(manejadorAddenda.class);
  public String uri;
  public String prefix;

  public manejadorAddenda(String invoiceId, Boolean generarAddenda) 
  {
	utilidadesAddenda ua = new utilidadesAddenda(invoiceId);
	log.info("MANEJADOR ADDENDA -- INVOICEID -- " + invoiceId);
	String strClaseAddenda = null;
	
	if (ua.validarJavaAddenda())
	{
	    strClaseAddenda = ua.sacaJavaAddenda();
	    this.paqueteJavaAddenda = ua.sacaPaqueteJavaAddenda();
	}
	else
	{
	    this.mensajeError = "No existe una clase de Java para manejar la addenda o la clase de java de la addenda no es válida";
	    this.hayError = true;
	    this.objetoAddenda = null;
	    this.paqueteJavaAddenda = null;
	    return;
	}
	
	if (generarAddenda) 
	{  	
	  try{
		if (strClaseAddenda != null)
		{
		    Class claseAddenda = Class.forName(strClaseAddenda);
		    Constructor<Object> ctor = claseAddenda.getDeclaredConstructor(String.class);
		    Object instance = ctor.newInstance(invoiceId);
		    //Se obtiene el objeto de addenda
		    this.objetoAddenda = (List<Object>) claseAddenda.getMethod("getObjetoAddenda").invoke(instance);
		    this.mensajeError = (String) claseAddenda.getMethod("getMensajeError").invoke(instance);
		    this.hayError = (Boolean) claseAddenda.getMethod("getHayError").invoke(instance);
		}
		else
		{
		    this.objetoAddenda = null;
		    this.mensajeError = null;
		    this.hayError = false;
		}
	  }
	  catch (Exception e)
	  {
		StringWriter w = new StringWriter();
		e.printStackTrace(new PrintWriter(w));
		String errorAddenda = w.toString();
		log.info(errorAddenda);
		
		this.mensajeError = errorAddenda;
		this.hayError = true;
		this.objetoAddenda = null;
		this.paqueteJavaAddenda = null;
	  }
	}
	else
	{	
		try
		{
		    this.mensajeError = "OK";
		    this.hayError = false;
		    this.objetoAddenda = null;;
		}
		catch (Exception e)
		{
		    StringWriter w = new StringWriter();
		    e.printStackTrace(new PrintWriter(w));
		    String errorAddenda = w.toString();
		    log.info(errorAddenda);
		    
		    this.mensajeError = errorAddenda;
		    this.hayError = true;
		    this.objetoAddenda = null;
		    this.paqueteJavaAddenda = null;
		}
	}
  }
  
    public manejadorAddenda()
  {
	  this.mensajeError = null;
	  this.hayError = true;
	  this.objetoAddenda = new ArrayList<Object>();
	  this.paqueteJavaAddenda = null;
  }
  
  public void setMensajeError(String mensajeErrorP) {
	this.mensajeError = mensajeErrorP;
  }
  
  public String getMensajeError() {
	return this.mensajeError;
  }
  
  public void setHayError(Boolean hayErrorP) {
	this.hayError = hayErrorP;
  }
  
  public Boolean getHayError() {
	return this.hayError;
  }
  
  public void setObjetoAddenda(List<Object> objetoAddendaP) {
	this.objetoAddenda = objetoAddendaP;
  }
  
  public List<Object> getObjetoAddenda() {
	return this.objetoAddenda;
  }
  
  public void setPaqueteJavaAddenda(String paqueteJavaAddendaP) {
	this.paqueteJavaAddenda = paqueteJavaAddendaP;
  }
  
  public String getPaqueteJavaAddenda() {
	return this.paqueteJavaAddenda;
  }
    
}
