package com.plateconstant.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.plateconstant.android.R;

public class Login extends Activity implements OnClickListener{
	
	EditText lName, pass, cPass;
	TextView loginErrorMsg;
	String name = "", password="", cPassword="";
	Button go;
	SqliteHandler db; 
	AlertDialogManager alert = new AlertDialogManager();
	
	@Override
    public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		db = new SqliteHandler(getApplicationContext());
		if(db.isUserLoggedIn(getApplicationContext())){
			Intent dashboard = new Intent(getApplicationContext(), LocationActivity.class);
			dashboard.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        	startActivity(dashboard);
        	// Closing dashboard screen
        	finish();
		}
		else{
	        setContentView(R.layout.login);
	        alert.showAlertDialog(Login.this, "Location",
	                "Please enter your gmail address and password associated with THIS Android Phone(Needed to send email automatically!)", true);
			lName = (EditText) findViewById(R.id.enter_id);
	        pass = (EditText) findViewById(R.id.enter_pass);
	        cPass = (EditText) findViewById(R.id.enter_pass_again);
	        loginErrorMsg = (TextView) findViewById(R.id.login_error);
	        go=(Button) findViewById(R.id.go);
	        go.setOnClickListener(this);
		}
    }
	@Override
	public void onClick(View v)
	{
		switch(v.getId())
		{
		case R.id.go:
			name = lName.getText().toString();
            password = pass.getText().toString();
            cPassword = cPass.getText().toString();
            
            // Check if user filled the form
            if(name.trim().length() > 0 && password.trim().length() > 0 && cPassword.trim().length() > 0){
            	if(password.equals(cPassword)){
            		SqliteHandler sql = new SqliteHandler(getApplicationContext());
            		sql.resetTables();
            		sql.addUser(name, password);
            		Intent i = new Intent(getApplicationContext(), LocationActivity.class);
            		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    // Close Login Screen
					finish();
            	}
            	else{
            		loginErrorMsg.setText("Please put same passwords");
            	}
            }
			
			break;
		}
	}	
}
