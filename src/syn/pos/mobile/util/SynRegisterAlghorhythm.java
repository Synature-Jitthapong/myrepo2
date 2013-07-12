package syn.pos.mobile.util;

/*
 * total length = 16 
 * position 15 is summary of even position
 * position 16 is summary of odd position
 */
public class SynRegisterAlghorhythm {
	public static String generateDeviceCode(String uuid){
		StringBuilder deviceCode = new StringBuilder();
		for(int i = 0; i < uuid.length(); i++){
			if(i < 14) // fix length to 14
				deviceCode.append(convertToCharNumber(uuid.charAt(i)));
		}
		deviceCode.append(oddEvenSummary(deviceCode.toString()));
		return deviceCode.toString();
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
		char num = '0';
		switch(ch){
			case 'a':
				num = '1';
				break;
			case 'b':
				num = '2';
				break;
			case 'c':
				num = '3';
				break;
			case 'd':
				num = '4';
				break;
			case 'e':
				num = '5';
				break;
			case 'f':
				num = '6';
				break;
			default:
				num = ch;
				break;
		}
		return num;
	}
}
