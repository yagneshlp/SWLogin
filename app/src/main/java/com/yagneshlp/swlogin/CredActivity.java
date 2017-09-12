package com.yagneshlp.swlogin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;

/**
 * Created by Yagnesh L P on 05-09-2017.
 */

public class CredActivity extends Activity {

    EditText roll,pass;
    Button button;
    private SQLiteHandler db;
    CheckBox cb;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cred);
        roll = (EditText) findViewById(R.id.RollNo);
        pass = (EditText) findViewById(R.id.Pass);
        button = (Button) findViewById(R.id.buttonsave);
        cb = (CheckBox) findViewById(R.id.checkBox);

        db = new SQLiteHandler(getApplicationContext());

        String dbroll,dbpass;
        dbroll=db.getRoll();
        dbpass=db.getPass();
        if(!dbroll.equals("null")) {
            roll.setText(dbroll);
            pass.setText(dbpass);
        }

        cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cb.isChecked())
                {
                    pass.setTransformationMethod(null);
                    pass.setSelection(pass.getText().length());
                }
                else
                {
                    pass.setTransformationMethod(new PasswordTransformationMethod());
                    pass.setSelection(pass.getText().length());
                }
            }
        });


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(roll.getText().toString().length() == 9){
                    if(pass.getText().toString().length()!=0 ) {
                        db.deleteUsers();
                        db.addUser(roll.getText().toString(), pass.getText().toString());
                        Intent intent = new Intent(CredActivity.this, ConnectionActivity.class);
                        startActivity(intent);
                        finish();
                }
                else
                    Toast.makeText(getBaseContext(),"Please fill the Password",Toast.LENGTH_LONG).show();
                }
                else
                    Toast.makeText(getBaseContext(),"Check your Roll number",Toast.LENGTH_LONG).show();
            }
        });

    }


}
