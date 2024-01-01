package com.example.restapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {
    private EditText edtData;

    // ______________________________________________________________________________________________________
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edtData = findViewById(R.id.edtData);
    }
    // ______________________________________________________________________________________________________
    public void onClickbtn(View view) {
        EditText edtCat = findViewById(R.id.edtCat);
        String url = "http://10.0.2.2:8080/Library/info.php?cat=" + edtCat.getText();   // Prepare the URL

        if (ContextCompat.checkSelfPermission(this,     // Check if permission to internet is given or not
                Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET}, 123);
        }
        else{
            Thread thread = new Thread(new MyTask(url));    // Send the request on another thread
            thread.start(); // Turn on the thread

//            DownloadTextTask runner = new DownloadTextTask();
//            runner.execute(url);    // Called doInBackground
        }
    }
    // ______________________________________________________________________________________________________
    // Get the URL & return InputStream to use it in reading
    private InputStream OpenHttpConnection(String urlString) throws IOException {
        InputStream in = null;
        int response = -1;

        URL url = new URL(urlString);   // Define URL Object
        URLConnection conn = url.openConnection();  // Open the connection

        if (!(conn instanceof HttpURLConnection))   // Ensure that the connection is true
            throw new IOException("Not an HTTP connection");
        try{
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET"); // Request of type GET
            httpConn.connect(); // Make connection

            response = httpConn.getResponseCode();  // Prepare the response

            if (response == HttpURLConnection.HTTP_OK)  // Ensure that we get response from the server
                in = httpConn.getInputStream(); // Get the InputStream value
        }
        catch (Exception ex) {
            Log.d("Networking", ex.getLocalizedMessage());
            throw new IOException("Error connecting");
        }
        return in;  // Return the InputStream
    }
    // ______________________________________________________________________________________________________
    private String DownloadText(String URL) {       // Send the URL(Get request) as a parameter
        int BUFFER_SIZE = 2000; // Buffer to read the data that will return
        InputStream in = null;

        try {
            in = OpenHttpConnection(URL);   // Get the inputStream to use it in reading
        }
        catch (IOException e) {
            Log.d("Networking", e.getLocalizedMessage());
            return "";
        }

        InputStreamReader isr = new InputStreamReader(in);  // Prepare InputStreamReader to read from the InputStream
        int charRead;
        String str = "";
        char[] inputBuffer = new char[BUFFER_SIZE];

        try {   // Read char by char and then return the result as a single String
            while ((charRead = isr.read(inputBuffer)) > 0) {
                // Convert the chars to a String
                String readString = String.copyValueOf(inputBuffer, 0, charRead);
                str += readString;
                inputBuffer = new char[BUFFER_SIZE];
            }
            in.close();     // Close the connection
        }
        catch (IOException e) {
            Log.d("Networking", e.getLocalizedMessage());
            return "";
        }
        return str; // str == books that has been returned
    }
    // ______________________________________________________________________________________________________

    public void onClickOpenPage(View view) {
        Intent intent = new Intent(this, SecondActivity.class);
        startActivity(intent);
    }
    // ######################################################################################################
    private class MyTask implements Runnable{
        private String url;
        // ______________________________________________________________________________________________________
        public MyTask(String url){  // Default Constructor
            this.url = url;
        }
        // ______________________________________________________________________________________________________
        @Override
        public void run() {
            final String result = DownloadText(url);

            edtData.post(new Runnable() {
                @Override
                public void run() {
                    edtData.setText(result);
                }
            });
        }
    }
    // ##########################################################################################################

    private class DownloadTextTask extends AsyncTask<String, Void, String>{ // Input String, No progress bar, output String
        @Override
        protected String doInBackground(String... urls) {   // This method will take long time, so it will execute on another thread
            return DownloadText(urls[0]);   // Send the URL(Get request) as a parameter
        }

        // This method will be called automatically after doInBackground finished(will take it's output as a parameter)
        @Override
        protected void onPostExecute(String result){    // Will be executed on the main thread
            EditText edtData = findViewById(R.id.edtData);
            edtData.setText(result);
        }
    }
}