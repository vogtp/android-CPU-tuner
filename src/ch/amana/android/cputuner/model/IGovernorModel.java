package ch.amana.android.cputuner.model;

public interface IGovernorModel {

	public void setVirtualGovernor(long id);

	public void setGov(String gov);

	public void setGovernorThresholdUp(int i);

	public void setGovernorThresholdDown(int i);

	public void setGovernorThresholdUp(String string);

	public void setGovernorThresholdDown(String string);

	public void setScript(String string);

	public void setPowersaveBias(int powersaveBias);

	public void setUseNumberOfCpus(int position);

	public int getGovernorThresholdUp();

	public int getGovernorThresholdDown();

	public String getGov();

	public boolean hasScript();

	public String getScript();

	public long getVirtualGovernor();

	public int getPowersaveBias();

	public int getUseNumberOfCpus();
}
