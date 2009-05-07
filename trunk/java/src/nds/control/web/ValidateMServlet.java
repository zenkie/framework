/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.control.web;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ValidateMServlet extends HttpServlet {

	/**
	 * Constructor of the object. no i and o since user may find confusion
	 */
	private char[] str={'A','B','C','D','E','F','G','H','J','K','L','M','N',
            'P','Q','R','S','T','U','V','W','X','Y','Z'
            };
	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		int width=60;
		int height=16;
		response.setContentType("image/jpeg");
		BufferedImage buffer=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		
		Graphics2D g=(Graphics2D)buffer.getGraphics();
		//g.setBackground( new java.awt.Color());
		g.setColor(new java.awt.Color(234,234,234));
		g.fillRect(0, 0, width, height);	
		
		String vstr="";
		for(int i=0;i<4;i++)
		{
			vstr+=str[(int)(str.length*Math.random())];
		}
		request.getSession().setAttribute("nds.control.web.ValidateMServlet", vstr);
		g.setColor(Color.BLACK);
		g.setFont(new Font("Arial",Font.BOLD,14));
		String strx=vstr.substring(0, 1);
		g.drawString(strx, 2, 14);
		//g.setColor(Color.BLACK);
		strx=vstr.substring(1, 2);
		g.drawString(strx, 16, 15);
		//g.setColor(Color.BLACK);
		strx=vstr.substring(2, 3);
		g.drawString(strx, 30, 14);
		//g.setColor(Color.BLACK);
		strx=vstr.substring(3, 4);
		g.drawString(strx, 44, 15);
		g.setColor(Color.LIGHT_GRAY);
		Random radm=new Random();
		for(int j=0;j<8;j++)
		{
			int x=radm.nextInt(width);
			int y=radm.nextInt(height);
			g.drawOval(x, y, 1, 1);
		}
		g.dispose();
		ImageIO.write(buffer, "JPEG", response.getOutputStream());
	}

	/**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out
				.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		out.println("<HTML>");
		out.println("  <HEAD><TITLE>A Servlet</TITLE></HEAD>");
		out.println("  <BODY>");
		out.print("    This is ");
		out.print(this.getClass());
		out.println(", using the POST method");
		out.println("  </BODY>");
		out.println("</HTML>");
		out.flush();
		out.close();
	}

}
