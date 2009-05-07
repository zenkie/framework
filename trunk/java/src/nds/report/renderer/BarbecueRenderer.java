/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.report.renderer;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import net.sf.jasperreports.engine.JRAbstractSvgRenderer;
import net.sourceforge.barbecue.Barcode;


/**
 * A wrapper for the Drawable interface in the JCommon library: you will need the
 * JCommon classes in your classpath to compile this class. In particular this can be
 * used to allow JFreeChart objects to be included in the output report in vector form.
 *
 * @author Teodor Danciu (teodord@users.sourceforge.net)
 * @version $Id: BarbecueRenderer.java,v 1.1 2006/06/24 00:35:24 Administrator Exp $
 */
public class BarbecueRenderer extends JRAbstractSvgRenderer
{

	/**
	 * 
	 */	
	private Barcode barcode = null;

	public BarbecueRenderer(Barcode barcode) 
	{
		this.barcode = barcode;
	}
	/**
	 * 
	 * @param barcode
	 * @param width in pixel  
	 * @param height in pixel
	 * @param fontSize font size 
	 */
	public BarbecueRenderer(Barcode barcode, int width, int height, int fontSize) 
	{
		this.barcode = barcode;
		barcode.setBarWidth((double)width);
		barcode.setBarHeight((double)height);
		barcode.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, fontSize));
	}


	/**
	 *
	 */
	public void render(Graphics2D grx, Rectangle2D rectangle) 
	{
		if (barcode != null) 
		{
			barcode.draw(grx, (int)rectangle.getX(), (int)rectangle.getY());
		}
	}

	
}
