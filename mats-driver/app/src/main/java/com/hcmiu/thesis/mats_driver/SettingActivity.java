package com.hcmiu.thesis.mats_driver;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hcmiu.thesis.mats_driver.DataAccess.DriverDataAccess;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by edunetjsc on 12/6/16.
 */

public class SettingActivity extends AppCompatActivity {

    String id = "";
    String username = "";
    String email = "";
    String phone = "";
    String password = "";
    String car_type = "";

    EditText email_edit;
    EditText name_edit;
    EditText phone_edit;
    EditText password_edit;
    EditText cartype_edit;
    Button save_account_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_info);

        email_edit = (EditText) findViewById(R.id.email_edit);
        name_edit = (EditText) findViewById(R.id.name_edit);
        phone_edit = (EditText) findViewById(R.id.phone_edit);
        password_edit = (EditText) findViewById(R.id.password_edit);
        cartype_edit = (EditText) findViewById(R.id.cartype_edit);
        save_account_button = (Button) findViewById(R.id.save_account_button);

        SharedPreferences sp = SettingActivity.this.getSharedPreferences("loginSaved", Context.MODE_PRIVATE);
        String save_username = sp.getString("email", null);
        String save_password = sp.getString("password", null);
        if(save_username != null || save_password != null || save_username != "" || save_password != ""){
            new MyTask(save_username,save_password).execute();
        }

        save_account_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email_get = email_edit.getText().toString();
                String phone_get = phone_edit.getText().toString();
                String name_get = name_edit.getText().toString();
                String password_get = password_edit.getText().toString();
                String cartype_get = cartype_edit.getText().toString();
                new MyTask2(name_get,email_get,phone_get,password_get,cartype_get).execute();
            }
        });
    }

    private class MyTask extends AsyncTask<Object, Object, Boolean> {

        private String username2;
        private String password2;

        public MyTask(String username, String password){
            this.username2 = username;
            this.password2 = password;
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            boolean key = false;
            try {
                DriverDataAccess pass = new DriverDataAccess();
                final ResultSet result = pass.select();
                while (result.next()) {
                    if (username2.equals(result.getString("email")) && password2.equals(result.getString("password"))) {
                        id = result.getString("id");
                        email = result.getString("email");
                        username = result.getString("name");
                        phone = result.getString("phone");
                        password = result.getString("password");
                        car_type = result.getString("car_type");
                        key = true;
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return key;
        }

        protected void onPostExecute(Boolean result) {
            Log.i("abcdef",email);
            if (result) {

                email_edit.setText(email);
                name_edit.setText(username);
                phone_edit.setText(phone);
                password_edit.setText(password);
                cartype_edit.setText(car_type);
            }
            else {

            }
            super.onPostExecute(result);
        }

    }

    private class MyTask2 extends AsyncTask<Object, Object, Boolean> {

        private String name3;
        private String email3;
        private String password3;
        private String phone3;
        private String car_type3;
        private String message = "Save Successfully";

        public MyTask2(String name, String email, String phone, String password,String car_type3){
            this.name3 = name;
            this.email3 = email;
            this.phone3 = phone;
            this.password3 = password;
            this.car_type3 = car_type3;
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            try {
                DriverDataAccess pass = new DriverDataAccess();
                pass.update(id,name3,email3,phone3,password3,car_type3);
                return true;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return false;
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(getBaseContext(), "Save has problem!", Toast.LENGTH_LONG).show();
            }
            super.onPostExecute(result);
        }

    }
}
