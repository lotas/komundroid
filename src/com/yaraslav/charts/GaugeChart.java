/**
 * Copyright (C) 2009, 2010 SC 4ViewSoft SRL
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yaraslav.charts;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import com.yaraslav.DbAdapter;
import com.yaraslav.KomundroidApplication;
import com.yaraslav.R;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;

/**
 * Sales growth demo chart.
 */
public class GaugeChart extends AbstractChart {
	/**
	 * Returns the chart name.
	 * 
	 * @return the chart name
	 */
	public String getName() {
		return "Gauge usage graph";
	}

	/**
	 * Returns the chart description.
	 * 
	 * @return the chart description
	 */
	public String getDesc() {
		return "Last 12 month data";
	}

	/**
	 * Executes the chart demo.
	 * 
	 * @param context
	 *            the context
	 * @return the built intent
	 */
	public Intent execute(Context context, long gaugeId, int gaugeTypeId) 
	{
		Integer[] mGaugeColorMap = KomundroidApplication.instance.gaugeColorMap;
		String mGaugeTitle = KomundroidApplication.instance.meterTitles[(int) gaugeId - 1];
		
		Cursor c = KomundroidApplication.instance.dbAdapter.getGaugeDataItemsCursor(gaugeId, "ASC");
		
		List<Date[]> dates = new ArrayList<Date[]>();
		List<double[]> values = new ArrayList<double[]>();
		
		Date firstdt = null, lastdt = null;
		
		String[] titles;
		
		Date[] dateValues = new Date[c.getCount()];
		double[] diffValues = new double[c.getCount()];
		
		double min = 99999, max = -99999;

		if ((c.getCount() > 0) && c.moveToFirst()) 
		{
			
			int i = 0;
			float prevValue = 0;
			
			do 
			{
				float value  = c.getFloat(c.getColumnIndex(DbAdapter.KEY_VALUE));
				Date dt      = new Date(c.getLong(c.getColumnIndex(DbAdapter.KEY_DT)));
				
				if (i == 0)
				{
					firstdt = dt;
				}
				else if (i == c.getCount()-1)
				{
					lastdt = dt;
				}
				
				dateValues[i] = dt;
				diffValues[i] = prevValue > 0 ? (value-prevValue) : 0;
				
				if (diffValues[i] > max) max = diffValues[i];
				if (diffValues[i] < min) min = diffValues[i];
							
				prevValue = value;
				i++;
			} 
			while (c.moveToNext());
			
			dates.add(dateValues);
			values.add(diffValues);
		}
		
		if (firstdt != null && lastdt != null)
		{
			titles = new String[] { mGaugeTitle + " " +  firstdt.getMonth() + "/" + (firstdt.getYear()+1900) + " - " + lastdt.getMonth() + "/" + (lastdt.getYear()+1900) };
		}
		else
		{
			titles = new String[] { mGaugeTitle };
		}
		
		
		int[] colors = new int[] { mGaugeColorMap[gaugeTypeId] };
		
		PointStyle[] styles = new PointStyle[] { PointStyle.TRIANGLE };
		
		
		XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
		
		setChartSettings(renderer, mGaugeTitle, context.getString(R.string.chart_date), context.getString(R.string.chart_consumption), dateValues[0].getTime(), dateValues[dateValues.length - 1].getTime(), 
				Math.round(min)-1, Math.round(max)+1, Color.GRAY, Color.LTGRAY);
		
		renderer.setYLabels(10);
		
		return ChartFactory.getTimeChartIntent(context, buildDateDataset(titles, dates, values), renderer, "MMM yy");
	}

}
