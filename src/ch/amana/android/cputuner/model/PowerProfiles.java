package ch.amana.android.cputuner.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
	private static List<IProfileChangeCallback> listeners;

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
		if (!currentProfile.equals(cpu.getProfileName())) {
			currentProfile = cpu.getProfileName();
			CpuHandler cpuHandler = new CpuHandler();
			cpuHandler.applyCpuSettings(cpu);
			notifyProfile();
			Notifier.notify(context, "Setting power profile to " + cpu.getProfileName(), 1);
		}
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
		if (batteryLevel != level) {
			batteryLevel = level;
			notifyBatteryLevel();
			if (!batteryLow && batteryLevel < 30) {
				setBatteryLow(true);
			}
			if (userProfiles) {
				applyPowerProfile();
			}
		}
	}

	public static int getBatteryLevel() {
		return batteryLevel;
	}

	public static void setAcPower(boolean power) {
		if (acPower != power) {
			acPower = power;
			notifyAcPower();
			applyPowerProfile();
		}
	}

	public static boolean getAcPower() {
		return acPower;
	}

	public static void setBatteryLow(boolean b) {
		if (batteryLow != b) {
			batteryLow = b;
			notifyBatteryLevel();
			applyPowerProfile();
		}
	}

	public static boolean getBatteryLow() {
		return batteryLow;
	}

	public static CharSequence getCurrentProfile() {
		return currentProfile;
	}

	public static void registerCallback(IProfileChangeCallback callback) {
		if (listeners == null) {
			listeners = new ArrayList<IProfileChangeCallback>();
		}
		listeners.add(callback);
	}

	public static void unregisterCallback(IProfileChangeCallback callback) {
		if (listeners == null) {
			return;
		}
		listeners.remove(callback);
	}

	private static void notifyBatteryLevel() {
		if (listeners == null) {
			return;
		}
		for (Iterator<IProfileChangeCallback> iterator = listeners.iterator(); iterator.hasNext();) {
			IProfileChangeCallback callback = iterator.next();
			callback.batteryLevelChanged();
		}
	}

	private static void notifyProfile() {
		if (listeners == null) {
			return;
		}
		for (Iterator<IProfileChangeCallback> iterator = listeners.iterator(); iterator.hasNext();) {
			IProfileChangeCallback callback = iterator.next();
			callback.profileChanged();
		}
	}

	private static void notifyAcPower() {
		if (listeners == null) {
			return;
		}
		for (Iterator<IProfileChangeCallback> iterator = listeners.iterator(); iterator.hasNext();) {
			IProfileChangeCallback callback = iterator.next();
			callback.acPowerChanged();
		}
	}
}
