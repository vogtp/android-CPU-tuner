package ch.amana.android.cputuner.view.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.EditorActionbarHelper;
import ch.amana.android.cputuner.helper.EditorActionbarHelper.EditorCallback;
import ch.amana.android.cputuner.helper.EditorActionbarHelper.ExitStatus;
import ch.amana.android.cputuner.helper.GeneralMenuHelper;
import ch.amana.android.cputuner.helper.GuiUtils;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.model.ModelAccess;
import ch.amana.android.cputuner.model.VirtualGovernorModel;
import ch.amana.android.cputuner.provider.DB;
import ch.amana.android.cputuner.provider.DB.VirtualGovernor;
import ch.amana.android.cputuner.view.fragments.GovernorFragment;
import ch.amana.android.cputuner.view.fragments.GovernorFragmentCallback;
import ch.amana.android.cputuner.view.widget.CputunerActionBar;

import com.markupartist.android.widget.ActionBar;

public class VirtualGovernorEditorActivity extends FragmentActivity implements GovernorFragmentCallback, EditorCallback {

	private static final String TAG_GOVERNOR_FRAGMENT = "TAG_GOVERNOR_FRAGMENT";
	private VirtualGovernorModel virtualGovModel;
	private EditText etVirtualGovernorName;
	private ExitStatus exitStatus = ExitStatus.undefined;
	private ModelAccess modelAccess;
	private VirtualGovernorModel origVirtualGovModel;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.virtual_governor_editor);

		modelAccess = ModelAccess.getInstace(this);

		String action = getIntent().getAction();
		if (Intent.ACTION_EDIT.equals(action)) {
			virtualGovModel = modelAccess.getVirtualGovernor(getIntent().getData());
		} else if (DB.ACTION_INSERT_AS_NEW.equals(action)) {
			virtualGovModel = modelAccess.getVirtualGovernor(getIntent().getData());
			virtualGovModel.setVirtualGovernorName(null);
			virtualGovModel.setDbId(-1);
		}

		if (virtualGovModel == null) {
			virtualGovModel = new VirtualGovernorModel();
			virtualGovModel.setVirtualGovernorName("");
			String[] availCpuGov = CpuHandler.getInstance().getAvailCpuGov();
			if (availCpuGov.length > 0) {
				virtualGovModel.setGov(availCpuGov[0]);
			}
		}

		origVirtualGovModel = new VirtualGovernorModel(virtualGovModel);

		CputunerActionBar cputunerActionBar = (CputunerActionBar) findViewById(R.id.abCpuTuner);
		if (SettingsStorage.getInstance(this).hasHoloTheme()) {
			getActionBar().setSubtitle(R.string.titleVirtualGovernorEditor);
			cputunerActionBar.setVisibility(View.GONE);
		} else {
			cputunerActionBar.setHomeAction(new ActionBar.Action() {
				@Override
				public void performAction(View view) {
					onBackPressed();
				}

				@Override
				public int getDrawable() {
					return R.drawable.cputuner_back;
				}
			});
			cputunerActionBar.setTitle(getString(R.string.titleVirtualGovernorEditor) + " " + virtualGovModel.getVirtualGovernorName());
			EditorActionbarHelper.addActions(this, cputunerActionBar);
		}
		etVirtualGovernorName = (EditText) findViewById(R.id.etVirtualGovernorName);
		GovernorFragment governorFragment = new GovernorFragment(this, virtualGovModel);
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.add(R.id.llGovernorFragmentAncor, governorFragment, TAG_GOVERNOR_FRAGMENT);
		fragmentTransaction.commit();
	}

	@Override
	public void updateModel() {
		virtualGovModel.setVirtualGovernorName(etVirtualGovernorName.getText().toString().trim());
		getGovernorFragment().updateModel();
	}

	private GovernorFragment getGovernorFragment() {
		return (GovernorFragment) getSupportFragmentManager().findFragmentByTag(TAG_GOVERNOR_FRAGMENT);
	}

	@Override
	public void updateView() {
		etVirtualGovernorName.setText(virtualGovModel.getVirtualGovernorName());
		getGovernorFragment().updateView();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (exitStatus != ExitStatus.discard) {
			updateModel();
			virtualGovModel.saveToBundle(outState);
		} else {
			origVirtualGovModel.saveToBundle(outState);
		}
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
		if (hasChange() && hasName() && isNameUnique()) {
			try {
				String action = getIntent().getAction();
				if (exitStatus == ExitStatus.save) {
					if (Intent.ACTION_INSERT.equals(action) || DB.ACTION_INSERT_AS_NEW.equals(action)) {
						modelAccess.insertVirtualGovernor(virtualGovModel);
					} else if (Intent.ACTION_EDIT.equals(action)) {
						modelAccess.updateVirtualGovernor(virtualGovModel);
					}
					modelAccess.updateProfileFromVirtualGovernor();// FIXME needed since gov information is in profile
					modelAccess.clearCache(); // FIXME needed since gov information is in profile
				}
			} catch (Exception e) {
				Logger.w("Cannot insert or update", e);
			}
		}
	}

	private boolean hasChange() {
		updateModel();
		return !origVirtualGovModel.equals(virtualGovModel);
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
			exitStatus = ExitStatus.discard;
			finish();
			break;
		case R.id.menuItemSave:
			exitStatus = ExitStatus.save;
			save();
			break;
		default:
			if (GeneralMenuHelper.onOptionsItemSelected(this, item, HelpActivity.PAGE_VIRTUAL_GOVERNOR)) {
				return true;
			}

		}
		return false;
	}

	@Override
	public void discard() {
		exitStatus = ExitStatus.discard;
		finish();
	}

	private boolean hasName() {
		String name = virtualGovModel.getVirtualGovernorName();
		return name != null && !"".equals(name.trim());
	}

	private boolean isNameUnique() {
		Cursor cursor = null;
		try {
			cursor = getContentResolver().query(VirtualGovernor.CONTENT_URI, VirtualGovernor.PROJECTION_ID_NAME, VirtualGovernor.SELECTION_NAME,
					new String[] { virtualGovModel.getVirtualGovernorName() }, null);
			if (cursor.moveToFirst()) {
				return cursor.getLong(DB.INDEX_ID) == virtualGovModel.getDbId();
			}
			return true;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	@Override
	public void save() {
		updateModel();
		boolean ok = true;
		if (!hasName()) {
			GuiUtils.showDialog(this, R.string.title_cannot_save, R.string.msg_no_virtgov_name);
			//			Toast.makeText(this, R.string.msg_no_virtgov_name, Toast.LENGTH_LONG).show();
			ok = false;
		}
		if (ok && !isNameUnique()) {
			GuiUtils.showDialog(this, R.string.title_cannot_save, R.string.msg_virtgovname_exists);
			//			Toast.makeText(this, R.string.msg_virtgovname_exists, Toast.LENGTH_LONG).show();
			ok = false;
		}
		if (ok) {
			exitStatus = ExitStatus.save;
			finish();
		}
	}

	@Override
	public void onBackPressed() {
		EditorActionbarHelper.onBackPressed(this, exitStatus, hasChange());
	}

	@Override
	public Context getContext() {
		return this;
	}
}
