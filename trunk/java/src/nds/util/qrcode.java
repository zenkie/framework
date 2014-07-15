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
	 * 生成二维码(QRCode)图片
	 * @param content 存储内容
	 * @param imgPath 图片路径
	 */
	public void encoderQRCode(String content, String imgPath) {
		this.encoderQRCode(content, imgPath, "png", 7,null);
	}
	
	/**
	 * 生成二维码(QRCode)图片
	 * @param content 存储内容
	 * @param output 输出流
	 */
	public void encoderQRCode(String content, OutputStream output) {
		this.encoderQRCode(content, output, "png", 7);
	}
	
	
	/**
	 * 生成二维码(QRCode)图片
	 * @param ServletOutputStream 存储内容
	 * @param output 输出流
	 */
	public void encoderQRCode(String content, ServletOutputStream output) {
		this.encoderQRCode(content, output, "png", 7,null);
	}
	
	/**
	 * 生成二维码(QRCode)图片
	 * @param content 存储内容
	 * @param imgPath 图片路径
	 * @param imgType 图片类型
	 */
	public void encoderQRCode(String content, String imgPath, String imgType) {
		this.encoderQRCode(content, imgPath, imgType, 7,null);
	}
	
	/**
	 * 生成二维码(QRCode)图片
	 * @param content 存储内容
	 * @param output 输出流
	 * @param imgType 图片类型
	 */
	public void encoderQRCode(String content, OutputStream output, String imgType) {
		this.encoderQRCode(content, output, imgType, 7);
	}

	/**
	 * 生成二维码(QRCode)图片
	 * @param content 存储内容
	 * @param imgPath 图片路径
	 * @param imgType 图片类型
	 * @param size 二维码尺寸
	 */
	public void encoderQRCode(String content, String imgPath, String imgType, int size,String logo) {
		try {
			BufferedImage bufImg = this.qRCodeCommon(content, imgType, size,logo);
			
			File imgFile = new File(imgPath);
			// 生成二维码QRCode图片
			ImageIO.write(bufImg, imgType, imgFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 生成二维码(QRCode)图片
	 * @param content 存储内容
	 * @param output 输出流
	 * @param imgType 图片类型
	 * @param size 二维码尺寸
	 */
	public void encoderQRCode(String content, OutputStream output, String imgType, int size) {
		try {
			BufferedImage bufImg = this.qRCodeCommon(content, imgType, size,null);
			// 生成二维码QRCode图片
			ImageIO.write(bufImg, imgType, output);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 生成二维码(QRCode)图片
	 * @param content 存储内容
	 * @param ServletOutputStream 输出流
	 * @param imgType 图片类型
	 * @param size 二维码尺寸
	 */
	public void encoderQRCode(String content, ServletOutputStream output, String imgType, int size,String logo) {
		try {
			BufferedImage bufImg = this.qRCodeCommon(content, imgType, size,logo);
			// 生成二维码QRCode图片
			ImageIO.write(bufImg, imgType, output);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 生成二维码(QRCode)图片的公共方法
	 * @param content 存储内容
	 * @param imgType 图片类型
	 * @param size 二维码尺寸
	 * @return
	 */
	private BufferedImage qRCodeCommon(String content, String imgType, int size,String logo) {
		BufferedImage bufImg = null;
		try {
			Qrcode qrcodeHandler = new Qrcode();
			// 设置二维码排错率，可选L(7%)、M(15%)、Q(25%)、H(30%)，排错率越高可存储的信息越少，但对二维码清晰度的要求越小
			qrcodeHandler.setQrcodeErrorCorrect('L');
			qrcodeHandler.setQrcodeEncodeMode('B');
			// 设置设置二维码尺寸，取值范围1-40，值越大尺寸越大，可存储的信息越大
			qrcodeHandler.setQrcodeVersion(9);
			// 获得内容的字节数组，设置编码格式
			byte[] contentBytes = content.getBytes("utf-8");
			// 图片尺寸
			int imgSize = 67 + 12 * (size - 1);
			bufImg = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_RGB);
			Graphics2D gs = bufImg.createGraphics();
			// 设置背景颜色
			gs.setBackground(null);
			gs.clearRect(0, 0, imgSize, imgSize);

			// 设定图像颜色> BLACK
			gs.setColor(new Color(0, 84, 165));
			// 设置偏移量，不设置可能导致解析出错
			int pixoff = 2;
			// 输出内容> 二维码
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
				//读取头像图片，调整图片大小。
//				byte[] bos =changePicSize(logo);
//				ByteArrayInputStream bais = new ByteArrayInputStream(bos);
//				BufferedImage bi1 =ImageIO.read(bais);
				//URL url = new URL(logo);
				//System.out.print(logo);
				try{
				BufferedImage bi1=ImageUtils.getScaledInstance(logo,60,60,"png",true); 
//				Image img = ImageIO.read(new File(ccbPath));//实例化一个Image对象。
				//读取图片，设置图片在二维码中起始位置。
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
	 * 生成二维码(QRCode)图片
	 * 
	 * @param content
	 *            二维码图片的内容
	 * @param imgPath
	 *            生成二维码图片完整的路径
	 * @param ccbpath
	 *            二维码图片中间的logo路径
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
			
			// 设定图像颜色 > BLACK
			gs.setColor(new Color(0, 84, 165));

			// 设置偏移量 不设置可能导致解析出错
			int pixoff = 3;
			// 输出内容 > 二维码
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
			//读取头像图片，调整图片大小。
			byte[] bos =changePicSize(ccbPath);
			ByteArrayInputStream bais = new ByteArrayInputStream(bos);
			BufferedImage bi1 =ImageIO.read(bais);
			//URL url = new URL(ccbPath); 

			//BufferedImage bi1=ImageUtils.getScaledInstance(url,60,60,"png",true); 
//			Image img = ImageIO.read(new File(ccbPath));//实例化一个Image对象。
			//读取图片，设置图片在二维码中起始位置。
			System.out.print(imgSize);
			gs.drawImage(bi1, 106,106, null);
			gs.dispose();
			bufImg.flush();

			// 生成二维码QRCode图片
			File imgFile = new File(imgPath);
			ImageIO.write(bufImg, "png", imgFile);
		} catch (Exception e) {
			e.printStackTrace();
			return -100;
		}
		return 0;
	}
	/**
	* @Description: 通过地址读取图片信息，并调整大小，返回byte[]  
	* @param path
	* @return byte[]
	* @throws
	 */
	public static byte[] changePicSize(String path){
		
		int new_w=60;     //输出的图片宽度
		int new_h=60;    //输出的图片高度
		
		//图片字节数组返回流
		ByteArrayOutputStream bos =new ByteArrayOutputStream();
		
		Image img=null;
		Toolkit tk=Toolkit.getDefaultToolkit();
		Applet app=new Applet();
		MediaTracker mt = new MediaTracker(app);
		try {
			img=tk.getImage(path);//读取图片
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
	 * 解析二维码（QRCode）
	 * @param imgPath 图片路径
	 * @return
	 */
	public String decoderQRCode(String imgPath) {
		// QRCode 二维码图片的文件
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
	 * 解析二维码（QRCode）
	 * @param input 输入流
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
		System.out.println("解析结果如下：");
		//System.out.println(decoderContent);
		System.out.println("========decoder success!!!");
	}
}