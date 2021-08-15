package com.nandbox.bots.control;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import com.nandbox.bots.currecnyconvertor.Constant;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

@SuppressWarnings("deprecation")
public class CurrencyConverter {

	private static final String APIKEY = "a01e221fa4e97909d733";

	public String convert(String receivedMessage) {

		String result = null;

		Double numberOfCurrency = 1.0;
		String currencyFrom = null;
		String currencyTo = null;

		Matcher firstMatcher = Pattern.compile("^([a-zA-Z_]{3})\\s*([:\\/\\\\]|to|\\s+)\\s*([a-zA-Z_]{3})",
				Pattern.MULTILINE | Pattern.CASE_INSENSITIVE).matcher(receivedMessage);

		Matcher secondMatcher = Pattern
				.compile("([+-]?([0-9]*[.])?[0-9]+)\\s*([a-zA-Z_]{3})\\s*([:\\/\\\\]|to|\\s+)\\s*([a-zA-Z_]{3})",
						Pattern.MULTILINE | Pattern.CASE_INSENSITIVE)
				.matcher(receivedMessage);

		while (firstMatcher.find()) {
			System.out.println(firstMatcher.group(1));
			System.out.println(firstMatcher.group(3));

			currencyFrom = firstMatcher.group(1).toUpperCase();
			currencyTo = firstMatcher.group(3).toUpperCase();
		}
		while (secondMatcher.find()) {

			System.out.println(secondMatcher.group(1));
			System.out.println(secondMatcher.group(3));
			System.out.println(secondMatcher.group(5));

			numberOfCurrency = Double.parseDouble(secondMatcher.group(1));
			currencyFrom = secondMatcher.group(3).toUpperCase();
			currencyTo = secondMatcher.group(5).toUpperCase();
		}

		@SuppressWarnings("resource")
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet("https://free.currencyconverterapi.com/api/v6/convert?q=" + currencyFrom + "_"
				+ currencyTo + "&compact=ultra&apiKey=" + APIKEY);

		System.out.println("URI " + httpGet.getURI());
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String responseBody = null;
		try {
			responseBody = httpclient.execute(httpGet, responseHandler);
		} catch (IOException e) {
			e.printStackTrace();
		}
		httpclient.getConnectionManager().shutdown();

		try {

			if (responseBody != null) {

				JSONObject obj = (JSONObject) JSONValue.parse(responseBody);

				Double currencyValue = (Double) obj.get(currencyFrom + "_" + currencyTo);

				currencyValue = round(currencyValue, 2);

				System.out.println(currencyValue);

				if (numberOfCurrency == 1.0) {

					result = ("1 " + currencyFrom + " equals " + currencyValue + " " + currencyTo);

				} else {

					Double currencyCount = numberOfCurrency * currencyValue;

					String tottalValue = Double.toString(round(currencyCount, 2));

					result = ("1 " + currencyFrom + " equals " + currencyValue + " " + currencyTo + "\r\n "
							+ numberOfCurrency + " " + currencyFrom + " = " + tottalValue + " " + currencyTo);

					System.out.println(result);
				}

			} else {

				System.out.println("problem in API");
			}
		} catch (Exception e) {

			System.out.println(e.getMessage());
			result = Constant.MAKE_SURE_CURRENCY_CODE;
		}

		return result;

	}

	public static double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}
}