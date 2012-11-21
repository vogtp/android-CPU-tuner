package ch.amana.android.cputuner.view.activity;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.SettingsStorage;

public class UserExperianceLevelChooser extends Dialog {

	private final RadioGroup rgUserLevel;
	private final SettingsStorage settingsStorage;

	public UserExperianceLevelChooser(Context context, boolean allowCancel) {
	    super(context);
		//	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
		//	             WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

	    setTitle(R.string.title_choose_experiance_level);
	    setContentView(R.layout.user_experiance_level_chooser);
	    rgUserLevel =  (RadioGroup)findViewById(R.id.rgUserlevel);
	    int userLevel = R.id.rbNormal;
	    settingsStorage = SettingsStorage.getInstance();
		if (settingsStorage.isPowerUser()) {
	    	userLevel = R.id.rbPowerUser;
	    }else if (settingsStorage.isBeginnerUser()) {
	    	userLevel = R.id.rbBeginner;
	    }
		rgUserLevel.check(userLevel);
		
		TextView tvNormal = (TextView)findViewById(R.id.tvNormal);
		tvNormal.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				rgUserLevel.check(R.id.rbNormal);
			}
		});
		TextView tvPowerUser = (TextView) findViewById(R.id.tvPowerUser);
		tvPowerUser.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				rgUserLevel.check(R.id.rbPowerUser);
			}
		});
		TextView tvBeginner = (TextView) findViewById(R.id.tvBeginner);
		tvBeginner.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				rgUserLevel.check(R.id.rbBeginner);
			}
		});
		
		
		((Button)findViewById(R.id.buOk)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				save();
				UserExperianceLevelChooser.this.dismiss();
			}
		});

		
		Button buCancel = (Button) findViewById(R.id.buCancel);
		buCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				UserExperianceLevelChooser.this.cancel();
			}
		});

		if (!allowCancel) {
			findViewById(R.id.buCancel).setVisibility(View.GONE);
		}
	}

	private void save() {
		int userLevel = 2;
		int checkedId = rgUserLevel.getCheckedRadioButtonId();
		if (checkedId == R.id.rbPowerUser) {
			userLevel = 3;
		} else if (checkedId == R.id.rbBeginner) {
			userLevel = 1;
		}
		settingsStorage.setUserLevel(userLevel);
	}

	@Override
	public void onBackPressed() {
		save();
		super.onBackPressed();
	}

}
