package ch.amana.android.cputuner.helper;

import ch.amana.android.cputuner.hw.CpuHandler;


public class GovernorConfigHelper {

	
	public interface GovernorConfig {

		public boolean hasThreshholdUpFeature();

		public boolean hasThreshholdDownFeature();

		public boolean hasPowersaveBias();

	}

	public static final GovernorConfig GOV = new GovernorConfig() {
		
		@Override
		public boolean hasThreshholdUpFeature() {
			return true;
		}
		
		@Override
		public boolean hasThreshholdDownFeature() {
			return true;
		}

		@Override
		public boolean hasPowersaveBias() {
			return false;
		}
	};
	
	private static final GovernorConfig GOV_ONDEMAND = new GovernorConfig() {
		
		@Override
		public boolean hasThreshholdUpFeature() {
			return true;
		}
		
		@Override
		public boolean hasThreshholdDownFeature() {
			return false;
		}

		@Override
		public boolean hasPowersaveBias() {
			return true;
		}
	};


	private static final GovernorConfig GOV_CONSERVATIVE = new GovernorConfig() {
		
		@Override
		public boolean hasThreshholdUpFeature() {
			return true;
		}
		
		@Override
		public boolean hasThreshholdDownFeature() {
			return true;
		}

		@Override
		public boolean hasPowersaveBias() {
			return false;
		}
	};


	private static final GovernorConfig GOV_INTERACTIVE = new GovernorConfig() {
		
		@Override
		public boolean hasThreshholdUpFeature() {
			return false;
		}
		
		@Override
		public boolean hasThreshholdDownFeature() {
			return false;
		}

		@Override
		public boolean hasPowersaveBias() {
			return false;
		}
	};


	private static final GovernorConfig GOV_PERFORMANCE = new GovernorConfig() {
		
		@Override
		public boolean hasThreshholdUpFeature() {
			return false;
		}
		
		@Override
		public boolean hasThreshholdDownFeature() {
			return false;
		}

		@Override
		public boolean hasPowersaveBias() {
			return false;
		}
	};


	private static final GovernorConfig GOV_POWERSAVE = new GovernorConfig() {
		
		@Override
		public boolean hasThreshholdUpFeature() {
			return false;
		}
		
		@Override
		public boolean hasThreshholdDownFeature() {
			return false;
		}

		@Override
		public boolean hasPowersaveBias() {
			return false;
		}
	};


	private static final GovernorConfig GOV_USERSPACE = new GovernorConfig() {
		
		@Override
		public boolean hasThreshholdUpFeature() {
			return false;
		}
		
		@Override
		public boolean hasThreshholdDownFeature() {
			return false;
		}

		@Override
		public boolean hasPowersaveBias() {
			return false;
		}
	};	
	
	
	public static GovernorConfig getGovernorConfig(String governor) {

		if (CpuHandler.GOV_ONDEMAND.equals(governor)) {
			return GOV_ONDEMAND;
		}
		if (CpuHandler.GOV_CONSERVATIVE.equals(governor)) {
			return GOV_CONSERVATIVE;
		}
		if (CpuHandler.GOV_INTERACTIVE.equals(governor)) {
			return GOV_INTERACTIVE;
		}
		if (CpuHandler.GOV_PERFORMANCE.equals(governor)) {
			return GOV_PERFORMANCE;
		}
		if (CpuHandler.GOV_POWERSAVE.equals(governor)) {
			return GOV_POWERSAVE;
		}
		if (CpuHandler.GOV_USERSPACE.equals(governor)) {
			return GOV_USERSPACE;
		}
//		if (CpuHandler.GOV_.equals(governor)) {
//			return GOV_;
//		}
		return GOV;
	}

}
