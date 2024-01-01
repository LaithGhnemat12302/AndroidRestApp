package com.example.restapp;

import android.Manifest;
import android.content.pm.PackageManager;
//import android.os.AsyncTask;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class SecondActivity extends AppCompatActivity {
    private EditText edtBookTitle;
    private EditText edtBookCategory;
    private EditText edtBookPages;
    private TextView txtResult;
    // ______________________________________________________________________________________________________
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        setUpViews();
    }
    // ______________________________________________________________________________________________________
    private void setUpViews() {
        edtBookTitle =  findViewById(R.id.edtBookTitle);
        edtBookCategory = findViewById(R.id.edtBookCategory);
        edtBookPages = findViewById(R.id.edtBookPages);
        txtResult = findViewById(R.id.txtResult);
    }
    // ______________________________________________________________________________________________________
    private String processRequest(String restUrl) throws UnsupportedEncodingException {
        String title = edtBookTitle.getText().toString();
        String category = edtBookCategory.getText().toString();
        String pages = edtBookPages.getText().toString();

        // We should make encode for each data when using POST Method
        String data = URLEncoder.encode("title", "UTF-8") + "=" + URLEncoder.encode(title, "UTF-8");
        data += "&" + URLEncoder.encode("cat", "UTF-8") + "=" + URLEncoder.encode(category, "UTF-8");
        data += "&" + URLEncoder.encode("pages", "UTF-8") + "=" + URLEncoder.encode(pages, "UTF-8");

        String text = "";
        BufferedReader reader = null;

        try {   // Send data
            URL url = new URL(restUrl);     // Defined URL  where to send data

            // Send POST data request
            URLConnection conn = url.openConnection();  // Open the connection
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream()); // Prepare the OutputStreamWriter to send tha data
            wr.write(data);
            wr.flush();

            // Get the server response
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = "";

            while((line = reader.readLine()) != null)      // Read Server Response
                sb.append(line + "\n");     // Append server response in string

            text = sb.toString();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }

        finally {
            try {
                reader.close();     // Close the connection
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
        }
        // Show response on activity
        return text;   // Return the String from the web service
    }
    // ______________________________________________________________________________________________________
    public void btnAddOnClick(View view) {
        String restUrl = "http://10.0.2.2:8080/Library/addBook.php";

        if (ContextCompat.checkSelfPermission(this, // Check the permissions to internet
                Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET},
                    123);

        } else{
//            SendPostRequest runner = new SendPostRequest();
//            runner.execute(restUrl);

            Thread thread = new Thread(new MyTask2(restUrl));
            thread.start();
        }
    }
    // ______________________________________________________________________________________________________
    private class MyTask2 implements Runnable{
        private String url;
        public MyTask2(String url){
            this.url = url;
        }

        @Override
        public void run() {
            final String result;
            try {
                result = processRequest(url);
                txtResult.post(new Runnable() {
                    @Override
                    public void run() {
                        txtResult.setText(result);
                    }
                });
            }
            catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }
    // ######################################################################################################

    private class sendPostRequest extends AsyncTask<String, Void, String> {  // Input String, No progress bar, output String
        @Override
        protected String doInBackground(String... urls) {   // This method will take long time, so it will execute on another thread
            try{
                return processRequest(urls[0]); // Send the URL(Get request) as a parameter
            }
            catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }
            return "";
        }

        // This method will be called automatically after doInBackground finished(will take it's output as a parameter)
        @Override
        protected void onPostExecute(String result){    // Will be executed on the main thread
            Toast.makeText(SecondActivity.this, result, Toast.LENGTH_SHORT).show();
        }
    }
}