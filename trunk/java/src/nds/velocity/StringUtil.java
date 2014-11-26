  package nds.velocity;
  
  import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;

//import com.liferay.portal.kernel.util.StringPool;
//import com.liferay.util.Randomizer;

import nds.util.Validator;
  
  public class StringUtil {

		public static String add(String s, String add) {
			return add(s, add, StringPool.COMMA);
		}

		public static String add(String s, String add, String delimiter) {
			return add(s, add, delimiter, false);
		}

		public static String add(
			String s, String add, String delimiter, boolean allowDuplicates) {

			if ((add == null) || (delimiter == null)) {
				return null;
			}

			if (s == null) {
				s = StringPool.BLANK;
			}

			if (allowDuplicates || !contains(s, add, delimiter)) {
				StringBuffer sm = new StringBuffer();

				sm.append(s);

				if (Validator.isNull(s) || s.endsWith(delimiter)) {
					sm.append(add);
					sm.append(delimiter);
				}
				else {
					sm.append(delimiter);
					sm.append(add);
					sm.append(delimiter);
				}

				s = sm.toString();
			}

			return s;
		}

		public static String bytesToHexString(byte[] bytes) {
			StringBuffer sm = new StringBuffer(bytes.length * 2);

			for (int i = 0; i < bytes.length; i++) {
				String hex = Integer.toHexString(
					0x0100 + (bytes[i] & 0x00FF)).substring(1);

				if (hex.length() < 2) {
					sm.append("0");
				}

				sm.append(hex);
			}

			return sm.toString();
		}

		public static boolean contains(String s, String text) {
			return contains(s, text, StringPool.COMMA);
		}

		public static boolean contains(String s, String text, String delimiter) {
			if ((s == null) || (text == null) || (delimiter == null)) {
				return false;
			}

			StringBuffer sm = null;

			if (!s.endsWith(delimiter)) {
				sm = new StringBuffer();

				sm.append(s);
				sm.append(delimiter);

				s = sm.toString();
			}

			sm = new StringBuffer();

			sm.append(delimiter);
			sm.append(text);
			sm.append(delimiter);

			String dtd = sm.toString();

			int pos = s.indexOf(dtd);

			if (pos == -1) {
				sm = new StringBuffer();

				sm.append(text);
				sm.append(delimiter);

				String td = sm.toString();

				if (s.startsWith(td)) {
					return true;
				}

				return false;
			}

			return true;
		}

		public static boolean contains(String s, String[] p,boolean isUL) {
			boolean isExists=false;
			if(p==null||p.length<=0) {return isExists;}
			
			for(String e:p) {
				if(nds.util.Validator.isNull(s)) {
					isExists=nds.util.Validator.isNull(e);
				}else {
					if(isUL) {isExists=s.equals(e);}
					else {isExists=s.equalsIgnoreCase(e);}
				}
				if(isExists) {break;}
			}
			return isExists;
		}
		
		public static int count(String s, String text) {
			if ((s == null) || (text == null)) {
				return 0;
			}

			int count = 0;

			int pos = s.indexOf(text);

			while (pos != -1) {
				pos = s.indexOf(text, pos + text.length());

				count++;
			}

			return count;
		}

		public static boolean endsWith(String s, char end) {
			return endsWith(s, (new Character(end)).toString());
		}

		public static boolean endsWith(String s, String end) {
			if ((s == null) || (end == null)) {
				return false;
			}

			if (end.length() > s.length()) {
				return false;
			}

			String temp = s.substring(s.length() - end.length(), s.length());

			if (temp.equalsIgnoreCase(end)) {
				return true;
			}
			else {
				return false;
			}
		}

		public static String extractChars(String s) {
			if (s == null) {
				return StringPool.BLANK;
			}

			StringBuffer sm = new StringBuffer();

			char[] c = s.toCharArray();

			for (int i = 0; i < c.length; i++) {
				if (Validator.isChar(c[i])) {
					sm.append(c[i]);
				}
			}

			return sm.toString();
		}

		public static String extractDigits(String s) {
			if (s == null) {
				return StringPool.BLANK;
			}

			StringBuffer sm = new StringBuffer();

			char[] c = s.toCharArray();

			for (int i = 0; i < c.length; i++) {
				if (Validator.isDigit(c[i])) {
					sm.append(c[i]);
				}
			}

			return sm.toString();
		}

		public static String extractFirst(String s, String delimiter) {
			if (s == null) {
				return null;
			}
			else {
				String[] array = split(s, delimiter);

				if (array.length > 0) {
					return array[0];
				}
				else {
					return null;
				}
			}
		}

		public static String extractLast(String s, String delimiter) {
			if (s == null) {
				return null;
			}
			else {
				String[] array = split(s, delimiter);

				if (array.length > 0) {
					return array[array.length - 1];
				}
				else {
					return null;
				}
			}
		}

		public static String highlight(String s, String keywords) {
			return highlight(s, keywords, "<b>", "</b>");
		}

		public static String highlight(
			String s, String keywords, String highlight1, String highlight2) {

			if (s == null) {
				return null;
			}

			// The problem with using a regexp is that it searches the text in a
			// case insenstive manner but doens't replace the text in a case
			// insenstive manner. So the search results actually get messed up. The
			// best way is to actually parse the results.

			//return s.replaceAll(
			//	"(?i)" + keywords, highlight1 + keywords + highlight2);

			StringBuffer sm = new StringBuffer(StringPool.SPACE);

			StringTokenizer st = new StringTokenizer(s);

			while (st.hasMoreTokens()) {
				String token = st.nextToken();

				if (token.equalsIgnoreCase(keywords)) {
					sm.append(highlight1);
					sm.append(token);
					sm.append(highlight2);
				}
				else {
					sm.append(token);
				}

				if (st.hasMoreTokens()) {
					sm.append(StringPool.SPACE);
				}
			}

			return sm.toString();
		}

		public static String lowerCase(String s) {
			if (s == null) {
				return null;
			}
			else {
				return s.toLowerCase();
			}
		}

		public static String merge(List list) {
			return merge(list, StringPool.COMMA);
		}

		public static String merge(List list, String delimiter) {
			return merge((Object[])list.toArray(
				new Object[list.size()]), delimiter);
		}

		public static String merge(Object[] array) {
			return merge(array, StringPool.COMMA);
		}

		public static String merge(Object[] array, String delimiter) {
			if (array == null) {
				return null;
			}

			StringBuffer sm = new StringBuffer();

			for (int i = 0; i < array.length; i++) {
				sm.append(String.valueOf(array[i]).trim());

				if ((i + 1) != array.length) {
					sm.append(delimiter);
				}
			}

			return sm.toString();
		}
/*
		public static String randomize(String s) {
			return Randomizer.getInstance().randomize(s);
		}
*/
		public static String read(ClassLoader classLoader, String name)
			throws IOException {

			return read(classLoader, name, false);
		}

		public static String read(ClassLoader classLoader, String name, boolean all)
			throws IOException {

			if (all) {
				StringBuffer sm = new StringBuffer();

				Enumeration enu = classLoader.getResources(name);

				while (enu.hasMoreElements()) {
					URL url = (URL)enu.nextElement();

					InputStream is = url.openStream();

					String s = read(is);

					if (s != null) {
						sm.append(s);
						sm.append(StringPool.NEW_LINE);
					}

					is.close();
				}

				return sm.toString().trim();
			}
			else {
				InputStream is = classLoader.getResourceAsStream(name);

				String s = read(is);

				is.close();

				return s;
			}
		}

		public static String read(InputStream is) throws IOException {
			StringBuffer sm = new StringBuffer();

			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			String line = null;

			while ((line = br.readLine()) != null) {
				sm.append(line).append('\n');
			}

			br.close();

			return sm.toString().trim();
		}

		public static String remove(String s, String remove) {
			return remove(s, remove, StringPool.COMMA);
		}

		public static String remove(String s, String remove, String delimiter) {
			if ((s == null) || (remove == null) || (delimiter == null)) {
				return null;
			}

			if (Validator.isNotNull(s) && !s.endsWith(delimiter)) {
				s += delimiter;
			}

			StringBuffer sm = new StringBuffer();

			sm.append(delimiter);
			sm.append(remove);
			sm.append(delimiter);

			String drd = sm.toString();

			sm = new StringBuffer();

			sm.append(remove);
			sm.append(delimiter);

			String rd = sm.toString();

			while (contains(s, remove, delimiter)) {
				int pos = s.indexOf(drd);

				if (pos == -1) {
					if (s.startsWith(rd)) {
						int x = remove.length() + delimiter.length();
						int y = s.length();

						s = s.substring(x, y);
					}
				}
				else {
					int x = pos + remove.length() + delimiter.length();
					int y = s.length();

					sm = new StringBuffer();

					sm.append(s.substring(0, pos));
					sm.append(s.substring(x, y));

					s =  sm.toString();
				}
			}

			return s;
		}

		public static String replace(String s, char oldSub, char newSub) {
			return replace(s, oldSub, new Character(newSub).toString());
		}

		public static String replace(String s, char oldSub, String newSub) {
			if ((s == null) || (newSub == null)) {
				return null;
			}

			StringBuffer sm = new StringBuffer();

			char[] c = s.toCharArray();

			for (int i = 0; i < c.length; i++) {
				if (c[i] == oldSub) {
					sm.append(newSub);
				}
				else {
					sm.append(c[i]);
				}
			}

			return sm.toString();
		}

		public static String replace(String s, String oldSub, String newSub) {
			if ((s == null) || (oldSub == null) || (newSub == null)) {
				return null;
			}

			int y = s.indexOf(oldSub);

			if (y >= 0) {
				StringBuffer sm = new StringBuffer();

				int length = oldSub.length();
				int x = 0;

				while (x <= y) {
					sm.append(s.substring(x, y));
					sm.append(newSub);
					x = y + length;
					y = s.indexOf(oldSub, x);
				}

				sm.append(s.substring(x));

				return sm.toString();
			}
			else {
				return s;
			}
		}

		public static String replace(String s, String[] oldSubs, String[] newSubs) {
			if ((s == null) || (oldSubs == null) || (newSubs == null)) {
				return null;
			}

			if (oldSubs.length != newSubs.length) {
				return s;
			}

			for (int i = 0; i < oldSubs.length; i++) {
				s = replace(s, oldSubs[i], newSubs[i]);
			}

			return s;
		}

		public static String reverse(String s) {
			if (s == null) {
				return null;
			}

			char[] c = s.toCharArray();
			char[] reverse = new char[c.length];

			for (int i = 0; i < c.length; i++) {
				reverse[i] = c[c.length - i - 1];
			}

			return new String(reverse);
		}

		public static String shorten(String s) {
			return shorten(s, 20);
		}

		public static String shorten(String s, int length) {
			return shorten(s, length, "...",true);
		}
		
		public static String shorten(String s, int length,Boolean ignoreWhitespace) {
			return shorten(s, length, "...",ignoreWhitespace);
		}

		public static String shorten(String s, String suffix) {
			return shorten(s, 20, suffix,true);
		}

		public static String shorten(String s, int length, String suffix,Boolean ignoreWhitespace) {
			if (s == null || suffix == null)  {
				return null;
			}

			if (s.length() > length) {
				for (int j = length; j >= 0; j--) {
					if (ignoreWhitespace&&Character.isWhitespace(s.charAt(j))) {
						length = j;

						break;
					}
				}

				StringBuffer sm = new StringBuffer();

				sm.append(s.substring(0, length));
				sm.append(suffix);

				s =  sm.toString();
			}

			return s;
		}

		public static String[] split(String s) {
			return split(s, StringPool.COMMA);
		}

		public static String[] split(String s, String delimiter) {
			if (s == null || delimiter == null) {
				return new String[0];
			}

			s = s.trim();

			if (!s.endsWith(delimiter)) {
				StringBuffer sm = new StringBuffer();

				sm.append(s);
				sm.append(delimiter);

				s = sm.toString();
			}

			if (s.equals(delimiter)) {
				return new String[0];
			}

			List nodeValues = new ArrayList();

			if (delimiter.equals("\n") || delimiter.equals("\r")) {
				try {
					BufferedReader br = new BufferedReader(new StringReader(s));

					String line = null;

					while ((line = br.readLine()) != null) {
						nodeValues.add(line);
					}

					br.close();
				}
				catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
			else {
				int offset = 0;
				int pos = s.indexOf(delimiter, offset);

				while (pos != -1) {
					nodeValues.add(new String(s.substring(offset, pos)));

					offset = pos + delimiter.length();
					pos = s.indexOf(delimiter, offset);
				}
			}

			return (String[])nodeValues.toArray(new String[nodeValues.size()]);
		}

		public static boolean[] split(String s, boolean x) {
			return split(s, StringPool.COMMA, x);
		}

		public static boolean[] split(String s, String delimiter, boolean x) {
			String[] array = split(s, delimiter);
			boolean[] newArray = new boolean[array.length];

			for (int i = 0; i < array.length; i++) {
				boolean value = x;

				try {
					value = Boolean.valueOf(array[i]).booleanValue();
				}
				catch (Exception e) {
				}

				newArray[i] = value;
			}

			return newArray;
		}

		public static double[] split(String s, double x) {
			return split(s, StringPool.COMMA, x);
		}

		public static double[] split(String s, String delimiter, double x) {
			String[] array = split(s, delimiter);
			double[] newArray = new double[array.length];

			for (int i = 0; i < array.length; i++) {
				double value = x;

				try {
					value = Double.parseDouble(array[i]);
				}
				catch (Exception e) {
				}

				newArray[i] = value;
			}

			return newArray;
		}

		public static float[] split(String s, float x) {
			return split(s, StringPool.COMMA, x);
		}

		public static float[] split(String s, String delimiter, float x) {
			String[] array = split(s, delimiter);
			float[] newArray = new float[array.length];

			for (int i = 0; i < array.length; i++) {
				float value = x;

				try {
					value = Float.parseFloat(array[i]);
				}
				catch (Exception e) {
				}

				newArray[i] = value;
			}

			return newArray;
		}

		public static int[] split(String s, int x) {
			return split(s, StringPool.COMMA, x);
		}

		public static int[] split(String s, String delimiter, int x) {
			String[] array = split(s, delimiter);
			int[] newArray = new int[array.length];

			for (int i = 0; i < array.length; i++) {
				int value = x;

				try {
					value = Integer.parseInt(array[i]);
				}
				catch (Exception e) {
				}

				newArray[i] = value;
			}

			return newArray;
		}

		public static long[] split(String s, long x) {
			return split(s, StringPool.COMMA, x);
		}

		public static long[] split(String s, String delimiter, long x) {
			String[] array = split(s, delimiter);
			long[] newArray = new long[array.length];

			for (int i = 0; i < array.length; i++) {
				long value = x;

				try {
					value = Long.parseLong(array[i]);
				}
				catch (Exception e) {
				}

				newArray[i] = value;
			}

			return newArray;
		}

		public static short[] split(String s, short x) {
			return split(s, StringPool.COMMA, x);
		}

		public static short[] split(String s, String delimiter, short x) {
			String[] array = split(s, delimiter);
			short[] newArray = new short[array.length];

			for (int i = 0; i < array.length; i++) {
				short value = x;

				try {
					value = Short.parseShort(array[i]);
				}
				catch (Exception e) {
				}

				newArray[i] = value;
			}

			return newArray;
		}

		public static boolean startsWith(String s, char begin) {
			return startsWith(s, (new Character(begin)).toString());
		}

		public static boolean startsWith(String s, String start) {
			if ((s == null) || (start == null)) {
				return false;
			}

			if (start.length() > s.length()) {
				return false;
			}

			String temp = s.substring(0, start.length());

			if (temp.equalsIgnoreCase(start)) {
				return true;
			}
			else {
				return false;
			}
		}

		public static String trim(String s) {
			return trim(s, null);
		}

		public static String trim(String s, char c) {
			return trim(s, new char[] {c});
		}

		public static String trim(String s, char[] exceptions) {
			if (s == null) {
				return null;
			}

			char[] charArray = s.toCharArray();

			int len = charArray.length;

			int x = 0;
			int y = charArray.length;

			for (int i = 0; i < len; i++) {
				char c = charArray[i];

				if (_isTrimable(c, exceptions)) {
					x = i + 1;
				}
				else {
					break;
				}
			}

			for (int i = len - 1; i >= 0; i--) {
				char c = charArray[i];

				if (_isTrimable(c, exceptions)) {
					y = i;
				}
				else {
					break;
				}
			}

			if ((x != 0) || (y != len)) {
				return s.substring(x, y);
			}
			else {
				return s;
			}
		}

		public static String trimLeading(String s) {
			return trimLeading(s, null);
		}

		public static String trimLeading(String s, char c) {
			return trimLeading(s, new char[] {c});
		}

		public static String trimLeading(String s, char[] exceptions) {
			if (s == null) {
				return null;
			}

			char[] charArray = s.toCharArray();

			int len = charArray.length;

			int x = 0;
			int y = charArray.length;

			for (int i = 0; i < len; i++) {
				char c = charArray[i];

				if (_isTrimable(c, exceptions)) {
					x = i + 1;
				}
				else {
					break;
				}
			}

			if ((x != 0) || (y != len)) {
				return s.substring(x, y);
			}
			else {
				return s;
			}
		}

		public static String trimTrailing(String s) {
			return trimTrailing(s, null);
		}

		public static String trimTrailing(String s, char c) {
			return trimTrailing(s, new char[] {c});
		}

		public static String trimTrailing(String s, char[] exceptions) {
			if (s == null) {
				return null;
			}

			char[] charArray = s.toCharArray();

			int len = charArray.length;

			int x = 0;
			int y = charArray.length;

			for (int i = len - 1; i >= 0; i--) {
				char c = charArray[i];

				if (_isTrimable(c, exceptions)) {
					y = i;
				}
				else {
					break;
				}
			}

			if ((x != 0) || (y != len)) {
				return s.substring(x, y);
			}
			else {
				return s;
			}
		}

		public static String upperCase(String s) {
			if (s == null) {
				return null;
			}
			else {
				return s.toUpperCase();
			}
		}

		public static String wrap(String text) {
			return wrap(text, 80, "\n");
		}

		public static String wrap(String text, int width, String lineSeparator) {
			if (text == null) {
				return null;
			}

			StringBuffer sm = new StringBuffer();

			try {
				BufferedReader br = new BufferedReader(new StringReader(text));

				String s = StringPool.BLANK;

				while ((s = br.readLine()) != null) {
					if (s.length() == 0) {
						sm.append(lineSeparator);
					}
					else {
						String[] tokens = s.split(StringPool.SPACE);
						boolean firstWord = true;
						int curLineLength = 0;

						for (int i = 0; i < tokens.length; i++) {
							if (!firstWord) {
								sm.append(StringPool.SPACE);
								curLineLength++;
							}

							if (firstWord) {
								sm.append(lineSeparator);
							}

							sm.append(tokens[i]);

							curLineLength += tokens[i].length();

							if (curLineLength >= width) {
								firstWord = true;
								curLineLength = 0;
							}
							else {
								firstWord = false;
							}
						}
					}
				}
			}
			catch (IOException ioe) {
				ioe.printStackTrace();
			}

			return sm.toString();
		}

		private static boolean _isTrimable(char c, char[] exceptions) {
			if ((exceptions != null) && (exceptions.length > 0)) {
				for (int i = 0; i < exceptions.length; i++) {
					if (c == exceptions[i]) {
						return false;
					}
				}
			}

			return Character.isWhitespace(c);
		}

	}

/* Location:           E:\portal5\portal422\server\default\deploy\nds.war\WEB-INF\classes\
 * Qualified Name:     nds.velocity.StringUtil
 * JD-Core Version:    0.6.2
 */