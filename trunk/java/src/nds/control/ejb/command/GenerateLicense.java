package nds.control.ejb.command;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.rmi.RemoteException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.List;
import nds.control.ejb.Command;
import nds.control.ejb.DefaultWebEventHelper;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.control.web.AttachmentManager;
import nds.control.web.ServletContextManager;
import nds.control.web.WebUtils;
import nds.log.Logger;
import nds.query.QueryEngine;
import nds.schema.Column;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.security.User;
import nds.util.AES;
import nds.util.Attachment;
import nds.util.B64Code;
import nds.util.Configurations;
import nds.util.FileUtils;
import nds.util.License;
import nds.util.NDSException;
import nds.util.StringUtils;
import nds.util.Tools;
import nds.util.Validator;

public class GenerateLicense extends Command
{
  private static String aesKeyEncoded = "A9EB6B3CB752CA02E22446AAC0419A88D46CBAC7B78DC5AFDC5455DB911EBD1F172B7585ED94F3E408A5A049F9E2616F6607026D8137512C1F5FA98D45DAC5B55E81050FA3B1591116D204616874692C6FE71C9773C5568D035FD4E1566186A36C8642D6FE5D56F3DA101E3F026EFB8C92C15920F4B296E0B56164319B73A1DDEE4091B0494D6BBB12D4660F13A1F984773DD7C86E427DEB560D5F08FBEC87797A702CFFF5C43E1C9CAACF31C251821CF7AB03A5ACB710DEF3854F24FB42C08988D95C894331AC31BF7F005C20BE616EF42D07911C764CE302B0DD5ABB6A816847AF537036C3C164BB4F97B525685B29328DB78E5D7ADA920805F408093DC50F108879130C83960C6A226D77C5E9DCCCE4A1844C3431A70DBFB61E4FFAD15423C3DBDC5379DF75A56FA382D8388AD359195CF256222A9F0F669E29202E787C80B1AB44851BB26CE9FEACA0223328567E8803B16E901793E55375B57523D9A728D89B32CC6C979EF6C3B6A4B650A36A5DE74984952F6CC40E88D80C02CCCE65441476B975970ACF604E39D89F21B25BC0CB93B8769F13FFF6432A35AAB01485C654E9BCB156F222366FEA31078091A325C945CE39F9159C23E24D5E69B8C707ECF3445131606743EB611CB7557FBD4C25";

  private String nvl(Object o)
  {
    if (o == null) return "";
    return o.toString();
  }
  
  public ValueHolder execute(DefaultWebEvent event)
    throws NDSException, RemoteException
  {
    User user = this.helper.getOperator(event);
    int licenseId = Tools.getInt(event.getParameterValue("objectid", true), -1);
    String password = (String)event.getParameterValue("password", true);
    if (Validator.isNull(password)) throw new NDSException("Empty password");

    ValueHolder holder = new ValueHolder();

    QueryEngine engine = QueryEngine.getInstance();
    Connection conn = null;
    String url = null;
    try {
      conn = engine.getConnection();
      List al = engine.doQueryList("select t.id,t.d_buyer_id,t.d_customer_id,g.name,nvl(t.USE_USER,0) as USE_USER,nvl(t.USE_POS,0) as USE_POS,t.expiresDate,to_char(t.creationDate,'yyyymmdd'),cuscode from D_CUSBOOK t,c_product g where t.c_product_id=g.id and t.status=2 and t.id=" + 
    	        licenseId, conn);
      //select t.id,t.d_buyer_id,t.d_customer_id,g.name,t.USE_USER,t.USE_POS from D_CUSBOOK t,c_product g where t.c_product_id=g.id
//      List al = engine.doQueryList("select licensetype,machinecode,name,'burgeon',versionNO,num_users,num_onlineuser,num_pos,urladdr,expires_date,creation_date from p_license where status=1 and id=" + 
//        licenseId, conn);
      if (al.size() == 0) throw new NDSException("License id=" + licenseId + " not found or submitted already");
      StringBuffer sb = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?><license> ");
      sb.append("<licenseID>").append(licenseId).append("</licenseID>")
        .append("<product>").append("jackrain").append("</product>")
        //.append("<licenseType>").append(nvl(((List)al.get(0)).get(0))).append("</licenseType>")
        .append("<licenseType>").append("burgeon").append("</licenseType>")
        .append("<machineCode>").append("").append("</machineCode>")
        .append("<name>").append(nvl(((List)al.get(0)).get(3))).append("</name>")
        .append("<company>").append(nvl(((List)al.get(0)).get(2))).append("</company>")
        .append("<version>").append("5.0").append("</version>")
        .append("<numUsers>").append(nvl(((List)al.get(0)).get(4))).append("</numUsers>")
        .append("<numOnlineUsers>").append(nvl(((List)al.get(0)).get(4))).append("</numOnlineUsers>")
        .append("<numPOS>").append(nvl(((List)al.get(0)).get(5))).append("</numPOS>")
        .append("<url>").append("").append("</url>")
        .append("<expiresDate>").append("</expiresDate>")
        .append("<creationDate>").append(todate(nvl(((List)al.get(0)).get(7)))).append("</creationDate>")
        //add cus_code
        .append("<cuscode>").append(nvl(((List)al.get(0)).get(8))).append("</cuscode>")
        .append("<signature>").append("</signature></license>");

      License license = License.fromXML(sb.toString());
      String fileContent = generateLicense(license, password);

      String xml = B64Code.decode(decrypt(fileContent));

      license = License.fromXML(xml);
      this.logger.debug("creationDate=" + license.getCreationDate());
      this.logger.debug("expiresDate=" + license.getExpiresDate());
      this.logger.debug("licenseid=" + license.getLicenseID());
      this.logger.debug("type=" + license.getLicenseType());
      this.logger.debug("date=" + license.getMaintenanceExpiresDate());
      this.logger.debug("version=" + license.getVersion());
      this.logger.debug("name=" + license.getName());
      this.logger.debug("company=" + license.getCompany());
      this.logger.debug("machineCode=" + license.getMachineCode());
      this.logger.debug("numUsers=" + license.getNumUsers());
      this.logger.debug("numOnlineUsers=" + license.getNumOnlineUsers());
      this.logger.debug("numPOS=" + license.getNumPOS());
      this.logger.debug("product=" + license.getProduct());
      this.logger.debug("cuscode=" + license.getCuscode());

      TableManager tm = TableManager.getInstance();
      Table table = tm.getTable("D_CUSBOOK");
      Column col = tm.getColumn("D_CUSBOOK", "fileurl");
      AttachmentManager attm = (AttachmentManager)WebUtils.getServletContextManager().getActor("nds.web.AttachmentManager");
      Attachment att = attm.getAttachmentInfo(user.getClientDomain() + "/" + table.getRealTableName() + "/" + col.getName(), String.valueOf(licenseId), -1);
      if (att == null)
      {
    	logger.debug("att is null!");
        att = new Attachment(user.getClientDomain() + "/" + table.getRealTableName() + "/" + col.getName(), String.valueOf(licenseId));
        att.setAuthor(user.name);
        att.setVersion(0);
        att.setExtension("dat");
        att.setOrigFileName("portal.license.dat");
      }
      File file = attm.putAttachmentData(att, new ByteArrayInputStream(fileContent.getBytes("UTF-8")));
      if (Validator.isNull(license.getMachineCode())) {
        String buildOutputPath = ((Configurations)WebUtils.getServletContextManager().getActor("nds.web.configs")).getProperty("build.output.path", "/opt/build/output");
        FileUtils.copyFile(file.getAbsolutePath(), buildOutputPath + File.separator + "portal.license.dat");
      }

      url = "/servlets/binserv/Attach?table=" + table.getId() + "&column=" + col.getId() + "&objectid=" + licenseId;
      String sql = "update D_CUSBOOK set fileurl='" + StringUtils.escapeForSQL(url) + "', modifierid=" + this.helper.getOperator(event).getId().intValue();
      sql = sql + ", modifieddate=sysdate where id=" + licenseId;

      engine.executeUpdate(sql);
      holder.put("data", url);
    } catch (Throwable t) {
      if ((t instanceof NDSException)) throw ((NDSException)t);
      throw new NDSException(t.getMessage(), t); } finally {
      try {
        conn.close(); } catch (Throwable localThrowable1) {  }
    }
    holder.put("message", "ok");
    holder.put("code", new Integer(0));
    return holder;
  }

  public String generateLicense(License license, String secretKey)
    throws Exception
  {
    AES aes = new AES(secretKey);
    String privateKey = aes.decrypt(aesKeyEncoded);
    KeyFactory keyFactory = KeyFactory.getInstance("DSA");

    Signature sig2 = Signature.getInstance("SHA1withDSA");
    PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(B64Code.decode(privateKey.toCharArray()));

    PrivateKey privKey = keyFactory.generatePrivate(privKeySpec);
    sig2.initSign(privKey);

    sig2.update(license.getFingerprint());
    String signature = new String(B64Code.encode(sig2.sign()));

    license.setLicenseSignature(signature);

    String signed = License.toXML(license);

    return encrypt(B64Code.encode(signed));
  }

  private String encrypt(String s) {
    StringBuffer buf = new StringBuffer();
    byte[] b = s.getBytes();

    synchronized (buf)
    {
      for (int i = 0; i < b.length; i++)
      {
        byte b1 = b[i];
        byte b2 = b[(s.length() - (i + 1))];
        int i1 = b1 + b2 + 127;
        int i2 = b1 - b2 + 127;
        int i0 = i1 * 256 + i2;
        String x = Integer.toString(i0, 36);

        switch (x.length()) {
        case 1:
          buf.append('0');
        case 2:
          buf.append('0');
        case 3:
          buf.append('0');
        }buf.append(x);
      }

      return buf.toString();
    }
  }

  private String decrypt(String s)
  {
    byte[] b = new byte[s.length() / 2];
    int l = 0;
    for (int i = 0; i < s.length(); i += 4)
    {
      String x = s.substring(i, i + 4);
      int i0 = Integer.parseInt(x, 36);
      int i1 = i0 / 256;
      int i2 = i0 % 256;
      b[(l++)] = ((byte)((i1 + i2 - 254) / 2));
    }

    return new String(b, 0, l);
  }

  private String todate(Object d)
    throws Exception
  {
    SimpleDateFormat a = new SimpleDateFormat("yyyyMMdd");
    a.setLenient(false);

    SimpleDateFormat b = new SimpleDateFormat("yyyy/MM/dd");
    b.setLenient(false);
    return b.format(a.parse(String.valueOf(d)));
  }
}