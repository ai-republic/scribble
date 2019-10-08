package microservice.webui.menu;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Backing for the main menubar.
 * 
 * @author Torsten Oltmanns
 *
 */
@ManagedBean
public class Menubar implements Serializable {
  private static final long serialVersionUID = 2809434288070667974L;
  private final static Logger LOG = LoggerFactory.getLogger(Menubar.class);

  public void actionDeveloperDocumentation() {
    LOG.debug("Documentation clicked");
  }

  public void actionAdminDocumentation() {
    LOG.debug("Documentation clicked");
  }

  public void actionDownload() {
    LOG.debug("Download clicked");

  }

  public void actionSupport() {
    LOG.debug("Support clicked");

  }

}
