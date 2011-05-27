package ch.amana.android.cputuner.receiver;

import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.hw.PowerProfiles;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class CallPhoneStateListener extends PhoneStateListener {

	@Override
	public void onCallStateChanged(int state, String incomingNumber) {
		super.onCallStateChanged(state, incomingNumber);
		Logger.v("Got call state: "+state+" (number "+incomingNumber+")");
		switch (state) {
		case TelephonyManager.CALL_STATE_IDLE:
			// hangup
			PowerProfiles.getInstance().setCallInProgress(false);
			break;
		case TelephonyManager.CALL_STATE_RINGING:
			// incomming
			PowerProfiles.getInstance().setCallInProgress(true);
			break;
		case TelephonyManager.CALL_STATE_OFFHOOK:
			// outgoing
			PowerProfiles.getInstance().setCallInProgress(true);
			break;

		default:
			break;
		}
		
	}
	
}
