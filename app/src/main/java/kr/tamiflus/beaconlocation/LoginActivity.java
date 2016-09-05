package kr.tamiflus.beaconlocation;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

/**
 * Created by juwoong on 16. 1. 24..
 */
public class LoginActivity extends Activity {

    EditText id, pwd, age;
    RadioButton male, female;
    Button submit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        id = (EditText) findViewById(R.id.idTextView);
        pwd = (EditText) findViewById(R.id.pwdTextView);
        age = (EditText) findViewById(R.id.age);

        male = (RadioButton) findViewById(R.id.male);
        female = (RadioButton) findViewById(R.id.female);

        submit = (Button) findViewById(R.id.submit_area);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendLoginInfoTask  task = new SendLoginInfoTask();
                try {
                    String token = task.execute(id.getText().toString(), Encrypter.SHA256(pwd.getText().toString()),
                            Boolean.toString(male.isChecked()), age.getText().toString()).get();

                    if(token == null) {
                        Toast.makeText(getApplicationContext(), "서버 오류가 발생했습니다.", Toast.LENGTH_LONG);
                    }else {
                        SharedPreferences prefs = getSharedPreferences("beaconSetting", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();

                        editor.putBoolean("isLogin", true);
                        editor.putString("token", token);
                        editor.commit();

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    //finish();
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });

        
    }

    private class SendLoginInfoTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String token;
            // params comes from the execute() call: params[0] is the url.
            try {
                token = InformationSender.signup(params[0], params[1], params[2], params[3]);


            }catch(Exception e) {
                e.printStackTrace();
                return null;
            }
            return token;
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

        }
    }
}
