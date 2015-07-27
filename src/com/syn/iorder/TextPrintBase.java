package com.syn.iorder;

public abstract class TextPrintBase {

	public static final int HORIZONTAL_MAX_SPACE = 38;
	public static final int MAX_TEXT_LENGTH = 29;
	
	private int horizontalMaxSpace = HORIZONTAL_MAX_SPACE;
	
	protected String createLine(String sign){
		StringBuilder line = new StringBuilder();
		for(int i = 0; i <= horizontalMaxSpace; i++){
			line.append(sign);
		}
		return line.toString();
	}
	
	protected String limitTextLength(String text){
		if(text == null)
			return "";
		if(text.length() > MAX_TEXT_LENGTH)
			text = text.substring(0, MAX_TEXT_LENGTH) + "...";
		return text;
	}
	
	protected String adjustAlignCenter(String text){
		int rimSpace = (horizontalMaxSpace - calculateLength(text)) / 2;
		StringBuilder empText = new StringBuilder();
		for(int i = 0; i < rimSpace; i++){
			empText.append(" ");
		}
		return empText.toString() + text + empText.toString();
	}
	
	protected String createHorizontalSpace(int usedSpace){
		StringBuilder space = new StringBuilder();
		if(usedSpace > horizontalMaxSpace){
			usedSpace = horizontalMaxSpace - 2;
		}
		for(int i = usedSpace; i <= horizontalMaxSpace; i++){
			space.append(" ");
		}
		return space.toString();
	}
	
	protected int calculateLength(String text){
		if(text == null)
			return 0;
		int length = 0;
		for(int i = 0; i < text.length(); i++){
			int code = (int) text.charAt(i);
			if(code != 3633 
					// thai
					&& code != 3636
					&& code != 3637
					&& code != 3638
					&& code != 3639
					&& code != 3640
					&& code != 3641
					&& code != 3642
					&& code != 3655
					&& code != 3656
					&& code != 3657
					&& code != 3658
					&& code != 3659
					&& code != 3660
					&& code != 3661
					&& code != 3662){
				length ++;
			}
		}
		return length == 0 ? text.length() : length;
	}

	public void setHorizontalMaxSpace(int horizontalMaxSpace) {
		this.horizontalMaxSpace = horizontalMaxSpace;
	}
}
