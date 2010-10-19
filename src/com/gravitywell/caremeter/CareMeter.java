package com.gravitywell.caremeter;

import com.gravitywell.caremeter.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class CareMeter extends Activity {
	/** Some variables **/
	private TextView mMeterTextTop;
	private TextView mMeterTextBottom;
	public String text;
	public int meter = 0;
	public double needlePosition = 0;
	public float needlePositionf = 0f;
	static final int DIALOG_TEXT_ID = 0;
	static final int DIALOG_METER_ID = 1;
	
	final RotateAnimation position = new RotateAnimation(90f, 90f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1f);
	RotateAnimation wobble;
	ImageView mFixedNeedle;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);

        mMeterTextTop = (TextView) findViewById(R.id.metertexttop);
        mMeterTextBottom = (TextView) findViewById(R.id.metertextbottom);
        
        SharedPreferences settings = getPreferences(0);
        text = settings.getString("meter_text", "Developer's inanity");
        meter = settings.getInt("meter", 0);
        
        updateText(text);
        updateMeter(meter);
        
        mMeterTextTop.setVisibility(4);
        
        ImageView mMeterImage = (ImageView) findViewById(R.id.meterimage);
        mMeterImage.setOnTouchListener(needleMoveListener);        
        
        mFixedNeedle = (ImageView) findViewById(R.id.fixed_needle);
        
        wobble = new RotateAnimation(needlePositionf - 1f, needlePositionf + 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1.7f);
    	wobble.setInterpolator(new LinearInterpolator());
    	wobble.setRepeatCount(Animation.INFINITE);
    	wobble.setRepeatMode(Animation.REVERSE);
    	wobble.setDuration(80);
    	
        mFixedNeedle.startAnimation(wobble);
        //uncertainNeedle();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.metermenu, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    		case R.id.change_text:
    			showDialog(0);
    			return true;
    		case R.id.change_style:
    			showDialog(1);
    			return true;
    		default:
    				return super.onOptionsItemSelected(item);
    	}
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	
    	// Opens key/value preferences files and edits instance 
    	SharedPreferences settings = getPreferences(0);
    	SharedPreferences.Editor editor = settings.edit();
    	editor.putString("meter_text", text);
    	//editor.putInt("meter", )
    	
    	// Saves stuff
    	editor.commit();
    }
    
    @Override 
    public void onStop(){
    	super.onStop();
    }
    
    protected Dialog onCreateDialog(int id) {
    	Dialog dialog;
    	switch(id) {
    		case DIALOG_TEXT_ID:
    			dialog = titleAlert();
    			break;
    		case DIALOG_METER_ID:
    			dialog = meterAlert();
    			break;
    		default: 
    			dialog = null;
    	}
    	
		return dialog;
    }
    
    public AlertDialog titleAlert() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setTitle("Title");
		builder.setMessage("Message");
		
		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		builder.setView(input);
		
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Save input text
				String value = input.getText().toString();
				updateText(value);
			}
		});
		
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Cancelled
				dialog.cancel();
			}
		});
		
		AlertDialog dialog = builder.create(); 
		return dialog;
    }
    
    public AlertDialog meterAlert() {
    	 final CharSequence[] meters = {"Style 1", "Style 2"};
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	
    	builder.setTitle("Meter style");
    	builder.setItems(meters, new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int value) {
    			Toast.makeText(getApplicationContext(), meters[value], Toast.LENGTH_SHORT).show();
    			updateMeter(value);
    		}
    	});
    	AlertDialog alert = builder.create();
    	return alert;
    }
    
    public void updateMeter(int value) {
		ImageView image = (ImageView) findViewById(R.id.meterimage);
		mFixedNeedle = (ImageView) findViewById(R.id.fixed_needle);
		if(value==0) {
			image.setImageResource(R.drawable.vumeter_1);
			mFixedNeedle.setImageDrawable(getResources().getDrawable(R.drawable.needle_red));
			mMeterTextTop.setVisibility(4);
			mMeterTextBottom.setVisibility(0);
			meter = 0;
		}
		else if(value==1) {
			image.setImageResource(R.drawable.vumeter_2);
			mFixedNeedle.setImageDrawable(getResources().getDrawable(R.drawable.needle_green));
			mMeterTextTop.setVisibility(0);
			mMeterTextBottom.setVisibility(4);
			meter = 1;
		}
		else updateText(Integer.toString(value));
    }
    
    public void updateText(String passed_text) {
    	text = passed_text;	
    	mMeterTextTop.setText(passed_text);
    	mMeterTextBottom.setText(passed_text);
    }
    
    public void uncertainNeedle() {
    	final ImageView quiver = (ImageView) findViewById(R.id.fixed_needle);
    	//quiver.startAnimation(anim);
    }
    
    private OnTouchListener needleMoveListener = new OnTouchListener() {
    	public boolean onTouch(View v, MotionEvent event) {
    		mFixedNeedle.clearAnimation();
    		wobble.reset();
    		    		
    		double x = (double) event.getX();
    		double y = (double) event.getY();
    		
    		Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    		double width = (double) display.getWidth();
    		double height = (double) display.getHeight();
    		    		
    		double rel_x = x - width/2;
    		double rel_y = (height - y) + 2*height/5;
    		
    		double angle = Math.toDegrees(Math.atan(rel_y / rel_x));
    		
    		if (angle < 0) 
    			angle = 180 + angle;
    		
    		if (angle < 58f)
    			angle = 58f;
    		else if (angle > 122f)
    			angle = 122f;
    		
    		updateNeedlePosition(angle);
    		//updateText(Double.toString(angle));
    		
    		return true;
    	}
    };
    
    public void updateNeedlePosition(double angle) {
    	/* ImageView b = (ImageView) findViewById(R.id.fixed_needle);
    	RotateDrawable needle = (RotateDrawable) b.getDrawable(); */
    	
    	needlePositionf = (float) needlePosition;
    	float anglef = (float) angle;
    	RotateAnimation moveNeedle = new RotateAnimation(needlePositionf, 90f - anglef, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1.4f);
    	moveNeedle.setInterpolator(new LinearInterpolator());
    	moveNeedle.setDuration(1000);
    	moveNeedle.setFillAfter(true);
    	
    	needlePosition = 90 - angle;
    	
    	final ImageView fixed_needle = (ImageView) findViewById(R.id.fixed_needle);
    	fixed_needle.startAnimation(moveNeedle);
    	
    	wobble = new RotateAnimation(needlePositionf - 1f, needlePositionf + 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1.4f);
    	wobble.setInterpolator(new LinearInterpolator());
    	wobble.setRepeatCount(Animation.INFINITE);
    	wobble.setRepeatMode(Animation.REVERSE);
    	wobble.setDuration(50);
    	
    	fixed_needle.startAnimation(wobble);
    }
}