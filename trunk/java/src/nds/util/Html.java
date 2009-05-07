/**
 * Copyright (c) 2000-2005 Liferay, LLC. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package nds.util;

import nds.util.FastStringBuffer;

/**
 * <a href="Html.java.html"><b><i>View Source</i></b></a>
 *
 * @author  Brian Wing Shun Chan
 * @author  Clarence Shen
 * @version $Revision: 1.1 $
 *
 */
public class Html {

	public static String escape(String text, boolean stripBlankSpaces) {
		if (text == null) {
			return null;
		}

		int pos = text.indexOf("& ");

		if (pos != -1) {
			text = StringUtils.replace(text, "& ", "&amp; ");
		}

		FastStringBuffer sb = new FastStringBuffer(text.length());
		char c;

		for (int i = 0; i < text.length(); i++) {
			c = text.charAt(i);

			switch (c) {
				case '<':
					sb.append("&lt;");
					break;

				case '>':
					sb.append("&gt;");
					break;

				case '\'':
					sb.append("&#39;");
					break;

				case '\"':
					sb.append("&quot;");
					break;

				case '\r':
					if (stripBlankSpaces) {
						break;
					}

				case '\n':
					if (stripBlankSpaces) {
						break;
					}

				case '\t':
					if (stripBlankSpaces) {
						break;
					}

				default:
					if (((int)c) > 255) {
						sb.append("&#").append(((int)c)).append(";");
					}
					else {
						sb.append(c);
					}
			}
		}

		return sb.toString();
	}

	public static String formatTo(String text) {
		return Html.escape(text, true);
	}

	public static String formatFrom(String text) {
		if (text == null) {
			return null;
		}

		// Optimize this

		text = StringUtils.replace(text, "&#42;", "*");
		text = StringUtils.replace(text, "&#47;", "/");
		text = StringUtils.replace(text, "&#58;", ":");
		text = StringUtils.replace(text, "&#63;", "?");

		return text;
	}

	public static String stripComments(String text) {
		if (text == null) {
			return null;
		}

		FastStringBuffer sb = new FastStringBuffer(text.length());

		int x = 0;
		int y = text.indexOf("<!--");

		while (y != -1) {
			sb.append(text.substring(x, y));

			x = text.indexOf("-->", y) + 3;
			y = text.indexOf("<!--", x);
		}

		if (y == -1) {
			sb.append(text.substring(x, text.length()));
		}

		return sb.toString();

		/*
		int x = text.indexOf("<!--");
		int y = text.indexOf("-->");

		if (x != -1 && y != -1) {
			return stripComments(
				text.substring(0, x) + text.substring(y + 3, text.length()));
		}
		*/

		/*
		Perl5Util util = new Perl5Util();

		text = util.substitute("s/<!--.*-->//g", text);
		*/

		//return text;
	}

	protected static final char[] TAG_SCRIPT = { 's', 'c', 'r', 'i', 'p', 't' };

	public static String stripHtml(String text) {
		if (text == null) {
			return null;
		}

		text = stripComments(text);

		FastStringBuffer sb = new FastStringBuffer(text.length());
		int x = 0;
		int y = text.indexOf("<");

		while (y != -1) {
                    sb.append(text.substring(x, y));

                    // look for text enclosed by <script></script>
                    boolean scriptFound = isScriptTag(text, y+1);

                    if (scriptFound) {
                        int pos = y+TAG_SCRIPT.length;

                        // find end of the tag
                        pos = text.indexOf(">", pos);

                        if (pos >= 0) {
                          // check if preceding character is / (i.e. is this instance of <script/>)
                          if (text.charAt(pos-1) != '/') {
                              // search for the ending </script> tag
                              for (;;) {
                                pos = text.indexOf("</", pos);

                                if (pos >= 0) {
                                  if (isScriptTag(text, pos + 2)) {
                                    y = pos;
                                    break;
                                  }
                                  else {
                                    // skip past "</"
                                    pos +=2;
                                  }
                                }
                                else {
                                  break;
                                }
                              }
                          }
                        }
                    }

                  x = text.indexOf(">", y) + 1;

                  if (x < y) {

                          // <b>Hello</b

                          break;
                  }

                  y = text.indexOf("<", x);
		}

		if (y == -1) {
			sb.append(text.substring(x, text.length()));
		}

		return sb.toString();
	}

	protected static boolean isScriptTag(String text, int start) {
          char item;
          int pos = start;

          if (pos + TAG_SCRIPT.length + 1 <= text.length()) {
            for (int i = 0; i < TAG_SCRIPT.length; i++) {
              item = text.charAt(pos++);

              if (Character.toLowerCase(item) != TAG_SCRIPT[i]) {
                return false;
              }
            }

            item = text.charAt(pos);

            // check that char after "script" is not a letter (i.e. another tag)
            return!Character.isLetter(item);
          }
          else {
            return false;
          }
	}
}

