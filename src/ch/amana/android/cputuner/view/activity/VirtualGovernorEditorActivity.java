package ch.amana.android.cputuner.view.activity;

import android.content.Intent;
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
import ch.amana.android.cputuner.model.ModelAccess;
import ch.amana.android.cputuner.model.VirtualGovernorModel;
import ch.amana.android.cputuner.view.fragments.GovernorBaseFragment;
import ch.amana.android.cputuner.view.fragments.GovernorFragment;
import ch.amana.android.cputuner.view.fragments.GovernorFragmentCallback;

public class VirtualGovernorEditorActivity extends FragmentActivity implements GovernorFragmentCallback {

	private GovernorBaseFragment governorFragment;
	private VirtualGovernorModel virtualGovModel;
	private EditText etVirtualGovernorName;
	private boolean save;
	private ModelAccess modelAccess;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

		setContentView(R.layout.virtual_governor_editor);

		modelAccess = ModelAccess.getInstace(this);

		String action = getIntent().getAction();
		if (Intent.ACTION_EDIT.equals(action)) {
			virtualGovModel = modelAccess.getVirtualGovernor(getIntent().getData());
		}

		if (virtualGovModel == null) {
			virtualGovModel = new VirtualGovernorModel();
			virtualGovModel.setVirtualGovernorName("");
		}

		setTitle(getString(R.string.titleVirtualGovernorEditor) + " " + virtualGovModel.getVirtualGovernorName());

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
		save = true;
		updateView();
	}

	@Override
	protected void onPause() {
		super.onPause();
		updateModel();
		try {
			String action = getIntent().getAction();
			if (Intent.ACTION_INSERT.equals(action)) {
				if (save) {
					modelAccess.insertVirtualGovernor(virtualGovModel);
				}
			} else if (Intent.ACTION_EDIT.equals(action)) {
				if (save) {
					modelAccess.updateVirtualGovernor(virtualGovModel);
				}
			}
		} catch (Exception e) {
			Logger.w("Cannot insert or update", e);

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
			save = false;
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
