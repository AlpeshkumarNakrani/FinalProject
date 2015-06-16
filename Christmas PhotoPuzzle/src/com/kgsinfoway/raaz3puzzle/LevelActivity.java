package com.kgsinfoway.raaz3puzzle;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class LevelActivity extends Activity implements OnClickListener{

	Button easy,medium,hard,very_hard;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.level);

		easy=(Button)findViewById(R.id.easy);
		medium=(Button)findViewById(R.id.medium);
		hard=(Button)findViewById(R.id.hard);
		very_hard=(Button)findViewById(R.id.very_hard);
		
		easy.setOnClickListener(this);
		medium.setOnClickListener(this);
		hard.setOnClickListener(this);
		very_hard.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
	
		Intent intent=new Intent(LevelActivity.this, PuzzleActivity.class);
		
		if(easy.equals(v))	{
			intent.putExtra("grid_size", 3);
		}else if(medium.equals(v))	{
			intent.putExtra("grid_size", 4);
		}else if(hard.equals(v))	{
			intent.putExtra("grid_size", 5);
		}else if(very_hard.equals(v))	{
			intent.putExtra("grid_size", 6);
		}
		startActivity(intent);
	}
}
