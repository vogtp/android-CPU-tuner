package ch.amana.android.cputuner.view.widget;

import java.util.EnumMap;
import java.util.Map;

import android.content.Context;
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

	private final Map<ServiceType, ImageView> serviceButtonMap = new EnumMap<ServiceType, ImageView>(ServiceType.class);
	private Context ctx;
	private SettingsStorage settings;

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
		LinearLayout llServices = (LinearLayout) findViewById(R.id.llServiceIcons);
		llServices.setOrientation(LinearLayout.HORIZONTAL);
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
			if (state == PowerProfiles.SERVICE_STATE_LEAVE) {
				icon.setAlpha(ALPHA_LEAVE);
			} else {
				icon.setAlpha(ALPHA_ON);
			}
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
		final ListPopupWindowStandalone lpw = new ListPopupWindowStandalone(this, v);
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
		PowerProfiles.getInstance(ctx).setServiceState(type, status, true);
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


}
