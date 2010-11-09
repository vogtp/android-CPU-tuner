package ch.amana.android.cputuner.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.Notifier;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.CpuHandler;

public class PowerProfiles {

	public static final String NO_PROFILE = "Unknown";
	public static final int PROFILE_AC = 100;
	public static final int PROFILE_BATTERY = 101;
	public static final int PROFILE_BATTERY_CRITICAL = 102;

	public static final int PROFILE_BATTERY_GOOD = 1;
	private static int batteryLevel;
	private static boolean acPower;
	private static boolean batteryLow;
	private static boolean userProfiles = false;
	private static Context context;
	private static CharSequence currentProfile = NO_PROFILE;
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

	public static void reapplyCurProfile() {
		applyPowerProfile(true, true);
	}

	public static void reapplyProfile(CharSequence profile) {
		if (currentProfile.equals(profile)) {
			applyPowerProfile(true, false);
		}
	}

	private static void applyPowerProfile(boolean force, boolean ignoreSettings) {
		if (!SettingsStorage.getInstance().isEnableProfiles()) {
			if (!ignoreSettings) {
				return;
			}
		}
		CpuModel cpu;
		if (acPower) {
			cpu = getCpuModelForProfile(PROFILE_AC);
		} else if (userProfiles) {
			cpu = applyUserPowerProfile();
		} else {
			cpu = applySystemPowerProfile();
		}
		if (force || !currentProfile.equals(cpu.getProfileName())) {
			currentProfile = cpu.getProfileName();
			CpuHandler cpuHandler = new CpuHandler();
			cpuHandler.applyCpuSettings(cpu);
			StringBuilder sb = new StringBuilder(50);
			if (force) {
				sb.append("Reappling power profile ");
			} else {
				sb.append("Setting power profile to ");
			}
			sb.append(cpu.getProfileName());
			notifyProfile();
			Notifier.notify(context, sb.toString(), 1);
			Notifier.notifyProfile(cpu.getProfileName());
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
				applyPowerProfile(false, false);
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
			applyPowerProfile(false, false);
		}
	}

	public static boolean getAcPower() {
		return acPower;
	}

	public static void setBatteryLow(boolean b) {
		if (batteryLow != b) {
			if (b && batteryLevel > 40) {
				batteryLow = b;
				notifyBatteryLevel();
				applyPowerProfile(false, false);
			}
		}
	}

	public static boolean getBatteryLow() {
		return batteryLow;
	}

	public static CharSequence getCurrentProfile() {
		if (NO_PROFILE.equals(currentProfile)) {
			if (acPower) {
				currentProfile = context.getString(R.string.profileAcPower);
			} else {
				currentProfile = context.getString(R.string.profileBatteryPower);
			}
		}
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
