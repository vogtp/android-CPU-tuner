package ch.amana.android.cputuner.view.activity;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.GeneralMenuHelper;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.model.ProfileModel;
import ch.amana.android.cputuner.model.VirtualGovernorModel;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.provider.db.DB.CpuProfile;
import ch.amana.android.cputuner.provider.db.DB.VirtualGovernor;
import ch.amana.android.cputuner.view.fragments.GovernorBaseFragment;
import ch.amana.android.cputuner.view.fragments.GovernorFragment;
import ch.amana.android.cputuner.view.fragments.GovernorFragmentCallback;

public class VirtualGovernorEditorActivity extends FragmentActivity implements GovernorFragmentCallback {

	private static final String PROFILE_SELECTION = CpuProfile.NAME_VIRTUAL_GOVERNOR + "=?";
	private GovernorBaseFragment governorFragment;
	private VirtualGovernorModel virtualGovModel;
	private VirtualGovernorModel origvirtualGovModel;
	private EditText etVirtualGovernorName;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

		setContentView(R.layout.virtual_governor_editor);

		etVirtualGovernorName = (EditText) findViewById(R.id.etVirtualGovernorName);

		String action = getIntent().getAction();
		if (Intent.ACTION_EDIT.equals(action)) {
			Cursor c = managedQuery(getIntent().getData(), DB.VirtualGovernor.PROJECTION_DEFAULT, null, null, null);
			if (c.moveToFirst()) {
				virtualGovModel = new VirtualGovernorModel(c);
				origvirtualGovModel = new VirtualGovernorModel(c);
			}
			c.close();
		}

		if (virtualGovModel == null) {
			virtualGovModel = new VirtualGovernorModel();
			virtualGovModel.setVirtualGovernorName("");
			origvirtualGovModel = new VirtualGovernorModel();
		}

		setTitle(getString(R.string.titleVirtualGovernorEditor) + " " + virtualGovModel.getVirtualGovernorName());

		governorFragment = new GovernorFragment(this, virtualGovModel);
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.add(R.id.llGovernorFragmentAncor, governorFragment);
		fragmentTransaction.commit();
	    // TODO Auto-generated method stub
	}

	@Override
	public void updateModel() {
		virtualGovModel.setVirtualGovernorName(etVirtualGovernorName.getText().toString());
		governorFragment.updateModel();
	}


	@Override
	public void updateView() {
		etVirtualGovernorName.setText(virtualGovModel.getVirtualGovernorName());
		governorFragment.updateView();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		updateModel();
		virtualGovModel.saveToBundle(outState);
		super.onSaveInstanceState(outState);
	}


	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		if (virtualGovModel == null) {
			virtualGovModel = new VirtualGovernorModel(savedInstanceState);
		} else {
			virtualGovModel.readFromBundle(savedInstanceState);
		}
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateView();
	}

	@Override
	protected void onPause() {
		super.onPause();
		updateModel();
		try {
			String action = getIntent().getAction();
			if (Intent.ACTION_INSERT.equals(action)) {
				Uri uri = getContentResolver().insert(DB.VirtualGovernor.CONTENT_URI, virtualGovModel.getValues());
				long id = ContentUris.parseId(uri);
				if (id > 0) {
					virtualGovModel.setDbId(id);
				}
			} else if (Intent.ACTION_EDIT.equals(action)) {
				if (origvirtualGovModel.equals(virtualGovModel)) {
					return;
				}
				if (!origvirtualGovModel.equals(virtualGovModel)) {
					updateAllProfiles();
					getContentResolver().update(DB.VirtualGovernor.CONTENT_URI, virtualGovModel.getValues(), DB.NAME_ID + "=?", new String[] { virtualGovModel.getDbId() + "" });
				}
			}
		} catch (Exception e) {
			Logger.w("Cannot insert or update", e);

		}
	}

	private void updateAllProfiles() {
		Cursor c = managedQuery(DB.CpuProfile.CONTENT_URI, CpuProfile.PROJECTION_DEFAULT, PROFILE_SELECTION, new String[] { virtualGovModel.getDbId()+"" }, VirtualGovernor.SORTORDER_DEFAULT);
		while (c.moveToNext()) {
			ProfileModel profile = new ProfileModel(c);
			profile.setGov(virtualGovModel.getGov());
			profile.setGovernorThresholdUp(virtualGovModel.getGovernorThresholdUp());
			profile.setGovernorThresholdDown(virtualGovModel.getGovernorThresholdDown());
			profile.setScript(virtualGovModel.getScript());
			profile.setPowersaveBias(virtualGovModel.getPowersaveBias());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.edit_option, menu);
		getMenuInflater().inflate(R.menu.gerneral_help_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuItemCancel:
			Bundle bundle = new Bundle();
			origvirtualGovModel.saveToBundle(bundle);
			virtualGovModel.readFromBundle(bundle);
			updateView();
			finish();
			break;
		case R.id.menuItemSave:
			finish();
			break;
		default:
			if (GeneralMenuHelper.onOptionsItemSelected(this, item, HelpActivity.PAGE_VIRTUAL_GOVERNOR)) {
				return true;
			}

		}
		return false;
	}
}
