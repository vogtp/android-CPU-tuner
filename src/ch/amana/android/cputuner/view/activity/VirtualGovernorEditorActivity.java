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
import android.widget.Toast;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.EditorActionbarHelper;
import ch.amana.android.cputuner.helper.EditorActionbarHelper.EditorCallback;
import ch.amana.android.cputuner.helper.EditorActionbarHelper.ExitStatus;
import ch.amana.android.cputuner.helper.GeneralMenuHelper;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.model.ModelAccess;
import ch.amana.android.cputuner.model.VirtualGovernorModel;
import ch.amana.android.cputuner.provider.CpuTunerProvider;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.provider.db.DB.VirtualGovernor;
import ch.amana.android.cputuner.view.fragments.GovernorBaseFragment;
import ch.amana.android.cputuner.view.fragments.GovernorFragment;
import ch.amana.android.cputuner.view.fragments.GovernorFragmentCallback;
import ch.amana.android.cputuner.view.widget.CputunerActionBar;

import com.markupartist.android.widget.ActionBar;

public class VirtualGovernorEditorActivity extends FragmentActivity implements GovernorFragmentCallback, EditorCallback {

	private GovernorBaseFragment governorFragment;
	private VirtualGovernorModel virtualGovModel;
	private EditText etVirtualGovernorName;
	private ExitStatus exitStatus = ExitStatus.save;
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
		} else if (CpuTunerProvider.ACTION_INSERT_AS_NEW.equals(action)) {
			virtualGovModel = modelAccess.getVirtualGovernor(getIntent().getData());
			virtualGovModel.setVirtualGovernorName(null);
			virtualGovModel.setDbId(-1);
		}

		if (virtualGovModel == null) {
			virtualGovModel = new VirtualGovernorModel();
			virtualGovModel.setVirtualGovernorName("");
		}
		Bundle bundle = new Bundle();
		virtualGovModel.saveToBundle(bundle);
		origVirtualGovModel = new VirtualGovernorModel(bundle);

		CputunerActionBar actionBar = (CputunerActionBar) findViewById(R.id.abCpuTuner);
		actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public void performAction(View view) {
			}

			@Override
			public int getDrawable() {
				return R.drawable.icon;
			}
		});
		actionBar.setTitle(getString(R.string.titleVirtualGovernorEditor) + " " + virtualGovModel.getVirtualGovernorName());
		EditorActionbarHelper.addActions(this, actionBar);

		etVirtualGovernorName = (EditText) findViewById(R.id.etVirtualGovernorName);
		governorFragment = new GovernorFragment(this, virtualGovModel);
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.add(R.id.llGovernorFragmentAncor, governorFragment);
		fragmentTransaction.commit();
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
		updateModel();
		if (hasChange() && hasName() && isNameUnique()) {
			try {
				String action = getIntent().getAction();
				if (exitStatus == ExitStatus.save) {
					if (Intent.ACTION_INSERT.equals(action)) {
						modelAccess.insertVirtualGovernor(virtualGovModel);
					} else if (Intent.ACTION_EDIT.equals(action)) {
						modelAccess.updateVirtualGovernor(virtualGovModel);
					}
				}
			} catch (Exception e) {
				Logger.w("Cannot insert or update", e);
			}
		}
	}

	private boolean hasChange() {
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
			finish();
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
		virtualGovModel = origVirtualGovModel;
		//		updateView();
		finish();
	}

	private boolean hasName() {
		String name = virtualGovModel.getVirtualGovernorName();
		return name != null && !"".equals(name.trim());
	}

	private boolean isNameUnique() {
		Cursor cursor = null;
		try {
			cursor = managedQuery(VirtualGovernor.CONTENT_URI, VirtualGovernor.PROJECTION_ID_NAME, VirtualGovernor.SELECTION_NAME, new String[] { virtualGovModel
					.getVirtualGovernorName() }, null);
			if (cursor.moveToFirst()) {
				return cursor.getLong(DB.INDEX_ID) == virtualGovModel.getDbId();
			} else {
				return true;
			}
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
			Toast.makeText(this, R.string.msg_no_virtgov_name, Toast.LENGTH_LONG).show();
			ok = false;
		}
		if (ok && !isNameUnique()) {
			Toast.makeText(this, R.string.msg_virtgovname_exists, Toast.LENGTH_LONG).show();
			ok = false;
		}
		if (ok) {
			exitStatus = ExitStatus.save;
			finish();
		}
	}

	//	@Override
	//	public void onBackPressed() {
	//		updateModel();
	//		EditorActionbarHelper.onBackPressed(this, exitStatus, !origVirtualGovModel.equals(virtualGovModel));
	//	}

	@Override
	public Context getContext() {
		return this;
	}
}
