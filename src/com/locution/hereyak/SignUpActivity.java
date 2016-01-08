package com.locution.hereyak;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SignUpActivity extends Activity {
	
	private UserSignUpTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mUsername;
	private String mEmail;
	private String mPassword;

	// UI references.
	private EditText mUsernameView;
	private EditText mEmailView;
	private EditText mPasswordView;
	private View mSignUpFormView;
	private View mSignUpStatusView;
	private TextView mSignUpStatusMessageView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_up);
		
		// Set up the sign_up form.
		mUsernameView = (EditText) findViewById(R.id.username);
		
		mEmail = getIntent().getStringExtra("email");
		mEmailView = (EditText) findViewById(R.id.email);
		mEmailView.setText(mEmail);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							register();
							return true;
						}
						return false;
					}
				});

		mSignUpFormView = findViewById(R.id.sign_up_form);
		mSignUpStatusView = findViewById(R.id.sign_up_status);
		mSignUpStatusMessageView = (TextView) findViewById(R.id.sign_up_status_message);

		findViewById(R.id.sign_up_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						register();
					}
				});
				
		findViewById(R.id.sign_up_text).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View view) {
						returnToLogin();
					}
					
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}
	
	private void returnToLogin() {
			Constants.logMessage(1,"sign_up_text_onClick","Killing this activity to return to register");
			finish();		
	}
	
	public void register() {
		if (mAuthTask != null) {
			return;
		}

		mUsernameView.setError(null);
		mEmailView.setError(null);
		mPasswordView.setError(null);

		mUsername = mUsernameView.getText().toString();
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_sign_up_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 6) {
			mPasswordView.setError(getString(R.string.error_sign_up_short_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_sign_up_field_required));
			focusView = mEmailView;
			cancel = true;
		} else if (!mEmail.contains("@")) {
			mEmailView.setError(getString(R.string.error_sign_up_invalid_email));
			focusView = mEmailView;
			cancel = true;
		}
		
		// Check for a valid user name.
				if (TextUtils.isEmpty(mUsername)) {
					mUsernameView.setError(getString(R.string.error_sign_up_field_required));
					focusView = mUsernameView;
					cancel = true;
				} else if (mUsername.length() < 3) {
					mUsernameView.setError(getString(R.string.error_sign_up_username_too_short));
					focusView = mUsernameView;
					cancel = true;
				} else if (!mUsername.matches("[a-zA-Z0-9]+")) {
					mUsernameView.setError(getString(R.string.error_sign_up_username));
					focusView = mUsernameView;
					cancel = true;
				}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mSignUpStatusMessageView.setText(R.string.sign_up_progress_signing_in);
			showProgress(true);
			mAuthTask = new UserSignUpTask();
			mAuthTask.execute((Void) null);
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mSignUpStatusView.setVisibility(View.VISIBLE);
			mSignUpStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mSignUpStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mSignUpFormView.setVisibility(View.VISIBLE);
			mSignUpFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mSignUpFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mSignUpStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mSignUpFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	   super.onActivityResult(requestCode, resultCode, data);
	   if (resultCode == RESULT_OK && requestCode == 1) {
	      setResult(RESULT_OK);
	      finish();
	   }
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserSignUpTask extends AsyncTask<Void, Void, Map<String,Object>> {
		@Override
		protected Map<String,Object> doInBackground(Void... params) {
	
			try {
				YakHttpClient yakClient = new YakHttpClient("sign_up");
				Map<String,Object> ret = new HashMap<String, Object>();
				ret = yakClient.sign_up(mUsername, mEmail, mPassword);

				//Constants.logMessage(1,"UserSignUpTask", ret.toString());
				return ret;
				
			} catch (MalformedURLException e) {
				Constants.logMessage(2,"UserSignUpTask",e.getMessage());
			} catch (IOException e) {
				Constants.logMessage(2,"UserSignUpTask",e.getMessage());
			} catch (Exception e) {
				Constants.logMessage(2,"UserSignUpTask",e.getMessage());			
			}
			return null;
		}

		@Override
		protected void onPostExecute(final Map<String,Object> ret) {
			mAuthTask = null;
			showProgress(false);
			
			Boolean success = false;
			
			if ((ret != null) && Integer.parseInt(((String)ret.get("status"))) == 0) {
				success = true;
			}

			if (success) {
				Constants.logMessage(1,"onPostExecute","Success!");
				Intent i = new Intent(getApplicationContext(), MainActivity.class);
				JSONObject json = (JSONObject)(ret.get("data"));
				try {
					i.putExtra("username", json.getString("username"));
					i.putExtra("access_token", json.getString("access_token"));
					i.putExtra("device_key", json.getString("device_key"));
					startActivityForResult(i, 1);
					setResult(RESULT_OK);
					finish();
				} catch (JSONException e) {
					Context context = getApplicationContext();
					CharSequence text = getString(R.string.error_server);
					int duration = Toast.LENGTH_SHORT;

					Toast toast = Toast.makeText(context, text, duration);
					toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
					toast.show();
				}
			} else {
				
				if (ret != null) {
					if (Integer.parseInt((String)ret.get("status")) == 1){	
						Constants.logMessage(1,"onPostExecute", "Received status 1 back from server");
						
						JSONObject json = (JSONObject) ret.get("data");
						try {
							if (json.has("password")) {
								mPasswordView.setError(getString(R.string.error_invalid_password));
								mPasswordView.requestFocus();
							}
							if (json.has("email")) {
								if (json.getString("email").contains("taken")) {
									mEmailView.setError(getString(R.string.error_sign_up_email_taken));
								} else {
									mEmailView.setError(getString(R.string.error_sign_up_invalid_email));
								}
								mEmailView.requestFocus();
							}
	
							if (json.has("username")) {
								if (json.getString("username").contains("taken")) {
									mUsernameView.setError(getString(R.string.error_sign_up_username_taken));
								} else {
									mUsernameView.setError(getString(R.string.error_sign_up_invalid_username));
								}
								mUsernameView.requestFocus();
							}
						} catch (JSONException e) {
							Constants.logMessage(1,"onPostExecute", "Exeption encountered: "+e.toString());
							
							Context context = getApplicationContext();
							CharSequence text = getString(R.string.error_server);
							int duration = Toast.LENGTH_SHORT;
	
							Toast toast = Toast.makeText(context, text, duration);
							toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
							toast.show();
						}
					}
					
				} else {
					Constants.logMessage(1,"onPostExecute", "Received status 2 back from server or connection error ");
					Context context = getApplicationContext();
					CharSequence text = getString(R.string.error_server);
					int duration = Toast.LENGTH_SHORT;

					Toast toast = Toast.makeText(context, text, duration);
					toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
					toast.show();
				}
				
				
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}

}
