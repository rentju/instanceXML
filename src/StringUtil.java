import java.io.*;
import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class StringUtil {
	public static String WINDOWS_LINE_SEPARATOR = "\r\n";
	public static String LINUX_LINE_SEPARATOR = "\n";

	public static boolean isMultipleLines(String text) {
		if (isEmpty(text)) {
			return false;
		}
		String tmpText = text.trim();
		return tmpText.contains(LINUX_LINE_SEPARATOR);
	}

	public static String repeat(String pattern, int repeat) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < repeat; i++) {
			sb.append(pattern);
		}
		return sb.toString();
	}

	public static boolean isNull(Object obj) {
		return obj == null;
	}

	public static boolean isMultipleLines(Object text) {
		String v = text == null ? "" : String.valueOf(text);
		return isMultipleLines(v);
	}

	public static String removeTrailingColonOfLabelText(String labelText) {
		int idx = labelText.lastIndexOf(":");
		if (idx == -1) {
			return labelText;
		}
		return labelText.substring(0, idx);
	}

	public static String valueOf(Object obj) {
		if (obj == null) {
			return "null";
		} else if (obj.getClass().isArray()) {
			return valueOfArray(obj);
		} else {
			return String.valueOf(obj);
		}
	}

	public static String valueOfArray(Object array) {
		if (!array.getClass().isArray()) {
			throw new RuntimeException("It is not an array : " + array);
		}
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		final int length = Array.getLength(array);
		for (int i = 0; i < length; i++) {
			Object obj = Array.get(array, i);
			String element = valueOf(obj);
			sb.append("\"" + element + "\"");
			if (i < length - 1) {
				sb.append(",");
			}
		}
		sb.append("}");
		return sb.toString();
	}

	/**
	 * pick up the maximal and minimal string in a list of string
	 *
	 * @param strings
	 * @return string1 will be the maximal one, string 2 will be the minimal one
	 */
	public static PairValue<String, String> getMaxAndMin(List<String> strings) {
		PairValue<String, String> p = new PairValue<String, String>();
		if (strings.size() == 0) {
			return p;
		}
		p.first = strings.get(0);
		p.second = strings.get(0);
		for (String s : strings) {
			if (s.compareTo(p.second) < 0) {
				p.second = s;
			} else if (s.compareTo(p.first) > 0) {
				p.first = s;
			}
		}
		return p;
	}

	public final static int toInt(String string, int defaultValue) {
		if (isEmpty(string)) {
			return defaultValue;
		}
		try {
			string = string.trim();
			return Integer.parseInt(string);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public final static int toInt(String string) {
		return toInt(string, 0);
	}

	public final static List<String> trimElement(List<String> list) {
		for (int si = 0; si < list.size(); si++) {
			String s = list.get(si);
			if (s != null) {
				list.set(si, s.trim());
			}
		}
		return list;
	}

	public final static List<String> removeNullOrEmptyElement(List<String> list) {
		for (int si = 0; si < list.size(); ) {
			String s = list.get(si);
			if (s == null) {
				list.remove(si);
			} else if (s.isEmpty()) {
				list.remove(si);
			} else {
				si++;
			}
		}
		return list;
	}

	public final static boolean containsNullOrEmptyElement(List<String> list) {
		for (int si = 0; si < list.size(); si++) {
			String s = list.get(si);
			if (s == null) {
				return true;
			} else if (s.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	public final static double toDouble(String string, double defaultValue) {
		try {
			string = string.trim();
			return Double.parseDouble(string);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public static List<List<String>> toStringTable(ResultSet resultSet) {
		List<List<String>> table = new ArrayList<List<String>>();
		try {
			ResultSetMetaData md = resultSet.getMetaData();
			final int colCount = md.getColumnCount();
			// 1.read the data row by row
			resultSet.beforeFirst();
			while (resultSet.next()) {
				// convert the whole record into a string
				List<String> record = new ArrayList<String>();
				for (int i = 1; i <= colCount; i++) {
					String cv = resultSet.getString(i);
					record.add(nonNull(cv));
				}
				// dump the record
				table.add(record);
			}
			// 2.move cursor back to just before first row
			resultSet.beforeFirst();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return table;
	}

	public final static double toDouble(String string) {
		return toDouble(string, 0.0F);
	}

	/**
	 * @param table
	 * @param columnToDelete 0 based
	 * @return
	 */
	public static List<List<String>> removeTableColumn(List<List<String>> table, int columnToDelete) {
		if (table == null) {
			return table;
		}
		for (int ri = 0; ri < table.size(); ri++) {
			List<String> row = table.get(ri);
			row.remove(columnToDelete);
			table.set(ri, row);
		}
		return table;
	}

	public static List<List<String>> removeTableFieldEnclosingQuotationMarks(List<List<String>> table) {
		if (table == null) {
			return table;
		}
		for (int ri = 0; ri < table.size(); ri++) {
			List<String> row = table.get(ri);
			for (int fi = 0; fi < row.size(); fi++) {
				String field = row.get(fi);
				field = removeEnclosingQuotationMarks(field);
				row.set(fi, field);
			}
			table.set(ri, row);
		}
		return table;
	}

	private static final String QUOTATOIN_MARK = "\"";

	public static boolean isEnclosedByQuotationMarks(String field) {
		return field.startsWith(QUOTATOIN_MARK) && field.endsWith(QUOTATOIN_MARK);
	}

	public static String removeEnclosingQuotationMarks(String field) {
		boolean yes = isEnclosedByQuotationMarks(field);
		if (!yes) {
			return field;
		}
		String newField = field.substring(1, field.length() - 1);
		return newField;
	}

	/**
	 * @param table
	 * @param rowToDelete 0 based
	 * @return
	 */
	public static List<List<String>> removeTableRow(List<List<String>> table, int rowToDelete) {
		if (table == null) {
			return table;
		}
		table.remove(rowToDelete);
		return table;
	}

	public final static boolean toBoolean(String string) {
		if (string == null) {
			return false;
		}
		string = string.trim();
		final String BOOLEAN_TRUE = "true";
		return BOOLEAN_TRUE.equalsIgnoreCase(string);
	}

	public final static String fromDate(Date date) throws Exception {
		String format = "yyyy-MM-dd HH:mm:ss.SSS";
		DateFormat sf = new SimpleDateFormat(format, Locale.US);
		return sf.format(date);
	}

	public static boolean isEmpty(String s) {
		boolean empty = (s == null || s.trim().isEmpty());
		return empty;
	}

	public static boolean isNotEmpty(String s) {
		return !isEmpty(s);
	}

	public static String nonNull(String s) {
		if (s == null) {
			return "";
		} else {
			return s;
		}
	}

	public final static List<String> parseLines(String text) {
		List<String> lines = new ArrayList<String>();
		if (text == null) {
			return lines;
		}
		try {
			StringReader sr = new StringReader(text);
			BufferedReader br = new BufferedReader(sr);
			while (true) {
				String line = br.readLine();
				if (line == null) {
					break;
				} else {
					lines.add(line);
				}
			}
			return lines;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public final static List<String> parseTokens(String line, String splitter, boolean trimToken) {
		List<String> tokens = parseTokens(line, splitter, trimToken, false);
		return tokens;
	}

	public final static List<String> parseTokens(String line, String splitter, boolean trimToken, boolean discardEmptyToken) {
		List<String> tokens = new ArrayList<String>();
		if (line == null) {
			return tokens;
		}
		String[] tks = line.split(splitter);
		if (trimToken) {
			for (int ti = 0; ti < tks.length; ti++) {
				String tk = tks[ti];
				tks[ti] = tk.trim();
			}
		}
		if (discardEmptyToken) {
			for (int ti = 0; ti < tks.length; ti++) {
				String tk = tks[ti];
				if (isEmpty(tk)) {
					continue;
				}
				tokens.add(tk);
			}
		} else {
			tokens = toStringList(tks);
		}
		return tokens;
	}

	/**
	 * parse name and value from a string nameValurePair using splitter and trim
	 * found name and value
	 *
	 * @param nameValuePair
	 * @param splitter
	 * @return
	 */
	public static PairValue<String, String> parseNamedValue(String nameValuePair, String splitter) {
		if (StringUtil.isEmpty(splitter) || nameValuePair == null) {
			throw new AssertException("Empty splitter or null string to split is not accepted : [" + nameValuePair + "," + splitter + "]");
		}
		final boolean trimToken = true;
		final boolean discardEmptyToken = true;
		final String SPLITTER = splitter;
		List<String> tokens = StringUtil.parseTokens(nameValuePair, SPLITTER, trimToken, discardEmptyToken);
		if (tokens.isEmpty()) {
			throw new RuntimeException("Invalid format of named value '" + nameValuePair + "', expected format [name " + SPLITTER + " value]");
		}
		PairValue<String, String> p = new PairValue<String, String>();
		p.first = tokens.get(0);
		final int valueStart = p.first.length() + 1;
		p.second = valueStart < nameValuePair.length() ? nameValuePair.substring(valueStart).trim() : "";
		return p;
	}

	/**
	 * if any string in expected is contained by actual return true
	 *
	 * @param actual
	 * @param expected
	 * @param caseSensitive
	 * @return
	 */
	public final static boolean containsAny(String actual, String[] expected, boolean caseSensitive) {
		if (actual == null) {
			actual = "";
		}
		if (expected == null) {
			expected = new String[0];
		}
		for (int si = 0; si < expected.length; si++) {
			boolean suc = contains(actual, expected[si], caseSensitive);
			if (suc) {
				return true;
			}
		}
		return false;
	}

	/**
	 * check if any string in expected is contained by actual
	 *
	 * @param actual
	 * @param expected
	 * @param caseSensitive
	 * @return
	 */
	public final static boolean containsAll(String actual, String[] expected, boolean caseSensitive) {
		if (actual == null) {
			actual = "";
		}
		List<String> expectedToBeContained = toStringList(expected);
		for (int si = 0; si < expectedToBeContained.size(); si++) {
			boolean suc = contains(actual, expectedToBeContained.get(si), caseSensitive);
			if (!suc) {
				return false;
			}
		}
		return true;
	}

	public final static boolean contains(String str, String strExpectedToBeContained) {
		final boolean caseSensitive = true;
		return contains(str, strExpectedToBeContained, caseSensitive);
	}

	public final static String normalizeSpace(String str) {
		if (isEmpty(str)) {
			return nonNull(str);
		}
		String result = replace(str, "\\s{2,}", " ");
		return result;
	}

	public final static boolean contains(String str, String strExpectedToBeContained, boolean caseSensitive) {
		String master = String.valueOf(str);
		String token = String.valueOf(strExpectedToBeContained);
		if (!caseSensitive) {
			master = master.toLowerCase();
			token = token.toLowerCase();
		}
		return master.contains(token);
	}

	public static List<String> toLowerCase(List<String> list) {
		if (list == null) {
			return list;
		}
		for (int i = 0; i < list.size(); i++) {
			String e = list.get(i);
			if (e != null) {
				list.set(i, e.toLowerCase());
			}
		}
		return list;
	}

	public static List<String> toUpperCase(List<String> list) {
		if (list == null) {
			return list;
		}
		for (int i = 0; i < list.size(); i++) {
			String e = list.get(i);
			if (e != null) {
				list.set(i, e.toUpperCase());
			}
		}
		return list;
	}

	public static String[] toStringArray(List<String> stringList) {
		if (stringList == null) {
			return new String[0];
		}
		return stringList.toArray(new String[0]);
	}

	public static List<String> toStringList(String[] stringArray) {
		if (stringArray == null) {
			return new ArrayList<String>();
		}
		List<String> sl = new ArrayList<String>();
		for (int si = 0; si < stringArray.length; si++)
			sl.add(stringArray[si]);
		return sl;
	}

	public static List<String> toStringList(String string) {
		if (string == null) {
			return new ArrayList<String>();
		}
		List<String> sl = new ArrayList<String>();
		sl.add(string);
		return sl;
	}

	public static String toPlainText(String[] stringArray, String separator) {
		List<String> stringList = toStringList(stringArray);
		return toPlainText(stringList, separator);
	}

	public static String toPlainText(List<String> stringList, String separator) {
		if (stringList == null) {
			return "";
		}
		final String sep = isEmpty(separator) ? "" : separator;
		StringBuilder sb = new StringBuilder();
		for (String element : stringList) {
			sb.append(element + sep);
		}
		return sb.toString();
	}

	/**
	 * convert the whole table into a plain text string, each row put in one
	 * single line, and cells separated by a whitespace ; lines are separated by
	 * "\r\n"
	 *
	 * @param table
	 * @param trim  to trim each cell text
	 * @return the string containing the plain text
	 */
	public static String toPlainText(List<List<String>> table, boolean trim) {
		if (table == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		final int idx_last_row = table.size() - 1;
		for (int ri = 0; ri < table.size(); ri++) {
			List<String> row = table.get(ri);
			for (int ci = 0; ci < row.size(); ci++) {
				String cell = row.get(ci);
				if (trim) {
					cell = cell.trim();
				}
				sb.append(cell);
				sb.append(",");
			}
			if (ri != idx_last_row) {
				sb.append(WINDOWS_LINE_SEPARATOR);
			}
		}
		return sb.toString();
	}

	/**
	 * compare if 2 tables of string are same, assume the table already sorted
	 *
	 * @param table1
	 * @param table2
	 * @return
	 */
	public static PairValue<Boolean, String> tableEquals(List<List<String>> table1, List<List<String>> table2) {
		PairValue<Boolean, String> result = new PairValue<Boolean, String>(false, "");
		if (table1 == null && table2 == null) {
			result.first = true;
			result.second = "Both table are null";
			return result;
		} else if (table1 == null || table2 == null) {
			result.first = false;
			result.second = "One table is null while the other not null";
			return result;
		}
		int size1 = table1.size();
		int size2 = table2.size();
		if (size1 != size2) {
			result.first = false;
			result.second = "The 2 tables are not of same size :" + size1 + "," + size2 + "";
			return result;
		}
		for (int ri = 0; ri < table1.size(); ri++) {
			List<String> row1 = table1.get(ri);
			List<String> row2 = table2.get(ri);
			PairValue<Boolean, String> subResult = listEquals(row1, row2);
			if (!subResult.first) {
				result.first = false;
				result.second = "The 2 tables are different, the first different row " + ri + ":" + row1.toString() + "," + row2.toString();
				return result;
			}
		}
		result.first = true;
		result.second = "same";
		return result;
	}

	/**
	 * check if all rows in table2 with size N are same with the first N rows of
	 * table1
	 *
	 * @param table1
	 * @param table2
	 * @return
	 */
	public static PairValue<Boolean, String> tableContains(List<List<String>> table1, List<List<String>> table2) {
		PairValue<Boolean, String> result = new PairValue<Boolean, String>(false, "");
		if (table1 == null && table2 == null) {
			result.first = true;
			result.second = "Both table are null";
			return result;
		} else if (table1 == null || table2 == null) {
			result.first = false;
			result.second = "One table is null while the other not null";
			return result;
		}
		int size1 = table1.size();
		int size2 = table2.size();
		if (size1 < size2) {
			result.first = false;
			result.second = "The table expected to be contained by the other table is bigger :" + size1 + "," + size2 + "";
			return result;
		}
		for (int ri = 0; ri < table2.size(); ri++) {
			List<String> row1 = table1.get(ri);
			List<String> row2 = table2.get(ri);
			PairValue<Boolean, String> subResult = listEquals(row1, row2);
			if (!subResult.first) {
				result.first = false;
				result.second = "The first different row " + ri + ":" + row1.toString() + "," + row2.toString();
				return result;
			}
		}
		result.first = true;
		result.second = "contained";
		return result;
	}

	/**
	 * check if list1 contains all elements of list2
	 *
	 * @param container
	 * @param toBeContained
	 * @return
	 */
	public static PairValue<Boolean, String> setContains(List<String> container, List<String> toBeContained) {
		PairValue<Boolean, String> result = new PairValue<Boolean, String>(false, "");
		if (container == null && toBeContained == null) {
			result.first = true;
			result.second = "Both sets are null";
			return result;
		} else if (container == null && toBeContained != null) {
			result.first = false;
			result.second = "The first set is expected to contain the second set, but the first set is null while the second one contains something";
			return result;
		}
		String sList1 = container.toString();
		String sList2 = toBeContained.toString();
		for (String candidate : toBeContained) {
			if (!container.contains(candidate)) {
				result.first = false;
				result.second = "The first set does not contain the second set : " + WINDOWS_LINE_SEPARATOR + sList1 + WINDOWS_LINE_SEPARATOR
						+ sList2;
				result.second += WINDOWS_LINE_SEPARATOR;
				result.second += "Element '" + candidate + "' of second set does not exist in the first set";
				return result;
			}
		}
		result.first = true;
		result.second = "The first set contains any element of the second one : " + WINDOWS_LINE_SEPARATOR + sList1 + WINDOWS_LINE_SEPARATOR + sList2;
		return result;
	}

	public static PairValue<Boolean, String> setContainsNo(List<String> container, String[] noneElementToBeContained) {
		return setContainsNo(container, toStringList(noneElementToBeContained));
	}

	public static PairValue<Boolean, String> setContainsNo(List<String> container, List<String> noneElementToBeContained) {
		PairValue<Boolean, String> result = new PairValue<Boolean, String>(false, "");
		if (container == null && noneElementToBeContained == null) {
			result.first = false;
			result.second = "Both sets are null";
			return result;
		} else if (container == null && noneElementToBeContained != null) {
			result.first = true;
			result.second = "The first set is expected to contain none in the second set, and the first set is null while the second one contains something";
			return result;
		}
		String sList1 = container.toString();
		String sList2 = noneElementToBeContained.toString();
		for (String elementNotToBeContained : noneElementToBeContained) {
			if (container.contains(elementNotToBeContained)) {
				result.first = false;
				result.second = "The first set contains some element of the second set : " + WINDOWS_LINE_SEPARATOR + sList1 + WINDOWS_LINE_SEPARATOR
						+ sList2;
				result.second += WINDOWS_LINE_SEPARATOR;
				result.second += "Element '" + elementNotToBeContained + "' exists in both sets";
				return result;
			}
		}
		result.first = true;
		result.second = "The first set does not contain any element of second set : " + WINDOWS_LINE_SEPARATOR + sList1 + WINDOWS_LINE_SEPARATOR
				+ sList2;
		return result;
	}

	public static PairValue<Boolean, String> listContains(List<String> container, List<String> toBeContained, boolean startWith) {
		if (!startWith) {
			return setContains(container, toBeContained);
		}
		PairValue<Boolean, String> result = new PairValue<Boolean, String>(false, "");
		if (container == null && toBeContained == null) {
			result.first = true;
			result.second = "Both lists are null";
			return result;
		} else if (container == null && toBeContained != null) {
			result.first = false;
			result.second = "The first list is expected to contain the second list, but the first list is null while the second one contains something";
			return result;
		}
		final String sList1 = container.toString();
		final String sList2 = toBeContained.toString();
		final String tmpList1 = sList1.replace("[", ", ");
		for (String candidate : toBeContained) {
			if (!tmpList1.contains(", " + candidate)) {
				result.first = false;
				result.second = "The first list does contains the second list : " + WINDOWS_LINE_SEPARATOR + sList1 + WINDOWS_LINE_SEPARATOR + sList2;
				result.second += WINDOWS_LINE_SEPARATOR;
				result.second += "Element '" + candidate + "' of second list does not exist in the first list";
				return result;
			}
		}
		result.first = true;
		result.second = "The first list contains the second list : " + WINDOWS_LINE_SEPARATOR + sList1 + WINDOWS_LINE_SEPARATOR + sList2;
		return result;
	}

	/**
	 * Compare the two list completely same
	 *
	 * @param list1
	 * @param list2
	 * @return
	 */
	public static PairValue<Boolean, String> listEquals(List<String> list1, List<String> list2) {
		PairValue<Boolean, String> result = new PairValue<Boolean, String>(false, "");
		if (list1 == null && list2 == null) {
			result.first = true;
			result.second = "Both lists are null";
			return result;
		} else if (list1 == null || list2 == null) {
			result.first = false;
			result.second = "One is null while the other not null";
			return result;
		}
		String sList1 = list1.toString();
		String sList2 = list2.toString();
		int size1 = list1.size();
		int size2 = list2.size();
		if (size1 != size2) {
			result.first = false;
			result.second = "The 2 lists are not of same size :" + size1 + "," + size2 + WINDOWS_LINE_SEPARATOR + sList1 + WINDOWS_LINE_SEPARATOR
					+ sList2;
			return result;
		}
		if (!sList1.equals(sList2)) {
			result.first = false;
			result.second = "The 2 lists are not same :" + sList1 + "," + sList2 + "";
			return result;
		}
		result.first = true;
		result.second = "The 2 lists are same : " + WINDOWS_LINE_SEPARATOR + sList1 + WINDOWS_LINE_SEPARATOR + sList2;
		return result;
	}

	/**
	 * Compare the two sets, expected that they contains same elements
	 *
	 * @param set1
	 * @param set2
	 * @return
	 */
	public static PairValue<Boolean, String> setEquals(List<String> set1, List<String> set2) {
		PairValue<Boolean, String> result = new PairValue<Boolean, String>(false, "");
		if (set1 == null && set2 == null) {
			result.first = true;
			result.second = "Both sets are null";
			return result;
		} else if (set1 == null || set2 == null) {
			result.first = false;
			result.second = "One set is null while the other not null";
			return result;
		}
		String sList1 = set1.toString();
		String sList2 = set2.toString();
		int size1 = set1.size();
		int size2 = set2.size();
		if (size1 != size2) {
			result.first = false;
			result.second = "The 2 sets are not of same size :" + size1 + "," + size2 + WINDOWS_LINE_SEPARATOR + sList1 + WINDOWS_LINE_SEPARATOR
					+ sList2;
			return result;
		}
		PairValue<Boolean, String> resultSet1ContainsSet2 = setContains(set1, set2);
		PairValue<Boolean, String> resultSet2ContainsSet1 = setContains(set2, set1);
		result.first = resultSet1ContainsSet2.first && resultSet2ContainsSet1.first;
		if (result.first) {
			result.second = "The 2 sets are same : " + WINDOWS_LINE_SEPARATOR + sList1 + WINDOWS_LINE_SEPARATOR + sList2;
		} else {
			result.second = "The 2 sets are of same size but containing different elements : " + WINDOWS_LINE_SEPARATOR + sList1
					+ WINDOWS_LINE_SEPARATOR + sList2;
		}
		return result;
	}

	private final static String INTEGER_FORMAT = "-?\\d+";

	/**
	 * parse and return integers from string
	 *
	 * @param str
	 * @return
	 */
	public static List<Integer> findIntegers(String str) {
		List<Integer> ints = new ArrayList<Integer>();
		Pattern pattern = Pattern.compile(INTEGER_FORMAT);
		Matcher matcher = pattern.matcher(str);
		while (matcher.find()) {
			String group = matcher.group();
			Integer num = toInt(group);
			ints.add(num);
		}
		return ints;
	}

	/**
	 * parse and return first found integer in the string
	 *
	 * @param str
	 * @return the integer value of found integer, null if nothing found
	 */
	public static Integer findInteger(String str) {
		Pattern pattern = Pattern.compile(INTEGER_FORMAT);
		Matcher matcher = pattern.matcher(str);
		if (!matcher.find()) {
			return null;
		}
		String group = matcher.group();
		int num = toInt(group);
		return num;
	}

	public static String findFirstMatch(String str, String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(str);
		if (!matcher.find()) {
			return "";
		}
		String group = matcher.group();
		return group;
	}

	public static PairValue<Boolean, String> matches(String str, String regex) {
		final boolean caseInsensitive = false;
		PairValue<Boolean, String> result = matches(str, regex, caseInsensitive);
		return result;
	}

	public static PairValue<Boolean, String> matches(String str, String regex, boolean caseInsensitive) {
		PairValue<Boolean, String> result = new PairValue<Boolean, String>(false, "");
		Pattern pattern = caseInsensitive ? Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL) : Pattern
				.compile(regex);
		Matcher matcher = pattern.matcher(str);
		result.first = matcher.find();
		StringBuilder sb = new StringBuilder();
		sb.append("String is epxected to match regular expression , ");
		sb.append(result.first ? "and it does" : "but it does not");
		sb.append(WINDOWS_LINE_SEPARATOR);
		sb.append("Actual : [" + str + "]" + WINDOWS_LINE_SEPARATOR);
		sb.append("Regex  : [" + regex + "]" + WINDOWS_LINE_SEPARATOR);
		result.second = sb.toString();
		return result;
	}

	public static String fromException(Throwable e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		String stackTrace = sw.toString();
		return stackTrace;
	}

	/**
	 * Check whether all the given regex contained in the expected list
	 *
	 * @param expectedList
	 * @param regexList
	 * @return
	 */
	public static PairValue<Boolean, String> containRegexList(List<String> expectedList, String[] regexList) {
		PairValue<Boolean, String> result = new PairValue<Boolean, String>(true, "");
		for (String regex : regexList) {
			PairValue<Boolean, String> currResult = new PairValue<Boolean, String>(false, "");
			for (String str : expectedList) {
				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = pattern.matcher(str);
				currResult.first = matcher.find();
				currResult.second = "Regex: [" + regex + "]" + WINDOWS_LINE_SEPARATOR;
				if (currResult.first) {
					break;
				}
			}
			result.first &= currResult.first;
			if (!currResult.first) {
				result.second += currResult.second;
			}
		}
		if (result.first) {
			result.second = "All regex can be found in the expected list" + WINDOWS_LINE_SEPARATOR;
		} else {
			result.second = "Regexes cannot be found: " + WINDOWS_LINE_SEPARATOR + result.second;
		}
		return result;
	}

	public static PairValue<Boolean, String> equals(String actual, String expected) {
		boolean normalizeLineSeparator = false;
		return equals(actual, expected, normalizeLineSeparator);
	}

	public static String removeLineSperators(String str) {
		str = str.replace(WINDOWS_LINE_SEPARATOR, "");
		str = str.replace(LINUX_LINE_SEPARATOR, "");
		return str;
	}

	public static PairValue<Boolean, String> equals(String actual, String expected, boolean normalizeLineSeparator) {
		return equals(actual, expected, normalizeLineSeparator, true);
	}

	/**
	 * @param actual
	 * @param expected
	 * @param normalizeLineSeparator true-look windows line separator and linux line separator as
	 *                               same
	 * @param caseSensitive
	 * @return
	 */
	public static PairValue<Boolean, String> equals(String actual, String expected, boolean normalizeLineSeparator, boolean caseSensitive) {
		PairValue<Boolean, String> result = new PairValue<Boolean, String>(false, "");
		StringBuilder sb = new StringBuilder();
		if (actual == null && expected == null) {
			result.first = true;
			result.second = "Both are null";
			return result;
		} else if (actual == null || expected == null) {
			result.first = false;
			if (result.first) {
				sb.append("2 strings are same\n");
			} else {
				sb.append("2 strings are different\n");
			}
			actual = (actual == null) ? "NULL" : actual;
			expected = (expected == null) ? "NULL" : expected;
			sb.append("Actual  :( " + actual.length() + " characters)" + WINDOWS_LINE_SEPARATOR);
			sb.append("[" + actual + "]" + WINDOWS_LINE_SEPARATOR);
			sb.append("Expected:( " + expected.length() + " characters)" + WINDOWS_LINE_SEPARATOR);
			sb.append("[" + expected + "]" + WINDOWS_LINE_SEPARATOR);
			result.second = sb.toString();
			return result;
		}
		if (normalizeLineSeparator) {
			String tmpActual = actual.replace(WINDOWS_LINE_SEPARATOR, "\n");
			String tmpExpected = expected.replace(WINDOWS_LINE_SEPARATOR, "\n");
			result.first = caseSensitive ? tmpActual.equals(tmpExpected) : tmpActual.equalsIgnoreCase(tmpExpected);
			sb.append("Ignore the line separator difference : " + normalizeLineSeparator + WINDOWS_LINE_SEPARATOR);
		} else {
			result.first = caseSensitive ? actual.equals(expected) : actual.equalsIgnoreCase(expected);
		}
		if (result.first) {
			sb.append("2 strings are same\n");
		} else {
			sb.append("2 strings are different\n");
		}
		sb.append("Actual  :( " + actual.length() + " characters)" + WINDOWS_LINE_SEPARATOR);
		sb.append("[" + actual + "]" + WINDOWS_LINE_SEPARATOR);
		sb.append("Expected:( " + expected.length() + " characters)" + WINDOWS_LINE_SEPARATOR);
		sb.append("[" + expected + "]" + WINDOWS_LINE_SEPARATOR);
		result.second = sb.toString();
		return result;
	}

	/**
	 * @param str
	 * @return
	 * @author whb Change the given string as first charactor to be
	 * uppercased only
	 */
	public static String makeUpperFirstChar(String str) {
		String firstChar = str.substring(0, 1).toUpperCase();
		String leftChars = str.substring(1).toLowerCase();
		return firstChar + leftChars;
	}

	/**
	 * Replace target string in template.
	 *
	 * @param template
	 * @param replacements
	 * @return
	 */
	public static String replace(String template, Properties replacements) {
		String result = template;
		for (Object key : replacements.keySet()) {
			final String sourceTokenFilter = key.toString();
			final String targetValue = nonNull(replacements.getProperty(sourceTokenFilter));
			Pattern pattern = Pattern.compile(sourceTokenFilter);
			Matcher matcher = pattern.matcher(result);
			// while (matcher.find()) {
			// System.out.println(" matcher.group(): " + matcher.group());
			// }
			result = matcher.replaceAll(targetValue);
		}
		return result;
	}

	public static String replace(String str, String regex, String newValue) {
		Properties replacements = new Properties();
		replacements.put(regex, newValue);
		return replace(str, replacements);
	}

	public static List<String> extractLine(String source, String regex) {
		BufferedReader reader = new BufferedReader(new StringReader(source));
		Pattern p = Pattern.compile(regex);
		List<String> ret = new ArrayList<String>();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				Matcher m = p.matcher(line);
				if (m.find()) {
					ret.add(m.group(1));
				}
			}
		} catch (IOException e) {
			return Collections.emptyList();
		}
		return ret;
	}

	public static List<String> grepLine(String source, String regex) {
		BufferedReader reader = new BufferedReader(new StringReader(source));
		Pattern p = Pattern.compile(regex);
		List<String> ret = new ArrayList<String>();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				Matcher m = p.matcher(line);
				if (m.find()) {
					ret.add(line);
				}
			}
		} catch (IOException e) {
			return Collections.emptyList();
		}
		return ret;
	}

	public static void main(String args[]) throws Exception {
		utNormalizeSpace();
	}

	protected static void utNormalizeSpace() {
		String str = "1,         0.03,select count(table_name),tablespace_name,sysdate from user_t,Statement processed.,1,";
		System.out.println(normalizeSpace(str));
	}

	protected static void utRegEx() {
		String master = "<HTML> <b> </HTML >";
		final String HTML_TAG_PATTERN = "^.*(</?([^>]*)>).*$";
		boolean match = master.matches(HTML_TAG_PATTERN);
		System.out.println(match);
	}

	protected static void utReplaceWithRegEx() {
		String master = "<html><a href=\"localhost:8080\"> My home page </a> <a href=\"localhost:8081\"> your home page</a> </HTML >";
		final String HTML_TAG_PATTERN = "<a href=\"([^\"]+)\">";
		master = replace(master, HTML_TAG_PATTERN, "<a>");
		System.out.println(master);
	}
}