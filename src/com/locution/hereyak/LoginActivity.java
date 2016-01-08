package com.locution.hereyak;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.GooglePlayServicesUtil;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {


	private UserLoginTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mEmail;
	private String mPassword;

	// UI references.
	private EditText mEmailView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SharedPreferences prefs = getSharedPreferences(Constants.PREFS_FILE_NAME, MODE_PRIVATE);
		if (prefs.getString("username", null) != null && prefs.getString("access_token", null) != null &&
				prefs.getString("device_key", null) != null){
			
			Intent i = new Intent(getApplicationContext(), MainActivity.class);
			i.putExtra("username", ObscureData.unObscureIt(this, prefs.getString("username", null)));
			i.putExtra("access_token", ObscureData.unObscureIt(this, prefs.getString("access_token", null)));
			i.putExtra("device_key", ObscureData.unObscureIt(this, prefs.getString("device_key", null)));
			startActivityForResult(i, 1);
			setResult(RESULT_OK);
			finish();
		}

		setContentView(R.layout.activity_login);

		// Set up the login form.
		//mEmail = getIntent().getStringExtra();
		mEmailView = (EditText) findViewById(R.id.email);
		mEmailView.setText(mEmail);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptLogin();
							return true;
						}
						return false;
					}
				});

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});
		
		findViewById(R.id.sign_up_text).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Constants.logMessage(1,"sign_up_text_onClick","Switching to sign up activity");
						
						Intent i = new Intent(getApplicationContext(), SignUpActivity.class);
						mEmail = mEmailView.getText().toString();
						i.putExtra("email", mEmail);
						startActivityForResult(i, 1);
					}
				});
		
		findViewById(R.id.forgot_password_text).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						lostPassword();
					}
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		
		MenuItem item = menu.findItem(R.id.action_forgot_password);

	    if (item != null) {
		    item.setOnMenuItemClickListener
		    (
		        new MenuItem.OnMenuItemClickListener() { 
		            public boolean onMenuItemClick(MenuItem item) { 
		            	lostPassword(); 
		            	return true;
		            }
		        }
		    );
	    }
	    	
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
     switch (item.getItemId()) {
        case R.id.menu_legalnotices:
         String LicenseInfo = GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(
           getApplicationContext());
         AlertDialog.Builder LicenseDialog = new AlertDialog.Builder(this);
         LicenseDialog.setTitle("Legal Notices");
         LicenseDialog.setMessage(LicenseInfo);
         LicenseDialog.show();
            return true;
        }
     return super.onOptionsItemSelected(item);
    }
	
	private void lostPassword() {
		Intent i = new Intent(Intent.ACTION_VIEW, 
		       	Uri.parse("https://hereyak.com/users/password/new"));
		startActivity(i);
	}

	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 6) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		} else if (!mEmail.contains("@")) {
			mEmailView.setError(getString(R.string.error_invalid_email));
			focusView = mEmailView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			mAuthTask = new UserLoginTask();
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

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
	 * Represents an asynchronous login task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, Map<String,Object>> {
		@Override
		protected Map<String,Object> doInBackground(Void... params) {
	
			try {
				YakHttpClient yakClient = new YakHttpClient("sign_in");
				Map<String,Object> ret = new HashMap<String, Object>();
				ret = yakClient.sign_in(mEmail, mPassword);
				Constants.logMessage(1,"UserLoginTask", ret.toString());
				return ret;
				
			} catch (MalformedURLException e) {
				Constants.logMessage(2,"UserLoginTask",e.getMessage());
			} catch (IOException e) {
				Constants.logMessage(2,"UserLoginTask",e.getMessage());
			} catch (Exception e) {
				Constants.logMessage(2,"UserLoginTask",e.getMessage());			
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

				Context context = getApplicationContext();
				CharSequence text = getString(R.string.error_incorrect_credentials);
				
				if ((ret != null) && (Integer.parseInt((String)ret.get("status")) == 2)){
					text = getString(R.string.error_server);
				}
				int duration = Toast.LENGTH_SHORT;

				Toast toast = Toast.makeText(context, text, duration);
				toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
				toast.show();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}

	}
}
