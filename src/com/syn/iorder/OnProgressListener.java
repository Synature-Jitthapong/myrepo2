package com.syn.iorder;

public interface OnProgressListener {
	void onPre();
	void onPost();
	void onError(String msg);
}
