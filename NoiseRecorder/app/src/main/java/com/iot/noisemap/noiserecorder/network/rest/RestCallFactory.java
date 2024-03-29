package com.iot.noisemap.noiserecorder.network.rest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.iot.noisemap.noiserecorder.LoginActivity;
import com.iot.noisemap.noiserecorder.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains builders for the HTTP requests.
 */
public final class RestCallFactory {

    /**
     * Creates a new get request.
     * @param url URL.
     * @param accessToken Access token.
     * @param caller Caller.
     * @return The built request.
     */
    public static StringRequest createGetRequest(final String url, final String accessToken, final OnRequestResponseCallback caller) {
        return new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject resp = new JSONObject();
                        try {
                            if(!response.equals("[]")) {
                                resp = new JSONObject(response);
                            }
                        } catch (JSONException e) {
                            Log.e("createGetRequest", "" + e.getStackTrace());
                        }
                        caller.onRequestResponseCallback(resp);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                JSONObject obj;
                String errorMsg = "unbekannter Fehler";
                try {
                    if(error != null && error.networkResponse != null) {
                        String msg = new String(error.networkResponse.data, "UTF-8");
                        obj = new JSONObject(msg);
                        errorMsg = obj.getString("error_description");
                    }
                } catch (UnsupportedEncodingException | JSONException e) {
                    Log.e("createGetRequest", "" + e.getStackTrace());
                }
                caller.onRequestResponseCallback(new JSONObject());
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + accessToken);
                return headers;
            }
        };
    }

    /**
     * Creates a new post request.
     * @param url URL.
     * @param accessToken Access token.
     * @param jsonBody Body of the post request..
     * @return The built request.
     */
    public static JsonObjectRequest createPostSampleRequest(JSONObject jsonBody, String url, final String accessToken) {
        return new JsonObjectRequest(url, jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {}
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    if(error != null && error.networkResponse != null) {
                        String msg = new String(error.networkResponse.data, "UTF-8");
                    }
                } catch (UnsupportedEncodingException e) {
                    Log.e("createPostSampleRequest", "onErrorResponse" + e.getStackTrace());
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken);
                return headers;
            }
            @Override
            public String getBodyContentType()
            {
                return "application/json; charset=utf-8";
            }
        };
    }


    /**
     * Creates a new api token request.
     * @param username Username.
     * @param password Password.
     * @param url URL.
     * @param activity Calling activity.
     * @return The built request.
     */
    public static StringRequest createApiTokenRequest(final String username, final String password, String url, final LoginActivity activity) {
        return new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject resp = new JSONObject(response);
                            final String accessToken = resp.getString("access_token");
                            Bundle b = new Bundle();
                            b.putString("accessToken", accessToken);
                            b.putString("username", username);
                            b.putString("password", password);

                            // save user data
                            SharedPreferences settings = activity.getSharedPreferences("UserData", 0);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putString("Username",username);
                            editor.putString("Password",password);
                            editor.commit();

                            Intent intent = new Intent(activity, MainActivity.class);
                            intent.putExtras(b);
                            activity.startActivity(intent);
                        } catch (JSONException e) {
                            Log.e(this.getClass().getName(), e.getStackTrace().toString());
                        }
                        activity.showProgress(false);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String msg;
                JSONObject obj;
                String errorMsg = "unknown error";
                if (error instanceof NetworkError) {
                    Toast.makeText(activity,
                            "Network error.",
                            Toast.LENGTH_LONG).show();
                } else if (error instanceof ServerError) {
                    Toast.makeText(activity,
                            "Server responded with an error.",
                            Toast.LENGTH_LONG).show();
                } else if (error instanceof AuthFailureError) {
                    Toast.makeText(activity,
                            "Authentication failed.",
                            Toast.LENGTH_LONG).show();
                } else if (error instanceof ParseError) {
                    Toast.makeText(activity,
                            "Servers response could not be parsed.",
                            Toast.LENGTH_LONG).show();
                } else if (error instanceof NoConnectionError) {
                    Toast.makeText(activity,
                            "Connection could not be established.",
                            Toast.LENGTH_LONG).show();
                } else if (error instanceof TimeoutError) {
                    Toast.makeText(activity,
                            "Timeout error. Please try again.",
                            Toast.LENGTH_LONG).show();
                } else {
                    try {
                        if(error != null && error.networkResponse != null && error.networkResponse.data != null) {
                            msg = new String(error.networkResponse.data, "UTF-8");
                            obj = new JSONObject(msg);
                            if(obj.has("error_description")) {
                                errorMsg = obj.getString("error_description");
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
                }
                activity.showProgress(false);
                Toast.makeText(activity,
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
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                return headers;
            }
        };
    }

    /**
     * Creates a new api token request.
     * @param username Username.
     * @param password Password.
     * @param confirmPassword Password.
     * @param url URL.
     * @param activity Calling activity.
     * @return The built request.
     * @throws InvalidParameterException Parameters are invalid (null).
     */
    public static JsonObjectRequest createRegisterUserRequest(final String username, final String password,
                                                              final String confirmPassword,final String url,
                                                              final LoginActivity activity)
                                                        throws InvalidParameterException {

        if(username == null) throw new InvalidParameterException("username");
        if(password == null) throw new InvalidParameterException("password");
        if(confirmPassword == null) throw new InvalidParameterException("confirmPassword");

        JSONObject jsonBody = new JSONObject();
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
                        activity.showProgress(false);
                        Toast.makeText(activity,
                                "Success",
                                Toast.LENGTH_LONG).show();
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
                        Toast.makeText(activity,
                                "Network error.",
                                Toast.LENGTH_LONG).show();
                    } else if (error instanceof ServerError) {
                        Toast.makeText(activity,
                                "Server responded with an error.",
                                Toast.LENGTH_LONG).show();
                    } else if (error instanceof AuthFailureError) {
                        Toast.makeText(activity,
                                "Authentication failed.",
                                Toast.LENGTH_LONG).show();
                    } else if (error instanceof ParseError) {
                        Toast.makeText(activity,
                                "Servers response could not be parsed.",
                                Toast.LENGTH_LONG).show();
                    } else if (error instanceof NoConnectionError) {
                        Toast.makeText(activity,
                                "Connection could not be established.",
                                Toast.LENGTH_LONG).show();
                    } else if (error instanceof TimeoutError) {
                        Toast.makeText(activity,
                                "Timeout error. Please try again.",
                                Toast.LENGTH_LONG).show();
                    }
                }
                activity.showProgress(false);
                Toast.makeText(activity,
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

}
