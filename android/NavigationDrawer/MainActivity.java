package com.example.navigationdrawertest;

import android.os.Bundle;
//import android.app.Activity;
import android.view.Menu;
import android.content.res.Configuration;
import android.support.v4.app.ActionBarDrawerToggle;
//import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
//import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends FragmentActivity implements ListView.OnItemClickListener {
	  static String[] menuItems = { "Item 1", "Item 2"};
	  static ArrayAdapter<String> adapter;
	  
	  DrawerLayout drawerLayout;
	  ListView drawerListView;
	  ActionBarDrawerToggle drawerToggle;
	  
	  TextView textView1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViews();
	    setListView();
	    setDrawer();
	}
	
	protected void findViews(){
	    drawerLayout =
	      (DrawerLayout) findViewById(R.id.drawer_layout);
	    drawerListView =
	      (ListView) findViewById(R.id.drawer_listview);
	    textView1 =
	      (TextView) findViewById(R.id.textView1);
	  }
	  
	  protected void setListView(){
	    // データアダプタ
	    adapter = new ArrayAdapter<String>(
	    this,
	    android.R.layout.simple_list_item_1,
	    menuItems);
	    drawerListView.setAdapter(adapter);
	    
	    // アイテムの選択
	    drawerListView.setOnItemClickListener(this);
	  }
	
	@Override
	  public void onItemClick(
	    AdapterView<?> parent, View view, int position, long id) {
	    //textView1.setText((position + 1) + "");
		selectItem(position);
	    drawerLayout.closeDrawers();
	  }
	
	private void selectItem(int position) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		switch(position) {
			case 0:
				ft.replace(R.id.content_frame, new FirstFragment());
				break;
			case 1:
				ft.replace(R.id.content_frame, new SecondFragment());
				break;
			default:
				break;
		}
		ft.addToBackStack(null);
		ft.commit();
	}
	
	protected void setDrawer(){
	    getActionBar().setDisplayHomeAsUpEnabled(true);
	    getActionBar().setHomeButtonEnabled(true);

	    drawerToggle = new ActionBarDrawerToggle(
	      this,
	      drawerLayout,
	      R.drawable.ic_drawer,
	      R.string.drawer_open,
	      R.string.drawer_close);
	    drawerLayout.setDrawerListener(drawerToggle);
	  }
	
	@Override
	  public boolean onOptionsItemSelected(MenuItem item) {
	    if (drawerToggle.onOptionsItemSelected(item)) {
	      return true;
	    }
	    return super.onOptionsItemSelected(item);
	  }
	  
	  @Override
	  protected void onPostCreate(Bundle savedInstanceState) {
	    super.onPostCreate(savedInstanceState);
	    drawerToggle.syncState();
	  }
	  
	  @Override
	  public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    drawerToggle.onConfigurationChanged(newConfig);
	  }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
