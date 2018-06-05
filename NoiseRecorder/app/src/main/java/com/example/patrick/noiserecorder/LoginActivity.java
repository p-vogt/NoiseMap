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
import android.util.Log;
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

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.patrick.noiserecorder.Network.RestCallFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A login screen that offers login via username/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask authTask = null;
    private RegisterUserTask registerUserTask = null;
    // UI references.
    private AutoCompleteTextView usernameView;
    private EditText passwordView;
    private EditText confirmPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private String accessToken;

    private RequestQueue requestQueue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        requestQueue = Volley.newRequestQueue(this);
        ActivityCompat.requestPermissions(LoginActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.RECORD_AUDIO}, 1); // 1 is a integer which will return the result in onRequestPermissionsResult

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
        SharedPreferences settings = getSharedPreferences("UserData", 0);
        usernameView.setText(settings.getString("Username", "").toString());
        passwordView.setText(settings.getString("Password", "").toString());

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

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                   // permission granted

                } else {
                    Toast.makeText(this,"App needs this permission to work!",Toast.LENGTH_LONG).show();
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

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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

    /**
     * Represents an asynchronous login task used to authenticate the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
        private static final String TAG = "UserLoginTask";
        private final String TOKEN_URL = "http://noisemaprestapi.azurewebsites.net/token";
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

            JsonRequest registerUserRequest = null;
            try {
                registerUserRequest = createRegisterUserRequest(username,password,confirmPassword,TOKEN_URL);
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
    private JsonObjectRequest createRegisterUserRequest(final String username, final String password, final String confirmPassword, String url) throws IllegalAccessException {

        if(username == null) throw new IllegalAccessException("username");
        if(password == null) throw new IllegalAccessException("password");
        if(confirmPassword == null) throw new IllegalAccessException("confirmPassword");

        JSONObject jsonBody = new JSONObject(); // TODO
        try {
            jsonBody.put("username", username);
            jsonBody.put("password", password);
            jsonBody.put("confirmPassword", confirmPassword);
        } catch (JSONException ex) {
            // can not happen
        }

        return new JsonObjectRequest(url, jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                            // TODO login user?
                        showProgress(false);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String msg = "";
                JSONObject obj;
                String errorMsg = "unknown error";
                boolean gotJsonResponse = false;
                try {
                    if(error != null && error.networkResponse != null && error.networkResponse.data != null) {
                        msg = new String(error.networkResponse.data, "UTF-8");
                        obj = new JSONObject(msg);
                        if(obj.has("error_description")) {
                            errorMsg = obj.getString("error_description");
                            gotJsonResponse = true;
                        } else if(obj.has("modelState")) {
                            obj = obj.getJSONObject("modelState");
                            if(obj.has("")) {
                                errorMsg = obj.getString("");
                            } else {
                                errorMsg = obj.toString();
                            }
                            gotJsonResponse = true;
                        }
                    }
                } catch (Exception e) {
                    if(e.getStackTrace() != null) {
                        Log.e(this.getClass().getName(), e.getStackTrace().toString());
                    }
                    else {
                        Log.e(this.getClass().getName(), e.getMessage());
                    }
                }
                if(!gotJsonResponse) {
                    if (error instanceof NetworkError) {
                        Toast.makeText(LoginActivity.this,
                                "Network error.",
                                Toast.LENGTH_LONG).show();
                    } else if (error instanceof ServerError) {
                        Toast.makeText(LoginActivity.this,
                                "Server responded with an error.",
                                Toast.LENGTH_LONG).show();
                    } else if (error instanceof AuthFailureError) {
                        Toast.makeText(LoginActivity.this,
                                "Authentication failed.",
                                Toast.LENGTH_LONG).show();
                    } else if (error instanceof ParseError) {
                        Toast.makeText(LoginActivity.this,
                                "Servers response could not be parsed.",
                                Toast.LENGTH_LONG).show();
                    } else if (error instanceof NoConnectionError) {
                        Toast.makeText(LoginActivity.this,
                                "Connection could not be established.",
                                Toast.LENGTH_LONG).show();
                    } else if (error instanceof TimeoutError) {
                        Toast.makeText(LoginActivity.this,
                                "Timeout error. Please try again.",
                                Toast.LENGTH_LONG).show();
                    }
                }
                showProgress(false);
                Toast.makeText(LoginActivity.this, //TODO
                        errorMsg,
                        Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("grant_type", "password");
                params.put("username", username);
                params.put("password", password);
                params.put("confirmPassword", confirmPassword);
                return params;
            }

            @Override
            public String getBodyContentType()
            {
                return "application/json; charset=utf-8";
            }
        };
    }
    private void enableButtons() {
        final Button btnSignIn = (Button) findViewById(R.id.btn_SignIn);
        final Button btnRegister = (Button) findViewById(R.id.btn_register);
        btnSignIn.setEnabled(true);
        btnRegister.setEnabled(true);
    }

}

