package com.cowwwsay;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.cowwwsay.R;
import com.cowwwsay.R.id;
import com.cowwwsay.R.layout;
import com.cowwwsay.R.menu;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private Button getCow_;
	private EditText sayText_;
	private TextView cowSay_;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		sayText_ = (EditText) findViewById(R.id.editText_sayText);
		getCow_ = (Button) findViewById(R.id.button_getCow);
		getCow_.setOnClickListener(getCowListener_);
		cowSay_ = (TextView) findViewById(R.id.textView_cowSay);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	private OnClickListener getCowListener_ = new OnClickListener() {

		@Override
		public void onClick(View v) {
			 new GetCowTask().execute("http://cowwwsay.com/say",sayText_.getText().toString());
		}
	};
	
	
	//Takes a string, converts it to a png, then saves it
	private class MakeCowTask extends AsyncTask<String, String, String>{

		
		//PARAMS: String text
		@Override
		protected String doInBackground(String... arg0) {
			
			String text = arg0[0];
			
			Paint paint = new Paint();
			//TODO: use monospaced font
			paint.setTextSize(12);
			paint.setColor(Color.WHITE);
			paint.setTextAlign(Paint.Align.LEFT);
			
			//Get bitmap dimensions
			int baseline = (int) (-paint.ascent() + .5f);
			int height = (int) (paint.descent()-paint.ascent() + .5f);
			int line_count = 0;
			int line_width = 0;
			for(String line: text.split("\n")){
				line_count++;
				if(paint.measureText(line)>line_width)
					line_width = (int)( paint.measureText(line) +.5f);
			}
			Bitmap bmp = Bitmap.createBitmap(line_width,height*line_count,Bitmap.Config.ARGB_4444);
			Canvas canvas = new Canvas(bmp);
			for(String line: text.split("\n")){
				canvas.drawText(line, 0, baseline, paint);
				baseline+= 12;
			}
			
			//Now we save this bitmap in a directory
			try {
				String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "cowsay_"+ new Random().toString() + ".png";
				FileOutputStream out = new FileOutputStream(dir);
				bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
				out.close();
			} catch (Exception e){
				e.printStackTrace();
			}
			
			return null;
		}
	    @Override
	    protected void onPostExecute(String result) {
	        super.onPostExecute(result);
	        Toast toast = Toast.makeText(getApplicationContext(), "image saved", 3);
	        toast.show();
	    }
	}
	
	
	/*	Requests a cowsay and stores it in cowSay_
		Spawns a MakeCowTask after recv'ing the cowsay.
	*/
	private class GetCowTask extends AsyncTask<String, String, String>{

		//PARAMS: String URL, String text
	    @Override
	    protected String doInBackground(String... uri) {
	        HttpClient httpclient = new DefaultHttpClient();
	        HttpResponse response;
	        String responseString = null;
	        
	        HttpPost httpPost = new HttpPost(uri[0]);

	        try {
		        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		        nameValuePairs.add(new BasicNameValuePair("say", uri[1]));//add our say text to the request
		        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	            response = httpclient.execute(httpPost);
	            StatusLine statusLine = response.getStatusLine();
	            
	            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
	                ByteArrayOutputStream out = new ByteArrayOutputStream();
	                response.getEntity().writeTo(out);
	                out.close();
	                responseString = out.toString();
	            } else{
	            	//TODO: we should fail gracefully
	                response.getEntity().getContent().close();
	                throw new IOException(statusLine.getReasonPhrase());
	            }
	        } catch (ClientProtocolException e) {
				e.printStackTrace();
	        } catch (IOException e) {
				e.printStackTrace();	            
	        }
	        return responseString;
	    }

	    @Override
	    protected void onPostExecute(String result) {
	        super.onPostExecute(result);
	        cowSay_.setText(result);
	        //starting the task to save the image
	        new MakeCowTask().execute(result);
	    }
	}

}
