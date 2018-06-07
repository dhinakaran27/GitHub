package com.unilever.rac.ui.common;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class UL4URLDecoder {
	static String dfltEncName = "UTF-8";

	@Deprecated
	public static String decode(String paramString) {
		String str = null;
		try {
			str = decode(paramString, dfltEncName);
		} catch (UnsupportedEncodingException localUnsupportedEncodingException) {
		}
		return str;
	}

	public static String decode(String paramString1, String paramString2)
			throws UnsupportedEncodingException {
		int i = 0;
		int j = paramString1.length();
		StringBuffer localStringBuffer = new StringBuffer((j > 500) ? j / 2 : j);
		int k = 0;
		if (paramString2.length() == 0)
			throw new UnsupportedEncodingException(
					"URLDecoder: empty string enc parameter");
		byte[] arrayOfByte = null;
		while (k < j) {
			char c = paramString1.charAt(k);
			switch (c) {
			case '%':
				try {
					if (arrayOfByte == null)
						arrayOfByte = new byte[(j - k) / 3];
					int l = 0;
					while ((k + 2 < j) && (c == '%')) {
						int i1 = Integer.parseInt(
								paramString1.substring(k + 1, k + 3), 16);
						if (i1 < 0)
							throw new IllegalArgumentException(
									"URLDecoder: Illegal hex characters in escape (%) pattern - negative value");
						arrayOfByte[(l++)] = (byte) i1;
						if ((k += 3) < j)
							c = paramString1.charAt(k);
					}
					if ((k < j) && (c == '%'))
						throw new IllegalArgumentException(
								"URLDecoder: Incomplete trailing escape (%) pattern");
					localStringBuffer.append(new String(arrayOfByte, 0, l,
							paramString2));
				} catch (NumberFormatException localNumberFormatException) {
					throw new IllegalArgumentException(
							"URLDecoder: Illegal hex characters in escape (%) pattern - "
									+ localNumberFormatException.getMessage());
				}
				i = 1;
				break;
			}
			localStringBuffer.append(c);
			++k;
		}
		return ((i != 0) ? localStringBuffer.toString() : paramString1);
	}
}