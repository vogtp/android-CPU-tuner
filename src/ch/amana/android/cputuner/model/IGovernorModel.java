package ch.amana.android.cputuner.model;

public interface IGovernorModel {

	public int getGovernorThresholdUp();

	public int getGovernorThresholdDown();

	public void setGov(String gov);

	public void setGovernorThresholdUp(String string);

	public void setGovernorThresholdDown(String string);

	public void setScript(String string);

	public String getGov();

	public CharSequence getScript();

	public void setGovernorThresholdUp(int i);

	public void setGovernorThresholdDown(int i);

	public void setVirtualGovernor(long id);

	public long getVirtualGovernor();

}
