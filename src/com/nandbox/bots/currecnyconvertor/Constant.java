package com.nandbox.bots.currecnyconvertor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Constant {

	private Constant() {

	}

	public static final String CURRENCY_CODE = ("AED - United Arab Emirates Dirham\r\n" + "ARS - Argentine Peso\r\n"
			+ "AUD - Australian Dollar\r\n" + "AZN - Azerbaijani Manat\r\n" + "BGN - Bulgarian Lev\r\n"
			+ "BHD - Bahraini Dinar\r\n" + "BND - Brunei Dollar\r\n" + "BRL - Brazilian Real\r\n"
			+ "CAD - Canadian Dollar\r\n" + "CHF - Swiss Franc\r\n" + "CLP - Chilean Peso\r\n"
			+ "CNY - Chinese Yuan\r\n" + "CZK - Czech Koruna\r\n" + "DKK - Danish Krone\r\n"
			+ "EGP - Egyptian Pound\r\n" + "EUR - Euro\r\n" + "FJD - Fiji Dollar\r\n" + "GBP - Pound Sterling\r\n"
			+ "HKD - Hong Kong Dollar\r\n" + "HUF - Hungarian Forint\r\n" + "IDR - Indonesian rRpiah\r\n"
			+ "ILS - Israeli New Shekel\r\n" + "INR - Indian Rupee\r\n" + "JPY - Japanese Yen\r\n"
			+ "KRW - South Korean Won\r\n" + "KWD - Kuwaiti Dinar\r\n" + "LKR - Sri Lankan Rupee\r\n"
			+ "MAD - Moroccan Dirham\r\n" + "MGA - Malagasy Ariary\r\n" + "MXN - Mexican Peso\r\n"
			+ "MYR - Malaysian Ringgit\r\n" + "NOK - Norwegian Krone\r\n" + "NZD - New Zealand Dollar\r\n"
			+ "OMR - Omani Rial\r\n" + "PEN - Peruvian Sol\r\n" + "PGK - Papua New Guinean Kina\r\n"
			+ "PHP - Philippine Peso\r\n" + "PKR - Pakistani Rupee\r\n" + "PLN - Polish Złoty\r\n"
			+ "RUB - Russian Ruble\r\n" + "SAR - Saudi Riyal\r\n" + "SBD - Solomon Islands Dollar\r\n"
			+ "SCR - Seychelles Rupee\r\n" + "SEK - Swedish Krona/Kronor\r\n" + "SGD - Singapore Dollar\r\n"
			+ "THB - Thai Baht\r\n" + "TOP - Tongan pPaʻanga\r\n" + "TRY - Turkish Lira\r\n"
			+ "TWD - New Taiwan Dollar\r\n" + "TZS - Tanzanian Shilling\r\n" + "USD - United States Dollar\r\n"
			+ "VEF - Venezuelan Bolívar\r\n" + "VND - Vietnamese Dồng\r\n" + "VUV - Vanuatu Vatu\r\n"
			+ "WST - Samoan Tala\r\n" + "XOF - CFA Franc BCEAO\r\n" + "ZAR - South African Rand");

	public static final String EXAMPLE = ("EUR : USD\r\n"
			+ "EUR/USD\r\n"
			+ "EUR to USD\r\n"
			+ "EUR \\ USD\r\n"
			+ "Eur USD\r\n"
			+ "10 USD to EUR\r\n"
			+ "\r\n"
			+ "/post_admin Eur USD\r\n" 
			+ "\r\n"
			+ "/post 10 USD : eur\r\n"
			+ "\r\n"
			+ "/post_daily usd to egp 15:54:00\r\n"
			+ "\r\n"
			+ "/schedule USD to EGP 2021-05-10 12:00:00");

	public static final String SUPPORT_TEXT_ONLY = "This bot support Text only ,Just send me the Currency Code and i will convert it -you can see some\r\n"
			+ "				// example by enter '/example";

	public static final String MAKE_SURE_CURRENCY_CODE = "Please make sure of your currency code"
			+ "\n -  if you want to know what is your currency code please enter '/info'"
			+ "\n - you can also see some example for how to you use it by entering /example";
	
	public static final String HELP = " Please make sure of your currency code\r\n"
			+ "- if you want to know what is your currency code please enter '/info'\r\n"
			+ "- you can also see some example for how to you use it by enter /example\r\n";
	
	public static String getTokenFromPropFile() throws IOException {
		Properties prop = new Properties();

		InputStream input = new FileInputStream("token.properties");
		prop.load(input);
		return prop.getProperty("Token");
	}
}
