package ch.amana.android.cputuner.view.widget;

import java.util.EnumMap;
import java.util.Map;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import ch.amana.android.cputuner.helper.PulseHelper;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.PowerProfiles;
import ch.amana.android.cputuner.hw.PowerProfiles.ServiceType;
import ch.amana.android.cputuner.hw.ServicesHandler;
import ch.amana.android.cputuner.log.Logger;

import com.markupartist.android.widget.actionbar.R;

public class ServiceSwitcher extends LinearLayout implements View.OnClickListener {

	public static final int ALPHA_ON = 1000;
	public static final int ALPHA_OFF = 40;
	public static final int ALPHA_LEAVE = 100;
	private static Object lock = new Object();

	private final Map<ServiceType, ImageView> serviceButtonMap = new EnumMap<ServiceType, ImageView>(ServiceType.class);
	private Context ctx;
	private SettingsStorage settings;
	private ServiceChangeReceiver receiver;

	class ServiceChangeReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			//			if (Logger.DEBUG) {
			//				// FIXME remove
			//				Logger.d("***********************************************");
			//				for (String key : intent.getExtras().keySet()) {
			//					try {
			//						Logger.d(action + " extra " + key + " -> " + intent.getExtras().get(key));
			//					} catch (Exception e) {
			//						// TODO: handle exception
			//					}
			//				}
			//			}
			if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
				updateButtonStateFromSystem(ServiceType.wifi);
			} else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
				updateButtonStateFromSystem(ServiceType.bluetooth);
			} else if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
				updateButtonStateFromSystem(ServiceType.airplainMode);
			} else if (ConnectivityManager.ACTION_BACKGROUND_DATA_SETTING_CHANGED.equals(action)) {
				updateButtonStateFromSystem(ServiceType.backgroundsync);
			} else {
				updateButtonStateFromSystem(ServiceType.mobiledata3g);
				updateButtonStateFromSystem(ServiceType.mobiledataConnection);
			}
		}

	}

	public static void registerReceiver(Context context, BroadcastReceiver receiver) {
		synchronized (lock) {
			if (receiver != null) {
				// bt: ok wifi: ok airplaine: ok md: yes 3g: yes
				// snyc: ?  
				// gps: not supported
				context.registerReceiver(receiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
				context.registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
				context.registerReceiver(receiver, new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED));
				context.registerReceiver(receiver, new IntentFilter(ConnectivityManager.ACTION_BACKGROUND_DATA_SETTING_CHANGED));
				context.registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
				context.registerReceiver(receiver, new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED));
				Logger.i("Registered service change receiver");

			}
		}
	}

	public static void unregisterReceiver(Context context, BroadcastReceiver receiver) {
		synchronized (lock) {
			if (receiver != null) {
				try {
					context.unregisterReceiver(receiver);
					Logger.i("Unegistered service change receiver");
				} catch (Throwable e) {
					Logger.w("Could not unregister service change receiver", e);
				}
			}
		}
	}

	public ServiceSwitcher(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public ServiceSwitcher(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public ServiceSwitcher(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {
		this.ctx = context;
		settings = SettingsStorage.getInstance();
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		addView(inflater.inflate(R.layout.service_icons_view, null));
		initaliseButton(R.id.ivServiceAirplane);
		initaliseButton(R.id.ivServiceAirplane);
		initaliseButton(R.id.ivServiceBluetooth);
		initaliseButton(R.id.ivServiceGPS);
		initaliseButton(R.id.ivServiceMD3g);
		initaliseButton(R.id.ivServiceMDCon);
		initaliseButton(R.id.ivServiceSync);
		initaliseButton(R.id.ivServiceWifi);

	}

	private void initaliseButton(int id) {
		ImageView serviceButton = (ImageView) findViewById(id);
		ServiceType serviceType = getServiceType(serviceButton);
		serviceButtonMap.put(serviceType, serviceButton);
		setButtuonVisibility(serviceType);
	}

	private ServiceType getServiceType(View view) {
		return ServiceType.valueOf((String) view.getTag());
	}

	public void updateAllButtonStateFromSystem() {
		ImageView gps = (ImageView) findViewById(R.id.ivServiceGPS);
		gps.setVisibility(View.GONE);
		ServiceType[] serviceTypes = ServiceType.values();
		for (int i = 0; i < serviceTypes.length; i++) {
			ServiceType st = serviceTypes[i];
			updateButtonStateFromSystem(st);
		}
	}

	public void updateButtonStateFromSystem(ServiceType st) {
		int state = ServicesHandler.getServiceState(ctx, st);
		if (PulseHelper.getInstance(ctx).isPulsing(st)) {
			state = PowerProfiles.SERVICE_STATE_PULSE;
		}
		setButtuonState(st, state);
	}

	@Override
	protected void onDetachedFromWindow() {
		unregisterReceiver(ctx, receiver);
		super.onDetachedFromWindow();
	}

	public void setButtuonState(String serviceType, int state) {
		try {
			setButtuonState(ServiceType.valueOf(serviceType), state);
		} catch (IllegalArgumentException e) {
			Logger.e("Cannot parse " + serviceType + " as service type", e);
		}
	}

	public void setButtuonState(ServiceType serviceType, int state) {
		ImageView icon = serviceButtonMap.get(serviceType);
		if (serviceType == ServiceType.mobiledata3g) {
			if (!settings.isEnableSwitchMobiledata3G()) {
				return;
			}
			icon.setAlpha(ALPHA_ON);
			icon.clearAnimation();
			if (state == PowerProfiles.SERVICE_STATE_2G) {
				icon.setImageResource(R.drawable.serviceicon_md_2g);
			} else if (state == PowerProfiles.SERVICE_STATE_2G_3G) {
				icon.setImageResource(R.drawable.serviceicon_md_2g3g);
			} else if (state == PowerProfiles.SERVICE_STATE_3G) {
				icon.setImageResource(R.drawable.serviceicon_md_3g);
			} else if (state == PowerProfiles.SERVICE_STATE_PREV) {
				icon.setImageResource(R.drawable.serviceicon_md_2g3g);
				setAnimation(icon, R.anim.back);
			}
			//			if (state == PowerProfiles.SERVICE_STATE_LEAVE) {
			//				icon.setAlpha(ALPHA_LEAVE);
			//			} else {
			//				icon.setAlpha(ALPHA_ON);
			//			}
		} else {
			setServiceStateIcon(icon, state);
		}
	}

	private void setButtuonVisibility(ServiceType serviceType) {
		ImageView view = serviceButtonMap.get(serviceType);
		view.setVisibility(settings.isServiceEnabled(serviceType) ? View.VISIBLE : View.GONE);
	}

	private void setServiceStateIcon(ImageView icon, int state) {
		icon.clearAnimation();
		if (state == PowerProfiles.SERVICE_STATE_LEAVE) {
			icon.setAlpha(ALPHA_LEAVE);
		} else if (state == PowerProfiles.SERVICE_STATE_OFF) {
			icon.setAlpha(ALPHA_OFF);
		} else if (state == PowerProfiles.SERVICE_STATE_PREV) {
			setAnimation(icon, R.anim.back);
		} else if (state == PowerProfiles.SERVICE_STATE_PULSE) {
			setAnimation(icon, R.anim.pluse);
		} else {
			icon.setAlpha(ALPHA_ON);
		}
	}

	private void setAnimation(final View v, int resID) {
		final AnimationSet c = (AnimationSet) AnimationUtils.loadAnimation(ctx, resID);
		c.setRepeatMode(Animation.RESTART);
		c.setRepeatCount(Animation.INFINITE);
		c.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				v.clearAnimation();
				v.startAnimation(c);
			}
		});
		v.clearAnimation();
		v.startAnimation(c);
	}

	@Override
	public void onClick(View v) {
		final ImageView iv = (ImageView) v;
		iv.setPressed(true);
		final ServiceType type = getServiceType(v);
		final ListPopupWindow lpw = new ListPopupWindowStandalone(this);
		lpw.setAnchorView(v);
		lpw.setModal(true);
		lpw.setWidth(LayoutParams.MATCH_PARENT);
		//		lpw.setPromptPosition(0);
		lpw.setAdapter(getServiceStateAdapter(ctx, type));
		lpw.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				setServiceStatusFromPosition(ctx, type, position);
				lpw.dismiss();
				updateAllButtonStateFromSystem();
				iv.setPressed(false);
			}

		});

		lpw.show();
	}

	public static void setServiceStatusFromPosition(Context ctx, ServiceType type, int position) {
		int[] statusValues = ctx.getResources().getIntArray(R.array.deviceStatesCurrentValues);
		if (type == ServiceType.mobiledata3g) {
			statusValues = ctx.getResources().getIntArray(R.array.mobiledataStatesCurrentValues);
		}
		int status = statusValues[position];
		PowerProfiles.getInstance(ctx).setServiceState(type, status);
	}

	public static ArrayAdapter<CharSequence> getServiceStateAdapter(Context ctx, ServiceType type) {
		int devicestates = R.array.deviceStatesCurrent;
		if (type == ServiceType.mobiledata3g) {
			devicestates = R.array.mobiledataStatesCurrent;
		}
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(ctx, devicestates, R.layout.profilechooser_item);
		//		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		return adapter;
	}

	public void setButtonPadding(float f) {
		int p = Math.round(f);
		for (ImageView iv : serviceButtonMap.values()) {
			int top = iv.getPaddingBottom();
			int bottom = iv.getPaddingBottom();
			iv.setPadding(p, top, p, bottom);
		}
	}

	public void setButtonClickable(boolean b) {
		OnClickListener listener = null;
		if (b) {
			listener = this;
		}
		for (ImageView iv : serviceButtonMap.values()) {
			iv.setOnClickListener(listener);
		}
	}

	public void startReceiver() {
		if (receiver == null) {
			receiver = new ServiceChangeReceiver();
			registerReceiver(ctx, receiver);
		}
	}

	public void stopReceiver() {
		unregisterReceiver(ctx, receiver);
		receiver = null;
	}

}
