package com.tegik.facelectr.ad_actionButton;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Hashtable;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.security.cert.X509Certificate;

import org.apache.axis.components.net.BooleanHolder;
import org.apache.axis.components.net.DefaultSocketFactory;
import org.apache.axis.components.net.SecureSocketFactory;
import org.apache.axis.components.net.TransportClientProperties;
import org.apache.axis.components.net.TransportClientPropertiesFactory;
import org.apache.axis.utils.Messages;
import org.apache.axis.utils.StringUtils;
import org.apache.axis.utils.XMLUtils;

/**
 * Created by IntelliJ IDEA. User: hari Date: 10/16/12 Time: 5:04 PM To change this template use
 * File | Settings | File Templates.
 */
public class CustomSSLSocketFactory extends DefaultSocketFactory implements SecureSocketFactory {

  public final static String TRUST_STORE = "ssl.trustStore";
  public final static String TRUST_STORE_PASSWORD = "ssl.trustStorePassword";
  public final static String TRUST_STORE_TYPE = "ssl.trustStoreType";
  public final static String TRUST_MANAGER_TYPE = "ssl.trustmanager.type";

  public final static String KEY_STORE = "ssl.keyStore";
  public final static String KEY_STORE_PASSWORD = "ssl.keyStorePassword";
  public final static String KEY_STORE_TYPE = "ssl.keyStoreType";
  public final static String KEY_MANAGER_TYPE = "ssl.keymanager.type";
  public final static String KEY_PASSWORD = "ssl.keyPassword"; // Not present in Specification

  public final static String SECURITY_PROVIDER_CLASS = "security.provider";
  public final static String SECURITY_PROTOCOL = "security.protocol";
  public final static String PROTOCOL_HANDLER_PACKAGES = "java.protocol.handler.pkgs";

  private static Hashtable<Store, SSLSocketFactory> socketFactories = new Hashtable<Store, SSLSocketFactory>();
  private static boolean verifyHostname = true;

  static {
    if (System.getProperty("VerifyHostName") != null
        && "false".equalsIgnoreCase(System.getProperty("VerifyHostName"))) {
      verifyHostname = new Boolean(System.getProperty("VerifyHostName"));
    }

  }

  class Store {

    String keyStore;
    String trustStore;
    String keyClientPasswd;
    String keyStorePasswd;
    String trustStorePasswd;
    String keyStoreType;

    public String getKeyStore() {
      return keyStore;
    }

    public void setKeyStore(String keyStore) {
      this.keyStore = keyStore;
    }

    public String getTrustStore() {
      return trustStore;
    }

    public void setTrustStore(String trustStore) {
      this.trustStore = trustStore;
    }

    public String getKeyClientPasswd() {
      return keyClientPasswd;
    }

    public void setKeyClientPasswd(String keyClientPasswd) {
      this.keyClientPasswd = keyClientPasswd;
    }

    public String getKeyStorePasswd() {
      return keyStorePasswd;
    }

    public void setKeyStorePasswd(String keyStorePasswd) {
      this.keyStorePasswd = keyStorePasswd;
    }

    public String getTrustStorePasswd() {
      return trustStorePasswd;
    }

    public void setTrustStorePasswd(String trustStorePasswd) {
      this.trustStorePasswd = trustStorePasswd;
    }

    public String getKeyStoreType() {
      return keyStoreType;
    }

    public void setKeyStoreType(String keyStoreType) {
      this.keyStoreType = keyStoreType;
    }

    public String getTrustStoreType() {
      return trustStoreType;
    }

    public void setTrustStoreType(String trustStoreType) {
      this.trustStoreType = trustStoreType;
    }

    String trustStoreType;

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;

      Store store = (Store) o;

      if (keyClientPasswd != null ? !keyClientPasswd.equals(store.keyClientPasswd)
          : store.keyClientPasswd != null)
        return false;
      if (keyStore != null ? !keyStore.equals(store.keyStore) : store.keyStore != null)
        return false;
      if (keyStorePasswd != null ? !keyStorePasswd.equals(store.keyStorePasswd)
          : store.keyStorePasswd != null)
        return false;
      if (keyStoreType != null ? !keyStoreType.equals(store.keyStoreType)
          : store.keyStoreType != null)
        return false;
      if (trustStore != null ? !trustStore.equals(store.trustStore) : store.trustStore != null)
        return false;
      if (trustStorePasswd != null ? !trustStorePasswd.equals(store.trustStorePasswd)
          : store.trustStorePasswd != null)
        return false;
      if (trustStoreType != null ? !trustStoreType.equals(store.trustStoreType)
          : store.trustStoreType != null)
        return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = keyStore != null ? keyStore.hashCode() : 0;
      result = 31 * result + (trustStore != null ? trustStore.hashCode() : 0);
      result = 31 * result + (keyClientPasswd != null ? keyClientPasswd.hashCode() : 0);
      result = 31 * result + (keyStorePasswd != null ? keyStorePasswd.hashCode() : 0);
      result = 31 * result + (trustStorePasswd != null ? trustStorePasswd.hashCode() : 0);
      result = 31 * result + (keyStoreType != null ? keyStoreType.hashCode() : 0);
      result = 31 * result + (trustStoreType != null ? trustStoreType.hashCode() : 0);
      return result;
    }
  }

  public CustomSSLSocketFactory(Hashtable attributes) {
    super(attributes);
  }

  public SSLSocketFactory getSSLSocketFactory() throws Exception {

    SSLSocketFactory factory;
    Store store;
    store = load();

    factory = socketFactories.get(store);
    if (factory != null) {
      return factory;
    }

    boolean useTrustStore = false;
    TrustManagerFactory tmf = null;
    KeyStore trustStore = null;

    if (store.getTrustStore() != null) {

      trustStore = KeyStore.getInstance(store.getTrustStoreType());
      String trustStorePasswd = store.getTrustStorePasswd();
      trustStore.load(new FileInputStream(store.getTrustStore()), trustStorePasswd == null ? null
          : trustStorePasswd.toCharArray());

      tmf = TrustManagerFactory.getInstance(System.getProperty(TRUST_MANAGER_TYPE, "SunX509"));
      tmf.init(trustStore);

      useTrustStore = true;
    }

    // Load the client's key store
    boolean useClientKeyStore = false;
    KeyManagerFactory kmf = null;

    if (store.getKeyStore() != null) {

      KeyStore clientKeyStore = KeyStore.getInstance(store.getKeyStoreType());
      String keyStorePasswd = store.getKeyStorePasswd();
      clientKeyStore.load(new FileInputStream(store.getKeyStore()), (keyStorePasswd == null) ? null
          : keyStorePasswd.toCharArray());

      kmf = KeyManagerFactory.getInstance(System.getProperty(KEY_MANAGER_TYPE, "SunX509"));
      String keyPasswd = store.getKeyClientPasswd();
      kmf.init(clientKeyStore, (keyPasswd == null) ? null : keyPasswd.toCharArray());

      useClientKeyStore = true;
    }

    // Create SSL Socket Factory
    SSLContext ctx = SSLContext.getInstance(System.getProperty(SECURITY_PROTOCOL, "TLS"));
    ctx.init(useClientKeyStore ? kmf.getKeyManagers() : null,
        useTrustStore ? tmf.getTrustManagers() : null, new SecureRandom());

    factory = ctx.getSocketFactory();

    if (factory != null && trustStore != null)
      socketFactories.put(store, factory);

    return factory;
  }

  private Store load() {
    Store store = new Store();
    store.setKeyStore((System.getProperty(KEY_STORE)));
    store.setKeyStorePasswd((System.getProperty(KEY_STORE_PASSWORD)));
    store.setKeyClientPasswd((System.getProperty(KEY_PASSWORD, store.getKeyStorePasswd())));
    store.setKeyStoreType((System.getProperty(KEY_STORE_TYPE, "JKS")));
    store.setTrustStore((System.getProperty(TRUST_STORE)));
    store.setTrustStorePasswd((System.getProperty(TRUST_STORE_PASSWORD)));
    store.setTrustStoreType((System.getProperty(TRUST_STORE_TYPE, "JKS")));
    return store;

  }

  public Socket create(String host, int port, StringBuffer otherHeaders, BooleanHolder useFullURL)
      throws Exception {

    SSLSocketFactory sslFactory = getSSLSocketFactory();

    if (port == -1) {
      port = 443;
    }

    TransportClientProperties tcp = TransportClientPropertiesFactory.create("https");

    boolean hostInNonProxyList = isHostInNonProxyList(host, tcp.getNonProxyHosts());

    Socket sslSocket = null;
    if (tcp.getProxyHost().length() == 0 || hostInNonProxyList) {
      // direct SSL connection
      sslSocket = sslFactory.createSocket(host, port);
    } else {

      // Default proxy port is 80, even for https
      int tunnelPort = (tcp.getProxyPort().length() != 0) ? Integer.parseInt(tcp.getProxyPort())
          : 80;
      if (tunnelPort < 0)
        tunnelPort = 80;

      // Create the regular socket connection to the proxy
      Socket tunnel = new Socket(tcp.getProxyHost(), tunnelPort);

      // The tunnel handshake method (condensed and made reflexive)
      OutputStream tunnelOutputStream = tunnel.getOutputStream();
      PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
          tunnelOutputStream)));

      out.print("CONNECT " + host + ":" + port + " HTTP/1.0\r\n" + "User-Agent: AxisClient");
      if (tcp.getProxyUser().length() != 0 && tcp.getProxyPassword().length() != 0) {

        // add basic authentication header for the proxy
        String encodedPassword = XMLUtils.base64encode((tcp.getProxyUser() + ":" + tcp
            .getProxyPassword()).getBytes());

        out.print("\nProxy-Authorization: Basic " + encodedPassword);
      }
      out.print("\nContent-Length: 0");
      out.print("\nPragma: no-cache");
      out.print("\r\n\r\n");
      out.flush();
      InputStream tunnelInputStream = tunnel.getInputStream();

      String replyStr = "";

      // Make sure to read all the response from the proxy to prevent SSL negotiation failure
      // Response message terminated by two sequential newlines
      int newlinesSeen = 0;
      boolean headerDone = false; /* Done on first newline */

      while (newlinesSeen < 2) {
        int i = tunnelInputStream.read();

        if (i < 0) {
          throw new IOException("Unexpected EOF from proxy");
        }
        if (i == '\n') {
          headerDone = true;
          ++newlinesSeen;
        } else if (i != '\r') {
          newlinesSeen = 0;
          if (!headerDone) {
            replyStr += String.valueOf((char) i);
          }
        }
      }
      if (StringUtils.startsWithIgnoreWhitespaces("HTTP/1.0 200", replyStr)
          && StringUtils.startsWithIgnoreWhitespaces("HTTP/1.1 200", replyStr)) {
        throw new IOException(Messages.getMessage("cantTunnel00", new String[] {
            tcp.getProxyHost(), "" + tunnelPort, replyStr }));
      }

      // End of condensed reflective tunnel handshake method
      sslSocket = sslFactory.createSocket(tunnel, host, port, true);
    }

    ((SSLSocket) sslSocket).startHandshake();
    verifyHostname((SSLSocket) sslSocket);
    // }
    return sslSocket;
  }

  /**
   * Describe <code>verifyHostname</code> method here.
   * 
   * @param socket
   *          a <code>SSLSocket</code> value
   * @throws SSLPeerUnverifiedException
   *           If there are problems obtaining the server certificates from the SSL session, or the
   *           server host name does not match with the "Common Name" in the server certificates
   *           SubjectDN.
   * @throws java.net.UnknownHostException
   *           If we are not able to resolve the SSL sessions returned server host name.
   */
  private void verifyHostname(SSLSocket socket) throws SSLPeerUnverifiedException,
      UnknownHostException {
    if (!verifyHostname)
      return;

    SSLSession session = socket.getSession();
    String hostname = session.getPeerHost();
    System.out.println("hostname:" + hostname);
    try {
      InetAddress addr = InetAddress.getByName(hostname);
    } catch (UnknownHostException uhe) {
      throw new UnknownHostException("Could not resolve SSL sessions " + "server hostname: "
          + hostname);
    }

    X509Certificate[] certs = session.getPeerCertificateChain();
    if (certs == null || certs.length == 0)
      throw new SSLPeerUnverifiedException("No server certificates found!");

    // get the servers DN in its string representation
    String dn = certs[0].getSubjectDN().getName();

    // get the common name from the first cert
    String cn = getCN(dn);
    System.out.println("CN:" + cn);
    if (hostname.equalsIgnoreCase(cn)) {
      // if (LOG.isDebugEnabled()) {
      // LOG.debug("Target hostname valid: " + cn);
      // }
    } else {
      throw new SSLPeerUnverifiedException("HTTPS hostname invalid: expected '" + hostname
          + "', received '" + cn + "'");
    }
  }

  /**
   * Parses a X.500 distinguished name for the value of the "Common Name" field. This is done a bit
   * sloppy right now and should probably be done a bit more according to <code>RFC 2253</code>.
   * 
   * @param dn
   *          a X.500 distinguished name.
   * @return the value of the "Common Name" field.
   */
  private String getCN(String dn) {
    int i = 0;
    i = dn.indexOf("CN=");
    if (i == -1) {
      return null;
    }
    // get the remaining DN without CN=
    dn = dn.substring(i + 3);
    // System.out.println("dn=" + dn);
    char[] dncs = dn.toCharArray();
    for (i = 0; i < dncs.length; i++) {
      if (dncs[i] == ',' && i > 0 && dncs[i - 1] != '\\') {
        break;
      }
    }
    return dn.substring(0, i);
  }

}
