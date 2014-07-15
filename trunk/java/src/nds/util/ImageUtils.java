package nds.util;

import javax.imageio.ImageIO;
import javax.imageio.IIOException;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.io.File;
import java.awt.image.AffineTransformOp;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

import net.coobird.thumbnailator.*;
import net.coobird.thumbnailator.Thumbnails.Builder;
/**
 * Common method handling image
 * @author yfzhu
 * add Thumbnailator
 */
public class ImageUtils {
	
	
		 
		 
	public static void main (String argv[]) throws Exception {
	 //参数1(from),参数2(to),参数3(宽),参数4(高)
		//URL url = new URL("http://lavasoft.blog.51cto.com/attachment/200811/200811271227767778082.jpg"); 
		//createThumbnailatorurl(url,"/Users/jackrain/Downloads/imgTest3.jpg",200,300);
		//createThumbnailator("/Users/jackrain/Downloads/4.jpg","/Users/jackrain/Downloads/imgTest2.jpg",200,200);
		
		//createThumbnail("/Users/jackrain/Downloads/4.jpg","/Users/jackrain/Downloads/imgTest4.jpg",200,300);
		
		File f = new File("/Users/jackrain/Downloads/bodybg.png");  
        InputStream in = new FileInputStream(f); 
        BufferedImage bi=ImageIO.read(in);
        System.out.print(in.available());
        ImageUtils.createThumbnailator(bi,"/Users/jackrain/Downloads/bodybg22.png","bodybg.png");
		//createThumbnailator(in,"/Users/jackrain/Downloads/bodybg22.png",200,100,true,"bodybg.png");
		
		
		//createThumbnails("c:/image2.jpg","d:/tmp","134.jpg",new int[]{140,49,34});
	}
	/**
	 * 在指定目录下创建若干个尺寸的缩略图, 文件名称为 原图片名＋"_1.jpg",  原图片名＋"_2.jpg", 依次类推
	 * @param fromFileStr 原图路径，仅支持jpg 和 png 两种格式
	 * @param destFolder 缩略图所在目录，最后不含分隔符，举例 c:\pdt
	 * @param fileName 缩略图的统一文件名模板， 生成的缩略图将按照 <文件名>+"_?."+ <文件名后缀> 生成
	 * @param widths， 不同的宽度, 高度将按原图高宽比自动计算
	 */
	public static void createThumbnails(String fromFileStr,String destFolder, String fileName, int[] widths) throws Exception{
		 BufferedImage srcImage;
		 String imgType = "JPEG";
		 if (fromFileStr.toLowerCase().endsWith(".png")) {
			 imgType = "PNG";
		 }
		 File fromFile=new File(fromFileStr);
		 srcImage = ImageIO.read(fromFile);
		 int origWidth= srcImage.getWidth();
		 int origHeight= srcImage.getHeight();
		 String origFileName= fileName;
		 int idx= origFileName.lastIndexOf('.');
		 
		 String ps= origFileName.substring(0,idx);
		 String pse=origFileName.substring(idx);
		 for(int i=0;i< widths.length;i++){
			 double sx = (double) widths[i] / origWidth;
			 int wid=widths[i];
			 if(sx>1) {
				 //keep original one
				 wid=origWidth;
				 sx=1;//throw new java.lang.IllegalArgumentException("Found thumbnail width ("+widths[i]+") greater than original image width "+origWidth);
			 }
			 int height= (int)(origHeight * sx);
			 String saveToFileStr=destFolder+ File.separator+ ps+"_"+ (i+1)+ pse;
			 File saveFile=new File(saveToFileStr);
			 BufferedImage desImage=getScaledInstance(srcImage, widths[i], height,
					 RenderingHints.VALUE_INTERPOLATION_BILINEAR,false,true);//VALUE_INTERPOLATION_BICUBIC
			 ImageIO.write(desImage, imgType, saveFile);
		 }
		 
		
	}
	/**
	 * 创建缩略图，目前原图和缩略图仅支持 jpg 和 png 两种格式，输入和输出格式保持一致
	 * @param fromFileStr 原图片路径，可以是 jgp 或 png 格式
	 * @param saveToFileStr 缩略图全路径
	 * @param width 缩略图最大宽度，最终尺寸可能小于此大小（锁定高宽比）
	 * @param hight 缩略图最大高度，最终尺寸可能小于此大小（锁定高宽比）
	 * @throws Exception
	 */ 
	public static void createThumbnail (String fromFileStr,String saveToFileStr,int width,int hight)
		 						throws Exception {
		 BufferedImage srcImage;
		 String imgType = "JPEG";
		 if (fromFileStr.toLowerCase().endsWith(".png")) {
			 imgType = "PNG";
		 }
		 File saveFile=new File(saveToFileStr);
		 File fromFile=new File(fromFileStr);
		 srcImage = ImageIO.read(fromFile);
		 srcImage = getScaledInstance(srcImage, width, hight,
					 RenderingHints.VALUE_INTERPOLATION_BILINEAR,true,true); // VALUE_INTERPOLATION_BICUBIC
		 ImageIO.write(srcImage, imgType, saveFile);
	}
	
	public static void createThumbnail(File saveFile,File data , int width,String imgtype) throws Exception {
		BufferedImage srcImage;
		/*
		String imgType = "JPEG";
		if (fromFileStr.toLowerCase().equals(".png")) {
			imgType = "PNG";
		}
		*/
		//File saveFile = new File(saveToFileStr);
		srcImage = ImageIO.read(data);
		 int origWidth= srcImage.getWidth();
		 int origHeight= srcImage.getHeight();
		 double sx = (double) width/ origWidth;
		 if(sx>1) {
			 //keep original one
			 sx=1;//throw new java.lang.IllegalArgumentException("Found thumbnail width ("+widths[i]+") greater than original image width "+origWidth);
		 }
		 int height= (int)(origHeight * sx);
		srcImage = getScaledInstance(srcImage, width, height,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR, true, true); // VALUE_INTERPOLATION_BICUBIC
		ImageIO.write(srcImage, imgtype, saveFile);
	}
	
	
	 /**
	     * Convenience method that returns a scaled instance of the
	     * provided {@code BufferedImage}.
	     *
	     * @param img the original image to be scaled
	     * @param targetWidth the desired width of the scaled instance,
	     *    in pixels
	     * @param targetHeight the desired height of the scaled instance,
	     *    in pixels
	     * @param hint one of the rendering hints that corresponds to
	     *    {@code RenderingHints.KEY_INTERPOLATION} (e.g.
	     *    {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
	     *    {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
	     *    {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
	     * @param fixedWHScale if true, will fixed scale rate on width and height, 
	     * @param higherQuality if true, this method will use a multi-step
	     *    scaling technique that provides higher quality than the usual
	     *    one-step technique (only useful in downscaling cases, where
	     *    {@code targetWidth} or {@code targetHeight} is
	     *    smaller than the original dimensions, and generally only when
	     *    the {@code BILINEAR} hint is specified)
	     * @return a scaled version of the original {@code BufferedImage}
	     */
	    public static BufferedImage getScaledInstance(BufferedImage img,
	                                           int targetWidth,
	                                           int targetHeight,
	                                           Object hint, boolean fixedWHScale,
	                                           boolean higherQuality)
	    {
	        int type = (img.getTransparency() == Transparency.OPAQUE) ?
	            BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
	        BufferedImage ret = (BufferedImage)img;
			if(fixedWHScale){ 
				double sx = (double) targetWidth / img.getWidth();
				double sy = (double) targetHeight / img.getHeight();
				 //这里想实现在targetW，targetH范围内实现等比缩放, 取缩放比例小的那个（即缩得更小）
				 if(sx>sy){
					 sx = sy;
					 targetWidth = (int)(sx * img.getWidth());
				 }else{
					 sy = sx;
					 targetHeight = (int)(sy * img.getHeight());
				 }
			}
	        int w, h;
	        if (higherQuality) {
	            // Use multi-step technique: start with original size, then
	            // scale down in multiple passes with drawImage()
	            // until the target size is reached
	            w = img.getWidth();
	            h = img.getHeight();
	        } else {
	            // Use one-step technique: scale directly from original
	            // size to target size with a single drawImage() call
	            w = targetWidth;
	            h = targetHeight;
	        }
	        
	        do {
	            if (higherQuality && w > targetWidth) {
	                w /= 2;
	                if (w < targetWidth) {
	                    w = targetWidth;
	                }
	            }

	            if (higherQuality && h > targetHeight) {
	                h /= 2;
	                if (h < targetHeight) {
	                    h = targetHeight;
	                }
	            }

	            BufferedImage tmp = new BufferedImage(w, h, type);
	            Graphics2D g2 = tmp.createGraphics();
	            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
	            g2.drawImage(ret, 0, 0, w, h, null);
	            g2.dispose();

	            ret = tmp;
	        } while (w != targetWidth || h != targetHeight);

	        return ret;
	    }
	    
	    
	    
	    
	public static void getScaledInstance(File saveFile, File data, int width,int hight,
			String imgtype,Boolean force) throws Exception {
		BufferedImage srcImage;
		Builder<BufferedImage> builder = null;  
		/*
		 * String imgType = "JPEG"; if
		 * (fromFileStr.toLowerCase().equals(".png")) { imgType = "PNG"; }
		 */
		// File saveFile = new File(saveToFileStr);
		srcImage = ImageIO.read(data);
		if(force){
			builder=Thumbnails.of(srcImage).forceSize(width,hight).outputFormat(imgtype).outputQuality(0.8f);
		}else{
			builder=Thumbnails.of(srcImage).size(width,hight).outputFormat(imgtype).outputQuality(0.8f);
		}
		builder.toFile(saveFile);
	}
	
	
	public static BufferedImage getScaledInstance(String path,int width,int hight,
			String imgtype,Boolean force) throws Exception {
		//BufferedImage srcImage;
	 
		File fromFile=new File(path);
		BufferedImage srcImage;
		Builder<BufferedImage> builder = null;  
		/*
		 * String imgType = "JPEG"; if
		 * (fromFileStr.toLowerCase().equals(".png")) { imgType = "PNG"; }
		 */
		// File saveFile = new File(saveToFileStr);
		srcImage = ImageIO.read(fromFile);
		/*
		 * String imgType = "JPEG"; if
		 * (fromFileStr.toLowerCase().equals(".png")) { imgType = "PNG"; }
		 */
		// File saveFile = new File(saveToFileStr);
		if(force){
			builder=Thumbnails.of(srcImage).forceSize(width,hight).outputFormat(imgtype).outputQuality(0.8f);
		}else{
			builder=Thumbnails.of(srcImage).size(width,hight).outputFormat(imgtype).outputQuality(0.8f);
		}
		return builder.asBufferedImage();
	}
	

		  

	
	public static void createThumbnailator(File saveFile,File data , int width,String imgtype) throws Exception {
		BufferedImage image = ImageIO.read(data);  
		int imageHeitht = image.getHeight();  
		getScaledInstance(saveFile,data,width,imageHeitht,"jpg",false);
		
	}
	
	public static void createThumbnailator (InputStream in,String saveToFileStr,int width,int hight, boolean force,String fileName)
			throws Exception {
		
		 String imgType = "jpg";
		 if (fileName.toLowerCase().endsWith(".png")) {
			 imgType = "png";
		 }
		 
		 File saveFile=new File(saveToFileStr);
		 Builder<? extends InputStream> builder = null;  
		if(force){
			builder=Thumbnails.of(in).forceSize(width,hight).outputFormat(imgType).outputQuality(0.8f);
		}else{
			builder=Thumbnails.of(in).size(width,hight).outputFormat(imgType).outputQuality(0.8f);
		}
		builder.toFile(saveFile);
		
	}
	
	public static void createThumbnailator (BufferedImage in,String saveToFileStr,int width,int hight, boolean force,String fileName)
			throws Exception {
		
		 String imgType = "jpg";
		 if (fileName.toLowerCase().endsWith(".png")) {
			 imgType = "png";
		 }
		 
		 File saveFile=new File(saveToFileStr);
		 Builder<BufferedImage> builder = null;  
		if(force){
			builder=Thumbnails.of(in).forceSize(width,hight).outputFormat(imgType).outputQuality(0.5f);
		}else{
			builder=Thumbnails.of(in).size(width,hight).outputFormat(imgType).outputQuality(0.5f);
		}
		builder.toFile(saveFile);
		
	}
	//不压缩 按原始比例
	public static void createThumbnailator (InputStream in,String saveToFileStr,String fileName)
			throws Exception {
		
		 String imgType = "jpg";
		 if (fileName.toLowerCase().endsWith(".png")) {
			 imgType = "png";
		 }
		 
		 File saveFile=new File(saveToFileStr);
		 Builder<? extends InputStream> builder = null;  
		builder=Thumbnails.of(in).scale(1).outputFormat(imgType).outputQuality(0.8f);
		builder.toFile(saveFile);
		
	}
	
	
	//不压缩 按原始比例
	public static void createThumbnailator (BufferedImage in,String saveToFileStr,String fileName)
			throws Exception {
		
		 String imgType = "jpg";
		 if (fileName.toLowerCase().endsWith(".png")) {
			 imgType = "png";
		 }
		 
		 File saveFile=new File(saveToFileStr);
		 Builder<BufferedImage> builder = null;  
		builder=Thumbnails.of(in).scale(1).outputFormat(imgType).outputQuality(0.8f);
		builder.toFile(saveFile);
		
	}


	public static void createThumbnailator (String fromFileStr,String saveToFileStr,int width,int hight)
				throws Exception {
		 String imgType = "jpg";
		 if (fromFileStr.toLowerCase().endsWith(".png")) {
			 imgType = "png";
		 }
		 File saveFile=new File(saveToFileStr);
		 File fromFile=new File(fromFileStr);
		getScaledInstance(saveFile,fromFile,width,hight,imgType,true);
	}
	
	public static void createThumbnailator (String fromFileStr,String saveToFileStr,int width,String imgtype)
			throws Exception {
	 String imgType = "jpg";
	 if (fromFileStr.toLowerCase().endsWith(".png")) {
		 imgType = "png";
	 }
	 File saveFile=new File(saveToFileStr);
	 File fromFile=new File(fromFileStr);
	 BufferedImage image = ImageIO.read(fromFile);  
		int hight = image.getHeight(); 
	getScaledInstance(saveFile,fromFile,width,hight,imgType,true);
	}
	
	
	//url load 
	public static void createThumbnailatorurl (URL url,String saveToFileStr,int width,int hight)
			throws Exception {
	 File saveFile=new File(saveToFileStr);
	getScaledInstance(saveFile,url,width,hight,"jpg",true);
	}
	
	private static void getScaledInstance(File saveFile, URL url, int width,
			int hight, String imgtype, boolean force) throws Exception {
		Builder<URL> builder = null;  
		if(force){
			builder=Thumbnails.of(url).forceSize(width,hight).outputFormat(imgtype).outputQuality(0.8f);
		}else{
			builder=Thumbnails.of(url).size(width,hight).outputFormat(imgtype).outputQuality(0.8f);
		}
		builder.toFile(saveFile);
	}
	

	
	
}
