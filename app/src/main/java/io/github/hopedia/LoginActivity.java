package io.github.hopedia;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
/** Hop Edia android client
 Copyright (C) 2016  koko-ng <koko.fr.mu@gmail.com>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class LoginActivity extends BaseActivity {

	private EditText emailField;
	private EditText passwordField;
	private TextView loginFailed;
	private Button loginButton;
	private Button signupButton;
	private TextView signupBegin;
	private AccountManager accountManager;
	private TextView signupSuccess;
	private TinyDB tinyDb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setLayout(R.layout.activity_login);

		accountManager = new AccountManager(LoginActivity.this);
		emailField = (EditText) findViewById(R.id.email);
		passwordField = (EditText) findViewById(R.id.password);
		loginButton = (Button) findViewById(R.id.login);
		signupButton = (Button) findViewById(R.id.signup);
		loginFailed = (TextView) findViewById(R.id.login_failed);
		signupSuccess = (TextView) findViewById(R.id.signup_success);
		signupBegin = (TextView) findViewById(R.id.begin_signup);

		tinyDb = new TinyDB(LoginActivity.this);
		emailField.setText(tinyDb.getString("email"));

		InputFilter[] lengthFilter = new InputFilter[1];
		lengthFilter[0] = new InputFilter.LengthFilter(25);
		//lengthFilter[1] = new InputFilter.

		loginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				doLogin(emailField.getText().toString(), passwordField.getText().toString());
			}
		});
		signupButton.setOnClickListener(new View.OnClickListener() {
			class Signup {
				public String email;
				public String username;
				public String password;
			}
			@Override
			public void onClick(View v) {
				loginFailed.setVisibility(View.GONE);
				Signup signup = new Signup();
				signup.email=emailField.getText().toString();
				signup.password=passwordField.getText().toString();
				Post p = new Post(LoginActivity.this, new Post.TaskListener() {
					@Override
					public void onFinished(Post.Result result) {
						if(result.content.equals("OK")) {
							//signup success
							//doLogin(emailField.getText().toString(), passwordField.getText().toString());
							loginButton.setVisibility(View.VISIBLE);
							signupButton.setVisibility(View.GONE);
							loginFailed.setText(getString(R.string.loginFailed));
							signupSuccess.setVisibility(View.VISIBLE);
							tinyDb.putString("email", emailField.getText().toString());
						}
						else{
							loginFailed.setText(getString(R.string.signup_failed));
							loginFailed.setVisibility(View.VISIBLE);
						}
					}
				});
				p.execute(new Post.Args(getResources().getString(R.string.server_url)+"/signup", signup, String.class, getResources().getInteger(R.integer.timeout)));
			}
		});
		signupBegin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loginButton.setVisibility(View.GONE);
				signupButton.setVisibility(View.VISIBLE);
				signupBegin.setVisibility(View.GONE);
			}
		});
	}
	private void doLogin(final String email, String password) {
		accountManager.getToken(email, password, new Post.TaskListener() {
			@Override
			public void onFinished(Post.Result result) {
				if(result.status) {
					Log.e("LOG", "Logged in");
					Intent intent = new Intent();
					intent.putExtra("email", email);
					tinyDb.putString("email", email);
					setResult(RESULT_OK,intent);
					//end the login & finish
					accountManager.loginServer(new Post.TaskListener() {
						@Override
						public void onFinished(Post.Result result) {
							if(result.status) {
								finish();
								return;
							}
							else {
								//token login failed
								loginFailed.setVisibility(View.VISIBLE);
								return;
							}
						}
					});
				}
				else {
					if(NetRequest.NETWORK_ERROR==result.content || NetRequest.NETWORK_TIMEOUT==result.content)
						Toast.makeText(LoginActivity.this, R.string.network_error, Toast.LENGTH_LONG).show();
					else
						loginFailed.setVisibility(View.VISIBLE);
				}
			}
		});
	}
}
