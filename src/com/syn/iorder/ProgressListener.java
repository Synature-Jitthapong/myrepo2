package com.syn.iorder;

public interface ProgressListener {
	void onPre();
	void onPost();
	void onError(String msg);
}
