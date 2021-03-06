package de.robv.android.xposed.installer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckedTextView;
import android.widget.SimpleAdapter;
import de.robv.android.xposed.installer.util.NavUtil;

public abstract class XposedDropdownNavActivity extends XposedBaseActivity {
	public static final int TAB_INSTALL = 0;
	public static final int TAB_MODULES = 1;
	public static final int TAB_DOWNLOAD = 2;
	public static final int TAB_SETTINGS = 3;
	public static final int TAB_LOGS = 4;
	public static final int TAB_ABOUT = 5;
	public static final int TAB_COUNT = TAB_ABOUT + 1;

	protected int currentNavItem = -1;
	protected static List<Map<String, Object>> navigationItemList = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancelAll();

		final ActionBar bar = getSupportActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		bar.setDisplayShowTitleEnabled(false);
		bar.setDisplayHomeAsUpEnabled(true);

		if (navigationItemList == null) {
			navigationItemList = new ArrayList<Map<String, Object>>();
			navigationItemList.add(makeNavigationItem(getString(R.string.tabInstall), InstallerFragment.class));
			navigationItemList.add(makeNavigationItem(getString(R.string.tabModules), ModulesFragment.class));
			navigationItemList.add(makeNavigationItem(getString(R.string.tabDownload), DownloadFragment.class));
			navigationItemList.add(makeNavigationItem(getString(R.string.tabSettings), SettingsFragment.class));
			navigationItemList.add(makeNavigationItem(getString(R.string.tabLogs), LogsFragment.class));
			navigationItemList.add(makeNavigationItem(getString(R.string.tabAbout), AboutFragment.class));
		}

		SimpleAdapter adapter = new SimpleAdapter(this, navigationItemList,
				R.layout.support_simple_spinner_dropdown_item,
				new String[] { "title" },
				new int[] { android.R.id.text1 });

		bar.setListNavigationCallbacks(adapter, new OnNavigationListener() {
			@Override
			public boolean onNavigationItemSelected(int itemPosition, long itemId) {
				if (currentNavItem == itemPosition)
					return true;

				if (navigateViaIntent()) {
					Intent intent = new Intent(XposedDropdownNavActivity.this, XposedInstallerActivity.class);
					intent.putExtra(XposedInstallerActivity.EXTRA_OPEN_TAB, itemPosition);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					finish();
					return true;
				}

				Map<String, Object> map = navigationItemList.get(itemPosition);
				String fragmentClass = (String) map.get("fragment_class");
				Fragment fragment = Fragment.instantiate(XposedDropdownNavActivity.this, fragmentClass);

				FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
				tx.replace(android.R.id.content, fragment);
				currentNavItem = itemPosition;
				tx.commit();

				getSupportFragmentManager().executePendingTransactions();

				return true;
			}
		});
	}

	private Map<String, Object> makeNavigationItem(String title, Class<? extends Fragment> fragmentClass) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("title", title);
		map.put("fragment_class", fragmentClass.getName());
		return map;
	}

	void setNavItem(int position) {
		this.currentNavItem = position;
		getSupportActionBar().setSelectedNavigationItem(position);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			if (!getIntent().getBooleanExtra(NavUtil.FINISH_ON_UP_NAVIGATION, false)) {
				Intent parentIntent = getParentIntent();
				parentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(parentIntent);
			}
			finish();
			NavUtil.setTransitionSlideLeave(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected boolean navigateViaIntent() {
		return false;
	}

	protected Intent getParentIntent() {
		return new Intent(this, WelcomeActivity.class);
	}
}
