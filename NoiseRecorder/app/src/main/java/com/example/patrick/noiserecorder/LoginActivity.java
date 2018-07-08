package com.example.patrick.noiserecorder;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.patrick.noiserecorder.network.rest.RestCallFactory;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A login screen that offers login via username/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {
    private final static int ACCESS_LOCATION_AND_RECORD_AUDIO_REQUEST_CODE = 1;
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask authTask = null;
    private RegisterUserTask registerUserTask = null;
    // UI references.
    private AutoCompleteTextView usernameView;
    private EditText passwordView;
    private EditText confirmPasswordView;
    private View progressView;
    private View loginFormView;

    private RequestQueue requestQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        requestQueue = Volley.newRequestQueue(this);
        ActivityCompat.requestPermissions(LoginActivity.this,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                                         Manifest.permission.RECORD_AUDIO},
                            ACCESS_LOCATION_AND_RECORD_AUDIO_REQUEST_CODE);

        // Set up the login form.
        usernameView = (AutoCompleteTextView) findViewById(R.id.email);

        passwordView = (EditText) findViewById(R.id.password);
        confirmPasswordView = (EditText) findViewById(R.id.confirmPassword);
        passwordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        // load user data
        loadUserData();

        final Button btnSignIn = (Button) findViewById(R.id.btn_SignIn);
        final Button btnRegister = (Button) findViewById(R.id.btn_register);
        btnSignIn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                btnSignIn.setEnabled(false);
                btnRegister.setEnabled(false);
                attemptLogin();
            }
        });
        btnRegister.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                btnSignIn.setEnabled(false);
                btnRegister.setEnabled(false);

                attemptRegister();
            }
        });

        loginFormView = findViewById(R.id.login_form);
        progressView = findViewById(R.id.login_progress);
    }

    private void loadUserData() {
        SharedPreferences settings = getSharedPreferences("UserData", 0);
        usernameView.setText(settings.getString("Username", "").toString());
        passwordView.setText(settings.getString("Password", "").toString());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case ACCESS_LOCATION_AND_RECORD_AUDIO_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                   // permission granted
                } else {
                    Toast.makeText(this,"App needs those permissions to work!",Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }
    }
    private void attemptRegister() {
        // TODO
        // http://noisemaprestapi.azurewebsites.net/api/Account/Register
        //{
        //	"Username": "<xyz>",
        //	"Password": "<pw>",
        //	"ConfirmPassword": "<pw>"
        //}
        View focusView = null;
        boolean cancel = false;
        final Button btnSignIn = (Button) findViewById(R.id.btn_SignIn);
        final Button btnRegister = (Button) findViewById(R.id.btn_register);
        btnSignIn.setEnabled(true);
        btnRegister.setEnabled(true);

        // Store values at the time of the login attempt.
        String email = usernameView.getText().toString();
        String password = passwordView.getText().toString();
        String confirmPassword = confirmPasswordView.getText().toString();

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            passwordView.setError(getString(R.string.error_invalid_password));
            focusView = passwordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            usernameView.setError(getString(R.string.error_field_required));
            focusView = usernameView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            usernameView.setError(getString(R.string.error_invalid_email));
            focusView = usernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            btnSignIn.setEnabled(true);
            btnRegister.setEnabled(true);
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            registerUserTask = new RegisterUserTask(email, password, confirmPassword);
            registerUserTask.execute((Void) null);
        }
        // TODO call attemptLogin() on success ?
    }
    /**
     * Attempts to sign in the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        final Button btnSignIn = (Button) findViewById(R.id.btn_SignIn);
        final Button btnRegister = (Button) findViewById(R.id.btn_register);
        if (authTask != null) {
            btnSignIn.setEnabled(true);
            btnRegister.setEnabled(true);
            return;
        }

        // Reset errors.
        usernameView.setError(null);
        passwordView.setError(null);

        // Store values at the time of the login attempt.
        String email = usernameView.getText().toString();
        String password = passwordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            passwordView.setError(getString(R.string.error_invalid_password));
            focusView = passwordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            usernameView.setError(getString(R.string.error_field_required));
            focusView = usernameView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            usernameView.setError(getString(R.string.error_invalid_email));
            focusView = usernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            btnSignIn.setEnabled(true);
            btnRegister.setEnabled(true);
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            authTask = new UserLoginTask(email, password);
            authTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO
        return true;
        //return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            loginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        usernameView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }
    private void enableButtons() {
        final Button btnSignIn = (Button) findViewById(R.id.btn_SignIn);
        final Button btnRegister = (Button) findViewById(R.id.btn_register);
        btnSignIn.setEnabled(true);
        btnRegister.setEnabled(true);
    }
    /**
     * Represents an asynchronous login task used to authenticate the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
        private static final String TAG = "UserLoginTask";
        private final String TOKEN_URL = Config.HOST_BASE_URL + "token";
        private final String email;
        private final String password;

        UserLoginTask(String email, String password) {
            this.email = email;
            this.password = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            final String userName = usernameView.getText().toString();
            final String password = passwordView.getText().toString();

            StringRequest tokenRequest = RestCallFactory.createApiTokenRequest(userName,password,TOKEN_URL,LoginActivity.this);
            // increase accepted timeout duration, because the azure web api seems to go into a
            // standby-ish mode when it gets no request for some time
            int acceptedTimeoutMs = 15000;
            tokenRequest.setRetryPolicy(new DefaultRetryPolicy(
                    acceptedTimeoutMs,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            requestQueue.add(tokenRequest);

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            authTask = null;

            if (success) {
               // finish();
            } else {
                passwordView.setError(getString(R.string.error_incorrect_password));
                passwordView.requestFocus();
            }
            enableButtons();
        }

        @Override
        protected void onCancelled() {
            authTask = null;
            showProgress(false);
            enableButtons();
        }
    }
    /**
     * Represents an asynchronous registration task used to register the user.
     */
    public class RegisterUserTask extends AsyncTask<Void, Void, Boolean> {
        private static final String TAG = "UserRegisterTask";
        private final String TOKEN_URL = "http://noisemaprestapi.azurewebsites.net/api/Account/Register";
        private final String username;
        private final String password;
        private final String confirmPassword;

        RegisterUserTask(String email, String password, String confirmPassword) {
            this.username = email;
            this.password = password;
            this.confirmPassword = confirmPassword;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            Request<JSONObject> registerUserRequest = null;
            try {
                registerUserRequest = RestCallFactory.createRegisterUserRequest(username, password, confirmPassword, TOKEN_URL, LoginActivity.this);
            } catch (IllegalAccessException e) {
                e.printStackTrace(); // username, password or confirmationPassword == null
            }
            // increase accepted timeout duration, because the azure web api seems to go into a
            // standby-ish mode when it gets no request for some time
            int acceptedTimeoutMs = 15000;
            registerUserRequest.setRetryPolicy(new DefaultRetryPolicy(
                    acceptedTimeoutMs,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            requestQueue.add(registerUserRequest);

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            authTask = null;

            if (success) {
                // finish();
            } else {
                passwordView.setError(getString(R.string.error_incorrect_password));
                passwordView.requestFocus();
            }
            enableButtons();
        }

        @Override
        protected void onCancelled() {
            authTask = null;
            showProgress(false);
            enableButtons();
        }
    }
}

