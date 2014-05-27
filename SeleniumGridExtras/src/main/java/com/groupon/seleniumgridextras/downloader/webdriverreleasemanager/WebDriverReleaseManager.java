package com.groupon.seleniumgridextras.downloader.webdriverreleasemanager;


import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


import org.dom4j.Element;
import org.dom4j.io.SAXReader;


public class WebDriverReleaseManager {

  private static final String WEBDRIVER_JAR = "webdriver-jar";
  private static final String IE_DRIVER = "ie-driver";
  private static final String CHROME_DRIVER = "chrome-driver";
  private WebDriverRelease latestWebdriverVersion;
  private WebDriverRelease latestIEDriverVersion;
  private WebDriverRelease latestChromeDriverVersion;

  private Document parsedXml;
  private static Logger logger = Logger.getLogger(WebDriverReleaseManager.class);


  private Map<String, List<WebDriverRelease>> allProducts;

  private void initialize() {
    allProducts = new HashMap<String, List<WebDriverRelease>>();
    allProducts.put(WEBDRIVER_JAR, new LinkedList<WebDriverRelease>());
    allProducts.put(IE_DRIVER, new LinkedList<WebDriverRelease>());
    allProducts.put(CHROME_DRIVER, new LinkedList<WebDriverRelease>());
  }

  public WebDriverReleaseManager(URL webDriverAndIEDriverURL, URL chromeDriverVersionURL) throws DocumentException {

    System.out.println("Checking the latest version of WebDriver, IEDriver, ChromeDriver from");
    System.out.println(webDriverAndIEDriverURL.toExternalForm());
    System.out.println("and from \n" + chromeDriverVersionURL.toExternalForm());
    initialize();

    SAXReader reader = new SAXReader();
    parsedXml = reader.read(webDriverAndIEDriverURL);
    loadWebDriverAndIEDriverVersions(parsedXml);
    loadChromeDriverVersionFromURL(chromeDriverVersionURL);
  }

  public WebDriverReleaseManager(String webDriverAndIEDriverXml, String chromeDriverVersion)
      throws DocumentException {
    initialize();

    SAXReader reader = new SAXReader();
    parsedXml = reader.read(webDriverAndIEDriverXml);
    loadWebDriverAndIEDriverVersions(parsedXml);
    loadChromeDriverVersion(chromeDriverVersion);
  }


  public int getWebdriverVersionCount() {
    return allProducts.get(WEBDRIVER_JAR).size();
  }

  public int getIEDriverVersionCount() {
    return allProducts.get(IE_DRIVER).size();
  }

  public WebDriverRelease getWedriverLatestVersion() {
    if (this.latestWebdriverVersion == null) {
      this.latestWebdriverVersion = findLatestRelease(allProducts.get(WEBDRIVER_JAR));
    }

    return this.latestWebdriverVersion;
  }

  public WebDriverRelease getIeDriverLatestVersion() {

    if (this.latestIEDriverVersion == null) {
      this.latestIEDriverVersion = findLatestRelease(allProducts.get(IE_DRIVER));
    }

    return this.latestIEDriverVersion;
  }

  public WebDriverRelease getChromeDriverLatestVersion(){
    return this.latestChromeDriverVersion;
  }

  private WebDriverRelease findLatestRelease(List<WebDriverRelease> list) {

    WebDriverRelease highestVersion = null;

    for (WebDriverRelease r : list) {

      if (highestVersion == null) {
        highestVersion = r;
      } else if (r.getComparableVersion() > highestVersion.getComparableVersion()) {
        highestVersion = r;
      }
    }

    return highestVersion;
  }

  public void loadChromeDriverVersionFromURL(URL url){
    InputStream in = null;
    try {
      in = url.openStream();
      loadChromeDriverVersion(IOUtils.toString(in));
    } catch (IOException e) {
      logger.error("Something went wrong when trying to get latest chrome driver version");
      logger.error(e.toString());
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  public void loadChromeDriverVersion(String version){
    this.latestChromeDriverVersion = new ChromeDriverRelease(version);
  }

  public void loadWebDriverAndIEDriverVersions(Document xml) {
    Element root = xml.getRootElement();
    for (Iterator i = root.elementIterator("Contents"); i.hasNext(); ) {
      Element node = (Element) i.next();

      WebDriverRelease release = new WebDriverRelease(node.elementText("Key"));

      if (release.getName() == null) {

      } else if (release.getName().equals("selenium-server-standalone")) {
        allProducts.get(WEBDRIVER_JAR).add(release);
      } else {
        allProducts.get(IE_DRIVER).add(release);
      }
    }
  }

}