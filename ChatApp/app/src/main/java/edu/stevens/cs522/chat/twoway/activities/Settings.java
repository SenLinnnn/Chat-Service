package edu.stevens.cs522.chat.twoway.activities;

/**
 * Created by DV6 on 3/1/2016.
 */
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import edu.stevens.cs522.chat.twoway.R;

public class Settings extends Activity implements View.OnClickListener {


    public static final String MY_PREFS = "mySharedPreferences";
    public static final String CLIENT_NAME = "clientname";

    private Button savePrefBtn;
    private EditText clientNameET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        clientNameET = (EditText)findViewById(R.id.client_name_edit_text);
        savePrefBtn = (Button)findViewById(R.id.save_peference_button);
        savePrefBtn.setOnClickListener(this);

        //clientNameET.setText(loadPreferences());
    }

    public void onClick(View view) {

        String clientName = clientNameET.getText().toString();
        savePreferences(clientName);

        String name = loadPreferences();
        Intent prefIntent = new Intent();
        if(name == null){
            Toast.makeText(this, "Name cannot be NULL", Toast.LENGTH_SHORT).show();
            setResult(Activity.RESULT_CANCELED, prefIntent);
        }else{
            prefIntent.putExtra(constants.saveClientNamePreferences, name);
            setResult(Activity.RESULT_OK, prefIntent);
            Toast.makeText(this, "Name Preferences Set", Toast.LENGTH_SHORT).show();
        }
        finish();
    }


    protected void savePreferences(String clientName) {

        // Create or retrieve the shared preference object.
        int mode = Activity.MODE_PRIVATE;
        SharedPreferences mySharedPreferences =  getSharedPreferences(MY_PREFS, mode);

        // Retrieve an editor to modify the shared preferences.
        SharedPreferences.Editor editor = mySharedPreferences.edit();

        //Store new primitive types in the shared preferences object.
        editor.putString(CLIENT_NAME, clientName);

        // Commit the changes.
        editor.commit();
    }

    public String loadPreferences(){
        //Restore Preferences
        int mode = Activity.MODE_PRIVATE;
        SharedPreferences mySharedPreferences = getSharedPreferences(MY_PREFS, mode);

        String clientNamePref = mySharedPreferences.getString(CLIENT_NAME, null);

        return clientNamePref;
    }
}

