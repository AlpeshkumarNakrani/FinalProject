/*
Copyright (C) 2011  Wade Chatam

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.kgsinfoway.raaz3puzzle;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.AdView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * This is the primary @class Activity for the PhotoGaffe application. It
 * creates the game board and handles any menu items selected.
 * 
 * @author wadechatam
 * 
 */
public final class PuzzleActivity extends Activity implements AdListener {

    public static final int IMAGEREQUESTCODE = 8242008;
    public static final int DIALOG_PICASA_ERROR_ID = 0;
    public static final int DIALOG_GRID_SIZE_ID = 1;
    public static final int DIALOG_COMPLETED_ID = 2;
    private GameBoard board;
    private Bitmap bitmap; // temporary holder for puzzle picture
    private boolean numbersVisible = false; // Whether a title is displayed that
    // shows the correct location of the
    // tiles.
    Button ad_close;
    FrameLayout adLayout;
    AdView mAdView2, mAdView;
    LinearLayout adLinearLayout;
    AdRequest adRequest;
    TableLayout tableLayout;
    private static final String TEST_DEVICE_ID = "...";
    int gridsize;
    Button Hint;
    TextView hintImageView;
    boolean show_hint = false;

    TextView moveTextView;

    int[] imagesID = { R.drawable.s_1, R.drawable.s_2, R.drawable.s_3,
	    R.drawable.s_4, R.drawable.s_5, R.drawable.s_6, R.drawable.s_7,
	    R.drawable.s_8, R.drawable.s_9, R.drawable.s_10, R.drawable.s_11,
	    R.drawable.s_12, R.drawable.s_13, R.drawable.s_14, R.drawable.s_15 };

    GridView gridview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.board);

	/*
	 * sendBroadcast(new Intent( Intent.ACTION_MEDIA_MOUNTED,
	 * Uri.parse("file://" + Environment.getExternalStorageDirectory())));
	 * 
	 * MediaScannerConnection.scanFile(this, new String[] { file.toString()
	 * }, null, new MediaScannerConnection.OnScanCompletedListener() {
	 * public void onScanCompleted(String path, Uri uri) {
	 * Log.i("ExternalStorage", "Scanned " + path + ":");
	 * Log.i("ExternalStorage", "-> uri=" + uri); } });
	 */

	Bundle extras = getIntent().getExtras();
	if (extras != null) {
	    gridsize = extras.getInt("grid_size");
	}

	gridview = (GridView) findViewById(R.id.gridview);
	gridview.setAdapter(new ImageAdapter(this));

	gridview.setOnItemClickListener(new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View v,
		    int position, long id) {
		Toast.makeText(PuzzleActivity.this, "" + position,
			Toast.LENGTH_SHORT).show();

		Uri imageUri = Uri.parse("android.resource://"
			+ PuzzleActivity.this.getPackageName() + "/"
			+ imagesID[position]);

		Log.v("log_tag", "Image Uri = " + imageUri.getPath());

		try {
		    bitmap = createScaledBitmap(imageUri);
		} catch (FileNotFoundException e) {

		    showDialog(DIALOG_PICASA_ERROR_ID);
		} catch (IOException e) {
		    e.printStackTrace();
		    finish();
		} catch (IllegalArgumentException e) {
		    showDialog(DIALOG_PICASA_ERROR_ID);
		}

		createGameBoard((short) gridsize);

		gridview.setVisibility(View.GONE);
	    }
	});

	adLayout = (FrameLayout) findViewById(R.id.ad_layout);
	tableLayout = (TableLayout) findViewById(R.id.parentLayout);

	mAdView2 = (AdView) findViewById(R.id.admobAdview_mrect);
	mAdView = (AdView) findViewById(R.id.admobAdview);
	tableLayout.setVisibility(View.VISIBLE);
	adLayout.setVisibility(View.GONE);

	moveTextView = (TextView) findViewById(R.id.movesTextView);

	Hint = (Button) findViewById(R.id.Hint);
	hintImageView = (TextView) findViewById(R.id.HintImageview);
	hintImageView.setVisibility(View.GONE);
	Hint.setText("Show Hint");
	Hint.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View v) {
		// TODO Auto-generated method stub

		if (show_hint) {
		    hintImageView.startAnimation(AnimationUtils.loadAnimation(
			    PuzzleActivity.this, R.anim.down_animation));

		    // tableLayout.setVisibility(View.VISIBLE);
		    hintImageView.setVisibility(View.GONE);
		    show_hint = false;
		    Hint.setText("Show Hint");
		} else {
		    show_hint = true;
		    Hint.setText("Show Puzzle");
		    // tableLayout.setVisibility(View.GONE);
		    hintImageView.setVisibility(View.VISIBLE);

		    hintImageView.startAnimation(AnimationUtils.loadAnimation(
			    PuzzleActivity.this, R.anim.up_animation));
		}

	    }
	});

	// Timer timer = new Timer();
	// TimerTask task = new TimerTask() {
	//
	// @Override
	// public void run() {
	//
	// runOnUiThread(new Runnable() {
	//
	// @Override
	// public void run() {
	// // TODO Auto-generated method stub
	// // TODO Auto-generated method stub
	// adRequest = new AdRequest();
	// adRequest.addTestDevice(TEST_DEVICE_ID);
	// mAdView2.loadAd(adRequest);
	//
	// adLayout.setVisibility(View.VISIBLE);
	// tableLayout.setVisibility(View.GONE);
	// }
	// });
	//
	// }
	// };
	// timer.scheduleAtFixedRate(task, 3000, 30000);

	ad_close = (Button) findViewById(R.id.ad_close);
	ad_close.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View v) {
		// TODO Auto-generated method stub
		adLayout.setVisibility(View.GONE);
		tableLayout.setVisibility(View.VISIBLE);
	    }
	});

	adLinearLayout = (LinearLayout) findViewById(R.id.adLayout);
	adLinearLayout.setVisibility(View.GONE);
	mAdView2.setAdListener(this);
	mAdView.setAdListener(this);
	selectImageFromGallery();

    }

    /*
     * (non-Javadoc) Will start an intent for external Gallery app. Image
     * returned via onActivityResult().
     */
    private void selectImageFromGallery() {

	// Intent galleryIntent = new Intent();
	// galleryIntent.setAction(Intent.ACTION_PICK);
	// File file = new File("/sdcard/Images");
	// galleryIntent.setDataAndType(Uri.fromFile(file), "image/*");
	// startActivityForResult(galleryIntent, IMAGEREQUESTCODE);

	// startActivity(new Intent(getApplicationContext(),
	// LoadImagesFromSDCardActivity.class));

	gridview.setVisibility(View.VISIBLE);

    }

    public class ImageAdapter extends BaseAdapter {
	private Context mContext;

	public ImageAdapter(Context c) {
	    mContext = c;
	}

	public int getCount() {
	    return imagesID.length;
	}

	public Object getItem(int position) {
	    return null;
	}

	public long getItemId(int position) {
	    return 0;
	}

	// create a new ImageView for each item referenced by the Adapter
	public View getView(int position, View convertView, ViewGroup parent) {
	    ImageView imageView;
	    if (convertView == null) { // if it's not recycled, initialize some
				       // attributes
		imageView = new ImageView(mContext);
		imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
		imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		imageView.setPadding(8, 8, 8, 8);
	    } else {
		imageView = (ImageView) convertView;
	    }

	    imageView.setImageBitmap(decodeSampledBitmapFromResource(
		    getResources(), imagesID[position], 100, 100));
	    return imageView;
	}

    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
	    int reqWidth, int reqHeight) {
	// Raw height and width of image
	final int height = options.outHeight;
	final int width = options.outWidth;
	int inSampleSize = 1;

	if (height > reqHeight || width > reqWidth) {
	    if (width > height) {
		inSampleSize = Math.round((float) height / (float) reqHeight);
	    } else {
		inSampleSize = Math.round((float) width / (float) reqWidth);
	    }
	}
	return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res,
	    int resId, int reqWidth, int reqHeight) {

	// First decode with inJustDecodeBounds=true to check dimensions
	final BitmapFactory.Options options = new BitmapFactory.Options();
	options.inJustDecodeBounds = true;
	BitmapFactory.decodeResource(res, resId, options);

	// Calculate inSampleSize
	options.inSampleSize = calculateInSampleSize(options, reqWidth,
		reqHeight);

	// Decode bitmap with inSampleSize set
	options.inJustDecodeBounds = false;
	return BitmapFactory.decodeResource(res, resId, options);
    }

    /*
     * (non-Javadoc) Run when Gallery app returns selected image.
     */
    @Override
    protected final void onActivityResult(final int requestCode,
	    final int resultCode, final Intent i) {
	super.onActivityResult(requestCode, resultCode, i);

	if (resultCode == RESULT_OK) {
	    switch (requestCode) {
	    case IMAGEREQUESTCODE:
		Uri imageUri = i.getData();

		try {
		    bitmap = createScaledBitmap(imageUri);
		} catch (FileNotFoundException e) {
		    // You see, what had happened was...
		    // When using the Gallery app for selecting an image, the
		    // Gallery will display the user's on-line Picasa web
		    // albums. If the user attempts to select one of the
		    // pictures from their Picasa web albums, Gingerbread and
		    // earlier versions of the OS will throw this exception.
		    // Honeycomb and later will automatically download the
		    // picture. This catch will be called for Gingerbread
		    // users and just tell them that we do not support using
		    // Picasa web album pictures for their version of Android.
		    // They will then be prompted to select another picture from
		    // the Gallery.
		    showDialog(DIALOG_PICASA_ERROR_ID);
		} catch (IOException e) {
		    e.printStackTrace();
		    finish();
		} catch (IllegalArgumentException e) {
		    showDialog(DIALOG_PICASA_ERROR_ID);
		}

		createGameBoard((short) gridsize);
		break;
	    } // end switch
	} // end if
    }

    /*
     * (non-Javadoc) Returns a scaled image of the bitmap at the given location.
     * This helps prevent OutOfMemory exceptions when loading large images from
     * the SD card.
     */
    private Bitmap createScaledBitmap(Uri uri) throws FileNotFoundException,
	    IOException, IllegalArgumentException {
	InputStream is = getContentResolver().openInputStream(uri);
	// DisplayMetrics metrics = new DisplayMetrics();
	// getWindowManager().getDefaultDisplay().getMetrics(metrics);
	//
	// BitmapFactory.Options boundingBox = new BitmapFactory.Options();
	// boundingBox.inJustDecodeBounds = true;
	// boundingBox.inDither = true;
	// BitmapFactory.decodeStream(is, null, boundingBox);
	// is.close();
	//
	// int screenSize = (int) (metrics.widthPixels * metrics.density
	// * (metrics.heightPixels) * metrics.density);
	// int imageSize = boundingBox.outWidth * boundingBox.outHeight;
	//
	// BitmapFactory.Options pictureOptions = new BitmapFactory.Options();
	//
	// // TODO improve the mechanism for determining if an image should be
	// // sampled based on memory available and size of image.
	// // if (imageSize > screenSize) {
	// // pictureOptions.inSampleSize = 8;
	// // // Integer.highestOneBit((int) Math.ceil(imageSize / screenSize));
	// // }
	//
	// pictureOptions.inDither = true;
	// is = getContentResolver().openInputStream(uri);
	Bitmap bitmap = BitmapFactory.decodeStream(is, null, null);
	is.close();
	return bitmap;
    }

    /*
     * (non-Javadoc) Basic wrapper method for creating the game board and
     * setting the number visibility.
     * 
     * @param gridSize row and column count (3 = 3x3; 4 = 4x4; 5 = 5x5; etc.)
     */
    private final void createGameBoard(short gridSize) {
	DisplayMetrics metrics = new DisplayMetrics();
	getWindowManager().getDefaultDisplay().getMetrics(metrics);

	tableLayout = (TableLayout) findViewById(R.id.parentLayout);
	tableLayout.removeAllViews();

	if (metrics.densityDpi == DisplayMetrics.DENSITY_MEDIUM) {

	    hintImageView
		    .setBackgroundDrawable(new BitmapDrawable(
			    Bitmap.createScaledBitmap(
				    bitmap,
				    (int) (metrics.widthPixels * metrics.density),
				    (int) ((metrics.heightPixels - 125) * metrics.density),
				    true)));

	    board = GameBoard.createGameBoard(this, bitmap, tableLayout,
		    (int) (metrics.widthPixels * metrics.density),
		    (int) ((metrics.heightPixels - 125) * metrics.density),
		    gridSize, moveTextView);

	} else if (metrics.densityDpi == DisplayMetrics.DENSITY_HIGH) {

	    hintImageView
		    .setBackgroundDrawable(new BitmapDrawable(
			    Bitmap.createScaledBitmap(
				    bitmap,
				    (int) (metrics.widthPixels * metrics.density),
				    (int) ((metrics.heightPixels - 188) * metrics.density),
				    true)));

	    board = GameBoard.createGameBoard(this, bitmap, tableLayout,
		    (int) (metrics.widthPixels * metrics.density),
		    (int) ((metrics.heightPixels - 188) * metrics.density),
		    gridSize, moveTextView);

	} else if (metrics.densityDpi == DisplayMetrics.DENSITY_LOW) {

	    hintImageView
		    .setBackgroundDrawable(new BitmapDrawable(
			    Bitmap.createScaledBitmap(
				    bitmap,
				    (int) (metrics.widthPixels * metrics.density),
				    (int) ((metrics.heightPixels - 94) * metrics.density),
				    true)));

	    board = GameBoard.createGameBoard(this, bitmap, tableLayout,
		    (int) (metrics.widthPixels * metrics.density),
		    (int) ((metrics.heightPixels - 94) * metrics.density),
		    gridSize, moveTextView);
	} else {

	    hintImageView
		    .setBackgroundDrawable(new BitmapDrawable(
			    Bitmap.createScaledBitmap(
				    bitmap,
				    (int) (metrics.widthPixels * metrics.density),
				    (int) ((metrics.heightPixels - 94) * metrics.density),
				    true)));

	    board = GameBoard.createGameBoard(this, bitmap, tableLayout,
		    (int) (metrics.widthPixels * metrics.density),
		    (int) ((metrics.heightPixels - 94) * metrics.density),
		    gridSize, moveTextView);
	}

	board.setNumbersVisible(numbersVisible);
	// bitmap.recycle(); // free memory for this copy of the picture since
	// the
	// picture is stored by the GameBoard class
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.arranger_menu, menu);
	return true;
    }

    // TODO Replace this with ActionBar support for IceCream Sandwich (4.0)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	boolean returnVal = true;

	switch (item.getItemId()) {
	case R.id.new_picture:
	    selectImageFromGallery();
	    break;
	case R.id.reshuffle:
	    board.shuffleTiles();
	    break;
	case R.id.settings:
	    startActivity(new Intent(PuzzleActivity.this,
		    SettingsActivity.class));
	default:
	    returnVal = super.onOptionsItemSelected(item);
	}

	return returnVal;
    }

    @Override
    protected void onResume() {
	super.onResume();
	numbersVisible = SettingsActivity.isNumbersVisible(this);

	adRequest = new AdRequest();
	adRequest.addTestDevice(TEST_DEVICE_ID);
	mAdView2.loadAd(adRequest);
	mAdView.loadAd(adRequest);

	if (board == null) {
	    return;
	}

	board.setNumbersVisible(numbersVisible);
	// Check if the size of the board has changed, since this puzzle was
	// started. If so, create a new board.
	if (board.getGridSize() != gridsize) {

	    createGameBoard((short) gridsize);
	}
    }

    @Override
    protected Dialog onCreateDialog(int id) {
	Dialog dialog;
	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	switch (id) {
	case DIALOG_PICASA_ERROR_ID:
	    builder.setMessage(R.string.picasa_error)
		    .setCancelable(false)
		    .setNeutralButton(R.string.ok,
			    new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,
					int which) {
				    dialog.dismiss();
				    // After message is displayed, have the user
				    // pick again.
				    selectImageFromGallery();
				}
			    });
	    dialog = builder.create();
	    break;
	case DIALOG_COMPLETED_ID:
	    builder.setMessage(createCompletionMessage())
		    .setCancelable(false)
		    .setNeutralButton(R.string.ok,
			    new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,
					int which) {
				    dialog.dismiss();
				    board.shuffleTiles();
				}
			    });
	    dialog = builder.create();
	    break;
	default:
	    dialog = null;
	}
	return dialog;
    }

    // TODO When updating to ICS-level API, replace this with Fragment
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
	switch (id) {
	case DIALOG_COMPLETED_ID:
	    ((AlertDialog) dialog).setMessage(createCompletionMessage());
	    break;
	}
    }

    /*
     * (non-Javadoc) Return a 'congratulatory' message that also contains the
     * number of moves.
     */
    private String createCompletionMessage() {
	String completeMsg = getResources().getString(R.string.congratulations)
		+ " " + String.valueOf(board.getMoveCount());
	String[] insults = getResources().getStringArray(R.array.insults);
	completeMsg += "\n";
	int insultIndex = (int) Math.floor(Math.random() * insults.length);
	completeMsg += insults[insultIndex];

	return completeMsg;
    }

    @Override
    public void onDismissScreen(Ad arg0) {
	// TODO Auto-generated method stub

    }

    @Override
    public void onFailedToReceiveAd(Ad arg0, ErrorCode arg1) {
	// TODO Auto-generated method stub
	adLayout.setVisibility(View.GONE);
    }

    @Override
    public void onLeaveApplication(Ad arg0) {
	// TODO Auto-generated method stub

    }

    @Override
    public void onPresentScreen(Ad arg0) {
	// TODO Auto-generated method stub

    }

    @Override
    public void onReceiveAd(Ad arg0) {
	// TODO Auto-generated method stub

	adLinearLayout.setVisibility(View.VISIBLE);
	mAdView.startAnimation(AnimationUtils.loadAnimation(
		PuzzleActivity.this, R.anim.slide_in_right));
    }

}