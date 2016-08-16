package com.coolweather.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coolweather.app.R;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

public class WeatherActivity extends Activity implements OnClickListener {
	private LinearLayout weatherInfoLayout;
	/*
	 * 用于显示城市名
	 */
	private TextView cityNameText;
	/*
	 * 用于显示发布时间
	 */
	private TextView publishText;
	/*
	 * 用于显示天气描述信息
	 */
	private TextView weatherDespText;
	/*
	 * 用于显示气温1和2
	 */
	private TextView temp1Text, temp2View;
	/*
	 * 用于显示当前日期
	 */
	private TextView currentDateText;
	/*
	 * 选择城市，更新天气信息
	 */
	private Button switchCity, refreshWeather;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		// 初始化各个控件
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		cityNameText = (TextView) findViewById(R.id.city_name);
		publishText = (TextView) findViewById(R.id.publish_text);
		weatherDespText = (TextView) findViewById(R.id.weather_desp);
		temp1Text = (TextView) findViewById(R.id.temp1);
		temp2View = (TextView) findViewById(R.id.temp2);

		currentDateText = (TextView) findViewById(R.id.current_date);
		String countyCode = getIntent().getStringExtra("county_code");
		
		
		switchCity=(Button)findViewById(R.id.switch_city);
		refreshWeather=(Button)findViewById(R.id.refresh_weather);
		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);
		
 
		if (!TextUtils.isEmpty(countyCode)) {
			// 有县级代号就去查天气
			publishText.setText("同步中...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		} else {
			// 没有县级代号就直接显示本地天气
			showWeather();
		}
	}

	/*
	 * 查询县级代号所对应的天气代号
	 */

	private void queryWeatherCode(String countyCode) {
		String address = "http://www.weather.com.cn/data/list3/city"
				+ countyCode + ".xml";
		queryFormServer(address, "countyCode");

	}

	/*
	 * 查询天气代号所对应的天气
	 */
	protected void queryWeatherInfo(String weatherCode) {
		String address = "http://www.weather.com.cn/data/cityinfo/"
				+ weatherCode + ".html";
		queryFormServer(address, "weatherCode");

	}

	/*
	 * 根据传入的地址和类型去向服务器查询天气代号或者天气信息
	 */
	private void queryFormServer(final String address, final String type) {

		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {

			@Override
			public void onFinish(final String response) {
				if ("countyCode".equals(type)) {
					if (!TextUtils.isEmpty(response)) {
						// 从服务器返回的数据解析出天气代号
						String[] array = response.split("\\|");
						if (array != null && array.length == 2) {
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				} else if ("weatherCode".equals(type)) {
					// 处理服务器返回的天气信息
					Utility.handleWeatherResponse(WeatherActivity.this,
							response);
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							showWeather();

						}
					});
				}

			}

			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						publishText.setText("同步失败！");

					}
				});

			}
		});

	}

	/*
	 * 从SharePreferences文件读取存储天气信息，并显示到界面
	 */
	private void showWeather() {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);

		cityNameText.setText(pref.getString("city_name", ""));
		temp1Text.setText(pref.getString("temp1", ""));
		temp2View.setText(pref.getString("temp2", ""));
		weatherDespText.setText(pref.getString("weather_desp", ""));

		publishText.setText("今天" + pref.getString("publish_time", "") + "发布");
		currentDateText.setText(pref.getString("current_date", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.switch_city:
			Intent intent=new Intent(this,ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			
			
			break;
		case R.id.refresh_weather:
			publishText.setText("同步中...");
			SharedPreferences pref=PreferenceManager.getDefaultSharedPreferences(this);
			String weatherCode=pref.getString("weather_code", "");
			if(!TextUtils.isEmpty(weatherCode)){
				queryWeatherInfo(weatherCode);
			}
			
			break;
			
		default:
			break;
		}
		
	}



}

	


