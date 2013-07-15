package syn.pos.mobile.iordertab;

public interface WebServiceTaskState extends WebServiceStateListener{
	public void onProgress();
	public void onSuccess(int arg);
}
