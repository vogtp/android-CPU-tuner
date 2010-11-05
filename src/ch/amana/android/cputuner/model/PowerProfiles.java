package ch.amana.android.cputuner.model;

import android.content.Context;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.Notifier;
import ch.amana.android.cputuner.hw.CpuHandler;

public class PowerProfiles {

	public static final int PROFILE_AC = 100;
	public static final int PROFILE_BATTERY = 101;
	public static final int PROFILE_BATTERY_CRITICAL = 102;

	public static final int PROFILE_BATTERY_GOOD = 1;
	private static int batteryLevel;
	private static boolean acPower;
	private static boolean batteryLow;
	private static boolean userProfiles = false;
	private static Context context;
	private static CharSequence currentProfile = "Unknown";

	public static void initContext(Context ctx) {
		context = ctx;
	}

	public static CpuModel getCpuModelForProfile(int profile) {
		String profileName = getProfileName(profile);
		return new CpuModel(profileName);
	}

	private static String getProfileName(int profile) {
		String profileString = CpuModel.NO_PROFILE;
		switch (profile) {
		case PROFILE_AC:
			profileString = context.getString(R.string.profileAcPower);
			break;
		case PROFILE_BATTERY:
			profileString = context.getString(R.string.profileBatteryPower);
			break;
		case PROFILE_BATTERY_CRITICAL:
			profileString = context.getString(R.string.profileBatteryCrtitical);
			break;
		case PROFILE_BATTERY_GOOD:
			profileString = context.getString(R.string.profileBatteryGood);
			break;

		default:
			break;
		}
		return profileString;
	}

	private static void applyPowerProfile() {
		CpuModel cpu;
		if (acPower) {
			cpu = getCpuModelForProfile(PROFILE_AC);
		} else if (userProfiles) {
			cpu = applyUserPowerProfile();
		} else {
			cpu = applySystemPowerProfile();
		}
		CpuHandler cpuHandler = new CpuHandler();
		cpuHandler.applyCpuSettings(cpu);
		currentProfile = cpu.getProfileName();
		Notifier.notify(context, "Setting power profile to " + cpu.getProfileName(), 1);
	}

	private static CpuModel applyUserPowerProfile() {
		throw new Error("User power profiles not yet implemented");
	}

	private static CpuModel applySystemPowerProfile() {
		if (batteryLow) {
			return getCpuModelForProfile(PROFILE_BATTERY_CRITICAL);
		} else {
			return getCpuModelForProfile(PROFILE_BATTERY);
		}
	}

	public static void setBatteryLevel(int level) {
		batteryLevel = level;
		if (userProfiles) {
			applyPowerProfile();
		}
	}

	public static int getBatteryLevel() {
		return batteryLevel;
	}

	public static void setAcPower(boolean power) {
		acPower = power;
		applyPowerProfile();
	}

	public static boolean getAcPower() {
		return acPower;
	}

	public static void setBatteryLow(boolean b) {
		batteryLow = b;
		applyPowerProfile();
	}

	public static boolean getBatteryLow() {
		return batteryLow;
	}

	public static CharSequence getCurrentProfile() {
		return currentProfile;
	}

}
