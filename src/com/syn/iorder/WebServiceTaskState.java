package com.syn.iorder;

public interface WebServiceTaskState extends WebServiceStateListener{
	public void onProgress();
	public void onSuccess(int arg);
}
