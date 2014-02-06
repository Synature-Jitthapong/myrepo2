package com.syn.iorder.util;

public class SynRegisterAlghorhythm {
	public static final int PRODUCT_CODE = 1101; 
			
	public static String generateDeviceCode(String uuid){
		StringBuilder deviceCode = new StringBuilder();
		for(int i = 0; i < uuid.length(); i++){
			if(i < 14) // fix length to 14
				deviceCode.append(convertToCharNumber(uuid.charAt(i)));
		}
		deviceCode.append(oddEvenSummary(deviceCode.toString()));
		return deviceCode.toString();
	}
	
	
	private static String combindChar(String serial1, String serial2){
		StringBuilder code = new StringBuilder();
		for(int i = 0; i < serial1.length(); i++){
			int char1 = Character.getNumericValue(serial1.charAt(i));
			int char2 = Character.getNumericValue(serial2.charAt(i));
			int combind = (char1 + char2) % 10;
			code.append(combind);
		}
		return code.toString();
	}
	
	public static int comparison(String serial1, String serial2, String reqCode) throws Exception{
		String code = combindChar(serial1, serial2);
		String codeSet1 = code.substring(0,4);
		String codeSet2 = code.substring(4,8);
		String codeSet3 = code.substring(8,12);
		String codeSet4 = code.substring(12,16);
		
		StringBuilder valueSet1 = new StringBuilder();
		StringBuilder valueSet3 = new StringBuilder();
		StringBuilder valueSet4 = new StringBuilder();
		
		for(int i = 0; i< codeSet2.length(); i++){
			int ch1 = Character.getNumericValue(codeSet1.charAt(i));
			int ch2 = Character.getNumericValue(codeSet2.charAt(i));
			int ch3 = Character.getNumericValue(codeSet3.charAt(i));
			int ch4 = Character.getNumericValue(codeSet4.charAt(i));
			
			valueSet1.append((ch1 ^ ch2) % 10);
			valueSet3.append((ch3 ^ ch2) % 10);
			valueSet4.append((ch4 ^ ch2) % 10);
		}
		
		String result = valueSet1 + codeSet2 + valueSet3 + valueSet4;
		return reqCode.compareTo(result);
	}

	public static int checkProductCode(String licenceCode) throws Exception{
		String codeSet1 = licenceCode.substring(0,4);
		String codeSet2 = licenceCode.substring(4,8);
		int value = Integer.parseInt(codeSet1) ^ Integer.parseInt(codeSet2);
		return value == PRODUCT_CODE ? 0 : -1;
	}
	
	private static String oddEvenSummary(String deviceCode){
		int odd = 0;
		int even = 0;
		String EvenOddSum = "00";
		
		for(int i = 0; i < deviceCode.length(); i++){
			if(i % 2 == 0){
				even += Character.getNumericValue(deviceCode.charAt(i));
			}else{
				odd += Character.getNumericValue(deviceCode.charAt(i));
			}
		}
		EvenOddSum = Integer.toString(even % 10) + Integer.toString(odd % 10);
		return EvenOddSum;
	}
	
	private static char convertToCharNumber(char ch){
		char num = ch;
		switch(ch){
			case 'A':
				num = '0';
				break;
			case 'B':
				num = '1';
				break;
			case 'C':
				num = '2';
				break;
			case 'D':
				num = '3';
				break;
			case 'E':
				num = '4';
				break;
			case 'F':
				num = '5';
				break;
		}
		return num;
	}
}
