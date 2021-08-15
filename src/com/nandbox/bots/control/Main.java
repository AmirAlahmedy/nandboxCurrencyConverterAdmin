package com.nandbox.bots.control;

public class Main {
	public static void main(String[] args) {
		
		
		
		System.out.println("please write your currency like this examples ");
		System.out.println("USD to EUR");

	
		String reciveMessage = "usd to egp";

		CurrencyConverter currencyConverter = new CurrencyConverter();
		String current = currencyConverter.convert(reciveMessage);
		System.out.println(current);
	}
}