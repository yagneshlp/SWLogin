package com.yagneshlp.swlogin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Yagnesh L P on 05-09-2017.
 */

public class CredActivity extends Activity {

    EditText roll,pass,user;
    Button button;
    private SQLiteHandler db;
    CheckBox cb;
    TextInputLayout til;
    Switch sw;
    TextView tv;
    boolean isFac=false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cred);
        roll = (EditText) findViewById(R.id.RollNo);
        pass = (EditText) findViewById(R.id.Pass);
        button = (Button) findViewById(R.id.buttonsave);
        cb = (CheckBox) findViewById(R.id.checkBox);
        til = (TextInputLayout) findViewById(R.id.input_layout_user);
        user = (EditText) findViewById(R.id.uname);
        sw = (Switch) findViewById(R.id.switch1);
        tv = (TextView) findViewById(R.id.RollNotv);

        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    roll.setVisibility(View.GONE);
                    tv.setVisibility(View.GONE);
                    til.setVisibility(View.VISIBLE);
                    isFac=true;

                }
                else
                {
                    roll.setVisibility(View.VISIBLE);
                    tv.setVisibility(View.VISIBLE);
                    til.setVisibility(View.GONE);
                    isFac=false;
                }
            }
        });

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
                if(!isFac) {
                    if (roll.getText().toString().length() == 9) {
                        if (pass.getText().toString().length() != 0) {
                            db.deleteUsers();
                            db.addUser(roll.getText().toString(), pass.getText().toString());
                            Intent intent = new Intent(CredActivity.this, ConnectionActivity.class);
                            startActivity(intent);
                            finish();
                        } else
                            Toast.makeText(getBaseContext(), "Please fill the Password", Toast.LENGTH_LONG).show();
                    } else
                        Toast.makeText(getBaseContext(), "Check your Roll number", Toast.LENGTH_LONG).show();
                }
                else
                {
                    if(user.getText().toString().length() != 0)
                    {
                        if (pass.getText().toString().length() != 0) {
                            db.deleteUsers();
                            db.addUser(user.getText().toString(), pass.getText().toString());
                            Intent intent = new Intent(CredActivity.this, ConnectionActivity.class);
                            startActivity(intent);
                            finish();
                        } else
                            Toast.makeText(getBaseContext(), "Please fill the Password", Toast.LENGTH_LONG).show();
                    }
                    else
                        Toast.makeText(getBaseContext(), "Check your Roll number", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public void help(View v)
    {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.dialog_layout, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CredActivity.this);
        alertDialogBuilder.setTitle("")
                .setView(alertLayout);
        alertDialogBuilder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }



}
