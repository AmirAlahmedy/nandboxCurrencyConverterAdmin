package com.nandbox.bots.currecnyconvertor;

import static com.nandbox.bots.currecnyconvertor.Constant.getTokenFromPropFile;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

import com.nandbox.bots.api.Nandbox;
import com.nandbox.bots.api.NandboxClient;
import com.nandbox.bots.api.data.Chat;
import com.nandbox.bots.api.data.User;
import com.nandbox.bots.api.inmessages.BlackList;
import com.nandbox.bots.api.inmessages.ChatAdministrators;
import com.nandbox.bots.api.inmessages.ChatMember;
import com.nandbox.bots.api.inmessages.ChatMenuCallback;
import com.nandbox.bots.api.inmessages.IncomingMessage;
import com.nandbox.bots.api.inmessages.InlineMessageCallback;
import com.nandbox.bots.api.inmessages.InlineSearch;
import com.nandbox.bots.api.inmessages.MessageAck;
import com.nandbox.bots.api.inmessages.PermanentUrl;
import com.nandbox.bots.api.inmessages.WhiteList;
import com.nandbox.bots.api.outmessages.TextOutMessage;
import com.nandbox.bots.api.util.Utils;
import com.nandbox.bots.control.CurrencyConverter;
import com.nandbox.bots.currecnyconvertor.Constant;

import net.minidev.json.JSONObject;

class Helper
{
	public boolean isPostCommand(String message)
	{
		if(Pattern.compile("\\/post\\s([0-9]+\\s)?[a-zA-Z]{3}((\\s:\\s)|(:)|(\\/)|(\\s\\\\\\s)|(\\s)|(\\sto\\s))[a-zA-Z]{3}\\s*").matcher(message).matches())
		{
			return true;
		}
		return false;
	}
	
	public boolean isPostDailyCommand(String message)
	{
		if(Pattern.compile("\\/post_daily\\s([0-9]+\\s)?[a-zA-Z]{3}((\\s:\\s)|(:)|(\\/)|(\\s\\\\\\s)|(\\s)|(\\sto\\s))[a-zA-Z]{3}\\s(([0-1][0-9])|(2[0-3])):[0-5][0-9]:[0-5][0-9]").matcher(message).matches())
		{
			return true;
		}
		return false;
	}
	
	public boolean isScheduleCommand(String message)
	{
		if(Pattern.compile("\\/schedule\\s([0-9]+\\s)?[a-zA-Z]{3}((\\s:\\s)|(:)|(\\/)|(\\s\\\\\\s)|(\\s)|(\\sto\\s))[a-zA-Z]{3}\\s[0-9]{4}-[0-9]{2}-[0-9]{2}\\s+(([0-1][0-9])|(2[0-3])):[0-5][0-9]:[0-5][0-9]").matcher(message).matches())
		{
			return true;
		}
		return false;
	}
	
	public boolean isPostAdminCommand(String message)
	{
		if(Pattern.compile("\\/post_admin\\s([0-9]+\\s)?[a-zA-Z]{3}((\\s:\\s)|(:)|(\\/)|(\\s\\\\\\s)|(\\s)|(\\sto\\s))[a-zA-Z]{3}\\s*").matcher(message).matches())
		{
			return true;
		}
		return false;
	}
	

}

class MyRunnable implements Runnable
{
	Database db;
	Nandbox.Api api;
	public MyRunnable(Database db,Nandbox.Api api)
	{
		this.db=db;
		this.api = api;
	}
	public void run()
	{
		Date currentDate = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd ");
		String dateTime = formatter.format(currentDate)+"23:59:59";
		Date startOfDayDate;
		long startOfDayEpoch = 0;
		try {
			startOfDayDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateTime);
			startOfDayEpoch = startOfDayDate.getTime();
		} catch (ParseException e2) {
			e2.printStackTrace();
		}
		;
		while(true)
		{
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			
			long currentEpoch = Instant.now().toEpochMilli();
			if(currentEpoch > startOfDayEpoch+10)
			{
				try {
					db.resetSentToday();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				currentDate = new Date();
				formatter = new SimpleDateFormat("yyyy-MM-dd ");
				dateTime = formatter.format(currentDate)+"23:59:59";
				
				try {
					startOfDayDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateTime);
					startOfDayEpoch = startOfDayDate.getTime();
				} catch (ParseException e2) {
					startOfDayEpoch += 86400000;
					e2.printStackTrace();
				}
				
			}
			
			
			ArrayList<ArrayList<String>> result;
			try {
				result = db.getAllMessages();
			} catch (SQLException e) {
				e.printStackTrace();
				continue;
			}
			
			for(int i=0; i < result.size();i++)
			{
				ArrayList<String> current = result.get(i);
				String chatId = current.get(0);
				String message = current.get(1);
				String timeString = current.get(2);
				String sentToday = current.get(3);
				
				if(sentToday.equals("1"))
				{
					continue;
				}
				
				
				Date date = new Date();
				formatter = new SimpleDateFormat("yyyy-MM-dd ");
				dateTime = formatter.format(date)+timeString;
				
				long scheduledTime = 0;
				Date scheduledDate;
				try {
					scheduledDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateTime);
					scheduledTime = scheduledDate.getTime();
				} catch (ParseException e) {
					System.out.println("Error occured while converting time string to epoch");
					e.printStackTrace();
					continue;
				}
				
				currentEpoch = Instant.now().toEpochMilli();
				
				if(scheduledTime < currentEpoch)
				{
					TextOutMessage currencyMessage = new TextOutMessage();
					currencyMessage.setText(message);
					currencyMessage.setChatId(chatId);
					long reference = Utils.getUniqueId();
					currencyMessage.setReference(reference);
					api.send(currencyMessage);
					
					try {
						db.setSentToday(chatId, message, timeString);
					} catch (SQLException e) {
						e.printStackTrace();
					}
					
				}
				
				
				
				
			}
		}
		
	}
}
public class CurrencyConverterBot {

	public static void main(String[] args) throws Exception {
		
		final Database db = new Database("Daily");

		String token = getTokenFromPropFile();

		NandboxClient client = NandboxClient.get();
		client.connect(token, new Nandbox.Callback() {
			Nandbox.Api api = null;

			@Override
			public void onConnect(Nandbox.Api api) {
				try {
					db.createTable();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				new Thread(new MyRunnable(db,api)).start();
				System.out.println("ONCONNECT");
				this.api = api;

			}

			@Override
			public void onReceive(IncomingMessage incomingMsg) {

				String chatId = incomingMsg.getChat().getId();

				String reciveMessage = incomingMsg.getText();
				
				Helper help = new Helper();
				

				if((incomingMsg.getChat().getType().equals("Group") || incomingMsg.getChat().getType().equals("Channel")) && incomingMsg.isFromAdmin() == 1)
				{
					if(incomingMsg.getChatSettings() == 1)
					{
						if(help.isPostCommand(reciveMessage))
						{
							String parsedString[] = reciveMessage.split(" ",2);
							String convertString = parsedString[1];
							
							
							
							CurrencyConverter currencyConverter = new CurrencyConverter();
							String current = currencyConverter.convert(convertString);

							String text = current;

							System.out.println(current);
							if(current.startsWith("Please make sure"))
							{
								TextOutMessage error = new TextOutMessage();
								error.setText(current);
								error.setChatId(chatId);
								long reference = Utils.getUniqueId();
								error.setReference(reference);
								error.setChatSettings(1);
								error.setToUserId(incomingMsg.getFrom().getId());
								api.send(error);
							}
							else
							{
								TextOutMessage confirmation = new TextOutMessage();
								confirmation.setText("Currency info will now be sent");
								confirmation.setChatId(chatId);
								long reference = Utils.getUniqueId();
								confirmation.setReference(reference);
								confirmation.setChatSettings(1);
								confirmation.setToUserId(incomingMsg.getFrom().getId());
								api.send(confirmation);
								
								api.sendText(chatId, text);
							}

							
							
						}
						
						
						else if(help.isPostDailyCommand(reciveMessage))
						{
							String parsedString[] = reciveMessage.split(" ",2);
							int lastSpaceIndex = parsedString[1].lastIndexOf(" ");
							String timeString = parsedString[1].substring(lastSpaceIndex+1);
							String convertString = parsedString[1].substring(0,lastSpaceIndex);
							
							Date date = new Date();
							SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd ");
							String dateTime = formatter.format(date)+timeString;
							
							long scheduledTime = 0;
							Date scheduledDate;
							try {
								scheduledDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateTime);
								scheduledTime = scheduledDate.getTime();
							} catch (ParseException e) {
								TextOutMessage error = new TextOutMessage();
								error.setText("Please make sure you entered the time in the following format 'yyyy-MM-dd HH:mm:ss'");
								error.setChatId(chatId);
								long reference = Utils.getUniqueId();
								error.setReference(reference);
								error.setChatSettings(1);
								error.setToUserId(incomingMsg.getFrom().getId());
								api.send(error);
								return;
							}
							
							long currentEpoch = Instant.now().toEpochMilli();
							System.out.println(currentEpoch);
							System.out.println(scheduledTime);
							System.out.println(dateTime);
							
							CurrencyConverter currencyConverter = new CurrencyConverter();
							String current = currencyConverter.convert(convertString);

							String text = current;

							if(text.equals(Constant.MAKE_SURE_CURRENCY_CODE))
							{
								TextOutMessage error = new TextOutMessage();
								error.setText(current);
								error.setChatId(chatId);
								long reference = Utils.getUniqueId();
								error.setReference(reference);
								error.setChatSettings(1);
								error.setToUserId(incomingMsg.getFrom().getId());
								api.send(error);
							}
							else
							{
								TextOutMessage confirmation = new TextOutMessage();
								confirmation.setText("Your message has been scheduled");
								confirmation.setChatId(chatId);
								long reference = Utils.getUniqueId();
								confirmation.setReference(reference);
								confirmation.setChatSettings(1);
								confirmation.setToUserId(incomingMsg.getFrom().getId());
								api.send(confirmation);
								String sentToday = "1";
								if(currentEpoch < scheduledTime)
								{
									System.out.println("current epoch is: "+currentEpoch);
									System.out.println("scheduled epoch is: "+scheduledTime);
									TextOutMessage currencyMessage = new TextOutMessage();
									currencyMessage.setText(text);
									currencyMessage.setChatId(chatId);
									reference = Utils.getUniqueId();
									currencyMessage.setReference(reference);
									currencyMessage.setScheduleDate(scheduledTime);
									api.send(currencyMessage);
								}
								
								try {
									db.insertMessage(chatId, text, timeString,sentToday);
								} catch (SQLException e) {
									e.printStackTrace();
								}
								
							}
						

							
						}
						
						else if(help.isScheduleCommand(reciveMessage))
						{
							String parsedString[] = reciveMessage.split(" ",2);
							int indexOfLastSpace = parsedString[1].lastIndexOf(" ");
							indexOfLastSpace = parsedString[1].lastIndexOf(" ",indexOfLastSpace-1);
							String dateTime = parsedString[1].substring(indexOfLastSpace+1);
							String convertString = parsedString[1].substring(0,indexOfLastSpace);
							
							System.out.println(dateTime);
							System.out.println(convertString);
							Date scheduledDate;
							long scheduledTime = 0;
							try {
								scheduledDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateTime);
								scheduledTime = scheduledDate.getTime();
							} catch (ParseException e) {
								TextOutMessage error = new TextOutMessage();
								error.setText("Please make sure you entered the date in the following format 'yyyy-MM-dd HH:mm:ss'");
								error.setChatId(chatId);
								long reference = Utils.getUniqueId();
								error.setReference(reference);
								error.setChatSettings(1);
								error.setToUserId(incomingMsg.getFrom().getId());
								api.send(error);
								return;
							}
							
							
							long currentEpoch = Instant.now().toEpochMilli();
							if(currentEpoch > scheduledTime)
							{
								TextOutMessage error = new TextOutMessage();
								error.setText("Please make sure your scheduled date is in the future");
								error.setChatId(chatId);
								long reference = Utils.getUniqueId();
								error.setReference(reference);
								error.setChatSettings(1);
								error.setToUserId(incomingMsg.getFrom().getId());
								api.send(error);
								return;
							}
							
							CurrencyConverter currencyConverter = new CurrencyConverter();
							String current = currencyConverter.convert(convertString);

							String text = current;

							if(text.equals(Constant.MAKE_SURE_CURRENCY_CODE))
							{
								TextOutMessage error = new TextOutMessage();
								error.setText(current);
								error.setChatId(chatId);
								long reference = Utils.getUniqueId();
								error.setReference(reference);
								error.setChatSettings(1);
								error.setToUserId(incomingMsg.getFrom().getId());
								api.send(error);
							}
							else
							{
								TextOutMessage confirmation = new TextOutMessage();
								confirmation.setText("Your message has been scheduled");
								confirmation.setChatId(chatId);
								long reference = Utils.getUniqueId();
								confirmation.setReference(reference);
								confirmation.setChatSettings(1);
								confirmation.setToUserId(incomingMsg.getFrom().getId());
								api.send(confirmation);
								
								TextOutMessage currencyMessage = new TextOutMessage();
								currencyMessage.setText(text);
								currencyMessage.setChatId(chatId);
								reference = Utils.getUniqueId();
								currencyMessage.setReference(reference);
								currencyMessage.setScheduleDate(scheduledTime);
								api.send(currencyMessage);

							}
							
						}
						
						else if(help.isPostAdminCommand(reciveMessage))
						{
							String parsedString[] = reciveMessage.split(" ",2);
							String convertString = parsedString[1];
							
							
							
							CurrencyConverter currencyConverter = new CurrencyConverter();
							String current = currencyConverter.convert(convertString);

							String text = current;

							System.out.println(current);
							if(current.startsWith("Please make sure"))
							{
								TextOutMessage error = new TextOutMessage();
								error.setText(current);
								error.setChatId(chatId);
								long reference = Utils.getUniqueId();
								error.setReference(reference);
								error.setChatSettings(1);
								error.setToUserId(incomingMsg.getFrom().getId());
								api.send(error);
							}
							else
							{
								TextOutMessage confirmation = new TextOutMessage();
								confirmation.setText(text);
								confirmation.setChatId(chatId);
								long reference = Utils.getUniqueId();
								confirmation.setReference(reference);
								confirmation.setChatSettings(1);
								confirmation.setToUserId(incomingMsg.getFrom().getId());
								api.send(confirmation);
							}
						}
						
						else if(reciveMessage.equalsIgnoreCase("/info"))
						{
							String text = Constant.CURRENCY_CODE;
							TextOutMessage confirmation = new TextOutMessage();
							confirmation.setText(text);
							confirmation.setChatId(chatId);
							long reference = Utils.getUniqueId();
							confirmation.setReference(reference);
							confirmation.setChatSettings(1);
							confirmation.setToUserId(incomingMsg.getFrom().getId());
							api.send(confirmation);
						}
						
						else if(reciveMessage.equalsIgnoreCase("/example"))
						{
							String text = Constant.EXAMPLE;
							TextOutMessage confirmation = new TextOutMessage();
							confirmation.setText(text);
							confirmation.setChatId(chatId);
							long reference = Utils.getUniqueId();
							confirmation.setReference(reference);
							confirmation.setChatSettings(1);
							confirmation.setToUserId(incomingMsg.getFrom().getId());
							api.send(confirmation);
						}
						else if(reciveMessage.equalsIgnoreCase("/help"))
						{
							String text = Constant.HELP;
							TextOutMessage confirmation = new TextOutMessage();
							confirmation.setText(text);
							confirmation.setChatId(chatId);
							long reference = Utils.getUniqueId();
							confirmation.setReference(reference);
							confirmation.setChatSettings(1);
							confirmation.setToUserId(incomingMsg.getFrom().getId());
							api.send(confirmation);
						}
					}
				}
				else
				{
					if (incomingMsg.getType().equals("text")) {

						if (reciveMessage.equalsIgnoreCase("/info")) {

							String text = Constant.CURRENCY_CODE;

							api.sendText(chatId, text);

						} else if (reciveMessage.equalsIgnoreCase("/example")) {

							String text = Constant.EXAMPLE;

							api.sendText(chatId, text);

						} else {
							CurrencyConverter currencyConverter = new CurrencyConverter();
							String current = currencyConverter.convert(reciveMessage);

							String text = current;

							System.out.println(current);

							api.sendText(chatId, text);

						}

					} else {
						String text = (Constant.SUPPORT_TEXT_ONLY);
						api.sendText(chatId, text);
					}
				}
				

			}

			@Override
			public void onReceive(JSONObject obj) {
			}

			@Override
			public void onClose() {
				System.out.println("ONCLOSE");
			}

			@Override
			public void onError() {
				System.out.println("ONERROR");
			}

			@Override
			public void onChatAdministrators(ChatAdministrators chatAdministrators) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onChatDetails(Chat chat) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onChatMember(ChatMember chatMember) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onChatMenuCallBack(ChatMenuCallback chatMenuCallback) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onInlineMessageCallback(InlineMessageCallback inlineMsgCallback) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onInlineSearh(InlineSearch inlineSearch) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onMessagAckCallback(MessageAck msgAck) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onMyProfile(User user) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onUserDetails(User user) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onUserJoinedBot(User user) {
				// TODO Auto-generated method stub

			}

			@Override
			public void permanentUrl(PermanentUrl permenantUrl) {
				// TODO Auto-generated method stub

			}

			@Override
			public void userLeftBot(User user) {
				// TODO Auto-generated method stub

			}

			@Override
			public void userStartedBot(User user) {
				// TODO Auto-generated method stub

			}

			@Override
			public void userStoppedBot(User user) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onBlackList(BlackList blackList) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onWhiteList(WhiteList whiteList) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onScheduleMessage(IncomingMessage incomingScheduleMsg) {
				// TODO Auto-generated method stub
				
			}

		});

	}

}
