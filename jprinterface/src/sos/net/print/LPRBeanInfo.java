
//Titel:Line Printer Daemon Protocol
//Version:
//Copyright:Copyright (c) 1998
//Autor:Mario Mueller
//Organisation:Shamrock-Online
//Beschreibung:

package sos.net.print;

import java.beans.*;

public class LPRBeanInfo extends SimpleBeanInfo {
  Class<LPR> beanClass = LPR.class;
  String iconColor16x16Filename = "lpr16c.gif";
  String iconColor32x32Filename = "lpr32c.gif";
  String iconMono16x16Filename = "lpr16m.gif";
  String iconMono32x32Filename = "lpr32m.gif";

  
  public LPRBeanInfo() {
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    try  {
      PropertyDescriptor __cfA_banner = new PropertyDescriptor("_cfA_banner", beanClass, "get_cfA_banner", "set_cfA_banner");
      __cfA_banner.setDisplayName("Banner");
      __cfA_banner.setShortDescription("print banner page");
      
      PropertyDescriptor __cfA_CIF = new PropertyDescriptor("_cfA_CIF", beanClass, "get_cfA_CIF", "set_cfA_CIF");
      __cfA_CIF.setDisplayName("CIF");
      __cfA_CIF.setShortDescription("print as CIF file");
      
      PropertyDescriptor __cfA_DVI = new PropertyDescriptor("_cfA_DVI", beanClass, "get_cfA_DVI", "set_cfA_DVI");
      __cfA_DVI.setDisplayName("DVI");
      __cfA_DVI.setShortDescription("print as DVI file");
      
      PropertyDescriptor __cfA_formatted = new PropertyDescriptor("_cfA_formatted", beanClass, "get_cfA_formatted", "set_cfA_formatted");
      __cfA_formatted.setDisplayName("Formatted");
      __cfA_formatted.setShortDescription("formatted output");
      
      PropertyDescriptor __cfA_jobname = new PropertyDescriptor("_cfA_jobname", beanClass, "get_cfA_jobname", "set_cfA_jobname");
      __cfA_jobname.setDisplayName("Jobname");
      __cfA_jobname.setShortDescription("Jobname");
      
      PropertyDescriptor __cfA_postscript = new PropertyDescriptor("_cfA_postscript", beanClass, "get_cfA_postscript", "set_cfA_postscript");
      __cfA_postscript.setDisplayName("Postscript");
      __cfA_postscript.setShortDescription("print as postscript file");
      
      PropertyDescriptor __cfA_pr = new PropertyDescriptor("_cfA_pr", beanClass, "get_cfA_pr", "set_cfA_pr");
      __cfA_pr.setDisplayName("prFormat");
      __cfA_pr.setShortDescription("print with 'pr' format");
      
      PropertyDescriptor _hostName = new PropertyDescriptor("hostName", beanClass, "getHostName", "setHostName");
      _hostName.setDisplayName("Hostname");
      _hostName.setShortDescription("Hostname");
      
      PropertyDescriptor _timeout = new PropertyDescriptor("timeout", beanClass, "getTimeout", "setTimeout");
      _timeout.setDisplayName("Timeout");
      _timeout.setShortDescription("Timeout");
      
      PropertyDescriptor _host = new PropertyDescriptor("host", beanClass, "getHost", "setHost");
      _host.setDisplayName("LPD Host");
      _host.setShortDescription("LPD Host");
      
      PropertyDescriptor _port = new PropertyDescriptor("port", beanClass, "getPort", "setPort");
      _port.setDisplayName("LPD Port");
      _port.setShortDescription("LPD Port");
      
      PropertyDescriptor _user = new PropertyDescriptor("user", beanClass, "getUser", "setUser");
      _user.setDisplayName("LPD User");
      _user.setShortDescription("LPD User");
      
      PropertyDescriptor[] pds = new PropertyDescriptor[] {
        __cfA_banner,
        __cfA_CIF,
        __cfA_DVI,
        __cfA_formatted,
        __cfA_jobname,
        __cfA_postscript,
        __cfA_pr,
        _hostName,
        _timeout,
        _host,
        _port,
        _user,
      };
      return pds;
    }
    catch (IntrospectionException ex) {
      ex.printStackTrace();
      return null;
    }
  }

  public java.awt.Image getIcon(int iconKind) {
    switch (iconKind) {
    case BeanInfo.ICON_COLOR_16x16:
      return iconColor16x16Filename != null ? loadImage(iconColor16x16Filename) : null;
    case BeanInfo.ICON_COLOR_32x32:
      return iconColor32x32Filename != null ? loadImage(iconColor32x32Filename) : null;
    case BeanInfo.ICON_MONO_16x16:
      return iconMono16x16Filename != null ? loadImage(iconMono16x16Filename) : null;
    case BeanInfo.ICON_MONO_32x32:
      return iconMono32x32Filename != null ? loadImage(iconMono32x32Filename) : null;
    }
    return null;
  }
}

 