package com.mejorandola.ejemplo18.activities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.mejorandola.ejemplo18.R;
import com.mejorandola.ejemplo18.data.App;
import com.mejorandola.ejemplo18.fragments.dialogs.ConfirmStorePhotoDialogFragment;
import com.mejorandola.ejemplo18.fragments.dialogs.SendDataDialogFragment;
import com.mejorandola.ejemplo18.models.Room;

public class RoomDetailActivity extends FragmentActivity implements SendDataDialogFragment.DialogListener,
																	ConfirmStorePhotoDialogFragment.DialogListener{
	public final static String ROOM_TYPE = "tipo de habitaci�n";
	public final static String ROOM_NUMBER = "n�mero de habitaci�n";
	public final static String DIALOG_TAG = "dialogo";
	public final static String CONFIRMATION_DIALOG_TAG = "dialogo de confirmacion";
		
	private App app;
	private Room room;
	private int resource = -1;
	private boolean favorite = false;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_room_detail);
	
		app = (App) getApplicationContext();
		
		Intent intent = getIntent();
		room = new Room(intent.getStringExtra(ROOM_NUMBER), intent.getStringExtra(ROOM_TYPE));
		ToggleButton toggle_recommendation = (ToggleButton)findViewById(R.id.toggle_recommendation);
		toggle_recommendation.setChecked(true);
		
		if (room.getRoomType().equals(Room.STANDARD_ROOM)) {
			resource = R.drawable.hotel1; 
		} else {
			resource = R.drawable.hotel2;
		}
		
		ImageView img_header = (ImageView)findViewById(R.id.img_header);
		img_header.setImageResource(resource);
		img_header.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				ConfirmStorePhotoDialogFragment frag = new ConfirmStorePhotoDialogFragment();
	        	frag.show(getSupportFragmentManager(),CONFIRMATION_DIALOG_TAG);
				return false;
			}
		});
		
		setTitle(room.getRoomNumber());
		
		SharedPreferences settings = getSharedPreferences(room.getRoomNumber(), 0);
		favorite = settings.getBoolean("favorite", false);	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.detail, menu);
    	if (favorite) {
    		MenuItem favorite_item = menu.getItem(0);
    		favorite_item.setIcon(getResources().getDrawable(R.drawable.rating_important));	
    	}		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.action_fav:
	        	Drawable icon = null;
	        	if (favorite) {
	        		icon = getResources().getDrawable(R.drawable.rating_not_important);
	        		app.removeFavoriteRoom(room);
	        	} else {
	        		icon = getResources().getDrawable(R.drawable.rating_important);
	        		app.addFavoriteRoom(room);
	        	}

	        	favorite = !favorite;
	        	item.setIcon(icon);
	            return true;
	        case R.id.action_share:
	        	Intent i = new Intent();
	        	i.setAction(Intent.ACTION_SEND);
	        	i.putExtra(Intent.EXTRA_TEXT, "Me gust� la habitaci�n " + room.getRoomNumber() + " tipo " + room.getRoomType());	        	
	        	i.putExtra(Intent.EXTRA_STREAM, Uri.parse("android.resource://" + getPackageName() + "/drawable/" + R.drawable.hotel1));
	        	i.setType("image/jpeg");
	        	startActivity(Intent.createChooser(i, getResources().getText(R.string.msg_share)));	        	
	            return true;
	        case R.id.action_dialog:
	        	SendDataDialogFragment frag = new SendDataDialogFragment();
	        	frag.show(getSupportFragmentManager(),DIALOG_TAG);	        	
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}	
	
    @Override
    protected void onStop(){
       super.onStop();

      SharedPreferences settings = getSharedPreferences(room.getRoomNumber(), 0);
      SharedPreferences.Editor editor = settings.edit();
      editor.putBoolean("favorite", favorite);
      editor.commit();
    }	
	
	public void toggleClicked(View v){
		Toast.makeText(getApplicationContext(), 
					   getResources().getString(R.string.msg_photo_stored_succesfull), 
					   Toast.LENGTH_SHORT).show();
	}
	
	@Override
    public void onDialogPositiveClick(DialogFragment dialog) {
		Toast.makeText(getApplicationContext(), "Click " + getResources().getString(R.string.msg_yes), Toast.LENGTH_SHORT).show();
    }
	
	@Override
    public void onDialogNegativeClick(DialogFragment dialog) {
		Toast.makeText(getApplicationContext(), "Click " + getResources().getString(R.string.msg_no), Toast.LENGTH_SHORT).show();
    }

	@Override
	public void confirmStorePhotoDialogPositiveClick(DialogFragment dialog) {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			
			File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
								 + "/" + getResources().getString(R.string.app_name));
			File img = new File(path,room.getRoomNumber() + ".jpg");

		    if (path.mkdirs() || path.isDirectory()) {
				try {
			    	InputStream is = getResources().openRawResource(resource);
			        OutputStream os = new FileOutputStream(img);
			        byte[] data = new byte[is.available()];
			        is.read(data);
			        os.write(data);
			        is.close();
			        os.close();			        	

                	final Toast success_toast = Toast.makeText(getApplicationContext(),
         				   									   getResources().getString(R.string.msg_photo_stored_succesfull),
         				   									   Toast.LENGTH_SHORT);
		            MediaScannerConnection.scanFile(this,
		                    						new String[] { img.toString() }, null,
		                    						new MediaScannerConnection.OnScanCompletedListener() {
										                public void onScanCompleted(String path, Uri uri) {
										                	success_toast.show();
										                }
		            								});		            
				} catch (FileNotFoundException e) {							
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} 
		    }					
		}
	}

	@Override
	public void confirmStorePhotoDialogNegativeClick(DialogFragment dialog) {
	}

}


