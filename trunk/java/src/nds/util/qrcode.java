package nds.util;

import java.applet.Applet;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;

import nds.control.web.binhandler.GetQrcode;
import nds.log.Logger;
import nds.log.LoggerManager;

import jp.sourceforge.qrcode.QRCodeDecoder;
import jp.sourceforge.qrcode.exception.DecodingFailedException;

import com.swetake.util.Qrcode;

public class qrcode {
	private Logger logger= LoggerManager.getInstance().getLogger(qrcode.class.getName());	 
	/**
	 * ���ɶ�ά��(QRCode)ͼƬ
	 * @param content �洢����
	 * @param imgPath ͼƬ·��
	 */
	public void encoderQRCode(String content, String imgPath) {
		this.encoderQRCode(content, imgPath, "png", 7,null);
	}
	
	/**
	 * ���ɶ�ά��(QRCode)ͼƬ
	 * @param content �洢����
	 * @param output �����
	 */
	public void encoderQRCode(String content, OutputStream output) {
		this.encoderQRCode(content, output, "png", 7);
	}
	
	
	/**
	 * ���ɶ�ά��(QRCode)ͼƬ
	 * @param ServletOutputStream �洢����
	 * @param output �����
	 */
	public void encoderQRCode(String content, ServletOutputStream output) {
		this.encoderQRCode(content, output, "png", 7,null);
	}
	
	/**
	 * ���ɶ�ά��(QRCode)ͼƬ
	 * @param content �洢����
	 * @param imgPath ͼƬ·��
	 * @param imgType ͼƬ����
	 */
	public void encoderQRCode(String content, String imgPath, String imgType) {
		this.encoderQRCode(content, imgPath, imgType, 7,null);
	}
	
	/**
	 * ���ɶ�ά��(QRCode)ͼƬ
	 * @param content �洢����
	 * @param output �����
	 * @param imgType ͼƬ����
	 */
	public void encoderQRCode(String content, OutputStream output, String imgType) {
		this.encoderQRCode(content, output, imgType, 7);
	}

	/**
	 * ���ɶ�ά��(QRCode)ͼƬ
	 * @param content �洢����
	 * @param imgPath ͼƬ·��
	 * @param imgType ͼƬ����
	 * @param size ��ά��ߴ�
	 */
	public void encoderQRCode(String content, String imgPath, String imgType, int size,String logo) {
		try {
			BufferedImage bufImg = this.qRCodeCommon(content, imgType, size,logo);
			
			File imgFile = new File(imgPath);
			// ���ɶ�ά��QRCodeͼƬ
			ImageIO.write(bufImg, imgType, imgFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ���ɶ�ά��(QRCode)ͼƬ
	 * @param content �洢����
	 * @param output �����
	 * @param imgType ͼƬ����
	 * @param size ��ά��ߴ�
	 */
	public void encoderQRCode(String content, OutputStream output, String imgType, int size) {
		try {
			BufferedImage bufImg = this.qRCodeCommon(content, imgType, size,null);
			// ���ɶ�ά��QRCodeͼƬ
			ImageIO.write(bufImg, imgType, output);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * ���ɶ�ά��(QRCode)ͼƬ
	 * @param content �洢����
	 * @param ServletOutputStream �����
	 * @param imgType ͼƬ����
	 * @param size ��ά��ߴ�
	 */
	public void encoderQRCode(String content, ServletOutputStream output, String imgType, int size,String logo) {
		try {
			BufferedImage bufImg = this.qRCodeCommon(content, imgType, size,logo);
			// ���ɶ�ά��QRCodeͼƬ
			ImageIO.write(bufImg, imgType, output);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ���ɶ�ά��(QRCode)ͼƬ�Ĺ�������
	 * @param content �洢����
	 * @param imgType ͼƬ����
	 * @param size ��ά��ߴ�
	 * @return
	 */
	private BufferedImage qRCodeCommon(String content, String imgType, int size,String logo) {
		BufferedImage bufImg = null;
		try {
			Qrcode qrcodeHandler = new Qrcode();
			// ���ö�ά���Ŵ��ʣ���ѡL(7%)��M(15%)��Q(25%)��H(30%)���Ŵ���Խ�߿ɴ洢����ϢԽ�٣����Զ�ά�������ȵ�Ҫ��ԽС
			qrcodeHandler.setQrcodeErrorCorrect('L');
			qrcodeHandler.setQrcodeEncodeMode('B');
			// �������ö�ά��ߴ磬ȡֵ��Χ1-40��ֵԽ��ߴ�Խ�󣬿ɴ洢����ϢԽ��
			qrcodeHandler.setQrcodeVersion(9);
			// ������ݵ��ֽ����飬���ñ����ʽ
			byte[] contentBytes = content.getBytes("utf-8");
			// ͼƬ�ߴ�
			int imgSize = 67 + 12 * (size - 1);
			bufImg = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_RGB);
			Graphics2D gs = bufImg.createGraphics();
			// ���ñ�����ɫ
			gs.setBackground(null);
			gs.clearRect(0, 0, imgSize, imgSize);

			// �趨ͼ����ɫ> BLACK
			gs.setColor(new Color(0, 84, 165));
			// ����ƫ�����������ÿ��ܵ��½�������
			int pixoff = 2;
			// �������> ��ά��
			if (contentBytes.length > 0 && contentBytes.length < 800) {
				boolean[][] codeOut = qrcodeHandler.calQrcode(contentBytes);
				for (int i = 0; i < codeOut.length; i++) {
					for (int j = 0; j < codeOut.length; j++) {
						if (codeOut[j][i]) {
							gs.fillRect(j * 5 + pixoff, i * 5 + pixoff, 5, 5);
						}
					}
				}
			} else {
				throw new Exception("QRCode content bytes length = " + contentBytes.length + " not in [0, 800].");
			}
			
			if(logo!=null){
				//��ȡͷ��ͼƬ������ͼƬ��С��
//				byte[] bos =changePicSize(logo);
//				ByteArrayInputStream bais = new ByteArrayInputStream(bos);
//				BufferedImage bi1 =ImageIO.read(bais);
				//URL url = new URL(logo);
				//System.out.print(logo);
				try{
				BufferedImage bi1=ImageUtils.getScaledInstance(logo,60,60,"png",true); 
//				Image img = ImageIO.read(new File(ccbPath));//ʵ����һ��Image����
				//��ȡͼƬ������ͼƬ�ڶ�ά������ʼλ�á�
				//System.out.print(imgSize);
				gs.drawImage(bi1, 106,106, null);
				}catch(Exception w){
				logger.debug("file is not find!");
				}
			}
			gs.dispose();
			bufImg.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bufImg;
	}
	
	
	/**
	 * ���ɶ�ά��(QRCode)ͼƬ
	 * 
	 * @param content
	 *            ��ά��ͼƬ������
	 * @param imgPath
	 *            ���ɶ�ά��ͼƬ������·��
	 * @param ccbpath
	 *            ��ά��ͼƬ�м��logo·��
	 */
	public static int createQRCode(String content, String imgPath,String ccbPath,int size) {
		try {
			Qrcode qrcodeHandler = new Qrcode();
			qrcodeHandler.setQrcodeErrorCorrect('L');
			qrcodeHandler.setQrcodeEncodeMode('B');
			qrcodeHandler.setQrcodeVersion(9);

			// System.out.println(content);
			byte[] contentBytes = content.getBytes("utf-8");
			int imgSize = 67 + 12 * (size - 1);
			BufferedImage bufImg = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_RGB);
			Graphics2D gs = bufImg.createGraphics();
			Color c=new Color(1f,1f,1f,.5f );
			gs.setBackground(c);

			gs.clearRect(0, 0, imgSize, imgSize);
			
			// �趨ͼ����ɫ > BLACK
			gs.setColor(new Color(0, 84, 165));

			// ����ƫ���� �����ÿ��ܵ��½�������
			int pixoff = 3;
			// ������� > ��ά��
			if (contentBytes.length > 0 && contentBytes.length < 800) {
				boolean[][] codeOut = qrcodeHandler.calQrcode(contentBytes);
				for (int i = 0; i < codeOut.length; i++) {
					for (int j = 0; j < codeOut.length; j++) {
						if (codeOut[j][i]) {
							gs.fillRect(j * 5 + pixoff, i * 5 + pixoff, 5, 5);
						}
					}
				}
			} else {
				System.err.println("QRCode content bytes length = "
					+ contentBytes.length + " not in [ 0,120 ]. ");
				return -1;
			}
			//��ȡͷ��ͼƬ������ͼƬ��С��
			byte[] bos =changePicSize(ccbPath);
			ByteArrayInputStream bais = new ByteArrayInputStream(bos);
			BufferedImage bi1 =ImageIO.read(bais);
			//URL url = new URL(ccbPath); 

			//BufferedImage bi1=ImageUtils.getScaledInstance(url,60,60,"png",true); 
//			Image img = ImageIO.read(new File(ccbPath));//ʵ����һ��Image����
			//��ȡͼƬ������ͼƬ�ڶ�ά������ʼλ�á�
			System.out.print(imgSize);
			gs.drawImage(bi1, 106,106, null);
			gs.dispose();
			bufImg.flush();

			// ���ɶ�ά��QRCodeͼƬ
			File imgFile = new File(imgPath);
			ImageIO.write(bufImg, "png", imgFile);
		} catch (Exception e) {
			e.printStackTrace();
			return -100;
		}
		return 0;
	}
	/**
	* @Description: ͨ����ַ��ȡͼƬ��Ϣ����������С������byte[]  
	* @param path
	* @return byte[]
	* @throws
	 */
	public static byte[] changePicSize(String path){
		
		int new_w=60;     //�����ͼƬ���
		int new_h=60;    //�����ͼƬ�߶�
		
		//ͼƬ�ֽ����鷵����
		ByteArrayOutputStream bos =new ByteArrayOutputStream();
		
		Image img=null;
		Toolkit tk=Toolkit.getDefaultToolkit();
		Applet app=new Applet();
		MediaTracker mt = new MediaTracker(app);
		try {
			img=tk.getImage(path);//��ȡͼƬ
			mt.addImage(img, 0);
			mt.waitForID(0);
		}catch(Exception e) {
			e.printStackTrace();
		}

		if(img.getWidth(null)==-1){
			System.out.println("   can't read,retry!"+"<BR>");
			return null;
		}else{
			BufferedImage buffImg = new BufferedImage(new_w,new_h,BufferedImage.TYPE_INT_RGB);
			Graphics g = buffImg.createGraphics();
			g.setColor(Color.white);
			g.fillRect(0,0,new_w,new_h);
			g.drawImage(img,0,0,new_w,new_h,null);
			g.dispose();

	        try {
				ImageIO.write(buffImg, "png", bos);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bos.toByteArray();
	}

	
	/**
	 * ������ά�루QRCode��
	 * @param imgPath ͼƬ·��
	 * @return
	 */
	public String decoderQRCode(String imgPath) {
		// QRCode ��ά��ͼƬ���ļ�
		File imageFile = new File(imgPath);
		BufferedImage bufImg = null;
		String content = null;
		try {
			bufImg = ImageIO.read(imageFile);
			QRCodeDecoder decoder = new QRCodeDecoder();
			content = new String(decoder.decode(new TwoDimensionCodeImage(bufImg)), "utf-8"); 
		} catch (IOException e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
		} catch (DecodingFailedException dfe) {
			System.out.println("Error: " + dfe.getMessage());
			dfe.printStackTrace();
		}
		return content;
	}
	
	/**
	 * ������ά�루QRCode��
	 * @param input ������
	 * @return
	 */
	public String decoderQRCode(InputStream input) {
		BufferedImage bufImg = null;
		String content = null;
		try {
			bufImg = ImageIO.read(input);
			QRCodeDecoder decoder = new QRCodeDecoder();
			content = new String(decoder.decode(new TwoDimensionCodeImage(bufImg)), "utf-8"); 
		} catch (IOException e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
		} catch (DecodingFailedException dfe) {
			System.out.println("Error: " + dfe.getMessage());
			dfe.printStackTrace();
		}
		return content;
	}

	public static void main(String[] args) {
		String imgPath = "/Users/jackrain/Desktop/Michael_QRCode.png";
		String encoderContent = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx288cb89fbc928013&redirect_uri=http://www.demo.com/html/nds/oto/webapp/product/index.vml?id=1&response_type=code&scope=snsapi_base&state=123#wechat_redirect";
		//String encoderContent="123123";
		qrcode handler = new qrcode();
		handler.encoderQRCode(encoderContent,imgPath,"png",18,"http://www.demo.com/servlets/userfolder/WX_APPENDGOODS/20140711162015498.jpg");
		//handler.createQRCode(encoderContent, imgPath,"http://www.demo.com/servlets/userfolder/WX_APPENDGOODS/20140711162015498.jpg",18);
//		try {
//			OutputStream output = new FileOutputStream(imgPath);
//			handler.encoderQRCode(content, output);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		System.out.println("========encoder success");
		
		
		//String decoderContent = handler.decoderQRCode(imgPath);
		System.out.println("����������£�");
		//System.out.println(decoderContent);
		System.out.println("========decoder success!!!");
	}
}