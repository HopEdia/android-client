package io.github.hopedia;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import java.util.Date;

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
class GetToken {
	String email;
	String password;
	public GetToken(String email, String password){
		this.email = email;
		this.password = password;
	}
}
class Login {
	String token;
	public Login(String token){
		this.token = token;
	}
}
public class AccountManager {
	TinyDB tinyDB;
	Context ctx;
	public Post.TaskListener tl;

	public AccountManager(Context ctx) {
		tinyDB = new TinyDB(ctx);
		this.ctx = ctx;
	}

	public void getToken(String email, String password, final Post.TaskListener taskListener) {
		GetToken content = new GetToken(email, password);
		Post.Args args = new Post.Args(ctx.getString(R.string.server_url)+"/getToken", content, String.class, ctx.getResources().getInteger(R.integer.timeout));
		Post post = new Post(ctx, new Post.TaskListener() {
			@Override
			public void onFinished(Post.Result result) {
				if(!result.status) {
					//error
					taskListener.onFinished(result);
				}
				else {
					if (result.content != null) {
						tinyDB.putString("token", (String) result.content);
						tinyDB.putLong("tokenDate", new Date().getTime());
						taskListener.onFinished(new Post.Result(null, true));
					}
					else {
						taskListener.onFinished(new Post.Result(null, true));
					}
				}
			}
		});
		post.execute(args);
	}

	public void login(Post.TaskListener taskListener) {
		if(tinyDB.getLong("tokenDate", 0) != 0) {
			//token exist
			if (tinyDB.getString("connectSid").length() != 0) {
					if (diffDate(new Date(tinyDB.getLong("connectSidDate", 0))) < 1) {
						//session exist & is valid
						taskListener.onFinished(new Post.Result(null, true));
					} else {
						//sessions exists, but isn't valid -> regenerate
						loginServer(taskListener);
					}

			}
			else {
				//create session
				loginServer(taskListener);
			}
			}
		else {
			//token doesn't exist
			auth(taskListener);
		}
	}
	private void auth(Post.TaskListener taskListener) {
		Intent intent = new Intent(ctx, LoginActivity.class);
		if(tinyDB.getString("email").length() != 0) {
			intent.putExtra("email", tinyDB.getString("email"));
		}
		//set as member -> must be called by BaseActivity onActivityResult
		tl = taskListener;
		((Activity) ctx).startActivityForResult(intent, Application.LOGIN_INTENT);
	}

	public void loginServer(final Post.TaskListener taskListener) {
		String token = tinyDB.getString("token");
			long days = diffDate(new Date(tinyDB.getLong("tokenDate", 0)));
			if (token.length() != 0 && days <= 27) {
				//token exists & is valid -> get session
				Post.Args args = new Post.Args(ctx.getString(R.string.server_url) + "/login", new Login(token), String.class, ctx.getResources().getInteger(R.integer.timeout), true);
				Post post = new Post(ctx, new Post.TaskListener<String>() {
					@Override
					public void onFinished(Post.Result<String> result) {
						if (result.status) {
							String connectSid = result.cookies.get(0);
							if (connectSid.length() != 0) {
								tinyDB.putString("connectSid", connectSid);
								tinyDB.putLong("connectSidDate", new Date().getTime());
								if(!result.content.equals("OK")) {
									//Not a status -> new token send
									tinyDB.putString("token", result.content);
									tinyDB.putLong("tokenDate", new Date().getTime());
								}
								taskListener.onFinished(new Post.Result(null, true));
								return;
							} else {
								taskListener.onFinished(new Post.Result(null, false));
								return;
							}
						} else {
							//error
							taskListener.onFinished(new Post.Result(null, false));
							return;
						}
					}
				});
				post.execute(args);
			} else {
				//login
				auth(taskListener);
			}
		}

	public String getSessionCookie() {
		String cookie = tinyDB.getString("connectSid");
			if(diffDate(new Date(tinyDB.getLong("connectSidDate", 0))) < 1) {
				return cookie;
			}
			else {
				return null;
			}
	}

	private long diffDate(Date issue) {
		//TODO
		long diff =  new Date().getTime() - issue.getTime();
		return diff / (24 * 60 * 60 * 1000);
	}
}
